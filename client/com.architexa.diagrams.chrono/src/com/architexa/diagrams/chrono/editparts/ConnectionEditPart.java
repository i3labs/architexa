package com.architexa.diagrams.chrono.editparts;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.NullLiteral;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jface.viewers.TextCellEditor;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;

import com.architexa.diagrams.chrono.animation.AnimateChainExpandCommand;
import com.architexa.diagrams.chrono.editpolicies.SeqConnectionEditPolicy;
import com.architexa.diagrams.chrono.editpolicies.SeqConnectionEndpointEditPolicy;
import com.architexa.diagrams.chrono.figures.ConnectionFigure;
import com.architexa.diagrams.chrono.figures.MemberFigure;
import com.architexa.diagrams.chrono.figures.MethodBoxFigure;
import com.architexa.diagrams.chrono.models.ConnectionModel;
import com.architexa.diagrams.chrono.models.DiagramModel;
import com.architexa.diagrams.chrono.models.HiddenNodeModel;
import com.architexa.diagrams.chrono.models.InstanceModel;
import com.architexa.diagrams.chrono.models.MemberModel;
import com.architexa.diagrams.chrono.models.MethodBoxModel;
import com.architexa.diagrams.chrono.models.NodeModel;
import com.architexa.diagrams.chrono.ui.ColorScheme;
import com.architexa.diagrams.chrono.util.ConnectionUtil;
import com.architexa.diagrams.chrono.util.MemberUtil;
import com.architexa.diagrams.chrono.util.MethodUtil;
import com.architexa.diagrams.draw2d.UnderlinableLabel;
import com.architexa.diagrams.eclipse.gef.UndoableLabelSource;
import com.architexa.diagrams.jdt.Invocation;
import com.architexa.diagrams.jdt.RJCore;
import com.architexa.diagrams.model.ArtifactFragment;
import com.architexa.diagrams.parts.AnnoLabelCellEditorLocator;
import com.architexa.diagrams.parts.AnnoLabelDirectEditPolicy;
import com.architexa.diagrams.utils.UILinkUtils;
import com.architexa.org.eclipse.draw2d.AbsoluteBendpoint;
import com.architexa.org.eclipse.draw2d.BendpointConnectionRouter;
import com.architexa.org.eclipse.draw2d.Connection;
import com.architexa.org.eclipse.draw2d.ConnectionRouter;
import com.architexa.org.eclipse.draw2d.IFigure;
import com.architexa.org.eclipse.draw2d.Label;
import com.architexa.org.eclipse.draw2d.ManhattanConnectionRouter;
import com.architexa.org.eclipse.draw2d.MouseEvent;
import com.architexa.org.eclipse.draw2d.MouseListener;
import com.architexa.org.eclipse.draw2d.MouseMotionListener;
import com.architexa.org.eclipse.draw2d.geometry.Point;
import com.architexa.org.eclipse.gef.EditPart;
import com.architexa.org.eclipse.gef.EditPolicy;
import com.architexa.org.eclipse.gef.GraphicalEditPart;
import com.architexa.org.eclipse.gef.Request;
import com.architexa.org.eclipse.gef.commands.Command;
import com.architexa.org.eclipse.gef.editparts.AbstractConnectionEditPart;
import com.architexa.org.eclipse.gef.tools.CellEditorLocator;
import com.architexa.org.eclipse.gef.tools.DirectEditManager;
import com.architexa.org.eclipse.gef.tools.LabelDirectEditManager;
import com.architexa.rse.BuildStatus;


/**
 * 
 * @author Elizabeth L. Murnane
 *
 */
public class ConnectionEditPart extends AbstractConnectionEditPart implements PropertyChangeListener, UndoableLabelSource {

	@Override
	public void activate() {
		if (isActive()) return;

		super.activate();
		ConnectionModel model = (ConnectionModel)getModel();
		model.addPropertyChangeListener(this);
		if(model.getSource() instanceof MethodBoxModel && model.getTarget() instanceof MethodBoxModel) {
			showOrHideNoConnectionsLabel((MethodBoxModel)model.getSource(), (MethodBoxModel)model.getTarget());
		}
	}

	@Override
	public void deactivate() {
		if (!isActive()) return;

		super.deactivate();
		ConnectionModel model = (ConnectionModel)getModel();
		model.removePropertyChangeListener(this);
		if(model.getSource() instanceof MethodBoxModel && model.getTarget() instanceof MethodBoxModel) {
			showOrHideNoConnectionsLabel((MethodBoxModel)model.getSource(), (MethodBoxModel)model.getTarget());
		}
	}

	@Override
	protected void createEditPolicies() {
		installEditPolicy(EditPolicy.CONNECTION_ENDPOINTS_ROLE, new SeqConnectionEndpointEditPolicy());
		installEditPolicy(EditPolicy.CONNECTION_ROLE, new SeqConnectionEditPolicy());
		installEditPolicy(EditPolicy.DIRECT_EDIT_ROLE, new AnnoLabelDirectEditPolicy(this, "Edit Connection Name"));
	}

	@Override
	protected IFigure createFigure() {
		ConnectionModel model = (ConnectionModel)getModel();
		List<Label> labelPieces = getMessageLabelPieces();
		String fullLabel = "";
		for(Label piece : labelPieces) fullLabel = fullLabel.concat(piece.getText());
		model.setLabel(fullLabel);
		ConnectionFigure connectionLine = new ConnectionFigure(model.getType(), labelPieces);
		model.setLine(connectionLine);
		connectionLine.addPropertyChangeListener(this);
		connectionLine.setConnectionRouter(getConnectionRouter());
		addMouseMotionListenerToConnectionLabel(connectionLine.getLabelContainer());
		return connectionLine;
	}

	private void addMouseMotionListenerToConnectionLabel(IFigure labelContainer){
		labelContainer.addMouseMotionListener(new MouseMotionListener() {
			public void mouseEntered(MouseEvent me) {
				((ConnectionFigure)getFigure()).showFullLabelText();
			}
			public void mouseExited(MouseEvent me) {
				int notSelected=EditPart.SELECTED_NONE;
				if(getSelected()==notSelected &&
						getSource() != null &&
						getSource().getSelected()==notSelected &&
						getTarget() != null &&
						getTarget().getSelected()==notSelected)
					((ConnectionFigure)getFigure()).showAbbreviatedLabelText();
			}
			public void mouseDragged(MouseEvent me) {}
			public void mouseHover(MouseEvent me) {}
			public void mouseMoved(MouseEvent me) {}
		});
	}

	public class UnfocusableLabelDirectEditManager extends LabelDirectEditManager{

		public UnfocusableLabelDirectEditManager(GraphicalEditPart source, Class<TextCellEditor> editorType,
				CellEditorLocator locator, IFigure directEditFigure) {
			super(source, editorType, locator, directEditFigure);
		}
		
		@Override
		protected void commit() {
//			showIsEditableTooltip(false);
			super.commit();
		}
	}
	
	@Override
	public void performRequest(Request req) { 
		if(REQ_OPEN.equals(req.getType())) {
			// Open the invocation corresponding to a connection 
			// in a java editor when it is double clicked
			EditPart src = getSource();
			EditPart tgt = getTarget();
			if (src instanceof UserCreatedMethodBoxEditPart || tgt instanceof UserCreatedMethodBoxEditPart){
				performDirectEdit();
				return;
			}
			
			if(src instanceof MemberEditPart && 
					((MemberModel)src.getModel()).isAccess())
				((MemberEditPart)src).openMemberInJavaEditor();
			else if(tgt instanceof MemberEditPart && 
					((MemberModel)tgt.getModel()).isAccess())
				((MemberEditPart)tgt).openMemberInJavaEditor();
		}
	}
	
	
	protected DirectEditManager manager;
	private String oldConnectionName;
	private void performDirectEdit() {
		if (manager == null)
			manager = new UnfocusableLabelDirectEditManager(this, TextCellEditor.class,
					new AnnoLabelCellEditorLocator(getAnnoLabelFigure()),
					getAnnoLabelFigure());
		manager.show();		
	}
	
	public IFigure getAnnoLabelFigure() {
		return ((ConnectionFigure)getFigure()).getFirstLabelChild();
	}
	
	public String getAnnoLabelText() {
		return ((ConnectionModel)getModel()).getLabel();
	}

	public String getOldAnnoLabelText() {
		return oldConnectionName;
	}
	
	public void setOldAnnoLabelText(String oldName) {
		oldConnectionName = oldName;
	}
	
	public void setAnnoLabelText(String str) {

		if (str == null) return;
		// Check for line feed(10) and carriage return(13)
		if (str.contains("\n") || str.contains("\r") || str.contains("\t")) {
			str.replaceAll("\n", "");
			str.replaceAll("\r", "");
			str.replaceAll("\t", "");
			((UnfocusableLabelDirectEditManager) manager).commit();
			return;
		}
		((ConnectionModel)getModel()).setLabel(str);
		((ConnectionFigure)getFigure()).setFirstLabelChild(str);
		updateUserCreatedMethodBox(str);
	}

	private void updateUserCreatedMethodBox(String str) {
		if (!((MethodBoxEditPart)getTarget() instanceof UserCreatedMethodBoxEditPart))
			return;
		UserCreatedMethodBoxEditPart methodEP = (UserCreatedMethodBoxEditPart) getTarget();
		methodEP.updateLabels(str, true);
			
	}

	private ConnectionRouter getConnectionRouter() {

		final URI connectionType = ((ConnectionModel)getModel()).getType();

		if(RJCore.overrides.equals(connectionType))
			return new ManhattanConnectionRouter();

		return new BendpointConnectionRouter() {
			@Override
			public void route(Connection conn) {

				IFigure source = conn.getSourceAnchor().getOwner();
				IFigure target = conn.getTargetAnchor().getOwner();
				if(!(source instanceof MemberFigure) || !(target instanceof MemberFigure)) {
					super.route(conn);
					return;
				}

				MemberFigure accessFig;
				MemberFigure declarationFig;
				if(ConnectionModel.CALL.equals(connectionType) 
						|| ConnectionModel.FIELD_READ.equals(connectionType) 
						|| ConnectionModel.FIELD_WRITE.equals(connectionType)) {
					accessFig = (MemberFigure)source;
					declarationFig = (MemberFigure)target;
				} else {
					accessFig = (MemberFigure)target;
					declarationFig = (MemberFigure)source;
				}
				if(!MemberUtil.isAnAccessToTheSameClass(accessFig, declarationFig)) {
					super.route(conn);
					return;
				}

				// The connection is a class making a call to one of
				// its own methods or accessing one of its own fields

				List<Object> bendpoints = new ArrayList<Object> (1);

				Point startPoint = getStartPoint(conn);
				conn.translateToRelative(startPoint);
				Point endPoint = getEndPoint(conn);
				conn.translateToRelative(endPoint);

				if(ConnectionModel.CALL.equals(connectionType)) {
					bendpoints.add(new AbsoluteBendpoint(endPoint.x, startPoint.y));
				} else if(ConnectionModel.FIELD_READ.equals(connectionType) 
						|| ConnectionModel.FIELD_WRITE.equals(connectionType)) {
					int bendout = MethodBoxFigure.DEFAULT_SIZE.width/2;
					bendpoints.add(new AbsoluteBendpoint(startPoint.x+bendout, startPoint.y));
					bendpoints.add(new AbsoluteBendpoint(endPoint.x+bendout, endPoint.y));
				}else {
					bendpoints.add(new AbsoluteBendpoint(startPoint.x, endPoint.y));
				}

				setConstraint(conn, bendpoints);
				super.route(conn);
			}
		};
	}

	public void propertyChange(PropertyChangeEvent event) {
		if (ConnectionModel.PROPERTY_SET_VISIBLE.equals(event.getPropertyName())) {
			boolean visible = (Boolean) event.getNewValue();
			getFigure().setVisible(visible);
		} else if (ConnectionModel.PROPERTY_REFRESH.equals(event.getPropertyName())) {
			setAnnoLabelText((String) event.getNewValue());
		}
	}

	private void showOrHideNoConnectionsLabel(MethodBoxModel source, MethodBoxModel target) {
		ArtifactFragment sourceContainerModel = source.getInstanceModel().getParentArt();
		ArtifactFragment targetContainerModel = target.getInstanceModel().getParentArt();
		if(sourceContainerModel instanceof HiddenNodeModel && !(targetContainerModel instanceof HiddenNodeModel)) {
			target.getFigure().showNoConnectionsBoxLabel();
		} else if(targetContainerModel instanceof HiddenNodeModel && !(sourceContainerModel instanceof HiddenNodeModel)) {
			source.getFigure().showNoConnectionsBoxLabel();
		} else {
			if (source.getFigure() != null)
				source.getFigure().hideNoConnectionsBoxLabel();
			if (target.getFigure() != null)
				target.getFigure().hideNoConnectionsBoxLabel();
		}
	}

	// each key is the label of one of the chained invocations, 
	// and it maps to the corresponding Invocation
	private Map<Label, Invocation> labelToInvocationMap = new LinkedHashMap<Label, Invocation>();
	// each key is some invocation in the chain's invocation argument's label, 
	// and it maps to the argument's corresponding Invocation
	private Map<Label, Invocation> labelToArgumentInvocationMap = new LinkedHashMap<Label, Invocation>();
	// each key is the label of one of the chained invocations, and it maps
	// to the labels of its arguments
	private Map<Label, List<Label>> labelToArgLabelsMap = new LinkedHashMap<Label, List<Label>>();

	private List<Label> getMessageLabelPieces() {
		List<Label> labelPieces = getChainedPieces();
		for(Label label : new ArrayList<Label>(labelPieces)) {
			getArgumentPieces(label, labelPieces.indexOf(label)+1, labelPieces);
		}
		return labelPieces;
	}

	// Gets a label for each invocation in the chain. For
	// example, a(), b(), c() for the chained call a().b().c()
	private List<Label> getChainedPieces() {

		ConnectionModel model = (ConnectionModel)getModel();
		List<Label> messageLabelPieces = new ArrayList<Label>();
		boolean fromChainedCall = true;
		if(!RJCore.calls.equals(model.getType()) || !(model.getSource() instanceof MethodBoxModel)) {
			messageLabelPieces.add(new Label(model.getLabel()));
			return messageLabelPieces;
		}

		MethodBoxModel source = (MethodBoxModel) ((ConnectionModel)getModel()).getSource();
		ASTNode node = source.getASTNode();

		List<Invocation> invocations = new ArrayList<Invocation>();
		if(Invocation.nodeIsATypeOfInvocation(node)) {
			invocations = Invocation.getEachInvocationInChain(new Invocation(node));
		}

		boolean firstInEntireChainPresent = true;
		// Check whether this message is only a portion of the entire chained 
		// call and remove the invocations not meant to be a part of this message
		List<Invocation> invocationsCopy = new ArrayList<Invocation>(invocations);
		for(int i=0; i<invocationsCopy.size(); i++) {
			Invocation invocation = invocationsCopy.get(i);
			String methodName = MethodUtil.getMethodName(invocation.getMethodElement(), invocation, fromChainedCall);
			if(model.getLabel()!= null && !model.getLabel().contains(methodName)) {
				invocations.remove(invocation);
				if(i==invocationsCopy.size()-1) firstInEntireChainPresent = false;
			}
		}

		if(invocations.size()==0) {
			Label label = new Label(model.getLabel());
			messageLabelPieces.add(label);
			if(Invocation.nodeIsATypeOfInvocation(node)) 
				labelToInvocationMap.put(label, new Invocation(node));
			return messageLabelPieces;
		}

		// If this message is not meant to be displayed as a chained call, it means that the
		// last call (call it C) in this chain used to be part of another chained message and 
		// that the user has selected C in it in order to display C. So, only C should
		// be displayed in this message.
		if(!source.isAChainedCall()) {
			Invocation invocation = invocations.get(0);

			String methodName = MethodUtil.getMethodName(invocation.getMethodElement(), invocation, fromChainedCall);
			Label invocationPiece = new Label(methodName);

			labelToInvocationMap.put(invocationPiece, invocation);
			messageLabelPieces.add(invocationPiece);
			return messageLabelPieces;
		}

		for(int i=0; i<invocations.size(); i++) {
			Invocation invocation = invocations.get(i);
			String methodName =	MethodUtil.getMethodName(invocation.getMethodElement(), invocation, fromChainedCall);
			Label invocationPiece = new UnderlinableLabel(methodName);

			labelToInvocationMap.put(invocationPiece, invocation);

			// We can only add the first or last invocation
			// in a chained invocation to the diagram
			if(invocations.size()>1 && (i==0 || i==invocations.size()-1)) {
				invocationPiece.setToolTip(new Label("Add this call to the diagram"));
				// clickable only if non-library code
				if (!isBinary(invocation)){
					invocationPiece.addMouseMotionListener(indicateClickableListener);
					invocationPiece.addMouseListener(clickedInvocationListener);
				}
			}

			if(messageLabelPieces.size()>0) messageLabelPieces.add(0, new Label("."));
			messageLabelPieces.add(0, invocationPiece);
		}

		// If we have a chained invocation, the instance figure will not be able to
		// be an indication of what instance the first method in the chain was invoked on,
		// so we need to show that instance's name as part of the message label. Won't do 
		// this if we have a chained invocation but it is only a portion of the entire 
		// chained call and doesn't contain the first method in the entire chain (because
		// that is what is invoked on the instance)
		if (invocations.size() > 1 && messageLabelPieces.size() > 0 && firstInEntireChainPresent) {
			String instanceCalledOn = 
				(node instanceof MethodInvocation) ? MethodUtil.getInstanceInvocationChainStartsOn(new Invocation((MethodInvocation)node)) : null;

				if (instanceCalledOn != null && !instanceCalledOn.equals("") && !instanceCalledOn.equals("super")) {
					messageLabelPieces.get(0).setText(instanceCalledOn + "." + messageLabelPieces.get(0).getText());
				}
		}

		return messageLabelPieces;
	}

	public boolean isBinary(Invocation invocation) {
		if (invocation.resolveMethodBinding().getJavaElement() == null) return false;
		if (((IMember) invocation.resolveMethodBinding().getJavaElement()).isBinary())
			return true;
		
		return false;
	}
	
	// Gets the labels for the arguments, including arguments that are
	// invocations. (For example, b() and c() in a(b(c())) or a(b(c())).d()).
	private int getArgumentPieces(Label invocationLabel, int nextLabelIndex, List<Label> fullMessageLabelPieces) {

		Invocation invocation = null;
		if(labelToInvocationMap.containsKey(invocationLabel))
			invocation = labelToInvocationMap.get(invocationLabel);
		else if(labelToArgumentInvocationMap.containsKey(invocationLabel))
			invocation = labelToArgumentInvocationMap.get(invocationLabel);
		if(invocation==null) return nextLabelIndex;

		List<Label> argumentLabels = new ArrayList<Label>();
		labelToArgLabelsMap.put(invocationLabel, argumentLabels);

		Map<String, Boolean> argLabelToIsExpandableMap = ((ConnectionModel)getModel()).getArgLabelToIsExpandableMap();

		String methodNameText = invocationLabel.getText().substring(0, invocationLabel.getText().indexOf("("));
		invocationLabel.setText(methodNameText);

		Label openParenLabel = new Label("(");
		fullMessageLabelPieces.add(nextLabelIndex++, openParenLabel);
		argumentLabels.add(openParenLabel);

		for(int i=0; i<invocation.getArguments().size(); i++) {

			Label argumentLabel = getArgumentLabel(invocation, i, argLabelToIsExpandableMap);
			fullMessageLabelPieces.add(nextLabelIndex++, argumentLabel);
			argumentLabels.add(argumentLabel);

			nextLabelIndex = getArgumentPieces(argumentLabel, nextLabelIndex, fullMessageLabelPieces);

			if(i<invocation.getArguments().size()-1) {
				Label commaLabel = new Label(", ");
				fullMessageLabelPieces.add(nextLabelIndex++, commaLabel);
				argumentLabels.add(commaLabel);
			}
		}

		Label endParenLabel = new Label(")");
		fullMessageLabelPieces.add(nextLabelIndex++, endParenLabel);
		argumentLabels.add(endParenLabel);
		return nextLabelIndex;
	}

	private Label getArgumentLabel(Invocation invocation, int argIndex, Map<String, Boolean> argLabelToIsExpandableMap) {

		if (argLabelToIsExpandableMap.size() == invocation.getArguments().size()) {
			String argKey = (String) argLabelToIsExpandableMap.keySet().toArray()[argIndex];
			// argument is invocation that has already been expanded
			if(!argLabelToIsExpandableMap.get(argKey)) return new Label(argKey);
		}

		Expression argument = invocation.getArguments().get(argIndex);

		// argument is an invocation that the user should be allowed to expand
		if (Invocation.nodeIsATypeOfInvocation(argument)) {
			Invocation argumentInvocation = new Invocation(argument);
			String argumentInvocationName = MethodUtil.getMethodName(argumentInvocation.getMethodElement(), argumentInvocation, false);
			if (argument instanceof ClassInstanceCreation)
				argumentInvocationName = "new " + argumentInvocationName;
			Label argumentLabel =  new Label(argumentInvocationName);
			labelToArgumentInvocationMap.put(argumentLabel, new Invocation(argument));
			argumentLabel.setToolTip(new Label("Add this call to the diagram"));
			
			//add library check
			if (!isBinary(argumentInvocation)) {
				argumentLabel.addMouseMotionListener(indicateClickableListener);
				argumentLabel.addMouseListener(clickedInvocationListener);
			}
			return argumentLabel;
		}

		// argument is not an invocation
		if(argument instanceof NullLiteral) {
			// want to show type of parameter, not 'null'
			return new Label("null");
		} else
			return new Label(argument.toString());
	}

	private MouseMotionListener indicateClickableListener = new MouseMotionListener() {

		public void mouseEntered(MouseEvent me) {
			if(!(me.getSource() instanceof Label)) return;
			((Label)me.getSource()).setForegroundColor(ColorScheme.chainedCallPieceHighlight);
			UILinkUtils.setUnderline((Label)me.getSource(), true);
			((ConnectionFigure)getFigure()).showFullLabelText();
		}

		public void mouseExited(MouseEvent me) {
			if(!(me.getSource() instanceof Label)) return;
			handleMouseExit((Label)me.getSource());
		}

		public void mouseDragged(MouseEvent me) {}
		public void mouseHover(MouseEvent me) {}
		public void mouseMoved(MouseEvent me) {}
	};

	private void handleMouseExit(Label label) {
		label.setForegroundColor(ColorScheme.connectionText);
		UILinkUtils.setUnderline(label, false);
		int notSelected=EditPart.SELECTED_NONE;
		if(getSelected()==notSelected && getSource().getSelected()==notSelected && getTarget().getSelected()==notSelected)
			((ConnectionFigure)getFigure()).showAbbreviatedLabelText();
	}

	
	private MouseListener clickedInvocationListener = new MouseListener() {

		public void mousePressed(MouseEvent me) {
			if(!(me.getSource() instanceof Label)) return;
			Label clickedLabel = (Label) me.getSource();
			if(labelToInvocationMap.containsKey(clickedLabel)) mousePressedOnInvocation(clickedLabel);
			else if(labelToArgumentInvocationMap.containsKey(clickedLabel)) mousePressedOnArgumentInvocation(clickedLabel);
		}

		private void mousePressedOnInvocation(Label clickedLabel) {
			// We can only add the first or last invocation
			// in the chained invocation to the diagram
			if(clickedLabel.equals(labelToInvocationMap.keySet().toArray()[labelToInvocationMap.size()-1])) {
				AnimateChainExpandCommand animationCmd = new AnimateChainExpandCommand();
				addFirstInChain(clickedLabel, labelToInvocationMap, false, animationCmd);
				updateLabelMapAndListeners(clickedLabel, true);
				updateLabel(clickedLabel, true);
				animationCmd.setSecondCallCallMessage(((ConnectionModel)getModel()).getLabel());
				getCmdStackAndExecute(animationCmd);
			} else if(clickedLabel.equals(labelToInvocationMap.keySet().toArray()[0])) {
				AnimateChainExpandCommand animationCmd = new AnimateChainExpandCommand();
				addLastInChain(clickedLabel, animationCmd);
				updateLabelMapAndListeners(clickedLabel, false);
				updateLabel(clickedLabel, false);
				animationCmd.setFirstCallCallMessage(((ConnectionModel)getModel()).getLabel());
				getCmdStackAndExecute(animationCmd);
			}
		}

		private void getCmdStackAndExecute(Command cmd) {
			BuildStatus.addUsage("Chrono > " + cmd.getLabel());
			((DiagramEditPart)getViewer().getContents()).getViewer().getEditDomain().getCommandStack().execute(cmd);
		}
		
		private void mousePressedOnArgumentInvocation(Label clickedLabel) {
			AnimateChainExpandCommand animationCmd = new AnimateChainExpandCommand();
			addFirstInChain(clickedLabel, labelToArgumentInvocationMap, true, animationCmd);

			// Change the argument to simply be the type returned by the 
			// invocation (will look how a non-invocation argument looks)
			Invocation invocation = labelToArgumentInvocationMap.get(clickedLabel);
			if(invocation!=null) {
				String returnType = MethodUtil.getReturnMessage(invocation);
				clickedLabel.setText(returnType);
			}
			removeArgumentLabels(clickedLabel, false);

			((ConnectionFigure)getFigure()).updateFullNameMapping(clickedLabel);

			labelToArgumentInvocationMap.remove(clickedLabel);
			clickedLabel.removeMouseListener(clickedInvocationListener);
			clickedLabel.removeMouseMotionListener(indicateClickableListener);
			handleMouseExit(clickedLabel);

			getCmdStackAndExecute(animationCmd);
		}

		public void mouseDoubleClicked(MouseEvent me) {}
		public void mouseReleased(MouseEvent me) {}
	};	

	private void addFirstInChain(Label clickedLabel, Map<Label, Invocation> map, boolean isArgumentCall, AnimateChainExpandCommand animationCmd) {

		MethodBoxModel sourceOfNewCall = createModelsForSelectedPiece(clickedLabel, map, isArgumentCall, true, true, animationCmd);
		MethodBoxModel targetOfChainedCall = (MethodBoxModel) ((ConnectionModel)getModel()).getTarget();

		//		if(sourceOfNewCall==null || sourceOfNewCall.getPartner()==null) return;
		if(sourceOfNewCall==null) return;

		// The final call in the chain is a call to the same class that contains the 
		// declaration where the first invocation is made, so we don't need to reorder
		// since that would result in a backward message
		//		if(!sourceOfNewCall.getInstanceModel().equals(targetOfChainedCall.getInstanceModel())) {

		// Since we just added the _first_ call in the chain to the diagram, the 
		// instance figure of the target of the newly added call should be
		// before the instance figure of the target of this call.
		//			orderTargetModels(sourceOfNewCall.getPartner().getInstanceModel(), targetOfChainedCall.getInstanceModel());
		//		}

		animationCmd.setDiagram((DiagramModel) ((DiagramEditPart)getViewer().getContents()).getModel());

		animationCmd.setChainedInvocation((MethodBoxModel)((ConnectionModel)getModel()).getSource());
		animationCmd.setChainedDeclaration(targetOfChainedCall);
		animationCmd.setChainedMessage(((ConnectionModel)getModel()).getLabel());
		animationCmd.setChainedReturnMessage(targetOfChainedCall.getOutgoingConnection().getLabel());

		//		animationCmd.setFirstCallInvocation(sourceOfNewCall);
		//		animationCmd.setFirstCallDeclaration(sourceOfNewCall.getPartner());
		//		animationCmd.setFirstCallCallMessage(sourceOfNewCall.getOutgoingConnection().getLabel());
		//		animationCmd.setFirstCallReturnMessage(sourceOfNewCall.getIncomingConnection().getLabel());

		animationCmd.setSecondCallInvocation(targetOfChainedCall.getPartner());
		animationCmd.setSecondCallDeclaration(targetOfChainedCall);
		animationCmd.setSecondCallReturnMessage(targetOfChainedCall.getPartner().getIncomingConnection().getLabel());
		//		animationCmd.setSecondCallCallMessage(targetOfChainedCall.getPartner().getOutgoingConnection().getLabel());

		//		animationCmd.setNewDeclaration(sourceOfNewCall.getPartner());
		animationCmd.setOriginalChainedDeclarationBounds(targetOfChainedCall.getFigure().getBounds().getCopy());
		//		animationCmd.setInstance(sourceOfNewCall.getPartner().getInstanceModel());
	}

	private void addLastInChain(Label clickedLabel, AnimateChainExpandCommand animationCmd) {

		// Since the last call in the chain is going to be removed and added to the 
		// diagram with a new pair of method models, the message that this connection
		// represents will change. The binding of both the source and target will correspond 
		// the new last call in the chain, the invocation of the source will correspond
		// to the set invocations made before the last invocation that is being removed, and
		// the declaration of the target will correspond to the declaration of the new last
		// method in the chain.

		Invocation invocation = labelToInvocationMap.get(clickedLabel);
		Invocation thisNewSourceInvocation;
		IMethodBinding bindingOfInvocationExpression;
		if(invocation.getExpression() instanceof MethodInvocation) {
			thisNewSourceInvocation = new Invocation((MethodInvocation)invocation.getExpression());
			bindingOfInvocationExpression = ((MethodInvocation)invocation.getExpression()).resolveMethodBinding();
		} else if(invocation.getExpression() instanceof SuperMethodInvocation) {
			thisNewSourceInvocation = new Invocation((SuperMethodInvocation)invocation.getExpression());
			bindingOfInvocationExpression = ((SuperMethodInvocation)invocation.getExpression()).resolveMethodBinding();
		} else return;

		if(bindingOfInvocationExpression==null || !(bindingOfInvocationExpression.getJavaElement() instanceof IMethod)) return;

		IMethod newMethod = (IMethod) bindingOfInvocationExpression.getJavaElement();
		ASTNode newDeclarationNode = RJCore.getCorrespondingASTNode((IMethod) bindingOfInvocationExpression.getJavaElement());

		MethodBoxModel sourceOfChainedCall = (MethodBoxModel) ((ConnectionModel)getModel()).getSource();
		MethodBoxModel targetOfChainedCall = (MethodBoxModel) ((ConnectionModel)getModel()).getTarget();

		animationCmd.setDiagram((DiagramModel) ((DiagramEditPart)getViewer().getContents()).getModel());

		animationCmd.setChainedInvocation(sourceOfChainedCall);
		animationCmd.setChainedDeclaration(targetOfChainedCall);
		animationCmd.setChainedMessage(((ConnectionModel)getModel()).getLabel());
		animationCmd.setChainedReturnMessage(targetOfChainedCall.getOutgoingConnection().getLabel());

		animationCmd.setOriginalChainedDeclarationBounds(targetOfChainedCall.getFigure().getBounds().getCopy());

		sourceOfChainedCall.changeMethodRepresented(newMethod, invocation.getExpression());
		targetOfChainedCall.changeMethodRepresented(newMethod, newDeclarationNode);


		// Now that the declaration that the target of this call represents has changed, 
		// the container of that declaration model may also have changed if the code for 
		// the original and new declarations are in different classes.

		Resource thisTargetNewInstance = MethodUtil.getClassOfInstanceCalledOn(thisNewSourceInvocation, sourceOfChainedCall.getInstanceModel());
		if(thisTargetNewInstance==null) return;

		NodeModel thisTargetNewContainerModel = MethodUtil.getContainerModel(thisNewSourceInvocation, sourceOfChainedCall, (DiagramModel) ((DiagramEditPart)getViewer().getContents()).getModel(), thisTargetNewInstance, animationCmd);

		InstanceModel thisTargetNewInstanceModel = (thisTargetNewContainerModel instanceof MethodBoxModel) ? ((MethodBoxModel)thisTargetNewContainerModel).getInstanceModel() : (InstanceModel)thisTargetNewContainerModel;
		InstanceModel thisTargetOldInstanceModel = targetOfChainedCall.getInstanceModel();
		if(!thisTargetNewInstanceModel.equals(thisTargetOldInstanceModel)) {
			NodeModel oldParent = targetOfChainedCall.getParent();

			targetOfChainedCall.setInstanceModel(thisTargetNewInstanceModel);
			thisTargetNewContainerModel.addChild(targetOfChainedCall);

			oldParent.removeChild(targetOfChainedCall);

			// The final call in the chain is a call to the same class that contains the 
			// declaration where the first invocation is made, so we don't need to reorder
			// since that would result in a backward message
			if(!sourceOfChainedCall.getInstanceModel().equals(thisTargetOldInstanceModel)) {
				// Since we just added the _last_ call in the chain to the diagram, the 
				// instance figure of the target of the newly added call should be
				// after the instance figure of the target of this call.
				orderTargetModels(thisTargetNewInstanceModel, thisTargetOldInstanceModel);
			}
		}

		// The target of this connection now represents a different method declaration, and 
		// that method may return a different type than the original method the target
		// represented did
		ConnectionModel oldReturnMessage = targetOfChainedCall.getOutgoingConnection();
		boolean isVisible = oldReturnMessage.getLine().isVisible();
		oldReturnMessage.disconnect();
		ConnectionModel returnConnection = ConnectionUtil.createConnection(MethodUtil.getReturnMessage(thisNewSourceInvocation), targetOfChainedCall, sourceOfChainedCall, ConnectionModel.RETURN);
		returnConnection.setVisible(isVisible);

		//		MethodBoxModel sourceOfNewCall = createModelsForSelectedPiece(clickedLabel, labelToInvocationMap, false, false, false, animationCmd);
		createModelsForSelectedPiece(clickedLabel, labelToInvocationMap, false, false, false, animationCmd);

		animationCmd.setFirstCallInvocation(sourceOfChainedCall);
		animationCmd.setFirstCallDeclaration(targetOfChainedCall);
		animationCmd.setFirstCallReturnMessage(sourceOfChainedCall.getIncomingConnection().getLabel());

		//		animationCmd.setSecondCallInvocation(sourceOfNewCall);
		//		animationCmd.setSecondCallDeclaration(sourceOfNewCall.getPartner());
		//		animationCmd.setSecondCallCallMessage(sourceOfNewCall.getOutgoingConnection().getLabel());
		//		animationCmd.setSecondCallReturnMessage(sourceOfNewCall.getIncomingConnection().getLabel());

		animationCmd.setNewDeclaration(targetOfChainedCall);
		animationCmd.setInstance(thisTargetNewInstanceModel);
	}

	private MethodBoxModel createModelsForSelectedPiece(Label selectedPiece, Map<Label, Invocation> map, boolean isArgumentCall, boolean addBefore, boolean isFirstCall, AnimateChainExpandCommand animationCmd) {

		Invocation invocation = map.get(selectedPiece);

		MethodBoxModel sourceOfChainedCall = (MethodBoxModel) ((ConnectionModel)getModel()).getSource();

		Resource classOfInstance = MethodUtil.getClassOfInstanceCalledOn(invocation, sourceOfChainedCall.getInstanceModel());
		if(classOfInstance==null) return null;

		NodeModel containerModel = MethodUtil.getContainerModel(invocation, sourceOfChainedCall.getDeclarationContainer(), (DiagramModel) ((DiagramEditPart)getViewer().getContents()).getModel(), classOfInstance, animationCmd);

		// creating for the new call connection a mapping from each of this invocation's 
		// arguments to whether or not the argument is expandable so that any invocation 
		// arguments that have already been expanded will show up that way (and no longer 
		// as expandable invocations) in the newly created connection
		Map<String, Boolean> argLabelToIsExpandableMap = new LinkedHashMap<String, Boolean>();
		if(labelToArgLabelsMap.containsKey(selectedPiece)) {
			for(Label argLabel : labelToArgLabelsMap.get(selectedPiece)) {
				String argText = argLabel.getText().trim();
				if("(".equals(argText) || ")".equals(argText) || ",".equals(argText)) continue;
				argLabelToIsExpandableMap.put(argLabel.getText(), labelToArgumentInvocationMap.containsKey(argLabel));
			}
		}

		MethodBoxModel model = MethodUtil.createModelsForMethodRes(
				invocation, 
				sourceOfChainedCall.getDeclarationContainer(), 
				(DiagramModel) ((DiagramEditPart)getViewer().getContents()).getModel(), 
				classOfInstance, 
				containerModel, 
				false,
				(LinkedHashMap<String, Boolean>)argLabelToIsExpandableMap,
				isFirstCall,
				animationCmd,
				null, false);
		return model;
	}

	private void orderTargetModels(InstanceModel shouldComeBefore, InstanceModel shouldComeAfter) {
		DiagramModel diagramModel = (DiagramModel) ((DiagramEditPart)getViewer().getContents()).getModel();
		int indexOfShouldComeBefore = diagramModel.getChildren().indexOf(shouldComeBefore);
		int indexOfShouldComeAfter = diagramModel.getChildren().indexOf(shouldComeAfter);

		if(indexOfShouldComeAfter==-1 || 
				indexOfShouldComeBefore==-1 || 
				indexOfShouldComeAfter>=indexOfShouldComeBefore) return;

		diagramModel.reorderChild(shouldComeBefore, indexOfShouldComeAfter);
	}

	// first should be true if labelToRemove was the first invocation in the
	// original chain, false if it was the last
	private void updateLabelMapAndListeners(Label labelToRemove, boolean first) {

		labelToInvocationMap.remove(labelToRemove);
		labelToRemove.removeMouseListener(clickedInvocationListener);
		labelToRemove.removeMouseMotionListener(indicateClickableListener);

		if(labelToInvocationMap.size() < 2) {
			// This connection represents only a single call now 
			// (it is no longer a chained invocation), so none of the
			// message label should be selectable
			for(Label label : labelToInvocationMap.keySet()) {
				label.setToolTip(null);
				if (!isBinary(labelToInvocationMap.get(label))) {
					label.removeMouseListener(clickedInvocationListener);
					label.removeMouseMotionListener(indicateClickableListener);
				}
			}
		} else {
			// This connection still represents a chained invocation, and
			// the first (if labelToRemove was the first invocation in the original 
			// chain) or last (if labelToRemove was the last invocation in the original
			// chain) invocation in it should now be selectable
			Label newSelectableLabel;
			if(first) newSelectableLabel = (Label) labelToInvocationMap.keySet().toArray()[labelToInvocationMap.size()-1];
			else newSelectableLabel = (Label) labelToInvocationMap.keySet().toArray()[0];
			newSelectableLabel.setToolTip(new Label("Add this call to the diagram"));
			if (isBinary(labelToInvocationMap.get(newSelectableLabel))) {
				newSelectableLabel.addMouseMotionListener(indicateClickableListener);
				newSelectableLabel.addMouseListener(clickedInvocationListener);
			}
		}
	}

	// first should be true if this label piece being removed was the first
	// piece in the chain, false if it was the last
	private void updateLabel(Label label, boolean first) {

		if(labelToInvocationMap.size()==1) {
			Label singleRemainingLabel = (Label) labelToInvocationMap.keySet().toArray()[0];
			// If the label of this connection shows only one method call but contains
			// a ".", it means that the method is called on an instance. Since this
			// connection is a single method call, the instance containing its target
			// will now display that instance name, so we remove it from the label.
			String labelText = singleRemainingLabel.getText();
			int indexOfDot = labelText.indexOf(".");
			if(indexOfDot > 0 && indexOfDot < labelText.length()-1 && !"super".equals(labelText.substring(0, indexOfDot))) {
				singleRemainingLabel.setText(labelText.substring(indexOfDot+1));
				((ConnectionFigure)getFigure()).updateFullNameMapping(singleRemainingLabel);
			}
		}

		// Remove all of the argument labels corresponding to this piece
		removeArgumentLabels(label, first);

		// Remove this piece and the dot before or after it
		String newLabel = ((ConnectionFigure)getFigure()).removeLabelPiece(label, true, !first);
		((ConnectionModel)getModel()).setLabel(newLabel);
	}

	private void removeArgumentLabels(Label label, boolean first) {
		if(!labelToArgLabelsMap.containsKey(label)) return;
		for(Label argLabel : labelToArgLabelsMap.get(label)) {
			removeArgumentLabels(argLabel, first);
			((ConnectionFigure)getFigure()).removeLabelPiece(argLabel, false, !first);
		}
	}

}