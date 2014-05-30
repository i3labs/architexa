package com.architexa.collab.ui.dialogs;

import org.apache.log4j.Logger;
import org.eclipse.jface.action.Action;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.Text;

import com.architexa.collab.UIUtils;
import com.architexa.collab.ui.Activator;

/**
 * 
 * @author Elizabeth L. Murnane
 *
 */
public abstract class ShareDialogTab extends Composite {
	static final Logger logger = Activator.getLogger(ShareDialogTab.class);

	boolean disableAll = false;

	protected Text diagramNameField;
	protected String diagramName;

	protected Text descriptionField;
	protected String description="";

	Label tagsLabel;
	protected Text diagramTagsField;
	protected String tags="";

	public ShareDialogTab(TabFolder parent, int style) {
		super(parent, style);
	}

	public void disableAllFields() {
		disableAll = true;
	}

	public boolean isDiagramDiff() {
		return false;
	}
	
	protected void createContent() {

		GridLayout layout = new GridLayout(2, false);
		layout.numColumns = 2;
		layout.verticalSpacing = 10;
		layout.marginWidth = 10;
		setLayout(layout);

		// Area for optional elements before areas for diagram name, 
		// who/what to share with, description, and tags.
		createHeaderArea(this);

		// Name
		GridData diagramNameFieldData = createDiagramNameArea(this);

		// Who/What to share with
		createShareTargetArea(this);

		// Description
		createDiagramDescriptionArea(this);

		// Tags
		createTagsArea(this, diagramNameFieldData);

		updateNameAndDescriptionFields();
	}

	protected abstract void createHeaderArea(Composite container);
	protected abstract void createShareTargetArea(Composite container);

	protected GridData createDiagramNameArea(Composite container) {
		Label diagramNameLabel = new Label(container, SWT.RIGHT);
		diagramNameLabel.setText("Diagram name: ");

		diagramNameField = new Text(container, SWT.SINGLE | SWT.BORDER);
		diagramNameField.setText("");
		GridData diagramNameFieldData = new GridData(GridData.FILL_HORIZONTAL);
		diagramNameFieldData.minimumWidth = 150;
		diagramNameField.setLayoutData(diagramNameFieldData);

		if(disableAll) {
			diagramNameLabel.setEnabled(false);
			diagramNameField.setEnabled(false);
		}

		return diagramNameFieldData;
	}

	protected void createDiagramDescriptionArea(Composite container) {
		Label descriptionLabel = new Label(container, SWT.TOP);
		descriptionLabel.setText("Description: ");
		GridData descriptionLabelData = new GridData(/*GridData.FILL_BOTH | GridData.VERTICAL_ALIGN_FILL*/);
		descriptionLabelData.verticalSpan = 3;
		descriptionLabel.setLayoutData(descriptionLabelData);

		descriptionField =  new Text(container, SWT.MULTI | SWT.BORDER | SWT.WRAP | SWT.V_SCROLL);
		descriptionField.setText("");
		descriptionField.addTraverseListener(new TraverseListener() {
			public void keyTraversed(TraverseEvent e) {
				if (e.detail == SWT.TRAVERSE_TAB_NEXT || e.detail == SWT.TRAVERSE_TAB_PREVIOUS) {
					e.doit = true;
				}
			} 
		});
		GridData descriptionFieldData = new GridData(/*GridData.FILL_BOTH | GridData.VERTICAL_ALIGN_FILL*/);
		descriptionFieldData.verticalSpan = 3;
		descriptionFieldData.minimumHeight = 100;
		descriptionFieldData.minimumWidth = 300;
		descriptionFieldData.widthHint = 350;
		descriptionFieldData.heightHint = 150;
		descriptionField.setLayoutData(descriptionFieldData);

		if(disableAll) {
			descriptionLabel.setEnabled(false);
			descriptionField.setEnabled(false);
		}
	}

	protected void createTagsArea(Composite container, GridData diagramNameFieldData) {
		tagsLabel = new Label(container, SWT.RIGHT);
		tagsLabel.setText("Tags: (comma separated)");

		diagramTagsField = new Text(container, SWT.MULTI | SWT.BORDER | SWT.WRAP);
		diagramTagsField.addTraverseListener(new TraverseListener() {
			public void keyTraversed(TraverseEvent e) {
				if (e.detail == SWT.TRAVERSE_TAB_NEXT || e.detail == SWT.TRAVERSE_TAB_PREVIOUS) {
					e.doit = true;
				}
			}
		});

		diagramTagsField.setLayoutData(diagramNameFieldData);

		if(disableAll) {
			tagsLabel.setEnabled(false);
			diagramTagsField.setEnabled(false);
		}
	}

	protected void updateNameAndDescriptionFields(){
		diagramNameField.setText("");
		descriptionField.setText("");
	}

	/**
	 * @return true if all required fields have been filled in
	 * by the user in an acceptable format, false otherwise
	 */
	protected abstract boolean requiredInfoEntered();

	/**
	 * Opens error dialog if no diagram name entered
	 * @return true if diagram name entered, false if
	 * no name entered and user must still enter a name
	 */
	protected boolean diagramNameEntered() {
		if(diagramNameField.getText()==null || "".equals(diagramNameField.getText().trim())) {
			UIUtils
			.errorPromptDialog("Please enter a name for the diagram.")
			.open();
			return false;
		}
		return true;
	}
	
	public boolean uploadDiff() {
		return false;
	}

	public void share() {
		Action shareAction = getShareAction();
		shareAction.run();
	}

	protected abstract Action getShareAction();

}
