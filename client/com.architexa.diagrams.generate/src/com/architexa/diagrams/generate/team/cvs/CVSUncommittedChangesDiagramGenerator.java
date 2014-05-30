package com.architexa.diagrams.generate.team.cvs;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.team.core.RepositoryProvider;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.history.IFileRevision;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.CVSProviderPlugin;
import org.eclipse.team.internal.ccvs.core.ICVSRemoteFile;
import org.eclipse.team.internal.ccvs.core.ICVSRemoteResource;
import org.eclipse.team.internal.ccvs.core.ICVSResource;
import org.eclipse.team.internal.ccvs.core.ILogEntry;
import org.eclipse.team.internal.ccvs.core.filehistory.CVSResourceVariantFileRevision;
import org.eclipse.team.internal.ccvs.core.resources.CVSWorkspaceRoot;
import org.eclipse.team.internal.ccvs.core.resources.RemoteFile;
import org.eclipse.team.internal.ccvs.ui.CVSLightweightDecorator;

import com.architexa.diagrams.generate.GeneratePlugin;
import com.architexa.diagrams.generate.team.ChangeSet;
import com.architexa.diagrams.generate.team.UncommittedChangesDiagramGenerator;
import com.architexa.diagrams.ui.PluggableDiagramsSupport.IRSEDiagramEngine;

/**
 * 
 * @author Elizabeth L. Murnane
 *
 */
public class CVSUncommittedChangesDiagramGenerator extends UncommittedChangesDiagramGenerator {

	private static final Logger logger = GeneratePlugin.getLogger(CVSUncommittedChangesDiagramGenerator.class);

	public CVSUncommittedChangesDiagramGenerator() {
	}

	public CVSUncommittedChangesDiagramGenerator(List<IJavaElement> selectedElements, IRSEDiagramEngine diagramEngine) {
		super(selectedElements, diagramEngine);
	}

	@Override
	public boolean isManaged(Object resource) {
		if(!(resource instanceof IFile)) return false;
		// TODO Only expecting resource to be an
		// IFile, but handle case when it isn't

		IFile file = (IFile) resource;
		ICVSResource cvsResource = CVSWorkspaceRoot.getCVSResourceFor(file);
		try {
			return cvsResource.isManaged();
		} catch (CVSException e) {
			logger.error("Unexpected exception while determining whether " +
					"resource" + file.getName() + " is managed by CVS", e);
		}
		return false;
	}

	@Override
	public boolean isChanged(IResource resource) {
		return CVSLightweightDecorator.isDirty(resource);
	}

	@Override
	public Object getLatestRevision(Object file) {
		if(file instanceof IFile) return getLatestRevision((IFile)file);
		// TODO Only expecting file to be an
		// IFile, but handle case when it isn't
		return null;
	}

	@Override
	public Map getChangeMap(List<ChangeSet> selectedChangeSetList,
			Map<Object, Object> changedFilesToComparedRevisions) {
		Map<Object, Object> selectedResToPreviousVersionOfRes = new HashMap<Object, Object>();
		if (selectedChangeSetList == null) return changedFilesToComparedRevisions;
		for (ChangeSet changeSet : selectedChangeSetList) {
			Object[] resArr = changeSet.getAffectedResources();
			for (Object res : resArr) {
				changeSet.addToMap(res, selectedResToPreviousVersionOfRes);
			}
		}
		return selectedResToPreviousVersionOfRes;
	}
	
	
	private Object getLatestRevision(IFile file) {
		RepositoryProvider provider = RepositoryProvider.getProvider(file.getProject());
		if(!CVSProviderPlugin.getTypeId().equals(provider.getID())) return null;

		try {
			final ICVSRemoteResource remoteRes = CVSWorkspaceRoot.getRemoteResourceFor(file);
			if(remoteRes==null) return null;

			ILogEntry[] logEntries = remoteRes.getRepository().getRemoteFile(remoteRes.getRepositoryRelativePath(), null).getLogEntries(new NullProgressMonitor());
			if(logEntries==null || logEntries.length==0) return null;

			ILogEntry entry = logEntries[0];
			final ICVSRemoteFile remoteFile = entry.getRemoteFile();
			IFileRevision revision = (IFileRevision)remoteFile.getAdapter(IFileRevision.class);
			return revision;

		} catch (CVSException e) {
			logger.error("Unexpected exception while finding latest revision for file " + file.getName(), e);
		} catch (TeamException e) {
			logger.error("Unexpected exception while finding latest revision for file " + file.getName(), e);
		}
		return null;
	}

	@Override
	public String getNameOfRevisionFile(Object file) {
		if(file instanceof IFile) return ((IFile)file).getName();
		if(file instanceof ICVSRemoteFile)
			return ((ICVSRemoteFile)file).getName();
		if(file!=null) return file.toString();
		return "Unknown CVS file";
	}
	
	@Override
	public String getCompleteFilePath(Object file) {
		String name = "";
		String path = "";
		if(file instanceof IFile) {
			path = ((IFile)file).getFullPath().toString();
			name = ((IFile)file).getName();
		}
		if(file instanceof ICVSRemoteFile) {
			name = ((ICVSRemoteFile)file).getName();
			try {
				path = ((ICVSRemoteFile)file).getRelativePath(null).toString();
			} catch (CVSException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		if (name.length() != 0 && path.length() != 0)
			return path + "/" + name;
		
		if(file!=null) return file.toString();
		return "Unknown CVS file";
	}

	@Override
	public InputStream getInputStreamFromDoc(Object revision, IProgressMonitor monitor) throws CoreException {
		if (!(revision instanceof CVSResourceVariantFileRevision) && !(revision instanceof RemoteFile)) return null;
		if (revision instanceof RemoteFile) return ((RemoteFile) revision).getStorage(monitor).getContents();
		return ((CVSResourceVariantFileRevision)revision).getStorage(monitor).getContents();
	}

}
