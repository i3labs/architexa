package com.architexa.org.eclipse.gef.extensions;

import com.architexa.org.eclipse.draw2d.IFigure;
import com.architexa.org.eclipse.gef.GraphicalViewer;
import com.architexa.org.eclipse.gef.editparts.ScalableFreeformRootEditPart;
import com.architexa.org.eclipse.gef.editparts.ScalableRootEditPart;
import com.architexa.org.eclipse.gef.ui.parts.GraphicalEditor;
import com.architexa.org.eclipse.gef.ui.parts.ScrollingGraphicalViewer;

import org.eclipse.swt.widgets.Composite;

import org.eclipse.jface.action.IMenuManager;

public interface GraphicalViewManager {
	public void init(GraphicalEditor editor, GraphicalViewer viewer);
	public void dispose(GraphicalEditor editor);
	public void updateMenuMap(GraphicalEditor editor, ServerExtensionContextMenuProvider provider, IMenuManager menu);
	public void initContextMenu(GraphicalEditor editor, GraphicalViewer graphicalViewer);
	public void initCanvas(GraphicalEditor editor, GraphicalViewer graphicalViewer);
	public void createGraphicalViewer(GraphicalEditor editor, Composite parent);
	public IFigure createGEFScaleableFigure(ScalableFreeformRootEditPart scalableFreeformRootEditPart);
	public IFigure createGEFScalableViewPort(ScalableRootEditPart scalableRootEditPart);
	
	public class NullImpl implements GraphicalViewManager {
		public void init(GraphicalEditor editor, GraphicalViewer viewer) {}
		public void dispose(GraphicalEditor editor) {}
		public void updateMenuMap(GraphicalEditor editor, ServerExtensionContextMenuProvider provider, IMenuManager menu) {}
		public void initContextMenu(GraphicalEditor editor, GraphicalViewer graphicalViewer) {}
		public void initCanvas(GraphicalEditor editor, GraphicalViewer graphicalViewer) {}
		public void createGraphicalViewer(GraphicalEditor editor, Composite parent) {
			GraphicalViewer viewer = new ScrollingGraphicalViewer();
			viewer.createControl(parent);
			editor.setGraphicalViewer(viewer);
			editor.configureGraphicalViewer();
			editor.hookGraphicalViewer();
			editor.initializeGraphicalViewer();
		}
		public IFigure createGEFScaleableFigure(ScalableFreeformRootEditPart scalableFreeformRootEditPart) {
			return scalableFreeformRootEditPart.createFigure();
		}
		public IFigure createGEFScalableViewPort(ScalableRootEditPart scalableRootEditPart) {
			return scalableRootEditPart.createFigure();
		}
	}

}