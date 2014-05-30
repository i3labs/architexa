package com.architexa.diagrams;

import java.util.ArrayList;
import java.util.List;

import com.architexa.diagrams.ui.RSEEditor;
import com.architexa.diagrams.ui.RSEEditorViewCommon.IRSEEditorViewCommon;

public class ErrorBuildListeners {

	private static List<IRSEEditorViewCommon> buildersListeners = new ArrayList<IRSEEditorViewCommon>(10);
	
	public static void add(IRSEEditorViewCommon editor) {
		if(!buildersListeners.contains(editor))
			buildersListeners.add(editor);
	}
	
	public static void remove(RSEEditor editor) {
		buildersListeners.remove(editor);
	}
	
	/**
	 * Checks all the editors in the buildersListeners list in order to correctly
	 * decorate or un-decorate their title images and the edit parts in their diagrams
	 *
	 */
	public static void check() {
		for(final IRSEEditorViewCommon editor : buildersListeners) {
			editor.checkError();
		}
	}

}
