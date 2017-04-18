package org.freejava.dependency.graphbuilder.impl;

import java.io.File;
import java.util.Map;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.freejava.dependency.classparser.impl.MavenClassParserImpl;
import org.freejava.dependency.graphbuilder.GraphBuilder;
import org.freejava.dependency.model.ClassInfo;
import org.freejava.dependency.model.FileParsingScope;
import org.freejava.dependency.model.Graph;
import org.freejava.dependency.model.Name;
import org.freejava.dependency.model.ViewCommand;
import org.freejava.tools.scopefinder.impl.ScopeFinderImpl;
import org.freejava.tools.scopeparser.impl.ScopeParserImpl;

public class GraphBuilderFacadeImpl implements GraphBuilder<Name> {
	ViewCommand command;
	IStructuredSelection selection;

	public GraphBuilderFacadeImpl(ViewCommand command, IStructuredSelection selection) {
		this.command = command;
		this.selection = selection;
	}

	public Graph<Name> build() throws Exception {
        return buildGraph(command, selection);
    }
    private Graph<Name> buildGraph(ViewCommand command, IStructuredSelection selection) throws Exception {


    	// map selection - files (to be parsed)
        FileParsingScope scope = new ScopeFinderImpl(command, selection).findScope();

        // map files - classinfos
        Map<ClassInfo, File> classInfos = new ScopeParserImpl(new MavenClassParserImpl()).parse(scope);

        // map classinfos - graph
        Graph<Name> graph = getGraphBuilderImpl(command, selection, scope, classInfos).build();

        return graph;
    }

	private GraphBuilder<Name> getGraphBuilderImpl(ViewCommand command, IStructuredSelection selection,
			FileParsingScope scope, Map<ClassInfo, File> classInfos) {
		GraphBuilder<Name> builder;
        if (command.isViewInboundClassDependency()) builder = new InboundClassGraphBuilderImpl(command, selection, classInfos);
        else if (command.isViewOutboundClassDependency()) builder = new OutboundClassGraphBuilderImpl(command, selection, classInfos);
        else if (command.isViewInboundPackageDependency()) builder = new InboundPackageGraphBuilderImpl(command, selection, classInfos);
        else if (command.isViewOutboundPackageDependency()) builder = new OutboundPackageGraphBuilderImpl(command, selection, classInfos);
        else builder = new OutboundModuleGraphBuilderImpl(command, selection, scope, classInfos);
		return builder;
	}


}
