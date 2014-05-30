package com.architexa.diagrams.parts;

import java.io.IOException;

import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.rio.RdfDocumentWriter;

import com.architexa.diagrams.RSECore;
import com.architexa.diagrams.model.ArtifactFragment;
import com.architexa.diagrams.model.Comment;
import com.architexa.diagrams.model.DiagramPolicy;
import com.architexa.org.eclipse.draw2d.Figure;
import com.architexa.org.eclipse.draw2d.FigureListener;
import com.architexa.org.eclipse.draw2d.IFigure;
import com.architexa.org.eclipse.draw2d.geometry.Point;
import com.architexa.store.ReloRdfRepository;
import com.architexa.store.StoreUtil;

/**
 * @author Abhishek Rakshit
 *
 */
public class PointPositionedDiagramPolicy extends DiagramPolicy{

	////
	// basic setup
	////
	public static final String DefaultKey = "PositionDiagramPolicy";
	public static final PointPositionedDiagramPolicy Type = new PointPositionedDiagramPolicy();
	
	////
	// Policy Fields, Constructors and Methods 
	////
	public static final URI posX = RSECore.createRseUri("core#posX");
	public static final URI posY = RSECore.createRseUri("core#posY");
	public static final URI relPosX = RSECore.createRseUri("core#relPosX");
	public static final URI relPosY = RSECore.createRseUri("core#relPosY");
	
	Point topLeft = new Point();


	public PointPositionedDiagramPolicy() {}
	
	@Override
	public void writeRDF(RdfDocumentWriter rdfWriter) throws IOException {
		rdfWriter.writeStatement(getHostAF().getInstanceRes(), posX, StoreUtil.createMemLiteral(Integer.toString(topLeft.x)));
		rdfWriter.writeStatement(getHostAF().getInstanceRes(), posY, StoreUtil.createMemLiteral(Integer.toString(topLeft.y)));
	}

	@Override
	public void readRDF(ReloRdfRepository queryRepo) {
		Value xpos = queryRepo.getStatement(getHostAF().getInstanceRes(), posX, null).getObject();
		Value ypos = queryRepo.getStatement(getHostAF().getInstanceRes(), posY, null).getObject();
		if (xpos != null && ypos != null) {
			topLeft.x = Integer.parseInt(((Literal)xpos).getLabel());
			topLeft.y = Integer.parseInt(((Literal)ypos).getLabel());
		}
		getHostAF().firePolicyContentsChanged();
	}

	public void setTopLeft(Point newLoc) {
		topLeft = newLoc;
		if(getHostAF() instanceof Comment)
			((Comment)getHostAF()).setTopLeft(topLeft);
		getHostAF().firePolicyContentsChanged();
	}

	public Point getTopLeft() {
		return topLeft;
	}
	
	class PolicyFigureListener implements FigureListener {
		public Figure figure;
		public PolicyFigureListener(Figure figure) {
			this.figure = figure;
		}
		public void figureMoved(IFigure source) {
			/**
			 * Why do we do this?? Removing it for now as it was causing issues when saving diagrams
			 */
//			if (getHostAF() instanceof Comment && 
//					((Comment)getHostAF()).getRelDistFromDiagTopLeft() != null) {
//				
//				topLeft = ((Comment)getHostAF()).getRelDistFromDiagTopLeft();
//				return;
//			}
			topLeft = figure.getBounds().getTopLeft();
		}
	};

	PolicyFigureListener policyListener;
	public void setActivePositionListener(Figure figure) {
		if (policyListener != null && policyListener.figure == figure) return;
		policyListener = new PolicyFigureListener(figure);
		figure.addFigureListener(policyListener);
	}

	public void getLocToFig(Figure figure) {
		setActivePositionListener(figure);
		Point origLocation = figure.getLocation();
		Point curPosition = getTopLeft();
		if (!origLocation.equals(curPosition)) {
			figure.setLocation(curPosition);
			figure.revalidate();
		}
	}

	/**
	 * sets AF location
	 */
	public static void setLoc(ArtifactFragment af, Point loc) {
		PointPositionedDiagramPolicy positioningPolicy = af.getTypedDiagramPolicy(Type, DefaultKey);
		if (positioningPolicy != null)
			positioningPolicy.setTopLeft(loc);
	}

	/**
	 * gets location from AF and sets figure location (also adds a
	 * listener to update for for future moves)
	 */
	public static void getLocToFig(ArtifactFragment af, Figure figure) {
		PointPositionedDiagramPolicy positioningPolicy = af.getTypedDiagramPolicy(Type, DefaultKey);
		if (positioningPolicy != null) positioningPolicy.getLocToFig(figure);
	}
}
