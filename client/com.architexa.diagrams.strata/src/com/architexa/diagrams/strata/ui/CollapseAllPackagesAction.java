package com.architexa.diagrams.strata.ui;

import java.util.List;

import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;

import com.architexa.diagrams.strata.model.policy.ClosedContainerDPolicy;
import com.architexa.diagrams.strata.parts.StrataArtFragEditPart;
import com.architexa.diagrams.strata.parts.StrataRootEditPart;
import com.architexa.diagrams.ui.RSEEditorViewCommon;
import com.architexa.diagrams.ui.RSENestedAction;
import com.architexa.org.eclipse.gef.commands.Command;
import com.architexa.org.eclipse.gef.commands.CompoundCommand;
import com.architexa.org.eclipse.gef.ui.actions.SelectionAction;

public class CollapseAllPackagesAction extends SelectionAction implements RSENestedAction{
    public static final String CollapseAllPackagesAction_Id = "collapseAll";
    public static final String CollapseAllPackagesAction_Label = "Collapse All Packages";
    public static final String CollapseAllPackagesAction_Tooltip = "Show only top level packages";
    public static final String CollapseAllPackagesAction_CommandName = "collapseAll";

    public CollapseAllPackagesAction (IWorkbenchPart part) {
		super(part);
	}

	@Override
	protected boolean calculateEnabled() {
		Command cmd = createCollapseAllCommand(getSelectedObjects());
		if (cmd == null)
			return false;
		return cmd.canExecute();
	}


    public Command createCollapseAllCommand(List<?> selected) {
    	CompoundCommand hideAFEPChildrenCmd = new CompoundCommand(CollapseAllPackagesAction_Label);
    	
    	for (StrataArtFragEditPart mySAFEP : getAllArtFragEP()){ 
    		if (ClosedContainerDPolicy.isShowingChildren(mySAFEP.getArtFrag()) && mySAFEP.isActive()){
             	ClosedContainerDPolicy.hideChildren(hideAFEPChildrenCmd, mySAFEP.getArtFrag());
            }	
    	}
    	return hideAFEPChildrenCmd;
	}

    @Override
	public void run() {
		execute(createCollapseAllCommand(getSelectedObjects()));
	}

    protected List<StrataArtFragEditPart> getAllArtFragEP() {
    	return ((StrataRootEditPart) ((RSEEditorViewCommon.IRSEEditorViewCommon) getWorkbenchPart()).getRootController()).getAllArtFragEP();
    }

    @Override
	protected void init() {
		super.init();
		setText(CollapseAllPackagesAction_Label);
		setToolTipText(CollapseAllPackagesAction_Tooltip);
		setId(CollapseAllPackagesAction_Id);
		
		// use images of delete for now
		ISharedImages sharedImages = PlatformUI.getWorkbench().getSharedImages();
		setImageDescriptor(sharedImages.getImageDescriptor(ISharedImages.IMG_OBJ_ELEMENT));
		setDisabledImageDescriptor(sharedImages.getImageDescriptor(
				ISharedImages.IMG_TOOL_DELETE_DISABLED));
		setEnabled(false);
	}
}