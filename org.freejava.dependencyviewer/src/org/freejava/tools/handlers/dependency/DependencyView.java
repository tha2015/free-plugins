package org.freejava.tools.handlers.dependency;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.zest.core.viewers.GraphViewer;
import org.eclipse.zest.core.viewers.IGraphContentProvider;
import org.eclipse.zest.core.widgets.ZestStyles;
import org.eclipse.zest.layouts.LayoutStyles;
import org.eclipse.zest.layouts.algorithms.SpringLayoutAlgorithm;
import org.freejava.dependency.builder.Name;
import org.freejava.dependency.builder.impl.RemoveNodesGraphTransformerImpl;
import org.freejava.dependency.graph.Edge;
import org.freejava.dependency.graph.Graph;
import org.freejava.dependency.graph.Vertex;
import org.freejava.tools.Activator;



public class DependencyView extends ViewPart {

    public static final String ID = "org.freejava.tools.dependency";
    protected GraphViewer viewer;

    static class MyContentProvider implements IGraphContentProvider {

        public Object getSource(Object rel) {
            Object[] obj = (Object[]) rel;
            return obj[0];
        }

        @SuppressWarnings("unchecked")
        public Object[] getElements(Object input) {
            Object[] result;
            if (input != null && input instanceof Graph) {
                result = getDependencyArrows((Graph<Name>) input);
            } else {
                result = new Object[0];
            }
            return result;
        }

        private Object[] getDependencyArrows(Graph<Name> g) {

            List<Object[]> arrows = new ArrayList<Object[]>();
            for (Edge<Name> e : g.getEdges()) {
                arrows.add(new Object[] {e.getFrom(), e.getTo()});
            }
            return arrows.toArray();
        }

        public Object getDestination(Object rel) {
            Object[] obj = (Object[]) rel;
            return obj[1];
        }

        public double getWeight(Object connection) {
            return 0;
        }

        public void dispose() {
        }

        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        }

    }

    static class MyLabelProvider extends LabelProvider {
        DependencyView dependencyView;
        public MyLabelProvider(DependencyView dependencyView) {
            this.dependencyView = dependencyView;
        }

        public Image getImage(Object element) {

            Image image = null;
            if (element instanceof Vertex) {
                Vertex<Name> node = (Vertex<Name>) element;
                if (node.getNode().isClass()) {
                    image = Activator.getDefault().getImageRegistry().get("class");
                } else if (node.getNode().isInterface()) {
                        image = Activator.getDefault().getImageRegistry().get("interface");
                } else {
                    image = Activator.getDefault().getImageRegistry().get("package");
                }
            }
            return image;
        }

        public String getText(Object element) {
            String name = null;
            if (element instanceof Vertex) {
                Vertex<Name> node = (Vertex<Name>) element;
                if (node.getNode().getName() == null || node.getNode().getName().equals("")) {
                    name = "<default>";
                } else {
                    name = node.getNode().getName();
                }
            }
            return name;
        }

    }

    @Override
    public void createPartControl(Composite parent) {
        viewer = new GraphViewer( parent, SWT.NONE );
        viewer.setConnectionStyle(ZestStyles.CONNECTIONS_DIRECTED);
        viewer.setContentProvider( new MyContentProvider() );
        viewer.setLabelProvider( new MyLabelProvider(this) );
//        viewer.setLayoutAlgorithm( new TreeLayoutAlgorithm(
//            LayoutStyles.NO_LAYOUT_NODE_RESIZING ) );
        viewer.setLayoutAlgorithm( new SpringLayoutAlgorithm(
                LayoutStyles.NO_LAYOUT_NODE_RESIZING ) );
//        viewer.setLayoutAlgorithm(new CompositeLayoutAlgorithm(LayoutStyles.NO_LAYOUT_NODE_RESIZING, new LayoutAlgorithm[] { new DirectedGraphLayoutAlgorithm(LayoutStyles.NO_LAYOUT_NODE_RESIZING), new HorizontalShift(LayoutStyles.NO_LAYOUT_NODE_RESIZING) }));

        viewer.setInput(new Object());
        viewer.getGraphControl().addKeyListener(new KeyListener() {
            @SuppressWarnings("unchecked")
            public void keyPressed(KeyEvent e) {

                Object input = viewer.getInput();
                if (input == null || !(input instanceof Graph)) return;

                Graph<Name> pkgs = (Graph<Name>) input;

                if (e.keyCode == SWT.DEL) {
                    ISelection selection = viewer.getSelection();
                    List selectedItems = ((IStructuredSelection) selection).toList();
                    List<Vertex> selectedPackages = new ArrayList<Vertex>();
                    for (Object selectedItem : selectedItems) {
                        if (selectedItem instanceof Vertex) {
                            selectedPackages.add((Vertex) selectedItem);
                        } else {
                            Object[] selectedArrow = (Object[]) selectedItem;
                            Vertex pkg1 = (Vertex) selectedArrow[0];
                            Vertex pkg2 = (Vertex) selectedArrow[1];
                            selectedPackages.add(pkg1);
                            selectedPackages.add(pkg2);
                        }
                    }

                    pkgs = new RemoveNodesGraphTransformerImpl(selectedPackages).transform(pkgs);

                    viewer.setInput(pkgs);
                }
            }

            public void keyReleased(KeyEvent e) {
                // TODO Auto-generated method stub

            }});
    }

    @Override
    public void setFocus() {

    }

    public void setDependencyInfo(Graph<Name> g) {
        viewer.setInput(g);
        viewer.refresh();

//		graph = new Graph(parent, SWT.NONE);
//		graph.setConnectionStyle(ZestStyles.CONNECTIONS_DIRECTED);
//
//		GraphNode n1 = new GraphNode(graph, SWT.NONE);
//		n1.setText("Node 1");
//
//		GraphNode n2 = new GraphNode(graph, SWT.NONE);
//		n2.setText("Node 2");
//
//		GraphConnection con1 = new GraphConnection(graph, SWT.NONE, n1, n2);
//
//		graph.setLayoutAlgorithm(new SpringLayoutAlgorithm(LayoutStyles.NO_LAYOUT_NODE_RESIZING), true);

    }

}
