package org.freejava.dependency.model;

import java.io.File;

import org.apache.commons.lang3.builder.HashCodeBuilder;

public class Name {
	public static final int CLASS_VAL = 0;
	public static final int INTERFACE_VAL = 1;
	public static final int PACKAGE_VAL = 2;
	public static final int ANNOTATION_VAL = 3;
	public static final int ENUM_VAL = 4;

    private String name;
    private int type;
    private String color;
    private File from;


    public String getName() {
        return name;
    }
    public boolean isClass() { return type == CLASS_VAL; }
    public boolean isInterface() { return type == INTERFACE_VAL; }
    public boolean isPackage() { return type == PACKAGE_VAL; }
    public boolean isAnnotation() { return type == ANNOTATION_VAL; }
    public boolean isEnum() { return type == ENUM_VAL; }

    private static Name newName(String name, int type, String color, File from) {
        Name result = new Name();
        result.name = name;
        result.type = type;
        result.color = color;
        result.from = from;
        return result;
    }

    public void setColor(String color) {
		this.color = color;
	}
    public Name setFrom(File from) {
		this.from = from;
		return this;
	}

    public String getColor() {
		return color;
	}

    public File getFrom() {
		return from;
	}

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Name && this.name.equals(((Name)obj).name) && (this.type == ((Name)obj).type);
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 31).append(name).append(type).build();
    }

    public static Name newClass(String name, File from) {
        return newName(name, CLASS_VAL, null, from);
    }
    public static Name newInterface(String name, File from) {
        return newName(name, INTERFACE_VAL, null, from);
    }
    public static Name newAnnotation(String name, File from) {
        return newName(name, ANNOTATION_VAL, null, from);
    }
    public static Name newEnum(String name, File from) {
        return newName(name, ENUM_VAL, null, from);
    }
    public static Name newPackage(String name) {
        return newName(name, PACKAGE_VAL, null, null);
    }
    public static Name newName(Name name) {
        return newName(name.name, name.type, name.color, name.from);

    }

    @Override
    public String toString() {
    	String typeStr = "";
    	switch (type) {
    	case CLASS_VAL:
    		typeStr = "";
    		break;
    	case PACKAGE_VAL:
    		typeStr = "";
    		break;
    	case INTERFACE_VAL:
    		typeStr = "(I)";
    		break;
    	case ANNOTATION_VAL:
    		typeStr = "(@)";
    		break;
    	case ENUM_VAL:
    		typeStr = "(E)";
    		break;
    	}
        return name + typeStr;
    }
}
