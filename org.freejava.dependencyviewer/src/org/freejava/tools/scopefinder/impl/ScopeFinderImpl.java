package org.freejava.tools.scopefinder.impl;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.freejava.dependency.model.FileParsingScope;
import org.freejava.dependency.model.ViewCommand;
import org.freejava.tools.scopefinder.ScopeFinder;

public class ScopeFinderImpl implements ScopeFinder {
	private ViewCommand command;
	private IStructuredSelection structuredSelection;

	public ScopeFinderImpl(ViewCommand command, IStructuredSelection structuredSelection) {
		this.command = command;
		this.structuredSelection = structuredSelection;
	}

	public FileParsingScope findScope() throws Exception {
		ParsingScopeBuilder col = new ParsingScopeBuilder();
		col.addSelectedJavaElements(structuredSelection);
		if (command.isViewClasses()) {
			col.setLevel(ParsingScopeBuilder.LEVEL_CLASS);
		} else if (command.isViewPackages()) {
			col.setLevel(ParsingScopeBuilder.LEVEL_PACKAGE);
		} else {
			col.setLevel(ParsingScopeBuilder.LEVEL_PACKAGE_ROOT);
		}

		FileParsingScope scope = col.build();
		return scope;
	}

}
