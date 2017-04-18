package org.freejava.tools.scopefinder.impl;

import java.io.File;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaModelException;

public class JREDetector {
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

	public static void excludeJRERoots(Set<IPackageFragmentRoot> resultRoots, Set<IPackageFragmentRoot> jrePackageRoots) throws JavaModelException {

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
	public static boolean isJREFile(File file) {
		return file.getName().equals("rt.jar");
	}


	public static boolean isJREPackage(String name) {
		return name.startsWith("java.") || name.startsWith("javax.xml.")|| name.startsWith("javax.swing.")|| name.startsWith("javax.security.");
	}
}
