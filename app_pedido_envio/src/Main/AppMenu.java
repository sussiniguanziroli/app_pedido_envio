package Main;

import Dao.EnvioDAO;
import Dao.PedidoDAO;
import Service.EnvioServiceImpl;
import Service.PedidoServiceImpl;

import java.util.Scanner;

/**
 * Orquestador principal del menú de la aplicación Pedido-Envio.
 * Gestiona el ciclo de vida del menú y coordina todas las dependencias.
 *
 * Responsabilidades:
 * - Crear y gestionar el Scanner único (evita múltiples instancias de System.in)
 * - Inicializar toda la cadena de dependencias (DAOs → Services → Handler)
 * - Ejecutar el loop principal del menú
 * - Manejar la selección de opciones y delegarlas a MenuHandler
 * - Cerrar recursos al salir (Scanner)
 *
 * Patrón: Application Controller + Dependency Injection manual
 * Arquitectura: Punto de entrada que ensambla las 4 capas (Main → Service → DAO → Models)
 *
 * IMPORTANTE: Esta clase NO tiene lógica de negocio ni de UI.
 * Solo coordina y delega.
 */
public class AppMenu {
    
    /**
     * Scanner único compartido por toda la aplicación.
     * IMPORTANTE: Solo debe haber UNA instancia de Scanner(System.in).
     * Múltiples instancias causan problemas de buffering de entrada.
     */
    private final Scanner scanner;
    
    /**
     * Handler que ejecuta las operaciones del menú.
     * Contiene toda la lógica de interacción con el usuario.
     */
    private final MenuHandler menuHandler;
    
    /**
     * Flag que controla el loop principal del menú.
     * Se setea a false cuando el usuario selecciona "0 - Salir".
     */
    private boolean running;
    
    /**
     * Constructor que inicializa la aplicación.
     *
     * Flujo de inicialización:
     * 1. Crea Scanner único para toda la aplicación
     * 2. Crea cadena de dependencias (DAOs → Services) mediante createPedidoService()
     * 3. Crea MenuHandler con Scanner y PedidoService
     * 4. Setea running=true para iniciar el loop
     *
     * Patrón de inyección de dependencias (DI) manual:
     * - EnvioDAO (sin dependencias)
     * - PedidoDAO (sin dependencias, pero usa EnvioDAO internamente para mapeo)
     * - EnvioServiceImpl (depende de EnvioDAO)
     * - PedidoServiceImpl (depende de PedidoDAO y EnvioServiceImpl)
     * - MenuHandler (depende de Scanner y PedidoServiceImpl)
     *
     * Esta inicialización garantiza que todas las dependencias estén correctamente conectadas.
     */
    public AppMenu() {
        this.scanner = new Scanner(System.in);
        PedidoServiceImpl pedidoService = createPedidoService();
        this.menuHandler = new MenuHandler(scanner, pedidoService);
        this.running = true;
    }
    
    /**
     * Punto de entrada alternativo de la aplicación Java.
     * Crea instancia de AppMenu y ejecuta el menú principal.
     *
     * @param args Argumentos de línea de comandos (no usados)
     */
    public static void main(String[] args) {
        AppMenu app = new AppMenu();
        app.run();
    }
    
    /**
     * Loop principal del menú.
     *
     * Flujo:
     * 1. Mientras running==true:
     *    a. Muestra menú con MenuDisplay.mostrarMenuPrincipal()
     *    b. Lee opción del usuario (scanner.nextLine())
     *    c. Convierte a int (puede lanzar NumberFormatException)
     *    d. Procesa opción con processOption()
     * 2. Si el usuario ingresa texto no numérico: Muestra mensaje de error y continúa
     * 3. Cuando running==false (opción 0): Sale del loop y cierra Scanner
     *
     * Manejo de errores:
     * - NumberFormatException: Captura entrada no numérica (ej: "abc")
     * - Muestra mensaje amigable y NO termina la aplicación
     * - El usuario puede volver a intentar
     *
     * IMPORTANTE: El Scanner se cierra al salir del loop.
     * Cerrar Scanner(System.in) cierra System.in para toda la aplicación.
     */
    public void run() {
        while (running) {
            try {
                MenuDisplay.mostrarMenuPrincipal();
                int opcion = Integer.parseInt(scanner.nextLine());
                processOption(opcion);
            } catch (NumberFormatException e) {
                System.out.println("Entrada inválida. Por favor, ingrese un número.");
            }
        }
        scanner.close();
        System.out.println("Aplicación cerrada correctamente.");
    }
    
    /**
     * Procesa la opción seleccionada por el usuario y delega a MenuHandler.
     *
     * Switch expression (Java 14+) con operador arrow (->):
     * - Más conciso que switch tradicional
     * - No requiere break (cada caso es independiente)
     * - Permite bloques con {} para múltiples statements
     *
     * Mapeo de opciones (corresponde a MenuDisplay):
     * === ENVÍOS ===
     * 1  → Crear envío
     * 2  → Listar envíos
     * 3  → Buscar envío por ID
     * 4  → Buscar envío por tracking
     * 5  → Actualizar envío
     * 6  → Eliminar envío (soft delete)
     * 
     * === PEDIDOS ===
     // * 7  → Crear pedido sin envío
     * 8  → Crear pedido con envío (transacción)
     * 9  → Listar pedidos
     * 10 → Buscar pedido por ID
     * 11 → Buscar pedido por número
     * 12 → Actualizar pedido
     * 13 → Eliminar pedido (soft delete)
     * 
     * 0  → Salir (setea running=false para terminar el loop)
     *
     * Opción inválida: Muestra mensaje y continúa el loop.
     *
     * IMPORTANTE: Todas las excepciones de MenuHandler se capturan dentro de los métodos.
     * processOption() NO propaga excepciones al caller (run()).
     *
     * @param opcion Número de opción ingresado por el usuario
     */
    private void processOption(int opcion) {
        switch (opcion) {
            // === ENVÍOS ===
            case 1 -> menuHandler.crearEnvio();
            case 2 -> menuHandler.listarEnvios();
            case 3 -> menuHandler.buscarEnvioPorId();
            case 4 -> menuHandler.buscarEnvioPorTracking();
            case 5 -> menuHandler.actualizarEnvio();
            case 6 -> menuHandler.eliminarEnvio();
            
            // === PEDIDOS ===
            //case 7 -> menuHandler.crearPedidoSinEnvio();
            case 7 -> menuHandler.crearPedidoConEnvio();
            case 8 -> menuHandler.listarPedidos();
            case 9 -> menuHandler.buscarPedidoPorId();
            case 10 -> menuHandler.buscarPedidoPorNumero();
            case 11 -> menuHandler.actualizarPedido();
            case 12 -> menuHandler.eliminarPedido();
            
            // === SALIR ===
            case 0 -> {
                System.out.println("\nSaliendo del sistema...");
                running = false;
            }
            
            default -> System.out.println("Opción no válida. Intente nuevamente.");
        }
    }
    
    /**
     * Factory method que crea la cadena de dependencias de servicios.
     * Implementa inyección de dependencias manual.
     *
     * Orden de creación (bottom-up desde la capa más baja):
     * 1. EnvioDAO: Sin dependencias, acceso directo a BD
     * 2. PedidoDAO: Sin dependencias directas (usa EnvioDAO.mapearEnvio() internamente)
     * 3. EnvioServiceImpl: Depende de EnvioDAO
     * 4. PedidoServiceImpl: Depende de PedidoDAO y EnvioServiceImpl
     *
     * Arquitectura resultante (4 capas):
     * Main (AppMenu, MenuHandler, MenuDisplay)
     *   ↓
     * Service (PedidoServiceImpl, EnvioServiceImpl)
     *   ↓
     * DAO (PedidoDAO, EnvioDAO)
     *   ↓
     * Models (Pedido, Envio, Base)
     *
     * ¿Por qué PedidoService necesita EnvioService?
     * - Para crear/actualizar envíos al crear/actualizar pedidos
     * - Para coordinar transacciones (crearPedidoConEnvio)
     * - Para gestionar la relación unidireccional Pedido→Envio
     *
     * Patrón: Factory Method para construcción de dependencias
     *
     * @return PedidoServiceImpl completamente inicializado con todas sus dependencias
     */
    private PedidoServiceImpl createPedidoService() {
        EnvioDAO envioDAO = new EnvioDAO();
        PedidoDAO pedidoDAO = new PedidoDAO();
        EnvioServiceImpl envioService = new EnvioServiceImpl(envioDAO);
        return new PedidoServiceImpl(pedidoDAO, envioService);
    }
}