package com.architexa.diagrams.chrono.editparts;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.NullLiteral;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory;

import com.architexa.diagrams.chrono.animation.AnimateCallMadeCommand;
import com.architexa.diagrams.chrono.editpolicies.SeqOrderedLayoutEditPolicy;
import com.architexa.diagrams.chrono.editpolicies.SeqSelectionEditPolicy;
import com.architexa.diagrams.chrono.figures.FigureWithGap;
import com.architexa.diagrams.chrono.figures.HiddenFigure;
import com.architexa.diagrams.chrono.figures.MemberFigure;
import com.architexa.diagrams.chrono.figures.MethodBoxFigure;
import com.architexa.diagrams.chrono.figures.UserCreatedMethodBoxFigure;
import com.architexa.diagrams.chrono.models.ConnectionModel;
import com.architexa.diagrams.chrono.models.HiddenNodeModel;
import com.architexa.diagrams.chrono.models.InstanceModel;
import com.architexa.diagrams.chrono.models.MemberModel;
import com.architexa.diagrams.chrono.models.MethodBoxModel;
import com.architexa.diagrams.chrono.models.MethodInvocationModel;
import com.architexa.diagrams.chrono.sequence.SeqPlugin;
import com.architexa.diagrams.chrono.ui.ColorScheme;
import com.architexa.diagrams.chrono.util.MethodUtil;
import com.architexa.diagrams.chrono.util.SeqUtil;
import com.architexa.diagrams.commands.BreakableCommand;
import com.architexa.diagrams.jdt.AnonymousClassConstructor;
import com.architexa.diagrams.jdt.Invocation;
import com.architexa.diagrams.jdt.RJCore;
import com.architexa.diagrams.model.ArtifactFragment;
import com.architexa.diagrams.parts.ArtifactRelEditPart;
import com.architexa.diagrams.ui.ImageCache;
import com.architexa.diagrams.ui.RSEContextMenuProvider;
import com.architexa.org.eclipse.draw2d.ActionEvent;
import com.architexa.org.eclipse.draw2d.ActionListener;
import com.architexa.org.eclipse.draw2d.Figure;
import com.architexa.org.eclipse.draw2d.IFigure;
import com.architexa.org.eclipse.draw2d.Label;
import com.architexa.org.eclipse.draw2d.LineBorder;
import com.architexa.org.eclipse.draw2d.MouseEvent;
import com.architexa.org.eclipse.draw2d.geometry.Point;
import com.architexa.org.eclipse.draw2d.geometry.Rectangle;
import com.architexa.org.eclipse.gef.ConnectionEditPart;
import com.architexa.org.eclipse.gef.EditPart;
import com.architexa.org.eclipse.gef.EditPolicy;
import com.architexa.org.eclipse.gef.GraphicalEditPart;
import com.architexa.org.eclipse.gef.commands.CompoundCommand;
import com.architexa.store.StoreUtil;

/**
 * 
 * @author Elizabeth L. Murnane
 *
 */
public class MethodBoxEditPart extends MemberEditPart implements ActionListener {
	private static final Logger logger = SeqPlugin.getLogger(MethodBoxEditPart.class);

	public static int BOX_GAP = 50;

	private boolean isABackwardCall = false;
	private boolean showingBackwardCalls = false;
	private boolean showingOverrideCalls = false;

	@Override
	protected IFigure createFigure() {
		MethodBoxModel model = (MethodBoxModel) getModel();
		String tooltipText = model.getName();
		String noConnectionText = model.getMethodName();
		Image methodAccessIcon = null;
		// tooltip on invocation is the actual invocation call with no icon
		if(model instanceof MethodInvocationModel) tooltipText = getInvocationTooltip().trim();
		else { // tooltip on declaration is the method name with an access level icon
			ImageDescriptor iconDesc = MethodUtil.getMethodIconDescriptor(model, false);
			if (iconDesc == null) {
				logger.error("Null image descriptor for method: " + model.getMethod() + "\nresource" + model.getMethodRes());
				iconDesc = SeqUtil.getImageDescriptorFromKey(null);
			}
			methodAccessIcon = ImageCache.calcImageFromDescriptor(iconDesc);
//			methodAccessIcon = MethodUtil.getMethodIconDescriptor(model, false).createImage();
			// if an anon class constructor decl, the occurence position of the anon class
			// definition is appended with a "#" after the method name
			if(model.getMethod() instanceof AnonymousClassConstructor) {
				String occurrencePosition = ((AnonymousClassConstructor)model.getMethod()).getAnonClassOccurrencePosition();
				tooltipText = tooltipText.trim()+" #"+occurrencePosition;

				// Using getName() instead of getMethodName() so that in the case where
				// the anon class is implementing an interface, the no-connection label
				// will be the name of the interface rather than "Object"
				noConnectionText = model.getName();
				int argumentsIndex = noConnectionText.indexOf("(");
				if(argumentsIndex!=-1) 
					noConnectionText = noConnectionText.substring(0, argumentsIndex);
			}
		}
		Label figureTooltip = new Label(" " + tooltipText + " ", methodAccessIcon);
		MethodBoxFigure methodBox = new MethodBoxFigure(model.getType(), figureTooltip, noConnectionText);
		methodBox.setPartner(getPartnerEP()==null?null:(MethodBoxFigure)getPartnerEP().getFigure());
		model.setFigure(methodBox);
		methodBox.addPropertyChangeListener(this);
		methodBox.addMouseListener(this);
		methodBox.addMouseMotionListener(this);
		methodBox.addFigureListener(this);
		return methodBox;
	}

	@Override
	protected void refreshVisuals() {
		MethodBoxModel model = (MethodBoxModel) getModel();
		IFigure methodBox = getFigure();
		if(model.getType() == MethodBoxModel.access) {
			methodBox.setBackgroundColor(ColorScheme.methodInvocationFigureBackground);
		} else if(model.getType() == MethodBoxModel.declaration) {
			((MethodBoxFigure) getFigure()).colorFigure(ColorScheme.methodDeclarationFigureBackground);
		}
	}
	
	@Override
	public void colorFigure(Color color) {
			((MethodBoxFigure)getFigure()).colorFigure(color);
	}
	
	private String getInvocationTooltip() {
		MethodBoxModel methodModel = (MethodBoxModel)getModel();
		if(/*(a)*/methodModel.getMethod() instanceof AnonymousClassConstructor ||
				/*(b)*/((MethodBoxModel)methodModel.getParent()).getMethod() instanceof AnonymousClassConstructor) {
			// (a) tooltip on invocation of anon class constructor should show 
			// full anon class definition
			// (b) tooltip on implicit invocation that anon class constructor makes to
			// constructor of class being extended or implemented should show "super([argument types])"
			return Invocation.getStringRepresentationOfInvocation(((MethodBoxModel)getModel()).getASTNode());
		}
		return getMessageLabelPieces();
	}

	// each key is the label of one of the chained invocations, 
	// and it maps to the corresponding Invocation
	private Map<Label, Invocation> labelToInvocationMap = new LinkedHashMap<Label, Invocation>();
	// each key is some invocation in the chain's invocation argument's label, 
	// and it maps to the argument's corresponding Invocation
	private Map<Label, Invocation> labelToArgumentInvocationMap = new LinkedHashMap<Label, Invocation>();
	private String getMessageLabelPieces() {
		List<Label> labelPieces = getChainedPieces();
		for (Label label : new ArrayList<Label>(labelPieces))
			getArgumentPieces(label, labelPieces.indexOf(label)+1, labelPieces);

		String fullLabel = "";
		for (Label piece : labelPieces) 
			fullLabel = fullLabel.concat(piece.getText());

		return fullLabel;
	}

	// Gets a label for each invocation in the chain. For
	// example, a(), b(), c() for the chained call a().b().c()
	private List<Label> getChainedPieces() {
		List<Label> messageLabelPieces = new ArrayList<Label>();
		boolean fromChainedCall = true;
		ASTNode node = ((MemberModel) getModel()).getASTNode();

		List<Invocation> invocations = new ArrayList<Invocation>();
		if (Invocation.nodeIsATypeOfInvocation(node))
			invocations = Invocation.getEachInvocationInChain(new Invocation(node));

		for (int i=0; i<invocations.size(); i++) {
			Invocation invocation = invocations.get(i);
			String methodName =	MethodUtil.getMethodName(invocation.getMethodElement(), invocation, fromChainedCall);
			Label invocationPiece = new Label(methodName);
			labelToInvocationMap.put(invocationPiece, invocation);

			if (messageLabelPieces.size()>0) 
				messageLabelPieces.add(0, new Label("."));

			messageLabelPieces.add(0, invocationPiece);
		}

		// If we have a chained invocation, the instance figure will not be able to
		// be an indication of what instance the first method in the chain was invoked on,
		// so we need to show that instance's name as part of the message label. Won't do 
		// this if we have a chained invocation but it is only a portion of the entire 
		// chained call and doesn't contain the first method in the entire chain (because
		// that is what is invoked on the instance)
		if (invocations.size() > 1 && messageLabelPieces.size() > 0) {
			String instanceCalledOn = (node instanceof MethodInvocation) ? 
					MethodUtil.getInstanceInvocationChainStartsOn(new Invocation((MethodInvocation)node)) : null;

					if (instanceCalledOn != null && !instanceCalledOn.equals("") && !instanceCalledOn.equals("super")) {
						messageLabelPieces.get(0).setText(instanceCalledOn + "." + messageLabelPieces.get(0).getText());
					}
		}
		return messageLabelPieces;
	}

	// Gets the labels for the arguments, including arguments that are
	// invocations. (For example, b() and c() in a(b(c())) or a(b(c())).d()).
	private int getArgumentPieces(Label invocationLabel, int nextLabelIndex, List<Label> fullMessageLabelPieces) {

		Invocation invocation = null;
		if (labelToInvocationMap.containsKey(invocationLabel))
			invocation = labelToInvocationMap.get(invocationLabel);
		else if (labelToArgumentInvocationMap.containsKey(invocationLabel))
			invocation = labelToArgumentInvocationMap.get(invocationLabel);
		if (invocation==null) 
			return nextLabelIndex;

		List<Label> argumentLabels = new ArrayList<Label>();
		String methodNameText = invocationLabel.getText().substring(0, invocationLabel.getText().indexOf("("));
		invocationLabel.setText(methodNameText);

		Label openParenLabel = new Label("(");
		fullMessageLabelPieces.add(nextLabelIndex++, openParenLabel);
		argumentLabels.add(openParenLabel);

		for (int i=0; i<invocation.getArguments().size(); i++) {
			Label argumentLabel = getArgumentLabel(invocation, i);
			fullMessageLabelPieces.add(nextLabelIndex++, argumentLabel);
			argumentLabels.add(argumentLabel);
			nextLabelIndex = getArgumentPieces(argumentLabel, nextLabelIndex, fullMessageLabelPieces);
			if (i<invocation.getArguments().size()-1) {
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

	private Label getArgumentLabel(Invocation invocation, int argIndex) {
		Expression argument = invocation.getArguments().get(argIndex);
		// argument is an invocation that the user should be allowed to expand
		if (Invocation.nodeIsATypeOfInvocation(argument)) {
			Invocation argumentInvocation = new Invocation(argument);
			String argumentInvocationName = MethodUtil.getMethodName(argumentInvocation.getMethodElement(), argumentInvocation, false);
			Label argumentLabel =  new Label(argumentInvocationName);
			labelToArgumentInvocationMap.put(argumentLabel, new Invocation(argument));
			return argumentLabel;
		}

		if (argument instanceof NullLiteral) {
			// want to show type of parameter, not 'null'
			String methodName =	MethodUtil.getMethodName(RJCore.jdtElementToResource(StoreUtil.getDefaultStoreRepository(), invocation.getMethodElement()), invocation.getInvocation(), true);
			String parameterList = methodName.substring(methodName.indexOf("(") + 1, methodName.indexOf(")"));
			return new Label(parameterList.split(",")[argIndex]);
		} else 
			return new Label(argument.resolveTypeBinding().getName());
	}

	@Override
	protected void createEditPolicies() {
		super.createEditPolicies();
		installEditPolicy(EditPolicy.LAYOUT_ROLE, new SeqOrderedLayoutEditPolicy());
		installEditPolicy(EditPolicy.PRIMARY_DRAG_ROLE, new SeqSelectionEditPolicy());
	}

	@Override
	protected List<ArtifactFragment> getModelChildren() {
		return ((MethodBoxModel)getModel()).getChildren();
	}

	@Override
	public MethodBoxEditPart getPartnerEP() {
		return (MethodBoxEditPart) super.getPartnerEP();
	}

	public void buildContextMenu(IMenuManager menu) {

		if(isABackwardCall()) {
			IAction backwardMessageAction = getHideShowBackwardMessagesAction();
			// backward messages setting goes in context menu's settings section
			menu.appendToGroup(RSEContextMenuProvider.GROUP_RSE_SETTINGS, backwardMessageAction);
		}
		
		// Chrono methods have multiple options for deletion and collapse, so make
		// the Delete entry in the context menu a submenu containing all the options
		createDeleteSubmenu(menu);
	}

	private void createDeleteSubmenu(IMenuManager menu) {

		// Create the delete menu and give it a delete icon
		MenuManager deleteMenu = new MenuManager("Delete", "GROUP_CHRONO_METHOD_DELETE") {
			@Override
			public void fill(Menu parent, int index) {
				super.fill(parent, index);

				// menuItem now been created so can access it and set an icon for it
				try {
					Field menuItemField = MenuManager.class.getDeclaredField("menuItem");
					menuItemField.setAccessible(true);
					MenuItem menuItem = (MenuItem) menuItemField.get(this);

					Image deleteImg = ImageCache.calcImageFromDescriptor(
							PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_TOOL_DELETE)
									);
					menuItem.setImage(deleteImg);
				} catch(Exception e) {
					logger.error("Unexpected exception while setting image of Delete submenu ", e);
				}
			}
		};

		// place delete menu at current location of standard "Delete" option
		menu.insertAfter(ActionFactory.DELETE.getId(), deleteMenu);

		// move that Delete option from the main context menu into the Delete submenu
		for(IContributionItem item : menu.getItems()) {
			if(!ActionFactory.DELETE.getId().equals(item.getId())) continue;

			final IAction standardDeleteAction = ((ActionContributionItem)item).getAction();

			// (use a copy of the action so that don't modify the original in the registry)
			IAction standardDeleteActionCopy = new Action(
					"Delete (Delete only this method)", // text distinguishes from extended delete
					standardDeleteAction.getImageDescriptor()) {
				@Override
				public void run() {
					standardDeleteAction.run();
				}
			};

			// put the standard Delete option into the submenu..
			deleteMenu.add(standardDeleteActionCopy);

			break;
		}
		// ..and remove it from the main context menu
		menu.remove(ActionFactory.DELETE.getId());
		
		// Extended Delete
		if(getModel() instanceof MemberModel){
			IAction extendedDeleteAction = getExtendedDeleteAction();
			deleteMenu.add(extendedDeleteAction);
		}

		// Collapse and Extended Collapse
		if(getModel() instanceof MethodBoxModel && MethodBoxModel.declaration==((MethodBoxModel)getModel()).getType()) {

			if( !((MethodBoxModel)getModel()).getOverriderConnections().isEmpty() || ((MethodBoxModel)getModel()).getOverridesConnection() != null){
				IAction override = getHideShowOverrideConections();
				menu.insertAfter(ActionFactory.DELETE.getId(), override);
			}

			IAction collapse = getCollapse();
			deleteMenu.add(collapse);

			IAction extendedCollapse = getExtendedCollapse();
			deleteMenu.add(extendedCollapse);

			if(((MethodBoxModel)getModel()).getChildren().isEmpty()){
				collapse.setEnabled(false);
				extendedCollapse.setEnabled(false);
			}
		}
	}

	@Override
	protected void addChildVisual(EditPart childEditPart, int index) {
		int numOfMethodBoxes = 0;
		for(int i=0; i<((MethodBoxFigure)getFigure()).getContainer().getChildren().size(); i++) {
			Object entry = ((MethodBoxFigure)getFigure()).getContainer().getChildren().get(i);
			if(entry instanceof MemberFigure || entry instanceof HiddenFigure)
				numOfMethodBoxes++;
		}

		IFigure child = ((GraphicalEditPart)childEditPart).getFigure();
		if(index==-1 || index==numOfMethodBoxes) {
			((MethodBoxFigure)getFigure()).addToMethodBox(child, -1);
		} else {
			int indexToAddAt = -1;
			int methodBoxFigIndex = -1;
			for(Object obj : ((MethodBoxFigure)getFigure()).getContainer().getChildren()) {
				if(obj instanceof MemberFigure || obj instanceof HiddenFigure) methodBoxFigIndex++;
				if(methodBoxFigIndex >= index) {
					// If the child is a MemberFigure, it has a gap so we need to 
					// subtract 1 from the index to take this into account
					int numExtras = ((obj instanceof MemberFigure && !((MemberFigure)obj).isUsingContainerGap())
							||(obj instanceof HiddenFigure && !((FigureWithGap)obj).isUsingContainerGap())) ? 1 : 0;
					indexToAddAt = ((MethodBoxFigure)getFigure()).getContainer().getChildren().indexOf(obj) - numExtras;
					break;
				}
			}
			((MethodBoxFigure)getFigure()).addToMethodBox(child, indexToAddAt);
		}
	}

	@Override
	protected void removeChildVisual(EditPart childEditPart) {
		IFigure child = ((GraphicalEditPart)childEditPart).getFigure();
		List<HiddenFigure> hiddenModelChildren = new ArrayList<HiddenFigure>();
		for(ArtifactFragment modelChild : getModelChildren()) {
			if(modelChild instanceof HiddenNodeModel) hiddenModelChildren.add(((HiddenNodeModel)modelChild).getFigure());
		}

		// Removing overrideIndicators if still visible in diagram
		if(child instanceof MethodBoxFigure && !(child instanceof UserCreatedMethodBoxFigure)){
			IFigure overrideIndicator = ((MethodBoxFigure)child).getOverridesIndicator();
			IFigure overriddenIndicator = ((MethodBoxFigure)child).getOverriddenIndicator(); 
			if( overrideIndicator != null &&
					getHandleLayer().getChildren().contains(overrideIndicator))
				getHandleLayer().remove(((MethodBoxFigure)child).getOverridesIndicator());
			if( overriddenIndicator!= null &&
					getHandleLayer().getChildren().contains(overriddenIndicator))
				getHandleLayer().remove(((MethodBoxFigure)child).getOverriddenIndicator());
		}

		((MethodBoxFigure)getContentPane()).remove(child);
	}

	@Override
	protected void addSourceConnection(ConnectionEditPart connection, int index) {
		super.addSourceConnection(connection, index);
		if (connection instanceof ArtifactRelEditPart) return;
		addOrRemoveSourceOrTargetConn((ConnectionModel)connection.getModel(), true, true);
	}

	@Override
	protected void addTargetConnection(ConnectionEditPart connection, int index) {
		super.addTargetConnection(connection, index);
		if (connection instanceof ArtifactRelEditPart) return;
		addOrRemoveSourceOrTargetConn((ConnectionModel)connection.getModel(), true, false);
	}

	@Override
	protected void removeSourceConnection(ConnectionEditPart connection) {
		super.removeSourceConnection(connection);
		if (connection instanceof ArtifactRelEditPart) return;
		addOrRemoveSourceOrTargetConn((ConnectionModel)connection.getModel(), false, true);
	}

	@Override
	protected void removeTargetConnection(ConnectionEditPart connection) {
		super.removeTargetConnection(connection);
		if (connection instanceof ArtifactRelEditPart) return;
		addOrRemoveSourceOrTargetConn((ConnectionModel)connection.getModel(), false, false);
	}

	// param add true if adding connection, false if removing
	// param source true if this is source of connection, false if target
	protected void addOrRemoveSourceOrTargetConn(ConnectionModel connection, boolean add, boolean source) {
		if(!(getModel() instanceof MethodBoxModel)) return;
		MethodBoxModel model = (MethodBoxModel)getModel();

		if(RJCore.overrides.equals(connection.getType())) {
			if(source) model.setOverridesConnection(add?connection:null);
			else model.addOrRemoveOverriderConnection(connection, add);
			addOrRemoveOverrideIndicator(add, source);
		} else {
			if(source) model.setOutgoingConnection(add?connection:null);
			else model.setIncomingConnection(add?connection:null);

			((MethodBoxFigure)getFigure()).setPartner(getPartnerEP()==null?null:(MethodBoxFigure)getPartnerEP().getFigure());
		}

		highlightAsBackwardCall();
		updateNoConnectionsLabel();
	}

	public void updateNoConnectionsLabel() {
		MethodBoxModel model = (MethodBoxModel) getModel();
		if(model.getOutgoingConnection()!=null || model.getIncomingConnection()!=null) 
			((MethodBoxFigure)getFigure()).hideNoConnectionsBoxLabel();
		else ((MethodBoxFigure)getFigure()).showNoConnectionsBoxLabel();
	}

	@Override
	public void figureMoved(IFigure source) {
		super.figureMoved(source);
		updateOverrideIndicators();
	}

	public void actionPerformed(ActionEvent event) {
		displayAllCallsMade();
	}

	// changed to private since nothing uses it right now
	private void displayAllCallsMade() {
//		CommandStackEventListener listener = ((DiagramEditPart)getViewer().getContents()).getListener();
//		CompoundCommand addMultipleCallsCmd = new CompoundCommand("displaying all calls made");
		BreakableCommand addMultipleCallsCmd = new BreakableCommand("displaying all calls made", AnimateCallMadeCommand.class);
		displayAllCallsMade(addMultipleCallsCmd);
		((DiagramEditPart)getViewer().getContents()).execute(addMultipleCallsCmd);
	}

	public void displayAllCallsMade(final CompoundCommand addMultipleCallsCmd) {
//	private void displayAllCallsMade(final CompoundCommand addMultipleCallsCmd) {
		((SeqSelectionEditPolicy)getEditPolicy(EditPolicy.PRIMARY_DRAG_ROLE)).removeSelectionHandles();

		final MethodBoxModel model = (MethodBoxModel) getModel();
		ASTNode node = model.getASTNode();
		if(node instanceof TypeDeclaration) { 
			// Problem finding corresponding MethodDeclaration, so 
			// its parent was returned. Search that parent for the
			// MethodDeclaration.
			TypeDeclaration t = (TypeDeclaration) node;
			for(Object bodyDecl : t.bodyDeclarations()) {
				if(!(bodyDecl instanceof MethodDeclaration)) continue;
				if(model.getMember().equals(((MethodDeclaration)bodyDecl).resolveBinding().getJavaElement())) {
					node = (MethodDeclaration) bodyDecl;
					break;
				}
			}
		}
		if (node==null || !(node instanceof MethodDeclaration)) return;
		final MethodDeclaration decl = (MethodDeclaration) node;
		try
		{
			final IRunnableWithProgress op=new IRunnableWithProgress(){
				public void run(final IProgressMonitor monitor)	throws InvocationTargetException, InterruptedException {
					monitor.beginTask("Displaying all calls made...", IProgressMonitor.UNKNOWN);
					Display.getDefault().asyncExec(new Runnable() {
						public void run() {
							MethodUtil.displayCallsMadeByMethodDeclToInstance(model, decl, null, (DiagramEditPart)getViewer().getContents(), addMultipleCallsCmd, monitor);
						}
					});
				}
			};
			new ProgressMonitorDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell()).run(true, false, op);
		}catch (InvocationTargetException e) {
			logger.error("Unexpected exception while displaying all calls made by method " + model, e);
		}catch (InterruptedException e) {
			logger.error("Unexpected exception while displaying all calls made by method " + model, e);
		}	
	}

	@Override
	public void mouseEntered(MouseEvent me) {
		if(!(getModel() instanceof MethodBoxModel)) return;
		MethodBoxModel model = (MethodBoxModel) getModel();
		if(model.getOverridesConnection()!=null) model.getOverridesConnection().setVisible(true);
		for(ConnectionModel overrider : model.getOverriderConnections()) overrider.setVisible(true);
		displayOrHideCallAndContainedBackwardCalls(true);
	}

	@Override
	public void mouseExited(MouseEvent me) {
		if(!(getModel() instanceof MethodBoxModel)) return;
		if(!getIsShowingOverrideCalls()){
			MethodBoxModel model = (MethodBoxModel) getModel();
			if(model.getOverridesConnection()!=null) model.getOverridesConnection().setVisible(false);
			for(ConnectionModel overrider : model.getOverriderConnections()) overrider.setVisible(false);
		}
		displayOrHideCallAndContainedBackwardCalls(false);
	}

	public void displayOrHideCallAndContainedBackwardCalls(boolean visible) {
		if(isABackwardCall()) displayOrHide(visible);

		for(Object child : getChildren()) {
			if(!(child instanceof MethodBoxEditPart)) continue;
			((MethodBoxEditPart)child).displayOrHideCallAndContainedBackwardCalls(visible);
		}
	}

	private void displayOrHide(boolean visible) {
		// If user has selected to show backward calls, don't hide them
		if(!visible && isShowingBackwardCalls()) return;
		MethodBoxModel model = (MethodBoxModel) getModel();
		if(model.getOutgoingConnection()!=null) model.getOutgoingConnection().setVisible(visible);
		if(model.getIncomingConnection()!=null) model.getIncomingConnection().setVisible(visible);
	}

	private Boolean getIsShowingOverrideCalls()
	{
		return showingOverrideCalls;
	}

	public void setIsShowingOverrideCalls(Boolean visible){
		this.showingOverrideCalls = visible;
	}

	private void hideOrShowOverrideCalls(){
		if(!(getModel() instanceof MethodBoxModel)) return;
		MethodBoxModel model = (MethodBoxModel) getModel();
		if(getIsShowingOverrideCalls()==false){
			setSelfAndPartnerIsShowingOverride(model, true);
		}else{
			setSelfAndPartnerIsShowingOverride(model, false);
		}
	}

	private void setSelfAndPartnerIsShowingOverride(MethodBoxModel model, boolean visible){
		// setting overrides
		if(model.getOverridesConnection()!=null){
			model.getOverridesConnection().setVisible(visible);
			for(Object connectionEP : this.getSourceConnections()){
				ConnectionEditPart connEP = (ConnectionEditPart) connectionEP; 
				if(connEP.getTarget().getModel().equals(model.getOverridesConnection().getTarget()))
					((MethodBoxEditPart)connEP.getTarget()).setIsShowingOverrideCalls(visible);
			}
		}

		// setting overrider
		for(ConnectionModel overrider : model.getOverriderConnections()){
			overrider.setVisible(visible);
			List<MethodBoxModel> overriderList = new ArrayList<MethodBoxModel>();
			for(ConnectionModel connModel:model.getOverriderConnections())
				overriderList.add((MethodBoxModel) connModel.getSource());

			for(Object connectionEP : this.getTargetConnections()){
				ConnectionEditPart connEP = (ConnectionEditPart) connectionEP;
				if(overriderList.contains(connEP.getSource().getModel()))
					((MethodBoxEditPart)connEP.getSource()).setIsShowingOverrideCalls(visible);
			}
		}
		setIsShowingOverrideCalls(visible);
	}

	private IAction getHideShowOverrideConections(){
		final boolean showing = getIsShowingOverrideCalls();
		String actionText = showing ? "Hide override connection" : "Keep override connection visible";
		IAction action = new Action(actionText) {
			@Override
			public void run() {
				hideOrShowOverrideCalls();
			}
		};
		return action;
	}

	private IAction getHideShowBackwardMessagesAction() {
		final MethodBoxEditPart thisMethod = this;
		final boolean showing = isShowingBackwardCalls();
		String actionText = showing ? "Hide backward message" : "Keep backward message visible";
		IAction action = new Action(actionText) {
			@Override
			public void run() {
				hideOrShowBackwardMessages(thisMethod);

				if(thisMethod.getPartnerEP()!=null) hideOrShowBackwardMessages(getPartnerEP());
			}
			private void hideOrShowBackwardMessages(MethodBoxEditPart method) {
				if(showing) {
					method.setShowingBackwardCalls(!showing);
					method.displayOrHide(!showing);
				} else {
					method.displayOrHide(!showing);
					method.setShowingBackwardCalls(!showing);
				}
			}
		};
		return action;
	}

	public void highlightAsBackwardCall() {
		if(isABackwardCall())
			getFigure().setBorder(new LineBorder(ColorScheme.backwardConnectionMethodBoxHighlight));
		else
			getFigure().setBorder(null);
	}

	public boolean isABackwardCall() {
		isABackwardCall = checkIsABackwardCall();
		return isABackwardCall;
	}

	private boolean checkIsABackwardCall() {
		if(getPartnerEP()==null) return false;
		InstanceModel thisInstanceModel = ((MethodBoxModel)getModel()).getInstanceModel();
		if(!(getPartnerEP().getModel() instanceof MethodBoxModel) ) return false; //partner is grouped method
		InstanceModel partnerInstanceModel = ((MethodBoxModel)getPartnerEP().getModel()).getInstanceModel();

		if(thisInstanceModel.equals(partnerInstanceModel)) return false;
		if(thisInstanceModel.getParentArt()==null || partnerInstanceModel.getParentArt() == null) return false;
		if(!thisInstanceModel.getParentArt().equals(
				partnerInstanceModel.getParentArt())) return false;

		List<ArtifactFragment> diagramChildren = thisInstanceModel.getParentArt().getShownChildren();
		int indexOfInstance = diagramChildren.indexOf(thisInstanceModel);
		int indexOfPartnerInstance = diagramChildren.indexOf(partnerInstanceModel);
		boolean partnerBeforeThis = indexOfPartnerInstance < indexOfInstance;
		return ((((MethodBoxModel)getModel()).getType()==MethodBoxModel.access && partnerBeforeThis) 
				|| (((MethodBoxModel)getModel()).getType()==MethodBoxModel.declaration && !partnerBeforeThis));
	}

	private void setShowingBackwardCalls(boolean show) {
		showingBackwardCalls = show;
	}

	private boolean isShowingBackwardCalls() {
		return showingBackwardCalls;
	}

	protected void addOrRemoveOverrideIndicator(boolean add, boolean isOverrider) {
		MethodBoxFigure methodFig = (MethodBoxFigure) getFigure();
		IFigure overrideIndicator = isOverrider ? methodFig.getOverridesIndicator() : methodFig.getOverriddenIndicator();
		if(add) {
			getHandleLayer().add(overrideIndicator);
			updateOverrideIndicators();
		} else if((isOverrider || ((MethodBoxModel)getModel()).getOverriderConnections().isEmpty()) 
				&& getHandleLayer().getChildren().contains(overrideIndicator)) {
			getHandleLayer().remove(overrideIndicator);
		} 
	}

	private void updateOverrideIndicators() {
		if(!(getModel() instanceof MethodBoxModel)) return;
		MethodBoxModel methodModel = (MethodBoxModel) getModel();
		if(methodModel.getOverridesConnection()!=null) {
			Figure overridesIndicator = ((MethodBoxFigure)getFigure()).getOverridesIndicator();
			Point topRight = ((SeqSelectionEditPolicy)getEditPolicy(EditPolicy.PRIMARY_DRAG_ROLE)).findTopRightBoundsForButton(overridesIndicator).getTopRight();
			Point location = new Point(topRight.x-overridesIndicator.getSize().width, topRight.y);
			overridesIndicator.setBounds(new Rectangle(location, overridesIndicator.getPreferredSize()));
		}
		if(!methodModel.getOverriderConnections().isEmpty()) {
			Figure overriddenIndicator = ((MethodBoxFigure)getFigure()).getOverriddenIndicator();
			Point bottomLeft = ((SeqSelectionEditPolicy)getEditPolicy(EditPolicy.PRIMARY_DRAG_ROLE)).findBottomLeftBoundsForButton(overriddenIndicator).getBottomLeft();
			Point location = new Point(bottomLeft.x, bottomLeft.y-overriddenIndicator.getSize().height);
			overriddenIndicator.setBounds(new Rectangle(location, overriddenIndicator.getPreferredSize()));
		}
	}

	private IAction getExtendedDeleteAction() {
		IAction action = new Action("Extended Delete (Delete this method, methods it calls, methods they call, etc)") {
			@Override
			public void run() {
				CompoundCommand command = new CompoundCommand("Extended Delete");
				MethodUtil.getExtendedDeleteCommand((MemberModel) getModel(), command);
				((DiagramEditPart)getViewer().getContents()).execute(command);
			}
		};
		action.setImageDescriptor(PlatformUI.getWorkbench().getActiveWorkbenchWindow().
				getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_TOOL_DELETE));
		return action;
	}

	private IAction getCollapse() {
		IAction action = new Action("Collapse (Delete methods this calls)") {
			@Override
			public void run() {
				CompoundCommand command = new CompoundCommand("Collapse");
				MethodUtil.getBasicCollapseCommand((MemberModel) getModel(), command);
				((DiagramEditPart)getViewer().getContents()).execute(command);
			}
		};
		action.setImageDescriptor(PlatformUI.getWorkbench().getActiveWorkbenchWindow().
				getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_TOOL_DELETE));
		return action;
	}

	private IAction getExtendedCollapse() {
		IAction action = new Action("Extended Collapse (Delete methods this calls, methods they call, etc)") {
			@Override
			public void run() {
				CompoundCommand command = new CompoundCommand("Extended Collapse");
				MethodUtil.getExtendedCollapseCommand((MemberModel) getModel(), command);
				((DiagramEditPart)getViewer().getContents()).execute(command);
			}
		};
		action.setImageDescriptor(PlatformUI.getWorkbench().getActiveWorkbenchWindow().
				getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_TOOL_DELETE));
		return action;
	}


}
