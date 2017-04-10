package org.freejava.dependency.model;

import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.lang3.builder.HashCodeBuilder;

public class Graph<V> {
    private final Set<Edge<V>> edges;

    public Graph(Set<Edge<V>> edges) {
        this.edges = new LinkedHashSet<Edge<V>>(edges);
    }

    public Set<Edge<V>> getEdges() {
        return new LinkedHashSet<Edge<V>>(this.edges);
    }

    @SuppressWarnings("rawtypes")
    @Override
    public boolean equals(Object obj) {
        return obj instanceof Graph && this.edges.equals(((Graph)obj).edges);
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(27, 41).append(edges).build();
    }

    @Override
    public String toString() {
        return edges.toString();
    }
}
