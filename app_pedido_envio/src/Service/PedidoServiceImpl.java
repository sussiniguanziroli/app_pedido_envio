package Service;

import java.sql.Connection;
import java.util.List;
import Config.DatabaseConnection;
import Config.TransactionManager;
import Dao.PedidoDAO;
import Models.Pedido;

/**
 * Servicio de negocio para la entidad Pedido.
 * Aplica validaciones y coordina operaciones con Envio.
 * 
 * IMPORTANTE: Este servicio coordina la creación transaccional de:
 * 1. Envio (primero, para obtener su ID)
 * 2. Pedido (después, usando el envio_id)
 */
public class PedidoServiceImpl implements GenericService<Pedido> {
    private final PedidoDAO pedidoDAO;
    private final EnvioServiceImpl envioService;
    
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
    
    /**
     * Inserta un pedido nuevo en la BD.
     * Si el pedido tiene envío asociado, lo crea primero.
     */
    @Override
    public void insertar(Pedido pedido) throws Exception {
        validatePedido(pedido);
        validateNumeroUnique(pedido.getNumero(), null);
        
        // Si tiene envío, insertarlo primero para obtener su ID
        if (pedido.getEnvio() != null) {
            if (pedido.getEnvio().getId() == 0) {
                // Envío nuevo: insertar primero
                envioService.insertar(pedido.getEnvio());
            } else {
                // Envío existente: actualizar
                envioService.actualizar(pedido.getEnvio());
            }
        }
        
        pedidoDAO.insertar(pedido);
    }
    
    /**
     * Crea un pedido con su envío en una TRANSACCIÓN.
     * Si algo falla, hace rollback de todo.
     * 
     * Este es el método RECOMENDADO para crear pedidos con envío.
     */
    public void crearPedidoConEnvio(Pedido pedido) throws Exception {
        validatePedido(pedido);
        validateNumeroUnique(pedido.getNumero(), null);
        
        if (pedido.getEnvio() == null) {
            throw new IllegalArgumentException("El pedido debe tener un envío asociado");
        }
        
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            TransactionManager tm = new TransactionManager(conn);
            tm.startTransaction();
            
            try {
                // 1. Crear el envío primero
                envioService.insertarTx(pedido.getEnvio(), conn);
                
                // 2. Crear el pedido (ya tiene envio_id)
                pedidoDAO.insertTx(pedido, conn);
                
                // 3. Todo OK → commit
                tm.commit();
                
            } catch (Exception e) {
                tm.rollback();
                throw new Exception("Error en transacción: " + e.getMessage(), e);
            } finally {
                tm.close();
            }
            
        } finally {
            if (conn != null && !conn.isClosed()) {
                conn.close();
            }
        }
    }
    
    @Override
    public void actualizar(Pedido pedido) throws Exception {
        validatePedido(pedido);
        if (pedido.getId() <= 0) {
            throw new IllegalArgumentException("El ID del pedido debe ser mayor a 0 para actualizar");
        }
        validateNumeroUnique(pedido.getNumero(), pedido.getId());
        
        // Si cambió el envío, actualizarlo también
        if (pedido.getEnvio() != null && pedido.getEnvio().getId() > 0) {
            envioService.actualizar(pedido.getEnvio());
        }
        
        pedidoDAO.actualizar(pedido);
    }
    
    @Override
    public void eliminar(int id) throws Exception {
        if (id <= 0) {
            throw new IllegalArgumentException("El ID debe ser mayor a 0");
        }
        
        // Obtener el pedido para ver si tiene envío
        Pedido pedido = pedidoDAO.getById(id);
        if (pedido == null) {
            throw new IllegalArgumentException("Pedido no encontrado con ID: " + id);
        }
        
        // Eliminar el pedido (soft delete)
        pedidoDAO.eliminar(id);
        
        // OPCIONAL: También eliminar el envío asociado
        // if (pedido.getEnvio() != null) {
        //     envioService.eliminar(pedido.getEnvio().getId());
        // }
    }
    
    @Override
    public Pedido getById(int id) throws Exception {
        if (id <= 0) {
            throw new IllegalArgumentException("El ID debe ser mayor a 0");
        }
        return pedidoDAO.getById(id);
    }
    
    @Override
    public List<Pedido> getAll() throws Exception {
        return pedidoDAO.getAll();
    }
    
    /**
     * Busca un pedido por su número (campo único).
     */
    public Pedido buscarPorNumero(String numero) throws Exception {
        if (numero == null || numero.trim().isEmpty()) {
            throw new IllegalArgumentException("El número de pedido no puede estar vacío");
        }
        return pedidoDAO.buscarPorNumero(numero);
    }
    
    /**
     * Expone el servicio de envíos para que el menú pueda usarlo.
     */
    public EnvioServiceImpl getEnvioService() {
        return this.envioService;
    }
    
    /**
     * Valida que un pedido tenga datos correctos.
     * 
     * Reglas de negocio:
     * - Número, fecha, cliente y total son obligatorios
     * - Total debe ser >= 0
     * - Estado es obligatorio
     */
    private void validatePedido(Pedido pedido) {
        if (pedido == null) {
            throw new IllegalArgumentException("El pedido no puede ser null");
        }
        if (pedido.getNumero() == null || pedido.getNumero().trim().isEmpty()) {
            throw new IllegalArgumentException("El número de pedido no puede estar vacío");
        }
        if (pedido.getFecha() == null) {
            throw new IllegalArgumentException("La fecha no puede ser null");
        }
        if (pedido.getClienteNombre() == null || pedido.getClienteNombre().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del cliente no puede estar vacío");
        }
        if (pedido.getTotal() == null || pedido.getTotal() < 0) {
            throw new IllegalArgumentException("El total debe ser mayor o igual a 0");
        }
        if (pedido.getEstado() == null) {
            throw new IllegalArgumentException("El estado es obligatorio");
        }
    }
    
    /**
     * Valida que el número de pedido sea único.
     * Similar a la validación de DNI en PersonaService.
     */
    private void validateNumeroUnique(String numero, Integer pedidoId) throws Exception {
        Pedido existente = pedidoDAO.buscarPorNumero(numero);
        if (existente != null) {
            if (pedidoId == null || existente.getId() != pedidoId) {
                throw new IllegalArgumentException("Ya existe un pedido con el número: " + numero);
            }
        }
    }
}