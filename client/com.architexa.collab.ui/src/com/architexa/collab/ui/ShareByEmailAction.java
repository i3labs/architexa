package com.architexa.collab.ui;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.Authenticator;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.PreferencesUtil;

import com.architexa.collab.UIUtils;
import com.architexa.diagrams.ui.RSEEditor;
import com.architexa.diagrams.utils.RootEditPartUtils;
import com.architexa.external.eclipse.Exporter;
import com.architexa.rse.AccountSettings;
import com.architexa.rse.BuildStatus;

public class ShareByEmailAction extends ShareAction {

	public static String shareByEmailText = "Email As Attachment";
	public static String shareByEmailIconPath = "icons/yellow_mail.png";

	static final Logger logger = Activator.getLogger(ShareByEmailAction.class);

	private static final String SSL_FACTORY = "javax.net.ssl.SSLSocketFactory";

	private String[] recipientEmails;

	public ShareByEmailAction(String diagramName, String diagramDescription, String[] recipientEmails) {
		super(diagramName, diagramDescription);
		this.recipientEmails = recipientEmails;
	}

	@Override
	public void run() {
		BuildStatus.addUsage("Share By Email");
		shareDiagram();
	}

	protected void shareDiagram() {

		final String sender = getSenderEmail();
		final String subject = "[Architexa Shared Diagram] "+diagramName;
		final String message = getMessage(diagramName, diagramDescription);
		final File attachment = getAttachment(diagramName); 
		if(attachment==null) {
			UIUtils
			.errorPromptDialog(
					"Share Failed", 
			"Unable to send email. Creation of diagram image attachment failed.").open();
			return;
		}
		final String attachmentName = getAttachmentName(diagramName);

		Job emailJob = new Job("Sending Email") {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				monitor.beginTask("Sending..", IProgressMonitor.UNKNOWN);
				try {
					sendEmail(sender, recipientEmails, subject,
							message, attachment, attachmentName);
					addRecipientsToAddressBook(recipientEmails);
				} catch (MessagingException e) {
					logger.error("Problem sending email. ", e);

					// Display cause to user so he sees 
					// specifics about what's wrong
					String message = e.getMessage();
					if(e.getCause()!=null) message = message+"\n"+e.getCause().getMessage();

					String defaultServer = AccountSettings.getDefaultStoredSMTPServer();
					String defaultPort = AccountSettings.getDefaultStoredSMTPPort();
					String defaultUsername = AccountSettings.getDefaultStoredSMTPUsername();
					String defaultPassword = AccountSettings.getDefaultStoredSMTPPassword();

					String storedServer = AccountSettings.getStoredSMTPServer();
					String storedPort = AccountSettings.getStoredSMTPPort();
					String storedUsername = AccountSettings.getStoredSMTPUsername();
					String storedPassword = AccountSettings.getStoredSMTPPassword();

					if(!defaultServer.equals(storedServer) || !defaultPort.equals(storedPort)
							|| !defaultUsername.equals(storedUsername) || !defaultPassword.equals(storedPassword)) {
						// User has changed default SMTP settings
						informOfBadCustomSMTPSettings(message);
						return Status.CANCEL_STATUS;
					}

					UIUtils.openErrorPromptDialog("Problem sending email.\n" + message);
					return Status.CANCEL_STATUS;
				}
				monitor.done();
				return Status.OK_STATUS;
			}
		};
		emailJob.setUser(true);
		emailJob.setPriority(Job.LONG);
		emailJob.schedule();
	}

	private String getSenderEmail() {
		return "";
	}

	private String getMessage(String diagramName, String description) {
		String from = getSenderEmail().trim();
		if("".equals(from)) from = "Someone";
		String message = from+" shared a diagram with you:\n\n"
		+diagramName.trim()+"\n"
		+description.trim()+"\n\n"
		+"Architexa helps you easily understand, document, and discuss code concepts."
		+"\nAn image of the shared diagram is attached to this email.";
		return message;
	}

	// Create a temp file for the diagram that
	// will be used for the email attachment
	private File getAttachment(String diagramName) {

		// Create a temp file
		File attachmentFile = null;
		try {
			attachmentFile = File.createTempFile("shareTemp", ".png");
		} catch (IOException e) {
			logger.error("Exception while creating temp attachment file. ", e);
		}
		if(attachmentFile==null) return null;

		// Save the diagram image to it
		String attachmentFilePath = attachmentFile.getPath();
		IEditorPart mpEditor = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
		IEditorPart activeEditor = (IEditorPart) RootEditPartUtils.getEditorFromRSEMultiPageEditor(mpEditor);
		Exporter.saveEditorContentsAsImage((RSEEditor)activeEditor, attachmentFilePath, null, SWT.IMAGE_PNG);

		return attachmentFile;
	}

	private String getAttachmentName(String diagramName) {
		String attachmentName = (diagramName!=null && 
				!"".equals(diagramName.trim())) ? diagramName : "share";
		// Remove any non alpha-numeric characters
		attachmentName = attachmentName.replaceAll("[^a-zA-Z0-9\\s]", "");
		// If name longer than 10 chars, remove any spaces
		if(attachmentName.length()>10) attachmentName= attachmentName.replaceAll("\\s+", "");	
		// Truncate after 10 chars
		if(attachmentName.length()>10) attachmentName = attachmentName.substring(0, 10);
		return attachmentName+".png";
	}

	private void sendEmail(String sender, String[] recipients, String subject,
			String body, File attachment, String attachmentName) throws MessagingException {

		String SMTP_HOST_NAME = AccountSettings.getStoredSMTPServer();
		String SMTP_PORT = AccountSettings.getStoredSMTPPort();
		final String SMTP_USERNAME = AccountSettings.getStoredSMTPUsername();
		final String SMTP_PASSWORD = AccountSettings.getStoredSMTPPassword();

		// Create mail session 
		Properties props = new Properties();
		props.put("mail.host", SMTP_HOST_NAME);
		props.put("mail.user", SMTP_USERNAME);
		props.put("mail.smtp.host", SMTP_HOST_NAME);
		props.put("mail.smtp.port", SMTP_PORT);
		props.put("mail.smtp.socketFactory.port", SMTP_PORT);
		props.put("mail.smtp.socketFactory.class", SSL_FACTORY);
		props.put("mail.smtp.socketFactory.fallback", "false");
		props.put("mail.smtp.auth", "true");
		// We need TTLS, which gmail requires
		props.put("mail.smtp.starttls.enable","true");

		// Create a session
		// Using Session.getInstance(..) and not Session.getDefaultInstance(..)
		// because we want a new Session object that uses props and not just the 
		// default Session that's already installed in case the user has changed
		// any SMTP settings.
		Session session = Session.getInstance(props, new Authenticator() {
			@Override
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(SMTP_USERNAME, SMTP_PASSWORD);
			}
		});

		// Make new mail message with from, to, subject
		Message mailMessage = new MimeMessage(session);
		mailMessage.setFrom(new InternetAddress(sender));
		for(String recipient : recipients) {
			mailMessage.addRecipient(Message.RecipientType.TO, new InternetAddress(recipient));
		}
		mailMessage.setSubject(subject);

		Multipart multipart = new MimeMultipart();

		// Add message body 
		BodyPart messageBodyPart = new MimeBodyPart();
		messageBodyPart.setText(body);
		multipart.addBodyPart(messageBodyPart);

		// Add diagram image attachment
		MimeBodyPart attachmentBodyPart = new MimeBodyPart();
		attachmentBodyPart.setFileName(attachmentName);
		// Using a JAF FileDataSource because it does MIME type detection
		DataSource source = new FileDataSource(attachment); 
		attachmentBodyPart.setDataHandler(new DataHandler(source));
		multipart.addBodyPart(attachmentBodyPart);

		mailMessage.setContent(multipart);

		/*
		 * Was added for a class loader issue which does not seem to happen anymore
		 */
//		Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
		// Send mail message
		Transport.send(mailMessage);

		// Delete the temp attachment file
		attachment.delete();
	}

	private void addRecipientsToAddressBook(String[] recipientEmails) {
		List<String> alreadyStoredContacts = Arrays.asList(AccountSettings.getStoredContacts());
		for(String recipient : recipientEmails) {
			// Eliminate extra space at start or end
			String trimmedRecipient = recipient.trim();
			// Don't add duplicate entry if recipient already in address book
			if(alreadyStoredContacts.contains(trimmedRecipient)) continue;
			ContactUtils.addContactToAddressBook(trimmedRecipient);
		}
	}

	/**
	 * 
	 * @return the resulting Exception if the connecting to the supplied
	 * host was unsuccessful, null if it was a success
	 */
	public static Exception testSMTPServer(String server, int port, 
			final String username, final String pw) {
		Properties props = new Properties();
		props.put("mail.host", server);
		props.put("mail.user", username);
		props.put("mail.smtp.host", server);
		props.put("mail.smtp.port", port);
		props.put("mail.smtp.socketFactory.port", port);
		props.put("mail.smtp.socketFactory.class", SSL_FACTORY);
		props.put("mail.smtp.socketFactory.fallback", "false");
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.starttls.enable","true");
		Session session = Session.getInstance(props, new Authenticator() {
			@Override
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(username, pw);
			}
		});
		try {
			Transport transport = session.getTransport("smtp");
			transport.connect(server, port, username, pw);
		} catch (MessagingException e) {
			return e;
		}
		return null;
	}

	// Open an error dialog telling the user that his custom SMTP server settings
	// are bad along with a link to open the pref page to change the settings
	private static void informOfBadCustomSMTPSettings(final String exceptionMsg) {		
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				if (PlatformUI.getWorkbench().getActiveWorkbenchWindow()==null) return;
				MessageDialog errorDialog = new MessageDialog(
						PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), 
						"Unable to Send Email",
						null, 
						exceptionMsg+
						"\n\nConsider restoring the default SMTP server settings.", 
						MessageDialog.ERROR, 
						new String[]{"OK"}, 
						0) {
					@Override
					protected Control createMessageArea(Composite composite) {

						// create image
						Image image = getImage();
						imageLabel = new Label(composite, SWT.NULL);
						image.setBackground(imageLabel.getBackground());
						imageLabel.setImage(image);
						GridDataFactory.fillDefaults().align(SWT.CENTER, SWT.BEGINNING)
						.applyTo(imageLabel);

						// create message
						Composite msgArea = new Composite(composite, SWT.NONE);
						msgArea.setLayout(new GridLayout());

						Text selectableMessageLabel = new Text(msgArea, SWT.MULTI | SWT.READ_ONLY);
						selectableMessageLabel.setText(message);

						Link editSMTPSettings = new Link(msgArea , SWT.LEFT | SWT.HIDE_SELECTION);
						editSMTPSettings.setText("<a> Edit SMTP Settings</a>");
						editSMTPSettings.addSelectionListener(new SelectionListener() {
							public void widgetDefaultSelected(SelectionEvent e) {}
							public void widgetSelected(SelectionEvent e) {
								try {
									Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
									PreferenceDialog pref = PreferencesUtil.createPreferenceDialogOn(shell, "CollabPreferencePage", null, null);
									if (pref != null) {
										CollabPreferencePage collabPrefPage = (CollabPreferencePage) pref.getSelectedPage();
										collabPrefPage.selectSMTPPreferenceTab();
										pref.open();
									}
								} catch (Throwable t) {
									logger.error("Unable to open SMTP preference tab " +
											"via link on email sending error dialog.", t);
								}
							}});
						return composite;
					}
				};
				errorDialog.open();
			}});
	}

}
