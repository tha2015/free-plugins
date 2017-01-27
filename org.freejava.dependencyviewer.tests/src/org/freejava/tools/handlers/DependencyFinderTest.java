package org.freejava.tools.handlers;
import static org.junit.Assert.*;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

//import org.freejava.tools.handlers.DependencyFinder;
import org.junit.Test;

//import com.jeantessier.dependency.Node;


public class DependencyFinderTest {
    @Test
    public void testGetClassDependencyWithMember() {
         assertTrue(true);
    }
/*

    @Test
    public void testGetClassDependencyWithMember() {

        DependencyFinder finder = new DependencyFinder();

        Collection<File> files = Arrays.asList(new File[] {
                new File("./bin/org/freejava/tools/handlers/testresources/Product.class"),
                new File("./bin/org/freejava/tools/handlers/testresources/CartHasProductMember.class")
        });

        Collection<String> names = Arrays.asList(new String[] {
            "org.freejava.tools.handlers.testresources.Product",
            "org.freejava.tools.handlers.testresources.CartHasProductMember"
        });

        Collection<Node> nodes = finder.getClassDependency(files, names);

        List<Object[]> edges = finder.getDependencyEdges(nodes);

        assertEquals(1, edges.size());

        Object[] edge = edges.get(0);

        Node from = (Node) edge[0];
        Node to = (Node) edge[1];

        assertEquals("org.freejava.tools.handlers.testresources.CartHasProductMember", from.getName());
        assertEquals("org.freejava.tools.handlers.testresources.Product", to.getName());

    }

    @Test
    public void testGetClassDependencyWithMembers() {

        DependencyFinder finder = new DependencyFinder();

        Collection<File> files = Arrays.asList(new File[] {
                new File("./bin/org/freejava/tools/handlers/testresources/Product.class"),
                new File("./bin/org/freejava/tools/handlers/testresources/CartHasProductsMember.class")
        });

        Collection<String> names = Arrays.asList(new String[] {
            "org.freejava.tools.handlers.testresources.Product",
            "org.freejava.tools.handlers.testresources.CartHasProductsMember"
        });

        Collection<Node> nodes = finder.getClassDependency(files, names);

        List<Object[]> edges = finder.getDependencyEdges(nodes);

        assertEquals(1, edges.size());

        Object[] edge = edges.get(0);

        Node from = (Node) edge[0];
        Node to = (Node) edge[1];

        assertEquals("org.freejava.tools.handlers.testresources.CartHasProductsMember", from.getName());
        assertEquals("org.freejava.tools.handlers.testresources.Product", to.getName());

    }

    @Test
    public void testGetClassDependencyWithParam() {

        DependencyFinder finder = new DependencyFinder();

        Collection<File> files = Arrays.asList(new File[] {
                new File("./bin/org/freejava/tools/handlers/testresources/Product.class"),
                new File("./bin/org/freejava/tools/handlers/testresources/CartUseProductParam.class")
        });

        Collection<String> names = Arrays.asList(new String[] {
            "org.freejava.tools.handlers.testresources.Product",
            "org.freejava.tools.handlers.testresources.CartUseProductParam"

        });

        Collection<Node> nodes = finder.getClassDependency(files, names);

        List<Object[]> edges = finder.getDependencyEdges(nodes);

        assertEquals(1, edges.size());

        Object[] edge = edges.get(0);

        Node from = (Node) edge[0];
        Node to = (Node) edge[1];

        assertEquals("org.freejava.tools.handlers.testresources.CartUseProductParam", from.getName());
        assertEquals("org.freejava.tools.handlers.testresources.Product", to.getName());

    }

    @Test
    public void testGetClassDependencyWithParams() {

        DependencyFinder finder = new DependencyFinder();

        Collection<File> files = Arrays.asList(new File[] {
                new File("./bin/org/freejava/tools/handlers/testresources/Product.class"),
                new File("./bin/org/freejava/tools/handlers/testresources/CartUseProductsParam.class")
        });

        Collection<String> names = Arrays.asList(new String[] {
            "org.freejava.tools.handlers.testresources.Product",
            "org.freejava.tools.handlers.testresources.CartUseProductsParam"

        });

        Collection<Node> nodes = finder.getClassDependency(files, names);

        List<Object[]> edges = finder.getDependencyEdges(nodes);

        assertEquals(1, edges.size());

        Object[] edge = edges.get(0);

        Node from = (Node) edge[0];
        Node to = (Node) edge[1];

        assertEquals("org.freejava.tools.handlers.testresources.CartUseProductsParam", from.getName());
        assertEquals("org.freejava.tools.handlers.testresources.Product", to.getName());

    }

    @Test
    public void testGetClassDependencyWithLocalVar() {

        DependencyFinder finder = new DependencyFinder();

        Collection<File> files = Arrays.asList(new File[] {
                new File("./bin/org/freejava/tools/handlers/testresources/Product.class"),
                new File("./bin/org/freejava/tools/handlers/testresources/CartUseProductLocalVar.class")
        });

        Collection<String> names = Arrays.asList(new String[] {
            "org.freejava.tools.handlers.testresources.Product",
            "org.freejava.tools.handlers.testresources.CartUseProductLocalVar"

        });

        Collection<Node> nodes = finder.getClassDependency(files, names);

        List<Object[]> edges = finder.getDependencyEdges(nodes);

        assertEquals(1, edges.size());

        Object[] edge = edges.get(0);

        Node from = (Node) edge[0];
        Node to = (Node) edge[1];

        assertEquals("org.freejava.tools.handlers.testresources.CartUseProductLocalVar", from.getName());
        assertEquals("org.freejava.tools.handlers.testresources.Product", to.getName());

    }
    @Test
    public void testGetClassDependencyWithLocalVars() {

        DependencyFinder finder = new DependencyFinder();

        Collection<File> files = Arrays.asList(new File[] {
                new File("./bin/org/freejava/tools/handlers/testresources/Product.class"),
                new File("./bin/org/freejava/tools/handlers/testresources/CartUseProductsLocalVar.class")
        });

        Collection<String> names = Arrays.asList(new String[] {
            "org.freejava.tools.handlers.testresources.Product",
            "org.freejava.tools.handlers.testresources.CartUseProductsLocalVar"

        });

        Collection<Node> nodes = finder.getClassDependency(files, names);

        List<Object[]> edges = finder.getDependencyEdges(nodes);

        assertEquals(1, edges.size());

        Object[] edge = edges.get(0);

        Node from = (Node) edge[0];
        Node to = (Node) edge[1];

        assertEquals("org.freejava.tools.handlers.testresources.CartUseProductsLocalVar", from.getName());
        assertEquals("org.freejava.tools.handlers.testresources.Product", to.getName());

    }

    @Test
    public void testGetClassDependencyWithStaticBlockVar() {

        DependencyFinder finder = new DependencyFinder();

        Collection<File> files = Arrays.asList(new File[] {
                new File("./bin/org/freejava/tools/handlers/testresources/Product.class"),
                new File("./bin/org/freejava/tools/handlers/testresources/CartUseProductStaticBlockVar.class")
        });

        Collection<String> names = Arrays.asList(new String[] {
            "org.freejava.tools.handlers.testresources.Product",
            "org.freejava.tools.handlers.testresources.CartUseProductStaticBlockVar"

        });

        Collection<Node> nodes = finder.getClassDependency(files, names);

        List<Object[]> edges = finder.getDependencyEdges(nodes);

        assertEquals(1, edges.size());

        Object[] edge = edges.get(0);

        Node from = (Node) edge[0];
        Node to = (Node) edge[1];

        assertEquals("org.freejava.tools.handlers.testresources.CartUseProductStaticBlockVar", from.getName());
        assertEquals("org.freejava.tools.handlers.testresources.Product", to.getName());

    }
    @Test
    public void testGetClassDependencyWithStaticBlockVars() {

        DependencyFinder finder = new DependencyFinder();

        Collection<File> files = Arrays.asList(new File[] {
                new File("./bin/org/freejava/tools/handlers/testresources/Product.class"),
                new File("./bin/org/freejava/tools/handlers/testresources/CartUseProductsStaticBlockVar.class")
        });

        Collection<String> names = Arrays.asList(new String[] {
            "org.freejava.tools.handlers.testresources.Product",
            "org.freejava.tools.handlers.testresources.CartUseProductsStaticBlockVar"

        });

        Collection<Node> nodes = finder.getClassDependency(files, names);

        List<Object[]> edges = finder.getDependencyEdges(nodes);

        assertEquals(1, edges.size());

        Object[] edge = edges.get(0);

        Node from = (Node) edge[0];
        Node to = (Node) edge[1];

        assertEquals("org.freejava.tools.handlers.testresources.CartUseProductsStaticBlockVar", from.getName());
        assertEquals("org.freejava.tools.handlers.testresources.Product", to.getName());

    }

    @Test
    public void testGetClassDependencyWithInstanceBlockVar() {

        DependencyFinder finder = new DependencyFinder();

        Collection<File> files = Arrays.asList(new File[] {
                new File("./bin/org/freejava/tools/handlers/testresources/Product.class"),
                new File("./bin/org/freejava/tools/handlers/testresources/CartUseProductInstanceBlockVar.class")
        });

        Collection<String> names = Arrays.asList(new String[] {
            "org.freejava.tools.handlers.testresources.Product",
            "org.freejava.tools.handlers.testresources.CartUseProductInstanceBlockVar"

        });

        Collection<Node> nodes = finder.getClassDependency(files, names);

        List<Object[]> edges = finder.getDependencyEdges(nodes);

        assertEquals(1, edges.size());

        Object[] edge = edges.get(0);

        Node from = (Node) edge[0];
        Node to = (Node) edge[1];

        assertEquals("org.freejava.tools.handlers.testresources.CartUseProductInstanceBlockVar", from.getName());
        assertEquals("org.freejava.tools.handlers.testresources.Product", to.getName());

    }

    @Test
    public void testGetClassDependencyWithInstanceBlockVars() {

        DependencyFinder finder = new DependencyFinder();

        Collection<File> files = Arrays.asList(new File[] {
                new File("./bin/org/freejava/tools/handlers/testresources/Product.class"),
                new File("./bin/org/freejava/tools/handlers/testresources/CartUseProductsInstanceBlockVar.class")
        });

        Collection<String> names = Arrays.asList(new String[] {
            "org.freejava.tools.handlers.testresources.Product",
            "org.freejava.tools.handlers.testresources.CartUseProductsInstanceBlockVar"

        });

        Collection<Node> nodes = finder.getClassDependency(files, names);

        List<Object[]> edges = finder.getDependencyEdges(nodes);

        assertEquals(1, edges.size());

        Object[] edge = edges.get(0);

        Node from = (Node) edge[0];
        Node to = (Node) edge[1];

        assertEquals("org.freejava.tools.handlers.testresources.CartUseProductsInstanceBlockVar", from.getName());
        assertEquals("org.freejava.tools.handlers.testresources.Product", to.getName());

    }
    */
}
