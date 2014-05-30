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
 * Created on Mar 4, 2005
 *
 */
package com.architexa.diagrams.relo.parts;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.Predicate;
import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;

import com.architexa.diagrams.RSECore;
import com.architexa.diagrams.commands.AddNodeAndRelCmd;
import com.architexa.diagrams.model.Artifact;
import com.architexa.diagrams.model.ArtifactFragment;
import com.architexa.diagrams.model.ArtifactRel;
import com.architexa.diagrams.model.DerivedArtifact;
import com.architexa.diagrams.model.DirectedRel;
import com.architexa.diagrams.parts.ArtifactRelModificationEditPolicy;
import com.architexa.diagrams.parts.BasicRootController;
import com.architexa.diagrams.parts.NavAidsEditPart;
import com.architexa.diagrams.parts.NavAidsEditPolicy;
import com.architexa.diagrams.parts.NavAidsSpec;
import com.architexa.diagrams.relo.ReloPlugin;
import com.architexa.diagrams.relo.commands.ReparentCommand;
import com.architexa.diagrams.relo.commands.old.CreateParentCommand;
import com.architexa.diagrams.relo.modelBridge.JoinedRelType;
import com.architexa.diagrams.relo.modelBridge.ReloDoc;
import com.architexa.diagrams.ui.MultiAddCommandAction;
import com.architexa.intro.preferences.LibraryPreferences;
import com.architexa.org.eclipse.draw2d.Figure;
import com.architexa.org.eclipse.draw2d.IFigure;
import com.architexa.org.eclipse.gef.EditPart;
import com.architexa.org.eclipse.gef.commands.Command;
import com.architexa.org.eclipse.gef.commands.CompoundCommand;
import com.architexa.org.eclipse.gef.editparts.AbstractGraphicalEditPart;
import com.architexa.store.ReloRdfRepository;


/**
 * Place for functionality that is essentially an RDF Controller/EditPart
 * @author vineet
 *
 */
public abstract class ArtifactEditPart extends AbstractReloEditPart implements PropertyChangeListener, NavAidsEditPart {
    static final Logger logger = ReloPlugin.getLogger(ArtifactEditPart.class);
    
	public ArtifactFragment getArtifact() {
    	Object retVal = getModel();
    	if (retVal instanceof ArtifactFragment) { 
    		return (ArtifactFragment) retVal;
    	} else if (retVal instanceof Artifact) {
    		// @tag examine-postRearch: need to re-enable code below and clean things up
    		//logger.warn("Got Artifact expecting ArtifactFragment.", new Throwable());
        	return new ArtifactFragment((Artifact)retVal);
    	} else {
    		logger.error("Expecting Artifact got: " + retVal.getClass(), new Throwable());
            return null;
    	}
    }
    public ArtifactFragment getArtFrag() {
        return this.getArtifact();
    }

   
	@Override
	public ReloController getRootController() {
		return (ReloController) this.getRoot().getContents();
	}

    @Override
	public List<ArtifactFragment> getModelChildren() {
        return getArtifact().getShownChildren();
    }
	public void appendModelChild(ArtifactFragment child) {
		getArtifact().appendShownChild(child);
    }
	public boolean removeModelChild(Object child) {
        return getArtifact().removeShownChild(child);
    }
	public void clearModelChildren() {
		getArtifact().clearShownChildren();
    }
	public void addModelAndChild(ArtifactEditPart child) {
    	addChild(child);
        appendModelChild(child.getArtifact());
    }
	// TODO: is this implemented correctly?
    public boolean removeChildInHeirarchy(EditPart child) {
	    // support the fact that children may not be directly under the parent
	    if (child.getParent() != this && child.getParent().getModel() instanceof DerivedArtifact)
	        return ((ArtifactEditPart) child.getParent()).removeChildInHeirarchy(child);
	    else {
	        return removeModelChild(child.getModel());
	    }
	}
    // TODO: is this implemented correctly?
    public static boolean removeArtFrag(ArtifactFragment childAF) {
    	return childAF.getParentArt().removeShownChild(childAF);
    }

    @Override
    protected List<ArtifactRel> getModelSourceConnections() {
        return getArtifact().getShownSourceConnections();
    }

    @Override
    protected List<ArtifactRel> getModelTargetConnections() {
        return getArtifact().getShownTargetConnections();
    }
	
	
    // misc methods
    
    public List<AbstractGraphicalEditPart> getSelectedAGEP() {
        List<ArtifactEditPart> retList = this.getRootController().getSelectedArtifactEditParts();
        return new ArrayList<AbstractGraphicalEditPart>(retList);
    }
    public List<Artifact> listModel(ReloRdfRepository repo, DirectedRel rel, Predicate filter) {
        return this.getArtifact().getArt().queryArtList(repo, rel, filter);
    }
    public List<Artifact> showableListModel(ReloRdfRepository repo,	DirectedRel rel, Predicate filter) {
    	BasicRootController rc = getRootController();
    	List<Artifact> filteredList = listModel(repo, rel, filter);

		for (Artifact relArt : new ArrayList<Artifact>(filteredList)) {
			if (!rc.canAddRel(getArtFrag(), rel, relArt))
				filteredList.remove(relArt);
		}
		return filteredList;
	}
    public Resource getElementRes() {
        return this.getArtifact().getArt().elementRes;
    }
    public String getRelModelLabel(Object model) {
        if (!(model instanceof Artifact)) return "{err}";
        return ((Artifact)model).queryName(getRepo());
    }
	public List<NavAidsSpec> getSingleSelectHandlesSpecList(NavAidsEditPolicy policy) {
		return new ArrayList<NavAidsSpec>();
	}
	public List<NavAidsSpec> getMultiSelectHandlesSpecList(NavAidsEditPolicy policy) {
		return new ArrayList<NavAidsSpec>();
	}
	// Overridden by CodeUnitEditPart
    public IFigure getMoreButton() {
    	return new Figure();
	}
    // Overridden by CodeUnitEditPart
    public void buildNavAidMenu(List<MultiAddCommandAction> menuActions, NavAidsSpec spec, 
    		IMenuManager defaultMenu, DirectedRel rel) {
    }
	
	
	
	@Override
	public void activate() {
		try {
			super.activate();
			this.getArtifact().addPropertyChangeListener(this);
		} catch (Throwable t) {
			logger.error("Unexpected Exception", t);
		}
	}
	@Override
	public void deactivate() {
		try {
			super.deactivate();
			this.getArtifact().removePropertyChangeListener(this);
		} catch (Throwable t) {
			logger.error("Unexpected Exception", t);
		}
	}
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		this.refresh();
	}
	
    
    @Override
    protected void createEditPolicies() {
        super.createEditPolicies();
        installEditPolicy(ArtifactRelModificationEditPolicy.KEY, new ArtifactRelModificationEditPolicy());
    }
	


    
	
	/*
	 * Detail level functionality
	 */
	protected int currDL = getDefaultDL();
	
	public int getDetailLevel() {
	    return currDL;
	}


    public int getMinimalDL() {
		return 0;
	}

    public int getDefaultDL() {
		return getMinimalDL();
	}

    public int getMaximumDL() {
		return getDefaultDL()+1;
	}

	/**
     * Used for debugging purposes
	 * @param dl
	 * @return
	 */
	public String getDLStr(int dl) {
	    if (dl == getMinimalDL())
	        return "minimalDL";
	    else
	        return "unknownDL";
	}

	public void suggestDetailLevelIncrease() {
	    //logger.debug(this.getClass().getName() + ".suggestDetailLevelIncrease: " + getDLStr(currDL));
		if (currDL <= getMaximumDL()) {
			updateMembers(currDL+1);
		    currDL++;
		}
	}
	public void suggestDetailLevelDecrease() {
	    //logger.debug(this.getClass().getName() + ".suggestDetailLevelDecrease: " + getDLStr(currDL));
	    //ConsoleView.logCause(new Exception());
		if (currDL >= getMinimalDL()) {
			updateMembers(currDL-1);
		    currDL--;
		}
	}
    



	
	@Override
    protected IFigure createFigure() {
		//logger.debug("Creating figure for: " + this);

		if (getDefaultDL() - 1 < getMinimalDL()) {
		    IFigure fig = createFigure(null, getMinimalDL());
		    currDL = getMinimalDL();
			return fig;
		} else {
		    IFigure fig = createFigure(null, getDefaultDL() - 1);
		    currDL = getDefaultDL() - 1;
			return fig;
		}
	}

	
	// called to update to new detail level
	protected abstract IFigure createFigure(IFigure curFig, int newDL);

	protected void updateMembers(int newDL) {
	    if (newDL > currDL && newDL > getMinimalDL())
	        realizeChildrenArtifacts(getArtifact().getArt().queryChildrenArtifacts(getRepo()));
	    if (newDL < currDL && newDL < getMaximumDL())
	        removeChildrenArtifacts(getArtifact().getArt().queryChildrenArtifacts(getRepo()));
	}


	/*
	 * other misc. functionality
	 *
	 */

    //TODO: review
	public void realizeChildrenArtifacts(List<Artifact> lst) {
		ReloController rc = (ReloController) getRoot().getContents();

		for (Artifact child : lst) {
			rc.getRootArtifact().addVisibleArt(child);
		}
		
	}
	
	public void removeChildrenArtifacts(Collection<?> lst) {
	    //logger.debug("removeChildrenCUs: size: " + lst.size());
		for (Object child : lst) {
			AbstractGraphicalEditPart childEP = findEditPart(child);
			if (childEP != null) 
			    removeChildInHeirarchy(childEP);
		}
		
	}


	/**
     * Add menu items to only singly selected AEP's
     * 
     * TODO: find the likely better/official way to do this
	 * @param menu
	 */
	public void buildContextMenu(IMenuManager menu) {
	}

    public Action getRelAction(final String text, final DirectedRel rel, final Predicate filter) {
        return new Action(text) {
            @Override
            public void run() {
                final CompoundCommand actionCmd = new CompoundCommand();
                
                final IRunnableWithProgress op=new IRunnableWithProgress(){
    				public void run(final IProgressMonitor monitor)	throws InvocationTargetException, InterruptedException {
    					monitor.beginTask(text, IProgressMonitor.UNKNOWN);
    					Display.getDefault().asyncExec(new Runnable() {
    						public void run() {
    							 ArtifactEditPart.this.showAllDirectRelation(actionCmd, rel, filter);
    				             if (actionCmd.size() > 0) ArtifactEditPart.this.execute(actionCmd);
    						}
    					});
    				}
    			};
    			try {
					new ProgressMonitorDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell()).run(true, false, op);
				} catch (InvocationTargetException e) {
					logger.error(e.getMessage());
				} catch (InterruptedException e) {
					logger.error(e.getMessage());
				}
            }
        };
    }

    public Action getRelAction(final String text, final DirectedRel rel) {
        return getRelAction(text, rel, null);
    }

    // breakable needed in strata, so we must have it here
    public MultiAddCommandAction getShowRelAction(final BasicRootController rc, final DirectedRel rel, final Object relArt, final String relArtLbl) {
    	return getShowRelAction(rc, rel, relArt, relArtLbl, true);
    }
    public MultiAddCommandAction getShowRelAction(final BasicRootController rc, final DirectedRel rel, final Object relArt, String relArtLbl, boolean breakable) {
    	if (!(relArt instanceof Artifact)) return null;
    	MultiAddCommandAction action = new MultiAddCommandAction(relArtLbl, getRootController()) {
			@Override
			public Command getCommand(Map<Artifact, ArtifactFragment> addedArtToAFMap) {
				CompoundCommand tgtCmd = new CompoundCommand();
				AddNodeAndRelCmd addCmd = new AddNodeAndRelCmd(rc, getArtFrag(), rel, (Artifact) relArt, addedArtToAFMap);
				tgtCmd.add(addCmd);
				if (addCmd.getNewParentArtFrag() != null)
					((ReloDoc) rc.getRootArtifact()).showIncludedRelationships(tgtCmd, addCmd.getNewArtFrag());
				return tgtCmd;
			}
    	};

    	// If relo preference set to only show lib code in
    	// menus and not in diagram, disable the action
    	if (!((Artifact)relArt).isInitialized(getRepo()) && LibraryPreferences.isReloLibCodeOnlyInMenu())
    		action.setEnabled(false);

    	return action;
    }
    
    public void buildMultipleSelectionContextMenu(IMenuManager menu) {
// Do not add autoBrowse feature until better supported / documented

//        IAction action = new Action("Autobrowse") {
//            @SuppressWarnings("unchecked")
//            @Override
//            public void run() {
//                final List<ArtifactEditPart> selEP = getViewer().getSelectedEditParts();
//                CollectionUtils.filter(selEP, PredicateUtils.instanceofPredicate(ArtifactEditPart.class));
//                if (selEP.size() < 2) return;
//
//                //PlatformUI.getWorkbench().getProgressService().runInUI()
//                Job autoBrowseJob = new Job("Browsing graph") {
//                    @Override
//                    public IStatus run(IProgressMonitor monitor) {
//                        autoBrowse(selEP, monitor);
//                        return Status.OK_STATUS;
//                    }
//                };
//                //autoBrowseJob.setSystem(true);
//                autoBrowseJob.setUser(true);
//                autoBrowseJob.setPriority(Job.INTERACTIVE);
//                autoBrowseJob.schedule();
//            }
//        };
       
        // menu.appendToGroup("main", action);
    }

    // TODO: clean the threading/scheduling functionality here (may need UNDO support)
    private void autoBrowse(List<ArtifactEditPart> selEP, IProgressMonitor monitor) {
        for (ArtifactEditPart endEP : selEP) {
            for (ArtifactEditPart startEP : selEP) {
                // check start and end so that we call auto-browse only once
                if (endEP == startEP) break;

                autoBrowse(startEP.getArtifact().getArt(), endEP.getArtifact().getArt(), 5, monitor);
            }
        }
    }

    protected void autoBrowse(Artifact startArt, Artifact endArt, int maxDepth, IProgressMonitor monitor) {
        logger.info("autoBrowsing: " + startArt + " // " + endArt);
        /*
        Pseudocode:
        1] let ptr=end
        2] ptrM1 = ptr-1
        3] get rel: ptr-?->ptrM1
        4] draw ptr-[rel]->ptrM1
        5] repeat till ptr=start
      */
        // draw from end-1
        Artifact ptr = endArt;
        while (!ptr.equals(startArt)) {
            logger.info("autoBrowsing - ptr: " + ptr);
            Artifact ptrM1 = breadthFirstSearch(startArt, ptr, maxDepth, monitor);
            if (ptrM1==null) return;

            // draw links
            final Artifact currPtr = ptr;
            final Artifact currPtrM1 = ptrM1;
            PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
                public void run() {
                    try {
                        URI rel;
    
                        // forward
                        rel = getRepo().getStatement(currPtrM1.elementRes, (URI) null, currPtr.elementRes).getPredicate();
                        if (rel != null) logger.info(currPtrM1 + " --" + rel + "--> " + currPtr);
                        if (rel != null && !rel.equals(RSECore.contains)) getRootController().addRel(currPtrM1, rel, currPtr);
    
                        // reverse
                        rel = getRepo().getStatement(currPtr.elementRes, (URI) null, currPtrM1.elementRes).getPredicate();
                        if (rel != null) logger.info(currPtr + " --" + rel + "--> " + currPtrM1);
                        if (rel != null && !rel.equals(RSECore.contains)) getRootController().addRel(currPtr, rel, currPtrM1);
                    } catch (Exception e) {
                        logger.error("Unexpected error while creating drawing figures", e);
                    }
                }});
            
            ptr = ptrM1;
        }
    }
    

    /**
     * Does breadth first search and returns the Artifact corresponding to the 
     * shortest path (just before the end)
     * 
     * @param startArt
     * @param endArt
     * @return - Artifact corresponding to endArt-1
     */
    private Artifact breadthFirstSearch(Artifact startArt, Artifact endArt, int maxDepth, IProgressMonitor monitor) {
        /*
         * Pseudocode: 
         * 1] before loop, if source!=target put in queue, else return null 
         * 2] while queue !empty, i.e. the loop: 
         * 3] pop from queue, and get all connected, if !=target queue 
         * 4] else: return current, i.e. the step before the target
         */
        Set<URI> filteredPreds = getFilteredPredsForAutoBrowse();
        
        if (startArt.equals(endArt)) {
            logger.error("breadthFirstSearch should not be called with start==end", new Exception());
            return null;
        }

        // note: visible artifacts don't really matter for breadth-first search
        //  (and in reality for autobrowse)
        Set<Artifact> visitedArtifact = new HashSet<Artifact>(1000);
        
        List<Artifact> searchQueue = new ArrayList<Artifact> (100);
        searchQueue.add(startArt);
        
        // use null as a marker to indicate the completion of one level of depth
        searchQueue.add(null);
        
        int currDepth = 0;
        
        while (true) {
            Artifact searchNode = searchQueue.remove(0);
            if (searchNode == null) {
                currDepth++;
                if (currDepth >= maxDepth) {
                    logger.info("Graph not connected in depth: " + maxDepth);
                    return null;
                }
                if (searchQueue.isEmpty()) {
                    logger.info("Graph not connected");
                    return null;
                }
                monitor.worked(1);
                if (monitor.isCanceled()) return null;
                searchQueue.add(null);
                continue;
            }
            for (Artifact child : searchNode.queryConnectedArtifactsList(getRepo(), filteredPreds)) {
                if (child.equals(endArt)) return searchNode;
                if (visitedArtifact.contains(child)) continue;

                searchQueue.add(child);
                visitedArtifact.add(child);
            }
        }
        
    }

    protected Set<URI> getFilteredPredsForAutoBrowse() {
    	Set<URI> filteredPreds = new HashSet<URI>();
    	filteredPreds.add(getRepo().rdfType);
    	filteredPreds.add(RSECore.contains);
    	return filteredPreds;
    }

    public void showAllDirectRelation(CompoundCommand tgtCmd, final DirectedRel rel, Predicate filter) {
        try {
            ReloController rc = getRootController();
            Map<Artifact, ArtifactFragment> addedArtToAF = new HashMap<Artifact,ArtifactFragment>();
            for (final Artifact relCU : getArtifact().getArt().queryArtList(getRepo(), rel, filter)) {
            	if (rc.canAddRel(getArtifact(), rel, relCU)){
            		AddNodeAndRelCmd addCmd = new AddNodeAndRelCmd(rc, ArtifactEditPart.this.getArtFrag(), rel, relCU, addedArtToAF); 
            		tgtCmd.add(addCmd);
            		((ReloDoc) rc.getRootArtifact()).showIncludedRelationships(tgtCmd, addCmd.getNewArtFrag());
            	}
            }
        } catch (Throwable t) {
            logger.error("Unexpected exception", t);
        }
    }
    

	/**
	 * Ensures that all elements on a valid path (given by rel) are shown
	 * Note: src.rel.tgt && src!=tgt has to be true 
	 * @param rel - join defining valid paths
	 */
	public void showPathElements(JoinedRelType rel) {
	    Artifact curArt = getArtifact().getArt();

		if (!isActive()) {
		    log("not active");
		    return;
		}

		logBeg("showPathElements");

		ReloController rc = getRootController();
        for (ArtifactEditPart visEP : rc.getVisibleNonDerivedArtifactEditParts()) {
			Artifact visArt = visEP.getArtifact().getArt();
			if (curArt.equals(visArt)) continue;
			//ObjectPair examiningPair = new ObjectPair(this, visEP);

            for (List<Artifact> resultSetVar : rel.getPaths(getRepo(), curArt, visArt)) {
                for (Artifact resultArt : resultSetVar) {
                    rc.createOrFindArtifactEditPart(resultArt);
                }
            }

			// flip order of pair (everything else is identical
            for (List<Artifact> resultSetVar : rel.getPaths(getRepo(), visArt, curArt)) {
                for (Artifact resultArt : resultSetVar) {
                    rc.createOrFindArtifactEditPart(resultArt);
                }
            }
		}
		logEnd();
	}



    /**
     * Asserts parenthood for this AEP
     */
    public void assertParenthood() {
        CompoundCommand cc = new CompoundCommand();
        assertParenthood(cc);
        cc.execute();
    }
    
    /**
     * Asserts parenthood for this AEP
     */
    public void assertParenthood(CompoundCommand tgtCmd) {
        final ArtifactEditPart thisAEP = this;
        Artifact thisArt = thisAEP.getArtifact().getArt();
        ReloController rc = getRootController();
        
        //logger.info("assertParenthood: " + thisAEP);
        
        for (Artifact child : thisArt.queryChildrenArtifacts(thisAEP.getRepo())) {
            final ArtifactEditPart childEP = rc.findArtifactEditPart(child);
            if (childEP == null) continue;

            final ArtifactEditPart oldParentEP = (ArtifactEditPart) childEP.getParent();
            
            Command reparentCmd = new ReparentCommand(childEP, thisAEP, oldParentEP);

            if (oldParentEP instanceof ReloController) {
                // was at the top level before
                tgtCmd.add(reparentCmd);
                continue;
            }
            
            ArtifactEditPart oldParentAEP = (ArtifactEditPart) oldParentEP;

            // want to do: if (oldParentAEP == thisAEP) continue;
            //      need to do this otherwise we will have infinite loops 
            // we also need to take into account derived code units as well
            
            //@tag post-rearch-verify
            if ( oldParentAEP.getArtifact().getNonDerivedBaseArtifact().equals(thisAEP.getArtifact().getNonDerivedBaseArtifact())) {
                continue;
            }

            tgtCmd.add(reparentCmd);
        }
    }
    
    
    public void realizeParent(CompoundCommand tgtCmd) {
        //logger.info("realizeParent: " + this);
        //logger.info(this.getClass().getName() + " .realizeParent() - parent: " + parent
        //        + " / findEditPart(parent): " + findEditPart(parent));
        
        if (getModel() instanceof DerivedArtifact) return;

        final Artifact parentArt = getArtifact().getArt().queryParentArtifact(getBrowseModel().getRepo());
        if (parentArt == null) return;
        
        final ReloController rc = getRootController();

        ArtifactEditPart parentEP = (ArtifactEditPart) findEditPart(parentArt);
        if (parentEP != null) {
            parentEP.assertParenthood(tgtCmd);
            return;
        }

        if (!rc.artCreatable(parentArt)) return;                    // check if browse model will allow
        
        // parentEP = null and it is creatable
        
        tgtCmd.add(new CreateParentCommand(this, "create parent and assert parenthood", parentArt, rc));
        return;
    }

    public Resource getInstanceRes() {
    	return getArtifact().getInstanceRes();
    }
    public void setInstanceRes(Resource viewRes) {
    	getArtifact().setInstanceRes(viewRes);
    }

}
