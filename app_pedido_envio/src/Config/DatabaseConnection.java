package Config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Clase utilitaria para gestionar conexiones a la base de datos MySQL.
 */
public final class DatabaseConnection {
    /** URL de conexión JDBC */
    private static final String URL = System.getProperty("db.url", "jdbc:mysql://localhost:3306/pedido_envio");

    /** Usuario de la base de datos */
    private static final String USER = System.getProperty("db.user", "root");

    /** Contraseña del usuario */
    private static final String PASSWORD = System.getProperty("db.password", "");

    static {
        try {
            // Carga explícita del driver JDBC de MySQL
            Class.forName("com.mysql.cj.jdbc.Driver");
            
            // Valida configuración
            validateConfiguration();
        } catch (ClassNotFoundException e) {
            throw new ExceptionInInitializerError("Error: No se encontró el driver JDBC de MySQL: " + e.getMessage());
        } catch (IllegalStateException e) {
            throw new ExceptionInInitializerError("Error en la configuración de la base de datos: " + e.getMessage());
        }
    }

    private DatabaseConnection() {
        throw new UnsupportedOperationException("Esta es una clase utilitaria y no debe ser instanciada");
    }

    /**
     * Obtiene una nueva conexión a la base de datos.
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    /**
     * Valida que los parámetros de configuración sean válidos.
     */
    private static void validateConfiguration() {
        if (URL == null || URL.trim().isEmpty()) {
            throw new IllegalStateException("La URL de la base de datos no está configurada");
        }
        if (USER == null || USER.trim().isEmpty()) {
            throw new IllegalStateException("El usuario de la base de datos no está configurado");
        }
        if (PASSWORD == null) {
            throw new IllegalStateException("La contraseña de la base de datos no está configurada");
        }
    }
}