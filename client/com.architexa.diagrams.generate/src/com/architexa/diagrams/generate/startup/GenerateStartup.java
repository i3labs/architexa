package com.architexa.diagrams.generate.startup;

import org.apache.log4j.Logger;
import org.eclipse.ui.IStartup;

import com.architexa.diagrams.generate.GeneratePlugin;
import com.architexa.diagrams.generate.tabs.OpenTabsDiagramGenerator;
import com.architexa.diagrams.generate.team.cvs.CVSRevisionDiagramGenerator;

/**
 * 
 * @author Elizabeth L. Murnane
 *
 */
public class GenerateStartup implements IStartup {
    static final Logger logger = GeneratePlugin.getLogger(GenerateStartup.class);

	// Initialize all classes that need to be activated after the workbench initializes
	public void earlyStartup() {
		try {
			earlyStartupInternal();
		} catch (Throwable t) {
			logger.error("Unexpected Error", t);
		}
	}
	private void earlyStartupInternal() {

		CVSRevisionDiagramGenerator cvsRevisionDiagramGenerator = new CVSRevisionDiagramGenerator();
		cvsRevisionDiagramGenerator.initialize();

		OpenTabsDiagramGenerator openTabsDiagramGenerator = new OpenTabsDiagramGenerator();
		openTabsDiagramGenerator.initialize();
	}

}
