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
 * Created on Mar 12, 2005
 */
package com.architexa.diagrams.relo.actions;

import java.util.List;
import java.util.Map;

import org.eclipse.jface.text.IDocument;

import com.architexa.diagrams.relo.ReloPlugin;
import com.architexa.diagrams.relo.agent.ReloBrowseModel;
import com.architexa.diagrams.relo.ui.ReloEditor;
import com.architexa.diagrams.ui.RSEShareableDiagramEditorInput;



public class ListEditorInput extends RSEShareableDiagramEditorInput {
	public final List<?> list;
    public final ReloBrowseModel browseModel;
    
	public ListEditorInput(List<?> list, ReloBrowseModel browseModel) {
		super("Class Diagram Editor", 
				ReloPlugin.getImageDescriptor("icons/relo-document.png"), 
				"Class Diagram", ReloEditor.editorId);
		this.list = list;
        this.browseModel = browseModel;
	}

	public ListEditorInput(List<?> list,
			ReloBrowseModel browseModel, Map<IDocument, IDocument> lToRDocMap, Map<IDocument, String> lToPathMap, StringBuffer docBuff) {
		super("Class Diagram Editor", 
				ReloPlugin.getImageDescriptor("icons/relo-document.png"), 
				"Class Diagram", ReloEditor.editorId, lToRDocMap, lToPathMap, docBuff);
		this.list = list;
        this.browseModel = browseModel;
	}
}