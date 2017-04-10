package org.freejava.dependency.graphtransformer.impl;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.freejava.dependency.graphtransformer.GraphTransformer;
import org.freejava.dependency.model.Edge;
import org.freejava.dependency.model.Graph;
import org.freejava.dependency.model.Vertex;

public class RemoveNodesGraphTransformerImpl<T> implements GraphTransformer<T> {
    private Collection<Vertex<T>> deletedNodes;
    public RemoveNodesGraphTransformerImpl(Collection<Vertex<T>> deletedNodes) {
        this.deletedNodes = deletedNodes;
    }
    public Graph<T> transform(Graph<T> graph) {


        Set<Edge<T>> edges = new HashSet<Edge<T>>();
        for (Edge<T> edge : graph.getEdges()) {
            if (!deletedNodes.contains(edge.getFrom()) && !deletedNodes.contains(edge.getTo())) {
                edges.add(edge);
            }
        }

        Graph<T> result = new Graph<T>(edges);

        return result;
    }


}