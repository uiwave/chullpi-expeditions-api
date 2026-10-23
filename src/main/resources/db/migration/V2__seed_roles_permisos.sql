-- ==========================================
-- ROLES
-- ==========================================

INSERT INTO roles (descripcion) VALUES
    ('ADMIN'),
    ('VENDEDOR'),
    ('ALMACENERO'),
    ('INVITADO');

-- ==========================================
-- PERMISOS (MENUS) POR ROL
-- ==========================================

INSERT INTO permisos (id_rol, nombre_menu)
SELECT r.id_rol, m.menu
FROM roles r
CROSS JOIN (VALUES
    ('usuarios'),
    ('roles'),
    ('permisos'),
    ('clientes'),
    ('proveedores'),
    ('categorias'),
    ('productos'),
    ('compras'),
    ('ventas')
) AS m(menu)
WHERE r.descripcion = 'ADMIN';

INSERT INTO permisos (id_rol, nombre_menu)
SELECT r.id_rol, m.menu
FROM roles r
CROSS JOIN (VALUES
    ('clientes'),
    ('productos'),
    ('ventas')
) AS m(menu)
WHERE r.descripcion = 'VENDEDOR';

INSERT INTO permisos (id_rol, nombre_menu)
SELECT r.id_rol, m.menu
FROM roles r
CROSS JOIN (VALUES
    ('categorias'),
    ('proveedores'),
    ('productos'),
    ('compras')
) AS m(menu)
WHERE r.descripcion = 'ALMACENERO';

-- El rol INVITADO no tiene permisos: los usuarios que se registran
-- automaticamente con Google solo pueden consultar /api/auth/me hasta
-- que un administrador les asigne un rol.
