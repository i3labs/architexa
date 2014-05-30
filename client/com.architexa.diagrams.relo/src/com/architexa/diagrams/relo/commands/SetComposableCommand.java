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
 * Created on Jul 18, 2004
 */
package com.architexa.diagrams.relo.commands;

import java.util.HashSet;
import java.util.Set;

import com.architexa.org.eclipse.gef.commands.Command;

/**
 * @author vineet
 *
 */
public abstract class SetComposableCommand extends ComposableCommand {
	protected Set<Object> setParam = new HashSet<Object> ();

	/**
	 * 
	 */
	public SetComposableCommand(Object param1Value) {
		super();
		setParam.add(param1Value);
	}

	/**
	 * @param label
	 */
	public SetComposableCommand(String label, Object param1Value) {
		super(label);
		setParam.add(param1Value);
	}

	/**
	 * @param command
	 * @return
	 * 
	 * Supports the composing of two of any subclasses of this class (with only 
	 * the subclass).
	 */
	@Override
    public boolean canCompose(Command command) {
		if (this.getClass().equals(command.getClass())) {
			return true;
		}
		return false;
	}

	/**
	 * @param command
	 */
	@Override
    public void compose(Command cmd) {
		setParam.addAll(((SetComposableCommand)cmd).setParam);
	}

}
