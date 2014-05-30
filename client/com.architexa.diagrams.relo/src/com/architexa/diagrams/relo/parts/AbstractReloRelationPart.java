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

import java.util.Map;

import jiggle.Graph;
import jiggle.Vertex;

import org.apache.log4j.Logger;

import com.architexa.diagrams.parts.AbstractRelationPart;
import com.architexa.diagrams.relo.ReloPlugin;
import com.architexa.diagrams.utils.DbgRes;
import com.architexa.org.eclipse.draw2d.ChopboxAnchor;
import com.architexa.org.eclipse.draw2d.ConnectionAnchor;
import com.architexa.org.eclipse.draw2d.GraphAnimation;
import com.architexa.org.eclipse.gef.EditPart;
import com.architexa.org.eclipse.gef.editparts.AbstractGraphicalEditPart;


/**
 * Relo Specific Relation funtionality
 *
 */
public class AbstractReloRelationPart extends AbstractRelationPart {

    static final Logger logger = ReloPlugin.getLogger(AbstractReloRelationPart.class);
	DbgRes tag = new DbgRes(AbstractReloRelationPart.class, this);

	public static void log(String str) {
		logger.info(str);
	    //ConsoleView.log("RP: " + str);
	}
	
	
	
	public void applyGraphResults(Graph graph, Map<AbstractGraphicalEditPart,Object> map) {
	    // nothing needs to be really done here (atleast for now)
	    return;
	}

    public void contributeEdgeToGraph(Graph graph, Map<AbstractGraphicalEditPart,Object> partsToVertexMap) {
		GraphAnimation.recordInitialState(getConnectionFigure());
		//tag.consolePrintln("CONTRIBUTION: Edge - " + this);

		if (getSource() == null || getTarget() == null) {
            /*
             * the below should be an error: the only reason it is happening
             * right now is because when a node is being removed the graph is
             * layed out and then the edges are removed - basically we need the
             * Command in RelationshipsManager to execute immediate and not
             * after the remove node command finishes executing (it has already
             * started and the removal process fires the event) - the best
             * possible solution might be to merge this command with the
             * previous and therefore allow undoing to happen properly
             */ 
			logger.warn("[" + tag.id + "] Null EditPart(s) - [s:" + getSource() + ",t:" + getTarget() + "]");
            
			//ReloController vc = (ReloController) getRoot().getContents();
			//tag.consolePrintln("WARN: Null EditPart(s) - [s:" 
			//        + vc.findEditPart(((ArtifactRel) getModel()).srcCU) + ",t:"
            //        + vc.findEditPart(((ArtifactRel) getModel()).dstCU) + "]");
			return;
		}
		
		/*
		Vertex source = (Vertex) partsToVertexMap.get(getSource());
		Vertex target = (Vertex) partsToVertexMap.get(getTarget());

		if (source == null || target == null) {
			tag.consolePrintln("ERROR: Node not in contribution map - [s:" + source + ",t:" + target + "] :: " + this);
			// let's approximate for now (this is most prob. happening because of
			//  subgraph deinition issues, note, that get.Source()/Target() are not null)
			if (source==null) {
				source = (Vertex) partsToVertexMap.get(getSource().getParent());
				if (source==null)
					tag.consolePrintln("ERROR ERROR ERROR ERROR: " + getSource().getParent() + " is also not in contribution map!!");
			}
			if (target==null) {
			    target = (Vertex) partsToVertexMap.get(getTarget().getParent());
				if (target==null)
					tag.consolePrintln("ERROR ERROR ERROR ERROR: " + getTarget().getParent() + " is also not in contribution map!!");
			}
			return;
		}
		*/
		
		EditPart srcEP = getSource();
		while (!partsToVertexMap.containsKey(srcEP))
            srcEP = srcEP.getParent();
		if (!(partsToVertexMap.get(srcEP) instanceof Vertex)) return;	// TODO: remove this hack 
		Vertex source = (Vertex) partsToVertexMap.get(srcEP);
		EditPart dstEP = getTarget();
		while (!partsToVertexMap.containsKey(dstEP))
		    dstEP = dstEP.getParent();
		if (!(partsToVertexMap.get(dstEP) instanceof Vertex)) return;	// TODO: remove this hack 
		Vertex target = (Vertex) partsToVertexMap.get(dstEP);
		
		//tag.consolePrintln("CONTRIBUTION: Edge [" + srcEP + " --> " + dstEP + "]");
		
        jiggle.Edge e = getEdgeForGraph(graph, source, target);
        graph.insertEdge(e);
		partsToVertexMap.put(this, e);
    }

    protected jiggle.Edge getEdgeForGraph(Graph graph, Vertex source, Vertex target) {
        return new jiggle.Edge (graph, source, target);
    }

    @Override
    public String toString() {
		return getModel().toString() + tag.getTrailer();
	}

    // connection stuff
    
	@Override
    protected ConnectionAnchor getSourceConnectionAnchor() {
		if (getSource() == null) {
		    return super.getSourceConnectionAnchor();
		}
        return getSourceConnectionAnchor((AbstractReloEditPart) getSource());
	}
	@Override
    protected ConnectionAnchor getTargetConnectionAnchor() {
		if (getTarget() == null) {
		    return super.getTargetConnectionAnchor();
		}
        return getTargetConnectionAnchor((AbstractReloEditPart) getTarget());
	}
	protected ConnectionAnchor getSourceConnectionAnchor(AbstractReloEditPart srcEP) {
        return new ChopboxAnchor(srcEP.getLabelFigure());
	}
	protected ConnectionAnchor getTargetConnectionAnchor(AbstractReloEditPart tgtEP) {
        return new ChopboxAnchor(tgtEP.getLabelFigure());
	}


}