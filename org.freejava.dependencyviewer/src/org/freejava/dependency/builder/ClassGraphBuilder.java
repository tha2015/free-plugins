package org.freejava.dependency.builder;

import java.io.File;
import java.util.Collection;

import org.freejava.dependency.graph.Graph;

public interface ClassGraphBuilder {

    Graph<Name> build(Collection<File> resources) throws Exception;

}