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
package com.architexa.org.eclipse.gef.internal.ui.palette;

import com.architexa.org.eclipse.gef.tools.SelectionTool;
import com.architexa.org.eclipse.gef.ui.palette.PaletteViewer;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;


/**
 * Selection Tool to be used in the Palette.
 */
public class PaletteSelectionTool 
	extends SelectionTool
{

private PaletteViewer getPaletteViewer() {
	return (PaletteViewer)getCurrentViewer();
}

private boolean handleAbort(KeyEvent e) {
	if (e.keyCode == SWT.ESC) {
		return (getPaletteViewer().getPaletteRoot().getDefaultEntry() != null);
	}
	return false;
}

protected boolean handleKeyDown(KeyEvent e) {
	if (handleAbort(e)) {
		loadDefaultTool();
		return true;
	}
	return super.handleKeyDown(e);
}

private void loadDefaultTool() {
	getPaletteViewer().setActiveTool(getPaletteViewer().getPaletteRoot().getDefaultEntry());
}

}
