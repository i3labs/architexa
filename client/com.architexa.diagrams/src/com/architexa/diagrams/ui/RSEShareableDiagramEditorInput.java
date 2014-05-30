package com.architexa.diagrams.ui;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;

import com.architexa.store.ReloRdfRepository;
import com.architexa.store.StoreUtil;

public class RSEShareableDiagramEditorInput implements IEditorInput {

	private String localName = "";
	private ImageDescriptor imgDesc = null;
	private String toolTipText= "RSE Diagram";
	private String id = null;
	private String serverName = "";
	private String groupId;
	private String description;
	private String tags;
	private String diagramType = "";
	private InputStream inputStream;
	private Map<IDocument, IDocument> lToRDocMap;
	private Map<IDocument, String> lToPathMap;
	private StringBuffer docBuff;
	private ReloRdfRepository memRepo;
	private boolean isLocal = true;
	private boolean isSavedFile = false;
	
	// allows architexa to open other rdf stores (null == uses default workspace)
	public IPath wkspcPath = null;
	private IFile file = null;
	
	
	public RSEShareableDiagramEditorInput(String name, ImageDescriptor imgDesc, String toolTipText, String diagramType) {
		this.localName = name;
		this.imgDesc = imgDesc;
		this.toolTipText = toolTipText;
		this.setDiagramType(diagramType);
	}
	
	public RSEShareableDiagramEditorInput(//InputStream contents, 
			String name, String diagramType, ReloRdfRepository memRepo, boolean isSavedFile) {
		this.diagramType = diagramType;
		setLocalName(name);
		setMemRepo(memRepo);
		setSavedFile(isSavedFile);
	}
	
	public RSEShareableDiagramEditorInput(String diagramId, String groupId,
			String name, String description) {
		setServerName(name);
		this.id = diagramId;
		this.groupId = groupId;
		this.description = description;
	}
	
	public RSEShareableDiagramEditorInput(String diagramId, String groupId,
			String name, InputStream inputStream, String description, String tags, 
			Map<IDocument, IDocument> lToRDocMap, Map<IDocument, String> lToPathMap, StringBuffer docBuff, String diagramType, boolean isLocal) {
		setServerName(name);
		this.id = diagramId;
		this.groupId = groupId;
		this.description = description;
		this.tags = tags;
		setInputStream(inputStream);
		setlToRDocMap(lToRDocMap);
		setlToPathMap(lToPathMap);
		setDocBuff(docBuff);
		setDiagramType(diagramType);
		setLocal(isLocal);
		setSavedFile(!isLocal);
	}

	// Used when a opening a saved diagram which has been previously shared on the server
	public RSEShareableDiagramEditorInput(String diagramId, String groupId,
			String name, String description, String savedName, String diagramType, ReloRdfRepository memRepo, boolean isSavedFile) {
		this(diagramId, groupId, name, description);
		setLocalName(savedName); // Show the saved name but the original name is the shared name
		setDiagramType(diagramType);
		setMemRepo(memRepo);
		setSavedFile(isSavedFile);
	}

	public RSEShareableDiagramEditorInput(String name, ImageDescriptor imgDesc, String toolTipText, String diagramType,
			Map<IDocument, IDocument> lToRDocMap, Map<IDocument, String> lToPathMap, StringBuffer docBuff) {
		this(name, imgDesc, toolTipText, diagramType);
		setlToRDocMap(new HashMap<IDocument, IDocument>(lToRDocMap));
		setlToPathMap(new HashMap<IDocument, String>(lToPathMap));
		setDocBuff(docBuff);
	}

//	public RSEShareableDiagramEditorInput(String name, String diagramType) {
//		this.diagramType = diagramType;
//		setLocalName(name);
//	}

	public boolean exists() {
		return false;
	}
	
	public IPersistableElement getPersistable() {
		return null;
	}
	
	@SuppressWarnings("rawtypes")
	public Object getAdapter(Class adapter) {
		return null;
	}
	
	public ImageDescriptor getImageDescriptor() {
		return imgDesc;
	}

	public String getName() {
		if (!isLocal && serverName.length() != 0)
			return serverName + SHARED;
		else
			return localName;
	}

	public String getLocalName() {
		return localName;
	}
	
	private static String SHARED = "[shared]";
	private static String SHARED_REGEX = "\\[shared\\]";
	private String removeSharedTag(String name) {
		if (name == null || name.length() == 0)
			return "";
		if (!name.contains(SHARED)) return name;
		
		String[] namePart = name.split(SHARED_REGEX);
		return namePart[0];
	}
	
	private void setLocalName(String name) {
		if (name == null) return;
		this.localName = removeSharedTag(name.trim());
	}
	
	private void setServerName(String name) {
		if (name == null) return;
		this.serverName = removeSharedTag(name.trim());
	}
	
	public void setName(String name, boolean isLocalSave) {
		if (name == null) return;
		if (isLocalSave)
			setLocalName(name);
		else
			setServerName(name);
	}	

	public String getServerName() {
		if (serverName.length()==0)
			return localName;
		return serverName;
	}
	
	public String getDescription() {
		return description;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}
	
	public String getToolTipText() {
		return toolTipText;
	}

	public String getGroupId() {
		return groupId;
	}

	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}
	
	public void setId(String id) {
		this.id = id;
	}
	
	public String getId() {
		return id;
	}

	public void setDiagramType(String diagramType) {
		this.diagramType = diagramType;
	}

	public String getDiagramType() {
		return diagramType;
	}

	public void setlToRDocMap(Map<IDocument, IDocument> lToRDocMap) {
		this.lToRDocMap = lToRDocMap;
	}

	public Map<IDocument, IDocument> getlToRDocMap() {
		return lToRDocMap;
	}

	public void setlToPathMap(Map<IDocument, String> lToPathMap) {
		this.lToPathMap = lToPathMap;
	}

	public Map<IDocument, String> getlToPathMap() {
		return lToPathMap;
	}

	public void setDocBuff(StringBuffer docBuff) {
		this.docBuff = docBuff;
	}

	public StringBuffer getDocBuff() {
		return docBuff;
	}

	public void setMemRepo(ReloRdfRepository memRepo) {
		this.memRepo = memRepo;
	}

	public ReloRdfRepository getMemRepo() {
		if (memRepo == null)
			return StoreUtil.getMemRepository();
		return memRepo;
	}

	public void setInputStream(InputStream inputStream) {
		this.inputStream = inputStream;
	}

	public InputStream getInputStream() {
		return inputStream;
	}

	public void setLocal(boolean isLocal) {
		this.isLocal = isLocal;
	}

	public boolean isLocal() {
		return isLocal;
	}

	public void setSavedFile(boolean isSavedFile) {
		this.isSavedFile = isSavedFile;
	}

	public boolean isSavedFile() {
		return isSavedFile;
	}

	public void setTags(String tags) {
		this.tags = tags;
	}

	public String getTags() {
		return tags;
	}

	public IFile getFile() {
		return file;
	}

	public void setFile(IFile file) {
		this.file = file;
	}
}
