package com.architexa.diagrams.chrono.models;

import java.util.ArrayList;
import java.util.List;

import org.openrdf.model.Resource;

import com.architexa.diagrams.chrono.controlflow.ControlFlowModel;
import com.architexa.diagrams.model.ArtifactFragment;
import com.architexa.diagrams.model.ColorDPolicy;
import com.architexa.diagrams.model.RootArtifact;
import com.architexa.intro.preferences.LibraryPreferences;
import com.architexa.org.eclipse.draw2d.IFigure;


/**
 * 
 * @author Elizabeth L. Murnane
 *
 */
public class DiagramModel extends RootArtifact {

	public static final String CHILD_ADDED_PROP = "DiagramModel.ChildAdded";
	public static final String CHILD_REMOVED_PROP = "DiagramModel.ChildRemoved";

	public static int TOP_MARGIN = 52;
	public static int BOTTOM_MARGIN = 50;
	public static int SIDE_MARGIN = 10;
	public static int INSTANCE_PANEL_MARGINS = 10;

	List<ControlFlowModel> conditionalLayerChildren;

	IFigure instancePanel;
	IFigure instancePanelLabelContainer;

	private Resource savedDiagramRes = null;

	public DiagramModel() {
		shownChildrenArt = new ArrayList<ArtifactFragment>();
		conditionalLayerChildren = new ArrayList<ControlFlowModel>();
	}

	public void setSavedDiagramResource(Resource res) {
		savedDiagramRes = res;
	}

	public Resource getSavedDiagramResource() {
		return savedDiagramRes;
	}

	public IFigure getInstancePanel() {
		return instancePanel;
	}

	public void setInstancePanel(IFigure panel) {
		instancePanel = panel;
	}

	@Override
	public void appendShownChild(ArtifactFragment child) {
		addChild(child);
	}

	@Override
	public void appendShownChild(ArtifactFragment child, int index) {
		addChild(child, index);
	}

	/** 
	 * @return true if the child was added, false otherwise
	 */
	public boolean addChild(ArtifactFragment child) {
		return addChild(child, -1);
	}

	public boolean addChild(ArtifactFragment child, int index) {
		if (child == null) return false;
		if (index >= 0)	shownChildrenArt.add(index,child);
		else shownChildrenArt.add(child);
		child.setParentArt(this);
		ArtifactFragment.ensureInstalledPolicy(child, ColorDPolicy.DefaultKey, ColorDPolicy.class);
		firePropertyChange(NodeModel.PROPERTY_CHILDREN, null, child);
		return true;
	}

	public boolean addChildToConditionalLayer(ControlFlowModel child) {
		if(child != null && conditionalLayerChildren.add(child)) {
			firePropertyChange(NodeModel.PROPERTY_CONDITIONAL_CHILDREN, null, child);
			return true;
		}
		return false;
	}

	/** 
	 * Removes the given child and deletes any conditional blocks 
	 * @return true if the child was removed, false otherwise
	 */
	public boolean removeChild(ArtifactFragment child) {
		if (child != null && shownChildrenArt.remove(child)) {
			if(this.equals(child.getParentArt())) child.setParentArt(null);
			firePropertyChange(NodeModel.PROPERTY_CHILDREN, null, child);
			return true;
		}
		return false;
	}

	public boolean removeAllChildren() {
		for(ArtifactFragment child : shownChildrenArt) {
			if(this.equals(child.getParentArt())) child.setParentArt(null);
		}
		shownChildrenArt.clear();
		firePropertyChange(NodeModel.PROPERTY_CHILDREN, null, null);
		return true;
	}

	public boolean removeChildFromConditionalLayer(ControlFlowModel child) {
		if(child !=null && conditionalLayerChildren.remove(child)) {
			for (ArtifactFragment innerChild : child.getInnerConditionalModels()) {
				ControlFlowModel innerCF = (ControlFlowModel) innerChild;
				innerCF.setOuterConditionalModel(null);
				conditionalLayerChildren.add(innerCF);
				
				innerCF.firePropChang(ControlFlowModel.PROPERTY_DELETE);
			}
			firePropertyChange(NodeModel.PROPERTY_CONDITIONAL_CHILDREN, null, child);
			return true;
		}
		return false;
	}

	public boolean reorderChild(ArtifactFragment child, int index) {
		if(child != null && shownChildrenArt.contains(child)) {
			shownChildrenArt.remove(child);
			shownChildrenArt.add(index, child);
			firePropertyChange(NodeModel.PROPERTY_REORDER, child, index);
			return true;
		}
		return false;
	}

	public List<ArtifactFragment> getChildren() {
		return shownChildrenArt;
	}

	public List<ControlFlowModel> getConditionalChildren() {
		return conditionalLayerChildren;
	}

	@Override
	public String toString() {
		return "Diagram";
	}

	@Override
	public boolean isLibCodeInDiagram() {
		return LibraryPreferences.isChronoLibCodeInDiagram();
	}
}
