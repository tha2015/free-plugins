package org.freejava.tools.handlers.classpathutil;

import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.swt.widgets.Shell;

public interface SourceAttacher {
    boolean attachSource(final Shell shell, IPackageFragmentRoot fRoot, String newSourcePath) throws Exception;
}
