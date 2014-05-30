/**
 * Copyright (c) 2007-2008 Architexa, Inc
 */ 
package com.architexa.diagrams.strata.cache;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;

import com.architexa.diagrams.model.ArtifactFragment;
import com.architexa.diagrams.strata.StrataPlugin;
import com.architexa.diagrams.strata.commands.ModelUtils;
import com.architexa.diagrams.strata.model.CompositeLayer;
import com.architexa.diagrams.strata.model.Layer;
import com.architexa.diagrams.strata.model.policy.ClosedContainerDPolicy;


/**
 * Note: almost all operations on this class are destructive (destroy the internal state)
 * TODO: ideally make the destructive operations static and instantiate the class internally
 * 
 * @author Vineet_Sinha
 *
 */
public class Partitioner implements DepSource {
	public static final Logger logger = StrataPlugin.getLogger(Partitioner.class);

	private final DepNdx depCacheCalculator;
	
	// represents the current iteration 
	private Matrix dep = null;
	private Map<Integer, ArtifactFragment> depNdxToArtFrag;
	private Map<ArtifactFragment, Integer> depArtFragToNdx;

	// accessors
	public int getSize() {
		return dep.getSize();
	}
	public int getDep(ArtifactFragment srcAF, ArtifactFragment dstAF, IProgressMonitor monitor, List<ArtifactFragment> artFragsToAdd) {
		return dep.data[depArtFragToNdx.get(srcAF)][depArtFragToNdx.get(dstAF)];
	}

	/**
	 * Gets complete partition
	 */
	public Partitioner(DepNdx _depCacheCalculator, int sz) {
		this.depCacheCalculator = _depCacheCalculator;
		
		this.dep = new Matrix(sz);
		depNdxToArtFrag = new HashMap<Integer, ArtifactFragment>(sz);
		depArtFragToNdx = new HashMap<ArtifactFragment, Integer>(sz);
	}
	
	public Partitioner(DepNdx _depCacheCalculator, List<ArtifactFragment> strataAFsForLayering) {
		this(_depCacheCalculator, strataAFsForLayering, /*quiet*/false);
	}
	
	/**
	 * @param quiet - true for small supporting partitioners, so that we don't display too much debug information
	 */
	public Partitioner(DepNdx _depCacheCalculator, List<ArtifactFragment> strataAFsForLayering, boolean quiet) {
		this(_depCacheCalculator, strataAFsForLayering.size());
		
		Layer lyr = new Layer(depCacheCalculator, strataAFsForLayering);
		focusLayerFromDep(lyr);
		
		if (!quiet) {
			logger.info("Focusing on: " + lyr);
			DepSourceUtils.toStream(System.out, this, depNdxToArtFrag, "Focused Matrix");
		}
	}
	

	private void filterSmallDep(int filterCnt) {
		logger.info("Using filter cnt of: " + filterCnt);
		// actually filter
		for (int i = 0; i<this.dep.getSize(); i++) {
			for (int j = 0; j<this.dep.getSize(); j++) {
				if (this.dep.data[i][j] > filterCnt) continue;
				this.dep.data[i][j] = 0;
			}
		}

		DepSourceUtils.toStream(System.out, this, depNdxToArtFrag, "Filtered Matrix");
	}

    private List<Layer> partitionDependencies() {
        List<Layer> topLayers = new ArrayList<Layer>();
        List<Layer> botLayers = new ArrayList<Layer>();

        Layer layer = null;
        do {
        	boolean foundNonCycLayer = true;
        	while (foundNonCycLayer) {
            	foundNonCycLayer = false;
            	
                // find bottom first, since top is making a stronger statement
                layer = findBotLayer();
                eliminateLayerFromDep(layer);
                logger.info("Found bot: " + layer);
                if (!layer.isEmpty()) {
                	foundNonCycLayer = true;
                	botLayers.add(layer);
                }

                layer = findTopLayer();
                eliminateLayerFromDep(layer);
                logger.info("Found top: " + layer);
                if (!layer.isEmpty()) {
                	foundNonCycLayer = true;
                	topLayers.add(layer);
                }
            };
            
            // look for cycles
            layer = findFirstCycleLayer();
            eliminateLayerFromDep(layer);
            logger.info("Found cyc: " + layer);
            logger.info("To process: " + getCurrentLayer());
            // add to botLayers, since if we did find the smallest cycle then
            // other cycles could depend on me, out approach of following all
            // dependencies will not find a cycle that depends on other cycles.
            // but this is dependent on the cycle finding algorithm
            if (!layer.isEmpty()) { layer.setType(Layer.cycle); botLayers.add(layer); }    

        } while (this.dep.getSize() > 0);
        
        // flip botLayers and add to topLayers
        ListIterator<Layer> botLayersIt = botLayers.listIterator(botLayers.size());
        while (botLayersIt.hasPrevious()) {
            Layer botLayer = botLayersIt.previous();
            topLayers.add(botLayer);            
        }
        
        // lets print out the layers
        logger.info("");
        logger.info("Partitioned layers:");
        for (Layer currLayer : topLayers) {
            logger.info(currLayer);
        }
        logger.info("");

        return topLayers;
    }

	private void focusLayerFromDep(Layer curDepLayer) {
		ArtifactFragment[] focusLayer = curDepLayer.getShownChildren().toArray(new ArtifactFragment [] {});
		int focusSize = focusLayer.length;
		Matrix newDep = new Matrix(focusSize);
		
		for (int r = 0; r < focusSize; r++) {
			for (int c = 0; c < focusSize; c++) {
				// the old way queried to build deps, now we get them from the model. These should be the same
				//newDep.data[r][c] = this.depCacheCalculator.getDep(focusLayer[r], focusLayer[c], new NullProgressMonitor(), null);
				newDep.data[r][c] = ModelUtils.getDep(focusLayer[r], focusLayer[c]);
			}
		}
		
		dep = newDep;
		for (int i = 0; i < focusSize; i++) {
			this.depNdxToArtFrag.put(i, focusLayer[i]);
			this.depArtFragToNdx.put(focusLayer[i], i);
		}
	}

	private void eliminateArtFragFromPartition(ArtifactFragment curDepLayerAF) {
		int oldPartitionSize = depArtFragToNdx.size();
		int curDepLayerAFNdx = depArtFragToNdx.get(curDepLayerAF);

		// delete from matrix
		dep = dep.getMatrixWithDeletedIndex(curDepLayerAFNdx);

		// update curDepLayerAF indexes
		depArtFragToNdx.remove(curDepLayerAF);
		depNdxToArtFrag.remove(curDepLayerAFNdx);

		// any indices > curDepLayerArtFragNdx need to change
		for (int extraNdx = curDepLayerAFNdx + 1; extraNdx < oldPartitionSize; extraNdx++) {
			ArtifactFragment extraAF = depNdxToArtFrag.get(extraNdx);
			depArtFragToNdx.put(extraAF, extraNdx-1);
			depNdxToArtFrag.put(extraNdx-1, extraAF);
		}
		depNdxToArtFrag.remove(oldPartitionSize-1);
	}

	/**
	 * This method eliminates the defined layer from the current dependencies
	 * and updates the index map
	 */
	private void eliminateLayerFromDep(Layer curDepLayer) {
		for (ArtifactFragment curDepLayerArtFrag : curDepLayer.getChildren()) {
			eliminateArtFragFromPartition(curDepLayerArtFrag);
		}
	}


	private Layer getInDep(int depNdx) {
		Layer retLayer = new Layer(depCacheCalculator);
		for (int srcModNdx=0; srcModNdx<this.dep.getSize(); srcModNdx++) {
			if (srcModNdx == depNdx) continue;
			if (this.dep.data[srcModNdx][depNdx] != 0) retLayer.add(depNdxToArtFrag.get(srcModNdx));
		}
		return retLayer;
	}

	private Layer getOutDep(int depNdx) {
		Layer retLayer = new Layer(depCacheCalculator);
		for (int dstModNdx=0; dstModNdx<this.dep.getSize(); dstModNdx++) {
			if (dstModNdx == depNdx) continue;
			if (this.dep.data[depNdx][dstModNdx] != 0) retLayer.add(depNdxToArtFrag.get(dstModNdx));
		}
		return retLayer;
	}

	/**
	 * gets the minimum (off-diagonal) depenency greater than the given input count
	 */
	private int getMinDep(int minFilterCnt) {
		int retVal = Integer.MAX_VALUE;
		for (int srcModNdx = 0; srcModNdx < this.dep.getSize(); srcModNdx++) {
			for (int dstModNdx = 0; dstModNdx < this.dep.getSize(); dstModNdx++) {
				if (dstModNdx == srcModNdx) continue;
				if (this.dep.data[srcModNdx][dstModNdx] > minFilterCnt)
					retVal = Math.min(retVal, this.dep.data[srcModNdx][dstModNdx]);
			}
		}
		return retVal;
	}


	private Layer getCurrentLayer() {
		Partitioner curDepPartitioner = this;

		Layer currentLayer = new Layer(depCacheCalculator);
		for (int modNdx=0; modNdx<curDepPartitioner.dep.getSize(); modNdx++) {
			currentLayer.add(depNdxToArtFrag.get(modNdx));
		}
		return currentLayer;
	}

	/**
	 * Searches for modules that no one depends on
	 */
	private Layer findTopLayer() {
		Partitioner curDepPartitioner = this;

		Layer currentLayer = new Layer(depCacheCalculator);
		for (int modNdx=0; modNdx<curDepPartitioner.dep.getSize(); modNdx++) {
			if (getInDep(modNdx).isEmpty()) currentLayer.add(depNdxToArtFrag.get(modNdx));
		}
		return currentLayer;
	}

	/**
	 * Searches for modules that do not depend on anything.
	 */
    private Layer findBotLayer() {
        Partitioner curDepPartitioner = this;

        Layer currentLayer = new Layer(depCacheCalculator);
        for (int modNdx=0; modNdx<curDepPartitioner.dep.getSize(); modNdx++) {
            if (getOutDep(modNdx).isEmpty())
            	currentLayer.add(depNdxToArtFrag.get(modNdx));
        }
        return currentLayer;
    }

	
	/**
	 * Returns the first cycle found (assuming that there are only cycles in the
	 * dep matrix).
	 * 
	 * We really find the cycle that the first module belongs to, but this
	 * should be all that is needed, since we should have eliminated the 'top'
	 * and 'bottom' portions already.
	 */
	private Layer findFirstCycleLayer() {
		Partitioner curDepPartitioner = this;
		
		Layer currentLayer = new Layer(depCacheCalculator);
		if (curDepPartitioner.dep.getSize() == 0) return currentLayer;
		
		currentLayer.add(depNdxToArtFrag.get(0));	// add the first

		// add everything that currentLayer depends on
		while(true) {
			Layer layerAdditions = new Layer(depCacheCalculator);
			for (ArtifactFragment modAF : currentLayer.getChildren()) {
				int modNdx = this.depArtFragToNdx.get(modAF);
				for (int destModNdx=0; destModNdx<curDepPartitioner.dep.getSize(); destModNdx++) {
					ArtifactFragment destModAF = this.depNdxToArtFrag.get(destModNdx);
					if (!currentLayer.contains(destModAF) && curDepPartitioner.dep.data[modNdx][destModNdx] != 0)
						layerAdditions.add(depNdxToArtFrag.get(destModNdx));
				}
			}
			if (layerAdditions.isEmpty()) break;
			currentLayer.append(layerAdditions);
		};

		return currentLayer;
	}


	public static List<Layer> smartPartitionDependencies(DepNdx dm, ArtifactFragment af) {
		logger.info("Starting Smart Partitioning.");

		// Do a nested partitioning and add layers into a single flat list
		List<Layer> layers = new ArrayList<Layer>();
		Map<Layer, Integer> layersToFilter = new HashMap<Layer, Integer>();
		
		Layer initLayer = new Layer(dm, ClosedContainerDPolicy.getShownChildren(af));
		layersToFilter.put(initLayer, -1);
		layers.add(initLayer);
		Partitioner.smartPartitionDependencies(dm, layers, layersToFilter);
		
		return layers;
	}
	
	// this works with multiple ratios as defined by filterRatios
	private static void smartPartitionDependencies(DepNdx dm, List<Layer> layers, Map<Layer, Integer> layersToFilter) {
		boolean changed;

		do{
			changed = false;
			
			for (int curLayerNdx = 0; curLayerNdx < layers.size(); curLayerNdx++) {
				Layer layer = layers.get(curLayerNdx);
				//logger.error(".: " + Layer + "  " + Layer.getType());
				if (layer.getType() != Layer.cycle && layers.size() != 1) {
					//logger.error("Skipping: " + Layer);
					continue;
				}
				
				// now do we want to handle it?
				int tgtFilterCnt = -1;
				int currFilterCnt = layersToFilter.get(layer);

				// if the layer is too small (and we have already filtered once)
				if (layer.size() < 3 && currFilterCnt != -1) continue;
				
				Partitioner curLvlPartitioner = new Partitioner(dm, layer.getShownChildren());
				tgtFilterCnt = curLvlPartitioner.getMinDep(currFilterCnt);
					
				if (tgtFilterCnt == Integer.MAX_VALUE) continue;
				//if (true) continue;
				
				curLvlPartitioner.filterSmallDep(tgtFilterCnt);
                List<Layer> filteredPartioning;
                filteredPartioning = curLvlPartitioner.partitionDependencies();
				
				layers.remove(curLayerNdx);
				layers.addAll(curLayerNdx, filteredPartioning);
				for (Layer modColl : filteredPartioning) {
					//layers.add(modColl);
					layersToFilter.put(modColl, tgtFilterCnt);
				}
				changed = true;
			}
		} while (changed);
	}

	/**
	 * Fix issue with dependency a=>b=>c with b,c on own layers
	 * move b up to layer a
	 */
	public List<Layer> mergeLayersForLonelyArtFrags(List<Layer> layers) {
		//dm.printDependencies(System.out, dm.dep, null, "Main Matrix");
		DepSourceUtils.toStream(System.out, this, depNdxToArtFrag, "Focused Matrix");
		
		//logger.info("Merging: " + layers);
		//logger.info("depNdxMap: " + this.depNdxToMainDepNdx);
		
		boolean changed;
		do {
			changed = false;
			
			for (int localDepNdx = 0; localDepNdx < dep.getSize(); localDepNdx++) {
				//int mainDepNdx = this.depNdxToMainDepNdx.get(localDepNdx);
				//logger.info("Considering Moving: " + localDepNdx + " / " + dm.depIndicesToObjs.get(mainDepNdx));
				
				// skip in cases such as after deleting...
				if (!this.depNdxToArtFrag.containsKey(localDepNdx)) continue;
				
				int depNdxLayer = getLayerNdx(layers, depNdxToArtFrag.get(localDepNdx));
				
				//skip if layer has more then one item
				if(layers.get(depNdxLayer).size()>1) continue;

				//skip if this item depends on multiple items 
				Layer outArtFrags = getOutDep(localDepNdx);
				if (outArtFrags.isEmpty()) continue;
				
				// skip if tgt layer has multiple children
				int outLayerNdx = getLayerNdx(layers, depNdxToArtFrag.get(localDepNdx));
				int layerndx = getLayerNdx(layers, depNdxToArtFrag.get(localDepNdx));
				if ((layerndx +1) < layers.size())
					 outLayerNdx = layerndx +1;
				else continue;
				if (layers.get(outLayerNdx).size()>3 || layers.get(outLayerNdx).size()==0) continue;
				
				//skip if we are looking at a Layer
				if (layers.get(outLayerNdx) instanceof CompositeLayer && ((CompositeLayer) layers.get(outLayerNdx)).getMultiLayer()) continue;
				
				layers.get(depNdxLayer).remove(depNdxToArtFrag.get(localDepNdx));
				layers.get(outLayerNdx).add(depNdxToArtFrag.get(localDepNdx));
				changed=true;
			}
		} while (changed);
		
		return crunchLayers(layers);
	}
	
	
	private static final List<Layer> crunchLayers(List<Layer> layers) {
		Iterator<Layer> layersIt = layers.iterator();
		while (layersIt.hasNext()) {
			if (layersIt.next().isEmpty()) layersIt.remove();
		}
		return layers;
	}
	/**
	 * until now, top becomes top if nothing depends on it, what we really
	 * want is to minimize false dependencies,
	 * i.e. to move down top so that it is only above something that it
	 * depends on
	 *  => there should be a direct dependency on the layer below it
	 *  => if a module has no dependencies on items above it, then the
	 *     module should be moved as low as possible (to the bottom or
	 *     just above the bottom most depenedency)
	 *     
	 *  => if a module has no dependencies on items above it (or at the 
	 *     same level), then the module should be moved as low as possible 
	 *     (to the bottom or just above the bottom most dependency)
	 */
	public List<Layer> mergeLayers(List<Layer> layers) {
		//dm.printDependencies(System.out, dm.dep, null, "Main Matrix");
		DepSourceUtils.toStream(System.out, this, depNdxToArtFrag, "Focused Matrix");
		
		//logger.info("Merging: " + layers);
		//logger.info("depNdxMap: " + this.depNdxToMainDepNdx);
		
		boolean changed;
		do {
			changed = false;
			
			for (int localDepNdx = 0; localDepNdx < dep.getSize(); localDepNdx++) {
				//int mainDepNdx = this.depNdxToMainDepNdx.get(localDepNdx);
				//logger.info("Considering Moving: " + localDepNdx + " / " + dm.depIndicesToObjs.get(mainDepNdx));
				
				// skip in cases such as after deleting...
				if (!this.depNdxToArtFrag.containsKey(localDepNdx)) continue;
				
				int depNdxLayer = getLayerNdx(layers, depNdxToArtFrag.get(localDepNdx));
				//Layer inLayer = getInDep(depNdx);
				//Set<Integer> inLayers = getLayers(layers, inLayer.indices);
				Layer outArtFrags = getOutDep(localDepNdx);
				Set<Integer> outLayers = getLayers(layers, outArtFrags.getChildren());

				// we can consider moving depNdx if there are no dependencies above it
				boolean considerMoving = true;
				for (Integer outLayersNdx : outLayers) {
					if (outLayersNdx <= depNdxLayer) considerMoving = false;
				}
				if (!considerMoving) continue;
				
				// we can move, find lowest out index and move to just before it
				int minLayerNdx = layers.size();
				for (Integer outLayersNdx : outLayers) {
					minLayerNdx = Math.min(minLayerNdx, outLayersNdx);
				}
				
				int tgtLayerNdx = minLayerNdx - 1;
				if (tgtLayerNdx == depNdxLayer) continue;

				//logger.info("Moving: " + dm.depIndicesToObjs.get(mainDepNdx) + " " + depNdxLayer + " --> " + tgtLayerNdx + " in: " + layers);
				
				// do the move
				layers.get(depNdxLayer).remove(depNdxToArtFrag.get(localDepNdx));
				layers.get(tgtLayerNdx).add(depNdxToArtFrag.get(localDepNdx));
                changed = true;
			}
		} while (changed);
		
		return crunchLayers(layers);
	}


    public List<Layer> splitLongLayers(List<Layer> layers) {
        // get current average
        int totAFSz = 0;
        //int numLayers = 0;
        for (Layer currLayer : layers) {
            totAFSz += currLayer.size();
            //numLayers++;
        }
        int idealLayerSz = (int) Math.sqrt(totAFSz);
         
        // XXX why was this here?
        // if (numLayers <= 1) return layers;

        // split any layer larger than twice the average (without this layer)
        ListIterator<Layer> layersIt = layers.listIterator();
        while (layersIt.hasNext()) {
            Layer currLayer = layersIt.next();
            //logger.info("Comparing: " + currLayer.size() + " to: " + (2 * (layersSz-currLayer.size()) / (numLayers-1)) + " :: " + currLayer.getChildren().toString());
            //logger.info("Comparing: " + currLayer.size() + " to: " + (2 * (layersSz-currLayer.size()) / (numLayers-1)) + " :: " + currLayer.toString());
        	//int newLayersCnt = currLayer.size() / 
            if (currLayer.size() >= (2 * idealLayerSz)) {
            	Layer childrenLayers[] = new Layer[currLayer.size()/idealLayerSz];
            	for (int i=0; i<childrenLayers.length; i++) {
					childrenLayers[i] = new Layer(currLayer.getRepo());
				}
                List<ArtifactFragment> currLayerArtFrags = new ArrayList<ArtifactFragment>(currLayer.getChildren());
                //Layer nextLayer = new Layer(dm);
                int currChildLayer = 0;
                for (ArtifactFragment currLayerArtFrag : currLayerArtFrags) {
                	currChildLayer++;
                	if (currChildLayer >= childrenLayers.length) currChildLayer = 0;
                    currLayer.remove(currLayerArtFrag);	// casing concurrent modification
                    childrenLayers[currChildLayer].add(currLayerArtFrag);
				}
                //layersIt.add(nextLayer);
                CompositeLayer newLayer = new CompositeLayer(currLayer.getRepo(), childrenLayers);
                newLayer.setMultiLayer(true);
                layersIt.remove();
                layersIt.add(newLayer);
            }
        }

        return layers;
    }

	/**
	 * Finds which of the layers has the given ArtFrag and returns the index of
	 * that layer
	 */
	public static int getLayerNdx(List<Layer> layers, ArtifactFragment child) {
		for (int layerNdx = 0; layerNdx < layers.size(); layerNdx++) {
			if (layers.get(layerNdx).contains(child)) return layerNdx;
		}
		return -1;
	}
    private Set<Integer> getLayers(List<Layer> layers, List<ArtifactFragment> children) {
		HashSet<Integer> retVal = new HashSet<Integer> (children.size());
		for (ArtifactFragment child : children) {
			retVal.add(getLayerNdx(layers, child));
		}
		return retVal;
	}
	public List<Layer> singleAddPartitioner(DepNdx depCacheCalculator, List<Layer> layers, ArtifactFragment newChildAF) {
			for (int localDepNdx = 0; localDepNdx < dep.getSize(); localDepNdx++) {
				int newLayerNdx  = singleAddIndex(layers, newChildAF);
				// skip if not new AF
				if (depNdxToArtFrag.get(localDepNdx) != newChildAF ) continue;
				
				// skip in cases such as after deleting...
				if (!this.depNdxToArtFrag.containsKey(localDepNdx)) continue;
				
				// remove any afs from layers if they have been moved to a new location
				removeDups(newChildAF, layers);
				
				if (newLayerNdx >= layers.size()) 
					layers.add(new Layer(depCacheCalculator));
				
				// remove old temp layer
				int oldLayer = getLayerNdx(layers, depNdxToArtFrag.get(localDepNdx));
				if (oldLayer!=-1)
					layers.get(oldLayer).remove(depNdxToArtFrag.get(localDepNdx));
				
				if (layers.get(newLayerNdx) instanceof CompositeLayer ) 
					((Layer) layers.get(newLayerNdx).getShownChildren().get(0)).add(depNdxToArtFrag.get(localDepNdx));
				else
					layers.get(newLayerNdx).add(depNdxToArtFrag.get(localDepNdx));
			}
		return crunchLayers(layers);
	}
	
	private void removeDups(ArtifactFragment af, List<Layer> currLayers) {
		for (ArtifactFragment childAF : af.getShownChildren()) {
			for (Layer layer : currLayers) {
				if (layer.contains(childAF))
					layer.remove(childAF);
			}
			removeDups(childAF, currLayers);
		}
	}
	
	public int singleAddIndex(List<Layer> layers, ArtifactFragment newChildAF) {
		int newLayerNdx = layers.size()-1;
		for (int localDepNdx = 0; localDepNdx < dep.getSize(); localDepNdx++) {
			// skip if not new AF
			if (depNdxToArtFrag.get(localDepNdx) != newChildAF ) continue;
			
			// skip in cases such as after deleting...
			if (!this.depNdxToArtFrag.containsKey(localDepNdx)) continue;
			
			Layer inArtFrags = getInDep(localDepNdx);
			Set<Integer> inLayers = getLayers(layers, inArtFrags.getChildren());
			
			int lowestIn = 0;//layers.size()-1;
			for (Integer inLayersNdx : inLayers) {
				if (lowestIn <= inLayersNdx ) lowestIn = inLayersNdx+1;
			}
			newLayerNdx = lowestIn;
		}
		return newLayerNdx;
	}
}