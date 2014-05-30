/**
 * 
 */
package com.architexa.diagrams.strata.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IParent;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.IWorkbenchWindow;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;

import com.architexa.collab.UIUtils;
import com.architexa.diagrams.jdt.RJCore;
import com.architexa.diagrams.jdt.RJMapToId;
import com.architexa.diagrams.model.Artifact;
import com.architexa.diagrams.model.ArtifactFragment;
import com.architexa.diagrams.strata.StrataPlugin;
import com.architexa.diagrams.strata.model.StrataRootDoc;
import com.architexa.diagrams.strata.model.policy.ClosedContainerDPolicy;
import com.architexa.diagrams.utils.ErrorUtils;
import com.architexa.intro.AtxaIntroPlugin;
import com.architexa.intro.preferences.PreferenceConstants;
import com.architexa.rse.PreferenceUtils;

public class SelectionCollector {
	public static final Logger logger = StrataPlugin.getLogger(SelectionCollector.class);

	private final StrataRootDoc strataDoc;

	private List<ArtifactFragment> artFrags = new ArrayList<ArtifactFragment>();

	// helps build the hierarchy (we get the name of the parent and see if it
	// has already been added)
	Map<String, ArtifactFragment> nameToAFMap = new HashMap<String, ArtifactFragment>();
	public Map<Resource, IJavaElement> mapRDFtoJElement = new HashMap<Resource, IJavaElement>();

	// We need to track the parent proj of the packages so that we can open them
	// in the correct proj if they exisit in multiple projs
	public Map<Resource, Resource> mapRDFtoProjRes = new HashMap<Resource, Resource>();
	
	
	private final boolean hierarchical;

	private int ndxOfSelPckg;

	private boolean errorShown = false;

	private List<ArtifactFragment> selectedAFs = new ArrayList<ArtifactFragment>();

	private List<IJavaElement> listOfErrorProjects = new ArrayList<IJavaElement>();

	
	public SelectionCollector(StrataRootDoc strataDoc) {
		this.strataDoc = strataDoc;
		final IWorkbenchWindow activeWorkbenchWindow = StrataPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow();
		this.hierarchical = !flatPackages(activeWorkbenchWindow);
	}

	private void collect(IJavaElement ije, boolean isSelected) {		
			if (ije instanceof IMember) { // IField or IMethod
				collect(((IMember)ije).getParent(), false);
				return;
			}
			// collect parents first so that hierarchy is created correctly
			collectParents(ije, RJMapToId.getId(ije).length(), isSelected);
			
			if (ije instanceof IPackageFragment) {
				// add the package folder: add an '.*'
				Resource rdfRes = RJCore.idToResource(strataDoc.getRepo(), RJMapToId.pckgIDToPckgFldrID(RJMapToId.getId(ije)));
				if (!mapRDFtoJElement.containsKey(rdfRes)) {
					collect(rdfRes, this.hierarchical && isSelected);
					mapRDFtoJElement.put(rdfRes, ije);
					IJavaProject ijProj = ije.getJavaProject();
					Resource projRes = RJCore.idToResource(strataDoc.getRepo(), ijProj.getElementName().toString());
					mapRDFtoProjRes.put(rdfRes, projRes);
				}
				rdfRes = RJCore.jdtElementToResource(strataDoc.getRepo(), ije);
				
				if (!mapRDFtoJElement.containsKey(rdfRes))
					collect(rdfRes, !this.hierarchical && isSelected);
				// Do not overwrite RDF map with empty packages from other projects
				try {
					if (!mapRDFtoJElement.containsKey(rdfRes) || ((IPackageFragment)ije).containsJavaResources())
						mapRDFtoJElement.put(rdfRes, ije);
				} catch (JavaModelException e) {
					logger.error("Unexpected Exception.", e);
				}
			}
			else {
				Resource rdfRes = RJCore.jdtElementToResource(strataDoc.getRepo(), ije);
				// do not add duplicate resources/ create duplicate AFs
				if (!mapRDFtoJElement.containsKey(rdfRes)) {
					mapRDFtoJElement.put(rdfRes, ije);
					collect(rdfRes, isSelected);
				}	
			}
	}
	
//	private void collectParentsForRes(IJavaElement ije, int index, boolean isSelected) {
//		Resource parentResPackage = null;
//		index = RJMapToId.getId(ije).lastIndexOf(".", index-1);
//		// collect parent package folder if it has to be inferred from a jar file
//		if (index >= ndxOfSelPckg && (ije instanceof IPackageFragment)){
//			collectParents(ije, index, isSelected);
//			parentResPackage = RJCore.idToResource(strataDoc.getRepo(), RJMapToId.pckgIDToPckgFldrID(RJMapToId.getId(ije).substring(0,index)));
//			if (!mapRDFtoJElement.containsKey(parentResPackage)){
//				collect(parentResPackage, isSelected);
//				mapRDFtoJElement.put(parentResPackage, null);
//			}
//		}
//		// collect parent
//		else if(ije.getParent()!=null && !mapRDFtoJElement.containsValue(ije.getParent()) && !(ije.getParent() instanceof PackageFragmentRoot) ) { 
//			collect(ije.getParent(), isSelected);
//		}
//	
//	}
	
	@SuppressWarnings("restriction")
	private void collectParents(IJavaElement ije, int index, boolean isSelected) {
		Resource parentResPackage = null;
		index = RJMapToId.getId(ije).lastIndexOf(".", index-1);
		// collect parent package folder if it has to be inferred from a jar file
		if (index >= ndxOfSelPckg && (ije instanceof IPackageFragment)){
			collectParents(ije, index, isSelected);
			parentResPackage = RJCore.idToResource(strataDoc.getRepo(), RJMapToId.pckgIDToPckgFldrID(RJMapToId.getId(ije).substring(0,index)));
			if (!mapRDFtoJElement.containsKey(parentResPackage)){
				collect(parentResPackage, isSelected);
				mapRDFtoJElement.put(parentResPackage, null);
			}
		}
		// collect parent
		else if(ije.getParent()!=null && !mapRDFtoJElement.containsValue(ije.getParent()) && 
				!(ije.getParent() instanceof org.eclipse.jdt.internal.core.PackageFragmentRoot) ) { 
			collect(ije.getParent(), isSelected);
		}
	
	}

	private void collectAllFromAF(ArtifactFragment af) {
		List<Artifact> children = af.getArt().queryChildrenArtifacts(strataDoc.getRepo());
		for (Artifact art : children) {
			collect(strataDoc.createArtFrag(art.elementRes), true);
		}
//			collect(strataDoc.createArtFrag(af.getArt().elementRes), true);
	}
	
	private void collect(Resource res, boolean isSelected) {
		if (res instanceof URI){
			collect(strataDoc.createArtFrag(res), isSelected);
		}
	}
	
	private void collect(ArtifactFragment af, boolean isSelected) {
		// TODO: check if af has already been added
		addToHierarchy(af, isSelected);
		//System.err.println("Collected: " + af.getArt().elementRes);
	}

	private boolean isJarSelected(String jarName, IParent parentItem) {
		try {
			QualifiedName name = new QualifiedName(PreferenceUtils.pageId, jarName);
			Object val = ((IJavaElement) parentItem).getResource()
					.getPersistentProperty(name);
			if (val == null)
				return false;
			if ("true".equals(val))
				return true;
			return false;
		} catch (CoreException e) {
			logger.error("Could not get jar properties: \n"+jarName+"\n", e);
		}
		return false;
	}
	
	@SuppressWarnings("restriction")
	private void collectChildren(IParent parentItem, String filterId, IJavaElement selItem) {
		IJavaElement[] children;
		try {
			children = parentItem.getChildren();
			
		} catch (JavaModelException e) {
			// Trying to open a project with no java children or anything that strata can open
			//logger.error("Unexpected Exception", e);
			listOfErrorProjects .add(selItem);
			return;
		}
		
		for (IJavaElement ije : children) {
			boolean isSelected = selItem!=null && ije == selItem;
			// everything we care about in our dive-in (include packages is a parent) 
			if (!(ije instanceof IParent)) continue;
			
			// package - add
			if (ije instanceof IPackageFragment) {
				if (filterId == null || RJMapToId.getId(ije).startsWith(filterId))
					this.collect((IPackageFragment) ije, isSelected);
			}
			
			// package - don't recurse
			if (ije instanceof IPackageFragment) continue;
			
			// handle jars
			boolean isAJar = ije.getPath().toString().endsWith(".jar");
			if (parentItem instanceof org.eclipse.jdt.internal.core.JavaProject && isAJar && isJarSelected(ije.getElementName(), parentItem))
				if (ije instanceof org.eclipse.jdt.internal.core.JarPackageFragmentRoot)
					collectChildren((IParent)ije, filterId, null);
			
			// source PFR - don't recurse
			if (!isSourcePFR(ije)) continue;

			collectChildren((IParent) ije, filterId, null);

		}
	}
	
	public void collectMultiType(List<?> selList) {
		if (selList == null) return;

		for (Object selItem : selList) {
			collectMultiType(selItem);
		}

		IPreferenceStore prefStore = AtxaIntroPlugin.getDefault().getPreferenceStore();
		boolean hideDlgPref = prefStore.getBoolean(PreferenceConstants.SHOW_DLG_WHEN_OPENING_NON_JAVA_IN_STRATA);
		if (!hideDlgPref && listOfErrorProjects.size() > 0 && selList.size() > listOfErrorProjects.size()) {
			UIUtils.promptDialogWithPreferenceCheckBox(MessageDialog.INFORMATION, 
				"Problem with Selection", 
				"Your selection contains some non-java files. They will not be shown in the diagram.", 
				new String[] {"OK"},
				"Do Not Show This Again",
				PreferenceConstants.SHOW_DLG_WHEN_OPENING_NON_JAVA_IN_STRATA,
				prefStore).open();
		}
		
	}
	private Set<Resource> adaptable = new HashSet<Resource>();
	
	@SuppressWarnings("restriction")
	private void collectMultiType(Object selItem) {
		if (selItem instanceof IJavaProject || selItem instanceof IPackageFragmentRoot) {
			nameToAFMap.clear(); // adding a new project - clear the old hierarchy map (we could also temporarily clear)
			collectChildren((IParent)selItem, null, (IJavaElement) selItem);
		} else if (selItem instanceof IPackageFragment && this.hierarchical) {
			String filterId = RJMapToId.getId((IPackageFragment)selItem);
			ndxOfSelPckg = filterId.length();
			collectChildren((IParent) ((IJavaElement)selItem).getParent(), filterId, (IJavaElement)selItem);
		} else if (selItem instanceof IJavaElement) {
			if (selItem instanceof org.eclipse.jdt.internal.core.PackageFragment && this.hierarchical) {
				String filterId = RJMapToId.getId((IPackageFragment)selItem);
				collectChildren((IParent) ((IJavaElement)selItem).getParent(), filterId, (IJavaElement)selItem);
			} else 
				collect((IJavaElement)selItem, true);
		} else if (selItem instanceof Resource)
			collect((Resource) selItem, true);
		else if (selItem instanceof Artifact) {
			IJavaElement ije = RJCore.resourceToJDTElement(strataDoc.getRepo(), ((Artifact) selItem).elementRes);
			if (ije != null)
				collect(ije, true);
			// We now use the IJE infrastructure to collect all items so the
			// hierarchy is build properly when opening from various places
			// collect(((Artifact) selItem).elementRes);
		} else if (selItem instanceof ArtifactFragment) {
			// collect((ArtifactFragment) selItem);
			IJavaElement ije = RJCore.resourceToJDTElement(strataDoc.getRepo(), ((ArtifactFragment) selItem).getArt().elementRes);
			if (ije != null)
				collect(ije, true);
			else
				collectAllFromAF((ArtifactFragment) selItem);
		} else if (selItem instanceof IAdaptable) {
			Artifact art = (Artifact) ((IAdaptable) selItem).getAdapter(Artifact.class);
			
	        boolean isValidType = ErrorUtils.isValidType(new ArtifactFragment(art), strataDoc.getRepo(), errorShown);
	        if (!isValidType) {
	        	errorShown = !isValidType;
	        	return;
	        }
	        
			adaptable.add(art.elementRes);
			if (art != null){
				collectMultiType(art);
				return;
			}
		} else {
			logger.error("Don't know what to do with: " + selItem.getClass());
			return;
		}
	}
		
	public void removeSingleChildArtFrags() {
		int changed = 0;
		for (ArtifactFragment artFrag : new ArrayList<ArtifactFragment>(artFrags)) {
			changed += removeSingleChildArtFrags(artFrag);
		}
		// recurse again if anything was removed to pick up new singleChildArtFrags
		if (changed > 0) removeSingleChildArtFrags();
	}
	public int removeSingleChildArtFrags(ArtifactFragment af) {
		List<ArtifactFragment> childrenAF = af.getShownChildren();
		int changed = 0;
		
		// set hasChildren to false only if we have a JavaElement that is a package with resources
		boolean hasJavaChildren = false;
		try {
			IJavaElement ije = mapRDFtoJElement.get(af.getArt().elementRes);
			// do not delete adapted EPs (opened from other diagrams) 
			if (adaptable.contains(af.getArt().elementRes))
				hasJavaChildren = true;
			else if (ije == null)
				hasJavaChildren = false;
			else if (ije instanceof IPackageFragment )
				hasJavaChildren = ((IPackageFragment)ije).containsJavaResources();
			// do not remove classes 
			else
				hasJavaChildren=true;
		} catch (JavaModelException e) {
			logger.error("Unexpected Exception.", e);
		}
		// remove af if it has 0 children and is a package with no resources(classes) or if it only has a single child
		if ((childrenAF.size() < 1  && !hasJavaChildren ) || childrenAF.size() == 1 ){
			removeAF(af);
			changed++;
		}
		// go into children
		for (ArtifactFragment childAF : new ArrayList<ArtifactFragment>(af.getShownChildren())) {
			changed += removeSingleChildArtFrags(childAF);
		}
		return changed;
	}

	private void removeAF(ArtifactFragment af) {
		ArtifactFragment parentAF = af.getParentArt();
		
		if (parentAF == null) {
			artFrags.remove(af);
			List<ArtifactFragment> childrenToMove = new ArrayList<ArtifactFragment>(af.getShownChildren());
			af.removeShownChildren(childrenToMove);
			artFrags.addAll(childrenToMove);
		} else {
			parentAF.removeShownChild(af);
			parentAF.appendShownChildren(new ArrayList<ArtifactFragment>(af.getShownChildren()));
		}
	}

	public List<ArtifactFragment> getArtFragList() {
		return artFrags;
	}

	public boolean isEmpty() {
		return artFrags.isEmpty();
	}

	private void addToHierarchy(ArtifactFragment af, boolean isSelected) {
		String afName = RJMapToId.getId(af.getArt().elementRes);
		ArtifactFragment parentAF = null ;
		ArtifactFragment parentPackageAF = null ;
		
		nameToAFMap.put(afName, af);
		parentAF = getParentPackageFolder(afName);
		parentPackageAF = nameToAFMap.get(RJMapToId.getParentPackage(afName));
		
		if (parentAF != null) {
			parentAF.appendShownChild(af);
		}
		else if (parentPackageAF!=null) {
			// case when given afs and do not need a package folder
			parentPackageAF.appendShownChild(af);
			ClosedContainerDPolicy.setAllParentsShowingChildren(parentPackageAF, true);
			
		} else
			artFrags.add(af);
		if (isSelected) selectedAFs.add(af);
	}

	@SuppressWarnings("restriction")
	private static boolean isSourcePFR(IJavaElement ije) {
		// return true if we want to go inside it (not jars)
		
		// the cases:
		//  org.eclipse.jdt.internal.core.PackageFragmentRoot
		//  |- [inh] org.eclipse.jdt.internal.core.ExternalPackageFragmentRoot
		//  |- [inh] org.eclipse.jdt.internal.core.JarPackageFragmentRoot
		
		if (ije.getClass().equals(org.eclipse.jdt.internal.core.JarPackageFragmentRoot.class))
			return false;
		
		return true;
	}

	private ArtifactFragment getParentPackageFolder(String tgtAFName) {
		// if its a Package Folder then we need the parents parent (not a for a.*)
		if (RJMapToId.isPckgID(tgtAFName)) {
			tgtAFName = RJMapToId.getParentPackage(RJMapToId.getParentPackage(tgtAFName));
		}
		return nameToAFMap.get(RJMapToId.pckgIDToPckgFldrID(tgtAFName));	
	}
	
	private static boolean flatPackages(IWorkbenchWindow activeWorkbenchWindow) {

		// Disabled for now since we have multiple views that could cause problems here. 
		// DEFUALT ot opening contents of a package. 

//		boolean flat = true;
//		IWorkbenchPart activePart = activeWorkbenchWindow.getPartService().getActivePart();
//		
//		if (activePart instanceof org.eclipse.jdt.internal.ui.packageview.PackageExplorerPart)
//			flat = ((org.eclipse.jdt.internal.ui.packageview.PackageExplorerPart)activePart).isFlatLayout();
//		return flat;
		return false;
	}

	public List<ArtifactFragment> getSelectedAFs() {
		return selectedAFs;
	}

}