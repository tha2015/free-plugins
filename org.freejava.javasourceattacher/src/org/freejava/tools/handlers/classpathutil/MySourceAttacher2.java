package org.freejava.tools.handlers.classpathutil;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.corext.util.JavaModelUtil;
import org.eclipse.jdt.internal.ui.wizards.buildpaths.BuildPathSupport;
import org.eclipse.jdt.internal.ui.wizards.buildpaths.CPListElement;
import org.eclipse.swt.widgets.Shell;

/**
 * Modified from org.eclipse.jdt.internal.ui.javaeditor.ClassFileEditor
 * org.eclipse.jdt.ui_3.10.1.v20140817-1500.jar (eclipse 4.4)
 *
 * IMPLEMENTED TO SUPPORT GRADLE CLASSPATH BUT DID NOT WORK
 */
public class MySourceAttacher2 implements SourceAttacher {

    public boolean attachSource(final Shell shell, IPackageFragmentRoot root, String sourcePath) throws Exception {
        IClasspathEntry entry;
        try {
            entry = JavaModelUtil.getClasspathEntry(root);
        } catch (JavaModelException ex) {
            if (ex.isDoesNotExist())
                entry = null;
            else
                throw ex;
        }
        IPath containerPath = null;

        IJavaProject jproject = root.getJavaProject();
        if (entry.getEntryKind() == IClasspathEntry.CPE_CONTAINER) {
            containerPath = entry.getPath();
            IClasspathContainer container = JavaCore.getClasspathContainer(containerPath, jproject);
            entry = JavaModelUtil.findEntryInContainer(container, root.getPath());
            Assert.isNotNull(entry);
        }

        IClasspathEntry newEntry = configureSourceAttachment(sourcePath, entry, jproject);

        applySourceAttachment(shell, newEntry, jproject, containerPath, entry.getReferencingEntry() != null);

        return true;
    }

    private IClasspathEntry configureSourceAttachment(String sourcePath, IClasspathEntry initialEntry, IJavaProject jproject) throws Exception {
        if (initialEntry == null) {
            throw new IllegalArgumentException();
        }
        int entryKind= initialEntry.getEntryKind();
        if (entryKind != IClasspathEntry.CPE_LIBRARY && entryKind != IClasspathEntry.CPE_VARIABLE) {
                throw new IllegalArgumentException();
        }

        return getNewEntry(sourcePath, initialEntry, jproject);
    }

    public IClasspathEntry getNewEntry(String sourcePath, IClasspathEntry fEntry, IJavaProject fProject) throws Exception {
        CPListElement elem = CPListElement.createFromExisting(fEntry, fProject);
        IPath sourceAttachmentPath = Path.fromOSString(sourcePath).makeAbsolute();
        String encoding = ResourcesPlugin.getWorkspace().getRoot().getDefaultCharset();

        elem.setAttribute(CPListElement.SOURCEATTACHMENT, sourceAttachmentPath);
        elem.setAttribute(CPListElement.SOURCE_ATTACHMENT_ENCODING, encoding);
        return elem.getClasspathEntry();
}

    private void applySourceAttachment(Shell shell, IClasspathEntry newEntry, IJavaProject project, IPath containerPath, boolean isReferencedEntry) throws Exception {

        String[] changedAttributes= { CPListElement.SOURCEATTACHMENT, CPListElement.SOURCE_ATTACHMENT_ENCODING };
        BuildPathSupport.modifyClasspathEntry(shell, newEntry, changedAttributes, project, containerPath, isReferencedEntry, new NullProgressMonitor());
    }
}
