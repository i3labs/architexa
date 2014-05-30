/* 
 * Copyright (c) 2004-2005 Massachusetts Institute of Technology. This code was
 * developed as part of the Haystack (http://haystack.lcs.mit.edu/) research 
 * project at MIT. Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation files (the 
 * "Software"), to deal in the Software without restriction, including without 
 * limitation the rights to use, copy, modify, merge, publish, distribute, 
 * sublicense, and/or sell copies of the Software, and to permit persons to whom
 * the Software is furnished to do so, subject to the following conditions: 
 * 
 * The above copyright notice and this permission notice shall be included in 
 * all copies or substantial portions of the Software. 
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR 
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, 
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE 
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER  
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, 
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE. 
 */

/*
 * Created on Jun 23, 2004
 */
package com.architexa.diagrams.relo.commands;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

import com.architexa.diagrams.UserTick;
import com.architexa.diagrams.relo.ReloPlugin;
import com.architexa.org.eclipse.gef.commands.Command;
import com.architexa.org.eclipse.gef.commands.CompoundCommand;


/**
 * @author vineet
 * 
 * CommandStack which two things:
 * 1] Ensures that only one command is running at a time.
 * 2] Composes commands that are of type ComposableCommand (to group multiple
 * requests) <--- look into why this is really needed.
 * 
 * Also hides commands from the user that are of the type ServiceCommand (from
 * base class)
 * 
 */
public class QueueableCommandStack extends ServicesSupportingCommandStack {
    static final Logger logger = ReloPlugin.getLogger(QueueableCommandStack.class);
    
	List<Command> executionQueue = Collections.synchronizedList(new LinkedList<Command>());

	boolean executing = false;
    
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.gef.commands.CommandStack#execute(org.eclipse.gef.commands.Command)
	 * 
	 * Designed to be thread safe
	 */
	@Override
    public void execute(Command command) {

		// queue items if needed
		synchronized (executionQueue) {
			if (executing) {
				if (command instanceof ComposableCommand) {
                    for (Command iterCmd : executionQueue) {
                        if (!(iterCmd instanceof ComposableCommand)) continue;
                        if (!((ComposableCommand) iterCmd).canCompose(command)) continue;
                        ((ComposableCommand) iterCmd).compose(command);
                        return;
                    }
				}
				executionQueue.add(command);
				return;
			} else {
				executing = true;
			}
		}

		// we can really execute
        //logger.info("executionQueue.executingFirst: " + command.getClass());
		while (executing) {
            try {
                //logger.info("executing: " + command.getClass());
                UserTick.logger.info("exec: " + getCmdStr(command));
                super.execute(command);
            } catch (Throwable t) {
                logger.error("Exception while trying to execute command: " + command.getClass(), t);
            }

			// check for any more items on the queue
			synchronized (executionQueue) {
				if (executionQueue.isEmpty()) {
                    //logger.info("executionQueue.isEmpty");
					executing = false;
					return;
				} else {
					command = executionQueue.remove(0);
				}
			}
		}
	}
    
    private String getCmdStr(Command cmd) {
        if (cmd instanceof CompoundCommand) {
            if (cmd.getLabel() != null) 
                return "CompoundCommand - " + cmd.getLabel();
            else
                return "CompoundCommand";
        }
        if (cmd.getLabel() != null) return cmd.getLabel();
        return cmd.getClass().toString();
    }

    @Override
    public void undo() {
        logger.info("undo: " + getCmdStr(getUndoCommand()));
        if (getUndoCommand() instanceof CompoundCommand) {
            List<?> commands = ((CompoundCommand)getUndoCommand()).getCommands();
            for (Object cmd: commands) {
                logger.info("  undo: " + getCmdStr((Command) cmd));
            }
        }
        try {
            super.undo();
        } catch (Exception e) {
            logger.error("Unexpected exception", e);
        }
    }
}