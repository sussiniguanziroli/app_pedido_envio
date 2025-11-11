package Main;

/**
 * Punto de entrada de la aplicación Pedido-Envio.
 * Clase simple que delega inmediatamente a AppMenu.
 *
 * Responsabilidad:
 * - Proporcionar un punto de entrada main() estándar
 * - Delegar la ejecución a AppMenu
 *
 * Sistema: Gestión de pedidos y envíos
 * Base de datos: pedido_envio
 */
public class Main {
    /**
     * Punto de entrada de la aplicación Java.
     * Crea AppMenu y ejecuta el menú principal.
     *
     * Flujo:
     * 1. Crea instancia de AppMenu (inicializa toda la aplicación)
     * 2. Llama a app.run() que ejecuta el loop del menú
     * 3. Cuando el usuario sale (opción 0), run() termina
     *
     * @param args Argumentos de línea de comandos (no usados)
     */
    public static void main(String[] args) {
        AppMenu app = new AppMenu();
        app.run();
    }
}