package com.architexa.rse.prod;

import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.application.WorkbenchWindowAdvisor;
import org.eclipse.ui.internal.ide.application.DelayedEventsProcessor;
import org.eclipse.ui.internal.ide.application.IDEWorkbenchAdvisor;
import org.eclipse.ui.internal.ide.application.IDEWorkbenchWindowAdvisor;

@SuppressWarnings("restriction")
public class RSEWorkbenchAdvisor extends IDEWorkbenchAdvisor {
	
	public RSEWorkbenchAdvisor(DelayedEventsProcessor processor) {
		super(processor);
	}

	@Override
	public String getInitialWindowPerspectiveId() {
		return "com.architexa.rse.prod.ArchitexaBrowsingPerspective";
	}

	@Override
	public WorkbenchWindowAdvisor createWorkbenchWindowAdvisor(IWorkbenchWindowConfigurer configurer) {
		return new IDEWorkbenchWindowAdvisor(this, configurer) {
			@Override
			public void preWindowOpen() {
				super.preWindowOpen();
				getWindowConfigurer().setShowPerspectiveBar(false);
			}
		};
	}

}
