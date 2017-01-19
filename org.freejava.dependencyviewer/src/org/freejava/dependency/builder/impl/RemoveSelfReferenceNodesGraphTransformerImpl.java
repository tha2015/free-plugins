package org.freejava.dependency.builder.impl;

import java.util.HashSet;
import java.util.Set;

import org.freejava.dependency.builder.GraphTransformer;
import org.freejava.dependency.graph.Edge;
import org.freejava.dependency.graph.Graph;

public class RemoveSelfReferenceNodesGraphTransformerImpl<T> implements GraphTransformer<T> {
    public Graph<T> transform(Graph<T> graph) {


        Set<Edge<T>> edges = new HashSet<Edge<T>>();
        for (Edge<T> edge : graph.getEdges()) {
            if (edge.getFrom() != edge.getTo()) {
                edges.add(edge);
            }
        }

        Graph<T> result = new Graph<T>(edges);

        return result;
    }


}