package com.architexa.diagrams.chrono.editpolicies;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;

import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.openrdf.model.Resource;

import com.architexa.diagrams.chrono.commands.InstanceCreateCommand;
import com.architexa.diagrams.chrono.commands.MemberCreateCommand;
import com.architexa.diagrams.chrono.commands.ReorderNodeCommand;
import com.architexa.diagrams.chrono.commands.UserInstanceCreateCommand;
import com.architexa.diagrams.chrono.commands.UserMethodBoxCreateCommand;
import com.architexa.diagrams.chrono.controlflow.CollapseExpandButton;
import com.architexa.diagrams.chrono.controlflow.ControlFlowBlock;
import com.architexa.diagrams.chrono.editparts.DiagramEditPart;
import com.architexa.diagrams.chrono.editparts.GroupedInstanceEditPart;
import com.architexa.diagrams.chrono.editparts.InstanceEditPart;
import com.architexa.diagrams.chrono.editparts.MemberEditPart;
import com.architexa.diagrams.chrono.editparts.MethodBoxEditPart;
import com.architexa.diagrams.chrono.editparts.SeqNodeEditPart;
import com.architexa.diagrams.chrono.figures.InstanceFigure;
import com.architexa.diagrams.chrono.figures.MemberFigure;
import com.architexa.diagrams.chrono.models.DiagramModel;
import com.architexa.diagrams.chrono.models.InstanceModel;
import com.architexa.diagrams.chrono.models.MemberModel;
import com.architexa.diagrams.chrono.models.MethodBoxModel;
import com.architexa.diagrams.chrono.models.NodeModel;
import com.architexa.diagrams.chrono.models.UserCreatedInstanceModel;
import com.architexa.diagrams.chrono.models.UserCreatedMethodBoxModel;
import com.architexa.diagrams.chrono.ui.SeqEditor;
import com.architexa.diagrams.chrono.util.GroupedUtil;
import com.architexa.diagrams.chrono.util.InstanceUtil;
import com.architexa.diagrams.commands.AddCommentCommand;
import com.architexa.diagrams.commands.MoveCommentCommand;
import com.architexa.diagrams.figures.CommentFigure;
import com.architexa.diagrams.jdt.JDTSelectionUtils;
import com.architexa.diagrams.jdt.RJCore;
import com.architexa.diagrams.model.ArtifactFragment;
import com.architexa.diagrams.model.Comment;
import com.architexa.diagrams.parts.CommentEditPart;
import com.architexa.diagrams.utils.BuildPreferenceUtils;
import com.architexa.diagrams.utils.ErrorUtils;
import com.architexa.diagrams.utils.RootEditPartUtils;
import com.architexa.org.eclipse.draw2d.IFigure;
import com.architexa.org.eclipse.draw2d.geometry.Point;
import com.architexa.org.eclipse.draw2d.geometry.Rectangle;
import com.architexa.org.eclipse.gef.EditPart;
import com.architexa.org.eclipse.gef.EditPolicy;
import com.architexa.org.eclipse.gef.GraphicalEditPart;
import com.architexa.org.eclipse.gef.LayerConstants;
import com.architexa.org.eclipse.gef.Request;
import com.architexa.org.eclipse.gef.commands.Command;
import com.architexa.org.eclipse.gef.commands.CompoundCommand;
import com.architexa.org.eclipse.gef.editpolicies.LayoutEditPolicy;
import com.architexa.org.eclipse.gef.editpolicies.OrderedLayoutEditPolicy;
import com.architexa.org.eclipse.gef.requests.ChangeBoundsRequest;
import com.architexa.org.eclipse.gef.requests.CreateRequest;
import com.architexa.store.ReloRdfRepository;
import com.architexa.store.StoreUtil;

/**
 * 
 * @author Elizabeth L. Murnane
 *
 */

public class SeqOrderedLayoutEditPolicy extends OrderedLayoutEditPolicy {

	public final static String SEQ_EDIT_POLICY = "SeqOrderedLayoutEditPolicy";

	@Override
	public Command getCommand(Request request) {
		if (request.getType().equals(REQ_RESIZE_CHILDREN)) {
			Command cmd = RootEditPartUtils.getResizeCommand((ChangeBoundsRequest)request);
			if(cmd!=null) return cmd;
		}
		return super.getCommand(request);
	}

	@Override
	protected EditPolicy createChildEditPolicy(EditPart child) {
		//if (child instanceof SeqNodeEditPart)
		//	return new SeqNodeEditPolicy();
		return new SeqSelectionEditPolicy();
	}

	@Override
	protected Command getCreateCommand(CreateRequest request) {
		
		List<?> reqList;

		Object newObj = request.getNewObject();
		if (newObj instanceof List<?>) {
    		reqList = (List<?>)newObj;
    	} else {
    		reqList = Arrays.asList(newObj);
    	}

		if (!BuildPreferenceUtils.selectionInBuild(reqList)) return null;

		EditPart host = getHost();
		while(!(host instanceof DiagramEditPart)) {
			host = host.getParent();
		}
		ReloRdfRepository repo = StoreUtil.getDefaultStoreRepository();
		DiagramModel parent = (DiagramModel) host.getModel();

		EditPart after = getInsertionReference(request);
		int index = host.getChildren().indexOf(after);
		//First check if a request has been sent by the user (eg Palette)
		//Check selections if the newObj is null
		//TODO TEST>>>>>>>>>
//		Object newObj = request.getNewObject();
		if (newObj == null) // Use JDTSelUtils so that we get any spring elmts
			newObj = JDTSelectionUtils.getSelectedJDTElements(true);
//		if (((List<?>)newObj).isEmpty())
//			newObj = request.getNewObject();
		if (newObj instanceof List<?>) {
			CompoundCommand createMultipleCmd = new CompoundCommand();
			for(Object o : ((List<?>)newObj)) {
				createMultipleCmd.add(getCreateCommand(o, parent, index, repo, request));
			}
			if (!createMultipleCmd.canExecute())
				ErrorUtils.openError("Class, method, or field");
			return createMultipleCmd;
		} else return getCreateCommand(newObj, parent, index, repo, request);
	}

	private Command getCreateCommand(final Object obj, final DiagramModel parent, final int index, final ReloRdfRepository repo, final CreateRequest req) {
		if (obj instanceof IMethod && ((IMethod)obj).getParent() instanceof IType) {

			CompoundCommand cc = new CompoundCommand("Create Member");
			IMethod method = (IMethod) obj;
			Resource classRes = RJCore.jdtElementToResource(repo, method.getDeclaringType());
			InstanceModel instance = InstanceUtil.findOrCreateContainerInstanceModel(null, InstanceUtil.getClassName(classRes, repo), classRes, parent, -1, cc);

			MethodBoxModel methodModel = new MethodBoxModel(instance, method, MethodBoxModel.declaration);
			MemberCreateCommand methodCmd = new MemberCreateCommand(methodModel, instance, MemberCreateCommand.NONE);
			cc.add(methodCmd);
			return cc;
		}
		if (obj instanceof IType || obj instanceof ICompilationUnit || obj instanceof IClassFile/*JarClassFiles*/) {
			IJavaElement selectedElement = (IJavaElement)obj;
			Resource classRes = RJCore.jdtElementToResource(repo, selectedElement);
			InstanceModel newChild = new InstanceModel(null, InstanceUtil.getClassName(classRes, repo), classRes);
			InstanceCreateCommand cmd = new InstanceCreateCommand((InstanceModel)newChild, parent, index, true, true);
			return cmd;
		} 

		if (obj instanceof Comment) {
			Point createLoc = req.getLocation();
			// Code to handle positioning when diagram is scrolled
			IFigure referenceFig = ((GraphicalEditPart)getHost()).getFigure();
			referenceFig.translateToRelative(createLoc);
			AddCommentCommand addComment = new AddCommentCommand(getDiagramModel(), (Comment) obj, createLoc);
			return addComment;
		}
		
		if (obj instanceof UserCreatedInstanceModel) {
			return new UserInstanceCreateCommand(getDiagramModel(), (UserCreatedInstanceModel) obj, index);
		}
		
		if (obj instanceof UserCreatedMethodBoxModel) {
			EditPart droppenOn = getPartDroppedOn(req);
			if (droppenOn == null || 
					!(droppenOn instanceof InstanceEditPart)) 
				return null;
			return new UserMethodBoxCreateCommand((MethodBoxModel)obj, (ArtifactFragment) droppenOn.getModel(), "");
		}
		return null;
	}

	private EditPart getPartDroppedOn(CreateRequest req) {
		Object newObject = req.getNewObject();
		if(newObject instanceof List<?>){
			List<?> objects = (List<?>)newObject;
			for(Object unit : objects){
				if(!(unit instanceof ICompilationUnit)) return null;
			}
		}
		else if(!(newObject instanceof ICompilationUnit) && 
				!(newObject instanceof NodeModel)) return null;

		// Handle case where a instance is dropped upon another instance
		if(getHost() instanceof InstanceEditPart)
			return getHost();

		return null;
	}
	
	private DiagramModel getDiagramModel() {
		DiagramModel diagram;
		DiagramEditPart diagramEP = null;
		ArtifactFragment model = (ArtifactFragment) getHost().getModel();
		if (model instanceof NodeModel) {
			if (model instanceof MemberModel) {
				diagram = (DiagramModel) ((MemberModel)model).getInstanceModel().getParentArt();
				diagramEP = (DiagramEditPart) ((MemberEditPart)getHost()).getParent().getParent();
			}
			else {
				diagram = (DiagramModel) ((NodeModel)model).getParentArt();
				diagramEP = (DiagramEditPart) ((InstanceEditPart)getHost()).getParent();
			}
		} else {
			diagramEP = ((DiagramEditPart)getHost());
			diagram=(DiagramModel) diagramEP.getModel();
		}
		return diagram;
	}
	
	@Override
	protected Command createAddCommand(EditPart child, EditPart after) {
		return null;
	}

	@Override
	protected Command createMoveChildCommand(EditPart child, EditPart after) {
		return createMoveChildCommand(child, after, null);
	}

	protected Command createMoveChildCommand(EditPart child, EditPart after, Request req) {
		if (!(child instanceof InstanceEditPart) &&
				!(child instanceof MethodBoxEditPart) && 
				!(child instanceof CommentEditPart)) 
			return null;

		if (child instanceof CommentEditPart && req instanceof ChangeBoundsRequest){
			Rectangle origBounds = ((CommentEditPart)child).getFigure().getBounds();
			Rectangle newBounds = ((ChangeBoundsRequest)req).getTransformedRectangle(origBounds);
			return new MoveCommentCommand((Comment)child.getModel(), origBounds, newBounds);
		}
		SeqNodeEditPart childEP = (SeqNodeEditPart) child;
		ArtifactFragment childModel;
		if (child instanceof GroupedInstanceEditPart){
			childModel = (NodeModel) child.getModel();
		} else {
			childModel = (ArtifactFragment) child.getModel();
		}
		ArtifactFragment parentModel = (ArtifactFragment)getHost().getModel();

		EditPart parent = getHost();
		int oldIndex = parent.getChildren().indexOf(child);
		int newIndex = after==null ? parent.getChildren().size()-1 : parent.getChildren().indexOf(after);
		if (parent instanceof DiagramEditPart && after == null) {
			newIndex = ((DiagramEditPart)parent).getRootInstanceChildren().size() - 1;
		}
		if (after != null && newIndex > oldIndex) newIndex--;
		if (oldIndex == newIndex) return null;

		ReorderNodeCommand command = new ReorderNodeCommand(childEP, childModel, parentModel, newIndex);
		return command;
	}

	@Override
	protected EditPart getInsertionReference(Request req) {
		if(req instanceof CreateRequest) return getInsertionReferenceForCreate((CreateRequest)req);
		if(req instanceof ChangeBoundsRequest) return getInsertionReferenceForMove((ChangeBoundsRequest)req);
		return null;
	}

	private EditPart getInsertionReferenceForCreate(CreateRequest request) {
		Object newObject = request.getNewObject();
		if(newObject instanceof List<?>){
			List<?> objects = (List<?>)newObject;
			for(Object unit : objects){
				if(!(unit instanceof ICompilationUnit)) return null;
			}
		}
		else if(!(newObject instanceof ICompilationUnit)) return null;

		EditPart referencePart = null;
		int dropLocation = request.getLocation().x;
		for(Object child : getHost().getChildren()) {
			if(!(child instanceof InstanceEditPart)) continue;
			if(((InstanceFigure)((InstanceEditPart)child).getFigure()).getLocation().x >= dropLocation) {
				referencePart = (InstanceEditPart) child;
				break;
			}
		}

		// Handle case where a instance is dropped upon another instance
		if(referencePart == null && getHost() instanceof InstanceEditPart){
			InstanceEditPart instanceEP = (InstanceEditPart) getHost(); 
			InstanceFigure instanceFig = (InstanceFigure) instanceEP.getFigure();
			int index = instanceEP.getParent().getChildren().indexOf(instanceEP);
			Point center = instanceFig.getBounds().getCenter();
			instanceFig.translateToAbsolute(center); // to counter any scroll in the diagram
			if(dropLocation <= center.x)
				referencePart = getHost();
			else if(index < (instanceEP.getParent().getChildren().size() - 1)){ // check if the EP is not the last in the list
				referencePart = (EditPart) instanceEP.getParent().getChildren().get(index+1);
			}
		}

		return referencePart;
	}

	private EditPart getInsertionReferenceForMove(ChangeBoundsRequest request) {

		boolean instanceMakingRequest = false;
		boolean methodMakingRequest = false;
		boolean otherMakingRequest = false;
		for(Object editPart : request.getEditParts()) {
			if(editPart instanceof InstanceEditPart) instanceMakingRequest = true;
			else if(editPart instanceof MethodBoxEditPart) methodMakingRequest = true;
			else otherMakingRequest = true;
		}

		// Can find an insertion reference only if instances or methods are making the
		// request and only if the parts making the request are all the same type (all of 
		// the selected parts are instances or all of the selected parts are methods)
		if(otherMakingRequest==true || instanceMakingRequest==methodMakingRequest) return null;

		if(instanceMakingRequest) {
			int dropLocation = request.getLocation().x;
			EditPart parentEditPart = ((InstanceEditPart)request.getEditParts().get(0)).getParent();
			for(Object child : parentEditPart.getChildren()) {
				if(!(child instanceof InstanceEditPart)) continue;

				Rectangle instanceBounds = Rectangle.SINGLETON;
				instanceBounds.setBounds(((InstanceFigure)((InstanceEditPart)child).getFigure()).getBounds());
				((InstanceFigure)((InstanceEditPart)child).getFigure()).translateToAbsolute(instanceBounds);

				if(instanceBounds.x >= dropLocation && !request.getEditParts().contains(child)) {
					return (InstanceEditPart) child;
				}
			}
		} else if(methodMakingRequest) {
			int dropLocation = request.getLocation().y;
			EditPart parentEditPart = ((MethodBoxEditPart)request.getEditParts().get(0)).getParent();
			for(Object child : parentEditPart.getChildren()) {
				Rectangle methodBoxBounds = Rectangle.SINGLETON;
				methodBoxBounds.setBounds(((MemberFigure)((MemberEditPart)child).getFigure()).getBounds());
				((MemberFigure)((MemberEditPart)child).getFigure()).translateToAbsolute(methodBoxBounds);

				if(methodBoxBounds.y >= dropLocation && !request.getEditParts().contains(child)) {
					return (MemberEditPart) child;
				}
			}
		}
		return null;
	}

	/**
	 * A move is interpreted here as a change in order of the children. This method obtains
	 * the proper index, and then calls {@link #createMoveChildCommand(EditPart, EditPart, Request)},
	 * which subclasses must implement. Subclasses should not override this method.
	 * @see LayoutEditPolicy#getMoveChildrenCommand(Request)
	 */
	@Override
	protected Command getMoveChildrenCommand(Request request) {
		CompoundCommand command = new CompoundCommand("Move");
		List<?> editParts = ((ChangeBoundsRequest)request).getEditParts();

		boolean instanceMakingRequest = false;
		boolean memberMakingRequest = false;
		boolean commentMakingRequest = false;
		for(Object editPart : editParts) {
			if(editPart instanceof InstanceEditPart) instanceMakingRequest = true;
			else if(editPart instanceof MemberEditPart) memberMakingRequest = true;
			else if(editPart instanceof CommentEditPart)commentMakingRequest = true;
		}
		// check for only one type of edit part a, b or c 
		// (!(((a ^ b) ^ c) ^ (a & b & c)))
		if (!(((instanceMakingRequest ^ memberMakingRequest) ^commentMakingRequest) ^
				(instanceMakingRequest & memberMakingRequest & commentMakingRequest)))
			return null;

		EditPart insertionReference = getInsertionReference(request);

		if (commentMakingRequest) {
			for (int i = 0; i < editParts.size(); i++) {
				EditPart child = (EditPart)editParts.get(i);
				command.add(createMoveChildCommand(child, insertionReference, request));
			}
			return command;
		}

		InstanceEditPart editPartDroppedOn = null;
		if(request instanceof ChangeBoundsRequest){
			editPartDroppedOn = getEditPartDroppedOn((ChangeBoundsRequest)request);
		}

		if(editPartDroppedOn!=null){
			List<InstanceEditPart> toGroup = new ArrayList<InstanceEditPart>();
			toGroup.add(editPartDroppedOn);
			for(int i = 0; i < editParts.size(); i++){
				EditPart child = (EditPart)editParts.get(i);
				toGroup.add((InstanceEditPart) child);
			}
			DiagramModel diagram = (DiagramModel) getHost().getModel();
			command.setLabel("Instance Grouping");
			GroupedUtil.createGroupFromEditParts(toGroup, diagram, command);
			command.unwrap();
			return command;
		}

		TreeMap<Integer,EditPart> indexToEditPart = new TreeMap<Integer, EditPart>();
		EditPart parent = ((EditPart) editParts.get(0)).getParent();
		for (int i = 0; i < editParts.size(); i++) {
			EditPart child = (EditPart)editParts.get(i);
			indexToEditPart.put(parent.getChildren().indexOf(child), child);
		}
		int refIndex = parent.getChildren().size(); // default last index
		if(insertionReference != null)
		{
			SeqNodeEditPart refEP = (SeqNodeEditPart) insertionReference;
			refIndex = parent.getChildren().indexOf(refEP);
		}

		// check added for cases where multiple selection is not moved beyond any other instance or ref index is in between first and last key
		if(refIndex == indexToEditPart.lastKey()+1 || (refIndex >= indexToEditPart.firstKey() && refIndex <= indexToEditPart.lastKey() )) 
			return null;

		Set<Integer> keyset;
		if(refIndex > indexToEditPart.firstKey()){
			keyset = indexToEditPart.keySet();
		}else{
			TreeMap<Integer,EditPart> reverseMap = new TreeMap<Integer, EditPart>(Collections.reverseOrder());
			reverseMap.putAll(indexToEditPart);
			keyset = reverseMap.keySet();
		}

		for(Integer index : keyset){
			EditPart child = indexToEditPart.get(index);
			command.add(createMoveChildCommand(child, insertionReference, request));
		}


		return command.unwrap();
	}

	// Method to find if an instance (Grouped/UnGrouped) is being dragged over another instance.
	private InstanceEditPart getEditPartDroppedOn(ChangeBoundsRequest request) {
		boolean instanceMakingRequest = false;
		boolean methodMakingRequest = false;
		boolean otherMakingRequest = false;
		boolean groupMakingRequest = false;

		for(Object editPart : request.getEditParts()) {
			if(editPart instanceof InstanceEditPart) instanceMakingRequest = true;
			else if(editPart instanceof MethodBoxEditPart) methodMakingRequest = true;
			else if(editPart instanceof GroupedInstanceEditPart) groupMakingRequest = true;
			else otherMakingRequest = true;
		}

		if(otherMakingRequest==true || methodMakingRequest == true) return null;

		if(instanceMakingRequest || groupMakingRequest) {
			Point dropLoc =  request.getLocation();
			EditPart parentEditPart = ((EditPart)request.getEditParts().get(0)).getParent();
			List<?> children = parentEditPart.getChildren();
			if (parentEditPart instanceof DiagramEditPart)
				children = ((DiagramEditPart)parentEditPart).getRootInstanceChildren();
			
			for(Object child : children) {
				if(request.getEditParts().contains(child)) continue;

				Rectangle instanceBounds = Rectangle.SINGLETON;
				instanceBounds.setBounds(((InstanceFigure)((InstanceEditPart)child).getFigure()).getBounds());
				((InstanceFigure)((InstanceEditPart)child).getFigure()).translateToAbsolute(instanceBounds);

				if(instanceBounds.getTopLeft().x < dropLoc.x && instanceBounds.getTopLeft().y < dropLoc.y 
						&& instanceBounds.getBottomRight().x > dropLoc.x && instanceBounds.getBottomRight().y > dropLoc.y){
					((InstanceFigure)((InstanceEditPart) child).getFigure()).highlightGroupedInstance();
					return (InstanceEditPart) child;
				}
				if(((InstanceFigure)((InstanceEditPart) child).getFigure()).isGrouped)
					((InstanceFigure)((InstanceEditPart) child).getFigure()).unHighlight();
			}
		} 

		return null;
	}

	public void addCommentFigure(IFigure figure) {
		if(!(figure instanceof CommentFigure)) return;
		getLayer(SeqEditor.COMMENT_LAYER).add(figure);
	}	

	public void addConditionalFigure(IFigure figure) {
		if(!(figure instanceof ControlFlowBlock)) return;

		addFeedback(figure);
		ControlFlowBlock block = (ControlFlowBlock) figure;
		getLayer(SeqEditor.CONDITIONAL_LAYER).add(block.getHighlight());

		for(CollapseExpandButton button : block.getCollapseExpandButtons()) {
			getHandleLayer().add(button);
		}
	}

	public void removeCommentFigure(IFigure figure){
		if(!(figure instanceof CommentFigure)) return;
		getLayer(SeqEditor.COMMENT_LAYER).remove(figure);
	}

	public void removeConditionalFigure(IFigure figure) {
		if(!(figure instanceof ControlFlowBlock)) return;

		removeFeedback(figure);
		ControlFlowBlock block = (ControlFlowBlock) figure;
		getLayer(SeqEditor.CONDITIONAL_LAYER).remove(block.getHighlight());

		for(CollapseExpandButton button : block.getCollapseExpandButtons()) {
			getHandleLayer().remove(button);
		}
	}

	public void addInstancesPanel(IFigure panel) {
		getLayer(SeqEditor.INSTANCE_PANEL_LAYER).add(panel);
	}

	private  IFigure getHandleLayer() {
		return getLayer(LayerConstants.HANDLE_LAYER);
	}
	
//Also look getFeedbackLayer in SeqSelectionEpol and getButtonLayer in SeqNodeEP
	@Override
	protected IFigure getFeedbackLayer() {
		return getLayer(LayerConstants.SCALED_FEEDBACK_LAYER);
	}
}