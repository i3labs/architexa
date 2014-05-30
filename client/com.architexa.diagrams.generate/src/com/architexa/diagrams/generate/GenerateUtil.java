package com.architexa.diagrams.generate;

import java.util.List;

import org.openrdf.model.Resource;

import com.architexa.diagrams.jdt.model.CodeUnit;
import com.architexa.diagrams.parts.PointPositionedDiagramPolicy;

/**
 * 
 * @author Elizabeth L. Murnane
 *
 */
public class GenerateUtil {

	/**
	 * Returns the component in the diagram that corresponds to the given Resource or 
	 * creates a new code unit component for the Resource if a representation for it 
	 * doesn't already exist
	 */
	public static CodeUnit getCodeUnitForRes(Resource res, String instanceName, List<CodeUnit> existingCodeUnits, CodeUnit parentUnit) {
		for(CodeUnit existingCodeUnit : existingCodeUnits) {
			boolean sameResource = res.equals(existingCodeUnit.getArt().elementRes);
			boolean sameInstanceName = (instanceName==null && existingCodeUnit.getInstanceName()==null) || (instanceName!=null && instanceName.equals(existingCodeUnit.getInstanceName()));
			if(sameResource && sameInstanceName) return existingCodeUnit;
		}

		CodeUnit newCodeUnit = new CodeUnit(res);
		newCodeUnit.setInstanceName(instanceName);
		CodeUnit.ensureInstalledPolicy(newCodeUnit, PointPositionedDiagramPolicy.DefaultKey, PointPositionedDiagramPolicy.class);
		existingCodeUnits.add(newCodeUnit);
		if(parentUnit!=null) parentUnit.appendShownChild(newCodeUnit);
		return newCodeUnit;
	}

}
