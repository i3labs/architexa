package com.architexa.diagrams.chrono.ui;

import java.util.List;

import org.eclipse.jface.action.IAction;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;

import com.architexa.diagrams.chrono.editparts.DiagramEditPart;
import com.architexa.diagrams.chrono.models.DiagramModel;
import com.architexa.diagrams.chrono.models.InstanceModel;
import com.architexa.diagrams.chrono.util.InstanceUtil;
import com.architexa.diagrams.ui.RSEAction;
import com.architexa.diagrams.utils.RootEditPartUtils;
import com.architexa.org.eclipse.gef.EditPart;
import com.architexa.org.eclipse.gef.commands.Command;
import com.architexa.org.eclipse.gef.commands.CompoundCommand;

public class RemoveAllAction extends RSEAction {

	@Override
	public void run(IAction action) {
		IEditorPart targetEditor = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
		
		if (!(RootEditPartUtils.getEditorFromRSEMultiPageEditor(targetEditor) instanceof SeqEditor)) {
			action.setEnabled(false);
			return;
		}
		((DiagramEditPart) rc).execute(getRemoveAllCmd());
	}
	
    public Command getRemoveAllCmd() {
    	List<EditPart> eps = rc.getChildren();
    	CompoundCommand cc = new CompoundCommand("Remove All");
    	for (EditPart part : eps) {
    		if (part.getModel() instanceof InstanceModel)
    			InstanceUtil.getInstanceDeleteCommand((DiagramModel)rc.getModel(), (InstanceModel)part.getModel(), rc.getChildren(), cc);
		}
    	return cc;
    }

}
