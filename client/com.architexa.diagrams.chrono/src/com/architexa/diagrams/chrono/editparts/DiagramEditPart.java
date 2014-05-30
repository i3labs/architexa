package com.architexa.diagrams.chrono.editparts;

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.sesame.sail.StatementIterator;

import com.architexa.diagrams.RSECore;
import com.architexa.diagrams.chrono.animation.AnimationLayoutManager;
import com.architexa.diagrams.chrono.animation.Animator;
import com.architexa.diagrams.chrono.commands.SeqCommand;
import com.architexa.diagrams.chrono.controlflow.ControlFlowBlock;
import com.architexa.diagrams.chrono.controlflow.ControlFlowEditPart;
import com.architexa.diagrams.chrono.controlflow.ControlFlowModel;
import com.architexa.diagrams.chrono.controlflow.IfBlockModel;
import com.architexa.diagrams.chrono.controlflow.LoopBlockModel;
import com.architexa.diagrams.chrono.controlflow.UserCreatedControlFlowEditPart;
import com.architexa.diagrams.chrono.controlflow.UserCreatedControlFlowModel;
import com.architexa.diagrams.chrono.editpolicies.SeqOrderedLayoutEditPolicy;
import com.architexa.diagrams.chrono.figures.BottomShadowBorder;
import com.architexa.diagrams.chrono.figures.HiddenFigure;
import com.architexa.diagrams.chrono.figures.InstanceFigure;
import com.architexa.diagrams.chrono.models.DiagramModel;
import com.architexa.diagrams.chrono.models.MemberModel;
import com.architexa.diagrams.chrono.models.MethodInvocationModel;
import com.architexa.diagrams.chrono.models.NodeModel;
import com.architexa.diagrams.chrono.models.UserCreatedInstanceModel;
import com.architexa.diagrams.chrono.sequence.SeqPlugin;
import com.architexa.diagrams.chrono.ui.ColorScheme;
import com.architexa.diagrams.chrono.ui.SeqEditor;
import com.architexa.diagrams.chrono.util.ConnectionUtil;
import com.architexa.diagrams.chrono.util.SeqUtil;
import com.architexa.diagrams.commands.BreakableCommand;
import com.architexa.diagrams.draw2d.NonEmptyFigure;
import com.architexa.diagrams.draw2d.NonEmptyFigureSupport;
import com.architexa.diagrams.jdt.RJCore;
import com.architexa.diagrams.model.Artifact;
import com.architexa.diagrams.model.ArtifactFragment;
import com.architexa.diagrams.model.ArtifactRel;
import com.architexa.diagrams.model.Comment;
import com.architexa.diagrams.model.DiagramPolicy;
import com.architexa.diagrams.model.NamedRel;
import com.architexa.diagrams.model.RootArtifact;
import com.architexa.diagrams.parts.PointPositionedDiagramPolicy;
import com.architexa.diagrams.parts.RootEditPart;
import com.architexa.diagrams.services.PluggableTypes;
import com.architexa.org.eclipse.draw2d.CompoundBorder;
import com.architexa.org.eclipse.draw2d.ConnectionLayer;
import com.architexa.org.eclipse.draw2d.Figure;
import com.architexa.org.eclipse.draw2d.FigureListener;
import com.architexa.org.eclipse.draw2d.FreeformViewport;
import com.architexa.org.eclipse.draw2d.IFigure;
import com.architexa.org.eclipse.draw2d.Label;
import com.architexa.org.eclipse.draw2d.MarginBorder;
import com.architexa.org.eclipse.draw2d.MouseEvent;
import com.architexa.org.eclipse.draw2d.MouseMotionListener;
import com.architexa.org.eclipse.draw2d.ToolbarLayout;
import com.architexa.org.eclipse.draw2d.geometry.Dimension;
import com.architexa.org.eclipse.draw2d.geometry.Point;
import com.architexa.org.eclipse.gef.EditPart;
import com.architexa.org.eclipse.gef.EditPolicy;
import com.architexa.org.eclipse.gef.LayerConstants;
import com.architexa.org.eclipse.gef.Request;
import com.architexa.org.eclipse.gef.RequestConstants;
import com.architexa.org.eclipse.gef.commands.Command;
import com.architexa.org.eclipse.gef.commands.CommandStack;
import com.architexa.org.eclipse.gef.commands.CommandStackEvent;
import com.architexa.org.eclipse.gef.commands.CommandStackEventListener;
import com.architexa.org.eclipse.gef.editparts.AbstractGraphicalEditPart;
import com.architexa.org.eclipse.gef.editpolicies.RootComponentEditPolicy;
import com.architexa.org.eclipse.gef.requests.LocationRequest;
import com.architexa.rse.BuildStatus;
import com.architexa.store.ReloRdfRepository;
import com.architexa.store.RepositoryMgr;

/**
 * 
 * @author Elizabeth L. Murnane
 *
 */
public class DiagramEditPart extends RootEditPart implements MouseMotionListener {

	// Mapping edit part to number of align attempts performed for that part 
	// since we only want to protect against a _particular member_ trying to 
	// align too many times (because it's in an infinite loop and will never
	// align), rather than against too many alignment attempts _total_ by any
	// members in the diagram during the execution of a command. (When the 
	// executing command involves the addition of a lot of members or when the 
	// diagram is large, the alignment count may get high and exceed the escape 
	// value simply because a lot of members have to be aligned when the command 
	// executes and the count is only reset when a new command is executed).
	public static Map<MemberEditPart, Integer> alignmentAttempts = new HashMap<MemberEditPart, Integer>();
	public static int alignmentInfiniteLoopEscape = 50;
	private static final Logger logger = SeqPlugin.getLogger(DiagramEditPart.class);

	CommandStackEventListener stackEventListener = new CommandStackEventListener() {
		public void stackChanged(CommandStackEvent event) {
			alignmentAttempts.clear();

			if(event.getDetail()!=CommandStack.POST_EXECUTE) return;
			if(event.getCommand() instanceof SeqCommand && !((SeqCommand)event.getCommand()).isAnimatable()) return;

			Animator.setCommand(event.getCommand());
			if (!Animator.captureLayout(getFigure())) return;

			while(Animator.step()) getFigure().getUpdateManager().performUpdate();
			Animator.end();
		}
	};
	private NonEmptyFigure nonEmptyFigure;
	private HashMap<Resource, ArtifactFragment> instanceResToAF;

	@Override
	public void activate() {
		if (isActive()) return;
		super.activate();
		Value detailLevel = null;
		try {
			detailLevel = ((RepositoryMgr)((RootArtifact) getModel()).getRepo()).getFileRepo().getStatement(RSECore.createRseUri("DetailNode"), RSECore.detailLevelURI, null).getObject();
		} catch (Throwable t) {
			
		}
		if (detailLevel != null) {
			((RootArtifact) getModel()).setDetailLevel(Integer.valueOf(((Literal) detailLevel).getLabel()));
		}

		DiagramModel diagramModel = ((DiagramModel)getModel());
		diagramModel.addPropertyChangeListener(this);
		NonEmptyFigureSupport.listenToModel((ArtifactFragment) getModel(), nonEmptyFigure);
		getViewer().getEditDomain().getCommandStack().addCommandStackEventListener(stackEventListener);
		addInstancePanel();

		if(diagramModel.getRepo()!=null && diagramModel.getSavedDiagramResource()!=null) {
			// Use the file repo to get the information
			ReloRdfRepository repo = diagramModel.getRepo().getFileRepo();
			Map<Resource,ArtifactFragment> instanceRes2AFMap = new HashMap<Resource,ArtifactFragment>();
			instanceResToAF = new HashMap<Resource, ArtifactFragment>();
			addSavedContainee(diagramModel, diagramModel.getSavedDiagramResource(), instanceRes2AFMap, repo);
			addSavedControlFlows(diagramModel,  diagramModel.getSavedDiagramResource(), instanceRes2AFMap, repo);
			addSavedConnections(diagramModel, instanceRes2AFMap, repo);
		}
	}

	@Override
	public void deactivate() {
		if (!isActive()) return;
		BuildStatus.updateDiagramItemMap(getViewer().getEditDomain()
				.getCommandStack().toString(), getAllChildren());
		getViewer().getEditDomain().getCommandStack().removeCommandStackEventListener(stackEventListener);
		super.deactivate();
		((DiagramModel) getModel()).removePropertyChangeListener(this);
	}

	private int getAllChildren() {
		return getViewer().getEditPartRegistry().keySet().size();
	}
	
	@Override
	protected IFigure createFigure() {
		Figure f = new FreeformViewport();
		ToolbarLayout tb = new AnimationLayoutManager(true);
		tb.setSpacing(30);
		tb.setStretchMinorAxis(false);
		f.setLayoutManager(tb);
		f.setBorder(new MarginBorder(DiagramModel.TOP_MARGIN, DiagramModel.SIDE_MARGIN, DiagramModel.BOTTOM_MARGIN, DiagramModel.SIDE_MARGIN));
		f.addMouseMotionListener(this);
		f.setBackgroundColor(ColorScheme.diagramBackground);
		IFigure label = new Label(" \n Drag classes or methods from the Package Explorer " +
				"into the diagram to explore and understand code." +
				"\n Or, use the expandable Palette on the right side of the diagram " +
				"to design.");
		this.nonEmptyFigure = new NonEmptyFigure(label);
		NonEmptyFigureSupport.instructionHighlight(label, 10);
		f.add(nonEmptyFigure);
		
		warning = new Label("");
		warning.setLayoutManager(new ToolbarLayout());
		NonEmptyFigureSupport.instructionHighlight(warning, 10);
		f.add(warning);
		return f;
	}

	@Override
	protected void createEditPolicies() {
		installEditPolicy(EditPolicy.COMPONENT_ROLE, new RootComponentEditPolicy());
		installEditPolicy(EditPolicy.LAYOUT_ROLE, new SeqOrderedLayoutEditPolicy());
	}

	private void addSavedContainee(
			ArtifactFragment parent,
			Resource parentRes, 
			Map<Resource, ArtifactFragment> instanceRes2AFMap,
			ReloRdfRepository repo) {
		StatementIterator containeeIter = repo.getStatements(parentRes, RSECore.contains, null);
		while(containeeIter.hasNext()){
			try {
				Value obj = containeeIter.next().getObject();
				if (!(obj instanceof Resource) || repo.getStatement((Resource)obj, RSECore.model, null)==null)
					throw new Exception("Child resource not found for parent resource: "+ parentRes.toString());

				Resource instanceRes = (Resource) obj;
				Resource modelRes = (Resource)repo.getStatement(instanceRes, RSECore.model, null).getObject();
				Resource type = new Artifact(modelRes).queryType(repo);
				if (type == null) // if this is a comment try to get the type from the repo
					type = (Resource) repo.getStatement(modelRes, repo.rdfType, null).getObject();
				boolean isUserCreated = false;
				if (type == null) 
					throw new Exception("Type is Null for model resource: " + modelRes.toString() + "\nParent Resource: " + instanceRes.toString());
				else {
					Value val = repo.getStatement(modelRes, RSECore.userCreated, null).getObject();
					if (val != null)
						isUserCreated = true;
				}
					
				if (RSECore.commentType.equals(type)){
					addSavedCommentRes((DiagramModel)parent, instanceRes, instanceRes2AFMap, repo);
				} else if (isUserCreated) {
					addSavedUserCreatedNodes(parent, instanceRes, modelRes, instanceRes2AFMap, repo);
				} else if (RSECore.controlFlowType.equals(type) ) {
					continue;//addControlFlowModel(parent, instanceRes, modelRes, instanceRes2AFMap, repo, (DiagramModel) this.getModel());
				} else {
					addSavedCodeRes(parent, instanceRes, instanceRes2AFMap, repo);
				}
			} catch(Exception e){
				logger.error("Error while adding saved containee.\n",e);
			}
		}
		refreshChildren();
	}
	
	
	private void addSavedControlFlows(
			ArtifactFragment parent,
			Resource parentRes, 
			Map<Resource, ArtifactFragment> instanceRes2AFMap,
			ReloRdfRepository repo) {
		StatementIterator containeeIter = repo.getStatements(parentRes, RSECore.contains, null);
		while(containeeIter.hasNext()){
			try {
				Value obj = containeeIter.next().getObject();
				if (!(obj instanceof Resource) || repo.getStatement((Resource)obj, RSECore.model, null)==null)
					throw new Exception("Child resource not found for parent resource: "+ parentRes.toString());

				Resource instanceRes = (Resource) obj;
				Resource modelRes = (Resource)repo.getStatement(instanceRes, RSECore.model, null).getObject();
				Resource type = new Artifact(modelRes).queryType(repo);
				if (type == null) // if this is a comment try to get the type from the repo
					type = (Resource) repo.getStatement(modelRes, repo.rdfType, null).getObject();
				
				 if (RSECore.controlFlowType.equals(type) )
					addControlFlowModel(parent, instanceRes, modelRes, instanceRes2AFMap, repo, (DiagramModel) this.getModel());
			} catch(Exception e){
				logger.error("Error while adding saved containee.\n",e);
			}
		}
		refreshChildren();
	}

	private void addControlFlowModel(ArtifactFragment parent, Resource instanceRes, Resource modelRes, Map<Resource, ArtifactFragment> instanceRes2AFMap, ReloRdfRepository repo, DiagramModel diagramModel) throws Exception {

			Statement cfLabelStmt = repo.getStatement(modelRes, RSECore.name, null);
			Statement cfType = repo.getStatement(modelRes, RSECore.controlFlowName, null);			
			
			
			ControlFlowModel cfModel = null; 
			String cfTypeString = cfType.getObject().toString();
			if (cfTypeString.equals(RSECore.ifBlock.toString())) {
				cfModel = new IfBlockModel(diagramModel, cfLabelStmt.getObject().toString());
				
				((IfBlockModel)cfModel).addElseIfStmts(cfTypeString, getListOfConditionalStatements(RSECore.ifStmt, modelRes, instanceRes, instanceRes2AFMap, repo));
				((IfBlockModel)cfModel).setElseStmts(getListOfConditionalStatements(RSECore.elseStmt, modelRes, instanceRes, instanceRes2AFMap, repo));
				((IfBlockModel)cfModel).setThenStmts(getListOfConditionalStatements(RSECore.thenStmt, modelRes, instanceRes, instanceRes2AFMap, repo));
			} else if (cfTypeString.equals(RSECore.loopBlock.toString())) {
				cfModel = new LoopBlockModel(diagramModel, cfLabelStmt.getObject().toString());
				((LoopBlockModel)cfModel).setLoopStmts(getListOfConditionalStatements(RSECore.loopStmt, modelRes, instanceRes, instanceRes2AFMap, repo));
			} else if (cfTypeString.equals(RSECore.userControlBlock.toString())) {
				cfModel = new UserCreatedControlFlowModel(diagramModel, cfLabelStmt.getObject().toString());
				((UserCreatedControlFlowModel)cfModel).setStatements(getListOfConditionalStatements(RSECore.conditionalStmt, modelRes, instanceRes, instanceRes2AFMap, repo));
			}
			
			if (parent instanceof DiagramModel) {
				diagramModel.addChildToConditionalLayer(cfModel);
			} else if (parent instanceof ControlFlowModel) {
				((ControlFlowModel) parent).addInnerConditionalModel(cfModel);
				cfModel.setOuterConditionalModel((ControlFlowModel) parent);
				diagramModel.firePropertyChange(NodeModel.PROPERTY_CONDITIONAL_CHILDREN, null, cfModel);
			}
			
			
			
			
			StatementIterator containeeIter = repo.getStatements(modelRes, RSECore.contains, null);
			while(containeeIter.hasNext()){
				Value obj = containeeIter.next().getObject();
				if (!(obj instanceof Resource) || repo.getStatement((Resource)obj, RSECore.model, null)==null)
					throw new Exception("Child resource not found for parent resource: "+ instanceRes.toString());

				Resource childInstanceRes = (Resource) obj;
				
				addControlFlowModel(cfModel, instanceRes, childInstanceRes, instanceRes2AFMap, repo, diagramModel);
			}
//			if(instanceModel!=null)
//				instanceModel.setInstanceRes(instanceRes);
//			else
//				logger.error("Could not create Instance model for resource: " + detailsRes.toString());
	}

	private List<MemberModel> getListOfConditionalStatements(URI stmtURI, Resource modelRes, Resource instanceRes, Map<Resource, ArtifactFragment> instanceRes2AFMap, ReloRdfRepository repo) throws Exception {
		StatementIterator stmtIter = repo.getStatements(modelRes, stmtURI, null);
		List<MemberModel> retStmts = new ArrayList<MemberModel>();
		while(stmtIter.hasNext()){
			Value obj = stmtIter.next().getObject();
//			if (!(obj instanceof Resource) || repo.getStatement((Resource)obj, RSECore.model, null)==null)
//				throw new Exception("Child resource not found for parent resource: "+ instanceRes.toString());
			
			MemberModel model = (MemberModel) checkMapforRes(instanceRes2AFMap, obj, instanceRes);
			if (model != null) {
				if (stmtURI == RSECore.thenStmt && !(model instanceof MethodInvocationModel)) 
					continue;
				else
					retStmts.add(model);
			}
		}
		
		return retStmts;
	}

	// this needs to run after the rest of the model has been added to the map
	private ArtifactFragment checkMapforRes(Map<Resource, ArtifactFragment> instanceRes2AFMap, Value cfTopRes, Resource cfInstanceModelRes) {
		ArtifactFragment retAF = null;
		for (ArtifactFragment resAF : instanceRes2AFMap.values()) {
			if (resAF.toString().equals(cfTopRes.toString())) {
				if (resAF instanceof MethodInvocationModel)
					retAF = resAF;
			}
			
		}
		return retAF;
	}

	private void addSavedUserCreatedNodes(ArtifactFragment parent, Resource instanceRes, Resource modelRes, Map<Resource, ArtifactFragment> instanceRes2AFMap, ReloRdfRepository repo) {
		ArtifactFragment childToAdd = SeqUtil.createAFForSavedResources(repo, modelRes, instanceRes, parent, (DiagramModel)getModel());
		if (childToAdd == null)
			logger.error("Could not create model for " + instanceRes + "in parent " + parent);
		
		// Add AF to parent and call same method with child 
		int indexWhenSaved = findSavedIndex(repo, instanceRes);
		List<ArtifactFragment> childrenAddedToParent = parent.getShownChildren();
		if (parent instanceof DiagramModel) childrenAddedToParent = ((DiagramModel)parent).getChildren();
		else if (parent instanceof UserCreatedInstanceModel) childrenAddedToParent = ((NodeModel)parent).getChildren();
		
		int indexToAddAt = getCurrentIndex(indexWhenSaved, repo, childrenAddedToParent);
		parent.appendShownChild(childToAdd, indexToAddAt);
		instanceRes2AFMap.put(instanceRes, childToAdd);

		for (DiagramPolicy pol : childToAdd.getDiagramPolicies())
			pol.readRDF(repo);
		
		addSavedContainee(childToAdd, instanceRes, instanceRes2AFMap, repo);
	}

	private int findSavedIndex(ReloRdfRepository repo, Resource codeRes) {
		Statement indexWhenSavedStmt = repo.getStatement(codeRes, RJCore.index, null);
		int indexWhenSaved = (indexWhenSavedStmt == null || indexWhenSavedStmt.getObject() == null) ? -1 : Integer.parseInt(indexWhenSavedStmt.getObject().toString());
		return indexWhenSaved;
	}
	
	private int getCurrentIndex(int indexWhenSaved, ReloRdfRepository repo, List<ArtifactFragment> childrenAddedToParent) {
		for(ArtifactFragment addedChild : childrenAddedToParent) {

			Resource addedChildSaveRes = addedChild.getInstanceRes();
			Statement addedChildIndexWhenSavedStmt = repo.getStatement(addedChildSaveRes, RJCore.index, null);
			int addedChildIndexWhenSaved = -1;
			if(addedChildIndexWhenSavedStmt.getObject()!=null) 
				addedChildIndexWhenSaved = Integer.parseInt(addedChildIndexWhenSavedStmt.getObject().toString());

			if(indexWhenSaved>-1 && indexWhenSaved<addedChildIndexWhenSaved) 
				return childrenAddedToParent.indexOf(addedChild);
		}
		return -1;
	}
	
	private void addSavedCodeRes(
			ArtifactFragment parent,
			Resource codeRes,
			Map<Resource, ArtifactFragment> instanceRes2AFMap,
			ReloRdfRepository repo) throws Exception {
		ArtifactFragment childToAdd = SeqUtil.createChronoModelForResource(codeRes, parent, repo, this);
		if (childToAdd == null)
			throw new Exception("Could not create model for resource: "+codeRes.toString());

		if (parent instanceof DiagramModel)
			instanceResToAF.put(codeRes, childToAdd);
		
//		Statement indexWhenSavedStmt = repo.getStatement(codeRes, RJCore.index, null);
//		int indexWhenSaved = (indexWhenSavedStmt == null || indexWhenSavedStmt.getObject() == null) ? -1 : Integer.parseInt(indexWhenSavedStmt.getObject().toString());
		int indexWhenSaved = findSavedIndex(repo, codeRes);
//		int indexToAddAt = -1;

		List<ArtifactFragment> childrenAddedToParent = parent.getShownChildren();
		if(parent instanceof DiagramModel) childrenAddedToParent = ((DiagramModel)parent).getChildren();
		else if(parent instanceof NodeModel) childrenAddedToParent = ((NodeModel)parent).getChildren();
//		for(ArtifactFragment addedChild : childrenAddedToParent) {
//
//			Resource addedChildSaveRes = addedChild.getInstanceRes();
//			Statement addedChildIndexWhenSavedStmt = repo.getStatement(addedChildSaveRes, RJCore.index, null);
//			int addedChildIndexWhenSaved = -1;
//			if(addedChildIndexWhenSavedStmt.getObject()!=null) 
//				addedChildIndexWhenSaved = Integer.parseInt(addedChildIndexWhenSavedStmt.getObject().toString());
//
//			if(indexWhenSaved>-1 && indexWhenSaved<addedChildIndexWhenSaved) 
//				indexToAddAt = childrenAddedToParent.indexOf(addedChild);
//		}
		int indexToAddAt = getCurrentIndex(indexWhenSaved, repo, childrenAddedToParent);
		parent.appendShownChild(childToAdd, indexToAddAt);
		instanceRes2AFMap.put(codeRes, childToAdd);

		for (DiagramPolicy pol : childToAdd.getDiagramPolicies()) {
			pol.readRDF(repo);
		}
		
		addSavedContainee(childToAdd, codeRes, instanceRes2AFMap, repo);
	}

	private void addSavedCommentRes(
			DiagramModel parent, 
			Resource commentRes, 
			Map<Resource, ArtifactFragment> instanceRes2AFMap, 
			ReloRdfRepository repo) {
		Comment com=new Comment();
		//get text
		String text=repo.getStatement(commentRes, Comment.commentTxt, null).getObject().toString();
		com.setAnnoLabelText(text);
		parent.addComment(com);
		//get position	
		String posX=repo.getStatement(commentRes, PointPositionedDiagramPolicy.posX, null).getObject().toString();
		String posY=repo.getStatement(commentRes, PointPositionedDiagramPolicy.posY, null).getObject().toString();

		int x=Integer.parseInt(posX);
		int y=Integer.parseInt(posY);
		Point createLoc=new Point(x,y);
		Statement stmt = repo.getStatement(commentRes, Comment.anchoredComment, null);
		if (stmt != null) {
			com.setAnchored(true);
			com.setRelDistance(createLoc);
		}
		setCommentLocation(com, createLoc);
		instanceRes2AFMap.put(commentRes, com);
	}

	private void addSavedConnections(DiagramModel diagramModel, Map<Resource, ArtifactFragment> instanceRes2AFMap, ReloRdfRepository repo) {
		Map<Resource, ArtifactRel> instanceRes2CommentRelMap = new HashMap<Resource, ArtifactRel>();

		StatementIterator connectionsIter = repo.getStatements(null, repo.rdfType, RSECore.link);
		while(connectionsIter.hasNext()) {
			try {
				Resource connRes = connectionsIter.next().getSubject();
				if(connRes==null) throw new Exception("Resource of model is NULL.");

				URI linkRes = (URI) repo.getStatement(connRes, RSECore.model, null).getObject();
				ArtifactRel artRel = PluggableTypes.getAR(linkRes);
				if (artRel instanceof NamedRel) {
					// Source and/or target of connection is a comment
					ArtifactRel.readRDF(diagramModel, repo, connRes, instanceRes2AFMap, instanceRes2CommentRelMap);
					continue;
				}

				// Source and target of connection are members

				// connRes is like node15vfsf791x41, which will fail the NameGuesser's
				// RJCore.isJDTWksp(res) test, resulting in a null/"" guessed name. 
				// Instead, we need to query the repo for the connection's name 
				// statement that was explicitly written in the saved file
				String message;
				Statement stmt = repo.getStatement(connRes, RSECore.name, null);
				if(stmt!=null && stmt.getObject()!=null) 
					message = stmt.getObject().toString();
				else message = repo.queryName(connRes);
				if(message==null)  logger.error("Message null for connection " + connRes);

				
				Value connTypeVal = repo.getStatement(connRes, RSECore.model, null).getObject();
				if(!(connTypeVal instanceof URI))
					throw new Exception("Could not get type (call or return) " +
							"for connection resource:"+connRes.toString());
				URI connType = (URI) connTypeVal;
				
				ArtifactFragment srcAF = instanceRes2AFMap.get( repo.getStatement(connRes, repo.rdfSubject, null).getObject() );
				ArtifactFragment tgtAF = instanceRes2AFMap.get( repo.getStatement(connRes, repo.rdfObject, null).getObject() );
//				if(!(srcAF instanceof MemberModel)) 
//					throw new Exception("Could not get source model of connection:"+connRes.toString());
//				if(!(tgtAF instanceof MemberModel))
//					throw new Exception("Could not get target model of connection:"+connRes.toString());
//
//				MemberModel sourceModel = (MemberModel) srcAF;
//				MemberModel targetModel = (MemberModel) tgtAF;
//
//				ConnectionUtil.createConnection(message, sourceModel, targetModel, connType);
				if (srcAF == null || tgtAF == null) {
					logger.error("Cannot create connection. Invalid Source/target for resource: "
							+ connRes + " ::" + message
							+ "\nSource: " + srcAF
							+ "\nTarget: " + tgtAF);
				}
				ConnectionUtil.createConnection(message, srcAF, tgtAF, connType);

			} catch(Exception e) {
				logger.error("Error while creating connection\n", e);
			}
		}
		connectionsIter.close();
	}
	
	private void addInstancePanel() {
		final Figure instancePanel = new Figure();
		instancePanel.setSize(new Dimension(300, DiagramModel.TOP_MARGIN));
		ToolbarLayout tb = new AnimationLayoutManager(true);
		tb.setSpacing(30);
		tb.setStretchMinorAxis(false);
		instancePanel.setLayoutManager(tb);
		instancePanel.setBackgroundColor(ColorScheme.instancePanelBackground);
		instancePanel.setOpaque(true);
		BottomShadowBorder bottomBorder = new BottomShadowBorder();
		bottomBorder.setColor(ColorScheme.instancePanelBottomBorder);
		int allSides = DiagramModel.INSTANCE_PANEL_MARGINS;
		instancePanel.setBorder(new CompoundBorder(bottomBorder, new MarginBorder(allSides,allSides,allSides,allSides)));

		((DiagramModel)getModel()).setInstancePanel(instancePanel);

		SeqOrderedLayoutEditPolicy policy = (SeqOrderedLayoutEditPolicy) getEditPolicy(EditPolicy.LAYOUT_ROLE);
		policy.addInstancesPanel(instancePanel);

		getFigure().addFigureListener(new FigureListener() {
			public void figureMoved(IFigure source) {
				instancePanel.setSize(getFigure().getBounds().width, instancePanel.getBounds().height);
			}
		});
	}

	public ConnectionLayer getConnectionLayer() {
		return (ConnectionLayer)getLayer(LayerConstants.CONNECTION_LAYER);
	}

	@Override
	protected List<ArtifactFragment> getModelChildren() {
		List<ArtifactFragment> allChildren = new ArrayList<ArtifactFragment>();
		allChildren.addAll( (List<ArtifactFragment> ) ((DiagramModel)getModel()).getChildren()  ) ;
		allChildren.addAll(getModelConditionalChildren());
		return allChildren;
	}

	protected List<ControlFlowModel> getModelConditionalChildren() {
		return ((DiagramModel)getModel()).getConditionalChildren();
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		super.propertyChange(evt);
		String propName = evt.getPropertyName();
		if(NodeModel.PROPERTY_CHILDREN.equals(propName)) {
			refreshChildren();
		} else if(NodeModel.PROPERTY_CONDITIONAL_CHILDREN.equals(propName)) {
			refreshChildren();
		} else if(NodeModel.PROPERTY_REORDER.equals(propName)) {
			ArtifactFragment childModel = (ArtifactFragment) evt.getOldValue();
			EditPart childEP = null;
			for(Object ep : getChildren()) {
				if(ep instanceof EditPart && ((EditPart)ep).getModel().equals(childModel)) {
					childEP = (EditPart) ep;
					break;
				}
			}
			if(childEP != null) reorderChild(childEP, (Integer)evt.getNewValue());
		}
	}

	@Override
	public void execute(Command command) {
//		AccountStatus.updateDiagramActionMap(getRoot().toString(), command.getLabel());
//		AccountStatus.addUsage("Chrono > " + command.getLabel());
		if(command instanceof BreakableCommand) {
			BreakableCommand.execute(getViewer().getEditDomain().getCommandStack(), (BreakableCommand)command);
		} else getViewer().getEditDomain().getCommandStack().execute(command);
	}

	@Override
	protected void addChildVisual(EditPart childEditPart, int index) {
		IFigure figure = ((AbstractGraphicalEditPart)childEditPart).getFigure();
		if(figure instanceof InstanceFigure) {
			InstanceFigure instanceFig = ((InstanceFigure)figure);
			((DiagramModel)getModel()).getInstancePanel().add(instanceFig, index);
			getLayer(SeqEditor.LIFE_LINE_IN_INSTANCE_PANEL_LAYER).add(instanceFig.getLifeLineInInstancePanel());
			getContentPane().add(instanceFig.getChildrenContainer(), index);
			getLayer(SeqEditor.LIFE_LINE_LAYER).add(instanceFig.getLifeLine());
		} else if(figure instanceof HiddenFigure) {
			super.addChildVisual(childEditPart, index);
			((DiagramModel)getModel()).getInstancePanel().add(new HiddenFigure(false, ""), index);
		} else if (figure instanceof ControlFlowBlock) {
			SeqOrderedLayoutEditPolicy policy = (SeqOrderedLayoutEditPolicy) getEditPolicy(EditPolicy.LAYOUT_ROLE);
			policy.addConditionalFigure(figure);
		} else {
			super.addChildVisual(childEditPart, index);
		}
	}

	@Override
	protected void removeChildVisual(EditPart childEditPart) {
		IFigure figure = ((AbstractGraphicalEditPart)childEditPart).getFigure();
		if(figure instanceof InstanceFigure) {
			InstanceFigure instanceFig = ((InstanceFigure)figure);
			((DiagramModel)getModel()).getInstancePanel().remove(instanceFig);
			getLayer(SeqEditor.LIFE_LINE_IN_INSTANCE_PANEL_LAYER).remove(instanceFig.getLifeLineInInstancePanel());
			getContentPane().remove(instanceFig.getChildrenContainer());
			getLayer(SeqEditor.LIFE_LINE_LAYER).remove(instanceFig.getLifeLine());
		} else if(figure instanceof HiddenFigure) {
			int index = getContentPane().getChildren().indexOf(figure);
			List<?> instancePanelChildren = ((DiagramModel)getModel()).getInstancePanel().getChildren();
			if(index >= 0 && index < instancePanelChildren.size() && 
					instancePanelChildren.get(index) instanceof HiddenFigure) {
				((DiagramModel)getModel()).getInstancePanel().remove((HiddenFigure)instancePanelChildren.get(index));
			}
			super.removeChildVisual(childEditPart);
		} else if (figure instanceof ControlFlowBlock) {
			SeqOrderedLayoutEditPolicy policy = (SeqOrderedLayoutEditPolicy) getEditPolicy(EditPolicy.LAYOUT_ROLE);
			policy.removeConditionalFigure(figure);
		}  else {
			super.removeChildVisual(childEditPart);
		}
	}

	public void mouseDragged(MouseEvent me) {}
	public void mouseEntered(MouseEvent me) {}
	public void mouseExited(MouseEvent me) {}
	public void mouseHover(MouseEvent me) {}

	public void mouseMoved(MouseEvent me) {
		updateConditionalUnderMouse(me.getLocation());
	}

	private void updateConditionalUnderMouse(Point mouseLocation) {
		for (ControlFlowEditPart controlFlowEP : getRootConditionalChildren()) {
			controlFlowEP.refresh();
			// make sure loaded diagram puts figs in the right spot
			if (!((ControlFlowModel)controlFlowEP.getModel()).getInnerConditionalModels().isEmpty()) {
				controlFlowEP.refresh();
			}
			ControlFlowBlock block = null;
			if(controlFlowEP!=null) { 
				if ( ((ControlFlowBlock) controlFlowEP.getFigure()).getBounds().contains(mouseLocation)) {
					block = ((ControlFlowBlock) controlFlowEP.getFigure());
				}
				else {
					ControlFlowBlock block2 = ((ControlFlowBlock) controlFlowEP.getFigure());
					block2.showOrHide(false);
					controlFlowEP.setSelected(SELECTED_NONE);
				}
			} 
			if(block==null) continue;

			block.showOrHide(true);
			controlFlowEP.setSelected(SELECTED_PRIMARY);
			block.showOrHideOuterBlock(mouseLocation);
		}
	}

	
	private List<ControlFlowEditPart> getRootConditionalChildren() {
		List<ControlFlowEditPart> rootChildrenList = new ArrayList<ControlFlowEditPart>();
		for (Object ep : getChildren()) {
			if (!(ep instanceof ControlFlowEditPart)) 
				continue;
			rootChildrenList.add((ControlFlowEditPart) ep);
		}
		return rootChildrenList;
	}
	
	public List<InstanceEditPart> getRootInstanceChildren() {
		List<InstanceEditPart> rootChildrenList = new ArrayList<InstanceEditPart>();
		for (Object ep : getChildren()) {
			if (!(ep instanceof InstanceEditPart)) 
				continue;
			rootChildrenList.add((InstanceEditPart) ep);
		}
		return rootChildrenList;
	}
	

	private Label warning;

	public void addUnbuiltWarning() {
		warning.setText(" \n  Some elements in this diagram may not have been indexed." +
				"\n  To update your index, go to the menu: Architexa->Update Indexes");
	}

	@Override
	public void performRequest(Request request) {
		if (request.getType() == RequestConstants.REQ_DIRECT_EDIT || request.getType() == RequestConstants.REQ_OPEN) {
			for (ControlFlowEditPart controlFlowEP : getRootConditionalChildren()) {
				controlFlowEP.refresh();
				// make sure loaded diagram puts figs in the right spot
				if (!((ControlFlowModel)controlFlowEP.getModel()).getInnerConditionalModels().isEmpty()) {
					controlFlowEP.refresh();
				}
				//ControlFlowBlock block = null;
				if (controlFlowEP != null && controlFlowEP instanceof UserCreatedControlFlowEditPart) {
					if ( ((ControlFlowBlock) controlFlowEP.getFigure()).getBounds().contains(((LocationRequest) request).getLocation())) {
						((UserCreatedControlFlowEditPart) controlFlowEP).performDirectEdit();
					}
				} 
			}
			
		}
		else
			super.performRequest(request);
	}
}
