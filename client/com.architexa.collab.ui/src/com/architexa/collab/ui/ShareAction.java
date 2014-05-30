package com.architexa.collab.ui;

import org.apache.log4j.Logger;
import org.eclipse.jface.action.Action;

/**
 * Common functionality for ShareOnServer and ShareByEmail Actions
 */
public abstract class ShareAction extends Action {
	static final Logger logger = Activator.getLogger(ShareAction.class);

	protected String diagramName="";
	protected String diagramDescription="";

	public ShareAction(String diagramName, String diagramDescription) {
		this.diagramName = diagramName;
		this.diagramDescription = diagramDescription;
	}

}
