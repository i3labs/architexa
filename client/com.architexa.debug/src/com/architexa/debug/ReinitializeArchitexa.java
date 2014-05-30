package com.architexa.debug;

import org.apache.log4j.Logger;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

import com.architexa.collab.CollabCoreStartup;
import com.architexa.collab.ui.Activator;
import com.architexa.diagrams.chrono.sequence.ChronoDiagramEngine;
import com.architexa.diagrams.generate.startup.GenerateStartup;
import com.architexa.diagrams.jdt.builder.ResourceQueueManager;
import com.architexa.diagrams.jdt.builder.asm.AnonymousMethodsBuildProcessor;
import com.architexa.diagrams.jdt.builder.asm.InferOverridesBuildProcessor;
import com.architexa.diagrams.jdt.builder.asm.LogicalContainmentHeirarchyProcessor;
import com.architexa.diagrams.jdt.builder.asm.ResolveBrokenReferencesBuildProcessor;
import com.architexa.diagrams.relo.ReloCore;
import com.architexa.diagrams.relo.jdt.ReloDiagramEngine;
import com.architexa.diagrams.relo.jdt.browse.LiveJDTTracker;
import com.architexa.diagrams.relo.jdt.parts.JDTEditPartsModel;
import com.architexa.diagrams.strata.SCore;
import com.architexa.diagrams.strata.StrataDiagramEngine;
import com.architexa.diagrams.ui.ArchitexaMenuAction;
import com.architexa.intro.Java5Check;
import com.architexa.store.VersionBasedStoreInitializerService;

public class ReinitializeArchitexa implements IWorkbenchWindowActionDelegate{

	static final Logger logger = Activator.getLogger(ReinitializeArchitexa.class);
	
	public void dispose() {
		// TODO Auto-generated method stub
		
	}

	public void init(IWorkbenchWindow window) {
		// TODO Auto-generated method stub
		
	}

	public void run(IAction action) {
		callAllEarlyStartups();
	}

	private void callAllEarlyStartups() {
		logger.error("Reinitialize");
		try {
			new CollabCoreStartup().earlyStartup(); // does nothing

			new GenerateStartup().earlyStartup(); //check
			new ArchitexaMenuAction.Init().earlyStartup();
			new Java5Check().earlyStartup(); //check
			new JDTEditPartsModel().earlyStartup();
			
			new AnonymousMethodsBuildProcessor().earlyStartup();
			new InferOverridesBuildProcessor().earlyStartup();
			new ResolveBrokenReferencesBuildProcessor().earlyStartup();
			new LogicalContainmentHeirarchyProcessor().earlyStartup();
			//TODO check this
//			new JAXRSAnnotationFactory().earlyStartup();			
			
			new LiveJDTTracker().earlyStartup();
			new ReloCore().earlyStartup();
			new ReloDiagramEngine().earlyStartup();
			new ResourceQueueManager().earlyStartup(); //check
			new SCore().earlyStartup();
			new ChronoDiagramEngine().earlyStartup();
			new StrataDiagramEngine().earlyStartup();
			new VersionBasedStoreInitializerService().earlyStartup(); // does nothing
		} catch (Throwable e) {
			logger.error("Error when Reinitializing: ", e);
		}
		logger.error("Reinitialize complete!!");
		
	}

	public void selectionChanged(IAction action, ISelection selection) {
		// TODO Auto-generated method stub
		
	}

}
