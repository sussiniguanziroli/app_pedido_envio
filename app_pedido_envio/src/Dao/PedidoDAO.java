package Dao;

import Config.DatabaseConnection;
import Models.Pedido;
import Models.Pedido.EstadoPedido;
import Models.Envio;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO para la entidad Pedido.
 * Gestiona todas las operaciones CRUD con la tabla 'pedido'.
 * Implementa soft delete, soporte para transacciones y carga de Envio asociado.
 * 
 * IMPORTANTE: Pedido tiene relación 1→1 con Envio (unidireccional).
 * Al leer un Pedido, se carga automáticamente su Envio mediante LEFT JOIN.
 * 
 * Tabla: pedido
 * Base de datos: pedido_envio
 */
public class PedidoDAO implements GenericDAO<Pedido> {
    
    private final EnvioDAO envioDAO;
    
    // ============================================
    // CONSTRUCTOR
    // ============================================
    
    /**
     * Constructor que inicializa el EnvioDAO para reutilizar su método de mapeo.
     */
    public PedidoDAO() {
        this.envioDAO = new EnvioDAO();
    }
    
    // ============================================
    // QUERIES SQL
    // ============================================
    
    private static final String INSERT_SQL = 
        "INSERT INTO pedido (eliminado, numero, fecha, cliente_nombre, total, estado, envio_id) " +
        "VALUES (?, ?, ?, ?, ?, ?, ?)";
    
    private static final String SELECT_BY_ID_SQL = 
        "SELECT p.id as p_id, p.eliminado as p_eliminado, p.numero, p.fecha, " +
        "p.cliente_nombre, p.total, p.estado as p_estado, p.envio_id, " +
        "e.id as e_id, e.eliminado as e_eliminado, e.tracking, e.empresa, " +
        "e.tipo, e.costo, e.fecha_despacho, e.fecha_estimada, e.estado as e_estado " +
        "FROM pedido p " +
        "LEFT JOIN envio e ON p.envio_id = e.id " +
        "WHERE p.id = ? AND p.eliminado = 0";
    
    private static final String SELECT_ALL_SQL = 
        "SELECT p.id as p_id, p.eliminado as p_eliminado, p.numero, p.fecha, " +
        "p.cliente_nombre, p.total, p.estado as p_estado, p.envio_id, " +
        "e.id as e_id, e.eliminado as e_eliminado, e.tracking, e.empresa, " +
        "e.tipo, e.costo, e.fecha_despacho, e.fecha_estimada, e.estado as e_estado " +
        "FROM pedido p " +
        "LEFT JOIN envio e ON p.envio_id = e.id " +
        "WHERE p.eliminado = 0 " +
        "ORDER BY p.id";
    
    private static final String UPDATE_SQL = 
        "UPDATE pedido SET numero = ?, fecha = ?, cliente_nombre = ?, total = ?, " +
        "estado = ?, envio_id = ? WHERE id = ? AND eliminado = 0";
    
    private static final String DELETE_SQL = 
        "UPDATE pedido SET eliminado = 1 WHERE id = ? AND eliminado = 0";
    
    private static final String SELECT_BY_NUMERO_SQL = 
       "SELECT p.id as p_id, p.eliminado as p_eliminado, p.numero, p.fecha, " +
       "p.cliente_nombre, p.total, p.estado as p_estado, p.envio_id, " +
       "e.id as e_id, e.eliminado as e_eliminado, e.tracking, e.empresa, " +
       "e.tipo, e.costo, e.fecha_despacho, e.fecha_estimada, e.estado as e_estado " +
       "FROM pedido p " +
       "LEFT JOIN envio e ON p.envio_id = e.id " +
       "WHERE p.numero = ? AND p.eliminado = 0";
    // ============================================
    // MÉTODO: crear (sin transacción)
    // ============================================
    
    @Override
    public void crear(Pedido pedido) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {
            
            setParametrosPedido(ps, pedido);
            ps.executeUpdate();
            
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    pedido.setId(rs.getLong(1));
                }
            }
        }
    }
    
    // ============================================
    // MÉTODO: crear (con Connection para transacciones)
    // ============================================
    
    @Override
    public void crear(Pedido pedido, Connection conn) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {
            setParametrosPedido(ps, pedido);
            ps.executeUpdate();
            
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    pedido.setId(rs.getLong(1));
                }
            }
        }
    }
    
    // ============================================
    // MÉTODO: leer por ID
    // ============================================
    
    @Override
    public Pedido leer(Long id) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_BY_ID_SQL)) {
            
            ps.setLong(1, id);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapearPedidoConEnvio(rs);
                }
            }
        }
        return null;
    }
    
    // ============================================
    // MÉTODO: leer todos
    // ============================================
    
    @Override
    public List<Pedido> leerTodos() throws SQLException {
        List<Pedido> pedidos = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_ALL_SQL);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                pedidos.add(mapearPedidoConEnvio(rs));
            }
        }
        
        return pedidos;
    }
    
    // ============================================
    // MÉTODO: actualizar (sin transacción)
    // ============================================
    
    @Override
    public void actualizar(Pedido pedido) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(UPDATE_SQL)) {
            
            ps.setString(1, pedido.getNumero());
            ps.setDate(2, pedido.getFecha() != null ? Date.valueOf(pedido.getFecha()) : null);
            ps.setString(3, pedido.getClienteNombre());
            ps.setObject(4, pedido.getTotal());
            ps.setString(5, pedido.getEstado() != null ? pedido.getEstado().name() : null);
            
            if (pedido.getEnvio() != null && pedido.getEnvio().getId() != null) {
                ps.setLong(6, pedido.getEnvio().getId());
            } else {
                ps.setNull(6, Types.BIGINT);
            }
            
            ps.setLong(7, pedido.getId());
            
            int rows = ps.executeUpdate();
            if (rows == 0) {
                throw new SQLException("No se encontró el pedido con ID: " + pedido.getId());
            }
        }
    }
    
    // ============================================
    // MÉTODO: actualizar (con Connection para transacciones)
    // ============================================
    
    @Override
    public void actualizar(Pedido pedido, Connection conn) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(UPDATE_SQL)) {
            ps.setString(1, pedido.getNumero());
            ps.setDate(2, pedido.getFecha() != null ? Date.valueOf(pedido.getFecha()) : null);
            ps.setString(3, pedido.getClienteNombre());
            ps.setObject(4, pedido.getTotal());
            ps.setString(5, pedido.getEstado() != null ? pedido.getEstado().name() : null);
            
            if (pedido.getEnvio() != null && pedido.getEnvio().getId() != null) {
                ps.setLong(6, pedido.getEnvio().getId());
            } else {
                ps.setNull(6, Types.BIGINT);
            }
            
            ps.setLong(7, pedido.getId());
            
            int rows = ps.executeUpdate();
            if (rows == 0) {
                throw new SQLException("No se encontró el pedido con ID: " + pedido.getId());
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
                throw new SQLException("No se encontró el pedido con ID: " + id);
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
                throw new SQLException("No se encontró el pedido con ID: " + id);
            }
        }
    }
    
    // ============================================
    // MÉTODO ADICIONAL: buscar por número
    // ============================================
    
    /**
     * Busca un pedido por su número (campo UNIQUE).
     * Incluye el envío asociado mediante LEFT JOIN.
     * 
     * @param numero Número del pedido
     * @return Pedido encontrado o null
     * @throws SQLException si ocurre un error
     */
    public Pedido buscarPorNumero(String numero) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_BY_NUMERO_SQL)) {
            
            ps.setString(1, numero);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapearPedidoConEnvio(rs);
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
    private void setParametrosPedido(PreparedStatement ps, Pedido pedido) throws SQLException {
        ps.setBoolean(1, pedido.getEliminado());
        ps.setString(2, pedido.getNumero());
        ps.setDate(3, pedido.getFecha() != null ? Date.valueOf(pedido.getFecha()) : null);
        ps.setString(4, pedido.getClienteNombre());
        ps.setObject(5, pedido.getTotal());
        ps.setString(6, pedido.getEstado() != null ? pedido.getEstado().name() : null);
        
        if (pedido.getEnvio() != null && pedido.getEnvio().getId() != null) {
            ps.setLong(7, pedido.getEnvio().getId());
        } else {
            ps.setNull(7, Types.BIGINT);
        }
    }
    
    /**
     * Mapea un ResultSet a un objeto Pedido CON su Envio asociado.
     * Usa LEFT JOIN para incluir el envío si existe.
     * 
     * @param rs ResultSet con datos de pedido y envío
     * @return Pedido mapeado con su envío (si tiene)
     * @throws SQLException si ocurre un error
     */
    private Pedido mapearPedidoConEnvio(ResultSet rs) throws SQLException {
        Pedido pedido = new Pedido();

        // Mapear PEDIDO con alias
        pedido.setId(rs.getLong("p_id"));
        pedido.setEliminado(rs.getBoolean("p_eliminado"));
        pedido.setNumero(rs.getString("numero"));

        Date fecha = rs.getDate("fecha");
        if (fecha != null) {
            pedido.setFecha(fecha.toLocalDate());
        }

        pedido.setClienteNombre(rs.getString("cliente_nombre"));
        pedido.setTotal(rs.getObject("total", Double.class));

        String estadoStr = rs.getString("p_estado");
        if (estadoStr != null) {
            pedido.setEstado(EstadoPedido.valueOf(estadoStr));
        }

        // Mapear ENVIO si existe
        Long envioId = rs.getObject("e_id", Long.class);
        if (envioId != null && envioId > 0) {
            Envio envio = mapearEnvioDesdeJoin(rs);
            pedido.setEnvio(envio);
        }

        return pedido;
    }

    private Envio mapearEnvioDesdeJoin(ResultSet rs) throws SQLException {
        Envio envio = new Envio();

        envio.setId(rs.getLong("e_id"));
        envio.setEliminado(rs.getBoolean("e_eliminado"));
        envio.setTracking(rs.getString("tracking"));

        String empresaStr = rs.getString("empresa");
        if (empresaStr != null) {
            envio.setEmpresa(Envio.EmpresaEnvio.valueOf(empresaStr));
        }

        String tipoStr = rs.getString("tipo");
        if (tipoStr != null) {
            envio.setTipo(Envio.TipoEnvio.valueOf(tipoStr));
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

        String estadoStr = rs.getString("e_estado");
        if (estadoStr != null) {
            envio.setEstado(Envio.EstadoEnvio.valueOf(estadoStr));
        }

        return envio;
    }
}
