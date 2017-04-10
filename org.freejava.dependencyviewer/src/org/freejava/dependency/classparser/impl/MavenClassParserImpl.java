package org.freejava.dependency.classparser.impl;

import java.io.InputStream;
import java.util.HashSet;

import org.freejava.dependency.classparser.ClassParser;
import org.freejava.dependency.model.ClassInfo;

public class MavenClassParserImpl implements ClassParser {

    public ClassInfo parse(InputStream is) throws Exception {
        ClassInfo result;

        MyDependencyClassFileVisitor visitor = new MyDependencyClassFileVisitor();

        visitor.visitClass( is );

        result = new ClassInfo(visitor.getClassName(), new HashSet<String>(visitor.getDependencies()), new HashSet<String>(visitor.getInterfaces()));


        return result;
    }

}
