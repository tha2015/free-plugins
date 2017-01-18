package org.freejava.dependency.builder.impl;

import java.util.HashSet;
import java.util.Set;

import org.freejava.dependency.builder.GraphTransformer;
import org.freejava.dependency.graph.Edge;
import org.freejava.dependency.graph.Graph;
import org.freejava.dependency.graph.Vertex;

public class RetainOnlyFromNodesGraphTransformerImpl<T> implements GraphTransformer<T> {

    public Graph<T> transform(Graph<T> graph) {

        Set<Vertex<T>> fromNodes = new HashSet<Vertex<T>>();
        for (Edge<T> edge : graph.getEdges()) {
            fromNodes.add(edge.getFrom());
        }

        Set<Edge<T>> edges = new HashSet<Edge<T>>();
        for (Edge<T> edge : graph.getEdges()) {
            if (fromNodes.contains(edge.getFrom()) && fromNodes.contains(edge.getTo())) {
                edges.add(edge);
            }
        }

        Graph<T> result = new Graph<T>(edges);

        return result;
    }


}