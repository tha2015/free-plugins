package org.freejava.dependency.graphtransformer.impl;

import java.util.HashSet;
import java.util.Set;

import org.freejava.dependency.graphtransformer.GraphTransformer;
import org.freejava.dependency.model.Edge;
import org.freejava.dependency.model.Graph;

public class RemoveSelfReferenceNodesGraphTransformerImpl<T> implements GraphTransformer<T> {
    public Graph<T> transform(Graph<T> graph) {


        Set<Edge<T>> edges = new HashSet<Edge<T>>();
        for (Edge<T> edge : graph.getEdges()) {
            if (!edge.getFrom().equals(edge.getTo())) {
                edges.add(edge);
            }
        }

        Graph<T> result = new Graph<T>(edges);

        return result;
    }


}