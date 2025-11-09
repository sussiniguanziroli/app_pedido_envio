package Service;

import java.sql.Connection;
import java.util.List;
import Dao.EnvioDAO;
import Models.Envio;

/**
 * Servicio de negocio para la entidad Envio.
 * Aplica validaciones y coordina operaciones.
 */
public class EnvioServiceImpl implements GenericService<Envio> {
    private final EnvioDAO envioDAO;
    
    public EnvioServiceImpl(EnvioDAO envioDAO) {
        if (envioDAO == null) {
            throw new IllegalArgumentException("EnvioDAO no puede ser null");
        }
        this.envioDAO = envioDAO;
    }
    
    @Override
    public void insertar(Envio envio) throws Exception {
        validateEnvio(envio);
        envioDAO.insertar(envio);
    }
    
    /**
     * Inserta un envío dentro de una transacción existente.
     * Usado por PedidoService.crearPedidoConEnvio()
     */
    public void insertarTx(Envio envio, Connection conn) throws Exception {
        validateEnvio(envio);
        envioDAO.insertTx(envio, conn);
    }
    
    @Override
    public void actualizar(Envio envio) throws Exception {
        validateEnvio(envio);
        if (envio.getId() <= 0) {
            throw new IllegalArgumentException("El ID del envío debe ser mayor a 0 para actualizar");
        }
        envioDAO.actualizar(envio);
    }
    
    @Override
    public void eliminar(int id) throws Exception {
        if (id <= 0) {
            throw new IllegalArgumentException("El ID debe ser mayor a 0");
        }
        envioDAO.eliminar(id);
    }
    
    @Override
    public Envio getById(int id) throws Exception {
        if (id <= 0) {
            throw new IllegalArgumentException("El ID debe ser mayor a 0");
        }
        return envioDAO.getById(id);
    }
    
    @Override
    public List<Envio> getAll() throws Exception {
        return envioDAO.getAll();
    }
    
    /**
     * Busca un envío por su tracking (campo único).
     */
    public Envio buscarPorTracking(String tracking) throws Exception {
        if (tracking == null || tracking.trim().isEmpty()) {
            throw new IllegalArgumentException("El tracking no puede estar vacío");
        }
        return envioDAO.buscarPorTracking(tracking);
    }
    
    /**
     * Valida que un envío tenga datos correctos.
     * 
     * Reglas de negocio:
     * - Empresa, tipo, costo y estado son obligatorios
     * - Costo debe ser >= 0
     */
    private void validateEnvio(Envio envio) {
        if (envio == null) {
            throw new IllegalArgumentException("El envío no puede ser null");
        }
        if (envio.getEmpresa() == null) {
            throw new IllegalArgumentException("La empresa es obligatoria");
        }
        if (envio.getTipo() == null) {
            throw new IllegalArgumentException("El tipo de envío es obligatorio");
        }
        if (envio.getCosto() == null || envio.getCosto() < 0) {
            throw new IllegalArgumentException("El costo debe ser mayor o igual a 0");
        }
        if (envio.getEstado() == null) {
            throw new IllegalArgumentException("El estado es obligatorio");
        }
    }
}