package org.freejava.windowstools

import org.apache.commons.io.FileUtils;
import java.util.Map
import java.io.File;
import java.nio.charset.Charset
import com.sun.jna.platform.win32.Advapi32Util;
import com.sun.jna.platform.win32.WinReg;
import com.google.common.io.Files
import com.google.common.io.LineProcessor
import org.apache.commons.lang.StringUtils
import org.apache.commons.lang.ArrayUtils
import org.apache.commons.codec.digest.DigestUtils
import org.apache.commons.io.IOUtils;


class CygwinInstaller {
    private static final def SETUP_INI_FILENAME = "setup.ini"
    private static final def SETUP_BZ2_FILENAME = "setup.bz2"

    void install(String path) {

        def setup = "http://cygwin.com/setup-x86_64.exe";
        def server = 'http://ftp.jaist.ac.jp/pub/cygwin/'
        def packageNames = 'autoconf, automake, make, gcc, gdb, subversion, git, gitk, bison, chere, cvs, dog, dos2unix, patch, rxvt, shutdown, upx, vim, wget, which'
        def root = StringUtils.isBlank(path) ? "C:\\cygwin" : path

        File baseDir = new File(System.getProperty("java.io.tmpdir"));
        File tmpDir = new File(baseDir, "cygwinsetup");
        tmpDir.mkdir();

        def ant = new AntBuilder()

        // Download fresh setup.ini
        def iniFile = new File(tmpDir, SETUP_INI_FILENAME)
        iniFile.delete();
        try {
            ant.get (src: new URL(new URL(server), "x86_64/" + SETUP_BZ2_FILENAME) , dest: tmpDir, usetimestamp: true, verbose: true)
            ant.bunzip2(src: new File(tmpDir, SETUP_BZ2_FILENAME), dest: iniFile)
        } catch (Exception e) {
            // ignore
        }
        if (!iniFile.exists()) {
            ant.get (src: new URL(new URL(server), "x86_64/" + SETUP_INI_FILENAME) , dest: tmpDir, usetimestamp: false, verbose: true)
        }

        // Process setup.ini
        final Map<String, Map> packs = new HashMap<String, Map>()
        def final packages = []
        def final selectedPackages = StringUtils.split(packageNames, " ,")
        Files.readLines(iniFile, Charset.forName("UTF-8"),  new LineProcessor<Map<String, Map>>(){
                    boolean processLine(String line) {
                        line = StringUtils.trim(line)
                        if (StringUtils.startsWith(line, "@")) {
                            def name = StringUtils.trim(StringUtils.substring(line, 1))
                            packages.add(name)
                            packs.put(name, [ 'name': name,
                                'download': ArrayUtils.contains(selectedPackages, name),
                                "requires": [], "install" : null])
                        } else if (packages.size() > 0) {
                            def name = packages[packages.size() - 1]
                            def pack = packs.get(name)
                            if (StringUtils.startsWith(line, "category:")) {
                                if (ArrayUtils.contains(StringUtils.split(StringUtils.substring(line, 9), " ,"), "Base")) {
                                    pack.put('download', true)
                                }
                            }
                            if (StringUtils.startsWith(line, "requires:")) {
                                pack.put('requires', StringUtils.split(StringUtils.substring(line, 9), " ,"))
                            }
                            if (StringUtils.startsWith(line, "install:") && pack['install'] == null) {
                                pack.put('install', StringUtils.split(StringUtils.substring(line, 8), " "))
                            }
                        }
                        return true;
                    }
                    Map<String, Map> getResult() {
                        return packs;
                    }
                });

        // Find all dependencies
        def processing = []
        for (def p : packages) if (packs[p]['download']) processing.add(p)
        def downloads = []
        while (processing.size() > 0) {
            def name = processing.remove(0)
            if (!downloads.contains(name)) {
                downloads.add(name)
                def pack = packs[name]
                processing.addAll(packs[name]['requires'])
                packs[name].put('download', true)
            }
        }
        Collections.sort(downloads)

        // Download setup.exe and all required packages
        ant.get (src: new URL(setup) , dest: tmpDir, usetimestamp: true, verbose: true)
        System.out.println(packages);
        System.out.println(downloads);
        for (def name : downloads) {
            def relpath = packs[name]['install'][0]
            def file = new File(tmpDir, relpath)
            Files.createParentDirs(file)
            ant.get (src: new URL(new URL(server), relpath) , dest: file, usetimestamp: true, verbose: true)

            def md5 = packs[name]['install'][2]
            String filemd5;
            InputStream is = null;
            try {
                is = new FileInputStream(file)
                filemd5 = DigestUtils.md5Hex(is)
            } catch (Exception e) {
                IOUtils.closeQuietly(is)
            }
            if (!StringUtils.equalsIgnoreCase(md5, filemd5)) {
                System.out.println("ERROR: Invalid download file :" + relpath);
                file.delete();
                break;
            }
        }

        // HACK: Update setup.ini to set "category: Base" for all required packages
        // so setup.exe will install these packages automatically
        def setupIniOrg = new File(tmpDir, "setup.ini.org")
        ant.move(file: iniFile, tofile: setupIniOrg)
        String thisPackageName = null;
        StringBuilder iniFileContent = new StringBuilder()
        Files.readLines(setupIniOrg, Charset.forName("UTF-8"),  new LineProcessor<Object>(){
                    boolean processLine(String line) {
                        line = StringUtils.trim(line)
                        if (StringUtils.startsWith(line, "@")) {
                            thisPackageName = StringUtils.trim(StringUtils.substring(line, 1))
                        } else if (downloads.contains(thisPackageName)) {
                            if (StringUtils.startsWith(line, "category:")) {
                                if (!ArrayUtils.contains(StringUtils.split(StringUtils.substring(line, 9), " ,"), "Base")) {
                                    line = "category: Base"
                                }
                            }
                        }
                        iniFileContent.append(line + "\n")
                        return true;
                    }
                    Object getResult() {
                        return null;
                    }
                });
        Files.write(iniFileContent, iniFile, Charset.forName("UTF-8"))

        ant.exec(dir: tmpDir, executable : new File(tmpDir, "setup-x86_64.exe")){
            arg(value: "--local-install")
            arg(value: "--local-package-dir")
            arg(value: tmpDir)
            arg(value: "--no-shortcuts")
            arg(value: "--quiet-mode")
            //arg(value: "--root")
            //arg(value: root)
        }
        // setup --local-install --local-package-dir C:\cygwinsetup --quiet-mode --root C:\cygwin
    }

    public static void createExplorerCommand(String name, String progPath) {
        String nameNoSpaces = name.replaceAll(" ", "");
        String cmd = progPath;
        if (cmd.indexOf(' ') != -1) {
            cmd = "\"" + cmd + "\"";
        }
        cmd += " \"%L\""

        Advapi32Util.registryCreateKey(WinReg.HKEY_CLASSES_ROOT, "Directory\\shell", nameNoSpaces)
        Advapi32Util.registrySetStringValue(WinReg.HKEY_CLASSES_ROOT, "Directory\\shell\\" + nameNoSpaces, "", "Open Cygwin Here")
        Advapi32Util.registryCreateKey(WinReg.HKEY_CLASSES_ROOT, "Directory\\shell\\" + nameNoSpaces, "command")
        Advapi32Util.registrySetStringValue(WinReg.HKEY_CLASSES_ROOT, "Directory\\shell\\" + nameNoSpaces + "\\command", "", cmd)

        Advapi32Util.registryCreateKey(WinReg.HKEY_CLASSES_ROOT, "Drive\\shell", nameNoSpaces)
        Advapi32Util.registrySetStringValue(WinReg.HKEY_CLASSES_ROOT, "Drive\\shell\\" + nameNoSpaces, "", "Open Cygwin Here")
        Advapi32Util.registryCreateKey(WinReg.HKEY_CLASSES_ROOT, "Drive\\shell\\" + nameNoSpaces, "command")
        Advapi32Util.registrySetStringValue(WinReg.HKEY_CLASSES_ROOT, "Drive\\shell\\" + nameNoSpaces + "\\command", "", cmd)
    }
}
