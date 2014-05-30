/**
 * 
 */
package com.architexa.diagrams.parts;


import com.architexa.diagrams.commands.AnnTextChangeCommand;
import com.architexa.diagrams.eclipse.gef.UndoableLabelSource;
import com.architexa.org.eclipse.gef.commands.Command;
import com.architexa.org.eclipse.gef.editpolicies.DirectEditPolicy;
import com.architexa.org.eclipse.gef.requests.DirectEditRequest;

/**
 * @author Abhishek Rakshit
 *
 */
public class AnnoLabelDirectEditPolicy extends DirectEditPolicy{
	private final UndoableLabelSource annoLabelSource;
	private final String commandName;

    public AnnoLabelDirectEditPolicy(UndoableLabelSource annoLabelSource, String name) {
        this.annoLabelSource = annoLabelSource;
        this.commandName = name;
    }

    @Override
    protected Command getDirectEditCommand(DirectEditRequest request) {
        // if we get in a strange state where the request is null, do not throw NPE, just return null
    	if (request == null || request.getCellEditor() == null) return null;
    	
    	return new AnnTextChangeCommand(annoLabelSource, (String) request.getCellEditor().getValue(), commandName);
    }

    @Override
    protected void showCurrentEditValue(DirectEditRequest request) {
        annoLabelSource.setAnnoLabelText((String) request.getCellEditor().getValue());
        //hack to prevent async layout from placing the cell editor
        // twice.
        annoLabelSource.getAnnoLabelFigure().getUpdateManager().performUpdate();
    }
}
