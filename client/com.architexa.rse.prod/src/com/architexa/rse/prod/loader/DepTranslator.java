package com.architexa.rse.prod.loader;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;

import com.architexa.diagrams.RSECore;
import com.architexa.diagrams.jdt.RJCore;
import com.architexa.diagrams.jdt.builder.asm.DepAndChildrenStrengthSummarizer;
import com.architexa.diagrams.jdt.builder.asm.LogicalContainmentHeirarchyProcessor;
import com.architexa.diagrams.model.Artifact;
import com.architexa.rse.RSE;
import com.architexa.store.PckgDirRepo;
import com.architexa.store.ReloRdfRepository;

/**
 * Writes Dependencies to the repository based on the given input
 */
public class DepTranslator extends ToolRunner.ShellStreamBase {
	//private final String projName;
	//private final String commonIDStart;
	private final int idBegSkip;
	
	private final ReloRdfRepository repo;
	private final Resource projRes;
	
	// add additional bits to the store
	// I. containment heirarchy
	final LogicalContainmentHeirarchyProcessor lhProcessor;
	// II. dependency summarization
	final DepAndChildrenStrengthSummarizer dacss;

	
	public DepTranslator(ReloRdfRepository repo, String projName, String commonIDStart) {
        this.repo = repo;
		//this.projName = projName;
		//this.commonIDStart = commonIDStart;

		this.idBegSkip = commonIDStart.length();
		this.projRes = RSECore.encodedIdToResource(repo, RSE.appRealTag + "-eclipseProj#", projName);

		add(projRes, repo.rdfType, RJCore.projectType);
		add(projRes, RJCore.pckgDirType, Boolean.toString(true));
		
		lhProcessor = new LogicalContainmentHeirarchyProcessor(); 
		lhProcessor.init(repo, new Artifact(projRes), null);
		dacss = DepAndChildrenStrengthSummarizer.getDACSS(repo, null, PckgDirRepo.getPDR(repo, RSECore.pckgDirContains));
	}

	private void add(Resource subj, URI pred, Value obj) {
		//System.out.println(subj + " " + pred + " " + obj);
		repo.addStatement(subj, pred, obj);
	}
	private void add(Resource subj, URI pred, String obj) {
		add(subj, pred, repo.getLiteral(obj));
	}
	private static boolean isIDMeth(String id) {
		return id.endsWith("()");
	}
	private static boolean isIDMethORField(String id) {
		int typeNdx = id.indexOf('$');
		if (id.indexOf('.', typeNdx) != -1)
			return true;
		else
			return false;
	}
	// the same as add - but these rel and summarized!
	// to connect to DACSS - we need src and dst types - we therefore get id's
	// TODO - connect to DACSS
	private void addRel(String srcId, URI pred, String dstId) {
		//System.err.println("\tAddRel: " + srcId + " --[" + pred.toString() + "]--> " + dstId);
		// we just want to do: add(getRes(srcId), pred, getRes(dstId));
		// but the add is going to be done by dacss (as well as other
		// caching related support)
		
		Resource srcRes = getRes(srcId);
		Resource dstRes = getRes(dstId);

		// src is method
		if (isIDMeth(srcId)) {
			if (isIDMeth(dstId)) {
				dacss.updateSrcFldrCache(getParentRes(getParentId(srcId)));
				dacss.updateSrcClassCache(getParentRes(srcId));
				dacss.storeNCacheMethMeth(srcRes, pred, dstRes, getParentRes(dstId));
			} else {
				dacss.updateSrcFldrCache(getParentRes(getParentId(srcId)));
				dacss.updateSrcClassCache(getParentRes(srcId));
				dacss.storeNCacheMethType(srcRes, pred, dstRes);
			}
			return;
		}
		if (isIDMethORField(srcId)) { // src is field
			startNewTx();
			dacss.updateSrcFldrCache(getParentRes(getParentId(srcId)));
			dacss.updateSrcClassCache(getParentRes(srcId));
			dacss.storeNCacheFieldType(srcRes, pred, dstRes);
			return;
		}
		// src is type
		if (isIDMethORField(dstId)) {
			// We should not get dependencies from type to field/method
			// TODO: test with static initializers etc
			System.err.println("Not recording dep: " + srcId + " --[" + pred.toString() + "]--> " + dstId);
			return;
		} else {
			dacss.updateSrcFldrCache(getParentRes(srcId));
			dacss.storeNCacheTypeType(srcRes, pred, dstRes);
		}
	}

	private void startNewTx() {
		repo.commitTransaction(); repo.startTransaction();
	}

	// TODO: we don't need to store any id's in the map below - just storing
	// them makes it faster - we would otherwise be writing the same
	// statements multiple times.
	Map<String, Resource> idToRes = new HashMap<String, Resource>(200);

	private Resource getRes(String myId) {
		if (!idToRes.containsKey(myId)) {
			Resource res = RJCore.idToResource(repo, myId);
			mapIdToRes(myId, res);
		}
		return idToRes.get(myId);
	}
	private void mapIdToRes(String myId, Resource res) {
		idToRes.put(myId, res);
	}

	private String getParentId(String myId) {
		int lastNdx = Math.max(myId.lastIndexOf('.'), myId.lastIndexOf('$'));
		return myId.substring(0, lastNdx);
	}
	private Resource getParentRes(String myId) {
		String prntId = getParentId(myId);
		if (!idToRes.containsKey(prntId)) {
			Resource prntRes = RJCore.idToResource(repo, prntId);
			if (!prntId.contains("$")) {
				// the parent res should have existed - unless it is for folders/packages
				add(projRes, RSECore.contains, prntRes);
				add(prntRes, RSECore.initialized, Boolean.toString(true));
				add(prntRes, RJCore.pckgDirType, Boolean.toString(true));
				lhProcessor.processPckg(new Artifact(prntRes));
			}
			mapIdToRes(prntId, prntRes);
		}
		return idToRes.get(prntId);
	}
	
	// we know that these types become ref types
	private Set<String> refTypes = new HashSet<String>(Arrays.asList(new String[] {"Couple", "Create", "Set", "Use", "Typed"}));

    public static final URI tableProp  = createRseUri("prod#tableProp");
    private static final URI createRseUri(String str) {
    	return RSECore.createRseUri(str);
    }

    @Override
	public void writeLine(String line) {
		System.err.println(line);
		super.writeLine(line);
		String[] tokens = line.split(",");
		if (tokens.length == 0) {
			System.err.println("ERR"); 
			return;
		}
		if (tokens[0].equals("C")) {
			return;
		}
		if (tokens[0].equals("E")) {
			String type = tokens[2].substring(1, tokens[2].length()-1);
			if (type.equals("Parameter")) return;

			if (tokens[1].length() < idBegSkip) {
				System.err.println("Not processing: " + line);
				return;
			}
			String entId = tokens[1].substring(idBegSkip);
			
			Resource entRes = getRes(entId);
			add(getParentRes(entId), RSECore.contains, entRes);
			
			if (type.startsWith("Public"))
				add(entRes, RJCore.access, RJCore.publicAccess);
			else if (type.startsWith("Protected"))
				add(entRes, RJCore.access, RJCore.protectedAccess);
			else if (type.startsWith("Private"))
				add(entRes, RJCore.access, RJCore.privateAccess);

			if (type.startsWith("TABLE"))
				add(entRes, DepTranslator.tableProp, RSECore.trueStatement);
			
			//if (type.endsWith("Class")) {
			//	//add(entRes, repo.rdfType, RJCore.classType);
			//} else if (type.endsWith("Method")) {
			//	//add(entRes, repo.rdfType, RJCore.methodType);
			//} else if (type.endsWith("Variable")) {
			//	//add(entRes, repo.rdfType, RJCore.methodType);
			//}
			
			return;
		}
		if (tokens[0].equals("R")) {
			if (tokens[1].length() < idBegSkip || tokens[3].length() < idBegSkip) {
				System.err.println("Not processing: " + line);
				return;
			}
			String srcId = tokens[1].substring(idBegSkip);
			String type = tokens[2].substring(1, tokens[2].length()-1);
			String dstId = tokens[3].substring(idBegSkip);

			String shortTypeName;
			if (type.startsWith("Java ")) {
				int typeBeg = "Java ".length();
				int typeEnd = type.indexOf(' ', typeBeg);
				if (typeEnd == -1) typeEnd = type.length();
				shortTypeName = type.substring(typeBeg, typeEnd);
			} else {
				shortTypeName = type;
			}

			if (type.contains("Implicit") ||
				type.endsWith("Throw")) {
				// we ignore these - for now (atleast it seems like the jdt builder was doing this)
			} else if (type.endsWith("Call")) {
				addRel(srcId, RJCore.calls, dstId);
			} else if (refTypes.contains(shortTypeName)) {
				addRel(srcId, RJCore.refType, dstId);
			} else if (shortTypeName.equals("Extend") || shortTypeName.equals("Implement")) {
				addRel(srcId, RJCore.inherits, dstId);
			} else {
				// log an error and just 'deal' with them
				addRel(srcId, RJCore.refType, dstId);
				System.err.println("Processed: " + type + " as reference: " + line);
			}
			return;
		}
	}

	@Override
	public void close() {
		super.close();
		lhProcessor.processProj(null, new NullProgressMonitor());
		dacss.finishSummarizing();
	}


}