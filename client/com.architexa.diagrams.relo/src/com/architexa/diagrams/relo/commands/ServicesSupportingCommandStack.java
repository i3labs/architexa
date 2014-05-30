package com.architexa.diagrams.relo.commands;

import java.util.List;

import com.architexa.org.eclipse.gef.commands.Command;
import com.architexa.org.eclipse.gef.commands.CommandStack;
import com.architexa.org.eclipse.gef.commands.CompoundCommand;

/**
 * Hides commands from the user that are of the type ServiceCommand
 */
public class ServicesSupportingCommandStack extends CommandStack {

    @Override
	public void execute(Command cmd) {
    	if (cmd == null) return;
    	
    	// do the chaining with previous command if possible. Check for:
    	// 1. service command tag: as these are the only 'invisible' commands
    	//    that we support
    	// 2. that the previous command can be chained
    	// 3. that there is no redo command - because we can't flush the redo 
    	//    stack as we would need to
    	if (isServiceCommand(cmd) && 
        		getUndoCommand() instanceof CompoundCommand &&
        		canRedo()) {

    		executeAndQueueWithPrev(cmd);
        	return;
        }

        if (!(cmd instanceof CompoundCommand)) {
        	// command is not a compound command - we won't be ale to chain
			// service commands onto it --> convert to compound
        	CompoundCommand newCmd = new CompoundCommand(cmd.getLabel());
        	newCmd.add(cmd);
        	cmd = newCmd;
        }
        
        super.execute(cmd);
	}


    @SuppressWarnings("deprecation")
	private void executeAndQueueWithPrev(Command cmd) {
    	// (we don't let super handle this part as it will add it to the undo
		// stack which we cannot remove it from)
    	//super.execute(cmd);
    	
    	notifyListeners(cmd, PRE_EXECUTE);
    	try {
    		cmd.execute();

    		// chain to command before instead of adding to the undo stack
    		//undoable.push(command);
        	CompoundCommand prevCmd = (CompoundCommand) getUndoCommand();
        	prevCmd.add(cmd);

        	notifyListeners();
    	} finally {
    		notifyListeners(cmd, POST_EXECUTE);
    	}
	}


	private boolean isServiceCommand(Command command) {
    	if (command instanceof ServiceCommand) return true;
    	if (command instanceof CompoundCommand) {
    		List<?> cmds = ((CompoundCommand)command).getCommands();
    		for (Object childCmd : cmds) {
				if (!isServiceCommand((Command) childCmd)) return false;
			}
    		// checked all children
    		return true;
    	}
		return false;
	}
}
