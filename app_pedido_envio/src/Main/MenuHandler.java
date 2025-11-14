package Main;

import Models.Envio;
import Models.Envio.EmpresaEnvio;
import Models.Envio.TipoEnvio;
import Models.Envio.EstadoEnvio;
import Models.Pedido;
import Models.Pedido.EstadoPedido;
import Service.EnvioServiceImpl;
import Service.PedidoServiceImpl;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;

/**
 * Handler que gestiona todas las operaciones del menú de la aplicación Pedido-Envio.
 * Contiene la lógica de interacción con el usuario para CRUD de Envios y Pedidos.
 *
 * Responsabilidades:
 * - Leer entrada del usuario (Scanner)
 * - Validar entrada básica (null, vacío, formato)
 * - Crear objetos de dominio (Envio, Pedido) desde input del usuario
 * - Llamar a los Services para ejecutar operaciones
 * - Mostrar resultados al usuario (éxito, error, listados)
 * - Manejar excepciones y mostrar mensajes amigables
 *
 * IMPORTANTE: Esta clase NO tiene lógica de negocio.
 * Solo coordina la interacción usuario ↔ Service.
 * Las validaciones de negocio están en Service.
 *
 * Arquitectura:
 * Main (MenuHandler) → Service → DAO → Models
 *
 * Patrón: Controller / Handler del MVC adaptado a consola
 */
public class MenuHandler {
    
    /**
     * Scanner compartido para leer entrada del usuario.
     * Inyectado desde AppMenu para garantizar una única instancia.
     */
    private final Scanner scanner;
    
    /**
     * Service principal de pedidos.
     * Coordina operaciones de Pedido y tiene acceso a EnvioService.
     */
    private final PedidoServiceImpl pedidoService;
    
    /**
     * Service de envíos (acceso directo).
     * Obtenido desde pedidoService.getEnvioService().
     */
    private final EnvioServiceImpl envioService;
    
    /**
     * Formateador de fechas para entrada/salida.
     * Formato: dd/MM/yyyy (ej: 15/01/2025)
     */
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    
    /**
     * Constructor que inicializa el handler con sus dependencias.
     *
     * @param scanner Scanner compartido de AppMenu
     * @param pedidoService Service de pedidos (contiene referencia a envioService)
     * @throws IllegalArgumentException si algún parámetro es null
     */
    public MenuHandler(Scanner scanner, PedidoServiceImpl pedidoService) {
        if (scanner == null) {
            throw new IllegalArgumentException("Scanner no puede ser null");
        }
        if (pedidoService == null) {
            throw new IllegalArgumentException("PedidoService no puede ser null");
        }
        
        this.scanner = scanner;
        this.pedidoService = pedidoService;
        this.envioService = pedidoService.getEnvioService();
    }
    
// ═══════════════════════════════════════════════════════════
    // MÉTODOS DE ENVÍO
    // ═══════════════════════════════════════════════════════════
    
    /**
     * Crea un nuevo envío solicitando datos al usuario.
     *
     * Flujo:
     * 1. Solicitar tracking (obligatorio, único, máx 40 chars)
     * 2. Solicitar empresa (ANDREANI, OCA, CORREO_ARG)
     * 3. Solicitar tipo (ESTANDAR, EXPRES)
     * 4. Solicitar costo (>= 0)
     * 5. Solicitar fecha de despacho (opcional, formato dd/MM/yyyy)
     * 6. Solicitar fecha estimada (opcional, formato dd/MM/yyyy)
     * 7. Solicitar estado (EN_PREPARACION, EN_TRANSITO, ENTREGADO)
     * 8. Crear objeto Envio
     * 9. Llamar a envioService.crear()
     * 10. Mostrar resultado (éxito o error)
     *
     * Validaciones:
     * - Tracking no vacío
     * - Empresa válida (enum)
     * - Tipo válido (enum)
     * - Costo numérico válido
     * - Fechas en formato correcto (si se ingresan)
     * - Estado válido (enum)
     *
     * Las validaciones de negocio (tracking único, costo >= 0, fechas consistentes)
     * se realizan en EnvioServiceImpl.
     */
    public void crearEnvio() {
        MenuDisplay.mostrarSeparador();
        System.out.println("CREAR NUEVO ENVÍO");
        MenuDisplay.mostrarSeparador();
        
        try {
            // 1. Tracking
            System.out.print("Ingrese tracking (máx 40 caracteres): ");
            String tracking = scanner.nextLine().trim();
            
            if (tracking.isEmpty()) {
                MenuDisplay.mostrarError("El tracking no puede estar vacío");
                return;
            }
            
            // 2. Empresa
            System.out.println("\nEmpresas disponibles:");
            System.out.println("  1. ANDREANI");
            System.out.println("  2. OCA");
            System.out.println("  3. CORREO_ARG");
            System.out.print("Seleccione empresa (1-3): ");
            
            EmpresaEnvio empresa = leerEmpresaEnvio();
            if (empresa == null) {
                MenuDisplay.mostrarError("Empresa inválida");
                return;
            }
            
            // 3. Tipo
            System.out.println("\nTipos disponibles:");
            System.out.println("  1. ESTANDAR");
            System.out.println("  2. EXPRES");
            System.out.print("Seleccione tipo (1-2): ");
            
            TipoEnvio tipo = leerTipoEnvio();
            if (tipo == null) {
                MenuDisplay.mostrarError("Tipo inválido");
                return;
            }
            
            // 4. Costo
            System.out.print("\nIngrese costo (número decimal, ej: 1500.50): ");
            Double costo = leerDouble();
            if (costo == null) {
                MenuDisplay.mostrarError("Costo inválido");
                return;
            }
            
            // 5. Fecha de despacho (opcional)
            System.out.print("\nIngrese fecha de despacho (dd/MM/yyyy) o Enter para omitir: ");
            String fechaDespachoStr = scanner.nextLine().trim();
            LocalDate fechaDespacho = null;
            
            if (!fechaDespachoStr.isEmpty()) {
                fechaDespacho = parsearFecha(fechaDespachoStr);
                if (fechaDespacho == null) {
                    MenuDisplay.mostrarError("Fecha de despacho inválida. Use formato dd/MM/yyyy");
                    return;
                }
            }
            
            // 6. Fecha estimada (opcional)
            System.out.print("Ingrese fecha estimada (dd/MM/yyyy) o Enter para omitir: ");
            String fechaEstimadaStr = scanner.nextLine().trim();
            LocalDate fechaEstimada = null;
            
            if (!fechaEstimadaStr.isEmpty()) {
                fechaEstimada = parsearFecha(fechaEstimadaStr);
                if (fechaEstimada == null) {
                    MenuDisplay.mostrarError("Fecha estimada inválida. Use formato dd/MM/yyyy");
                    return;
                }
            }
            
            // 7. Estado
            System.out.println("\nEstados disponibles:");
            System.out.println("  1. EN_PREPARACION");
            System.out.println("  2. EN_TRANSITO");
            System.out.println("  3. ENTREGADO");
            System.out.print("Seleccione estado (1-3): ");
            
            EstadoEnvio estado = leerEstadoEnvio();
            if (estado == null) {
                MenuDisplay.mostrarError("Estado inválido");
                return;
            }
            
            // 8. Crear objeto Envio
            Envio envio = new Envio(tracking, empresa, tipo, costo, fechaDespacho, fechaEstimada, estado);
            
            // 9. Llamar al service
            envioService.crear(envio);
            
            // 10. Mostrar resultado
            MenuDisplay.mostrarExito("Envío creado exitosamente con ID: " + envio.getId());
            System.out.println(envio);
            
        } catch (IllegalArgumentException e) {
            MenuDisplay.mostrarError("Validación: " + e.getMessage());
        } catch (SQLException e) {
            MenuDisplay.mostrarError("Error de base de datos: " + e.getMessage());
        } catch (Exception e) {
            MenuDisplay.mostrarError("Error inesperado: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
/**
     * Lista todos los envíos activos (no eliminados).
     *
     * Flujo:
     * 1. Llamar a envioService.obtenerTodos()
     * 2. Si no hay envíos, mostrar mensaje
     * 3. Si hay envíos, mostrarlos en formato tabla
     */
    public void listarEnvios() {
        MenuDisplay.mostrarSeparador();
        System.out.println("LISTADO DE ENVÍOS");
        MenuDisplay.mostrarSeparador();
        
        try {
            List<Envio> envios = envioService.obtenerTodos();
            
            if (envios.isEmpty()) {
                System.out.println("\nNo hay envíos registrados.");
                return;
            }
            
            System.out.println("\nTotal de envíos: " + envios.size());
            System.out.println();
            
            for (Envio envio : envios) {
                mostrarEnvio(envio);
                System.out.println();
            }
            
        } catch (SQLException e) {
            MenuDisplay.mostrarError("Error al listar envíos: " + e.getMessage());
        } catch (Exception e) {
            MenuDisplay.mostrarError("Error inesperado: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Busca un envío por su ID.
     *
     * Flujo:
     * 1. Solicitar ID
     * 2. Llamar a envioService.obtenerPorId()
     * 3. Mostrar envío encontrado o mensaje de error
     */
    public void buscarEnvioPorId() {
        MenuDisplay.mostrarSeparador();
        System.out.println("BUSCAR ENVÍO POR ID");
        MenuDisplay.mostrarSeparador();
        
        try {
            System.out.print("\nIngrese ID del envío: ");
            Long id = leerLong();
            
            if (id == null) {
                MenuDisplay.mostrarError("ID inválido");
                return;
            }
            
            Envio envio = envioService.obtenerPorId(id);
            
            System.out.println("\nEnvío encontrado:");
            mostrarEnvio(envio);
            
        } catch (IllegalArgumentException e) {
            MenuDisplay.mostrarError(e.getMessage());
        } catch (SQLException e) {
            MenuDisplay.mostrarError("Error de base de datos: " + e.getMessage());
        } catch (Exception e) {
            MenuDisplay.mostrarError("Error inesperado: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Busca un envío por su código de tracking.
     *
     * Flujo:
     * 1. Solicitar tracking
     * 2. Llamar a envioService.buscarPorTracking()
     * 3. Mostrar envío encontrado o mensaje si no existe
     */
    public void buscarEnvioPorTracking() {
        MenuDisplay.mostrarSeparador();
        System.out.println("BUSCAR ENVÍO POR TRACKING");
        MenuDisplay.mostrarSeparador();
        
        try {
            System.out.print("\nIngrese código de tracking: ");
            String tracking = scanner.nextLine().trim();
            
            if (tracking.isEmpty()) {
                MenuDisplay.mostrarError("El tracking no puede estar vacío");
                return;
            }
            
            Envio envio = envioService.buscarPorTracking(tracking);
            
            if (envio == null) {
                System.out.println("\nNo se encontró envío con tracking: " + tracking);
                return;
            }
            
            System.out.println("\nEnvío encontrado:");
            mostrarEnvio(envio);
            
        } catch (IllegalArgumentException e) {
            MenuDisplay.mostrarError(e.getMessage());
        } catch (SQLException e) {
            MenuDisplay.mostrarError("Error de base de datos: " + e.getMessage());
        } catch (Exception e) {
            MenuDisplay.mostrarError("Error inesperado: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
/**
     * Actualiza un envío existente.
     *
     * Flujo:
     * 1. Solicitar ID del envío a actualizar
     * 2. Buscar envío actual
     * 3. Mostrar datos actuales
     * 4. Solicitar nuevos datos (permitir Enter para mantener actual)
     * 5. Actualizar objeto Envio
     * 6. Llamar a envioService.actualizar()
     * 7. Mostrar resultado
     */
    public void actualizarEnvio() {
        MenuDisplay.mostrarSeparador();
        System.out.println("ACTUALIZAR ENVÍO");
        MenuDisplay.mostrarSeparador();
        
        try {
            // 1. Buscar envío a actualizar
            System.out.print("\nIngrese ID del envío a actualizar: ");
            Long id = leerLong();
            
            if (id == null) {
                MenuDisplay.mostrarError("ID inválido");
                return;
            }
            
            Envio envioActual = envioService.obtenerPorId(id);
            
            // 2. Mostrar datos actuales
            System.out.println("\nDatos actuales del envío:");
            mostrarEnvio(envioActual);
            
            System.out.println("\nPresione Enter para mantener el valor actual");
            MenuDisplay.mostrarSeparador();
            
            // 3. Solicitar nuevos datos
            System.out.print("\nTracking actual [" + envioActual.getTracking() + "]: ");
            String tracking = scanner.nextLine().trim();
            if (!tracking.isEmpty()) {
                envioActual.setTracking(tracking);
            }
            
            System.out.println("\nEmpresa actual [" + envioActual.getEmpresa() + "]");
            System.out.println("Empresas disponibles:");
            System.out.println("  1. ANDREANI");
            System.out.println("  2. OCA");
            System.out.println("  3. CORREO_ARG");
            System.out.print("Nueva empresa (1-3) o Enter: ");
            String empresaInput = scanner.nextLine().trim();
            if (!empresaInput.isEmpty()) {
                EmpresaEnvio empresa = leerEmpresaEnvioDesdeString(empresaInput);
                if (empresa != null) {
                    envioActual.setEmpresa(empresa);
                }
            }
            
            System.out.println("\nTipo actual [" + envioActual.getTipo() + "]");
            System.out.println("Tipos disponibles:");
            System.out.println("  1. ESTANDAR");
            System.out.println("  2. EXPRES");
            System.out.print("Nuevo tipo (1-2) o Enter: ");
            String tipoInput = scanner.nextLine().trim();
            if (!tipoInput.isEmpty()) {
                TipoEnvio tipo = leerTipoEnvioDesdeString(tipoInput);
                if (tipo != null) {
                    envioActual.setTipo(tipo);
                }
            }
            
            System.out.print("\nCosto actual [" + envioActual.getCosto() + "]: ");
            String costoStr = scanner.nextLine().trim();
            if (!costoStr.isEmpty()) {
                Double costo = parseDouble(costoStr);
                if (costo != null) {
                    envioActual.setCosto(costo);
                }
            }
            
            System.out.print("\nFecha despacho actual [" + 
                (envioActual.getFechaDespacho() != null ? envioActual.getFechaDespacho().format(DATE_FORMATTER) : "Sin fecha") 
                + "] (dd/mm/aaaa): ");
            String fechaDespachoStr = scanner.nextLine().trim();
            if (!fechaDespachoStr.isEmpty()) {
                LocalDate fechaDespacho = parsearFecha(fechaDespachoStr);
                if (fechaDespacho != null) {
                    envioActual.setFechaDespacho(fechaDespacho);
                }
            }
            
            System.out.print("\nFecha estimada actual [" + 
                (envioActual.getFechaEstimada() != null ? envioActual.getFechaEstimada().format(DATE_FORMATTER) : "Sin fecha") 
                + "] (dd/mm/aaaa): ");
            String fechaEstimadaStr = scanner.nextLine().trim();
            if (!fechaEstimadaStr.isEmpty()) {
                LocalDate fechaEstimada = parsearFecha(fechaEstimadaStr);
                if (fechaEstimada != null) {
                    envioActual.setFechaEstimada(fechaEstimada);
                }
            }
            
            System.out.println("\nEstado actual [" + envioActual.getEstado() + "]");
            System.out.println("Estados disponibles:");
            System.out.println("  1. EN_PREPARACION");
            System.out.println("  2. EN_TRANSITO");
            System.out.println("  3. ENTREGADO");
            System.out.print("Nuevo estado (1-3) o Enter: ");
            String estadoInput = scanner.nextLine().trim();
            if (!estadoInput.isEmpty()) {
                EstadoEnvio estado = leerEstadoEnvioDesdeString(estadoInput);
                if (estado != null) {
                    envioActual.setEstado(estado);
                }
            }
            
            // 4. Actualizar
            envioService.actualizar(envioActual);
            
            // 5. Mostrar resultado
            MenuDisplay.mostrarExito("Envío actualizado exitosamente");
            mostrarEnvio(envioActual);
            
        } catch (IllegalArgumentException e) {
            MenuDisplay.mostrarError("Validación: " + e.getMessage());
        } catch (SQLException e) {
            MenuDisplay.mostrarError("Error de base de datos: " + e.getMessage());
        } catch (Exception e) {
            MenuDisplay.mostrarError("Error inesperado: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Elimina un envío (soft delete).
     *
     * Flujo:
     * 1. Solicitar ID del envío a eliminar
     * 2. Buscar envío
     * 3. Mostrar datos y pedir confirmación
     * 4. Llamar a envioService.eliminar()
     * 5. Mostrar resultado
     */
    public void eliminarEnvio() {
        MenuDisplay.mostrarSeparador();
        System.out.println("ELIMINAR ENVÍO");
        MenuDisplay.mostrarSeparador();
        
        try {
            System.out.print("\nIngrese ID del envío a eliminar: ");
            Long id = leerLong();
            
            if (id == null) {
                MenuDisplay.mostrarError("ID inválido");
                return;
            }
            
            Envio envio = envioService.obtenerPorId(id);
            
            System.out.println("\nEnvío a eliminar:");
            mostrarEnvio(envio);
            
            System.out.print("\n¿Está seguro de eliminar este envío? (S/N): ");
            String confirmacion = scanner.nextLine().trim().toUpperCase();
            
            if (!confirmacion.equals("S")) {
                System.out.println("Operación cancelada");
                return;
            }
            
            envioService.eliminar(id);
            
            MenuDisplay.mostrarExito("Envío eliminado exitosamente");
            
        } catch (IllegalArgumentException e) {
            MenuDisplay.mostrarError(e.getMessage());
        } catch (SQLException e) {
            MenuDisplay.mostrarError("Error de base de datos: " + e.getMessage());
        } catch (Exception e) {
            MenuDisplay.mostrarError("Error inesperado: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
// ═══════════════════════════════════════════════════════════
    // MÉTODOS DE PEDIDO
    // ═══════════════════════════════════════════════════════════
    
    /**
     * Crea un nuevo pedido SIN envío asociado.
     *
     * Flujo:
     * 1. Solicitar número de pedido (obligatorio, único, máx 20 chars)
     * 2. Solicitar fecha (formato dd/MM/yyyy)
     * 3. Solicitar nombre del cliente (obligatorio, máx 120 chars)
     * 4. Solicitar total (>= 0)
     * 5. Solicitar estado (NUEVO, FACTURADO, ENVIADO)
     * 6. Crear objeto Pedido (sin envío)
     * 7. Llamar a pedidoService.crear()
     * 8. Mostrar resultado
     *
     * Validaciones:
     * - Número no vacío
     * - Fecha en formato correcto
     * - Cliente no vacío
     * - Total numérico válido
     * - Estado válido (enum)
     *
     * Las validaciones de negocio (número único, total >= 0)
     * se realizan en PedidoServiceImpl.
     */
    
    /*
    public void crearPedidoSinEnvio() {
        MenuDisplay.mostrarSeparador();
        System.out.println("CREAR NUEVO PEDIDO (SIN ENVÍO)");
        MenuDisplay.mostrarSeparador();
        
        try {
            // 1. Número de pedido
            System.out.print("Ingrese número de pedido (máx 20 caracteres): ");
            String numero = scanner.nextLine().trim();
            
            if (numero.isEmpty()) {
                MenuDisplay.mostrarError("El número de pedido no puede estar vacío");
                return;
            }
            
            // 2. Fecha
            System.out.print("\nIngrese fecha del pedido (dd/MM/yyyy): ");
            String fechaStr = scanner.nextLine().trim();
            LocalDate fecha = parsearFecha(fechaStr);
            
            if (fecha == null) {
                MenuDisplay.mostrarError("Fecha inválida. Use formato dd/MM/yyyy");
                return;
            }
            
            // 3. Nombre del cliente
            System.out.print("\nIngrese nombre del cliente (máx 120 caracteres): ");
            String clienteNombre = scanner.nextLine().trim();
            
            if (clienteNombre.isEmpty()) {
                MenuDisplay.mostrarError("El nombre del cliente no puede estar vacío");
                return;
            }
            
            // 4. Total
            System.out.print("\nIngrese total del pedido (número decimal, ej: 5000.00): ");
            Double total = leerDouble();
            
            if (total == null) {
                MenuDisplay.mostrarError("Total inválido");
                return;
            }
            
            // 5. Estado
            System.out.println("\nEstados disponibles:");
            System.out.println("  1. NUEVO");
            System.out.println("  2. FACTURADO");
            System.out.println("  3. ENVIADO");
            System.out.print("Seleccione estado (1-3): ");
            
            EstadoPedido estado = leerEstadoPedido();
            if (estado == null) {
                MenuDisplay.mostrarError("Estado inválido");
                return;
            }
            
            // 6. Crear objeto Pedido (sin envío)
            Pedido pedido = new Pedido(numero, fecha, clienteNombre, total, estado);
            
            // 7. Llamar al service
            pedidoService.crear(pedido);
            
            // 8. Mostrar resultado
            MenuDisplay.mostrarExito("Pedido creado exitosamente con ID: " + pedido.getId());
            System.out.println(pedido);
            
        } catch (IllegalArgumentException e) {
            MenuDisplay.mostrarError("Validación: " + e.getMessage());
        } catch (SQLException e) {
            MenuDisplay.mostrarError("Error de base de datos: " + e.getMessage());
        } catch (Exception e) {
            MenuDisplay.mostrarError("Error inesperado: " + e.getMessage());
            e.printStackTrace();
        }
    }
    */

/**
     * Crea un nuevo pedido CON envío en una TRANSACCIÓN ATÓMICA.
     * Si algo falla, hace rollback de ambos (pedido y envío).
     *
     * Este es el método RECOMENDADO para crear pedidos con envío.
     *
     * Flujo:
     * 1. Solicitar datos del PEDIDO (número, fecha, cliente, total, estado)
     * 2. Solicitar datos del ENVÍO (tracking, empresa, tipo, costo, fechas, estado)
     * 3. Crear objeto Pedido
     * 4. Crear objeto Envio
     * 5. Asociar envío al pedido
     * 6. Llamar a pedidoService.crearPedidoConEnvio() → TRANSACCIÓN
     * 7. Mostrar resultado (éxito o rollback)
     *
     * IMPORTANTE: Si falla la creación del envío o del pedido,
     * TODA la operación se deshace (rollback).
     */
    public void crearPedidoConEnvio() {
        MenuDisplay.mostrarSeparador();
        System.out.println("CREAR PEDIDO CON ENVÍO (TRANSACCIÓN)");
        MenuDisplay.mostrarSeparador();
        
        try {
            // ═══ PASO 1: DATOS DEL PEDIDO ═══
            System.out.println("\nDATOS DEL PEDIDO:");
            MenuDisplay.mostrarSeparador();
            
            System.out.print("Número de pedido (máx 20 caracteres): ");
            String numero = scanner.nextLine().trim();
            
            if (numero.isEmpty()) {
                MenuDisplay.mostrarError("El número de pedido no puede estar vacío");
                return;
            }
            
            System.out.print("\nFecha del pedido (dd/MM/yyyy): ");
            String fechaStr = scanner.nextLine().trim();
            LocalDate fecha = parsearFecha(fechaStr);
            
            if (fecha == null) {
                MenuDisplay.mostrarError("Fecha inválida. Use formato dd/MM/yyyy");
                return;
            }
            
            System.out.print("\nNombre del cliente (máx 120 caracteres): ");
            String clienteNombre = scanner.nextLine().trim();
            
            if (clienteNombre.isEmpty()) {
                MenuDisplay.mostrarError("El nombre del cliente no puede estar vacío");
                return;
            }
            
            System.out.print("\nTotal del pedido (número decimal): ");
            Double total = leerDouble();
            
            if (total == null) {
                MenuDisplay.mostrarError("Total inválido");
                return;
            }
            
            System.out.println("\nEstados de pedido disponibles:");
            System.out.println("  1. NUEVO");
            System.out.println("  2. FACTURADO");
            System.out.println("  3. ENVIADO");
            System.out.print("Seleccione estado (1-3): ");
            
            EstadoPedido estadoPedido = leerEstadoPedido();
            if (estadoPedido == null) {
                MenuDisplay.mostrarError("Estado inválido");
                return;
            }
            
            // ═══ PASO 2: DATOS DEL ENVÍO ═══
            System.out.println("\nDATOS DEL ENVÍO:");
            MenuDisplay.mostrarSeparador();
            
            System.out.print("Tracking (máx 40 caracteres): ");
            String tracking = scanner.nextLine().trim();
            
            if (tracking.isEmpty()) {
                MenuDisplay.mostrarError("El tracking no puede estar vacío");
                return;
            }
            
            System.out.println("\nEmpresas disponibles:");
            System.out.println("  1. ANDREANI");
            System.out.println("  2. OCA");
            System.out.println("  3. CORREO_ARG");
            System.out.print("Seleccione empresa (1-3): ");
            
            EmpresaEnvio empresa = leerEmpresaEnvio();
            if (empresa == null) {
                MenuDisplay.mostrarError("Empresa inválida");
                return;
            }
            
            System.out.println("\nTipos disponibles:");
            System.out.println("  1. ESTANDAR");
            System.out.println("  2. EXPRES");
            System.out.print("Seleccione tipo (1-2): ");
            
            TipoEnvio tipo = leerTipoEnvio();
            if (tipo == null) {
                MenuDisplay.mostrarError("Tipo inválido");
                return;
            }
            
            System.out.print("\nCosto del envío (número decimal): ");
            Double costo = leerDouble();
            if (costo == null) {
                MenuDisplay.mostrarError("Costo inválido");
                return;
            }
            
            System.out.print("\nFecha de despacho (dd/MM/yyyy) o Enter para omitir: ");
            String fechaDespachoStr = scanner.nextLine().trim();
            LocalDate fechaDespacho = null;
            
            if (!fechaDespachoStr.isEmpty()) {
                fechaDespacho = parsearFecha(fechaDespachoStr);
                if (fechaDespacho == null) {
                    MenuDisplay.mostrarError("Fecha de despacho inválida");
                    return;
                }
            }
            
            System.out.print("Fecha estimada (dd/MM/yyyy) o Enter para omitir: ");
            String fechaEstimadaStr = scanner.nextLine().trim();
            LocalDate fechaEstimada = null;
            
            if (!fechaEstimadaStr.isEmpty()) {
                fechaEstimada = parsearFecha(fechaEstimadaStr);
                if (fechaEstimada == null) {
                    MenuDisplay.mostrarError("Fecha estimada inválida");
                    return;
                }
            }
            
            System.out.println("\nEstados de envío disponibles:");
            System.out.println("  1. EN_PREPARACION");
            System.out.println("  2. EN_TRANSITO");
            System.out.println("  3. ENTREGADO");
            System.out.print("Seleccione estado (1-3): ");
            
            EstadoEnvio estadoEnvio = leerEstadoEnvio();
            if (estadoEnvio == null) {
                MenuDisplay.mostrarError("Estado inválido");
                return;
            }
            
            // ═══ PASO 3: CREAR OBJETOS ═══
            Envio envio = new Envio(tracking, empresa, tipo, costo, fechaDespacho, fechaEstimada, estadoEnvio);
            Pedido pedido = new Pedido(numero, fecha, clienteNombre, total, estadoPedido, envio);
            
            // ═══ PASO 4: ASOCIAR ENVÍO AL PEDIDO ═══
            // pedido.setEnvio(envio);
            
            // ═══ PASO 5: TRANSACCIÓN ═══
            System.out.println("\nCreando pedido con envío en transacción...");
            pedidoService.crearPedidoConEnvio(pedido);
            
            // ═══ PASO 6: RESULTADO ═══
            MenuDisplay.mostrarExito("Pedido y envío creados exitosamente en transacción");
            System.out.println("\nEnvío creado - ID: " + envio.getId() + " | Tracking: " + envio.getTracking());
            System.out.println("Pedido creado - ID: " + pedido.getId() + " | Número: " + pedido.getNumero());
            
        } catch (IllegalArgumentException e) {
            MenuDisplay.mostrarError("Validación: " + e.getMessage());
        } catch (SQLException e) {
            MenuDisplay.mostrarError("Error de base de datos (rollback realizado): " + e.getMessage());
        } catch (Exception e) {
            MenuDisplay.mostrarError("Error inesperado (rollback realizado): " + e.getMessage());
            e.printStackTrace();
        }
    }
    

/**
     * Lista todos los pedidos activos (no eliminados) con sus envíos.
     *
     * Flujo:
     * 1. Llamar a pedidoService.obtenerTodos()
     * 2. Si no hay pedidos, mostrar mensaje
     * 3. Si hay pedidos, mostrarlos con sus envíos asociados
     */
    public void listarPedidos() {
        MenuDisplay.mostrarSeparador();
        System.out.println("LISTADO DE PEDIDOS");
        MenuDisplay.mostrarSeparador();
        
        try {
            List<Pedido> pedidos = pedidoService.obtenerTodos();
            
            if (pedidos.isEmpty()) {
                System.out.println("\nNo hay pedidos registrados.");
                return;
            }
            
            System.out.println("\nTotal de pedidos: " + pedidos.size());
            System.out.println();
            
            for (Pedido pedido : pedidos) {
                mostrarPedido(pedido);
                System.out.println();
            }
            
        } catch (SQLException e) {
            MenuDisplay.mostrarError("Error al listar pedidos: " + e.getMessage());
        } catch (Exception e) {
            MenuDisplay.mostrarError("Error inesperado: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Busca un pedido por su ID.
     *
     * Flujo:
     * 1. Solicitar ID
     * 2. Llamar a pedidoService.obtenerPorId()
     * 3. Mostrar pedido encontrado con su envío (si tiene)
     */
    public void buscarPedidoPorId() {
        MenuDisplay.mostrarSeparador();
        System.out.println("BUSCAR PEDIDO POR ID");
        MenuDisplay.mostrarSeparador();
        
        try {
            System.out.print("\nIngrese ID del pedido: ");
            Long id = leerLong();
            
            if (id == null) {
                MenuDisplay.mostrarError("ID inválido");
                return;
            }
            
            Pedido pedido = pedidoService.obtenerPorId(id);
            
            System.out.println("\nPedido encontrado:");
            mostrarPedido(pedido);
            
        } catch (IllegalArgumentException e) {
            MenuDisplay.mostrarError(e.getMessage());
        } catch (SQLException e) {
            MenuDisplay.mostrarError("Error de base de datos: " + e.getMessage());
        } catch (Exception e) {
            MenuDisplay.mostrarError("Error inesperado: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Busca un pedido por su número.
     *
     * Flujo:
     * 1. Solicitar número
     * 2. Llamar a pedidoService.buscarPorNumero()
     * 3. Mostrar pedido encontrado o mensaje si no existe
     */
    public void buscarPedidoPorNumero() {
        MenuDisplay.mostrarSeparador();
        System.out.println("BUSCAR PEDIDO POR NÚMERO");
        MenuDisplay.mostrarSeparador();
        
        try {
            System.out.print("\nIngrese número de pedido: ");
            String numero = scanner.nextLine().trim();
            
            if (numero.isEmpty()) {
                MenuDisplay.mostrarError("El número de pedido no puede estar vacío");
                return;
            }
            
            Pedido pedido = pedidoService.buscarPorNumero(numero);
            
            if (pedido == null) {
                System.out.println("\nNo se encontró pedido con número: " + numero);
                return;
            }
            
            System.out.println("\nPedido encontrado:");
            mostrarPedido(pedido);
            
        } catch (IllegalArgumentException e) {
            MenuDisplay.mostrarError(e.getMessage());
        } catch (SQLException e) {
            MenuDisplay.mostrarError("Error de base de datos: " + e.getMessage());
        } catch (Exception e) {
            MenuDisplay.mostrarError("Error inesperado: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
/**
     * Actualiza un pedido existente.
     *
     * Flujo:
     * 1. Solicitar ID del pedido a actualizar
     * 2. Buscar pedido actual
     * 3. Mostrar datos actuales
     * 4. Solicitar nuevos datos (permitir Enter para mantener actual)
     * 5. Actualizar objeto Pedido
     * 6. Llamar a pedidoService.actualizar()
     * 7. Mostrar resultado
     *
     * NOTA: Esta opción NO actualiza el envío asociado.
     * Para actualizar el envío, usar opción 5 (Actualizar envío).
     */
    public void actualizarPedido() {
        MenuDisplay.mostrarSeparador();
        System.out.println("ACTUALIZAR PEDIDO");
        MenuDisplay.mostrarSeparador();
        
        try {
            // 1. Buscar pedido a actualizar
            System.out.print("\nIngrese ID del pedido a actualizar: ");
            Long id = leerLong();
            
            if (id == null) {
                MenuDisplay.mostrarError("ID inválido");
                return;
            }
            
            Pedido pedidoActual = pedidoService.obtenerPorId(id);
            
            // 2. Mostrar datos actuales
            System.out.println("\nDatos actuales del pedido:");
            mostrarPedido(pedidoActual);
            
            System.out.println("\nPresione Enter para mantener el valor actual");
            MenuDisplay.mostrarSeparador();
            
            // 3. Solicitar nuevos datos
            System.out.print("\nNúmero actual [" + pedidoActual.getNumero() + "]: ");
            String numero = scanner.nextLine().trim();
            if (!numero.isEmpty()) {
                pedidoActual.setNumero(numero);
            }
            
            System.out.print("\nFecha actual [" + pedidoActual.getFecha().format(DATE_FORMATTER) + "] (dd/mm/aaaa): ");
            String fechaStr = scanner.nextLine().trim();
            if (!fechaStr.isEmpty()) {
                LocalDate fecha = parsearFecha(fechaStr);
                if (fecha != null) {
                    pedidoActual.setFecha(fecha);
                }
            }
            
            System.out.print("\nCliente actual [" + pedidoActual.getClienteNombre() + "]: ");
            String clienteNombre = scanner.nextLine().trim();
            if (!clienteNombre.isEmpty()) {
                pedidoActual.setClienteNombre(clienteNombre);
            }
            
            System.out.print("\nTotal actual [" + pedidoActual.getTotal() + "]: ");
            String totalStr = scanner.nextLine().trim();
            if (!totalStr.isEmpty()) {
                Double total = parseDouble(totalStr);
                if (total != null) {
                    pedidoActual.setTotal(total);
                }
            }
            
            System.out.println("\nEstado actual [" + pedidoActual.getEstado() + "]");
            System.out.println("Estados disponibles:");
            System.out.println("  1. NUEVO");
            System.out.println("  2. FACTURADO");
            System.out.println("  3. ENVIADO");
            System.out.print("Nuevo estado (1-3) o Enter: ");
            String estadoInput = scanner.nextLine().trim();
            if (!estadoInput.isEmpty()) {
                EstadoPedido estado = leerEstadoPedidoDesdeString(estadoInput);
                if (estado != null) {
                    pedidoActual.setEstado(estado);
                }
            }
            
            // 4. Actualizar
            pedidoService.actualizar(pedidoActual);
            
            // 5. Mostrar resultado
            MenuDisplay.mostrarExito("Pedido actualizado exitosamente");
            mostrarPedido(pedidoActual);
            
        } catch (IllegalArgumentException e) {
            MenuDisplay.mostrarError("Validación: " + e.getMessage());
        } catch (SQLException e) {
            MenuDisplay.mostrarError("Error de base de datos: " + e.getMessage());
        } catch (Exception e) {
            MenuDisplay.mostrarError("Error inesperado: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Elimina un pedido (soft delete).
     *
     * Flujo:
     * 1. Solicitar ID del pedido a eliminar
     * 2. Buscar pedido
     * 3. Mostrar datos y pedir confirmación
     * 4. Llamar a pedidoService.eliminar()
     * 5. Mostrar resultado
     *
     * NOTA: Eliminar un pedido NO elimina el envío asociado.
     * El envío queda activo en el sistema.
     */
    public void eliminarPedido() {
        MenuDisplay.mostrarSeparador();
        System.out.println("ELIMINAR PEDIDO");
        MenuDisplay.mostrarSeparador();
        
        try {
            System.out.print("\nIngrese ID del pedido a eliminar: ");
            Long id = leerLong();
            
            if (id == null) {
                MenuDisplay.mostrarError("ID inválido");
                return;
            }
            
            Pedido pedido = pedidoService.obtenerPorId(id);
            
            System.out.println("\nPedido a eliminar:");
            mostrarPedido(pedido);
            
            if (pedido.tieneEnvio()) {
                MenuDisplay.mostrarAdvertencia("Este pedido tiene un envío asociado.");
                System.out.println("El envío NO será eliminado, solo el pedido.");
            }
            
            System.out.print("\n¿Está seguro de eliminar este pedido? (S/N): ");
            String confirmacion = scanner.nextLine().trim().toUpperCase();
            
            if (!confirmacion.equals("S")) {
                System.out.println("Operación cancelada");
                return;
            }
            
            pedidoService.eliminar(id);
            
            MenuDisplay.mostrarExito("Pedido eliminado exitosamente");
            
        } catch (IllegalArgumentException e) {
            MenuDisplay.mostrarError(e.getMessage());
        } catch (SQLException e) {
            MenuDisplay.mostrarError("Error de base de datos: " + e.getMessage());
        } catch (Exception e) {
            MenuDisplay.mostrarError("Error inesperado: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
// ═══════════════════════════════════════════════════════════
    // MÉTODOS AUXILIARES - LECTORES DE ENUMS
    // ═══════════════════════════════════════════════════════════
    
    /**
     * Lee y convierte input del usuario a EmpresaEnvio.
     * 
     * @return EmpresaEnvio seleccionada o null si es inválida
     */
    private EmpresaEnvio leerEmpresaEnvio() {
        String input = scanner.nextLine().trim();
        return leerEmpresaEnvioDesdeString(input);
    }
    
    /**
     * Convierte un String a EmpresaEnvio.
     * 
     * @param input String con opción (1-3)
     * @return EmpresaEnvio correspondiente o null
     */
    private EmpresaEnvio leerEmpresaEnvioDesdeString(String input) {
        try {
            int opcion = Integer.parseInt(input);
            return switch (opcion) {
                case 1 -> EmpresaEnvio.ANDREANI;
                case 2 -> EmpresaEnvio.OCA;
                case 3 -> EmpresaEnvio.CORREO_ARG;
                default -> null;
            };
        } catch (NumberFormatException e) {
            return null;
        }
    }
    
    /**
     * Lee y convierte input del usuario a TipoEnvio.
     * 
     * @return TipoEnvio seleccionado o null si es inválido
     */
    private TipoEnvio leerTipoEnvio() {
        String input = scanner.nextLine().trim();
        return leerTipoEnvioDesdeString(input);
    }
    
    /**
     * Convierte un String a TipoEnvio.
     * 
     * @param input String con opción (1-2)
     * @return TipoEnvio correspondiente o null
     */
    private TipoEnvio leerTipoEnvioDesdeString(String input) {
        try {
            int opcion = Integer.parseInt(input);
            return switch (opcion) {
                case 1 -> TipoEnvio.ESTANDAR;
                case 2 -> TipoEnvio.EXPRES;
                default -> null;
            };
        } catch (NumberFormatException e) {
            return null;
        }
    }
    
    /**
     * Lee y convierte input del usuario a EstadoEnvio.
     * 
     * @return EstadoEnvio seleccionado o null si es inválido
     */
    private EstadoEnvio leerEstadoEnvio() {
        String input = scanner.nextLine().trim();
        return leerEstadoEnvioDesdeString(input);
    }
    
    /**
     * Convierte un String a EstadoEnvio.
     * 
     * @param input String con opción (1-3)
     * @return EstadoEnvio correspondiente o null
     */
    private EstadoEnvio leerEstadoEnvioDesdeString(String input) {
        try {
            int opcion = Integer.parseInt(input);
            return switch (opcion) {
                case 1 -> EstadoEnvio.EN_PREPARACION;
                case 2 -> EstadoEnvio.EN_TRANSITO;
                case 3 -> EstadoEnvio.ENTREGADO;
                default -> null;
            };
        } catch (NumberFormatException e) {
            return null;
        }
    }
    
    /**
     * Lee y convierte input del usuario a EstadoPedido.
     * 
     * @return EstadoPedido seleccionado o null si es inválido
     */
    private EstadoPedido leerEstadoPedido() {
        String input = scanner.nextLine().trim();
        return leerEstadoPedidoDesdeString(input);
    }
    
    /**
     * Convierte un String a EstadoPedido.
     * 
     * @param input String con opción (1-3)
     * @return EstadoPedido correspondiente o null
     */
    private EstadoPedido leerEstadoPedidoDesdeString(String input) {
        try {
            int opcion = Integer.parseInt(input);
            return switch (opcion) {
                case 1 -> EstadoPedido.NUEVO;
                case 2 -> EstadoPedido.FACTURADO;
                case 3 -> EstadoPedido.ENVIADO;
                default -> null;
            };
        } catch (NumberFormatException e) {
            return null;
        }
    }
    
// ═══════════════════════════════════════════════════════════
    // MÉTODOS AUXILIARES - LECTORES Y PARSEADORES
    // ═══════════════════════════════════════════════════════════
    
    /**
     * Lee un Long desde el scanner.
     * 
     * @return Long leído o null si es inválido
     */
    private Long leerLong() {
        try {
            String input = scanner.nextLine().trim();
            return Long.parseLong(input);
        } catch (NumberFormatException e) {
            return null;
        }
    }
    
    /**
     * Lee un Double desde el scanner.
     * 
     * @return Double leído o null si es inválido
     */
    private Double leerDouble() {
        try {
            String input = scanner.nextLine().trim();
            return Double.parseDouble(input);
        } catch (NumberFormatException e) {
            return null;
        }
    }
    
    /**
     * Parsea un String a Double.
     * 
     * @param input String con el número
     * @return Double parseado o null si es inválido
     */
    private Double parseDouble(String input) {
        try {
            return Double.parseDouble(input);
        } catch (NumberFormatException e) {
            return null;
        }
    }
    
    /**
     * Parsea una fecha en formato dd/MM/yyyy.
     * 
     * @param fechaStr String con la fecha
     * @return LocalDate parseada o null si es inválida
     */
    private LocalDate parsearFecha(String fechaStr) {
        try {
            return LocalDate.parse(fechaStr, DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            return null;
        }
    }
    
// ═══════════════════════════════════════════════════════════
    // MÉTODOS AUXILIARES - FORMATEO Y VISUALIZACIÓN
    // ═══════════════════════════════════════════════════════════
    
    /**
     * Muestra un envío en formato legible.
     * 
     * @param envio Envío a mostrar
     */
    private void mostrarEnvio(Envio envio) {
        System.out.println("╔══════════════════════════════════════════════════╗");
        System.out.println("║                 ENVÍO #" + envio.getId() + "                   ║");
        System.out.println("╠══════════════════════════════════════════════════╣");
        System.out.printf("║ Tracking:        %-25s ║%n", envio.getTracking());
        System.out.printf("║ Empresa:         %-25s ║%n", envio.getEmpresa());
        System.out.printf("║ Tipo:            %-25s ║%n", envio.getTipo());
        System.out.printf("║ Costo:           $%-24.2f ║%n", envio.getCosto());
        System.out.printf("║ F. Despacho:     %-25s ║%n", 
            envio.getFechaDespacho() != null ? envio.getFechaDespacho().format(DATE_FORMATTER) : "Sin fecha");
        System.out.printf("║ F. Estimada:     %-25s ║%n", 
            envio.getFechaEstimada() != null ? envio.getFechaEstimada().format(DATE_FORMATTER) : "Sin fecha");
        System.out.printf("║ Estado:          %-25s ║%n", envio.getEstado());
        System.out.println("╚══════════════════════════════════════════════════╝");
    }
    
    /**
     * Muestra un pedido con su envío asociado (si tiene).
     * 
     * @param pedido Pedido a mostrar
     */
    private void mostrarPedido(Pedido pedido) {
        System.out.println("╔══════════════════════════════════════════════════╗");
        System.out.println("║                PEDIDO #" + pedido.getId() + "                   ║");
        System.out.println("╠══════════════════════════════════════════════════╣");
        System.out.printf("║ Número:          %-25s ║%n", pedido.getNumero());
        System.out.printf("║ Fecha:           %-25s ║%n", pedido.getFecha().format(DATE_FORMATTER));
        System.out.printf("║ Cliente:         %-25s ║%n", pedido.getClienteNombre());
        System.out.printf("║ Total:           $%-24.2f ║%n", pedido.getTotal());
        System.out.printf("║ Estado:          %-25s ║%n", pedido.getEstado());
        
        if (pedido.tieneEnvio()) {
            Envio envio = pedido.getEnvio();
            System.out.println("╠══════════════════════════════════════════════════╣");
            System.out.println("║               ENVÍO ASOCIADO               ║");
            System.out.println("╠══════════════════════════════════════════════════╣");
            System.out.printf("║ ID Envío:        %-25s ║%n", envio.getId());
            System.out.printf("║ Tracking:        %-25s ║%n", envio.getTracking());
            System.out.printf("║ Empresa:         %-25s ║%n", envio.getEmpresa());
            System.out.printf("║ Estado Envío:    %-25s ║%n", envio.getEstado());
        } else {
            System.out.println("╠══════════════════════════════════════════════════╣");
            System.out.println("║             SIN ENVÍO ASOCIADO             ║");
        }
        
        System.out.println("╚══════════════════════════════════════════════════╝");
    }
}
