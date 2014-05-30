/**
 * 
 */
package com.architexa.diagrams.model;

import java.io.IOException;

import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.rio.RdfDocumentWriter;

import com.architexa.diagrams.RSECore;
import com.architexa.diagrams.parts.PointPositionedDiagramPolicy;
import com.architexa.org.eclipse.draw2d.geometry.Point;
import com.architexa.store.ReloRdfRepository;
import com.architexa.store.StoreUtil;

/**
 * @author Abhishek Rakshit
 * 
 */
public class Comment extends ArtifactFragment {

	private Point topLeft;
	private boolean isAnchored = false;
	private Point relDistance;
	private Point relDistFromDiagTopLeft;

	public Comment() {
		super(StoreUtil.createBNode());
	}

	public Comment(Point topLeft) {
		this();
		this.topLeft = topLeft;
	}

	public Point getTopLeft() {
		return topLeft;
	}

	public void setTopLeft(Point topLeft) {
		this.topLeft = topLeft;
	}

	@Override
	public Resource queryType(ReloRdfRepository repo) {
		return RSECore.commentType;
	}

	protected static final URI createReloURI(String str) {
		return StoreUtil.createMemURI(ReloRdfRepository.atxaRdfNamespace + str);
	}

	public static final URI commentTxt = createReloURI("core#commentTxt");
	public static final URI anchoredComment = createReloURI("core#anchoredComment");

	public static final String defaultComment = "<<Comment Text>>";

	private String text = defaultComment;
	private String oldText = text;

	public void setAnnoLabelText(String text) {
		this.text = text;
		this.firePropChang("comment");
	}

	public String getAnnoLabelText() {
		return text;
	}

	public void setOldAnnoLabelText(String oldText) {
		this.oldText = oldText;
	}

	public String getOldAnnoLabelText() {
		return oldText;
	}

	@Override
	public void writeRDFNode(RdfDocumentWriter rdfWriter,
			Resource parentInstance) throws IOException {
		super.writeRDFNode(rdfWriter, parentInstance);

		rdfWriter.writeStatement(getInstanceRes(), commentTxt, StoreUtil
				.createMemLiteral(getAnnoLabelText()));
		rdfWriter.writeStatement(getArt().elementRes,
				getRootArt().getRepo().rdfType, RSECore.commentType);
		if (isAnchored())
			rdfWriter.writeStatement(getInstanceRes(), anchoredComment, StoreUtil
					.createMemLiteral("isAnchored"));
	}

	@Override
	public void readRDFNode(ReloRdfRepository queryRepo) {
		// need to write basic data before we reposition and initialize figure
		// (in base class)
		setAnnoLabelText(queryRepo.getStatement(getInstanceRes(), commentTxt,null).getObject().toString());
		// logger.debug("After setText: " + comment.getText());

		super.readRDFNode(queryRepo);
	}

	public static void initComment(Comment reqObj, Point relDist) {
		ArtifactFragment.ensureInstalledPolicy(reqObj, PointPositionedDiagramPolicy.DefaultKey, PointPositionedDiagramPolicy.class);
		reqObj.setRelDistFromDiagTopLeft(relDist);
	}
	
	public static void initComment(Comment reqObj) {
		ArtifactFragment.ensureInstalledPolicy(reqObj, PointPositionedDiagramPolicy.DefaultKey, PointPositionedDiagramPolicy.class);
	}

	public void setAnchored(boolean isAnchored) {
		this.isAnchored = isAnchored;
	}

	public boolean isAnchored() {
		return isAnchored;
	}

	public void setRelDistance(Point relDistance) {
		this.relDistance = relDistance;
	}

	public Point getRelDistance() {
		return relDistance;
	}

	public void setRelDistFromDiagTopLeft(Point relDistFromDiagTopLeft) {
		this.relDistFromDiagTopLeft = relDistFromDiagTopLeft;
	}

	public Point getRelDistFromDiagTopLeft() {
		return relDistFromDiagTopLeft;
	}
}
