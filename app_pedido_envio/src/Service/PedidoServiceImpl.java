package Service;

import Config.DatabaseConnection;
import Dao.PedidoDAO;
import Models.Pedido;
import Models.Pedido.EstadoPedido;
import Config.TransactionManager;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * Implementación del Service para Pedido.
 * Contiene toda la lógica de negocio relacionada con pedidos.
 * 
 * IMPORTANTE: Este servicio coordina la creación transaccional de:
 * 1. Envio (primero, para obtener su ID)
 * 2. Pedido (después, usando el envio_id)
 * 
 * Responsabilidades:
 * - Validar datos de pedido antes de crear/actualizar
 * - Verificar unicidad de número de pedido
 * - Coordinar transacciones entre Pedido y Envio
 * - Gestionar la relación unidireccional Pedido→Envio
 */
public class PedidoServiceImpl implements GenericService<Pedido> {
    
    private final PedidoDAO pedidoDAO;
    private final EnvioServiceImpl envioService;
    
    // ============================================
    // CONSTRUCTORES
    // ============================================
    
    /**
     * Constructor por defecto que inicializa DAOs y Services.
     */
    public PedidoServiceImpl() {
        this.pedidoDAO = new PedidoDAO();
        this.envioService = new EnvioServiceImpl();
    }
    
    /**
     * Constructor con inyección de dependencia (para testing o flexibilidad).
     * 
     * @param pedidoDAO DAO de pedido
     * @param envioService Service de envío
     * @throws IllegalArgumentException si algún parámetro es null
     */
    public PedidoServiceImpl(PedidoDAO pedidoDAO, EnvioServiceImpl envioService) {
        if (pedidoDAO == null) {
            throw new IllegalArgumentException("PedidoDAO no puede ser null");
        }
        if (envioService == null) {
            throw new IllegalArgumentException("EnvioService no puede ser null");
        }
        this.pedidoDAO = pedidoDAO;
        this.envioService = envioService;
    }
    
    // ============================================
    // MÉTODO: crear (sin envío o con envío existente)
    // ============================================
    
    @Override
    public void crear(Pedido pedido) throws IllegalArgumentException, SQLException {
        // Validar datos básicos
        validarPedido(pedido);
        
        // Validar unicidad de número
        validarNumeroUnico(pedido.getNumero(), null);
        
        // Si tiene envío NUEVO (sin ID), crearlo primero
        if (pedido.getEnvio() != null && (pedido.getEnvio().getId() == null || pedido.getEnvio().getId() <= 0)) {
            envioService.crear(pedido.getEnvio());
        }
        
        // Crear pedido
        pedidoDAO.crear(pedido);
    }
    
    /**
     * Crea un pedido con su envío en una TRANSACCIÓN ATÓMICA.
     * Si algo falla, hace rollback de todo (pedido y envío).
     * 
     * Este es el método RECOMENDADO para crear pedidos con envío nuevo.
     * 
     * @param pedido Pedido a crear (debe tener envío asociado)
     * @throws IllegalArgumentException si los datos no son válidos
     * @throws SQLException si ocurre un error de base de datos
     */
    public void crearPedidoConEnvio(Pedido pedido) throws IllegalArgumentException, SQLException {
        validarPedido(pedido);
        validarNumeroUnico(pedido.getNumero(), null);
        
        if (pedido.getEnvio() == null) {
            throw new IllegalArgumentException("El pedido debe tener un envío asociado");
        }
        
        Connection conn = null;
        TransactionManager tm = null;
        
        try {
            conn = DatabaseConnection.getConnection();
            tm = new TransactionManager(conn);
            tm.startTransaction();
            
            try {
                // 1. Crear el envío primero (obtiene ID autogenerado)
                envioService.crear(pedido.getEnvio(), tm.getConnection());
                
                // 2. Crear el pedido (ya tiene envio_id)
                pedidoDAO.crear(pedido, conn);
                
                // 3. Todo OK → commit
                tm.commit();
                
            } catch (Exception e) {
                // Algo falló → rollback
                tm.rollback();
                throw new SQLException("Error en transacción al crear pedido con envío: " + e.getMessage(), e);
            }
            
        } finally {
            if (tm != null) {
                tm.close();  // Cierra conexión y hace rollback si es necesario
            }
        }
    }
    
    // ============================================
    // MÉTODO: obtenerPorId
    // ============================================
    
    @Override
    public Pedido obtenerPorId(Long id) throws IllegalArgumentException, SQLException {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("El ID debe ser mayor a 0");
        }
        
        Pedido pedido = pedidoDAO.leer(id);
        
        if (pedido == null) {
            throw new IllegalArgumentException("No se encontró pedido con ID: " + id);
        }
        
        return pedido;
    }
    
    // ============================================
    // MÉTODO: obtenerTodos
    // ============================================
    
    @Override
    public List<Pedido> obtenerTodos() throws SQLException {
        return pedidoDAO.leerTodos();
    }
    
    // ============================================
    // MÉTODO: actualizar
    // ============================================
    
    @Override
    public void actualizar(Pedido pedido) throws IllegalArgumentException, SQLException {
        // Validar que tenga ID
        if (pedido.getId() == null || pedido.getId() <= 0) {
            throw new IllegalArgumentException("El pedido debe tener un ID válido para actualizar");
        }
        
        // Validar que exista
        Pedido existente = pedidoDAO.leer(pedido.getId());
        if (existente == null) {
            throw new IllegalArgumentException("No se encontró pedido con ID: " + pedido.getId());
        }
        
        // Validar datos
        validarPedido(pedido);
        
        // Validar número único (excluyendo el actual)
        validarNumeroUnico(pedido.getNumero(), pedido.getId());
        
        // Si tiene envío asociado, actualizarlo también
        if (pedido.getEnvio() != null && pedido.getEnvio().getId() != null && pedido.getEnvio().getId() > 0) {
            envioService.actualizar(pedido.getEnvio());
        }
        
        // Actualizar pedido
        pedidoDAO.actualizar(pedido);
    }
    
    // ============================================
    // MÉTODO: eliminar
    // ============================================
    
    @Override
    public void eliminar(Long id) throws IllegalArgumentException, SQLException {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("El ID debe ser mayor a 0");
        }
        
        // Validar que exista
        Pedido pedido = pedidoDAO.leer(id);
        if (pedido == null) {
            throw new IllegalArgumentException("No se encontró pedido con ID: " + id);
        }
        
        // Eliminar pedido (soft delete)
        pedidoDAO.eliminar(id);
        
        // OPCIONAL: También eliminar el envío asociado
        // Si descomentas esto, el envío también se marca como eliminado
        // if (pedido.getEnvio() != null && pedido.getEnvio().getId() != null) {
        //     envioService.eliminar(pedido.getEnvio().getId());
        // }
    }
    
    // ============================================
    // MÉTODO ADICIONAL: buscarPorNumero
    // ============================================
    
    /**
     * Busca un pedido por su número.
     * 
     * @param numero Número del pedido
     * @return Pedido encontrado o null si no existe
     * @throws IllegalArgumentException si número es vacío
     * @throws SQLException si ocurre un error de base de datos
     */
    public Pedido buscarPorNumero(String numero) throws IllegalArgumentException, SQLException {
        if (numero == null || numero.trim().isEmpty()) {
            throw new IllegalArgumentException("El número de pedido no puede estar vacío");
        }
        
        return pedidoDAO.buscarPorNumero(numero);
    }
    
    /**
     * Obtiene el servicio de envíos.
     * Útil para que Main pueda acceder a operaciones de envío a través del PedidoService.
     * 
     * @return Service de envíos
     */
    public EnvioServiceImpl getEnvioService() {
        return this.envioService;
    }
    
    // ============================================
    // VALIDACIONES PRIVADAS
    // ============================================
    
    /**
     * Valida que un pedido cumpla todas las reglas de negocio.
     * 
     * Reglas:
     * - Pedido no puede ser null
     * - Número: obligatorio, máx. 20 caracteres
     * - Fecha: obligatoria
     * - Cliente: obligatorio, máx. 120 caracteres
     * - Total: obligatorio, >= 0
     * - Estado: obligatorio
     * 
     * @param pedido Pedido a validar
     * @throws IllegalArgumentException si alguna validación falla
     */
    private void validarPedido(Pedido pedido) throws IllegalArgumentException {
        if (pedido == null) {
            throw new IllegalArgumentException("El pedido no puede ser null");
        }
        
        // Validar número
        if (pedido.getNumero() == null || pedido.getNumero().trim().isEmpty()) {
            throw new IllegalArgumentException("El número de pedido es obligatorio");
        }
        
        if (pedido.getNumero().length() > 20) {
            throw new IllegalArgumentException("El número de pedido no puede exceder 20 caracteres");
        }
        
        // Validar fecha
        if (pedido.getFecha() == null) {
            throw new IllegalArgumentException("La fecha es obligatoria");
        }
        
        // Validar cliente
        if (pedido.getClienteNombre() == null || pedido.getClienteNombre().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del cliente es obligatorio");
        }
        
        if (pedido.getClienteNombre().length() > 120) {
            throw new IllegalArgumentException("El nombre del cliente no puede exceder 120 caracteres");
        }
        
        // Validar total
        if (pedido.getTotal() == null) {
            throw new IllegalArgumentException("El total es obligatorio");
        }
        
        if (pedido.getTotal() < 0) {
            throw new IllegalArgumentException("El total no puede ser negativo");
        }
        
        // Validar estado
        if (pedido.getEstado() == null) {
            throw new IllegalArgumentException("El estado es obligatorio");
        }
    }
    
    /**
     * Valida que el número de pedido sea único en el sistema.
     * 
     * @param numero Número a validar
     * @param idExcluir ID del pedido a excluir de la validación (para UPDATE)
     * @throws IllegalArgumentException si el número ya existe
     * @throws SQLException si ocurre un error de base de datos
     */
    private void validarNumeroUnico(String numero, Long idExcluir) 
            throws IllegalArgumentException, SQLException {
        
        Pedido existente = pedidoDAO.buscarPorNumero(numero);
        
        if (existente != null) {
            // Si estamos actualizando, excluir el propio registro
            if (idExcluir == null || !existente.getId().equals(idExcluir)) {
                throw new IllegalArgumentException(
                    "Ya existe un pedido con el número: " + numero
                );
            }
        }
    }
}