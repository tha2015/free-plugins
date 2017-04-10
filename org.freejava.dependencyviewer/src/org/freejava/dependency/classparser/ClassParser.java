package org.freejava.dependency.classparser;

import java.io.InputStream;

import org.freejava.dependency.model.ClassInfo;

public interface ClassParser {
    ClassInfo parse(InputStream classFile) throws Exception;
}
