/**
 * 
 */
package com.architexa.diagrams.jdt.builder.asm;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.jdt.core.JavaModelException;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Type;
import org.openrdf.model.Resource;

import com.architexa.collab.UIUtils;
import com.architexa.diagrams.RSECore;
import com.architexa.diagrams.jdt.Activator;
import com.architexa.diagrams.jdt.RJCore;
import com.architexa.diagrams.jdt.builder.AtxaBuildVisitor;
import com.architexa.diagrams.jdt.builder.PluggableExtensionBuilderSupport;
import com.architexa.diagrams.jdt.builder.ReloASTExtractor;
import com.architexa.diagrams.jdt.builder.ResourceQueue;
import com.architexa.diagrams.jdt.builder.ResourceToProcess;
import com.architexa.rse.PreferenceUtils;
import com.architexa.store.ReloRdfRepository;
import com.architexa.store.StoreUtil;

public class ClassExtensionBuilder implements PluggableExtensionBuilderSupport.IAtxaExtensionBuilder {
	private static final Logger logger = Activator.getLogger(ClassExtensionBuilder.class);
	
	public static final String JARS_TO_BUILD = "jarsToBuild";
	public static final String JARS_TO_REMOVE = "jarsToRemove";
	public static final String JARS_SELECTED = "jarsSelected";

	private AtxaBuildVisitor builder;
	private ReloRdfRepository reloRdf;

	public void processProject(AtxaBuildVisitor builder, IProject project) {
		try {
			this.builder = builder;
			this.reloRdf = builder.getRepo();

			addPrimitives();
			processArchives(project);
		} catch (CoreException e) {
			logger.error("Issue processing " + project.getName(), e);
		}
	}
	public List<Resource> processExtensionResource(AtxaBuildVisitor builder, ResourceToProcess rtp) {
		try {
			this.builder = builder;
			this.reloRdf = builder.getRepo();
			
			return processClassFile(rtp);
		} catch (JavaModelException e) {
			logger.error("Issue processing "+ rtp.getName(), e);
		}
		return null;
	}
    private List<Resource> processClassFile(ResourceToProcess rtp) throws JavaModelException{
    	IFile classFile = (IFile) rtp.resource;
    	String classPath = rtp.getName();
    	
    	reloRdf.startTransaction();

		if (rtp.remove) {
			// file has been removed ==> remove statements
			Resource currFileRes = RSECore.eclipseResourceToRDFResource(reloRdf, rtp.resource);
			List<Resource> srcResources = ReloASTExtractor.getResourceForFile(reloRdf, currFileRes);
			if (srcResources != null) {
				for(Resource res : srcResources) {	
					reloRdf.removeStatements(res, null, null);
//					List<Resource> reloFiles = LoadUtils.getDependentReloFiles(res, reloRdf);
//					if(LoadUtils.outdatedCheckingOn) LoadUtils.checkDependentReloFiles(rtp.resource, reloFiles, reloRdf);
//					ErrorBuildListeners.check();
				}
			}

			//Resource is removed, so remove the contains statement to it from its package
			Type classType = AsmUtil.internalNameToType(classPath.replace(".class", ""));
			Resource classRes = AsmUtil.getClassRes(classType.getClassName(), reloRdf);
			Resource packageRes = AsmPackageSupport.getPackage(reloRdf, classType, builder.getProjectResource());
			reloRdf.removeStatements(packageRes, RSECore.contains, classRes);
			builder.getDSS().updateSrcFldrCache(packageRes);
			// if nothing to add then commit repository and return
			if (!rtp.add) {
				reloRdf.commitTransaction();
				return null;
			}
		}

		String name = classPath.replace(AtxaBuildVisitor.ClassExt, "").replaceAll("/", ".");
		
		
		
		// Do no check for relo errors. Reenable in the future
		// Resource classKey = AsmUtil.getClassRes(name.replace(AtxaBuildVisitor.ClassExt, ""), reloRdf,0);
		// if(classKey!=null) {
		// 	reloFiles = LoadUtils.getDependentReloFiles(classKey, reloRdf);
		// }
		
		List<Resource> classResList = parseClassFile(name, rtp.remove, rtp.add, classFile);
		reloRdf.addNameStatement(RSECore.eclipseResourceToRDFResource(reloRdf, classFile), 
								RSECore.name, 
								classFile.getName());
		
//		List<Resource> reloFiles = null;
//		if(reloFiles!=null) {
//			LoadUtils.checkDependentReloFiles(rtp.resource, reloFiles, reloRdf);
//			ErrorBuildListeners.check();
//		}

		reloRdf.commitTransaction();
		return classResList;
    }
    
	private void processArchives(IProject projectRes) throws CoreException {
		String prop = getPersistentProperty(projectRes);
		Object selectedProperty  = ClassExtensionBuilder.parsePersistent(prop);
		if (selectedProperty != null && (selectedProperty instanceof ArrayList<?>)) {
			processJarsList((List<?>)selectedProperty, true, false);
		}
	}

	private void processJarsList(List<?> jarsList, boolean add, boolean remove) {
		for(int i=0; i<jarsList.size(); i++) {
			try {
				String jarLocation = jarsList.get(i).toString();
				URL jarURL = new URL(jarLocation.replaceAll(" ", "%20").replaceAll("file://", "file:/"));
				URI uri = jarURL.toURI();
				parseArchive(new ZipFile(new File(uri)), add, remove);
			} catch(Exception e) {
				logger.error("Could not parse file. ", e);
			}
		}
	}
    
    private void parseArchive(ZipFile archive, boolean add, boolean remove) {
    	if (!reloRdf.transactionStarted())
			reloRdf.startTransaction();
    	int commitInterval = 1000;
    	int count = 0;
    	for (Enumeration<? extends ZipEntry> en = archive.entries(); en.hasMoreElements();) {
    		if (++count % commitInterval == 0) {
    			reloRdf.commitTransaction();
    			reloRdf.startTransaction();
    		}
    		if (builder.isTaskCancelled()) {
    			reloRdf.commitTransaction();
    			return;
    		}
    		ZipEntry ze = en.nextElement();
    		
    		builder.updateTask(ze.getName() + ": Parsing " + archive.getName());
    		if (!reloRdf.transactionStarted())
    			reloRdf.startTransaction();
			try {
	    		if (ze.getName().endsWith(AtxaBuildVisitor.ClassExt)) {
					parseClassFileInArchive(archive, add, remove, ze);
				} else if (ze.getName().endsWith(AtxaBuildVisitor.JarExt) || ze.getName().endsWith(AtxaBuildVisitor.ZipExt)) {
					parseArchiveInArchive(archive, add, remove, ze);
				}
			} catch (IOException e) {
				logger.error("Error parsing: " + ze.getName(), e);
			} catch (URISyntaxException e) {
				logger.error("Error parsing: " + ze.getName(), e);
			}
		}
		reloRdf.commitTransaction();
	}

	private void parseArchiveInArchive(ZipFile archive, boolean add, boolean remove, ZipEntry ze) throws IOException, FileNotFoundException,
			MalformedURLException, URISyntaxException, ZipException {
		String path = archive.getName().substring(0, archive.getName().lastIndexOf("\\") + 1) + ze.getName().replace("/", "-").replace("\\", "-");
		BufferedInputStream input = new BufferedInputStream(archive.getInputStream(ze));
		BufferedOutputStream output = new BufferedOutputStream(new FileOutputStream(path));
		
		int buffer_size = 1024 * 50;
		byte[] buffer = new byte[buffer_size];
		
		int len = 0;
		while ((len = input.read(buffer)) > 0) {
			output.write(buffer, 0, len);
		}
		input.close();
		output.flush();
		output.close();
		
		if(!(path.startsWith("file:"))) {
			path = "file:/" + path;
		}
		URL noAuthorityURL = new URL(path.replaceAll(" ", "%20").replaceAll("file://", "file:/"));
		URI entryURI = noAuthorityURL.toURI();
		File file = new File(entryURI);
		ZipFile zip = new ZipFile(file);
		parseArchive(zip, add, remove);
		file.delete();
	}

	private void parseClassFileInArchive(ZipFile archive, boolean add, boolean remove, ZipEntry ze) throws IOException {
		if (builder.isTaskCancelled()) return;
		List<Resource> resList;
		if (remove && !add){
			resList = AsmClassSupport.removeClassStatement(builder.getRepo(), ze.getName(), builder);
//			builder.getRepo().commitTransaction();
			for (Resource res : resList)
				builder.runProcessors(res, false);
		}
		if(add && !remove) {
			Resource classRes = parseClassFileFromStream(archive.getInputStream(ze), null, true);
//			builder.getRepo().commitTransaction();
			builder.runProcessors(classRes, false);  
		}
	}

	private List<Resource> parseClassFile(String className, boolean remove, boolean add, IFile classFile) {
		builder.updateTask("Parsing " + className);
		className = AsmUtil.quickReplace(className, ".", "/").append(AtxaBuildVisitor.ClassExt).toString();
		
		InputStream classStream = null;
		try {
			List<Resource> resList = new ArrayList<Resource>();
			if(remove)
				resList.addAll(AsmClassSupport.removeClassStatement(builder.getRepo(), className, builder));
			if(add) {
				classStream = classFile.getContents(true);
				Resource res = parseClassFileFromStream(classStream, classFile, false);
				if (res != null)
					resList.add(res);
			}
			return resList;
		} catch (CoreException e) {
			// will happen if the classFile either (a) does not exist or (b) is not local
			// i.e. we should just ignore
		} catch (FileNotFoundException e) {
			// we are really not expecting this - but it does seem to get thrown
			// this should be treated same as above
		}catch (StackOverflowError e) {
			StoreUtil.deleteDefaultStoreRepository();
			ResourceQueue.clear();
			UIUtils.openErrorPromptDialog("Unexpected Error While Building Architexa", "Please restart your Eclipse Workspace and Rebuild your" +
						"\nArchitexa Index by going to 'Architexa->Rebuild Complete Index'");
		}catch (Throwable e) {
			logger.error("Unexpected Exception, trying to parse: " + className + " loc: " + classFile.getFullPath(), e);
		} finally {
			try { 
				if (classStream != null) classStream.close(); 
			} catch (Throwable t) {}
		}
		return null;
	}
	
	private Resource parseClassFileFromStream(InputStream classStream, IResource classFile, boolean isArchive) throws IOException {
		ClassReader cr = new ClassReader(classStream);
		AsmClassSupport classVisitor = new AsmClassSupport(
				builder.getRepo(),
				builder.getDSS(), 
				builder.getProjectResource(), 
				builder.getProjectName(), 
				classFile);
		cr.accept(classVisitor, ClassReader.SKIP_FRAMES);
		return classVisitor.getClassRes();
	}
	
	private void addPrimitives() {
		String[] types = new String[] { 
				"B", "C", "D", "F", "I", "J", "V", "Z",
				"S", "LList;", "LMap;", "LSet;", "LString;" };
		String[] subTypes = new String[] { "", "[]", "[][]" };
		reloRdf.startTransaction();

		for (int i = 0; i < types.length; i++) {
			String name = Type.getType(types[i]).getClassName();

			for (int j = 0; j < subTypes.length; j++) {
				// vineet: bug? this seems to be incorrect
				Resource uri = reloRdf.getDefaultURI(RJCore.jdtWkspcNS + "$" + name + subTypes[j]);

//				if (reloRdf.contains(uri, RSECore.name, (Value) null)) {
//					continue;
//				}

				reloRdf.addNameStatement(uri, RSECore.name, name + subTypes[j]);
				reloRdf.addInitializedStatement(uri, RSECore.initialized, true);
				reloRdf.addTypeStatement(uri, RJCore.classType);
			}
		}
		reloRdf.commitTransaction();
	}
	
	// Parse Saved PersistentProperty from String into List<URL>
	public static Collection<URL> parsePersistent(String persistentProperty) {
		Collection<URL> retVal = new ArrayList<URL>();
		String delims = "[,]";
		if (persistentProperty == null || persistentProperty.equals("")) return null;
		persistentProperty = persistentProperty.substring(1, persistentProperty.length()-1);
		
		String[] tokens = persistentProperty.split(delims);
		for (int i = 0; i < tokens.length; i++) {
			if (tokens[i].equals("")) continue;
			try {
				retVal.add(new URL(tokens[i]));
			} catch (MalformedURLException e) {
			}
		}
		return retVal;
	}
	
	public static String getPersistentProperty(IProject proj) {
		try {
			int i = 0;
			String prop = proj.getPersistentProperty(new QualifiedName(ClassExtensionBuilder.JARS_SELECTED, ClassExtensionBuilder.JARS_SELECTED));
			if (prop != null && !prop.equals(""))
				return prop;

			prop = proj.getPersistentProperty(new QualifiedName(ClassExtensionBuilder.JARS_SELECTED, proj.getName() + i));
			String propNext = prop;
			while (propNext != null && propNext.length() == PreferenceUtils.divVal) {
				i = i + PreferenceUtils.divVal;
				propNext = proj.getPersistentProperty(new QualifiedName(ClassExtensionBuilder.JARS_SELECTED, proj.getName() + i));
				if (propNext != null)
					prop += propNext; 
			}
			
			if (propNext != null && !propNext.equals("")) {
				i += propNext.length();
				prop += proj.getPersistentProperty(new QualifiedName(ClassExtensionBuilder.JARS_SELECTED, proj.getName() + i));
			}
			
			return prop;
		} catch (CoreException e) {
			logger.error("Error getting project persitent properties for libraries. \n");
			e.printStackTrace();
			return "";
		}
	}
	
	public void autoBuildJar(String uri) throws ZipException, IOException {
		parseArchive(new ZipFile(new File(uri)), true, false);
	}
	
	public void setRepo(ReloRdfRepository reloRdf) {
		this.reloRdf = reloRdf;
	}
	public void setBuilder(AtxaBuildVisitor _builder) {
		this.builder = _builder;
		
	}
}