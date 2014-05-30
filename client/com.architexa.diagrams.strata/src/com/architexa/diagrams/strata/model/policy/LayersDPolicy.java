package com.architexa.diagrams.strata.model.policy;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.openrdf.rio.RdfDocumentWriter;

import com.architexa.diagrams.model.ArtifactFragment;
import com.architexa.diagrams.model.DiagramPolicy;
import com.architexa.diagrams.strata.StrataPlugin;
import com.architexa.diagrams.strata.model.CompositeLayer;
import com.architexa.diagrams.strata.model.Layer;
import com.architexa.store.ReloRdfRepository;


public class LayersDPolicy extends DiagramPolicy {
	////
	// basic setup
	////
	static final Logger logger = StrataPlugin.getLogger(LayersDPolicy.class);
	public static final String DefaultKey = "LayersDPolicy";
	public static final String LayersChanged = "LayersChanged";
	public static final LayersDPolicy Type = new LayersDPolicy();

	////
	// Policy Fields, Constructors and Methods 
	////
	private List<Layer> layers = new ArrayList<Layer>();
	
	// TODO: aren't the two fields below the same?
	public boolean layersNeedBuilding = true;
	private boolean autoLayering = false;

	public LayersDPolicy() {
	}

	@Override
	public void setHost(Object hostAF) {
		super.setHost(hostAF);
		
		getHostAF().addPropertyChangeListener(new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent evt) {
				//System.err.println("LDP: " + evt.getPropertyName() + " this: " + getHost().toString());
				if (evt.getPropertyName().equals(ArtifactFragment.Contents_Changed)) {
					if (autoLayering) LayersDPolicy.flushLayers(getHostAF());
				}
			}
		});
	}

	public List<Layer> getLayers() {
		return layers;
	}
	
	/**
	 * try to keep the layer class around and just move the contents (we don't
	 * want to create new objects unless necessary)
	 */
	public void setLayersByMergingIntoCurrent(List<Layer> rhsLayers) {
		List<Layer> lhsLayers = this.layers;
		if (lhsLayers == null 
				/*|| (lhsLayers.size() == 1 && lhsLayers.get(0).size() == 0)*/
				) {
			this.layers = rhsLayers;
			this.layersNeedBuilding = false;
			return;
		}
		
		for (int i = 0; i < Math.min(lhsLayers.size(), rhsLayers.size()); i++) {
			Layer currRHSLayer = rhsLayers.get(i);
			Layer currLHSLayer = lhsLayers.get(i);
			if (currRHSLayer.getClass().equals(currLHSLayer.getClass())) {
				currLHSLayer.clearShownChildren();
				currLHSLayer.append(rhsLayers.get(i));
			} else
				lhsLayers.set(i, currRHSLayer);
		}
		while (lhsLayers.size() < rhsLayers.size()) {
			lhsLayers.add(rhsLayers.get(lhsLayers.size()));
		}
		while (lhsLayers.size() > rhsLayers.size()) {
			lhsLayers.remove(lhsLayers.size() - 1);
		}
		this.layersNeedBuilding = false;
		this.autoLayering = false;
	}
	public void addLayers(List<Layer> rhsLayers) {
		List<Layer> lhsLayers = this.layers;
		for (Layer rhsLayer : rhsLayers) {
			lhsLayers.add(rhsLayer);
		}
	}
	public void removeLayers(List<Layer> rhsLayers) {
		List<Layer> lhsLayers = this.layers;
		for (Layer rhsLayer : rhsLayers) {
			lhsLayers.remove(rhsLayer);
		}
	}

	
	// @tag implement-for-correct-saving-support
	@Override
	public void writeRDF(RdfDocumentWriter rdfWriter) throws IOException {
	}
	@Override
	public void readRDF(ReloRdfRepository queryRepo) {
		this.layersNeedBuilding = false;
	}


	////
	// Static Helpers 
	////
	public static void flushLayers(ArtifactFragment artFrag) {
		LayersDPolicy afLayersPolicy = artFrag.getTypedDiagramPolicy(Type, DefaultKey);
		if (afLayersPolicy != null) {
			afLayersPolicy.layersNeedBuilding = true;
	        artFrag.firePropChang(LayersChanged);
		}
	}
	public static boolean layersNeedBuilding(ArtifactFragment artFrag) {
		LayersDPolicy afLayersPolicy = artFrag.getTypedDiagramPolicy(Type, DefaultKey);
		if (afLayersPolicy != null) return afLayersPolicy.layersNeedBuilding;
		return false;
	}
	public static boolean setLayersNeedBuilding(ArtifactFragment artFrag, boolean layersNeedBuilding) {
		LayersDPolicy afLayersPolicy = artFrag.getTypedDiagramPolicy(Type, DefaultKey);
		if (afLayersPolicy != null) 
			afLayersPolicy.layersNeedBuilding = layersNeedBuilding;
		return false;
	}
	
	public static void setAutoLayering(ArtifactFragment artFrag, boolean b) {
		LayersDPolicy afLayersPolicy = artFrag.getTypedDiagramPolicy(Type, DefaultKey);
		if (afLayersPolicy != null) afLayersPolicy.autoLayering = b;
	}

	public static List<Layer> getLayers(ArtifactFragment artFrag) {
		if (artFrag == null) return null; // if af was deleted
		LayersDPolicy afLayersPolicy = artFrag.getTypedDiagramPolicy(Type, DefaultKey);
		if (afLayersPolicy != null) return afLayersPolicy.getLayers();
		return null;
	}
	
	public static List<Layer> getLayersCopy(ArtifactFragment artFrag, ReloRdfRepository repo) {
		List<Layer> orgLayers = getLayers(artFrag);
		List<Layer> layersCopy = new ArrayList<Layer>();
		for (Layer layer : orgLayers) {
			Layer newLayer = new Layer(repo, layer.getLayout());
			if (layer instanceof CompositeLayer) {
				newLayer = new CompositeLayer(repo, getLayersCopy(layer, repo));
				((CompositeLayer)newLayer).setMultiLayer(((CompositeLayer) layer).getMultiLayer());
			} else
				newLayer.appendShownChildren(layer.getChildren());
			layersCopy.add(newLayer);
		}
		return layersCopy;
	}

	public static void setLayers(ArtifactFragment artFrag, List<Layer> newLayers) {
		LayersDPolicy afLayersPolicy = artFrag.getTypedDiagramPolicy(Type, DefaultKey);
		if (afLayersPolicy != null) 	afLayersPolicy.layers = newLayers;
	}
	
	public static void setLayersByMergingIntoCurrent(ArtifactFragment artFrag, List<Layer> rhsLayers) {
		LayersDPolicy afLayersPolicy = artFrag.getTypedDiagramPolicy(Type, DefaultKey);
		if (afLayersPolicy != null) afLayersPolicy.setLayersByMergingIntoCurrent(rhsLayers);
	}

	public static void addLayers(ArtifactFragment artFrag, List<Layer> rhsLayers) {
		LayersDPolicy afLayersPolicy = artFrag.getTypedDiagramPolicy(Type, DefaultKey);
		if (afLayersPolicy != null) afLayersPolicy.addLayers(rhsLayers);
	}
	public static void removeLayers(ArtifactFragment artFrag, List<Layer> rhsLayers) {
		LayersDPolicy afLayersPolicy = artFrag.getTypedDiagramPolicy(Type, DefaultKey);
		if (afLayersPolicy != null) afLayersPolicy.removeLayers(rhsLayers);
	}
	/**
	 * Moves layers - callers will also need to move the actual AFs after moving the layers
	 */
	public static void moveLayers(ArtifactFragment srcAF, ArtifactFragment dstAF, List<Layer> rhsLayers) {
		removeLayers(srcAF, rhsLayers);
		addLayers(dstAF, rhsLayers);
	}

	public static List<Layer> copyLayers(ReloRdfRepository repo, ArtifactFragment afToCopy) {
		List<Layer> newLayers = new ArrayList<Layer>();
		for (Layer orgLayer : new ArrayList<Layer>(LayersDPolicy.getLayers(afToCopy))) {
			Layer newLayer = null;
			if (orgLayer instanceof CompositeLayer) {
				newLayer = new CompositeLayer(repo, copyLayers(repo, orgLayer));
				((CompositeLayer)newLayer).setMultiLayer(((CompositeLayer) orgLayer).getMultiLayer());
			} else {
				newLayer = new Layer(repo, orgLayer.getLayout());
				for (ArtifactFragment af : orgLayer.getChildren()) {
					newLayer.add(af);	
				}
			}
			newLayers.add(newLayer);
		}
		return newLayers;
	}
}
