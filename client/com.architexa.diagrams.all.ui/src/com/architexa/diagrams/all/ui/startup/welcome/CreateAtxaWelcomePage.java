package com.architexa.diagrams.all.ui.startup.welcome;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;

public class CreateAtxaWelcomePage extends FormEditor {
	 private FormToolkit toolkit;
	 private ScrolledForm form;

	 /**
	  * The constructor.
	  */
	 
	 
	 public CreateAtxaWelcomePage() {
	 }

	 /**
	  * Passing the focus request to the form.
	  */
	 @Override
	public void setFocus() {	
//	  form.setFocus();
	 }

	 /**
	  * Disposes the toolkit
	  */
	 @Override
	public void dispose() {
	  if (toolkit!=null)
		 toolkit.dispose();
	  super.dispose();
	}

	@Override
	protected void addPages() {
		try {
			addPage(new AtxaWelcomeFormPage(this));
		} catch (PartInitException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void doSave(IProgressMonitor monitor) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void doSaveAs() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isSaveAsAllowed() {
		// TODO Auto-generated method stub
		return false;
	}
	}