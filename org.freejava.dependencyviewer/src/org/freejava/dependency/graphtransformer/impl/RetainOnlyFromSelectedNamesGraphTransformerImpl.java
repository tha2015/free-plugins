package org.freejava.dependency.graphtransformer.impl;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.freejava.dependency.graphtransformer.GraphTransformer;
import org.freejava.dependency.model.Edge;
import org.freejava.dependency.model.Graph;
import org.freejava.dependency.model.Name;

public class RetainOnlyFromSelectedNamesGraphTransformerImpl implements GraphTransformer<Name> {
    private Collection<String> names;
    public RetainOnlyFromSelectedNamesGraphTransformerImpl(Collection<String> names) {
        this.names = names;
    }
    public Graph<Name> transform(Graph<Name> graph) {


        Set<Edge<Name>> edges = new HashSet<Edge<Name>>();
        for (Edge<Name> edge : graph.getEdges()) {
            if (names.contains(edge.getFrom().getNode().getName())) {
                edges.add(edge);
            }
        }

        Graph<Name> result = new Graph<Name>(edges);

        return result;
    }


}