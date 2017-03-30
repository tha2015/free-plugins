package org.freejava.dependency.builder.impl;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.freejava.dependency.builder.GraphTransformer;
import org.freejava.dependency.builder.Name;
import org.freejava.dependency.graph.Edge;
import org.freejava.dependency.graph.Graph;
import org.freejava.dependency.graph.Vertex;

public class MarkNonSelectedNamesGraphTransformerImpl implements GraphTransformer<Name> {
    private Collection<String> names;
    public MarkNonSelectedNamesGraphTransformerImpl(Collection<String> names) {
        this.names = names;
    }
    public Graph<Name> transform(Graph<Name> graph) {


        Set<Edge<Name>> edges = new HashSet<Edge<Name>>();

        for (Edge<Name> edge : graph.getEdges()) {
        	Name from = Name.newName(edge.getFrom().getNode());

            if (!names.contains(from.getName())) {
            	from.setFoundViaDependency(true);
            }

        	Name to = Name.newName(edge.getTo().getNode());

            if (!names.contains(to.getName())) {
            	to.setFoundViaDependency(true);
            }

            edges.add(new Edge<Name>(new Vertex<Name>(from), new Vertex<Name>(to)));
        }

        Graph<Name> result = new Graph<Name>(edges);

        return result;
    }


}