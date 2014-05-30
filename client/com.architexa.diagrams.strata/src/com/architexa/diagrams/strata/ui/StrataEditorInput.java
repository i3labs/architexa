/**
 * Copyright (c) 2007-2008 Architexa, Inc
 */ 
package com.architexa.diagrams.strata.ui;

import java.util.Map;

import org.eclipse.jface.text.IDocument;

import com.architexa.diagrams.strata.StrataPlugin;
import com.architexa.diagrams.strata.model.StrataRootDoc;
import com.architexa.diagrams.ui.RSEShareableDiagramEditorInput;

public class StrataEditorInput extends RSEShareableDiagramEditorInput {

	private final StrataRootDoc strataRootDoc; 

	public StrataEditorInput(StrataRootDoc _strataRootDoc) {
		super("Layered Diagram Editor", StrataPlugin.getImageDescriptor("/icons/office-document.png"), "Layered Diagram", StrataEditor.editorId);
		this.strataRootDoc = _strataRootDoc;		
	}
	
	public StrataEditorInput(StrataRootDoc _strataRootDoc, Map<IDocument, IDocument> lToRDocMap, Map<IDocument, String> lToPathMap, StringBuffer docBuff) {
		super("Layered Diagram Editor", StrataPlugin.getImageDescriptor("/icons/office-document.png"), "Layered Diagram", StrataEditor.editorId, lToRDocMap, lToPathMap, docBuff);
		this.strataRootDoc = _strataRootDoc;		
	}

	public StrataRootDoc getStrataRootDoc() {
		return strataRootDoc;
	}
	
	@Override
	public boolean exists() {
		return true;
	}
}
