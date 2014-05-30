package com.architexa.diagrams.relo.jdt.commands;


import com.architexa.diagrams.jdt.model.CodeUnit;
import com.architexa.diagrams.jdt.utils.JDTUISupport;
import com.architexa.org.eclipse.gef.commands.Command;
import com.architexa.store.ReloRdfRepository;

// TODO 
// this should be an action, not a command, i.e. it should not go on
// the undo/redo stack

//This is tied to assertParenhood in ArtifactEditPart which needs to be 

public class OpenInEditorCommand extends Command  {
	private CodeUnit codeUnit;
	private ReloRdfRepository repo;

	public OpenInEditorCommand(CodeUnit codeUnit, ReloRdfRepository repo) {
    	super(JDTUISupport.getOpenInJavaEditorActionString());
    	this.codeUnit = codeUnit;
    	this.repo = repo;
    }

	@Override
    public void execute() {
	        JDTUISupport.openInEditor(codeUnit,repo);
    }
}