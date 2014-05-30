package com.architexa.diagrams.chrono.models;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.openrdf.model.Resource;
import org.openrdf.rio.RdfDocumentWriter;

import com.architexa.diagrams.RSECore;
import com.architexa.diagrams.chrono.controlflow.ControlFlowBlock;
import com.architexa.diagrams.chrono.editparts.MethodBoxEditPart;
import com.architexa.diagrams.chrono.figures.AbstractSeqFigure;
import com.architexa.diagrams.jdt.RJCore;
import com.architexa.diagrams.jdt.model.CodeUnit;
import com.architexa.diagrams.model.AnnotatedRel;
import com.architexa.diagrams.model.ArtifactFragment;
import com.architexa.diagrams.model.ArtifactRel;
import com.architexa.diagrams.model.ColorDPolicy;
import com.architexa.diagrams.utils.RelUtils;
import com.architexa.org.eclipse.draw2d.geometry.Dimension;
import com.architexa.org.eclipse.draw2d.geometry.Point;
import com.architexa.store.ReloRdfRepository;
import com.architexa.store.StoreUtil;


/**
 * 
 * @author Elizabeth L. Murnane
 *
 */
public class NodeModel extends CodeUnit {

	public static final String PROPERTY_LOCATION = "nodeLocation";
	public static final String PROPERTY_X_POS = "xPos";
	public static final String PROPERTY_Y_POS = "yPos";

	public static final String PROPERTY_SIZE = "size";
	public static final String PROPERTY_HEIGHT = "height";
	public static final String PROPERTY_WIDTH = "width";

	public static final String PROPERTY_CHILDREN = "children";
	public static final String PROPERTY_COMMENT_CHILDREN ="commentChildren";
	public static final String PROPERTY_CONDITIONAL_CHILDREN = "conditionalChildren";
	public static final String PROPERTY_REORDER = "reorder";

	public static final String PROPERTY_SOURCE_CONNNECTION = "sourceConnection";
	public static final String PROPERTY_TARGET_CONNECTION = "targetConnection";

	private Dimension size = new Dimension(50, 50);
	private Point location = new Point(0, 0);
	AbstractSeqFigure figure;

	private List<ConnectionModel> sourceConnections = new ArrayList<ConnectionModel>();
	private List<ConnectionModel> targetConnections = new ArrayList<ConnectionModel>();
	private int numConnectionsAdded = 0;

	private List<ControlFlowBlock> conditionalBlocksContainedIn = new ArrayList<ControlFlowBlock>();

	private boolean isUserCreated = false;
	
	public NodeModel(Resource res) {
		super(res);
	}

	public NodeModel() {
		super();
	}

	public void setLocation(Point newLocation) {
		if (newLocation !=null && !newLocation.equals(location)) {
			location.setLocation(newLocation);
			firePropertyChange(NodeModel.PROPERTY_LOCATION, null, location);
		}
	}

	public Point getLocation() {
		return location.getCopy();
	}

	public void setSize(Dimension newSize) {
		if (newSize != null && !newSize.equals(size)) {
			size.setSize(newSize);
			firePropertyChange(NodeModel.PROPERTY_SIZE, null, size);
		}
	}

	public Dimension getSize() {
		return size.getCopy();
	}

	public void setFigure(AbstractSeqFigure figure) {
		this.figure = figure;
	}

	public AbstractSeqFigure getFigure() {
		return figure;
	}

	public void addConnection(ConnectionModel conn) {
		if (conn == null || conn.getSource() == conn.getTarget()) return;
		if (conn.getSource() == this) {
			addSourceConnection(conn);
			sourceConnections.add(conn);
			firePropertyChange(NodeModel.PROPERTY_SOURCE_CONNNECTION, null, conn);
		} else if (conn.getTarget() == this) {
			addTargetConnection(conn);
			targetConnections.add(conn);
			firePropertyChange(NodeModel.PROPERTY_TARGET_CONNECTION, null, conn);
		}
	}

	public void writeRDF(RdfDocumentWriter rdfWriter, ReloRdfRepository repo, Resource childSaveRes) {
		try {
			Resource modelRes = getArt().elementRes;
			if (this instanceof UserCreatedInstanceModel) {
				rdfWriter.writeStatement(modelRes, getRootArt().getRepo().rdfType, RJCore.classType);
				String instanceName = getInstanceName();
				if (instanceName != null)
					rdfWriter.writeStatement(modelRes, RSECore.instanceName, repo.getLiteral(instanceName));
			} else if (this instanceof MethodBoxModel) {
				MethodBoxModel model = ((MethodBoxModel)this);
				rdfWriter.writeStatement(modelRes, getRootArt().getRepo().rdfType, RJCore.methodType);
				String methodName = model.getMethodName();
				if (methodName != null)
					rdfWriter.writeStatement(modelRes, RSECore.instanceName, repo.getLiteral(methodName));
			}
			rdfWriter.writeStatement(modelRes, RSECore.userCreated, StoreUtil.createMemLiteral("true"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void removeConnection(ConnectionModel conn) {
		if (conn == null) return;
		if (conn.getSource() == this) {
			removeSourceConnection(conn);
			sourceConnections.remove(conn);
			firePropertyChange(NodeModel.PROPERTY_SOURCE_CONNNECTION, null, conn);
		} else if (conn.getTarget() == this) {
			removeTargetConnection(conn);
			targetConnections.remove(conn);
			firePropertyChange(NodeModel.PROPERTY_TARGET_CONNECTION, null, conn);
		}
	}

	public void setNumConnectionsAdded(int num) {
		numConnectionsAdded = num;
	}

	public int getNumConnectionsAdded() {
		return numConnectionsAdded;
	}

	public int getNumberOfMessages() {
		return sourceConnections.size() + targetConnections.size();
	}

	@Override
	public void appendShownChild(ArtifactFragment child) {
		addChild(child, -1);
	}

	@Override
	public void appendShownChild(ArtifactFragment child, int index) {
		addChild(child, index);
	}

	public boolean addChild(NodeModel child) {
		if (child != null && shownChildrenArt.add(child)) {
			child.setParentArt(this);
			if(this instanceof InstanceModel && child instanceof MemberModel)
				((MemberModel)child).setInstanceModel((InstanceModel)this);
			firePropertyChange(PROPERTY_CHILDREN, null, child);
			return true;
		}
		return false;
	}

	public boolean addChild(ArtifactFragment child, int index) {
		if (child == null) return false;
		if (index >= 0)	shownChildrenArt.add(index,child);
		else shownChildrenArt.add(child);
		child.setParentArt(this);
		if(this instanceof InstanceModel && child instanceof MemberModel)
			((MemberModel)child).setInstanceModel((InstanceModel)this);
		ArtifactFragment.ensureInstalledPolicy(child, ColorDPolicy.DefaultKey, ColorDPolicy.class);
		firePropertyChange(PROPERTY_CHILDREN, null, child);
		return true;
	}

	public boolean removeChild(ArtifactFragment child) {
		if (child != null && shownChildrenArt.remove(child)) {
			if(this.equals(child.getParentArt())) child.setParentArt(null);
			firePropertyChange(PROPERTY_CHILDREN, null, child);
			if(child instanceof MethodBoxModel) {
				if(this instanceof InstanceModel) {
					List<MethodBoxEditPart> boxes = new ArrayList<MethodBoxEditPart>(((InstanceModel)this).getMethodBoxes());
					for(MethodBoxEditPart box : boxes) {
						if(((MethodBoxModel)child).equals(box.getModel())) ((InstanceModel)this).removeMethodBox(box);
					}
				}
			}
			removeChildAnnotatedRels(child);
			return true;
		}
		return false;
	}

	// when removing a node from the diagram check for any attached Annotated
	// rels and remove them
	private void removeChildAnnotatedRels(ArtifactFragment child) {
		List<ArtifactRel> rels = 
			new ArrayList<ArtifactRel>(child.getSourceConnections());
		rels.addAll(child.getTargetConnections());
		for (ArtifactRel rel : rels) {
			if (rel instanceof AnnotatedRel) {
				RelUtils.removeModelSourceConnections(rel.getSrc(), rel);
				RelUtils.removeModelTargetConnections(rel.getDest(), rel);
			}
		}
	}

	public boolean removeAllChildren() {
		if(this instanceof InstanceModel) {
			List<MethodBoxEditPart> boxes = new ArrayList<MethodBoxEditPart>(((InstanceModel)this).getMethodBoxes());
			for(MethodBoxEditPart box : boxes) ((InstanceModel)this).removeMethodBox(box);
		}
		for(ArtifactFragment child : shownChildrenArt) {
			if(this.equals(child.getParentArt())) child.setParentArt(null);
		}
		shownChildrenArt.clear();
		firePropChang(PROPERTY_CHILDREN);
		return true;
	}

	public boolean reorderChild(NodeModel child, int index) {
		if(child != null && shownChildrenArt.contains(child)) {
			shownChildrenArt.remove(child);
			shownChildrenArt.add(index, child);
			firePropertyChange(NodeModel.PROPERTY_REORDER, child, index);
			return true;
		}
		return false;
	}

	@Override
	public List<ArtifactFragment> getShownChildren() {
		List<ArtifactFragment> shownChildren = new ArrayList<ArtifactFragment>(shownChildrenArt);
		return shownChildren;
	}

	public List<ArtifactFragment> getChildren() {
		return shownChildrenArt;
	}

	/**
	 * 
	 * @return the children of this that are method or field models
	 */
	public List<MemberModel> getMemberChildren() {
		List<MemberModel> memberChildren = new ArrayList<MemberModel>();
		for(ArtifactFragment child : getChildren()) {
			if(child instanceof MethodBoxModel || child instanceof FieldModel) {
				memberChildren.add((MemberModel)child);
			}
		}
		return memberChildren;
	}

	/**
	 * 
	 * @return the children of this that are method models
	 */
	public List<MethodBoxModel> getMethodChildren() {
		List<MethodBoxModel> methodChildren = new ArrayList<MethodBoxModel>();
		for(ArtifactFragment child : getChildren()) {
			if (!(child instanceof MethodBoxModel)) continue;
			methodChildren.add((MethodBoxModel)child);
		}
		return methodChildren;
	}

	/**
	 * 
	 * @return the children of this that are field models
	 */
	public List<FieldModel> getFieldChildren() {
		List<FieldModel> fieldChildren = new ArrayList<FieldModel>();
		for(ArtifactFragment child : getChildren()) {
			if(!(child instanceof FieldModel)) continue;
			fieldChildren.add((FieldModel)child);
		}
		return fieldChildren;
	}

	public List<MemberModel> getCollapsedMethodChildren() {
		List<MemberModel> hiddenMethods = new ArrayList<MemberModel>();
		for (ArtifactFragment child : getChildren()) {
			if (!(child instanceof HiddenNodeModel))
				continue;
			hiddenMethods.addAll(((HiddenNodeModel) child).getControlFlowMethodsHiding());
		}
		return hiddenMethods;
	}

	public void addConditionalBlock(ControlFlowBlock block) {
		if(!conditionalBlocksContainedIn.contains(block))
			conditionalBlocksContainedIn.add(block);
	}

	public void removeConditionalBlock(ControlFlowBlock block) {
		conditionalBlocksContainedIn.remove(block);
	}

	public List<ControlFlowBlock> getConditionalBlocksContainedIn() {
		return conditionalBlocksContainedIn;
	}

	public void setUserCreated(boolean isUserCreated) {
		this.isUserCreated = isUserCreated;
	}

	public boolean isUserCreated() {
		return isUserCreated;
	}

}
