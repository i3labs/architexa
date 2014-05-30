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
package com.architexa.diagrams.relo.parts;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.jface.resource.ImageDescriptor;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.sesame.sail.StatementIterator;

import com.architexa.diagrams.RSECore;
import com.architexa.diagrams.commands.SetConnectionBendpointCommand;
import com.architexa.diagrams.model.ArtifactFragment;
import com.architexa.diagrams.model.ArtifactRel;
import com.architexa.diagrams.model.BrowseModel;
import com.architexa.diagrams.model.ConnectionBendpoint;
import com.architexa.diagrams.model.NamedRel;
import com.architexa.diagrams.parts.ConnectionBendpointDPolicy;
import com.architexa.diagrams.parts.NavAidsRelEditPart;
import com.architexa.diagrams.relo.ReloPlugin;
import com.architexa.diagrams.relo.ui.ColorScheme;
import com.architexa.diagrams.relo.ui.ReloEditor;
import com.architexa.diagrams.services.PluggableTypes;
import com.architexa.diagrams.services.PluggableTypes.PluggableTypeInfo;
import com.architexa.diagrams.ui.ImageCache;
import com.architexa.org.eclipse.draw2d.Bendpoint;
import com.architexa.org.eclipse.draw2d.IFigure;
import com.architexa.org.eclipse.draw2d.Label;
import com.architexa.org.eclipse.draw2d.MidpointLocator;
import com.architexa.org.eclipse.draw2d.PolylineConnection;
import com.architexa.org.eclipse.draw2d.RoundedPolylineConnection;
import com.architexa.org.eclipse.draw2d.geometry.Point;
import com.architexa.org.eclipse.gef.commands.CompoundCommand;
import com.architexa.org.eclipse.gef.editpolicies.SelectionEditPolicy;
import com.architexa.store.ReloRdfRepository;


/**
 * Support for RDF
 * Note: Both source and targe have to be ArtifactEditPart
 */
public class ReloArtifactRelEditPart extends AbstractReloRelationPart implements NavAidsRelEditPart {
    static final Logger logger = ReloPlugin.getLogger(ReloArtifactRelEditPart.class);
    
	public BrowseModel getBrowseModel() {
		return ((ReloController) getRoot().getContents()).bm;
	}

    public Resource getInstanceRes() {
    	return getArtifactRel().getInstanceRes();
    }
    public void setInstanceRes(Resource instanceRes) {
    	getArtifactRel().setInstanceRes(instanceRes);
    }
    
    @Override
    protected void refreshBendpoints() {
    	getConnectionFigure().setRoutingConstraint(ConnectionBendpointDPolicy.getBendpoints(getArtifactRel()));
    }
    
    @Override
    protected void createEditPolicies() {
        super.createEditPolicies();
        installEditPolicy("DebugRole", new SelectionEditPolicy() {
			@Override
			protected void hideSelection() {}

			@Override
			protected void showSelection() {
		        logger.info("sel: " + getHost().getModel() + " {{ " + getHost().getClass() + "/" + getHost().getModel().getClass());
			}});
    }

    public ArtifactRel getArtifactRel() {
        return (ArtifactRel) getModel();
    }
    
    public String getRelationLabel() {
        return getRelationLabel(getBrowseModel().getRepo(), getArtifactRel().getType());
    }

    public String getRelationLabel(ReloRdfRepository repo, URI relType) {
    	PluggableTypeInfo pti = PluggableTypes.getRegisteredTypeInfo(relType, repo, relType);
    	if (pti != null) return pti.label; else return "";
    }

    /**
     * @see com.architexa.org.eclipse.gef.editparts.AbstractConnectionEditPart#createFigure()
     */
    @Override
    protected IFigure createFigure() {
    	PolylineConnection conn = (PolylineConnection) super.createFigure();
        conn.setToolTip(new Label(" " + getRelationLabel() + " "));
        
		ArtifactFragment destAF = ((ArtifactRel) getModel()).getDest();
		ReloRdfRepository repo = null;
		if (getTarget()!=null) repo = ((ArtifactEditPart) getTarget()).getRepo();
		if (getSource()!=null) repo = ((ArtifactEditPart) getSource()).getRepo();
		
		if (!RSECore.isInitialized(repo , destAF.getArt().elementRes))
			conn.setForegroundColor(ColorScheme.ghostBorder);
        return conn;
    }

    
    @Override
    public void refreshVisuals() {
    	IFigure connFigure = getFigure();
    	RoundedPolylineConnection conn = null;
		if(connFigure instanceof RoundedPolylineConnection) {
			conn = (RoundedPolylineConnection)connFigure;
		}
    	if(conn==null) return;
    	    	
    	ArtifactRel artRel = getArtifactRel();
    	Resource src = artRel.getSrc().getArt().elementRes;
    	Resource dst = artRel.getDest().getArt().elementRes;
    	URI link = artRel.relationRes;
    	
    	// we do not want to check against the saved repo for error flags since there will not be any differences
    	//ReloRdfRepository repo = ((ReloDoc)getBrowseModel().getRootArt()).getInputRDFRepo();
    	//if (repo == null)
    	
    	// this is used when saved file is opened
    	ReloRdfRepository repo = this.getBrowseModel().getRepo();
        StatementIterator it = repo.getStatements(src, link, dst);
		Literal isUserCreated = (Literal)repo.getStatement(artRel.getInstanceRes(), RSECore.userCreated, null).getObject();
		if (it.hasNext() || getModel() instanceof NamedRel || isUserCreated != null) {
			removeRelationErrDecoration(this, conn);
		} else {
			addRelationErrDecoration(this, conn);
		}
    }

	private static int relLabelIndex = 0;
    private static void addRelationErrDecoration(ReloArtifactRelEditPart arep, RoundedPolylineConnection conn) {
    	//Don't add decoration if it's already been added
    	if(conn.getChildren().get(relLabelIndex) instanceof Label) return;
    	
    	MidpointLocator labelLocator = new MidpointLocator(conn, 0);
    	Label label = new Label(ImageCache.calcImageFromDescriptor(ImageDescriptor.createFromFile(ReloEditor.class, "error_co.gif")));
    	label.setOpaque(false);
    	conn.add(label, labelLocator, relLabelIndex);
    	arep.setFigure(conn);
    }
    private static void removeRelationErrDecoration(ReloArtifactRelEditPart arep, RoundedPolylineConnection conn) {
    	if(!(conn.getChildren().get(relLabelIndex) instanceof Label)) return;
    	
    	Label relLabel = (Label) conn.getChildren().get(relLabelIndex);
    	conn.remove(relLabel);
    	arep.setFigure(conn);
    }

    //Overriden by the respective parts
	public void addBendPointRemoveCmd(CompoundCommand cmd, Point newPoint, boolean isSrc) {}
	public void addMoveAllBendpointCmd(CompoundCommand cmd, Point moveDelta) {
		List<Bendpoint> constraints = ConnectionBendpointDPolicy.getBendpoints(getArtifactRel());
		List<Bendpoint> constraintsCopy = new ArrayList<Bendpoint>();
		for (int i = 0; i < constraints.size(); i++) {
			ConnectionBendpoint bp = (ConnectionBendpoint) constraints.get(i);
			Point loc = new Point(bp.getLocation());
			loc.translate(moveDelta);
			constraintsCopy.add(i, new ConnectionBendpoint(loc));
		}
		cmd.add(new SetConnectionBendpointCommand(constraintsCopy, getArtifactRel()));
	}
    
}