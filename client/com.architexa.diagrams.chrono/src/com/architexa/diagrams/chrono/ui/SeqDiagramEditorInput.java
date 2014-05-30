package com.architexa.diagrams.chrono.ui;

import java.util.Map;

import org.eclipse.jface.text.IDocument;

import com.architexa.diagrams.chrono.sequence.SeqPlugin;
import com.architexa.diagrams.ui.RSEShareableDiagramEditorInput;

/**
 * 
 * @author Elizabeth L. Murnane
 *
 */
public class SeqDiagramEditorInput extends RSEShareableDiagramEditorInput {


	public SeqDiagramEditorInput() {
		super("Sequence Diagram Editor", SeqPlugin
				.getImageDescriptor("icons/chrono-document.png"),
				"Sequence Diagram", SeqEditor.editorId);
	}
	
	public SeqDiagramEditorInput(Map<IDocument, IDocument> lToRDocMap, Map<IDocument, String> lToPathMap, StringBuffer docBuff) {
		super("Sequence Diagram Editor", SeqPlugin
				.getImageDescriptor("icons/chrono-document.png"),
				"Sequence Diagram", SeqEditor.editorId, lToRDocMap, lToPathMap, docBuff);
	}

	@Override
	public boolean exists() {
		return true;
	}

}
