package org.freejava.dependency.graphtransformer.impl;

import java.io.File;
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
	Map<File, Set<String>> roots2Classes;

	public Class2PackageRootGraphTransformerImpl(Map<File, Set<String>> roots2Classes) {
		this.roots2Classes = roots2Classes;
	}

	public Graph<Name> transform(Graph<Name> graph) {

        Set<Edge<Name>> edges = new HashSet<Edge<Name>>();

        Map<String, Vertex<Name>> cachedPackageVerteces = new HashMap<String, Vertex<Name>>();
        for (Edge<Name> edge : graph.getEdges()) {
            Vertex<Name> from = findPackageVertexFromClassVertex(cachedPackageVerteces, edge.getFrom());
            Vertex<Name> to = findPackageVertexFromClassVertex(cachedPackageVerteces, edge.getTo());
            Edge<Name> newEdge = new Edge<Name>(from, to);
            if (!edges.contains(newEdge)) {
                edges.add(newEdge);
            }
        }

        Graph<Name> result = new Graph<Name>(edges);

        return result;
    }

    private Vertex<Name> findPackageVertexFromClassVertex(Map<String, Vertex<Name>> cachedPackageVerteces, Vertex<Name> classVertex) {
    	Vertex<Name> packageVertex;
    	String packageName = getPackageName(classVertex);
        if (cachedPackageVerteces.containsKey(packageName)) {
        	packageVertex = cachedPackageVerteces.get(packageName);
        } else {
        	packageVertex = new Vertex<Name>(Name.newPackage(packageName).setFrom(classVertex.getNode().getFrom()));
        	if (packageVertex.getNode().getFrom() == null) {
        		packageVertex.getNode().setFrom(getJarFile(classVertex.getNode().getName()));
        	}
            cachedPackageVerteces.put(packageName, packageVertex);
        }
        return packageVertex;
    }

    private String getPackageName(Vertex<Name> classVertex) {
    	String name;
    	if (classVertex.getNode().getFrom() != null) {
    		name = file2Name(classVertex.getNode().getFrom());
    	} else {
    		name = getRootFromClass(classVertex.getNode().getName());

    	}
    	return name;
    }

	private String getRootFromClass(String clazz) {
		File jarFile = getJarFile(clazz);

		if (jarFile != null) return file2Name(jarFile);

		if (clazz.contains(".")) {
			clazz = clazz.substring(0, clazz.lastIndexOf('.'));
		} else {
			String s = "";
		}
		return clazz;
	}

	public static String file2Name(File file) {
		return file.getName().contains(".") ? file.getName() : file.getAbsolutePath();
	}

	private File getJarFile(String clazz) {
		for (File file : roots2Classes.keySet()) {
			if (roots2Classes.get(file).contains(clazz)) return file;
		}
		return null;
	}

}