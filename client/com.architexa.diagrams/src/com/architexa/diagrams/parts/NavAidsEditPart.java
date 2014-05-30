/**
 * 
 */
package com.architexa.diagrams.parts;

import java.util.List;

import org.apache.commons.collections.Predicate;
import org.eclipse.jface.action.IMenuManager;
import org.openrdf.model.Resource;

import com.architexa.diagrams.model.Artifact;
import com.architexa.diagrams.model.ArtifactFragment;
import com.architexa.diagrams.model.DirectedRel;
import com.architexa.diagrams.ui.MultiAddCommandAction;
import com.architexa.org.eclipse.draw2d.IFigure;
import com.architexa.org.eclipse.gef.EditPartViewer;
import com.architexa.org.eclipse.gef.commands.CompoundCommand;
import com.architexa.org.eclipse.gef.editparts.AbstractGraphicalEditPart;
import com.architexa.store.ReloRdfRepository;

public interface NavAidsEditPart {
    List<NavAidsSpec> getSingleSelectHandlesSpecList(NavAidsEditPolicy policy);
    List<NavAidsSpec> getMultiSelectHandlesSpecList(NavAidsEditPolicy policy);
    void showAllDirectRelation(CompoundCommand btnExecCmd, DirectedRel rel, Predicate filter);
    MultiAddCommandAction getShowRelAction(BasicRootController rc, DirectedRel rel, Object relArt, String relArtLbl);
    String getRelModelLabel(Object model);
    
    // should be implemented by base (and we need it)
    EditPartViewer getViewer();
    ReloRdfRepository getRepo();
    List<AbstractGraphicalEditPart> getSelectedAGEP();
    BasicRootController getRootController();
    List<Artifact> listModel(ReloRdfRepository repo, DirectedRel rel, Predicate filter);
    
    // filters and checks if it has not been added
    List<Artifact> showableListModel(ReloRdfRepository repo, DirectedRel rel, Predicate filter);
    
    Resource getElementRes();
    ArtifactFragment getArtFrag();
    
	IFigure getMoreButton();
	void buildNavAidMenu(List<MultiAddCommandAction> menuActions, NavAidsSpec spec, 
			IMenuManager defaultMenu, DirectedRel rel);
}