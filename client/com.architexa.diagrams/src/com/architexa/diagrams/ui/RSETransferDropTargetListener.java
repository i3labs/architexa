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
 * Created on Feb 3, 2005
 *
 */
package com.architexa.diagrams.ui;

import java.util.List;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.ui.views.navigator.LocalSelectionTransfer;

import com.architexa.diagrams.ui.RSEEditorViewCommon.IRSEEditorViewCommon;
import com.architexa.org.eclipse.gef.EditPartViewer;
import com.architexa.org.eclipse.gef.Request;
import com.architexa.org.eclipse.gef.dnd.AbstractTransferDropTargetListener;
import com.architexa.org.eclipse.gef.requests.CreateRequest;
import com.architexa.org.eclipse.gef.requests.CreationFactory;

/**
 * @author vineet
 *
 */
public class RSETransferDropTargetListener extends AbstractTransferDropTargetListener {

	private final IRSEEditorViewCommon rseEVC;

	public RSETransferDropTargetListener(RSEEditorViewCommon.IRSEEditorViewCommon rseEVC, EditPartViewer viewer) {
        super(viewer, LocalSelectionTransfer.getInstance());
		this.rseEVC = rseEVC;
    }

    /* (non-Javadoc)
     * @see org.eclipse.gef.dnd.AbstractTransferDropTargetListener#updateTargetRequest()
     */
    @Override
    protected void updateTargetRequest() {
        ((CreateRequest)getTargetRequest()).setLocation(getDropLocation());
    }

    protected List<?> dropValues = null;
    
    @Override
    protected Request createTargetRequest() {
        CreateRequest request = new CreateRequest();
        request.setFactory(new CreationFactory() {
            public Object getNewObject() {
                // we might have multiple objects so we return the list itself
                return dropValues;
            }
            public Object getObjectType() {
                return List.class;
            }});
    	return request;
    }

    @Override
    protected void handleDragOver() {
    	// Following line needed because if drop detail left as 
    	// DND.DROP_MOVE, drag source will assume the resource 
    	// is somewhere else after the drop and remove the original
        getCurrentEvent().detail = DND.DROP_COPY;
        super.handleDragOver();
    }

    @Override
    protected void handleDrop() {
    	// Following line needed because if drop detail left as 
    	// DND.DROP_MOVE, drag source will assume the resource 
    	// is somewhere else after the drop and remove the original
        getCurrentEvent().detail = DND.DROP_COPY;
        if (getCurrentEvent().data != null && getCurrentEvent().data instanceof StructuredSelection) {
            StructuredSelection sel = (StructuredSelection) getCurrentEvent().data;
            dropValues = sel.toList();
        }
        super.handleDrop();
    }

    @Override
	public boolean isEnabled(DropTargetEvent event) {
		Transfer transfer = getTransfer();
		if(!(transfer instanceof LocalSelectionTransfer) ||
				!(((LocalSelectionTransfer)transfer).getSelection() instanceof IStructuredSelection))
			return super.isEnabled(event);

		IStructuredSelection sel = 
			(IStructuredSelection) ((LocalSelectionTransfer)transfer).getSelection();

		boolean candrop = rseEVC.canDropOnEditor(sel);
		if (candrop == false) return false;

		return super.isEnabled(event);
	}

}
