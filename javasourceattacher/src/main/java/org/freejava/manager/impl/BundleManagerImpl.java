package org.freejava.manager.impl;

import java.util.List;
import java.util.Map;

import org.freejava.dao.BundleDao;
import org.freejava.manager.BundleManager;
import org.freejava.model.Bundle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class BundleManagerImpl implements BundleManager {
	private BundleDao bundleDao;

    @Autowired
    public BundleManagerImpl(BundleDao bundleDao) {
        this.bundleDao = bundleDao;
    }

	@Override
	public Bundle add(Bundle bundle) {
		return bundleDao.persist(bundle);
	}


	@Override
	public Bundle findById(long id) throws Exception {
		return bundleDao.findById(id);
	}

	@Override
	public List<Bundle> findByConditions(Map<String, Object[]> criteriaValues, int startPosition, int maxResult) {
		List<Bundle> result = bundleDao.findByCriteria(criteriaValues, startPosition, maxResult);
		return result;
	}

	@Override
	public Long findMaxId() {
		Long result = bundleDao.findMaxId();
		return result;
	}

}
