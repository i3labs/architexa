package com.architexa.diagrams.jdt.utils;

import java.util.Iterator;

import org.apache.log4j.Logger;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;

import com.architexa.diagrams.RSECore;
import com.architexa.diagrams.jdt.Activator;
import com.architexa.diagrams.jdt.RJCore;
import com.architexa.diagrams.model.Artifact;
import com.architexa.store.ReloRdfRepository;

public class MethodParametersSupport {
    static final Logger logger = Activator.getLogger(MethodParametersSupport.class);

	public static final URI parameterCachedLabel = RSECore.createRseUri("jdt#parameterCachedLabel");

    public static String getInOutSig(Artifact art, ReloRdfRepository repo, int detailLvl) {
    	if(RSECore.isUserCreated(repo, art.elementRes))
    		return "()";
    	if(detailLvl == 2)
    		return "(...)";
        //return getInOutSig_Calc(art, repo);
    	String cachedInOutSig = getInOutSig_Cached(art, repo);
    	if (detailLvl == 1) {
    		String[] splitInOutSig = cachedInOutSig.split(":");
    		String params = splitInOutSig[0];
    		String retType = splitInOutSig[1];
    		retType = retType.substring(retType.lastIndexOf(".")+1);
    		cachedInOutSig = params + " : " + retType;
    	}
        return cachedInOutSig;
    }

    public static String getInOutSig_Cached(Artifact art, ReloRdfRepository repo) {
        return repo.getRequiredLiteral(art.elementRes, MethodParametersSupport.parameterCachedLabel);
    }

    public static String getInOutSig_Calc(Artifact art, ReloRdfRepository repo) {
        String retVal = "(";
        String methodSig = getParamString(art, repo);
        retVal += methodSig;
        retVal += "): ";
        try {
            Resource retType = (Resource) repo.getStatement(art.elementRes, RJCore.returnType, null).getObject();
            retVal += repo.queryName(retType);
        } catch (Exception e) {
            retVal += "void";
            logger.error("Unexpected Exception", e);
        }
        return retVal;
    }
    
    public static String getParamString(Artifact art, ReloRdfRepository repo) {
        String methodSig = "";
        try {
            Resource paramRDFListRes = (Resource) repo.getStatement(art.elementRes, RJCore.parameter, null)
                                                .getObject();
            if(paramRDFListRes==null) return methodSig;
            Iterator<Value> paramIt = repo.getListIterator(paramRDFListRes);
            boolean first = true;
            while (paramIt.hasNext()) {
                if (first) first = false; else methodSig += ",";
                Resource paramTypeRes = (Resource) paramIt.next();
                methodSig += repo.queryName(paramTypeRes);
            }
        } catch (Exception e) {
            methodSig = "...";
            logger.error("Unexpected Exception", e);
        }
        return methodSig;
    }
}
