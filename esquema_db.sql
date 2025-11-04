CREATE DATABASE IF NOT EXISTS pedido_envio_db;

DROP SCHEMA IF EXISTS pedido_envio;
CREATE SCHEMA pedido_envio;
USE pedido_envio;

DROP TABLE IF EXISTS PEDIDO;
DROP TABLE IF EXISTS ENVIO;

USE pedido_envio;

CREATE TABLE ENVIO (
    id INT AUTO_INCREMENT PRIMARY KEY UNIQUE,
    eliminado BOOL,
    tracking VARCHAR(40),
    empresa VARCHAR(10) NOT NULL,
    tipo VARCHAR(8) NOT NULL DEFAULT 'Estandar',
    costo DECIMAL(10,2) NOT NULL,
    fecha_despacho DATETIME,
    fecha_entrega DATETIME,
    estado VARCHAR(15) NOT NULL DEFAULT 'En Preparacion',
    CONSTRAINT uk_envio_tracking UNIQUE (tracking),
    CONSTRAINT uk_envio_id UNIQUE (id),
    CONSTRAINT chk_envio_estado CHECK (
        estado IN ('En Preparacion', 'En Transito', 'Entregado')
    ),
    CONSTRAINT chk_envio_costo CHECK (costo >= 0),
    CONSTRAINT chk_envio_fechas CHECK (fecha_entrega >= fecha_despacho)
);


CREATE TABLE PEDIDO (
    id INT AUTO_INCREMENT PRIMARY KEY UNIQUE,
    eliminado BOOL,
    numero VARCHAR(20) NOT NULL,
    fecha DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    cliente_nombre VARCHAR(120) NOT NULL,
    total DECIMAL(12,2) NOT NULL,
    estado VARCHAR(10) NOT NULL DEFAULT 'Nuevo',
    envio_id INT NOT NULL UNIQUE,
    CONSTRAINT fk_pedido_envio FOREIGN KEY (envio_id) 
        REFERENCES ENVIO(id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,
	CONSTRAINT uk_pedido_numero UNIQUE (numero),
    CONSTRAINT uk_pedido_id UNIQUE (id),
    CONSTRAINT chk_pedido_estado CHECK (
        estado IN ('Nuevo', 'Facturado', 'Enviado')
    ),
    CONSTRAINT chk_pedido_total CHECK (total >= 0)
);