/**
 * Copyright (c) 2007-2008 Architexa, Inc
 */ 
package com.architexa.diagrams.strata.model;


import java.io.IOException;
import java.util.List;

import org.apache.log4j.Logger;
import org.openrdf.model.URI;
import org.openrdf.rio.RdfDocumentWriter;

import com.architexa.diagrams.jdt.model.CodeUnit;
import com.architexa.diagrams.model.ArtifactFragment;
import com.architexa.diagrams.model.ArtifactRel;
import com.architexa.diagrams.strata.StrataPlugin;
import com.architexa.store.ReloRdfRepository;
import com.architexa.store.StoreUtil;


public class DependencyRelation extends ArtifactRel {
    public static final Logger logger = StrataPlugin.getLogger(DependencyRelation.class);

    //////////////
    // support for undirected relations with two arrows
    //////////////
    
    // these relations need to be directed, i.e. we can't just make two
    // relations be identical one with the src and dst flipped. The reason is that
    // otherwise the last initialized nodes will overwrite the source or target
    // in the relation and all relations will point to themselves.
    
    public static boolean isSrc(ArtifactFragment me, ArtifactFragment other) {
        // it doesn't really matter how this works as long as it is consistent
        return (me.toString().compareTo(other.toString()) < 0);
    }
    public static boolean isTgt(ArtifactFragment me, ArtifactFragment other) {
        return !isSrc(me, other);
    }

	public int depCnt;
    public int revDepCnt;
	public boolean pinned;

    private final static String reloRdfNamespace = ReloRdfRepository.atxaRdfNamespace;
    public static final URI createReloURI(String str) {
    	return StoreUtil.createMemURI(reloRdfNamespace + str);
    }
    public static final URI depRel = createReloURI("grouped#depRel");

	public DependencyRelation() {}
	
	private DependencyRelation(ArtifactFragment src, ArtifactFragment dst, int depCnt, int revDepCnt) {
		super(src, dst, depRel);
        this.depCnt = depCnt;
        this.revDepCnt = revDepCnt;
	}
    
    public static DependencyRelation getDepRel(ArtifactFragment af1, ArtifactFragment af2, int depCnt, int revDepCnt) {
    	if (isSrc(af1, af2))
    		return new DependencyRelation(af1, af2, depCnt, revDepCnt);
    	else
    		return new DependencyRelation(af2, af1, revDepCnt, depCnt);
    }
    
    public boolean biDirectional() {
        return (revDepCnt != 0 && depCnt != 0);
    }
	
	public String getLabel(ReloRdfRepository repo) {
        ArtifactFragment lblSrc;
        ArtifactFragment lblDst;
        int lblDepCnt;
        int lblRevDepCnt;
        if (depCnt >= revDepCnt) {
            lblSrc = srcArt;
            lblDst = dstArt;
            lblDepCnt = depCnt;
            lblRevDepCnt = revDepCnt;
        } else {
            lblSrc = dstArt;
            lblDst = srcArt;
            lblDepCnt = revDepCnt;
            lblRevDepCnt = depCnt;
        }
        //assert lblDepCnt != 0;
        
        StringBuffer lbl = new StringBuffer();
        
        lbl.append(CodeUnit.getLabelWithContext(repo, lblSrc));

        lbl.append(" ");

        //if (lblRevDepCnt != 0) lbl.append("<");
        lbl.append("--");
        lbl.append(">");

        lbl.append(" ");
        
        lbl.append(CodeUnit.getLabelWithContext(repo, lblDst));

        lbl.append(" ");

        lbl.append("(");
        lbl.append(lblDepCnt);
        if (lblRevDepCnt != 0) {
            lbl.append("/rev:");
            lbl.append(lblRevDepCnt);
        }
        lbl.append(")");
        
        //return src.getLabel(repo) + " --> " + dst.getLabel(repo) + " (" + depCnt + "/" + revDepCnt + ")";
        return lbl.toString();
	}

	@Override
	public String toString() {
		return getLabel(null);
	}

	@Override
    public int hashCode() {
		int retVal = srcArt.hashCode();
		retVal *= 2;
		retVal += dstArt.hashCode();
        retVal *= 2;
        retVal += depCnt;
        retVal *= 2;
        retVal += revDepCnt;
		return retVal;
	}
    @Override
	public boolean equals(Object arg0) {
		if (!(arg0 instanceof DependencyRelation)) {
			return false;
		}
		DependencyRelation rhs = (DependencyRelation)arg0;
		if (!this.srcArt.equals(rhs.srcArt)) return false;
		if (!this.dstArt.equals(rhs.dstArt)) return false;
        if (this.depCnt != rhs.depCnt) return false;
        if (this.revDepCnt != rhs.revDepCnt) return false;
		return true;
	}

    
    
    
    
	// Do not need to save/load deps, they will be recalculated on load anyway.
	// Saving can result in file sizes that are too large
    @Override
	public void writeRDF(RdfDocumentWriter rdfWriter, List<ArtifactRel> savedRels) throws IOException {
    }

    @Override
    protected void readRDF(ReloRdfRepository queryRepo) {
    }
}
