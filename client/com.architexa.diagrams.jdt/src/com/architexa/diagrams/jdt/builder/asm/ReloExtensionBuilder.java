/**
 * 
 */
package com.architexa.diagrams.jdt.builder.asm;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipException;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.openrdf.model.Resource;
import org.openrdf.model.Value;
import org.openrdf.rio.ParseException;
import org.openrdf.rio.Parser;
import org.openrdf.rio.StatementHandler;
import org.openrdf.rio.StatementHandlerException;
import org.openrdf.sesame.sail.StatementIterator;

import com.architexa.diagrams.RSECore;
import com.architexa.diagrams.jdt.Activator;
import com.architexa.diagrams.jdt.builder.AtxaBuildVisitor;
import com.architexa.diagrams.jdt.builder.PluggableExtensionBuilderSupport;
import com.architexa.diagrams.jdt.builder.ResourceToProcess;
import com.architexa.store.ReloRdfRepository;
import com.architexa.store.StoreUtil;

public class ReloExtensionBuilder implements PluggableExtensionBuilderSupport.IAtxaExtensionBuilder {
	private static final Logger logger = Activator.getLogger(ReloExtensionBuilder.class);

	private ReloRdfRepository reloRdf;

	public void processProject(AtxaBuildVisitor builder, IProject resource) {}
	
	public List<Resource> processExtensionResource(AtxaBuildVisitor builder, ResourceToProcess rtp) {
		this.reloRdf = builder.getRepo();

		try {
			processReloFile(rtp);
		} catch (Exception e) {
			logger.error("Unexpected Error Parsing: " + rtp.resource.getFullPath(), e);
		}
		// doesnt need to call the runProcessors
		return null;
	}
	/**
	 * If .relo file is deleted, removes all statements that were put 
	 * in the repository when the .relo file was created. If .relo file 
	 * is added, add all its statements back into the repository
	 * @throws CoreException 
	 * @throws StatementHandlerException 
	 * @throws ParseException 
	 * @throws IOException 
	 *
	 */
	private void processReloFile(ResourceToProcess rtp) throws CoreException, IOException, ParseException, StatementHandlerException {
		final Resource reloRes = RSECore.eclipseResourceToRDFResource(reloRdf, rtp.resource);
		if(rtp.remove) {
			//Remove all statements that were added when the .relo file was created
			StatementIterator containsIter = reloRdf.getStatements(reloRes, RSECore.contains, null);
			reloRdf.startTransaction();
			while(containsIter.hasNext()) {
				Resource res = (Resource) containsIter.next().getObject();
				reloRdf.removeStatements(res, null, null);
			}
			reloRdf.commitTransaction();
			
			reloRdf.startTransaction();
			reloRdf.removeStatements(reloRes, null, null);
			reloRdf.commitTransaction();
		} 
		
		if (rtp.add) {
			//Read and parse the contents of the .relo file and add the statements to the repository
			reloRdf.startTransaction();
			reloRdf.addStatement(reloRes, reloRdf.rdfType, RSECore.reloFile);
			reloRdf.commitTransaction();
			
			BufferedReader reader = new BufferedReader(new InputStreamReader(((IFile)rtp.resource).getContents(true)));
			Parser rdfParser = StoreUtil.getRDFParser(reloRdf);
			rdfParser.setStatementHandler(new StatementHandler() {
				public void handleStatement(Resource subj, org.openrdf.model.URI pred, Value obj) throws StatementHandlerException {
					reloRdf.addStatement(reloRes, RSECore.contains, subj);
					reloRdf.addStatement(subj, pred, obj);
				}});
			
			reloRdf.startTransaction();
			rdfParser.parse(reader, ReloRdfRepository.atxaRdfNamespace);
			reloRdf.commitTransaction();
			reader.close();

			List<Resource> reloFiles = new ArrayList<Resource>();
			reloFiles.add(reloRes);
//			if(LoadUtils.outdatedCheckingOn) LoadUtils.checkDependentReloFiles(rtp.resource, reloFiles, reloRdf);
		}
	}

	public void autoBuildJar(String string) throws ZipException, IOException {
		
	}
}