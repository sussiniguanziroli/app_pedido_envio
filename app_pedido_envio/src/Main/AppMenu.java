package Main;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;

import Config.DatabaseConnection;
import Dao.EnvioDAO;
import Dao.PedidoDAO;
import Models.*;
import Service.EnvioServiceImpl;
import Service.PedidoServiceImpl;

/**
 * Menú principal de la aplicación Pedido-Envio.
 * Versión simplificada para empezar.
 */
public class AppMenu {
    private final Scanner scanner;
    private final PedidoServiceImpl pedidoService;
    private final EnvioServiceImpl envioService;
    private final DateTimeFormatter dateFormatter;
    private boolean running;
    
    public AppMenu() {
        this.scanner = new Scanner(System.in);
        this.dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        
        // Inicializar servicios
        EnvioDAO envioDAO = new EnvioDAO();
        PedidoDAO pedidoDAO = new PedidoDAO();
        this.envioService = new EnvioServiceImpl(envioDAO);
        this.pedidoService = new PedidoServiceImpl(pedidoDAO, envioService);
        
        this.running = true;
    }
    
    public static void main(String[] args) {
        AppMenu app = new AppMenu();
        app.run();
    }
    
    public void run() {
        System.out.println("=================================");
        System.out.println("  SISTEMA PEDIDO-ENVÍO");
        System.out.println("=================================\n");
        
        while (running) {
            try {
                mostrarMenu();
                int opcion = leerEntero("Seleccione una opción: ");
                procesarOpcion(opcion);
            } catch (Exception e) {
                System.err.println("❌ Error: " + e.getMessage());
            }
        }
        
        scanner.close();
        System.out.println("\n¡Hasta luego!");
    }
    
    private void mostrarMenu() {
        System.out.println("\n╔════════════════════════════════════╗");
        System.out.println("║          MENÚ PRINCIPAL            ║");
        System.out.println("╠════════════════════════════════════╣");
        System.out.println("║  PEDIDOS                           ║");
        System.out.println("║  1. Crear pedido con envío         ║");
        System.out.println("║  2. Listar todos los pedidos       ║");
        System.out.println("║  3. Buscar pedido por número       ║");
        System.out.println("║  4. Actualizar pedido              ║");
        System.out.println("║  5. Eliminar pedido                ║");
        System.out.println("║                                    ║");
        System.out.println("║  ENVÍOS                            ║");
        System.out.println("║  6. Listar todos los envíos        ║");
        System.out.println("║  7. Buscar envío por tracking      ║");
        System.out.println("║  8. Actualizar estado de envío     ║");
        System.out.println("║                                    ║");
        System.out.println("║  0. Salir                          ║");
        System.out.println("╚════════════════════════════════════╝");
    }
    
    private void procesarOpcion(int opcion) {
        try {
            switch (opcion) {
                case 1 -> crearPedidoConEnvio();
                case 2 -> listarPedidos();
                case 3 -> buscarPedidoPorNumero();
                case 4 -> actualizarPedido();
                case 5 -> eliminarPedido();
                case 6 -> listarEnvios();
                case 7 -> buscarEnvioPorTracking();
                case 8 -> actualizarEstadoEnvio();
                case 0 -> {
                    System.out.println("\n👋 Cerrando aplicación...");
                    running = false;
                }
                default -> System.out.println("❌ Opción inválida");
            }
        } catch (Exception e) {
            System.err.println("❌ Error: " + e.getMessage());
        }
    }
    
    // ==================== OPERACIONES DE PEDIDO ====================
    
    private void crearPedidoConEnvio() {
        try {
            System.out.println("\n--- CREAR PEDIDO CON ENVÍO ---");
            
            // Datos del pedido
            String numero = leerTexto("Número de pedido: ").toUpperCase();
            LocalDate fecha = leerFecha("Fecha (dd/MM/yyyy): ");
            String clienteNombre = leerTexto("Nombre del cliente: ");
            double total = leerDouble("Total: $");
            EstadoPedido estadoPedido = seleccionarEstadoPedido();
            
            // Datos del envío
            System.out.println("\n--- Datos del Envío ---");
            String tracking = leerTexto("Código de tracking: ").toUpperCase();
            EmpresaEnvio empresa = seleccionarEmpresa();
            TipoEnvio tipo = seleccionarTipoEnvio();
            double costo = leerDouble("Costo del envío: $");
            EstadoEnvio estadoEnvio = seleccionarEstadoEnvio();
            
            // Crear objetos
            Envio envio = new Envio();
            envio.setTracking(tracking);
            envio.setEmpresa(empresa);
            envio.setTipo(tipo);
            envio.setCosto(costo);
            envio.setEstado(estadoEnvio);
            
            Pedido pedido = new Pedido();
            pedido.setNumero(numero);
            pedido.setFecha(fecha);
            pedido.setClienteNombre(clienteNombre);
            pedido.setTotal(total);
            pedido.setEstado(estadoPedido);
            pedido.setEnvio(envio);
            
            // Guardar con transacción
            pedidoService.crearPedidoConEnvio(pedido);
            
            System.out.println("✅ Pedido creado exitosamente!");
            System.out.println("   ID Pedido: " + pedido.getId());
            System.out.println("   ID Envío: " + envio.getId());
            
        } catch (Exception e) {
            System.err.println("❌ Error al crear pedido: " + e.getMessage());
        }
    }
    
    private void listarPedidos() {
        try {
            System.out.println("\n--- LISTA DE PEDIDOS ---");
            List<Pedido> pedidos = pedidoService.getAll();
            
            if (pedidos.isEmpty()) {
                System.out.println("📋 No hay pedidos registrados.");
                return;
            }
            
            System.out.println("📋 Total de pedidos: " + pedidos.size() + "\n");
            
            for (Pedido p : pedidos) {
                System.out.println("┌─────────────────────────────────────");
                System.out.println("│ ID: " + p.getId());
                System.out.println("│ Número: " + p.getNumero());
                System.out.println("│ Fecha: " + p.getFecha().format(dateFormatter));
                System.out.println("│ Cliente: " + p.getClienteNombre());
                System.out.println("│ Total: $" + String.format("%.2f", p.getTotal()));
                System.out.println("│ Estado: " + p.getEstado());
                
                if (p.getEnvio() != null) {
                    System.out.println("│ Envío: " + p.getEnvio().getTracking() + 
                                     " (" + p.getEnvio().getEstado() + ")");
                }
                
                System.out.println("└─────────────────────────────────────");
            }
            
        } catch (Exception e) {
            System.err.println("❌ Error al listar pedidos: " + e.getMessage());
        }
    }
    
    private void buscarPedidoPorNumero() {
        try {
            System.out.println("\n--- BUSCAR PEDIDO ---");
            String numero = leerTexto("Ingrese el número de pedido: ").toUpperCase();
            
            Pedido pedido = pedidoService.buscarPorNumero(numero);
            
            if (pedido == null) {
                System.out.println("❌ No se encontró pedido con número: " + numero);
                return;
            }
            
            mostrarDetallePedido(pedido);
            
        } catch (Exception e) {
            System.err.println("❌ Error al buscar pedido: " + e.getMessage());
        }
    }
    
    private void actualizarPedido() {
        try {
            System.out.println("\n--- ACTUALIZAR PEDIDO ---");
            int id = leerEntero("ID del pedido a actualizar: ");
            
            Pedido pedido = pedidoService.getById(id);
            if (pedido == null) {
                System.out.println("❌ No se encontró pedido con ID: " + id);
                return;
            }
            
            System.out.println("\n📋 Pedido actual:");
            mostrarDetallePedido(pedido);
            
            System.out.println("\n--- Nuevos datos (Enter para mantener) ---");
            
            String numero = leerTextoOpcional("Número [" + pedido.getNumero() + "]: ");
            if (!numero.isEmpty()) {
                pedido.setNumero(numero.toUpperCase());
            }
            
            String cliente = leerTextoOpcional("Cliente [" + pedido.getClienteNombre() + "]: ");
            if (!cliente.isEmpty()) {
                pedido.setClienteNombre(cliente);
            }
            
            System.out.print("Total [" + pedido.getTotal() + "]: $");
            String totalStr = scanner.nextLine();
            if (!totalStr.isEmpty()) {
                pedido.setTotal(Double.parseDouble(totalStr));
            }
            
            System.out.println("\nEstado actual: " + pedido.getEstado());
            System.out.print("¿Cambiar estado? (S/N): ");
            if (scanner.nextLine().trim().toUpperCase().equals("S")) {
                pedido.setEstado(seleccionarEstadoPedido());
            }
            
            pedidoService.actualizar(pedido);
            System.out.println("✅ Pedido actualizado exitosamente!");
            
        } catch (Exception e) {
            System.err.println("❌ Error al actualizar pedido: " + e.getMessage());
        }
    }
    
    private void eliminarPedido() {
        try {
            System.out.println("\n--- ELIMINAR PEDIDO ---");
            int id = leerEntero("ID del pedido a eliminar: ");
            
            Pedido pedido = pedidoService.getById(id);
            if (pedido == null) {
                System.out.println("❌ No se encontró pedido con ID: " + id);
                return;
            }
            
            mostrarDetallePedido(pedido);
            
            System.out.print("\n⚠️  ¿Está seguro de eliminar este pedido? (S/N): ");
            String confirmacion = scanner.nextLine().trim().toUpperCase();
            
            if (confirmacion.equals("S")) {
                pedidoService.eliminar(id);
                System.out.println("✅ Pedido eliminado exitosamente!");
            } else {
                System.out.println("❌ Operación cancelada.");
            }
            
        } catch (Exception e) {
            System.err.println("❌ Error al eliminar pedido: " + e.getMessage());
        }
    }
    
    // ==================== OPERACIONES DE ENVÍO ====================
    
    private void listarEnvios() {
        try {
            System.out.println("\n--- LISTA DE ENVÍOS ---");
            List<Envio> envios = envioService.getAll();
            
            if (envios.isEmpty()) {
                System.out.println("📦 No hay envíos registrados.");
                return;
            }
            
            System.out.println("📦 Total de envíos: " + envios.size() + "\n");
            
            for (Envio e : envios) {
                System.out.println("┌─────────────────────────────────────");
                System.out.println("│ ID: " + e.getId());
                System.out.println("│ Tracking: " + e.getTracking());
                System.out.println("│ Empresa: " + e.getEmpresa());
                System.out.println("│ Tipo: " + e.getTipo());
                System.out.println("│ Costo: $" + String.format("%.2f", e.getCosto()));
                System.out.println("│ Estado: " + e.getEstado());
                System.out.println("└─────────────────────────────────────");
            }
            
        } catch (Exception e) {
            System.err.println("❌ Error al listar envíos: " + e.getMessage());
        }
    }
    
    private void buscarEnvioPorTracking() {
        try {
            System.out.println("\n--- BUSCAR ENVÍO ---");
            String tracking = leerTexto("Ingrese el código de tracking: ").toUpperCase();
            
            Envio envio = envioService.buscarPorTracking(tracking);
            
            if (envio == null) {
                System.out.println("❌ No se encontró envío con tracking: " + tracking);
                return;
            }
            
            System.out.println("\n📦 Envío encontrado:");
            System.out.println("ID: " + envio.getId());
            System.out.println("Tracking: " + envio.getTracking());
            System.out.println("Empresa: " + envio.getEmpresa());
            System.out.println("Tipo: " + envio.getTipo());
            System.out.println("Costo: $" + String.format("%.2f", envio.getCosto()));
            System.out.println("Estado: " + envio.getEstado());
            
        } catch (Exception e) {
            System.err.println("❌ Error al buscar envío: " + e.getMessage());
        }
    }
    
    private void actualizarEstadoEnvio() {
        try {
            System.out.println("\n--- ACTUALIZAR ESTADO DE ENVÍO ---");
            int id = leerEntero("ID del envío: ");
            
            Envio envio = envioService.getById(id);
            if (envio == null) {
                System.out.println("❌ No se encontró envío con ID: " + id);
                return;
            }
            
            System.out.println("\nEnvío: " + envio.getTracking());
            System.out.println("Estado actual: " + envio.getEstado());
            
            EstadoEnvio nuevoEstado = seleccionarEstadoEnvio();
            envio.setEstado(nuevoEstado);
            
            envioService.actualizar(envio);
            System.out.println("✅ Estado actualizado exitosamente!");
            
        } catch (Exception e) {
            System.err.println("❌ Error al actualizar envío: " + e.getMessage());
        }
    }
    
    // ==================== MÉTODOS AUXILIARES ====================
    
    private void mostrarDetallePedido(Pedido p) {
        System.out.println("\n╔═════════════════════════════════════╗");
        System.out.println("║       DETALLE DEL PEDIDO            ║");
        System.out.println("╠═════════════════════════════════════╣");
        System.out.println("║ ID: " + p.getId());
        System.out.println("║ Número: " + p.getNumero());
        System.out.println("║ Fecha: " + p.getFecha().format(dateFormatter));
        System.out.println("║ Cliente: " + p.getClienteNombre());
        System.out.println("║ Total: $" + String.format("%.2f", p.getTotal()));
        System.out.println("║ Estado: " + p.getEstado());
        
        if (p.getEnvio() != null) {
            Envio e = p.getEnvio();
            System.out.println("║─────────────────────────────────────");
            System.out.println("║ ENVÍO:");
            System.out.println("║   Tracking: " + e.getTracking());
            System.out.println("║   Empresa: " + e.getEmpresa());
            System.out.println("║   Tipo: " + e.getTipo());
            System.out.println("║   Costo: $" + String.format("%.2f", e.getCosto()));
            System.out.println("║   Estado: " + e.getEstado());
        }
        
        System.out.println("╚═════════════════════════════════════╝");
    }
    
    private EstadoPedido seleccionarEstadoPedido() {
        System.out.println("\nEstados de pedido:");
        EstadoPedido[] estados = EstadoPedido.values();
        for (int i = 0; i < estados.length; i++) {
            System.out.println((i + 1) + ". " + estados[i]);
        }
        
        int opcion = leerEntero("Seleccione estado (1-" + estados.length + "): ");
        return estados[opcion - 1];
    }
    
    private EmpresaEnvio seleccionarEmpresa() {
        System.out.println("\nEmpresas de envío:");
        EmpresaEnvio[] empresas = EmpresaEnvio.values();
        for (int i = 0; i < empresas.length; i++) {
            System.out.println((i + 1) + ". " + empresas[i]);
        }
        
        int opcion = leerEntero("Seleccione empresa (1-" + empresas.length + "): ");
        return empresas[opcion - 1];
    }
    
    private TipoEnvio seleccionarTipoEnvio() {
        System.out.println("\nTipos de envío:");
        TipoEnvio[] tipos = TipoEnvio.values();
        for (int i = 0; i < tipos.length; i++) {
            System.out.println((i + 1) + ". " + tipos[i]);
        }
        
        int opcion = leerEntero("Seleccione tipo (1-" + tipos.length + "): ");
        return tipos[opcion - 1];
    }
    
    private EstadoEnvio seleccionarEstadoEnvio() {
        System.out.println("\nEstados de envío:");
        EstadoEnvio[] estados = EstadoEnvio.values();
        for (int i = 0; i < estados.length; i++) {
            System.out.println((i + 1) + ". " + estados[i]);
        }
        
        int opcion = leerEntero("Seleccione estado (1-" + estados.length + "): ");
        return estados[opcion - 1];
    }
    
    private String leerTexto(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }
    
    private String leerTextoOpcional(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }
    
    private int leerEntero(String prompt) {
        while (true) {
            try {
                System.out.print(prompt);
                return Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("❌ Por favor ingrese un número válido.");
            }
        }
    }
    
    private double leerDouble(String prompt) {
        while (true) {
            try {
                System.out.print(prompt);
                return Double.parseDouble(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("❌ Por favor ingrese un número válido.");
            }
        }
    }
    
    private LocalDate leerFecha(String prompt) {
        while (true) {
            try {
                System.out.print(prompt);
                String fechaStr = scanner.nextLine().trim();
                return LocalDate.parse(fechaStr, dateFormatter);
            } catch (DateTimeParseException e) {
                System.out.println("❌ Formato de fecha inválido. Use dd/MM/yyyy");
            }
        }
    }
}