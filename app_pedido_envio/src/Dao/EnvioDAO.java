package Dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import Config.DatabaseConnection;
import Models.Envio;
import Models.EmpresaEnvio;
import Models.TipoEnvio;
import Models.EstadoEnvio;

/**
 * DAO para la entidad Envio.
 * Gestiona todas las operaciones CRUD de envíos en la base de datos.
 * 
 * Base de datos: pedido_envio
 */
public class EnvioDAO implements GenericDAO<Envio> {
    
    private static final String INSERT_SQL = 
        "INSERT INTO envio (tracking, empresa, tipo, costo, fecha_despacho, fecha_estimada, estado, eliminado) " +
        "VALUES (?, ?, ?, ?, ?, ?, ?, FALSE)";
    
    private static final String UPDATE_SQL = 
        "UPDATE envio SET tracking = ?, empresa = ?, tipo = ?, costo = ?, " +
        "fecha_despacho = ?, fecha_estimada = ?, estado = ? WHERE id = ?";
    
    private static final String DELETE_SQL = 
        "UPDATE envio SET eliminado = TRUE WHERE id = ?";
    
    private static final String SELECT_BY_ID_SQL = 
        "SELECT * FROM envio WHERE id = ? AND eliminado = FALSE";
    
    private static final String SELECT_ALL_SQL = 
        "SELECT * FROM envio WHERE eliminado = FALSE";
    
    private static final String SELECT_BY_TRACKING_SQL = 
        "SELECT * FROM envio WHERE tracking = ? AND eliminado = FALSE";
    
    @Override
    public void insertar(Envio envio) throws Exception {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {
            
            setEnvioParameters(stmt, envio);
            stmt.executeUpdate();
            setGeneratedId(stmt, envio);
        }
    }
    
    @Override
    public void insertTx(Envio envio, Connection conn) throws Exception {
        try (PreparedStatement stmt = conn.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {
            setEnvioParameters(stmt, envio);
            stmt.executeUpdate();
            setGeneratedId(stmt, envio);
        }
    }
    
    @Override
    public void actualizar(Envio envio) throws Exception {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(UPDATE_SQL)) {
            
            stmt.setString(1, envio.getTracking());
            stmt.setString(2, envio.getEmpresa().name());
            stmt.setString(3, envio.getTipo().name());
            stmt.setDouble(4, envio.getCosto());
            stmt.setDate(5, envio.getFechaDespacho() != null ? Date.valueOf(envio.getFechaDespacho()) : null);
            stmt.setDate(6, envio.getFechaEstimada() != null ? Date.valueOf(envio.getFechaEstimada()) : null);
            stmt.setString(7, envio.getEstado().name());
            stmt.setInt(8, envio.getId());
            
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("No se pudo actualizar el envío con ID: " + envio.getId());
            }
        }
    }
    
    @Override
    public void eliminar(int id) throws Exception {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(DELETE_SQL)) {
            
            stmt.setInt(1, id);
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected == 0) {
                throw new SQLException("No se encontró envío con ID: " + id);
            }
        }
    }
    
    @Override
    public Envio getById(int id) throws Exception {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_BY_ID_SQL)) {
            
            stmt.setInt(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapearEnvio(rs);
                }
            }
        }
        return null;
    }
    
    @Override
    public List<Envio> getAll() throws Exception {
        List<Envio> envios = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(SELECT_ALL_SQL)) {
            
            while (rs.next()) {
                envios.add(mapearEnvio(rs));
            }
        }
        
        return envios;
    }
    
    /**
     * Busca un envío por su tracking (campo UNIQUE).
     */
    public Envio buscarPorTracking(String tracking) throws Exception {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_BY_TRACKING_SQL)) {
            
            stmt.setString(1, tracking);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapearEnvio(rs);
                }
            }
        }
        return null;
    }
    
    /**
     * Setea los parámetros de un envío en el PreparedStatement.
     */
    private void setEnvioParameters(PreparedStatement stmt, Envio envio) throws SQLException {
        stmt.setString(1, envio.getTracking());
        stmt.setString(2, envio.getEmpresa().name());
        stmt.setString(3, envio.getTipo().name());
        stmt.setDouble(4, envio.getCosto());
        stmt.setDate(5, envio.getFechaDespacho() != null ? Date.valueOf(envio.getFechaDespacho()) : null);
        stmt.setDate(6, envio.getFechaEstimada() != null ? Date.valueOf(envio.getFechaEstimada()) : null);
        stmt.setString(7, envio.getEstado().name());
    }
    
    /**
     * Obtiene el ID autogenerado y lo asigna al envío.
     */
    private void setGeneratedId(PreparedStatement stmt, Envio envio) throws SQLException {
        try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
            if (generatedKeys.next()) {
                envio.setId(generatedKeys.getInt(1));
            } else {
                throw new SQLException("La inserción del envío falló, no se obtuvo ID generado");
            }
        }
    }
    
    /**
     * Mapea un ResultSet a un objeto Envio.
     */
    private Envio mapearEnvio(ResultSet rs) throws SQLException {
        Envio envio = new Envio();
        
        envio.setId(rs.getInt("id"));
        envio.setEliminado(rs.getBoolean("eliminado"));
        envio.setTracking(rs.getString("tracking"));
        envio.setEmpresa(EmpresaEnvio.valueOf(rs.getString("empresa")));
        envio.setTipo(TipoEnvio.valueOf(rs.getString("tipo")));
        envio.setCosto(rs.getDouble("costo"));
        
        Date fechaDespacho = rs.getDate("fecha_despacho");
        if (fechaDespacho != null) {
            envio.setFechaDespacho(fechaDespacho.toLocalDate());
        }
        
        Date fechaEstimada = rs.getDate("fecha_estimada");
        if (fechaEstimada != null) {
            envio.setFechaEstimada(fechaEstimada.toLocalDate());
        }
        
        envio.setEstado(EstadoEnvio.valueOf(rs.getString("estado")));
        
        return envio;
    }
}