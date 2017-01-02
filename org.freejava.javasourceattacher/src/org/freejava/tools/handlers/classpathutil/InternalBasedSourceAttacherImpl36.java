package org.freejava.tools.handlers.classpathutil;

import java.io.File;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.ClasspathContainerInitializer;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.corext.util.JavaModelUtil;
import org.eclipse.jdt.internal.ui.wizards.buildpaths.BuildPathSupport;
import org.eclipse.jdt.internal.ui.wizards.buildpaths.CPListElement;
import org.eclipse.swt.widgets.Shell;

// copied and modified from org.eclipse.jdt.internal.ui.preferences.SourceAttachmentPropertyPage
public class InternalBasedSourceAttacherImpl36 implements SourceAttacher {

    public boolean attachSource(final Shell shell, IPackageFragmentRoot fRoot, String newSourcePath)
            throws CoreException {

        IPath fContainerPath;
        IClasspathEntry fEntry;

        try {
            fContainerPath= null;
            fEntry= null;
            if (fRoot == null || fRoot.getKind() != IPackageFragmentRoot.K_BINARY) {
                // error
                Logger.debug("error(!=K_BINARY)", null);
                return false;
            }

            IPath containerPath= null;
            IJavaProject jproject= fRoot.getJavaProject();
            IClasspathEntry entry0 = JavaModelUtil.getClasspathEntry(fRoot);
            if (entry0.getEntryKind() == IClasspathEntry.CPE_CONTAINER) {
                containerPath= entry0.getPath();
                ClasspathContainerInitializer initializer= JavaCore.getClasspathContainerInitializer(containerPath.segment(0));
                IClasspathContainer container= JavaCore.getClasspathContainer(containerPath, jproject);
                if (initializer == null || container == null) {
                    // error
                    Logger.debug("error(initializer == null || container == null)", null);
                    return false;
                }

                IStatus status= initializer.getSourceAttachmentStatus(containerPath, jproject);
                if (status.getCode() == ClasspathContainerInitializer.ATTRIBUTE_NOT_SUPPORTED) {
                    // error
                    Logger.debug("error(ATTRIBUTE_NOT_SUPPORTED)", null);
                    return false;
                }
                if (status.getCode() == ClasspathContainerInitializer.ATTRIBUTE_READ_ONLY) {
                    // error
                    Logger.debug("error(ATTRIBUTE_READ_ONLY)", null);
                    return false;
                }
                entry0= JavaModelUtil.findEntryInContainer(container, fRoot.getPath());
            }
            fContainerPath= containerPath;
            fEntry= entry0;


            // getNewEntry()
            IClasspathEntry entry;
            CPListElement elem= CPListElement.createFromExisting(fEntry, null);
            IPath srcAttPath = Path.fromOSString(newSourcePath).makeAbsolute();
            if (fEntry.getEntryKind() == IClasspathEntry.CPE_VARIABLE) {
                File sourceAttacherDir = new File(newSourcePath).getParentFile();
                JavaCore.setClasspathVariable("SOURCE_ATTACHER",
                        new Path(sourceAttacherDir.getAbsolutePath()), null);
                srcAttPath = new Path("SOURCE_ATTACHER/" + new File(newSourcePath).getName());
            }
            elem.setAttribute(CPListElement.SOURCEATTACHMENT, srcAttPath);
            entry = elem.getClasspathEntry();

            if (entry.equals(fEntry)) {
                Logger.debug("NO CHANGE", null);
                return true; // no change
            }

            IClasspathEntry newEntry = entry;
            boolean isReferencedEntry = fEntry.getReferencingEntry() != null;

            String[] changedAttributes= { CPListElement.SOURCEATTACHMENT };
            BuildPathSupport.modifyClasspathEntry(null, newEntry, changedAttributes, jproject, fContainerPath, isReferencedEntry, new NullProgressMonitor());

        } catch (CoreException e) {
            // error
            Logger.debug("error", e);
            return false;
        }

        return true;
    }
}
