package org.freejava.windowstools

import org.apache.commons.io.FileUtils;

import com.sun.jna.platform.win32.Advapi32Util;
import com.sun.jna.platform.win32.WinReg;

class MingwInstaller {
	void install(String path) {

		if (new File(path).exists()) return;

		def ant = new AntBuilder()
		def url = "http://softlayer-dal.dl.sourceforge.net/project/mingw/Installer/mingw-get/mingw-get-0.6.2-beta-20131004-1/mingw-get-0.6.2-mingw32-beta-20131004-1-bin.zip"
		def zipFileName = url.substring(url.lastIndexOf('/') + 1)
		File tmpDir = new File(System.getProperty("java.io.tmpdir"))
		ant.get (src: url, dest: tmpDir, usetimestamp: true, verbose: true)
		ant.unzip (dest: path) { fileset(dir: tmpDir){ include (name: zipFileName)} }
		ant.exec(dir: new File(path, "bin"), executable: new File(path, "bin\\mingw-get.exe")){arg(line:"update")}
		ant.exec(dir: new File(path, "bin"), executable: new File(path, "bin\\mingw-get.exe")){
			arg(line:"install mingw-get pkginfo base gcc-g++ msys-base mingw-dtk msys-man msys-rxvt mintty msys-wget msys-unzip msys-console")
		}

		File fstab = new File(path, "msys\\1.0\\etc\\fstab")
		if (!fstab.exists()) FileUtils.writeStringToFile(fstab, path.replace('\\', '/') + " /mingw\r\n")

		File msysbat = new File(path, "msys\\1.0\\msys.bat")
		String setUsername = 'set USERNAME=%USERNAME: =%'
		String msysbatStr = FileUtils.readFileToString(msysbat, "UTF-8")
		if (!msysbatStr.startsWith(setUsername)) {
			FileUtils.writeStringToFile(msysbat, setUsername + "\r\n" + msysbatStr)
		}

		File profile = new File(path, "msys\\1.0\\etc\\profile")
		String profileStr = FileUtils.readFileToString(profile, "UTF-8")
		String cdHome = 'cd "$HOME"'
		String commentedCdHome = '#cd "$HOME"'
		if (profileStr.indexOf(cdHome) != -1 && profileStr.indexOf(commentedCdHome) == -1) {
			FileUtils.writeStringToFile(profile, profileStr.replace(cdHome, commentedCdHome))
		}

		if (!new File(path, "msys\\1.0\\console2.bat").exists()) {
			ant.exec(dir: new File(path, "bin"), executable: new File(path, "msys\\1.0\\bin\\sh.exe")){
				arg(line:"--login -i -c console-config")
				env(key:"USERNAME", value: System.getenv("USERNAME").replace(" ",""))
			}
			String console2batStr = '''\
@echo off
if "%OS%"=="Windows_NT" @setlocal
if "%OS%"=="WINNT" @setlocal
set USERNAME=%USERNAME: =%
if "x%~1" == "x" goto NoDir
set DIR=%~1
goto OkDir
:NoDir
set DIR=.
goto OkDir
:OkDir
"%~dp0lib\\Console2\\Console.exe" -t "MinGW" -d "%DIR%"
:end
if "%OS%"=="Windows_NT" goto endNT
if "%OS%"=="WINNT" goto endNT
goto postExec
:endNT
@endlocal
:postExec
'''
			FileUtils.writeStringToFile(new File(path, "msys\\1.0\\console2.bat"), console2batStr.replace("\n", "\r\n"))
			String batPath = new File(path, "msys\\1.0\\console2.bat").getAbsolutePath();

			createExplorerCommand("Open MSYS Here", batPath)
		}
	}

	public static void createExplorerCommand(String name, String progPath) {
		String nameNoSpaces = name.replaceAll(" ", "");
		String cmd = progPath;
		if (cmd.indexOf(' ') != -1) {
			cmd = "\"" + cmd + "\"";
		}
		cmd += " \"%L\""

		Advapi32Util.registryCreateKey(WinReg.HKEY_CLASSES_ROOT, "Directory\\shell", nameNoSpaces)
		Advapi32Util.registrySetStringValue(WinReg.HKEY_CLASSES_ROOT, "Directory\\shell\\" + nameNoSpaces, "", "Open MSYS Here")
		Advapi32Util.registryCreateKey(WinReg.HKEY_CLASSES_ROOT, "Directory\\shell\\" + nameNoSpaces, "command")
		Advapi32Util.registrySetStringValue(WinReg.HKEY_CLASSES_ROOT, "Directory\\shell\\" + nameNoSpaces + "\\command", "", cmd)

		Advapi32Util.registryCreateKey(WinReg.HKEY_CLASSES_ROOT, "Drive\\shell", nameNoSpaces)
		Advapi32Util.registrySetStringValue(WinReg.HKEY_CLASSES_ROOT, "Drive\\shell\\" + nameNoSpaces, "", "Open MSYS Here")
		Advapi32Util.registryCreateKey(WinReg.HKEY_CLASSES_ROOT, "Drive\\shell\\" + nameNoSpaces, "command")
		Advapi32Util.registrySetStringValue(WinReg.HKEY_CLASSES_ROOT, "Drive\\shell\\" + nameNoSpaces + "\\command", "", cmd)
	}
}
/*
# see http://mingw.cvs.sourceforge.net/viewvc/mingw/mingw-get-inst/mingw-get-inst.iss?view=markup

download http://nchc.dl.sourceforge.net/project/mingw/Automated%20MinGW%20Installer/mingw-get/mingw-get-0.3-alpha-2.1/mingw-get-0.3-mingw32-alpha-2.1-bin.zip
unzip to C:\MinGW


C:\MinGW\bin\mingw-get.exe update
C:\MinGW\bin\mingw-get.exe install mingw-get pkginfo base gcc-g++ msys-base mingw-dtk msys-rxvt mintty msys-wget msys-unzip msys-console


create C:\MinGW\msys\1.0\etc\fstab
C:\MinGW\   /mingw  + #13#10


update c:\MinGW\msys\1.0\msys.bat
set USERNAME=%USERNAME: =%

update  c:\MinGW\msys\1.0\etc\profile
#cd "$HOME"

set USERNAME=%USERNAME: =%
c:\MinGW\msys\1.0\bin\sh --login -i -c console-config

execute from c:/MinGW/msys/1.0/bin
C:\MinGW\msys\1.0\lib\Console2\Console.exe -t "MinGW" -d "C:\"

# see http://www.burgaud.com/open-command-window-here/
[HKEY_CLASSES_ROOT\Directory\shell\CommandPrompt]
@="Open Command Window Here"
[HKEY_CLASSES_ROOT\Directory\shell\CommandPrompt\command]
@="cmd.exe /k pushd %L"
[HKEY_CLASSES_ROOT\Drive\shell\CommandPrompt]
@="Open Command Window Here"
[HKEY_CLASSES_ROOT\Drive\shell\CommandPrompt\command]
@="cmd.exe /k pushd %L"
*/