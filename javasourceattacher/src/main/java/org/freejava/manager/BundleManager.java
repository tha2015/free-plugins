package org.freejava.manager;

import java.util.List;
import java.util.Map;

import org.freejava.model.Bundle;

public interface BundleManager {
	Bundle add(Bundle bundle);
	Bundle findById(long id) throws Exception;
	List<Bundle> findByConditions(Map<String, Object[]> criteriaValues, int startPosition, int maxResult);
	Long findMaxId();
}
