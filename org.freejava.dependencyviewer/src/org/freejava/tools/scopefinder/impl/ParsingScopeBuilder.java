package org.freejava.tools.scopefinder.impl;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IRegion;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.freejava.dependency.model.FileParsingScope;

public class ParsingScopeBuilder {
	public static final int LEVEL_CLASS = 1;
	public static final int LEVEL_PACKAGE = 2;
	public static final int LEVEL_PACKAGE_ROOT = 3;
	private int level;

	private Set<IJavaProject> projects = new HashSet<IJavaProject>();
	private Set<IPackageFragmentRoot> roots = new HashSet<IPackageFragmentRoot>();
	private Set<IPackageFragment> packages = new HashSet<IPackageFragment>();
	private Set<ICompilationUnit> compilelationUnits = new HashSet<ICompilationUnit>();
	private Set<IClassFile> classFiles = new HashSet<IClassFile>();

	public ParsingScopeBuilder setLevel(int level) {
		this.level = level;
		return this;
	}

	private ParsingScopeBuilder addElement(IJavaElement element) {
		if (element.getElementType() == IJavaElement.JAVA_PROJECT) {
			projects.add((IJavaProject) element);
		}
		if (element.getElementType() == IJavaElement.PACKAGE_FRAGMENT_ROOT) {
			roots.add((IPackageFragmentRoot) element);
		}
		if (element.getElementType() == IJavaElement.PACKAGE_FRAGMENT) {
			packages.add((IPackageFragment) element);
		}
		if (element.getElementType() == IJavaElement.COMPILATION_UNIT) {
			compilelationUnits.add((ICompilationUnit) element);
		}
		if (element.getElementType() == IJavaElement.CLASS_FILE) {
			classFiles.add((IClassFile) element);
		}
		return this;
	}
	public void addSelectedJavaElements(IStructuredSelection structuredSelection) {
        for (Iterator<?> iterator = structuredSelection.iterator(); iterator.hasNext();) {
             Object aSelection = iterator.next();
             if (aSelection instanceof IJavaElement) {
            	 addElement((IJavaElement) aSelection);
             }
        }
	}
	public FileParsingScope build() throws JavaModelException {

		Set<File> result = new HashSet<File>();

		Set<IPackageFragmentRoot> resultRoots = new HashSet<IPackageFragmentRoot>(projects2FragmentRoots(this.projects));
		resultRoots.addAll(this.roots);

		if (level == LEVEL_PACKAGE_ROOT || level == LEVEL_PACKAGE) {
			for (IClassFile cf : classFiles) {
				packages.add((IPackageFragment) cf.getParent());
			}
			classFiles.clear();
			for (ICompilationUnit cu : compilelationUnits) {
				packages.add((IPackageFragment) cu.getParent());
			}
			compilelationUnits.clear();
		}
		if (level == LEVEL_PACKAGE_ROOT) {
			for (IPackageFragment pkg : packages) {
				roots.add((IPackageFragmentRoot) pkg.getParent());
			}
			packages.clear();
		}

		// exclude jre runtime (rt.jar)
		excludeJRERoots(resultRoots, detectJREPackageFragementRoots(this.projects));

		if (level == LEVEL_PACKAGE_ROOT) {
			result.addAll(roots2Files(resultRoots));
		}

		if (level == LEVEL_PACKAGE || level == LEVEL_CLASS) {

			Set<IPackageFragment> resultPackages = new HashSet<IPackageFragment>(roots2Packages(resultRoots));
			resultPackages.addAll(this.packages);

			if (level == LEVEL_PACKAGE) {
				result.addAll(pkgs2Files(resultPackages));

			} else {
                result.addAll(pkgs2Files(resultPackages));
                result.addAll(classFiles2Files(classFiles));
                result.addAll(compilationUnits2Files(compilelationUnits));
			}
		}

		excludeJREFiles(result);

		FileParsingScope result2 = new FileParsingScope();
		result2.addFiles(result);
		return result2;
	}

	private void excludeJREFiles(Set<File> files) {
		for (Iterator<File> it = files.iterator(); it.hasNext(); ) {
			if (it.next().getName().equals("rt.jar")) {
				it.remove();
			}
		}
	}

	private void excludeJRERoots(Set<IPackageFragmentRoot> resultRoots, Set<IPackageFragmentRoot> jrePackageRoots) throws JavaModelException {

		resultRoots.removeAll(jrePackageRoots);

		for (Iterator<IPackageFragmentRoot> pkgRootIt = resultRoots.iterator(); pkgRootIt.hasNext();) {

			IPackageFragmentRoot pkgRoot = pkgRootIt.next();

			if (pkgRoot.getKind() == IPackageFragmentRoot.K_BINARY) {
			    File file;
			    if (!pkgRoot.isExternal()) {
			        file = pkgRoot.getResource().getLocation().toFile();
			    } else {
			        file = pkgRoot.getPath().toFile();
			    }
			    if (file.getAbsolutePath().contains("/jre/") || file.getAbsolutePath().contains("\\jre\\")) {
			    	pkgRootIt.remove();
			    }
			}
		}
	}



	private Collection<File> compilationUnits2Files(Set<ICompilationUnit> compilelationUnits2) {
		Set<File> files = new HashSet<File>();

        IRegion region = JavaCore.newRegion();
        for (ICompilationUnit cu : compilelationUnits2) {
        	region.add(cu);
        }
        IResource[] resources = JavaCore.getGeneratedResources(region, false);
        for(IResource resource : resources){
        	files.add(resource.getLocation().toFile());
        }

		return files;
	}

	private Set<File> classFiles2Files(Set<IClassFile> classFiles2) {
		Set<File> files = new HashSet<File>();

        for (IClassFile clf : classFiles2) {
            IPackageFragment pkg = (IPackageFragment) clf.getParent();
            IPackageFragmentRoot pkgRoot = ((IPackageFragmentRoot)pkg.getParent());
            File file;
            if (!pkgRoot.isExternal()) {
                file = pkgRoot.getResource().getLocation().toFile();
            } else {
                file = pkgRoot.getPath().toFile();
            }
            files.add(file);
        }
        return files;
	}

	private Set<IPackageFragment> roots2Packages(Set<IPackageFragmentRoot> fragmentRoots) throws JavaModelException {
		Set<IPackageFragment> result = new HashSet<IPackageFragment>();
		for (IPackageFragmentRoot fragmentRoot : fragmentRoots) {
			for (IJavaElement pkg : fragmentRoot.getChildren()) {
				result.add((IPackageFragment) pkg);
			}
		}
 		return result;
	}

	private Set<IPackageFragmentRoot> projects2FragmentRoots(Set<IJavaProject> projects) throws JavaModelException {
		Set<IPackageFragmentRoot> result = new HashSet<IPackageFragmentRoot>();
		for (IJavaProject project : projects) {
			result.addAll(Arrays.asList(project.getPackageFragmentRoots()));
		}
		return result;
	}

	private Set<File> roots2Files(Set<IPackageFragmentRoot> roots) throws JavaModelException {
		Set<File> files = new HashSet<File>();
		for (IPackageFragmentRoot pkgRoot : roots) {
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
			    region.add(pkgRoot);
			    IResource[] resources = JavaCore.getGeneratedResources(region, false);
			    for(IResource resource : resources){
			    	files.add(resource.getLocation().toFile());
			    }
			}
		}
		return files;

	}
	private Set<File> pkgs2Files(Set<IPackageFragment> packages) throws JavaModelException {
		Set<File> files = new HashSet<File>();
		for (IPackageFragment pkg : packages) {
            IPackageFragmentRoot pkgRoot = ((IPackageFragmentRoot)pkg.getParent());
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
			    region.add(pkg);
			    IResource[] resources = JavaCore.getGeneratedResources(region, false);
			    for(IResource resource : resources){
			    	files.add(resource.getLocation().toFile());
			    }
			}
		}
		return files;
	}
    public static Set<IPackageFragmentRoot> detectJREPackageFragementRoots(Collection<IJavaProject> javaProjects) {
        // Please note that this is merely a heuristic to detect if a Jar is part of the JRE or not:
        // All Jars in the JRE_Container which are not located in the ext folder are considered part of the JRE.
        Set<IPackageFragmentRoot> jreRoots = new HashSet<IPackageFragmentRoot>();
        try {
            for (IJavaProject javaProject : javaProjects) {
	            for (IClasspathEntry entry : javaProject.getRawClasspath()) {
	                if (entry.getEntryKind() == IClasspathEntry.CPE_CONTAINER) {
	                    if (entry.getPath().toString().contains("org.eclipse.jdt.launching.JRE_CONTAINER")) { //$NON-NLS-1$
	                        for (IPackageFragmentRoot packageFragmentRoot : javaProject.findPackageFragmentRoots(entry)) {
	                            if (!packageFragmentRoot.getPath().toFile().getParentFile().getName().equals("ext")) { //$NON-NLS-1$
	                                jreRoots.add(packageFragmentRoot);
	                            }
	                        }
	                    }
	                }
	            }
            }
        } catch (JavaModelException e) {

        }
        return jreRoots;
    }
}