package org.freejava.dao;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * A generic DAO interface.
 *
 * @param <T> entity type
 * @param <ID> primary key type
 */
public interface GenericDao<T, ID extends Serializable> {

    /**
     * Loads read-only (detached) entity by ID.
     *
     * @param id ID
     * @return entity
     * @throws Exception if any error happen
     */
    T findById(ID id) throws Exception;

    List<T> findByCriteria(Map<String, Object[]> criteriaValues, int startPosition, int maxResult);

    List<T> findAll();

    /**
     * Store <code>object</code> in the database.
     *
     * @param object the instance to save in the database
     */
    public T persist(T object);

    /**
     * Remove <code>object</code> from the database.
     *
     * @param object the object to be removed from the database
     */
    public void remove(T object);

    /**
     * Taken from the EntityManager documentation, Synchronize the persistence
     * context to the underlying database.
     *
     */
    public void flush();

    /**
     * Taken from the EntityManager documentation: Clear the persistence
     * context, causing all managed entities to become detached. Changes made to
     * entities that have not been flushed to the database will not be
     * persisted.
     *
     */
    public void clear();

}
