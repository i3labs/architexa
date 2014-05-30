package com.architexa.diagrams.strata.ui;

import java.util.List;

import org.eclipse.jface.action.IAction;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;

import com.architexa.diagrams.model.ArtifactFragment;
import com.architexa.diagrams.strata.commands.CreateVertLayerAndMoveCmd;
import com.architexa.diagrams.strata.model.Layer;
import com.architexa.diagrams.strata.parts.LayerEditPart;
import com.architexa.diagrams.strata.parts.StrataArtFragEditPart;
import com.architexa.diagrams.strata.parts.TitledArtifactEditPart;
import com.architexa.org.eclipse.gef.EditPart;
import com.architexa.org.eclipse.gef.commands.Command;

public class CreateLeftVertLayerAndMoveAction extends CreateVertLayerAndMoveAction implements IAction {
	public static final String CreateVertLayerAndMoveAction_Id = "createAndFlipLeft";
    public static final String CreateVertLayerAndMoveAction_Label = "Left";
    public static final String CreateVertLayerAndMoveAction_Tooltip = "Move to Vertical Layer on Left";
    public static final String CreateVertLayerAndMoveAction_CommandName = "createAndFlipLeft";
	public CreateLeftVertLayerAndMoveAction(IWorkbenchPart wbPart, List<?> sel) {
		super(wbPart, sel);
	}
	@Override
	protected void init() {
		super.init();
		setText(CreateVertLayerAndMoveAction_Label);
		setToolTipText(CreateVertLayerAndMoveAction_Tooltip);
		setId(CreateVertLayerAndMoveAction_Id);
		
		// use images of delete for now
		ISharedImages sharedImages = PlatformUI.getWorkbench().getSharedImages();
		setImageDescriptor(sharedImages.getImageDescriptor(ISharedImages.IMG_TOOL_UNDO));
		setDisabledImageDescriptor(sharedImages.getImageDescriptor(
				ISharedImages.IMG_TOOL_UNDO_DISABLED));
		setEnabled(false);
	}
	
	 @Override
	public Command getCreateVertLayerAndMoveCommand(List<?> newLayerObjects) {
	    	// TODO LAYER: support mult selection
	    	Object obj = newLayerObjects.get(0);
	        if (!(obj instanceof StrataArtFragEditPart)) return null;
	        StrataArtFragEditPart selectedEP = (StrataArtFragEditPart) obj;
	            
	        EditPart parent = selectedEP.getParent();
	        if (!(parent instanceof LayerEditPart)) return null;
	        Layer orgParentLayer = (Layer)parent.getModel();
	        TitledArtifactEditPart parentTAFEP = selectedEP.getParentTAFEP();
			ArtifactFragment parentTitledAF = (ArtifactFragment) parentTAFEP.getModel();
			return new CreateVertLayerAndMoveCmd((ArtifactFragment) selectedEP.getModel(), orgParentLayer, parentTitledAF, false);    
		}
}
