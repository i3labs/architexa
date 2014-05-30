package com.architexa.rse;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IBundleGroup;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.internal.ui.compare.ResizableDialog;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.osgi.framework.adaptor.BundleData;
import org.eclipse.osgi.framework.internal.core.AbstractBundle;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PlatformUI;
import org.osgi.framework.Bundle;

import com.architexa.collab.UIUtils;
import com.architexa.collab.proxy.PluginUtils;
import com.architexa.intro.AtxaIntroPlugin;


public class FeedbackDialog extends ResizableDialog {
	static final Logger s_logger = AtxaIntroPlugin.getLogger(FeedbackDialog.class);

    private Text commentsEdit;
    private Text emailEdit;
    private Combo typeDropDown;
	private Button errorLogCB;
    
    public static class FeedbackDlgAction implements  IWorkbenchWindowActionDelegate {
		public void run(IAction action) {
        	FeedbackDialog dlg = new FeedbackDialog(WorkbenchUtils.getActiveWorkbenchWindowShell());
        	dlg.open();
		}

		public void dispose() {}

		public void init(IWorkbenchWindow window) {}

		public void selectionChanged(IAction action, ISelection selection) {}

		public static void init(IAction myAction) {
			myAction.setText("Provide Feedback");
			myAction.setImageDescriptor(AtxaIntroPlugin.getImageDescriptor("icons/mail-forward.png"));
		}
    }

    protected FeedbackDialog(Shell parentShell) {
		super(parentShell, null);
	}

    @Override
	protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText("Architexa RSE Feedback");
     }

    @Override
	protected Control createDialogArea(Composite parent) {
        Composite composite = (Composite) super.createDialogArea(parent);

        Label messageLabel = new Label(composite, SWT.WRAP);
        messageLabel.setText("Feedback (or bug reports) will help in focusing efforts within the Architexa Suite.");
        GridData data = new GridData(GridData.GRAB_HORIZONTAL
                | GridData.HORIZONTAL_ALIGN_FILL
                | GridData.VERTICAL_ALIGN_BEGINNING);
        data.widthHint = convertHorizontalDLUsToPixels(IDialogConstants.MINIMUM_MESSAGE_AREA_WIDTH);
        messageLabel.setLayoutData(data);
        
        createBodyArea(composite);
        
        return composite;
     }

	private void createBodyArea(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        GridData data = new GridData(GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL
                | GridData.HORIZONTAL_ALIGN_FILL
                | GridData.VERTICAL_ALIGN_FILL);
        data.widthHint = convertHorizontalDLUsToPixels(IDialogConstants.MINIMUM_MESSAGE_AREA_WIDTH);
        composite.setLayoutData(data);

        GridLayout layout = new GridLayout();
        layout.numColumns = 2;
        composite.setLayout(layout);

        Label lbl;

        // lbl = new Label(composite, SWT.WRAP);
		// lbl.setText("Name:\n(optional)");
		// lbl.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false));
		//
		// nameEdit = new Text(composite, SWT.SINGLE | SWT.BORDER);
		// nameEdit.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL));
		
		if (AccountSettings.getStoredAccountEmail().equals("")) {
        
			lbl = new Label(composite, SWT.WRAP);
			lbl.setText("E-mail address:\n(optional)");
			lbl.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false));
			
			emailEdit = new Text(composite, SWT.SINGLE | SWT.BORDER);
			emailEdit.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL));
		}
		
		//AccountConnection accountConnector = new AccountConnection();
		//if (AccountStatus.isAccountValid() && accountConnector.getApi() != null) {
		//	try {
		//		nameEdit.setText(accountConnector.getApi().users().getMyName());
		//	} catch (ConnectException e) {
		//		// errors should not occur since api is checked above
		//		UIUtils.errorPromptDialog("Could not connect to server").open();
		//		return;
		//	} catch (UnauthorizedException e) {
		//		UIUtils.errorPromptDialog("Could not connect to server").open();
		//		return;
		//	}
		//	emailEdit.setText(AccountSettings.getStoredAccountEmail());
		//}
        
        lbl = new Label(composite, SWT.WRAP);
        lbl.setText("Feedback Type:");
        lbl.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false));
        
        typeDropDown = new Combo(composite, SWT.DROP_DOWN|SWT.READ_ONLY);
        typeDropDown.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
        typeDropDown.add("Bug Report");
        typeDropDown.select(0); // Make "Bug Report" the default selection
        typeDropDown.add("Question");
        typeDropDown.add("Suggestion");
        typeDropDown.add("Other");
        
        lbl = new Label(composite, SWT.WRAP);
        lbl.setText("Comments:");
        lbl.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false));
        
//        Text commentsEdit = new Text(composite, SWT.BORDER | SWT.WRAP | SWT.MULTI);
//		GridData gridData = new GridData();
//		gridData.horizontalAlignment = SWT.FILL;
//		gridData.grabExcessHorizontalSpace = true;
//		gridData.verticalAlignment = SWT.FILL;
//		gridData.grabExcessVerticalSpace = true;
//		commentsEdit.setLayoutData(gridData);
        
		commentsEdit = new Text(composite, SWT.MULTI | SWT.BORDER | SWT.WRAP);
        commentsEdit.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 5));
//		commentsEdit.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_VERTICAL));
//        
//		lbl = new Label(composite, SWT.WRAP);
//		lbl.setText(" ");
//		lbl.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false));
//		
//		lbl = new Label(composite, SWT.WRAP);
//		lbl.setText(" ");
//		lbl.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false));
//		
//		lbl = new Label(composite, SWT.WRAP);
//		lbl.setText(" ");
//		lbl.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false));
//		
//		lbl = new Label(composite, SWT.WRAP);
//		lbl.setText(" ");
//		lbl.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false));
		
		// this would need to be enabled only for bug reports
		errorLogCB = new Button(composite, SWT.CHECK);
		errorLogCB.setText("Transmit Error Log");
		errorLogCB.setSelection(true);
		errorLogCB.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));
		errorLogCB.setVisible(true); // visible since "Bug Report" will show as default option

		typeDropDown.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				if (e.getSource() instanceof Combo && ((Combo)e.getSource()).getSelectionIndex() == 0)
					errorLogCB.setVisible(true);
				else
					errorLogCB.setVisible(false);
			}
			public void widgetDefaultSelected(SelectionEvent e) {
			}
        });
		
		lbl = new Label(composite, SWT.WRAP);
		lbl.setText("(Clicking 'Send Feedback' will send your Eclipse configuration" +
					" \ninformation to us so that we can better assist you)");
		lbl.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));
		
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
        createButton(parent, IDialogConstants.OK_ID, "Send Feedback", true);
        createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
    }

	@Override
	protected void buttonPressed(int buttonId) {
		if (buttonId == IDialogConstants.CANCEL_ID) close();
    	
    	if (buttonId == IDialogConstants.OK_ID) {
    		this.getButton(IDialogConstants.OK_ID).setText("Sending...");
    		this.getButton(IDialogConstants.OK_ID).setEnabled(false);
    		try {
				sendFeedback();
			} catch (IOException e) {

				Display.getDefault().asyncExec(new Runnable() {
				
					public void run() {
						MessageDialog md = UIUtils.promptDialogWithCustomButtons(MessageDialog.ERROR, "Error Sending Feedback", 
								"Could not send feedback email: Please Try Again.\n",
								new String[] {"Try Again","Cancel"});
						openDialog(md);
						
				}});
				
				s_logger.error("Error while sending feedback", e);
			}
			close();
    	}
    }

	protected void openDialog(MessageDialog md) {
		int returnVal = md.open();
		if (returnVal == 0) {
			try {
				sendFeedback();
			} catch (IOException e) {
				openDialog(md);
			}
		} else
			close();
	}

	private void sendFeedback() throws IOException {
		//System.err.println(this.commentsEdit.getText());

		URL url;
		URLConnection urlConn;

		// URL of CGI-Bin script.
		url = new URL("http://www.architexa.com/about/send-feedback");

		urlConn = url.openConnection();
		urlConn.setDoInput(true);
		urlConn.setUseCaches(false);
		urlConn.setDoOutput(true);

		urlConn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

		String email = AccountSettings.getStoredAccountEmail();
		if (email.equals("")) email = emailEdit.getText();
	    String content =
			"email=" + URLEncoder.encode (email, "UTF-8") +
			"&feedbackType=" + URLEncoder.encode (this.typeDropDown.getText(), "UTF-8")	+
			"&comments=" + URLEncoder.encode (this.commentsEdit.getText(), "UTF-8")	+
			"&errors=";

		//"&config=" + URLEncoder.encode (Platform.getConfigurationLocation().toString(), "UTF-8") +
	    FileInputStream errorLog = null;
	    DataOutputStream printout = new DataOutputStream (urlConn.getOutputStream ());
	    printout.writeBytes (content);
	 
	    try {	 
	    	errorLog = new FileInputStream(Platform.getLogFileLocation().toFile());
	         if (errorLogCB.getSelection() && errorLog!=null) {
		    	int c;
		    	 while ((c = errorLog.read()) != -1) 
		         {
		    		 printout.write(c);
		         }
		    }
	    } catch (FileNotFoundException e) {
	    	 printout.writeChars("No Error Log Found");
	    }
	    
	    // get plugin config info and version numbers
		String configContents = "&config=";
		IBundleGroup[] bundleGroups = Platform.getBundleGroupProviders()[0].getBundleGroups(); //[0].getBundles();
		for (int i=0; i<bundleGroups.length; i++) {
			Bundle[] bundles =  bundleGroups[i].getBundles();
			configContents += "\n"+bundleGroups[i].getName() + " - " +  bundleGroups[i].getVersion();
			for (int j=0; j<bundles.length; j++) {
				configContents += "\n"+bundles[j].getSymbolicName() + " - " +  getVersion(bundles[j]);	
			}
			configContents += "\n";
		}

		printout.writeBytes (configContents);

		printout.flush ();
	    printout.close ();

	    // Get response data (otherwise the connection is not happy).
		DataInputStream input;
	    input = new DataInputStream (urlConn.getInputStream ());
	    BufferedReader rdr = new BufferedReader(new InputStreamReader(input));
	    while (null != rdr.readLine()) {}
		//System.out.println (str);
	    rdr.close();
	    input.close();
	}

	private static double jdtUIVer = PluginUtils.getPluginVer("org.eclipse.jdt.ui");
	
	private String getVersion(Bundle bundle) {
		
		//3.5+ call existing method
		if (jdtUIVer >= 3.5 )
		{	
			try {
				Method mth = Bundle.class.getMethod("getVersion", null);
				return mth.invoke(bundle, null).toString();
			} catch (Exception e) {
				System.err.println("Issue stemming from method getVersion in Bundle: " + e);
			} 
		} else {
		
			try {
				Field[] fields = AbstractBundle.class.getDeclaredFields();
				for (int i =0; i< fields.length; i++) {
					if (!fields[i].getName().equals("bundledata")) continue;
					fields[i].setAccessible(true);
					BundleData bd = (BundleData) fields[i].get(bundle);
					return (String) bd.getVersion().toString();
				}
			} catch (Throwable e) {
				s_logger.error("Error while sending feedback", e);
			}
		}
		return "";
	}
	
	
}
