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
 * Created on Jan 26, 2005
 *
 */
package com.architexa.diagrams.parts;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.openrdf.model.Resource;

import com.architexa.diagrams.Activator;
import com.architexa.diagrams.RSECore;
import com.architexa.diagrams.UserTick;
import com.architexa.diagrams.eclipse.gef.MenuButton;
import com.architexa.diagrams.utils.MoreButtonUtils;
import com.architexa.org.eclipse.draw2d.Figure;
import com.architexa.org.eclipse.draw2d.FigureListener;
import com.architexa.org.eclipse.draw2d.IFigure;
import com.architexa.org.eclipse.draw2d.SimpleRaisedBorder;
import com.architexa.org.eclipse.draw2d.ToolbarLayout;
import com.architexa.org.eclipse.draw2d.geometry.Point;
import com.architexa.org.eclipse.draw2d.geometry.Rectangle;
import com.architexa.org.eclipse.gef.ContextMenuProvider;
import com.architexa.org.eclipse.gef.EditPolicy;
import com.architexa.org.eclipse.gef.LayerConstants;
import com.architexa.org.eclipse.gef.commands.Command;
import com.architexa.org.eclipse.gef.commands.CommandStack;
import com.architexa.org.eclipse.gef.commands.CommandStackEvent;
import com.architexa.org.eclipse.gef.commands.CommandStackEventListener;
import com.architexa.org.eclipse.gef.editparts.AbstractGraphicalEditPart;
import com.architexa.org.eclipse.gef.editpolicies.SelectionEditPolicy;


/**
 * @author vineet
 * 
 * Shows toolbar on mouse-select (prev. version showed toolbar on mouse-over)
 * 
 * NOTE: NavAids are added to the SCALED_FEEDBACK_LAYER. To use, when the figure
 * of the ScalableRootEditPart is created, the following line needs to be added
 * to enable the layer:
 *    getLayer(LayerConstants.SCALED_FEEDBACK_LAYER).setEnabled(true);
 */
public class NavAidsEditPolicy extends SelectionEditPolicy {
    /*
     * Note: We use SCALED_FEEDBACK_LAYER instead of HANDLE_LAYER (as provided
     * by SelectionHandlesEditPolicy). This was done, because we needed scaling
     * to work, and the feedback layer scales automatically. We need to figure
     * out which is the right one.
     * 
     * To use the scaled feedback layer We therefore also modify ReloEditor
     * .configureGraphicalViewer to enable the layer
     */

    static final Logger logger = Activator.getLogger(NavAidsEditPolicy.class);

    public final static String HANDLES_ROLE = "HandlesEditPolicy";

    // labels for the commands
    public final static String REMOVE_NODE = "Remove Node";
    public final static String REMOVE_ALL_SELECTED = "hide all selected";
    
    public NavAidsEditPolicy() {}
    
    protected List<NavAidsSpec> navAidsSingleSpec = new ArrayList<NavAidsSpec> (5);
    protected List<NavAidsSpec> navAidsMultiSpec = new ArrayList<NavAidsSpec> (1);
    protected List<IFigure> instantiatedNavAids;
    private Command multiRemoveCmd;
    
    protected NavAidsEditPart getArtifactSelectionHandlesHost() {
        return (NavAidsEditPart) getHost();
    }
    
    @Override
    public void activate() {
        super.activate();
        navAidsSingleSpec = getArtifactSelectionHandlesHost().getSingleSelectHandlesSpecList(this);
        navAidsMultiSpec = getArtifactSelectionHandlesHost().getMultiSelectHandlesSpecList(this);
    }

    private void onSelectionDump() {
    	if (!getHost().isActive()) return;

    	Resource selRes = getArtifactSelectionHandlesHost().getElementRes();
		getArtifactSelectionHandlesHost().getRepo().dumpStatements(selRes, null, null);
		//getArtifactSelectionHandlesHost().getRepo().dumpStatements(null, null, selRes);
    }
    
    @Override
    protected void showSelection() {
        UserTick.logger.info("sel: " + getHost().getModel() + " {{ " + getHost().getClass() + "/" + getHost().getModel().getClass());
    	if (RSECore.dumpStatementsOnMouseSelection) onSelectionDump();
        //onSelectionDump();
        if (getHost().isActive()) {
        	List<AbstractGraphicalEditPart> selected = getSelectedNavAidsEditParts();
        	if(selected.size()>1) {
            	addMultiNavAids(selected);
        	} else {
        		addNavAids(false, true);
        	}
        }
    }

	@Override
    protected void hideSelection() {
        try {
        	UserTick.logger.info("Remove sel: " + getHost().getModel() + " {{ " + getHost().getClass() + "/" + getHost().getModel().getClass());
            if (getHost().isActive()) removeNavAids();
            // @tag glued-bug: the check for isActive should not be needed. Need
            // to debug why in some cases the listened is not being removed. The
            // problem is that the bug is not really reproducible.
            // 
            // It seems to happen after we show nav aids and then delete and
            // then break, the break causes a command to get fired which calls
            // here.
            //
            // Likely can debug by polling at every 5s and checking in which
            // cases is the listener still tracking.
        } catch (Throwable t) {
            logger.error("Unexpected exception. Hiding: " + this.getHost(), t);
        }
    }

    private CommandStackEventListener handlesUpdater = new CommandStackEventListener() {
        public void stackChanged(CommandStackEvent event) {
            switch (event.getDetail()) {
            case CommandStack.PRE_EXECUTE:
            case CommandStack.PRE_REDO:
            case CommandStack.PRE_UNDO:
                break;
            case CommandStack.POST_EXECUTE:
            case CommandStack.POST_REDO:
            case CommandStack.POST_UNDO:
            	Display.getDefault().asyncExec(new Runnable() {
					public void run() {
		                hideSelection();
		                showSelection();
					}});
                break;
            }
        }
    };

	private IFigure moreButton;

    private void addMultiNavAids(List<AbstractGraphicalEditPart> selectedEP) {
		boolean firstEP = true;
		for (AbstractGraphicalEditPart ep : selectedEP) {
			EditPolicy edPolicy = ep.getEditPolicy(HANDLES_ROLE);
			if (!(edPolicy instanceof NavAidsEditPolicy)) continue;
			
			NavAidsEditPolicy policy = (NavAidsEditPolicy) edPolicy;
			policy.addNavAids(/* multi */true, /* show */firstEP);

			if (firstEP) firstEP = false;
		}
	}

    protected void addNavAids(boolean multi, boolean show) {
    	try {
            removeNavAids();
            IFigure layer = getLayer(LayerConstants.SCALED_FEEDBACK_LAYER);
            instantiatedNavAids = createNavAids(multi, show);
            for (int i = 0; i < instantiatedNavAids.size(); i++)
            	layer.add((IFigure)instantiatedNavAids.get(i));
            
//            getHost().getViewer().getEditDomain().getCommandStack().removeCommandStackEventListener(handlesUpdater);
            getHost().getViewer().getEditDomain().getCommandStack().addCommandStackEventListener(handlesUpdater);
            //System.err.println("ANAEP->pos: " + this.getHostFigure().getBounds());
    	} catch (Throwable t) {
    		logger.error("Unexpected Exception", t);
    	}
    }

    protected void removeNavAids() {
        if (instantiatedNavAids == null) return;
        IFigure layer = getLayer(LayerConstants.SCALED_FEEDBACK_LAYER);
        for (int i = 0; i < instantiatedNavAids.size(); i++)
            layer.remove((IFigure)instantiatedNavAids.get(i));
        instantiatedNavAids = null;

        getHost().getViewer().getEditDomain().getCommandStack().removeCommandStackEventListener(handlesUpdater);
    }
    
    protected List<IFigure> createNavAids(boolean multi, boolean show) {
    	List<IFigure> retVal = new ArrayList<IFigure> (navAidsSingleSpec.size() + navAidsMultiSpec.size());
		NavAidsSpec firstNAS = null;
    	if (multi) {
    		if (!navAidsMultiSpec.isEmpty()) firstNAS = navAidsMultiSpec.get(0);
    		for (NavAidsSpec ds : navAidsMultiSpec) {
        		ds.navAidsPolicy = this;
    			ds.firstNAS = firstNAS;
        		IFigure decorationFig = getMultiHandle(ds, show);
        		if (decorationFig.getChildren().size()>0 && show) retVal.add(decorationFig);
			}
        } else {
        	// add more button if single item selected
    		moreButton = ((NavAidsEditPart) getHost()).getMoreButton();
    		retVal.add(moreButton);
    		if (!navAidsSingleSpec.isEmpty()) firstNAS = navAidsSingleSpec.get(0);
    		for (NavAidsSpec ds : navAidsSingleSpec) {
    			ds.navAidsPolicy = this;
    			ds.firstNAS = firstNAS;
    			IFigure decorationFig = getSingleHandle(ds);
    			if (decorationFig.getChildren().size() > 0) retVal.add(decorationFig);
			}
    	} 	
        return retVal;
    }


    private IFigure getSingleHandle(final NavAidsSpec hs) {
        hs.decorationFig = new Figure();
        hs.buildHandles();

        final IFigure hostFig = getHostFigure();
        hs.decorationFig.setOpaque(true);
        hs.decorationFig.setBorder(new SimpleRaisedBorder());
        hs.decorationFig.setFont(JFaceResources.getDialogFont());
        hs.decorationFig.setLayoutManager(new ToolbarLayout(true));

        Point topLeft = hs.getHandlesPosition(hostFig);
       	hs.decorationFig.setBounds(new Rectangle(topLeft, hs.decorationFig.getPreferredSize()));
       	if (moreButton.intersects(hs.decorationFig.getBounds())) hs.decorationFig.getBounds().y += moreButton.getBounds().height;
       	hostFig.addFigureListener(new FigureListener() {
            public void figureMoved(IFigure source) {
                Point topLeft = hs.getHandlesPosition(hostFig);
                hs.decorationFig.setLocation(topLeft);
            }
        });

        return hs.decorationFig;
    }
    
    private IFigure getMultiHandle(final NavAidsSpec hs, boolean show) {
    	hs.decorationFig = new Figure();
        hs.buildHandles();
        
        hs.decorationFig.setOpaque(true);
        hs.decorationFig.setBorder(new SimpleRaisedBorder());
        hs.decorationFig.setFont(JFaceResources.getDialogFont());
        hs.decorationFig.setLayoutManager(new ToolbarLayout(true));
    	
        List<AbstractGraphicalEditPart> selected = getSelectedNavAidsEditParts();
        int maxX = 0;
        int minY = Integer.MAX_VALUE;
    	for(AbstractGraphicalEditPart part : selected) {
    		final IFigure hostFig = part.getFigure();
        	Point topLeft = hostFig.getBounds().getTopRight();
    		int x = topLeft.x;
    		int y = topLeft.y;
    		if(x > maxX) {
    			maxX = x;
    		}
    		if(y < minY) {
    			minY = y;
    		}
    	} 

    	Point topLeft = new Point(maxX + 5+hs.decorationFig.getPreferredSize().width, minY);
        hs.decorationFig.setBounds(new Rectangle(topLeft, hs.decorationFig.getPreferredSize()));
        final IFigure hostFig = getHostFigure();
        hostFig.addFigureListener(new FigureListener() {
            public void figureMoved(IFigure source) {
            	List<AbstractGraphicalEditPart> selected = getSelectedNavAidsEditParts();
                int maxX = 0;
                int minY = Integer.MAX_VALUE;
            	for(AbstractGraphicalEditPart part : selected) {
            		final IFigure hostFig = part.getFigure();
                	Point topLeft = hostFig.getBounds().getTopRight();
            		int x = topLeft.x;
            		int y = topLeft.y;
            		if(x > maxX) {
            			maxX = x;
            		}
            		if(y < minY) {
            			minY = y;
            		}
            	}
            	Point topLeft = new Point(maxX + 5+hs.decorationFig.getPreferredSize().width, minY);
                hs.decorationFig.setLocation(topLeft);
            }
        }); 
        
        return hs.decorationFig;
    }
    
    protected IFigure getButton(IFigure contents, final Command btnExecCmd, final String tooltip) {
        // creates multi remove button in relo
    	if(btnExecCmd.getLabel()!=null && tooltip != null) {
        	if(btnExecCmd.getLabel().equals(REMOVE_NODE) && tooltip.equals(REMOVE_ALL_SELECTED)) {        	
        		this.multiRemoveCmd = btnExecCmd;
        	}
        }

    	MenuButton btn = new MenuButton(contents, getHost().getViewer()) {
			@Override
			public void buildMenu(final IMenuManager menu) {
            		// just execute command
                    // asyncExec is needed otherwise the mouse seems captured or something
	                Display.getCurrent().asyncExec(new Runnable() {
	                    public void run() {                    
	                    	performBtnExecCmd(btnExecCmd, tooltip);
	                    }
					});
			}};
		return btn;

    }

    
    protected IFigure getRelButton(IFigure contents, final ContextMenuProvider dropDownMenu, final String tooltip) {
    	MenuButton btn = new MenuButton(contents, getHost().getViewer()) {
			@Override
			public void buildMenu(final IMenuManager menu) {
        	    // if there is a menu and there is more than one option         	
				if (dropDownMenu == null) return;
				dropDownMenu.buildContextMenu(menu);
        		
				if (menu.getItems().length <= 2 || dropDownMenu.find(MoreButtonUtils.showAllId) != null) 
					return;
				MoreButtonUtils.addShowAllItem(menu, NavAidsEditPolicy.this.getArtifactSelectionHandlesHost().getRootController());
			}};
		return btn;

    }

	private void performBtnExecCmd(final Command btnExecCmd, final String tooltip) {
		// REVIEW: Why is tooltip needed here? Why are we special casing the removes?
        BasicRootController rc = this.getArtifactSelectionHandlesHost().getRootController();
		if(btnExecCmd.getLabel()!=null && btnExecCmd.getLabel().equals(REMOVE_NODE) 
    			&& tooltip != null && tooltip.equals(REMOVE_ALL_SELECTED)) {
    		List<AbstractGraphicalEditPart> selectedParts = getSelectedNavAidsEditParts();
    		for(AbstractGraphicalEditPart part : selectedParts) {
    			EditPolicy policy = part.getEditPolicy(HANDLES_ROLE);
    			if(part.isActive() && (policy instanceof NavAidsEditPolicy)) {
    				NavAidsEditPolicy artifactNavAidsEP = (NavAidsEditPolicy) policy;
    				rc.execute(artifactNavAidsEP.getMultiRemoveCmd());
    			}
    		} 
    	} else { 
    		rc.execute(btnExecCmd);
    	}
	}
	
    /**
     * Returns the command for multiple selection that is associated
     * with this ArtifactNavAidsEditPolicy
     */
    private Command getMultiRemoveCmd() {
    	return multiRemoveCmd;
    }
     
    private List<AbstractGraphicalEditPart> getSelectedNavAidsEditParts() {
    	List<AbstractGraphicalEditPart> selectedNavAidsEP = new ArrayList<AbstractGraphicalEditPart>();
    	List<AbstractGraphicalEditPart> selectedAGEP = this.getArtifactSelectionHandlesHost().getSelectedAGEP();
    	for(AbstractGraphicalEditPart part : selectedAGEP) {
			selectedNavAidsEP.add(part);
    	}
    	return selectedNavAidsEP;
    }
}
