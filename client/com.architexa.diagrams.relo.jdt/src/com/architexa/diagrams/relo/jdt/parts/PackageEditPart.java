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


import org.apache.log4j.Logger;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;

import com.architexa.diagrams.eclipse.gef.UndoableLabelSource;
import com.architexa.diagrams.jdt.RJCore;
import com.architexa.diagrams.jdt.model.CodeUnit;
import com.architexa.diagrams.relo.figures.IncompleteSideLineBorder;
import com.architexa.diagrams.relo.figures.PackageFigure;
import com.architexa.diagrams.relo.figures.PartialLineBorder;
import com.architexa.diagrams.relo.figures.StackedBorder;
import com.architexa.diagrams.relo.graph.GraphLayoutManager;
import com.architexa.diagrams.relo.jdt.ReloJDTPlugin;
import com.architexa.diagrams.relo.jdt.commands.HideCommand;
import com.architexa.diagrams.relo.parts.ReloController;
import com.architexa.diagrams.relo.ui.ColorScheme;
import com.architexa.diagrams.ui.RSEContextMenuProvider;
import com.architexa.org.eclipse.draw2d.IFigure;
import com.architexa.org.eclipse.draw2d.PositionConstants;
import com.architexa.org.eclipse.gef.EditPart;
import com.architexa.org.eclipse.gef.commands.CompoundCommand;
import com.architexa.org.eclipse.gef.editparts.AbstractGraphicalEditPart;


/**
 * @author vineet
 *
 */
public class PackageEditPart extends CodeUnitEditPart implements UndoableLabelSource {
    static final Logger logger = ReloJDTPlugin.getLogger(PackageEditPart.class);
	private String label;

	/* (non-Javadoc)
	 * @see org.eclipse.gef.editparts.AbstractGraphicalEditPart#createFigure()
	 */
    @Override
    protected IFigure createFigure(IFigure curFig, int newDL) {
		if (curFig == null) {
			label = getLabel();
			curFig = new PackageFigure(
		            label,
		            getArtifact().getArt().queryName(getRepo()),
		            CodeUnit.getIcon(getRepo(), getCU().getArt(), RJCore.packageType));
		}
		return curFig;
	}

    @Override
    protected String getTextChangeCmdName() {
    	return "Edit Package Name";
    }

    @Override
    protected String getChildrenLabel() {
        return "Classes";
    }

    @Override
    public void buildContextMenu(IMenuManager menu) {
    	// we don't want the CUEP 'Open in Editor' context menu
        //super.buildContextMenu(menu);
        
	    final PackageEditPart packageCUEP = this;
	    IAction action;
        action = new Action("Hide Package") {
            @Override
            public void run() {
                CompoundCommand actionCmd = new CompoundCommand();
                actionCmd.add(new HideCommand(packageCUEP));
                //cuep.realizeParent(actionCmd, /*inferring*/ false);
                PackageEditPart.this.execute(actionCmd);
            }
        };
        menu.appendToGroup(RSEContextMenuProvider.GROUP_RSE_TOOLS, action);
    }
    
    /////////
	// We place children classes in the root content pane - this allows us to
	// easily ensure that they are not overlapped
    /////////
    
    @Override
    protected void addChildVisual(EditPart childEditPart, int index) {
    	if(childEditPart instanceof ClassEditPart) {
    		IFigure figure = ((AbstractGraphicalEditPart)childEditPart).getFigure();
    		ReloController rc = (ReloController) getRoot().getContents();
    		rc.getContentPane().add(figure);
    	} else {
    		super.addChildVisual(childEditPart, index);
    	}
    }

    @Override
    protected void removeChildVisual(EditPart childEditPart) {
    	if(childEditPart instanceof ClassEditPart) {
    		IFigure figure = ((AbstractGraphicalEditPart)childEditPart).getFigure();
    		ReloController rc = (ReloController) getRoot().getContents();
    		rc.getContentPane().remove(figure);
    	} else {
    		super.removeChildVisual(childEditPart);
    	}
    }


	@Override
	public void deactivate() {
		super.deactivate();
		
		// since the children classes are not our children we need to make sure
		// to remove them as well when we get removed
		ReloController rc = (ReloController) getRoot().getContents();
		for (EditPart childEP : getChildrenAsTypedList()) {
			if (!(childEP instanceof ClassEditPart)) continue;
			IFigure figure = ((AbstractGraphicalEditPart)childEP).getFigure();
			rc.getContentPane().remove(figure);
		}
	}

	// TODO: thois is similar to the PackageFigure constructor. Should this be shared in the EP of Fig? 
	@Override
	protected void updateColors() {
		IFigure fig = getFigure();
		if (!(fig instanceof PackageFigure)) return;
		PackageFigure packFig = (PackageFigure) fig;
		packFig.getLabel().setBackgroundColor(ColorScheme.packageColor);
		packFig.getLabel().setBorder(new PartialLineBorder(ColorScheme.packageBorder, 1, true, true, false, true));
		
		IFigure contentFigure = packFig.getContentFig();
		StackedBorder packageBodyBorder = new StackedBorder();
		packageBodyBorder.addBorder(new PartialLineBorder(ColorScheme.packageBorder, 1, false, true, true, true));
		packageBodyBorder.addBorder(new IncompleteSideLineBorder(
				ColorScheme.packageBorder, 1, 
				PositionConstants.NORTH, packFig.getLabel(), 
				PositionConstants.NORTH_WEST));
		contentFigure.setBorder(packageBodyBorder);
		contentFigure.setBackgroundColor(ColorScheme.packageColor);
		contentFigure.setOpaque(true);
		contentFigure.setLayoutManager(new GraphLayoutManager.SubgraphLayout());
	}

	
}
