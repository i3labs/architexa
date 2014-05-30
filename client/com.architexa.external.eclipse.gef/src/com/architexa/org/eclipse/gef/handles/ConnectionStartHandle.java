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

import com.architexa.org.eclipse.draw2d.ConnectionLocator;
import com.architexa.org.eclipse.gef.ConnectionEditPart;
import com.architexa.org.eclipse.gef.DragTracker;
import com.architexa.org.eclipse.gef.RequestConstants;
import com.architexa.org.eclipse.gef.tools.ConnectionEndpointTracker;



/**
 * A handle used at the start of the {@link com.architexa.org.eclipse.draw2d.Connection}.  
 * This is treated differently than the end of the Connection.
 */
public final class ConnectionStartHandle
	extends ConnectionHandle
{
	
/**
 * Creates a new ConnectionStartHandle, sets its owner to <code>owner</code>,
 * and sets its locator to a {@link ConnectionLocator}.
 * @param owner the ConnectionEditPart owner
 */
public ConnectionStartHandle(ConnectionEditPart owner) {
	setOwner(owner);
	setLocator(new ConnectionLocator(getConnection(), ConnectionLocator.SOURCE));
}

/**
 * Creates a new ConnectionStartHandle and sets its owner to <code>owner</code>.
 * If the handle is fixed, it cannot be dragged.
 * @param owner the ConnectionEditPart owner
 * @param fixed if true, handle cannot be dragged.
 */
public ConnectionStartHandle(ConnectionEditPart owner, boolean fixed) {
	super(fixed);
	setOwner(owner);
	setLocator(new ConnectionLocator(getConnection(), ConnectionLocator.SOURCE));
}

/**
 * Creates a new ConnectionStartHandle.
 */
public ConnectionStartHandle() { }

/**
 * Creates and returns a new {@link ConnectionEndpointTracker}.
 * @return the new ConnectionEndpointTracker
 */
protected DragTracker createDragTracker() {
	if (isFixed()) 
		return null;
	ConnectionEndpointTracker tracker;
	tracker = new ConnectionEndpointTracker((ConnectionEditPart)getOwner());
	tracker.setCommandName(RequestConstants.REQ_RECONNECT_SOURCE);
	tracker.setDefaultCursor(getCursor());
	return tracker;
}

}
