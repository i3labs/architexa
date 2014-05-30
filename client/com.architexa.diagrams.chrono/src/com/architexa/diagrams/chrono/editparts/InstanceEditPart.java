package com.architexa.diagrams.chrono.editparts;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.PartInitException;
import org.openrdf.model.Resource;
import org.openrdf.model.Value;
import org.openrdf.sesame.sail.StatementIterator;

import com.architexa.diagrams.RSECore;
import com.architexa.diagrams.chrono.animation.AnimateCallMadeCommand;
import com.architexa.diagrams.chrono.commands.InstanceCreateCommand;
import com.architexa.diagrams.chrono.commands.MemberCreateCommand;
import com.architexa.diagrams.chrono.commands.ResizeInstanceCommand;
import com.architexa.diagrams.chrono.editpolicies.InstanceResizableEditPolicy;
import com.architexa.diagrams.chrono.editpolicies.SeqOrderedLayoutEditPolicy;
import com.architexa.diagrams.chrono.editpolicies.SeqSelectionEditPolicy;
import com.architexa.diagrams.chrono.figures.FieldFigure;
import com.architexa.diagrams.chrono.figures.HiddenFigure;
import com.architexa.diagrams.chrono.figures.InstanceFigure;
import com.architexa.diagrams.chrono.figures.MemberFigure;
import com.architexa.diagrams.chrono.figures.MethodBoxFigure;
import com.architexa.diagrams.chrono.models.DiagramModel;
import com.architexa.diagrams.chrono.models.GroupedInstanceModel;
import com.architexa.diagrams.chrono.models.InstanceModel;
import com.architexa.diagrams.chrono.models.MethodBoxModel;
import com.architexa.diagrams.chrono.models.NodeModel;
import com.architexa.diagrams.chrono.sequence.SeqPlugin;
import com.architexa.diagrams.chrono.ui.ColorScheme;
import com.architexa.diagrams.chrono.util.GroupedUtil;
import com.architexa.diagrams.chrono.util.InstanceUtil;
import com.architexa.diagrams.chrono.util.MethodUtil;
import com.architexa.diagrams.commands.BreakableCommand;
import com.architexa.diagrams.eclipse.gef.MenuButton;
import com.architexa.diagrams.eclipse.gef.UndoableLabelSource;
import com.architexa.diagrams.jdt.IJavaElementContainer;
import com.architexa.diagrams.jdt.InitializerWrapper;
import com.architexa.diagrams.jdt.RJCore;
import com.architexa.diagrams.jdt.ui.RSEOutlineInformationControl;
import com.architexa.diagrams.model.Artifact;
import com.architexa.diagrams.model.ArtifactFragment;
import com.architexa.diagrams.parts.AnnoLabelCellEditorLocator;
import com.architexa.diagrams.parts.AnnoLabelDirectEditPolicy;
import com.architexa.diagrams.relo.eclipse.gef.EditPartListener2;
import com.architexa.diagrams.ui.ImageCache;
import com.architexa.diagrams.ui.MultiAddCommandAction;
import com.architexa.diagrams.ui.RSEContextMenuProvider;
import com.architexa.diagrams.utils.MoreButtonUtils;
import com.architexa.org.eclipse.draw2d.Figure;
import com.architexa.org.eclipse.draw2d.FlowLayout;
import com.architexa.org.eclipse.draw2d.IFigure;
import com.architexa.org.eclipse.draw2d.Label;
import com.architexa.org.eclipse.draw2d.LineBorder;
import com.architexa.org.eclipse.draw2d.MouseEvent;
import com.architexa.org.eclipse.draw2d.PositionConstants;
import com.architexa.org.eclipse.draw2d.ToolbarLayout;
import com.architexa.org.eclipse.draw2d.geometry.Dimension;
import com.architexa.org.eclipse.draw2d.geometry.Point;
import com.architexa.org.eclipse.draw2d.geometry.Rectangle;
import com.architexa.org.eclipse.gef.DragTracker;
import com.architexa.org.eclipse.gef.EditPart;
import com.architexa.org.eclipse.gef.EditPartViewer;
import com.architexa.org.eclipse.gef.EditPolicy;
import com.architexa.org.eclipse.gef.GraphicalEditPart;
import com.architexa.org.eclipse.gef.Request;
import com.architexa.org.eclipse.gef.commands.Command;
import com.architexa.org.eclipse.gef.commands.CompoundCommand;
import com.architexa.org.eclipse.gef.editpolicies.ResizableEditPolicy;
import com.architexa.org.eclipse.gef.requests.ChangeBoundsRequest;
import com.architexa.org.eclipse.gef.requests.LocationRequest;
import com.architexa.org.eclipse.gef.requests.SelectionRequest;
import com.architexa.org.eclipse.gef.tools.DirectEditManager;
import com.architexa.store.ReloRdfRepository;
import com.architexa.store.StoreUtil;
/**
 * 
 * @author Elizabeth L. Murnane
 *
 */
public class InstanceEditPart extends SeqNodeEditPart implements PropertyChangeListener, IJavaElementContainer, UndoableLabelSource{
	private static final Logger logger = SeqPlugin.getLogger(InstanceEditPart.class);

	String instanceName = "";
	String oldInstanceName = "";
	String className;
	InstanceFigure figure;
	boolean enableGrouping = true;
	
	private IFigure moreMembersButton;
	Label moreLabel;
	String buttonText = " member";

	public InstanceEditPart(String instanceName, String className) {
		this.instanceName = (instanceName==null) ? "" : instanceName;
		this.className = (className==null) ? "" : className;
	}

	@Override
	public void colorFigure(Color color) {
			((InstanceFigure)getFigure()).colorFigure(color);
	}
	
	@Override
	protected void refreshVisuals() {
		InstanceFigure instanceFigure = (InstanceFigure) getFigure();
		instanceFigure.colorFigure(ColorScheme.instanceFigureBackground);
		if (com.architexa.diagrams.ColorScheme.SchemeV1) {
			ImageDescriptor imgDesc = getInstanceImage();
			if (imgDesc == null)
				return;
			instanceFigure.iconLabel.setIcon(ImageCache.calcImageFromDescriptor(imgDesc));
		} else {
			instanceFigure.iconLabel.setIcon(null);
		}
	}
	
	@Override
	protected IFigure createFigure() {
		ReloRdfRepository repo = StoreUtil.getDefaultStoreRepository();
		Artifact parentArt = ((InstanceModel)getModel()).getArt().queryParentArtifact(repo);
		while(parentArt!=null && !RJCore.packageType.equals(parentArt.queryType(repo))){
			parentArt = parentArt.queryParentArtifact(repo);
		}
		String packageName = parentArt!=null ? parentArt.queryName(repo): null;
		ImageDescriptor imgDesc = getInstanceImage();
		
		Image image = null;
		
		if (com.architexa.diagrams.ColorScheme.SchemeV1)
			image = ImageCache.calcImageFromDescriptor(imgDesc);
		
		figure = new InstanceFigure(instanceName, className, packageName, image);
		((InstanceModel)getModel()).setFigure(figure);
		figure.getInstanceBox().addMouseMotionListener(this);
		figure.getInstanceBox().addFigureListener(this);
		moreMembersButton = getMoreButton();
		return figure;
	}

	
	private ImageDescriptor getInstanceImage() {
		ImageDescriptor imgDesc;
		if (((InstanceModel)getModel()).isBean())
			imgDesc = SeqPlugin.getImageDescriptor("/icons/bean.gif");
		else
			imgDesc = InstanceUtil.getInstanceIconDescriptor((InstanceModel)getModel());
		return imgDesc;
	}

	// Returns the method box edit part if the cursor is on the label of the method box else returns the parent instance
	@Override
	public EditPart getTargetEditPart(Request request) {
		Point cursorLocation = new Point(0,0);
		if (request instanceof SelectionRequest)
			cursorLocation = ((SelectionRequest)request).getLocation();
		
		for (Object obj : this.getChildren()) {
			if (obj instanceof MethodBoxEditPart) {
				MethodBoxFigure figure = (MethodBoxFigure) ((MethodBoxEditPart)obj).getFigure();
				Rectangle labelBounds = new Rectangle(figure.getNoConnectionsBoxLabel().getBounds());
				if (labelBounds == null || cursorLocation == null) continue; // When you click on the instance box sometimes the cursor locations comes as null
				((InstanceFigure)getFigure()).getChildrenContainer().translateToAbsolute(labelBounds);
				if (labelBounds.contains(cursorLocation))
					return (EditPart) obj;
			}
		}
		return super.getTargetEditPart(request);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	protected void registerVisuals() {
		// Since the instance label box and the children container are placed
		// in different layers, we need to make sure they're both mapped
		// to this edit part
		getViewer().getVisualPartMap().put(getFigure(), this);
		getViewer().getVisualPartMap().put(figure.getChildrenContainer(), this);
	}

	@Override
	protected void createEditPolicies() {
		super.createEditPolicies();
		InstanceResizableEditPolicy resizePolicy = new InstanceResizableEditPolicy();
		resizePolicy.setResizeDirections(PositionConstants.EAST_WEST);
		installEditPolicy(EditPolicy.GRAPHICAL_NODE_ROLE, resizePolicy);
		installEditPolicy(EditPolicy.LAYOUT_ROLE, new SeqOrderedLayoutEditPolicy());
		installEditPolicy(EditPolicy.DIRECT_EDIT_ROLE, new AnnoLabelDirectEditPolicy(this, "Edit Instance Name"));
	}

	@Override
	public Command getCommand(Request request) {
		if (request instanceof ChangeBoundsRequest) {
			if (request.getType().equals("resize"))
				return new ResizeInstanceCommand((InstanceModel) this.getModel(), ((ChangeBoundsRequest)request).getSizeDelta());
		}
		return super.getCommand(request);
	}
	
	@Override
	public List<ArtifactFragment> getModelChildren() {
		return ((InstanceModel)getModel()).getChildren();
	}

	@Override
	public void performRequest(Request req) {
		if(REQ_OPEN.equals(req.getType()) || REQ_DIRECT_EDIT.equals(req.getType())) {
			Point loc = ((LocationRequest) req).getLocation().getCopy();
			Figure instFigure = ((InstanceFigure) getFigure()).getInstanceBox();
			instFigure.translateToRelative(loc); // handle scrolling
			if (!instFigure.getBounds().contains(loc))
				return;
			
			showIsEditableTooltip(true);
			performDirectEdit();
		}
	}

	@Override
	protected void showIsEditableTooltip(boolean isEditable) {
		if (isEditable) {
			String classNameText = "Group";
			if (!(this instanceof GroupedInstanceEditPart) && className != null)
				classNameText = className;
			figure.getInstanceBox().setToolTip(new Label("Edit instance name of this " + classNameText + " Object"));
			return;
		}
		figure.getInstanceBox().setToolTip(figure.getToolTipLabel());
	}

	protected DirectEditManager manager;

	protected void performDirectEdit() {
		if (manager == null)
			manager = new UnfocusableLabelDirectEditManager(this, TextCellEditor.class,
					new AnnoLabelCellEditorLocator(getAnnoLabelFigure()),
					getAnnoLabelFigure());
		manager.show();
	}
	
	public IFigure getAnnoLabelFigure() {
		return ((InstanceFigure)getFigure()).getInstanceNameLabel();
	}

	public String getAnnoLabelText() {
		return instanceName;
	}

	public String getOldAnnoLabelText() {
		return oldInstanceName;
	}
	
	public void setOldAnnoLabelText(String oldName) {
		oldInstanceName = oldName;
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
		((ArtifactFragment)getModel()).setInstanceName(str);
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		String propName = evt.getPropertyName();
		if(ArtifactFragment.PROPERTY_EDIT_INSTANCE_NAME.equals(propName)) {
			changeName();
		}
		if(NodeModel.PROPERTY_WIDTH.equals(propName))
			changeWidth();
		super.propertyChange(evt);
	}
	
	private void changeWidth() {
		int newWidth = ((InstanceModel) getModel()).getInstanceBoxWidth();
		// we want to subtract the size of the instance name/class from the size of the spaces added
		int oldWidth = ((InstanceFigure)getFigure()).nameAndIconContainer.getBounds().width - ((InstanceFigure)getFigure()).spacerLabel.getBounds().width;
		newWidth = newWidth - oldWidth; 
		newWidth = newWidth / 3;
		// add a spacer with 'newWidth' number of blank spaces
		((InstanceFigure) getFigure()).spacerLabel.setText(String.format("%1$#"+newWidth+"s", ""));
		
		// TODO: Try to get this to work? Something with layout is overriding this...
		//((InstanceFigure) getFigure()).spacerLabel.setMinimumSize(new Dimension(newWidth, 30));//d)setText(String.format("%1$#"+newWidth+"s", ""));
		//((InstanceFigure) getFigure()).setMinimumSize(new Dimension(newWidth, 30));//d)setText(String.format("%1$#"+newWidth+"s", ""));
	}

	@Override
	protected void changeName() {
		String str = ((ArtifactFragment)getModel()).getInstanceName();
		((InstanceFigure) getFigure()).setInstanceName(str);
		this.instanceName = str;
	}
	
	public void buildContextMenu(IMenuManager menu, List<InstanceEditPart> selectedParts) {
		if(selectedParts==null || selectedParts.size()==0) return;

		if(selectedParts.size()==1 && selectedParts.get(0).getModel() instanceof GroupedInstanceModel){
			if (!((GroupedInstanceModel) selectedParts.get(0).getModel()).getInstanceChildren().isEmpty()) {
				IAction unGroupAction = getUngroupAction(selectedParts);
				menu.appendToGroup(RSEContextMenuProvider.GROUP_RSE_TOOLS, unGroupAction);
			}
		}

		if(selectedParts.size()>1 && enableGrouping) {
			IAction groupAction = getGroupAction(selectedParts);
			groupAction.getId();
			menu.appendToGroup(RSEContextMenuProvider.GROUP_RSE_TOOLS, groupAction);
		} 
		if (selectedParts.size() == 2) {
			for (EditPart part : selectedParts)
				if (part instanceof GroupedInstanceEditPart) return;
			IAction showAllMessagesAction = getShowAllMessagesAction(selectedParts.get(0), selectedParts.get(1));
			menu.appendToGroup(RSEContextMenuProvider.GROUP_RSE_TOOLS, showAllMessagesAction);
		}
	}

	private IAction getUngroupAction(final List<InstanceEditPart> selectedParts) {
		ImageDescriptor ungroupIcon = SeqPlugin.getImageDescriptor("icons/ungroup.PNG");
		IAction action = new Action("Ungroup", ungroupIcon){
			@Override
			public void run(){
				unGroupInstances(selectedParts);
			}
		};
		return action;
	}

	private void unGroupInstances(List<InstanceEditPart> selectedParts){
		DiagramModel diagram = (DiagramModel) ((DiagramEditPart)getViewer().getContents()).getModel();
		GroupedInstanceModel groupedInstanceModel= (GroupedInstanceModel) selectedParts.get(0).getModel();

		CompoundCommand ungroupCommand = new CompoundCommand("UnGroup");
		GroupedUtil.ungroup(diagram, groupedInstanceModel, ungroupCommand);

		((DiagramEditPart)getViewer().getContents()).execute(ungroupCommand);
	}

	private IAction getGroupAction(final List<InstanceEditPart> selectedParts) {
		ImageDescriptor groupIcon = SeqPlugin.getImageDescriptor("icons/group.PNG");
		IAction action = new Action("Group", groupIcon) {
			@Override
			public void run(){
				groupInstances(selectedParts);
			}
		};
		return action;
	}

	private void groupInstances(List<InstanceEditPart> selectedParts){

		CompoundCommand cmd = new CompoundCommand("Instance Grouping");
		DiagramModel diagram = (DiagramModel) ((DiagramEditPart)getViewer().getContents()).getModel();
		GroupedUtil.createGroupFromEditParts(selectedParts, diagram, cmd);
		((DiagramEditPart)getViewer().getContents()).execute(cmd);
	}

	private IAction getShowAllMessagesAction(final InstanceEditPart instance1, final InstanceEditPart instance2) {
		String instance1String = instance1.getModel().toString().trim();
		String instance2String = instance2.getModel().toString().trim();
		if(instance1String.indexOf(":")==0) instance1String = instance1String.replace(":", "").trim();
		if(instance2String.indexOf(":")==0) instance2String = instance2String.replace(":", "").trim();
		IAction action = new Action("Show Interactions from " + instance1String + " to " + instance2String) {
			@Override
			public void run() {
				BreakableCommand addMultipleCallsCmd = new BreakableCommand("adding all messages between classes", AnimateCallMadeCommand.class);
				instance1.addMessagesTo((InstanceModel)instance2.getModel(), addMultipleCallsCmd);
				((DiagramEditPart)getViewer().getContents()).execute(addMultipleCallsCmd);
				instance2.removeIfDuplicateWithNoChildrenAndNoCorrespondingInstance();
			}
		};
		return action;
	}

	private IJavaElement cachedIJE = null;

	private RSEOutlineInformationControl popup; 
	public IJavaElement getJaveElement() {
		if (cachedIJE == null && getModel() instanceof InstanceModel)
			cachedIJE = RJCore.resourceToJDTElement(StoreUtil.getDefaultStoreRepository(), getContainedArtifact().elementRes);
		return cachedIJE;
	}

	public Artifact getContainedArtifact() {
		return ((InstanceModel)getModel()).getArt();
	}

	@Override
	public IAction getOpenInJavaEditorAction(String actionName, ImageDescriptor image) {

		IAction action = new Action(actionName, image) {
			@Override
			public void run() {
				try {
					JavaUI.openInEditor(getJaveElement());
				} catch (PartInitException e) {
					logger.error("Unexpected Exception.", e);
				} catch (JavaModelException e) {
					logger.error("Unexpected Exception.", e);
				}
			}
		};
		return action;
	}

	public String getInstanceName() {
		return instanceName;
	}

	public String getClassName() {
		return className;
	}

	public IFigure getMoreMethodsButton() {
		if(moreMembersButton==null) moreMembersButton = getMoreButton();
		return moreMembersButton;
	}

	@Override
	protected void addChildVisual(EditPart childEditPart, int index) {
		int numOfMethodBoxes = 0;
		Figure childrenContainer = ((InstanceFigure)getFigure()).getChildrenContainer();
		for(int i=0; i<childrenContainer.getChildren().size(); i++) {
			Object entry = childrenContainer.getChildren().get(i);
			if(entry instanceof MemberFigure || entry instanceof HiddenFigure)
				numOfMethodBoxes++;
		}

		IFigure child = ((GraphicalEditPart)childEditPart).getFigure();
		if(index==-1 || index==numOfMethodBoxes) {
			getContentPane().add(child, -1);
		} else {
			int indexToAddAt = -1;
			int methodBoxFigIndex = -1;
			for(Object obj : childrenContainer.getChildren()) {
				if(obj instanceof MemberFigure || obj instanceof HiddenFigure) methodBoxFigIndex++;
				if(methodBoxFigIndex >= index) {
					// If the child is a MethodBoxFigure, it has a gap and a 
					// no connections label, so we need to subtract 2 from the
					// index to take these into account. If the child is a FieldFigure, 
					// it has a gap, so we need to subtract 1.
					int numExtras = obj instanceof MethodBoxFigure ? 2 : obj instanceof FieldFigure ? 1 : 0;
					indexToAddAt = childrenContainer.getChildren().indexOf(obj) - numExtras;
					break;
				}
			}
			getContentPane().add(child, indexToAddAt);
		}
	}

	@Override
	protected void removeChildVisual(EditPart childEditPart) {

		IFigure child = ((GraphicalEditPart)childEditPart).getFigure();

		// Functionality to remove override indicators when the child method gets a new parent, e.g becomes a self call to a method
		if(child instanceof MethodBoxFigure){
			IFigure overrideIndicator = ((MethodBoxFigure)child).getOverridesIndicator();
			IFigure overriddenIndicator = ((MethodBoxFigure)child).getOverriddenIndicator(); 
			if( overrideIndicator != null &&
					getHandleLayer().getChildren().contains(overrideIndicator))
				getHandleLayer().remove(((MethodBoxFigure)child).getOverridesIndicator());
			if( overriddenIndicator!= null &&
					getHandleLayer().getChildren().contains(overriddenIndicator))
				getHandleLayer().remove(((MethodBoxFigure)child).getOverriddenIndicator());
		}

		super.removeChildVisual(childEditPart);
	}

	@Override
	public void mouseEntered(MouseEvent me) {
		displayOrHideBackwardMessages(true);
	}

	@Override
	public void mouseExited(MouseEvent me) {
		displayOrHideBackwardMessages(false);
	}

	private void displayOrHideBackwardMessages(boolean visible) {
		for(Object child : getChildren()) {
			if(!(child instanceof MethodBoxEditPart)) continue;
			((MethodBoxEditPart)child).displayOrHideCallAndContainedBackwardCalls(visible);
		}
	}

	public int setMemberCount(){
		int numMembers = ((InstanceModel)getModel()).getMemberMenuCount();
		String plural = (numMembers != 1) ? "s " : " ";
		moreLabel.setText(numMembers + buttonText + plural);
		return numMembers;
	}
	
	public IFigure getMoreButton() {
		moreLabel = new Label();
		moreLabel.setLabelAlignment(PositionConstants.CENTER);
		
		final int numMembers = setMemberCount();
		IFigure moreFig = new Figure();
		ToolbarLayout tb = new ToolbarLayout(true);
		tb.setMinorAlignment(ToolbarLayout.ALIGN_CENTER);
		moreFig.setLayoutManager(tb);
		moreFig.add(moreLabel);

		moreFig.add(MoreButtonUtils.getMoreButtonTriangle());

		final IFigure moreContainer = new Figure();
		FlowLayout layout = new FlowLayout(true);
		layout.setMajorAlignment(ToolbarLayout.ALIGN_BOTTOMRIGHT);
		moreContainer.setLayoutManager(layout);
		moreContainer.add(moreFig);

		MenuButton menuButton = new MenuButton(moreContainer, getViewer()) {
			@Override
			public void buildMenu(IMenuManager menu) {
				if(numMembers==0) return; // if no members, don't open an empty menu
				MenuButton button = this;
				buildMoreChildrenContextMenu(button); 
			}};

			// update label
			addEditPartListener(new EditPartListener2.Stub() {
				@Override
				public void childAdded(EditPart child, int index) {
					updateMoreButton(true, child);
				}
				@Override
				public void removedChild(EditPart child) {
					updateMoreButton(false, child);
				}

				@Override
				public void removingChild(EditPart child, int index) {
					updateMoreButton(false, child);
				}
			});

			IFigure innerMoreButtonFig = menuButton;
			Figure moreButton = new Figure();
			moreButton.setBackgroundColor(ColorScheme.instanceFigureBackground);
			moreButton.setOpaque(true);
			moreButton.setLayoutManager(new ToolbarLayout() {
				@Override
				protected Dimension calculatePreferredSize(IFigure container, int wHint, int hHint) {
					Dimension size = super.calculatePreferredSize(container, wHint, hHint);
					if(getFigure()==null) return size;

					int instanceBoxWidth = ((InstanceFigure)getFigure()).getInstanceBox().getSize().width+2;
					return new Dimension(Math.max(size.width, instanceBoxWidth), size.height);
				}
			});
			moreButton.add(innerMoreButtonFig);
			moreButton.setBorder(new LineBorder());

			return moreButton;
	}

	private void buildMoreChildrenContextMenu(MenuButton button) {

		// Give menu a title label of "Members of MyClass:"
		String titleText = "Members of "+getClassName()+":";

		// contained methods, inner classes, and enums
		List<Object> actions = new ArrayList<Object>(getAllMemberMenuActions());
		// inherited methods that this class does not implement itself
		List<MultiAddCommandAction> inheritedMethods = getInheritedMethodsMenuActions();

		// Show inherited methods in a subtree in a chrono instance's members button 
		Map<Object, List<MultiAddCommandAction>> inheritedSubTree = new HashMap<Object, List<MultiAddCommandAction>>();
		if(!inheritedMethods.isEmpty()) {
			String inheritedMethodsHeading = "Inherited Methods";
			actions.add(inheritedMethodsHeading);
			inheritedSubTree.put(inheritedMethodsHeading, inheritedMethods);
		}

		// Members could have multiple access levels or 
		// kinds, and members will not be library code
		String[] filters = new String[] {
				RSEOutlineInformationControl.FILTERS_ACCESS_LEVEL, 
				RSEOutlineInformationControl.FILTERS_MEMBER_KIND};

		if (popup!=null && popup.active) 
			return;
		popup = new RSEOutlineInformationControl(StoreUtil.getDefaultStoreRepository(), titleText, inheritedSubTree, filters);
		popup.setInput(actions);
		

		// Make button to Add All members that are visible and enabled in the menu
		List<MultiAddCommandAction> macas = new ArrayList<MultiAddCommandAction>();
		for(Object o : actions) 
			if(o instanceof MultiAddCommandAction) macas.add((MultiAddCommandAction)o);
		macas.addAll(inheritedMethods);
		IAction addAllAction = null;
		if(macas.size() > 1) // only need Add All action if more than one item in menu list
			addAllAction = getShowAllItemAction(macas, popup, (DiagramEditPart)getViewer().getContents());
		if(addAllAction!=null) {
			List<IAction> addAllActionAsList = new ArrayList<IAction>();
			addAllActionAsList.add(addAllAction);
			popup.setButtonInput(addAllActionAsList);
		}

		// menu should open along the right hand side of the members button
		EditPartViewer parentViewer = getViewer();
		Control parent = parentViewer.getControl();
		Rectangle figBounds = button.getBounds().getCopy();
		button.translateToAbsolute(figBounds);
		org.eclipse.swt.graphics.Point menuLocation = parent.toDisplay(figBounds.getTopRight().x+1, figBounds.getTopRight().y-1);
		popup.setLocation(menuLocation);

		// make sure menu is proper size to show all components but not stretch too wide
		popup.pack();
		popup.setInitSize();
		popup.open();
	}

	private List<MultiAddCommandAction> getAllMemberMenuActions() {	

		List<MultiAddCommandAction> addActions = new ArrayList<MultiAddCommandAction>();

		// Methods
		for (MethodBoxModel method : ((InstanceModel)getModel()).getDeclaredMethodsCurrentlyInMenu()) {
			MultiAddCommandAction action = getAddMethodCommandAction(method, false);
			addActions.add(action);
		}

		// Inner classes and enums
		for (InstanceModel instance : ((InstanceModel)getModel()).getModelsOfTypeMembers()) {
			// Inner classes and enums listed in the 'members' button 
			// do not need to have the containing class prefix included 
			// in their menu label. Can simply put "InnerClass" in the 
			// menu instead of "OuterClass.InnerClass" since it's obvious 
			// InnerClass is contained in OuterClass since it's OuterClass's 
			// members button (and also otherwise every single entry has that prefix). 
			int innerClassParentPrefix = instance.getClassName().indexOf(".")+1;
			String label = instance.getClassName().substring(innerClassParentPrefix);
			MultiAddCommandAction action = getAddInstanceCommandAction(label, instance);
			addActions.add(action);
		}

		return addActions;
	}

	private List<MultiAddCommandAction> getInheritedMethodsMenuActions() {	
		List<MultiAddCommandAction> inheritedMethods = new ArrayList<MultiAddCommandAction>();
		for (MethodBoxModel method : ((InstanceModel)getModel()).getInheritedMethodsCurrentlyInMenu()) {
			MultiAddCommandAction action = getAddMethodCommandAction(method, true);
			inheritedMethods.add(action);
		}
		return inheritedMethods;
	}

	private MultiAddCommandAction getAddInstanceCommandAction(String label, final InstanceModel instance) {

		// Once an inner class has been selected from the list and added to the 
		// diagram, it is not removed from the list. This means it can be selected 
		// again, so creating a new instance model whenever it is selected
		InstanceModel innerClass = new InstanceModel(null, instance.getClassName(), instance.getResource());
		DiagramModel diagram = (DiagramModel) ((DiagramEditPart)getViewer().getContents()).getModel();
		InstanceCreateCommand cmd = new InstanceCreateCommand(innerClass, diagram, diagram.getChildren().indexOf((InstanceModel)getModel())+1, true, true);

		MultiAddCommandAction action = makeMultiAddCommandAction(label, cmd);
		ImageDescriptor des = InstanceUtil.getInstanceIconDescriptor(instance);
		if (des != null) action.setImageDescriptor(des);

		return action;
	}

	private MultiAddCommandAction getAddMethodCommandAction(final MethodBoxModel methodBox, boolean inherited) {
		MemberCreateCommand cmd = new MemberCreateCommand(methodBox, methodBox.getInstanceModel(), MemberCreateCommand.FROM_INSTANCE);

		String label = getMenuMemberLabel(methodBox, inherited);
		MultiAddCommandAction action = makeMultiAddCommandAction(label, cmd);
		ImageDescriptor icon = MethodUtil.getMethodIconDescriptor(methodBox, inherited);
		if(icon!=null) action.setImageDescriptor(icon);
		
		return action;
	}
	
	/*
	 * Make the label "[method name] : [return type]", ie "method() : void" rather
	 * than "void method()" so that the label's first character is part of the name
	 * of the method. This will make typing to filter members more straightforward for
	 * the user and also match the way eclipse displays member type in the Outline view
	 */
	private String getMenuMemberLabel(MethodBoxModel method, boolean inherited) {

		String labelWithReturnType = method.getName(); // looks like "void method()"
		String labelNoReturnType = SeqSelectionEditPolicy.getMethodNameNoReturnType(
				method); // looks like "method()"

		// CodeUnit.getLabel will return a string like "packagename.superclass.method()" for
		// inherited methods, so remove everything before last "." to get just the method name
		if(inherited) {
			int methodStart = labelNoReturnType.lastIndexOf(".");
			if(methodStart!=-1) labelNoReturnType = labelNoReturnType.substring(methodStart+1);
		}

		// "void" or "" for constructors, static initializers, and inner classes
		String returnType = labelWithReturnType.replace(labelNoReturnType, "").trim();

		// if not "", place it after the method name with a colon in between
		if(returnType.length()!=0) returnType = " : "+returnType;
		String label = labelNoReturnType.trim()+returnType;
		if(inherited) {
			// Label for inherited methods will look like
			// "[method name] : [return type] - in [Super Class]"
			//  ie "method() : void - in Foo"
			IType declaringClass = method.getMethod().getDeclaringType();
			label = label+" - in "+declaringClass.getElementName();
		}
		return label;
	}

	private MultiAddCommandAction makeMultiAddCommandAction(String label, final Command cmd) {
		return new MultiAddCommandAction(label, null) {
			@Override
			public Command getCommand(Map<Artifact, ArtifactFragment> addedArtToAFMap) {        		
				CompoundCommand tgtCmd = new CompoundCommand();
				tgtCmd.add(cmd);
				return tgtCmd;
			}
			@Override
			// Need to override run() because null was passed as this action's brc since
			// DiagramEditPart is not a BasicRootController (TODO DiagramEditPart implements BasicRootController)
			public void run() {
				((DiagramEditPart)getViewer().getContents()).execute(getCommand(new HashMap<Artifact, ArtifactFragment>()));
			}
		};
	}

	private void updateMoreButton(boolean removeFromMenu, EditPart part) {
		List<MethodBoxModel> methodsInMenu = ((InstanceModel)getModel()).getModelsOfMethodMembers();
		if (methodsInMenu == null) return;
		if(!(part.getModel() instanceof MethodBoxModel)) return;

		MethodBoxModel box = (MethodBoxModel) part.getModel();
		List<MethodBoxModel> methodsInMenuCopy = new ArrayList<MethodBoxModel>(methodsInMenu);
		if(removeFromMenu) {
			for(MethodBoxModel mbm : methodsInMenuCopy) {
				if(box.getMethod()!=null && box.getMethod().equals(mbm.getMethod())) {
					methodsInMenu.remove(mbm);
					break;
				}
			}
		} else {
			boolean containsAlready = false;
			for(MethodBoxModel mbm : methodsInMenu) {
				if(box.getMethod()!=null && box.getMethod().equals(mbm.getMethod())) containsAlready = true;
			}
			if(!containsAlready && box.getMethod()!=null) {
				// We are re-adding a method to the list because it has just been 
				// removed from this instance. This means that the edit part is being 
				// deactivated and the figure destroyed, so we need to create a new 
				// model for the method and cannot use the given edit part's model.
				MethodBoxModel newModel = null;
				for(IMethodBinding decl : ((InstanceModel)getModel()).getDeclaredAndInheritedMethods()) {
					if(!(decl.getJavaElement() instanceof IMethod)) continue;
					IMethod method = (IMethod) decl.getJavaElement();
					if(box.getMethod().equals(method)) {
						newModel = new MethodBoxModel((InstanceModel)getModel(), method, MethodBoxModel.declaration);
						methodsInMenu.add(newModel);
						break;
					}
				}
				for(InitializerWrapper init : ((InstanceModel)getModel()).getInitializers()) {
					if(box.getMethod().equals(init)) {
						newModel = new MethodBoxModel((InstanceModel)getModel(), init, MethodBoxModel.declaration);
						methodsInMenu.add(newModel);
						break;
					}
				}
				if(newModel==null) {
					// Couldn't find the method by looking through
					// allMethodDeclarations and allInitializerDeclarations.
					// Could be because this is an anonymous class constructor 
					// whose astnode is null so we need to use the repo
					ReloRdfRepository repo = StoreUtil.getDefaultStoreRepository();
					StatementIterator containsIter = repo.getStatements(((InstanceModel)getModel()).getResource(), RSECore.contains, null);
					while(containsIter.hasNext()) {
						Value containee = containsIter.next().getObject();
						if(containee==null || !(containee instanceof Resource)) continue;
						if(((Resource)containee).equals(box.getMethodRes())) {
							newModel = new MethodBoxModel((InstanceModel)getModel(), (Resource)containee, MethodBoxModel.declaration);
							methodsInMenu.add(newModel);
							break;
						}
					}
				}
			}
		}

		Collections.sort(methodsInMenu, InstanceModel.methodComparator);
		((InstanceModel)getModel()).updateMethodLists();
		setMemberCount();
	}

	// TODO This method is identical to CodeUnitEditPart.getShowAllItemAction except
	// that in CUEP, the DiagramEditPart parameter is instead a BasicRootController.
	// Once DiagramEditPart implements BasicRootController, this method can be
	// removed and the one in CUEP used instead.
	private IAction getShowAllItemAction(final List<MultiAddCommandAction> addActions, 
			final RSEOutlineInformationControl navAidMenu, final DiagramEditPart diagramEP) {
		final String actionText = "Add All";
		IAction showAllAction = new Action(actionText) {
			@Override
			public void run() {
				CompoundCommand showAllCmd = new CompoundCommand(actionText);

				// When user selects Add All, only calls that are visible 
				// in the menu and enabled should be added to the diagram
				List<MultiAddCommandAction> visibleAndEnabledItems = new ArrayList<MultiAddCommandAction>();
				getVisibleAndEnabledItems(navAidMenu.getTreeViewer().getTree().getItems(),
						visibleAndEnabledItems);

				Map<Artifact, ArtifactFragment> addedArtToAFMap = new HashMap<Artifact, ArtifactFragment>();
				for (MultiAddCommandAction maca : addActions) {
					if(!visibleAndEnabledItems.contains(maca)) continue;
					MoreButtonUtils.addCommand(maca, showAllCmd, addedArtToAFMap);
				}
				if(!showAllCmd.isEmpty()) {
					navAidMenu.dispose(); // close the menu
					diagramEP.execute(showAllCmd); // and add the calls
				}
			}
			public void getVisibleAndEnabledItems(TreeItem[] menuItems, 
					List<MultiAddCommandAction> visibleAndEnabledItems) {
				for(TreeItem visibleItem : menuItems) {
					Object data = visibleItem.getData();
					if(data instanceof MultiAddCommandAction &&
							((MultiAddCommandAction)data).isEnabled())
						visibleAndEnabledItems.add((MultiAddCommandAction) data);
					// Only add expanded subtrees; if the user has collapsed a subtree
					// of members, we assume they are not of interest and don't add
					boolean isExpanded = 
						visibleItem.getItemCount()==0 || // consider expanded b/c not a parent node
						visibleItem.getExpanded(); // is a subtree parent, so test if expanded
					if(isExpanded)
						getVisibleAndEnabledItems(visibleItem.getItems(), visibleAndEnabledItems);
				}
			}
		};
		return showAllAction;
	}

	@Override
	public void setSelected(int value) {
		super.setSelected(value);
	}
	
	//TODO check with Liz
//	private Triangle2 getMoreButtonTriangle(int x, int y) {
//		Triangle2 t;
//		t = new Triangle2();
//		t.setClosed(false);
//		t.setFill(false);
//		t.setBounds(new Rectangle(x, y, 5, 5));
//		t.setBackgroundColor(ColorScheme.instanceFigureMoreButtonArrows);
//		t.setDirection(PositionConstants.EAST);
//		return t;
//	}

	public void addMessagesTo(InstanceModel model2, BreakableCommand addMultipleCallsCmd) {
		InstanceModel instance = (InstanceModel) getModel();
		for(IMethodBinding method : instance.getDeclaredAndInheritedMethods()) {
			ASTNode node =  RJCore.getCorrespondingASTNode((IMethod)method.getJavaElement());
			if(!(node instanceof MethodDeclaration)) {
				//TODO: When would this happen? Figure 
				//it out and handle if necessary
				continue;
			}
			MethodDeclaration methodDecl = (MethodDeclaration)node;
			if(methodDecl==null || methodDecl.getBody()==null) continue;

			IMethod methodDeclElmt = (methodDecl==null || methodDecl.resolveBinding()==null) ? null :(IMethod)methodDecl.resolveBinding().getJavaElement();
			MethodBoxModel declarationBox = MethodUtil.findDeclarationHolder(methodDeclElmt, instance, addMultipleCallsCmd);
			MemberCreateCommand createDeclCmd = null;
			if(declarationBox==null) {
				declarationBox = methodDeclElmt==null ? new MethodBoxModel(instance, MethodBoxModel.declaration) : new MethodBoxModel(instance, methodDeclElmt, MethodBoxModel.declaration);
				createDeclCmd = new MemberCreateCommand(declarationBox, instance, MemberCreateCommand.NONE);
				addMultipleCallsCmd.add(createDeclCmd);
			}
			boolean makesCalls = MethodUtil.displayCallsMadeByMethodDeclToInstance(declarationBox, methodDecl, model2,  (DiagramEditPart)getViewer().getContents(), addMultipleCallsCmd, new NullProgressMonitor());

			// If declaration wasn't already in the diagram,
			// only want to add it if it actually makes calls
			if(createDeclCmd!=null && !makesCalls) {
				addMultipleCallsCmd.remove(createDeclCmd);
			}
		}
	}


	/**
	 * Removes this from the diagram if it has no children, if it
	 * is not a particular instance of the class it represents, and
	 * if another component that represents the same class and does
	 * correspond to a particular instance of it is present in the diagram
	 */
	public void removeIfDuplicateWithNoChildrenAndNoCorrespondingInstance() {
		if(getModelChildren().size()>0) return;
		if(!getInstanceName().trim().equals("")) return;

		DiagramEditPart diagramEP = (DiagramEditPart) getViewer().getContents();
		for(Object child : diagramEP.getChildren()) {
			if(this.equals(child) || !(child instanceof InstanceEditPart)) continue;
			if(!this.getClassName().equals(((InstanceEditPart)child).getClassName())) continue; //different class
			((DiagramModel)diagramEP.getModel()).removeChild((InstanceModel)this.getModel());
			return;
		}
	}

	@Override
	public DragTracker getDragTracker(Request request) {
		return new com.architexa.org.eclipse.gef.tools.DragEditPartsTracker(this){
			@Override
			protected boolean isMove() {
				EditPart part = getSourceEditPart();
				while (part != getTargetEditPart() && part != null) {
					if ( part.getSelected() != EditPart.SELECTED_NONE)
						return true;
					part = part.getParent();
				}
				return false;
			}
		};
	}

}
