package org.freejava.dependency.builder;

import org.apache.commons.lang3.builder.HashCodeBuilder;

public class Name {
    private String name;
    private int type; // 0-  class, 1 - interface, 2 - package
    private boolean foundViaDependency = false; // nodes found indirectly (derived) from user-selected nodes


    public String getName() {
        return name;
    }
    public boolean isClass() { return type == 0; }
    public boolean isInterface() { return type == 1; }
    public boolean isPackage() { return type == 2; }

    private static Name newName(String name, int type, boolean foundViaDependency) {
        Name result = new Name();
        result.name = name;
        result.type = type;
        result.foundViaDependency = foundViaDependency;
        return result;
    }

    public void setFoundViaDependency(boolean foundViaDependency) {
		this.foundViaDependency = foundViaDependency;
	}

    public boolean isFoundViaDependency() {
		return foundViaDependency;
	}

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Name && this.name.equals(((Name)obj).name) && (this.type == ((Name)obj).type);
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 31).append(name).append(type).build();
    }

    public static Name newClass(String name) {
        return newName(name, 0, false);
    }
    public static Name newInterface(String name) {
        return newName(name, 1, false);
    }
    public static Name newPackage(String name) {
        return newName(name, 2, false);
    }
    public static Name newName(Name name) {
        return newName(name.name, name.type, name.foundViaDependency);

    }

    @Override
    public String toString() {
        return name + (isInterface() ? "(I)" : "");
    }
}
