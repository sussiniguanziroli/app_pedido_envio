package Main;

import Config.DatabaseConnection;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

/**
 * Clase de prueba para verificar la conexión a la base de datos.
 * Ejecutar ANTES de usar la aplicación para validar configuración.
 *
 * Propósito:
 * - Verificar que el driver MySQL está disponible
 * - Probar la conexión a la BD pedido_envio
 * - Mostrar información de la conexión (usuario, URL, driver)
 *
 * Uso:
 * 1. Ejecutar esta clase antes de Main
 * 2. Si falla, verificar:
 *    - MySQL está corriendo (puerto 3306)
 *    - Base de datos 'pedido_envio' existe
 *    - Usuario/password en DatabaseConnection son correctos
 *    - Driver MySQL JDBC está en el classpath
 */
public class TestConexion {
    
    public static void main(String[] args) {
        System.out.println("🔧 Probando conexión a la base de datos...\n");
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            if (conn != null) {
                System.out.println("✅ Conexión exitosa a la base de datos");
                
                DatabaseMetaData metaData = conn.getMetaData();
                System.out.println("\n📊 INFORMACIÓN DE CONEXIÓN:");
                System.out.println("   Usuario conectado: " + metaData.getUserName());
                System.out.println("   Base de datos: " + conn.getCatalog());
                System.out.println("   URL: " + metaData.getURL());
                System.out.println("   Driver: " + metaData.getDriverName() + " v" + metaData.getDriverVersion());
                
                System.out.println("\n✅ Todas las verificaciones pasaron correctamente.");
                System.out.println("✅ La aplicación está lista para usarse.");
                
            } else {
                System.out.println("❌ No se pudo establecer la conexión.");
            }
            
        } catch (SQLException e) {
            System.err.println("\n❌ Error al conectar a la base de datos:");
            System.err.println("   Mensaje: " + e.getMessage());
            System.err.println("\n🔍 POSIBLES CAUSAS:");
            System.err.println("   1. MySQL no está corriendo (verificar servicio)");
            System.err.println("   2. Base de datos 'pedido_envio' no existe");
            System.err.println("   3. Usuario/password incorrectos en DatabaseConnection");
            System.err.println("   4. Driver MySQL JDBC no está en el classpath");
            System.err.println("   5. Puerto 3306 bloqueado o en uso");
            
            e.printStackTrace();
        }
    }
}