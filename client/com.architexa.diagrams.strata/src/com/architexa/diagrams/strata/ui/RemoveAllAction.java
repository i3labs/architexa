package com.architexa.diagrams.strata.ui;

import java.util.List;

import org.eclipse.jface.action.IAction;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;

import com.architexa.diagrams.strata.parts.StrataArtFragEditPart;
import com.architexa.diagrams.strata.parts.StrataRootEditPart;
import com.architexa.diagrams.ui.RSEAction;
import com.architexa.diagrams.utils.RootEditPartUtils;
import com.architexa.org.eclipse.gef.EditPart;
import com.architexa.org.eclipse.gef.Request;
import com.architexa.org.eclipse.gef.RequestConstants;
import com.architexa.org.eclipse.gef.commands.Command;
import com.architexa.org.eclipse.gef.commands.CompoundCommand;

public class RemoveAllAction extends RSEAction {

	@Override
	public void run(IAction action) {
		IWorkbenchPart targetPart = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActivePart();
		if (targetPart instanceof IEditorPart && !(RootEditPartUtils.getEditorFromRSEMultiPageEditor(targetPart) instanceof StrataEditor)) {
			action.setEnabled(false);
			return;
		}
//		if (targetPart instanceof IViewPart &&  targetPart instanceof StrataView) {
//			action.setEnabled(true);
//			return;
//		}
		((StrataRootEditPart) rc).execute(getRemoveAllCmd());
	}
	
    public Command getRemoveAllCmd() {
    	List<StrataArtFragEditPart> eps = ((StrataRootEditPart) rc).getAllArtFragEP();
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
