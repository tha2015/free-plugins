package org.freejava.dao.impl;

import org.freejava.dao.BundleDao;
import org.freejava.model.Bundle;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public class BundleDaoImpl extends GenericDaoImpl<Bundle, Long> implements BundleDao {

	@Override
	public Long findMaxId() {
        Bundle result = (Bundle) getEntityManager().createQuery("select o from "
        		+ getPersistentClass().getName() + " o order by id desc").setMaxResults(1).getSingleResult();
        return result.getId();
	}
}
