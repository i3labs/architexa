/*******************************************************************************
 * Copyright (c) 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package com.architexa.org.eclipse.gef.extensions;

import java.util.Map;

import org.eclipse.jface.action.IMenuManager;

public interface ServerExtensionContextMenuProvider {

	public Map<String, String> getMenuMapToSendToServer(IMenuManager menu);
}
