package org.freejava.dependency.model;

import java.util.Set;

public class ClassInfo {
    private String className;
    private Set<String> dependencies;
    private Set<String> interfaces;

    public ClassInfo(String className, Set<String> dependencies, Set<String> interfaces) {
        super();
        this.className = className;
        this.dependencies = dependencies;
        this.interfaces = interfaces;
    }

    public String getClassName() {
        return className;
    }


    public Set<String> getDependencies() {
        return dependencies;
    }


    public Set<String> getInterfaces() {
        return interfaces;
    }


    @Override
    public String toString() {
        return "this: " + className + "; others: " + dependencies + "; interfaces: " + interfaces;
    }
}
