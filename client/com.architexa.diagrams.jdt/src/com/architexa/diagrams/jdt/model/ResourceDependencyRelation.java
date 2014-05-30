package com.architexa.diagrams.jdt.model;

import org.openrdf.model.Resource;
import org.openrdf.model.URI;

import com.architexa.store.ReloRdfRepository;
import com.architexa.store.StoreUtil;

// Dependency Relation between two RESOURCES. Used for calculating/aggregating dependencies when opening a strata diagram
public class ResourceDependencyRelation {
    
    public static boolean isSrc(Resource me, Resource other) {
        // it doesn't really matter how this works as long as it is consistent
        return (me.toString().compareTo(other.toString()) < 0);
    }
    public static boolean isTgt(Resource me, Resource other) {
        return !isSrc(me, other);
    }

    public static ResourceDependencyRelation getDepRel(Resource af1, Resource af2, int depCnt, int revDepCnt) {
    	if (isSrc(af1, af2))
    		return new ResourceDependencyRelation(af1, af2, depCnt, revDepCnt);
    	else
    		return new ResourceDependencyRelation(af2, af1, revDepCnt, depCnt);
    }
    
    private Resource srcRes;
	private Resource dstRes;

	public URI relationRes;
	private int depCnt;
    private int revDepCnt;
	public boolean pinned;

    private final static String reloRdfNamespace = ReloRdfRepository.atxaRdfNamespace;
    public static final URI createReloURI(String str) {
    	return StoreUtil.createMemURI(reloRdfNamespace + str);
    }
    public static final URI depRel = createReloURI("grouped#depRel");

	public ResourceDependencyRelation() {}
	
	public ResourceDependencyRelation(Resource src, Resource dst, int depCnt, int revDepCnt) {
		setSrcRes(src);
		setDstRes(dst);
		relationRes = depRel;
        this.setDepCnt(depCnt);
        this.setRevDepCnt(revDepCnt);
	}
    
    public boolean biDirectional() {
        return (getRevDepCnt() != 0 && getDepCnt() != 0);
    }
	
	@Override
    public int hashCode() {
		int retVal = getSrcRes().hashCode();
		retVal *= 2;
		retVal += getDstRes().hashCode();
        retVal *= 2;
        retVal += getDepCnt();
        retVal *= 2;
        retVal += getRevDepCnt();
		return retVal;
	}
    @Override
	public boolean equals(Object arg0) {
		if (!(arg0 instanceof ResourceDependencyRelation)) {
			return false;
		}
		ResourceDependencyRelation rhs = (ResourceDependencyRelation)arg0;
		if (!this.getSrcRes().equals(rhs.getSrcRes())) return false;
		if (!this.getDstRes().equals(rhs.getDstRes())) return false;
        // we do not care about comparing Res strengths for the purpose of aggregating deps when building the diagram
		//if (this.getDepCnt() != rhs.getDepCnt()) return false;
        //if (this.getRevDepCnt() != rhs.getRevDepCnt()) return false;
		return true;
	}
	public void setSrcRes(Resource srcArt) {
		this.srcRes = srcArt;
	}
	public Resource getSrcRes() {
		return srcRes;
	}
	public void setDstRes(Resource dstArt) {
		this.dstRes = dstArt;
	}
	public Resource getDstRes() {
		return dstRes;
	}
	public void setDepCnt(int depCnt) {
		this.depCnt = depCnt;
	}
	public int getDepCnt() {
		return depCnt;
	}
	public void setRevDepCnt(int revDepCnt) {
		this.revDepCnt = revDepCnt;
	}
	public int getRevDepCnt() {
		return revDepCnt;
	}

}
