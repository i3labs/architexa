/**
 * Copyright (c) 2007-2008 Architexa, Inc
 */ 
package com.architexa.diagrams.strata.ui;

import java.util.List;

import org.eclipse.ui.IWorkbenchPart;

import com.architexa.diagrams.strata.StrataPlugin;
import com.architexa.diagrams.strata.parts.CompositeLayerEditPart;
import com.architexa.org.eclipse.gef.EditPart;
import com.architexa.org.eclipse.gef.commands.Command;
import com.architexa.org.eclipse.gef.commands.CompoundCommand;
import com.architexa.org.eclipse.gef.requests.GroupRequest;

public class BreakAction extends CommandStackListeningSelectionAction {

    public static final String BreakAction_Id = "break";
    public static final String BreakRequest = "break";
    public static final String BreakAction_Label = "Break";
    public static final String BreakAction_Tooltip = "Break containing module";
    public static final String CommandName = "Break Module";

    public BreakAction(IWorkbenchPart part) {
		super(part);
	}

	@Override
	protected boolean calculateEnabled() {
		Command cmd = createBreakCommand(getSelectedObjects());
		if (cmd == null)
			return false;
		return cmd.canExecute();
	}


    public Command createBreakCommand(List<?> objects) {
		if (objects.isEmpty()) return null;
		if (!(objects.get(0) instanceof EditPart)) return null;

		// Do not allow breaking for items in a composite layer 
		EditPart ep = (EditPart) objects.get(0);
		EditPart parentEP = ep.getParent();
		if (parentEP!= null && parentEP.getParent()!=null) {
			if (parentEP.getParent() instanceof CompositeLayerEditPart) return null;
		}
		
		GroupRequest breakReq = new GroupRequest(BreakRequest);
		breakReq.setEditParts(objects);

		CompoundCommand compoundCmd = new CompoundCommand(CommandName);
		for (int i = 0; i < objects.size(); i++) {
			EditPart object = (EditPart) objects.get(i);
			Command cmd = object.getCommand(breakReq);
			if (cmd != null) 
				compoundCmd.add(cmd);
		}

		return compoundCmd;
	}

	@Override
	public void run() {
		execute(createBreakCommand(getSelectedObjects()));
	}

    @Override
	protected void init() {
		super.init();
		setText(BreakAction_Label);
		setToolTipText(BreakAction_Tooltip);
		setId(BreakAction_Id);
		
		// use images of delete for now
//		ISharedImages sharedImages = PlatformUI.getWorkbench().getSharedImages();
//		setImageDescriptor(sharedImages.getImageDescriptor(ISharedImages.IMG_TOOL_DELETE));
		setImageDescriptor(StrataPlugin.getImageDescriptor("icons/break-icon.png"));
//		setDisabledImageDescriptor(sharedImages.getImageDescriptor(
//				ISharedImages.IMG_TOOL_DELETE_DISABLED));
		setEnabled(false);
	}
}
