package org.freejava.tools.scopeparser.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.freejava.dependency.classparser.ClassParser;
import org.freejava.dependency.model.ClassInfo;
import org.freejava.dependency.model.FileParsingScope;
import org.freejava.tools.scopeparser.ScopeParser;

public class ScopeParserImpl implements ScopeParser {
    private ClassParser classParser;

    public ScopeParserImpl(ClassParser classParser) {
        super();
        this.classParser = classParser;
    }

	public Map<ClassInfo, File> parse(FileParsingScope scope) throws Exception {


        Map<ClassInfo, File> classInfos = new HashMap<ClassInfo, File>();

        for (File f : scope.getFiles()) {

            if (f.isFile() && f.getName().endsWith(".class")) {
                InputStream is = new FileInputStream(f);
                try {
                    classInfos.put(this.classParser.parse(is), f);
                } catch (Exception ex) {
                } finally {
                    IOUtils.closeQuietly(is);
                }
            }
            if (f.isFile() && f.getName().endsWith(".jar")) {
                JarFile jarFile = new JarFile(f);
                Enumeration<JarEntry> e = jarFile.entries();
                while (e.hasMoreElements()) {
                  JarEntry entry = (JarEntry) e.nextElement();
                  if (!entry.isDirectory() && entry.getName().endsWith(".class")) {
                      InputStream is = jarFile.getInputStream(entry);
                      try {
                          classInfos.put(this.classParser.parse(is), f);
                      } catch (Exception ex) {
                      } finally {
                          IOUtils.closeQuietly(is);
                      }
                  }
               }
               jarFile.close();
            }
            if (f.isDirectory()) {
                for (File file : FileUtils.listFiles(f, new String[] {"class"}, true)) {
                    InputStream is = new FileInputStream(file);
                    try {
                        classInfos.put(this.classParser.parse(is), f);
                    } catch (Exception ex) {
                    } finally {
                        IOUtils.closeQuietly(is);
                    }
                }
            }

        }

		return classInfos;
	}

}
