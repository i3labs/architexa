/**
 * Copyright (c) 2007-2008 Architexa, Inc
 */ 
package com.architexa.diagrams.strata.ui;

import org.apache.log4j.Logger;

import com.architexa.diagrams.model.Artifact;
import com.architexa.diagrams.model.ArtifactFragment;
import com.architexa.diagrams.model.Comment;
import com.architexa.diagrams.model.NamedRel;
import com.architexa.diagrams.parts.CommentEditPart;
import com.architexa.diagrams.parts.NamedRelationPart;
import com.architexa.diagrams.strata.StrataPlugin;
import com.architexa.diagrams.strata.model.CompositeLayer;
import com.architexa.diagrams.strata.model.DependencyRelation;
import com.architexa.diagrams.strata.model.Layer;
import com.architexa.diagrams.strata.model.StrataRootDoc;
import com.architexa.diagrams.strata.parts.CompositeLayerEditPart;
import com.architexa.diagrams.strata.parts.DependencyRelationEditPart;
import com.architexa.diagrams.strata.parts.LayerEditPart;
import com.architexa.diagrams.strata.parts.StrataArtFragEditPart;
import com.architexa.diagrams.strata.parts.StrataRootEditPart;
import com.architexa.org.eclipse.gef.EditPart;
import com.architexa.org.eclipse.gef.EditPartFactory;


public class StrataEditPartFactory implements EditPartFactory {
	public static final Logger logger = StrataPlugin.getLogger(StrataEditPartFactory.class);

	/* (non-Javadoc)
	 * @see org.eclipse.gef.EditPartFactory#createEditPart(org.eclipse.gef.EditPart, java.lang.Object)
	 */
	public EditPart createEditPart(EditPart context, Object model) {
        EditPart part = null;

        if (model instanceof StrataRootDoc) {
			part = new StrataRootEditPart();
        } else if (model instanceof CompositeLayer) {
				part = new CompositeLayerEditPart();
        } else if (model instanceof Layer) {
				part = new LayerEditPart();
        } else if (model instanceof ArtifactFragment) {
        	if(model instanceof Comment)
        		part=new CommentEditPart();
        	else	
        		part = new StrataArtFragEditPart();
        } else if (model instanceof Artifact) {
			part = new StrataArtFragEditPart();
		} else if (model instanceof DependencyRelation) {
			part = new DependencyRelationEditPart();
		} else if (model instanceof NamedRel) {
			part = new NamedRelationPart();
		} else if (model == null) {
			logger.error("StrataEditPartFactory.createEditPart: Unexpected Null model", new Exception());
			return null;
		} else {
			logger.error("StrataEditPartFactory.createEditPart: Unexpected Type: " + model.getClass());
			return null;
		}

		part.setModel(model);
		return part;
	}

}
