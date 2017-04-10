package org.freejava.dependency.classparser.impl;

import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

import org.apache.maven.shared.dependency.analyzer.asm.DefaultAnnotationVisitor;
import org.apache.maven.shared.dependency.analyzer.asm.DefaultFieldVisitor;
import org.apache.maven.shared.dependency.analyzer.asm.DefaultMethodVisitor;
import org.apache.maven.shared.dependency.analyzer.asm.DefaultSignatureVisitor;
import org.apache.maven.shared.dependency.analyzer.asm.ResultCollector;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.objectweb.asm.signature.SignatureVisitor;

public final class MyDependencyClassFileVisitor  {
    private String className;
    private Set<String> interfaces;
    private final ResultCollector resultCollector = new ResultCollector();

    public void visitClass(InputStream in ) throws Exception {
        ClassReader reader = new ClassReader( in );
        AnnotationVisitor annotationVisitor = new DefaultAnnotationVisitor( resultCollector );
        SignatureVisitor signatureVisitor = new DefaultSignatureVisitor( resultCollector );
        FieldVisitor fieldVisitor = new DefaultFieldVisitor( annotationVisitor, resultCollector );
        MethodVisitor mv = new DefaultMethodVisitor( annotationVisitor, signatureVisitor, resultCollector );
        MyDefaultClassVisitor classVisitor = new MyDefaultClassVisitor( signatureVisitor, annotationVisitor, fieldVisitor, mv, resultCollector );
        reader.accept( classVisitor, 0 );

        this.className = Type.getObjectType(classVisitor.getName()).getClassName();
        this.interfaces = new HashSet<String>();
        for (String interf : classVisitor.getInterfaces()) {
            this.interfaces.add(Type.getObjectType(interf).getClassName());
        }

    }


    public String getClassName() {
        return className;
    }


    public Set<String> getInterfaces() {
        return interfaces;
    }


    public Set<String> getDependencies() {
        return resultCollector.getDependencies();
    }
}