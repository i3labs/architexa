package com.architexa.org.eclipse.gef.tools;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

import com.architexa.org.eclipse.draw2d.IFigure;
import com.architexa.org.eclipse.gef.GraphicalEditPart;
import com.architexa.org.eclipse.gef.editparts.ZoomListener;
import com.architexa.org.eclipse.gef.editparts.ZoomManager;
import com.architexa.org.eclipse.gef.tools.CellEditorLocator;
import com.architexa.org.eclipse.gef.tools.DirectEditManager;


public class LabelDirectEditManager extends DirectEditManager {

    private double cachedZoom = -1.0;
    private Font scaledFont;
    private ZoomListener zoomListener = new ZoomListener() {
        public void zoomChanged(double newZoom) {
            updateScaledFont(newZoom);
        }
    };
    final IFigure directEditFigure;

    public LabelDirectEditManager(
            GraphicalEditPart source, 
            Class editorType, 
            CellEditorLocator locator, 
            IFigure directEditFigure) {
        super(source, editorType, locator);
        if (!(source instanceof LabelSource)) throw new IllegalArgumentException();
        this.directEditFigure = directEditFigure;
    }

    /**
     * @see com.architexa.org.eclipse.gef.tools.DirectEditManager#bringDown()
     */
    @Override
    protected void bringDown() {
        ZoomManager zoomMgr = (ZoomManager)getEditPart().getViewer()
                .getProperty(ZoomManager.class.toString());
        zoomMgr.removeZoomListener(zoomListener);
        super.bringDown();
        disposeScaledFont();
    }

    @Override
    protected void initCellEditor() {
        Text text = (Text)getCellEditor().getControl();
        text.setText(getInitialText());
        ZoomManager zoomMgr = (ZoomManager)getEditPart().getViewer()
                .getProperty(ZoomManager.class.toString());
        cachedZoom = -1.0;
        updateScaledFont(zoomMgr.getZoom());
        zoomMgr.addZoomListener(zoomListener);
    }

    private void updateScaledFont(double zoom) {
        if (cachedZoom == zoom)
            return;
        
        Text text = (Text)getCellEditor().getControl();
        Font font = getEditPart().getFigure().getFont();
        
        disposeScaledFont();
        cachedZoom = zoom;
        if (zoom == 1.0)
            text.setFont(font);
        else {
            FontData fd = font.getFontData()[0];
            fd.setHeight((int)(fd.getHeight() * zoom));
            text.setFont(scaledFont = new Font(null, fd));
        }
    }
    private void disposeScaledFont() {
        if (scaledFont != null) {
            scaledFont.dispose();
            scaledFont = null;
        }
    }

    /**
     * Creates the cell editor on the given composite. The cell editor is
     * created by instantiating the cell editor type passed into this
     * DirectEditManager's constuctor.
     * 
     * @param composite
     *            the composite to create the cell editor on
     * @return the newly created cell editor
     */
    @Override
    protected CellEditor createCellEditorOn(Composite composite) {
        return new TextCellEditor(composite, SWT.MULTI | SWT.WRAP);
    }

    /**
     * @return the initial value of the text shown in the cell editor for
     *         direct-editing; cannot return <code>null</code>
     */
    protected String getInitialText() {
        return ((LabelSource) getEditPart()).getAnnoLabelText();
    }

    /**
     * Used to determine the initial text of the cell editor and to
     * determine the font size of the text.
     * 
     * @return the figure being edited
     */
    protected IFigure getDirectEditFigure() {
        return ((LabelSource) getEditPart()).getAnnoLabelFigure();
    }

}