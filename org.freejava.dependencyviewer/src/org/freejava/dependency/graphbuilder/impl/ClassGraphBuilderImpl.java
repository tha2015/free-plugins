package org.freejava.dependency.graphbuilder.impl;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.freejava.dependency.graphbuilder.GraphBuilder;
import org.freejava.dependency.model.ClassInfo;
import org.freejava.dependency.model.Edge;
import org.freejava.dependency.model.Graph;
import org.freejava.dependency.model.Name;
import org.freejava.dependency.model.Vertex;

public class ClassGraphBuilderImpl implements GraphBuilder<Name> {
	Map<ClassInfo, File> classInfos;

    public ClassGraphBuilderImpl(Map<ClassInfo, File> classInfos) {
		this.classInfos = classInfos;
	}

	public Graph<Name> build() throws Exception {

		Map<String, File> className2File = getClassLocations(classInfos);

        Map<String, Vertex<Name>> classToVertex = new HashMap<String, Vertex<Name>>();

        // initialize interfaces
        for (ClassInfo classInfo : classInfos.keySet()) {
            for (String name : classInfo.getTypes().keySet()) {
            	Integer type = classInfo.getTypes().get(name);
            	Name nameObj;
            	switch (type.intValue()) {
            	case Name.CLASS_VAL:
            		nameObj = Name.newClass(name, className2File.get(name));
            		break;
            	case Name.INTERFACE_VAL:
            		nameObj = Name.newInterface(name, className2File.get(name));
            		break;
            	case Name.ANNOTATION_VAL:
            		nameObj = Name.newAnnotation(name, className2File.get(name));
            		break;
            	case Name.ENUM_VAL:
            		nameObj = Name.newEnum(name, className2File.get(name));
            		break;
            	default:
            		throw new IllegalArgumentException("Unknown type:" + type);
            	}
                classToVertex.put(name, new Vertex<Name>(nameObj));
            }
        }

        // initialize classes
        for (ClassInfo classInfo : classInfos.keySet()) {

            Set<String> classNames = new HashSet<String>(classInfo.getDependencies());
            classNames.add(classInfo.getClassName());

            for (String className : classNames) {
                if (!classToVertex.containsKey(className)) {
                    classToVertex.put(className, new Vertex<Name>(Name.newClass(className, className2File.get(className))));
                }
            }
        }

        // build graph
        Set<Edge<Name>> edges = new HashSet<Edge<Name>>();
        for (ClassInfo classInfo : classInfos.keySet()) {

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

	private Map<String, File> getClassLocations(Map<ClassInfo, File> classInfos) {
		Map<String, File> result = new HashMap<String, File>();
		for (ClassInfo ci : classInfos.keySet()) {
			result.put(ci.getClassName(), classInfos.get(ci));
		}
		return result;
	}
}
