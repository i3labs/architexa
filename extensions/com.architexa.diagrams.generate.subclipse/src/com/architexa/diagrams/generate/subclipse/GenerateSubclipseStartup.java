package com.architexa.diagrams.generate.subclipse;

import org.apache.log4j.Logger;
import org.eclipse.ui.IStartup;

import com.architexa.diagrams.generate.GeneratePlugin;
import com.architexa.diagrams.generate.team.TeamMenuAction;

/**
 * 
 * @author Elizabeth L. Murnane
 *
 */
public class GenerateSubclipseStartup implements IStartup {
    static final Logger logger = GeneratePlugin.getLogger(GenerateSubclipseStartup.class);

	// Initialize all classes that need to be activated after the workbench initializes
	public void earlyStartup() {
		try {
			earlyStartupInternal();
		} catch (Throwable t) {
			logger.error("Unexpected Error", t);
		}
	}
	private void earlyStartupInternal() {
		SubclipseRevisionDiagramGenerator subclipseRevisionDiagramGenerator = new SubclipseRevisionDiagramGenerator();
		subclipseRevisionDiagramGenerator.initialize();
		TeamMenuAction.teamSyncGeneratorType = new SubclipseUncommittedChangesDiagramGenerator();
	}
	
}
