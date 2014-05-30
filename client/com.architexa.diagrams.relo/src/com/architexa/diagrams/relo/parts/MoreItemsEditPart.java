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
package com.architexa.diagrams.relo.parts;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.Predicate;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.resource.ImageDescriptor;
import org.openrdf.model.Resource;

import com.architexa.diagrams.commands.AddNodeAndRelCmd;
import com.architexa.diagrams.eclipse.gef.MenuButton;
import com.architexa.diagrams.model.Artifact;
import com.architexa.diagrams.model.ArtifactFragment;
import com.architexa.diagrams.relo.modelBridge.ArtifactFragmentUtil;
import com.architexa.diagrams.relo.modelBridge.ReloDoc;
import com.architexa.diagrams.ui.MultiAddCommandAction;
import com.architexa.diagrams.ui.RSEEditor;
import com.architexa.diagrams.utils.MoreButtonUtils;
import com.architexa.org.eclipse.draw2d.Figure;
import com.architexa.org.eclipse.draw2d.FlowLayout;
import com.architexa.org.eclipse.draw2d.IFigure;
import com.architexa.org.eclipse.draw2d.Label;
import com.architexa.org.eclipse.draw2d.PositionConstants;
import com.architexa.org.eclipse.draw2d.ToolbarLayout;
import com.architexa.org.eclipse.gef.DefaultEditDomain;
import com.architexa.org.eclipse.gef.EditDomain;
import com.architexa.org.eclipse.gef.commands.Command;
import com.architexa.org.eclipse.gef.commands.CompoundCommand;


/**
 * All edit parts should inherit from here, ArtifactEditPart's are for
 * anonymous/derive EditParts
 * 
 * @author vineet
 */
public abstract class MoreItemsEditPart extends ArtifactNodeEditPart {
    private static String MORE_BUTTON_TOOLTIP_TEXT = " Add Children ";
    private static String MORE_BUTTON_TEXT = " Member"; // no space at end or else +plural will leave a space before the s
	private Predicate pred = null;

	private Map<String, Artifact> getLabelsMap(Set<Artifact> artSet) {
        Map<String, Artifact> strToCUMap = new HashMap<String,Artifact> (artSet.size());
        for (Artifact art : artSet) {
            String lbl;
            try {
                lbl = getLabel(art, getArtifact().getArt());
            } catch (Throwable t) {
                logger.error("Unexpected error while getting label for: " + art);
                lbl = art.toString();
            }
            if (lbl.contains("this$")) continue;
            strToCUMap.put(lbl, art);
        }
        return strToCUMap;
    }

    protected String getChildrenLabel() {
        return null;
    }

    public String getLabel(Artifact art, Artifact contextArt) {
        return art.toString();
    }

    public ImageDescriptor getIconDescriptor(Artifact art, Resource resType) {
    	return null;
    }
	protected List<?> getVisibleEditPartChildren() {
	    // TODO: really get this from the model (is that possible with the anonymous nodes)
	    return getChildren();
	}
	public List<Artifact> getFakeInitializerChildren() {
		return new ArrayList<Artifact>();
	}

	public IFigure getMoreBtn() {
		// add text
    	Label moreLabel = new Label();
		moreLabel.setLabelAlignment(PositionConstants.CENTER);
		int numMembers = 
			getArtifact().getArt().queryChildrenArtifacts(getRepo()).size() 
			- getVisibleEditPartChildren().size()
			- getFakeInitializerChildren().size();
		int anonThisCnt = MoreButtonUtils.containsAnonThis(getArtifact().getArt().queryChildrenArtifacts(getRepo()));
		if (anonThisCnt>0) numMembers -= anonThisCnt; 
		if (numMembers < 1) return new Figure();
		String plural = (numMembers != 1) ? "s " : " ";
		moreLabel.setText(numMembers + MORE_BUTTON_TEXT  + plural);

		// add layout
		IFigure moreFig = new Figure();
		ToolbarLayout tb = new ToolbarLayout(true);
		tb.setMinorAlignment(ToolbarLayout.ALIGN_CENTER);
		moreFig.setLayoutManager(tb);
		moreFig.add(moreLabel);

		//add arrow
		moreFig.add(MoreButtonUtils.getMoreButtonTriangle());

		IFigure moreContainer = new Figure();
		FlowLayout layout = new FlowLayout(true);
		layout.setMajorAlignment(ToolbarLayout.ALIGN_BOTTOMRIGHT);
		moreContainer.setLayoutManager(layout);
		moreContainer.add(moreFig);
		
		// add button
		MenuButton menuButton = new MenuButton(moreContainer, getViewer()) {
			@Override
			public void buildMenu(IMenuManager menu) {
				MenuButton button = this;
				buildMoreMenu(menu, button);
		}};

		// set properties, set bounds and add
		Figure moreButton = MoreButtonUtils.setProperties(menuButton, getFigure().getFont(), MORE_BUTTON_TOOLTIP_TEXT);
		MoreButtonUtils.setMoreButtonBounds(moreButton, getFigure());
		return moreButton;
	}
	
	public void buildMoreMenu(IMenuManager menu, MenuButton button) {
		menu.add(new Separator("main"));
		buildMoreChildrenContextMenu(menu); 
		if (menu.getItems().length <= 2) 
			return;
		MoreButtonUtils.addShowAllItem(menu, getRootController());
	}

	private Map<String,Artifact> getChildToLabelMap() {

        Set<Artifact> extraChildren = new HashSet<Artifact>(
        				getArtifact().getArt().
        					queryChildrenArtifacts(getRepo(),pred ));
        
        // remove visible items
        Iterator<?> curChildrenIt = getVisibleEditPartChildren().iterator();
        while (curChildrenIt.hasNext()) {
            ArtifactEditPart childAEP = (ArtifactEditPart) curChildrenIt.next();
            ArtifactFragmentUtil.removeByRDFResourceFromSet(extraChildren, childAEP.getArtifact().getArt());
        }
        
        // remove children that are represented in the repo
        // like static initializers, but are actually static
        // fields, which are already handled separately
        List<Artifact> fakes = getFakeInitializerChildren();
        for(Artifact fake : fakes) {
        	ArtifactFragmentUtil.removeByRDFResourceFromSet(extraChildren, fake);
        }

        Map<String,Artifact> strToCUMap = getLabelsMap(extraChildren);
        return strToCUMap;
	}

	int MAX_LENGTH = 15;
	private void buildMoreChildrenContextMenu(IMenuManager menu) {
		Map<String,Artifact> strToCUMap = getChildToLabelMap();
        String[] strArray = strToCUMap.keySet().toArray(new String[] {});
        Arrays.sort(strArray, Collator.getInstance());
        
        List<String> childList = new ArrayList<String>(Arrays.asList(strArray));
        List<String> tempList = new ArrayList<String>(Arrays.asList(strArray));
        boolean createMultipleMenus = childList.size() > MAX_LENGTH ? true : false;
        Iterator<String> itr = tempList.iterator();
        final List<ArtifactFragment> allAFsAdded = new ArrayList<ArtifactFragment>();
        while (!childList.isEmpty()) {
        	String menuStrStart = childList.get(0);
 			MenuManager subMenu;
 			
 			if (createMultipleMenus) {
	 			subMenu = new MenuManager(menuStrStart+ " - ...");
				subMenu.add(new Separator("main"));
				menu.add(subMenu);
 			} else
 				subMenu = (MenuManager) menu;
 			int i = 0;
 			while (i < MAX_LENGTH && itr.hasNext()) {
 				String extraActionLbl = itr.next();
 				childList.remove(extraActionLbl);
 				MultiAddCommandAction action = getMultiAddCommandAction(extraActionLbl, strToCUMap, allAFsAdded);
 	            subMenu.appendToGroup("main", action);
 	            i++;
 			}
        }
    }

	public List<MultiAddCommandAction> getAllMemberMenuActions() {
		Map<String,Artifact> strToCUMap = getChildToLabelMap();    

		String[] strArray = strToCUMap.keySet().toArray(new String[] {});
		Arrays.sort(strArray, Collator.getInstance());

		final List<ArtifactFragment> allAFsAdded = new ArrayList<ArtifactFragment>();
		List<MultiAddCommandAction> addActions = new ArrayList<MultiAddCommandAction>();
		for(String extraActionLbl : strArray) {
			MultiAddCommandAction action = getMultiAddCommandAction(extraActionLbl, strToCUMap, allAFsAdded);
			addActions.add(action);
		}
		
		EditDomain editDomain = getRoot().getViewer().getEditDomain();
		
		// when using a view (views not supported byu exploration server)
		if (!(editDomain instanceof DefaultEditDomain))
			return addActions;
		
		RSEEditor editor = (RSEEditor) ((DefaultEditDomain)editDomain).getEditorPart();
		if (editor.rseInjectableOICMenuController.createMenu(addActions, this))
			return null;
		return addActions;
	}

	private MultiAddCommandAction getMultiAddCommandAction(String extraActionLbl, 
			Map<String,Artifact> strToCUMap, final List<ArtifactFragment> allAFsAdded) {

		Artifact extraArt = strToCUMap.get(extraActionLbl);
		extraActionLbl = MoreButtonUtils.fixAnonFinalVars(extraActionLbl);
		MultiAddCommandAction action = getAddNodeCmdAction(allAFsAdded, extraActionLbl, extraArt);

		return action;
	}
	
    private MultiAddCommandAction getAddNodeCmdAction(final List<ArtifactFragment> allAFsAdded, String extraActionLbl, final Artifact extraArt) {
        final ReloController rc = (ReloController) getRoot().getContents();
        
        MultiAddCommandAction action = new MultiAddCommandAction(extraActionLbl, getRootController()) {
        	@Override
			public Command getCommand(Map<Artifact, ArtifactFragment> addedArtToAFMap) {        		
				CompoundCommand tgtCmd = new CompoundCommand();
				addedArtToAFMap.put(MoreItemsEditPart.this.getArtFrag().getArt(), MoreItemsEditPart.this.getArtFrag());
            	AddNodeAndRelCmd addCmd = new AddNodeAndRelCmd(rc, extraArt, addedArtToAFMap);
            	allAFsAdded.add(addCmd.getNewArtFrag());
            	tgtCmd.add(addCmd);
                ((ReloDoc) rc.getRootArtifact()).showIncludedRelationships(tgtCmd, addCmd.getNewArtFrag());
                return tgtCmd;
            }
        };
        
        try {
            ImageDescriptor des = getIconDescriptor(extraArt, extraArt.queryType(getRepo()));
            if (des != null) action.setImageDescriptor(des);
        } catch (Throwable t) {
            logger.error("Unexpected error while getting icon for: " + extraArt);
        }
        return action;
    }
}
