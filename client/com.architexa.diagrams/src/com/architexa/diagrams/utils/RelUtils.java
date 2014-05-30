package com.architexa.diagrams.utils;

import org.apache.log4j.Logger;

import com.architexa.diagrams.Activator;
import com.architexa.diagrams.model.ArtifactFragment;
import com.architexa.diagrams.model.ArtifactRel;

public class RelUtils {
	static final Logger logger = Activator.getLogger(RelUtils.class);
	
	public static void addModelSourceConnections(ArtifactFragment srcArtFrag, ArtifactRel conn) {
	    if (srcArtFrag.sourceConnectionsContains(conn)) {
	    	logger.error("ERR: addModelSourceConnections called when relation already exists", new Exception());
	    }
	    srcArtFrag.addSourceConnection(conn);
	}

	public static void removeModelSourceConnections(ArtifactFragment srcArtFrag, ArtifactRel conn) {
        if (!srcArtFrag.sourceConnectionsContains(conn)) {
	    	logger.error("ERR: removeModelSourceConnections called but relation does not exist", new Exception());
        	return;
        }

        srcArtFrag.removeSourceConnection(conn);
    }

	public static void addModelTargetConnections(ArtifactFragment tgtArtFrag, ArtifactRel conn) {
	    if (tgtArtFrag.targetConnectionsContains(conn)) {
	    	logger.error("ERR: addModelTargetConnections called when relation already exists", new Exception());
	    }
	    tgtArtFrag.addTargetConnection(conn);
	}
	
    public static void removeModelTargetConnections(ArtifactFragment tgtArtFrag, ArtifactRel conn) {
        if (!tgtArtFrag.targetConnectionsContains(conn)) {
	    	logger.error("ERR: removeModelTargetConnections called but relation does not exist", new Exception());
        }
        tgtArtFrag.removeTargetConnection(conn);
    }
}
