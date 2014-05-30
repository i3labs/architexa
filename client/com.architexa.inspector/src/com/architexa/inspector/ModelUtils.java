package com.architexa.inspector;

import java.util.ArrayList;
import java.util.List;

import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.sesame.sail.StatementIterator;

import com.architexa.diagrams.RSECore;
import com.architexa.diagrams.jdt.RJCore;
import com.architexa.diagrams.model.ArtifactFragment;
import com.architexa.inspector.InspectorView.TreeNode;
import com.architexa.store.ReloRdfRepository;
import com.architexa.store.StoreUtil;

// we should ideally move references to resources and keep them to artfrags 
public class ModelUtils {

	private static ReloRdfRepository rdfRepo = null;
	
	public static ReloRdfRepository getRepo() {
		if (rdfRepo == null)
			rdfRepo = StoreUtil.getDefaultStoreRepository();
		return rdfRepo;
	}
	

	public static List<TreeNode> getProjects() {
		ReloRdfRepository repo = getRepo();
		List<Resource> childrenRes = new ArrayList<Resource>(); 
		repo.getResourcesFor(childrenRes, null, repo.rdfType, RJCore.projectType);
		List<TreeNode> retVal = new ArrayList<TreeNode>(childrenRes.size());
		for (Resource childRes : childrenRes) {
			retVal.add(new TreeNode(new ArtifactFragment(childRes), repo.hasStatement(childRes, RSECore.contains, null)));
		}
		return retVal;
	}

	public static List<TreeNode> getChildren(TreeNode obj) {
		ReloRdfRepository repo = getRepo();
		List<Resource> childrenRes = new ArrayList<Resource>(); 
		repo.getResourcesFor(childrenRes, obj.af.getArt().elementRes, RSECore.contains, null);
		List<TreeNode> retVal = new ArrayList<TreeNode>(childrenRes.size());
		for (Resource childRes : childrenRes) {
			retVal.add(new TreeNode(new ArtifactFragment(childRes), repo.hasStatement(childRes, RSECore.contains, null)));
		}
		return retVal;
	}


	public static String getName(TreeNode to) {
		String resStr = to.af.getArt().elementRes.toString();
		int tgtNdx = -1;
		//tgtNdx = Math.max(tgtNdx, resStr.lastIndexOf('.'));
		tgtNdx = Math.max(tgtNdx, resStr.lastIndexOf('$'));
		tgtNdx = Math.max(tgtNdx, resStr.lastIndexOf('#'));
		return resStr.substring(tgtNdx+1);
	}


	public static void dumpDB() {
		ReloRdfRepository repo = getRepo();
		List<TreeNode> projs = getProjects();
		for (TreeNode proj : projs) {
			dumpStatements(repo, "", proj.af.getArt().elementRes);
		}
	}


	private static void dumpStatements(ReloRdfRepository repo, String hdr, Resource res) {
		StatementIterator resStmts = repo.getStatements(res, null, null);
		while (resStmts.hasNext()) {
			Statement stmt = resStmts.next();
			System.out.println(hdr + stmt.getSubject() + " " + stmt.getPredicate() + " " + stmt.getObject());
			if (stmt.getPredicate().equals(RSECore.contains) && stmt.getObject() instanceof Resource)
				dumpStatements(repo, hdr + "   ", (Resource) stmt.getObject());
		}
	}

}
