package Dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * Interfaz genérica que define operaciones CRUD para cualquier entidad.
 * Define el contrato que deben cumplir todos los DAOs del sistema.
 * 
 * @param <T> Tipo de entidad que maneja el DAO
 * 
 * Propósito:
 * - Evitar duplicación de código entre DAOs
 * - Garantizar consistencia en las operaciones CRUD
 * - Facilitar el mantenimiento y escalabilidad
 * 
 * Patrón de diseño: Data Access Object (DAO)
 */
public interface GenericDAO<T> {
    
    /**
     * Crea una nueva entidad en la base de datos.
     * Genera automáticamente el ID y lo asigna a la entidad.
     * 
     * @param entidad Entidad a crear
     * @throws SQLException si ocurre un error de base de datos
     */
    void crear(T entidad) throws SQLException;
    
    /**
     * Crea una entidad usando una conexión externa (para transacciones).
     * NO cierra la conexión (es responsabilidad del caller).
     * 
     * @param entidad Entidad a crear
     * @param conn Conexión externa proporcionada por el Service
     * @throws SQLException si ocurre un error de base de datos
     */
    void crear(T entidad, Connection conn) throws SQLException;
    
    /**
     * Lee una entidad por su ID.
     * Solo retorna entidades activas (eliminado = false).
     * 
     * @param id ID de la entidad a buscar
     * @return Entidad encontrada o null si no existe
     * @throws SQLException si ocurre un error de base de datos
     */
    T leer(Long id) throws SQLException;
    
    /**
     * Obtiene todas las entidades activas (no eliminadas).
     * 
     * @return Lista de entidades activas
     * @throws SQLException si ocurre un error de base de datos
     */
    List<T> leerTodos() throws SQLException;
    
    /**
     * Actualiza una entidad existente.
     * 
     * @param entidad Entidad con datos actualizados (debe tener ID)
     * @throws SQLException si ocurre un error o la entidad no existe
     */
    void actualizar(T entidad) throws SQLException;
    
    /**
     * Actualiza una entidad usando una conexión externa (para transacciones).
     * NO cierra la conexión (es responsabilidad del caller).
     * 
     * @param entidad Entidad a actualizar
     * @param conn Conexión externa proporcionada por el Service
     * @throws SQLException si ocurre un error de base de datos
     */
    void actualizar(T entidad, Connection conn) throws SQLException;
    
    /**
     * Elimina lógicamente una entidad (soft delete).
     * Marca eliminado = true sin borrar físicamente el registro.
     * 
     * @param id ID de la entidad a eliminar
     * @throws SQLException si ocurre un error o la entidad no existe
     */
    void eliminar(Long id) throws SQLException;
    
    /**
     * Elimina lógicamente una entidad usando una conexión externa (para transacciones).
     * NO cierra la conexión (es responsabilidad del caller).
     * 
     * @param id ID de la entidad a eliminar
     * @param conn Conexión externa proporcionada por el Service
     * @throws SQLException si ocurre un error de base de datos
     */
    void eliminar(Long id, Connection conn) throws SQLException;
}