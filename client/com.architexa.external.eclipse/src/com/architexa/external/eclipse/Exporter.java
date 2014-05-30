package com.architexa.external.eclipse;

import java.io.OutputStream;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PlatformUI;

import com.architexa.collab.proxy.PluginUtils;
import com.architexa.org.eclipse.draw2d.Graphics;
import com.architexa.org.eclipse.draw2d.IFigure;
import com.architexa.org.eclipse.draw2d.SWTGraphics;
import com.architexa.org.eclipse.draw2d.geometry.Point;
import com.architexa.org.eclipse.draw2d.geometry.Rectangle;
import com.architexa.org.eclipse.gef.GraphicalViewer;
import com.architexa.org.eclipse.gef.LayerConstants;
import com.architexa.org.eclipse.gef.editparts.LayerManager;
import com.architexa.rse.BuildStatus;

//Based on code written by venkataramana for eclipse.tools.gef newsgroup
public class Exporter implements IWorkbenchWindowActionDelegate {

	public void run(IAction action) {
		exportImage();
	}
	
	public static void exportImage() {
		BuildStatus.addUsage("Export Diagram As Image");
		IWorkbenchWindow activeWorkbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		IEditorPart activeEditor = activeWorkbenchWindow.getActivePage().getActiveEditor();
		if(activeEditor==null || !(activeEditor instanceof IExportableEditorPart)) return;
		((IExportableEditorPart)activeEditor).addRemoveImageExportCustomizations(true);
		save((IExportableEditorPart)activeEditor);
		((IExportableEditorPart)activeEditor).addRemoveImageExportCustomizations(false);
	}

	public static boolean save(IExportableEditorPart editorPart) {
		String filename = getSaveFilePath(editorPart);
		if (filename == null)
			return false;

		int format = SWT.IMAGE_JPEG;
		if (filename.endsWith(".jpg"))
			format = SWT.IMAGE_JPEG;
		else if (filename.endsWith(".bmp"))
			format = SWT.IMAGE_BMP;
		else if (filename.endsWith(".png")) 
			format = SWT.IMAGE_PNG;
		else if (filename.endsWith(".gif"))	//Only supported with 1,4,8 bits per pixel
			format = SWT.IMAGE_GIF; 			//Unfortunately windows images are 32 bpp
		else
			return false;

		return save(editorPart, filename, format);
	}

	private static String getSaveFilePath(IExportableEditorPart editor) {
		FileDialog fileDialog = new FileDialog(editor.getEditorSite().getShell(), SWT.SAVE);

		String[] filterExtensions = new String[] {
				"*.png", "*.gif", "*.jpg", "*.bmp"
		};
		fileDialog.setFilterExtensions(filterExtensions);

		return fileDialog.open();
	}

	private static boolean save(IExportableEditorPart editor, String filename, int format) {
		try {
			// 3.2 does not support PNGs so we need to change format
			if (PluginUtils.getPluginVer("org.eclipse.jdt.ui") <= 3.2)
				format = SWT.IMAGE_JPEG;
			saveEditorContentsAsImage(editor, filename, null, format);
		} catch (Exception ex) {
			MessageDialog.openError(editor.getEditorSite().getShell(), "Save Error",
			"Could not save editor contents");
			return false;
		}

		return true;
	}

	public static Rectangle saveEditorContentsAsImage(IExportableEditorPart editor, String saveFilePath,
			OutputStream outputStream, int format) {
		final GraphicalViewer viewer = (GraphicalViewer) editor.getAdapter(GraphicalViewer.class);

		/*
		 * 1. First get the figure whose visuals we want to save as image. So we would
		 * like to save the rooteditpart which actually hosts all the printable layers.
		 * NOTE: ScalableRootEditPart manages layers and is registered graphicalviewer's
		 * editpartregistry with the key LayerManager.ID ... well that is because
		 * ScalableRootEditPart manages all layers that are hosted on a FigureCanvas. Many
		 * layers exist for doing different things
		 */

		LayerManager rootEditPart =
			(LayerManager) viewer.getEditPartRegistry().get(LayerManager.ID);
		IFigure rootFigure = rootEditPart.getLayer(LayerConstants.PRINTABLE_LAYERS);

		// Measure the diagram.
		// The actual visible part of the diagram is probably less than the
		// entire root figure. We only want to export the "interesting part".
		Point orgLoc = editor.getDiagramsOriginalHeight();
		Rectangle bounds = editor.getDiagramsBoundsForExport();

		/*
		 * 2. Now we want to get the GC associated with the control on which all figures
		 * are painted by SWTGraphics. For that first get the SWT Control associated with
		 * the viewer on which the rooteditpart is set as contents
		 */
		Control figureCanvas = viewer.getControl();
		GC figureCanvasGC = new GC(figureCanvas);

		/* 3. Create a new Graphics for an Image onto which we want to paint rootFigure */
		Image img = new Image(null, bounds.width, bounds.height);
		GC imageGC = new GC(img);
		imageGC.setBackground(figureCanvasGC.getBackground());
		imageGC.setForeground(figureCanvasGC.getForeground());
		imageGC.setFont(figureCanvasGC.getFont());
		imageGC.setLineStyle(figureCanvasGC.getLineStyle());
		imageGC.setLineWidth(figureCanvasGC.getLineWidth());
		// imageGC.setXORMode(figureCanvasGC.getXORMode());

		Graphics imgGraphics = new SWTGraphics(imageGC);
		imgGraphics.translate(-bounds.x, -bounds.y);

		try {
			/* 4. Draw rootFigure onto image. After that image will be ready for save */
			rootFigure.paint(imgGraphics);

			/* 5. Save image */
			ImageData[] imgData = new ImageData[1];
			imgData[0] = img.getImageData();

			ImageLoader imgLoader = new ImageLoader();
			imgLoader.data = imgData;

			if(saveFilePath!=null) imgLoader.save(saveFilePath, format);
			else if(outputStream!=null) imgLoader.save(outputStream, format);
		} finally {
			// release OS resources
			editor.returnToOrigLoc(orgLoc);
			figureCanvasGC.dispose();
			imageGC.dispose();
			img.dispose();
		}
		
		return bounds;
	}

	public void dispose() {}
	public void init(IWorkbenchWindow window) {}
	public void selectionChanged(IAction action, ISelection selection) {}

}
