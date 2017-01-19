package org.freejava.tools.handlers.dependency;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.freejava.dependency.builder.Name;
import org.freejava.dependency.graph.Vertex;
import org.freejava.tools.Activator;

public class MyLabelProvider extends LabelProvider {

    public Image getImage(Object element) {

        Image image = null;
        if (element instanceof Vertex) {
            @SuppressWarnings("unchecked")
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
            @SuppressWarnings("unchecked")
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