package com.architexa.diagrams.ui;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

public class RelatedDiagramsProvider implements ITreeContentProvider {

	public static final Object[] EMPTY_ARRAY = new Object[0];

	public void dispose() {}
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {}

	public Object[] getChildren(Object parentElement) {
		return EMPTY_ARRAY;
	}

	public Object getParent(Object element) {
		return null;
	}

	public boolean hasChildren(Object element) {
		return false;
	}

	public Object[] getElements(Object inputElement) {
		return EMPTY_ARRAY;
	}

}
