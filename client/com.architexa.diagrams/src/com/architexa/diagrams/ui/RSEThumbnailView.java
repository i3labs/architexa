package com.architexa.diagrams.ui;

import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

import com.architexa.diagrams.Activator;
import com.architexa.diagrams.editors.RSEMultiPageEditor;
import com.architexa.diagrams.utils.RootEditPartUtils;
import com.architexa.org.eclipse.draw2d.LightweightSystem;
import com.architexa.org.eclipse.draw2d.MarginBorder;
import com.architexa.org.eclipse.draw2d.Viewport;
import com.architexa.org.eclipse.draw2d.parts.ScrollableThumbnail;
import com.architexa.org.eclipse.gef.LayerConstants;
import com.architexa.org.eclipse.gef.RootEditPart;
import com.architexa.org.eclipse.gef.editparts.AbstractGraphicalEditPart;
import com.architexa.org.eclipse.gef.editparts.FreeformGraphicalRootEditPart;
import com.architexa.org.eclipse.gef.editparts.ScalableRootEditPart;

/**
 * @author vineet
 * @author emurnane
 */
public class RSEThumbnailView extends ViewPart {
	static final Logger logger = Activator.getLogger(RSEThumbnailView.class);

    private IPartListener ipl = null;

    private Composite frame;
    private Canvas overviewCanvas;

    private RSEEditor currentEditor;

    /* (non-Javadoc)
     * @see org.eclipse.ui.IWorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
     */
    @Override
    public void createPartControl(Composite parent) {
        parent.setLayout(new FillLayout());
        
        frame = parent;
        
    	ipl = new IPartListener() {
            public void partActivated(IWorkbenchPart part) {
            	IWorkbenchPart editor = RootEditPartUtils.getEditorFromRSEMultiPageEditor(part);
            	if (editor instanceof RSEEditor)
            		setViewer((RSEEditor) editor);
            }
            public void partBroughtToTop(IWorkbenchPart part) {}
            public void partClosed(IWorkbenchPart part) {
            	if (RootEditPartUtils.getEditorFromRSEMultiPageEditor(part) instanceof RSEEditor) {
	            	RSEEditor editor = (RSEEditor) RootEditPartUtils.getEditorFromRSEMultiPageEditor(part);
	            	if (editor == currentEditor)
	            		unsetViewer(editor);
            	}
            }
            public void partDeactivated(IWorkbenchPart part) {}
            public void partOpened(IWorkbenchPart part) {}
        };
        getSite().getWorkbenchWindow().getPartService().addPartListener(ipl);

        IWorkbenchPage iap = getActivePage();
        if (iap == null) return;
        
        IEditorPart iep = iap.getActiveEditor();
        IWorkbenchPart editor = RootEditPartUtils.getEditorFromRSEMultiPageEditor(iep);
    	
        if (editor != null) {
        	if (editor instanceof RSEEditor)
        		setViewer((RSEEditor) editor);
        } else {
            // let's search for editor
            IEditorReference[] ier = iap.getEditorReferences();
            for (int i=0;i<ier.length;i++) {
                //System.err.println(ier[i].getId());
            	if (RSEEditor.isEditorID(ier[i].getId())) {
                    setViewer((RSEEditor)ier[i].getEditor(true));
                    break;
                }
            }
            
        }
    }
    
    private IWorkbenchPage getActivePage() {
        IWorkbench iwb = PlatformUI.getWorkbench();
        if (iwb==null)	return null;
        
        IWorkbenchWindow iww = iwb.getActiveWorkbenchWindow();
        if (iww==null)	return null;
        
        IWorkbenchPage iwp = iww.getActivePage();
        return iwp;
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.ui.IWorkbenchPart#dispose()
     */
    @Override
    public void dispose() {
        super.dispose();
        getSite().getWorkbenchWindow().getPartService().removePartListener(ipl); 
    }
    

    /* (non-Javadoc)
     * @see org.eclipse.ui.IWorkbenchPart#setFocus()
     */
    @Override
    public void setFocus() {
    }


    private void setViewer(RSEEditor editor) {
        if (currentEditor != null) return;

		RootEditPart root = editor.getRootEditPart();
		if (root == null) return;
		
		if (!(root instanceof AbstractGraphicalEditPart)) {
			logger.error("Unsupported root edit part type:" + root.getClass());
			return;
		}

		ScrollableThumbnail thumbnail = new ScrollableThumbnail((Viewport)((AbstractGraphicalEditPart)root).getFigure());
		thumbnail.setBorder(new MarginBorder(3));
		if (root instanceof FreeformGraphicalRootEditPart)
			thumbnail.setSource( ((FreeformGraphicalRootEditPart)root).getLayer(LayerConstants.PRINTABLE_LAYERS));      
		else if (root instanceof ScalableRootEditPart)
			thumbnail.setSource( ((ScalableRootEditPart)root).getLayer(LayerConstants.PRINTABLE_LAYERS));      
		else
			logger.error("Unsupported root edit part type:" + root.getClass());

		overviewCanvas = new Canvas(frame, SWT.NULL);
		overviewCanvas.setLayout(new FillLayout());
		LightweightSystem overview = new LightweightSystem(overviewCanvas);
		overview.setContents(thumbnail);
		frame.layout();
		currentEditor = editor;
    }

    protected void unsetViewer(RSEEditor editor) {
        if (overviewCanvas != null) {
            overviewCanvas.dispose();
            frame.layout();
            overviewCanvas = null;
        } else {
            System.err.println("Canvas is already null!!");
        }
        currentEditor = null;
    }
}
