package com.architexa.diagrams.generate.subclipse;
import java.io.InputStream;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.team.core.RepositoryProvider;
import org.eclipse.team.core.variants.IResourceVariant;
import org.tigris.subversion.subclipse.core.ISVNLocalFile;
import org.tigris.subversion.subclipse.core.ISVNLocalResource;
import org.tigris.subversion.subclipse.core.ISVNRemoteFile;
import org.tigris.subversion.subclipse.core.ISVNRemoteResource;
import org.tigris.subversion.subclipse.core.SVNException;
import org.tigris.subversion.subclipse.core.SVNProviderPlugin;
import org.tigris.subversion.subclipse.core.resources.SVNWorkspaceRoot;
import org.tigris.subversion.subclipse.ui.decorator.SVNLightweightDecorator;

import com.architexa.diagrams.generate.GeneratePlugin;
import com.architexa.diagrams.generate.team.UncommittedChangesDiagramGenerator;
import com.architexa.diagrams.ui.PluggableDiagramsSupport.IRSEDiagramEngine;

/**
 * 
 * @author Elizabeth L. Murnane
 *
 */
public class SubclipseUncommittedChangesDiagramGenerator extends UncommittedChangesDiagramGenerator {

	private static final Logger logger = GeneratePlugin.getLogger(SubclipseUncommittedChangesDiagramGenerator.class);

	public SubclipseUncommittedChangesDiagramGenerator() {
	}

	public SubclipseUncommittedChangesDiagramGenerator(List<IJavaElement> selectedElements, IRSEDiagramEngine diagramEngine) {
		super(selectedElements, diagramEngine);
	}

	@Override
	public boolean isManaged(Object resource) {
		if(!(resource instanceof IFile)) return false;
		// TODO Only expecting resource to be an
		// IFile, but handle case when it isn't

		IFile file = (IFile) resource;
		ISVNLocalFile svnFile = SVNWorkspaceRoot.getSVNFileFor(file);

		try {
			return svnFile.isManaged();
		} catch (SVNException e) {
			logger.error("Unexpected exception while determining whether " +
					"resource" + file.getName() + " is managed by SVN", e);
		} 
		return false;
	}

	@Override
	public boolean isChanged(IResource resource) {
		ISVNLocalResource svnResource = SVNWorkspaceRoot.getSVNResourceFor(resource);
		try {
			// If it hasn't been checked in yet, 
			// consider it an uncommitted change
			if(!svnResource.isManaged()) return true;
		} catch (SVNException e) {
			logger.error("Unexpected exception while determining whether " +
					"resource" + resource.getName() + " is managed by SVN");
		} 
		return SVNLightweightDecorator.isDirty(svnResource);
	}

	@Override
	public Object getLatestRevision(Object file) {
		if(file instanceof IFile) return getLatestRevision((IFile)file);
		// TODO Only expecting file to be an
		// IFile, but handle case when it isn't
		return null;
	}

	private Object getLatestRevision(IFile file) {
		RepositoryProvider provider = RepositoryProvider.getProvider(file.getProject());
		if(!SVNProviderPlugin.getTypeId().equals(provider.getID())) return null;

		try {
			ISVNRemoteResource svnRes = SVNWorkspaceRoot.getLatestResourceFor(file);
			if(svnRes==null || !(svnRes instanceof ISVNRemoteFile)) return null;
			return (ISVNRemoteFile) svnRes;
		} catch (SVNException e) {
			logger.error("Unexpected exception while finding latest revision for file " + file.getName(), e);
		}
		return null;
	}

	@Override
	public String getNameOfRevisionFile(Object file) {
		if(file instanceof IFile) return ((IFile)file).getName();
		if(file instanceof ISVNRemoteResource) 
			return ((ISVNRemoteResource)file).getName();
		if(file!=null) return file.toString();
		return "Unknown SVN file";
	}
	
	@Override
	public String getCompleteFilePath(Object file) {
		String name = "";
		String path = "";
		if(file instanceof IFile) {
			path = ((IFile)file).getFullPath().toString();
			name = ((IFile)file).getName();
		}
		if(file instanceof ISVNRemoteResource) {
			name = ((ISVNRemoteResource)file).getName();
			path = ((ISVNRemoteResource)file).getProjectRelativePath();
		}
		
		if (name.length() != 0 && path.length() != 0)
			return path + "/" + name;
		
		if(file!=null) return file.toString();
		return "Unknown SVN file";
	}
	
	@Override
	public InputStream getInputStreamFromDoc(Object revision, IProgressMonitor monitor) throws CoreException {
		if (!(revision instanceof IResourceVariant)) return null;
		return ((IResourceVariant)revision).getStorage(monitor).getContents();
	}
}
