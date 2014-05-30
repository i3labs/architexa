package com.architexa.diagrams.chrono.util;

import java.util.ArrayList;
import java.util.List;


import com.architexa.diagrams.chrono.editparts.SeqNodeEditPart;
import com.architexa.diagrams.model.AnnotatedRel;
import com.architexa.diagrams.model.ArtifactFragment;
import com.architexa.diagrams.model.ArtifactRel;
import com.architexa.diagrams.parts.CommentEditPart;
import com.architexa.diagrams.parts.NamedRelationPart;
import com.architexa.diagrams.utils.RelUtils;
import com.architexa.org.eclipse.gef.editparts.AbstractConnectionEditPart;

public class SeqRelUtils {

	public static void addRels(List<ArtifactRel> removedRels) {
		for (ArtifactRel rel : removedRels) {
			ArtifactFragment srcAF = rel.getSrc(); 
	    	ArtifactFragment tgtAF = rel.getDest();
	    	rel.init(srcAF, tgtAF, rel.relationRes);
	    	rel.connect(srcAF, tgtAF);
		}
	}
	
	public static List<ArtifactRel> removeChildAnnotatedRels(ArtifactFragment child) {
		List<ArtifactRel> rels = child.getSourceConnections();
		List<ArtifactRel> removedRels = new ArrayList<ArtifactRel>();
		rels.addAll(child.getTargetConnections());
		for (ArtifactRel rel : new ArrayList<ArtifactRel>(rels)) {
			if (rel instanceof AnnotatedRel) {
				
				removedRels.add(rel);
				RelUtils.removeModelSourceConnections(rel.getSrc(), rel);
				RelUtils.removeModelTargetConnections(rel.getDest(), rel);
			}
		}
		return removedRels;
	}

	public static List<CommentEditPart> getAnchoredComments(SeqNodeEditPart child) {
		List<AbstractConnectionEditPart> rels = child.getSourceConnections();
		List<CommentEditPart> anchoredComments = new ArrayList<CommentEditPart>();
		for (AbstractConnectionEditPart rel : rels) {
			if (rel instanceof NamedRelationPart && rel.getTarget() instanceof CommentEditPart)
				anchoredComments.add((CommentEditPart) rel.getTarget());
		}
		rels = child.getTargetConnections();
		for (AbstractConnectionEditPart rel : rels) {
			if (rel instanceof NamedRelationPart && rel.getSource() instanceof CommentEditPart)
				anchoredComments.add((CommentEditPart) rel.getSource());
		}

		return anchoredComments;
	}

}
