package org.freejava.dependency.classparser.impl;

import java.util.HashMap;
import java.util.Map;

import org.apache.maven.shared.dependency.analyzer.asm.DefaultClassVisitor;
import org.apache.maven.shared.dependency.analyzer.asm.ResultCollector;
import org.freejava.dependency.model.Name;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.signature.SignatureVisitor;

public class MyDefaultClassVisitor extends DefaultClassVisitor {
    private String name;
    private Map<String, Integer> types = new HashMap<String, Integer>();

    public MyDefaultClassVisitor(SignatureVisitor signatureVisitor, AnnotationVisitor annotationVisitor, FieldVisitor fieldVisitor, MethodVisitor methodVisitor, ResultCollector resultCollector) {
        super(signatureVisitor, annotationVisitor, fieldVisitor, methodVisitor, resultCollector);
    }
    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        super.visit(version, access, name, signature, superName, interfaces);

        this.name = name;

        if (interfaces!= null && interfaces.length > 0) {
            for (String interfaceName: interfaces) {
                this.types.put(interfaceName, Name.INTERFACE_VAL);
            }
        }
        if ((access & Opcodes.ACC_ANNOTATION) != 0) {
            this.types.put(name, Name.ANNOTATION_VAL);
        } else if ((access & Opcodes.ACC_ENUM) != 0) {
            this.types.put(name, Name.ENUM_VAL);
        } else if ((access & Opcodes.ACC_INTERFACE) != 0) {
            this.types.put(name, Name.INTERFACE_VAL);
        } else {
            this.types.put(name, Name.CLASS_VAL);
        }
    }

    public String getName() {
        return name;
    }

    public Map<String, Integer> getTypes() {
        return types;
    }

}
