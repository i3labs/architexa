package com.architexa.diagrams.strata.commands;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.openrdf.model.Resource;
import org.openrdf.sesame.sail.StatementIterator;

import com.architexa.diagrams.Activator;
import com.architexa.diagrams.RSECore;
import com.architexa.diagrams.model.ArtifactFragment;
import com.architexa.diagrams.model.ArtifactRel;
import com.architexa.diagrams.model.DiagramPolicy;
import com.architexa.diagrams.strata.model.DependencyRelation;
import com.architexa.diagrams.strata.model.StrataRootDoc;
import com.architexa.diagrams.strata.model.policy.ClosedContainerDPolicy;
import com.architexa.diagrams.strata.model.policy.DependencyRelDPolicy;
import com.architexa.diagrams.ui.LongCommand;
import com.architexa.store.ReloRdfRepository;

public class LoadRelsCommand extends LongCommand {
    static final Logger logger = Activator.getLogger(LoadRelsCommand.class);

	private final List<ArtifactFragment> childrenAF;
	private List<DependencyRelation> newRels = null;
	private final StrataRootDoc rootDoc;

	private ReloRdfRepository inputRDF;

	/**
	 * @param rootDoc 
	 * @param inputRDF 
	 * 
	 */
	
	public LoadRelsCommand(StrataRootDoc rootDoc, List<ArtifactFragment> childrenAF, ReloRdfRepository inputRDF) {
		super("Load Relationships");
		this.rootDoc = rootDoc;
		this.childrenAF = childrenAF;
		this.inputRDF = inputRDF;
	}

	@Override
	public void prep(IProgressMonitor monitor) {
		List<ArtifactFragment> childrenAFWithRels = new ArrayList<ArtifactFragment>();
		// we do not care about rels added by hidden children packages. They are added when the parent package is opened
		for (ArtifactFragment af : childrenAF) {
			if (ClosedContainerDPolicy.isShowingChildren(af.getParentArt()))
				childrenAFWithRels.add(af);
		}
		
		newRels = rootDoc.getRelationshipsToAdd(childrenAFWithRels, monitor); 
	}

	@Override
	public void execute() {
		if (newRels == null) {
			logger.error("Unexpected Error - not prepped");
			return;
		}
		rootDoc.addRelationships(newRels);
		StatementIterator si = inputRDF.getStatements(null, inputRDF.rdfType, RSECore.link);
		while (si.hasNext()) {
			Resource viewRes = si.next().getSubject();
			Resource src = (Resource) inputRDF.getStatement(viewRes, inputRDF.rdfSubject, null).getObject();
			Resource dest = (Resource) inputRDF.getStatement(viewRes, inputRDF.rdfObject, null).getObject();
	        
			for (DependencyRelation rel : newRels) {
				if (!rel.getSrc().getInstanceRes().equals(src) || !rel.getDest().getInstanceRes().equals(dest))
					continue;
				ArtifactRel.ensureInstalledPolicy(rel, DependencyRelDPolicy.DefaultKey, DependencyRelDPolicy.class);
				for (DiagramPolicy diagPolicy : rel.getDiagramPolicies()) {
		    		diagPolicy.getHostRel().setInstanceRes(viewRes);
					diagPolicy.readRDF(inputRDF);
				} 
			}
		}
	}

	
}
