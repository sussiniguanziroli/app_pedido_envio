package Main;

import java.sql.Connection;
import Config.DatabaseConnection;

public class TestConexion {
    public static void main(String[] args) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            if (conn != null) {
                System.out.println("✅ Conexión exitosa!");
                System.out.println("Base de datos: " + conn.getCatalog());
            }
        } catch (Exception e) {
            System.err.println("❌ Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}