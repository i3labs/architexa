package com.architexa.diagrams.generate.team;

import java.util.List;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

public class TreeContentProvider implements ITreeContentProvider {

	public Object[] getChildren(Object parentElement) {
		return ((TreeElement) parentElement).getChildren().toArray();
	}

	public Object getParent(Object element) {
		return ((TreeElement) element).getParentElement();
	}

	public boolean hasChildren(Object element) {
		TreeElement e = (TreeElement) element;
		if (e.getChildren() != null && e.getChildren().size() > 0) {
			return true;
		}
		return false;
	}

	public Object[] getElements(Object inputElement) {
		Object[] objs = new Object[0];
		if (inputElement instanceof java.util.List) {
			List li = (List) inputElement;
			objs = li.toArray();
		}
		return objs;
	}

	public void dispose() {
	}

	public void inputChanged(Viewer arg0, Object arg1, Object arg2) {
	}

}
