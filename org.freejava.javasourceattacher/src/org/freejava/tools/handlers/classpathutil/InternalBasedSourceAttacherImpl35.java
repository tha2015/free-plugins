package org.freejava.tools.handlers.classpathutil;

import java.io.File;
import java.lang.reflect.InvocationTargetException;

import org.apache.commons.beanutils.MethodUtils;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
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
public class InternalBasedSourceAttacherImpl35 implements SourceAttacher {

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

            ///
            IPath containerPath= null;
            IJavaProject jproject= fRoot.getJavaProject();
            IClasspathEntry entry= fRoot.getRawClasspathEntry();
            if (entry == null) {
                // use a dummy entry to use for initialization
                entry= JavaCore.newLibraryEntry(fRoot.getPath(), null, null);
            } else {
                if (entry.getEntryKind() == IClasspathEntry.CPE_CONTAINER) {
                    containerPath= entry.getPath();
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
                    entry= JavaModelUtil.findEntryInContainer(container, fRoot.getPath());
                    Assert.isNotNull(entry);
                }
            }
            fContainerPath= containerPath;
            fEntry= entry;
            ////


            // getNewEntry()
            IClasspathEntry entry1;
            CPListElement elem= CPListElement.createFromExisting(fEntry, null);
            IPath srcAttPath = Path.fromOSString(newSourcePath).makeAbsolute();
            if (fEntry.getEntryKind() == IClasspathEntry.CPE_VARIABLE) {
                File sourceAttacherDir = new File(newSourcePath).getParentFile();
                JavaCore.setClasspathVariable("SOURCE_ATTACHER",
                        new Path(sourceAttacherDir.getAbsolutePath()), null);
                srcAttPath = new Path("SOURCE_ATTACHER/" + new File(newSourcePath).getName());
            }
            elem.setAttribute(CPListElement.SOURCEATTACHMENT, srcAttPath);
            entry1 = elem.getClasspathEntry();

            if (entry1.equals(fEntry)) {
                Logger.debug("NO CHANGE", null);
                return true; // no change
            }

            IClasspathEntry newEntry = entry1;

            String[] changedAttributes= { CPListElement.SOURCEATTACHMENT };
            try {
                MethodUtils.invokeExactStaticMethod(BuildPathSupport.class, "modifyClasspathEntry",
                        new Object[] {null, newEntry, changedAttributes, jproject, fContainerPath, new NullProgressMonitor()},
                        new Class[] {Shell.class, IClasspathEntry.class, String[].class, IJavaProject.class, IPath.class, IProgressMonitor.class});
            } catch (NoSuchMethodException e) {
                throw new RuntimeException(e);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }

        } catch (InvocationTargetException e) {
            // error
            Logger.debug("error", e);
            return false;
        } catch (CoreException e) {
            // error
            Logger.debug("error", e);
            return false;
        }

        return true;
    }

}
