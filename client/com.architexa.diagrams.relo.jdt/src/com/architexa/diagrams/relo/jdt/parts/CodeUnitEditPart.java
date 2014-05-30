/* 
 * Copyright (c) 2004-2005 Massachusetts Institute of Technology. This code was
 * developed as part of the Haystack (http://haystack.lcs.mit.edu/) research 
 * project at MIT. Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation files (the 
 * "Software"), to deal in the Software without restriction, including without 
 * limitation the rights to use, copy, modify, merge, publish, distribute, 
 * sublicense, and/or sell copies of the Software, and to permit persons to whom
 * the Software is furnished to do so, subject to the following conditions: 
 * 
 * The above copyright notice and this permission notice shall be included in 
 * all copies or substantial portions of the Software. 
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR 
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, 
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE 
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER  
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, 
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE. 
 */
/*
 * Created on Jun 13, 2004
 *
 */
package com.architexa.diagrams.relo.jdt.parts;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.Predicate;
import org.apache.log4j.Logger;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.search.ui.ISearchQuery;
import org.eclipse.search2.internal.ui.InternalSearchUI;
import org.eclipse.search2.internal.ui.text2.DefaultTextSearchQueryProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.TreeItem;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;

import com.architexa.diagrams.RSECore;
import com.architexa.diagrams.commands.AddNodeAndRelCmd;
import com.architexa.diagrams.commands.ColorActionCommand;
import com.architexa.diagrams.draw2d.IFigureWithContents;
import com.architexa.diagrams.eclipse.gef.MenuButton;
import com.architexa.diagrams.eclipse.gef.UndoableLabelSource;
import com.architexa.diagrams.jdt.Activator;
import com.architexa.diagrams.jdt.ArtifactContainer;
import com.architexa.diagrams.jdt.Filters;
import com.architexa.diagrams.jdt.IJavaElementContainer;
import com.architexa.diagrams.jdt.RJCore;
import com.architexa.diagrams.jdt.actions.AddCalleeHierarchy;
import com.architexa.diagrams.jdt.model.CodeUnit;
import com.architexa.diagrams.jdt.model.UserCreatedFragment;
import com.architexa.diagrams.jdt.ui.RSEOutlineInformationControl;
import com.architexa.diagrams.jdt.utils.JDTUISupport;
import com.architexa.diagrams.model.Artifact;
import com.architexa.diagrams.model.ArtifactFragment;
import com.architexa.diagrams.model.DerivedArtifact;
import com.architexa.diagrams.model.DirectedRel;
import com.architexa.diagrams.model.NamedRel;
import com.architexa.diagrams.parts.AnnoLabelCellEditorLocator;
import com.architexa.diagrams.parts.AnnoLabelDirectEditPolicy;
import com.architexa.diagrams.parts.BasicRootController;
import com.architexa.diagrams.parts.NavAidsEditPolicy;
import com.architexa.diagrams.parts.NavAidsSpec;
import com.architexa.diagrams.parts.PointPositionedDiagramPolicy;
import com.architexa.diagrams.parts.RelNavAidsSpec;
import com.architexa.diagrams.relo.commands.InteriorMoveCommand;
import com.architexa.diagrams.relo.figures.CodeUnitFigure;
import com.architexa.diagrams.relo.jdt.ReloJDTPlugin;
import com.architexa.diagrams.relo.jdt.actions.AddJavaDocAction;
import com.architexa.diagrams.relo.jdt.actions.EditJavaDocDialogAction;
import com.architexa.diagrams.relo.jdt.commands.DeleteCommand;
import com.architexa.diagrams.relo.jdt.commands.OpenInEditorCommand;
import com.architexa.diagrams.relo.jdt.parts.CompartmentedCodeUnitEditPart.CompartmentCUEditPart;
import com.architexa.diagrams.relo.jdt.services.PluggableEditPartSupport;
import com.architexa.diagrams.relo.modelBridge.ReloDoc;
import com.architexa.diagrams.relo.parts.ArtifactEditPart;
import com.architexa.diagrams.relo.parts.MoreItemsEditPart;
import com.architexa.diagrams.relo.parts.ReloController;
import com.architexa.diagrams.relo.ui.ColorScheme;
import com.architexa.diagrams.services.PluggableNavAids;
import com.architexa.diagrams.services.PluggableNavAids.INavAidSpecSource;
import com.architexa.diagrams.ui.ImageCache;
import com.architexa.diagrams.ui.MultiAddCommandAction;
import com.architexa.diagrams.ui.RSEContextMenuProvider;
import com.architexa.diagrams.ui.RSEEditor;
import com.architexa.diagrams.utils.MoreButtonUtils;
import com.architexa.org.eclipse.draw2d.Figure;
import com.architexa.org.eclipse.draw2d.IFigure;
import com.architexa.org.eclipse.draw2d.Label;
import com.architexa.org.eclipse.draw2d.ToolbarLayout;
import com.architexa.org.eclipse.draw2d.geometry.Dimension;
import com.architexa.org.eclipse.draw2d.geometry.Point;
import com.architexa.org.eclipse.draw2d.geometry.Rectangle;
import com.architexa.org.eclipse.gef.ConnectionEditPart;
import com.architexa.org.eclipse.gef.DefaultEditDomain;
import com.architexa.org.eclipse.gef.EditDomain;
import com.architexa.org.eclipse.gef.EditPart;
import com.architexa.org.eclipse.gef.EditPartViewer;
import com.architexa.org.eclipse.gef.EditPolicy;
import com.architexa.org.eclipse.gef.GraphicalEditPart;
import com.architexa.org.eclipse.gef.Request;
import com.architexa.org.eclipse.gef.RequestConstants;
import com.architexa.org.eclipse.gef.commands.Command;
import com.architexa.org.eclipse.gef.commands.CompoundCommand;
import com.architexa.org.eclipse.gef.requests.CreateConnectionRequest;
import com.architexa.org.eclipse.gef.requests.CreateRequest;
import com.architexa.org.eclipse.gef.requests.GroupRequest;
import com.architexa.org.eclipse.gef.requests.ReconnectRequest;
import com.architexa.org.eclipse.gef.tools.CellEditorLocator;
import com.architexa.org.eclipse.gef.tools.LabelDirectEditManager;
import com.architexa.org.eclipse.gef.tools.LabelSource;
import com.architexa.rse.BuildStatus;
import com.architexa.store.ReloRdfRepository;


/**
 * Java Related Functionality
 * 
 * @author vineet
 */
public class CodeUnitEditPart extends MoreItemsEditPart implements IJavaElementContainer, ArtifactContainer {
	static final Logger logger = ReloJDTPlugin.getLogger(CodeUnitEditPart.class);

    public final static String REQ_REDUCE = "minimize";
    public final static String REQ_EXPAND = "expand";
    
    // tooltips
    public final static String COLLAPSE = "collapse";
    public final static String EXPAND = "expand";
    public final static String HIDE = "hide";
    
    private final static boolean useCtrlOStyleMemberMenu = true;

	private String label;

    @Override
    protected IFigure createFigure(IFigure curFig, int newDL) {
        if (curFig == null) {
        	ImageDescriptor id = this.getIconDescriptor(getCU().getArt(), getCU().queryType(getRepo()));        	
		    Image img = ImageCache.calcImageFromDescriptor(id);
		    label = getLabel();
		    	
		    Label nameLbl = new Label(label, img);
			
            Figure lblFig = new Figure();
            lblFig.setLayoutManager(new ToolbarLayout(true));
            lblFig.add(nameLbl);
            lblFig.add(new Label("  "));

            curFig = new CodeUnitFigure(lblFig, null, null);
            curFig.setToolTip(new Label(CodeUnit.getLabelWithContext(getRepo(), (ArtifactFragment) getModel())));
        }

        return curFig;
    }

	public CodeUnit getCU() {
	    return (CodeUnit) getModel();
	}
    
	
	@Override
    public void setModel(Object model) {
		// make sure that the model is of an easy to handle type
		if (!(model instanceof CodeUnit)) {
			logger.error("Can't deal with type: " + model.getClass());
		}
		super.setModel(model);
	}

	
    @Override
    public String getLabel(Artifact art, Artifact contextArt) {
        return CodeUnit.getLabel(getRepo(), art, contextArt);
    }
    
    @Override
    public ImageDescriptor getIconDescriptor(Artifact art, Resource resType) {
    	ImageDescriptor icon = PluggableEditPartSupport.getIconDescriptor(getRepo(), art, resType);
    	return CodeUnit.getDecoratedIcon(getRepo(), art, icon);
    }
    @Override
    public void activate() {
		super.activate();
		updateMembers(currDL);
	}
	
	private IJavaElement cachedIJE = null;

	private RSEOutlineInformationControl popup = null; 
	public IJavaElement getJaveElement() {
		if (cachedIJE == null)
			cachedIJE = RJCore.resourceToJDTElement(this.getRepo(), getContainedArtifact().elementRes);
		return cachedIJE;
	}

	public Artifact getContainedArtifact() {
		return getArtifact().getArt();
	}
	
	
    /* (non-Javadoc)
     * @see org.eclipse.gef.editparts.AbstractEditPart#createEditPolicies()
     */
    @Override
    protected void createEditPolicies() {
        super.createEditPolicies();
        installEditPolicy(NavAidsEditPolicy.HANDLES_ROLE, new NavAidsEditPolicy());
        installDirectEditPolicy();
    }


	
	
	// content pane
	@Override
    public IFigure getContentPane() {
		IFigure fig = getFigure();
		while (fig instanceof IFigureWithContents)
			fig = ((IFigureWithContents)fig).getContentFig();
		return fig;
	}
	public String getLabel() {
		return getCU().getLabel(getRepo(), getCU().getParentArt().getArt()) + tag.getTrailer();
	}
	
	public String getLabel(Object context) {
		if (context instanceof CodeUnit) {
			return getCU().getLabel(getRepo(), ((CodeUnit) context).getArt()) + tag.getTrailer();
		} else {
			return getCU().getLabel(getRepo()) + tag.getTrailer();
		}
	}

	@Override
	public IFigure getMoreButton() {
		return getMoreBtn();
	}
	
	@Override
	public void buildMoreMenu(IMenuManager menu, MenuButton button) {
		if(!useCtrlOStyleMemberMenu) {
			super.buildMoreMenu(menu, button);
			return;
		}
		// Give menu a title label of "Members of MyClass:"
		String titleText = "Members of "+getLabel().trim()+":";
		List<MultiAddCommandAction> actions = getAllMemberMenuActions();
		if (actions == null) return; // return if actions are being sent to server
		
		// Members could have multiple access levels or 
		// kinds, and members will not be library code
		String[] filters = new String[] {
				RSEOutlineInformationControl.FILTERS_ACCESS_LEVEL, 
				RSEOutlineInformationControl.FILTERS_MEMBER_KIND};
		for (MultiAddCommandAction maca : actions) {
			Artifact art = RSEOutlineInformationControl.getArtifact(maca);
			// TODO: remove deplicated string here and inject this from EJCore
			// make sure to use this namespace part of the string and not 'web-root' since there can be nested web folders
			if (art.elementRes.toString().contains("jdtext-wkspc#")) {
				filters =  new String[] {""};
				break;
			}
		}
		
		createInformationControlMenu(titleText, actions, null, filters, button);
	}

	@Override
	public void buildNavAidMenu(List<MultiAddCommandAction> menuActions, NavAidsSpec spec, 
			IMenuManager defaultMenu, DirectedRel rel) {

		String[] filters;
		if(RJCore.inherits.equals(rel.res))
			// Multiple possible access levels and kinds, so show all filters in the menu
			filters = RSEOutlineInformationControl.FILTERS_ALL;
		else 
			// Everything in a calls nav aid or an overrides nav aid will be of kind
			// Method, so don't want filters for member kind because seeing check boxes 
			// for Class or Field can be confusing here
			filters = new String[] {RSEOutlineInformationControl.FILTERS_ACCESS_LEVEL, 
				RSEOutlineInformationControl.FILTERS_LIBRARY_CODE};

		createInformationControlMenu(getNavAidTitle(rel), menuActions, rel, filters, spec.decorationFig);
	}

	// Get a title appropriate for the nav aid menu depending on the rel and its direction
	private String getNavAidTitle(DirectedRel rel) {

		String cuName = getLabel().trim();
		String relName = rel.res.getLocalName().trim();
		String relNameCap = relName.substring(0, 1).toUpperCase()+relName.substring(1); // first letter capitalized

		String firstWord = "";
		String secondWord = "";
		String thirdWord = "";

		if(RJCore.inherits.equals(rel.res)) {
			if(rel.isFwd) {
				// inherits nav aid on top means this is a subclass, so make
				// menu title "MyClass inherits from:"
				firstWord = cuName;
				secondWord = relName;
				thirdWord = "from";
			} else {
				// inherits nav aid on bottom means this is a superclass, so make 
				// menu title "Inherits from MyClass:"
				firstWord = relNameCap;
				secondWord = "from";
				thirdWord = cuName;
			}
		} else if(RJCore.overrides.equals(rel.res)) {
			if(rel.isFwd) {
				// overrides nav aid on top means this member overrides something, so 
				// make menu title "foo() overrides:"
				firstWord = cuName;
				secondWord = relName;
			} else {
				// overrides nav aid on bottom means this member is overridden, so 
				// make menu title "Overrides foo():"
				firstWord = relNameCap;
				secondWord = cuName;
			}
		} else {
			// make menu title of nav aid on left "Members calling foo():" and on
			// right "Calls from foo():" to match eclipse Call Hierarchy messages
			firstWord = rel.isFwd ? relNameCap : "Members";
			secondWord = rel.isFwd ? "from" : "calling";
			thirdWord = cuName;
		}

		return (firstWord+" "+secondWord+" "+thirdWord).trim()+":";
	}

	protected void createInformationControlMenu(String menuTitle,
			List<MultiAddCommandAction> menuActions, DirectedRel addButtonsActionsRel, 
			String[] filters, IFigure menuButton) {

		if (popup!=null && popup.active) 
			return;
		popup = new RSEOutlineInformationControl(getBrowseModel().getRepo(), menuTitle, null, filters);
		popup.setInput(menuActions);

		// Get the actions for the buttons that add multiple items at once
		List<IAction> addButtonsActions = getAddButtonsActions(menuActions, popup, addButtonsActionsRel);
		popup.setButtonInput(addButtonsActions);

		// menu should open along the right hand side of the member / nav aid button
		EditPartViewer parentViewer = getViewer();
		Control parent = parentViewer.getControl();
		Rectangle figBounds = menuButton.getBounds().getCopy();
		menuButton.translateToAbsolute(figBounds);
		org.eclipse.swt.graphics.Point menuLocation = parent.toDisplay(figBounds.getTopRight().x+1, figBounds.getTopRight().y-1);
		popup.setLocation(menuLocation);

		// make sure menu is proper size to show all components but not stretch too wide
		popup.pack();
		popup.setInitSize();
		popup.open();
	}

	private List<IAction> getAddButtonsActions(List<MultiAddCommandAction> menuActions, 
			RSEOutlineInformationControl navAidMenu, DirectedRel rel) {
		List<IAction> addActions = new ArrayList<IAction>();

		// Option that will add all enabled and visible calls in the menu to the diagram
		IAction addAllAction = getAddAllItemsAction(navAidMenu, getRootController());
		if(addAllAction!=null) addActions.add(addAllAction);

		if(rel!=null && RJCore.calls.equals(rel.res)) {
			// Option that will incrementally add all of the enabled and visible 
			// calls this method makes, every subsequent call those called 
			// methods make, every subsequent call those methods make, etc.
			IAction calleeHierAction = getAddCalleeHierarchyAction(navAidMenu, getRootController());
			addActions.add(calleeHierAction);

			// lifecycle action will have an icon, so give the Add All button
			// in the calls nav aid an icon too with a similar appearance in 
			// order to help users understand the difference between the two options
			addAllAction.setImageDescriptor(Activator.getImageDescriptor("icons/addAll_callees.png"));
		}

		// If there is not at least one enabled call in the list, continue showing
		// these buttons so the menu always looks consistent but disable the buttons
		boolean atLeastOneAddableItem = false;
		for(MultiAddCommandAction action : menuActions) {
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

	private IAction getAddAllItemsAction(
			final RSEOutlineInformationControl navAidMenu, final BasicRootController rc) {
		final String actionText = "Add All";
		IAction showAllAction = new Action(actionText) {
			@Override
			public void run() {
				CompoundCommand addAllCmd = getAddAllCmd(actionText, navAidMenu);
				runAddAll(addAllCmd, navAidMenu, rc);
			}
		};
		return showAllAction;
	}

	// Returns a compound command containing the commands of
	// visible and enabled (unfiltered and selectable) tree items
	private CompoundCommand getAddAllCmd(String addAllLabel, 
			RSEOutlineInformationControl navAidMenu) {
		CompoundCommand addAllCmd = new CompoundCommand(addAllLabel);
		Map<Artifact, ArtifactFragment> addedArtToAFMap = new HashMap<Artifact, ArtifactFragment>();
		addCommandsToAddAllCmd(navAidMenu.getTreeViewer().getTree().getItems(), 
				addAllCmd, addedArtToAFMap);
		return addAllCmd;
	}
	// Adds only the commands of visible and enabled tree items to the given compound cmd
	private void addCommandsToAddAllCmd(final TreeItem[] menuItems, 
			CompoundCommand addAllCmd, Map<Artifact, ArtifactFragment> addedArtToAFMap) {
		for(TreeItem visibleItem : menuItems) {
			Object data = visibleItem.getData();
			if(data instanceof MultiAddCommandAction) {
				MultiAddCommandAction maca = (MultiAddCommandAction) data;
				if(maca.isEnabled())
					MoreButtonUtils.addCommand(maca, addAllCmd, addedArtToAFMap);
			}

			// Only add expanded subtrees; if the user has collapsed a
			// subtree, we assume they are not of interest and don't add
			boolean isExpanded = 
				visibleItem.getItemCount()==0 || // consider expanded b/c not a parent node
				visibleItem.getExpanded(); // is a subtree parent, so test if expanded
			if(isExpanded)
				addCommandsToAddAllCmd(visibleItem.getItems(), addAllCmd, addedArtToAFMap);
		}
	}

	private void runAddAll(CompoundCommand addAllCmd, 
			RSEOutlineInformationControl navAidMenu, BasicRootController rc) {
		if(!addAllCmd.isEmpty()) {
			navAidMenu.dispose(); // close the menu
			rc.execute(addAllCmd); // and add the calls
		}
	}

	private IAction getAddCalleeHierarchyAction(
			final RSEOutlineInformationControl navAidMenu, final BasicRootController rc) {
		IAction action = new AddCalleeHierarchy(this, getLabel().trim(), 
				((ArtifactFragment)getModel()).getParentArt().getArt().queryName(getRepo()).trim(), 
				getRepo()) {

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
						runAddAll(addAllCmd, navAidMenu, rc);
					}
				};
				setAddAllCallsAction(addAllAction);

				super.run();
			}

			List<Resource> methodsAlreadyDone = new ArrayList<Resource>();
			@Override
			public boolean isMethodAlreadyDone(ArtifactFragment methodModel){
				return methodsAlreadyDone.contains(methodModel.getArt().elementRes);
			}
			@Override
			public void addMethodDone(ArtifactFragment methodModel) {
				methodsAlreadyDone.add(methodModel.getArt().elementRes);
			}

			@Override
			public boolean makesCallNotInDiagram(EditPart methodEP, DirectedRel rel) {
				if(!(methodEP instanceof CodeUnitEditPart)) return false;
				CodeUnitEditPart cuep = (CodeUnitEditPart) methodEP;
				List<Artifact> calls = 
					cuep.showableListModel(cuep.getRepo(), rel, null);
				return calls!=null && calls.size()!=0;
			}

			@Override
			public void displayAllCallsMade(EditPart methodEP, DirectedRel rel) {
				if(!(methodEP instanceof CodeUnitEditPart)) return;
				CodeUnitEditPart cuep = (CodeUnitEditPart) methodEP;

				CompoundCommand addAllCmd = new CompoundCommand();
				BasicRootController rc = cuep.getRootController();
				List<Artifact> showableSetModel = cuep.showableListModel(cuep.getRepo(), rel, null);
				for (Artifact relArt : showableSetModel) {
					String relArtLbl = cuep.getRelModelLabel(relArt);
					MultiAddCommandAction action = cuep.getShowRelAction(rc, rel, relArt, relArtLbl);
					if (action!= null)
						addAllCmd.add(action.getCommand(new HashMap<Artifact, ArtifactFragment>()));
				}
				rc.execute(addAllCmd);
			}

			@Override
			public List<EditPart> getNextLevelOfMethods(EditPart methodEP) {
				List<EditPart> targets = new ArrayList<EditPart>();
				if(!(methodEP instanceof CodeUnitEditPart)) return targets;

				CodeUnitEditPart cuep = (CodeUnitEditPart) methodEP;
				for(Object conn : cuep.getSourceConnections()) {
					if(conn instanceof ConnectionEditPart &&
							((ConnectionEditPart)conn).getTarget() instanceof CodeUnitEditPart)
						targets.add(((ConnectionEditPart)conn).getTarget());
				}
				return targets;
			}
		};
		return action;
	}

	@Override
    /**
     * If a class contains a static or instance initializer, there will be a
     * stmt in the repo saying the class contains a <clinit> Resource. However,
     * the builder places an identical statement in the repo when the
     * class contains a static field, which is already handled separately
     * and we therefore want to remove from the given list. To identify 
     * the case where the child Artifact is an initializer, we can test 
     * whether there are any 'calls' stmts for it in the repo, since only 
     * initialization blocks will have these stmts. 
     * @return a list of the children Artifacts that are present as
     * <clinit> in the repository but do not actually represent initializer
     * blocks and therefore should not be included in the members button
     */
    public List<Artifact> getFakeInitializerChildren() {

    	List<Artifact> fakes = new ArrayList<Artifact>();
    	Set<Artifact> children = new HashSet<Artifact>(
    			getArtifact().getArt().
    			queryChildrenArtifacts(getRepo()));
    	for(Artifact child : new ArrayList<Artifact>(children)) {
    		if(!"<clinit>".equals(child.queryName(getRepo()))) continue;

    		boolean makesAnyCalls = getRepo().hasStatement(child.elementRes, RJCore.calls, null);
    		if(!makesAnyCalls) fakes.add(child);
    	}

    	return fakes;
    }
	
	
	
	/*
	 * other misc. functionality
	 *
	 */



    @Override
	public String getRelModelLabel(Object model) {
        if (!(model instanceof Artifact)) return "{err}";
        String label = this.getLabel((Artifact)model, this.getArtifact().getArt());
        
        // change label for calls to anon class constructors
        if(RSECore.isAnonClassConstructorCall(label)) 
        	label = label.replace("##", "#");

        // Mark library code. (If model is lib code, only reach 
        // here if user has pref set to show lib code in menus)
        if(!RSECore.isInitialized(getBrowseModel().getRepo(), ((Artifact)model).elementRes))
        	label = label+ArtifactFragment.libraryAnnotation;
        return label;
    }

	

    @Override
	public List<NavAidsSpec> getSingleSelectHandlesSpecList(final NavAidsEditPolicy bdec) {
		final List<NavAidsSpec> decorations = new ArrayList<NavAidsSpec> (5);

		// let others plug into here
		Set<INavAidSpecSource> registeredNASS = PluggableNavAids.getRegisteredNavAidsSources();
		for (INavAidSpecSource iNavAidSpecSource : registeredNASS) {
			decorations.add(iNavAidSpecSource.getNavAids(decorations, this));
		}

		// Delete
		final NavAidsSpec deleteSpec = new NavAidsSpec() {
			@Override
			public void buildHandles() {
	            IFigure btn;
	            
	        	// TODO Need to rework the command infrastructure so that expand and
				// collapse buttons work correctly
		    	// currently collapse/ reduce does nothing
	
				//btn = getReqButton(CodeUnitEditPart.this, "collapse.gif", REQ_REDUCE, COLLAPSE);
				//if (btn != null) decorationFig.add(btn);
	           	
				//btn = getReqButton(CodeUnitEditPart.this, "expand.gif", REQ_EXPAND, EXPAND);
				//if (btn != null) decorationFig.add(btn);
	            
	            btn = getReqButton(CodeUnitEditPart.this, "remove.gif", RequestConstants.REQ_DELETE, HIDE);
	            if (btn != null) decorationFig.add(btn);
	        }
            @Override
            public Point getHandlesPosition(IFigure containerFig) {
            	// Place in upper right corner, to right of any other nav aid
            	// (currently only other nav aid in this location is calls nav
            	// aid) except the context-menu button, which is the rightmost,
            	// so order would be like calls, delete, context-menu
            	Point decPos = containerFig.getBounds().getTopRight();
            	decPos.x = Math.max(
            			decPos.x, 
            			firstNAS.decorationFig.getBounds().getTopRight().x);
            	for (NavAidsSpec naSpec : decorations) {
            		if (naSpec.decorationFig==null) continue;

            		if (naSpec instanceof RelNavAidsSpec
            				&& RJCore.calls.equals(((RelNavAidsSpec)naSpec).getRel().res)
            				&& ((RelNavAidsSpec)naSpec).getRel().isFwd) {
            			// delete nav aid is right of every other 
            			// nav aid - currently only calls nav aid
            			Rectangle callsBtnBounds = naSpec.decorationFig.getBounds().getCopy();
            			decPos.x = callsBtnBounds.x + callsBtnBounds.width;
            			if (bdec.getHost() instanceof ClassEditPart)
            				decPos.x = decPos.x - 8;
            		} else if (naSpec.decorationFig.containsPoint(decPos.getCopy().getTranslated(3, 3)) && !(bdec.getHost() instanceof ClassEditPart)) {
            			decPos.x = decPos.x + naSpec.decorationFig.getBounds().width;
            		}
            	}
            	return decPos; 
            }
		};
		decorations.add(deleteSpec);

		// Context menu (a nav aid whose menu contains all the actions that are found 
		// in the selected item's context menu. Will make it easier for a user to 
		// realize and find the capabilities available to him and the actions 
		// that can be performed on a diagram item. 
		decorations.add(new NavAidsSpec() {
			@Override
			public void buildHandles() {
				IFigure btn = RSEContextMenuProvider.getContextMenuNavAid(getViewer());
				if (btn!=null) decorationFig.add(btn);				
			}
			@Override
			public Point getHandlesPosition(IFigure containerFig) {
				// Place in upper right corner, to right of any other nav aid
				Point decPos = containerFig.getBounds().getTopRight();
				decPos.x = Math.max(
						decPos.x, 
						firstNAS.decorationFig.getBounds().getTopRight().x);
				for (NavAidsSpec naSpec : decorations) {
					if (naSpec.decorationFig==null) continue;

					if(naSpec.equals(deleteSpec)) {
						// delete nav aid is right of every other nav aid (currently only
						// calls nav aid), so place to right of delete so this is rightmost
						Rectangle deleteBtnBounds = naSpec.decorationFig.getBounds().getCopy();
						decPos.x = deleteBtnBounds.x + deleteBtnBounds.width;
						decPos.translate(new Point(Display.getCurrent().getBounds().x, 0));
					} else if (naSpec.decorationFig.containsPoint(decPos.getCopy().getTranslated(3, 3)) && !(bdec.getHost() instanceof ClassEditPart)) {
						decPos.x = decPos.x + naSpec.decorationFig.getBounds().width;
					}
//					decPos.x = decPos.x;
				}
				return decPos; 
			}
		});

		return decorations;
	}
	
    @Override
	public List<NavAidsSpec> getMultiSelectHandlesSpecList(NavAidsEditPolicy bdec) {
		final List<NavAidsSpec> decorations = new ArrayList<NavAidsSpec> (5);

		// TODO: Add multi delete button and get it working properly
		/*decorations.add(new NavAidsSpec() {
			@Override
			public void buildHandles() {
				IFigure btn;
	            
	            btn = getReqButton(CodeUnitEditPart.this, "remove.gif", RequestConstants.REQ_DELETE, NavAidsEditPolicy.REMOVE_ALL_SELECTED);
	            if (btn != null) decorationFig.add(btn);
			}
            @Override
			public Point getHandlesPosition(IFigure containerFig) {
            	while (containerFig instanceof IFigureWithContents) {
                    if (containerFig instanceof CodeUnitFigure)
                        containerFig = ((CodeUnitFigure)containerFig).getLabel();
                    else
                    	containerFig = ((IFigureWithContents)containerFig).getContentFig();
            	}
                
                return containerFig.getBounds().getTopRight(); 
            }			
		});*/
	    return decorations;
	}
    
    public CodeUnitFigure getCodeUnitFigure() {
    	IFigure fig = getFigure();
    	while (fig instanceof IFigureWithContents) {
            if (fig instanceof CodeUnitFigure)
                return (CodeUnitFigure) fig;
            else
            	fig = ((IFigureWithContents)fig).getContentFig();
    	}
    	return null;
    }
	

	@Override
    protected void refreshVisuals() {
		// set detail level flag in CodeUnit
		if (getModel() instanceof ArtifactFragment)
			CodeUnit.detailLevel = ((ArtifactFragment) getModel()).getRootArt().getDetailLevel();
		
		ColorScheme.init();
		updateColors();
		
		CodeUnitFigure cuf = getCodeUnitFigure();
		String localLabel = getLabel();
		this.label = getLabel();
		if (cuf != null) {
			
			// change label for Anon classes
			if (RSECore.isAnonClassName(localLabel)){
				String label = RSECore.stripAnonNumber(localLabel);
				cuf.getLabel().setText(label);
			} else
				cuf.getLabel().setText(localLabel);
			
			Artifact art = getArtifact().getArt();
			ReloRdfRepository repo = getBrowseModel().getRepo();
			Image icon;
			if (getModel() instanceof UserCreatedFragment)
				icon =((UserCreatedFragment) getModel()).getIcon(repo);
			else {
				ImageDescriptor id = this.getIconDescriptor(art, art.queryType(repo));
				icon = ImageCache.calcImageFromDescriptor(id);
			}
				
			if (com.architexa.diagrams.ColorScheme.SchemeV1) {
				cuf.getLabel().setIcon(icon);
			} else {
				cuf.getLabel().setIcon(null);
				// need to add some spacing when we arent showing an icon
				cuf.getLabel().setIconDimension(new Dimension(5, 5));
			}
			PointPositionedDiagramPolicy.getLocToFig(this.getArtifact(), (Figure) this.getFigure());
		}
	}

	// implemented by individual packages/classes
	protected void updateColors() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.gef.EditPart#getCommand(org.eclipse.gef.Request)
	 */
	@Override
    public Command getCommand(Request request) {
		if (request.getType().equals(REQ_EXPAND)) {
	    	return getExpandCmd();
	    }
		if (request.getType().equals(RequestConstants.REQ_OPEN)) {

			if(getArtFrag() instanceof UserCreatedFragment)
				// user can rename his created frag by double clicking it
				return getDirectEditCommand();

			if(!(this instanceof PackageEditPart)) { // can't open package in editor
				// this should be an action, not a command, i.e. it should not go on
				// the undo/redo stack
				return new OpenInEditorCommand(getCU(), getRepo());
			}
		}
	    if (request.getType().equals(RequestConstants.REQ_OPEN) && !(this instanceof PackageEditPart)) {
	    	// this should be an action, not a command, i.e. it should not go on
			// the undo/redo stack

	    	return new OpenInEditorCommand(getCU(), getRepo());
	    }
	    if (request.getType().equals(RequestConstants.REQ_DELETE)) {
		    final CodeUnitEditPart cuep = CodeUnitEditPart.this;
		    // ignore requests if the parent will be deleted anyway
		    if (request instanceof GroupRequest && ((GroupRequest) request).getEditParts().contains(getParent())) return null;
			return new DeleteCommand(getRootController().getRootArtifact(), cuep.getArtifact(), getRootController());
	    }
	    if (request.getType().equals(REQ_REDUCE)) { 	    	
		    final CodeUnitEditPart cuep = CodeUnitEditPart.this;
	        if (cuep.currDL == cuep.getMinimalDL()) return null;
			return new Command() {
				@Override
                public void execute() {
				    cuep.suggestDetailLevelDecrease();
				}
			};
	    }
	    if (request.getType().equals(REQ_ADD)) { 	    	
		    final CodeUnitEditPart cuep = CodeUnitEditPart.this;
		    List<EditPart> selectedEPs = getGroupRequestEditParts((GroupRequest)request);;
		    CompoundCommand compoundMoveCmd = new CompoundCommand("Move Item");
		    for (EditPart ep : selectedEPs) {
			    final ArtifactFragment reqChildAF = (ArtifactFragment) ep.getModel();
			    
			    // if one of the moving EPs is a comment/rel/etc then ignore r
			    if (!(ep instanceof CodeUnitEditPart)) continue;
			    
			    EditPart srcParentEP = ((CodeUnitEditPart) ep).getParent();
			    ArtifactFragment targetAF = (ArtifactFragment) cuep.getParent().getModel();
			    ArtifactFragment srcAF = (ArtifactFragment) srcParentEP.getModel(); 
	
			    // moving to a specific spot in a compartment
			    // ignore adds for items that are not within a class or are in a different compartment
			    // these will be handled by the AbstractReloEditPart
			    if (cuep.getParent().getParent().getModel() instanceof CodeUnit 
			    		&& (targetAF.getClass() == srcAF.getClass()) 
			    		&& (((CodeUnit) cuep.getParent().getParent().getModel()).getArt().equals(((CodeUnit) srcParentEP.getParent().getModel()).getArt())
			    		&& cuep.getParent() instanceof CompartmentCUEditPart) ) {
			    	  List<ArtifactFragment> compartmentChildren = ((CompartmentedCodeUnitEditPart) cuep.getParent().getParent()).getArtifact().getShownChildren();
						int newIndex = compartmentChildren.indexOf(cuep.getArtifact());
					    int oldIndex = ((ArtifactFragment) srcParentEP.getParent().getModel()).getShownChildren().indexOf(reqChildAF);
						ArtifactFragment parentAF = ((ArtifactFragment)cuep.getParent().getParent().getModel());
						boolean move = false;
						if (!((CodeUnit) cuep.getParent().getParent().getModel()).equals(((CodeUnit) srcParentEP.getParent().getModel())))
								move = true;
						compoundMoveCmd.add(new InteriorMoveCommand(getRootController().getRootArtifact(), parentAF, reqChildAF, newIndex, oldIndex, move));
			    }
			}
			if (compoundMoveCmd.isEmpty() ) return super.getCommand(request); 
			else return compoundMoveCmd;
	    }
	 // if we are moving connections make sure they are connecting the
		// correct editparts
	    if (request.getType().equals(REQ_CONNECTION_START) || request.getType().equals(REQ_CONNECTION_END) 
	    		|| request.getType().equals(REQ_RECONNECT_SOURCE) || request.getType().equals(REQ_RECONNECT_TARGET)) {
	    	Object newConn = null;
	    	if (request instanceof CreateConnectionRequest)
	    		newConn = ((CreateRequest)request).getNewObject();
	    	if (request instanceof ReconnectRequest)
	    		newConn = ((ReconnectRequest)request).getConnectionEditPart().getModel();
	    	
	    	if (!(newConn instanceof NamedRel)) 
	    		return null;
	    	String connResource = ((NamedRel)newConn).relationRes.toString();
	    	if (connResource.contains(RSECore.namedRel.toString())) return super.getCommand(request);
	    	if (connResource.contains(RJCore.inherits.toString()) && this instanceof ClassEditPart) return super.getCommand(request);
	    	if (connResource.contains(RJCore.overrides.toString()) && this instanceof MethodEditPart) return super.getCommand(request);
	    	if (connResource.contains(RJCore.calls.toString()) && this instanceof MethodEditPart) return super.getCommand(request);
	    	
	    	return null;
	    }
		return super.getCommand(request);
	}
	
	@SuppressWarnings("unchecked")
	private static List<EditPart> getGroupRequestEditParts(GroupRequest grpReq) {
		return (List<EditPart>) grpReq.getEditParts();
	}

	protected Command getExpandCmd() {
		final CodeUnitEditPart cuep = CodeUnitEditPart.this;
		if (cuep.currDL == cuep.getMaximumDL()) return null;
		return new Command() {
			@Override
		    public void execute() {
			    //logger.info("COMMAND: Open execute start");
		        //logger.error("Opening: " + getCU());
		        //realizeParent();
		        CompoundCommand relaizeParentCmd = new CompoundCommand();
		        CodeUnitEditPart.this.realizeParent(relaizeParentCmd);
		        relaizeParentCmd.execute();
				suggestDetailLevelIncrease();
			    //logger.info("COMMAND: Open execute end");
			}
			@Override
		    public void undo() {
				suggestDetailLevelDecrease();
				logger.info("Trying to undo open request!!");
			}
		};
	}

	// Subclassing LabelDirectEditManager so that we can access its
	// commit() method in order to end an edit when the user hits Enter
	public class CULabelDirectEditManager extends LabelDirectEditManager {
		public CULabelDirectEditManager(GraphicalEditPart source, Class<TextCellEditor> editorType,
				CellEditorLocator locator, IFigure directEditFigure) {
			super(source, editorType, locator, directEditFigure);
		}
		@Override
		protected void commit() {
			super.commit();
		}
	}
	protected CULabelDirectEditManager manager;
	protected String oldName;

	protected void installDirectEditPolicy() {
		if(!(this instanceof UndoableLabelSource)) return;
		installEditPolicy(EditPolicy.DIRECT_EDIT_ROLE, 
				new AnnoLabelDirectEditPolicy((UndoableLabelSource)this, getTextChangeCmdName()));
	}
	protected String getTextChangeCmdName() {
		return "Edit Name";
	}
	protected Command getDirectEditCommand() {
		if(!(this instanceof LabelSource)) return null;
		final CodeUnitEditPart cuep = CodeUnitEditPart.this;
		EditDomain editDomain = getRoot().getViewer().getEditDomain();
		final RSEEditor editor = (RSEEditor) ((DefaultEditDomain)editDomain).getEditorPart();
		return new Command("Edit User Created Element") {
			@Override
			public void execute() {
				if (oldName == null)
					oldName = getArtFrag().getArt().queryName(getRepo());
				if (manager == null)
					manager = new CULabelDirectEditManager(cuep, TextCellEditor.class,
							new AnnoLabelCellEditorLocator(getAnnoLabelFigure()),
							getAnnoLabelFigure()) {
					@Override
					protected void commit() {
						super.commit();
						// Typing finished, so update Resource and repo with new name
						editor.rseInjectableCommentEditor.run(); 
						getArtFrag().setInstanceName(getAnnoLabelText());
					}
				};
				manager.show();
				editor.rseInjectableCommentEditor.handleTextEditing(getAnnoLabelText(), (UndoableLabelSource) cuep, manager, getAnnoLabelFigure());
			}
		};
	}
	public String getOldAnnoLabelText() {
		return oldName;
	}
	public void setOldAnnoLabelText(String oldName) {
		this.oldName = oldName;
	}
	public IFigure getAnnoLabelFigure() {
		return getCodeUnitFigure().getLabel();
	}
	public String getAnnoLabelText() {
		return getCodeUnitFigure().getLabel().getText();
	}
	public void setAnnoLabelText(String str) {
		if (str == null) return;

		if (str.contains("\n") || str.contains("\r") || str.contains("\t")) {
			// Not allowing multiple lines in text, so treating
			// user hitting Enter as the end of the edit
			str = str.replaceAll("\n", "");
			str = str.replaceAll("\r", "");
			str = str.replaceAll("\t", "");
			updateFigure(str);
			manager.commit();
			return;
		}
		updateFigure(str);
	}
	private void updateFigure(String newName) {
		((Label)getAnnoLabelFigure()).setText(newName);
		((Label)getAnnoLabelFigure()).setToolTip(new Label(newName));
	}
	
	//TODO Remove
//	private void updateResource() {
//		ReloRdfRepository repo = getRepo();
//
//		ArtifactFragment thisFrag = getArtFrag();
//		Resource oldRes = thisFrag.getArt().elementRes;
//
//		String namePreEdit = getArtFrag().getArt().queryName(repo);
//		String newName = getAnnoLabelText();
//
//		String newResString = oldRes.toString().replace(namePreEdit, newName);
//		newResString = newResString.substring(newResString.indexOf("#")+1); // removing namespace
//		Resource newRes = thisFrag.getRootArt().getBrowseModel().createResForUserCreatedFrag(newResString);
//		thisFrag.setArt(new Artifact(newRes));
//
//		repo.startTransaction();
//
//		StatementIterator iter = repo.getStatements(oldRes, null, null);
//		while(iter.hasNext()) {
//			Statement stmt = iter.next();
//			repo.addStatement(newRes, stmt.getPredicate(), stmt.getObject());
//		}
//		repo.removeStatements(oldRes, null, null);
//
//		iter = repo.getStatements(null, null, oldRes);
//		while(iter.hasNext()) {
//			Statement stmt = iter.next();
//			repo.addStatement(stmt.getSubject(), stmt.getPredicate(), newRes);
//		}
//		repo.removeStatements((Resource)null, null, oldRes);
//
//		repo.commitTransaction();
//	}

	/* (non-Javadoc)
     * @see org.eclipse.gef.EditPart#performRequest(org.eclipse.gef.Request)
     */
    @Override
    public void performRequest(Request req) {
        //System.err.println("performRequest: req.getType()= " + req.getType());
        Command command = getCommand(req);
        if (command != null) {
            if (command.canExecute()) {
                execute(command);
            }
            return;
        }

        super.performRequest(req);
    }

    /* (non-Javadoc)
	 * @see org.eclipse.gef.EditPart#refresh()
	 */
	@Override
    public void refresh() {
		//if (getModelSourceConnections().size()
		//		+ getModelTargetConnections().size() > 0) {
		//	System.err.println(this + ",s:"
		//			+ getModelSourceConnections().size() + ",t:"
		//			+ getModelTargetConnections().size());
		//}
		super.refresh();
	}

	public List<ArtifactFragment> getNonDerivedModelChildren() {
		List<ArtifactFragment> retVal = new ArrayList<ArtifactFragment> (getModelChildren());
		ListIterator<ArtifactFragment> li = retVal.listIterator();
		while (li.hasNext()) {
			if (li.next() instanceof DerivedArtifact) {
				li.remove();
			}
		}
		return retVal;
	}

    @Override
    public void buildContextMenu(IMenuManager menu) {
        super.buildContextMenu(menu);

        // Don't need javadoc and Open/Find menu entries if this is a palette frag
        if (!(this.getModel() instanceof UserCreatedFragment)) {

        	// javadoc actions:

        	IAction action = new AddJavaDocAction(this);
        	if ( !((AddJavaDocAction) action).canRun((ArtifactFragment) getModel())) 
        		action.setEnabled(false);
        	menu.appendToGroup(RSEContextMenuProvider.GROUP_RSE_TOOLS, action);

        	action = new EditJavaDocDialogAction(this, this.getRepo());
        	menu.appendToGroup(RSEContextMenuProvider.GROUP_RSE_TOOLS, action);
        	//Disable if cannot run
        	if (!((EditJavaDocDialogAction) action).canRun((ArtifactFragment) getModel()))
        		action.setEnabled(false);

        	// Put Open in Java Editor if jdt element
        	// or Find in Workspace if not jdt element
        	
			// JDT Wkspace Element: Open in a java editor
			action = new Action(JDTUISupport.getOpenInJavaEditorActionString(),
					JDTUISupport.getOpenInJavaEditorActionIcon()) {
				@Override
				public void run() {
					CodeUnitEditPart cuep = CodeUnitEditPart.this;
					JDTUISupport.openInEditor(cuep.getCU(), cuep.getRepo());
				}
			};
			
			// Otherwise: open the search dialog
			if (!RJCore.isJDTWksp(getArtFrag().getArt().elementRes)) {
				action = new Action("Find in Workspace", Activator.getImageDescriptor("icons/jcu_obj.gif")) {
					@Override
					public void run() {
						try {
							ISearchQuery query = DefaultTextSearchQueryProvider.getPreferred().createQuery(getArtFrag().getArt().queryName(getRepo()));
							InternalSearchUI searchUI = InternalSearchUI.getInstance();
							searchUI.addQuery(query);
							searchUI.runSearchInForeground(new ProgressMonitorDialog(Display.getCurrent().getActiveShell()), query, searchUI.getSearchView());
						} catch (CoreException e) {
							logger.error("Error parsing name for search", e);
							e.printStackTrace();
						}
					}
				};
			}
			menu.appendToGroup(RSEContextMenuProvider.GROUP_RSE_EDITORS, action);
		}
        
        if (!(this instanceof ClassEditPart))
        	return;
        final ClassEditPart cep = (ClassEditPart)this;
    	List<ArtifactEditPart> epList = new ArrayList<ArtifactEditPart>();
    	epList.add(cep);
        addColorAction(menu, epList);
       
    }
    
    public static String DEFAULT = "Default";
    public void addColorAction(IMenuManager menu, List<ArtifactEditPart> epList) {

    	MenuManager subMenu = new MenuManager("Highlight");
    	subMenu.add(getColorAction(ColorScheme.RED, epList));
		subMenu.add(getColorAction(ColorScheme.BLUE, epList));
		subMenu.add(getColorAction(ColorScheme.GREEN, epList));
		subMenu.add(new Separator());
		subMenu.add(getColorAction(DEFAULT, epList));

		// Highlight menu goes in the edit appearance section
		menu.appendToGroup(RSEContextMenuProvider.GROUP_RSE_EDIT_APPEARANCE, subMenu);
    }
    
    private IAction getColorAction(final String actnText, final List<ArtifactEditPart> listEP) {
    	return new Action(actnText) {
         	@Override
			public void run() {
         		CompoundCommand cmd = new CompoundCommand("Coloring: " + actnText);
         		BuildStatus.addUsage("Relo > " + cmd.getLabel());
         		for (ArtifactEditPart cep : listEP)
         			cmd.add(new ColorActionCommand(actnText, (ArtifactFragment)((EditPart)cep).getModel()));
         		
         		getViewer().getEditDomain().getCommandStack().execute(cmd);
         		refresh();
         	}
 		};
    }

    // dir is true if this is a fwd relation (Supertypes) and false for reverse relations (subtypes)
    public IAction createHierarchyAction(final String label, final CodeUnitEditPart cuep, final DirectedRel directedRel) {
    	return new Action(label) {
		    @Override
			public void run() {
				try {
					CodeUnit cu = cuep.getCU();
					Map<Artifact, ArtifactFragment> addedArtToAF = new HashMap<Artifact,ArtifactFragment>();
					
					List <CodeUnit> cuList;
					if (directedRel.isFwd)
						cuList = cu.getExtendedTypeList(cuep.getRepo());
					else 
						cuList = cu.getInheritedTypeList(cuep.getRepo());
					
					ArtifactFragment srcAF = cuep.getArtFrag();  
		    		CompoundCommand tgtCmd = new CompoundCommand(label);
		    		ReloController rc = (ReloController) cuep.getRoot().getContents();
		    		
					Predicate filter = Filters.getTypeFilter(cuep.getRepo(), RJCore.interfaceType);
			        for (final Artifact relCU : srcAF.getArt().queryArtList(getRepo(), directedRel, filter)) {
						if (!rc.canAddRel(srcAF, directedRel, relCU)) continue;
						AddNodeAndRelCmd addInterfaceCmd = new AddNodeAndRelCmd(rc, srcAF, directedRel, relCU, addedArtToAF);
						tgtCmd.add(addInterfaceCmd);
						// layout at end, not here
						// tgtCmd.add(rc.getLayoutCmd());
						((ReloDoc) rc.getRootArtifact()).showIncludedRelationships(tgtCmd, addInterfaceCmd.getNewArtFrag());
			        }
		    		for (ArtifactFragment tgtAF : cuList) {
		    			recursiveAddHierarchy(tgtCmd, cuep, srcAF, tgtAF, directedRel, addedArtToAF);
		    		}
		    		
		    		((ReloController) cuep.getRoot().getContents()).execute(tgtCmd);	
				} catch (Exception e) {
					logger.error("Unexpected exception", e);
				}
			}
		};
    }
    
	private void recursiveAddHierarchy(CompoundCommand tgtCmd, CodeUnitEditPart cuep, ArtifactFragment srcAF, ArtifactFragment tgtAF, DirectedRel directedRel, Map<Artifact, ArtifactFragment> addedArtToAF) {
		ReloController rc = (ReloController) cuep.getRoot().getContents();
		// Add Classes
		AddNodeAndRelCmd addCmd = new AddNodeAndRelCmd(rc, srcAF, directedRel, tgtAF.getArt(), addedArtToAF);
		tgtCmd.add(addCmd);
    	((ReloDoc) rc.getRootArtifact()).showIncludedRelationships(tgtCmd, addCmd.getNewArtFrag());
    	// layout at end, not here
		// tgtCmd.add(rc.getLayoutCmd());
    	
    	ArtifactFragment newSrcAF = addCmd.getNewArtFrag();
    	List <CodeUnit> cuList;
    	if (directedRel.isFwd)
			cuList = ((CodeUnit) tgtAF).getExtendedTypeList(cuep.getRepo());
    	else
    		cuList = ((CodeUnit) tgtAF).getInheritedTypeList(cuep.getRepo());
    	
    	// Add Interfaces
        Predicate filter = Filters.getTypeFilter(cuep.getRepo(), RJCore.interfaceType);
        for (final Artifact relCU : srcAF.getArt().queryArtList(getRepo(), directedRel, filter)) {
			if (!rc.canAddRel(srcAF, directedRel, relCU)) continue;
			AddNodeAndRelCmd addInterfaceCmd = new AddNodeAndRelCmd(rc, srcAF, directedRel, relCU, addedArtToAF);
			tgtCmd.add(addInterfaceCmd);
			tgtCmd.add(rc.getLayoutCmd());
			((ReloDoc) rc.getRootArtifact()).showIncludedRelationships(tgtCmd, addInterfaceCmd.getNewArtFrag());
        }
    	
    	for (ArtifactFragment newTgtAF : cuList) {
    		recursiveAddHierarchy(tgtCmd, cuep, newSrcAF, newTgtAF, directedRel, addedArtToAF);
    	}
	}

	@Override
	protected Set<URI> getFilteredPredsForAutoBrowse() {
		Set<URI> filteredPreds = super.getFilteredPredsForAutoBrowse();
		filteredPreds.add(RJCore.access);
		return filteredPreds;
	}
}
