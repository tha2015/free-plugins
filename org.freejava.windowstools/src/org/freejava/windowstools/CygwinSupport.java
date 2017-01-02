package org.freejava.windowstools;

import java.io.File;
import java.io.IOException;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

import com.sun.jna.platform.win32.Advapi32Util;
import com.sun.jna.platform.win32.WinReg;

public class CygwinSupport {
    private static final String REGISTRY_KEY_SETUP = "SOFTWARE\\Cygwin\\setup"; //$NON-NLS-1$
    private static final String REGISTRY_KEY_SETUP_WIN64 = "SOFTWARE\\Wow6432Node\\Cygwin\\setup"; //$NON-NLS-1$
    // note that in Cygwin 1.7 the mount point storage has been moved out of the registry
    private static final String REGISTRY_KEY_MOUNTS = "SOFTWARE\\Cygnus Solutions\\Cygwin\\mounts v2\\"; //$NON-NLS-1$
    private static final String PATH_NAME = "native";   //$NON-NLS-1$
    private static final String SSLASH = "/";           //$NON-NLS-1$
    private static final String BSLASH = "\\\\";        //$NON-NLS-1$
    private static final String ROOTPATTERN = SSLASH;


    private static String readValueFromRegistry(String key, String name) {
        /*WindowsRegistry registry = WindowsRegistry.getRegistry();
        if (null != registry) {
            String s = registry.getCurrentUserValue(key, name);
            if(s == null)
                s = registry.getLocalMachineValue(key, name);

            if (s != null)
                return (s.replaceAll(BSLASH, SSLASH));
        }*/
        String s = null;
        try {
            s = Advapi32Util.registryGetStringValue(WinReg.HKEY_CURRENT_USER, key, name);
        } catch (Exception e) {
            // ignored
        }

        if(s == null) {
            try {
                s = Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE, key, name);
            } catch (Exception e) {
                // ignored
            }
        }

        if (s != null) {
            return (s.replaceAll(BSLASH, SSLASH));
        }

        return null;
    }


    private String getCygwinRootDir() {
        String rootValue = null;

        // 1. Look in PATH values. Look for bin\cygwin1.dll
        IPath location = WindowsAPI.findProgramLocation("cygwin1.dll", null); //$NON-NLS-1$
        if (location!=null) {
            rootValue = location.removeLastSegments(2).toOSString();
        }

        // Find in Start/Programs menu for Cygwin Bash Shell.lnk
        if(rootValue == null) {
            String target = WindowsAPI.getTargetForShortcut("*Cygwin*Shell*.lnk");
            if (target != null) {
                rootValue = new Path(target).removeLastSegments(1).toOSString();
            }
        }

        // 2. Try to find the root dir in SOFTWARE\Cygwin\setup
        if(rootValue == null) {
            rootValue = readValueFromRegistry(REGISTRY_KEY_SETUP, "rootdir"); //$NON-NLS-1$
        }

        // 3. Try to find the root dir in SOFTWARE\Wow6432Node\Cygwin\setup
        if(rootValue == null) {
            rootValue = readValueFromRegistry(REGISTRY_KEY_SETUP_WIN64, "rootdir"); //$NON-NLS-1$
        }

        // 4. Try to find the root dir in SOFTWARE\Cygnus Solutions
        if (rootValue == null) {
            rootValue = readValueFromRegistry(REGISTRY_KEY_MOUNTS + ROOTPATTERN, PATH_NAME);
        }

        // 5. Try the default Cygwin install dir
        if(rootValue == null) {
            for (File root : File.listRoots()) {
                File rootDir = new File(root, "cygwin"); //$NON-NLS-1$
                if (rootDir.exists() && rootDir.isDirectory()) {
                    rootValue = rootDir.getAbsolutePath();
                    break;
                }
                                rootDir = new File(root, "cygwin64"); //$NON-NLS-1$
                                if (rootDir.exists() && rootDir.isDirectory()) {
                                        rootValue = rootDir.getAbsolutePath();
                                        break;
                                }
            }
        }

        if (rootValue != null) {
            rootValue = rootValue.replaceAll(BSLASH, SSLASH);
        }

        return rootValue;
    }
    public boolean isInstalled() {
        return getCygwinRootDir() != null;
    }

    public void shell(String shell, File workingDir, String defaultInstallationPath) throws IOException {

        String cygwinroot = getCygwinRootDir();
        if (cygwinroot == null) {
            new CygwinInstaller().install(defaultInstallationPath);
            if (new File(defaultInstallationPath).exists()) {
                cygwinroot = new File(defaultInstallationPath).getCanonicalPath();
            } else {
                cygwinroot = getCygwinRootDir();
            }
        }

        if (cygwinroot != null) {
            if (StringUtils.equals(shell, "mintty") && new File(cygwinroot, "bin\\mintty.exe").exists()) {
                Process child = Runtime.getRuntime().exec(new String[]{
                        new File(cygwinroot, "bin\\mintty.exe").getAbsolutePath(),
                        "/bin/env", "CHERE_INVOKING=1", "/bin/bash", "-l",
                        "-c", "cd '" + workingDir.getAbsolutePath() + "' ; exec /bin/bash -rcfile ~/.bashrc"}, null,
                        new File(cygwinroot, "bin"));
            } else if (StringUtils.equals(shell, "rxvt") && new File(cygwinroot, "bin\\rxvt.exe").exists()) {
                Process child = Runtime.getRuntime().exec(new String[]{
                        new File(cygwinroot, "bin\\rxvt.exe").getAbsolutePath(),
                        "-fn", "courier",
                        "-e", "/bin/bash",
                        "--login", "-i",
                        "-c", "cd '" + workingDir.getAbsolutePath() + "' ; exec /bin/bash -rcfile ~/.bashrc"}, null,
                        new File(cygwinroot, "bin"));
            } else {
                Process child = Runtime.getRuntime().exec(new String[]{
                        "cmd.exe",
                        "/c",
                        "start",
                        new File(cygwinroot, "bin\\bash.exe").getAbsolutePath(),
                        "--login", "-i",
                        "-c", "cd '" + workingDir.getAbsolutePath() + "' ; exec /bin/bash -rcfile ~/.bashrc"}, null,
                        workingDir);
            }
        }


    }

}
