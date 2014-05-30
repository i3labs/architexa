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
package com.architexa.org.eclipse.gef.handles;

import com.architexa.org.eclipse.draw2d.BendpointLocator;
import com.architexa.org.eclipse.draw2d.Locator;
import com.architexa.org.eclipse.gef.ConnectionEditPart;
import com.architexa.org.eclipse.gef.DragTracker;
import com.architexa.org.eclipse.gef.RequestConstants;
import com.architexa.org.eclipse.gef.SharedCursors;
import com.architexa.org.eclipse.gef.tools.ConnectionBendpointTracker;



/**
 * A BendpointHandle that is used to move an existing bendpoint.
 */
public class BendpointMoveHandle extends BendpointHandle {

{
	setCursor(SharedCursors.SIZEALL);
}

/**
 * Creates a new BendpointMoveHandle.
 */
public BendpointMoveHandle() { }

/**
 * Creates a new BendpointMoveHandle, sets its owner to <code>owner</code>
 * and its index to <code>index</code>, and sets its locator to a new
 * {@link BendpointLocator}.
 * @param owner the ConnectionEditPart owner
 * @param index the index
 */
public BendpointMoveHandle(ConnectionEditPart owner, int index) {
	setOwner(owner);
	setIndex(index);
	setLocator(new BendpointLocator(getConnection(), index + 1));
}

/**
 * Creates a new BendpointMoveHandle, sets its owner to <code>owner</code>
 * and its index to <code>index</code>, and sets its locator to a new
 * {@link BendpointLocator} with the given <code>locatorIndex</code>.
 * @param owner the ConnectionEditPart owner
 * @param index the index
 * @param locatorIndex the index to use for the locator
 */
public BendpointMoveHandle(ConnectionEditPart owner, int index, int locatorIndex) {
	setOwner(owner);
	setIndex(index);
	setLocator(new BendpointLocator(getConnection(), locatorIndex));
}

/**
 * Creates a new BendpointMoveHandle and sets its owner to <code>owner</code>,
 * sets its index to <code>index</code>, and sets its locator to 
 * <code>locator</code>.
 * @param owner the ConnectionEditPart owner
 * @param index the index
 * @param locator the Locator
 */
public BendpointMoveHandle(ConnectionEditPart owner, int index, Locator locator) {
	setOwner(owner);
	setIndex(index);
	setLocator(locator);
}

/**
 * Creates and returns a new {@link ConnectionBendpointTracker}.
 * @return the new ConnectionBendpointTracker
 */
protected DragTracker createDragTracker() {
	ConnectionBendpointTracker tracker;
	tracker = new ConnectionBendpointTracker(
		(ConnectionEditPart)getOwner(),
		getIndex());
	tracker.setType(RequestConstants.REQ_MOVE_BENDPOINT);
	tracker.setDefaultCursor(getCursor());
	return tracker;
}

}
