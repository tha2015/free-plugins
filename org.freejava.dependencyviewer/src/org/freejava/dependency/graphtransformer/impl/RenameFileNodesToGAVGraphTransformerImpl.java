package org.freejava.dependency.graphtransformer.impl;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.freejava.dependency.gavfinder.MavenCentralGAVFinderImpl;
import org.freejava.dependency.graphtransformer.GraphTransformer;
import org.freejava.dependency.model.Edge;
import org.freejava.dependency.model.GAV;
import org.freejava.dependency.model.Graph;
import org.freejava.dependency.model.Name;
import org.freejava.dependency.model.Vertex;

public class RenameFileNodesToGAVGraphTransformerImpl implements GraphTransformer<Name> {

	private MavenCentralGAVFinderImpl gavFinder = new MavenCentralGAVFinderImpl();


    public Graph<Name> transform(Graph<Name> graph) {



        Set<Edge<Name>> edges = new HashSet<Edge<Name>>();

        Map<String, Vertex<Name>> cachedPackageVerteces = new HashMap<String, Vertex<Name>>();

        for (Edge<Name> edge : graph.getEdges()) {
            Vertex<Name> from = transformFileNameToGAV(cachedPackageVerteces, edge.getFrom());
            Vertex<Name> to = transformFileNameToGAV(cachedPackageVerteces, edge.getTo());
            Edge<Name> newEdge = new Edge<Name>(from, to);
            if (!edges.contains(newEdge)) {
                edges.add(newEdge);
            }
        }

        Graph<Name> result = new Graph<Name>(edges);

        return result;
    }


	private Vertex<Name> transformFileNameToGAV(Map<String, Vertex<Name>> cachedPackageVerteces, Vertex<Name> vertex) {
    	Vertex<Name> packageVertex;
    	String packageName = vertex.getNode().getName();
        if (cachedPackageVerteces.containsKey(packageName)) {
        	packageVertex = cachedPackageVerteces.get(packageName);
        } else {
        	String gavsStr = getGAV(vertex);
        	packageVertex = new Vertex<Name>(Name.newPackage(packageName + gavsStr).setFrom(vertex.getNode().getFrom()));

            cachedPackageVerteces.put(packageName, packageVertex);
        }
        return packageVertex;
	}


	private String getGAV(Vertex<Name> vertex) {
		String gavsStr = "";
		File file = vertex.getNode().getFrom();
		if (file != null && file.getName().endsWith(".jar")) {
			try {
				Collection<GAV> gavs = gavFinder.find(file);
				if (!gavs.isEmpty()) {
					gavsStr = new HashSet<GAV>(gavs).toString();
				}
			} catch (Exception e) {

			}
		}
		return gavsStr;
	}


}