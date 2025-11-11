package Models;

/**
 * Clase base abstracta para todas las entidades del sistema Pedido-Envío.
 * Implementa el patrón de soft delete mediante el campo 'eliminado'.
 *
 * Propósito:
 * - Proporcionar campos comunes a todas las entidades (id, eliminado)
 * - Implementar el patrón de herencia para evitar duplicación de código
 * - Soportar eliminación lógica en lugar de eliminación física
 * - Mantener integridad referencial y datos históricos
 *
 * Patrón de diseño: Template (clase base abstracta)
 * 
 * Entidades que heredan:
 * - Pedido: Representa un pedido realizado por un cliente
 * - Envio: Representa el envío asociado a un pedido
 */
public abstract class Base {
    
    /**
     * Identificador único de la entidad.
     * Generado automáticamente por la base de datos (AUTO_INCREMENT).
     * Tipo Long para soportar BIGINT en MySQL (mayor rango que INT).
     */
    private Long id;
    
    /**
     * Flag de eliminación lógica (soft delete).
     * - true: La entidad está marcada como eliminada (no se mostrará en consultas activas)
     * - false: La entidad está activa y visible
     *
     * Beneficios del soft delete:
     * - Mantiene integridad referencial
     * - Permite auditoría y recuperación de datos
     * - Evita errores de foreign key al eliminar registros relacionados
     */
    private Boolean eliminado;
    
    /**
     * Constructor completo con todos los campos.
     * Usado por los DAOs al reconstruir entidades desde la base de datos.
     *
     * @param id Identificador único de la entidad
     * @param eliminado Estado de eliminación (true = eliminado, false = activo)
     */
    protected Base(Long id, Boolean eliminado) {
        this.id = id;
        this.eliminado = eliminado;
    }
    
    /**
     * Constructor por defecto.
     * Inicializa una entidad nueva sin ID (será asignado automáticamente por la BD).
     * Por defecto, las entidades nuevas NO están eliminadas (eliminado = false).
     * 
     * Uso típico: Al crear nuevas instancias antes de persistir en BD.
     */
    protected Base() {
        this.eliminado = false;
    }
    
    /**
     * Obtiene el ID de la entidad.
     * 
     * @return ID de la entidad, null si aún no ha sido persistida en la BD
     */
    public Long getId() {
        return id;
    }
    
    /**
     * Establece el ID de la entidad.
     * Típicamente llamado por el DAO después de insertar en la BD para asignar el ID generado.
     *
     * @param id Nuevo ID de la entidad (generado por AUTO_INCREMENT)
     */
    public void setId(Long id) {
        this.id = id;
    }
    
    /**
     * Verifica si la entidad está marcada como eliminada (soft delete).
     * 
     * @return true si está eliminada lógicamente, false si está activa
     */
    public Boolean getEliminado() {
        return eliminado;
    }
    
    /**
     * Alternativa compatible con convención JavaBean 'isXxx' para booleanos.
     * Algunos frameworks requieren este método además de getEliminado().
     * 
     * @return true si está eliminada, false si está activa
     */
    public boolean isEliminado() {
        return eliminado != null && eliminado;
    }
    
    /**
     * Marca o desmarca la entidad como eliminada (soft delete).
     * 
     * Uso en Service Layer:
     * - Para eliminar: entidad.setEliminado(true)
     * - Para reactivar: entidad.setEliminado(false)
     * 
     * Nota: La eliminación es lógica, el registro permanece en la BD.
     *
     * @param eliminado true para marcar como eliminada, false para reactivar
     */
    public void setEliminado(Boolean eliminado) {
        this.eliminado = eliminado;
    }
    
    /**
     * Verifica si la entidad está activa (no eliminada).
     * Método auxiliar para legibilidad en el código de negocio.
     * 
     * @return true si la entidad está activa, false si está eliminada
     */
    public boolean isActivo() {
        return !isEliminado();
    }
    
    /**
     * Verifica si la entidad ha sido persistida en la base de datos.
     * Una entidad se considera persistida si tiene un ID asignado.
     * 
     * @return true si la entidad tiene ID (está en BD), false si es nueva
     */
    public boolean isPersistido() {
        return id != null && id > 0;
    }
}