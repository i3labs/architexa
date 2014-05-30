package com.architexa.diagrams.strata;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IStartup;
import org.eclipse.ui.IWorkbenchWindow;
import org.openrdf.model.Resource;

import com.architexa.diagrams.jdt.RJCore;
import com.architexa.diagrams.model.Artifact;
import com.architexa.diagrams.model.ArtifactFragment;
import com.architexa.diagrams.strata.ui.OpenStrata;
import com.architexa.diagrams.strata.ui.StrataEditor;
import com.architexa.diagrams.strata.ui.StrataEditorInput;
import com.architexa.diagrams.ui.PluggableDiagramsSupport;
import com.architexa.diagrams.ui.RSEAction;
import com.architexa.diagrams.ui.RSEEditor;
import com.architexa.diagrams.ui.SelectableAction;
import com.architexa.store.StoreUtil;

public class StrataDiagramEngine implements IStartup, PluggableDiagramsSupport.IRSEDiagramEngine {
	static final Logger logger = StrataPlugin.getLogger(StrataDiagramEngine.class);

	public void earlyStartup() {
		try {
			earlyStartupInternal();
		} catch (Throwable t) {
			logger.error("Unexpected Error", t);
		}
	}
	private void earlyStartupInternal() {
		PluggableDiagramsSupport.registerDiagram(this);
	}

	public Class<? extends SelectableAction> getOpenActionClass() {
		return OpenStrata.class;
	}

	public String diagramType() {
		return "Layered Diagram";
	}

	public String diagramUsageName() {
		return "Strata";
	}

	public int getMenuPosition() {
		return 1;
	}

	public String editorId() {
		return StrataEditor.editorId;
	}

	public ImageDescriptor getImageDescriptor() {
		return StrataPlugin.getImageDescriptor("icons/office-document.png");
	}

	public ImageDescriptor getNewEditorImageDescriptor() {
		return StrataPlugin.getImageDescriptor("icons/new_strata.png");
	}

	public IEditorInput newEditorInput() {
//		return new ListEditorInput(new ArrayList<Object>(), new ClassStrucBrowseModel());
		return new StrataEditorInput(null);
	}

	public Class<? extends RSEEditor> getEditorClass() {
		return StrataEditor.class;
	}

	public Collection<? extends ArtifactFragment> getShownChildren(RSEEditor activeEditor) {
		// TODO: Needs implementation
		return Collections.emptyList();
	}

	public void openSwitchedDiagramEditor(List<ArtifactFragment> shownAF) {
		// TODO: Needs implementation
	}

	public void openDiagramFromNavigatedTabs(
			List<ArtifactFragment> navigatedTabFragsWithChildren, 
			List<ArtifactFragment> allNavigatedTabs) {
		// remove nested classes from item to add to diagram to prevent duplicates
		for (Object toAdd : new ArrayList<Object>(navigatedTabFragsWithChildren)) {
			if(!(toAdd instanceof ArtifactFragment)) continue; 
			if(((ArtifactFragment)toAdd).getParentArt()==null) continue;
			Resource parentType = ((ArtifactFragment)toAdd).getParentArt().queryType(StoreUtil.getDefaultStoreRepository());
			if(RJCore.classType.equals(parentType) || RJCore.interfaceType.equals(parentType))
				navigatedTabFragsWithChildren.remove(toAdd);
		}
		List<Artifact> artsToAdd = new ArrayList<Artifact>();
		for(ArtifactFragment toAdd : navigatedTabFragsWithChildren) artsToAdd.add(toAdd.getArt());
		IWorkbenchWindow activeWorkbenchWindow = StrataPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow();
		OpenStrata.buildAndOpenStrataDoc(activeWorkbenchWindow, artsToAdd, null, null, null, null);
	}

	public RSEAction getTracker() {
		return new com.architexa.diagrams.strata.ui.JDTLinkedTracker.LinkedTrackerActionDelegate();
	}

}
