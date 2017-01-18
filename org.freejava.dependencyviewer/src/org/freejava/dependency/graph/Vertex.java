package org.freejava.dependency.graph;

public class Vertex<V>{
    private V node;

    public Vertex(V v) {
        this.node = v;
    }

    public V getNode() {
        return node;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Vertex && this.node.equals(((Vertex)obj).node);
    }

    @Override
    public int hashCode() {
        return node.hashCode();
    }

    @Override
    public String toString() {
        return String.valueOf(node);
    }
}
