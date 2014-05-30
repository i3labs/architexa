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
 * Created on Aug 12, 2005
 */
package com.architexa.diagrams.jdt.ui;

import org.apache.log4j.Logger;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.debug.core.IJavaFieldVariable;
import org.eclipse.jdt.internal.ui.actions.SelectionConverter;
import org.eclipse.jdt.internal.ui.javaeditor.JavaEditor;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.ui.IWorkbenchPart;
import org.openrdf.model.URI;

import com.architexa.diagrams.RSECore;
import com.architexa.diagrams.jdt.Activator;
import com.architexa.diagrams.jdt.RJCore;
import com.architexa.diagrams.jdt.compat.SCUtils;
import com.architexa.diagrams.model.Artifact;
import com.architexa.diagrams.ui.NavTracker;
import com.architexa.store.ReloRdfRepository;
import com.architexa.store.StoreUtil;


/**
 * TODO {P2} We do not catch rapid open's; i.e. when user clicks on a method and immediately presses F3
 * 
 * @author vineet
 *
 */
public class JDTTracker extends NavTracker {
    static final Logger logger = Activator.getLogger(JDTTracker.class);

    /*
     * TODO: There will be bugs in tracking when there are two windows, however,
     * we might want to get this code having two seperate instances (per
     * workbench window)
     */
    
    /**
     * 
     */
    ////////////////////////////////////////////////////////////
    /// Actual Selection Processing Code
    ////////////////////////////////////////////////////////////
    
    private IJavaElement prevDeclaredElement;
    private JavaEditor prevEditor;
    private ITextSelection prevTextSelection;
    public static boolean EDITOR_NAV_ONLY = true;
    
    @Override
    protected void processSelection(IWorkbenchPart selectedPart, ITextSelection textSelection) {
        if (!(selectedPart instanceof JavaEditor)) return;

        try {
            IJavaElement selEditorInput = SCUtils.SelectionConverter_getInput( (JavaEditor)selectedPart );
            IJavaElement selDeclaredElement = SelectionConverter.resolveEnclosingElement(selEditorInput, textSelection);
    
            // ignore elements that represent the entire editor, since they seem
            // to be selected temproarily when the editor is opened
            if (selDeclaredElement==null || selDeclaredElement.equals(selEditorInput)) return;
            
            //logger.error("sel: " + selDeclaredElement.getHandleIdentifier());

            // infer relationship *only* if the selection has changed
            if (diffSel(prevEditor, prevTextSelection, selectedPart, textSelection)) {
                processSelectionForRelationship(selDeclaredElement);
                addNavigationItem(selectedPart, selDeclaredElement);
            }
            
            // store values for next time (we also copy even if the declared element
            // did not change, since the referred element could have changed)
            prevDeclaredElement = selDeclaredElement;
            prevEditor = (JavaEditor) selectedPart;
            prevTextSelection = textSelection;
        } catch (Throwable e) {
            logger.error("Unexpected Error", e);
        }
    }
    
    /**
     * @return true if given selections are different
     */
    private static boolean diffSel(IWorkbenchPart sel1Part, ITextSelection sel1TxtSel, IWorkbenchPart sel2Part, ITextSelection sel2TxtSel) {
    	if (sel1Part == null || sel1TxtSel == null || sel2Part == null || sel2TxtSel == null) return true;
    	if (!sel1Part.equals(sel2Part)) return true;
    	if (sel1TxtSel.getOffset() != sel2TxtSel.getOffset()) return true;
    	if (sel1TxtSel.getLength() != sel2TxtSel.getLength()) return true;
    	return false;
    }
    
    @Override
    protected void processSelection(IWorkbenchPart selectedPart, Object selectionItem) {
        try {
        	if (selectionItem instanceof IJavaElement) {
        		processSelectionForRelationship((IJavaElement) selectionItem);
                addNavigationItem(selectedPart, selectionItem);
        	} else if (selectionItem instanceof IJavaFieldVariable) {
        		IJavaFieldVariable var = (IJavaFieldVariable)selectionItem;
        		ReloRdfRepository reloRepo = StoreUtil.getMemRepository();
        		String varId = JDTDebugIdSupport.getId(var);
        		Artifact varArt = new Artifact(RSECore.idToResource(reloRepo, RJCore.jdtWkspcNS, varId));
                addNavigationItem(selectedPart, varArt);
        	} else {
        		//logger.info("no support for type: " + selectionItem.getClass());
        	}
            
        } catch (Throwable e) {
            logger.error("Unexpected Error", e);
        }
    }


    protected void processSelectionForRelationship(IJavaElement currDeclaredElement) throws JavaModelException {
        //logger.info(".");
        if (prevTextSelection == null) return;
        IJavaElement prevEditorInput = SCUtils.SelectionConverter_getInput(prevEditor);
        //logger.info("..");

        IJavaElement[] prevReferencedElements = null;
        try {
        	prevReferencedElements = SelectionConverter.codeResolve(prevEditorInput, prevTextSelection);
        } catch (JavaModelException jme) {
        	// the prevTextSelection might not be valid right now, so we could get an error
        	// ideally we need to get this informaiton in another manner
        	return;
        }
        if ( prevReferencedElements == null || prevReferencedElements.length != 1) return;
        //logger.info("...");
        
        // draw relationship if
        //  a] previous selection was *in* previous declaration [check: prevSel!=prevDecl]
        //  b] previous selection *is* current declations
        final IJavaElement prevSelJE = prevReferencedElements[0];
        if (!prevSelJE.equals(prevDeclaredElement) && prevSelJE.equals(currDeclaredElement)) {
            //logger.error("lre: " + prevSelJE.getHandleIdentifier());

            //logger.info("Adding relationship");
            /*
             * Since previous referencedElement has become the
             * declaredElement, the user went directly from the editor to
             * the new editor; i.e. show the relationship [we should also
             * show changes in other views (like call-heirarchy)]
             */

            // add relationship
            URI addingRel = RJCore.refType;
            if (currDeclaredElement instanceof IMethod) {
                addingRel = RJCore.calls;
            }
            addNavigationPath(prevDeclaredElement, addingRel, currDeclaredElement);
        } else {
            //logger.info("failed");
            //logger.info("lre: " + prevSelJE.getHandleIdentifier());
            //logger.info("wasRef:" + !prevSelJE.equals(prevDeclaredElement) + " !=" + prevDeclaredElement.getHandleIdentifier());
            //logger.info("isDecl:" + prevSelJE.equals(currDeclaredElement) + " ==" + currDeclaredElement.getHandleIdentifier());
        }
    }





}
