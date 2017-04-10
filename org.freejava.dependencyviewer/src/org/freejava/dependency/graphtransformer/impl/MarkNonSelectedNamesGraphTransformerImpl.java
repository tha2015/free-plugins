package org.freejava.dependency.graphtransformer.impl;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.freejava.dependency.graphtransformer.GraphTransformer;
import org.freejava.dependency.model.Edge;
import org.freejava.dependency.model.Graph;
import org.freejava.dependency.model.Name;
import org.freejava.dependency.model.Vertex;

public class MarkNonSelectedNamesGraphTransformerImpl implements GraphTransformer<Name> {
    private Collection<String> names;
    private String color;
    public MarkNonSelectedNamesGraphTransformerImpl(Collection<String> names, String color) {
        this.names = names;
        this.color = color;
    }
    public Graph<Name> transform(Graph<Name> graph) {


        Set<Edge<Name>> edges = new HashSet<Edge<Name>>();

        for (Edge<Name> edge : graph.getEdges()) {
        	Name from = Name.newName(edge.getFrom().getNode());

            if (!names.contains(from.getName())) {
            	from.setColor(color);
            }

        	Name to = Name.newName(edge.getTo().getNode());

            if (!names.contains(to.getName())) {
            	to.setColor(color);
            }

            edges.add(new Edge<Name>(new Vertex<Name>(from), new Vertex<Name>(to)));
        }

        Graph<Name> result = new Graph<Name>(edges);

        return result;
    }


}