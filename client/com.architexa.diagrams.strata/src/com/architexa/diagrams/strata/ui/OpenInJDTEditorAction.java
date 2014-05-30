/**
 * Copyright (c) 2007-2008 Architexa, Inc
 */ 
package com.architexa.diagrams.strata.ui;

import org.eclipse.ui.IWorkbenchPart;

import com.architexa.diagrams.jdt.model.CodeUnit;
import com.architexa.diagrams.jdt.model.UserCreatedFragment;
import com.architexa.diagrams.jdt.utils.JDTUISupport;
import com.architexa.diagrams.strata.parts.StrataArtFragEditPart;
import com.architexa.diagrams.strata.parts.StrataRootEditPart;
import com.architexa.diagrams.ui.RSEContextMenuEntry;
import com.architexa.diagrams.ui.RSEContextMenuProvider;
import com.architexa.org.eclipse.gef.EditPart;
import com.architexa.org.eclipse.gef.ui.actions.SelectionAction;
import com.architexa.rse.RSE;
import com.architexa.store.ReloRdfRepository;


public class OpenInJDTEditorAction extends SelectionAction implements RSEContextMenuEntry {

    public static final String action_Id = "openInEditor";
    public static final String requestType = action_Id;
    public static final String commandName = action_Id;

    public OpenInJDTEditorAction(IWorkbenchPart part) {
		super(part);
	}

	@Override
	protected boolean calculateEnabled() {
		if (getSelectedObjects().size() == 1) {
			Object selObj = getSelectedObjects().get(0);
			if (selObj instanceof StrataRootEditPart)
				return false;
			if (selObj instanceof StrataArtFragEditPart && 
					!(((StrataArtFragEditPart) selObj).getModel() instanceof UserCreatedFragment)) {
				StrataArtFragEditPart afep = (StrataArtFragEditPart) selObj;
				ReloRdfRepository repo = afep.getRepo();
				return CodeUnit.isType(repo, afep.getArtFrag());
			}
		}
		return false;
	}

	@Override
	public void run() {
		EditPart selObj = (StrataArtFragEditPart) getSelectedObjects().get(0);
		StrataArtFragEditPart afep = (StrataArtFragEditPart) selObj;
		CodeUnit selCU = new CodeUnit(afep.getArtFrag().getArt());
		JDTUISupport.openInEditor(selCU, afep.getRepo());
	}

    
    @Override
	protected void init() {
		super.init();
		setText(JDTUISupport.getOpenInJavaEditorActionString());
		setImageDescriptor(JDTUISupport.getOpenInJavaEditorActionIcon());
		setToolTipText(JDTUISupport.getOpenInJavaEditorActionString());
		setId(action_Id);
		
		/*try {
	        URL editURL = StrataPlugin.getDefault().getBundle().getEntry("icons/codeEdit.gif");
	        ImageDescriptor editID = ImageDescriptor.createFromURL(editURL);
	        setImageDescriptor(editID);
	        
	        URL editDisURL = StrataPlugin.getDefault().getBundle().getEntry("icons/codeEdit_dis.gif");
	        ImageDescriptor editDisID = ImageDescriptor.createFromURL(editDisURL);
	        setImageDescriptor(editDisID);
		} catch (Throwable t) {
			t.printStackTrace();
		}*/
		setEnabled(false);
	}

    public String getContextMenuGroup() {
    	return RSEContextMenuProvider.GROUP_RSE_EDITORS;
    }
}
