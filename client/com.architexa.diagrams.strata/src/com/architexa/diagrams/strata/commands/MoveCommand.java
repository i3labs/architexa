/**
 * 
 */
package com.architexa.diagrams.strata.commands;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.architexa.diagrams.model.ArtifactFragment;
import com.architexa.diagrams.strata.StrataPlugin;
import com.architexa.diagrams.strata.model.CompositeLayer;
import com.architexa.diagrams.strata.model.Layer;
import com.architexa.diagrams.strata.model.policy.LayersDPolicy;
import com.architexa.org.eclipse.gef.commands.Command;


final public class MoveCommand extends Command {
	public static final Logger logger = StrataPlugin.getLogger(MoveCommand.class);
	
	private final Layer oldParentLyr;
	private final ArtifactFragment oldParentAF;
	private final ArtifactFragment newParentAF;
	private final ArtifactFragment movingAF;
	private final Layer newParentLyr;
	private int oldLayerChildNdx = 0;
	private int newLayerChildNdx = 0;
	private int oldLayerNdx = -1;
	private int newLayerNdx = -1;
	
	public MoveCommand(Layer oldParentLyr, ArtifactFragment oldParentAF,
			ArtifactFragment newParentAF, ArtifactFragment movingAF,
			Layer newParentLyr, int newLayerChildNdx, int newLayerNdx) {
		this.oldParentLyr = oldParentLyr;
		this.oldParentAF = oldParentAF;
		this.newParentAF = newParentAF;
		this.movingAF = movingAF;
		this.newParentLyr = newParentLyr;
		this.newLayerChildNdx = newLayerChildNdx;
		this.oldLayerChildNdx = oldParentLyr.getShownChildren().indexOf(movingAF);
		this.newLayerNdx = newLayerNdx;

		// support moving to the right within the same layer (as the current AF
		// is included in the index given to us)
		if (oldLayerChildNdx < newLayerChildNdx && oldParentLyr == newParentLyr)
			this.newLayerChildNdx--;
	}

	@Override
	public void execute() {
		oldLayerNdx = move(movingAF, oldParentLyr, oldParentAF, newParentLyr, newParentAF, newLayerChildNdx, newLayerNdx);
	}
	
	@Override
	public void undo() {
		move(movingAF, newParentLyr, newParentAF, oldParentLyr, oldParentAF, oldLayerChildNdx, oldLayerNdx);
	}

	/**
	 * Moves tgtAF from oldParentAF within oldParentLyr to be a child of
	 * newParentAF within newParentLyr. If a new layer is being created
	 * newLayerNdx is used. It returns the oldLayersNdx (-1 if it
	 * doesn't change).<br>
	 * <br>
	 * <br>
	 * This method does a couple of things: If moving to a new parent AF remove
	 * tgtAF from the old one and add to the new one. If moving to a
	 * non-existent layer, create it, with the correct index if passed. Remove
	 * tgtAF from oldparentLyr and add to the new Layer with correct index if
	 * passed. Check for an empty old layer and delete if necessary.
	 */
	private int move(ArtifactFragment tgtAF, Layer oldParentLyr, ArtifactFragment oldParentAF, Layer newParentLyr, ArtifactFragment newParentAF, int newLayerChildNdx, int newLayerNdx){
		if (newParentLyr == null) {
			logger.error("Unexpected moving to null layer");
			return -1;
		}

		int oldLayersNdx = -1;

		// moving to a different parent
		if (oldParentAF != newParentAF) {
			if (!(oldParentAF instanceof CompositeLayer) && !(newParentLyr instanceof CompositeLayer)) {
				logger.info("AF-move parentAF: " + newParentAF + " movedAF: "+ tgtAF);
				oldParentAF.getShownChildren().remove(tgtAF);
				newParentAF.getShownChildren().add(tgtAF);
				tgtAF.setParentArt(newParentAF);
			}
		} else {
			logger.info("Lyr-move parentAF: " + newParentLyr + " movedAF: "+ tgtAF);
		}
		
		
		// creating a new layer
		if (newParentLyr.isEmpty()) {
			if (newParentAF instanceof CompositeLayer) {
				List<ArtifactFragment> newParentLayers = ((CompositeLayer) newParentAF).getChildren();
				if (newLayerNdx == -1) 
					newLayerNdx = newParentLayers.size();
				newParentLayers.add(newLayerNdx, newParentLyr);
			} else {
				List<Layer> newParentLayers = LayersDPolicy.getLayers(newParentAF);
				if (newLayerNdx == -1) 
					newLayerNdx = newParentLayers.size();
				newParentLayers.add(newLayerNdx, newParentLyr);
				LayersDPolicy.setLayers(newParentAF, newParentLayers);
				LayersDPolicy.setLayersNeedBuilding(newParentAF, false);
			}
		}
		
		// adding to layer
		oldParentLyr.removeShownChild(tgtAF);
		if (newLayerChildNdx == -1)
			newLayerChildNdx = newParentLyr.size();
		newParentLyr.appendShownChild(tgtAF, newLayerChildNdx);
		
		
		
		
		// remove empty layers
		List<Layer> oldParentLayers = LayersDPolicy.getLayers(oldParentAF);
		for(Layer layer : new ArrayList<Layer>(oldParentLayers)) {
			if (layer.getShownChildrenCnt() == 0) {
				oldLayersNdx = oldParentLayers.indexOf(layer);
				oldParentLayers.remove(layer);
			}
		}
		
		// remove empty new layers  [dont think this ever happens]
		List<Layer> newParentLayers = LayersDPolicy.getLayers(newParentAF);
		for (Layer layer : new ArrayList<Layer>(newParentLayers)) {
			if(layer.getShownChildrenCnt()==0) newParentLayers.remove(layer);
		}
//		LayersDPolicy.setLayers(newParentAF, newParentLayers);
		
		return oldLayersNdx;
		
		
//		if (newParentLyr == null) {
//			logger.error("Unexpected moving to null layer");
//			return -1;
//		}
//
//		int oldLayersNdx = -1;
//
//		// moving to a different parent
//		if (oldParentAF != newParentAF) {
//			if (newParentAF instanceof CompositeLayer) {
//				oldParentAF.getShownChildren().remove(tgtAF);
//				newParentAF.getShownChildren().add(tgtAF);
//				tgtAF.setParentArt(newParentAF.getParentArt());
//			} else {
//				logger.info("AF-move parentAF: " + newParentAF + " movedAF: "+ tgtAF);
//				oldParentAF.getShownChildren().remove(tgtAF);
//				newParentAF.getShownChildren().add(tgtAF);
//				tgtAF.setParentArt(newParentAF);
//			}
//		} else {
//			logger.info("Lyr-move parentAF: " + newParentLyr + " movedAF: "+ tgtAF);
//		}
//		
//		
//		// creating a new layer
//		if (newParentLyr.isEmpty()) {
//			List<Layer> newParentLayers;
//			if (newParentAF instanceof CompositeLayer)
//				newParentLayers= ((CompositeLayer) newParentAF).getChildrenLayers();
//			else
//				newParentLayers = LayersDPolicy.getLayers(newParentAF);
//			if (newLayerNdx == -1) 
//				newLayerNdx = newParentLayers.size();
////			if (newParentAF instanceof CompositeLayer)
////				newParentLyr.add(tgtAF);
//			newParentLayers.add(newLayerNdx, newParentLyr);
////			if (oldParentLyr instanceof CompositeLayer)
////				oldParentLyr = new CompositeLayer(tgtAF.getRootArt().getRepo(), newParentLayers);
////			newParentLayers.add(newParentLyr);
//		}
//		
//		// adding to layer
//		oldParentLyr.removeShownChild(tgtAF);
//		if (newLayerChildNdx == -1)
//			newLayerChildNdx = newParentLyr.size();
//		if (!(newParentAF instanceof CompositeLayer))
//			newParentLyr.appendShownChild(tgtAF, newLayerChildNdx);
//		
//		
//		// remove empty layers
//		List<Layer> oldParentLayers = LayersDPolicy.getLayers(oldParentAF);
//		for(Layer layer : new ArrayList<Layer>(oldParentLayers)) {
//			if (layer.getShownChildrenCnt() == 0) {
//				oldLayersNdx = oldParentLayers.indexOf(layer);
//				oldParentLayers.remove(layer);
//			}
//		}
//		
//		// remove empty new layers  [dont think this ever happens]
//		List<Layer> newParentLayers = LayersDPolicy.getLayers(newParentAF);
//		for (Layer layer : new ArrayList<Layer>(newParentLayers)) {
//			if(layer.getShownChildrenCnt()==0) newParentLayers.remove(layer);
//		}
////		if (newParentAF instanceof CompositeLayer)
////			LayersDPolicy.setLayers(newParentAF, newParentLayers);
//		
//		return oldLayersNdx;
	}
}