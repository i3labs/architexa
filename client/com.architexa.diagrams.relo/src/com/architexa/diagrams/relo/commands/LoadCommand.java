/**
 * 
 */
package com.architexa.diagrams.relo.commands;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.Closure;
import org.apache.commons.collections.CollectionUtils;
import org.eclipse.core.resources.IResource;
import org.openrdf.model.Resource;
import org.openrdf.sesame.sail.StatementIterator;

import com.architexa.diagrams.RSECore;
import com.architexa.diagrams.model.ArtifactFragment;
import com.architexa.diagrams.model.ArtifactRel;
import com.architexa.diagrams.model.Comment;
import com.architexa.diagrams.model.DocPath;
import com.architexa.diagrams.relo.graph.GraphLayoutManager;
import com.architexa.diagrams.relo.modelBridge.ReloDoc;
import com.architexa.diagrams.relo.parts.ReloController;
import com.architexa.diagrams.relo.ui.ReloEditor;
import com.architexa.diagrams.utils.LoadUtils;
import com.architexa.org.eclipse.gef.commands.Command;
import com.architexa.store.ReloRdfRepository;
import com.architexa.store.RepositoryMgr;
import com.architexa.store.StoreUtil;

public final class LoadCommand extends Command {
	/**
	 * 
	 */
	private final ReloController reloController;
	private ReloDoc doc;
	private ReloRdfRepository repo;

	public LoadCommand(ReloController reloController, ReloDoc reloDoc, ReloRdfRepository reloRdfRepository) {
		//super("Load document (" + reloController.getReloDoc().getInputItems().size() + " items)");
		super("Load document");
		this.reloController = reloController;
		this.doc = reloDoc;
		this.repo = reloRdfRepository;
	}

	@Override
	public void execute() {
		((RepositoryMgr)repo).setFileRepo(StoreUtil.getMemRepository());
		// connect to the model and so set the focusCU
		if (doc.getInputItems().size() > 0) {
		    addFocusArtifacts(doc.getInputItems());
		}
		if (doc.getInputRDFRepo() != null) {
			// @tag reviewCode: not sure why the 2 lines below happen
			IResource editorResource = reloController.getReloEditorResource();
			if(LoadUtils.outdatedCheckingOn) LoadUtils.removeErrors(editorResource);
	        Map<Resource,ArtifactFragment> instanceRes2AFMap = new HashMap<Resource,ArtifactFragment> ();
	        Map<Resource,ArtifactRel> instanceRes2ARMap = new HashMap<Resource,ArtifactRel> ();
	        addNodes(doc.getInputRDFRepo(), instanceRes2AFMap);
			addLinks(doc.getInputRDFRepo(), instanceRes2AFMap, instanceRes2ARMap);
			
			// go through loaded AF's and AR's to make sure that they are all anchored
	    	// @tag rearch-stabilize: we need to work towards removing this - especially since we now have PointPositionedDiagramPolicy
			GraphLayoutManager lm = reloController.getLayoutMgr();
			for (ArtifactFragment node : instanceRes2AFMap.values()) {
				if(node instanceof Comment){
					lm.anchorPart(reloController.createOrFindCommentEditPart(node));
				}else
					lm.anchorPart(reloController.findArtifactEditPart(node));
			}
			for (ArtifactRel rel : instanceRes2ARMap.values()) {
				lm.anchorPart(reloController.findEditPart(rel));
			}
			
			
			// go through the loaded AF's and AR's - check and add decoration if the relo file is out-of-date
			// @liz why does line 2 work and line 1 not work? I would have guessed line 1 to work and not line 2.
			// @liz why do errors on nodes not seem to be shown?
			//ReloRdfRepository repo = getRepo();
			//ReloRdfRepository repo = StoreUtil.getDefaultStoreRepository();
			ReloEditor editor = this.reloController.getReloEditor();
			//IResource editorResource = getReloEditorResource();
			if(LoadUtils.outdatedCheckingOn) {
				for (ArtifactFragment loadedAF : instanceRes2AFMap.values()) {
					if(isOutdated(repo, loadedAF, editorResource)) {
						addErrors(editorResource, loadedAF);
						if (editor != null) editor.addErrorDecoration();
					}
				}
				for (ArtifactRel loadedAR : instanceRes2ARMap.values()) {
					// find the source and destination resources and check whether the link between them still exists
					if(isOutdated(repo, loadedAR, editorResource)) {
						addErrors(editorResource, loadedAR);
						if (editor != null) editor.addErrorDecoration();
					}
				}
			}
		}
	}

	// TODO: want to move the below methods to LoadUtils, but can't because ArtifactFragment 
	//     & ArtifactRel haven't had their dependencies cleaned
	// Easy Accessors - Raising the abstraction level (below needs JavaDoc)
	public boolean isOutdated(ReloRdfRepository repo, ArtifactFragment ar, IResource parentRDFFileResource) {
		return LoadUtils.isOutdated(repo, ar.getArt().elementRes, parentRDFFileResource);
	}

	public boolean isOutdated(ReloRdfRepository repo, ArtifactRel ar, IResource parentRDFFileResource) {
		return LoadUtils.isOutdated(repo, ar.getSrc().getArt().elementRes, ar.relationRes, ar.getDest().getArt().elementRes, parentRDFFileResource);
	}

	public void addErrors(IResource parentRDFFileResource, ArtifactFragment af) {
		LoadUtils.addErrors(parentRDFFileResource, af.getArt().elementRes, null);
	}

	public void addErrors(IResource parentRDFFileResource, ArtifactRel ar) {
		LoadUtils.addErrors(parentRDFFileResource, ar.getSrc().getArt().elementRes, ar.relationRes);
	}
	
	
    void addFocusArtifacts(List<?> items) {
	    CollectionUtils.forAllDo(items, new Closure() {
            public void execute(Object item) {
                ArtifactFragment art = null;
                if (item instanceof DocPath)
                    art = doc.getArtifact( ((DocPath) item).src );
		        else 
		            art = doc.getArtifact(item);
		        
		        if (art == null) return;
		        
		        doc.addVisibleArt(art);
		        
		        /*
                //logger.info("Creating aep: " + art);
            	ArtifactEditPart aep  = createOrFindArtifactEditPart(art);
           	
                // is relationship stored?
                if (item instanceof DocPath) {
                    DocPath path = (DocPath) item;
                    Artifact dstArt = bm.getArtifact( path.dst );
                    ArtifactEditPart dstAEP  = createOrFindArtifactEditPart(dstArt);
                    if (dstAEP != null)
                        ReloController.this.addRel(aep, path.rel, dstAEP);
                }
                */
            }});
	    reloController.performUndoableLayout();
	}
    
    void addNodes(ReloRdfRepository docRepo, Map<Resource,ArtifactFragment> instanceRes2AFMap) {
    	ArtifactFragment.readChildren(doc, docRepo, instanceRes2AFMap, doc.getInstanceRes(), doc);
    }
    
    void addLinks(ReloRdfRepository docRepo,
			Map<Resource, ArtifactFragment> instanceRes2AFMap,
			Map<Resource, ArtifactRel> instanceRes2ARMap) {
    	
        StatementIterator si = docRepo.getStatements(null, docRepo.rdfType, RSECore.link);
		while (si.hasNext()) {
			Resource viewRes = si.next().getSubject();
            ArtifactRel.readRDF(doc, docRepo, viewRes, instanceRes2AFMap, instanceRes2ARMap);
		}
		si.close();
    }

    // Creates a list of the ArtFrags being loaded so that showIncludedRels can know about them
	public List<ArtifactFragment> getArtFragList() {
		List<ArtifactFragment> artFragList = new ArrayList<ArtifactFragment>();
		if (doc.getInputItems().size() > 0) {
			for (Object item : doc.getInputItems()) {
				if (item instanceof DocPath)
					artFragList.add(doc.getArtifact(((DocPath) item).src));
				else
					artFragList.add(doc.getArtifact(item));
			}

		}
		return artFragList;
	}
	
}