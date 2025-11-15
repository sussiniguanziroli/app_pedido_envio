# Sistema de Gestión de Pedidos y Envíos

Sistema de gestión de pedidos y envíos desarrollado en Java con arquitectura en capas, implementando patrones DAO, Service y transacciones ACID.

## Descripción

Aplicación de consola que permite gestionar pedidos y sus envíos asociados, con soporte para operaciones CRUD completas, soft delete y transacciones atómicas. El sistema mantiene una relación unidireccional Pedido→Envío con integridad referencial.

## 👥 Autores

**Equipo de Desarrollo:**

- **Matías Ezequiel Vazquez** - Desarrollo de Capas de la Etapa ENVIO
- **Patricio Sussini Guanziroli** - Desarrollo de Capas de la Etapa PEDIDO

- **Trabajo compartido en Capa Main y Aplicativos del Menu

**Distribución de trabajo:** Los dos integrantes contribuyeron equitativamente (~50% cada uno) en el diseño, implementación y documentación del sistema, incluido el diseño y desarrollo de la DB en SQL.

## Trabajo Práctico Integrador - Programación 2

### Descripción del Proyecto

Este Trabajo Práctico Integrador tiene como objetivo demostrar la aplicación práctica de los conceptos fundamentales de Programación Orientada a Objetos y Persistencia de Datos aprendidos durante el cursado de Programación 2. El proyecto consiste en desarrollar un sistema completo de gestión de personas y domicilios que permita realizar operaciones CRUD (Crear, Leer, Actualizar, Eliminar) sobre estas entidades, implementando una arquitectura robusta y profesional.

### Objetivos Académicos

El desarrollo de este sistema permite aplicar y consolidar los siguientes conceptos clave de la materia:

**1. Arquitectura en Capas (Layered Architecture)**
- Implementación de separación de responsabilidades en 4 capas diferenciadas
- Capa de Presentación (Main/UI): Interacción con el usuario mediante consola
- Capa de Lógica de Negocio (Service): Validaciones y reglas de negocio
- Capa de Acceso a Datos (DAO): Operaciones de persistencia
- Capa de Modelo (Models): Representación de entidades del dominio

**2. Programación Orientada a Objetos**
- Aplicación de principios SOLID (Single Responsibility, Dependency Injection)
- Uso de herencia mediante clase abstracta Base
- Implementación de interfaces genéricas (GenericDAO, GenericService)
- Encapsulamiento con atributos privados y métodos de acceso
- Sobrescritura de métodos (equals, hashCode, toString)

**3. Persistencia de Datos con JDBC**
- Conexión a base de datos MySQL mediante JDBC
- Implementación del patrón DAO (Data Access Object)
- Uso de PreparedStatements para prevenir SQL Injection
- Gestión de transacciones con commit y rollback
- Manejo de claves autogeneradas (AUTO_INCREMENT)
- Consultas con LEFT JOIN para relaciones entre entidades

**4. Manejo de Recursos y Excepciones**
- Uso del patrón try-with-resources para gestión automática de recursos JDBC
- Implementación de AutoCloseable en TransactionManager
- Manejo apropiado de excepciones con propagación controlada
- Validación multi-nivel: base de datos y aplicación

**5. Patrones de Diseño**
- Factory Pattern (DatabaseConnection)
- Service Layer Pattern (separación lógica de negocio)
- DAO Pattern (abstracción del acceso a datos)
- Soft Delete Pattern (eliminación lógica de registros)
- Dependency Injection manual

**6. Validación de Integridad de Datos**
- Validación de unicidad (DNI único por persona)
- Validación de campos obligatorios en múltiples niveles
- Validación de integridad referencial (Foreign Keys)
- Implementación de eliminación segura para prevenir referencias huérfanas

### Funcionalidades Implementadas

El sistema permite gestionar dos entidades principales con las siguientes operaciones:

## Características Principales

- **CRUD completo**: Crear, listar, actualizar y eliminar pedidos con/sin envíos asociados
- **Gestión de Envíos**: Administrar envíos de forma independiente o asociados a pedidos
- **Transacciones ACID**: Para crear pedido con envío en una sola operación
- **Soft delete**: Eliminación lógica que preserva la integridad de datos
- **Validaciones de negocio**: Unicidad, consistencia de fechas, valores no negativos
- **Arquitectura en capas**: Models, DAO, Service, Main
- **Transacciones**: Soporte para operaciones atómicas con rollback automático
- **Interfaz de consola**: Menú interactivo que permite facilidad de navegación

## Tecnologías Utilizadas

- **Java 17+**
- **MySQL 8.0+**
- **JDBC** (MySQL Connector/J 8.0.33)


## Estructura del Proyecto
```
pedido-envio/
├── Config/
│   ├── DatabaseConnection.java    # Conexión a base de datos
│   └── TransactionManager.java    # Gestión de transacciones
├── Models/
│   ├── Base.java                  # Clase base con ID y eliminado
│   ├── Envio.java                 # Entidad Envío con enums
│   └── Pedido.java                # Entidad Pedido con relación a Envío
├── Dao/
│   ├── GenericDAO.java            # Interfaz genérica CRUD
│   ├── EnvioDAO.java              # DAO de Envío
│   └── PedidoDAO.java             # DAO de Pedido con JOIN
├── Service/
│   ├── GenericService.java        # Interfaz de servicios
│   ├── EnvioServiceImpl.java      # Lógica de negocio de Envío
│   └── PedidoServiceImpl.java     # Lógica de negocio de Pedido
├── Main/
│   ├── Main.java                  # Punto de entrada
│   ├── AppMenu.java               # Controlador del menú
│   ├── MenuDisplay.java           # Visualización del menú
│   ├── MenuHandler.java           # Handlers de operaciones
│   └── TestConexion.java          # Verificación de conexión
└── lib/
    └── mysql-connector-j-8.0.33.jar
```

## Modelo de Datos

### Tabla: `envio`
```sql
- id (BIGINT, PK, AUTO_INCREMENT)
- eliminado (BOOLEAN, DEFAULT FALSE)
- tracking (VARCHAR(40), UNIQUE)
- empresa (ENUM: ANDREANI, OCA, CORREO_ARG)
- tipo (ENUM: ESTANDAR, EXPRES)
- costo (DECIMAL(10,2), >= 0)
- fecha_despacho (DATE, NULL)
- fecha_estimada (DATE, NULL)
- estado (ENUM: EN_PREPARACION, EN_TRANSITO, ENTREGADO)
```

### Tabla: `pedido`
```sql
- id (BIGINT, PK, AUTO_INCREMENT)
- eliminado (BOOLEAN, DEFAULT FALSE)
- numero (VARCHAR(20), UNIQUE)
- fecha (DATE, NOT NULL)
- cliente_nombre (VARCHAR(120), NOT NULL)
- total (DECIMAL(12,2), >= 0)
- estado (ENUM: NUEVO, FACTURADO, ENVIADO)
- envio_id (BIGINT, FK → envio.id, NULL)
```

**Relación:** Pedido → Envío (unidireccional, 1:1 estricta)
- Un Pedido DEBE tener exactamente un Envío (`envio_id NOT NULL`)
- Un Envío solo puede estar en un Pedido (`UNIQUE envio_id`)
- Unidireccional: Pedido conoce a Envío, Envío no conoce a Pedido
- ON DELETE RESTRICT: No se puede eliminar envío con pedido asociado
- Garantizado por: FK + UNIQUE + NOT NULL en BD, validaciones en Service

**Flujos permitidos:**
1. Crear Envío independiente → Disponible para asignar a pedidos
2. Crear Pedido + Envío en transacción → Ambos se crean juntos (opción 8)

## Instalación y Configuración

### Requisitos Previos

- JDK 17 o superior
- MySQL 8.0 o superior
- XAMPP/WAMP/MySQL Workbench

### Paso 1: Configurar Base de Datos

1. Iniciar MySQL
2. Ejecutar script de esquema:
```sql
DROP SCHEMA IF EXISTS pedido_envio;
CREATE SCHEMA pedido_envio;
USE pedido_envio;

-- Crear tabla envio
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

-- Crear tabla pedido
CREATE TABLE pedido (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    eliminado BOOL DEFAULT FALSE NOT NULL,
    numero VARCHAR(20) NOT NULL,
    fecha DATE NOT NULL,
    cliente_nombre VARCHAR(120) NOT NULL,
    total DECIMAL(12,2) NOT NULL,
    estado ENUM ('NUEVO', 'FACTURADO', 'ENVIADO') NOT NULL DEFAULT 'NUEVO',
    envio_id BIGINT NOT NULL,
    CONSTRAINT fk_pedido_envio FOREIGN KEY (envio_id) 
        REFERENCES envio(id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,	
    CONSTRAINT uk_pedido_numero UNIQUE (numero),
    CONSTRAINT uk_pedido_envio_id UNIQUE (envio_id),
    CONSTRAINT chk_pedido_total CHECK (total >= 0)
);
```

3. Insertar datos de prueba:
```sql
INSERT INTO envio (id, eliminado, tracking, empresa, tipo, costo, fecha_despacho, fecha_estimada, estado) VALUES
(1, 0, 'AND-2025-000001', 'ANDREANI', 'EXPRES', 1500.00, '2025-01-15', '2025-01-17', 'EN_TRANSITO'),
(2, 0, 'OCA-2025-000001', 'OCA', 'ESTANDAR', 800.00, '2025-01-14', '2025-01-20', 'ENTREGADO'),
(3, 0, 'ARG-2025-000001', 'CORREO_ARG', 'ESTANDAR', 600.00, '2025-01-16', '2025-01-22', 'EN_PREPARACION'),
(4, 0, 'ARG-2025-000002', 'CORREO_ARG', 'ESTANDAR', 600.00, '2025-01-16', '2025-01-22', 'ENTREGADO');

INSERT INTO pedido (id, eliminado, numero, fecha, cliente_nombre, total, estado, envio_id) VALUES
(1, 0, 'PED-2025-0001', '2025-01-10', 'Juan Pérez', 25000.50, 'ENVIADO', 1),
(2, 0, 'PED-2025-0002', '2025-01-11', 'María González', 15500.00, 'ENVIADO', 2),
(3, 0, 'PED-2025-0003', '2025-01-12', 'Carlos Rodríguez', 8900.75, 'FACTURADO', 3),
(4, 0, 'PED-2025-0004', '2025-01-13', 'Ana Martínez', 12300.00, 'NUEVO', 4);

COMMIT;
```

### Paso 2: Configurar Conexión

Editar `Config/DatabaseConnection.java`:
```java
private static final String URL = "jdbc:mysql://localhost:3306/pedido_envio";
private static final String USER = "root";
private static final String PASSWORD = "";  // Ajustar según tu configuración
```

### Paso 3: Agregar Driver JDBC

1. Descargar `mysql-connector-j-8.0.33.jar`
2. Colocar en carpeta `lib/`
3. Agregar al classpath del IDE

### Paso 4: Compilar y Ejecutar

**Desde IDE:**
```
Run Main.java o AppMenu.java
```

**Desde línea de comandos:**
```bash
# Compilar
javac -cp "lib/*" -d bin Config/*.java Models/*.java Dao/*.java Service/*.java Main/*.java

# Ejecutar
java -cp "lib/*:bin" Main.Main
```

## Uso del Sistema

### Menú Principal
```
GESTIÓN DE ENVÍOS:
  1. Crear envío
  2. Listar envíos
  3. Buscar envío por ID
  4. Buscar envío por tracking
  5. Actualizar envío
  6. Eliminar envío

GESTIÓN DE PEDIDOS:
  7. Crear pedido con envío (transacción)
  8. Listar pedidos
  9. Buscar pedido por ID
  10. Buscar pedido por número
  11. Actualizar pedido
  12. Eliminar pedido

SALIR:
  0. Salir del sistema
```

### Funcionalidades Clave

#### Crear Pedido con Envío (Transacción ACID)

Opción más importante del sistema. Crea pedido y envío en una sola transacción atómica:
```
Opción 8 → Ingresa datos del pedido → Ingresa datos del envío
→ Si todo OK: ambos se crean
→ Si algo falla: rollback automático (ninguno se crea)
```

**Ejemplo de validación con rollback:**
```
- Ingresa tracking duplicado (ej: AND-2025-000001)
- Sistema rechaza y hace rollback
- Ni pedido ni envío se crean en la BD
```

#### Soft Delete

Los registros eliminados se marcan con `eliminado = TRUE` pero permanecen en la BD:
```sql
-- Antes de eliminar
SELECT * FROM pedido WHERE id = 0;  -- eliminado = 0

-- Después de eliminar
SELECT * FROM pedido WHERE id = 1;  -- eliminado = 1

-- No aparece en listar (opción 9) porque filtra por eliminado = 0
```

## Validaciones Implementadas

### Envío
- **Tracking único** (no puede haber duplicados)
- **Costo >= 0** (no puede ser negativo)
- **Fechas consistentes** (fecha_estimada >= fecha_despacho)
- **Campos obligatorios** (tracking, empresa, tipo, costo, estado)

### Pedido
- **Número único** (no puede haber duplicados)
- **Total >= 0** (no puede ser negativo)
- **Campos obligatorios** (numero, fecha, cliente_nombre, total, estado)
- **Envío obligatorio** (envio_id NOT NULL, debe existir)
- **Envío único** (un envío solo puede estar en un pedido)
- **Integridad referencial** (envio_id debe referenciar envío existente)

**Validaciones en Aplicación:**
- Service valida que pedido tenga envío antes de crear
- Service valida que envío no esté ya usado por otro pedido
- Transacción atómica garantiza consistencia (commit/rollback)

### Escenarios Soportados

**1. Crear Envío Independiente (Opción 1)**
```
Un envío puede crearse sin pedido asociado (inventario disponible)
Posteriormente puede asignarse a un pedido nuevo
```

**2. Crear Pedido + Envío en Transacción (Opción 8)**
```
Ambos se crean juntos en una operación atómica
Si algo falla, ninguno se crea (rollback automático)
```

**3. Restricción al Eliminar**
```
No se puede eliminar un envío que tiene pedido asociado
Garantizado por ON DELETE RESTRICT en la FK
```


## Arquitectura y Patrones

### Capas
```
Main (UI) → Service (Lógica de Negocio) → DAO (Acceso a Datos) → Models (Entidades)
```

### Patrones Utilizados

- **DAO (Data Access Object):** Separación de lógica de acceso a datos
- **Service Layer:** Capa de lógica de negocio y validaciones
- **Transaction Script:** Gestión de transacciones ACID
- **Soft Delete:** Baja lógica para mantener historial
- **Generic Types:** Interfaces genéricas para reutilización

### Transacciones
```java
// Ejemplo de transacción atómica en PedidoServiceImpl
TransactionManager tm = new TransactionManager(conn);
tm.startTransaction();

try {
    envioService.crear(envio, tm.getConnection());  // 1. Crear envío
    pedidoDAO.crear(pedido, conn);                  // 2. Crear pedido
    tm.commit();                                    // 3. Commit si todo OK
} catch (Exception e) {
    // Rollback automático en tm.close()
    throw new SQLException("Error en transacción", e);
}
```

## Solución de Problemas

### Error: "Driver MySQL no encontrado"
```bash
Verificar que mysql-connector-j.jar esté en lib/ y en el classpath
```

### Error: "Access denied for user 'root'"
```bash
Verificar credenciales en DatabaseConnection.java
```

### Error: "Unknown database 'pedido_envio'"
```bash
Ejecutar script de esquema en MySQL
```

### No lista envíos/pedidos pero existen en BD
```bash
En MySQL Workbench ejecutar: COMMIT;
Verificar que eliminado = 0 en los registros
```

## Testing

Para verificar la conexión antes de usar la app:
```bash
java -cp "lib/*:bin" Main.TestConexion
```

Salida esperada:
```
Conexión exitosa a la base de datos
Base de datos: pedido_envio
Driver: MySQL Connector/J v8.0.33
```

## Contexto Académico

**Materia**: Programación 2
**Tipo de Evaluación**: Trabajo Práctico Integrador (TPI)
**Modalidad**: Desarrollo de sistema CRUD con persistencia en base de datos
**Objetivo**: Aplicar conceptos de POO, JDBC, arquitectura en capas y patrones de diseño

Este proyecto representa la integración de todos los conceptos vistos durante el cuatrimestre, demostrando capacidad para:
- Diseñar sistemas con arquitectura profesional
- Implementar persistencia de datos con JDBC
- Aplicar patrones de diseño apropiados
- Manejar recursos y excepciones correctamente
- Validar integridad de datos en múltiples niveles
- Documentar código de forma profesional
