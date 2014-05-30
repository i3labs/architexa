package com.architexa.diagrams.jdt.model;

import java.io.IOException;

import org.eclipse.swt.graphics.Image;
import org.openrdf.model.Resource;
import org.openrdf.rio.RdfDocumentWriter;

import com.architexa.diagrams.RSECore;
import com.architexa.diagrams.jdt.RJCore;
import com.architexa.diagrams.model.Artifact;
import com.architexa.diagrams.model.ArtifactFragment;
import com.architexa.diagrams.model.BrowseModel;
import com.architexa.diagrams.model.ColorDPolicy;
import com.architexa.diagrams.parts.PointPositionedDiagramPolicy;
import com.architexa.store.ReloRdfRepository;
import com.architexa.store.StoreUtil;

/**
 * 
 * Represents items created by the user via the palette
 *
 */
public class UserCreatedFragment extends CodeUnit {

	// Store the parent frag since there's no
	// corresponding code to determine the parent from
	private ArtifactFragment enclosingFrag = null;
	public UserCreatedFragment(BrowseModel bm, String fragName) {
		super(StoreUtil.createBNode());
		this.setInstanceName(fragName);
		createResource(this, bm, fragName);
		ArtifactFragment.ensureInstalledPolicy(this, PointPositionedDiagramPolicy.DefaultKey, PointPositionedDiagramPolicy.class);
		ArtifactFragment.ensureInstalledPolicy(this, ColorDPolicy.DefaultKey, ColorDPolicy.class);
	}

	public UserCreatedFragment(ArtifactFragment af) {
		super(af);
		String resString = af.getArt().elementRes.toString();
		if (resString.contains("#"))
			setInstanceName(resString.substring(resString.indexOf("#")+1));
	}

	public static Resource createResource(CodeUnit userCreatedFrag, BrowseModel bm, String fragName) {

		// Create an rdf Resource for this frag and replace the empty BNode
		Resource res = StoreUtil.createBNode();
		userCreatedFrag.setArt(new Artifact(res));

		ReloRdfRepository repo = bm.getRepo();
		repo.startTransaction();
		// Add a stmt indicating that this is user created
		repo.addStatement(res, RSECore.userCreated, StoreUtil.createMemLiteral("true"));
		// Make it look public by default
		repo.addStatement(res, RJCore.access, RJCore.publicAccess);
		repo.commitTransaction();

		return res;
	}

	@Override
	public void writeRDFNode(RdfDocumentWriter rdfWriter, Resource parentInstance)
			throws IOException {
		super.writeRDFNode(rdfWriter, parentInstance);
		rdfWriter.writeStatement(getArt().elementRes,  RSECore.userCreated, StoreUtil.createMemLiteral("true"));
		rdfWriter.writeStatement(getArt().elementRes,  RSECore.userCreatedNameText, StoreUtil.createMemLiteral(getInstanceName()));
		rdfWriter.writeStatement(getArt().elementRes, RJCore.access, RJCore.publicAccess);
		rdfWriter.writeStatement(getArt().elementRes, StoreUtil.getDefaultStoreRepository().rdfType, queryType(null));
	}
	
	
	@Override
	public String getLabel(ReloRdfRepository repo, Artifact contextArt) {
		// TODO Auto-generated method stub
		return getLabelName();
	}
	
	private String getLabelName() {
		String name = getInstanceName();
		Resource type = queryType(null);
		if (RJCore.packageType.equals(type))
			return name.substring(name.lastIndexOf("#") + 1);
		else if (RJCore.classType.equals(type))
				return name.substring(name.lastIndexOf("$") + 1);
		else if (RJCore.methodType.equals(type) || RJCore.fieldType.equals(type))
				return name.substring(name.lastIndexOf(".") + 1);	
		return name;
	}

	@Override
	public Image getIcon(ReloRdfRepository repo) {
		return getIcon(repo, this.getArt(), queryType(repo));
	}
	
	@Override
	public Resource queryType(ReloRdfRepository repo) {
		String resourceStr = getInstanceName();
		if (resourceStr == null) {
			try {
			return super.queryType(repo);
			} catch (Exception e) {
				logger.info("Could not find name for element: " + this.getArt().elementRes + "\n" + e);
				return null;
			}
		}
		if (!resourceStr.contains("$")) {
			return RJCore.packageType;
    	}

		int memberSeperatorNdx = resourceStr.indexOf(".", resourceStr.indexOf("$"));
		if (memberSeperatorNdx>0) {
			if (resourceStr.indexOf("(",memberSeperatorNdx)>0)
				return RJCore.methodType;
			else
				return RJCore.fieldType;
		} else
			return RJCore.classType;
	}
	
	@Override
	public void setEnclosingFrag(ArtifactFragment parent) {
		this.enclosingFrag = parent;
	}
	
	public ArtifactFragment getEnclosingParentFrag() {
		return enclosingFrag;
	}

	@Override
	public void setInstanceName(String name) {
//		System.err.println(name);
		super.setInstanceName(processName(name));
	}

	// We save complete name in saved file as well as when a new object is created newPackage$NewClass.newMethod()
	// So when a name is changed using direct edit we have to make sure to change the correct value
	private String processName(String name) {
		String currentName = getInstanceName();
		if (currentName == null)
			return name;
		Resource type = queryType(null);
		if (RJCore.classType.equals(type))
			return currentName.substring(0, currentName.lastIndexOf("$")+1) + name;
		if (RJCore.methodType.equals(type) || RJCore.fieldType.equals(type))
			return currentName.substring(0, currentName.lastIndexOf(".")+1) + name;
		return name;
	}

	public String getFullName() {
		if (getInstanceName() != null)
			return RJCore.atxaRdfNamespace + RJCore.jdtWkspcNS + getInstanceName();
		return this.getArt().elementRes.toString();
	}

}
