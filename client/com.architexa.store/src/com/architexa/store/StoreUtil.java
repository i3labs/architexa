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
 * Created on Jan 7, 2005
 *
 */
package com.architexa.store;

import java.io.File;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.Predicate;
import org.apache.log4j.Logger;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.openrdf.model.BNode;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.GraphImpl;
import org.openrdf.rio.Parser;
import org.openrdf.rio.RdfDocumentWriter;
import org.openrdf.rio.turtle.TurtleParser;
import org.openrdf.rio.turtle.TurtleWriter;
import org.openrdf.sesame.repository.local.LocalRepository;
import org.openrdf.sesame.sail.SailUpdateException;
import org.openrdf.sesame.sail.StatementIterator;

/**
 * @author vineet
 *
 */
public class StoreUtil {
    static final Logger logger = ReloStorePlugin.getLogger(StoreUtil.class);
    
    //locate here for now, keep a list of RSEEditors
    private static ArrayList<IEditorPart> theDiagrams = new ArrayList<IEditorPart>();
    public static void trackDiagram(IEditorPart toKeep){
    	theDiagrams.add(toKeep);
    }
    public static void removeDiagram(IEditorPart toLose){
    	theDiagrams.remove(toLose);
    }
    public static boolean hasDiagrams(){
    	return !theDiagrams.isEmpty();
    }
    //get the diagrams if needed
    public static void killDiagrams(IWorkbenchPage page){
    	while(!theDiagrams.isEmpty()){
    		IEditorPart iep =  theDiagrams.get(0);
    		page.closeEditor(iep, false);
    		removeDiagram(iep);
    	}
    }

    private static ValueFactory valFactory = (new GraphImpl()).getValueFactory();
    public static final URI createMemURI(String str) {
    	return valFactory.createURI(str);
    }
    public static final Literal createMemLiteral(String str) {
    	return valFactory.createLiteral(str);
    }
    public static final BNode createBNode() {
    	return valFactory.createBNode();
    }
    
    public static Parser getRDFParser(ReloRdfRepository rdfRepo) {
        return new TurtleParser(rdfRepo.sailRepo.getValueFactory());
    }
    public static RdfDocumentWriter getRDFWriter(OutputStream out) {
        return new TurtleWriter(out);
    }

    /**
     * @param superTypesModel
     * @param elementRes
     * @param inherits
     */
    public static void getObjectsRecursively(ReloRdfRepository rdfRepo, Resource subj, URI pred) {
        StatementIterator si = rdfRepo.getStatements(subj, pred, null);
	    while (si.hasNext()) {
	        Resource objRes = (Resource) si.next().getObject();
	        rdfRepo.addStatement(subj, pred, objRes);
			getObjectsRecursively(rdfRepo, objRes, pred);
	    }
	    si.close();
    }

	public static Predicate filterSubjectResPred(final ReloRdfRepository repo, final URI p, final Resource o) {
        return new Predicate() {
            public boolean evaluate(Object arg0) {
                return repo.hasStatement((Resource)arg0, p, o);
            }};
    }


    public static Resource getResource(ReloRdfRepository repo, Resource res) {
        if (res instanceof BNode)
            return repo.createBNode(((BNode)res).getID());
        else
            return repo.createURI(((URI)res).getNamespace(), ((URI)res).getLocalName());
    }

    
    private static IExtensionRegistry	registry	= Platform.getExtensionRegistry();

	private static void loadEclipseClasses(List<Object> loadedClasses, String extPt, String confName, String attrName) {
		// <bg pattern="stripes"/>
		// corresponds to a configuration element named "bg" with an attribute named
		// "pattern" with attribute value "stripes".
		
		// for us we want the attr value
        Map<Object, String> runBeforeToIDMap = new HashMap<Object, String>();
        Map<Object, String> runAfterToIDMap = new HashMap<Object, String>();
        Map<Object, String> classToIDMap = new HashMap<Object, String> ();

        IExtensionPoint extensionPoint = registry.getExtensionPoint(extPt);
		if (extensionPoint == null) {
			System.err.println("Failed to get extension point: " + extPt);
			return;
		}

		IConfigurationElement[] confEls = extensionPoint.getConfigurationElements();

		for (int i = 0; i < confEls.length; i++) {
			if (confEls[i].getName().equals(confName)) {
			    try {
			    	Object loadedClass = confEls[i].createExecutableExtension(attrName);
                    loadedClasses.add(loadedClass);
    				if (confEls[i].getDeclaringExtension().getUniqueIdentifier() != null)
    					classToIDMap.put(loadedClass, confEls[i].getDeclaringExtension().getUniqueIdentifier());
                    if (confEls[i].getAttribute("runBefore") != null)
                    	runBeforeToIDMap.put(loadedClass, confEls[i].getAttribute("runBefore"));
                    if (confEls[i].getAttribute("runAfter") != null)
                    	runAfterToIDMap.put(loadedClass, confEls[i].getAttribute("runAfter"));
                } catch (CoreException e) {
                	logger.error("Failed to load: " + confEls[i].getAttribute(attrName), e );
                }
			}
		}
		
		boolean done = false;
		mainReorderLoop: do {
			
			// make a copy so that we can change it inside the loop
			for (Object outClass : new ArrayList<Object> (loadedClasses)) {
				// process runBefore
				if (runBeforeToIDMap.containsKey(outClass)) {
					String beforeOutClassID = runBeforeToIDMap.get(outClass);
					int outNdx = loadedClasses.indexOf(outClass);
					for (int inNdx = 0; inNdx < outNdx; inNdx++) {
						Object inClass = loadedClasses.get(inNdx);
						if (classToIDMap.containsKey(inClass) && classToIDMap.get(inClass).equals(beforeOutClassID)) {
							loadedClasses.remove(outClass);
							loadedClasses.add(inNdx, outClass);
							continue mainReorderLoop;
						}
					}
				}

				// process runAfter
				if (runAfterToIDMap.containsKey(outClass)) {
					String afterOutClassID = runAfterToIDMap.get(outClass);
					int outNdx = loadedClasses.indexOf(outClass);
					if (outNdx+1<loadedClasses.size()) {
						for (int inNdx = outNdx + 1; inNdx < loadedClasses.size(); inNdx++) {
							Object inClass = loadedClasses.get(inNdx);
							if (classToIDMap.containsKey(inClass) && classToIDMap.get(inClass).equals(afterOutClassID)) {
								loadedClasses.remove(outClass);
								// we really want to add to inNdx+1 but because we
								// removed outClass, we just need to add to inNdx
								loadedClasses.add(inNdx, outClass);
								continue mainReorderLoop;
							}
						}
					}
				}
			}
			
			done = true;
		} while (!done);

		for (Object printClass : loadedClasses) {
			logger.info("Loaded class type: " + printClass.getClass());
		}
	}
	public static void loadEclipseClasses(List<Object> loadedClasses, String extPt, String property) {
		loadEclipseClasses(loadedClasses, extPt, property, "class");
	}
	public static void loadEclipseClasses(List<Object> loadedClasses, String extPt) {
		loadEclipseClasses(loadedClasses, extPt, "class", "name");
	}

	@SuppressWarnings("unchecked")
	public static <T extends Object> void loadEclipseClasses(List<T> loadedClasses, Class<?> loadType, String extPt) {
        List<Object> untypedLoadedClasses = new ArrayList<Object> (10);
		loadEclipseClasses(untypedLoadedClasses, extPt);
		for (Object untypedLoadedClass : untypedLoadedClasses) {
			if (loadType.isInstance(untypedLoadedClass))
				loadedClasses.add((T) untypedLoadedClass);
			else
            	logger.error("Expecting type: " + loadType + " got: " + untypedLoadedClass.getClass(), new Exception());
		}
	}

	private static final String pluginImplementationSep = "/";

	// these classes retreived to load by loadClass below
    public static String getClassLoc(String pluginId, Class<?> clazz) {
        return getClassLoc(pluginId, clazz.getName());
    }

    private static String getClassLoc(String pluginId, String className) {
        return pluginId + pluginImplementationSep + className;
    }

	//public static String findClassLoc(ReloRdfRepository repo, Resource subj, URI locProperty) {
	//    Value classLoc = repo.getStatement(subj, locProperty, null).getObject();
	//    if (classLoc == null) {
	//    	return null;
	//    }
	//    return classLoc.toString();
	//}
    
    // below are now only used when writing to a file
    public static Object loadClass(ReloRdfRepository repo, Resource subj, URI locProperty) {
        Value classLoc = repo.getStatement(subj, locProperty, null).getObject();
        if (classLoc == null) {
            logger.warn("Trying to load class with no location: " + subj + " --> " + locProperty, new Exception());
            return null;
        }
        return loadClass(classLoc.toString());
    }
    
    public static Object loadClass(String classLocStr) {    	
        int sepIndex = classLocStr.indexOf(pluginImplementationSep);
        if (sepIndex == -1) {
            logger.warn("Trying to load class with no seperator: " + classLocStr, new Exception());
            return null;
        }

        String contPlugin = classLocStr.substring(0,sepIndex);
        String contClass = classLocStr.substring(sepIndex+1);

        try {
	        return Platform.getBundle(contPlugin).loadClass(contClass).newInstance();
        } catch (InstantiationException e) {
            logger.error("Unexpected error loading class: " + classLocStr, e);
        } catch (IllegalAccessException e) {
            logger.error("Unexpected error loading class: " + classLocStr, e);
        } catch (ClassNotFoundException e) {
            logger.error("Unexpected error loading class: " + classLocStr, e);
        }
        return null;
    }

    // TODO: more work needs to be done on the plugin.xml/rdf-store duality
    // removed below as they were not being used (and were a dependency on jena)
    //public static void loadStoreClasses(Collection retVal, Model rdfModel, Resource subj, Property locProperty)
	//public static List loadClasses(Model rdfModel, Resource subj, Property locProperty)

    

    
    public static ReloRdfRepository getMemRepository() {
    	System.err.print("[M]");
        return new ReloRdfRepository();
    }
	public static ReloRdfRepository getMemRepository(IPath wkspcPath) {
    	return new ReloRdfRepository(wkspcPath);
	}
    public static ReloRdfRepository getDefaultStoreRepository() {
    	return getStoreRepository(null);
    }
    public static ReloRdfRepository getStoreRepository(IPath path) {
    	return ReloRdfRepository.getRepo(path);
    }

    public static void deleteDefaultStoreRepository() {
    	ReloRdfRepository defaultRepo = ReloRdfRepository.returnDefaultRepo();
        if (defaultRepo != null) defaultRepo.shutdown();
        deleteDB(defaultRepo, ReloRdfRepository.defaultDBName);
        ReloRdfRepository.clearDefaultRepo();
    }

    public static void purgeDefaultRepository() {
    	purgeRepository(getDefaultStoreRepository());
    }
    public static void purgeRepository(ReloRdfRepository repo) {
        try {
            repo.sailRepo.startTransaction();
            repo.sailRepo.clearRepository();
            repo.sailRepo.commitTransaction();
		} catch (SailUpdateException e) {
			logger.error("Unexpected exception", e);
		}
    }
    public static void shutdown(LocalRepository localRepo) {
    	localRepo.shutDown();
    }
	public static void shutdownDefaultRepository() {
    	ReloRdfRepository defaultRepo = ReloRdfRepository.returnDefaultRepo();
		if (defaultRepo != null) defaultRepo.shutdown();
        ReloRdfRepository.clearDefaultRepo();
	}
	public static void shutdownRepositories() {
		ReloRdfRepository.shutdownRepositories();
	}
    
    private static void deleteDB(ReloRdfRepository repo, String dbName)  {
        IPath dbPath = repo.getLocation().addTrailingSeparator().append(dbName);
        deleteFile(dbPath.toFile());
        //logger.info("Deleted file: " + dbPath);
    }
    private static void deleteFile(File file) {
    	if (file.isDirectory()) {
    		File[] children = file.listFiles();
    		for (File child : children) {
				deleteFile(child);
			}
    	}
    	file.delete();
	}
    
}
