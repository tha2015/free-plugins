package org.freejava.dependency.model;

import org.apache.commons.lang3.builder.HashCodeBuilder;

public class Name {
    private String name;
    private int type; // 0-  class, 1 - interface, 2 - package
    private String color;


    public String getName() {
        return name;
    }
    public boolean isClass() { return type == 0; }
    public boolean isInterface() { return type == 1; }
    public boolean isPackage() { return type == 2; }

    private static Name newName(String name, int type, String color) {
        Name result = new Name();
        result.name = name;
        result.type = type;
        result.color = color;
        return result;
    }

    public void setColor(String color) {
		this.color = color;
	}

    public String getColor() {
		return color;
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
        return newName(name, 0, null);
    }
    public static Name newInterface(String name) {
        return newName(name, 1, null);
    }
    public static Name newPackage(String name) {
        return newName(name, 2, null);
    }
    public static Name newName(Name name) {
        return newName(name.name, name.type, name.color);

    }

    @Override
    public String toString() {
        return name + (isInterface() ? "(I)" : "");
    }
}
