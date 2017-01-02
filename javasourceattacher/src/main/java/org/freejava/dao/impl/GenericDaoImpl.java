package org.freejava.dao.impl;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.freejava.dao.GenericDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * A Generic DAO class.
 *
 * @param <T> entity type
 * @param <ID> primary key type
 */
@Repository
@Transactional
public abstract class GenericDaoImpl <T, ID extends Serializable>
        implements GenericDao<T, ID> {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(GenericDaoImpl.class);

    private Class<T> persistentClass;

    private EntityManager entityManager;

    /**
     * Default constructor.
     */
    @SuppressWarnings("unchecked")
    public GenericDaoImpl() {
        this.persistentClass = (Class<T>) ((ParameterizedType) getClass()
                .getGenericSuperclass()).getActualTypeArguments()[0];
    }

    @PersistenceContext
    public void setEntityManager(EntityManager entityManager) {
		this.entityManager = entityManager;
	}
    protected EntityManager getEntityManager() {
        return this.entityManager;
    }

    protected Class<T> getPersistentClass() {
        return this.persistentClass;
    }

    public T findById(ID id) {
        return entityManager.find(getPersistentClass(), id);
    }


    public List<T> findByCriteria(Map<String, Object[]> criteriaValues,
            int startPosition, int maxResult) {
        List<T> result;

        String ejbqlString = "select o from " + getPersistentClass().getName() + " o ";
        List<Object> paramValues = new ArrayList<Object>();

        if (!criteriaValues.isEmpty()) {
            String criteria = "";
            int index = 1;
            for (Map.Entry<String, Object[]> entry : criteriaValues.entrySet()) {
                String propertyName = entry.getKey();
                Object[] values = entry.getValue();

                String criterion = propertyName + " = ?" + index++;
                paramValues.add(values[0]);
                for (int i = 1; i < values.length; i++) {
                    criterion += " or " + propertyName + " = ?" + index++;
                    paramValues.add(values[i]);
                }
                criterion = " ( " + criterion + " ) ";

                if (!"".equals(criteria)) {
                    criteria += " and ";
                }
                criteria += criterion;
            }
            ejbqlString += " where " + criteria + " order by id desc";
        }
        LOGGER.debug(ejbqlString + ";" + paramValues );
        Query query = entityManager.createQuery(ejbqlString);
        for (int i = 0; i < paramValues.size(); i++) {
            query.setParameter(i + 1, paramValues.get(i));
        }
        query.setFirstResult(startPosition);
        query.setMaxResults(maxResult);

        result = query.getResultList();

        result.size();

        return result;
    }

    @SuppressWarnings("unchecked")
	public List<T> findAll() {
        List<T> results = entityManager.createQuery("select o from "
        		+ getPersistentClass().getName() + " o").getResultList();
        results.size(); // In JPA apps, we don't need this call!!
        return results;
    }

    public T persist(T object) {
        entityManager.persist(object);
        entityManager.flush();
        return object;
    }

    public void flush() {
        entityManager.flush();
    }

    public void clear() {
        entityManager.clear();
    }

    public void remove(T object) {
    	if (!entityManager.contains(object)) {
    		// if object isn't managed by EM, load it into EM
    		object = entityManager.merge(object);
    	}
    	// object is now a managed object so it can be removed.
        entityManager.remove(object);
    }
}
