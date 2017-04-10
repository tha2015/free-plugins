package org.freejava.dependency.graphbuilder.impl;

import java.io.File;
import java.util.Map;
import java.util.Set;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.freejava.dependency.graphtransformer.impl.RemoveJRENodesGraphTransformerImpl;
import org.freejava.dependency.graphtransformer.impl.RemoveSelfReferenceNodesGraphTransformerImpl;
import org.freejava.dependency.graphtransformer.impl.RetainOnlySelectedNamesGraphTransformerImpl;
import org.freejava.dependency.model.ClassInfo;
import org.freejava.dependency.model.Graph;
import org.freejava.dependency.model.Name;
import org.freejava.dependency.model.ViewCommand;

public class InboundClassGraphBuilderImpl extends AbstractClassBuilderImpl {
	ViewCommand command;
	IStructuredSelection selection;
	Map<ClassInfo, File> classInfos;

	public InboundClassGraphBuilderImpl(ViewCommand command, IStructuredSelection selection,
			Map<ClassInfo, File> classInfos) {
		super();
		this.command = command;
		this.selection = selection;
		this.classInfos = classInfos;
	}

	@Override
	public Graph<Name> build() throws Exception {
		Graph<Name> graph;

        Graph<Name> classes = new ClassGraphBuilderImpl(classInfos).build();
        Set<String> names = findSelectedNames(command, selection);

        graph = new RetainOnlySelectedNamesGraphTransformerImpl(names).transform(classes);
        graph = new RemoveSelfReferenceNodesGraphTransformerImpl<Name>().transform(graph);
        graph = new RemoveJRENodesGraphTransformerImpl().transform(graph);

		return graph;
	}

}
