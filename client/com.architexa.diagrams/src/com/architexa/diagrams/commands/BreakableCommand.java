package com.architexa.diagrams.commands;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.architexa.diagrams.Activator;
import com.architexa.org.eclipse.gef.commands.Command;
import com.architexa.org.eclipse.gef.commands.CommandStack;
import com.architexa.org.eclipse.gef.commands.CompoundCommand;

/**
 * An aggregation of multiple <code>CompoundCommands</code>. When executed,
 * a BreakableCommand can be 'broken apart' into its contained CompoundCommands 
 * and each of those executed separately by the CommandStack 
 */
public class BreakableCommand extends CompoundCommand {
	static final Logger logger = Activator.getLogger(BreakableCommand.class);

	int index = 0;
	List<CompoundCommand> commands = new ArrayList<CompoundCommand>();

	public BreakableCommand(String label, CompoundCommand cc) {
		commands.add(cc);
		setLabel(label);
	}

	public BreakableCommand(String label, Class<?> compoundCommandType) {
		this(label, createCompoundCommand(compoundCommandType));
	}

	@Override
	public void add(Command command) {
		commands.get(index).add(command);
	}

	// TODO: If one of the CompoundCommands in commands equals or contains
	// the command to be removed, this method will be able to remove it. 
	// But if the command to be removed is nested within multiple CompoundCommands
	// (for example a CompoundCommand in commands contains another CompoundCommand 
	// that contains the command to remove), this method will not find the 
	// command and remove it
	public void remove(Command command) {
		if(commands.contains(command)) {
			commands.remove(command);
			return;
		}
		for(CompoundCommand cc : new ArrayList<CompoundCommand>(commands)) {
			if(cc.getCommands().contains(command)) {
				cc.getCommands().remove(command);
				return;
			}
		}
	}

	public void addBreakPlace(CompoundCommand cc) {
		commands.add(cc);
		index++;
	}

	public void addBreakPlace(Class<?> compoundCommandType) {
		addBreakPlace(createCompoundCommand(compoundCommandType));
	}

	@Override
	public List<CompoundCommand> getCommands() {
		return commands;
	}

	public static void execute(CommandStack cmdStack, BreakableCommand cmd) {
		for(CompoundCommand cc : cmd.getCommands()) {
			cmdStack.execute(cc);
		}
	}

	private static CompoundCommand createCompoundCommand(Class<?> compoundCommandType) {
		CompoundCommand cc = null;
		try {
			cc = (CompoundCommand) compoundCommandType.newInstance();
		} catch (InstantiationException e) {
			logger.error("Couldn't instantiate type " + compoundCommandType, e);
		} catch (IllegalAccessException e) {
			logger.error("Couldn't create type " + compoundCommandType, e);
		}
		if(cc==null) cc = new CompoundCommand();
		return cc;
	}

}
