package com.architexa.diagrams.relo.parts;

import org.openrdf.model.Resource;

import com.architexa.diagrams.RSECore;
import com.architexa.diagrams.model.NamedRel;
import com.architexa.org.eclipse.draw2d.ChopboxAnchor;
import com.architexa.org.eclipse.draw2d.ConnectionAnchor;
import com.architexa.org.eclipse.gef.ConnectionEditPart;
import com.architexa.org.eclipse.gef.NodeEditPart;
import com.architexa.org.eclipse.gef.Request;
import com.architexa.org.eclipse.gef.RequestConstants;
import com.architexa.org.eclipse.gef.requests.CreateConnectionRequest;

/*
 * This Controller exists since the RootArtifact is an ArtifactFragment but not really a node
 */
public abstract class ArtifactNodeEditPart extends ArtifactEditPart implements NodeEditPart {

	// connection anchors
	public ConnectionAnchor getSourceConnectionAnchor(ConnectionEditPart connection) {
		return new ChopboxAnchor(getLabelFigure());
	}


	public ConnectionAnchor getSourceConnectionAnchor(Request request) {
		return new ChopboxAnchor(getLabelFigure());
	}


	public ConnectionAnchor getTargetConnectionAnchor(ConnectionEditPart connection) {
		return new ChopboxAnchor(getLabelFigure());
	}


	public ConnectionAnchor getTargetConnectionAnchor(Request request) {
		ChopboxAnchor chopBoxAnchor = new ChopboxAnchor(getLabelFigure());

		if(!(request instanceof CreateConnectionRequest) ||
				!RequestConstants.REQ_CONNECTION_END.equals(request.getType())) 
			return chopBoxAnchor;
		CreateConnectionRequest ccr = (CreateConnectionRequest) request;
		if(!(ccr.getNewObject() instanceof NamedRel)) return chopBoxAnchor;

		// General Connection can go between any two frags
		Resource connRelation = ((NamedRel)ccr.getNewObject()).relationRes;
		if(RSECore.namedRel.equals(connRelation)) return chopBoxAnchor;

		// Other connections must have source and target of same type
		// (inheritance must be class -> class, method call and method
		// override must be method -> method)
		if(ccr.getSourceEditPart().getClass().equals(ccr.getTargetEditPart().getClass())) 
			return chopBoxAnchor;

		// Otherwise, the connection is not allowed, so return null so the feedback
		// line just follows the mouse and doesn't anchor to an illegal target
		return null;
	}

}
