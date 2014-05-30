package com.architexa.diagrams.jdt.builder.asm;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.IPath;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.sesame.sail.StatementIterator;

import com.architexa.collab.UIUtils;
import com.architexa.diagrams.jdt.RJCore;
import com.architexa.store.RSERepo;
import com.architexa.store.ReloRdfRepository;

/**
 * Stores to file all dependencies stored with their strengths grouped at a package to package level for a target
 * relations (likely RJCore.containmentBasedRefType)
 */
public class DepRepoReplacement extends RSERepo {
	
    // store both fwd and backwards connection strengths so they are easier to query
    public class DepStrength {
		public int fwd;
    	public int bck;
    	public DepStrength(int fwd, int bck) {
    		this.fwd=fwd;
    		this.bck=bck;
    	}
    }
    

	private static final String relStrengthFileName = "relStrengthCache.txt";
	private static final String relMapFileName = "relMapCache.txt";
	private static final String containmentCacheFileName = "containmentCache.txt";
	
	// src --[strength]--> dst
    
    // public Map<Resource, List<Map<RelDst, Integer>>> cachedRelStrengthMap = null;::
	// was - map of packages to list of destination packages with their strengths
	// now - map of src-dst package strings to strengths (to simplify adding/removing/writing/reading queries) 
    // 5-20,  this can probably be removed now...Unless it is being used by the layering algo
	private Map<String, Integer> cachedRelStrengthMap = null;
    
    // tracking relation statements added (not all rels have a strength):: ?map of packages to other packages?
    public Map<Resource, Map<Resource, DepStrength>> cachedContainmentBasedRefTypeRelMap = null;
    
    // track containment statements:: map of packages (and ?package frag?) to the number of classes it contains (?included nested classes?)
    private Map<Resource, String> cachedContainmentMap = null;

    
    private ReloRdfRepository rdfRepo; 
	
	private static boolean useRdfRepo = false;
	
	public DepRepoReplacement(ReloRdfRepository _rdfRepo, IPath _path) {
		super(_path);
		this.rdfRepo = _rdfRepo;
		
		this.cachedRelStrengthMap = new HashMap<String, Integer>();
		this.cachedContainmentBasedRefTypeRelMap = new HashMap<Resource, Map<Resource, DepStrength>>();
		this.cachedContainmentMap = new HashMap<Resource, String>();
	}

	public Statement createStmt(Resource subj, URI pred, Resource obj) {
		return rdfRepo.createStmt(subj, pred, obj);
	}

	// add src -vrel - dest  addCachedRel
	// We are now working under the assumption that all preds/vrels are containmentBased types
	public void addRelStatement(Resource subj, URI pred, Resource obj) {
		if (useRdfRepo) {
			rdfRepo.addStatement(subj, pred, obj);
		} else {
			if (RJCore.containmentBasedRefType.equals(pred)) {
				Map<Resource,DepRepoReplacement.DepStrength> destMap = cachedContainmentBasedRefTypeRelMap.get(subj);
				if (destMap == null) {
					destMap =  new HashMap<Resource,DepRepoReplacement.DepStrength>();
				}
				if (destMap.get(obj) == null)
					destMap.put(obj, new DepStrength(0,0));
				cachedContainmentBasedRefTypeRelMap.put(subj, destMap);
				
				Map<Resource,DepRepoReplacement.DepStrength> srcMap = cachedContainmentBasedRefTypeRelMap.get(obj);
				if (srcMap == null) {
					srcMap =  new HashMap<Resource,DepRepoReplacement.DepStrength>();
				}
				if (srcMap.get(subj) == null)
					srcMap.put(subj, new DepStrength(0,0));
				cachedContainmentBasedRefTypeRelMap.put(obj, srcMap);
			}
		}
	}
	
	// (sub-res-tgt) -> stngth -> int
	// add cachedRelStrength
	public void addRelStrengthStatement(Statement subj, URI pred, String obj) {
		if (useRdfRepo)
			rdfRepo.addStatement(subj, pred, obj);
		else {
			cachedRelStrengthMap.put(getSubjStr(subj), Integer.parseInt(obj));
			Resource src = subj.getSubject();
			Value dest = subj.getObject();
			
			Map<Resource, DepStrength> destMap = cachedContainmentBasedRefTypeRelMap.get(src);	
			if (destMap == null) {
				destMap =  new HashMap<Resource,DepRepoReplacement.DepStrength>();
			}
			DepStrength destStr = destMap.get(dest);
			if (destStr == null) {
				destStr =  new DepStrength(0, 0);
			}
			destStr.fwd = Integer.parseInt(obj);

			Map<Resource, DepStrength> srcMap = cachedContainmentBasedRefTypeRelMap.get(dest);
			if (srcMap == null) {
				srcMap =  new HashMap<Resource,DepRepoReplacement.DepStrength>();
			}
			DepStrength srcStr = srcMap.get(src);
			if (srcStr == null) {
				srcStr =  new DepStrength(0, 0);
			}
			srcStr.bck = Integer.parseInt(obj); 
		}
			
	}
	
	// add containmentBasedClassesInside statement
	public void addContainmentStatement(Resource subj, URI pred, String obj) {
		if (useRdfRepo)
			rdfRepo.addStatement(subj, pred, obj);
		else {
			cachedContainmentMap.put(subj, obj);
		}
	}

	
	// get the int strength of the subject statement from cachedRelStrengthMap
	public Integer getStrengthOfRelStatement(Statement subj, URI pred, Value obj) {
		if (useRdfRepo) {
			Statement statement = rdfRepo.getStatement(subj, pred, obj);
			if (statement!=null && statement.getObject()!=null)
				return Integer.parseInt(statement.getObject().toString());
			return -1;
		} else {
			if (cachedRelStrengthMap.containsKey(getSubjStr(subj)))
				return cachedRelStrengthMap.get(getSubjStr(subj));
			return -1;
		}
	}

	// get a list of all the dest/tgts of this particular subject/pred combo from cachedRelStrengthMap
	public List<Resource> getFwdStatements(Resource subj, URI pred, Value obj) {
		List<Resource> retList = new ArrayList<Resource>();
		if (useRdfRepo) {
			StatementIterator si = rdfRepo.getStatements(subj, pred, obj);
			while (si.hasNext()) {
				retList.add((Resource) si.next().getObject());
			}
			return retList;
		} else {
			if (RJCore.containmentBasedRefType.equals(pred)) {
				Map<Resource, DepStrength> destMap = cachedContainmentBasedRefTypeRelMap.get(subj);
				if (destMap == null) {
					destMap =  new HashMap<Resource,DepRepoReplacement.DepStrength>();
				}
				Set<Resource> list = new HashSet<Resource>();
				for(Resource r : destMap.keySet()) {
					if (destMap.get(r).fwd > 0)
						list.add(r);
				}
				return new ArrayList<Resource>(list);
			}
			return retList;
		}
	}
	// get a list of all the sources of this particular pred/object combo from cachedRelStrengthMap
	public List<Resource> getRevStatements(Resource subj, URI pred, Value obj) { 
		List<Resource> retList = new ArrayList<Resource>();
		if (useRdfRepo) {
			StatementIterator si = rdfRepo.getStatements(subj, pred, obj);
			while (si.hasNext()) {
				retList.add((Resource) si.next().getObject());
			}
			return retList;
		} else {
			if (RJCore.containmentBasedRefType.equals(pred)) {
				Map<Resource, DepStrength> destMap = cachedContainmentBasedRefTypeRelMap.get(obj);
				if (destMap == null) {
					destMap =  new HashMap<Resource,DepRepoReplacement.DepStrength>();
				}
				Set<Resource> list = new HashSet<Resource>();
				for(Resource r : destMap.keySet()) {
					if (destMap.get(r).bck > 0)
						list.add(r);
				}
				return new ArrayList<Resource>(list);
			}
		}
		return retList;
	}
	
	// get containmentBasedClassesInside
	public String getContainedClassesStatement(Resource subj, URI pred, Value obj) {
		if (useRdfRepo) {
			Statement statement = rdfRepo.getStatement(subj, pred, obj);
			if (statement!=null && statement.getObject()!=null)
				return statement.getObject().toString();
			return "-1";
		} else {
			return cachedContainmentMap.get(subj);
		}
	}

	// remove containmentBasedClassesInside
	public void removeStatements(Resource subj, URI pred, Value obj) {
		if (useRdfRepo) {
			rdfRepo.removeStatements(subj, pred, obj);
		} 
		else {
			if (cachedContainmentMap.containsKey(subj)) {
				cachedContainmentMap.remove(subj);
			}
		}
	}

	// remove strength statements for this particular subject
	public void removeStatements(Statement subj, URI pred, Value obj) {
		if (useRdfRepo)
			rdfRepo.removeStatements(subj, pred, obj);
		else 
			cachedRelStrengthMap.remove(getSubjStr(subj));
	}
	
	public boolean hasStatement(Resource subj, URI pred, Resource obj) {
		if (useRdfRepo) {
			return rdfRepo.hasStatement(subj, pred, obj);
		} else {
			if (RJCore.containmentBasedRefType.equals(pred)) {
				return cachedContainmentBasedRefTypeRelMap.containsKey(subj) 
						&& cachedContainmentBasedRefTypeRelMap.get(subj).keySet().contains(obj);
			}
			return false;
		}
	}
	
	// READING AND WRITING
	
	public void writeToFile() {
		if (useRdfRepo) return;
		try{
		    FileWriter fstream = new FileWriter(getLocation() + containmentCacheFileName);
		    BufferedWriter out = new BufferedWriter(fstream);
		    writeContainmentCache(out);
		    out.close();
		    
		    fstream = new FileWriter(getLocation() + relMapFileName);
		    out = new BufferedWriter(fstream);
		    writeRelMapCache(out);
		    out.close();

		    fstream = new FileWriter(getLocation() + relStrengthFileName);
		    out = new BufferedWriter(fstream);
		    writeRelStrengthCache(out);
		    out.close();
	    } catch (Exception e){//Catch exception if any
		      System.err.println("Error: " + e.getMessage());
	    }
	}
	public void readFile() {
		if (useRdfRepo) return;
		try{
			File containmentFile = new File(getLocation() + containmentCacheFileName);
			if (containmentFile.exists()) {
			    FileReader fstream = new FileReader(getLocation() + containmentCacheFileName);
			    BufferedReader in = new BufferedReader(fstream);
			    if (!readContainmentCache(in)) return;
			    in.close();
			}

			File relMapFile = new File(getLocation() + relMapFileName);
			if (relMapFile .exists()) {
				FileReader fstream = new FileReader(getLocation() + relMapFileName);
				BufferedReader in = new BufferedReader(fstream);
				cachedContainmentBasedRefTypeRelMap.clear();
				if (!readRelMapCache(in)) return;
			    in.close();
			}
			
			File relStrengthFile = new File(getLocation() + relStrengthFileName);
			if (relStrengthFile .exists()) {
			    FileReader fstream = new FileReader(getLocation() + relStrengthFileName);
			    BufferedReader in = new BufferedReader(fstream);
			    if (!readRelStrengthCache(in)) return;
			    in.close();
			}		    		    
	    } catch (Exception e){//Catch exception if any
		      System.err.println("Error: " + e.getMessage());
		      e.printStackTrace();
	    }
//	    System.err.println("SZ: cachedContainmentMap: " + cachedContainmentMap.size());
//	    System.err.println("SZ: cachedContainmentBasedRefTypeRelMap: " + cachedContainmentBasedRefTypeRelMap.size());
//	    System.err.println("SZ: cachedRelStrengthMap: " + cachedRelStrengthMap.size());
	}

	private void writeRelStrengthCache(BufferedWriter out) throws IOException {
		for (Map.Entry<String, Integer> entry : cachedRelStrengthMap.entrySet()) {
			out.write(entry.getKey().toString());
	    	out.write("~");
	    	out.write(entry.getValue().toString());
	    	out.write(";");
		}
	}
	
	private boolean readRelStrengthCache(BufferedReader in) throws IOException {
		String relStrengthMapString = in.readLine();
		
		// file was corrupt - Architexa needs to be reBuilt
		if (relStrengthMapString == null || relStrengthMapString.equals("")) {
			UIUtils.openErrorPromptDialog("Error Loading Layered Diagram Dependencies", "Please Rebuild your Architexa Index" +
			"\nby going to 'Architexa->Rebuild Complete Index'");
			return false;
		}
		
		for (String entry : relStrengthMapString.split(";")) {
			cachedRelStrengthMap.put(entry.split("~")[0], Integer.parseInt(entry.split("~")[1]));
		}
		return true;
	}
	//TODO cut size in half by storing fwd/bck correctly
	private void writeRelMapCache(BufferedWriter out) throws IOException {
		for (Resource key : cachedContainmentBasedRefTypeRelMap.keySet()) {
			out.write(key.toString());
			out.write("~~~");
			Map<Resource, DepStrength> mapDestResToStr = cachedContainmentBasedRefTypeRelMap.get(key);
			for (Resource destRes : mapDestResToStr.keySet()) {
				out.write(destRes.toString());
				out.write("->");
		    	DepStrength str = mapDestResToStr.get(destRes);
		    	if (str ==null) {
		    		out.write("0");
		    		out.write("__");
		    		out.write("0");
		    	} else {
		    		out.write(Integer.toString(str.fwd));
		    		out.write("__");
		    		out.write(Integer.toString(str.bck));
		    	}
		    	out.write(";");
			}
			out.write("@@@");
		}
	}
	
	private boolean readRelMapCache(BufferedReader in) throws IOException {
		String relMapString = in.readLine();
		// file was corrupt - Architexa needs to be reBuilt
		if (relMapString == null || relMapString.equals("")) {
			UIUtils.openErrorPromptDialog("Error Loading Layered Diagram Dependencies", "Please Rebuild your Architexa Index" +
			"\nby going to 'Architexa->Rebuild Complete Index'");
			return false;
		}
		for (String entry : relMapString.split("@@@")) {
			String resStr = entry.split("~~~")[0];
			String listStr = entry.split("~~~")[1];
			for (String dstAndStr : listStr.split(";")){
					URI subj = rdfRepo.createURI(resStr);

					Map<Resource, DepStrength> destMap= cachedContainmentBasedRefTypeRelMap.get(subj);
					if (destMap == null) {
						destMap =  new HashMap<Resource, DepStrength>();
					}
					
					String destRes = dstAndStr.split("->")[0];
					String strString = dstAndStr.split("->")[1];
					
					DepStrength depStr = new DepStrength(Integer.parseInt(strString.split("__")[0]), Integer.parseInt(strString.split("__")[1]));
					
					destMap.put( rdfRepo.createURI(destRes), depStr);
					cachedContainmentBasedRefTypeRelMap.put(subj, destMap);
			}
		}
		return true;
	}

	private void writeContainmentCache(BufferedWriter out) throws IOException {

		for (Map.Entry<Resource, String> entry : cachedContainmentMap
				.entrySet()) {
			out.write(entry.getKey().toString());
			out.write("~~~");
			out.write(entry.getValue());
			out.write("@@@");
		}
	}
	
	private boolean readContainmentCache(BufferedReader in) throws IOException {
		String containmentMapString = in.readLine();
		// file was corrupt - Architexa needs to be reBuilt
		if (containmentMapString == null || containmentMapString.equals("")) {
			UIUtils.openErrorPromptDialog("Error Loading Layered Diagram Dependencies", "Please Rebuild your Architexa Index" +
			"\nby going to 'Architexa->Rebuild Complete Index'");
			return false;
		}
		for (String entry : containmentMapString.split("@@@")) {
			String resStr = entry.split("~~~")[0];
			String listStr = entry.split("~~~")[1];
			cachedContainmentMap.put(rdfRepo.createURI(resStr), listStr);
		}
		return true;
	}

	
	// UTILS
	private String getSubjStr(Statement subj) {
		return subj.getSubject().toString() +"??" + subj.getObject().toString();
	}
}
