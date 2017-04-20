package org.freejava.dependency.gavfinder;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import org.freejava.dependency.model.GAV;

public interface GAVFinder {
	Collection<GAV> find(File file) throws IOException;
}
