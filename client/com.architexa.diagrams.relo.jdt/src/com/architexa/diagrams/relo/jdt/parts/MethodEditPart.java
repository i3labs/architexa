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

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.collections.Predicate;
import org.apache.log4j.Logger;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.ui.actions.IJavaEditorActionDefinitionIds;
import org.eclipse.jdt.ui.actions.OpenAction;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.openrdf.model.Resource;

import com.architexa.diagrams.RSECore;
import com.architexa.diagrams.commands.AddNodeAndRelCmd;
import com.architexa.diagrams.eclipse.gef.UndoableLabelSource;
import com.architexa.diagrams.jdt.RJCore;
import com.architexa.diagrams.jdt.model.CodeUnit;
import com.architexa.diagrams.jdt.model.UserCreatedFragment;
import com.architexa.diagrams.model.Artifact;
import com.architexa.diagrams.model.ArtifactFragment;
import com.architexa.diagrams.model.DerivedArtifact;
import com.architexa.diagrams.model.DirectedRel;
import com.architexa.diagrams.relo.figures.CodeUnitFigure;
import com.architexa.diagrams.relo.jdt.JavaEditorProxyFigure;
import com.architexa.diagrams.relo.jdt.JavaEditorlet;
import com.architexa.diagrams.relo.jdt.ReloJDTPlugin;
import com.architexa.diagrams.relo.modelBridge.ControllerDerivedAF;
import com.architexa.diagrams.relo.modelBridge.ReloDoc;
import com.architexa.diagrams.relo.parts.ArtifactEditPart;
import com.architexa.diagrams.relo.parts.ModelControllerManager;
import com.architexa.diagrams.relo.parts.ReloController;
import com.architexa.diagrams.ui.RSEContextMenuProvider;
import com.architexa.org.eclipse.draw2d.IFigure;
import com.architexa.org.eclipse.gef.commands.CompoundCommand;
import com.architexa.store.ReloRdfRepository;


/**
 * @author vineet
 *  
 */
public class MethodEditPart extends CodeUnitEditPart implements UndoableLabelSource {
    static final Logger logger = ReloJDTPlugin.getLogger(MethodEditPart.class);

    // no longer used
    // public static Color methodColor = new Color(null, 255, 230, 230);


    @Override
    protected void updateMembers(int newDL) {
        if (newDL > currDL) {
            // make sure that multiple increase requests don't add multiple editors
            if (getModelChildren().isEmpty())
            	ModelControllerManager.appendModelAndChild(getArtifact(), new NestedMethodEditorArtifact(getCU().getArt()), getRootController() );
        } else if (newDL < currDL) {
            // let the child be disposed
            clearModelChildren();
        } else {
            // =
            super.updateMembers(newDL);
        }
    }
    
    
    @Override
    public IFigure getLabelFigure() {
        return ((CodeUnitFigure) getFigure()).getLabel();
    }

    @Override
    protected String getTextChangeCmdName() {
    	return "Edit Method Name";
    }
    @Override
    public void setAnnoLabelText(String str) {
    	// Method names should include the parameter parenthesis
    	// (If user typed something like "foo()" or "foo(String)", we don't need to
    	// do anything, but if he only typed "foo", then just append some empty parens)
    	if(str!=null && !str.contains("(")) str = str+"()";
    	super.setAnnoLabelText(str);
    }

    @Override
    public void buildContextMenu(IMenuManager menu) {

    	if (this.getModel() instanceof UserCreatedFragment) {
			super.buildContextMenu(menu);
			return;
		}
    	
        IAction action;

        action = new Action("Open in Embedded Java Editor") {
		    @Override
            public void run() {
		    	MethodEditPart.this.execute(MethodEditPart.this.getExpandCmd());
                CompoundCommand actionCmd = new CompoundCommand();
                MethodEditPart.this.realizeParent(actionCmd);
                if (actionCmd.size() > 0) MethodEditPart.this.execute(actionCmd);
		    }};
		menu.appendToGroup(RSEContextMenuProvider.GROUP_RSE_TOOLS, action);

        menu.appendToGroup(RSEContextMenuProvider.GROUP_RSE_TOOLS, getRelAction("Show Called Methods", DirectedRel.getFwd(RJCore.calls)));
        menu.appendToGroup(RSEContextMenuProvider.GROUP_RSE_TOOLS, getRelAction("Show Calling Methods", DirectedRel.getRev(RJCore.calls)));
        Predicate refPred = new Predicate() {
        	Resource methodRes = getElementRes();
        	public boolean evaluate(Object arg0) { // arg0 is a Type this method references
        		if(!(arg0 instanceof Resource)) return true;

        		// Don't show references from this
        		// method to a class containing it
        		Resource parent = methodRes;
        		while(parent!=null) {
        			if(arg0.equals(parent)) return false;
        			parent = (Resource) getRepo().getStatement((Resource)null, RSECore.contains, parent).getSubject();
        		}
        		return true;
        	}
        };
        menu.appendToGroup(RSEContextMenuProvider.GROUP_RSE_TOOLS, getRelAction("Show Referenced Types", DirectedRel.getFwd(RJCore.refType), refPred));
        menu.appendToGroup(RSEContextMenuProvider.GROUP_RSE_TOOLS, getRelAction("Show Referencing Methods", DirectedRel.getRev(RJCore.refType)));
		
        action = new Action("Show Declaring Class") {
		    @Override
            public void run() {
                CompoundCommand actionCmd = new CompoundCommand();
                MethodEditPart.this.realizeParent(actionCmd);
                if (actionCmd.size() > 0) MethodEditPart.this.execute(actionCmd);
		    }};
		menu.appendToGroup(RSEContextMenuProvider.GROUP_RSE_TOOLS, action);
		
		// Calling super last because this will create the java doc actions and 
		// we want them at the bottom of the context menu group not the beginning

		// add separator before java doc actions
		menu.appendToGroup(RSEContextMenuProvider.GROUP_RSE_TOOLS, new Separator());
		super.buildContextMenu(menu);
	}

    private final class NestedMethodEditorArtifact extends ControllerDerivedAF {
        private NestedMethodEditorArtifact(Artifact art) {
            super(art);
        }

        @Override
        public ArtifactEditPart createController() {
            return new NestedMethodEditorArtifactEditPart();
        }
    }
    
    private final class NestedMethodEditorArtifactEditPart extends ArtifactEditPart {
        @Override
        protected IFigure createFigure(IFigure curFig, int newDL) {
            DerivedArtifact model = (DerivedArtifact) getModel();
            CodeUnit methodCU = new CodeUnit(model.getEnclosingArtifact());
            logger.info("Opening editor for: " + methodCU);
            final JavaEditorProxyFigure epf = new JavaEditorProxyFigure(getRoot(), methodCU);
            epf.setEditorInitializer(new Runnable() {
                public void run() {
                    JavaEditorlet je = epf.getEditor();
                    IAction action = new MethodOpenAction(je);
                    action.setActionDefinitionId(IJavaEditorActionDefinitionIds.OPEN_EDITOR);
                    je.setAction("OpenEditor", action);
                }
            });
            epf.activate();
            return epf;
        }

        @Override
        public void deactivate() {
            JavaEditorProxyFigure epf = (JavaEditorProxyFigure) getFigure();
            epf.deactivate();
            super.deactivate();
        }
    }

    private final class MethodOpenAction extends OpenAction {
        private MethodOpenAction(JavaEditorlet editor) {
            super(editor);
        }

        @Override
        public void run(Object[] elements) {
            if (elements[0] instanceof IJavaElement) {
                ReloRdfRepository repo = MethodEditPart.this.getBrowseModel().getRepo();
                CodeUnit newElemCU = CodeUnit.getCodeUnit(repo, (IJavaElement) elements[0]);
        
                ReloController rc = MethodEditPart.this.getRootController();
				AddNodeAndRelCmd addCmd = null;
				
				// Is it necessary to add 'showIncludedRels' to method nodes if they only apply to inheritance? 
				Map<Artifact, ArtifactFragment> addedArtToAF = new HashMap<Artifact, ArtifactFragment>();
				if (newElemCU.isMethod(repo)){
					addCmd = new AddNodeAndRelCmd(rc, MethodEditPart.this.getArtFrag(), new DirectedRel(RJCore.calls, true), newElemCU.getArt(), addedArtToAF );
					CompoundCommand tgtCmd = new CompoundCommand();
		    		tgtCmd.add(addCmd);
		        	((ReloDoc) rc.getRootArtifact()).showIncludedRelationships(tgtCmd, addCmd.getNewArtFrag());
				} else {
					addCmd = new AddNodeAndRelCmd(rc, newElemCU.getArt(), addedArtToAF);
					CompoundCommand tgtCmd = new CompoundCommand();
		    		tgtCmd.add(addCmd);
		        	((ReloDoc) rc.getRootArtifact()).showIncludedRelationships(tgtCmd, addCmd.getNewArtFrag());
				}
				rc.execute(addCmd);
				//ArtifactEditPart aep = rc.createOrFindArtifactEditPart(newElemCU);
				//if (newElemCU.isMethod(repo))
				//    rc.addRel(MethodEditPart.this, RJCore.calls, aep);
        
            } else {
                logger.error("Don't know how to open: " + elements[0]);
            }
        }
    }

}