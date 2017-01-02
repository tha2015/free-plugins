package org.freejava.windowstools;

import java.io.File;
import java.util.Collection;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.swt.internal.win32.OS;
import org.eclipse.swt.internal.win32.TCHAR;
import org.eclipse.swt.ole.win32.OLE;
import org.eclipse.swt.ole.win32.OleAutomation;
import org.eclipse.swt.ole.win32.Variant;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.win32.W32APIOptions;

public class WindowsAPI {

	public static boolean isWindows() {
		return (System.getProperty("os.name").toLowerCase().startsWith("windows"));
	}

	public static String getTargetForShortcut(String shortcutPattern) {
		String target = null;
		try {
	        OleAutomation shell = new OleAutomation("WScript.Shell");
	        OleAutomation folders = null;
			Variant varResult = shell.getProperty(shell.getIDsOfNames(new String[] { "SpecialFolders" })[0]);
			if (varResult != null && varResult.getType() != OLE.VT_EMPTY) {
				folders = varResult.getAutomation();
				varResult.dispose();
			}

	        String Programs;
	        int[] rgdispid = folders.getIDsOfNames(new String[] { "Item", "Index" });
	        int dispIdMember = rgdispid[0];
	        Variant[] rgvarg = new Variant[1];
	        rgvarg[0] = new Variant("Programs");
	        int[] rgdispidNamedArgs = new int[1];
	        rgdispidNamedArgs[0] = rgdispid[1];
	        varResult = folders.invoke(dispIdMember, rgvarg, rgdispidNamedArgs);
	        Programs = varResult.getString();
	        varResult.dispose();
	        Collection<File> links = FileUtils.listFiles(new File(Programs), new WildcardFileFilter(shortcutPattern), DirectoryFileFilter.INSTANCE);
	        if (links.isEmpty()) {
		        String AllUsersPrograms;
		        rgvarg[0] = new Variant("AllUsersPrograms");
		        varResult = folders.invoke(dispIdMember, rgvarg, rgdispidNamedArgs);
		        AllUsersPrograms = varResult.getString();
		        varResult.dispose();
		        links = FileUtils.listFiles(new File(AllUsersPrograms), new WildcardFileFilter(shortcutPattern), DirectoryFileFilter.INSTANCE);
	        }
	        if (!links.isEmpty()) {
		        File linkFile = links.iterator().next();
		        OleAutomation shortcut = null;
		        varResult = shell.invoke(shell.getIDsOfNames(new String[] { "CreateShortcut" })[0], new Variant[] { new Variant(linkFile.getAbsolutePath()) });
				if (varResult != null && varResult.getType() != OLE.VT_EMPTY) {
					shortcut = varResult.getAutomation();
					varResult.dispose();
				}
				varResult = shortcut.getProperty(shortcut.getIDsOfNames(new String[] {"TargetPath"})[0]);
				target = varResult.getString();
				varResult.dispose();
	        }
		} catch (Exception e) {
			// ignore
		}
		return target;
	}

	public static IPath findProgramLocation(String prog, String pathsStr) {
		if (prog==null || prog.trim().length()==0)
			return null;

		if (pathsStr==null)
			pathsStr = System.getenv("PATH"); //$NON-NLS-1$

		if (pathsStr.trim().length()==0)
			return null;

		String locationStr = null;
		String[] dirs = pathsStr.split(File.pathSeparator);

		// check "prog" on Unix and Windows too (if was not found) - could be cygwin or something
		// do it in separate loop due to performance and correctness of Windows regular case
		if (locationStr==null) {
			for (String dir : dirs) {
				IPath dirLocation = new Path(dir);
				File file = null;

				file = dirLocation.append(prog).toFile();
				if (file.isFile() && file.canRead()) {
					locationStr = file.getAbsolutePath();
					break;
				}
			}
		}

		if (locationStr!=null)
			return new Path(locationStr);

		return null;
	}

	public static interface User32 extends Library {
	    User32 INSTANCE = (User32) Native.loadLibrary("user32", User32.class,W32APIOptions.DEFAULT_OPTIONS);
	    int MessageBox(HWND hWnd, String text, String lpCaption, int uType);
	}

    public static final int MB_OKCANCEL = OS.MB_OKCANCEL;

	public static void MessageBox(Object hwnd, String string, String string2, int mbYesno) {
		User32.INSTANCE.MessageBox(null, string, string2, mbYesno); // jna
		OS.MessageBox(0, new TCHAR (0, string, true), new TCHAR (0, string2, true), mbYesno); // swt OS
	}

}
