package com.architexa.diagrams.jdt;

import org.eclipse.jdt.core.IJavaElement;

/**
 * Selectable items that implement this interface will be adaptable to
 * IJavaElement and get those actions in their context menu
 */
public interface IJavaElementContainer {
	public IJavaElement getJaveElement();
}
