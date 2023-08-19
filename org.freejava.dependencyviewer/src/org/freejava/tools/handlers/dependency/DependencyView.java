package org.freejava.tools.handlers.dependency;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.zest.core.viewers.GraphViewer;
import org.eclipse.zest.core.widgets.ZestStyles;
import org.eclipse.zest.layouts.LayoutStyles;
import org.eclipse.zest.layouts.algorithms.SpringLayoutAlgorithm;
import org.freejava.dependency.graphtransformer.impl.RemoveNodesGraphTransformerImpl;
import org.freejava.dependency.model.Edge;
import org.freejava.dependency.model.Graph;
import org.freejava.dependency.model.Name;
import org.freejava.dependency.model.Vertex;



public class DependencyView extends ViewPart {

    public static final String ID = "org.freejava.tools.dependency";
    protected GraphViewer viewer;

    @Override
    public void createPartControl(Composite parent) {
        viewer = new GraphViewer( parent, SWT.NONE );
        viewer.setConnectionStyle(ZestStyles.CONNECTIONS_DIRECTED);
        viewer.setContentProvider( new MyContentProvider() );
        viewer.setLabelProvider( new MyLabelProvider() );
//        viewer.setLayoutAlgorithm( new TreeLayoutAlgorithm(LayoutStyles.NO_LAYOUT_NODE_RESIZING ) );
        viewer.setLayoutAlgorithm( new SpringLayoutAlgorithm(LayoutStyles.NO_LAYOUT_NODE_RESIZING ) );
//        viewer.setLayoutAlgorithm(new CompositeLayoutAlgorithm(LayoutStyles.NO_LAYOUT_NODE_RESIZING, new LayoutAlgorithm[] { new DirectedGraphLayoutAlgorithm(LayoutStyles.NO_LAYOUT_NODE_RESIZING), new HorizontalShift(LayoutStyles.NO_LAYOUT_NODE_RESIZING) }));

        viewer.setInput(new Object());
        viewer.getGraphControl().addKeyListener(new KeyListener() {
            @SuppressWarnings("unchecked")
            public void keyPressed(KeyEvent e) {

                Object input = viewer.getInput();
                if (input == null || !(input instanceof Graph)) return;


                if (e.keyCode == SWT.DEL || e.keyCode == SWT.BS) {
                    ISelection selection = viewer.getSelection();
                    List<Object> selectedItems = ((IStructuredSelection) selection).toList();
                    List<Vertex<Name>> deletedNodes = new ArrayList<Vertex<Name>>();
                    List<Edge<Name>> selectedEdges = new ArrayList<Edge<Name>>();
                    for (Object selectedItem : selectedItems) {
                        if (selectedItem instanceof Vertex) {
                        	deletedNodes.add((Vertex<Name>) selectedItem);
                        } else {
                            Object[] selectedArrow = (Object[]) selectedItem;
                        	selectedEdges.add(new Edge<Name>((Vertex<Name>) selectedArrow[0], (Vertex<Name>) selectedArrow[1]));
                        }
                    }

                    Graph<Name> pkgs = new RemoveNodesGraphTransformerImpl<Name>(deletedNodes, selectedEdges).transform((Graph<Name>) input);

                    setDependencyInfo(pkgs);
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
    }

}
