package org.freejava.dependency.model;

import java.util.Map;
import java.util.Set;

public class ClassInfo {
    private String className;
    private Set<String> dependencies;
    private Map<String, Integer> types;

    public ClassInfo(String className, Set<String> dependencies, Map<String, Integer> types) {
        super();
        this.className = className;
        this.dependencies = dependencies;
        this.types = types;
    }

    public String getClassName() {
        return className;
    }


    public Set<String> getDependencies() {
        return dependencies;
    }


    public Map<String, Integer> getTypes() {
        return types;
    }


    @Override
    public String toString() {
        return "this: " + className + "; others: " + dependencies + "; types: " + types;
    }
}
