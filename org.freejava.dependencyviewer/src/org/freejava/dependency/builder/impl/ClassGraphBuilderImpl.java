package org.freejava.dependency.builder.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.freejava.dependency.builder.ClassGraphBuilder;
import org.freejava.dependency.builder.Name;
import org.freejava.dependency.graph.Edge;
import org.freejava.dependency.graph.Graph;
import org.freejava.dependency.graph.Vertex;
import org.freejava.dependency.parser.ClassInfo;
import org.freejava.dependency.parser.ClassParser;

public class ClassGraphBuilderImpl implements ClassGraphBuilder {

    private ClassParser classParser;

    public ClassGraphBuilderImpl(ClassParser classParser) {
        super();
        this.classParser = classParser;
    }

    public Graph<Name> build(Collection<File> resources) throws Exception {

        Collection<ClassInfo> classInfos = new LinkedList<ClassInfo>();
        for (File f : resources) {

            if (f.isFile() && f.getName().endsWith(".class")) {
                InputStream is = new FileInputStream(f);
                try {
                    classInfos.add(this.classParser.parse(is));
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
                          classInfos.add(this.classParser.parse(is));
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
                        classInfos.add(this.classParser.parse(is));
                    } catch (Exception ex) {
                    } finally {
                        IOUtils.closeQuietly(is);
                    }
                }
            }

        }
        Map<String, Vertex<Name>> classToVertex = new HashMap<String, Vertex<Name>>();

        // initialize interfaces
        for (ClassInfo classInfo : classInfos) {
            for (String interfaceName : classInfo.getInterfaces()) {
                classToVertex.put(interfaceName, new Vertex<Name>(Name.newInterface(interfaceName)));
            }
        }

        // initialize classes
        for (ClassInfo classInfo : classInfos) {

            Set<String> classNames = new HashSet<String>(classInfo.getDependencies());
            classNames.add(classInfo.getClassName());

            for (String className : classNames) {
                if (!classToVertex.containsKey(className)) {
                    classToVertex.put(className, new Vertex<Name>(Name.newClass(className)));
                }
            }
        }

        // build graph
        Set<Edge<Name>> edges = new HashSet<Edge<Name>>();
        for (ClassInfo classInfo : classInfos) {

            Vertex<Name> from = classToVertex.get(classInfo.getClassName());

            for (String dependency : classInfo.getDependencies()) {
                Vertex<Name> to = classToVertex.get(dependency);
                if (!from.equals(to)) {
                    edges.add(new Edge<Name>(from, to));
                }

            }
        }
        Graph<Name> result = new Graph<Name>(edges);

        return result;
    }
}
