package com.architexa.collab;

import java.net.URL;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Link;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.browser.IWebBrowser;
import org.eclipse.ui.browser.IWorkbenchBrowserSupport;


public class UIUtils {

	public static MessageDialog getConnectErrorWithFirewallMessage(
			int dlgType, String title, String preFirewallMessage) {
		return promptDialogWithLink(dlgType, 
				title,
				preFirewallMessage +
				"\n\nBehind a firewall?" +
				"\nInformation for connecting is available here, under 'Licensing Issues'",
				"http://www.architexa.com/support/faq");
	}

	/**
	 * This message NEEDS to be called from the UI thread
	 */
	public static MessageDialog promptDialog(int dlgType, String title, String msg) {
		return new MessageDialog(
				PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), 
				title,
				null, 
				msg, 
				dlgType, 
				new String[]{"OK"}, 
				1);
	}
	
	public static MessageDialog promptDialogWithCustomButtons(int dlgType, String title, String msg, String[] buttons) {
		return new MessageDialog(
				PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), 
				title,
				null, 
				msg, 
				dlgType, 
				buttons, 
				1);
	}
	
	public static MessageDialog promptDialogWithPreferenceCheckBox(int dlgType, String title, String msg, String[] buttons, final String checkBoxTxt, final String prefKey, final IPreferenceStore atxaPrefStore) {
		return new MessageDialog(
				PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), 
				title,
				null, 
				msg, 
				dlgType, 
				buttons, 
				1) {
			@Override
			protected Control createCustomArea(Composite composite) {
				Composite msgArea = new Composite(composite, SWT.NONE);
				GridLayout msgAreaLayout = new GridLayout();
				msgAreaLayout.numColumns = 2;
				msgAreaLayout.marginLeft = 40;
				msgArea.setLayout(msgAreaLayout);
				if (checkBoxTxt!="") {
					
					Button checkBox = new Button(msgArea, SWT.CHECK);
					checkBox.setText(checkBoxTxt);
					
					checkBox.addSelectionListener(new SelectionListener() {
						public void widgetDefaultSelected(SelectionEvent e) {}
						public void widgetSelected(SelectionEvent e) {
							try {
								atxaPrefStore.setValue(prefKey, true);
							} catch (Throwable t) {
								t.printStackTrace();
							}
						}});
				}
				return composite;
			}};
		
	}
	
	public static MessageDialog promptDialogWithLink(int dlgType, String title, String msg, final String url) {
		return promptDialogWithLinkEmbedded(dlgType, title, msg, url, false);
	}
	
	public static MessageDialog promptDialogWithLinkEmbedded(int dlgType, final String title, String msg, final String url, final boolean embed) {
		return new MessageDialog(
				PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), 
				title,
				null, 
				msg, 
				dlgType, 
				new String[]{"OK"}, 
				1) {
			@Override
			protected Control createCustomArea(Composite composite) {
				Composite msgArea = new Composite(composite, SWT.NONE);
				GridLayout msgAreaLayout = new GridLayout();
				msgAreaLayout.numColumns = 2;
				msgAreaLayout.marginLeft = 40;
				msgArea.setLayout(msgAreaLayout);
				if (url!="") {
										
					Link diagramLink = new Link(msgArea , SWT.LEFT);
					diagramLink.setText("<a>"+url+"</a>");
					diagramLink.addSelectionListener(new SelectionListener() {
						public void widgetDefaultSelected(SelectionEvent e) {}
						public void widgetSelected(SelectionEvent e) {
							try {
								if (embed) {
									IWorkbenchBrowserSupport browserSupport = CollabPlugin.getDefault().getWorkbench().getBrowserSupport();
								    IWebBrowser browser;
									browser = browserSupport.createBrowser(IWorkbenchBrowserSupport.LOCATION_BAR, null,title, title);
									URL url2 = new URL(url);
									browser.openURL(url2);
								} else
									PlatformUI.getWorkbench().getBrowserSupport().getExternalBrowser().openURL(new URL(url));
							} catch (Throwable t) {
								t.printStackTrace();
							}
						}});
				}
				return composite;
			}};
	}
	
	// @Deprecated - Methods should ideally call the 'open...' version - as it runs in the UI thread
	public static MessageDialog errorPromptDialog(String title, String msg) {
		return promptDialog(MessageDialog.ERROR, title, msg);
	}

	// @Deprecated - Methods should ideally call the 'open...' version - as it runs in the UI thread
	public static MessageDialog errorPromptDialog(String msg) {
		return errorPromptDialog("Error", msg);
	}

	public static void openErrorPromptDialog(String msg) {
		openErrorPromptDialog("Error", msg);
	}

	public static void openErrorPromptDialog(final String title, final String msg) {
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				if (PlatformUI.getWorkbench().getActiveWorkbenchWindow()==null) return;
				errorPromptDialog("Architexa: " + title, msg).open();
			}});
	}
	
	public static void openWarnPromptDialog(final String title, final String msg) {
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				if (PlatformUI.getWorkbench().getActiveWorkbenchWindow()==null) return;
				promptDialog(MessageDialog.WARNING, "Architexa: " + title, msg).open();
			}});
	}

}
