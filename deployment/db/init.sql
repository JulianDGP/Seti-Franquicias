-- Schema + search path
CREATE SCHEMA IF NOT EXISTS dbo;
SET
search_path TO dbo, public;

-- =========================
-- Tabla: franquicia
-- =========================
CREATE TABLE franquicia
(
    id             BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nombre         VARCHAR(120) NOT NULL,
    fecha_creacion TIMESTAMPTZ  NOT NULL DEFAULT now()
);

ALTER TABLE franquicia
    ADD CONSTRAINT uq_franquicia_nombre UNIQUE (nombre);

COMMENT
ON TABLE franquicia IS
'Catálogo de franquicias. Cada franquicia agrupa varias sucursales.';

COMMENT
ON COLUMN franquicia.id IS
'Identificador único de la franquicia (clave primaria).';

COMMENT
ON COLUMN franquicia.nombre IS
'Nombre de la franquicia. Debe ser único en todo el sistema.';

COMMENT
ON COLUMN franquicia.fecha_creacion IS
'Fecha y hora de creación del registro para trazabilidad básica.';


-- =========================
-- Tabla: sucursal
-- =========================
CREATE TABLE sucursal
(
    id             BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    franquicia_id  BIGINT       NOT NULL REFERENCES franquicia (id) ON DELETE CASCADE,
    nombre         VARCHAR(120) NOT NULL,
    fecha_creacion TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT uq_sucursal_por_franquicia UNIQUE (franquicia_id, nombre)
);

CREATE INDEX idx_sucursal_franquicia ON sucursal (franquicia_id);

COMMENT
ON TABLE sucursal IS
'Sucursal perteneciente a una franquicia. Contiene y oferta productos.';

COMMENT
ON COLUMN sucursal.id IS
'Identificador único de la sucursal (clave primaria).';

COMMENT
ON COLUMN sucursal.franquicia_id IS
'Referencia a la franquicia dueña de la sucursal. ON DELETE CASCADE elimina sus sucursales si se elimina la franquicia.';

COMMENT
ON COLUMN sucursal.nombre IS
'Nombre de la sucursal. Es único dentro de la misma franquicia.';

COMMENT
ON COLUMN sucursal.fecha_creacion IS
'Fecha y hora de creación del registro para trazabilidad básica.';


-- =========================
-- Tabla: producto
-- =========================
CREATE TABLE producto
(
    id             BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    sucursal_id    BIGINT       NOT NULL REFERENCES sucursal (id) ON DELETE CASCADE,
    nombre         VARCHAR(120) NOT NULL,
    stock          INTEGER      NOT NULL DEFAULT 0,
    fecha_creacion TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT ck_producto_stock_no_negativo CHECK (stock >= 0),
    CONSTRAINT uq_producto_por_sucursal UNIQUE (sucursal_id, nombre)
);

CREATE INDEX idx_producto_sucursal ON producto (sucursal_id);

COMMENT
ON TABLE producto IS
'Producto ofertado en una sucursal específica. El stock se gestiona por sucursal.';

COMMENT
ON COLUMN producto.id IS
'Identificador único del producto (clave primaria).';

COMMENT
ON COLUMN producto.sucursal_id IS
'Referencia a la sucursal donde se oferta el producto. ON DELETE CASCADE elimina los productos si se elimina la sucursal.';

COMMENT
ON COLUMN producto.nombre IS
'Nombre del producto dentro de la sucursal. Único por sucursal.';

COMMENT
ON COLUMN producto.stock IS
'Cantidad disponible en inventario para la sucursal. Restricción: stock >= 0.';

COMMENT
ON COLUMN producto.fecha_creacion IS
'Fecha y hora de creación del registro para trazabilidad básica.';


-- =========================
-- Vista de apoyo: producto con mayor stock por sucursal
-- Útil para el endpoint funcional #6.
-- =========================
CREATE
OR REPLACE VIEW v_producto_max_stock_por_sucursal AS
SELECT f.id     AS franquicia_id,
       f.nombre AS franquicia_nombre,
       s.id     AS sucursal_id,
       s.nombre AS sucursal_nombre,
       p.id     AS producto_id,
       p.nombre AS producto_nombre,
       p.stock  AS stock
FROM (SELECT p.*,
             ROW_NUMBER() OVER (
            PARTITION BY p.sucursal_id
            ORDER BY p.stock DESC, p.nombre ASC
        ) AS rn
      FROM producto p) p
         JOIN sucursal s ON s.id = p.sucursal_id
         JOIN franquicia f ON f.id = s.franquicia_id
WHERE p.rn = 1;

COMMENT
ON VIEW v_producto_max_stock_por_sucursal IS
'Para cada sucursal, expone el producto con mayor stock (desempate por nombre). Ideal para consultas agregadas del API.';


-- =========================
-- Franquicias (3)
-- =========================
INSERT INTO franquicia (nombre)
VALUES ('La Buena Pizza'),
       ('TecnoMundo'),
       ('Casa Verde');

-- =========================
-- Sucursales (9: 3 por franquicia)
-- =========================
-- La Buena Pizza
INSERT INTO sucursal (franquicia_id, nombre)
VALUES ((SELECT id FROM franquicia WHERE nombre = 'La Buena Pizza'), 'Centro'),
       ((SELECT id FROM franquicia WHERE nombre = 'La Buena Pizza'), 'Norte'),
       ((SELECT id FROM franquicia WHERE nombre = 'La Buena Pizza'), 'Sur');

-- TecnoMundo
INSERT INTO sucursal (franquicia_id, nombre)
VALUES ((SELECT id FROM franquicia WHERE nombre = 'TecnoMundo'), 'Centro'),
       ((SELECT id FROM franquicia WHERE nombre = 'TecnoMundo'), 'Norte'),
       ((SELECT id FROM franquicia WHERE nombre = 'TecnoMundo'), 'Sur');

-- Casa Verde
INSERT INTO sucursal (franquicia_id, nombre)
VALUES ((SELECT id FROM franquicia WHERE nombre = 'Casa Verde'), 'Centro'),
       ((SELECT id FROM franquicia WHERE nombre = 'Casa Verde'), 'Norte'),
       ((SELECT id FROM franquicia WHERE nombre = 'Casa Verde'), 'Sur');

-- =========================
-- Productos (27: 3 por sucursal)
-- =========================
-- ---------- La Buena Pizza ----------
-- Centro
INSERT INTO producto (sucursal_id, nombre, stock)
VALUES ((SELECT s.id
         FROM sucursal s
                  JOIN franquicia f ON f.id = s.franquicia_id
         WHERE f.nombre = 'La Buena Pizza'
           AND s.nombre = 'Centro'), 'Pizza Margarita', 45),
       ((SELECT s.id
         FROM sucursal s
                  JOIN franquicia f ON f.id = s.franquicia_id
         WHERE f.nombre = 'La Buena Pizza'
           AND s.nombre = 'Centro'), 'Pizza Pepperoni', 60),
       ((SELECT s.id
         FROM sucursal s
                  JOIN franquicia f ON f.id = s.franquicia_id
         WHERE f.nombre = 'La Buena Pizza'
           AND s.nombre = 'Centro'), 'Bebida Gaseosa 400ml', 120);

-- Norte
INSERT INTO producto (sucursal_id, nombre, stock)
VALUES ((SELECT s.id
         FROM sucursal s
                  JOIN franquicia f ON f.id = s.franquicia_id
         WHERE f.nombre = 'La Buena Pizza'
           AND s.nombre = 'Norte'), 'Pizza Hawaiana', 30),
       ((SELECT s.id
         FROM sucursal s
                  JOIN franquicia f ON f.id = s.franquicia_id
         WHERE f.nombre = 'La Buena Pizza'
           AND s.nombre = 'Norte'), 'Lasaña de Carne', 18),
       ((SELECT s.id
         FROM sucursal s
                  JOIN franquicia f ON f.id = s.franquicia_id
         WHERE f.nombre = 'La Buena Pizza'
           AND s.nombre = 'Norte'), 'Jugo Natural 500ml', 75);

-- Sur
INSERT INTO producto (sucursal_id, nombre, stock)
VALUES ((SELECT s.id
         FROM sucursal s
                  JOIN franquicia f ON f.id = s.franquicia_id
         WHERE f.nombre = 'La Buena Pizza'
           AND s.nombre = 'Sur'), 'Pizza Vegetariana', 28),
       ((SELECT s.id
         FROM sucursal s
                  JOIN franquicia f ON f.id = s.franquicia_id
         WHERE f.nombre = 'La Buena Pizza'
           AND s.nombre = 'Sur'), 'Calzone', 22),
       ((SELECT s.id
         FROM sucursal s
                  JOIN franquicia f ON f.id = s.franquicia_id
         WHERE f.nombre = 'La Buena Pizza'
           AND s.nombre = 'Sur'), 'Agua sin Gas 600ml', 95);

-- ---------- TecnoMundo ----------
-- Centro
INSERT INTO producto (sucursal_id, nombre, stock)
VALUES ((SELECT s.id
         FROM sucursal s
                  JOIN franquicia f ON f.id = s.franquicia_id
         WHERE f.nombre = 'TecnoMundo'
           AND s.nombre = 'Centro'), 'Teclado Mecánico', 40),
       ((SELECT s.id
         FROM sucursal s
                  JOIN franquicia f ON f.id = s.franquicia_id
         WHERE f.nombre = 'TecnoMundo'
           AND s.nombre = 'Centro'), 'Mouse Inalámbrico', 110),
       ((SELECT s.id
         FROM sucursal s
                  JOIN franquicia f ON f.id = s.franquicia_id
         WHERE f.nombre = 'TecnoMundo'
           AND s.nombre = 'Centro'), 'Monitor 24"', 25);

-- Norte
INSERT INTO producto (sucursal_id, nombre, stock)
VALUES ((SELECT s.id
         FROM sucursal s
                  JOIN franquicia f ON f.id = s.franquicia_id
         WHERE f.nombre = 'TecnoMundo'
           AND s.nombre = 'Norte'), 'Audífonos BT', 85),
       ((SELECT s.id
         FROM sucursal s
                  JOIN franquicia f ON f.id = s.franquicia_id
         WHERE f.nombre = 'TecnoMundo'
           AND s.nombre = 'Norte'), 'SSD 1TB NVMe', 33),
       ((SELECT s.id
         FROM sucursal s
                  JOIN franquicia f ON f.id = s.franquicia_id
         WHERE f.nombre = 'TecnoMundo'
           AND s.nombre = 'Norte'), 'Cargador USB-C 65W', 57);

-- Sur
INSERT INTO producto (sucursal_id, nombre, stock)
VALUES ((SELECT s.id
         FROM sucursal s
                  JOIN franquicia f ON f.id = s.franquicia_id
         WHERE f.nombre = 'TecnoMundo'
           AND s.nombre = 'Sur'), 'Router WiFi 6', 19),
       ((SELECT s.id
         FROM sucursal s
                  JOIN franquicia f ON f.id = s.franquicia_id
         WHERE f.nombre = 'TecnoMundo'
           AND s.nombre = 'Sur'), 'Webcam 1080p', 41),
       ((SELECT s.id
         FROM sucursal s
                  JOIN franquicia f ON f.id = s.franquicia_id
         WHERE f.nombre = 'TecnoMundo'
           AND s.nombre = 'Sur'), 'Power Bank 20k', 38);

-- ---------- Casa Verde ----------
-- Centro
INSERT INTO producto (sucursal_id, nombre, stock)
VALUES ((SELECT s.id
         FROM sucursal s
                  JOIN franquicia f ON f.id = s.franquicia_id
         WHERE f.nombre = 'Casa Verde'
           AND s.nombre = 'Centro'), 'Maceta Cerámica', 70),
       ((SELECT s.id
         FROM sucursal s
                  JOIN franquicia f ON f.id = s.franquicia_id
         WHERE f.nombre = 'Casa Verde'
           AND s.nombre = 'Centro'), 'Sustrato Universal 5kg', 44),
       ((SELECT s.id
         FROM sucursal s
                  JOIN franquicia f ON f.id = s.franquicia_id
         WHERE f.nombre = 'Casa Verde'
           AND s.nombre = 'Centro'), 'Regadera 2L', 29);

-- Norte
INSERT INTO producto (sucursal_id, nombre, stock)
VALUES ((SELECT s.id
         FROM sucursal s
                  JOIN franquicia f ON f.id = s.franquicia_id
         WHERE f.nombre = 'Casa Verde'
           AND s.nombre = 'Norte'), 'Planta Suculenta', 120),
       ((SELECT s.id
         FROM sucursal s
                  JOIN franquicia f ON f.id = s.franquicia_id
         WHERE f.nombre = 'Casa Verde'
           AND s.nombre = 'Norte'), 'Abono Orgánico 2kg', 36),
       ((SELECT s.id
         FROM sucursal s
                  JOIN franquicia f ON f.id = s.franquicia_id
         WHERE f.nombre = 'Casa Verde'
           AND s.nombre = 'Norte'), 'Guantes Jardinería', 64);

-- Sur
INSERT INTO producto (sucursal_id, nombre, stock)
VALUES ((SELECT s.id
         FROM sucursal s
                  JOIN franquicia f ON f.id = s.franquicia_id
         WHERE f.nombre = 'Casa Verde'
           AND s.nombre = 'Sur'), 'Tijeras de Poda', 21),
       ((SELECT s.id
         FROM sucursal s
                  JOIN franquicia f ON f.id = s.franquicia_id
         WHERE f.nombre = 'Casa Verde'
           AND s.nombre = 'Sur'), 'Manguera 15m', 17),
       ((SELECT s.id
         FROM sucursal s
                  JOIN franquicia f ON f.id = s.franquicia_id
         WHERE f.nombre = 'Casa Verde'
           AND s.nombre = 'Sur'), 'Pulverizador 1L', 40);