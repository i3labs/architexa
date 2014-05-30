package com.architexa.diagrams.relo.jdt.browse;

import java.util.Timer;
import java.util.TimerTask;

import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ui.IEditorPart;

import com.architexa.diagrams.relo.ui.ReloEditor;
import com.architexa.diagrams.ui.RSEAction;
import com.architexa.diagrams.utils.RootEditPartUtils;


public class EmbeddedMethodAction extends RSEAction implements IPropertyChangeListener {
	
	protected IAction linkedTrackerAction = null;
	
	public final static String embeddedMethodId = "com.architexa.diagrams.relo.jdt.embeddedMethod";
	protected IAction embeddedMethodAction = null;	// me (actually my proxy)

	@Override
	public void setActiveEditor(IAction action, IEditorPart targetEditor) {
		if (targetEditor == null) return;
		if (!(RootEditPartUtils.getEditorFromRSEMultiPageEditor(targetEditor) instanceof ReloEditor)) {
			action.setEnabled(false);
			return;
		} else
			action.setEnabled(true);
		super.setActiveEditor(action, targetEditor);
	}
	
	@Override
	public void initAction() {
		Timer t = new Timer();
		t.schedule(new TimerTask() {
			@Override
			public void run() {
				for (IContributionItem item : tbm.getItems()) {
					if (linkedTrackerId.equals(item.getId())) {
						linkedTrackerAction = ((ActionContributionItem) item).getAction();
					}
					if (embeddedMethodId.equals(item.getId())) {
						embeddedMethodAction = ((ActionContributionItem) item).getAction();
					}
				}

				if (linkedTrackerAction != null)
					linkedTrackerAction.addPropertyChangeListener(EmbeddedMethodAction.this);
				if (embeddedMethodAction != null && linkedTrackerAction != null)
					updateMyState();
			}
		}, 1000);
	}
	
	@Override
	public void run(IAction action) {
	}

	public void propertyChange(PropertyChangeEvent event) {
		updateMyState();
	}

	protected void updateMyState() {
		embeddedMethodAction.setEnabled(linkedTrackerAction.isChecked());
	}

}
