package com.architexa.diagrams.strata.parts;

import java.util.Collections;
import java.util.List;


import com.architexa.diagrams.jdt.model.CodeUnit;
import com.architexa.diagrams.jdt.model.UserCreatedFragment;
import com.architexa.diagrams.model.ArtifactFragment;
import com.architexa.diagrams.parts.ArtifactRelModificationEditPolicy;
import com.architexa.diagrams.strata.cache.PartitionerSupport;
import com.architexa.diagrams.strata.model.EmbeddedFrag;
import com.architexa.diagrams.strata.model.policy.ClosedContainerDPolicy;
import com.architexa.diagrams.strata.model.policy.LayersDPolicy;
import com.architexa.org.eclipse.draw2d.IFigure;

/**
 * Basically a non-Layer has Title support and the artFrags need to have layers
 * built
 * 
 * @author vineet
 * 
 */
public abstract class TitledArtifactEditPart extends ContainableEditPart {

	@Override
	protected abstract IFigure createContainerFigure();

	@Override
	public List<?> getModelChildren() {
		try {
			ArtifactFragment artFrag = this.getArtFrag();
			if (!ClosedContainerDPolicy.isInstalled(artFrag)) {
				logger.error("TitledArtFrag is expected to have CloseContainedPolicy");
				return Collections.EMPTY_LIST;
			}
			if (!ClosedContainerDPolicy.isShowingChildren(artFrag)) return Collections.emptyList();
			
			PartitionerSupport.partitionLayers(this.getRootModel(), artFrag);

			return LayersDPolicy.getLayers(artFrag);
		} catch (Throwable t) {
			logger.error("Unexpected exception while getting children from dependency cache for: " + this.getModel(), t);
			return Collections.EMPTY_LIST;
		}
	}
	
	@Override
	protected void createEditPolicies() {
		super.createEditPolicies();
		installEditPolicy(ArtifactRelModificationEditPolicy.KEY, new ArtifactRelModificationEditPolicy());
	}
	
	protected String getCommonContextLabel() {
		String commonLbl = null;
		ArtifactFragment af = this.getArtFrag();
		List<ArtifactFragment> children = ClosedContainerDPolicy.getShownChildren(af);
		for (ArtifactFragment strataAF : children) {
			String doLabel;
			if (strataAF instanceof UserCreatedFragment)
				doLabel = ((UserCreatedFragment) strataAF).getLabel(getRepo());
			else
				doLabel = CodeUnit.getLabel(getRepo(), strataAF, null, true);
			//  doLabel = CodeUnit.getLabelWithContext(getRepo(), strataAF);
			if (strataAF instanceof EmbeddedFrag) continue;
			commonLbl = getCommon(commonLbl, doLabel);
		}
		
		return commonLbl;
	}

	private static String getCommon(String lhsLbl, String rhsLbl) {
		// if null we are beginning the scan so just return
		if (lhsLbl == null) return rhsLbl;
		if (rhsLbl == null) return lhsLbl;
		
		// if one of the sides is empty then there is no common parts, return the empty string 
		if (lhsLbl.equals("")) return lhsLbl; 
		if (rhsLbl.equals("")) return rhsLbl;
		
		
		lhsLbl = lhsLbl.trim();
		rhsLbl = rhsLbl.trim();
		int ndx = 0;
		int pndx = 0; //want the last period
		while (ndx < lhsLbl.length() && ndx < rhsLbl.length()) {
			if (lhsLbl.charAt(ndx) != rhsLbl.charAt(ndx)) break;
			if(lhsLbl.charAt(ndx) == '.') 
				pndx = ndx;
			ndx++;
		}

		// if returning everything - go ahead
		// if (ndx == lhsLbl.length() || ndx == rhsLbl.length()) return lhsLbl.substring(0, ndx);
		
		// need to check if we are breaking the context between packages. We
		// dont want lapis.abc and lapis.ab to have lapis.ab in common while we
		// do want lapis.ab and lapis.ab.c to have lapis.ab in common
		if ( (ndx == lhsLbl.length() && rhsLbl.length()>ndx && rhsLbl.charAt(ndx) == '.') ) 
			return lhsLbl.substring(0, ndx);
		if ( (ndx == rhsLbl.length() && lhsLbl.length()>ndx && lhsLbl.charAt(ndx) == '.') )
			return lhsLbl.substring(0, ndx);

		// we don't want to clip a word
		
		// if returning a period go ahead
		if (lhsLbl.length() > pndx ) {
			if (lhsLbl.charAt(pndx) == '.') return lhsLbl.substring(0, pndx); 
			// return pndx not ndx otherwise we may get half a package
		} else
			return "";
		if (lhsLbl.charAt(ndx) == '.') return lhsLbl.substring(0, ndx);
		// Why was this extra test needed? Was causing problems where bar.Foo was added to something with context bar.F  (label ended up as 'oo')
		// if (lhsLbl.charAt(ndx) == '.' || lhsLbl.charAt(ndx) == ' ') return lhsLbl.substring(0, ndx);
		
		return lhsLbl.substring(0, pndx);
	}
	
}
