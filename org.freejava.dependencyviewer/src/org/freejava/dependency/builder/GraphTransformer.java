package org.freejava.dependency.builder;

import org.freejava.dependency.graph.Graph;

public interface GraphTransformer<T> {

    Graph<T> transform(Graph<T> graph);

}