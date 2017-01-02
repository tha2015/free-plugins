package org.freejava.manager.impl;

import java.util.List;
import java.util.Map;

import org.freejava.dao.LocationDao;
import org.freejava.manager.LocationManager;
import org.freejava.model.Location;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class LocationManagerImpl implements LocationManager {
	private LocationDao locationDao;

    @Autowired
    public LocationManagerImpl(LocationDao locationDao) {
        this.locationDao = locationDao;
    }

	@Override
	public Location add(Location location) {
		return locationDao.persist(location);
	}


	@Override
	public Location findById(long id) throws Exception {
		return locationDao.findById(id);
	}

	@Override
	public List<Location> findByConditions(Map<String, Object[]> criteriaValues, int startPosition, int maxResult) {
		List<Location> result = locationDao.findByCriteria(criteriaValues, startPosition, maxResult);
		return result;
	}

}
