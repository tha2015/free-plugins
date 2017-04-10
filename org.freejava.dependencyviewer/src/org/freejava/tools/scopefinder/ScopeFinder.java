package org.freejava.tools.scopefinder;

import org.freejava.dependency.model.FileParsingScope;

public interface ScopeFinder {
	FileParsingScope findScope() throws Exception;

}
