package org.freejava.dao;

import org.freejava.model.Bundle;

public interface BundleDao extends GenericDao<Bundle, Long> {

	Long findMaxId();


}
