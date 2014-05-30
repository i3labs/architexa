package com.architexa.store;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;

/**
 * Mapping a package to the packages it contains. Previously represented
 * in the rdf repo with a statement (pckgA, RJCore.pckgDirContains, pckgB)
 */
public class PckgDirRepo extends RSERepo {   
	static final Logger logger = ReloStorePlugin.getLogger(PckgDirRepo.class);

	private static boolean useRdfRepo = false;
	private final ReloRdfRepository repo;			// used only when the above flag is true
	private final URI containsPred;

	private static final String fileName = "PckgDirContainsCache.txt";

	private LinkedHashMap<Resource, ArrayList<Resource>> pckgDirContainsMap = null;

    private static Map<IPath, PckgDirRepo> activePDR = new HashMap<IPath, PckgDirRepo>();

	/**
	 * Used when trying to get the already open PDR. Usually you want to get
	 * this from the RootArt, but this method is used by the builder to see what
	 * is open.
	 */
	public static PckgDirRepo getPDR(ReloRdfRepository _repo, URI _containsPred) {
		return getPDR(_repo, _repo.getPath(), _containsPred);
	}
	public static PckgDirRepo getPDR(ReloRdfRepository _repo, IPath _path, URI _containsPred) {
		if (_path == null) _path = Path.EMPTY;
		if (!activePDR.containsKey(_path)) {
			activePDR.put(_path, new PckgDirRepo(_repo, _path, _containsPred));
		}
		return activePDR.get(_path);
	}
	public static void resetPDR() {
		activePDR.clear();
	}
	
	/**
	 * @param containsPred
	 *            is really hardcoded to RJCore.pckgDirContains - but for now we
	 *            are 'injecting' the value in
	 */
	private PckgDirRepo(ReloRdfRepository _repo, IPath _path, URI containsPred) {
		super(_path);
		this.repo = _repo;
		this.containsPred = containsPred;
	}
	
	
	public void init(boolean populateMapFromFile) {
		if (pckgDirContainsMap == null) {
			pckgDirContainsMap = new LinkedHashMap<Resource, ArrayList<Resource>>();
			if(populateMapFromFile) readFile(); // Initialize map from stored file
			// Save map upon eclipse shutdown
			Runtime.getRuntime().addShutdownHook(new Thread() {
				@Override
				public void run() {
					writeFile(); 
				}
			});
		}
	}

	public void addContains(Resource subj, Value obj) {
		if (useRdfRepo) repo.addStatement(subj, containsPred, obj);
		else {
			if(pckgDirContainsMap==null) init(true);
			addToMap(subj, (Resource)obj);
		}
	}

	public List<Resource> queryArtList(Resource elementRes, boolean relIsFwd, URI relRes) {
		if(useRdfRepo) return queryRdfRepo(repo, elementRes, relIsFwd, relRes);
		else {
			if(pckgDirContainsMap==null) init(true);
			return queryMap(elementRes, relIsFwd);
		}
	}

	// XXX - subj param is always null!!
	public Resource getStatementSubj(Resource subj, URI pred, Value obj) {
		if(useRdfRepo) return (Resource) repo.getStatement(subj, pred, obj).getSubject();
		else {
			if(pckgDirContainsMap==null) init(true);
			return getParentFromMap((Resource)obj);
		}
	}

	// when multiple parents exist (package folder in multiple projs) we may
	// need to find all of them to get to correct one
	public List<Resource> getParents(Resource subj, URI pred, Value obj) {
		if(pckgDirContainsMap==null) init(true);
		return getParentsFromMap((Resource)obj);
	}
	
	private void addToMap(Resource key, Resource value) {
		ArrayList<Resource> values = pckgDirContainsMap.get(key);
		if(values==null) values = new ArrayList<Resource>();
		if(!values.contains(value)) values.add(value);

		// removing before putting so that this pair will now 
		// be at the end of the map as the most recently added
		pckgDirContainsMap.remove(key);
		pckgDirContainsMap.put(key, values);
	}

	private List<Resource> queryRdfRepo(
			ReloRdfRepository repo, Resource elementRes, boolean relIsFwd, URI relRes) {
		List<Resource> retValAsResource = new LinkedList<Resource> ();
		if (relIsFwd)
			repo.getResourcesFor(retValAsResource, elementRes, relRes, null);
		else
			repo.getResourcesFor(retValAsResource, null, relRes, elementRes);
		return retValAsResource;
	}

	private List<Resource> queryMap(Resource elementRes, boolean relisFwd) {
		if(relisFwd) {
			if(pckgDirContainsMap.containsKey(elementRes))
				return new ArrayList<Resource>(pckgDirContainsMap.get(elementRes));
			return new ArrayList<Resource>();
		} else {
			List<Resource> containers = new ArrayList<Resource>();
			for(Resource key : pckgDirContainsMap.keySet()) {
				List<Resource> values = pckgDirContainsMap.get(key);
				if(values.contains(elementRes)) containers.add(key);
			}
			return containers;
		}
	}

	private Resource getParentFromMap(Resource containee) {
		// ReloRdfRepository.getStatement(subj,pred,obj) returns the 
		// most recently added statement. This means we should return the  
		// mapping most recently added, so we iterate backward through
		// the map. (Remember, we've use a LinkedHashMap so that 
		// insertion order is preserved).
		List<Resource> keys = new ArrayList<Resource>(pckgDirContainsMap.keySet());
		for(int j=keys.size()-1; j>=0; j--) {
			Resource key = keys.get(j);
			List<Resource> values = pckgDirContainsMap.get(key);
			if(values.contains(containee)) return key;
		}
		return null;
	}

	private List<Resource> getParentsFromMap(Resource containee) {
		// ReloRdfRepository.getStatement(subj,pred,obj) returns the 
		// most recently added statement. This means we should return the  
		// mapping most recently added, so we iterate backward through
		// the map. (Remember, we've use a LinkedHashMap so that 
		// insertion order is preserved).
		List<Resource> parentsList = new ArrayList<Resource>();
		List<Resource> keys = new ArrayList<Resource>(pckgDirContainsMap.keySet());
		for(int j=keys.size()-1; j>=0; j--) {
			Resource key = keys.get(j);
			List<Resource> values = pckgDirContainsMap.get(key);
			if(values.contains(containee)) 
				parentsList.add(key);
		}
		return parentsList;
	}
	
	
	/*
	 * Converting each key from a Resource to a String, 
	 * converting each value List to a semicolon delineated String,  
	 * and writing each line as "keyString->valueString"
	 */
	private void writeFile() {
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter(getLocation()+fileName));
			for(Resource keyRes : pckgDirContainsMap.keySet()) {

				String valuesAsString = "";
				for(Resource res : pckgDirContainsMap.get(keyRes)) 
					valuesAsString = valuesAsString+res.toString().trim()+";";

				out.write(keyRes.toString()+"->"+valuesAsString);
				out.newLine();
			}
			out.close();
		} catch (Exception e) {
			logger.error("Unexpected exception while writing " +
					"pckgDirContains cache to persistent file: " + e.getMessage());
		}
	}

	/* 
	 * File has the format "keyString->valueString", where valueString
	 * is a semicolon delineated String representing the value List. 
	 * Converting it back to a Resource->List<Resource> map. 
	 */
	private void readFile() {
		pckgDirContainsMap = new LinkedHashMap<Resource, ArrayList<Resource>>();

		try {
			File file = new File(getLocation()+fileName);
			if(!file.exists()) return;

			BufferedReader in = new BufferedReader(new FileReader(getLocation()+fileName));
			String line = in.readLine();

			// Last line is a blank line, which we don't care about
			while(line != null && !"".equals(line.trim())) {
				String[] splitLine = line.split("->");
				if(splitLine.length!=2) {
					logger.error("Unable to read pckgDirContains cache file.");
					return;
				}

				String keyString = splitLine[0];
				Resource keyRes = repo.createURI(keyString);

				String valueString = splitLine[1];
				ArrayList<Resource> valueList = new ArrayList<Resource>();
				for(String s : valueString.split(";")) {
					valueList.add(repo.createURI(s));
				}

				pckgDirContainsMap.put(keyRes, valueList);

				line = in.readLine();
			}

			in.close();
		} catch (Exception e){
			logger.error("Unexpected exception while populating " +
					"pckgDirContains cache from persistent file: " + e.getMessage());
		}
	}

}
