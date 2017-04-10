package org.freejava.dependency.graphbuilder;

import org.freejava.dependency.model.Graph;

public interface GraphBuilder<T> {

    Graph<T> build() throws Exception;

}