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
 * Created on Oct 18, 2004
 */
package com.architexa.diagrams.relo.actions;

import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.ui.IWorkbenchActionConstants;

import com.architexa.diagrams.model.WidthDPolicy;
import com.architexa.diagrams.relo.ReloPlugin;
import com.architexa.diagrams.relo.parts.ArtifactEditPart;
import com.architexa.diagrams.ui.RSEContextMenuProvider;
import com.architexa.org.eclipse.gef.ContextMenuProvider;
import com.architexa.org.eclipse.gef.EditPart;
import com.architexa.org.eclipse.gef.EditPartViewer;
import com.architexa.org.eclipse.gef.GraphicalViewer;
import com.architexa.org.eclipse.gef.ui.actions.ActionRegistry;


/**
 * Provides a context menu for the Relo
 * @author Vineet Sinha
 */
public class ReloContextMenuProvider extends RSEContextMenuProvider {
	static final Logger logger = ReloPlugin.getLogger(ReloContextMenuProvider.class);

	/**
	 * Creates a new ReloContextMenuProvider assoicated with the given viewer and 
	 * action registry.
	 * @param viewer the viewer
	 * @param registry the action registry
	 */
	public ReloContextMenuProvider(EditPartViewer viewer, ActionRegistry registry) {
		super(viewer, registry);
	}

	//private IAction getAction(String actionId) {
	//	return actionRegistry.getAction(actionId);
	//}
	
	/**
	 * @see ContextMenuProvider#buildContextMenu(org.eclipse.jface.action.IMenuManager)
	 */
	@Override
    public void buildContextMenu(IMenuManager menu) {
		try {
			menu.add(new Separator("main"));
			super.buildContextMenu(menu);
		
			//menu.appendToGroup(
			//		GEFActionConstants.GROUP_UNDO, 
			//		getAction(ActionFactory.UNDO.getId()));
			//menu.appendToGroup(
			//		GEFActionConstants.GROUP_UNDO, 
			//		getAction(ActionFactory.REDO.getId()));
			
			//menu.appendToGroup(
			//		GEFActionConstants.GROUP_EDIT,
			//		getAction(ActionFactory.DELETE.getId()));
		
			/*
			action = getAction(GEFActionConstants.ZOOM_IN);
			if (action.isEnabled())
				menu.appendToGroup(GEFActionConstants.GROUP_EDIT, action);
			*/
			
			List<?> sel = getViewer().getSelectedEditParts();
	        if (sel == null || sel.size() == 0) return;
	
	        if (sel.size() > 1) {
	            EditPart ep = (EditPart)sel.get(0);
	            if (ep instanceof ArtifactEditPart)
	                ((ArtifactEditPart) ep).buildMultipleSelectionContextMenu(menu);
	            return;
	        }
	
	        // context menu for single items only
			EditPart ep = (EditPart)sel.get(0);
	
			menu.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	
			if (ep instanceof ArtifactEditPart)
			    ((ArtifactEditPart) ep).buildContextMenu(menu);
			
	
			MenuManager subMenu;
	
			subMenu = new MenuManager("Agents");
			menu.appendToGroup(RSEContextMenuProvider.GROUP_RSE_TOOLS, subMenu);
	
			subMenu.add(new Separator("main"));
			subMenu.add(new Separator("main2"));
	
			if (ep instanceof ArtifactEditPart)
			    ((ArtifactEditPart) ep).buildModelContextMenu(subMenu, "main2");
	
			//AgentManager.buildContextMenu(subMenu, "main2");
	
			/*
			subMenu = new MenuManager("Controls");
	
			subMenu.add(new Separator("core1"));
			subMenu.add(new Separator("core2"));
			
			action = new DummyAction("blah 4");
			subMenu.appendToGroup("core1", action);
		
			action = new DummyAction("blah 5");
			subMenu.appendToGroup("core2", action);
	
			menu.appendToGroup("extra", subMenu);
			*/
			// action to change connection width
			WidthDPolicy.addConnectionWidthChangeAction(sel, menu, (GraphicalViewer) getViewer());
		} catch (Throwable t) {
			logger.error("Could not create class diagram menu. ", t);
		}
	}



}
