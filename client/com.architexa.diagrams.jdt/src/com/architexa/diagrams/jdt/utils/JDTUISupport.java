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
 * Created on Jan 16, 2006
 */
package com.architexa.diagrams.jdt.utils;

import org.apache.log4j.Logger;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import com.architexa.diagrams.jdt.Activator;
import com.architexa.diagrams.jdt.AnonymousClassConstructor;
import com.architexa.diagrams.jdt.CompilerGeneratedDefaultConstructor;
import com.architexa.diagrams.jdt.model.CodeUnit;
import com.architexa.diagrams.model.Artifact;
import com.architexa.rse.RSE;
import com.architexa.store.ReloRdfRepository;


public class JDTUISupport {
    static final Logger logger = Activator.getLogger(JDTUISupport.class);

    public static void openInEditor(CodeUnit cu, ReloRdfRepository repo) {
    	logger.info("Opening: " + cu);
        // TODO: ideally we should check if there is another page in the WW, if so use it, otherwise look for other WW
        IJavaElement javaElem = cu.getJDTElement(repo);
        while (javaElem == null && !cu.isPackage(repo)) {
        	// deal with cases where we cannot find java element (for example default constructors)
        	// do not create codeUnit if parent is null which can happen in some strange cases
        	// cu = new CodeUnit(cu.getArt().queryParentArtifact(repo));
        	Artifact parent = cu.getArt().queryParentArtifact(repo);
        	if (parent == null) 
        		return;
        	cu = new CodeUnit(parent);
        	javaElem = cu.getJDTElement(repo);
        }

        // handle cases where the java element represents
        // something that doesn't actually exist in the code
        IJavaElement elementToOpen = getElementToOpen(javaElem);

        IWorkbenchWindow[] wwin = PlatformUI.getWorkbench().getWorkbenchWindows();
        if (wwin.length > 1) {
            // open in different workbench than mine
            final IWorkbenchWindow currWW = JavaPlugin.getActiveWorkbenchWindow();
            IWorkbenchWindow tgtWW = null;
            for (IWorkbenchWindow ww : wwin) {
                if (!ww.equals(currWW)) {
                    tgtWW = ww;
                    break;
                }
            }
            tgtWW.getShell().setFocus();
        }
        try {
            IEditorPart editor = JavaUI.openInEditor(elementToOpen);
            JavaUI.revealInEditor(editor, elementToOpen);
        } catch (Throwable t) {
            logger.error("Unexpected error launching editor", t);
        }
    }

    /** 
     * Returns an IJavaElement corresponding to the code that
     * should be opened when the user asks to open the given
     * IJavaElement.
     * 
     * If selElmt is a CompilerGeneratedDefaultConstructor, the
     * declaration of the class of which it is a constructor is opened.
     * If selElmt is an AnonymousClassConstructor, the anonymous
     * class definition is opened.
     */
    public static IJavaElement getElementToOpen(IJavaElement selElmt) {
    	if(selElmt instanceof CompilerGeneratedDefaultConstructor) 
    		return ((IMember)selElmt).getDeclaringType();

    	if(selElmt instanceof AnonymousClassConstructor)
    		return ((AnonymousClassConstructor)selElmt).getAnonymousClassDeclaration();

    	return selElmt;
    }

    public static String getOpenInJavaEditorActionString() {
    	return "Open in Java Editor";
    }

    public static ImageDescriptor getOpenInJavaEditorActionIcon() {
    	return Activator.getImageDescriptor("icons/jcu_obj.gif");
    }
}
