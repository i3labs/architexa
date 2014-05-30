package com.architexa.diagrams.parts;

import com.architexa.diagrams.model.AnnotatedRel;

/**
 * @author Seth 
 * 
 * 		   Shared functionality for 'comment' relationships. Any
 *         relationship that is added between ArtFrags independent of diagram
 *         type
 * 
 */

//TODO Is this class needed anymore
public class ArtifactRelEditPart extends AbstractRelationPart {


	@Override
	public void activate() {
		if (isActive())
			return;
		super.activate();
		((AnnotatedRel)getModel()).addPropertyChangeListener(this);
	}

		
}
