package com.architexa.diagrams.relo.ui;

import java.util.List;

import org.eclipse.jface.action.IAction;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;

import com.architexa.diagrams.relo.parts.ReloController;
import com.architexa.diagrams.ui.RSEAction;
import com.architexa.diagrams.utils.RootEditPartUtils;
import com.architexa.org.eclipse.gef.EditPart;
import com.architexa.org.eclipse.gef.Request;
import com.architexa.org.eclipse.gef.RequestConstants;
import com.architexa.org.eclipse.gef.commands.Command;
import com.architexa.org.eclipse.gef.commands.CompoundCommand;

public class RemoveAllAction extends RSEAction {

	@Override
	public void setActiveEditor(IAction action, IEditorPart targetEditor) {
		if (!(RootEditPartUtils.getEditorFromRSEMultiPageEditor(targetEditor) instanceof ReloEditor)) {
			action.setEnabled(false);
			return;
		} else
			action.setEnabled(true);
		super.setActiveEditor(action, targetEditor);
	}
	
	@Override
	public void run(IAction action) {
		((ReloController) rc).execute(getRemoveAllCmd());
	}
	
    public Command getRemoveAllCmd() {
    	List<EditPart> eps = ((ReloController)rc).getChildrenAsTypedList();
    	CompoundCommand cc = new CompoundCommand();
    	for (EditPart part : eps) {
			Command currCmd = part.getCommand(new Request(RequestConstants.REQ_DELETE));
			if (currCmd != null) {
				cc.add(currCmd);
			}
		}
    	return cc;
    }

}
