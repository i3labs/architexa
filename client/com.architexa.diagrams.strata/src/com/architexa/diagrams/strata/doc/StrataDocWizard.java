/**
 * Copyright (c) 2007-2008 Architexa, Inc
 */ 
package com.architexa.diagrams.strata.doc;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.WizardNewFileCreationPage;

public class StrataDocWizard extends Wizard {

	private WizardNewFileCreationPage mainPage;

	private IStructuredSelection selection;

	public IStructuredSelection getSelection() {
		return selection;
	}

	public void init(IWorkbench workbench, IStructuredSelection currentSelection) {
		this.selection = currentSelection;
		
		initializeDefaultPageImageDescriptor();

		setWindowTitle("New Strata Document");
		setNeedsProgressMonitor(true);
	}

	protected void initializeDefaultPageImageDescriptor() {
		String iconPath = "icons/full/";
		try {
			URL installURL = Platform.getBundle(PlatformUI.PLUGIN_ID).getEntry("/");
			URL url = new URL(installURL, iconPath + "wizban/new_wiz.png");
			ImageDescriptor desc = ImageDescriptor.createFromURL(url);
			setDefaultPageImageDescriptor(desc);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
	}
	
	@Override
    public void addPages() {
		super.addPages();
		mainPage = new WizardNewFileCreationPage("newFilePage1", getSelection());
		mainPage.setTitle("Strata Document");
		mainPage.setDescription("Create a new Strata Document.");
		mainPage.setFileName("Layered.atxa");
		addPage(mainPage);
	}

	boolean cancelled = false;

	@Override
    public boolean performCancel() {
		cancelled = true;
		return super.performCancel();
	}

	public IFile getFile() {
		if (cancelled) 
			return null;
		else
			return mainPage.createNewFile();
	}

	@Override
    public boolean performFinish() {
		// return false if file cannot be created
		IFile file = mainPage.createNewFile();
		if (file == null)
			return false;

		return true;
	}

}
