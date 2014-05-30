package com.architexa.diagrams.generate.debugger;

import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.DebugElement;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.core.model.IDebugTarget;

public class DebugElementAdapterFactory implements IAdapterFactory {

	// The supported types that we can adapt to
	private static final Class[] types = {
		DebugElement.class,
	};


	public Object getAdapter(Object launch, Class debugElementType) {
		if (!IDebugElement.class.equals(debugElementType) ||
				!(launch instanceof ILaunch)) return null;
		IDebugTarget firstTarget = ((ILaunch)launch).getDebugTarget();
		if(firstTarget!=null)
			return firstTarget;
		return null;
	}


	public Class[] getAdapterList() {
		return types;
	}



}
