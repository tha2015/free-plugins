package org.freejava.dependency.model;

import org.eclipse.core.commands.Command;

public class ViewCommand {
	String commandId;

	public ViewCommand(Command command) {
		this.commandId = command.getId();
	}
	public boolean isViewInboundPackageDependency() { return commandId.equals("org.freejava.tools.commands.viewPackageDependencyCommand");}
	public boolean isViewOutboundPackageDependency() { return commandId.equals("org.freejava.tools.commands.viewOutboundPackageDependencyCommand");}

	public boolean isViewInboundClassDependency() { return commandId.equals("org.freejava.tools.commands.viewClassDependencyCommand");}
	public boolean isViewOutboundClassDependency() { return commandId.equals("org.freejava.tools.commands.viewOutboundClassDependencyCommand");}

	public boolean isViewOutboundModuleDependency() { return commandId.equals("org.freejava.tools.commands.viewOutboundModuleDependencyCommand");}

	public boolean isViewClasses() {
		return isViewInboundClassDependency() || isViewOutboundClassDependency();
	}

	public boolean isViewPackages() {
		return isViewInboundPackageDependency() || isViewOutboundPackageDependency();
	}


}
