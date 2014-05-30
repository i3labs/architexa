package com.architexa.diagrams.relo.jdt.parts;

import jiggle.Graph;
import jiggle.PointedEdge;
import jiggle.Vertex;


import com.architexa.diagrams.relo.parts.AbstractReloEditPart;
import com.architexa.diagrams.relo.parts.ReloArtifactRelEditPart;
import com.architexa.diagrams.relo.parts.SideCenteredAnchor;
import com.architexa.org.eclipse.draw2d.ConnectionAnchor;

public class SimpleCallRelationPart extends ReloArtifactRelEditPart {

    @Override
    protected jiggle.Edge getEdgeForGraph(Graph graph, Vertex source, Vertex target) {
        return new PointedEdge(graph, source, target, /*vert*/false, /*asc*/true);
    }

	@Override
    protected ConnectionAnchor getSourceConnectionAnchor(AbstractReloEditPart srcEP) {
        return new SideCenteredAnchor(srcEP.getLabelFigure(), SideCenteredAnchor.right);
        //return new SideRestrictedChopboxAnchor(srcEP.getLabelFigure(), SideRestrictedChopboxAnchor.right);
	}

	@Override
    protected ConnectionAnchor getTargetConnectionAnchor(AbstractReloEditPart tgtEP) {
        return new SideCenteredAnchor(tgtEP.getLabelFigure(), SideCenteredAnchor.left);
        //return new SideRestrictedChopboxAnchor(tgtEP.getLabelFigure(), SideRestrictedChopboxAnchor.left);
	}

	
}
