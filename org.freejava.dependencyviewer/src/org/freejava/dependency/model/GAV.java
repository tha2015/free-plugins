package org.freejava.dependency.model;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class GAV {
    private String g;
    private String a;
    private String v;


    public GAV(String g, String a, String v) {
		super();
		this.g = g;
		this.a = a;
		this.v = v;
	}
	public String getG() {
        return g;
    }
    public void setG(String g) {
        this.g = g;
    }
    public String getA() {
        return a;
    }
    public void setA(String a) {
        this.a = a;
    }
    public String getV() {
        return v;
    }
    public void setV(String v) {
        this.v = v;
    }

    @Override
    public String toString() {
    	return g + ":" + a + ":" + v;
    }

    @Override
    public boolean equals(Object obj) {
        return EqualsBuilder.reflectionEquals(this, obj);
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

	public static Set<GAV> fromString(String list) {
		Set<GAV> result = new HashSet<GAV>();
		for (String gavStr : list.split(",")) {
			if (gavStr == null || gavStr.trim().equals("")) continue;
			String[] gavAr = gavStr.split(":");
			GAV gav = new GAV(gavAr[0], gavAr[1], gavAr[2]);
			result.add(gav);
		}
		return result;
	}
	public static String toString(Collection<GAV> list) {
		String result = "";
		for (GAV gav : list) {
			String str = gav.toString();
			if (result.equals("")) result = str;
			else result += "," + str;
		}
		return result;
	}


}
