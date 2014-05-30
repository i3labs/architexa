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
package com.architexa.org.eclipse.gef.ui.palette.customize;

import com.architexa.org.eclipse.gef.palette.PaletteContainer;
import com.architexa.org.eclipse.gef.palette.PaletteEntry;
import com.architexa.org.eclipse.gef.palette.PaletteStack;
import com.architexa.org.eclipse.gef.palette.ToolEntry;
import com.architexa.org.eclipse.gef.ui.palette.PaletteMessages;

import org.eclipse.swt.widgets.Shell;


/**
 * Factory to create {@link com.architexa.org.eclipse.gef.palette.PaletteStack}
 * 
 * @author Whitney Sorenson
 * @since 3.0
 */
public class PaletteStackFactory extends PaletteEntryFactory {

/**
 * Creates a new PaletteStackFactory with label PaletteMessages.MODEL_TYPE_STACK
 */
public PaletteStackFactory() {
	setLabel(PaletteMessages.MODEL_TYPE_STACK);
}

/**
 * @see com.architexa.org.eclipse.gef.ui.palette.customize.PaletteEntryFactory#canCreate(com.architexa.org.eclipse.gef.palette.PaletteEntry)
 */
public boolean canCreate(PaletteEntry selected) {
	if (!(selected instanceof ToolEntry) || selected.getParent() instanceof PaletteStack)
		return false;
	return super.canCreate(selected);
}

/**
 * @see com.architexa.org.eclipse.gef.ui.palette.customize.PaletteEntryFactory#createNewEntry(Shell)
 */
protected PaletteEntry createNewEntry(Shell shell) {
	return new PaletteStack(PaletteMessages.NEW_STACK_LABEL, null, null);
}

/**
 * @see com.architexa.org.eclipse.gef.ui.palette.customize.PaletteEntryFactory#createNewEntry(org.eclipse.swt.widgets.Shell, com.architexa.org.eclipse.gef.palette.PaletteEntry)
 */
public PaletteEntry createNewEntry(Shell shell, PaletteEntry selected) {
	PaletteContainer parent = determineContainerForNewEntry(selected);
	int index = determineIndexForNewEntry(parent, selected);
	PaletteEntry entry = createNewEntry(shell);
	parent.remove(selected);
	parent.add(index - 1, entry);
	((PaletteStack)entry).add(selected);
	entry.setUserModificationPermission(PaletteEntry.PERMISSION_FULL_MODIFICATION);
	return entry;
}

/**
 * @see com.architexa.org.eclipse.gef.ui.palette.customize.PaletteEntryFactory#determineTypeForNewEntry(com.architexa.org.eclipse.gef.palette.PaletteEntry)
 */
protected Object determineTypeForNewEntry(PaletteEntry selected) {
	return PaletteStack.PALETTE_TYPE_STACK;
}

}
