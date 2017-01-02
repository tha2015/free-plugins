package org.freejava.windowstools;

import java.io.File;
import java.io.IOException;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;


public class MingwSupport {

    public boolean isInstalled() {
        return getMsysBinDir() != null;
    }

    public void shell(String shell, File workingDir, String defaultInstallationPath) throws IOException {
        IPath path = getMsysBinDir();

        if (path == null) {
            new MingwInstaller().install(defaultInstallationPath);
            if (new File(defaultInstallationPath).exists()) {
                path = new Path(new File(defaultInstallationPath, "msys\\1.0\\bin").getCanonicalPath());
            }
        }

        if (path != null) {
            if ("console".equals(shell)) {
                File msys = path.toFile().getParentFile();
                File console2bat = new File(msys, "console2.bat");
                if (console2bat.exists()) {
                    Process child = Runtime.getRuntime().exec(new String[]{
                            console2bat.getAbsolutePath(),
                            workingDir.getAbsolutePath()},
                            null, path.toFile());
                }
            } else if ("mintty".equals(shell) && new File(path.toOSString(), "mintty.exe").exists()) {

                ProcessBuilder pb = new ProcessBuilder(new String[]{
                        new File(path.toOSString(), "mintty.exe").getAbsolutePath(),
                        "/bin/env", "CHERE_INVOKING=1", "/bin/bash", "-l",
                        "-c", "cd '" + workingDir.getAbsolutePath() + "' ; exec /bin/bash -rcfile ~/.bashrc"});
                pb.environment().put("USERNAME", System.getenv("USERNAME").replace(" ", ""));
                pb.directory(path.toFile());
                pb.start();
            } else {
                path = path.removeLastSegments(1).append("msys.bat");
                Process child = Runtime.getRuntime().exec(
                        new String[]{path.toOSString()},
                        null,
                        workingDir);
            }
        }
    }


    private static IPath getMsysBinDir() {

        IPath msysBinPath;

        msysBinPath = getMsysBinDirFromEclipse();
        if (msysBinPath != null && msysBinPath.toFile().isDirectory())
            return msysBinPath;

        String mingwHome = System.getenv("MINGW_HOME"); //$NON-NLS-1$
        if (mingwHome == null) {
            IPath mingwbin = null;
            // Look in PATH values. Look for mingw32-gcc.exe
            IPath gccLoc = WindowsAPI.findProgramLocation("mingw32-gcc.exe", null); //$NON-NLS-1$
            if (gccLoc != null)
                mingwbin = gccLoc.removeLastSegments(1);
            if (mingwbin != null) {
                mingwHome = mingwbin.toFile().getParent();
            }
        }
        if (mingwHome != null) {
            msysBinPath = new Path(mingwHome + "\\msys\\1.0\\bin"); //$NON-NLS-1$
            if (msysBinPath.toFile().isDirectory())
                return msysBinPath;
        }

        // Search in Start/Programs menu for MinGW Shell.lnk
        String target = WindowsAPI.getTargetForShortcut("*MinGW*Shell*.lnk");
        if (target != null)
            msysBinPath = new Path(target).removeLastSegments(1).append("bin");
        if (msysBinPath.toFile().isDirectory())
            return msysBinPath;

        // Try to find MinGW msys bin dir at drives' roots
        for (File root : File.listRoots()) {
            msysBinPath = new Path(new File(root, "MinGW\\msys\\1.0\\bin").getAbsolutePath()); //$NON-NLS-1$
            if (msysBinPath.toFile().isDirectory())
                return msysBinPath;
        }

        return null;
    }

    private static IPath getMsysBinDirFromEclipse() {
        // Try the mingw directory in the platform install directory
        // CDT distributions like Wascana may distribute MinGW like that
        IPath msysBinPath = null;
        try {
            IPath installPath = new Path(Platform.getInstallLocation().getURL().getFile());
            msysBinPath = installPath.append("msys\\bin"); //$NON-NLS-1$
        } catch (Exception e) {
            // ignore
        }
        return msysBinPath;
    }

}
