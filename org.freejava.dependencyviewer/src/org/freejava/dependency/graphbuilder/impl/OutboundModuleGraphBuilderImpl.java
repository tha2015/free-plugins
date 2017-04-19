package org.freejava.dependency.graphbuilder.impl;

import java.io.File;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.freejava.dependency.graphtransformer.impl.Class2PackageRootGraphTransformerImpl;
import org.freejava.dependency.graphtransformer.impl.MarkNonSelectedNamesGraphTransformerImpl;
import org.freejava.dependency.graphtransformer.impl.RemoveJRENodesGraphTransformerImpl;
import org.freejava.dependency.graphtransformer.impl.RemoveSelfReferenceNodesGraphTransformerImpl;
import org.freejava.dependency.graphtransformer.impl.RetainOnlyFromSelectedNamesGraphTransformerImpl;
import org.freejava.dependency.model.ClassInfo;
import org.freejava.dependency.model.FileParsingScope;
import org.freejava.dependency.model.Graph;
import org.freejava.dependency.model.Name;
import org.freejava.dependency.model.ViewCommand;

public class OutboundModuleGraphBuilderImpl extends AbstractClassBuilderImpl {
	ViewCommand command;
	IStructuredSelection selection;
	FileParsingScope scope;
	Map<ClassInfo, File> classInfos;

	public OutboundModuleGraphBuilderImpl(ViewCommand command, IStructuredSelection selection,
			FileParsingScope scope, Map<ClassInfo, File> classInfos) {
		super();
		this.command = command;
		this.selection = selection;
		this.scope = scope;
		this.classInfos = classInfos;
	}
	@Override
	public Graph<Name> build() throws Exception {

		Graph<Name> graph;

        Graph<Name> classes = new ClassGraphBuilderImpl(classInfos).build();

        Set<String> names = findSelectedRoots(scope.getRoot2FilesMap().keySet());

        graph = new MarkNonSelectedNamesGraphTransformerImpl(names, "orange").transform(new RetainOnlyFromSelectedNamesGraphTransformerImpl(names).transform(new Class2PackageRootGraphTransformerImpl(scope.getRoots2Classes()).transform(classes)));
        graph = new RemoveSelfReferenceNodesGraphTransformerImpl<Name>().transform(graph);
        graph = new RemoveJRENodesGraphTransformerImpl().transform(graph);

		return graph;

	}
	private Set<String> findSelectedRoots(Set<File> files) {
		Set<String> result = new HashSet<String>();
		for (File file : files) result.add(Class2PackageRootGraphTransformerImpl.file2Name(file));
		return result;
	}

}
