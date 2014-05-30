/**
 * Copyright (c) 2007-2008 Architexa, Inc
 */ 
package com.architexa.diagrams.strata.ui;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;

import com.architexa.diagrams.jdt.RJCore;
import com.architexa.diagrams.strata.commands.FocusCommand;
import com.architexa.diagrams.strata.model.StrataRootDoc;
import com.architexa.diagrams.strata.parts.ContainableEditPart;
import com.architexa.diagrams.strata.parts.LayerEditPart;
import com.architexa.diagrams.strata.parts.StrataArtFragEditPart;
import com.architexa.diagrams.strata.parts.StrataRootEditPart;
import com.architexa.diagrams.ui.RSEEditorViewCommon;
import com.architexa.org.eclipse.gef.EditPart;
import com.architexa.org.eclipse.gef.RequestConstants;
import com.architexa.org.eclipse.gef.commands.Command;
import com.architexa.org.eclipse.gef.editparts.AbstractConnectionEditPart;
import com.architexa.org.eclipse.gef.requests.GroupRequest;
import com.architexa.org.eclipse.gef.ui.actions.SelectionAction;
import com.architexa.store.ReloRdfRepository;

public class FocusAction extends SelectionAction {

    public static final String FocusAction_Id = "focus";
    public static final String FocusAction_Label = "Focus";
    public static final String FocusAction_Tooltip = "Focus containing module";
    public static final String FocusAction_CommandName = "focus";
	private StrataRootDoc rootAF;

    public FocusAction(IWorkbenchPart part, StrataRootDoc rootContent) {
		super(part);
		rootAF = rootContent;
	}

	@Override
	protected boolean calculateEnabled() {
		Command cmd = createFocusCommand(getSelectedObjects());
		if (cmd == null)
			return false;
		return cmd.canExecute();
	}


    public Command createFocusCommand(List<?> focusObjects) {
        focusObjects = extractSourceAndTargets(focusObjects);
        
        //System.out.println("createFocusCommand:");
        // MODA: let the autoExpander expand, we just delete
		if (focusObjects.isEmpty()) return null;
        
        // focus = open selection + remove invert selection
        List<StrataArtFragEditPart> allObjectEPs = getAllArtFragEP();
        List<StrataArtFragEditPart> selectedEPs = new ArrayList<StrataArtFragEditPart>();
        GroupRequest openReq = new GroupRequest(RequestConstants.REQ_OPEN);
        openReq.setEditParts(focusObjects);
        boolean focusEPExists = false;
        for (Object obj : focusObjects) {
            if (!(obj instanceof EditPart)) continue;
            
            // remove obj and parents from to be Deleted List (allObjectEPs)
            EditPart selectedEP = (EditPart) obj;
            while (selectedEP != null) {
                //System.out.println("notRemoving: " + selectedEP);
                allObjectEPs.remove(selectedEP);
                if (selectedEP instanceof StrataArtFragEditPart)
                	selectedEPs.add((StrataArtFragEditPart) selectedEP);
                selectedEP = selectedEP.getParent();
            }
			// remove Children recursively from to be Deleted List (allObjectEPs)
            recursiveRemoveChildren(allObjectEPs, obj);
            	
            // MODA
            focusEPExists = true;
            //Command cmd = ((EditPart)obj).getCommand(openReq);
            //if (cmd != null) focusCmd.add(cmd);
        }
        
        // MODA
        if (!focusEPExists) return null;
        //if (focusCmd.size() == 0) return null;
        
        // get cmd to remove and save layers 
        FocusCommand focusCmd = new FocusCommand(allObjectEPs, selectedEPs, rootAF);
        
//        GroupRequest deleteReq = new GroupRequest(RequestConstants.REQ_DELETE);
//        deleteReq.setEditParts(allObjectEPs);
//        for (StrataArtFragEditPart doep : allObjectEPs) {
//            //System.out.println("removing: " + doep);
//            Command cmd = doep.getCommand(deleteReq);
//            if (cmd != null) focusCmd.add(cmd);
//        }

		return focusCmd;
	}

	private void recursiveRemoveChildren(List<StrataArtFragEditPart> allObjectEPs, Object obj) {
		if(obj instanceof StrataArtFragEditPart){
			allObjectEPs.remove(obj);	
		}
		for (Object childObj : ((EditPart) obj).getChildren() ){
			recursiveRemoveChildren(allObjectEPs, childObj);
		}	
	}

	private List<?> extractSourceAndTargets(List<?> focusObjects) {
        List<Object> newFocusObjects = new ArrayList<Object> (focusObjects.size());
        for (Object focusObj : focusObjects) {
        	// do not focus if parent is null (FocusObj has been deleted)
        	if (focusObj instanceof ContainableEditPart && ((ContainableEditPart) focusObj).getArtFrag().getParentArt() == null) continue;
        	// do not focus on layers
            if (focusObj instanceof LayerEditPart) continue;
			// or classes
			if (focusObj instanceof ContainableEditPart && ((ContainableEditPart ) focusObj).getArtFrag().getRootArt()!= ((ContainableEditPart ) focusObj).getArtFrag()) {
				ReloRdfRepository repo = ((ContainableEditPart ) focusObj).getRepo();
				if(((ContainableEditPart) focusObj).getArtFrag().getArt().queryWarnedType(repo) != null 
						&& ((ContainableEditPart) focusObj).getArtFrag().getArt().queryWarnedType(repo).equals(RJCore.classType))
					continue;
			}
        	if (focusObj instanceof AbstractConnectionEditPart) {
                newFocusObjects.add(((AbstractConnectionEditPart)focusObj).getSource());
                newFocusObjects.add(((AbstractConnectionEditPart)focusObj).getTarget());
            } else {
                newFocusObjects.add(focusObj);
            }
        }
        return newFocusObjects;
    }

    @Override
	public void run() {
		execute(createFocusCommand(getSelectedObjects()));
	}

   protected List<StrataArtFragEditPart> getAllArtFragEP() {
    	return ((StrataRootEditPart) ((RSEEditorViewCommon.IRSEEditorViewCommon) getWorkbenchPart()).getRootController()).getAllArtFragEP();
    }

    @Override
	protected void init() {
		super.init();
		setText(FocusAction_Label);
		setToolTipText(FocusAction_Tooltip);
		setId(FocusAction_Id);
		
		// use images of delete for now
		ISharedImages sharedImages = PlatformUI.getWorkbench().getSharedImages();
		setImageDescriptor(sharedImages.getImageDescriptor(ISharedImages.IMG_OBJ_ELEMENT));
		setDisabledImageDescriptor(sharedImages.getImageDescriptor(
				ISharedImages.IMG_TOOL_DELETE_DISABLED));
		setEnabled(false);
	}
}
