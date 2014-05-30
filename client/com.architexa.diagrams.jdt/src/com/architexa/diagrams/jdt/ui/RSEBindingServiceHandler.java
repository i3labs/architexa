package com.architexa.diagrams.jdt.ui;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.keys.IBindingService;

public class RSEBindingServiceHandler extends AbstractHandler {

	private static IBindingService bindingService;

	public void initialize() {
		if (bindingService == null)
			bindingService  = (IBindingService) PlatformUI.getWorkbench().getService(IBindingService.class);
	}

	public Object execute(ExecutionEvent event) throws ExecutionException {
		return null;
	}

	public IBindingService getBindingService() {
		return bindingService;
	}

}
