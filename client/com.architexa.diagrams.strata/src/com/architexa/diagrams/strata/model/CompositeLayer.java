package com.architexa.diagrams.strata.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;

import com.architexa.diagrams.jdt.model.CodeUnit;
import com.architexa.diagrams.model.ArtifactFragment;
import com.architexa.diagrams.strata.StrataPlugin;
import com.architexa.diagrams.strata.model.policy.LayerPositionedDPolicy;
import com.architexa.diagrams.strata.model.policy.LayersDPolicy;
import com.architexa.store.ReloRdfRepository;


public class CompositeLayer extends Layer {
	public static final Logger logger = StrataPlugin.getLogger(CompositeLayer.class);

	private boolean isMultiLayer;	// determines if children are added horizontally or vertically 

	public CompositeLayer(ReloRdfRepository _repo, Layer[] childrenLayers) {
		this(_repo, new ArrayList<Layer>(Arrays.asList(childrenLayers)));
	}
	public CompositeLayer(ReloRdfRepository _repo, List<Layer> childrenLayers) {
		super(_repo);
		ArtifactFragment.ensureInstalledPolicy(this, LayersDPolicy.DefaultKey, LayersDPolicy.class);
		ArtifactFragment.ensureInstalledPolicy(this, LayerPositionedDPolicy.DefaultKey, LayerPositionedDPolicy.class);
		LayersDPolicy.setLayers(this, childrenLayers);
		this.appendShownChildren(childrenLayers);
	}
	
	public CompositeLayer(ReloRdfRepository repo, Layer[] childrenLayers, boolean isHorz) {
		this(repo, childrenLayers);
		setLayout(isHorz);
		setMultiLayer(false);
	}

	public void setMultiLayer(boolean isMultiLayer) {
		this.isMultiLayer = isMultiLayer;
	}
	public boolean getMultiLayer() {
		return isMultiLayer;
	}

	@Override
	public boolean contains(ArtifactFragment child) {
		if (this.containsChild(child)) return true;
		for (ArtifactFragment childLayer : this.getShownChildren()) {
			if (!(childLayer instanceof Layer)) {
				logger.error("MultiLayer Contains non Layer: Check Hierachy creation");
				continue;
			}
			if (((Layer)childLayer).contains(child)) return true;
		}
		return false;
	}

	public List<Layer> getChildrenLayers() {
		List<Layer> children = new ArrayList<Layer>();
		for (ArtifactFragment layer : getChildren()) {
			if (!(layer instanceof Layer))
				logger.error("Unexpected Exception", new IllegalArgumentException("Adding Non Layer to a MultiLayer"));
			else 
				children.add((Layer) layer);	
		}
		return children;		
	}

	private List<List<ArtifactFragment>> getChildrenAsAF() {
		List<List<ArtifactFragment>> children = new ArrayList<List<ArtifactFragment>>();
		for (ArtifactFragment layer : getChildren()) {
			if (layer instanceof Layer)
				children.add(layer.getShownChildren());
		}
		return children;		
	}

	@Override
	public String toString() {
		return this.size() + ": " + CodeUnit.getLabelss(getRepo(), this.getChildrenAsAF());
	}
	
	
}
