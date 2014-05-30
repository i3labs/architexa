/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package com.architexa.org.eclipse.draw2d.text;

import com.architexa.org.eclipse.draw2d.IFigure;
import com.architexa.org.eclipse.draw2d.LayoutManager;
import com.architexa.org.eclipse.draw2d.geometry.Dimension;

/**
 * A LayoutManager for use with FlowFigure.
 * 
 * <P>WARNING: This class is not intended to be subclassed by clients.
 * @author hudsonr
 * @since 2.1
 */
public abstract class FlowFigureLayout
	implements LayoutManager
{

/**
 * The flow context in which this LayoutManager exists.
 */
private FlowContext context;

/**
 * The figure passed by layout(Figure) is held for convenience.
 */
private final FlowFigure flowFigure;

/**
 * Constructs a new FlowFigureLayout with the given FlowFigure.
 * @param flowfigure the FlowFigure
 */
protected FlowFigureLayout(FlowFigure flowfigure) {
	this.flowFigure = flowfigure;
}

/**
 * Not applicable.
 * @see com.architexa.org.eclipse.draw2d.LayoutManager#getConstraint(com.architexa.org.eclipse.draw2d.IFigure)
 */
public Object getConstraint(IFigure child) {
	return null;
}

/**
 * Returns this layout's context or <code>null</code>.
 * @return <code>null</code> or a context
 * @since 3.1
 */
protected FlowContext getContext() {
	return context;
}

/**
 * @return the FlowFigure
 */
protected FlowFigure getFlowFigure() {
	return flowFigure;
}

/**
 * Not applicable.
 * @see com.architexa.org.eclipse.draw2d.LayoutManager#getMinimumSize(com.architexa.org.eclipse.draw2d.IFigure, int, int)
 */
public Dimension getMinimumSize(IFigure container, int wHint, int hHint) {
	return null;
}

/**
 * Not applicable.
 * @see com.architexa.org.eclipse.draw2d.LayoutManager#getPreferredSize(com.architexa.org.eclipse.draw2d.IFigure, int, int)
 */
public Dimension getPreferredSize(IFigure container, int wHint, int hHint) {
	return null;
}

/**
 * Not applicable. 
 * @see com.architexa.org.eclipse.draw2d.LayoutManager#invalidate()
 */
public void invalidate() { }

/**
 * Called during {@link #layout(IFigure)}.
 */
protected abstract void layout();

/**
 * @see com.architexa.org.eclipse.draw2d.LayoutManager#layout(IFigure)
 */
public final void layout(IFigure figure) {
	layout ();
}

/**
 * Not applicable.
 * @see com.architexa.org.eclipse.draw2d.LayoutManager#remove(com.architexa.org.eclipse.draw2d.IFigure)
 */
public void remove(IFigure child) { }

/**
 * Not applicable.
 * @see com.architexa.org.eclipse.draw2d.LayoutManager#setConstraint(com.architexa.org.eclipse.draw2d.IFigure, java.lang.Object)
 */
public void setConstraint(IFigure child, Object constraint) { }

/**
 * Sets the context for this layout manager.
 * @param flowContext the context of this layout
 */
public void setFlowContext(FlowContext flowContext) {
	context = flowContext;
}

}
