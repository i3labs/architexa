package com.architexa.diagrams.relo.ui;

import java.io.IOException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.util.TransferDropTargetListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.rio.RdfDocumentWriter;

import com.architexa.diagrams.RSECore;
import com.architexa.diagrams.model.BrowseModel;
import com.architexa.diagrams.relo.modelBridge.ReloDoc;
import com.architexa.diagrams.relo.parts.ReloController;
import com.architexa.diagrams.relo.parts.ReloPartFactory;
import com.architexa.diagrams.ui.RSECommandStack;
import com.architexa.diagrams.ui.RSEEditorViewCommon;
import com.architexa.diagrams.ui.RSETransferDropTargetListener;
import com.architexa.diagrams.ui.RSEView;
import com.architexa.org.eclipse.gef.EditDomain;
import com.architexa.org.eclipse.gef.KeyHandler;
import com.architexa.org.eclipse.gef.editparts.AbstractGraphicalEditPart;
import com.architexa.org.eclipse.gef.ui.parts.ScrollingGraphicalViewer;
import com.architexa.store.StoreUtil;



public class ReloView extends RSEView {
	
	// this should really be in the repository somewhere
	private static final String defaulBMPath = 
		"com.architexa.diagrams.relo.jdt/com.architexa.diagrams.relo.jdt.browse.ClassStrucBrowseModel";
	
	public static final String viewId = "com.architexa.diagrams.relo.ui.ReloView";
	
	public ReloView() {
		EditDomain defaultEditDomain = getEditDomain();
		// Removed QueueableCommandStack: Its functionality is obsolete
		defaultEditDomain.setCommandStack(new RSECommandStack("Relo"));
		setEditDomain(defaultEditDomain);

		BrowseModel bm = (BrowseModel) StoreUtil.loadClass(defaulBMPath);
		rootModel.setBrowseModel(bm);
		bm.setRootArt(rootModel);
		bm.setRepo(rootModel.getRepo());
	}

	private ReloDoc rootModel = new ReloDoc();

	@Override
	public ReloDoc getRootModel() {
		return rootModel;
	}

	@Override
	protected void initializeGraphicalViewer() {
		getGraphicalViewer().setContents(rootModel);
		
		TransferDropTargetListener dragSrc = new RSETransferDropTargetListener(this, getGraphicalViewer());
		getGraphicalViewer().addDropTargetListener(dragSrc);
	}

	public boolean canDropOnEditor(IStructuredSelection sel) {
		return ReloViewEditorCommon.canDropOnEditor(sel);
	}

    public static final URI browseModel = RSECore.createRseUri("core#browseModel");

	@Override
    protected void configureGraphicalViewer() {
		super.configureGraphicalViewer();
		ScrollingGraphicalViewer viewer = (ScrollingGraphicalViewer)getGraphicalViewer();
//		BrowseModel bm = null;
		BrowseModel bm = rootModel.getBrowseModel(); // this is needed for relo view to work
		ReloViewEditorCommon.configureGraphicalViewer(this, viewer, getActionRegistry(), getSite(), bm, getCommonKeyHandler(viewer), null);
		viewer.setEditPartFactory(new ReloPartFactory(bm));
	}

	private KeyHandler getCommonKeyHandler(ScrollingGraphicalViewer viewer) {
		return RSEEditorViewCommon.getCommonKeyHandler(this, viewer);
    }
    
	
	public ReloController getReloController() {
		return (ReloController) this.getGraphicalViewer().getEditPartRegistry().get(this.rootModel);
	}

	IProject defaultProject = null;
	
	@Override
	public IFile getNewSaveFile() {
		return ReloViewEditorCommon.getNewSaveFile(this, defaultProject);
	}

	@Override
	public void writeFile(RdfDocumentWriter rdfWriter, Resource diagramRes) throws IOException {
		BrowseModel bm = rootModel.getBrowseModel();
		diagramRes = ReloViewEditorCommon.writeFile(this, getReloController(), rootModel, browseModel, bm, rdfWriter, diagramRes);
	}


	public boolean isSaveOnCloseNeeded() {
		return false;
	}
	// Only used by strata
	// TODO make all 'get root' methods the same
	@Deprecated
	public AbstractGraphicalEditPart getRootController() {
		return getReloController();
	}
}
