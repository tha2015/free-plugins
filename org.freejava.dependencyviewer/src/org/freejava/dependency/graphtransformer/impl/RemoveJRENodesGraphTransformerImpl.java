package org.freejava.dependency.graphtransformer.impl;

import java.util.HashSet;
import java.util.Set;

import org.freejava.dependency.graphtransformer.GraphTransformer;
import org.freejava.dependency.model.Edge;
import org.freejava.dependency.model.Graph;
import org.freejava.dependency.model.Name;

public class RemoveJRENodesGraphTransformerImpl implements GraphTransformer<Name> {
    public Graph<Name> transform(Graph<Name> graph) {


        Set<Edge<Name>> edges = new HashSet<Edge<Name>>();
        for (Edge<Name> edge : graph.getEdges()) {
        	String from = edge.getFrom().getNode().getName();
        	String to = edge.getTo().getNode().getName();

            if (!isJREPackage(from) && !isJREPackage(to)) {
                edges.add(edge);
            }

        }

        Graph<Name> result = new Graph<Name>(edges);

        return result;
    }

	private boolean isJREPackage(String name) {
		return name.startsWith("java.") || name.startsWith("javax.xml.")|| name.startsWith("javax.swing.")|| name.startsWith("javax.security.");
	}


}