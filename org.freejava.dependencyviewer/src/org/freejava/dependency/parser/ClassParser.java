package org.freejava.dependency.parser;

import java.io.InputStream;

public interface ClassParser {
    ClassInfo parse(InputStream classFile) throws Exception;
}
