package com.architexa.diagrams.strata.ui;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;

import com.architexa.diagrams.model.Artifact;
import com.architexa.diagrams.model.ArtifactFragment;
import com.architexa.diagrams.strata.model.StrataRootDoc;
import com.architexa.diagrams.strata.model.policy.ClosedContainerDPolicy;
import com.architexa.diagrams.strata.parts.StrataRootEditPart;
import com.architexa.diagrams.ui.RSEEditorViewCommon;
import com.architexa.diagrams.ui.RSENestedAction;
import com.architexa.org.eclipse.gef.commands.Command;
import com.architexa.org.eclipse.gef.commands.CompoundCommand;
import com.architexa.org.eclipse.gef.ui.actions.SelectionAction;

public class ReduceAction extends SelectionAction implements RSENestedAction{

    public static final String ReduceAction_Id = "reduce";
    public static final String ReduceAction_Label = "Reduce Diagram";
    public static final String ReduceAction_Tooltip = "Collapse large packages and break packages with only one child";
    public static final String ReduceAction_CommandName = "reduce";
	private StrataRootDoc rootDoc;
	private StrataRootEditPart rc;
	private List<Artifact> artsAdded = new ArrayList<Artifact>();
	private int nestLvl = 0;
	private int maxNestLvl = 0;

    public ReduceAction(IWorkbenchPart part) {
		super(part);
    }

	@Override
	protected boolean calculateEnabled() {
		Command cmd = createReduceCommand(getSelectedObjects());
		artsAdded.clear();
		if (cmd == null)
			return false;
		return cmd.canExecute();
	}

	private Command createReduceCommand(List<?> selectedObjects) {
		CompoundCommand compoundCmd = new CompoundCommand(ReduceAction_Label);
		this.rc = getRootController();
		this.rootDoc = rc.getRootModel();
		
		List<ArtifactFragment> artFragsToCollapse = new ArrayList<ArtifactFragment>();
		nestLvl = 0;
		maxNestLvl = 0;
		getLowestChildToCollapse(rootDoc, artFragsToCollapse);
		
		for (ArtifactFragment afToCollapse : artFragsToCollapse) {
    		ClosedContainerDPolicy.hideChildren(compoundCmd, afToCollapse);
		}
		
		return compoundCmd;
	}
		
    private void getLowestChildToCollapse(ArtifactFragment af, List<ArtifactFragment> artFragsToCollapse) {
    	nestLvl++;
    	for (ArtifactFragment afChild : af.getShownChildren()) {
    		
    		if (afChild.getShownChildren().isEmpty() && nestLvl > maxNestLvl 
    				&& ClosedContainerDPolicy.isShowingChildren(af)) {
    			maxNestLvl = nestLvl;
    			artFragsToCollapse.clear();
    			artFragsToCollapse.add(af);
    		} 
    		else if (nestLvl == maxNestLvl) {
    			artFragsToCollapse.add(af);	
    		}
    		getLowestChildToCollapse(afChild, artFragsToCollapse);
    	}
    	nestLvl--;
	}

	@Override
	public void run() {
    	artsAdded.clear();
		execute(createReduceCommand(getSelectedObjects()));
	}

    protected StrataRootEditPart getRootController() {
    	return (StrataRootEditPart) ((RSEEditorViewCommon.IRSEEditorViewCommon) getWorkbenchPart()).getRootController();
    }

    @Override
	protected void init() {
		super.init();
		setText(ReduceAction_Label);
		setToolTipText(ReduceAction_Tooltip);
		setId(ReduceAction_Id);
		
		// use images of delete for now
		ISharedImages sharedImages = PlatformUI.getWorkbench().getSharedImages();
		setImageDescriptor(sharedImages.getImageDescriptor(ISharedImages.IMG_OBJ_ELEMENT));
		setDisabledImageDescriptor(sharedImages.getImageDescriptor(
				ISharedImages.IMG_TOOL_DELETE_DISABLED));
		setEnabled(false);
	}
}