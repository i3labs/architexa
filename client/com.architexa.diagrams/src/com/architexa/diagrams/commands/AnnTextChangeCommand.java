package com.architexa.diagrams.commands;


import com.architexa.diagrams.eclipse.gef.UndoableLabelSource;
import com.architexa.org.eclipse.gef.commands.Command;

/**
 * @author Abhishek Rakshit
 */
public class AnnTextChangeCommand extends Command {

	private UndoableLabelSource annoLabelSource;

	private String newText, oldText;

	public AnnTextChangeCommand(UndoableLabelSource annoLabelSource, String newText, String commandName) {
		super(commandName);
		this.annoLabelSource = annoLabelSource;
		this.newText = newText;
	}

	@Override
	public boolean canExecute() {
		return annoLabelSource != null && newText != null;
	}

	@Override
	public void execute() {
		oldText = annoLabelSource.getOldAnnoLabelText();
		annoLabelSource.setAnnoLabelText(newText);
		annoLabelSource.setOldAnnoLabelText(newText);
	}

	@Override
	public void undo() {
		annoLabelSource.setAnnoLabelText(oldText);
		annoLabelSource.setOldAnnoLabelText(oldText);
	}
	
	@Override
	public void redo() {
		annoLabelSource.setAnnoLabelText(newText);
		annoLabelSource.setOldAnnoLabelText(newText);
	}
}
