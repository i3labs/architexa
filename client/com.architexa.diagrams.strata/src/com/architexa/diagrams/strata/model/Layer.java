/**
 * Copyright (c) 2007-2008 Architexa, Inc
 */ 
package com.architexa.diagrams.strata.model;

import java.util.List;

import org.apache.log4j.Logger;

import com.architexa.diagrams.jdt.model.CodeUnit;
import com.architexa.diagrams.model.ArtifactFragment;
import com.architexa.diagrams.model.DerivedArtifact;
import com.architexa.diagrams.model.INonOwnerContainerFragment;
import com.architexa.diagrams.strata.StrataPlugin;
import com.architexa.diagrams.strata.cache.DepNdx;
import com.architexa.store.ReloRdfRepository;


/**
 * Represents layers in the model, but can also be used for an arbitrary
 * collection of art frags.
 * 
 * The class really only stores the indices to the dependency matrix.
 * 
 * @author Vineet_Sinha
 * 
 */
public class Layer extends DerivedArtifact implements INonOwnerContainerFragment {
	public static final Logger logger = StrataPlugin.getLogger(Layer.class);
	private boolean isHorz;
	
	public Layer(ReloRdfRepository _repo) {
		super(null);
		this.repo = _repo;
		this.isHorz = true;
	}
	public Layer(DepNdx _cacheCalcRepo) {
		this(_cacheCalcRepo.getRepo());
	}
	public Layer(ReloRdfRepository _repo, List<ArtifactFragment> children) {
		super(null);
		this.repo = _repo;
		this.appendShownChildren(children);
		this.isHorz = true;
	}
	public Layer(DepNdx _cacheCalcRepo, List<ArtifactFragment> children) {
		this(_cacheCalcRepo.getRepo(), children);
	}

	public Layer(ReloRdfRepository _repo, boolean isHorz) {
		this(_repo);
		this.isHorz = isHorz;
	}
	
	private final ReloRdfRepository repo;

	public ReloRdfRepository getRepo() {
		return repo;
	}

	// pretty much the only extra functionality here - we all others to set a
	// type
	private Object type = null;
	public Object getType() {
		return type;
	}
	public void setType(Object type) {
		this.type = type;
	}




    
	public static final String cycle = "cycle";
	//public static final String cycleLayers = "cycleLayers";
	
	public List<ArtifactFragment> getChildren() {
		return getShownChildren();
	}
	
	public void add(ArtifactFragment strataAF) {
		this.appendShownChild(strataAF);
	}
	public void add(ArtifactFragment strataAF, int index) {
		this.appendShownChild(strataAF, index);
	}

	public void remove(ArtifactFragment strataAF) {
		this.removeShownChild(strataAF);
	}

	public void append(Layer layerAdditions) {
		this.appendShownChildren(layerAdditions.getShownChildren());
	}

	public boolean isEmpty() {
		return this.isShowingChildren();
	}

	public boolean contains(ArtifactFragment child) {
		return this.containsChild(child);
	}

	public int size() {
		return this.getShownChildrenCnt();
	}
	
	@Override
	public String toString() {
		return this.size() + ": " + CodeUnit.getLabels(repo, this.getShownChildren());
	}
	
	public void setLayout(boolean isHorz) {
		this.isHorz = isHorz;
	}
	
	public boolean getLayout() {
		return isHorz;
	}
	
	// Utility method for finding the first parent of this layer/composite layer
	// in the model. This way we can access the model from the layer for
	// deletion etc. 
	public ArtifactFragment getParentArtOfLayer() {
		if (this instanceof CompositeLayer)
			return ((Layer) getChildren().get(0)).getParentArtOfLayer();
		if (getChildren().size()>0)
			return getChildren().get(0).getParentArt();
		return null;
	}
}
