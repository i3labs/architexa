package com.architexa.rse.update;

import org.eclipse.jface.action.ControlContribution;
import org.eclipse.jface.action.StatusLineManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.WorkbenchWindow;

import com.architexa.intro.AtxaIntroPlugin;
import com.architexa.intro.preferences.PreferenceConstants;

// This is called by the CheckForUpdateJob  which is currently disabled. 
// TODO: reimplement check for updateJob so this notification shows up
public class UpdateToolbar {

	private static ControlContribution updateNotifier;

	public static void showNotification(final boolean show, final boolean isExtendedVersion) {
		final boolean showAndPref = show && AtxaIntroPlugin.getDefault().getPreferenceStore().getBoolean(PreferenceConstants.ShowWhenUpdatesAreAvailableKey);
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				if (!show && updateNotifier==null) return;
				WorkbenchWindow activeWindow = 
					(WorkbenchWindow) PlatformUI.getWorkbench().getActiveWorkbenchWindow();
				StatusLineManager statusLineMgr = activeWindow.getStatusLineManager();

				if(updateNotifier==null) createUpdateNotifierAndAddToTrim(statusLineMgr, isExtendedVersion);
				updateNotifier.setVisible(showAndPref);

				// Refresh the UI
				statusLineMgr.markDirty();
				statusLineMgr.update(true);
			}
		});
	}

	private static void createUpdateNotifierAndAddToTrim(StatusLineManager statusLineMgr, final boolean isExtendedVersion) {		

		updateNotifier = new ControlContribution("com.architexa.intro.updateToolbar") {
			@Override
			protected Control createControl(Composite parent) {

				// Create a composite to hold the update notification
				Composite container = new Composite(parent, SWT.NONE);
				RowLayout layout = new RowLayout();
				layout.marginTop = 0;
				layout.marginBottom = 0;
				container.setLayout(layout);

				// Tell user update is available
				Composite labelArea = new Composite(container, SWT.NONE);
				RowLayout labelLayout = new RowLayout();
				labelLayout.marginTop = 5;
				labelArea.setLayout(labelLayout);
				Label alertImage = new Label(labelArea, SWT.NONE);
				alertImage.setImage(AtxaIntroPlugin.
						getImageDescriptor("icons/alert-triangle-blue.png").createImage());

				// And give link to get the update
				Link diagramLink = new Link(labelArea , SWT.BOTTOM);
				diagramLink.setText("<a>Architexa update available</a>");
				diagramLink.setToolTipText("Click to update Architexa");
				diagramLink.addSelectionListener(new SelectionListener() {
					public void widgetDefaultSelected(SelectionEvent e) {}
					public void widgetSelected(SelectionEvent e) {
						new UpdateAction(isExtendedVersion).run();
					}});

				container.pack();
				return container;
			}
		};

		// Notification should only show when an
		// update is available, so hiding it by default
		updateNotifier.setVisible(false);

		// Add notifier to the status trim
		statusLineMgr.add(updateNotifier);

		// And refresh the status trim UI
		statusLineMgr.markDirty();
		statusLineMgr.update(true);
	}

}
