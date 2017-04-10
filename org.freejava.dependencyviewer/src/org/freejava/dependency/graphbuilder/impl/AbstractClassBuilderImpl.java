package org.freejava.dependency.graphbuilder.impl;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.freejava.dependency.graphbuilder.GraphBuilder;
import org.freejava.dependency.model.Graph;
import org.freejava.dependency.model.Name;
import org.freejava.dependency.model.ViewCommand;

public abstract class AbstractClassBuilderImpl implements GraphBuilder<Name>{

	public abstract Graph<Name> build() throws Exception;

    protected Set<String> findSelectedNames(ViewCommand command,
            IStructuredSelection structuredSelection) throws JavaModelException {
        boolean isViewPackages = command.isViewInboundPackageDependency() || command.isViewOutboundPackageDependency();

        Set<String> names = new HashSet<String>();

        for (Iterator<?> iterator = structuredSelection.iterator(); iterator.hasNext();) {
            IJavaElement aSelection = (IJavaElement) iterator.next();
            if (isViewPackages) {
                if (aSelection instanceof ICompilationUnit) {
                    // ignore
                } else if (aSelection instanceof IPackageFragment) {
                    names.add(aSelection.getElementName());

                } else if (aSelection instanceof IPackageFragmentRoot) {
                    IPackageFragmentRoot pkgRoot = (IPackageFragmentRoot) aSelection;
                    for (IJavaElement e : pkgRoot.getChildren()) {
                        names.add(e.getElementName());
                    }
                } else if (aSelection instanceof IJavaProject) {
                    IJavaProject p = (IJavaProject) aSelection;
                    for (IPackageFragment pkg : p.getPackageFragments()) {
                        names.add(pkg.getElementName());
                    }
                }
            } else {
                if (aSelection instanceof IClassFile) {
                    IClassFile clf = (IClassFile) aSelection;
                    names.add(clf.getType().getFullyQualifiedName());
                } else if (aSelection instanceof ICompilationUnit) {
                    ICompilationUnit unit = (ICompilationUnit) aSelection;
                    for(IType type : unit.getTypes()){
                        names.add(type.getFullyQualifiedName());
                    }
                } else if (aSelection instanceof IPackageFragment) {
                    IPackageFragment pkg = (IPackageFragment) aSelection;
                    collectClassNames(names, pkg);
                } else if (aSelection instanceof IPackageFragmentRoot) {
                    IPackageFragmentRoot pkgRoot = (IPackageFragmentRoot) aSelection;
                    for (IJavaElement e : pkgRoot.getChildren()) {
                        IPackageFragment pkg = (IPackageFragment) e;
                        collectClassNames(names, pkg);
                    }
                } else if (aSelection instanceof IJavaProject) {
                    IJavaProject p = (IJavaProject) aSelection;
                    for (IPackageFragment pkg : p.getPackageFragments()) {
                        collectClassNames(names, pkg);
                    }
                }
            }
        }
        return names;
    }



    protected void collectClassNames(Set<String> names, IPackageFragment pkg) throws JavaModelException {
		for (IJavaElement e2 : pkg.getChildren()) {
		    if (e2 instanceof ICompilationUnit) {
		        ICompilationUnit unit = (ICompilationUnit) e2;
		        for(IType type : unit.getTypes()){
		            names.add(type.getFullyQualifiedName());
		        }
		    }
		    if (e2 instanceof IClassFile) {
		        IClassFile clf = (IClassFile) e2;
		        names.add(clf.getType().getFullyQualifiedName());
		    }
		}
	}
}
