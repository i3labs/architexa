package com.architexa.diagrams.relo.jdt.actions;
import org.eclipse.jface.action.Action;
import org.eclipse.ui.PlatformUI;

import com.architexa.diagrams.jdt.RJCore;
import com.architexa.diagrams.model.ArtifactFragment;
import com.architexa.diagrams.model.RootArtifact;
import com.architexa.diagrams.relo.jdt.ReloJDTPlugin;
import com.architexa.org.eclipse.gef.editparts.AbstractGraphicalEditPart;
import com.architexa.store.ReloRdfRepository;

public class EditJavaDocDialogAction extends Action {
		private AbstractGraphicalEditPart part;
		private ReloRdfRepository repo;

		public EditJavaDocDialogAction(AbstractGraphicalEditPart ep, ReloRdfRepository repo) {
			super("Edit Java Doc in Source Code...", ReloJDTPlugin.getImageDescriptor("icons/javadoc_location_attrib.gif"));
			this.part =ep;
			this.repo = repo;
		}

		@Override
		public void run() {
			EditJavaDocDialog dlg = new EditJavaDocDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), part, repo);
        	dlg.open();
		}
		
		public boolean canRun(Object obj) {
			if (!(obj instanceof ArtifactFragment)) return false;
			ArtifactFragment af = (ArtifactFragment) obj;
			if (af instanceof RootArtifact)
				return false;
			if (!af.getArt().elementRes.toString().contains(RJCore.jdtWkspcNS)) return false;
			return true;
		}

    }
