/**
 * Copyright (c) 2007-2008 Architexa, Inc
 */ 
package com.architexa.diagrams.strata.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;

import com.architexa.diagrams.model.ArtifactFragment;
import com.architexa.diagrams.strata.model.policy.ClosedContainerDPolicy;
import com.architexa.diagrams.strata.model.policy.LayersDPolicy;
import com.architexa.diagrams.strata.parts.ContainableEditPart;
import com.architexa.diagrams.strata.parts.DependencyRelationEditPart;
import com.architexa.diagrams.strata.parts.StrataRootEditPart;
import com.architexa.diagrams.strata.parts.TitledArtifactEditPart;


public class ReLayerAction extends CommandStackListeningSelectionAction {

    public static final String ReLayerAction_Id = "relayer";
    public static final String ReLayerAction_Label = "Re-Layer";
    public static final String ReLayerAction_Tooltip = "Re-Layer containing modules";

    public ReLayerAction(IWorkbenchPart part) {
		super(part);
	}
    
    private boolean isAFEPShowingChildren(Object obj) {
    	if (!(obj instanceof TitledArtifactEditPart)) return false;
    	ArtifactFragment af = ((TitledArtifactEditPart)obj).getArtFrag();
        if (ClosedContainerDPolicy.isShowingChildren(af))
            return true;
        else
        	return false;
    }

	@Override
	protected boolean calculateEnabled() {
        List<?> selObjs = getSelectedObjects();
        for (Object selObj : selObjs) {
        	if (isAFEPShowingChildren(selObj)) return true;
        }
		return false;
	}

    @Override
    public void run() {
        List<?> selObjs = getSelectedObjects();
        for (Object selObj : selObjs) {
        	ContainableEditPart cEP = (ContainableEditPart) selObj;
			ReLayerCommand relayerCommand = new ReLayerCommand((ArtifactFragment) cEP.getModel(), cEP.getRootModel());
        	cEP.getViewer().getEditDomain().getCommandStack().execute(relayerCommand);
        }
    }

    private static void reLayer(TitledArtifactEditPart part) {
    	LayersDPolicy.flushLayers(part.getArtFrag());
	}
    
    // 'adjust' fixes things up if something unexpected happens, it does this by
    // waiting for 2 seconds and then reLayering the root, it also makes sure
    // not to not multi-adjust
    public static void adjust(final DependencyRelationEditPart depRelEP) {
        // eventually we can implement a better version of this, which doesn't
        // go all the way up
        final StrataRootEditPart afEP = depRelEP.getContentEP();
        if (adjustments.contains(afEP)) return;
        
        adjustments.add(afEP);
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                adjustments.remove(afEP);
                reLayer(afEP);
            }}, 2000);
    }
    private static List<StrataRootEditPart> adjustments = new ArrayList<StrataRootEditPart>(); 

 
    @Override
	protected void init() {
		super.init();
		setText(ReLayerAction_Label);
		setToolTipText(ReLayerAction_Tooltip);
		setId(ReLayerAction_Id);
		
		ISharedImages sharedImages = PlatformUI.getWorkbench().getSharedImages();
		setImageDescriptor(sharedImages.getImageDescriptor(IDE.SharedImages.IMG_OBJS_TASK_TSK));
		setDisabledImageDescriptor(sharedImages.getImageDescriptor(
                IDE.SharedImages.IMG_OBJS_TASK_TSK));
		setEnabled(false);
	}
}
