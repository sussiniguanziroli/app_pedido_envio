package Main;

/**
 * Clase utilitaria para mostrar el menú de la aplicación Pedido-Envio.
 * Solo contiene métodos estáticos de visualización (no tiene estado).
 *
 * Responsabilidades:
 * - Mostrar el menú principal con todas las opciones disponibles
 * - Formatear la salida de forma consistente
 *
 * Patrón: Utility class (solo métodos estáticos, no instanciable)
 *
 * IMPORTANTE: Esta clase NO lee entrada del usuario.
 * Solo muestra el menú. AppMenu es responsable de leer la opción.
 */
public class MenuDisplay {
    
    /**
     * Muestra el menú principal con todas las opciones CRUD para Envios y Pedidos.
     *
     * === ENVÍOS (1-6) ===
     * 1. Crear envío: Crea un nuevo envío independiente
     * 2. Listar envíos: Muestra todos los envíos activos
     * 3. Buscar envío por ID: Busca un envío específico por su ID
     * 4. Buscar envío por tracking: Busca un envío por su código de tracking
     * 5. Actualizar envío: Modifica datos de un envío existente
     * 6. Eliminar envío: Soft delete de envío
     *
     * === PEDIDOS (7-13) ===
     * 7. Crear pedido sin envío: Crea un pedido nuevo sin envío asociado
     * 8. Crear pedido con envío: Crea pedido y envío en TRANSACCIÓN (recomendado)
     * 9. Listar pedidos: Muestra todos los pedidos activos con sus envíos
     * 10. Buscar pedido por ID: Busca un pedido específico por su ID
     * 11. Buscar pedido por número: Busca un pedido por su número único
     * 12. Actualizar pedido: Modifica datos de un pedido existente
     * 13. Eliminar pedido: Soft delete de pedido (NO elimina envío asociado)
     *
     * === SALIR ===
     * 0. Salir: Termina la aplicación
     *
     * Formato:
     * - Separador visual con emojis para mejor UX
     * - Secciones claramente divididas (ENVÍOS vs PEDIDOS)
     * - Lista numerada clara
     * - Prompt "Ingrese una opción: " sin salto de línea (espera input)
     *
     * Nota: Los números de opción corresponden al switch en AppMenu.processOption().
     */
    public static void mostrarMenuPrincipal() {
        System.out.println("\n╔════════════════════════════════════════╗");
        System.out.println("║     SISTEMA PEDIDO-ENVÍO               ║");
        System.out.println("╚════════════════════════════════════════╝");
        
        System.out.println("\nGESTIÓN DE ENVÍOS:");
        System.out.println("  1. Crear envío");
        System.out.println("  2. Listar envíos");
        System.out.println("  3. Buscar envío por ID");
        System.out.println("  4. Buscar envío por tracking");
        System.out.println("  5. Actualizar envío");
        System.out.println("  6. Eliminar envío");
        
        System.out.println("\nGESTIÓN DE PEDIDOS:");
        System.out.println("  7. Crear pedido sin envío");
        System.out.println("  8. Crear pedido con envío (transacción)");
        System.out.println("  9. Listar pedidos");
        System.out.println("  10. Buscar pedido por ID");
        System.out.println("  11. Buscar pedido por número");
        System.out.println("  12. Actualizar pedido");
        System.out.println("  13. Eliminar pedido");
        
        System.out.println("\nSALIR:");
        System.out.println("  0. Salir del sistema");
        
        System.out.print("\n👉 Ingrese una opción: ");
    }
    
    /**
     * Muestra un separador visual.
     * Útil para separar secciones de output en la consola.
     */
    public static void mostrarSeparador() {
        System.out.println("\n─────────────────────────────────────────");
    }
    
    /**
     * Muestra un mensaje de éxito con formato.
     * 
     * @param mensaje Mensaje a mostrar
     */
    public static void mostrarExito(String mensaje) {
        System.out.println("\n" + mensaje);
    }
    
    /**
     * Muestra un mensaje de error con formato.
     * 
     * @param mensaje Mensaje a mostrar
     */
    public static void mostrarError(String mensaje) {
        System.out.println("\nERROR: " + mensaje);
    }
    
    /**
     * Muestra un mensaje de advertencia con formato.
     * 
     * @param mensaje Mensaje a mostrar
     */
    public static void mostrarAdvertencia(String mensaje) {
        System.out.println("\nADVERTENCIA: " + mensaje);
    }
}