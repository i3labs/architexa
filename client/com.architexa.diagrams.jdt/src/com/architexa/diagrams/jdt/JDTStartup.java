package com.architexa.diagrams.jdt;

import org.apache.log4j.Logger;
import org.eclipse.ui.IStartup;

import com.architexa.diagrams.Activator;
import com.architexa.diagrams.jdt.ui.EditorSwitchInterceptor;
import com.architexa.diagrams.jdt.ui.RSEOpenTypeHandler;

public class JDTStartup implements IStartup {

	static final Logger logger = Activator.getLogger(JDTStartup.class);

	public void earlyStartup() {
		try {
			earlyStartupInternal();
		} catch (Throwable t) {
			logger.error("Unexpected Error", t);
		}
	}

	private void earlyStartupInternal() {
		EditorSwitchInterceptor linkSwitchInterceptor = new EditorSwitchInterceptor();
		linkSwitchInterceptor.initialize();

		RSEOpenTypeHandler openTypeDialogHandler = new RSEOpenTypeHandler();
		openTypeDialogHandler.initialize();
	}

}
