package com.architexa.diagrams.parts;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.TextCellEditor;

import com.architexa.diagrams.eclipse.gef.UndoableLabelSource;
import com.architexa.org.eclipse.draw2d.IFigure;
import com.architexa.org.eclipse.gef.GraphicalEditPart;
import com.architexa.org.eclipse.gef.extensions.ServerAction;
import com.architexa.org.eclipse.gef.tools.DirectEditManager;
import com.architexa.org.eclipse.gef.tools.LabelDirectEditManager;

public class RSEInjectableCommentEditor extends Action implements ServerAction {

	protected String oldTxt;
	protected String newTxt;
	protected UndoableLabelSource commentEP;
	
	public void handleTextEditing(String annoLabelText, UndoableLabelSource commentEP, DirectEditManager manager, IFigure annoTextFig) {
		if (manager == null)
			manager = new LabelDirectEditManager((GraphicalEditPart) commentEP, TextCellEditor.class,
					new AnnoLabelCellEditorLocator(annoTextFig),
					annoTextFig);
		manager.show();
	}
	
	public void init(Object[] params) {}
	
	@Override
	public void run() {}

}
