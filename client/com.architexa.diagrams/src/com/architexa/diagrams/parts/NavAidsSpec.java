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
 * Created on Mar 29, 2006
 */
package com.architexa.diagrams.parts;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.resource.ImageDescriptor;

import com.architexa.diagrams.Activator;
import com.architexa.diagrams.model.Artifact;
import com.architexa.diagrams.model.DirectedRel;
import com.architexa.diagrams.services.PluggableTypes;
import com.architexa.diagrams.ui.ImageCache;
import com.architexa.diagrams.ui.MultiAddCommandAction;
import com.architexa.diagrams.ui.RSEEditor;
import com.architexa.org.eclipse.draw2d.IFigure;
import com.architexa.org.eclipse.draw2d.Label;
import com.architexa.org.eclipse.draw2d.geometry.Point;
import com.architexa.org.eclipse.gef.ContextMenuProvider;
import com.architexa.org.eclipse.gef.DefaultEditDomain;
import com.architexa.org.eclipse.gef.EditDomain;
import com.architexa.org.eclipse.gef.EditPart;
import com.architexa.org.eclipse.gef.Request;
import com.architexa.org.eclipse.gef.commands.Command;
import com.architexa.org.eclipse.gef.editparts.AbstractEditPart;


public abstract class NavAidsSpec {
    static final Logger logger = Activator.getLogger(NavAidsSpec.class);

    public NavAidsEditPolicy navAidsPolicy = null;
    public IFigure decorationFig = null;
	public NavAidsSpec firstNAS = null;

    protected IFigure getReqButton(final EditPart hostEP, String iconName, String reqType, String tooltip) {
        Request req = new Request(reqType);
        final Command cmd = hostEP.getCommand(req);
        if (cmd == null) return null;

        IFigure lbl = new Label(ImageCache.getImage(iconName));
        IFigure btn = navAidsPolicy.getButton(lbl, cmd, tooltip);
        btn.setToolTip(new Label(" " + tooltip + " "));
        return btn;
    }

    protected IFigure getRelation(EditPart hostEP, DirectedRel rel) {
        NavAidsEditPart aep = (NavAidsEditPart) hostEP;
        int relCnt = directRelationCnt(aep, rel);
        if (relCnt == 0) return null;
        
        return getRelation(aep, rel, relCnt);
    }
    // call with relCnt = -1 if count is not available and you still want the relation to show 
    protected IFigure getRelation(NavAidsEditPart aep, final DirectedRel rel, int relCnt, List<Artifact> childList) {
        NavAidsRelEditPart relInst = (NavAidsRelEditPart) PluggableTypes.getController(aep.getRepo(), rel.res, rel.res);
        if (relInst == null) {
        	logger.error("Not able to load AREP class for : " + rel.res, new Exception());
        	return null;
        }
        IFigure arrow = relInst.getArrow();
        if (relCnt != -1)
            arrow.setToolTip(new Label(" " + relInst.getRelationLabel(aep.getRepo(), rel.res) + " (" + relCnt + " items) "));

        ContextMenuProvider dropDownMenuBuilder = getRelationContextMenu(aep, rel, childList);
	    IFigure btn = navAidsPolicy.getRelButton(arrow, dropDownMenuBuilder, null);
	    return btn;
    }
    
    protected IFigure getRelation(NavAidsEditPart aep, final DirectedRel rel, int relCnt) {
    	return getRelation(aep, rel, relCnt, null);
//        NavAidsRelEditPart relInst = (NavAidsRelEditPart) PluggableTypes.getController(aep.getRepo(), rel.res, rel.res);
//        if (relInst == null) {
//        	logger.error("Not able to load AREP class for : " + rel.res, new Exception());
//        	return null;
//        }
//        IFigure arrow = relInst.getArrow();
//        if (relCnt != -1)
//            arrow.setToolTip(new Label(" " + relInst.getRelationLabel(aep.getRepo(), rel.res) + " (" + relCnt + " items) "));
//
//        ContextMenuProvider dropDownMenuBuilder = getRelationContextMenu(aep, rel);
//	    IFigure btn = navAidsPolicy.getRelButton(arrow, dropDownMenuBuilder, null);
//	    return btn;
    }

    private int directRelationCnt(NavAidsEditPart aep, DirectedRel prop) {
    	// TODO: examine for removing 
    	// just a quick shortcut check here - so that we can decide if it is
		// worth doing the detailed building the menu or not
    	Set<Artifact> artsInMenu = new HashSet<Artifact>(aep.showableListModel(aep.getRepo(), prop, null));
    	return artsInMenu.size();
    }


    private ContextMenuProvider getRelationContextMenu(final NavAidsEditPart aep, final DirectedRel rel, final List<Artifact> childList) {
        return new ContextMenuProvider(aep.getViewer()) {
            @Override
            public void buildContextMenu(IMenuManager menuManager) {
                final BasicRootController rc = aep.getRootController();
                HashSet<Artifact> children;
                if (childList == null)
                	children = new HashSet<Artifact>(aep.showableListModel(aep.getRepo(), rel, null));
                else
                	children = new HashSet<Artifact>(childList);
                List<MultiAddCommandAction> actions = 
                	getMenuActions(children, aep, rel, rc);
                if (actions == null) return;
                aep.buildNavAidMenu(actions, NavAidsSpec.this, menuManager, rel);
            }
        };
    }
    
//    private ContextMenuProvider getRelationContextMenu(final NavAidsEditPart aep, final DirectedRel rel) {
//        return new ContextMenuProvider(aep.getViewer()) {
//            @Override
//            public void buildContextMenu(IMenuManager menuManager) {
//                final BasicRootController rc = aep.getRootController();
//                List<MultiAddCommandAction> actions = 
//                	getMenuActions(new HashSet<Artifact>(aep.showableListModel(aep.getRepo(), rel, null)), aep, rel, rc);
//                aep.buildNavAidMenu(actions, NavAidsSpec.this, menuManager, rel);
//            }
//        };
//    }

    public void sortArtifactListByName(List<Artifact> allMoreChildren, final NavAidsEditPart aep) {
		Collections.sort(allMoreChildren, new Comparator<Artifact>() {
			public int compare(Artifact art1, Artifact art2) {
				if (art1 == null || art2 == null) return 0;
				return aep.getRelModelLabel(art1).compareTo(aep.getRelModelLabel(art2));
			}
		});
	}
    
    
    protected List<MultiAddCommandAction> getMenuActions(
			Set<Artifact> showableSetModel, NavAidsEditPart aep, DirectedRel rel, BasicRootController rc) {
    	
    	List<MultiAddCommandAction> actions = new ArrayList<MultiAddCommandAction>();

    	List<Artifact> listOrdered = new ArrayList<Artifact>(showableSetModel);
    	sortArtifactListByName(listOrdered, aep);
    	for (Artifact relArt : listOrdered) {
    		String relArtLbl = aep.getRelModelLabel(relArt);
    		MultiAddCommandAction action = aep.getShowRelAction(rc, rel, relArt, relArtLbl);
    		if (action == null)	continue;
    		try {
    			ImageDescriptor des = rc.getIconDescriptor(aep, relArt);
    			if (des != null)
    				action.setImageDescriptor(des);
    		} catch (Throwable t) {
    			NavAidsEditPolicy.logger.error("Unexpected error while getting icon for: " + relArt, t);
    		}
    		actions.add(action);
    	}
		EditDomain editDomain = ((AbstractEditPart) aep.getRootController()).getViewer().getEditDomain();
		// when using a view (views not supported byu exploration server)
		if (!(editDomain instanceof DefaultEditDomain))
			return actions;
				
		RSEEditor editor = (RSEEditor) ((DefaultEditDomain)editDomain).getEditorPart();
		if (editor.rseInjectableOICMenuController.createMenu(actions, aep.getSelectedAGEP().get(0)))
			return null;
 		return actions;
	}

    int MAX_LENGTH = 15;
    public void createMenu(IMenuManager menuManager,
    		Set<Artifact> showableSetModel, NavAidsEditPart aep, DirectedRel rel, BasicRootController rc) {

    	IContributionItem[] contributions = menuManager.getItems();
    	if (contributions.length > 0)
    		menuManager.add(new Separator("header"));
    	for (IContributionItem menuItem : contributions) {
    		menuManager.add(menuItem);
    	}
    	menuManager.add(new Separator("main"));

    	List<Artifact> listOrdered = new ArrayList<Artifact>(showableSetModel);
    	sortArtifactListByName(listOrdered, aep);
    	boolean createMultipleMenus = showableSetModel.size() > MAX_LENGTH ? true : false;
    	List<Artifact> tempList = new ArrayList<Artifact>(listOrdered);
    	Iterator<Artifact> itr = tempList.iterator();
    	while (!listOrdered.isEmpty()) {
    		String menuStrStart = aep.getRelModelLabel(listOrdered.get(0));
    		MenuManager subMenu;

    		if (createMultipleMenus) {
    			subMenu = new MenuManager(menuStrStart+ " - ...");
    			subMenu.add(new Separator("main"));
    			menuManager.add(subMenu);
    		} else
    			subMenu = (MenuManager) menuManager;

    		int i = 0;
    		while (i < MAX_LENGTH && itr.hasNext()) {
    			Artifact relArt = itr.next();
    			listOrdered.remove(relArt);

    			String relArtLbl = aep.getRelModelLabel(relArt);
    			IAction action = aep.getShowRelAction(rc, rel, relArt, relArtLbl);
    			if (action == null)	continue;
    			try {
    				ImageDescriptor des = rc.getIconDescriptor(aep, relArt);
    				if (des != null)
    					action.setImageDescriptor(des);
    			} catch (Throwable t) {
    				NavAidsEditPolicy.logger.error("Unexpected error while getting icon for: " + relArt, t);
    			}
    			subMenu.appendToGroup("main", action);
    			i++;
    		}
    	}
    }

	// actually build the decoration figure
    abstract public void buildHandles();
    
    // would like to have a nice set of alignment operators for use here 
    abstract public Point getHandlesPosition(IFigure containerFig);
}