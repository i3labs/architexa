package com.architexa.diagrams.relo.jdt;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IStartup;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import com.architexa.diagrams.Activator;
import com.architexa.diagrams.jdt.RJCore;
import com.architexa.diagrams.jdt.model.CodeUnit;
import com.architexa.diagrams.model.Artifact;
import com.architexa.diagrams.model.ArtifactFragment;
import com.architexa.diagrams.model.ArtifactRel;
import com.architexa.diagrams.relo.actions.ListEditorInput;
import com.architexa.diagrams.relo.jdt.actions.OpenForBrowsingAction;
import com.architexa.diagrams.relo.jdt.browse.ClassStrucBrowseModel;
import com.architexa.diagrams.relo.ui.ReloEditor;
import com.architexa.diagrams.ui.PluggableDiagramsSupport;
import com.architexa.diagrams.ui.RSEAction;
import com.architexa.diagrams.ui.RSEEditor;
import com.architexa.diagrams.ui.SelectableAction;

public class ReloDiagramEngine implements IStartup, PluggableDiagramsSupport.IRSEDiagramEngine {
	static final Logger logger = ReloJDTPlugin.getLogger(ReloDiagramEngine.class);

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
		return OpenForBrowsingAction.class;
	}
	public String diagramType() {
		return "Class Diagram";
	}

	public String diagramUsageName() {
		return "Relo";
	}

	public int getMenuPosition() {
		return 2;
	}

	public String editorId() {
		return ReloEditor.editorId;
	}

	public ImageDescriptor getImageDescriptor() {
		return ReloJDTPlugin.getImageDescriptor("icons/relo-document.png");
	}

	public ImageDescriptor getNewEditorImageDescriptor() {
		return ReloJDTPlugin.getImageDescriptor("icons/new_relo.png");
	}

	public IEditorInput newEditorInput() {
		return new ListEditorInput(new ArrayList<Object>(), new ClassStrucBrowseModel());
	}

	public Class<? extends RSEEditor> getEditorClass() {
		return ReloEditor.class;
	}

	public Collection<? extends ArtifactFragment> getShownChildren(RSEEditor activeEditor) {
		return ((ReloEditor)activeEditor).getReloController().getReloDoc().getShownChildren();
	}

	public void openSwitchedDiagramEditor(List<ArtifactFragment> shownAF) {
		IWorkbenchWindow activeWorkbenchWindow = Activator.getDefault().getWorkbench().getActiveWorkbenchWindow();

		Map<ArtifactFragment, CodeUnit> mapOrigToNewAF = new HashMap<ArtifactFragment, CodeUnit>();
		// retrieve chrono model and store it in a map with its copy
		for (ArtifactFragment origAF : shownAF) {
			if(origAF.getShownChildren().isEmpty()) {
				// Fragment is a top level type that contains no children, so
				// it won't get automatically generated when a child gets added.
				// Adding it to the map so that it isn't excluded from the diagram.
				// TODO: Once allowance of duplicate classes is eliminated from Relo,
				// the if can be removed and the frag always added to the map since
				// if it does have children, adding them won't make a duplicate class
				ArtifactFragment newAF = new ArtifactFragment(origAF.getArt());
				CodeUnit newCU = new CodeUnit(newAF);
				mapOrigToNewAF.put(origAF, newCU);
			}

			List<ArtifactFragment> nestedChildren = new ArrayList<ArtifactFragment>();
			Map<ArtifactFragment, List<Artifact>> parentToAddedArts = new HashMap<ArtifactFragment, List<Artifact>>();
			for (ArtifactFragment child : origAF.getShownChildren()) {
				nestedChildren.addAll(getRecursiveChildren((ArtifactFragment) child, parentToAddedArts));
			}
			for (ArtifactFragment child : nestedChildren) {
				ArtifactFragment newChildAF = new ArtifactFragment(child.getArt());
				CodeUnit newChildCU = new CodeUnit(newChildAF);
				mapOrigToNewAF.put(child, newChildCU);	
			}
		}
		for(ArtifactFragment origAF : new ArrayList<ArtifactFragment>(mapOrigToNewAF.keySet())) {
			handleConnections(origAF, mapOrigToNewAF);
		}

		// pass copy to relo
		ArrayList<CodeUnit> toAddCopy = new ArrayList<CodeUnit>(mapOrigToNewAF.values());
		OpenForBrowsingAction.openReloViz(activeWorkbenchWindow, toAddCopy, null, null, null, null, null);
	}

	private List<ArtifactFragment> getRecursiveChildren(ArtifactFragment model, Map<ArtifactFragment, List<Artifact>> parentToAddedArts) {
		List<ArtifactFragment> children = new ArrayList<ArtifactFragment>();
		for (ArtifactFragment nodeChild : model.getShownChildren()) {
			children.addAll(getRecursiveChildren(nodeChild, parentToAddedArts));
		}
		// Some children in the list may be 'duplicates' if chrono has numerous 
		// instances of the same call. Necessary because if one duplicate call
		// originates from a nested declaration but another originates from a
		// declaration that is a direct child of the class, there will be two
		// entries in parentToAddedArts for those invocations because the parents
		// are different so we have to also keep two entries for their declarations
		// instead of just one or else we can have problems when we look up in 
		// mapOrigToNewAF the declaration of the invocation whose "duplicate" 
		// declaration partner was not also added
		ArtifactFragment parent = model.getParentArt();
		if(!parentToAddedArts.containsKey(parent)) {
			List<Artifact> addedArts = new ArrayList<Artifact>();
			addedArts.add(model.getArt());
			parentToAddedArts.put(parent, addedArts);
			children.add(model);
		} else {
			parentToAddedArts.get(parent).add(model.getArt());
			children.add(model);
		}
		return children;
	}			

	private void handleConnections(ArtifactFragment origAF, Map<ArtifactFragment, CodeUnit> mapOrigToNewAF) {
		for (ArtifactRel origConn : origAF.getSourceConnections()) {

			ArtifactFragment origSourceAF = null;
			if(origConn.getType().equals(RJCore.overrides)) origSourceAF = origAF;
			else if(origConn.getType().equals(RJCore.calls)) {
				// origAF must be an invocation model because the source
				// of a 'calls' connection is always an invocation model 
				// in chrono. In relo, this call would have as its source 
				// the declaration containing the invocation
				origSourceAF = origAF.getParentArt();

				// Relo does not include invocations like Chrono
				// does, so don't want a model for it in the map
				mapOrigToNewAF.remove(origAF);
			}

			// Ignoring 'return' and field access connections from chrono
			if(origSourceAF==null) continue;

			ArtifactFragment newSrcAF = mapOrigToNewAF.get(origSourceAF);
			ArtifactFragment newDestAF = mapOrigToNewAF.get(origConn.getDest());

			// Case where method calls itself (recursive method)
			if (newSrcAF.getArt().equals(newDestAF.getArt())) {
				newSrcAF = newDestAF;
			}

			ArtifactRel newConn = new ArtifactRel(newSrcAF, newDestAF, origConn.getType());
			newSrcAF.addSourceConnection(newConn);
			newDestAF.addTargetConnection(newConn);
		}
	}

	public void openDiagramFromNavigatedTabs(
			List<ArtifactFragment> navigatedTabFragsWithChildren, 
			List<ArtifactFragment> allNavigatedTabs) {
		//Relo supports connections between classes (inheritance relationships)
		for(ArtifactFragment classUnit : allNavigatedTabs) {
			if(navigatedTabFragsWithChildren.contains(classUnit)) continue;
			if(classUnit.getSourceConnections().size()>0 ||
					classUnit.getTargetConnections().size()>0) navigatedTabFragsWithChildren.add(classUnit);
		}
		IWorkbenchWindow activeWorkbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		OpenForBrowsingAction.openReloViz(activeWorkbenchWindow, navigatedTabFragsWithChildren, null, null, null, null, null);
	}

	public RSEAction getTracker() {
		return new com.architexa.diagrams.relo.jdt.browse.JDTLinkedTracker.LinkedTrackerAction();
	}

}
