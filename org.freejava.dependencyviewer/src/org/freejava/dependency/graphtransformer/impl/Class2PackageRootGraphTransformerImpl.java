package org.freejava.dependency.graphtransformer.impl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.freejava.dependency.graphtransformer.GraphTransformer;
import org.freejava.dependency.model.Edge;
import org.freejava.dependency.model.Graph;
import org.freejava.dependency.model.Name;
import org.freejava.dependency.model.Vertex;

public class Class2PackageRootGraphTransformerImpl implements GraphTransformer<Name> {

	public Graph<Name> transform(Graph<Name> graph) {

        Set<Edge<Name>> edges = new HashSet<Edge<Name>>();

        Map<String, Vertex<Name>> packageToVertex = new HashMap<String, Vertex<Name>>();
        for (Edge<Name> edge : graph.getEdges()) {
            Vertex<Name> from = findVertex(packageToVertex, getRootName(edge.getFrom()));
            Vertex<Name> to = findVertex(packageToVertex, getRootName(edge.getTo()));
            Edge<Name> newEdge = new Edge<Name>(from, to);
            if (!edges.contains(newEdge)) {
                edges.add(newEdge);
            }
        }

        Graph<Name> result = new Graph<Name>(edges);

        return result;
    }

    private Vertex<Name> findVertex(Map<String, Vertex<Name>> packageToVertex, String packageName) {
        Vertex<Name> vertex;
        if (packageToVertex.containsKey(packageName)) {
            vertex = packageToVertex.get(packageName);
        } else {
            vertex = new Vertex<Name>(Name.newPackage(packageName));
            packageToVertex.put(packageName, vertex);
        }
        return vertex;
    }

    private String getRootName(Vertex<Name> v) {
    	String name;
    	if (v.getNode().getFrom() != null) {
    		name = v.getNode().getFrom().getAbsolutePath();
    	} else {
    		name = v.getNode().getName();
    		if (name.contains(".")) {
    			name = name.substring(0, name.lastIndexOf('.'));
    		}
    	}
    	return name;
    }


}