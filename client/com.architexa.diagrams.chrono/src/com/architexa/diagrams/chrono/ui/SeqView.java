package com.architexa.diagrams.chrono.ui;


import java.io.IOException;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.openrdf.model.Resource;
import org.openrdf.rio.RdfDocumentWriter;

import com.architexa.diagrams.chrono.editparts.DiagramEditPart;
import com.architexa.diagrams.chrono.models.DiagramModel;
import com.architexa.diagrams.model.RootArtifact;
import com.architexa.diagrams.ui.RSECommandStack;
import com.architexa.diagrams.ui.RSEEditorViewCommon;
import com.architexa.diagrams.ui.RSETransferDropTargetListener;
import com.architexa.diagrams.ui.RSEView;
import com.architexa.org.eclipse.gef.EditDomain;
import com.architexa.org.eclipse.gef.KeyHandler;
import com.architexa.org.eclipse.gef.editparts.AbstractGraphicalEditPart;
import com.architexa.org.eclipse.gef.ui.parts.ScrollingGraphicalViewer;


/**
 * 
 * @author Elizabeth L. Murnane
 *
 */
public class SeqView extends RSEView {

	public static final String viewId = "com.architexa.diagrams.sequence.ui.SeqView";
	private DiagramModel model;
	
	public SeqView() {
		EditDomain defaultEditDomain = getEditDomain();
		defaultEditDomain.setCommandStack(new RSECommandStack("Chrono"));
		setEditDomain(defaultEditDomain);
	}

	
	public DiagramModel getModel() {
		return model;
	}
	
	// TODO: integrate this with above getModel
	@Override
	public RootArtifact getRootModel() {
		return model;
	}
	
	@Override
	protected void initializeGraphicalViewer() {
		model = new DiagramModel();
		getGraphicalViewer().setContents(model);
		getGraphicalViewer().addDropTargetListener(new RSETransferDropTargetListener(this, getGraphicalViewer()));
	}

	public boolean canDropOnEditor(IStructuredSelection sel) {
		return SeqViewEditorCommon.canDropOnEditor(sel);
	}

	@Override
	protected void configureGraphicalViewer() {
		super.configureGraphicalViewer();
		ScrollingGraphicalViewer viewer = (ScrollingGraphicalViewer)getGraphicalViewer();
		SeqViewEditorCommon.configureGraphicalViewer(this, viewer, getActionRegistry(), getCommonKeyHandler(viewer), model);
	}

	private KeyHandler getCommonKeyHandler(ScrollingGraphicalViewer viewer) {
		return RSEEditorViewCommon.getCommonKeyHandler(this, viewer);
	}

	public DiagramEditPart getDiagramController() {
		return (DiagramEditPart) getGraphicalViewer().getEditPartRegistry().get(model);
	}
	
	@Override
	public IFile getNewSaveFile() {
		return SeqViewEditorCommon.getNewSaveFile(this);
	}

	@Override
	public void writeFile(RdfDocumentWriter rdfWriter, Resource fileRes) throws IOException {
		model = SeqViewEditorCommon.writeFile(this, model, rdfWriter, fileRes);
	}

	public boolean isSaveOnCloseNeeded() {
		return false;
	}
	// Only used by strata
	// TODO make all 'get root' methods the same
	@Deprecated
	public AbstractGraphicalEditPart getRootController() {
		return getDiagramController();
	}

}
