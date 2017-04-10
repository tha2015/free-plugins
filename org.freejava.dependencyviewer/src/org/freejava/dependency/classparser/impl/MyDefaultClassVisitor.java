package org.freejava.dependency.classparser.impl;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.apache.maven.shared.dependency.analyzer.asm.DefaultClassVisitor;
import org.apache.maven.shared.dependency.analyzer.asm.ResultCollector;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.signature.SignatureVisitor;

public class MyDefaultClassVisitor extends DefaultClassVisitor {
    private String name;
    private Set<String> interfaces = new HashSet<String>();

    public MyDefaultClassVisitor(SignatureVisitor signatureVisitor, AnnotationVisitor annotationVisitor, FieldVisitor fieldVisitor, MethodVisitor methodVisitor, ResultCollector resultCollector) {
        super(signatureVisitor, annotationVisitor, fieldVisitor, methodVisitor, resultCollector);
    }
    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        super.visit(version, access, name, signature, superName, interfaces);

        this.name = name;
        if (interfaces!= null && interfaces.length > 0) {
            this.interfaces.addAll(Arrays.asList(interfaces));
        }
        if ((access & Opcodes.ACC_INTERFACE) != 0) {
            this.interfaces.add(name);
        }
    }
    public String getName() {
        return name;
    }
    public Set<String> getInterfaces() {
        return interfaces;
    }

}
