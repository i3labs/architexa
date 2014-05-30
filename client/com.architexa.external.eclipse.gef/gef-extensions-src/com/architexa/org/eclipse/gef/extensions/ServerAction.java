package com.architexa.org.eclipse.gef.extensions;
import org.eclipse.jface.action.IAction;

public interface ServerAction extends IAction {
	
	/**
	 * initialize action via given parameters
	 */
	public void init(Object[] params);
}
