package com.architexa.collab.ui.dialogs;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.fieldassist.ContentProposalAdapter;
import org.eclipse.jface.fieldassist.IContentProposal;
import org.eclipse.jface.fieldassist.IContentProposalListener;
import org.eclipse.jface.fieldassist.SimpleContentProposalProvider;
import org.eclipse.jface.fieldassist.TextContentAdapter;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.ListSelectionDialog;

import com.architexa.collab.UIUtils;
import com.architexa.collab.proxy.PluginUtils;
import com.architexa.collab.ui.CollabUIUtils;
import com.architexa.collab.ui.ShareByEmailAction;
import com.architexa.rse.AccountSettings;
import com.architexa.rse.BuildStatus;

public class ShareByEmailTab extends ShareDialogTab {

	Text toField;
	String[] recipientEmails;

	public ShareByEmailTab(TabFolder tabFolder, int style) {
		super(tabFolder, style);
	}

	@Override
	protected void createHeaderArea(Composite dialogArea) {
	}

	@Override
	protected void createShareTargetArea(Composite dialogArea) {
		Label toLabel = new Label(dialogArea, SWT.TOP);
		toLabel.setText("Email address(es) to send to: ");
		GridData toLabelData = new GridData(GridData.FILL_BOTH | GridData.VERTICAL_ALIGN_FILL);
		toLabelData.verticalSpan = 2;
		toLabel.setLayoutData(toLabelData);

		// Area containing the "Email address(es) to send to: "
		// text field and the link to open address book
		Composite fieldArea = new Composite(dialogArea, SWT.NONE);
		GridLayout fieldAreaLayout = new GridLayout(1, false);
		fieldAreaLayout.marginWidth = 0;
		fieldAreaLayout.marginHeight = 0;
		fieldAreaLayout.verticalSpacing = 0;
		fieldArea.setLayout(fieldAreaLayout);
		GridData fieldAreaData = new GridData(GridData.FILL_HORIZONTAL);
		fieldAreaData.verticalSpan = 2;
		fieldArea.setLayoutData(fieldAreaData);

		// Email address recipients text field

		// Since 3.3, can give Text a SWT.SEARCH style and use Text.setMessage() 
		// to display the field text as a "hint" (gray text that explains the 
		// purpose of the field)
		double jdtUIVer = PluginUtils.getPluginVer("org.eclipse.jdt.ui");
		String hintMsg = "Separate each address with a semicolon";
		if(jdtUIVer >= 3.3) {
			toField = new Text(fieldArea, SWT.SINGLE | SWT.BORDER | 1 << 7); // SWT.SEARCH = 1 << 7;
			toField.setText("");
			try {
				// set hint message:
				Method mth = toField.getClass().getMethod("setMessage", String.class);
				mth.invoke(toField, hintMsg);
			} catch(Exception e) {
				logger.error("Unexpected exception while setting hint of To: field ", e);
			}
		} else {
			toField = new Text(fieldArea, SWT.SINGLE | SWT.BORDER);
			toField.setText(hintMsg); // For 3.2, just make the field text the purpose explanation
		}

		GridData toFieldData = new GridData(GridData.FILL_HORIZONTAL);
		toFieldData.minimumWidth = 150;
		toField.setLayoutData(toFieldData);

		// Link to open address book to select contacts for To: field
		Link addressBookLink = new Link(fieldArea, SWT.RIGHT);
		addressBookLink.setText("<a>Choose from Address Book</a>");
		addressBookLink.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {}
			public void widgetSelected(SelectionEvent e) {
				chooseFromAddressBook();
			}
		});

		// Auto-complete the To: field using user's address book
		String[] contactEmails = AccountSettings.getStoredContacts();
		// No need to try auto completing if address book is empty
		if(AccountSettings.isAddressBookEmpty()) return;
		SimpleContentProposalProvider proposalProvider = new SimpleContentProposalProvider(contactEmails) {
			@Override
			public IContentProposal[] getProposals(String contents, int position) {
				// Only look for proposals for email currently being typed; ignore
				// any previously entered email, which will be separated from the
				// current one with a semicolon
				int startOfContactBeingTyped = contents.lastIndexOf(";")+1;
				String contactBeingTyped = contents.substring(startOfContactBeingTyped);
				// Replace any extra space at the end or beginning
				contactBeingTyped = contactBeingTyped.trim();
				IContentProposal[] proposals = super.getProposals(contactBeingTyped, position);
				// Sort proposals alphabetically
				Comparator<IContentProposal> alphabeticalProposalComparator = new Comparator<IContentProposal>() {
					public int compare(IContentProposal p1, IContentProposal p2) {
						return p1.getContent().compareToIgnoreCase(p2.getContent());
					}
				};
				Arrays.sort(proposals, alphabeticalProposalComparator);
				return proposals;
			}
		};

//		proposalProvider.setFiltering(true); (only available since 3.3)
		if(jdtUIVer >= 3.3) {
			try {
				// show only contacts matching what user has typed so far
				Method mth = proposalProvider.getClass().getMethod("setFiltering", boolean.class);
				mth.invoke(proposalProvider, true);
			} catch(Exception e) {
				logger.error("Unexpected exception while setting content proposal provider " +
						"to filter proposals based on the current field content", e);
			}
		}

		final ContentProposalAdapter proposalAdapter = new ContentProposalAdapter(
				toField,
				new TextContentAdapter(),
				proposalProvider,
				null,
				null);
		
		// Handle the user accepting a suggestion from the auto-complete list
		proposalAdapter.setProposalAcceptanceStyle(ContentProposalAdapter.PROPOSAL_IGNORE);
		proposalAdapter.addContentProposalListener(new IContentProposalListener() {
			public void proposalAccepted(IContentProposal proposal) {
				// Replace the contact currently being typed with the proposal
				// by removing what has been typed so far of that current contact 
				// and appending the accepted proposal for it onto any previously
				// entered contacts already in the field
				int startOfContactBeingTyped = toField.getText().lastIndexOf(";")+1;
				String previouslyEnteredContacts = 
					startOfContactBeingTyped==0 ? "" : toField.getText(0, startOfContactBeingTyped);
				String contents = previouslyEnteredContacts+proposal.getContent()+";";
				// Put the cursor at the end
				int cursorPos = contents.length();
				proposalAdapter.getControlContentAdapter().setControlContents(
						proposalAdapter.getControl(), 
						contents, 
						cursorPos);
			}
		});
	}

	/* Opens a selection dialog containing a list of all contacts. Any selected
	 * contacts are appended to the recipient field. If the user's address book
	 * contains no contacts, he gets an error message and the selection dialog 
	 *  doesn't open.
	 */
	private void chooseFromAddressBook() {
		Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
		if(AccountSettings.isAddressBookEmpty()) {
			UIUtils.errorPromptDialog("No Contacts", 
			"You have no contacts in your address book").open();
			return;
		}
		List<String> contacts = Arrays.asList(AccountSettings.getStoredContacts());
		Collections.sort(contacts, CollabUIUtils.getAlphabeticalIgnoringCaseComparator());
		ListSelectionDialog addressBookDialog = new ListSelectionDialog(
				shell, 
				contacts, 
				new ArrayContentProvider(), 
				new LabelProvider(), 
		"Choose from Contacts:");
		addressBookDialog.setTitle("Address Book");
		if(addressBookDialog.open()==Window.OK) {
			Object[] selections = addressBookDialog.getResult();
			if(selections==null || selections.length==0) return;

			String enteredText = toField.getText().trim();
			if(enteredText.length()>0) {
				// user already has some email addresses entered, so make
				// sure there is a ";" after the last one before appending
				// any address selected from the address book
				String lastChar = enteredText.substring(enteredText.length()-1);
				if(!";".equals(lastChar)) toField.append(";");
			}
			for(Object selection : selections) {
				toField.append((String)selection+";");
			}
		}
	}

	@Override
	// Disable tags when sharing by email (but leave area visible so user knows that
	// tags support exists and is encouraged to share on server instead to use it)
	protected void createTagsArea(Composite dialogArea, GridData diagramNameFieldData) {
		super.createTagsArea(dialogArea, diagramNameFieldData);
		tagsLabel.setEnabled(false);
		diagramTagsField.setEnabled(false);
		diagramTagsField.setText("Tags only available when sharing on server");
	}

	@Override
	protected boolean requiredInfoEntered() {
		if(!diagramNameEntered()) return false; // Open error if no diagram name entered
		if(diagramNameField.getText().length()<3) {
			// Open error if diagram name too short to use as name
			// of temp attachment file created later by ShareByEmailAction
			UIUtils.errorPromptDialog("Diagram name must be at least 3 characters long.")
			.open();
			return false;
		}
		if(toField.getText()==null || "".equals(toField.getText().trim())) {
			// Open error if no recipient(s) entered
			UIUtils
			.errorPromptDialog("Please enter at least one email " +
			"address to send this diagram to.")
			.open();
			return false;
		}

		diagramName = diagramNameField.getText();
		// We tell user to separate each recipient with semicolon, but
		// also handling user separating recipients with comma
		recipientEmails = toField.getText().replace(",", ";").split(";");
		description = descriptionField.getText();
		return true;
	}

	@Override
	public Action getShareAction() {
		BuildStatus.addUsage("EmailShare");
		return new ShareByEmailAction(diagramName, description, recipientEmails);
	}

}
