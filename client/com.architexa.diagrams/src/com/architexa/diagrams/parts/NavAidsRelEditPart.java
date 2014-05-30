/**
 * 
 */
package com.architexa.diagrams.parts;

import org.openrdf.model.URI;

import com.architexa.org.eclipse.draw2d.IFigure;
import com.architexa.store.ReloRdfRepository;

public interface NavAidsRelEditPart {
    IFigure getArrow();
    String getRelationLabel(ReloRdfRepository repo, URI res);
}