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
 * Created on Aug 26, 2005
 */
package com.architexa.diagrams.jdt.utils;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

public class ArrayMultipleElementSelectionDialog extends Dialog {
    private final Object[] arr;
    private final String dlgTitle;
    public Object[] sel = null;
    private final LabelProvider arrProvider;
    private StructuredViewer viewer;

    public ArrayMultipleElementSelectionDialog(Shell shell, String dlgTitle, Object[] arr, LabelProvider arrProvider) {
        super(shell);
        this.dlgTitle = dlgTitle;
        this.arr = arr;
        this.arrProvider = arrProvider;
    }

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText(dlgTitle);
     }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite composite = (Composite)super.createDialogArea(parent);
        viewer = new TableViewer(composite, SWT.BORDER | SWT.CHECK);
        viewer.getControl().setLayoutData(new GridData(GridData.FILL_BOTH));
        viewer.setContentProvider(new ArrayContentProvider());
        viewer.setLabelProvider(arrProvider);
        viewer.setInput(arr);
        viewer.setSelection(new StructuredSelection(arr[0]));
        return composite;
     }

    @Override
    protected void okPressed() {
        ISelection ssel = viewer.getSelection();
        if (ssel instanceof IStructuredSelection)
            sel = ((IStructuredSelection)ssel).toArray();
        super.okPressed();
    }
}