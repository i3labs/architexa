/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package com.architexa.org.eclipse.gef.ui.actions;

import com.architexa.org.eclipse.gef.GraphicalViewer;
import com.architexa.org.eclipse.gef.SnapToGeometry;
import com.architexa.org.eclipse.gef.internal.GEFMessages;

import org.eclipse.jface.action.Action;


/**
 * An action that toggles the {@link com.architexa.org.eclipse.gef.SnapToGeometry#PROPERTY_SNAP_ENABLED
 * snap to geometry} property on the given viewer.  This action can handle the case where
 * that property is not set on the viewer initially.
 * 
 * @author Pratik Shah
 * @since 3.0
 */
public class ToggleSnapToGeometryAction 
	extends Action 
{
	
private GraphicalViewer diagramViewer;

/**
 * Constructor
 * @param	diagramViewer	the GraphicalViewer whose snap to geometry property is to be
 * 							toggled
 */
public ToggleSnapToGeometryAction(GraphicalViewer diagramViewer) {
	super(GEFMessages.ToggleSnapToGeometry_Label, AS_CHECK_BOX);
	this.diagramViewer = diagramViewer;
	setToolTipText(GEFMessages.ToggleSnapToGeometry_Tooltip);
	setId(GEFActionConstants.TOGGLE_SNAP_TO_GEOMETRY);
	setActionDefinitionId(GEFActionConstants.TOGGLE_SNAP_TO_GEOMETRY);
	setChecked(isChecked());
}

/**
 * @see org.eclipse.jface.action.IAction#isChecked()
 */
public boolean isChecked() {
	Boolean val = (Boolean)diagramViewer.getProperty(SnapToGeometry.PROPERTY_SNAP_ENABLED);
	if (val != null)
		return val.booleanValue();
	return false;
}

/**
 * @see org.eclipse.jface.action.IAction#run()
 */
public void run() {
	diagramViewer.setProperty(SnapToGeometry.PROPERTY_SNAP_ENABLED, 
			new Boolean(!isChecked()));
}

}
