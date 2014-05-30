package com.architexa.diagrams.ui;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.internal.utils.FileUtil;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.ui.ISaveablePart;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.WorkbenchPart;
import org.openrdf.model.Resource;
import org.openrdf.rio.RdfDocumentWriter;

import com.architexa.collab.UIUtils;
import com.architexa.diagrams.Activator;
import com.architexa.diagrams.ErrorBuildListeners;
import com.architexa.diagrams.RSECore;
import com.architexa.diagrams.model.ArtifactRel;
import com.architexa.diagrams.model.Comment;
import com.architexa.org.eclipse.draw2d.FreeformLayer;
import com.architexa.org.eclipse.draw2d.LayeredPane;
import com.architexa.org.eclipse.gef.KeyHandler;
import com.architexa.org.eclipse.gef.editparts.AbstractGraphicalEditPart;
import com.architexa.org.eclipse.gef.ui.parts.GraphicalView;
import com.architexa.org.eclipse.gef.ui.parts.GraphicalViewerKeyHandler;
import com.architexa.org.eclipse.gef.ui.parts.ScrollingGraphicalViewer;
import com.architexa.rse.BuildStatus;
import com.architexa.store.ReloRdfRepository;
import com.architexa.store.StoreUtil;

public class RSEEditorViewCommon {
	static final Logger logger = Activator.getLogger(RSEEditor.class);
	// injected 
	public static Object api = null;
	public interface IRSEEditorViewCommon extends IWorkbenchPart, ISaveablePart{
		public IFile getNewSaveFile();
		public void writeFile(RdfDocumentWriter rdfWriter, Resource fileRes) throws IOException;
	    public void setName(String partName, boolean isLocalSave);
	    public void clearDirtyFlag();
		public void checkError();
		public boolean isDirty();
		public AbstractGraphicalEditPart getRootController();
		public boolean isDiff();
		public void showLocalSaveDialog();
		public boolean canDropOnEditor(IStructuredSelection sel);
	}

	protected static void addGeneralConnectionLayer(LayeredPane pane) {
		pane.add(new FreeformLayer(), RSEEditor.GENERAL_CONNECTION_LAYER);
	}
	
	public static IFile doSave(IRSEEditorViewCommon rseEditorView, IProgressMonitor monitor, IFile file) {
		if (file == null) {
			file = rseEditorView.getNewSaveFile();
			if (file == null) {	// no new file and author asked to save, cancel 
				monitor.setCanceled(true);
				return file;
			}
		}
		if (file.getFileExtension() == null || file.getFileExtension().equals("")) {
//			UIUtils.openErrorPromptDialog("Architexa RSE - Error while saving", "Please make sure to include the correct extension for your diagram type.");
//			doSave(rseEditorView, monitor, null);
		}
		BuildStatus.addUsage("LocalSave: " + file);
		save(rseEditorView, file);
		rseEditorView.clearDirtyFlag();
		return file;
	}

	public static IFile save(final IRSEEditorViewCommon rseEditorView, final IFile file) {
		ErrorBuildListeners.add(rseEditorView);
		try {
			final PipedInputStream in = new PipedInputStream();
			final PipedOutputStream out = new PipedOutputStream();
			in.connect(out);
			Thread t = new Thread() {
				@Override
				public void run() {
					try {
						Resource res = RSECore.eclipseResourceToRDFResource(StoreUtil.getDefaultStoreRepository(), file);
						final RdfDocumentWriter rdfWriter = StoreUtil.getRDFWriter(out);
						
						rseEditorView.writeFile(rdfWriter, res);
						out.close();
					} catch (Throwable t){
						UIUtils.openErrorPromptDialog("Architexa RSE - Error while saving", "There was an error while trying to save your diagram.\n Some data may be lost");
						logger.error("Error while Saving, some data may be lost",t);
					} finally {
						try { out.close(); } catch (IOException e) { e.printStackTrace(); }
					}
				}
			};
			t.start();
			file.setContents(in, /*force*/true, /*keepHistory*/true, /*IProgressMonitor*/null);
			rseEditorView.setName(file.getName(), true);
		} catch (IOException e) {
			logger.error("Unexpected Error", e);
		} catch (CoreException e) {
			logger.error("Unexpected Error", e);
		}
		return file;
	}
	
	
	// TODO: share code with above save()
	public static void saveJavaFile(final IRSEEditorViewCommon rseEditorView, final File file, IProgressMonitor monitor) {
		ErrorBuildListeners.add(rseEditorView);
		try {
			final PipedInputStream in = new PipedInputStream();
			final PipedOutputStream out = new PipedOutputStream();
			in.connect(out);
			Thread t = new Thread() {
				@Override
				public void run() {
					try {
						Resource diagramRes = StoreUtil.getDefaultStoreRepository().createBNode();
						final RdfDocumentWriter rdfWriter = StoreUtil.getRDFWriter(out);
						
						rseEditorView.writeFile(rdfWriter, diagramRes);
						out.close();
					} catch (Throwable t){
						UIUtils.openErrorPromptDialog("Architexa RSE - Error while saving", "There was an error while trying to save your diagram.\n Some data may be lost");
						logger.error("Error while Saving, some data may be lost",t);
					} finally {
						try { out.close(); } catch (IOException e) { e.printStackTrace(); }
					}
				}
			};
			t.start();
			FileOutputStream fos = new FileOutputStream(file);
			FileUtil.transferStreams(in, fos, file.getAbsolutePath(), monitor);
			rseEditorView.setName(file.getName(), true);
		} catch (IOException e) {
			logger.error("Unexpected Error", e);
		} catch (CoreException e) {
			logger.error("Unexpected Error", e);
		}
	}

	public static void writeCommentChildren(Resource parentRes,
			List<Comment> childrenList, RdfDocumentWriter rdfWriter, List<ArtifactRel> savedRels) throws IOException {
		ReloRdfRepository repo = StoreUtil.getDefaultStoreRepository();
		for(Comment comment:childrenList){
			Resource childSaveRes = comment.getInstanceRes();
			rdfWriter.writeStatement(parentRes, RSECore.contains, childSaveRes);
			rdfWriter.writeStatement(childSaveRes, repo.rdfType, RSECore.node);
			rdfWriter.writeStatement(childSaveRes, RSECore.model, comment.getArt().elementRes);

			comment.writeRDFNode(rdfWriter, comment.getParentArt().getInstanceRes());
			for (ArtifactRel ar : comment.getSourceConnections()) {
		        ar.writeRDF(rdfWriter, savedRels);
			}
		}
	}

	public static boolean isDirty(IRSEEditorViewCommon rseEditorView, IFile file) {
		// when no file exists => we don't allow saving, i.e. nothing is dirty
		if (file == null) return false;
		return rseEditorView.isDirty();
	}

	public static KeyHandler getCommonKeyHandler(final WorkbenchPart wbPart, ScrollingGraphicalViewer viewer) {
		if (!(wbPart instanceof GraphicalView)) return null;
		return new GraphicalViewerKeyHandler(viewer) {
        	@Override
			public boolean keyPressed(KeyEvent e) {
        		if (((GraphicalView)wbPart).trapKeys(e))
        			return true;
        		else
        			return super.keyPressed(e);
        	}
        };
	}

	public boolean isDiff() {
		return false;
	}

}
