package org.freejava.tools.handlers.classpathutil;

import java.io.File;

import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.swt.widgets.Shell;

public class MySourceAttacher implements SourceAttacher {

    public boolean attachSource(final Shell shell, IPackageFragmentRoot root, String sourcePath) throws Exception {

        IJavaProject javaProject = root.getJavaProject();
        IClasspathEntry[] entries = (IClasspathEntry[]) javaProject.getRawClasspath().clone();
        boolean attached = false;
        for (int i = 0; i < entries.length; i++) {
            IClasspathEntry entry = entries[i];
            String entryPath;
            if (entry.getEntryKind() == IClasspathEntry.CPE_VARIABLE) {
                entryPath = JavaCore.getResolvedVariablePath(entry.getPath()).toOSString();
            } else {
                entryPath = entry.getPath().toOSString();
            }
            String rootPath = root.getPath().toOSString();
            if (entryPath.equals(rootPath)) {
                entries[i] = addSourceAttachment(root, entries[i], sourcePath, null);
                attached = true;
                break;
            }
        }
        if (!attached) {
            root.attachSource(new Path(sourcePath), null, null);
        }
        javaProject.setRawClasspath(entries, null);
        return true;
    }

    private static IClasspathEntry addSourceAttachment(
            IPackageFragmentRoot root, IClasspathEntry entry,
            String sourcePath, String sourceRoot) throws Exception {
        IClasspathEntry result;
        int entryKind = entry.getEntryKind();
        // CPE_PROJECT, CPE_LIBRARY, CPE_SOURCE, CPE_VARIABLE or CPE_CONTAINER
        switch (entryKind) {
        case IClasspathEntry.CPE_LIBRARY:
            result = JavaCore.newLibraryEntry(entry.getPath(),
                    sourcePath == null ? null : new Path(sourcePath),
                    sourceRoot == null ? null : new Path(sourceRoot),
                    entry.getAccessRules(), entry.getExtraAttributes(),
                    entry.isExported());
            break;
        case IClasspathEntry.CPE_VARIABLE:
            File sourceAttacherDir = new File(sourcePath).getParentFile();
            JavaCore.setClasspathVariable("SOURCE_ATTACHER",
                    new Path(sourceAttacherDir.getAbsolutePath()), null);
            Path varAttPath = new Path("SOURCE_ATTACHER/"
                    + new File(sourcePath).getName());
            result = JavaCore.newVariableEntry(entry.getPath(), varAttPath,
                    sourceRoot == null ? null : new Path(sourceRoot),
                    entry.getAccessRules(), entry.getExtraAttributes(),
                    entry.isExported());
            break;
        default:
            result = entry;
        }
        return result;
    }


}
