package org.freejava.dependency.graphtransformer;

import org.freejava.dependency.model.Graph;

public interface GraphTransformer<T> {

    Graph<T> transform(Graph<T> graph);

}