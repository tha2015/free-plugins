package org.freejava.dependency.model;

import org.apache.commons.lang3.builder.HashCodeBuilder;

public class Edge<V> {
    private Vertex<V> from;
    private Vertex<V> to;

    public Edge(Vertex<V> from, Vertex<V> to) {
        this.from = from;
        this.to = to;
    }

    public Vertex<V> getFrom() {
        return this.from;
    }
    public Vertex<V> getTo() {
        return this.to;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Edge && this.from.equals(((Edge)obj).from) && this.to.equals(((Edge)obj).to);
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(37, 51).append(from).append(to).build();
    }

    @Override
    public String toString() {
        return String.valueOf(from) + " -> " + String.valueOf(to);
    }

}
