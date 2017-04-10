package org.freejava.tools.scopeparser;

import java.io.File;
import java.util.Map;

import org.freejava.dependency.model.ClassInfo;
import org.freejava.dependency.model.FileParsingScope;

public interface ScopeParser {
	Map<ClassInfo, File> parse(FileParsingScope scope) throws Exception;
}
