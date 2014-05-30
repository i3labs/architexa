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
/*
 * Created on Jun 12, 2004
 *
 */
package com.architexa.diagrams.relo.parts;


import org.apache.log4j.Logger;
import org.openrdf.model.Resource;

import com.architexa.diagrams.model.ArtifactFragment;
import com.architexa.diagrams.model.ArtifactRel;
import com.architexa.diagrams.model.BrowseModel;
import com.architexa.diagrams.relo.ReloPlugin;
import com.architexa.diagrams.relo.modelBridge.ControllerDerivedAF;
import com.architexa.diagrams.relo.modelBridge.ReloDoc;
import com.architexa.diagrams.services.PluggableTypes;
import com.architexa.org.eclipse.gef.EditPart;
import com.architexa.org.eclipse.gef.EditPartFactory;
import com.architexa.store.ReloRdfRepository;


/**
 * @author vineet
 *
 */
public class ReloPartFactory implements EditPartFactory {

    static final Logger logger = ReloPlugin.getLogger(ReloPartFactory.class);
	
	protected final BrowseModel bm;

	public ReloPartFactory(BrowseModel bm) {
		this.bm = bm;
	}
	
	protected EditPart getController(ReloRdfRepository rdfModel, Resource modelRes, Resource modelType) {
		return PluggableTypes.getController(rdfModel, modelRes, modelType);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.gef.EditPartFactory#createEditPart(org.eclipse.gef.EditPart, java.lang.Object)
	 */
	public EditPart createEditPart(EditPart context, Object model) {
        ReloRdfRepository repo = bm.getRepo();

        EditPart part = null;

		if (model instanceof ReloDoc) {
			part = new ReloController();
		} else if (model instanceof ControllerDerivedAF) {
			part = ((ControllerDerivedAF)model).createController();
		} else if (model instanceof ArtifactFragment) {
			ArtifactFragment art = (ArtifactFragment) model;
			
			Resource artType = art.queryType(repo);

			if (artType == null || !bm.artifactLoadable(art.getArt(), artType)) {
				logger.error("Artifact not loadable -- type: " + artType 
				        + " ... artifact: " + art.getClass() + " / " + art);
			    return null;
			}

            part = getController(repo, art.getArt().elementRes, artType);
            
		} else if (model instanceof ArtifactRel) {
		    ArtifactRel rel = (ArtifactRel) model;
            
            part = getController(repo, rel.getInstanceRes(), rel.getType());
		} else {
			logger.error("Unrecognized type: " + model.getClass(), new Exception());
			return null;
		}

		part.setModel(model);	// we will phase out need for this
		return part;
	}

}
