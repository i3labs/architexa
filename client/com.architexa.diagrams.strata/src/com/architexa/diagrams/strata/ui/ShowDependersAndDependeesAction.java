package com.architexa.diagrams.strata.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;

import com.architexa.diagrams.jdt.RJCore;
import com.architexa.diagrams.model.Artifact;
import com.architexa.diagrams.model.ArtifactFragment;
import com.architexa.diagrams.model.DirectedRel;
import com.architexa.diagrams.strata.parts.StrataArtFragEditPart;
import com.architexa.diagrams.ui.RSENestedAction;
import com.architexa.org.eclipse.gef.commands.Command;
import com.architexa.org.eclipse.gef.commands.CompoundCommand;
import com.architexa.org.eclipse.gef.ui.actions.SelectionAction;

public class ShowDependersAndDependeesAction extends SelectionAction implements RSENestedAction{

    public static final String ShowDependersAndDependeesAction_Id = "sdd";
    public static final String ShowDependersAndDependeesAction_Label = "Show Dependers And Dependees";
    public static final String ShowDependersAndDependeesAction_Tooltip = "Show all dependers and dependees for the selected items";
    public static final String ShowDependersAndDependeesAction_CommandName = "sdd";

    public ShowDependersAndDependeesAction(IWorkbenchPart part) {
		super(part);
	}

	@Override
	protected boolean calculateEnabled() {
		Command cmd = createSDDCommand(getSelectedObjects());
		if (cmd == null)
			return false;
		return cmd.canExecute();
	}


	public Command createSDDCommand(List<?> selectedObjects) {
		if (selectedObjects.isEmpty()) return null;
		
		CompoundCommand cmd = new CompoundCommand(ShowDependersAndDependeesAction_Label);
		List<Artifact> artsAdded = new ArrayList<Artifact>();
		Map<Artifact, ArtifactFragment> addedArtToAFMap = new HashMap<Artifact, ArtifactFragment>();
        
		for (Object obj : selectedObjects) {
			if (obj instanceof StrataArtFragEditPart){
				StrataArtFragEditPart sAFEP = (StrataArtFragEditPart) obj;
				DirectedRel revRel = DirectedRel.getRev(RJCore.containmentBasedRefType);
			    for (final Artifact relArt : sAFEP.showableListModel(sAFEP.getRepo(), revRel, null)) {
			    	if (artsAdded.contains(relArt)) continue;
			    	StrataArtFragEditPart.addArtAndContainers(cmd, addedArtToAFMap, sAFEP.getRootController(), sAFEP.getRepo(), relArt);
			    }
			    DirectedRel fwdRel = DirectedRel.getFwd(RJCore.containmentBasedRefType);
			    for (final Artifact relArt : sAFEP.showableListModel(sAFEP.getRepo(), fwdRel, null)) {
			    	if (artsAdded.contains(relArt)) continue;
			    	StrataArtFragEditPart.addArtAndContainers(cmd, addedArtToAFMap, sAFEP.getRootController(), sAFEP.getRepo(), relArt);
			    }
			}
		}
		return cmd;
	}



    @Override
	public void run() {
		execute(createSDDCommand(getSelectedObjects()));
	}

    protected List<StrataArtFragEditPart> getAllArtFragEP() {
        StrataEditor editor = (StrataEditor) getWorkbenchPart();
        return editor.getRootController().getAllArtFragEP();
    }

    @Override
	protected void init() {
		super.init();
		setText(ShowDependersAndDependeesAction_Label);
		setToolTipText(ShowDependersAndDependeesAction_Tooltip);
		setId(ShowDependersAndDependeesAction_Id);
		
		// use images of delete for now
		ISharedImages sharedImages = PlatformUI.getWorkbench().getSharedImages();
		setImageDescriptor(sharedImages.getImageDescriptor(ISharedImages.IMG_OBJ_ELEMENT));
		setDisabledImageDescriptor(sharedImages.getImageDescriptor(
				ISharedImages.IMG_TOOL_DELETE_DISABLED));
		setEnabled(false);
	}
}