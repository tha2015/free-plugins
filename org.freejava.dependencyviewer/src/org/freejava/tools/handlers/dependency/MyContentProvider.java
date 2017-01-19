package org.freejava.tools.handlers.dependency;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.zest.core.viewers.IGraphContentProvider;
import org.freejava.dependency.builder.Name;
import org.freejava.dependency.graph.Edge;
import org.freejava.dependency.graph.Graph;

public class MyContentProvider implements IGraphContentProvider {

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