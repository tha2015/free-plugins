package org.freejava.tools.handlers;

import java.io.File;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IRegion;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;
import org.freejava.dependency.builder.Name;
import org.freejava.dependency.builder.impl.Class2PackageGraphTransformerImpl;
import org.freejava.dependency.builder.impl.ClassGraphBuilderImpl;
import org.freejava.dependency.builder.impl.RemoveSelfReferenceNodesGraphTransformerImpl;
import org.freejava.dependency.builder.impl.RetainOnlyFromSelectedNamesGraphTransformerImpl;
import org.freejava.dependency.builder.impl.RetainOnlySelectedNamesGraphTransformerImpl;
import org.freejava.dependency.graph.Graph;
import org.freejava.dependency.parser.maven.MavenClassParserImpl;
import org.freejava.tools.Activator;
import org.freejava.tools.handlers.dependency.DependencyView;

/**
 * Handler class for View Class/Package Dependency action.
 *
 * @see org.eclipse.core.commands.IHandler
 * @see org.eclipse.core.commands.AbstractHandler
 */
public class ViewDependencyHandler extends AbstractHandler {
    /**
     * The constructor.
     */
    public ViewDependencyHandler() {
    }

    /**
     * the command has been executed, so extract extract the needed information
     * from the application context.
     */
    public Object execute(ExecutionEvent event) throws ExecutionException {
        ISelection selection = HandlerUtil.getCurrentSelection(event);

        if (!(selection instanceof IStructuredSelection)) {
            return null;
        }
//   	 * <li><code>org.eclipse.jdt.core.ICompilationUnit</code></li>
//	 * <li><code>org.eclipse.jdt.core.IPackageFragment</code></li>
//	 * <li><code>org.eclipse.jdt.core.IPackageFragmentRoot</code></li>
//	 * <li><code>org.eclipse.jdt.core.IJavaProject</code></li>
        try {
            boolean isViewPackageDependency = event.getCommand().getId().equals("org.freejava.tools.commands.viewPackageDependencyCommand");
            boolean isViewOutboundPackageDependency = event.getCommand().getId().equals("org.freejava.tools.commands.viewOutboundPackageDependencyCommand");
            boolean isViewClassDependency = event.getCommand().getId().equals("org.freejava.tools.commands.viewClassDependencyCommand");

            boolean isViewPackages = isViewPackageDependency || isViewOutboundPackageDependency;

            Set<String> names = new HashSet<String>();
            Set<File> files = new HashSet<File>();
            findFilterNamesAndJarClassFiles(isViewPackages, (IStructuredSelection) selection, names, files);


            IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();

            // Send the result to the Dependency view and show that view to user
            IViewReference[] viewRefs =  page.getViewReferences();
            DependencyView  dependencyView = null;
            for (IViewReference viewRef : viewRefs) {
                if (viewRef.getId().equals(DependencyView.ID)) {
                    try {
                        dependencyView = (DependencyView) viewRef.getPage().showView(DependencyView.ID, null, IWorkbenchPage.VIEW_ACTIVATE);
                    } catch (PartInitException e) {
                        Activator.logError("Cannot show view " + DependencyView.ID, e);				}
                }
            }
            if (dependencyView == null) {
                try {
                    dependencyView = (DependencyView) page.showView(DependencyView.ID, null, IWorkbenchPage.VIEW_ACTIVATE);
                } catch (Exception e) {
                    Activator.logError("Cannot show view " + DependencyView.ID, e);
                }
            }

            final Graph<Name> graphNodes = buildGraph(isViewPackageDependency ? 1 : (isViewOutboundPackageDependency ? 2 : (isViewClassDependency ? 3 : 4)), names, files);
            dependencyView.setDependencyInfo(graphNodes);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private Graph<Name> buildGraph(int type , Set<String> names, Set<File> files) throws Exception {
        Graph<Name> graph;
        Graph<Name> classes = new ClassGraphBuilderImpl(new MavenClassParserImpl()).build(files);
        if (type == 1) { // inbound package dependencies
            graph = new RetainOnlySelectedNamesGraphTransformerImpl(names).transform(new Class2PackageGraphTransformerImpl().transform(classes));
        } else if (type == 2) { // outbound package dependencies
            graph = new RetainOnlyFromSelectedNamesGraphTransformerImpl(names).transform(new Class2PackageGraphTransformerImpl().transform(classes));
        } else if (type == 3) { // inbound class dependencies
            graph =  new RetainOnlySelectedNamesGraphTransformerImpl(names).transform(classes);
        } else { // outbound classes dependencies
            graph = new RetainOnlyFromSelectedNamesGraphTransformerImpl(names).transform(classes);
        }
        graph = new RemoveSelfReferenceNodesGraphTransformerImpl<Name>().transform(graph);
        return graph;
    }


    private void findFilterNamesAndJarClassFiles(boolean isViewPackageDependency,
            IStructuredSelection structuredSelection, Set<String> names/*, Set<String> interfaces*/,
            Set<File> files) throws JavaModelException {
        for (Iterator<?> iterator = structuredSelection.iterator(); iterator.hasNext();) {
            IJavaElement aSelection = (IJavaElement) iterator.next();
            if (isViewPackageDependency) {
                if (aSelection instanceof ICompilationUnit) {
                    // ignore
                } else if (aSelection instanceof IPackageFragment) {
                    names.add(aSelection.getElementName());
                    if ((((IPackageFragment) aSelection).getKind() == IPackageFragmentRoot.K_BINARY)) {
                        IPackageFragment pkg = (IPackageFragment) aSelection;
                        IPackageFragmentRoot pkgRoot = ((IPackageFragmentRoot)pkg.getParent());
                        File file;
                        if (!pkgRoot.isExternal()) {
                            file = pkgRoot.getResource().getLocation().toFile();
                        } else {
                            file = pkgRoot.getPath().toFile();
                        }
                        files.add(file);
                    } else {
                        IRegion region = JavaCore.newRegion();
                        region.add(aSelection);
                        IResource[] resources = JavaCore.getGeneratedResources(region, false);
                        for(IResource resource : resources){
                            files.add(resource.getLocation().toFile());
                        }
                    }
                } else if (aSelection instanceof IPackageFragmentRoot) {
                    IPackageFragmentRoot pkgRoot = (IPackageFragmentRoot) aSelection;
                    for (IJavaElement e : pkgRoot.getChildren()) {
                        names.add(e.getElementName());
                    }
                    if (pkgRoot.getKind() == IPackageFragmentRoot.K_BINARY) {
                        File file;
                        if (!pkgRoot.isExternal()) {
                            file = pkgRoot.getResource().getLocation().toFile();
                        } else {
                            file = pkgRoot.getPath().toFile();
                        }
                        files.add(file);
                    } else {
                        IRegion region = JavaCore.newRegion();
                        region.add(aSelection);
                        IResource[] resources = JavaCore.getGeneratedResources(region, false);
                        for(IResource resource : resources){
                            files.add(resource.getLocation().toFile());
                        }
                    }
                } else if (aSelection instanceof IJavaProject) {
                    IJavaProject p = (IJavaProject) aSelection;
                    for (IPackageFragment pkg : p.getPackageFragments()) {
                        names.add(pkg.getElementName());
                    }
                    for (IPackageFragmentRoot pkgRoot : p.getPackageFragmentRoots()) {
                        if (pkgRoot.getKind() == IPackageFragmentRoot.K_BINARY) {
                            File file;
                            if (!pkgRoot.isExternal()) {
                                file = pkgRoot.getResource().getLocation().toFile();
                            } else {
                                file = pkgRoot.getPath().toFile();
                            }
                            files.add(file);
                        } else {
                            IRegion region = JavaCore.newRegion();
                            region.add(aSelection);
                            IResource[] resources = JavaCore.getGeneratedResources(region, false);
                            for(IResource resource : resources){
                                files.add(resource.getLocation().toFile());
                            }
                        }
                    }
                }
            } else {
                if (aSelection instanceof IClassFile) {
                    IClassFile clf = (IClassFile) aSelection;
                    names.add(clf.getType().getFullyQualifiedName());
                    //if (clf.getType().isInterface()) interfaces.add(clf.getType().getFullyQualifiedName());
                    IPackageFragment pkg = (IPackageFragment) clf.getParent();
                    IPackageFragmentRoot pkgRoot = ((IPackageFragmentRoot)pkg.getParent());
                    File file;
                    if (!pkgRoot.isExternal()) {
                        file = pkgRoot.getResource().getLocation().toFile();
                    } else {
                        file = pkgRoot.getPath().toFile();
                    }
                    files.add(file);
                } else if (aSelection instanceof ICompilationUnit) {
                    ICompilationUnit unit = (ICompilationUnit) aSelection;
                    for(IType type : unit.getTypes()){
                        names.add(type.getFullyQualifiedName());
                        //if (type.isInterface()) interfaces.add(type.getFullyQualifiedName());
                    }
                    IRegion region = JavaCore.newRegion();
                    region.add(aSelection);
                    IResource[] resources = JavaCore.getGeneratedResources(region, false);
                    for(IResource resource : resources){
                        files.add(resource.getLocation().toFile());
                    }
                } else if (aSelection instanceof IPackageFragment) {
                    IPackageFragment pkg = (IPackageFragment) aSelection;
                    for (IJavaElement e : pkg.getChildren()) {
                        if (e instanceof ICompilationUnit) {
                            ICompilationUnit unit = (ICompilationUnit) e;
                            for(IType type : unit.getTypes()){
                                names.add(type.getFullyQualifiedName());
                                //if (type.isInterface()) interfaces.add(type.getFullyQualifiedName());
                            }
                        }
                        if (e instanceof IClassFile) {
                            IClassFile clf = (IClassFile) e;
                            names.add(clf.getType().getFullyQualifiedName());
                            //if (clf.getType().isInterface()) interfaces.add(clf.getType().getFullyQualifiedName());
                        }
                    }
                    if ((((IPackageFragment)aSelection).getKind() == IPackageFragmentRoot.K_BINARY)) {
                        IPackageFragmentRoot pkgRoot = ((IPackageFragmentRoot)pkg.getParent());
                        File file;
                        if (!pkgRoot.isExternal()) {
                            file = pkgRoot.getResource().getLocation().toFile();
                        } else {
                            file = pkgRoot.getPath().toFile();
                        }
                        files.add(file);
                    } else {
                        IRegion region = JavaCore.newRegion();
                        region.add(aSelection);
                        IResource[] resources = JavaCore.getGeneratedResources(region, false);
                        for(IResource resource : resources){
                            files.add(resource.getLocation().toFile());
                        }
                    }
                } else if (aSelection instanceof IPackageFragmentRoot) {
                    IPackageFragmentRoot pkgRoot = (IPackageFragmentRoot) aSelection;
                    for (IJavaElement e : pkgRoot.getChildren()) {
                        IPackageFragment pkg = (IPackageFragment) e;
                        for (IJavaElement e2 : pkg.getChildren()) {
                            if (e2 instanceof ICompilationUnit) {
                                ICompilationUnit unit = (ICompilationUnit) e2;
                                for(IType type : unit.getTypes()){
                                    names.add(type.getFullyQualifiedName());
                                    //if (type.isInterface()) interfaces.add(type.getFullyQualifiedName());
                                }
                            }
                            if (e2 instanceof IClassFile) {
                                IClassFile clf = (IClassFile) e2;
                                names.add(clf.getType().getFullyQualifiedName());
                                //if (clf.getType().isInterface()) interfaces.add(clf.getType().getFullyQualifiedName());
                            }
                        }
                    }
                    if (pkgRoot.getKind() == IPackageFragmentRoot.K_BINARY) {
                        File file;
                        if (!pkgRoot.isExternal()) {
                            file = pkgRoot.getResource().getLocation().toFile();
                        } else {
                            file = pkgRoot.getPath().toFile();
                        }
                        files.add(file);
                    } else {
                        IRegion region = JavaCore.newRegion();
                        region.add(aSelection);
                        IResource[] resources = JavaCore.getGeneratedResources(region, false);
                        for(IResource resource : resources){
                            files.add(resource.getLocation().toFile());
                        }
                    }
                } else if (aSelection instanceof IJavaProject) {
                    IJavaProject p = (IJavaProject) aSelection;
                    for (IPackageFragment pkg : p.getPackageFragments()) {
                        for (IJavaElement e2 : pkg.getChildren()) {
                            if (e2 instanceof ICompilationUnit) {
                                ICompilationUnit unit = (ICompilationUnit) e2;
                                for(IType type : unit.getTypes()){
                                    names.add(type.getFullyQualifiedName());
                                    //if (type.isInterface()) interfaces.add(type.getFullyQualifiedName());
                                }
                            }
                            if (e2 instanceof IClassFile) {
                                IClassFile clf = (IClassFile) e2;
                                names.add(clf.getType().getFullyQualifiedName());
                                //if (clf.getType().isInterface()) interfaces.add(clf.getType().getFullyQualifiedName());
                            }
                        }
                    }
                    for (IPackageFragmentRoot pkgRoot : p.getPackageFragmentRoots()) {
                        if (pkgRoot.getKind() == IPackageFragmentRoot.K_BINARY) {
                            File file;
                            if (!pkgRoot.isExternal()) {
                                file = pkgRoot.getResource().getLocation().toFile();
                            } else {
                                file = pkgRoot.getPath().toFile();
                            }
                            files.add(file);
                        } else {
                            IRegion region = JavaCore.newRegion();
                            region.add(aSelection);
                            IResource[] resources = JavaCore.getGeneratedResources(region, false);
                            for(IResource resource : resources){
                                files.add(resource.getLocation().toFile());
                            }
                        }
                    }
                }
            }
        }
    }

}
