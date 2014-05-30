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
package com.architexa.org.eclipse.gef.ui.actions;

import com.architexa.org.eclipse.gef.internal.GEFMessages;

import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.RetargetAction;


/**
 * @author Eric Bordeau
 * @deprecated	Use org.eclipse.ui.actions.ActionFactory instead
 */
public class CopyRetargetAction extends RetargetAction {

/**
 * Constructs a new CopyRetargetAction with the default ID, label and image.
 */
public CopyRetargetAction() {
	super(ActionFactory.COPY.getId(), GEFMessages.CopyAction_Label);
	ISharedImages sharedImages = PlatformUI.getWorkbench().getSharedImages();
	setImageDescriptor(sharedImages.getImageDescriptor(ISharedImages.IMG_TOOL_COPY));
	setDisabledImageDescriptor(sharedImages.getImageDescriptor(
			ISharedImages.IMG_TOOL_COPY_DISABLED));
}								

}
