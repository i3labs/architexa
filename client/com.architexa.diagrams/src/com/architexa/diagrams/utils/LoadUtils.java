package com.architexa.diagrams.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.part.WorkbenchPart;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.rio.RdfDocumentWriter;
import org.openrdf.sesame.sail.StatementIterator;
import org.openrdf.sesame.sailimpl.memory.LiteralNode;
import org.openrdf.vocabulary.RDF;

import com.architexa.diagrams.Activator;
import com.architexa.diagrams.RSECore;
import com.architexa.diagrams.ui.RSEEditor;
import com.architexa.diagrams.ui.RSEShareableDiagramEditorInput;
import com.architexa.store.ReloRdfRepository;
import com.architexa.store.StoreUtil;

/**
 * @author Elizabeth L. Murnane
 */
// VS: @Liz, would another name be more appropriate?
// VS: Don't like the locations for this class
public class LoadUtils {
	
	private static final Logger logger = Activator.getLogger(LoadUtils.class);
	
	public static boolean outdatedCheckingOn = false;
	
	/**
	 * If the repository does not contain any statement with the given 
	 * subject, method returns true 
	 *  
	 * @return true if the given IResource is outdated, false otherwise
	 *
	 */
	public static boolean isOutdated(ReloRdfRepository repo, Resource subj, IResource parentRDFFileResource) {
		return isOutdated(repo, subj, null, null, parentRDFFileResource);
	}

	/**
	 * If the repository does not contain any statement with the given 
	 * subject, predicate, and object, method returns true
	 *  
	 * @return true if the given IResource is outdated, false otherwise
	 *
	 */
	public static boolean isOutdated(ReloRdfRepository repo, Resource subj, URI pred, Value obj, IResource parentRDFFileResource) {
		
		//Return false if the given IResource does not exist
		if(!parentRDFFileResource.exists()) return false;
		
		//Return false if the repository contains any statement with the given subj, pred, and obj
		StatementIterator it = repo.getStatements(subj, pred, obj);
		if (it.hasNext()) return false;
				
		return true;
	}

	 /**
	  * Creates a problem marker on the given out of date IResource. 
	  * An error message shows up in the error log, all file and editor icons are 
	  * flagged with an error decoration, and when the file is opened in a text 
	  * editor, the line containing the outdated statement is flagged
	  * 
	  */
	public static void addErrors(IResource parentRDFFileResource, Resource subj, URI pred) {
		try {
			//Remove any previous errors on the bad resource and add the current error
			parentRDFFileResource.deleteMarkers(IMarker.PROBLEM, false, IResource.DEPTH_ZERO);
			IMarker errorMarker = parentRDFFileResource.createMarker(IMarker.PROBLEM);
			errorMarker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_ERROR);
			
			//If problem is a link problem, it is given by predicate. Otherwise, given by subject
			String problem = "";
			if(pred==null) {
				problem = subj.toString().substring(subj.toString().lastIndexOf("$")+1);
			} else {
				problem = pred.toString().substring(pred.toString().lastIndexOf("#")+1);
			}
			errorMarker.setAttribute(IMarker.MESSAGE, "Diagram is outdated. " +problem+ " no longer exists");
			
			//Flag the line containing out-of-date statement when file is opened in text editor
			int lineNumber = 1;
			if(parentRDFFileResource instanceof IFile) {
				IFile badFile = (IFile)parentRDFFileResource;
				BufferedReader reader = new BufferedReader(new InputStreamReader(badFile.getContents(true)));
				String line = reader.readLine();
				while (line != null && !line.contains(problem)) {
					lineNumber++;
					line = reader.readLine();
				}
				reader.close();
			}
			errorMarker.setAttribute(IMarker.LINE_NUMBER, lineNumber);
			errorMarker.setAttribute(IMarker.LOCATION, "line " + lineNumber);
			
		} catch (CoreException e) {
			logger.error("Could not create error marker", e);
		} catch (IOException e) {
			logger.error("Could not read contents of outdated saved diagram", e);
		}
	}
	
	/**
	 * Check all the given .relo files to see if they are in 
	 * sync with the given repository
	 */
	@SuppressWarnings("unused")
	public static void checkDependentReloFiles(IResource resource, List<Resource> reloFiles, final ReloRdfRepository repo) {
		// We are no longer adding checks for errors in relo files
		if (true) return;
		for(Resource reloRes : reloFiles) {
			IResource reloFile = RSECore.resourceToEclipseResource(repo, reloRes, resource.getProject());
			if (reloFile == null) continue;

			boolean isOutdated = false;
			
			//Find all statements the .relo file contains
			StatementIterator fileIt = repo.getStatements(reloRes, RSECore.contains, null);
			while(fileIt.hasNext()) {
				Resource editPart = (Resource) fileIt.next().getObject();
				URI type = (URI) repo.getStatement(editPart, repo.rdfType, null).getObject();
				if(RSECore.node.equals(type)) {
					//Find the model Resource and see if it still exists in the repository
					Resource modelRes = (Resource) repo.getStatement(editPart, RSECore.model, null).getObject();
					isOutdated = LoadUtils.isOutdated(repo, modelRes, reloFile) ? true : isOutdated;
					if(isOutdated) {
						addErrors(reloFile, modelRes, null);
					}
				} else if (RSECore.link.equals(type)) {
					//Find the source and destination resources and check whether the link between them still exists
					URI linkRes =(URI) repo.getStatement(editPart, RSECore.model, null).getObject(); 
					Resource srcNode = (Resource) repo.getStatement(editPart, StoreUtil.createMemURI(RDF.SUBJECT), null).getObject();
					Resource dstNode = (Resource) repo.getStatement(editPart, StoreUtil.createMemURI(RDF.OBJECT), null).getObject();
					Resource modelResSrc = (Resource) repo.getStatement(srcNode, RSECore.model, null).getObject();
					Resource modelResDst = (Resource) repo.getStatement(dstNode, RSECore.model, null).getObject();
					isOutdated = LoadUtils.isOutdated(repo, modelResSrc, linkRes, modelResDst, reloFile) ? true : isOutdated;
					if(isOutdated) {
						addErrors(reloFile, modelResSrc, linkRes);
					}
				}
			}
			
			//If the .relo file was found to not be outdated, remove any errors on it
			if(!isOutdated && reloFile.exists()) {
				removeErrors(reloFile);
			}
		}
	}
	
	/**
	 * Returns a list of all .relo files containing a statement that depends 
	 * on the given IResource
	 *
	 */
	public static List<Resource> getDependentReloFiles(Resource classKey, ReloRdfRepository repo) {
		
		List<Resource> reloFiles = new ArrayList<Resource>();
		List<Resource> containedResources = new ArrayList<Resource>();
		containedResources.add(classKey);
		
		//Find all resources the given resource contains
		StatementIterator it = repo.getStatements(classKey, RSECore.contains, null);
		while(it.hasNext()) {
			Resource contained = (Resource) it.next().getObject();
			containedResources.add(contained);
		}
		
		//For each contained Resource, find all .relo files that depend on 
		//that Resource
		for(Resource contained : containedResources) {
			StatementIterator nodeIter = repo.getStatements(null, RSECore.model, contained);
			while(nodeIter.hasNext()) {
				Resource node = nodeIter.next().getSubject();
				StatementIterator reloFileIt = repo.getStatements(null, RSECore.contains, node);
				while(reloFileIt.hasNext()) {
					Resource reloFile = reloFileIt.next().getSubject();
					reloFiles.add(reloFile);
				}
			}
		}
		
		//Check to make sure they are all .relo files
		List<Resource> reloFilesCopy = new ArrayList<Resource>(reloFiles);
		for(Resource file : reloFilesCopy) {
			StatementIterator fileIt = repo.getStatements(file, repo.rdfType, RSECore.reloFile);
			if(!fileIt.hasNext()) {
				reloFiles.remove(file);
			}
		}
		
		return reloFiles;
	}
	
	/**
	 * Remove all problem markers on the given IResource
	 * 
	 */
	public final static void removeErrors(IResource accurateFile) {
		try {
			accurateFile.deleteMarkers(IMarker.PROBLEM, false, IResource.DEPTH_ZERO);
		} catch (CoreException e) {
			logger.error("Could not delete error marker", e);
		}
	}

	
	public static RSEShareableDiagramEditorInput isLocalFileShared(IFile file, ReloRdfRepository memRepo, URI fileType, String diagramType) {
		Resource diagramRes = memRepo.getStatement((Resource)null, memRepo.rdfType, fileType).getSubject();
		if (diagramRes == null)
			logger.error("Diagram Resource not found.");

		LiteralNode diagramId = (LiteralNode) memRepo.getStatement(diagramRes, RSECore.sharedDiagramID, (Resource)null).getObject();
		if (diagramId == null) return null;
		LiteralNode groupId =  (LiteralNode) memRepo.getStatement(diagramRes, RSECore.sharedGroupID, (Resource)null).getObject();
		if (groupId == null) return null;
		LiteralNode diagramName = (LiteralNode) memRepo.getStatement(diagramRes, RSECore.sharedName, (Resource)null).getObject();
		if (diagramName == null) return null;
		LiteralNode desc = (LiteralNode) memRepo.getStatement(diagramRes, RSECore.sharedDesc, (Resource)null).getObject();
		if (desc == null) return null;
		
		RSEShareableDiagramEditorInput rseShareableEditorInput = null;	
		try {
			rseShareableEditorInput = new RSEShareableDiagramEditorInput(diagramId.toString(), groupId.toString(), diagramName.toString(), desc.toString(), file.getName(), diagramType, memRepo, true);
		} catch (Exception e) {
			logger.error("Error loading shared local file: ", e);
			return null;
		}
		return rseShareableEditorInput;
	}

	public static void checkForSavingSharedFile(WorkbenchPart editor, RdfDocumentWriter rdfWriter, Resource fileRes) throws IOException {
		if (editor instanceof RSEEditor) {
			RSEEditor rseEditor = (RSEEditor) editor;
			if (rseEditor.getEditorInput() instanceof RSEShareableDiagramEditorInput && 
					((RSEShareableDiagramEditorInput)rseEditor.getEditorInput()).getId() != null){
				RSEShareableDiagramEditorInput shrdInp = (RSEShareableDiagramEditorInput)rseEditor.getEditorInput();
				String diagramId = shrdInp.getId();
				String groupId = shrdInp.getGroupId();
				String desc  = shrdInp.getDescription();
				String name = shrdInp.getServerName();
				rdfWriter.writeStatement(fileRes, RSECore.sharedDiagramID, StoreUtil.createMemLiteral(diagramId));
				rdfWriter.writeStatement(fileRes, RSECore.sharedGroupID, StoreUtil.createMemLiteral(groupId));
				rdfWriter.writeStatement(fileRes, RSECore.sharedDesc, StoreUtil.createMemLiteral(desc));
				rdfWriter.writeStatement(fileRes, RSECore.sharedName, StoreUtil.createMemLiteral(name));
			}
		}
	}

}
