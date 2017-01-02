package org.freejava.dao.impl;

import org.freejava.dao.LocationDao;
import org.freejava.model.Location;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public class LocationDaoImpl extends GenericDaoImpl<Location, Long> implements LocationDao {


}
