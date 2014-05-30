package com.architexa.diagrams.parts;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.rio.RdfDocumentWriter;
import org.openrdf.sesame.sail.StatementIterator;

import com.architexa.diagrams.Activator;
import com.architexa.diagrams.RSECore;
import com.architexa.diagrams.model.ArtifactRel;
import com.architexa.diagrams.model.ConnectionBendpoint;
import com.architexa.diagrams.model.DiagramPolicy;
import com.architexa.org.eclipse.draw2d.Bendpoint;
import com.architexa.org.eclipse.draw2d.geometry.Point;
import com.architexa.store.ReloRdfRepository;
import com.architexa.store.StoreUtil;

public class ConnectionBendpointDPolicy extends DiagramPolicy{

	static final Logger logger = Activator.getLogger(ConnectionBendpointDPolicy.class);
	// basic setup
	////
	public static final String DefaultKey = "ConnectionBendpointDiagramPolicy";
	public static final ConnectionBendpointDPolicy Type = new ConnectionBendpointDPolicy();
	private boolean isModified = false;
	
	////
	// Policy Fields, Constructors and Methods 
	////
	public static final URI bendpointURI = RSECore.createRseUri("core#bendpoint");
	public static final URI BPLocURI = RSECore.createRseUri("core#bpLocation");
	public static final URI bpIndexURI = RSECore.createRseUri("core#bpIndex");
	
	private List<Bendpoint> bpList = new ArrayList<Bendpoint>();
	
	public static void addBendpoint(ArtifactRel rel, Bendpoint bp, int index, boolean fireEvent) {
		ConnectionBendpointDPolicy pol = rel.getTypedDiagramPolicy(Type, DefaultKey);
		if (pol != null)
			pol.addBendpoint(index, bp, fireEvent);
	}
	
	public static void removeBendpoint(ArtifactRel rel, int index) {
		ConnectionBendpointDPolicy pol = rel.getTypedDiagramPolicy(Type, DefaultKey);
		if (pol != null)
			pol.removeBendpoint(index, true);
	}
	
	private void removeBendpoint(int index, boolean fireEvent) {
		if (getBpList().size() > index) {
			getBpList().remove(index);
			if (fireEvent)
				getHostRel().firePropertyChange(DefaultKey, null, null);
		}
	}
	
	public static void clearBendPoints(ArtifactRel rel, boolean fireEvent) {
		ConnectionBendpointDPolicy pol = rel.getTypedDiagramPolicy(Type, DefaultKey);
		if (pol != null)
			pol.clearBendPoints(fireEvent);
	}
	
	private void clearBendPoints(boolean fireEvent) {
		bpList.clear();
		if (fireEvent)
			getHostRel().firePropertyChange(DefaultKey, null, null);
	}

	public static Bendpoint getBendpoint(ArtifactRel rel, int index) {
		ConnectionBendpointDPolicy pol = rel.getTypedDiagramPolicy(Type, DefaultKey);
		if (pol != null)
			return pol.getBendpoint(index);
		return null;
	}

	private Bendpoint getBendpoint(int index) {
		if (getBpList().size() > index)
			return getBpList().get(index);
		return null;
	}

	private void addBendpoint(int index, Bendpoint bp, boolean fireEvent) {
		for (Bendpoint savedBp : getBpList()) {
			if (savedBp.getLocation().x == bp.getLocation().x &&
					savedBp.getLocation().y == bp.getLocation().y)
				return;
		}
		if (getBpList().size() >= index)
			getBpList().add(index, bp);
		else
			getBpList().add(bp);
		if (fireEvent)
			getHostRel().firePropertyChange(DefaultKey, null, null);
	}
	
	public static List<Bendpoint> getBendpoints(ArtifactRel rel) {
		ConnectionBendpointDPolicy pol = rel.getTypedDiagramPolicy(Type, DefaultKey);
		if (pol != null)
			return new ArrayList<Bendpoint>(pol.getBpList());
		return new ArrayList<Bendpoint>();
	}

	private List<Bendpoint> getBpList() {
		return bpList;
	}
	
	@Override
	public void writeRDF(RdfDocumentWriter rdfWriter) throws IOException {
		if (bpList.isEmpty()) return;
		for (int i = 0; i < bpList.size(); i++) {
			ConnectionBendpoint cbp = (ConnectionBendpoint)bpList.get(i); 
			Resource bpRes = cbp.getInstanceRes();
			rdfWriter.writeStatement(getHostRel().getInstanceRes(), bendpointURI, bpRes);
			rdfWriter.writeStatement(bpRes, BPLocURI, StoreUtil.createMemLiteral(Integer.toString(cbp.getLocation().x) + "," + Integer.toString(cbp.getLocation().y)));
			rdfWriter.writeStatement(bpRes, bpIndexURI, StoreUtil.createMemLiteral(Integer.toString(i)));
		}
	}
	
	@Override
	public void readRDF(ReloRdfRepository queryRepo) {
		try {
			StatementIterator bpIter = queryRepo.getStatements(getHostRel().getInstanceRes(), bendpointURI, null);
			while (bpIter.hasNext()) {
				Resource bpRes = (Resource) bpIter.next().getObject();
				if (bpRes == null) continue;
				Value fDim = queryRepo.getStatement(bpRes, BPLocURI, null).getObject();
				Value bpIndex = queryRepo.getStatement(bpRes, bpIndexURI, null).getObject();
				
				int index = Integer.parseInt(((Literal)bpIndex).getLabel());
				ConnectionBendpoint cbp = new ConnectionBendpoint(getSavedLocation(((Literal)fDim).getLabel()));
				addBendpoint(index, cbp, true);
			}
		} catch (Exception e) {
			logger.error("Error adding bend points" + e);
		}
	}

	private Point getSavedLocation(String str) {
		String[] arr = str.split(",");
		return new Point(Integer.parseInt(arr[0]), Integer.parseInt(arr[1])); 
	}

	public static void removeBendpoint(ArtifactRel rel, int index, boolean fireEvent) {
		ConnectionBendpointDPolicy pol = rel.getTypedDiagramPolicy(Type, DefaultKey);
		if (pol != null)
			pol.removeBendpoint(index, fireEvent);
	}

	public static void setModified(ArtifactRel rel, boolean isModified) {
		ConnectionBendpointDPolicy pol = rel.getTypedDiagramPolicy(Type, DefaultKey);
		if (pol != null)
			pol.setModified(isModified);
	}

	private void setModified(boolean isModified) {
		this.isModified = isModified;
	}

	public static boolean isModified(ArtifactRel rel) {
		ConnectionBendpointDPolicy pol = rel.getTypedDiagramPolicy(Type, DefaultKey);
		if (pol != null)
			return pol.isModified();
		return false;
	}

	private boolean isModified() {
		return isModified;
	}
	
}
