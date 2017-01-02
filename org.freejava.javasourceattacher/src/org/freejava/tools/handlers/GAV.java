package org.freejava.tools.handlers;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

public class GAV {
    private String g;
    private String a;
    private String v;
    private String artifactLink;

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

    public String getArtifactLink() {
        return artifactLink;
    }
    public void setArtifactLink(String artifactLink) {
        this.artifactLink = artifactLink;
    }

    @Override
    public boolean equals(Object obj) {
        return EqualsBuilder.reflectionEquals(this, obj);
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

}
