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
 * Created on Mar 12, 2005
 *
 */
package com.architexa.diagrams.relo.ui;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.dialogs.WizardNewFileCreationPage;

import com.architexa.diagrams.relo.ReloPlugin;

/**
 * @author vineet
 * 
 * Note: We assume here that the file is created lazily (on save). We therefore
 * do not have file opening logic here.
 */
public class ReloDocWizard extends Wizard {

	private IWorkbench workbench;
	private IStructuredSelection selection;

	public IStructuredSelection getSelection() {
		return selection;
	}

	public IWorkbench getWorkbench() {
		return workbench;
	}

	/**
	 * Creates a wizard for creating a new file resource in the workspace.
	 */
	public ReloDocWizard() {
		super();
	}

	private WizardNewFileCreationPage mainPage;

	/*
	 * (non-Javadoc) Method declared on IWizard.
	 */
	@Override
	public void addPages() {
		super.addPages();
		mainPage = new WizardNewFileCreationPage("newFilePage1", getSelection());
		mainPage.setTitle("Architexa RSE Document");
		mainPage.setDescription("Create a new Architexa RSE Document.");
		mainPage.setFileName("Class.atxa");
		addPage(mainPage);
	}

	/*
	 * (non-Javadoc) Method declared on IWorkbenchWizard.
	 */
	public void init(IWorkbench workbench, IStructuredSelection currentSelection) {
		this.workbench = workbench;
		this.selection = currentSelection;

		initializeDefaultPageImageDescriptor();

		setWindowTitle("New Relo Document");
		setNeedsProgressMonitor(true);
	}

	/*
	 * (non-Javadoc) Method declared on BasicNewResourceWizard.
	 */
	protected void initializeDefaultPageImageDescriptor() {
		// TODO: needed (perhaps) for Eclipse 3.4
		//setDefaultPageImageDescriptor(WorkbenchImages
		//        .getImageDescriptor(IWorkbenchGraphicConstants.IMG_WIZBAN_NEW_WIZ));
		String iconPath = "icons/full/";
		try {
			URL installURL  = ReloPlugin.getDefault().getBundle().getEntry("/");
			URL url = new URL(installURL, iconPath + "wizban/newfile_wiz.gif");//$NON-NLS-1$
			ImageDescriptor desc = ImageDescriptor.createFromURL(url);
			setDefaultPageImageDescriptor(desc);
		} catch (MalformedURLException e) {
			// Should not happen. Ignore.
		}
	}

	/*
	 * Returns null on cancel.
	 */
	public IFile getFile() {
		if (cancelled) 
			return null;
		else
			return mainPage.createNewFile();
	}

	boolean cancelled = false;
	@Override
	public boolean performCancel() {
		cancelled = true;
		return super.performCancel();
	}

	/*
	 * (non-Javadoc) Method declared on IWizard.
	 */
	@Override
	public boolean performFinish() {
		// return false if file cannot be created
		IFile file = mainPage.createNewFile();
		if (file == null)
			return false;

		return true;
	}
}