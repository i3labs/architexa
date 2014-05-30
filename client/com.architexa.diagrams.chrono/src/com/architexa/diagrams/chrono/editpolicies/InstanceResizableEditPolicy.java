package com.architexa.diagrams.chrono.editpolicies;

import java.util.ArrayList;
import java.util.List;

import com.architexa.org.eclipse.draw2d.PositionConstants;
import com.architexa.org.eclipse.gef.GraphicalEditPart;
import com.architexa.org.eclipse.gef.editpolicies.ResizableEditPolicy;
import com.architexa.org.eclipse.gef.handles.ResizableHandleKit;

public class InstanceResizableEditPolicy extends ResizableEditPolicy {
	@Override
	protected List createSelectionHandles() {
		List list = new ArrayList();
		ResizableHandleKit.addHandle((GraphicalEditPart)getHost(), list,PositionConstants.EAST);
		ResizableHandleKit.addHandle((GraphicalEditPart)getHost(), list,PositionConstants.WEST);
		return list;
	}
}
