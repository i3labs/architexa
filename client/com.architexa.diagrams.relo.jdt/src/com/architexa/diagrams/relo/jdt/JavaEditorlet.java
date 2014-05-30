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
 * Created on Jul 30, 2004
 *
 */
package com.architexa.diagrams.relo.jdt;

import org.apache.log4j.Logger;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.internal.ui.javaeditor.CompilationUnitEditor;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.IVerticalRuler;
import org.eclipse.jface.text.source.VerticalRuler;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;


/**
 * @author vineet
 *  
 */
public class JavaEditorlet extends CompilationUnitEditor {
	static final Logger logger = ReloJDTPlugin.getLogger(JavaEditorProxyFigure.class);

    /**
     * @param srcRange
     * @return the number of lines taken by the given range
     */
    public int setShowRange(ISourceRange srcRange) {
        // Based on TogglePresentationAction .run & .synchronizeWithPreference
        // Can't really move method since getSourceViewer() and hideOverviewRuler() are protected
        
        showHighlightRangeOnly(true);
        setHighlightRange(srcRange.getOffset(), srcRange.getLength(), true);

        hideOverviewRuler();

        ISourceViewer sourceViewer = getSourceViewer();
        sourceViewer.getTextWidget().setTabs(2);
        FontData[] curFontData = sourceViewer.getTextWidget().getFont().getFontData();
        for (int i=0;i<curFontData.length; i++) {
            curFontData[i].setHeight(curFontData[i].getHeight()*80/100);
        }
        Font newFont = new Font(null, curFontData);
        sourceViewer.getTextWidget().setFont(newFont);
        
        
		IDocument doc = getSourceViewer().getDocument();
		try {
            return doc.getNumberOfLines(srcRange.getOffset(), srcRange.getLength());
        } catch (BadLocationException e) {
        	logger.error("Unexpected exception", e);
        }
        return 0;
    }

    @Override
    protected IVerticalRuler createVerticalRuler() {
        // make vertical ruler minimal
        return new VerticalRuler(1);
    }

    

}