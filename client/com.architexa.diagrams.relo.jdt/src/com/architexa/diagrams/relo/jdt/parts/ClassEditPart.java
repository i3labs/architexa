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
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.collections.PredicateUtils;
import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.IJobChangeListener;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionManager;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;
import org.openrdf.model.Resource;

import com.architexa.diagrams.RSECore;
import com.architexa.diagrams.commands.AddNodeAndRelCmd;
import com.architexa.diagrams.draw2d.FigureUtilities;
import com.architexa.diagrams.draw2d.ShadowBorder;
import com.architexa.diagrams.eclipse.gef.UndoableLabelSource;
import com.architexa.diagrams.jdt.Filters;
import com.architexa.diagrams.jdt.RJCore;
import com.architexa.diagrams.jdt.model.CodeUnit;
import com.architexa.diagrams.jdt.model.UserCreatedFragment;
import com.architexa.diagrams.jdt.ui.RSEOutlineInformationControl;
import com.architexa.diagrams.model.Artifact;
import com.architexa.diagrams.model.ArtifactFragment;
import com.architexa.diagrams.model.ColorDPolicy;
import com.architexa.diagrams.model.DirectedRel;
import com.architexa.diagrams.relo.commands.old.FindOrAddNodesAndRelCmd;
import com.architexa.diagrams.relo.figures.CodeUnitFigure;
import com.architexa.diagrams.relo.figures.GradientFigure;
import com.architexa.diagrams.relo.figures.PartialLineBorder;
import com.architexa.diagrams.relo.figures.ToolbarCompartmentFigure;
import com.architexa.diagrams.relo.jdt.ReloJDTPlugin;
import com.architexa.diagrams.relo.jdt.services.PluggableEditPartSupport;
import com.architexa.diagrams.relo.modelBridge.ReloDoc;
import com.architexa.diagrams.relo.parts.ArtifactEditPart;
import com.architexa.diagrams.relo.parts.ReloController;
import com.architexa.diagrams.relo.ui.ColorScheme;
import com.architexa.diagrams.ui.FontCache;
import com.architexa.diagrams.ui.MultiAddCommandAction;
import com.architexa.diagrams.ui.RSEContextMenuProvider;
import com.architexa.org.eclipse.draw2d.ColorConstants;
import com.architexa.org.eclipse.draw2d.Figure;
import com.architexa.org.eclipse.draw2d.FlowLayout;
import com.architexa.org.eclipse.draw2d.IFigure;
import com.architexa.org.eclipse.draw2d.Label;
import com.architexa.org.eclipse.draw2d.LineBorder;
import com.architexa.org.eclipse.draw2d.MarginBorder;
import com.architexa.org.eclipse.draw2d.ToolbarLayout;
import com.architexa.org.eclipse.draw2d.geometry.Dimension;
import com.architexa.org.eclipse.gef.commands.Command;
import com.architexa.org.eclipse.gef.commands.CompoundCommand;
import com.architexa.store.ReloRdfRepository;


/**
 * @author vineet
 *  
 */
public class ClassEditPart extends CompartmentedCodeUnitEditPart implements UndoableLabelSource {
	static final Logger logger = ReloJDTPlugin.getLogger(ClassEditPart.class);


    {
        compartmentTypes.add(RJCore.fieldType);
        compartmentTypes.add(RJCore.methodType);
        compartmentTypes.add(RJCore.interfaceType);
        compartmentTypes.add(RJCore.classType);
    }

    @Override
	public CompartmentCodeUnit generateCompartment(Resource compartmentType, ArtifactFragment compartmentParent) {
    	if (compartmentType.equals(RJCore.fieldType))
    		return new FieldCompartmentCodeUnit(compartmentParent);
    	else if (compartmentType.equals(RJCore.methodType))
    		return new MethodCompartmentCodeUnit(compartmentParent);
    	else if (compartmentType.equals(RJCore.interfaceType))
    		return new ClassCompartmentCodeUnit(compartmentParent);
    	else if (compartmentType.equals(RJCore.classType))
    		return new ClassCompartmentCodeUnit(compartmentParent);
    	else
    		return null;
    }


    // detail level
	final int labelWithPublicChildren = labelWithCompartments + 1;
	final int labelWithProtectedChildren = labelWithCompartments + 2;
	final int labelWithAllChildren = labelWithCompartments + 3;
	
	/* (non-Javadoc)
	 * @see com.architexa.diagrams.relo.parts.ArtifactEditPart#getDLStr(int)
	 */
	@Override
    public String getDLStr(int dl) {
	    if (dl == labelWithPublicChildren)
	        return "labelWithPublicChildren";
	    else if (dl == labelWithProtectedChildren)
	        return "labelWithProtectedChildren";
	    else if (dl == labelWithAllChildren)
	        return "labelWithAllChildren";
	    return super.getDLStr(dl);
	}

	public @Override int getDefaultDL() {
		//return labelWithPublicChildren;
		return labelWithCompartments;
	}

	public @Override int getMaximumDL() {
		return labelWithAllChildren;
	}

	@Override
    protected IFigure createFigure(IFigure curFig, int newDL) {
		if (curFig == null) {
			
			Label cufLbl = new Label(getLabel()+ " ", getCU().getIcon(getRepo()));
			String toolTipLabel = getLabel();
			
			// change label for Anon classes
			if (RSECore.isAnonClassName(toolTipLabel)){
				String label = RSECore.stripAnonNumber(toolTipLabel);
				cufLbl = new Label(label+ " ", getCU().getIcon(getRepo()));
			}
			
			//cufLbl.setBorder(new MarginBorder(2,0,0,0)); //@tag bug-q: this line seems to add more to the bottom than the top
			cufLbl.setBorder(new MarginBorder(2));
	        cufLbl.setFont(FontCache.dialogFontBold);
			//cufLbl.setForegroundColor(ColorConstants.white);
			//IFigure cufHdr = GradientFigure.wrap(cufLbl, true, ColorConstants.menuBackground, ColorConstants.buttonDarkest);
			//IFigure cufHdr = GradientFigure.wrap(cufLbl, true, ColorConstants.menuBackground, ColorConstants.darkGray);
			IFigure cufHdr = cufLbl;
			FigureUtilities.insertBorder(cufHdr, new PartialLineBorder(ColorScheme.classHdrBottom, 1, false, false, true, false));
			
			boolean isClass = true;
			if (getParent().getModel() instanceof ClassCompartmentCodeUnit)
				isClass = false;
			
			if (!RSECore.isInitialized(getRepo(), getElementRes()))
				curFig = new CodeUnitFigure(cufHdr, null, ColorScheme.ghostBorder, isClass);
			else curFig = new CodeUnitFigure(cufHdr, null, ColorScheme.classBorder, isClass);
			// add tooltip
			curFig.setToolTip(new Label(toolTipLabel));
			
            // right align more button
            IFigure moreFig = new Figure();
            FlowLayout layout = new FlowLayout(/*isHorizontal*/true);
            layout.setMajorAlignment(ToolbarLayout.ALIGN_BOTTOMRIGHT);
            moreFig.setLayoutManager(layout);
            curFig.add(moreFig);
            
            //curFig.setBackgroundColor(ColorConstants.green);
            //curFig.setOpaque(true);
			if (!RSECore.isInitialized(getRepo(), getElementRes()))
	            curFig = GradientFigure.wrap(curFig, true, ColorScheme.ghostBackground, ColorConstants.white);
			else curFig = GradientFigure.wrap(curFig, true, ColorScheme.classColor, ColorConstants.white);
			/*
            Figure tstFig = new Figure();
            tstFig.setOpaque(true);
            tstFig.setLayoutManager(new ToolbarLayout());
            tstFig.add(curFig);
            curFig = tstFig;
            */
            //curFig.setOpaque(false);
			
			if (com.architexa.diagrams.ColorScheme.SchemeV1) {
				if (!RSECore.isInitialized(getRepo(), getElementRes()))
					FigureUtilities.insertBorder(curFig, new ShadowBorder(ColorScheme.ghostShadow, new Dimension(5, 5)));
				else FigureUtilities.insertBorder(curFig, new ShadowBorder(ColorScheme.classShadow, new Dimension(5, 5)));
			}
			
			
		}

		return curFig;
	}
	
	@Override
	protected void updateColors() {
		if (getFigure() instanceof GradientFigure) {	
			
			if (!ColorDPolicy.isDefaultColor(getModel(), ColorScheme.classColor)) 
				return;
			GradientFigure gradFig = ((GradientFigure) getFigure());
			IFigure contentFig = gradFig.getContentFig();
			gradFig.setVertBegGradient(ColorScheme.classColor);
			
			
			Label label = ((CodeUnitFigure) contentFig).getLabel();
			contentFig.setBorder(new LineBorder(ColorScheme.classBorder, 1));
			label.setBorder(null);
			FigureUtilities.insertBorder(label, new PartialLineBorder(ColorScheme.classHdrBottom, 1, false, false, true, false));
		}
		
		if (com.architexa.diagrams.ColorScheme.SchemeV1) {
			if (getFigure().getBorder() == null) {
				if (!RSECore.isInitialized(getRepo(), getElementRes()))
					FigureUtilities.insertBorder(getFigure(), new ShadowBorder(ColorScheme.ghostShadow, new Dimension(5, 5)));
				else FigureUtilities.insertBorder(getFigure(), new ShadowBorder(ColorScheme.classShadow, new Dimension(5, 5)));
			}
		} else {
			getFigure().setBorder(null);
		}
	}
	
	@Override
	protected String getTextChangeCmdName() {
		return "Edit Class Name";
	}

    @Override
    protected String getChildrenLabel() {
        return "Members";
    }
    
    
	@Override
    protected void updateMembers(int newDL) {
        //logger.debug("updateMembers: " + getDLStr(newDL));
        super.updateMembers(newDL);
	    CodeUnit cu = getCU();
	    ReloRdfRepository repo = getRepo();

	    if (newDL >= currDL) {
		    if (newDL >= labelWithPublicChildren)
		        realizeChildrenArtifacts(cu.getArt().queryChildrenArtifacts(repo, Filters.getAccessFilter(repo, RJCore.publicAccess)));
	        if (newDL >= labelWithProtectedChildren)
		        realizeChildrenArtifacts(cu.getArt().queryChildrenArtifacts(repo, Filters.getAccessFilter(repo, RJCore.protectedAccess)));
	        if (newDL >= labelWithAllChildren)
		        realizeChildrenArtifacts(cu.getArt().queryChildrenArtifacts(repo, Filters.getAccessFilter(repo, RJCore.privateAccess)));
	    }
	    if (newDL < currDL) {
	        if (newDL < labelWithAllChildren)
	            removeChildrenArtifacts(cu.getArt().queryChildrenArtifacts(repo, Filters.getAccessFilter(repo, RJCore.privateAccess)));
	        if (newDL < labelWithProtectedChildren)
	            removeChildrenArtifacts(cu.getArt().queryChildrenArtifacts(repo, Filters.getAccessFilter(repo, RJCore.protectedAccess)));
	        if (newDL < labelWithPublicChildren)
	            removeChildrenArtifacts(cu.getArt().queryChildrenArtifacts(repo, Filters.getAccessFilter(repo, RJCore.publicAccess)));
	    }
	}
    
    
    
    // layout support for nested classes
    /*
    @Override
    public void contributeNodesToGraph(Graph graph, Subgraph sg, Map<AbstractGraphicalEditPart,Object> partsToNodesMap) {
        CompartmentEditPart nestedClassesEP = compartmentMap.get(RJCore.classType);
        if (nestedClassesEP == null || nestedClassesEP.getChildren().size() == 0) {
            contributeNodeToGraph(graph, sg, partsToNodesMap);
            return;
        }
        //contributeNodeToGraph(graph, sg, partsToNodesMap);

        try {
        // nested children: we have a subgraph embedded
        Subgraph mySG = nestedClassesEP.contributeSubgraphToGraph(graph, sg, partsToNodesMap);
        //contributeChildrenToGraph(graph, mySG, partsToNodesMap);
        for (int i = 0; i < nestedClassesEP.getChildren().size(); i++) {
            AbstractReloEditPart cuep = (AbstractReloEditPart) nestedClassesEP.getChildren().get(i);
            cuep.contributeNodesToGraph(graph, mySG, partsToNodesMap);
        }
        } catch (Throwable t) {
            logger.info("Unexepected", t);
        }
    }

    @Override
    protected void applyOwnResults(Graph graph, Map partsToCellMap) {
        CompartmentEditPart nestedClassesEP = compartmentMap.get(RJCore.classType);
        if (nestedClassesEP == null || nestedClassesEP.getChildren().size() == 0) {
            super.applyOwnResults(graph, partsToCellMap);
            return;
        }

        Cell n = (Cell) partsToCellMap.get(this);
        int extraTop = getNonNestedHeight();
        Rectangle nesedClassBounds = n.getBounds();
        nesedClassBounds.y += extraTop;
        nesedClassBounds.height -= extraTop;
        nestedClassesEP.getFigure().setBounds(n.getBounds());
        
        for (int i = 0; i < getSourceConnections().size(); i++) {
            AbstractReloRelationPart rel = (AbstractReloRelationPart) getSourceConnections().get(i);
            rel.applyGraphResults(graph, partsToCellMap);
        }
    }
    @Override
    protected void applyChildrenResults(Graph graph, Map partsToCellMap) {
        CompartmentEditPart nestedClassesEP = compartmentMap.get(RJCore.classType);
        if (nestedClassesEP == null || nestedClassesEP.getChildren().size() == 0) {
            return;
        }
        for (int i = 0; i < nestedClassesEP.getChildren().size(); i++) {
            AbstractReloEditPart part = (AbstractReloEditPart)nestedClassesEP.getChildren().get(i);
            part.applyGraphResults(graph, partsToCellMap);
        }
    }


    @Override
    protected Insets contributeSubgraghInsets() {
        // only used when we have nested children
        Insets insets = super.contributeSubgraghInsets();
        insets.top += getNonNestedHeight();
        //return insets;
        return insets
                .getAdded(GraphLayoutManager.PADDING)
                .getAdded(GraphLayoutManager.PADDING)
                .getAdded(GraphLayoutManager.PADDING);

    }
    protected int getNonNestedHeight() {
        int extraTop = 0;
        CompartmentEditPart nestedClassesEP = compartmentMap.get(RJCore.classType);
        for (CompartmentEditPart cep : compartmentMap.values()) {
            if (cep == nestedClassesEP) continue;
            extraTop += cep.getFigure().getBounds().height;
        }
        return extraTop;
    }
    */
    
    
    
	int MAX_LENGTH = 15;
	private boolean isHighlighted;
    @Override
    public void buildContextMenu(IMenuManager menu) {
        
	    final CodeUnitEditPart cuep = this;
        final CodeUnit cu = cuep.getCU();
		IAction action;
		
		if (!(cu instanceof UserCreatedFragment)) {
			if(getRepo().hasStatement(cu.getArt().elementRes, RJCore.inherits, null)) {
				action = cuep.createHierarchyAction("Show Supertype Hierarchy", cuep, DirectedRel.getFwd(RJCore.inherits));
				menu.appendToGroup(RSEContextMenuProvider.GROUP_RSE_TOOLS, action);
			}
			if(getRepo().hasStatement(null, RJCore.inherits, cu.getArt().elementRes)) {
				action = cuep.createHierarchyAction("Show Subtype Hierarchy", cuep, DirectedRel.getRev(RJCore.inherits));
				menu.appendToGroup(RSEContextMenuProvider.GROUP_RSE_TOOLS, action);
			}
		}
//		action = new Action("Show Classes being Extended") {
//		    @Override
//            public void run() {
//		        try {
//		    		ReloController rc = (ReloController) cuep.getRoot().getContents();
//		    		CodeUnit supTypeCU = cu.getExtendedTypeCU(cuep.getRepo());
//		    		ArtifactFragment supTypeBaseAF = cuep.getArtFrag();  
//		    		CompoundCommand tgtCmd = new CompoundCommand();
//		    		Map<Artifact, ArtifactFragment> addedArtToAF = new HashMap<Artifact, ArtifactFragment>();
//		    		while (supTypeCU != null) {	
//		    			AddNodeAndRelCmd addCmd = new AddNodeAndRelCmd(rc, supTypeBaseAF, new DirectedRel(RJCore.inherits, true), supTypeCU.getArt(), addedArtToAF);
//		    			tgtCmd.add(addCmd);
//		            	((ReloDoc) rc.getRootArtifact()).showIncludedRelationships(tgtCmd, addCmd.getNewArtFrag());
//		            	tgtCmd.add(rc.getLayoutCmd());
//		            	
//		            	ArtifactFragment supTypeAF = addCmd.getNewArtFrag();
//		                supTypeCU = supTypeCU.getExtendedTypeCU(cuep.getRepo());
//                        supTypeBaseAF =  supTypeAF;
//		    		}
//		    		rc.execute(tgtCmd);
//		    		
//                } catch (Exception e) {
//                    logger.error("Unexpected exception", e);
//                }
//		    }};
//		menu.appendToGroup("main", action);
//        menu.appendToGroup("main", cuep.getRelAction("Show Extending Classes", DirectedRel.getRev(RJCore.inherits)));
//        menu.appendToGroup("main", cuep.getRelAction(
//                "Show Interfaces being Implemented", 
//                DirectedRel.getFwd(RJCore.inherits), 
//                Filters.getTypeFilter(cuep.getRepo(), RJCore.interfaceType)));
//        menu.appendToGroup("main", cuep.getRelAction("Show Referencing Types+Methods", DirectedRel.getRev(RJCore.refType), getPredicateForShowRef()));
       addRefMenu(menu, cuep);
       
		action = new Action("Show Containing Package") {
		    @Override
            public void run() {
                CompoundCommand actionCmd = new CompoundCommand();
                cuep.realizeParent(actionCmd);
                if (actionCmd.size() > 0) ClassEditPart.this.execute(actionCmd);
		}};
//		menu.appendToGroup("main", action);
        
//        action = new Action("Hide Class") {
//            @Override
//            public void run() {
//                CompoundCommand actionCmd = new CompoundCommand();
//                actionCmd.add(new HideCommand(classCUEP));
//                //cuep.realizeParent(actionCmd, /*inferring*/ false);
//                ClassEditPart.this.execute(actionCmd);
//            }
//        };
        // disabled: until this code and it and its undo has been tested.
        // menu.appendToGroup("main", action);

        // Calling super last because this will create the java doc actions and 
        // we want them at the bottom of the context menu group not the beginning

        // add separator before java doc actions
        menu.appendToGroup(RSEContextMenuProvider.GROUP_RSE_TOOLS, new Separator());
        super.buildContextMenu(menu);
	}

    private void addRefMenu(IContributionManager menu, ArtifactEditPart cuep) {
    	if (cuep.getModel() instanceof UserCreatedFragment) return;

    	final DirectedRel rel = DirectedRel.getRev(RJCore.refType);
    	final ReloController rc = getRootController();
    	List<Artifact> artifactsToAddList = new ArrayList<Artifact>(getArtifact().getArt().queryArtList(getRepo(), rel, getPredicateForShowRef()));

    	// Menu of classes and interfaces that reference this class
    	addReferencingMenu(menu, artifactsToAddList, rel, rc, "Types");

    	// Menu of methods that reference this class
//    	addReferencingMethodsMenu(menu, cuep, artifactsToAddList, rel, rc);
    	addReferencingMenu(menu, artifactsToAddList, rel, rc, "Methods");
    }


	/*
     * Submenu that contains an alphabetical list of the types (classes and interfaces)
     * that reference this class. Selecting an item in the list adds it to the diagram.
     * 
     * If nothing references, this menu will not appear in the context menu.
     */
    private void addReferencingMenu(IContributionManager menu, 
    		final List<Artifact> referencingMembers, final DirectedRel rel, final ReloController rc, final String typeOfReferences) {

    	final String menuTitle = "Show Referencing "+typeOfReferences+"...";
		//    	MenuManager showRefTypeMenu = new MenuManager("Show Referencing Types");
    	menu.appendToGroup(RSEContextMenuProvider.GROUP_RSE_TOOLS, new Action(menuTitle) {
    		final String[] filters = new String[] {
					RSEOutlineInformationControl.FILTERS_ACCESS_LEVEL, 
					RSEOutlineInformationControl.FILTERS_MEMBER_KIND};
    		List<MultiAddCommandAction> actions = new ArrayList<MultiAddCommandAction>();
    		@Override
    		public void run() {
    			Job runWithPM = new Job(menuTitle) {
					@Override
					protected IStatus run(final IProgressMonitor monitor) {
						monitor.setTaskName("Finding Referenced Types in Workspace...");
						monitor.beginTask("", referencingMembers.size()*2);
						if (typeOfReferences.equals("Types"))
							actions = getRefTypesActions(referencingMembers, rel, rc, monitor);
						else if (typeOfReferences.equals("Methods"))
							actions = getRefMethodActions(referencingMembers, rel, rc, monitor);
						return Status.OK_STATUS;
					}
					
				};
				runWithPM.setUser(true);
				runWithPM.setSystem(false);
				runWithPM.addJobChangeListener(new IJobChangeListener() {
					public void sleeping(IJobChangeEvent event) {}
					public void scheduled(IJobChangeEvent event) {}
					public void running(IJobChangeEvent event) {}
					public void done(IJobChangeEvent event) {
						Display.getDefault().asyncExec(new Runnable() {
							public void run() {
								ClassEditPart.this.createInformationControlMenu(menuTitle,
				    	    			actions, null, filters, getFigure());
							}
						});
					}
					public void awake(IJobChangeEvent event) {}
					public void aboutToRun(IJobChangeEvent event) {}
				});
				runWithPM.schedule();
    		}
		});
    }

    protected List<MultiAddCommandAction> getRefTypesActions(List<Artifact> referencingMembers, final DirectedRel rel, final ReloController rc) {
    	return getRefTypesActions(referencingMembers, rel, rc, new NullProgressMonitor());
    }
    
    
    private List<MultiAddCommandAction> getRefMethodActions(List<Artifact> referencingMembers, final DirectedRel rel, final ReloController rc, IProgressMonitor monitor) {
    	Collections.sort(referencingMembers, new Comparator<Artifact>() {
    		public int compare(Artifact o1, Artifact o2) {
    			String firstName = getRefCallerName(o1.toString());
    			String secondName = getRefCallerName(o2.toString());
    			return firstName.compareToIgnoreCase(secondName);
    		}
    	});
    	List<MultiAddCommandAction> refMethodsActions = new ArrayList<MultiAddCommandAction>();
    	List<Artifact> tempArtList = new ArrayList<Artifact>(referencingMembers);
    	Iterator<Artifact> itr = tempArtList.iterator();
    	while (!referencingMembers.isEmpty()) {
    		int i = 0;

    		MultiAddCommandAction action1;
    		while (i < MAX_LENGTH && itr.hasNext()) {
    			// Add action for each artifact
    			final Artifact relCU = itr.next();
    			i++;

    			referencingMembers.remove(relCU);
    			action1 = new MultiAddCommandAction(getRefCallerName(relCU.toString()), getRootController()) {
    				@Override
    				public Command getCommand(Map<Artifact, ArtifactFragment> addedArtToAFMap) {
    					CompoundCommand tgtCmd = new CompoundCommand();
    					AddNodeAndRelCmd addCmd = new AddNodeAndRelCmd(rc, getArtFrag(), rel, (Artifact) relCU, addedArtToAFMap);
    					tgtCmd.add(addCmd);
    					if (addCmd.getNewParentArtFrag() != null)
    						((ReloDoc) rc.getRootArtifact()).showIncludedRelationships(tgtCmd, addCmd.getNewArtFrag());
    					return tgtCmd;
    				}
    			};
    			try {
	    			ImageDescriptor des = rc.getIconDescriptor(this, relCU);
	    			if (des != null)
	    				action1.setImageDescriptor(des);
	    		} catch (Throwable t) {
	    			ClassEditPart.logger.error("Unexpected error while getting icon for: " + rel, t);
	    		}
				refMethodsActions.add(action1);
    		}
    	}
    	return refMethodsActions;
	}
    
    protected List<MultiAddCommandAction> getRefTypesActions(List<Artifact> referencingMembers, final DirectedRel rel, final ReloController rc, IProgressMonitor monitor) {

    	ReloRdfRepository repo = getRepo();
    	List<Artifact> referencingTypes = new ArrayList<Artifact>();
    	for(Artifact art : referencingMembers) {
    		monitor.worked(1);
    		Artifact ref = art;
    		for(Resource refType=ref.queryType(repo); ref!=null && 
    		!RJCore.classType.equals(refType) && !RJCore.interfaceType.equals(refType);) {
    			// find class or interface that makes the reference
    			ref = art.queryParentArtifact(repo);
    			if (ref==null) continue;
    			refType = ref.queryType(repo);
    		}
    		if(ref!=null && !referencingTypes.contains(ref)) referencingTypes.add(ref);
    	}
    	// sort them alphabetically
    	Collections.sort(referencingTypes, new Comparator<Artifact>() {
    		public int compare(Artifact o1, Artifact o2) {
    			return o1.toString().compareToIgnoreCase(o2.toString());
    		}
    	});

    	// create action so that selecting a reference 
    	// from the menu will add it to the diagram
    	final List<MultiAddCommandAction> addActions = new ArrayList<MultiAddCommandAction>();
    	for(final Artifact ref : referencingTypes) {
    		monitor.worked(1);
    		IAction action = new MultiAddCommandAction(ref.queryName(repo), getRootController()) {
    			@Override
    			public Command getCommand(Map<Artifact, ArtifactFragment> addedArtToAFMap) {
    				CompoundCommand tgtCmd = new CompoundCommand();
    				AddNodeAndRelCmd addCmd = new AddNodeAndRelCmd(rc, getArtFrag(), rel, ref, addedArtToAFMap);
    				tgtCmd.add(addCmd);
    				if (addCmd.getNewParentArtFrag() != null)
    					((ReloDoc) rc.getRootArtifact()).showIncludedRelationships(tgtCmd, addCmd.getNewArtFrag());
    				return tgtCmd;
    			}
    		};
//    		showRefTypeMenu.add(action);
    		
    		try {
    			ImageDescriptor des = rc.getIconDescriptor(this, ref);
    			if (des != null)
    				action.setImageDescriptor(des);
    		} catch (Throwable t) {
    			ClassEditPart.logger.error("Unexpected error while getting icon for: " + ref, t);
    		}
    		addActions.add((MultiAddCommandAction)action);
    	}
    	return addActions;
    }


    private String getRefCallerName(String refStr) {
    	return refStr.substring(refStr.lastIndexOf(".")+1);
	}

	@Override
    public void buildMultipleSelectionContextMenu(IMenuManager menu) {
    	super.buildMultipleSelectionContextMenu(menu);

    	// TODO: get this working on methods/ packages also     test for ALL cases
    	IAction interactionAction = new Action("Show Interactions") {
    		@SuppressWarnings("unchecked")
    		@Override
    		public void run() {
    			List<ArtifactEditPart> selEPs = getViewer().getSelectedEditParts();
    			CollectionUtils.filter(new ArrayList<ArtifactEditPart>(selEPs), PredicateUtils.instanceofPredicate(ClassEditPart.class));
    			if (selEPs.size() < 2) return;

    			List<Resource> selResources = new ArrayList<Resource>();
    			for(ArtifactEditPart selEP : selEPs) {
    				selResources.add(selEP.getElementRes());
    			}

    			final ReloController rc = getRootController();
    			CompoundCommand addedInteractionsCC = new CompoundCommand("Show Interactions");
	
    			for(ArtifactEditPart aep : selEPs) {
    				for(Artifact child : aep.getArtifact().getArt().queryChildrenArtifacts(getRepo())) {
    					for(Artifact calledArt : child.queryArtList(getRepo(), DirectedRel.getFwd(RJCore.calls))) {
    						Artifact parentOfCalledArt = calledArt.queryParentArtifact(getRepo());
    						if(parentOfCalledArt==null
    								|| aep.getElementRes().equals(parentOfCalledArt.elementRes) // call to same class (not showing these)
    								|| !selResources.contains(parentOfCalledArt.elementRes)) // call not made to a selected class 
    						{
    							continue;
    						}
     						addedInteractionsCC.add(new FindOrAddNodesAndRelCmd(rc, child, RJCore.calls, calledArt));
    					}
    				}
    			}
    			rc.execute(addedInteractionsCC);
    		
    		
    			
    		}
    		
    	};
    	menu.appendToGroup(RSEContextMenuProvider.GROUP_RSE_TOOLS, interactionAction);
    	
    	
    	final List<ArtifactEditPart> selEPs = getViewer().getSelectedEditParts();
    	if (!allHighlightable(selEPs)) 
    		return;
    		
    	addColorAction(menu, selEPs);
    }
    
    public boolean allHighlightable(List<ArtifactEditPart> selEPs) {
    	for (ArtifactEditPart part : selEPs) {
			if (!(part instanceof ClassEditPart))
				return false;
    	}
    	return true;
    }
    
    
    
    public boolean isHighlighted() {
		return isHighlighted;
	}
	
	@Override
	public void colorFigure(Color color) {
		if (color == null)
			color = ColorScheme.classColor;
		IFigure fig = getFigure();
		if (fig instanceof GradientFigure) {
			((GradientFigure)fig).setVertBegGradient(color);
			fig.repaint();
		}
	}
	
	private static abstract class ArtifactCompartmentEditPart extends CompartmentCUEditPart {
        @Override
        public String getLabel(Artifact art, Artifact contextArt) {
            return CodeUnit.getLabel(getRepo(), art, contextArt);
        }
        @Override
        public ImageDescriptor getIconDescriptor(Artifact art, Resource resType) {
            return PluggableEditPartSupport.getIconDescriptor(getRepo(), art, resType);
        }
    }
    private static abstract class ToolbarCompartmentEditPart extends ArtifactCompartmentEditPart {
		@Override
        protected IFigure createFigure(IFigure curFig, int newDL) {
			if (curFig == null) {
				curFig = new ToolbarCompartmentFigure();
			}
			return curFig;
		}
		@Override
        public void suggestDetailLevelIncrease() {
			if (currDL == getMinimalDL()) {
				// @tag post-rearch-verify
				realizeChildrenArtifacts(getArtifact().getNonDerivedBaseArtifact().queryChildrenArtifacts(getRepo(), Filters.getAccessFilter(getRepo(), RJCore.publicAccess)));
				currDL++;
			}
		}
	}

	private final static class MethodCompartmentCodeUnit extends CompartmentCodeUnit {
        public MethodCompartmentCodeUnit(ArtifactFragment cu) {
            super(cu);
        }
        @Override
        public void registerTypes(Map<Resource, CompartmentCodeUnit> compartmentMap) {
            compartmentMap.put(RJCore.methodType, this);
        }

        @Override
        public ArtifactEditPart createController() {
            ArtifactEditPart methodCompartment = new ToolbarCompartmentEditPart() {
                @Override
                protected String getChildrenLabel() {
                    return "Methods";
                }
        		@Override
                public String toString() {
        			return "methodCompartment" + tag.getTrailer();
        		}
            };
            return methodCompartment;
        }

        @Override
        public List<Artifact> queryChildrenArtifacts(ReloRdfRepository repo, Predicate filterPred) {
            if (filterPred == null) filterPred = PredicateUtils.truePredicate();
        	return super.queryChildrenArtifacts(repo, PredicateUtils.andPredicate(
        	        filterPred,
        	        Filters.getTypeFilter(repo, RJCore.methodType)
        	        ));
        }
    }

    private static final class FieldCompartmentCodeUnit extends CompartmentCodeUnit {
        public FieldCompartmentCodeUnit(ArtifactFragment cu) {
            super(cu);
        }
        @Override
        public void registerTypes(Map<Resource, CompartmentCodeUnit> compartmentMap) {
            compartmentMap.put(RJCore.fieldType, this);
        }

        @Override
        public ArtifactEditPart createController() {
            ArtifactEditPart attributeCompartment = new ToolbarCompartmentEditPart() {
                @Override
                protected String getChildrenLabel() {
                    return "Fields";
                }
        		@Override
                public String toString() {
        			return "fieldsCompartment" + tag.getTrailer();
        		}
            };
            return attributeCompartment;
        }

        @Override
        public List<Artifact> queryChildrenArtifacts(ReloRdfRepository repo, Predicate filterPred) {
            if (filterPred == null) filterPred = PredicateUtils.truePredicate();
        	return super.queryChildrenArtifacts(repo, PredicateUtils.andPredicate(
        	        filterPred,
        	        Filters.getTypeFilter(repo, RJCore.fieldType)
        	        ));
        }
    }


    private static final class ClassCompartmentCodeUnit extends CompartmentCodeUnit {
        public ClassCompartmentCodeUnit(ArtifactFragment cu) {
            super(cu);
        }
        @Override
        public void registerTypes(Map<Resource, CompartmentCodeUnit> compartmentMap) {
            compartmentMap.put(RJCore.classType, this);
            compartmentMap.put(RJCore.interfaceType, this);
        }

        @Override
        public ArtifactEditPart createController() {
            ArtifactEditPart attributeCompartment = new ArtifactCompartmentEditPart() {
                @Override
                protected String getChildrenLabel() {
                    return "Class";
                }
                @Override
                public String toString() {
                    return "classCompartment" + tag.getTrailer();
                }
                /*
                @Override
                protected IFigure createFigure(IFigure curFig, int newDL) {
                    if (curFig == null) {
                        curFig = new Figure();
                        curFig.setLayoutManager(new GraphLayoutManager.SubgraphLayout());
                    }
                    return curFig;
                }
                */
                @Override
                protected IFigure createFigure(IFigure curFig, int newDL) {
                    if (curFig == null) {
                        curFig = new ToolbarCompartmentFigure();
                    }
                    return curFig;
                }
            };
            return attributeCompartment;
        }

        @Override
        public List<Artifact> queryChildrenArtifacts(ReloRdfRepository repo, Predicate filterPred) {
            if (filterPred == null) filterPred = PredicateUtils.truePredicate();
            return super.queryChildrenArtifacts(repo, PredicateUtils.andPredicate(
                    filterPred,
                    PredicateUtils.orPredicate(
                    		Filters.getTypeFilter(repo, RJCore.classType),
                    		Filters.getTypeFilter(repo, RJCore.interfaceType)
                            )
                    ));
        }
    }
    
    protected Predicate getPredicateForShowRef() {
    	Predicate refPred = new Predicate() {
    		Resource classRes = getElementRes();
    		public boolean evaluate(Object arg0) { // arg0 is a method that references this Type
    			if(!(arg0 instanceof Resource)) return true;

    			// Don't show references from a
    			// method that this class contains
    			Resource parent = (Resource) arg0;
    			while(parent!=null) {
    				if(classRes.equals(parent)) return false;
    				parent = (Resource) getRepo().getStatement((Resource)null, RSECore.contains, parent).getSubject();
    			}
    			return true;
    		}
    	};
    	return refPred;
    }

}