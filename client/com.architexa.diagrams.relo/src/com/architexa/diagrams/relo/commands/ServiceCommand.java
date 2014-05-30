package com.architexa.diagrams.relo.commands;

import com.architexa.org.eclipse.gef.commands.Command;

// These commands are hidden from the user
// - they don't show in the undo stack
// - when being undone the command before them is undone
//
// i.e. we don't want the user to know about such Commands - but we add them 
// to the command stack to support undoing 
//
// These commands are basically added by 'intelligent services', i.e. are 
// not added directly by the user
public abstract class ServiceCommand extends Command {

	public ServiceCommand(String label) {
		super(label);
	}

}
