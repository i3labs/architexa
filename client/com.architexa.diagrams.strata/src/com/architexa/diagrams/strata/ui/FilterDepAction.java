/**
 * Copyright (c) 2007-2008 Architexa, Inc
 */ 
package com.architexa.diagrams.strata.ui;

import java.util.List;

import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;

import com.architexa.org.eclipse.gef.EditPart;
import com.architexa.org.eclipse.gef.commands.Command;
import com.architexa.org.eclipse.gef.commands.CompoundCommand;
import com.architexa.org.eclipse.gef.requests.GroupRequest;

public class FilterDepAction extends CmdBasedSelectionAction {

    public static final String action_Id = "filterDep";
    public static final String label = "Fitler Dependencies";
    public static final String tooltip = "Fitler bependencies etween these modules";
    public static final String requestType = action_Id;
    public static final String commandName = action_Id;

    public FilterDepAction(IWorkbenchPart part) {
		super(part);
	}


    @Override
	public Command createCommand(List<?> objects) {
		if (objects.isEmpty()) return null;
		if (!(objects.get(0) instanceof EditPart)) return null;

		GroupRequest deleteReq = new GroupRequest(requestType);
		deleteReq.setEditParts(objects);

		CompoundCommand compoundCmd = new CompoundCommand(commandName);
		for (int i = 0; i < objects.size(); i++) {
			EditPart object = (EditPart) objects.get(i);
			Command cmd = object.getCommand(deleteReq);
			if (cmd != null) compoundCmd.add(cmd);
		}

		return compoundCmd;
	}

    @Override
	protected void init() {
		super.init();
		setText(label);
		setToolTipText(tooltip);
		setId(action_Id);
		
		// use images of delete for now
		ISharedImages sharedImages = PlatformUI.getWorkbench().getSharedImages();
		setImageDescriptor(sharedImages.getImageDescriptor(ISharedImages.IMG_TOOL_DELETE));
		setDisabledImageDescriptor(sharedImages.getImageDescriptor(
				ISharedImages.IMG_TOOL_DELETE_DISABLED));
		setEnabled(false);
	}
}
