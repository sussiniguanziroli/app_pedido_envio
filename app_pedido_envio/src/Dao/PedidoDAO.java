package Dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import Config.DatabaseConnection;
import Models.Pedido;
import Models.Envio;
import Models.EstadoPedido;
import Models.EmpresaEnvio;
import Models.TipoEnvio;
import Models.EstadoEnvio;

/**
 * DAO para la entidad Pedido.
 * Gestiona todas las operaciones CRUD de pedidos en la base de datos.
 * 
 * IMPORTANTE: Pedido tiene relación 1→1 con Envio.
 * Al leer un Pedido, se carga su Envio asociado mediante LEFT JOIN.
 * 
 * Base de datos: pedido_envio
 */
public class PedidoDAO implements GenericDAO<Pedido> {
    
    private static final String INSERT_SQL = 
        "INSERT INTO pedido (numero, fecha, cliente_nombre, total, estado, envio_id, eliminado) " +
        "VALUES (?, ?, ?, ?, ?, ?, FALSE)";
    
    private static final String UPDATE_SQL = 
        "UPDATE pedido SET numero = ?, fecha = ?, cliente_nombre = ?, " +
        "total = ?, estado = ?, envio_id = ? WHERE id = ?";
    
    private static final String DELETE_SQL = 
        "UPDATE pedido SET eliminado = TRUE WHERE id = ?";
    
    private static final String SELECT_BY_ID_SQL = 
        "SELECT p.*, e.* FROM pedido p " +
        "LEFT JOIN envio e ON p.envio_id = e.id " +
        "WHERE p.id = ? AND p.eliminado = FALSE";
    
    private static final String SELECT_ALL_SQL = 
        "SELECT p.*, e.* FROM pedido p " +
        "LEFT JOIN envio e ON p.envio_id = e.id " +
        "WHERE p.eliminado = FALSE";
    
    private static final String SELECT_BY_NUMERO_SQL = 
        "SELECT p.*, e.* FROM pedido p " +
        "LEFT JOIN envio e ON p.envio_id = e.id " +
        "WHERE p.numero = ? AND p.eliminado = FALSE";
    
    @Override
    public void insertar(Pedido pedido) throws Exception {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {
            
            setPedidoParameters(stmt, pedido);
            stmt.executeUpdate();
            setGeneratedId(stmt, pedido);
        }
    }
    
    @Override
    public void insertTx(Pedido pedido, Connection conn) throws Exception {
        try (PreparedStatement stmt = conn.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {
            setPedidoParameters(stmt, pedido);
            stmt.executeUpdate();
            setGeneratedId(stmt, pedido);
        }
    }
    
    @Override
    public void actualizar(Pedido pedido) throws Exception {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(UPDATE_SQL)) {
            
            stmt.setString(1, pedido.getNumero());
            stmt.setDate(2, pedido.getFecha() != null ? Date.valueOf(pedido.getFecha()) : null);
            stmt.setString(3, pedido.getClienteNombre());
            stmt.setDouble(4, pedido.getTotal());
            stmt.setString(5, pedido.getEstado().name());
            
            // FK al envío (puede ser null)
            if (pedido.getEnvio() != null && pedido.getEnvio().getId() > 0) {
                stmt.setLong(6, pedido.getEnvio().getId());
            } else {
                stmt.setNull(6, Types.BIGINT);
            }
            
            stmt.setInt(7, pedido.getId());
            
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("No se pudo actualizar el pedido con ID: " + pedido.getId());
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
                throw new SQLException("No se encontró pedido con ID: " + id);
            }
        }
    }
    
    @Override
    public Pedido getById(int id) throws Exception {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_BY_ID_SQL)) {
            
            stmt.setInt(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapearPedidoConEnvio(rs);
                }
            }
        }
        return null;
    }
    
    @Override
    public List<Pedido> getAll() throws Exception {
        List<Pedido> pedidos = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(SELECT_ALL_SQL)) {
            
            while (rs.next()) {
                pedidos.add(mapearPedidoConEnvio(rs));
            }
        }
        
        return pedidos;
    }
    
    /**
     * Busca un pedido por su número (campo UNIQUE).
     * Incluye el envío asociado mediante LEFT JOIN.
     */
    public Pedido buscarPorNumero(String numero) throws Exception {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_BY_NUMERO_SQL)) {
            
            stmt.setString(1, numero);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapearPedidoConEnvio(rs);
                }
            }
        }
        return null;
    }
    
    /**
     * Setea los parámetros de un pedido en el PreparedStatement.
     */
    private void setPedidoParameters(PreparedStatement stmt, Pedido pedido) throws SQLException {
        stmt.setString(1, pedido.getNumero());
        stmt.setDate(2, pedido.getFecha() != null ? Date.valueOf(pedido.getFecha()) : null);
        stmt.setString(3, pedido.getClienteNombre());
        stmt.setDouble(4, pedido.getTotal());
        stmt.setString(5, pedido.getEstado().name());
        
        // FK al envío (puede ser null)
        if (pedido.getEnvio() != null && pedido.getEnvio().getId() > 0) {
            stmt.setLong(6, pedido.getEnvio().getId());
        } else {
            stmt.setNull(6, Types.BIGINT);
        }
    }
    
    /**
     * Obtiene el ID autogenerado y lo asigna al pedido.
     */
    private void setGeneratedId(PreparedStatement stmt, Pedido pedido) throws SQLException {
        try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
            if (generatedKeys.next()) {
                pedido.setId(generatedKeys.getInt(1));
            } else {
                throw new SQLException("La inserción del pedido falló, no se obtuvo ID generado");
            }
        }
    }
    
    /**
     * Mapea un ResultSet a un objeto Pedido CON su Envio asociado.
     * 
     * Orden de columnas del LEFT JOIN:
     * PEDIDO: id, eliminado, numero, fecha, cliente_nombre, total, estado, envio_id
     * ENVIO: id, eliminado, tracking, empresa, tipo, costo, fecha_despacho, fecha_estimada, estado
     */
    private Pedido mapearPedidoConEnvio(ResultSet rs) throws SQLException {
        Pedido pedido = new Pedido();
        
        // Mapear campos de PEDIDO (primeras 8 columnas)
        pedido.setId(rs.getInt(1));           // p.id
        pedido.setEliminado(rs.getBoolean(2)); // p.eliminado
        pedido.setNumero(rs.getString(3));     // p.numero
        
        Date fecha = rs.getDate(4);            // p.fecha
        if (fecha != null) {
            pedido.setFecha(fecha.toLocalDate());
        }
        
        pedido.setClienteNombre(rs.getString(5)); // p.cliente_nombre
        pedido.setTotal(rs.getDouble(6));         // p.total
        pedido.setEstado(EstadoPedido.valueOf(rs.getString(7))); // p.estado
        // Columna 8 es p.envio_id (no la necesitamos directamente)
        
        // Mapear ENVIO asociado (columnas 9-17, pueden ser null si LEFT JOIN no encuentra match)
        Long envioId = rs.getObject(9, Long.class); // e.id
        if (envioId != null && envioId > 0) {
            Envio envio = new Envio();
            envio.setId(envioId.intValue());           // e.id
            envio.setEliminado(rs.getBoolean(10));     // e.eliminado
            envio.setTracking(rs.getString(11));       // e.tracking
            envio.setEmpresa(EmpresaEnvio.valueOf(rs.getString(12))); // e.empresa
            envio.setTipo(TipoEnvio.valueOf(rs.getString(13)));       // e.tipo
            envio.setCosto(rs.getDouble(14));          // e.costo
            
            Date fechaDespacho = rs.getDate(15);       // e.fecha_despacho
            if (fechaDespacho != null) {
                envio.setFechaDespacho(fechaDespacho.toLocalDate());
            }
            
            Date fechaEstimada = rs.getDate(16);       // e.fecha_estimada
            if (fechaEstimada != null) {
                envio.setFechaEstimada(fechaEstimada.toLocalDate());
            }
            
            envio.setEstado(EstadoEnvio.valueOf(rs.getString(17))); // e.estado
            
            pedido.setEnvio(envio);  // ⬅️ Asociar el envío al pedido
        }
        
        return pedido;
    }
}