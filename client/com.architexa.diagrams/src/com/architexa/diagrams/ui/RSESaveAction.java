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
package com.architexa.diagrams.ui;

import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.actions.ActionFactory;

import com.architexa.compat.CompatUtils;
import com.architexa.diagrams.ui.RSEEditorViewCommon.IRSEEditorViewCommon;
import com.architexa.org.eclipse.gef.internal.GEFMessages;
import com.architexa.org.eclipse.gef.ui.actions.WorkbenchPartAction;

/**
 * An action to save the editor's current state.
 */
public class RSESaveAction
	extends WorkbenchPartAction
{

/**
 * Constructs a <code>SaveAction</code> and associates it with the given editor.
 * @param editor the IEditorPart
 */
public RSESaveAction(IWorkbenchPart iWorkbenchPart) {
	super(iWorkbenchPart);
	setLazyEnablementCalculation(false);
}

/**
 * @see com.architexa.org.eclipse.gef.ui.actions.WorkbenchPartAction#calculateEnabled()
 */
@Override
protected boolean calculateEnabled() {
	return ((IRSEEditorViewCommon)getWorkbenchPart()).isDirty();
}

/**
 * Initializes this action's text.
 */
@Override
protected void init() {
	setId(ActionFactory.SAVE.getId());
	setText(GEFMessages.SaveAction_Label);
	setToolTipText(GEFMessages.SaveAction_Tooltip);
}

/**
 * Saves the state of the associated editor.
 */
@Override
public void run() {
	CompatUtils.rseSaveAction(getWorkbenchPart());
}

}
