package com.architexa.diagrams.ui;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.ui.IEditorPart;
import org.openrdf.model.Resource;
import org.openrdf.rio.RdfDocumentWriter;

import com.architexa.diagrams.Activator;
import com.architexa.diagrams.model.RootArtifact;
import com.architexa.diagrams.utils.RecentlyCreatedDiagramUtils;
import com.architexa.org.eclipse.gef.ui.parts.GraphicalView;

public abstract class RSEView extends GraphicalView implements RSEEditorViewCommon.IRSEEditorViewCommon/*, ISaveablePart2*/ {
	static final Logger logger = Activator.getLogger(RSEEditor.class);
	
	public void setName(String partName, boolean isLocalSave) {
    	// we do not want to rename the view window when saving
		// setPartName(partName);
    }

    @Override
	protected abstract void initializeGraphicalViewer();

    @Override
    public void setFocus() {
    }
    
    public void checkError() {
	}
    
	protected IFile file = null;

	/* (non-Javadoc)
	 * @see edu.mit.csail.pdeConsole.part.EditorPart#doSave(org.eclipse.core.runtime.IProgressMonitor)
	 */
	 public void doSave(IProgressMonitor monitor) {
		file = RSEEditorViewCommon.doSave(this, monitor, file);
	}

	public void clearDirtyFlag() {
		getCommandStack().markSaveLocation();
		this.firePropertyChange(IEditorPart.PROP_DIRTY);
	}

	public void showLocalSaveDialog() {
		file = RSEEditorViewCommon.doSave(this, new NullProgressMonitor(), getNewSaveFile());
	}
	
	/* (non-Javadoc)
	 * @see edu.mit.csail.pdeConsole.part.EditorPart#doSaveAs()
	 */
	public void doSaveAs() {
		IFile curFile = file;
		file = null;
		doSave(new NullProgressMonitor());

		// if it fails for some reason reset to original
		if (file == null) file = curFile;
	}

	/* (non-Javadoc)
	 * @see edu.mit.csail.pdeConsole.part.EditorPart#gotoMarker(org.eclipse.core.resources.IMarker)
	 */
	public void gotoMarker(IMarker marker) {}

	/* (non-Javadoc)
	 * @see edu.mit.csail.pdeConsole.part.EditorPart#isSaveAsAllowed()
	 */
	public boolean isSaveAsAllowed() {
		return true;
	}

	/* (non-Javadoc)
	 * @see edu.mit.csail.pdeConsole.part.EditorPart#isDirty()
	 */
	public boolean isDirty() {
		// when no file exists => we don't allow saving, i.e. nothing is dirty
		if (file == null) return false;

		return true;//super.isDirty();
	}

	/**
	 * Shared support for writing the editor's output - this allows easily
	 * calling the functionality from additional tools - like in Collab
	 */
	public abstract void writeFile(RdfDocumentWriter rdfWriter, Resource diagramRes) throws IOException;

	/**
	 * just get a new file name (and open it for our use)
	 */
	public abstract IFile getNewSaveFile();
	public abstract RootArtifact getRootModel();
	public boolean isDiff() {
		return false;
	}
	
	@Override
	public void dispose() {
		super.dispose();
		if (isDirty())
			RecentlyCreatedDiagramUtils.saveDiagram(this, null, getRootModel());
	}
}
