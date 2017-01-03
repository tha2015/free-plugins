package org.freejava.tools.handlers.dependency;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

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
import org.freejava.tools.Activator;
import org.freejava.tools.handlers.DependencyFinder;

import com.jeantessier.dependency.ClassNode;
import com.jeantessier.dependency.Node;
import com.jeantessier.dependency.PackageNode;


public class DependencyView extends ViewPart {

    public static final String ID = "org.freejava.tools.dependency";
    protected GraphViewer viewer;
    protected Set<String> interfaces;

    static class MyContentProvider implements IGraphContentProvider {

        public Object getSource(Object rel) {
            Object[] obj = (Object[]) rel;
            return obj[0];
        }

        @SuppressWarnings("unchecked")
        public Object[] getElements(Object input) {
            Object[] result;
            if (input != null && input instanceof Collection) {
                result = getDependencyArrows((Collection<Node>) input);
            } else {
                result = new Object[0];
            }
            return result;
        }

        private Object[] getDependencyArrows(Collection<Node> input) {
            List<Object[]> arrows = new DependencyFinder().getDependencyEdges(input);
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
            if (element instanceof Node) {
                Node node = (Node) element;
                if (node instanceof ClassNode) {
                    if (dependencyView.getInterfaces() != null && dependencyView.getInterfaces().contains(node.getName())) {
                        image = Activator.getDefault().getImageRegistry().get("interface");
                    } else {
                        image = Activator.getDefault().getImageRegistry().get("class");
                    }
                } else if (node instanceof PackageNode) {
                    image = Activator.getDefault().getImageRegistry().get("package");
                }
            }
            return image;
        }

        public String getText(Object element) {
            String name = null;
            if (element instanceof Node) {
                Node node = (Node) element;
                if (node.getName() == null || node.getName().equals("")) {
                    name = "<default>";
                } else {
                    name = node.getName();
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
                if (input == null || !(input instanceof Collection)) return;

                Collection<Node> pkgs = (Collection<Node>) input;

                if (e.keyCode == SWT.DEL) {
                    ISelection selection = viewer.getSelection();
                    List selectedItems = ((IStructuredSelection) selection).toList();
                    List<Node> selectedPackages = new ArrayList<Node>();
                    for (Object selectedItem : selectedItems) {
                        if (selectedItem instanceof Node) {
                            selectedPackages.add((Node) selectedItem);
                        } else {
                            Object[] selectedArrow = (Object[]) selectedItem;
                            Node pkg1 = (Node) selectedArrow[0];
                            Node pkg2 = (Node) selectedArrow[1];
                            pkg1.removeDependency(pkg2);
                        }
                    }
                    for (Node pkg : pkgs) {
                        pkg.removeDependencies(selectedPackages);
                    }
                    pkgs = new ArrayList<Node>(pkgs);
                    pkgs.removeAll(selectedPackages);
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

    public Set<String> getInterfaces() {
        return interfaces;
    }


    public void setDependencyInfo(Collection<Node> pkgs, Set<String> interfaces) {
        this.interfaces = interfaces;
        viewer.setInput(pkgs);
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
