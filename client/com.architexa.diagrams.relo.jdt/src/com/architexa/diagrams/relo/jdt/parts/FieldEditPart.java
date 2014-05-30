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

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.swt.graphics.Color;

import com.architexa.diagrams.eclipse.gef.UndoableLabelSource;
import com.architexa.diagrams.jdt.RJCore;
import com.architexa.diagrams.jdt.model.CodeUnit;
import com.architexa.diagrams.jdt.model.UserCreatedFragment;
import com.architexa.diagrams.model.DirectedRel;
import com.architexa.diagrams.relo.figures.CodeUnitFigure;
import com.architexa.diagrams.ui.RSEContextMenuProvider;
import com.architexa.diagrams.utils.MoreButtonUtils;
import com.architexa.org.eclipse.draw2d.Figure;
import com.architexa.org.eclipse.draw2d.IFigure;
import com.architexa.org.eclipse.draw2d.Label;
import com.architexa.org.eclipse.draw2d.ToolbarLayout;
import com.architexa.org.eclipse.gef.commands.CompoundCommand;


/**
 * @author vineet
 *  
 */
public class FieldEditPart extends CodeUnitEditPart implements UndoableLabelSource {
	public static Color fieldColor = new Color(null, 206, 206, 255);

	@Override
    protected IFigure createFigure(IFigure curFig, int newDL) {
		if (curFig == null) {
		    
			String label = getLabel();
			label = MoreButtonUtils.fixAnonFinalVars(label);
			Label nameLbl = new Label(label, CodeUnit.getIcon(getRepo(), getCU().getArt(), RJCore.fieldType));

            Figure lblFig = new Figure();
            lblFig.setLayoutManager(new ToolbarLayout(true));
            lblFig.add(nameLbl);
            lblFig.add(new Label("  "));

            //curFig = new CodeUnitFigure(lblFig, fieldColor, false);
            curFig = new CodeUnitFigure(lblFig, null, null, false /*isClass*/);

			//curFig = new CodeUnitFigure(getLabel(), isi
			//		.getImage(ISharedImages.IMG_OBJS_PUBLIC), fieldColor, false);
		}

		return curFig;
	}

	@Override
	protected String getTextChangeCmdName() {
		return "Edit Field Name";
	}

	@Override
    public void buildContextMenu(IMenuManager menu) {

		if (this.getModel() instanceof UserCreatedFragment) {
			super.buildContextMenu(menu);
			return;
		}
		
		menu.appendToGroup(RSEContextMenuProvider.GROUP_RSE_TOOLS, 
				getRelAction("Show Type", DirectedRel.getFwd(RJCore.refType)));
		menu.appendToGroup(RSEContextMenuProvider.GROUP_RSE_TOOLS, 
				getRelAction("Show Referencing Types+Methods", DirectedRel.getRev(RJCore.refType)));

        IAction action;
        action = new Action("Show Declaring Class") {
		    @Override
            public void run() {
                CompoundCommand actionCmd = new CompoundCommand();
                FieldEditPart.this.realizeParent(actionCmd);
                if (actionCmd.size() > 0) FieldEditPart.this.execute(actionCmd);
		    }};
		 menu.appendToGroup(RSEContextMenuProvider.GROUP_RSE_TOOLS, action);
		 
		 // Calling super last because this will create the java doc actions and 
		 // we want them at the bottom of the context menu group not the beginning

		 // add separator before java doc actions
		 menu.appendToGroup(RSEContextMenuProvider.GROUP_RSE_TOOLS, new Separator());
		 super.buildContextMenu(menu);
	}

    @Override
    protected void refreshVisuals() {
		super.refreshVisuals();
		CodeUnitFigure cuf = getCodeUnitFigure();
		if (cuf != null) 
			cuf.getLabel().setText(MoreButtonUtils.fixAnonFinalVars(getLabel()));
	}
    
}