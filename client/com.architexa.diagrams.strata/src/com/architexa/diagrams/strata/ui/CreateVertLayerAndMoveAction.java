package com.architexa.diagrams.strata.ui;

import java.util.List;

import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;

import com.architexa.diagrams.model.ArtifactFragment;
import com.architexa.diagrams.strata.commands.CreateVertLayerAndMoveCmd;
import com.architexa.diagrams.strata.model.CompositeLayer;
import com.architexa.diagrams.strata.model.Layer;
import com.architexa.diagrams.strata.parts.LayerEditPart;
import com.architexa.diagrams.strata.parts.StrataArtFragEditPart;
import com.architexa.diagrams.strata.parts.StrataRootEditPart;
import com.architexa.diagrams.strata.parts.TitledArtifactEditPart;
import com.architexa.diagrams.ui.RSEEditorViewCommon;
import com.architexa.org.eclipse.gef.EditPart;
import com.architexa.org.eclipse.gef.commands.Command;
import com.architexa.org.eclipse.gef.ui.actions.WorkbenchPartAction;

public class CreateVertLayerAndMoveAction extends WorkbenchPartAction{

    public static final String CreateVertLayerAndMoveAction_Id = "createAndFlip";
    public static final String CreateVertLayerAndMoveAction_Label = "Right";
    public static final String CreateVertLayerAndMoveAction_Tooltip = "Move to Vertical Layer on Right";
    public static final String CreateVertLayerAndMoveAction_CommandName = "createAndFlip";
	private List<?> selection;

    public CreateVertLayerAndMoveAction(IWorkbenchPart wbPart, List<?> sel) {
		super(wbPart);
		selection = sel;
		init();
	}

	@Override
	protected boolean calculateEnabled() {
		if (selection.isEmpty()) return false;
		Object obj = selection.get(0);
		if (obj instanceof EditPart) {
		
			Object model = ((EditPart) obj).getParent().getModel();
			boolean layout = false;
			if (model instanceof Layer && !(model instanceof CompositeLayer))
				layout = ((Layer) model).getLayout();
			return layout;
		}
			
		return false;
	}


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
		return new CreateVertLayerAndMoveCmd((ArtifactFragment) selectedEP.getModel(), orgParentLayer, parentTitledAF);    
	}

    @Override
	public void run() {
		execute(getCreateVertLayerAndMoveCommand(selection));
	}

   protected List<StrataArtFragEditPart> getAllArtFragEP() {
    	return ((StrataRootEditPart) ((RSEEditorViewCommon.IRSEEditorViewCommon) getWorkbenchPart()).getRootController()).getAllArtFragEP();
    }

	@Override
	protected void init() {
		setText(CreateVertLayerAndMoveAction_Label);
		setToolTipText(CreateVertLayerAndMoveAction_Tooltip);
		setId(CreateVertLayerAndMoveAction_Id);
		
		// use images of delete for now
		ISharedImages sharedImages = PlatformUI.getWorkbench().getSharedImages();
		setImageDescriptor(sharedImages.getImageDescriptor(ISharedImages.IMG_TOOL_REDO));
		setDisabledImageDescriptor(sharedImages.getImageDescriptor(
				ISharedImages.IMG_TOOL_REDO_DISABLED));
		setEnabled(false);
	}
}
