package Dao;

import Config.DatabaseConnection;
import Models.Envio;
import Models.Envio.EmpresaEnvio;
import Models.Envio.TipoEnvio;
import Models.Envio.EstadoEnvio;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO para la entidad Envio.
 * Gestiona todas las operaciones CRUD con la tabla 'envio'.
 * Implementa soft delete y soporte para transacciones.
 * 
 * Tabla: envio
 * Base de datos: pedido_envio
 */
public class EnvioDAO implements GenericDAO<Envio> {
    
    // ============================================
    // QUERIES SQL
    // ============================================
    
    private static final String INSERT_SQL = 
        "INSERT INTO envio (eliminado, tracking, empresa, tipo, costo, " +
        "fecha_despacho, fecha_estimada, estado) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
    
    private static final String SELECT_BY_ID_SQL = 
        "SELECT * FROM envio WHERE id = ? AND eliminado = FALSE";
    
    private static final String SELECT_ALL_SQL = 
        "SELECT * FROM envio WHERE eliminado = FALSE ORDER BY id";
    
    private static final String UPDATE_SQL = 
        "UPDATE envio SET tracking = ?, empresa = ?, tipo = ?, costo = ?, " +
        "fecha_despacho = ?, fecha_estimada = ?, estado = ? " +
        "WHERE id = ? AND eliminado = FALSE";
    
    private static final String DELETE_SQL = 
        "UPDATE envio SET eliminado = TRUE WHERE id = ? AND eliminado = FALSE";
    
    private static final String SELECT_BY_TRACKING_SQL = 
        "SELECT * FROM envio WHERE tracking = ? AND eliminado = FALSE";
    
    // ============================================
    // MÉTODO: crear (sin transacción)
    // ============================================
    
    @Override
    public void crear(Envio envio) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {
            
            setParametrosEnvio(ps, envio);
            ps.executeUpdate();
            
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    envio.setId(rs.getLong(1));
                }
            }
        }
    }
    
    // ============================================
    // MÉTODO: crear (con Connection para transacciones)
    // ============================================
    
    @Override
    public void crear(Envio envio, Connection conn) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {
            setParametrosEnvio(ps, envio);
            ps.executeUpdate();
            
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    envio.setId(rs.getLong(1));
                }
            }
        }
    }
    
    // ============================================
    // MÉTODO: leer por ID
    // ============================================
    
    @Override
    public Envio leer(Long id) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_BY_ID_SQL)) {
            
            ps.setLong(1, id);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapearEnvio(rs);
                }
            }
        }
        return null;
    }
    
    // ============================================
    // MÉTODO: leer todos
    // ============================================
    
    @Override
    public List<Envio> leerTodos() throws SQLException {
        List<Envio> envios = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_ALL_SQL);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                envios.add(mapearEnvio(rs));
            }
        }
        
        return envios;
    }
    
    // ============================================
    // MÉTODO: actualizar (sin transacción)
    // ============================================
    
    @Override
    public void actualizar(Envio envio) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(UPDATE_SQL)) {
            
            ps.setString(1, envio.getTracking());
            ps.setString(2, envio.getEmpresa() != null ? envio.getEmpresa().name() : null);
            ps.setString(3, envio.getTipo() != null ? envio.getTipo().name() : null);
            ps.setObject(4, envio.getCosto());
            ps.setDate(5, envio.getFechaDespacho() != null ? Date.valueOf(envio.getFechaDespacho()) : null);
            ps.setDate(6, envio.getFechaEstimada() != null ? Date.valueOf(envio.getFechaEstimada()) : null);
            ps.setString(7, envio.getEstado() != null ? envio.getEstado().name() : null);
            ps.setLong(8, envio.getId());
            
            int rows = ps.executeUpdate();
            if (rows == 0) {
                throw new SQLException("No se encontró el envío con ID: " + envio.getId());
            }
        }
    }
    
    // ============================================
    // MÉTODO: actualizar (con Connection para transacciones)
    // ============================================
    
    @Override
    public void actualizar(Envio envio, Connection conn) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(UPDATE_SQL)) {
            ps.setString(1, envio.getTracking());
            ps.setString(2, envio.getEmpresa() != null ? envio.getEmpresa().name() : null);
            ps.setString(3, envio.getTipo() != null ? envio.getTipo().name() : null);
            ps.setObject(4, envio.getCosto());
            ps.setDate(5, envio.getFechaDespacho() != null ? Date.valueOf(envio.getFechaDespacho()) : null);
            ps.setDate(6, envio.getFechaEstimada() != null ? Date.valueOf(envio.getFechaEstimada()) : null);
            ps.setString(7, envio.getEstado() != null ? envio.getEstado().name() : null);
            ps.setLong(8, envio.getId());
            
            int rows = ps.executeUpdate();
            if (rows == 0) {
                throw new SQLException("No se encontró el envío con ID: " + envio.getId());
            }
        }
    }
    
    // ============================================
    // MÉTODO: eliminar (baja lógica)
    // ============================================
    
    @Override
    public void eliminar(Long id) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(DELETE_SQL)) {
            
            ps.setLong(1, id);
            int rows = ps.executeUpdate();
            
            if (rows == 0) {
                throw new SQLException("No se encontró el envío con ID: " + id);
            }
        }
    }
    
    // ============================================
    // MÉTODO: eliminar (con Connection para transacciones)
    // ============================================
    
    @Override
    public void eliminar(Long id, Connection conn) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(DELETE_SQL)) {
            ps.setLong(1, id);
            int rows = ps.executeUpdate();
            
            if (rows == 0) {
                throw new SQLException("No se encontró el envío con ID: " + id);
            }
        }
    }
    
    // ============================================
    // MÉTODO ADICIONAL: buscar por tracking
    // ============================================
    
    /**
     * Busca un envío por su código de tracking (campo UNIQUE).
     * 
     * @param tracking Código de seguimiento
     * @return Envio encontrado o null
     * @throws SQLException si ocurre un error
     */
    public Envio buscarPorTracking(String tracking) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_BY_TRACKING_SQL)) {
            
            ps.setString(1, tracking);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapearEnvio(rs);
                }
            }
        }
        return null;
    }
    
    // ============================================
    // MÉTODOS AUXILIARES PRIVADOS
    // ============================================
    
    /**
     * Setea los parámetros del PreparedStatement para INSERT.
     */
    private void setParametrosEnvio(PreparedStatement ps, Envio envio) throws SQLException {
        ps.setBoolean(1, envio.getEliminado());
        ps.setString(2, envio.getTracking());
        ps.setString(3, envio.getEmpresa() != null ? envio.getEmpresa().name() : null);
        ps.setString(4, envio.getTipo() != null ? envio.getTipo().name() : null);
        ps.setObject(5, envio.getCosto());
        ps.setDate(6, envio.getFechaDespacho() != null ? Date.valueOf(envio.getFechaDespacho()) : null);
        ps.setDate(7, envio.getFechaEstimada() != null ? Date.valueOf(envio.getFechaEstimada()) : null);
        ps.setString(8, envio.getEstado() != null ? envio.getEstado().name() : null);
    }
    
    /**
     * Mapea un ResultSet a un objeto Envio.
     * IMPORTANTE: Este método es PUBLIC para que PedidoDAO pueda reutilizarlo.
     * 
     * @param rs ResultSet con los datos del envío
     * @return Envio mapeado
     * @throws SQLException si ocurre un error
     */
    public Envio mapearEnvio(ResultSet rs) throws SQLException {
        Envio envio = new Envio();
        
        envio.setId(rs.getLong("id"));
        envio.setEliminado(rs.getBoolean("eliminado"));
        envio.setTracking(rs.getString("tracking"));
        
        String empresaStr = rs.getString("empresa");
        if (empresaStr != null) {
            envio.setEmpresa(EmpresaEnvio.valueOf(empresaStr));
        }
        
        String tipoStr = rs.getString("tipo");
        if (tipoStr != null) {
            envio.setTipo(TipoEnvio.valueOf(tipoStr));
        }
        
        envio.setCosto(rs.getObject("costo", Double.class));
        
        Date fechaDespacho = rs.getDate("fecha_despacho");
        if (fechaDespacho != null) {
            envio.setFechaDespacho(fechaDespacho.toLocalDate());
        }
        
        Date fechaEstimada = rs.getDate("fecha_estimada");
        if (fechaEstimada != null) {
            envio.setFechaEstimada(fechaEstimada.toLocalDate());
        }
        
        String estadoStr = rs.getString("estado");
        if (estadoStr != null) {
            envio.setEstado(EstadoEnvio.valueOf(estadoStr));
        }
        
        return envio;
    }
}