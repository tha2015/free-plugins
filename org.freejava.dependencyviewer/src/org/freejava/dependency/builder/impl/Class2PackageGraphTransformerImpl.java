package org.freejava.dependency.builder.impl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.freejava.dependency.builder.GraphTransformer;
import org.freejava.dependency.builder.Name;
import org.freejava.dependency.graph.Edge;
import org.freejava.dependency.graph.Graph;
import org.freejava.dependency.graph.Vertex;

public class Class2PackageGraphTransformerImpl implements GraphTransformer<Name> {

    public Graph<Name> transform(Graph<Name> graph) {

        Set<Edge<Name>> edges = new HashSet<Edge<Name>>();

        Map<String, Vertex<Name>> packageToVertex = new HashMap<String, Vertex<Name>>();
        for (Edge<Name> edge : graph.getEdges()) {
            Vertex<Name> from = findVertex(packageToVertex, getPackageName(edge.getFrom()));
            Vertex<Name> to = findVertex(packageToVertex, getPackageName(edge.getTo()));
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

    private String getPackageName(Vertex<Name> v) {
        String packageName;
        if (v.getNode().isPackage()) {
            packageName = v.getNode().getName();
        } else {
            String className = v.getNode().getName();
            if (className.indexOf('.') != -1) {
                packageName = className.substring(0, className.lastIndexOf('.'));
            } else {
                packageName = "";
            }
        }
        return packageName;
    }


}