package Service;

import java.sql.SQLException;
import java.util.List;

/**
 * Interfaz genérica que define operaciones de negocio para cualquier entidad.
 * Define el contrato que deben cumplir todos los Services del sistema.
 * 
 * @param <T> Tipo de entidad que maneja el Service
 * 
 * Propósito:
 * - Encapsular lógica de negocio y validaciones
 * - Proporcionar una capa de abstracción entre Main y DAO
 * - Coordinar transacciones complejas
 * - Garantizar consistencia de datos
 * 
 * Patrón de diseño: Service Layer / Business Logic Layer
 */
public interface GenericService<T> {
    
    /**
     * Crea una nueva entidad después de validar reglas de negocio.
     * 
     * @param entidad Entidad a crear
     * @throws IllegalArgumentException si los datos no son válidos
     * @throws SQLException si ocurre un error de base de datos
     */
    void crear(T entidad) throws IllegalArgumentException, SQLException;
    
    /**
     * Obtiene una entidad por su ID.
     * 
     * @param id ID de la entidad a buscar
     * @return Entidad encontrada o null si no existe
     * @throws IllegalArgumentException si el ID no es válido
     * @throws SQLException si ocurre un error de base de datos
     */
    T obtenerPorId(Long id) throws IllegalArgumentException, SQLException;
    
    /**
     * Obtiene todas las entidades activas (no eliminadas).
     * 
     * @return Lista de entidades activas
     * @throws SQLException si ocurre un error de base de datos
     */
    List<T> obtenerTodos() throws SQLException;
    
    /**
     * Actualiza una entidad existente después de validar reglas de negocio.
     * 
     * @param entidad Entidad con datos actualizados (debe tener ID)
     * @throws IllegalArgumentException si los datos no son válidos
     * @throws SQLException si ocurre un error de base de datos
     */
    void actualizar(T entidad) throws IllegalArgumentException, SQLException;
    
    /**
     * Elimina lógicamente una entidad (soft delete).
     * 
     * @param id ID de la entidad a eliminar
     * @throws IllegalArgumentException si el ID no es válido o no se puede eliminar
     * @throws SQLException si ocurre un error de base de datos
     */
    void eliminar(Long id) throws IllegalArgumentException, SQLException;
}