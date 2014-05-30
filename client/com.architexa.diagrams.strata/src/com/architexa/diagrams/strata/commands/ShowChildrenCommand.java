/**
 * 
 */
package com.architexa.diagrams.strata.commands;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;

import com.architexa.diagrams.model.ArtifactFragment;
import com.architexa.diagrams.model.ArtifactRel;
import com.architexa.diagrams.model.NamedRel;
import com.architexa.diagrams.strata.StrataPlugin;
import com.architexa.diagrams.strata.model.DependencyRelation;
import com.architexa.diagrams.strata.model.StrataRootDoc;
import com.architexa.diagrams.strata.model.policy.ClosedContainerDPolicy;
import com.architexa.diagrams.ui.LongCommand;
import com.architexa.diagrams.utils.RelUtils;

public class ShowChildrenCommand extends LongCommand {
	/**
	 * 
	 */
	public static final Logger logger = StrataPlugin.getLogger(BreakCommand.class);
	protected boolean showChildren, previousShowChildrenState;
	
	protected ArtifactFragment af;
	protected ClosedContainerDPolicy closedContainerDPolicy;
	
	protected StrataRootDoc rootDoc;
	
	protected List<DependencyRelation> newRels = null;
	
	private boolean copyDeps = false;
	
	
	public ShowChildrenCommand(ArtifactFragment af, String label, StrataRootDoc strataRootDoc) {
		this(af, label, true, strataRootDoc);
	}

	public ShowChildrenCommand(ArtifactFragment af, String label, boolean showChildren, StrataRootDoc strataRootDoc) {
		super(label);
		this.af = af;
		this.showChildren=showChildren;
		this.rootDoc = strataRootDoc;
		if (af.getShownChildrenCnt() == 1)
			copyDeps = true;
	}

	@Override
	public void prep(IProgressMonitor monitor) {
		// this can NOT copy rels that are added in the prep stage of previous cmds in this same long cmd
		if (false){//copyDeps) {
				if (newRels == null)
					newRels = new ArrayList<DependencyRelation>();
					
				List<ArtifactRel> srcRels = af.getSourceConnections();
				List<ArtifactRel> tgtRels = af.getTargetConnections();
				for (ArtifactRel rel : srcRels) {
					if (rel instanceof DependencyRelation)
						newRels.add((DependencyRelation) rel);
				}
				for (ArtifactRel rel : tgtRels) {
					if (rel instanceof DependencyRelation)
						newRels.add((DependencyRelation) rel);
				}
				return;	
		} 
		newRels = rootDoc.getRelationshipsToAdd(af.getShownChildren(), monitor);
		if (monitor.isCanceled())
			newRels = null;
	}
	
	@Override
	public void execute() { 
		execute(af);
	}

	protected void addRels() {
		if (newRels == null) {
			logger.error("Unexpected Error - not prepped");
			return;
		}
		rootDoc.addRelationships(newRels);
	}
	public void execute(ArtifactFragment af) {
		closedContainerDPolicy = (ClosedContainerDPolicy) af.getDiagramPolicy(ClosedContainerDPolicy.DefaultKey);
		addRels();
		
		previousShowChildrenState = ClosedContainerDPolicy.isShowingChildren(closedContainerDPolicy.getHostAF());
		this.closedContainerDPolicy.setShowingChildren(showChildren);
		logger.info("Showing Children of: " + closedContainerDPolicy.getHostAF());
	
		// switch comment/anchored rels to point to the parent if the parent is
		// closed. TODO add support for remembering which child the rel was to
		if (showChildren) return;
		ArtifactFragment artFrag = closedContainerDPolicy.getHostAF();
		for (ArtifactFragment child : ModelUtils.getAllNestedChildren(artFrag)) {
			List<ArtifactRel> conns = new ArrayList<ArtifactRel>(child.getTargetConnections());
			conns.addAll(child.getSourceConnections());
			for (ArtifactRel conn : conns) {
	    		if (conn instanceof NamedRel) {
	    			RelUtils.removeModelSourceConnections(conn.getSrc(), conn);
	    			RelUtils.removeModelTargetConnections(conn.getDest(), conn);
	    			conn.init(conn.getSrc(), artFrag, conn.relationRes);
	    			conn.connect(conn.getSrc(), artFrag);
	    		}
	    	}
		}
	}

	@Override
	public void undo() {
		this.closedContainerDPolicy.setShowingChildren(previousShowChildrenState);
	}
}