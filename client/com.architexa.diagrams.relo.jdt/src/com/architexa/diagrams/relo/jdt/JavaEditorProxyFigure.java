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
 * Created on Aug 5, 2004
 *
 */
package com.architexa.diagrams.relo.jdt;

import java.util.Arrays;
import java.util.LinkedList;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.part.FileEditorInput;

import com.architexa.diagrams.jdt.model.CodeUnit;
import com.architexa.diagrams.relo.figures.SWTWidgetProxyFigure;
import com.architexa.diagrams.relo.parts.ReloController;
import com.architexa.org.eclipse.draw2d.FigureCanvas;
import com.architexa.org.eclipse.gef.DefaultEditDomain;
import com.architexa.org.eclipse.gef.EditDomain;
import com.architexa.org.eclipse.gef.RootEditPart;
import com.architexa.org.eclipse.gef.ui.parts.GraphicalView.ViewEditDomain;
import com.architexa.store.ReloRdfRepository;




/**
 * @author vineet
 *
 */
public class JavaEditorProxyFigure extends SWTWidgetProxyFigure {
	static final Logger logger = ReloJDTPlugin.getLogger(JavaEditorProxyFigure.class);

    private final CodeUnit methodCU;
    private final RootEditPart rootEP;
    private final FigureCanvas gefCanvas;
	
    protected SelectionListener resetWidgetBounds = new SelectionAdapter() {
        @Override
        public void widgetSelected(SelectionEvent e) {
            updateWidgetBounds();
        }
    };

	public JavaEditorProxyFigure(RootEditPart rootEP, CodeUnit methodCU) {
		this.rootEP = rootEP;
        this.methodCU = methodCU;
        this.gefCanvas = (FigureCanvas) rootEP.getViewer().getControl();
    }

    private JavaEditorlet je = null;
    private Runnable editorInitializer = null;

    public Runnable getEditorInitializer() {
        return editorInitializer;
    }
    public void setEditorInitializer(Runnable editorInitializer) {
        this.editorInitializer = editorInitializer;
    }

    @Override
    protected void createWidget() {
        super.createWidget();
        this.je = new JavaEditorlet();
		
		try {
		    ReloRdfRepository repo = ((ReloController) rootEP.getContents()).bm.getRepo();
            IMethod methodJElem = (IMethod) methodCU.getJDTElement(repo);
		    ICompilationUnit icu = null;
            if (methodJElem != null)                        // TODO {P5} figure out why this becomes null
                icu = methodJElem.getCompilationUnit();
		    if (icu == null) {
                logger.error("icu==null: Creating empty canvas");
		        swtWidget = new Composite(gefCanvas, 0);
				defaultWidth = 0;
				defaultHeight = 0;
		        return;
		    }
			IFile file = (IFile) icu.getResource();
			EditDomain domain = rootEP.getViewer().getEditDomain();
			IEditorSite iES = null;
			if (domain instanceof DefaultEditDomain) {
				iES = (IEditorSite) ((DefaultEditDomain)domain).getEditorPart().getSite();
			} else if (domain instanceof ViewEditDomain) {
				// we need an EditorSite, let's just use the current editor's site
				iES =  ((ViewEditDomain) domain).getViewPart().getSite().getWorkbenchWindow()
							.getActivePage()
							.getActiveEditor()
							.getEditorSite();
			} else {
				logger.error("Failed to open Java Editor. Unknwon Domain: " + domain.getClass(), new Exception());
				return;
			}
			je.init(iES, new FileEditorInput(file));
			
            /*
			 * create part control
			 * 
			 * A little ugly code to create it and get a pointer to the control (for moving purposes)
			 * 
			 * We should be able to peak in through reflection or javaEditorlet (but this works for now)
			 */ 
			Control[] c1 = gefCanvas.getChildren(); 
			je.createPartControl(gefCanvas);
			Control[] c2 = gefCanvas.getChildren();
			java.util.List<Control> l = new LinkedList<Control> (Arrays.asList(c2));
			l.removeAll(Arrays.asList(c1));
			swtWidget = l.get(0);

			//ISourceRange srcRange = ParseUtilities.getBodyRange(method);
			ISourceRange srcRange = ParseUtilities.getMethodRange(methodJElem);
            int lineCnt = je.setShowRange(srcRange);

            je.setFocus();
			
			defaultWidth = 350;
			defaultHeight = 20 * (Math.min(10, lineCnt) + 1);
            
            if (editorInitializer != null) editorInitializer.run();
            
            updateWidgetBounds();
        } catch (Exception e) {
            logger.error("Unexpected exception", e);
		}
    }
    
    
    public JavaEditorlet getEditor() {
        return je;
    }
	
	@Override
    protected void closeWidget() {
		try {
			je.getSite().getPage().saveEditor(je, true);
		} catch (Throwable t) {
			logger.error("Dealing with unexpected error", t);
		}
		je.dispose();
        je = null;
        super.closeWidget();
	}


    public void activate() {
        if (gefCanvas.getVerticalBar() != null)
            gefCanvas.getVerticalBar().addSelectionListener(resetWidgetBounds);
        if (gefCanvas.getHorizontalBar() != null)
            gefCanvas.getHorizontalBar().addSelectionListener(resetWidgetBounds);
    }

    public void deactivate() {
		if (gefCanvas.getVerticalBar() != null)
		    gefCanvas.getVerticalBar().removeSelectionListener(resetWidgetBounds);
		if (gefCanvas.getHorizontalBar() != null)
		    gefCanvas.getHorizontalBar().removeSelectionListener(resetWidgetBounds);
    }



}
