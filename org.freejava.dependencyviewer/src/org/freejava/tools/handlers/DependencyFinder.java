package org.freejava.tools.handlers;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import com.google.common.base.CharMatcher;
import com.google.common.base.Splitter;
import com.jeantessier.classreader.AggregatingClassfileLoader;
import com.jeantessier.classreader.Attribute_info;
import com.jeantessier.classreader.ClassNameHelper;
import com.jeantessier.classreader.ClassfileLoader;
import com.jeantessier.classreader.DescriptorHelper;
import com.jeantessier.classreader.Field_info;
import com.jeantessier.classreader.LoadListenerVisitorAdapter;
import com.jeantessier.classreader.LocalVariable;
import com.jeantessier.classreader.LocalVariableTable_attribute;
import com.jeantessier.classreader.LocalVariableType;
import com.jeantessier.classreader.LocalVariableTypeTable_attribute;
import com.jeantessier.classreader.Method_info;
import com.jeantessier.classreader.Signature_attribute;
import com.jeantessier.classreader.TransientClassfileLoader;
import com.jeantessier.classreader.Visitor;
import com.jeantessier.dependency.CodeDependencyCollector;
import com.jeantessier.dependency.CollectionSelectionCriteria;
import com.jeantessier.dependency.GraphSummarizer;
import com.jeantessier.dependency.LinkMaximizer;
import com.jeantessier.dependency.Node;
import com.jeantessier.dependency.NodeFactory;

public class DependencyFinder {

    public Collection<Node> getPackageDependency(Collection<File> files, Collection<String> names) {

        Collection<String> sources = new HashSet<String>();
        for (File file : files) {
            sources.add(file.getAbsolutePath());
        }

        NodeFactory factory = new NodeFactory();
        Visitor visitor = new CodeDependencyCollector(factory);
        ClassfileLoader loader = new TransientClassfileLoader();
        loader.addLoadListener(new LoadListenerVisitorAdapter(visitor));
        loader.load(sources);

        LinkMaximizer maximizer = new LinkMaximizer();
        maximizer.traverseNodes(factory.getPackages().values());

        CollectionSelectionCriteria scopeCriteria = new CollectionSelectionCriteria(names, null);
        scopeCriteria.setMatchingPackages(true);
        scopeCriteria.setMatchingClasses(false);
        scopeCriteria.setMatchingFeatures(false);
        CollectionSelectionCriteria filterCriteria = new CollectionSelectionCriteria(names, null);
        filterCriteria.setMatchingPackages(true);
        filterCriteria.setMatchingClasses(false);
        filterCriteria.setMatchingFeatures(false);

        GraphSummarizer dependenciesQuery = new GraphSummarizer(scopeCriteria, filterCriteria);
        dependenciesQuery.traverseNodes(factory.getPackages().values());
        Collection<Node> nodes = new ArrayList<Node>();
        nodes.addAll(dependenciesQuery.getScopeFactory().getPackages().values());
        return nodes;
    }

    public Collection<Node> getClassDependency(Collection<File> files, Collection<String> names) {
        Collection<String> sources = new HashSet<String>();
        for (File file : files) {
            sources.add(file.getAbsolutePath());
        }

        NodeFactory factory = new NodeFactory();
        Visitor visitor = new CodeDependencyCollector(factory) {
            @Override
            public void visitSignature_attribute(Signature_attribute attribute) {
                super.visitSignature_attribute(attribute);

                String ownerFullSignature = findOwnerFullSignature(attribute);

                if (ownerFullSignature != null) {
                    String sig = attribute.getSignature();
                    for (String id : Splitter.on(CharMatcher.anyOf("<>;")).omitEmptyStrings().split(sig)) {
                        if (id.startsWith("L")) {
                            String className = id.substring(1);
                            className = ClassNameHelper.path2ClassName(className);
                            Node node = getFactory().createFeature(ownerFullSignature, true);
                            node.addDependency(getFactory().createClass(className));
                        }
                    }
                }
            }
            private String findOwnerFullSignature(Attribute_info owner) {
                String ownerFullSignature = null;
                Object currentObject = owner;
                while (true) {
                    if (currentObject instanceof Field_info) {
                        ownerFullSignature = ((Field_info) currentObject).getFullSignature();
                    } else if (currentObject instanceof Method_info) {
                        ownerFullSignature = ((Method_info) currentObject).getFullSignature();
                    } else if (currentObject instanceof Attribute_info) {
                        currentObject = ((Attribute_info)currentObject).getOwner();
                        continue;
                    }
                    break;
                }

                return ownerFullSignature;
            }
            @Override
            public void visitLocalVariable(LocalVariable helper) {
                super.visitLocalVariable(helper);

                LocalVariableTable_attribute attribute = helper.getLocalVariableTable();
                String ownerFullSignature = findOwnerFullSignature(attribute);

                for (String className : Splitter.on(CharMatcher.anyOf("<>;")).omitEmptyStrings().split(DescriptorHelper.getType(helper.getDescriptor()))) {
                    Node node = getFactory().createFeature(ownerFullSignature, true);
                    node.addDependency(getFactory().createClass(className));
                }


            }
            @Override
            public void visitLocalVariableTypeTable_attribute(LocalVariableTypeTable_attribute attribute) {
                super.visitLocalVariableTypeTable_attribute(attribute);

                String ownerFullSignature = findOwnerFullSignature(attribute);
                for (LocalVariableType type : attribute.getLocalVariableTypes()) {
                    for (String id : Splitter.on(CharMatcher.anyOf("<>;")).omitEmptyStrings().split(type.getSignature())) {
                        if (id.startsWith("L")) {
                            String className = id.substring(1);
                            className = ClassNameHelper.path2ClassName(className);
                            Node node = getFactory().createFeature(ownerFullSignature, true);
                            node.addDependency(getFactory().createClass(className));
                        }
                    }
                }
            }
        };
        ClassfileLoader loader = new AggregatingClassfileLoader();
        loader.addLoadListener(new LoadListenerVisitorAdapter(visitor));
        loader.load(sources);

        LinkMaximizer maximizer = new LinkMaximizer();
        maximizer.traverseNodes(factory.getPackages().values());

        CollectionSelectionCriteria scopeCriteria = new CollectionSelectionCriteria(names, null);
        scopeCriteria.setMatchingPackages(false);
        scopeCriteria.setMatchingClasses(true);
        scopeCriteria.setMatchingFeatures(false);
        CollectionSelectionCriteria filterCriteria = new CollectionSelectionCriteria(names, null);
        filterCriteria.setMatchingPackages(false);
        filterCriteria.setMatchingClasses(true);
        filterCriteria.setMatchingFeatures(false);

        GraphSummarizer dependenciesQuery = new GraphSummarizer(scopeCriteria, filterCriteria);
        dependenciesQuery.traverseNodes(factory.getPackages().values());
        Collection<Node> nodes = new ArrayList<Node>();
        nodes.addAll(dependenciesQuery.getScopeFactory().getClasses().values());
        return nodes;
    }


    public List<Object[]> getDependencyEdges(Collection<Node> input) {
        List<Object[]> arrows = new ArrayList<Object[]>();
        for (Node pkg : input) {
            for (Node node : pkg.getOutboundDependencies()) {
                if (node instanceof Node) {
                    Object[] arrow = new Object[2];
                    arrow[0] = pkg;
                    arrow[1] = node;
                    arrows.add(arrow);
                }
            }
        }
        return arrows;
    }

}
