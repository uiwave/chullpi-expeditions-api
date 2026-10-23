-- ==========================================
-- 1. ROLES Y PERMISOS
-- ==========================================

CREATE TABLE roles (
    id_rol SERIAL PRIMARY KEY,
    descripcion VARCHAR(100) NOT NULL UNIQUE,
    fecha_registro TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE permisos (
    id_permiso SERIAL PRIMARY KEY,
    id_rol INT NOT NULL,
    nombre_menu VARCHAR(100) NOT NULL,
    fecha_registro TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_permiso_rol FOREIGN KEY (id_rol)
        REFERENCES roles(id_rol) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT uq_rol_menu UNIQUE (id_rol, nombre_menu)
);

-- ==========================================
-- 2. USUARIOS, CLIENTES Y PROVEEDORES
-- ==========================================

CREATE TABLE usuarios (
    id_usuario SERIAL PRIMARY KEY,
    documento VARCHAR(20) UNIQUE,
    nombre_completo VARCHAR(150) NOT NULL,
    correo VARCHAR(100) NOT NULL UNIQUE,
    clave VARCHAR(255),
    google_id VARCHAR(64) UNIQUE,
    id_rol INT NOT NULL,
    estado BOOLEAN NOT NULL DEFAULT true,
    fecha_registro TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_usuario_rol FOREIGN KEY (id_rol)
        REFERENCES roles(id_rol) ON DELETE RESTRICT ON UPDATE CASCADE
);

CREATE TABLE clientes (
    id_cliente SERIAL PRIMARY KEY,
    documento VARCHAR(20) NOT NULL UNIQUE,
    nombre_completo VARCHAR(150) NOT NULL,
    correo VARCHAR(100),
    telefono VARCHAR(20),
    estado BOOLEAN NOT NULL DEFAULT true,
    fecha_registro TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE proveedores (
    id_proveedor SERIAL PRIMARY KEY,
    documento VARCHAR(20) NOT NULL UNIQUE,
    razon_social VARCHAR(150) NOT NULL,
    correo VARCHAR(100),
    telefono VARCHAR(20),
    estado BOOLEAN NOT NULL DEFAULT true,
    fecha_registro TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

-- ==========================================
-- 3. CATALOGO DE PRODUCTOS
-- ==========================================

CREATE TABLE categorias (
    id_categoria SERIAL PRIMARY KEY,
    descripcion VARCHAR(100) NOT NULL UNIQUE,
    estado BOOLEAN NOT NULL DEFAULT true,
    fecha_registro TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE productos (
    id_producto BIGSERIAL PRIMARY KEY,
    codigo VARCHAR(50) NOT NULL UNIQUE,
    nombre VARCHAR(150) NOT NULL,
    descripcion TEXT,
    id_categoria INT NOT NULL,
    stock INT NOT NULL DEFAULT 0 CHECK (stock >= 0),
    precio_compra NUMERIC(12, 2) NOT NULL DEFAULT 0.00 CHECK (precio_compra >= 0),
    precio_venta NUMERIC(12, 2) NOT NULL DEFAULT 0.00 CHECK (precio_venta >= 0),
    estado BOOLEAN NOT NULL DEFAULT true,
    fecha_registro TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_producto_categoria FOREIGN KEY (id_categoria)
        REFERENCES categorias(id_categoria) ON DELETE RESTRICT ON UPDATE CASCADE
);

-- ==========================================
-- 4. TRANSACCIONES: COMPRAS
-- ==========================================

CREATE TABLE compras (
    id_compra BIGSERIAL PRIMARY KEY,
    id_usuario INT NOT NULL,
    id_proveedor INT NOT NULL,
    tipo_documento VARCHAR(50) NOT NULL,
    numero_documento VARCHAR(50) NOT NULL,
    monto_total NUMERIC(12, 2) NOT NULL DEFAULT 0.00 CHECK (monto_total >= 0),
    fecha_registro TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_compra_usuario FOREIGN KEY (id_usuario)
        REFERENCES usuarios(id_usuario) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_compra_proveedor FOREIGN KEY (id_proveedor)
        REFERENCES proveedores(id_proveedor) ON DELETE RESTRICT ON UPDATE CASCADE
);

CREATE TABLE detalle_compras (
    id_detalle_compra BIGSERIAL PRIMARY KEY,
    id_compra BIGINT NOT NULL,
    id_producto BIGINT NOT NULL,
    precio_compra NUMERIC(12, 2) NOT NULL CHECK (precio_compra >= 0),
    precio_venta NUMERIC(12, 2) NOT NULL CHECK (precio_venta >= 0),
    cantidad INT NOT NULL CHECK (cantidad > 0),
    monto_total NUMERIC(12, 2) NOT NULL CHECK (monto_total >= 0),
    fecha_registro TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_detalle_compra_cabecera FOREIGN KEY (id_compra)
        REFERENCES compras(id_compra) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_detalle_compra_producto FOREIGN KEY (id_producto)
        REFERENCES productos(id_producto) ON DELETE RESTRICT ON UPDATE CASCADE
);

-- ==========================================
-- 5. TRANSACCIONES: VENTAS
-- ==========================================

CREATE TABLE ventas (
    id_venta BIGSERIAL PRIMARY KEY,
    id_usuario INT NOT NULL,
    id_cliente INT,
    tipo_documento VARCHAR(50) NOT NULL,
    numero_documento VARCHAR(50) NOT NULL,
    documento_cliente VARCHAR(20),
    nombre_cliente VARCHAR(150),
    monto_pago NUMERIC(12, 2) NOT NULL DEFAULT 0.00 CHECK (monto_pago >= 0),
    monto_cambio NUMERIC(12, 2) NOT NULL DEFAULT 0.00 CHECK (monto_cambio >= 0),
    monto_total NUMERIC(12, 2) NOT NULL DEFAULT 0.00 CHECK (monto_total >= 0),
    fecha_registro TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_venta_usuario FOREIGN KEY (id_usuario)
        REFERENCES usuarios(id_usuario) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_venta_cliente FOREIGN KEY (id_cliente)
        REFERENCES clientes(id_cliente) ON DELETE SET NULL ON UPDATE CASCADE
);

CREATE TABLE detalle_ventas (
    id_detalle_venta BIGSERIAL PRIMARY KEY,
    id_venta BIGINT NOT NULL,
    id_producto BIGINT NOT NULL,
    precio_venta NUMERIC(12, 2) NOT NULL CHECK (precio_venta >= 0),
    cantidad INT NOT NULL CHECK (cantidad > 0),
    sub_total NUMERIC(12, 2) NOT NULL CHECK (sub_total >= 0),
    fecha_registro TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_detalle_venta_cabecera FOREIGN KEY (id_venta)
        REFERENCES ventas(id_venta) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_detalle_venta_producto FOREIGN KEY (id_producto)
        REFERENCES productos(id_producto) ON DELETE RESTRICT ON UPDATE CASCADE
);

-- ==========================================
-- 6. INDICES DE RENDIMIENTO (FOREIGN KEYS & SEARCHES)
-- ==========================================

CREATE INDEX idx_usuarios_rol ON usuarios(id_rol);
CREATE INDEX idx_permisos_rol ON permisos(id_rol);

CREATE INDEX idx_productos_categoria ON productos(id_categoria);
CREATE INDEX idx_productos_codigo ON productos(codigo);

CREATE INDEX idx_compras_usuario ON compras(id_usuario);
CREATE INDEX idx_compras_proveedor ON compras(id_proveedor);
CREATE INDEX idx_detalle_compras_compra ON detalle_compras(id_compra);
CREATE INDEX idx_detalle_compras_producto ON detalle_compras(id_producto);

CREATE INDEX idx_ventas_usuario ON ventas(id_usuario);
CREATE INDEX idx_ventas_cliente ON ventas(id_cliente);
CREATE INDEX idx_detalle_ventas_venta ON detalle_ventas(id_venta);
CREATE INDEX idx_detalle_ventas_producto ON detalle_ventas(id_producto);
