package Service;

import Dao.EnvioDAO;
import Models.Envio;
import Models.Envio.EmpresaEnvio;
import Models.Envio.TipoEnvio;
import Models.Envio.EstadoEnvio;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * Implementación del Service para Envio.
 * Contiene toda la lógica de negocio relacionada con envíos.
 * 
 * Responsabilidades:
 * - Validar datos de envío antes de crear/actualizar
 * - Verificar unicidad de tracking
 * - Garantizar consistencia de fechas
 * - Coordinar operaciones con EnvioDAO
 */
public class EnvioServiceImpl implements GenericService<Envio> {
    
    private final EnvioDAO envioDAO;
    
    // ============================================
    // CONSTRUCTORES
    // ============================================
    
    /**
     * Constructor por defecto que inicializa el DAO.
     */
    public EnvioServiceImpl() {
        this.envioDAO = new EnvioDAO();
    }
    
    /**
     * Constructor con inyección de dependencia (para testing o flexibilidad).
     * 
     * @param envioDAO DAO de envío
     * @throws IllegalArgumentException si envioDAO es null
     */
    public EnvioServiceImpl(EnvioDAO envioDAO) {
        if (envioDAO == null) {
            throw new IllegalArgumentException("EnvioDAO no puede ser null");
        }
        this.envioDAO = envioDAO;
    }
    
    // ============================================
    // MÉTODO: crear (sin transacción)
    // ============================================
    
    @Override
    public void crear(Envio envio) throws IllegalArgumentException, SQLException {
        // Validar datos básicos
        validarEnvio(envio);
        
        // Validar unicidad de tracking
        validarTrackingUnico(envio.getTracking(), null);
        
        // Crear en BD
        envioDAO.crear(envio);
    }
    
    // ============================================
    // MÉTODO: crear (con Connection para transacciones)
    // ============================================
    
    /**
     * Crea un envío usando una conexión externa (para transacciones).
     * Usado por PedidoService al crear pedido con envío en la misma transacción.
     * 
     * @param envio Envío a crear
     * @param conn Conexión externa (NO se cierra aquí)
     * @throws IllegalArgumentException si los datos no son válidos
     * @throws SQLException si ocurre un error de base de datos
     */
    public void crear(Envio envio, Connection conn) throws IllegalArgumentException, SQLException {
        validarEnvio(envio);
        validarTrackingUnico(envio.getTracking(), null);
        envioDAO.crear(envio, conn);
    }
    
    // ============================================
    // MÉTODO: obtenerPorId
    // ============================================
    
    @Override
    public Envio obtenerPorId(Long id) throws IllegalArgumentException, SQLException {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("El ID debe ser mayor a 0");
        }
        
        Envio envio = envioDAO.leer(id);
        
        if (envio == null) {
            throw new IllegalArgumentException("No se encontró envío con ID: " + id);
        }
        
        return envio;
    }
    
    // ============================================
    // MÉTODO: obtenerTodos
    // ============================================
    
    @Override
    public List<Envio> obtenerTodos() throws SQLException {
        return envioDAO.leerTodos();
    }
    
    // ============================================
    // MÉTODO: actualizar (sin transacción)
    // ============================================
    
    @Override
    public void actualizar(Envio envio) throws IllegalArgumentException, SQLException {
        // Validar que tenga ID
        if (envio.getId() == null || envio.getId() <= 0) {
            throw new IllegalArgumentException("El envío debe tener un ID válido para actualizar");
        }
        
        // Validar que exista
        Envio existente = envioDAO.leer(envio.getId());
        if (existente == null) {
            throw new IllegalArgumentException("No se encontró envío con ID: " + envio.getId());
        }
        
        // Validar datos
        validarEnvio(envio);
        
        // Validar tracking único (excluyendo el actual)
        validarTrackingUnico(envio.getTracking(), envio.getId());
        
        // Actualizar en BD
        envioDAO.actualizar(envio);
    }
    
    // ============================================
    // MÉTODO: actualizar (con Connection para transacciones)
    // ============================================
    
    /**
     * Actualiza un envío dentro de una transacción externa.
     * 
     * @param envio Envío a actualizar
     * @param conn Conexión externa (NO se cierra aquí)
     * @throws IllegalArgumentException si los datos no son válidos
     * @throws SQLException si ocurre un error de base de datos
     */
    public void actualizar(Envio envio, Connection conn) throws IllegalArgumentException, SQLException {
        if (envio.getId() == null || envio.getId() <= 0) {
            throw new IllegalArgumentException("El envío debe tener un ID válido para actualizar");
        }
        
        validarEnvio(envio);
        validarTrackingUnico(envio.getTracking(), envio.getId());
        envioDAO.actualizar(envio, conn);
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
        Envio envio = envioDAO.leer(id);
        if (envio == null) {
            throw new IllegalArgumentException("No se encontró envío con ID: " + id);
        }
        
        // Eliminar (soft delete)
        envioDAO.eliminar(id);
    }
    
    // ============================================
    // MÉTODO ADICIONAL: buscarPorTracking
    // ============================================
    
    /**
     * Busca un envío por su código de tracking.
     * 
     * @param tracking Código de seguimiento
     * @return Envío encontrado o null si no existe
     * @throws IllegalArgumentException si tracking es vacío
     * @throws SQLException si ocurre un error de base de datos
     */
    public Envio buscarPorTracking(String tracking) throws IllegalArgumentException, SQLException {
        if (tracking == null || tracking.trim().isEmpty()) {
            throw new IllegalArgumentException("El tracking no puede estar vacío");
        }
        
        return envioDAO.buscarPorTracking(tracking);
    }
    
    // ============================================
    // VALIDACIONES PRIVADAS
    // ============================================
    
    /**
     * Valida que un envío cumpla todas las reglas de negocio.
     * 
     * Reglas:
     * - Envío no puede ser null
     * - Tracking: obligatorio, máx. 40 caracteres
     * - Empresa: obligatoria
     * - Tipo: obligatorio
     * - Costo: obligatorio, >= 0
     * - Estado: obligatorio
     * - Fechas: si ambas existen, fecha_estimada >= fecha_despacho
     * 
     * @param envio Envío a validar
     * @throws IllegalArgumentException si alguna validación falla
     */
    private void validarEnvio(Envio envio) throws IllegalArgumentException {
        if (envio == null) {
            throw new IllegalArgumentException("El envío no puede ser null");
        }
        
        // Validar tracking
        if (envio.getTracking() == null || envio.getTracking().trim().isEmpty()) {
            throw new IllegalArgumentException("El tracking es obligatorio");
        }
        
        if (envio.getTracking().length() > 40) {
            throw new IllegalArgumentException("El tracking no puede exceder 40 caracteres");
        }
        
        // Validar empresa
        if (envio.getEmpresa() == null) {
            throw new IllegalArgumentException("La empresa de envío es obligatoria");
        }
        
        // Validar tipo
        if (envio.getTipo() == null) {
            throw new IllegalArgumentException("El tipo de envío es obligatorio");
        }
        
        // Validar costo
        if (envio.getCosto() == null) {
            throw new IllegalArgumentException("El costo es obligatorio");
        }
        
        if (envio.getCosto() < 0) {
            throw new IllegalArgumentException("El costo no puede ser negativo");
        }
        
        // Validar estado
        if (envio.getEstado() == null) {
            throw new IllegalArgumentException("El estado es obligatorio");
        }
        
        // Validar consistencia de fechas
        validarFechas(envio);
    }
    
    /**
     * Valida que las fechas sean consistentes.
     * Si ambas fechas existen, fecha_estimada debe ser >= fecha_despacho.
     * 
     * @param envio Envío con las fechas a validar
     * @throws IllegalArgumentException si las fechas son inconsistentes
     */
    private void validarFechas(Envio envio) throws IllegalArgumentException {
        if (envio.getFechaDespacho() != null && envio.getFechaEstimada() != null) {
            if (envio.getFechaEstimada().isBefore(envio.getFechaDespacho())) {
                throw new IllegalArgumentException(
                    "La fecha estimada no puede ser anterior a la fecha de despacho"
                );
            }
        }
    }
    
    /**
     * Valida que el tracking sea único en el sistema.
     * 
     * @param tracking Tracking a validar
     * @param idExcluir ID del envío a excluir de la validación (para UPDATE)
     * @throws IllegalArgumentException si el tracking ya existe
     * @throws SQLException si ocurre un error de base de datos
     */
    private void validarTrackingUnico(String tracking, Long idExcluir) 
            throws IllegalArgumentException, SQLException {
        
        Envio existente = envioDAO.buscarPorTracking(tracking);
        
        if (existente != null) {
            // Si estamos actualizando, excluir el propio registro
            if (idExcluir == null || !existente.getId().equals(idExcluir)) {
                throw new IllegalArgumentException(
                    "Ya existe un envío con el tracking: " + tracking
                );
            }
        }
    }
}