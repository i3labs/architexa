/* 
 * Copyright (c) 2004-2005 Massachusetts Institute of Technology. This code was
 * developed as part of the Haystack (http://haystack.lcs.mit.edu/) research 
 * project at MIT. Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation files (the 
 * "Software"), to deal in the Software without restriction, including without 
 * limitation the rights to use, copy, modify, merge, publish, distribute, 
 * sublicense, and/or sell copies of the Software, and to permit persons to whom
 * the Software is furnished to do so, subject to the following conditions: 
 * 
 * The above copyright notice and this permission notice shall be included in 
 * all copies or substantial portions of the Software. 
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR 
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, 
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE 
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER  
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, 
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE. 
 */
package com.architexa.diagrams.parts;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.eclipse.jface.viewers.TextCellEditor;

import com.architexa.diagrams.eclipse.gef.UndoableLabelSource;
import com.architexa.diagrams.model.ArtifactRel;
import com.architexa.diagrams.model.DiagramPolicy;
import com.architexa.diagrams.model.NamedRel;
import com.architexa.diagrams.model.WidthDPolicy;
import com.architexa.diagrams.ui.FontCache;
import com.architexa.diagrams.ui.RSEEditor;
import com.architexa.org.eclipse.draw2d.Graphics;
import com.architexa.org.eclipse.draw2d.IFigure;
import com.architexa.org.eclipse.draw2d.Label;
import com.architexa.org.eclipse.draw2d.MidpointLocator;
import com.architexa.org.eclipse.draw2d.PolygonDecoration;
import com.architexa.org.eclipse.draw2d.PolylineConnection;
import com.architexa.org.eclipse.draw2d.Shape;
import com.architexa.org.eclipse.draw2d.geometry.Dimension;
import com.architexa.org.eclipse.draw2d.geometry.PointList;
import com.architexa.org.eclipse.draw2d.geometry.Rectangle;
import com.architexa.org.eclipse.gef.EditPart;
import com.architexa.org.eclipse.gef.EditPolicy;
import com.architexa.org.eclipse.gef.Request;
import com.architexa.org.eclipse.gef.RequestConstants;
import com.architexa.org.eclipse.gef.tools.DirectEditManager;
import com.architexa.org.eclipse.gef.tools.LabelDirectEditManager;



/**
 * TODO: need to listen to model and update the label and refresh
 */
public class NamedRelationPart extends ArtifactRelEditPart implements UndoableLabelSource, PropertyChangeListener {
    
	@Override
    protected IFigure createFigure() {
	    PolylineConnection conn = (PolylineConnection) super.createFigure();
	    conn.setLineStyle(Graphics.LINE_DOT);
	    conn.setLineWidth(getPolicyWidth());
	    Dimension arrowDim = new Dimension(getPolicyWidth()+defaultArrowSize.width, getPolicyWidth()+defaultArrowSize.height);
	    conn.setTargetDecoration(getSizedArrow(arrowDim));
	    
        lbl = new Label(getAnnoLabelText()){
        	@Override
			public Rectangle getClientArea(Rectangle rect) {
				Rectangle origArea = new Rectangle(super.getClientArea(rect).getCopy());
				origArea.expand(11, 0); // To make sure that the editing part encompasses the whole comment and not scrol to next line
				return origArea;
			}
        	
        	@Override
			public Rectangle getTextBounds() {
				Dimension dim = new Dimension(getTextSize());
				if (dim.width > 0) {
					dim.expand(11, 0);
				}
				return new Rectangle(getBounds().getLocation().translate(getTextLocation()), dim);
			}
        };
        lbl.setFont(FontCache.dialogFont);
        conn.add(lbl, new MidpointLocator(conn, 0));

        return conn;
	}
	
	@Override
	protected void activateFigure() {
		getLayer(RSEEditor.GENERAL_CONNECTION_LAYER).add(getFigure());
	}
	
	@Override
	protected void deactivateFigure() {
		getLayer(RSEEditor.GENERAL_CONNECTION_LAYER).remove(getFigure());
		getConnectionFigure().setSourceAnchor(null);
		getConnectionFigure().setTargetAnchor(null);
	}
	
	public static Dimension defaultArrowSize = new Dimension(7,3);
	public static PolygonDecoration getSizedArrow(Dimension dimension) {
        PolygonDecoration arrow = new PolygonDecoration();
       
        PointList triangleBaseAtConnEnd = new PointList();
        triangleBaseAtConnEnd.addPoint(1, 0);
        triangleBaseAtConnEnd.addPoint(0, 1);
        triangleBaseAtConnEnd.addPoint(0, -1);
        arrow.setTemplate(triangleBaseAtConnEnd);

        arrow.setScale(dimension.width, dimension.height);
        return arrow;
    }

	private int getPolicyWidth() {
		DiagramPolicy widthPol = ((ArtifactRel)getModel()).getDiagramPolicy(WidthDPolicy.DefaultKey);
	    if (widthPol != null && widthPol instanceof WidthDPolicy)
	    	return ((WidthDPolicy) widthPol).getWidth();
	    return -1;
	}

	@Override
    protected void createEditPolicies() {
        super.createEditPolicies();
        installEditPolicy(EditPolicy.DIRECT_EDIT_ROLE, new AnnoLabelDirectEditPolicy(this, "Edit Relation"));
        ArtifactRel.ensureInstalledPolicy((ArtifactRel)getModel(), WidthDPolicy.DefaultKey, WidthDPolicy.class);
    }
	
    protected DirectEditManager manager;
    protected void performDirectEdit() {
        if (manager == null)
            manager = new LabelDirectEditManager(
                            this, 
                            TextCellEditor.class, 
                            new AnnoLabelCellEditorLocator(getAnnoLabelFigure()), 
                            getAnnoLabelFigure());
        manager.show();
    }

    @Override
    public void performRequest(Request request) {
        if (request.getType() == RequestConstants.REQ_DIRECT_EDIT)
            performDirectEdit();
        else if (request.getType() == RequestConstants.REQ_OPEN)
            performDirectEdit();
        else
            super.performRequest(request);
    }

    Label lbl = null;
    public Label getAnnoLabelFigure() {
        return lbl;
    }
    public void setAnnoLabelText(String text) {
    	((NamedRel)getModel()).setAnnoLabelText(text);
    }
    public String getAnnoLabelText() {
        return ((NamedRel)getModel()).getAnnoLabelText();
    }
    
	@Override
	public void refreshVisuals() {
		super.refreshVisuals();
		int width = getPolicyWidth();
		changeWidth(width);
	    Dimension arrowDim = new Dimension(width+defaultArrowSize.width, width+defaultArrowSize.height);
		((PolylineConnection) figure).setTargetDecoration(getSizedArrow(arrowDim));
		getAnnoLabelFigure().setText(getAnnoLabelText());
		lbl.setSize(lbl.getPreferredSize());
		figure.setToolTip(new Label(lbl.getText()));
	}

	public String getOldAnnoLabelText() {
		return getAnnoLabelText();
	}

	public void setOldAnnoLabelText(String str) {
		setAnnoLabelText(str);
	}
	
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		String prop = evt.getPropertyName();
		if (WidthDPolicy.DefaultKey.equals(prop)) {
			changeWidth(WidthDPolicy.getWidth((ArtifactRel) getModel()));
		}
		super.propertyChange(evt);
	}

	private void changeWidth(int width) {
		IFigure lineFig = getFigure();
		if (lineFig instanceof PolylineConnection)
			((PolylineConnection) lineFig ).setLineWidth(width);
	}
	
	@Override
    public void setSelected(int value) {
		super.setSelected(value);
		if (value != EditPart.SELECTED_NONE) {
			((Shape) getFigure()).setLineWidth(getPolicyWidth()+1);
		} else {
			((Shape) getFigure()).setLineWidth(getPolicyWidth());
		}
	}
	
}