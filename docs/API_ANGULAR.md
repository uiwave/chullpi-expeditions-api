# Guía de integración API — Chullpi Expeditions

Documento para el equipo de frontend (Angular). Describe autenticación, roles, endpoints y reglas de negocio.

## 1. Información general

| Dato | Valor |
|---|---|
| Base URL (desarrollo) | `http://localhost:8080` |
| Swagger UI | `http://localhost:8080/swagger-ui/index.html` |
| OpenAPI JSON | `http://localhost:8080/v3/api-docs` |
| CORS permitido (por defecto) | `http://localhost:4200` (configurable con `CORS_ALLOWED_ORIGINS`) |
| Formato de fechas | ISO 8601 con zona horaria, ej. `2026-10-23T00:42:28.236112-05:00` |
| Moneda | Soles (`S/`), valores numéricos con 2 decimales |

Todas las respuestas son JSON. Las listas están paginadas (ver sección 5).

---

## 2. Autenticación

### 2.1 Resumen del flujo

El backend **emite su propio JWT (HS256)**. Angular nunca usa los tokens de Google para llamar a la API: solo los intercambia por el JWT propio.

```
[Angular] --(correo/clave o idToken de Google)--> [Backend] --(JWT propio)--> [Angular]
[Angular] --(Authorization: Bearer <JWT>)--> [Backend API]
```

- **Login local**: `POST /api/auth/login` con correo y clave.
- **Login Google**: Angular obtiene el `idToken` con Google Identity Services (GIS) y lo envía a `POST /api/auth/google`.
- En ambos casos la respuesta es idéntica (ver 2.4), y el token se envía en cada petición protegida en el header `Authorization: Bearer <token>`.

### 2.2 Login local

`POST /api/auth/login`

```json
{
  "correo": "admin@chullpi.com",
  "clave": "Admin123!"
}
```

Respuesta `200`:

```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "tokenType": "Bearer",
  "expiresIn": 3600,
  "usuario": {
    "id": 1,
    "documento": null,
    "nombreCompleto": "Administrador Chullpi",
    "correo": "admin@chullpi.com",
    "rol": "ADMIN",
    "idRol": 1,
    "menus": ["usuarios","roles","permisos","clientes","proveedores","categorias","productos","compras","ventas"],
    "estado": true,
    "fechaRegistro": "2026-10-22T20:00:00-05:00"
  }
}
```

Errores:
- `400` si el body no pasa validación (correo con formato inválido, campos vacíos).
- `401` `{"message": "Credenciales inválidas"}` si el correo no existe o la clave es incorrecta.
- `403` `{"message": "El usuario está deshabilitado"}` si `estado = false`.

### 2.3 Login con Google (Google Identity Services)

**Requisito en Google Cloud Console**: en el OAuth Client ID (tipo *Web application*) agregar los orígenes JavaScript autorizados:
- `http://localhost:4200` (desarrollo)
- el dominio de producción (ej. `https://app.chullpi.com`)

No se necesita Redirect URI porque el backend no redirige: Angular obtiene el `idToken` directamente con GIS y lo intercambia por el JWT propio.

Flujo:

1. Cargar el script de GIS en `index.html`:

```html
<script src="https://accounts.google.com/gsi/client" async defer></script>
```

2. Renderizar el botón de Google y capturar el `credential` (que es el `idToken`):

```ts
// google-login.component.ts
declare const google: any;

ngAfterViewInit(): void {
  google.accounts.id.initialize({
    client_id: environment.googleClientId, // mismo CLIENT_ID del backend
    callback: (response: { credential: string }) => this.loginConGoogle(response.credential)
  });
  google.accounts.id.renderButton(
    document.getElementById('google-btn'),
    { theme: 'outline', size: 'large', text: 'signin_with' }
  );
}

loginConGoogle(idToken: string): void {
  this.http.post<AuthRes>(`${environment.apiUrl}/api/auth/google`, { idToken })
    .subscribe(res => this.authService.guardarSesion(res));
}
```

3. El backend valida el `idToken` (firma, audiencia = `CLIENT_ID`, emisor Google, correo verificado) y responde igual que el login local.

Comportamiento del backend con usuarios de Google:
- Si el `google_id` o el correo ya existen → inicia sesión y vincula la cuenta.
- Si el correo no existe → **crea el usuario automáticamente con rol `INVITADO`** (sin menús). El usuario verá una pantalla de "cuenta pendiente de habilitación" hasta que un administrador le asigne rol.
- Si el token es inválido o expiró → `401` `{"message": "El token de Google es inválido o expiró"}`.
- Si el correo de Google no está verificado → `401`.

### 2.4 Sesión en Angular

Estructura de la respuesta de login (`AuthRes`):

```ts
interface AuthRes {
  token: string;
  tokenType: 'Bearer';
  expiresIn: number; // segundos
  usuario: UsuarioRes;
}

interface UsuarioRes {
  id: number;
  documento: string | null;
  nombreCompleto: string;
  correo: string;
  rol: string;        // ADMIN | VENDEDOR | ALMACENERO | INVITADO
  idRol: number;
  menus: string[];    // menús habilitados para el rol
  estado: boolean;
  fechaRegistro: string;
}
```

Claims del JWT (por si se necesita decodificar):

| Claim | Descripción |
|---|---|
| `sub` | id del usuario |
| `correo` | correo |
| `nombre` | nombre completo |
| `role` | nombre del rol (sin prefijo) |
| `menus` | array de menús habilitados |
| `iss` | `chullpi-expeditions-api` |
| `exp` | expiración (por defecto 60 min, configurable con `JWT_EXPIRATION_MINUTES`) |

**Recomendación**: guardar `token` + `usuario` (localStorage o memoria) y construir el sidebar **desde `usuario.menus`**, no desde el rol hardcodeado; así los cambios de permisos que haga un admin se reflejan al volver a iniciar sesión.

Interceptor sugerido:

```ts
// auth.interceptor.ts
export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const token = localStorage.getItem('token');
  const isAuthEndpoint = req.url.includes('/api/auth/login') || req.url.includes('/api/auth/google');
  if (token && !isAuthEndpoint) {
    req = req.clone({ setHeaders: { Authorization: `Bearer ${token}` } });
  }
  return next(req).pipe(
    catchError((err: HttpErrorResponse) => {
      if (err.status === 401) {
        localStorage.removeItem('token');
        localStorage.removeItem('usuario');
        // redirigir a /login
      }
      return throwError(() => err);
    })
  );
};
```

- **401** → token ausente/vencido/inválido: cerrar sesión y volver al login.
- **403** → autenticado sin permisos: mostrar aviso "No tienes permisos para esta operación". Conviene un `roleGuard`/`menuGuard` que compare la ruta requerida contra `usuario.menus`.

### 2.5 Perfil propio

| Método | Endpoint | Descripción |
|---|---|---|
| GET | `/api/auth/me` | Devuelve `UsuarioRes` del usuario autenticado |
| PUT | `/api/auth/me` | Actualiza `documento` y `nombreCompleto` (opcionales) |

```json
// PUT /api/auth/me
{
  "documento": "45678912",
  "nombreCompleto": "Juan Pérez Gómez"
}
```

---

## 3. Roles y permisos

Roles de fábrica (seed `V2__seed_roles_permisos.sql`):

| Rol | Menús (`usuario.menus`) |
|---|---|
| ADMIN | usuarios, roles, permisos, clientes, proveedores, categorias, productos, compras, ventas |
| VENDEDOR | clientes, productos, ventas |
| ALMACENERO | categorias, proveedores, productos, compras |
| INVITADO | (ninguno) |

Acceso técnico por endpoint (aplicado en backend, `403` si no cumple):

| Módulo | ADMIN | VENDEDOR | ALMACENERO | INVITADO |
|---|---|---|---|---|
| Usuarios / Roles / Permisos | ✅ | ❌ | ❌ | ❌ |
| Clientes | ✅ | ✅ | ❌ | ❌ |
| Proveedores | ✅ | ❌ | ✅ | ❌ |
| Categorías | ✅ | ❌ | ✅ | ❌ |
| Productos (leer) | ✅ | ✅ | ✅ | ❌ |
| Productos (crear/editar/eliminar) | ✅ | ❌ | ✅ | ❌ |
| Compras | ✅ | ❌ | ✅ | ❌ |
| Ventas | ✅ | ✅ | ❌ | ❌ |
| `/api/auth/me` | ✅ | ✅ | ✅ | ✅ |

> Nota: los controllers validan por **rol** (`ROLE_*`), mientras `menus` se usa para la UI. Un admin puede modificar los menús de un rol (`POST/PUT /api/roles`), pero eso **no** cambia los permisos técnicos de los endpoints.

---

## 4. Endpoints

### 4.1 Usuarios (solo ADMIN)

| Método | Ruta | Body | Respuesta |
|---|---|---|---|
| GET | `/api/usuarios?page=0&size=10` | — | `PageRes<UsuarioRes>` |
| GET | `/api/usuarios/{id}` | — | `UsuarioRes` |
| POST | `/api/usuarios` | `UsuarioReq` | `201 UsuarioRes` |
| PUT | `/api/usuarios/{id}` | `UsuarioUpdateReq` | `UsuarioRes` |
| DELETE | `/api/usuarios/{id}` | — | `204` |

```json
// POST /api/usuarios
{
  "documento": "77889900",
  "nombreCompleto": "Vendedor Uno",
  "correo": "vendedor@chullpi.com",
  "clave": "Vende123!",
  "idRol": 2,
  "estado": true
}
```

`PUT` es parcial: solo actualiza los campos enviados; `clave` es opcional (si se envía, se re-hashea). `DELETE` falla con `409` si intentas eliminar tu propio usuario.

### 4.2 Roles (solo ADMIN)

| Método | Ruta | Body | Respuesta |
|---|---|---|---|
| GET | `/api/roles?page=0&size=10` | — | `PageRes<RoleRes>` |
| GET | `/api/roles/{id}` | — | `RoleRes` |
| POST | `/api/roles` | `RoleReq` | `201 RoleRes` |
| PUT | `/api/roles/{id}` | `RoleReq` | `RoleRes` |
| DELETE | `/api/roles/{id}` | — | `204` |

```json
// POST /api/roles  (menús válidos, ver MenuNames)
{
  "descripcion": "CAJERO",
  "menus": ["clientes", "productos", "ventas"]
}
```

`PUT` reemplaza el set completo de menús. `DELETE` falla con `409` si el rol tiene usuarios asignados.

### 4.3 Permisos (solo ADMIN)

| Método | Ruta | Body | Respuesta |
|---|---|---|---|
| GET | `/api/permisos?page=0&size=10` | — | `PageRes<PermisoRes>` |
| GET | `/api/permisos/rol/{idRol}` | — | `PermisoRes[]` |
| POST | `/api/permisos` | `PermisoReq` | `201 PermisoRes` |
| DELETE | `/api/permisos/{id}` | — | `204` |

```json
// POST /api/permisos
{ "idRol": 4, "nombreMenu": "productos" }
```

Menús válidos: `usuarios`, `roles`, `permisos`, `clientes`, `proveedores`, `categorias`, `productos`, `compras`, `ventas`. Un menú duplicado en el mismo rol devuelve `409`.

### 4.4 Clientes (ADMIN, VENDEDOR)

| Método | Ruta | Body | Respuesta |
|---|---|---|---|
| GET | `/api/clientes?page=0&size=10` | — | `PageRes<ClienteRes>` |
| GET | `/api/clientes/{id}` | — | `ClienteRes` |
| POST | `/api/clientes` | `ClienteReq` | `201 ClienteRes` |
| PUT | `/api/clientes/{id}` | `ClienteReq` | `ClienteRes` |
| DELETE | `/api/clientes/{id}` | — | `204` |

```json
// POST /api/clientes  (documento es opcional pero único)
{
  "documento": "45678912",
  "nombreCompleto": "Juan Pérez",
  "correo": "juan@gmail.com",
  "telefono": "999888777",
  "estado": true
}
```

### 4.5 Proveedores (ADMIN, ALMACENERO)

Igual que clientes pero con `razonSocial` en lugar de `nombreCompleto`:

| Método | Ruta |
|---|---|
| GET | `/api/proveedores?page=0&size=10` |
| GET | `/api/proveedores/{id}` |
| POST | `/api/proveedores` |
| PUT | `/api/proveedores/{id}` |
| DELETE | `/api/proveedores/{id}` |

```json
{
  "documento": "20555444",
  "razonSocial": "Distribuidora Norte SAC",
  "correo": "ventas@norte.pe",
  "telefono": "014567890",
  "estado": true
}
```

### 4.6 Categorías (ADMIN, ALMACENERO)

| Método | Ruta |
|---|---|
| GET | `/api/categorias?page=0&size=10` |
| GET | `/api/categorias/{id}` |
| POST | `/api/categorias` |
| PUT | `/api/categorias/{id}` |
| DELETE | `/api/categorias/{id}` |

```json
{ "descripcion": "Bebidas", "estado": true }
```

`DELETE` falla con `409` si la categoría tiene productos asociados.

### 4.7 Productos (leer: todos los roles de trabajo · escribir: ADMIN, ALMACENERO)

| Método | Ruta | Notas |
|---|---|---|
| GET | `/api/productos?page=0&size=10&idCategoria=1` | `idCategoria` opcional |
| GET | `/api/productos/{id}` | — |
| POST | `/api/productos` | solo ADMIN/ALMACENERO |
| PUT | `/api/productos/{id}` | solo ADMIN/ALMACENERO |
| DELETE | `/api/productos/{id}` | solo ADMIN/ALMACENERO |

```json
// POST /api/productos
{
  "codigo": "P001",
  "nombre": "Agua 500ml",
  "descripcion": "Botella de agua",
  "idCategoria": 1,
  "stock": 10,
  "precioCompra": 1.50,
  "precioVenta": 2.50,
  "estado": true
}
```

`codigo` es único (`409` si se repite). El `stock` normalmente se deja en `0` y se carga con compras (ver 4.8).

### 4.8 Compras (ADMIN, ALMACENERO) — aumentan stock

| Método | Ruta | Respuesta |
|---|---|---|
| GET | `/api/compras?page=0&size=10` | `PageRes<CompraRes>` (más recientes primero) |
| GET | `/api/compras/{id}` | `CompraRes` con detalle |
| POST | `/api/compras` | `201 CompraRes` |

```json
// POST /api/compras  (el usuario se toma del JWT, no se envía)
{
  "idProveedor": 1,
  "tipoDocumento": "FACTURA",
  "numeroDocumento": "F001-123",
  "detalles": [
    { "idProducto": 1, "precioCompra": 1.40, "precioVenta": 2.50, "cantidad": 5 }
  ]
}
```

Respuesta:

```json
{
  "id": 1,
  "idUsuario": 1,
  "usuario": "Administrador Chullpi",
  "idProveedor": 1,
  "proveedor": "Distribuidora Norte SAC",
  "tipoDocumento": "FACTURA",
  "numeroDocumento": "F001-123",
  "montoTotal": 7.00,
  "fechaRegistro": "2026-10-23T00:42:33.819826-05:00",
  "detalles": [
    { "idProducto": 1, "producto": "Agua 500ml", "precioCompra": 1.40, "precioVenta": 2.50, "cantidad": 5, "montoTotal": 7.00 }
  ]
}
```

Regla: cada detalle **actualiza** `precioCompra` y `precioVenta` del producto y **suma** `cantidad` al `stock`.

### 4.9 Ventas (ADMIN, VENDEDOR) — descuentan stock

| Método | Ruta | Respuesta |
|---|---|---|
| GET | `/api/ventas?page=0&size=10` | `PageRes<VentaRes>` (más recientes primero) |
| GET | `/api/ventas/{id}` | `VentaRes` con detalle |
| POST | `/api/ventas` | `201 VentaRes` |

```json
// POST /api/ventas  (el vendedor se toma del JWT)
{
  "idCliente": 1,
  "tipoDocumento": "BOLETA",
  "numeroDocumento": "B001-1",
  "montoPago": 10.00,
  "detalles": [
    { "idProducto": 1, "cantidad": 2 }
  ]
}
```

Si no quieres asociar cliente registrado, omite `idCliente` y envía `documentoCliente` y/o `nombreCliente` como texto (venta rápida):

```json
{
  "tipoDocumento": "BOLETA",
  "numeroDocumento": "B001-2",
  "documentoCliente": "12345678",
  "nombreCliente": "Cliente varios",
  "montoPago": 20.00,
  "detalles": [ { "idProducto": 1, "cantidad": 2 } ]
}
```

Respuesta:

```json
{
  "id": 1,
  "idUsuario": 1,
  "usuario": "Administrador Chullpi",
  "idCliente": 1,
  "cliente": "Juan Pérez",
  "tipoDocumento": "BOLETA",
  "numeroDocumento": "B001-1",
  "documentoCliente": "45678912",
  "nombreCliente": "Juan Pérez",
  "montoPago": 10.00,
  "montoCambio": 5.00,
  "montoTotal": 5.00,
  "fechaRegistro": "2026-10-23T00:42:33.918783-05:00",
  "detalles": [
    { "idProducto": 1, "producto": "Agua 500ml", "precioVenta": 2.50, "cantidad": 2, "subTotal": 5.00 }
  ]
}
```

**Importante (anti-manipulación)**: el cliente **no envía precios**. El backend usa `precioVenta` del producto. Errores posibles:
- `400` stock insuficiente: `"Stock insuficiente para 'Agua 500ml': disponible 13, solicitado 999"`.
- `400` pago insuficiente: `"El monto de pago (1.00) es menor al total (2.50)"`.
- En ambos casos la transacción se revierte completa (sin cambios de stock).

---

## 5. Paginación

Todas las listas (`/api/*` con paginación) devuelven el mismo sobre:

```json
{
  "content": [ ... ],
  "page": 0,
  "size": 10,
  "totalElements": 42,
  "totalPages": 5,
  "last": false
}
```

Query params: `page` (base 0) y `size` (máximo 100; si envías más, se limita a 100).

---

## 6. Errores

Formato estándar:

```json
{
  "timestamp": "2026-10-23T00:42:33.996415939",
  "status": 400,
  "error": "Bad Request",
  "message": "Stock insuficiente para 'Agua 500ml': disponible 13, solicitado 999"
}
```

Errores de validación (`400`) agregan `fields`:

```json
{
  "timestamp": "2026-10-23T00:42:42.244107413",
  "status": 400,
  "error": "Validación fallida",
  "fields": {
    "correo": "El correo no tiene un formato válido",
    "nombreCompleto": "El nombre completo es obligatorio"
  }
}
```

| Status | Cuándo | Qué mostrar en UI |
|---|---|---|
| 400 | Validación, stock insuficiente, pago insuficiente, JSON inválido | mensaje del campo o `message` |
| 401 | Sin token, token vencido, credenciales o token Google inválidos | redirigir a login |
| 403 | Rol sin permiso o usuario deshabilitado | aviso "sin permisos" |
| 404 | Recurso no encontrado | aviso o volver al listado |
| 409 | Documento/correo/código duplicado, recurso con dependencias, auto-eliminación | `message` |

---

## 7. Entorno local de pruebas

Credenciales del administrador creado automáticamente al arrancar (`app.admin.*` en `application.yaml`):

```
correo: admin@chullpi.com
clave:  Admin123!
```

Datos generados en pruebas locales (puedes usarlos como referencia): categoría `Bebidas`, producto `P001 Agua 500ml`, cliente `Juan Pérez`, proveedor `Distribuidora Norte SAC`, usuario `vendedor@chullpi.com / Vende123!` (rol VENDEDOR).

Para consumir la API desde otro dispositivo/emulador, ajusta `CORS_ALLOWED_ORIGINS` en el `.env` del backend (lista separada por comas) y la `baseUrl` en el `environment.ts` de Angular.
