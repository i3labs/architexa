package com.architexa.diagrams.strata.cache;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.openrdf.model.Resource;

import com.architexa.diagrams.jdt.model.CodeUnit;
import com.architexa.diagrams.model.ArtifactFragment;
import com.architexa.diagrams.strata.StrataPlugin;
import com.architexa.diagrams.strata.model.CompositeLayer;
import com.architexa.diagrams.strata.model.Layer;
import com.architexa.diagrams.strata.model.StrataRootDoc;
import com.architexa.diagrams.strata.model.policy.ClosedContainerDPolicy;
import com.architexa.diagrams.strata.model.policy.LayersDPolicy;

/**
 * Provides partitioning support in one of two modes - either partitions all
 * children or only does partitioning for one item being added/removed (the
 * later being referred to as singly added/removed)
 * 
 * @author Vineet
 * 
 */
public class PartitionerSupport {
	public static final Logger logger = StrataPlugin.getLogger(PartitionerSupport.class);
	private static final Logger PartitioningLogger = Partitioner.logger;
	
	private static Map<Resource, List<ArtifactFragment>> parentToSinglyAddedChildAFs = new HashMap<Resource, List<ArtifactFragment>>();
	private static Map<Resource, List<ArtifactFragment>> parentToSinglyRemovedChildAFs = new HashMap<Resource, List<ArtifactFragment>>();
	
	public static void partitionLayers(StrataRootDoc rootDoc, ArtifactFragment artFrag) {
		DepNdx depCacheCalculator = rootDoc.getDepNdx();
		
		PartitioningLogger.info("DepCache.getChildren: " + CodeUnit.getLabelWithContext(depCacheCalculator.getRepo(), artFrag));
		
		PartitioningLogger.info("Partitioning: " + CodeUnit.getLabels(depCacheCalculator.getRepo(), ClosedContainerDPolicy.getShownChildren(artFrag)));
		Resource afResource = artFrag.getArt().elementRes;
		
		if (!getSinglyAddedChildren(afResource).isEmpty()) {
			partitionSinglyAddedChild(artFrag, afResource, rootDoc);
		}
		if (!getSinglyRemovedChildren(afResource).isEmpty()) {
			partitionSinglyRemovedChild(artFrag, afResource, rootDoc, LayersDPolicy.getLayers(artFrag));
		}
		// default Relayering process: only happens when adding to the root or opening a package
		if (LayersDPolicy.layersNeedBuilding(artFrag)) {
			List<Layer> layers = smartDependenciesPartitioner(depCacheCalculator, artFrag);
			LayersDPolicy.setLayersByMergingIntoCurrent(artFrag, layers);
			PartitioningLogger.info("Returning Layers: " + LayersDPolicy.getLayers(artFrag));
		}
	}

	private static void partitionSinglyRemovedChild(ArtifactFragment artFrag, Resource afResource, StrataRootDoc rootDoc, List<Layer> currLayers) {
		// removes empty layers and updats layers when items are removed
		if (currLayers.isEmpty()) currLayers.add(new Layer(rootDoc.getDepNdx()));
		for (ArtifactFragment oldAF : new ArrayList<ArtifactFragment>(getSinglyRemovedChildren(afResource)) ) {
			for (Layer layer : new ArrayList<Layer>(currLayers)) {
				if (layer.contains(oldAF)) layer.remove(oldAF);
				if (layer instanceof CompositeLayer) {
					CompositeLayer compLayer = (CompositeLayer)layer;
					partitionSinglyRemovedChild(artFrag, afResource, rootDoc, compLayer.getChildrenLayers());
					List<Layer> compChildrenLayer = compLayer.getChildrenLayers();
					if (compLayer.getMultiLayer() && compChildrenLayer.size() == 1) {
						// multi layer with only one layer - remove it (helps clean logic in other places)
						int ndx = currLayers.indexOf(compLayer);
						currLayers.remove(ndx);
						currLayers.add(ndx, compChildrenLayer.get(0));
					}
				}
				if (layer.isEmpty()) currLayers.remove(layer);	
			}
			getSinglyRemovedChildren(afResource).clear();
		}
		LayersDPolicy.setLayers(artFrag, currLayers);
		getSinglyRemovedChildren(afResource).clear();	
	}


	private static void partitionSinglyAddedChild(ArtifactFragment artFrag, Resource afResource, StrataRootDoc rootDoc) {
		List<Layer> currLayers = LayersDPolicy.getLayers(artFrag);
		// add a blank layer if none exist yet
		if (currLayers.isEmpty()) currLayers.add(new Layer(rootDoc.getDepNdx()));
		
		// partition each child and update the list of layers
		for (ArtifactFragment af : getSinglyAddedChildren(afResource)) {
			currLayers = partitionSingleChild(rootDoc, artFrag, af, currLayers);
		}
		// update layersPolicy without making any additional changes to the
		// layers. And then clear the list of items to be added
		LayersDPolicy.setLayers(artFrag, currLayers);
		getSinglyAddedChildren(afResource).clear();
	}

	protected static List<Layer> smartDependenciesPartitioner(DepNdx depCacheCalculator, ArtifactFragment af) {
		PartitioningLogger.info("Smart Partitioning. - " + Thread.currentThread().getName());

		List<Layer> partitionedLayers =  Partitioner.smartPartitionDependencies(depCacheCalculator, af);
		
		PartitioningLogger.info("Pre-Merge Layers: " + partitionedLayers);

        Partitioner mergePartitioner = new Partitioner(depCacheCalculator, ClosedContainerDPolicy.getShownChildren(af), /*quiet*/true);
        List<Layer> mergedLayers = mergePartitioner.mergeLayers(partitionedLayers);

        PartitioningLogger.info("Merged Layers: " + mergedLayers);

        Partitioner splitPartitioner = new Partitioner(depCacheCalculator, ClosedContainerDPolicy.getShownChildren(af), /*quiet*/true);
        List<Layer> splitLayers = splitPartitioner.splitLongLayers(mergedLayers);

        PartitioningLogger.info("Split Layers: " + splitLayers);
        
        Partitioner mergeLonelyPartitioner = new Partitioner(depCacheCalculator, ClosedContainerDPolicy.getShownChildren(af), /*quiet*/true);
        List<Layer> fixLayers = mergeLonelyPartitioner.mergeLayersForLonelyArtFrags(splitLayers);

        PartitioningLogger.info("Merge Lonely Layers: " + fixLayers);

        return fixLayers;

	}

	private static List<Layer> partitionSingleChild(StrataRootDoc rootDoc, ArtifactFragment parentAF, ArtifactFragment newChildAF, List<Layer> layers) {
		DepNdx depCacheCalculator = rootDoc.getDepNdx();
		Partitioner singleChildPartitioner = new Partitioner(depCacheCalculator, ClosedContainerDPolicy.getShownChildren(parentAF), /*quiet*/true);
		List<Layer> newLayers =  singleChildPartitioner.singleAddPartitioner(depCacheCalculator, layers, newChildAF);
		return newLayers;
	}

	public static int getCorrectIndex(StrataRootDoc rootDoc, ArtifactFragment parentAF, ArtifactFragment newChildAF, List<Layer> layers) {
		DepNdx depCacheCalculator = rootDoc.getDepNdx();
		Partitioner singleChildPartitioner = new Partitioner(depCacheCalculator, ClosedContainerDPolicy.getShownChildren(parentAF), /*quiet*/true);
		return singleChildPartitioner.singleAddIndex(layers, newChildAF);
	}


	// Map setters and getters

	private static List<ArtifactFragment> getSinglyAddedChildren(Resource parentAF) {
		if (parentToSinglyAddedChildAFs.get(parentAF) == null )
			parentToSinglyAddedChildAFs.put(parentAF, new ArrayList<ArtifactFragment>());
		return parentToSinglyAddedChildAFs.get(parentAF);
	}

	public static void addSinglyAddedChild(Resource parent, ArtifactFragment child) {
		if (parentToSinglyAddedChildAFs.get(parent) == null) 
			parentToSinglyAddedChildAFs.put(parent, new ArrayList<ArtifactFragment>());
		parentToSinglyAddedChildAFs.get(parent).add(child);
	}
	
	private static List<ArtifactFragment> getSinglyRemovedChildren(Resource parentAF) {
		if (parentToSinglyRemovedChildAFs.get(parentAF) == null ) 
			parentToSinglyRemovedChildAFs.put(parentAF, new ArrayList<ArtifactFragment>());
		return parentToSinglyRemovedChildAFs.get(parentAF);
	}

	public static void setRemovedSingleChildren(Resource parent, ArtifactFragment child) {
		if (parentToSinglyRemovedChildAFs.get(parent) == null)
			parentToSinglyRemovedChildAFs.put(parent, new ArrayList<ArtifactFragment>());
		parentToSinglyRemovedChildAFs.get(parent).add(child);
	}
	
}
