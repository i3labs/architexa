package com.architexa.diagrams.chrono.editpolicies;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.ConstructorInvocation;
import org.eclipse.jdt.core.dom.Initializer;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SuperConstructorInvocation;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.internal.core.BinaryType;
import org.eclipse.jdt.internal.corext.callhierarchy.CallLocation;
import org.eclipse.jdt.internal.corext.util.JdtFlags;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.progress.UIJob;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.sesame.sail.StatementIterator;

import com.architexa.diagrams.chrono.animation.AnimateCallMadeCommand;
import com.architexa.diagrams.chrono.animation.AnimateCalledByCommand;
import com.architexa.diagrams.chrono.animation.AnimateOverrideCommand;
import com.architexa.diagrams.chrono.animation.AnimateOverrideCommand.AnimateOverrideConnectionCommand;
import com.architexa.diagrams.chrono.commands.ChangeParentOfMethodCommand;
import com.architexa.diagrams.chrono.commands.ConnectionCreateCommand;
import com.architexa.diagrams.chrono.commands.MemberCreateCommand;
import com.architexa.diagrams.chrono.editparts.DiagramEditPart;
import com.architexa.diagrams.chrono.editparts.FieldEditPart;
import com.architexa.diagrams.chrono.editparts.GroupedInstanceEditPart;
import com.architexa.diagrams.chrono.editparts.InstanceEditPart;
import com.architexa.diagrams.chrono.editparts.MethodBoxEditPart;
import com.architexa.diagrams.chrono.editparts.UserCreatedInstanceEditPart;
import com.architexa.diagrams.chrono.editparts.UserCreatedMethodBoxEditPart;
import com.architexa.diagrams.chrono.figures.InstanceFigure;
import com.architexa.diagrams.chrono.figures.LeftRightBorder;
import com.architexa.diagrams.chrono.figures.MethodBoxFigure;
import com.architexa.diagrams.chrono.models.ConnectionModel;
import com.architexa.diagrams.chrono.models.DiagramModel;
import com.architexa.diagrams.chrono.models.GroupedInstanceModel;
import com.architexa.diagrams.chrono.models.HiddenNodeModel;
import com.architexa.diagrams.chrono.models.InstanceModel;
import com.architexa.diagrams.chrono.models.MemberModel;
import com.architexa.diagrams.chrono.models.MethodBoxModel;
import com.architexa.diagrams.chrono.models.NodeModel;
import com.architexa.diagrams.chrono.sequence.SeqPlugin;
import com.architexa.diagrams.chrono.util.GroupedUtil;
import com.architexa.diagrams.chrono.util.InstanceUtil;
import com.architexa.diagrams.chrono.util.MemberUtil;
import com.architexa.diagrams.chrono.util.MethodUtil;
import com.architexa.diagrams.commands.BreakableCommand;
import com.architexa.diagrams.eclipse.gef.MenuButton;
import com.architexa.diagrams.jdt.Activator;
import com.architexa.diagrams.jdt.AnonymousClassConstructor;
import com.architexa.diagrams.jdt.CompilerGeneratedDefaultConstructor;
import com.architexa.diagrams.jdt.ImplicitConstructorCallLocation;
import com.architexa.diagrams.jdt.InitializerWrapper;
import com.architexa.diagrams.jdt.Invocation;
import com.architexa.diagrams.jdt.RJCore;
import com.architexa.diagrams.jdt.actions.AddCalleeHierarchy;
import com.architexa.diagrams.jdt.model.CodeUnit;
import com.architexa.diagrams.jdt.ui.RSEOutlineInformationControl;
import com.architexa.diagrams.jdt.utils.MethodInvocationFinder;
import com.architexa.diagrams.model.Artifact;
import com.architexa.diagrams.model.ArtifactFragment;
import com.architexa.diagrams.model.DirectedRel;
import com.architexa.diagrams.parts.CommentComponentEditPolicy;
import com.architexa.diagrams.relo.parts.AbstractReloRelationPart;
import com.architexa.diagrams.ui.ImageCache;
import com.architexa.diagrams.ui.MultiAddCommandAction;
import com.architexa.diagrams.ui.RSEContextMenuProvider;
import com.architexa.diagrams.utils.MoreButtonUtils;
import com.architexa.intro.preferences.LibraryPreferences;
import com.architexa.org.eclipse.draw2d.ActionEvent;
import com.architexa.org.eclipse.draw2d.ActionListener;
import com.architexa.org.eclipse.draw2d.Button;
import com.architexa.org.eclipse.draw2d.ColorConstants;
import com.architexa.org.eclipse.draw2d.FigureListener;
import com.architexa.org.eclipse.draw2d.IFigure;
import com.architexa.org.eclipse.draw2d.Label;
import com.architexa.org.eclipse.draw2d.PositionConstants;
import com.architexa.org.eclipse.draw2d.Triangle2;
import com.architexa.org.eclipse.draw2d.geometry.Dimension;
import com.architexa.org.eclipse.draw2d.geometry.Point;
import com.architexa.org.eclipse.draw2d.geometry.PrecisionRectangle;
import com.architexa.org.eclipse.draw2d.geometry.Rectangle;
import com.architexa.org.eclipse.gef.DragTracker;
import com.architexa.org.eclipse.gef.EditPart;
import com.architexa.org.eclipse.gef.EditPartViewer;
import com.architexa.org.eclipse.gef.EditPolicy;
import com.architexa.org.eclipse.gef.GraphicalEditPart;
import com.architexa.org.eclipse.gef.Handle;
import com.architexa.org.eclipse.gef.LayerConstants;
import com.architexa.org.eclipse.gef.SharedCursors;
import com.architexa.org.eclipse.gef.commands.Command;
import com.architexa.org.eclipse.gef.commands.CompoundCommand;
import com.architexa.org.eclipse.gef.editpolicies.ComponentEditPolicy;
import com.architexa.org.eclipse.gef.editpolicies.NonResizableEditPolicy;
import com.architexa.org.eclipse.gef.requests.ChangeBoundsRequest;
import com.architexa.org.eclipse.gef.requests.GroupRequest;
import com.architexa.org.eclipse.gef.tools.SelectEditPartTracker;
import com.architexa.store.ReloRdfRepository;
import com.architexa.store.StoreUtil;


/**
 * 
 * @author Elizabeth L. Murnane
 *
 */
public class SeqSelectionEditPolicy extends NonResizableEditPolicy {
	private static final Logger logger = SeqPlugin.getLogger(SeqSelectionEditPolicy.class);
	
	private static String interfaceAnnotation="[Interface]";
	private static String abstractAnnotation="[Abstract]";
	IFigure moreMethodsButton;
	MenuButton showCallsMadeListButton;
	MenuButton showCalledByListButton;
	MenuButton overridesButton;
	MenuButton overridenByButton;
	MenuButton contextMenuButton;
	Button deleteButton;
	Button ungroupButton;

	protected static Comparator<IAction> alphabeticalComparator = new Comparator<IAction>() {
		public int compare(IAction action1, IAction action2) {
			return action1.getText().toLowerCase().compareTo(action2.getText().toLowerCase());
		}
	};

	@Override
	public void setSelectedState(int value){
		super.setSelectedState(value);

		if(getHost().getModel() instanceof MethodBoxModel) {
			MemberUtil.showFullOrAbbrevConnLabel(value, (MethodBoxModel)getHost().getModel());
		}
	}

	@Override
	protected void addSelectionHandles() {
		EditPart hostEP = getHost();
		
		// Check for invocation
		if (hostEP instanceof MethodBoxEditPart && 
				//(((NodeModel) hostEP.getModel()).isUserCreated()) &&
				((MethodBoxModel)hostEP.getModel()).getType() == MethodBoxModel.access)
			return;
		
		if (hostEP instanceof FieldEditPart) return;

		super.addSelectionHandles();

		// For classes:
		// <--> (Ungroup) | ... (Context menu) | X (Delete) as right side nav aids, 
		// Members as button beneath
		if (hostEP instanceof InstanceEditPart) {

			if (hostEP instanceof GroupedInstanceEditPart)
				addUngroupButton();
			else if (hostEP instanceof UserCreatedInstanceEditPart) {
				// Will need later
			} else
				addMoreMethodsButton();
		}

		// Nav aids or methods:
		// ^ (Overrides) on top, ^ (Overriders) on bottom, -> (Callers) on left,
		// and on right: -> (Calls) | ... (Context Menu) | X (Delete)
		if (hostEP instanceof MethodBoxEditPart && !(((NodeModel) hostEP.getModel()).isUserCreated())) {
			MethodBoxModel methodModel = (MethodBoxModel) hostEP.getModel();
			DiagramModel diagramModel = (DiagramModel) getHost().getViewer().getContents().getModel();

			// If the method makes some call(s) and at least
			// one call or a contained indirect call 
			// isn't in the diagram, add the expand button 
			if (MethodUtil.makesACall(methodModel) && 
					(makesCallNotInDiagram(methodModel) || containsIndirectAnonCallNotInDiagram(methodModel, diagramModel))) 
				addExpandButton();

			if (MethodUtil.isInvoked(methodModel) && methodModel.hasInvocationsOf()) addCalledByButton();

			if(MethodUtil.isOverrider(methodModel) &&
					methodModel.getOverridesConnection()==null) addOverridesButton();

			if(MethodUtil.getNumOverriders(methodModel) > methodModel.getOverriderConnections().size())
				addOverriddenByButton();
		}
		addDeletebutton();
		addContextMenuButton();
	}

	@Override
	public void removeSelectionHandles() {
		if(getHost().getParent()==null) return;
		if(getHost() instanceof MethodBoxEditPart && !(getHost() instanceof UserCreatedMethodBoxEditPart) &&
				((MethodBoxModel)getHost().getModel()).getType()==MethodBoxModel.access)
			return;

		super.removeSelectionHandles();

		IFigure layer = getHandleLayer();

		if (layer.getChildren().contains(contextMenuButton))
			layer.remove(contextMenuButton);

		if (layer.getChildren().contains(deleteButton))
			layer.remove(deleteButton);

		if (getHost() instanceof GroupedInstanceEditPart && layer.getChildren().contains(ungroupButton))
			layer.remove(ungroupButton);

		if (getHost() instanceof InstanceEditPart && moreMethodsButton != null && layer.getChildren().contains(moreMethodsButton)) {
			layer.remove(moreMethodsButton);
		} else if (getHost() instanceof MethodBoxEditPart) {
			if (showCallsMadeListButton != null) {
				if(layer.getChildren().contains(showCallsMadeListButton)) layer.remove(showCallsMadeListButton);
				showCallsMadeListButton = null;
			}
			if (showCalledByListButton != null) {
				if (layer.getChildren().contains(showCalledByListButton)) layer.remove(showCalledByListButton);
				showCalledByListButton = null;
			}
			if (overridesButton != null) {
				if (layer.getChildren().contains(overridesButton)) layer.remove(overridesButton);
			}
			if (overridenByButton != null) {
				if (layer.getChildren().contains(overridenByButton)) layer.remove(overridenByButton);
				overridenByButton = null;
			}
		}
	}

	private  IFigure getHandleLayer() {
		return getLayer(LayerConstants.HANDLE_LAYER);
	}

	@Override
	protected List<Object> createSelectionHandles() {
		List<Object> list = new ArrayList<Object>();
		if (isDragAllowed())
			addHandles((GraphicalEditPart)getHost(), list);
		else
			addHandles((GraphicalEditPart)getHost(), list, 
					new SelectEditPartTracker(getHost()), SharedCursors.ARROW);
		return list;
	}

	public static IFigure getReferenceFigure(GraphicalEditPart part) {
		if(part instanceof InstanceEditPart) {
			InstanceFigure instanceFig = (InstanceFigure) ((InstanceEditPart)part).getFigure();
			return instanceFig.getInstanceBox();
		} 
		if(part instanceof MethodBoxEditPart && 
				((MethodBoxFigure)part.getFigure()).getMethodBox().getBorder() instanceof LeftRightBorder 
				&& ((part.getModel() instanceof MethodBoxModel) || ((NodeModel) part.getModel()).isUserCreated())) {
			return ((MethodBoxFigure)part.getFigure()).getMethodBoxWithoutBorder();
		}
		return part.getFigure();
	}

	// Returns true if method makes at least one invocation that is not yet in the diagram
	private static boolean makesCallNotInDiagram(MethodBoxModel method){
		if(method.getType()!=MethodBoxModel.declaration || method.getMethod()==null)
			return false;

		// An anonymous class definition implicitly calls the superclass constructor
		// (Test size of children list to make sure this call isn't already in diagram)
		if(method.getMethod() instanceof AnonymousClassConstructor && 
				method.getChildren().size()==0) return true;

		if(method.getASTNode()==null) return false;

		if(!method.isInvocationListInitialized()){
			// the method has been selected first time
			MethodInvocationFinder invocationFinder = new MethodInvocationFinder(method.getASTNode());
			List<Invocation> invoc=invocationFinder.getAllInvocations();
			for(Invocation invocation : invoc) {
				if(invocation.getMethodElement()==null) continue;
				ASTNode parent = invocation.getInvocation().getParent();
				while(parent!=null 
						&& !(parent instanceof MethodDeclaration)
						&& !(parent instanceof Initializer)) {
					parent = parent.getParent();
				}
				if(parent==null) continue;

				// Can't simply do "return parent.equals(getASTNode())" because
				// if for some reason getASTNode() can't return a node for this
				// method and instead simply returns this method's enclosing
				// node, parent and getASTNode() won't be equal even when parent
				// does correspond to this method

				if(parent instanceof Initializer && parent.equals(method.getASTNode())) return true;
				if(!(parent instanceof MethodDeclaration)) continue;

				// For the case where getASTNode() returns the node enclosing this node
				// because it can't find the exact node for this method, we need to test
				// whether parent corresponds to this method based on the IJavaElements
				IJavaElement containingDecl = ((MethodDeclaration)parent).resolveBinding().getJavaElement();
				if(!method.getMember().equals(containingDecl)) continue;

				return true;
			}
		} else {
			// look at the method invocations made by this method declaration and
			// return true as soon as find one that is not already in the diagram
			List<Invocation> allCallsMade = new ArrayList<Invocation>(method.getCallsMade(null));
			for(Invocation invoc : new ArrayList<Invocation>(allCallsMade)) {
				if(!isInDiagram(method, invoc)) return true;
			}
		}

		// couldn't find any call not already in diagram
		return false;
	}
	private static boolean isInDiagram(MethodBoxModel methodDecl, Invocation invoc) {
		for(ArtifactFragment child : methodDecl.getChildren()) {

			if(child instanceof MethodBoxModel &&
					invoc.getStartPosition()==((MethodBoxModel)child).getCharStart() &&
					invoc.getEndPosition()==((MethodBoxModel)child).getCharEnd())
				return true;

			if(child instanceof HiddenNodeModel) {
				for(MemberModel hiddenChild : ((HiddenNodeModel)child).getControlFlowMethodsHiding()) {
					if(invoc.getStartPosition()==hiddenChild.getCharStart() && 
							invoc.getEndPosition()==hiddenChild.getCharEnd())
						return true;
				}
			}
		}
		return false; // couldn't find any already existent child that matches callLoc
	}

	// Returns true if method creates at least one anonymous class that
	// makes at least one invocation that is not yet in the diagram
	private boolean containsIndirectAnonCallNotInDiagram(MethodBoxModel method, DiagramModel diagram) {
		for(Invocation invocation : new ArrayList<Invocation>(method.getCallsMade(null))) {
			Map<AnonymousClassDeclaration, List<Invocation>> anonCallMap = 
				invocation.getInvocationsInAnonClasses();
			for(AnonymousClassDeclaration anonClass : anonCallMap.keySet()) {
				IJavaElement anonClassElmt = anonClass.resolveBinding().getJavaElement();
				for(Invocation anonInvoc : anonCallMap.get(anonClass)) {
					if(!isIndirectAnonCallInDiagram(anonInvoc, anonClassElmt, diagram))
						return true;
				}
			}
		}
		return false;  // couldn't find any already indirect anon calls not yet in diagram
	}
	private boolean isIndirectAnonCallInDiagram(Invocation invocation, 
			IJavaElement anonClassElmt, DiagramModel diagram) {
		ASTNode methodDecl = invocation.getInvocation().getParent();
		while(methodDecl!=null && !(methodDecl instanceof MethodDeclaration))
			methodDecl = methodDecl.getParent();
		if(!(methodDecl instanceof MethodDeclaration)) 
			return true; // unexpected, so don't count it as a call not yet in diagram

		return isInvocationAlreadyInDiagram(invocation, 
				(IMethod)((MethodDeclaration)methodDecl).resolveBinding().getJavaElement(), 
				anonClassElmt, diagram);
	}

	private void addMoreMethodsButton() {
		moreMethodsButton = ((InstanceEditPart)getHost()).getMoreMethodsButton();
		getHandleLayer().add(moreMethodsButton);

		Point bottomRight = findBottomRightBoundsForButton(moreMethodsButton).getBottomRight();
		Point forRightAlignment = new Point(bottomRight.x - moreMethodsButton.getPreferredSize().width + 1, bottomRight.y);
//		If a instance is selected before the layout has been properly rendered it produces a incorrect more method box
		Rectangle buttonBounds;
		if (moreMethodsButton.getBounds().width !=  ((InstanceFigure)((InstanceEditPart)getHost()).getFigure()).getInstanceBox().getBounds().width)
			buttonBounds = new Rectangle(forRightAlignment, new Dimension(((InstanceFigure)((InstanceEditPart)getHost()).getFigure()).getInstanceBox().getBounds().width, moreMethodsButton.getPreferredSize().height));
		else
			buttonBounds = new Rectangle(forRightAlignment, moreMethodsButton.getPreferredSize());
		moreMethodsButton.setBounds(buttonBounds);
		((InstanceFigure)getHostFigure()).getInstanceBox().addFigureListener(new FigureListener() {
			public void figureMoved(IFigure source) {
				if(moreMethodsButton==null || 
						!getHandleLayer().getChildren().contains(moreMethodsButton)) 
					return;

				Point bottomRight = findBottomRightBoundsForButton(moreMethodsButton).getBottomRight();
				Point forRightAlignment = new Point(bottomRight.x - moreMethodsButton.getPreferredSize().width + 1, bottomRight.y);
				Rectangle buttonBounds;
				if (moreMethodsButton.getBounds().width !=  ((InstanceFigure)((InstanceEditPart)getHost()).getFigure()).getInstanceBox().getBounds().width)
					buttonBounds = new Rectangle(forRightAlignment, new Dimension(((InstanceFigure)((InstanceEditPart)getHost()).getFigure()).getInstanceBox().getBounds().width, moreMethodsButton.getPreferredSize().height));
				else
					buttonBounds = new Rectangle(forRightAlignment, moreMethodsButton.getPreferredSize());
				moreMethodsButton.setBounds(buttonBounds);
			}
		});
	}

	private void addUngroupButton() {
		ungroupButton = createUngroupButton(getHost());
		getHandleLayer().add(ungroupButton);

		Point topRight = findTopRightBoundsForButton(ungroupButton).getTopRight();
		ungroupButton.setBounds(new Rectangle(new Point(topRight.x+1, topRight.y), ungroupButton.getPreferredSize()));

		IFigure fig = getHostFigure();
		if (fig instanceof InstanceFigure)
			fig = ((InstanceFigure) fig).getInstanceBox();

		fig.addFigureListener(new FigureListener() {
			public void figureMoved(IFigure source) {
				if (ungroupButton == null) return;
				Point topRight = findTopRightBoundsForButton(ungroupButton).getTopRight();
				ungroupButton.setBounds(new Rectangle(new Point(topRight.x + 1, topRight.y), ungroupButton.getPreferredSize()));
			}
		});
	}

	// Nav aid whose menu contains all the actions that are found in the selected item's 
	// context menu. Will make it easier for a user to realize and find the capabilities 
	// available to him and the actions that can be performed on a diagram item. 
	private void addContextMenuButton() {
		contextMenuButton = RSEContextMenuProvider.getContextMenuNavAid(getHost().getViewer());
		getHandleLayer().add(contextMenuButton);

		// Place it to the right of all other nav aids
		int translateWidth = 0;
		if(ungroupButton!=null) translateWidth+=ungroupButton.getBounds().width;
		if(showCallsMadeListButton!=null) translateWidth = showCallsMadeListButton.getBounds().width;
		if(deleteButton!=null) translateWidth+=deleteButton.getBounds().width;
		Point topRight = findTopRightBoundsForButton(contextMenuButton).getTopRight().getTranslated(translateWidth, 0);
		contextMenuButton.setBounds(new Rectangle(new Point(topRight.x+1, topRight.y), contextMenuButton.getPreferredSize()));
		IFigure fig = getHostFigure();
		if (fig instanceof InstanceFigure)
			fig = ((InstanceFigure) fig).getInstanceBox(); // for instances listen to the label container instead of the whole container.

		fig.addFigureListener(new FigureListener() {
			public void figureMoved(IFigure source) {
				if (contextMenuButton == null) return;

				int translateWidth = 0;
				if(ungroupButton!=null) translateWidth+=ungroupButton.getBounds().width;
				if(showCallsMadeListButton!=null) translateWidth = showCallsMadeListButton.getBounds().width;
				if(deleteButton!=null) translateWidth+=deleteButton.getBounds().width;
				Point topRight = findTopRightBoundsForButton(contextMenuButton).getTopRight().getTranslated(translateWidth, 0);
				contextMenuButton.setBounds(new Rectangle(new Point(topRight.x+1, topRight.y), contextMenuButton.getPreferredSize()));
			}
		});
	}

	private void addDeletebutton() {
		deleteButton = createDeleteButton(getHost());
		getHandleLayer().add(deleteButton);

		// Place it to the right of all other nav aids except the
		// context-menu button (context-menu button is rightmost)
		int translateWidth = 0;
		if(ungroupButton!=null) translateWidth+=ungroupButton.getBounds().width;
		if(showCallsMadeListButton!=null) translateWidth = showCallsMadeListButton.getBounds().width;
		Point topRight = findTopRightBoundsForButton(deleteButton).getTopRight().getTranslated(translateWidth, 0);
		deleteButton.setBounds(new Rectangle(new Point(topRight.x+1, topRight.y), deleteButton.getPreferredSize()));
		IFigure fig = getHostFigure();
		if (fig instanceof InstanceFigure)
			fig = ((InstanceFigure) fig).getInstanceBox(); // for instances listen to the label container instead of the whole container.

		fig.addFigureListener(new FigureListener() {
			public void figureMoved(IFigure source) {
				if (deleteButton == null) return;

				int translateWidth = 0;
				if(ungroupButton!=null) translateWidth+=ungroupButton.getBounds().width;
				if(showCallsMadeListButton!=null) translateWidth = showCallsMadeListButton.getBounds().width;
				Point topRight = findTopRightBoundsForButton(deleteButton).getTopRight().getTranslated(translateWidth, 0);
				deleteButton.setBounds(new Rectangle(new Point(topRight.x+1, topRight.y), deleteButton.getPreferredSize()));
			}
		});
	}


	private Button createUngroupButton(EditPart host) {
		Button button = new Button(ImageCache.ungroup);
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				DiagramEditPart diagramEP = (DiagramEditPart) getHost().getParent();
				DiagramModel diagram = (DiagramModel) diagramEP.getModel();
				CompoundCommand command = new CompoundCommand("UnGroup");
				GroupedUtil.ungroup(diagram, (GroupedInstanceModel) getHost().getModel(), command);
				diagramEP.execute(command);
			}
		});

		button.setBackgroundColor(ColorConstants.listBackground);
		button.setToolTip(new Label(" ungroup "));
		return button;
	}

	private Button createDeleteButton(EditPart host) {
		Button button = new Button(ImageCache.remove);
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				EditPolicy ePol = getHost().getEditPolicy(EditPolicy.COMPONENT_ROLE);
				if (ePol instanceof ComponentEditPolicy) {
					GroupRequest req = new GroupRequest();
					req.setEditParts(getHost());
					Command command = null;
					if (ePol instanceof SeqComponentEditPolicy)
						command = ((SeqComponentEditPolicy) ePol).createDeleteCommand(req);
					else if (ePol instanceof CommentComponentEditPolicy) // delete comments
						command = ((CommentComponentEditPolicy) ePol).createDeleteCommand(req);
					getHost().getViewer().getEditDomain().getCommandStack().execute(command);
				}
			}
		});

		button.setBackgroundColor(ColorConstants.listBackground);
		button.setToolTip(new Label(" delete "));
		return button;
	}

	private void addExpandButton() {
		showCallsMadeListButton = createExpandButton((MethodBoxEditPart)getHost());
		getHandleLayer().add(showCallsMadeListButton);

		Point topRight = findTopRightBoundsForButton(showCallsMadeListButton).getTopRight();
		showCallsMadeListButton.setBounds(new Rectangle(new Point(topRight.x+1, topRight.y), showCallsMadeListButton.getPreferredSize()));
		getHostFigure().addFigureListener(new FigureListener() {
			public void figureMoved(IFigure source) {
				if(showCallsMadeListButton==null) return;

				Point topRight = findTopRightBoundsForButton(showCallsMadeListButton).getTopRight();
				showCallsMadeListButton.setBounds(new Rectangle(new Point(topRight.x+1, topRight.y), showCallsMadeListButton.getPreferredSize()));
			}
		});
	}

	List<Invocation> allInvocations;
	protected Invocation getInvocation(MethodInvocation methInvoc) {
		if (allInvocations == null) {
			allInvocations = new ArrayList<Invocation>();
			MethodInvocationFinder invocFinder = new MethodInvocationFinder(((MethodBoxModel) getHost().getModel()).getASTNode(), true);
			allInvocations = invocFinder.getAllInvocations();
		}

		for (Invocation invoc : allInvocations) {
			if (invoc.getInvocation().equals(methInvoc))
				return invoc;
		}

		return null;
	}

	private MenuButton createExpandButton(final MethodBoxEditPart methodEP) {
		final MethodBoxModel methodModel = (MethodBoxModel) methodEP.getModel();
		IFigure arrow = AbstractReloRelationPart.getArrow(PositionConstants.EAST, new Triangle2());
		MenuButton button = new MenuButton(arrow, getHost().getViewer()) {
			@Override
			public void buildMenu(IMenuManager menu) {
				buildInformationControlMenu(methodEP, methodModel, this);
			}
		};
		button.setSize(new Dimension(22, 15));
		button.setToolTip(new Label("Show list of methods invoked by " + methodModel.getName()));

		return button;
	}

	private void buildInformationControlMenu(final MethodBoxEditPart methodEP, 
			final MethodBoxModel methodModel, final MenuButton button) {

		// Get the calls this method makes, passing a job that will
		// handle showing the calls in an information control menu
		methodModel.getCallsMade(new UIJob("Opening menu of calls ") {
			private RSEOutlineInformationControl popup;

			@Override
			public IStatus runInUIThread(IProgressMonitor monitor) {
				// Find calls made directly by the selected method, which will appear
				// at level 0 in the menu tree, and also populate the subtree mapping
				// from declared anonymous classes to the calls they make, which will
				// appear at level 1+ in the menu tree
				Map<Object, List<MultiAddCommandAction>> subTreeMap = new HashMap<Object, List<MultiAddCommandAction>>();
				List<Object> hasOverridenSubTree = new ArrayList<Object>();
				List<MultiAddCommandAction> callActions = getCallsMenuActions((MethodBoxModel)methodEP.getModel(), subTreeMap, hasOverridenSubTree);

				// Give menu a title label of "Calls from foo():"
				// unless no calls will appear in the menu, in which case make 
				// the title label say that so that the menu isn't just empty
				String labelNoReturnType = getMethodNameNoReturnType(methodModel);
				String titleText = (callActions.size()!=0) ? 
						"Calls from "+labelNoReturnType+":" 
						: "Sorry, no more calls from "+labelNoReturnType;

				// All the calls will be of kind "Method", so don't want filters for member kind
				// because seeing check boxes for Class or Field can be confusing here since some 
				// calls do involve anonymous classes or field accesses
				String[] filters = new String[] {RSEOutlineInformationControl.FILTERS_ACCESS_LEVEL, 
						RSEOutlineInformationControl.FILTERS_LIBRARY_CODE};

				if (popup!=null && popup.active) 
					return Status.OK_STATUS;
				popup = new RSEOutlineInformationControl(StoreUtil.getDefaultStoreRepository(), titleText, subTreeMap, hasOverridenSubTree, filters);
				popup.setInput(callActions);
				
				// Get the actions for the buttons that add multiple calls at once
				List<IAction> addButtonsActions = getAddButtonsActions(
						callActions, methodEP, methodModel, popup);
				popup.setButtonInput(addButtonsActions);

				// menu should open along the right hand side of the nav aid button
				EditPartViewer parentViewer = getHost().getViewer();
				Control parent = parentViewer.getControl();
				Rectangle figBounds = button.getBounds().getCopy();
				button.translateToAbsolute(figBounds);
				org.eclipse.swt.graphics.Point menuLocation = parent.toDisplay(figBounds.getTopRight().x+1, figBounds.getTopRight().y-1);
				popup.setLocation(menuLocation);

				// make sure menu is proper size to show all components but not stretch too wide
				popup.pack();
				popup.setInitSize();

				popup.open();

				return Status.OK_STATUS;
			}
		});
	}

	private List<MultiAddCommandAction> getCallsMenuActions(final MethodBoxModel methodModel, 
			Map<Object, List<MultiAddCommandAction>> subTreeMap, List<Object> hasOverridenSubTree) {

		ReloRdfRepository repo = StoreUtil.getDefaultStoreRepository();

		// the actions for the calls made directly by methodModel
		List<MultiAddCommandAction> actions = new ArrayList<MultiAddCommandAction>();
		// the actions for the calls made within anonymous classes declared
		// by the invocation will be added to subTreeMap
		
		// the actions corresponding to calls already in the diagram
		List<MultiAddCommandAction> callsAlreadyInDiagram = new ArrayList<MultiAddCommandAction>();

		List<Invocation> callsMade = methodModel.getCallsMade(null);
		for(final Invocation invocation : callsMade) {
			try {
				String callName = getCallActionLabel(invocation);
				MultiAddCommandAction action = makeActionForInvoc(methodModel, invocation, 
						invocation.getMethodElement(), callName, null, callsAlreadyInDiagram);
				if(action!=null) {
					actions.add(action);
					makeSubMenuActions(methodModel, invocation, action, 
							subTreeMap, hasOverridenSubTree, callsAlreadyInDiagram, repo);
				}
			} catch(Exception e) {
				logger.error("Unexpected exception encountered while creating " +
						"menu action for invocation " + invocation);
			}
		}

		// Remove any invocations that are already in the diagram and 
		// that contain no submenu invocations not yet in the diagram
		List<Object> parentNodeSetCopy = new ArrayList<Object>(subTreeMap.keySet());
		for(Object parentNode : parentNodeSetCopy) {
			List<MultiAddCommandAction> subMenuActionsCopy = 
				new ArrayList<MultiAddCommandAction>(subTreeMap.get(parentNode));
			for(MultiAddCommandAction subMenuAction : subMenuActionsCopy) {
				if(includeInMenu(subMenuAction, subTreeMap, callsAlreadyInDiagram)) continue;
				subTreeMap.get(parentNode).remove(subMenuAction);
			}
		}
		List<MultiAddCommandAction> actionsCopy = new ArrayList<MultiAddCommandAction>(actions);
		for(MultiAddCommandAction invocationAction : actionsCopy) {
			if(includeInMenu(invocationAction, subTreeMap, callsAlreadyInDiagram)) continue;
			actions.remove(invocationAction);
		}

		return actions;
	}

	/*
	 * Makes an action with the given label that when selected will add a call 
	 * representing the given invocation from a new invocation model created 
	 * inside invokingMethod to a declaration model of invokedMethod. Any
	 * commands that should run before this call is created should be passed in
	 * initCommands (for example, for indirect anon invocations, commands to create the
	 * anon class model and the model of the method declaration making the indirect call)
	 * 
	 */
	private MultiAddCommandAction makeActionForInvoc(final MethodBoxModel invokingMethod, 
			final Invocation invocation, IMethod invokedMethod, String actionLabel, 
			final List<Command> initCommands, List<MultiAddCommandAction> callsAlreadyInDiagram) {

		IJavaElement invokedMethodParent = invokedMethod.getParent();
		final Resource invokedMethodParentRes = RJCore.jdtElementToResource(StoreUtil.getDefaultStoreRepository(), invokedMethodParent);

		boolean isLibraryCall = false; 
		if (invokedMethodParentRes == null || invokedMethodParent.getResource() == null || invokedMethodParent instanceof BinaryType){
			isLibraryCall = isLibraryCall(invocation);

			// Only show lib code in nav aid if user has
			// set preference to show it in menus or diagram
			if (isLibraryCall && LibraryPreferences.isChronoLibCodeHidden()) return null;

			if (isLibraryCall)
				actionLabel = actionLabel+ArtifactFragment.libraryAnnotation;
		}

		//check for inherited methods
		ReloRdfRepository repo = StoreUtil.getDefaultStoreRepository();
		Artifact parentArt = invokingMethod.getArt().queryParentArtifact(repo);

		// Check for Spring Injection
		boolean classOfInstanceIsBean = false;
		Resource classOfInstance = MethodUtil.findInjectedBeanClass(invocation, invokingMethod.getMember());
		if (classOfInstance == null)
			classOfInstance = MethodUtil.getClassOfInstanceCalledOn(invocation, invokingMethod.getInstanceModel());
		else
			classOfInstanceIsBean = true;

		Resource parentRes = parentArt.elementRes;
		if (classOfInstance == null) {
			classOfInstance = parentRes;
		}

		final Resource finalClassOfInstance = classOfInstance;
		final boolean finalClassOfInstanceIsBean = classOfInstanceIsBean;

		if (invokedMethodParentRes == null || classOfInstance == null) return null;

		final MethodBoxModel tempCalledModel = new MethodBoxModel(null, invokedMethod, MethodBoxModel.declaration);
		MultiAddCommandAction action = new MultiAddCommandAction(actionLabel, null) {
			@Override
			public Command getCommand(Map<Artifact, ArtifactFragment> addedArtToAFMap) { 
				CompoundCommand tgtCmd = new CompoundCommand();
				tgtCmd.add(makeCommand());
				return tgtCmd;
			}
			@Override
			// Need to override run() because null was passed as this action's brc since
			// DiagramEditPart is not a BasicRootController (TODO DiagramEditPart implements BasicRootController)
			public void run() {
				getHost().getViewer().deselectAll();
				((DiagramEditPart)getHost().getViewer().getContents()).execute(getCommand(new HashMap<Artifact, ArtifactFragment>()));
			}
			
			@Override
			public Artifact getInvokedModelArtifact () {
				return tempCalledModel.getArt();
			}
			
			// TODO: use commented lines below to implement per method conditionals?
			private Command makeCommand() {
//				BreakableCommand addMultipleCallsCmd = new BreakableCommand("displaying all calls made", AnimateCallMadeCommand.class);
				AnimateCallMadeCommand methodCallCmd = new AnimateCallMadeCommand(tempCalledModel.getArt());
//				addMultipleCallsCmd.add(methodCallCmd);
				if(initCommands!=null) {
					for(Command initCommand : initCommands) methodCallCmd.add(initCommand);
				}

				DiagramEditPart diagramEP = (DiagramEditPart)getHost().getViewer().getContents();
				MethodUtil.createModelsForMethodRes(invocation, invokingMethod, (DiagramModel)diagramEP.getModel(), finalClassOfInstance, null, false, methodCallCmd, null, finalClassOfInstanceIsBean);
				methodCallCmd.setMethodCall();

//				((MethodBoxEditPart) getHost()).displayAllCallsMade(addMultipleCallsCmd);
//				
//				try
//				{
//					final IRunnableWithProgress op=new IRunnableWithProgress(){
//						public void run(final IProgressMonitor monitor)	throws InvocationTargetException, InterruptedException {
//							monitor.beginTask("Displaying all calls made...", IProgressMonitor.UNKNOWN);
//							Display.getDefault().asyncExec(new Runnable() {
//								public void run() {
//									MethodUtil.displayCallsMadeByMethodDeclToInstance((DiagramModel)diagramEP.getModel(), decl, null, (DiagramEditPart)getViewer().getContents(), addMultipleCallsCmd, monitor);
//								}
//							});
//						}
//					};
//					new ProgressMonitorDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell()).run(true, false, op);
//				}catch (InvocationTargetException e) {
//					logger.error("Unexpected exception while displaying all calls made by method " + model, e);
//				}catch (InterruptedException e) {
//					logger.error("Unexpected exception while displaying all calls made by method " + model, e);
//				}	
//				
				return methodCallCmd;
			}
		};
		ImageDescriptor calledMethodIcon = MethodUtil.getMethodIconDescriptor(tempCalledModel, false);
		action.setImageDescriptor(calledMethodIcon);

		// If it's not library code or if it is lib code but user
		// has set pref to show lib code in a diagram, enable it.
		boolean enable = !isLibraryCall || LibraryPreferences.isChronoLibCodeInDiagram();
		action.setEnabled(enable);

		// If invocation already in diagram, flag it for removal from menu
		flagInvocationIfAlreadyInDiagram(action, invocation, 
				invokingMethod.getMember(),
				invokingMethod.getInstanceModel().getInstanceElem(), 
				(DiagramModel)((DiagramEditPart)getHost().getViewer().getContents()).getModel(), 
				callsAlreadyInDiagram);

		return action;
	}

	private void makeSubMenuActions(MethodBoxModel invokingMethod, Invocation invocation, 
			final MultiAddCommandAction invocAction, 
			final Map<Object, List<MultiAddCommandAction>> subMenuTree, 
			List<Object> hasOverridenSubTree, List<MultiAddCommandAction> callsAlreadyInDiagram, ReloRdfRepository repo) {
		
		// For any anonymous class this invocation creates, find the 
		// invocations that anon class makes and add them as a 
		// submenu to the invocation's action in the menu tree
		Map<AnonymousClassDeclaration, List<Invocation>> anonSubMenuCallMap = 
			invocation.getInvocationsInAnonClasses();
		for(AnonymousClassDeclaration anonClass : anonSubMenuCallMap.keySet()) {
			makeAnonClassSubMenuActions(anonClass, anonSubMenuCallMap.get(anonClass), 
					invocAction, subMenuTree, callsAlreadyInDiagram, repo);
		}

		// For any call to an abstract method or a method in an interface, 
		// make a subtree of the methods that implement the abstract method
		makeInterfaceOrAbstractImplementingSubMenuActions(invokingMethod, invocation, 
				invocAction, subMenuTree, hasOverridenSubTree, repo);
	}

	/*
	 * anonClass is the anonymous class and its direct calls are in the 
	 * list anonClassInvocations. subTreeParent is the action corresponding 
	 * to the invocation that creates anonClass, and it will be the parent 
	 * node in the tree of the submenu actions created here.
	 * subMenuTree will map subTreeParent to the list of submenu actions 
	 * corresponding to calls that anonClass makes.
	 */
	private void makeAnonClassSubMenuActions(
			AnonymousClassDeclaration anonClass, List<Invocation> anonClassInvocations, 
			final MultiAddCommandAction subTreeParent, Map<Object, List<MultiAddCommandAction>> subMenuTree, 
			List<MultiAddCommandAction> callsAlreadyInDiagram, ReloRdfRepository repo) {

		// Determine anon class info
		DiagramModel diagram = (DiagramModel) getHost().getViewer().getContents().getModel();
		IJavaElement anonClassElmt = anonClass.resolveBinding().getJavaElement();
		Resource anonClassRes = RJCore.jdtElementToResource(repo, anonClassElmt);
		String instanceName = "";
		if((anonClass.getParent() instanceof ClassInstanceCreation) && 
				(anonClass.getParent().getParent() instanceof VariableDeclarationFragment)) {
			// case like Bar bar = new Bar() { baz() { ... 
			VariableDeclarationFragment variableDecl = (VariableDeclarationFragment) anonClass.getParent().getParent();
			instanceName = variableDecl.getName().getIdentifier();
		} else instanceName = null; // case like foo(new Runnable() { run() { ...
		String className = InstanceUtil.getClassName(anonClassRes, repo);

		List<MultiAddCommandAction> subMenuActions = new ArrayList<MultiAddCommandAction>();

		for(Invocation invocation : anonClassInvocations) {

			// Find or create a model for the anon class
			CompoundCommand anonClassCreateCmd = new CompoundCommand();
			final InstanceModel anonClassInstanceModel = InstanceUtil.findOrCreateContainerInstanceModel(instanceName, className, anonClassRes, diagram, -1, anonClassCreateCmd);

			// TODO When the Add All button is pushed, if the same anon class has 
			// multiple invocations, that anon class will be created multiple
			// times in the diagram. Want to eliminate these duplicates.

			// Determine the method declaration in the
			// anonymous class that makes this invocation
			ASTNode methodDecl = invocation.getInvocation().getParent();
			while(methodDecl!=null && !(methodDecl instanceof MethodDeclaration))
				methodDecl = methodDecl.getParent();
			if(!(methodDecl instanceof MethodDeclaration)) continue;
			// and find or create a model for it
			MethodDeclaration methodDeclNode = (MethodDeclaration) methodDecl;
			IMethod methodDeclElmt = (IMethod) methodDeclNode.resolveBinding().getJavaElement();
			Resource methodDeclRes = RJCore.jdtElementToResource(repo, methodDeclElmt);
			CompoundCommand invokingMethodCreateCmd = new CompoundCommand();
			final MethodBoxModel invokingMethod = MethodUtil.findOrCreateMethodModel(methodDeclElmt, methodDeclRes, anonClassInstanceModel, -1, invokingMethodCreateCmd);

			String callName = getCallActionLabel(invocation);
			// To make it easier to understand where an indirect anon call comes
			// from, want to include the invoking method in the menu label. 
			// "Runnable.run() -> foo(true)" looks bad, so doing
			// "foo(true) - from Runnable.run()", which is clear and also is similar 
			// to how Call Hierarchy view shows containment
			String anonClassSimpleName = className.substring(0, className.indexOf("(")).replace("new", "").trim();
			String invokingMethodName = MethodUtil.getMethodName(methodDeclNode);
			callName = callName+" - from "+anonClassSimpleName+"."+invokingMethodName;

			// If anon class and/or its method that makes this call are not already in 
			// the diagram, give the commands to add them to the MultiAddCommandAction 
			// to execute (unless already given to a previous MultiAddCommandAction)
			List<Command> initCommands = new ArrayList<Command>();
			if(!anonClassCreateCmd.isEmpty())
				initCommands.add(anonClassCreateCmd);
			if(!invokingMethodCreateCmd.isEmpty())
				initCommands.add(invokingMethodCreateCmd);

			MultiAddCommandAction action = makeActionForInvoc(invokingMethod, invocation, 
					invocation.getMethodElement(), callName, initCommands, callsAlreadyInDiagram);
			subMenuActions.add(action);

			// If invocation already in diagram, flag it for removal from menu
			flagInvocationIfAlreadyInDiagram(action, invocation, 
					methodDeclElmt, anonClassElmt, diagram, callsAlreadyInDiagram);

			// invocation may create another anonymous class, whose calls
			// should be placed in another subtree that is one level deeper
			// and has invocation's action as a parent
			makeSubMenuActions(invokingMethod, invocation, action, 
					subMenuTree, new ArrayList<Object>(), callsAlreadyInDiagram, repo);
		}

		// Done making actions for each invocation that goes in the subtree, so add to 
		// map: invocation creating anon class -> subtree actions of anon class's calls
		subMenuTree.put(subTreeParent, subMenuActions);
	}

	private void makeInterfaceOrAbstractImplementingSubMenuActions(
			final MethodBoxModel invokingMethod, Invocation invocation, 
			final MultiAddCommandAction invocAction, 
			Map<Object, List<MultiAddCommandAction>> subMenuTree, 
			List<Object> hasOverridenSubTree, ReloRdfRepository repo) {

		// Determine whether target of call is an abstract method 
		// in an abstract class or a method in an interface
		Resource targetClass = MethodUtil.getClassOfInstanceCalledOn(invocation, invokingMethod.getInstanceModel());
		String abstractOrInterfaceString = getAbstractLabelString(invocation.getMethodElement(), targetClass, repo);
		if(abstractOrInterfaceString==null) 
			return; // not in an interface and not an abstract method in an abstract class

		Artifact invokedArt = RSEOutlineInformationControl.getArtifact(invocAction);
		if(invokedArt==null || invokedArt.elementRes==null) return;

		// Find the implementers of the abstract method
		List<Resource> implementingMethods = new ArrayList<Resource>();
		StatementIterator implementersIter = repo.getStatements(null, RJCore.overrides, invokedArt.elementRes);
		while(implementersIter.hasNext()) {
			Value implementer = implementersIter.next().getSubject();
			if(implementer==null || !(implementer instanceof Resource)) continue;

			if(!implementingMethods.contains(implementer))
				implementingMethods.add((Resource)implementer);
		}

		List<MultiAddCommandAction> subMenuActions = new ArrayList<MultiAddCommandAction>();
		DiagramEditPart diagramEP = (DiagramEditPart)getHost().getViewer().getContents();
		final DiagramModel diagram = (DiagramModel) diagramEP.getModel();

		// When user selects one of the implementing calls from the submenu, add
		// the actual call made to the abstract class or interface that's in the
		// code, and then add the implementing method with an overrides connection
		for(final Resource implementingMethodRes : implementingMethods) {

			IJavaElement implementingMethodElmt = RJCore.resourceToJDTElement(repo, implementingMethodRes);
			if(!(implementingMethodElmt instanceof IMethod)) continue;
			final IMethod implementingMethod = (IMethod) implementingMethodElmt;
			final MethodBoxModel tempImplementingMethodModel = new MethodBoxModel(null, implementingMethod, MethodBoxModel.declaration);

			IJavaElement implementingClass = implementingMethod.getParent();
			final Resource implementingClassRes = RJCore.jdtElementToResource(repo, implementingClass);
			final String implementingClassName = InstanceUtil.getClassName(implementingClassRes, repo);

			String label = implementingClassName+"."+MethodUtil.getMethodName(implementingMethod, null, false);
			MultiAddCommandAction action = new MultiAddCommandAction(label, null) {
				@Override
				public Command getCommand(Map<Artifact, ArtifactFragment> addedArtToAFMap) { 
					CompoundCommand tgtCmd = new CompoundCommand();
					tgtCmd.add(makeCommand());
					return tgtCmd;
				}
				@Override
				// Need to override run() because null was passed as this action's brc since
				// DiagramEditPart is not a BasicRootController (TODO DiagramEditPart implements BasicRootController)
				public void run() {
					getHost().getViewer().deselectAll();
					((DiagramEditPart)getHost().getViewer().getContents()).execute(getCommand(new HashMap<Artifact, ArtifactFragment>()));
				}
				private Command makeCommand() {

					AnimateOverrideCommand overrideCommand = new AnimateOverrideCommand();

					// Add the actual call made to the abstract 
					// class or interface that's in the code 
					Command addCallInCode = invocAction.getCommand(new HashMap<Artifact, ArtifactFragment>());
					overrideCommand.add(addCallInCode);

					// then add the implementing method
					int invokingClassIndex = diagram.getChildren().indexOf(invokingMethod.getInstanceModel());
					InstanceModel implementingInstanceModel = InstanceUtil.findOrCreateContainerInstanceModel(
							null, implementingClassName, implementingClassRes, diagram, invokingClassIndex+1, overrideCommand);
					MethodBoxModel implementingMethodModel = MethodUtil.findOrCreateMethodModel(implementingMethod, 
							implementingMethodRes, implementingInstanceModel, -1, overrideCommand);
					overrideCommand.setInstance(implementingInstanceModel);

					// The overrider is the implementing method shown in the submenu
					// and the overridden method is the call in the code to the
					// abstract class or interface
					MethodBoxModel overrider = implementingMethodModel;
					overrideCommand.setNewMethod(implementingMethodModel);
					MethodBoxModel overridden = null;
					for(Object o : ((CompoundCommand)addCallInCode).getCommands()) {
						if(!(o instanceof AnimateCallMadeCommand)) continue;
						overridden = ((AnimateCallMadeCommand)o).getDeclaration();
					}
					overrideCommand.setOverrider(overrider);
					overrideCommand.setOverridden(overridden);

					// Add an overrides connection from the implementing
					// method to the method in the abstract class or interface
					AnimateOverrideConnectionCommand connAnimationCmd = overrideCommand.createAnimateOverrideConnectionCommand();
					connAnimationCmd.add(new ConnectionCreateCommand(overrider, overridden, "overrides", ConnectionModel.OVERRIDES));
					overrideCommand.add(connAnimationCmd);

					return overrideCommand;
				}
			};
			ImageDescriptor calledMethodIcon = MethodUtil.getMethodIconDescriptor(tempImplementingMethodModel, false);
			action.setImageDescriptor(calledMethodIcon);

			subMenuActions.add(action);
			hasOverridenSubTree.add(action);
		}
		subMenuTree.put(invocAction, subMenuActions);
		if(!subMenuActions.isEmpty()) {
			// If there is at least one implementing method, change the 
			// abstract method's label in the menu to indicate that
			invocAction.setText(invocAction.getText()+"  "+abstractOrInterfaceString);
		}
	}

	/**
	 * Returns a String "Interface" if the given class containing the given 
	 * method is an interface, a String "Abstract" if the given class and method
	 * are abstract, and null otherwise
	 */
	private String getAbstractLabelString(IMethod method, 
			Resource targetClass, ReloRdfRepository repo) {

		// All methods in an interface are implicitly abstract
		Statement interfaceStmt = repo.getStatement(targetClass, RJCore.isInterface, null);
		if(interfaceStmt!=null && RJCore.interfaceType.equals(interfaceStmt.getObject())) 
			return interfaceAnnotation;

		try {
			// Not abstract if compiler generated or an anon class constructor
			if(method instanceof CompilerGeneratedDefaultConstructor ||
					method instanceof AnonymousClassConstructor) return null;

			// JdtFlags.isAbstract(method) will also return true if method is in an 
			// interface, but the above repo query should have caught that case and
			// returned the "Interface" string, so isAbstract returning true should mean
			// the method has an abstract modifier (is abstract method in abstract class)
			if(method!=null && JdtFlags.isAbstract(method))
				return abstractAnnotation;
		} catch (JavaModelException e) {
			logger.error("Unexpected error when determining " +
					"whether method "+method+" is abstract", e);
		}

		return null;
	}

	// If invocation is already in the diagram and it has no submenu items, don't
	// want to include it in the menu. Can't remove it now, however, because those 
	// submenu items haven't been determined yet, so we add it to a list that can 
	// be accessed once subMenuTree is fully populated in order to remove this 
	// invocation if it has no submenu items
	private void flagInvocationIfAlreadyInDiagram(MultiAddCommandAction invocationAction,
			Invocation invocation, 
			IMethod invokingMethod, 
			IJavaElement classContainingInvokingMethod, 
			DiagramModel diagram, 
			List<MultiAddCommandAction> callsAlreadyInDiagram) {
		if(isInvocationAlreadyInDiagram(invocation, invokingMethod, classContainingInvokingMethod, diagram))
			callsAlreadyInDiagram.add(invocationAction);
	}

	// Check each class already in the diagram to see
	// whether it contains a method making this invocation,
	// and if so, return true, otherwise return false
	private boolean isInvocationAlreadyInDiagram(Invocation invocation, 
			IMethod invokingMethod, 
			IJavaElement classContainingInvokingMethod, 
			DiagramModel diagram) {

		for (ArtifactFragment frag : diagram.getChildren()) {
			if (!(frag instanceof InstanceModel)) continue;
			InstanceModel instanceInDiagram = (InstanceModel) frag;

			// Check that instanceInDiagram is the class that contains
			// the method making the invocation we're looking for
			if (instanceInDiagram.getInstanceElem() == null ||
					!instanceInDiagram.getInstanceElem().equals(classContainingInvokingMethod)) continue;

			for (MethodBoxModel methodInDiagram : instanceInDiagram.getMethodChildren()) {

				// An anon class constructor doesn't make any explicit
				// calls (it only makes one call - the implicit call
				// to the constructor of the class it implements)
				if (methodInDiagram.getMember() instanceof AnonymousClassConstructor) 
					continue; 

				// Check that methodInDiagram is the method 
				// that makes the invocation we're looking for
				if (!invokingMethod.equals(methodInDiagram.getMember())) 
					continue;

				// Check whether any of the invocations the method decl contains
				// in the diagram match the invocation we're looking for
				if(isInDiagram(methodInDiagram, invocation)) return true;
			}
		}
		return false;
	}
	
	// Invocations already in the diagram have been added to the callsAlreadyInDiagram.
	// list. If the given action is not in this list or has children not in this list (ie
	// not yet in the diagram), it should be included in the menu. Otherwise, it should 
	// not be in the menu because it and any submenu calls are already in the diagram.
	private boolean includeInMenu(MultiAddCommandAction callAction, 
			Map<Object, List<MultiAddCommandAction>> subTreeMap, 
			List<MultiAddCommandAction> callsAlreadyInDiagram) {

		// not in list of calls already in diagram, so include in menu
		if(!callsAlreadyInDiagram.contains(callAction)) return true;
		
		// in list of calls already in diagram and has no submenu items, so remove from menu
		if(!subTreeMap.containsKey(callAction)) return false; 

		// has submenu children, and if any of their calls are not yet in the diagram, 
		// we want them in the menu and therefore can't remove the parent (callAction)
		return hasChildrenToIncludeInMenu(callAction, subTreeMap, callsAlreadyInDiagram);
	}
	private boolean hasChildrenToIncludeInMenu(MultiAddCommandAction parentAction, 
			Map<Object, List<MultiAddCommandAction>> subTreeMap,
			List<MultiAddCommandAction> callsAlreadyInDiagram) {

		// If has no submenu children, remove from menu
		if(!subTreeMap.containsKey(parentAction)) return false;
		List<MultiAddCommandAction> subMenuItems = subTreeMap.get(parentAction);
		if(subMenuItems.size()==0) return false;

		for(MultiAddCommandAction subMenuItem : subMenuItems) {
			if(includeInMenu(subMenuItem, subTreeMap, callsAlreadyInDiagram)) 
				return true; // has at least one child not yet in diagram
		}
		return false;
	}

	private String getCallActionLabel(Invocation call) {

		String invocationString = Invocation.getStringRepresentationOfInvocation(((Invocation)call).getInvocation());
		// If invoking method is an anon class constructor, the invocation must be 
		// the implicit call to super class constructor (since that's the only call
		// an anon class constructor makes). Currently the label will just be "super()";
		// if want to make it the super class's name ie "Activator()", pass in an 
		// IMethod of the invoking method declaration and do the following
//		if (invokingMethod instanceof AnonymousClassConstructor) 
//			invocationString = ((AnonymousClassConstructor)invokingMethod).getName();
		IMethod invokedMethod = ((Invocation)call).getMethodElement();

		String callLabel = (invokedMethod instanceof AnonymousClassConstructor) ?
				((AnonymousClassConstructor)invokedMethod).getAnonClassNavAidString(invocationString) 
				: invocationString;
		
		// Make sure that string is only a single line so it doesn't appear
		// with unreadable characters (important if call contains an anon
		// class decl as an argument) and that it has no extra whitespace
		callLabel = removeLinesAndExtraSpaces(callLabel);

		return callLabel;
	}

	/*
	 * Returns a string that is a single line and has no tabs 
	 * or "extra" whitespace (more than a single character long)
	 */
	private String removeLinesAndExtraSpaces(String s) {

		// replace line terminators with a single space
		char singleSpace = ' ';
		s = s.replace('\n', singleSpace); // newline
		s = s.replace('\r', singleSpace); // carriage return
		s = s.replace('\u0085', singleSpace); // next line
		s = s.replace('\u2028', singleSpace); // line separator
		s = s.replace('\u2029', singleSpace); // paragraph separator

		s = s.replace("\t", " "); // replace tabs with a single space
		s = s.replaceAll("\\s+", " "); // replace extra whitespace with a single space

		return s;
	}

	public static String getMethodNameNoReturnType(MethodBoxModel methodModel) {
		String lbl = CodeUnit.getLabel(StoreUtil.getDefaultStoreRepository(),
				methodModel.getArt(), methodModel.getInstanceModel()).trim();
		// Fix string (for example CodeUnit.getLabel will 
		// include "##" to flag anon class constructors)
		lbl = MoreButtonUtils.fixAnonFinalVars(lbl);
		return lbl;
	}

	private List<IAction> getAddButtonsActions(List<MultiAddCommandAction> callActions,
			MethodBoxEditPart methodEP, MethodBoxModel methodModel, 
			RSEOutlineInformationControl navAidMenu) {
		List<IAction> addActions = new ArrayList<IAction>();

		// Option that will add all enabled and visible calls in the menu to the diagram
		IAction addAllAction = getAddAllItemsAction(navAidMenu, methodEP);

		// Option that will incrementally add all of the enabled and visible 
		// calls this method makes, every subsequent call those called 
		// methods make, every subsequent calls those methods make, etc.
		IAction calleeHierAction = getAddCalleeHierarchyAction(methodEP, methodModel, navAidMenu);

		addActions.add(addAllAction);
		
		addActions.add(calleeHierAction);

		// If there is not at least one enabled call in the list, continue showing
		// these buttons so the menu always looks consistent but disable the buttons
		boolean atLeastOneAddableItem = false;
		for(MultiAddCommandAction action : callActions) {
			if(action.isEnabled()) {
				atLeastOneAddableItem = true;
				break;
			}
		}
		if(!atLeastOneAddableItem) {
			for(IAction addAction : addActions) addAction.setEnabled(false);
		}
		
		return addActions;
	}

	private IAction getAddAllItemsAction(final RSEOutlineInformationControl navAidMenu, final MethodBoxEditPart methodEP) {
		final String actionText = "Add All";
		ImageDescriptor displayAllIcon = Activator.getImageDescriptor("icons/addAll_callees.png");
		return new Action(actionText, displayAllIcon) {
			@Override
			public void run() {
				CompoundCommand addAllCmd = getAddAllCmd(actionText, navAidMenu);
				runAddAll(addAllCmd, navAidMenu);
				CompoundCommand addConditionals = new CompoundCommand("Add Conditionals");
				methodEP.displayAllCallsMade(addConditionals);
				runAddAll(addConditionals, navAidMenu);
			}
		};
	}

	// Returns a compound command containing the commands of
	// visible and enabled (unfiltered and selectable) tree items
	private CompoundCommand getAddAllCmd(String addAllLabel, 
			RSEOutlineInformationControl navAidMenu) {
		InstanceUtil.instancesAdded = new ArrayList<InstanceModel>();
		CompoundCommand addAllCmd = new CompoundCommand(addAllLabel);
		addCommandsToAddAllCmd(navAidMenu.getTreeViewer().getTree().getItems(), addAllCmd);
		InstanceUtil.instancesAdded = null;
		return addAllCmd;
	}

	//tracks instances that are added during 'addAll' so that we do not get duplicate instances
	
	
	// Adds only the commands of visible and enabled tree items to the given
	// compound cmd. Also, doesn't add abstract/interface subtree implementer calls.
	private void addCommandsToAddAllCmd(final TreeItem[] menuItems, CompoundCommand addAllCmd) {
		for(TreeItem visibleItem : menuItems) {
			Object data = visibleItem.getData();
			if(data instanceof MultiAddCommandAction) {
				MultiAddCommandAction maca = (MultiAddCommandAction) data;
				if(maca.isEnabled())
					addAllCmd.add(maca.getCommand(new HashMap<Artifact, ArtifactFragment>()));

				// Only add the abstract/interface call that's in the code when
				// press Add All, not each implementer since that would result
				// in multiple similar calls cluttering the diagram, all of which 
				// are not the actual call made at runtime except one anyway
				boolean isAbstractMethodCall = 
					maca.getText().contains(interfaceAnnotation) || 
					maca.getText().contains(abstractAnnotation);
				// Also, only add expanded subtrees; if the user has collapsed a
				// subtree of calls, we assume they are not of interest and don't add
				boolean isExpanded = 
					visibleItem.getItemCount()==0 || // consider expanded b/c not a parent node
					visibleItem.getExpanded(); // is a subtree parent, so test if expanded
				if(!isAbstractMethodCall && isExpanded) {
					addCommandsToAddAllCmd(visibleItem.getItems(), addAllCmd);
				}
			}
		}
	}

	private void runAddAll(CompoundCommand addAllCmd, RSEOutlineInformationControl navAidMenu) {
		if(!addAllCmd.isEmpty()) {
			// close the menu, add the calls, and update the selected method's 
			// border to look right after the addition of the new children
			navAidMenu.dispose();
			getHost().getViewer().deselectAll();
			((DiagramEditPart)getHost().getViewer().getContents()).execute(addAllCmd);
			((MethodBoxFigure)((MethodBoxEditPart)getHost()).getFigure()).createOrUpdateMethodBoxWithoutBorder();
		}
	}

	private IAction getAddCalleeHierarchyAction(MethodBoxEditPart methodEP, 
			MethodBoxModel methodModel, final RSEOutlineInformationControl navAidMenu) {

		String methodName = getMethodNameNoReturnType(methodModel).trim();
		int classNameNdx = methodModel.toString().indexOf(":");
		String className = methodModel.toString().substring(classNameNdx +1).trim();

		IAction action = new AddCalleeHierarchy(methodEP, methodName, className,
				StoreUtil.getDefaultStoreRepository()) {

			@Override
			public void run() {

				// First set the action that will add to the diagram the first set of
				// calls in the hierarchy - the enabled and visible calls in the menu.
				// See {@link #setAddAllCallsAction(IAction)} for why setting at this 
				// point in the run() method.
				final CompoundCommand addAllCmd = getAddAllCmd(actionLbl, navAidMenu);
				IAction addAllAction = new Action() {
					@Override
					public void run() {
						runAddAll(addAllCmd, navAidMenu);
					}
				};
				setAddAllCallsAction(addAllAction);

				super.run();
			}

			List<IMethod> methodsAlreadyDone = new ArrayList<IMethod>();
			@Override
			public boolean isMethodAlreadyDone(ArtifactFragment methodModel){
				if(!(methodModel instanceof MethodBoxModel)) return false;
				return methodsAlreadyDone.contains(
						((MethodBoxModel)methodModel).getMethod());
			}
			@Override
			public void addMethodDone(ArtifactFragment methodModel) {
				if(!(methodModel instanceof MethodBoxModel)) return;
				methodsAlreadyDone.add(((MethodBoxModel)methodModel).getMethod());
			}

			@Override
			public boolean makesCallNotInDiagram(EditPart methodEP, DirectedRel rel) {
				if(!(methodEP.getModel() instanceof MethodBoxModel)) return false;
				return SeqSelectionEditPolicy.makesCallNotInDiagram((MethodBoxModel)methodEP.getModel());
			}

			@Override
			public void displayAllCallsMade(EditPart methodEP, DirectedRel rel) {
				if(!(methodEP.getModel() instanceof MethodBoxModel)) return;

				Map<Object, List<MultiAddCommandAction>> subTreeMap = new HashMap<Object, List<MultiAddCommandAction>>();
				List<MultiAddCommandAction> callActions = getCallsMenuActions((MethodBoxModel)methodEP.getModel(), subTreeMap, new ArrayList<Object>());

				CompoundCommand addAllCmd = new CompoundCommand();
				for(MultiAddCommandAction callAction : callActions) 
					addAllCmd.add(callAction.getCommand(new HashMap<Artifact, ArtifactFragment>()));

				((DiagramEditPart)getHost().getViewer().getContents()).execute(addAllCmd);
			}

			@Override
			public List<EditPart> getNextLevelOfMethods(EditPart methodEP) {
				List<EditPart> calledDecls = new ArrayList<EditPart>();
				if(!(methodEP instanceof MethodBoxEditPart)) return calledDecls;

				for(Object child : methodEP.getChildren()) {
					if(!(child instanceof MethodBoxEditPart)) continue;
					MethodBoxEditPart invocationEP = (MethodBoxEditPart) child; 
					if(((MethodBoxModel)invocationEP.getModel()).getType()
							!=MethodBoxModel.access) continue;

					MethodBoxEditPart declarationEP = invocationEP.getPartnerEP();
					if(declarationEP==null) continue;
					calledDecls.add(declarationEP);
				}
				return calledDecls;
			}

		};
		return action;
	}

	// The 'called by' nav aid displays a call as "X calls Y". When the caller (X)
	// is an anon class constructor we make it "new Foo() #3 in Bar.a() calls Foo()",
	// which provides some context about where the anon class is located.
	private String getDeclarationNameForAnonClassCaller(String callText, AnonymousClassConstructor callingMember, IJavaElement parent) {
		// The "new Foo() #3" part
		String declarationName = "new "+callingMember.getAnonClassNavAidString(callText);

		// For an anon class constructor, parent represents the anon class Type. To get
		// the element that contains the anon class declaration, do getParent() again.
		IJavaElement anonClassContainer = parent.getParent();
		// If the anon class is declared in a method, display "in Bar.a()".
		// If the anon class is declared as a class field, display "in Bar".
		String methodContainer = (anonClassContainer instanceof IMethod) ? 
				"."+MethodUtil.getMethodName((IMethod)anonClassContainer, null, false) 
				: "";
		while(!(anonClassContainer instanceof IType))
			anonClassContainer = anonClassContainer.getParent();
		if(anonClassContainer instanceof IType)
			declarationName = declarationName+" in " + anonClassContainer.getElementName()+methodContainer;
		return declarationName;
	}

	protected boolean isLibraryCall(Invocation invocation) {
		if (invocation == null) 
			throw new IllegalArgumentException();

		final List<ASTNode> nodeList = new ArrayList<ASTNode>(); 

		invocation.getInvocation().accept(new ASTVisitor(){

			@Override
			public boolean visit(MethodInvocation node) {
				IType type = (IType) node.resolveMethodBinding().getDeclaringClass().getJavaElement();
				if (type != null && !type.isBinary()) 
					nodeList.add(node);
				return true;
			}

			@Override
			public boolean visit(SuperMethodInvocation node) {
				IType type = (IType) node.resolveMethodBinding().getDeclaringClass().getJavaElement();
				if (type != null && !type.isBinary()) 
					nodeList.add(node);
				return true;
			}

			@Override
			public boolean visit(SuperConstructorInvocation node) {
				IType type = (IType) node.resolveConstructorBinding().getDeclaringClass().getJavaElement();
				if (type != null && !type.isBinary()) 
					nodeList.add(node);
				return true;
			}

			@Override
			public boolean visit(ConstructorInvocation node) {
				IType type = (IType) node.resolveConstructorBinding().getDeclaringClass().getJavaElement();
				if (type != null && !type.isBinary()) 
					nodeList.add(node);
				return true;
			}

			@Override
			public boolean visit(ClassInstanceCreation node) {
				IType type = (IType) node.resolveTypeBinding().getJavaElement();
				if (type != null && node.getAnonymousClassDeclaration() != null) {
					if (((IType) node.resolveTypeBinding().getSuperclass().getJavaElement()).isBinary())
						nodeList.add(node);
				}
				return true;
			}

		});

		if (nodeList.isEmpty()) return true;
		return false;
	}


	public Rectangle findTopLeftBoundsForButton(IFigure figure) {
		return findBoundsForButton(figure, 0.0, 0.0);
	}

	public Rectangle findTopRightBoundsForButton(IFigure figure) {
		return findBoundsForButton(figure, 1.0, 0.0);
	}

	public Rectangle findBottomLeftBoundsForButton(IFigure figure) {
		return findBoundsForButton(figure, 0.0, 1.0);
	}

	public Rectangle findBottomRightBoundsForButton(IFigure figure) {
		return findBoundsForButton(figure, 1.0, 1.0);
	}

	private Rectangle findBoundsForButton(IFigure figure, double relativeX, double relativeY) {
		IFigure referenceFig = getReferenceFigure((GraphicalEditPart)getHost());

		Rectangle buttonLocation = new PrecisionRectangle(referenceFig.getBounds().getResized(-1, -1));
		referenceFig.translateToAbsolute(buttonLocation);
		figure.translateToRelative(buttonLocation);
		buttonLocation.resize(1, 1);

		buttonLocation.x += (int) (buttonLocation.width * relativeX);
		buttonLocation.y += (int) (buttonLocation.height * relativeY);
		buttonLocation.setSize(new Dimension(0, 0));

		return buttonLocation;
	}

	private void addCalledByButton() {
		MethodBoxModel methodModel = (MethodBoxModel) ((MethodBoxEditPart)getHost()).getModel();

		showCalledByListButton = createCalledByButton(methodModel);
		getHandleLayer().add(showCalledByListButton);

		Point topLeftOverlappingBox = findTopLeftBoundsForButton(showCalledByListButton).getTopLeft();
		Point topLeft = new Point(topLeftOverlappingBox.x - showCalledByListButton.getSize().width - 4, topLeftOverlappingBox.y);
		showCalledByListButton.setBounds(new Rectangle(topLeft, showCalledByListButton.getPreferredSize()));
		getHostFigure().addFigureListener(new FigureListener() {
			public void figureMoved(IFigure source) {
				if(showCalledByListButton==null) return;

				Point topLeftOverlappingBox = findTopLeftBoundsForButton(showCalledByListButton).getTopLeft();
				Point topLeft = new Point(topLeftOverlappingBox.x - showCalledByListButton.getSize().width - 4, topLeftOverlappingBox.y);
				showCalledByListButton.setLocation(topLeft);
			}
		});
	}

	private MenuButton createCalledByButton(final MethodBoxModel selectedMethod) {
		IFigure arrow = AbstractReloRelationPart.getArrow(PositionConstants.EAST, new Triangle2());
		MenuButton button = new MenuButton(arrow, getHost().getViewer()) {
			@Override
			public void buildMenu(IMenuManager menu) {

				menu.add(new Separator("calledByList"));
				List<IAction> menuActions = new ArrayList<IAction>();

				// Don't want the button list to show any calls already in the diagram 
				List<CallLocation> allCallers = selectedMethod.getCallsOfNotInDiagram();
				for(final CallLocation callLocation : allCallers) {
					final IMember callingMember = callLocation.getMember();
					IMember calledMember = callLocation.getCalledMember();
					
					IJavaElement parent = callingMember.getParent();
					final Resource classRes = RJCore.jdtElementToResource(StoreUtil.getDefaultStoreRepository(), parent);
					final String className = InstanceUtil.getClassName(classRes, StoreUtil.getDefaultStoreRepository());

					// Checking for anon class since we want to display an anon class as
					// "new AnonClass() #[occurrence position]" in any nav aid 
					String callText = callLocation.getCallText();

					String declarationName = "";
					if(callLocation instanceof ImplicitConstructorCallLocation &&
							callingMember instanceof AnonymousClassConstructor) {
						declarationName = getDeclarationNameForAnonClassCaller(callText, (AnonymousClassConstructor)callingMember, parent);
					}	else {
						String methodName = MethodUtil.getMethodName((IMethod)callingMember, null, false);
						declarationName = callingMember instanceof InitializerWrapper ? 
								methodName+" in "+className : className+"."+methodName;
					}

					String invocationText = (calledMember instanceof AnonymousClassConstructor) ? 
							((AnonymousClassConstructor)calledMember).getAnonClassNavAidString(callText) 
							: callText;
					
					String invocationLabel = declarationName+" calls "+invocationText;
					MethodBoxModel tempCallerModel = new MethodBoxModel(null, (IMethod)callingMember, MethodBoxModel.declaration);
					ImageDescriptor callerMethodIcon = MethodUtil.getMethodIconDescriptor(tempCallerModel, false);
					IAction action = new Action(invocationLabel, callerMethodIcon) {
						@Override
						public void run() {

							getHost().getViewer().deselectAll();
							// At least 3 and at most 6 components may need to be added:
							// A: The class containing the declaration that makes 
							// the selected invocation (may already be in diagram)
							// B: The declaration making the selected
							// invocation (may already be in diagram)
							// C: The invocation (has to be added)
							// D: The declaration of the invocation (may already be target
							// of another call, in which case will have to be added)
							// E: The call connection (has to be added)
							// F: The return connection (has to be added)

							AnimateCalledByCommand calledByCommand = new AnimateCalledByCommand();
							DiagramModel diagram = (DiagramModel) ((DiagramEditPart)getHost().getViewer().getContents()).getModel();

							InstanceModel instanceOfSelectedMethod = selectedMethod.getInstanceModel();
							InstanceModel classContainingDeclMakingInvoc;
							if(classRes.equals(instanceOfSelectedMethod.getResource())) { // call to the same class
								classContainingDeclMakingInvoc = instanceOfSelectedMethod;
							} else {
								// A
								int indexToAddAt = diagram.getChildren().indexOf(instanceOfSelectedMethod);
								classContainingDeclMakingInvoc = InstanceUtil.findOrCreateContainerInstanceModel(null, className, classRes, diagram, indexToAddAt, calledByCommand);
							}

							// B
							IMethod declaration = (IMethod)callingMember;
							MethodBoxModel declarationMakingInvocation = MethodUtil.findDeclarationHolder(declaration, classContainingDeclMakingInvoc, calledByCommand);
							if(declarationMakingInvocation==null) {
								declarationMakingInvocation = declaration==null ? new MethodBoxModel(classContainingDeclMakingInvoc, MethodBoxModel.declaration) : new MethodBoxModel(classContainingDeclMakingInvoc, declaration, MethodBoxModel.declaration);
								MemberCreateCommand createMethodCmd = new MemberCreateCommand(declarationMakingInvocation, classContainingDeclMakingInvoc, MemberCreateCommand.NONE);
								calledByCommand.add(createMethodCmd);
							}

							// C
							Invocation invocation = MethodUtil.getInvocation(callLocation, (IMethod)callingMember);
							MethodBoxModel invocationModel = MethodUtil.createInvocationModel(invocation, classContainingDeclMakingInvoc);
							MemberCreateCommand createInvocationCmd = new MemberCreateCommand(invocationModel, declarationMakingInvocation, MemberCreateCommand.NONE);
							calledByCommand.add(createInvocationCmd);

							MethodBoxModel declarationOfInvokedMethod;
							boolean isCallToSameClass = classContainingDeclMakingInvoc.equals(instanceOfSelectedMethod);
							if(selectedMethod.getPartner()!=null) {
								// D
								// declaration is already the target of a call, so 
								// make a new declaration model that can represent 
								// the target of the selected call
								declarationOfInvokedMethod = new MethodBoxModel(instanceOfSelectedMethod, selectedMethod.getMethod(), MethodBoxModel.declaration);
								NodeModel declOfInvocParent = isCallToSameClass ? invocationModel : instanceOfSelectedMethod;
								MemberCreateCommand createDeclOfInvocCmd = new MemberCreateCommand(declarationOfInvokedMethod, declOfInvocParent, MemberCreateCommand.NONE);
								calledByCommand.add(createDeclOfInvocCmd);
							} else {
								declarationOfInvokedMethod = selectedMethod;
								if(isCallToSameClass) {
									// since is a call to same class, need to move the 
									// declaration from the instance into the invocation
									ChangeParentOfMethodCommand moveCmd = new ChangeParentOfMethodCommand(declarationOfInvokedMethod, invocationModel, -1);
									calledByCommand.add(moveCmd);
								}
							}		

							// E, F
							ConnectionCreateCommand callConn = new ConnectionCreateCommand(invocationModel, declarationOfInvokedMethod, MethodUtil.getMethodName(invocationModel.getMethod(), invocation, false), ConnectionModel.CALL);							
							ConnectionCreateCommand returnConn = new ConnectionCreateCommand(declarationOfInvokedMethod, invocationModel, MethodUtil.getReturnMessage(invocation), ConnectionModel.RETURN);
							calledByCommand.add(callConn);
							calledByCommand.add(returnConn);

							calledByCommand.setDeclMakingInvoc(declarationMakingInvocation);
							calledByCommand.setInvocation(invocationModel);
							calledByCommand.setDeclaration(declarationOfInvokedMethod);

							((DiagramEditPart)getHost().getViewer().getContents()).execute(calledByCommand);
						}
					};
					menuActions.add(action);
				}
				Collections.sort(menuActions, alphabeticalComparator);
				for(IAction action : menuActions) menu.appendToGroup("calledByList", action);

				// If no callers in the list (should never happen, but in case it
				// does), showing disabled entry that indicates no more callers so
				// that clicking the button in this case doesn't just do nothing
				if(menu.getItems().length<=1) { // need to test <=1 not ==0 to account for "calledByList" separator
					menu.add(new Separator("displayNone"));
					IAction displayNoneAction = new Action("No more callers") {
						@Override
						public void run() {
						}
					};
					menu.appendToGroup("displayNone", displayNoneAction);
					displayNoneAction.setEnabled(false);
				}
			}
		};
		button.setSize(new Dimension(22, 15));
		button.setToolTip(new Label("Show list of methods that invoke " + selectedMethod.getName()));

		return button;
	}

	private void addOverridesButton() {
		MethodBoxModel methodModel = (MethodBoxModel) ((MethodBoxEditPart)getHost()).getModel();

		overridesButton = createOverridesButton(methodModel);
		getHandleLayer().add(overridesButton);

		Point topRightOverlappingBox = findTopRightBoundsForButton(overridesButton).getTopRight();
		Point topRight = new Point(topRightOverlappingBox.x - MethodBoxFigure.DEFAULT_SIZE.width/2, topRightOverlappingBox.y - overridesButton.getSize().height - 4);
		overridesButton.setBounds(new Rectangle(topRight, overridesButton.getPreferredSize()));
		getHostFigure().addFigureListener(new FigureListener() {
			public void figureMoved(IFigure source) {
				if(overridesButton==null) return;

				Point topRightOverlappingBox = findTopRightBoundsForButton(overridesButton).getTopRight();
				Point topRight = new Point(topRightOverlappingBox.x - MethodBoxFigure.DEFAULT_SIZE.width/2, topRightOverlappingBox.y - overridesButton.getSize().height);
				overridesButton.setBounds(new Rectangle(topRight, overridesButton.getPreferredSize()));
			}
		});
	}

	private MenuButton createOverridesButton(final MethodBoxModel selectedMethod) {
		IFigure arrow = new Label(ImageCache.impl_co);
		MenuButton button = new MenuButton(arrow, getHost().getViewer()) {
			@Override
			public void buildMenu(IMenuManager menu) {

				menu.add(new Separator("overridesList"));

				ReloRdfRepository repo = StoreUtil.getDefaultStoreRepository();
				Value obj = repo.getStatement(selectedMethod.getMethodRes(), RJCore.overrides, null).getObject();
				if(obj==null || !(obj instanceof URI)) return;

				IJavaElement overriddenElmt = RJCore.resourceToJDTElement(repo, (URI)obj);
				if(!(overriddenElmt instanceof IMethod)) return;

				IAction action = createMenuActionForOverride(selectedMethod, true, (IMethod)overriddenElmt);
				menu.appendToGroup("overridesList", action);
			}
		};
		button.setSize(new Dimension(22, 15));
		button.setToolTip(new Label("Show method that " + selectedMethod.getName() + "overrides or implements"));

		return button;
	}

	private void addOverriddenByButton() {
		MethodBoxModel methodModel = (MethodBoxModel) ((MethodBoxEditPart)getHost()).getModel();

		overridenByButton = createOverridenByButton(methodModel);
		getHandleLayer().add(overridenByButton);

		Point bottomLeftOverlappingBox = findBottomLeftBoundsForButton(overridenByButton).getBottomLeft();
		Point bottomLeft = new Point(bottomLeftOverlappingBox.x-3, bottomLeftOverlappingBox.y+1);
		overridenByButton.setBounds(new Rectangle(bottomLeft, overridenByButton.getPreferredSize()));
		getHostFigure().addFigureListener(new FigureListener() {
			public void figureMoved(IFigure source) {
				if(overridenByButton==null) return;

				Point bottomLeftOverlappingBox = findBottomLeftBoundsForButton(overridenByButton).getBottomLeft();
				Point bottomLeft = new Point(bottomLeftOverlappingBox.x-3, bottomLeftOverlappingBox.y+1);
				overridenByButton.setBounds(new Rectangle(bottomLeft, overridenByButton.getPreferredSize()));
			}
		});
	}

	private MenuButton createOverridenByButton(final MethodBoxModel selectedMethod) {
		IFigure arrow = new Label(ImageCache.impl_co);
		MenuButton button = new MenuButton(arrow, getHost().getViewer()) {
			@Override
			public void buildMenu(IMenuManager menu) {

				menu.add(new Separator("overridenByList"));

				List<IAction> menuActions = new ArrayList<IAction>();
				for(final IMethod declaration : selectedMethod.getMethodsThatOverride()) {

					boolean overriderAlreadyAdded = false;
					for(ConnectionModel overriderConn : selectedMethod.getOverriderConnections()) {
						NodeModel overrider = (NodeModel) overriderConn.getSource();
						if(overrider instanceof MethodBoxModel &&
								declaration.equals(((MethodBoxModel)overrider).getMethod())) {
							overriderAlreadyAdded = true;
							break;
						}

					}
					if(overriderAlreadyAdded) continue;

					IAction action = createMenuActionForOverride(selectedMethod, false, declaration);
					menuActions.add(action);
				}
				Collections.sort(menuActions, alphabeticalComparator);
				for(IAction action : menuActions) menu.appendToGroup("overridenByList", action);
			}
		};
		button.setSize(new Dimension(22, 15));
		button.setToolTip(new Label("Show methods that override or implement " + selectedMethod.getName()));

		return button;
	}

	/*
	 * 
	 * @param selectedMethod the selected method, whose overrides 
	 * or overridden by button we are creating an action for
	 * @param isOverriderSelected true if we are creating an action for the 
	 * selected method's overrides button list, false if we are creating an 
	 * action for the selected method's overridden by button list
	 * @param method the IMethod of the method that either overrides (if dealing 
	 * with selectedMethod's overridden by button) or is overridden by (if dealing 
	 * with selectedMethod's overrides button) selectedMethod
	 * @return an IAction that finds or creates models for method and its instance 
	 * and adds an overrides connection from selectedMethod to the method model 
	 * (if dealing with selectedMethod's overrides button) or from the method model 
	 * to selectedMethod (if dealing with selectedMethod's overridden by button)   
	 */
	private IAction createMenuActionForOverride(final MethodBoxModel selectedMethod, final boolean isOverriderSelected, final IMethod method) {

		IJavaElement declaringClass = method.getParent();
		final Resource declaringClassRes = RJCore.jdtElementToResource(StoreUtil.getDefaultStoreRepository(), declaringClass);
		final String className = InstanceUtil.getClassName(declaringClassRes, StoreUtil.getDefaultStoreRepository());

		String declarationName = className+"."+MethodUtil.getMethodName(method, null, false);
		MethodBoxModel tempDeclarationModel = new MethodBoxModel(null, method, MethodBoxModel.declaration);
		ImageDescriptor declarationIcon = MethodUtil.getMethodIconDescriptor(tempDeclarationModel, false);
		IAction action = new Action(declarationName, declarationIcon) {
			@Override
			public void run() {

				getHost().getViewer().deselectAll();
				DiagramModel diagram = (DiagramModel) ((DiagramEditPart)getHost().getViewer().getContents()).getModel();
				int selMethodInstanceIndex = diagram.getChildren().indexOf(selectedMethod.getInstanceModel());
				int indexToAddAt = isOverriderSelected ? selMethodInstanceIndex+1 : selMethodInstanceIndex;

				AnimateOverrideCommand overrideCommand = new AnimateOverrideCommand();
				BreakableCommand addMethodAndOverridesConnCmd = new BreakableCommand("adding overrides relationship", overrideCommand);

				InstanceModel declaringClassModel = InstanceUtil.findOrCreateContainerInstanceModel(null, className, declaringClassRes, diagram, indexToAddAt, overrideCommand);
				MethodBoxModel methodModel = MethodUtil.findDeclarationHolder(method, declaringClassModel, overrideCommand);
				if(methodModel==null) {
					methodModel = method==null ? new MethodBoxModel(declaringClassModel, MethodBoxModel.declaration) : new MethodBoxModel(declaringClassModel, method, MethodBoxModel.declaration);
					MemberCreateCommand createMethodCmd = new MemberCreateCommand(methodModel, declaringClassModel, MemberCreateCommand.NONE);
					overrideCommand.add(createMethodCmd);
					overrideCommand.setNewMethod(methodModel);
				}

				overrideCommand.setInstance(declaringClassModel);
				MethodBoxModel overrider = isOverriderSelected ? selectedMethod : methodModel;
				MethodBoxModel overridden = isOverriderSelected ? methodModel : selectedMethod;
				overrideCommand.setOverrider(overrider);
				overrideCommand.setOverridden(overridden);

				AnimateOverrideConnectionCommand connAnimationCmd = overrideCommand.createAnimateOverrideConnectionCommand();
				addMethodAndOverridesConnCmd.addBreakPlace(connAnimationCmd);

				ConnectionCreateCommand overridesConnCmd = new ConnectionCreateCommand(overrider, overridden, "overrides", ConnectionModel.OVERRIDES);							
				addMethodAndOverridesConnCmd.add(overridesConnCmd);

				((DiagramEditPart)getHost().getViewer().getContents()).execute(addMethodAndOverridesConnCmd);

				overridesConnCmd.setVisible(false);
				overridesConnCmd.getConnection().setVisible(false);
			}
		};
		return action;
	}

	private static void addHandles(GraphicalEditPart part, List<Object> handles) {
		addMoveHandle(part, handles);
	}

	private static void addHandles(GraphicalEditPart part, List<Object> handles, DragTracker tracker, Cursor cursor) {
		addMoveHandle(part, handles, tracker, cursor);
	}

	private static void addMoveHandle(GraphicalEditPart f, List<Object> handles) {
		handles.add(moveHandle(f));
	}

	private static void addMoveHandle(GraphicalEditPart f, List<Object> handles, DragTracker tracker, Cursor cursor) {
		handles.add(moveHandle(f, tracker, cursor));
	}

	private static Handle moveHandle(GraphicalEditPart owner) {
		return new SeqMoveHandle(owner);
	}

	private static Handle moveHandle(GraphicalEditPart owner, DragTracker tracker, Cursor cursor) {
		SeqMoveHandle moveHandle = new SeqMoveHandle(owner);
		moveHandle.setDragTracker(tracker);
		moveHandle.setCursor(cursor);
		return moveHandle;
	}

	// Overriding method to specifically handle multiple selected instances 
	// to be grouped using drag and drop.
	@Override
	protected Command getMoveCommand(ChangeBoundsRequest request) {
		ChangeBoundsRequest req = new ChangeBoundsRequest(REQ_MOVE_CHILDREN);
		req.setEditParts(request.getEditParts());

		req.setMoveDelta(request.getMoveDelta());
		req.setSizeDelta(request.getSizeDelta());
		req.setLocation(request.getLocation());
		req.setExtendedData(request.getExtendedData());
		// making sure that the commands are made only once(for the first edit part)
		if(req.getEditParts().get(0).equals(getHost())) 
			return getHost().getParent().getCommand(req);
		return null;
	}
}
