/* 
 * Copyright (c) 2004-2005 Massachusetts Institute of Technology. This code was
 * developed as part of the Haystack (http://haystack.lcs.mit.edu/) research 
 * project at MIT. Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation files (the 
 * "Software"), to deal in the Software without restriction, including without 
 * limitation the rights to use, copy, modify, merge, publish, distribute, 
 * sublicense, and/or sell copies of the Software, and to permit persons to whom
 * the Software is furnished to do so, subject to the following conditions: 
 * 
 * The above copyright notice and this permission notice shall be included in 
 * all copies or substantial portions of the Software. 
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR 
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, 
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE 
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER  
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, 
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE. 
 */

/*
 * Created on Jun 15, 2004
 */
package com.architexa.diagrams.relo.actions;

import java.util.Iterator;
import java.util.Map;

import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPart;

import com.architexa.org.eclipse.draw2d.PolylineConnection;
import com.architexa.org.eclipse.draw2d.geometry.PointList;
import com.architexa.org.eclipse.draw2d.geometry.Rectangle;
import com.architexa.org.eclipse.gef.ConnectionEditPart;
import com.architexa.org.eclipse.gef.EditPart;
import com.architexa.org.eclipse.gef.EditPartViewer;
import com.architexa.org.eclipse.gef.GraphicalEditPart;
import com.architexa.org.eclipse.gef.GraphicalViewer;
import com.architexa.org.eclipse.gef.Request;
import com.architexa.org.eclipse.gef.RequestConstants;
import com.architexa.org.eclipse.gef.ui.actions.GEFActionConstants;
import com.architexa.org.eclipse.gef.ui.actions.SelectionAction;

/**
 * @author vineet
 *
 */
public class FisheyeSemanticZoomInAction extends SelectionAction {

	public static final Request zoomInRequest = new Request("zoom in");

	public FisheyeSemanticZoomInAction(IEditorPart editor) {
		this((IWorkbenchPart) editor);
	}

	public FisheyeSemanticZoomInAction(IWorkbenchPart part) {
		super(part);
	}

	/**
	 * @see com.architexa.org.eclipse.gef.ui.actions.WorkbenchPartAction#init()
	 */
	@Override
    protected void init() {
		super.init();
		setText("Zoom In");
		setToolTipText("Makes current item larger");
		setId(GEFActionConstants.ZOOM_IN);
	}

	// need to convert from DirectEditAction to FisheyeSemanticZoomInAction

	/**
	 * returns <code>true</code> if there is exactly 1 EditPart selected that understand
	 * a request of type: {@link RequestConstants#REQ_DIRECT_EDIT}.
	 * @return <code>true</code> if enabled
	 */
	@Override
    protected boolean calculateEnabled() {
		//if (getSelectedObjects().size() == 1
		//  && (getSelectedObjects().get(0) instanceof EditPart)) {
		//	EditPart part = (EditPart)getSelectedObjects().get(0);
		//	return part.understandsRequest(zoomInRequest);
		//}
		//return false;

		// let's just enable always for now
		return true;
	}

	/**
	 * @see org.eclipse.jface.action.IAction#run()
	 */
	@Override
    public void run() {
		//((EditPart)getSelectedObjects().get(0)).getRoot().refresh();
		// lets walk the part registry
	    IWorkbenchPart editor = getWorkbenchPart();
	    EditPartViewer partViewer = (EditPartViewer) editor.getAdapter(GraphicalViewer.class);
		Map modelToEPMap = partViewer.getEditPartRegistry();
		
		Iterator epRegWalker = modelToEPMap.values().iterator();
		while (epRegWalker.hasNext()) {
			GraphicalEditPart gep = (GraphicalEditPart) epRegWalker.next();
			if (!(gep instanceof ConnectionEditPart)) {
				System.err.println("GEP: " + gep.toString());
				Rectangle bounds = gep.getFigure().getBounds();
				System.err.println("     " + bounds);
			}
		}
		epRegWalker = modelToEPMap.values().iterator();
		while (epRegWalker.hasNext()) {
			EditPart ep = (EditPart) epRegWalker.next();
			if (ep instanceof ConnectionEditPart) {
				ConnectionEditPart cep = (ConnectionEditPart) ep;
				System.err.println("CEP: " + cep.toString());
				Rectangle bounds = cep.getFigure().getBounds();
				System.err.println("     " + bounds);
				PointList pl = ((PolylineConnection)cep.getFigure()).getPoints();
				for (int i=0;i<pl.size();i++) {
					System.err.println("     " + pl.getPoint(i));
				}
			}
		}

		try {
			EditPart part = (EditPart) getSelectedObjects().get(0);
			part.performRequest(zoomInRequest);
		} catch (ClassCastException e) {
			Display.getCurrent().beep();
		} catch (IndexOutOfBoundsException e) {
			Display.getCurrent().beep();
		}
	}

}
