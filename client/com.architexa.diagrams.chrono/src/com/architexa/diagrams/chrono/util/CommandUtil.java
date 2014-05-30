package com.architexa.diagrams.chrono.util;

import java.util.ArrayList;
import java.util.List;

import com.architexa.org.eclipse.gef.commands.Command;
import com.architexa.org.eclipse.gef.commands.CompoundCommand;

/**
 * 
 * @author Elizabeth L. Murnane
 *
 */
public class CommandUtil {

	/**
	 * CompoundCommands may contain other CompoundCommands, so when trying 
	 * to determine whether a CompoundCommand contains at any level a 
	 * particular command, it is necessary to iterate down through all 
	 * commands contained by the given CompoundCommand, commands those 
	 * contain, etc..
	 * @param compoundCmd
	 * @return a list containing all of the commands that are contained 
	 * at any nested level by the given CompoundCommand
	 */
	public static List<Command> getAllContainedCommands(CompoundCommand cc) {
		List<Command> allCommands = new ArrayList<Command>();
		getAllContainedCommands(cc, allCommands);
		return allCommands;
	}

	private static void getAllContainedCommands(CompoundCommand cc, List<Command> allCommands) {
		for(Object cmd : cc.getCommands()) {
			allCommands.add((Command)cmd);
			if(cmd instanceof CompoundCommand) {
				getAllContainedCommands((CompoundCommand)cmd, allCommands);
			}
		}
	}

}
