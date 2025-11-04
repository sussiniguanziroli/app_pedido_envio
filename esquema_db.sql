DROP SCHEMA IF EXISTS pedido_envio;
CREATE SCHEMA pedido_envio;
USE pedido_envio;

DROP TABLE IF EXISTS pedido;
DROP TABLE IF EXISTS envio;

USE pedido_envio;

CREATE TABLE envio (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    eliminado BOOL DEFAULT FALSE NOT NULL,
    tracking VARCHAR(40) UNIQUE,
    empresa ENUM('ANDREANI', 'OCA', 'CORREO_ARG') NOT NULL,
    tipo ENUM('ESTANDAR', 'EXPRES') NOT NULL DEFAULT 'ESTANDAR',
    costo DECIMAL(10,2) NOT NULL,
    fecha_despacho DATE,
    fecha_estimada DATE,
    estado ENUM ('EN_PREPARACION', 'EN_TRANSITO', 'ENTREGADO') NOT NULL DEFAULT 'EN_PREPARACION',
    CONSTRAINT chk_envio_costo CHECK (costo >= 0)
);


CREATE TABLE pedido (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    eliminado BOOL DEFAULT FALSE NOT NULL,
    numero VARCHAR(20) NOT NULL,
    fecha DATE NOT NULL,
    cliente_nombre VARCHAR(120) NOT NULL,
    total DECIMAL(12,2) NOT NULL,
    estado ENUM ('NUEVO', 'FACTURADO', 'ENVIADO') NOT NULL DEFAULT 'NUEVO',
    envio_id BIGINT NULL,
    CONSTRAINT fk_pedido_envio FOREIGN KEY (envio_id) 
        REFERENCES ENVIO(id)
        ON DELETE SET NULL
        ON UPDATE CASCADE,	
	CONSTRAINT uk_pedido_numero UNIQUE (numero),
    CONSTRAINT uk_pedido_id UNIQUE (id),
    CONSTRAINT uk_pedido_envio_id UNIQUE (envio_id),
    CONSTRAINT chk_pedido_total CHECK (total >= 0)
);