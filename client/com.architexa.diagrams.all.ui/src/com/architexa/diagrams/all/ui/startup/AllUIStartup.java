package com.architexa.diagrams.all.ui.startup;
import org.eclipse.ui.IStartup;

import com.architexa.diagrams.all.ui.startup.welcome.OpenWelcomePage;
import com.architexa.diagrams.chrono.sequence.ChronoDiagramEngine;
import com.architexa.diagrams.relo.jdt.ReloDiagramEngine;
import com.architexa.diagrams.strata.StrataDiagramEngine;
import com.architexa.diagrams.ui.PluggableDiagramsSupport;
import com.architexa.diagrams.ui.PluggableDiagramsSupport.IRSEDiagramEngine;
import com.architexa.rse.BuildStatus;


public class AllUIStartup implements IStartup {

	public void earlyStartup() {
//		final String cheatSheetId = "com.architexa.diagrams.all.ui.tutorialCheatSheet";
//		Runnable openCheatSheet = new Runnable() {
//			public void run() {
//				OpenCheatSheetAction openAction = new OpenCheatSheetAction(cheatSheetId);
//				openAction.run();
//			}
//		};
//		 Launch the tutorial cheat sheet for a user once his account is validated
//		BuildStatus.addRunnable(openCheatSheet);
			
		Runnable openWelcomePage = new Runnable() {
			public void run() {
				OpenWelcomePage openAction = new OpenWelcomePage();
				openAction.run(null);
			}
		};
	//	 Launch the welcome page for a user once his account is validated
		BuildStatus.addRunnable(openWelcomePage);
	}

	public static void initDiagramEngines() {
		IRSEDiagramEngine diagEngine = new StrataDiagramEngine();
		if (!PluggableDiagramsSupport.isRegistered(diagEngine))
			PluggableDiagramsSupport.registerDiagram(diagEngine);
		diagEngine = new ReloDiagramEngine();
		if (!PluggableDiagramsSupport.isRegistered(diagEngine))
			PluggableDiagramsSupport.registerDiagram(diagEngine);
		diagEngine = new ChronoDiagramEngine();
		if (!PluggableDiagramsSupport.isRegistered(diagEngine))
			PluggableDiagramsSupport.registerDiagram(diagEngine);
	}
	
}