package com.architexa.diagrams.strata.commands;

import java.util.ArrayList;
import java.util.List;


import com.architexa.diagrams.model.ArtifactFragment;
import com.architexa.diagrams.strata.model.CompositeLayer;
import com.architexa.diagrams.strata.model.Layer;
import com.architexa.diagrams.strata.model.policy.LayersDPolicy;
import com.architexa.org.eclipse.gef.commands.Command;
import com.architexa.store.ReloRdfRepository;

public class CreateVertLayerAndMoveCmd extends Command {
	
	private Layer orgParentLayerAF;
	private ArtifactFragment strataAFToAddToLayer;
	private ReloRdfRepository repo;
	private ArtifactFragment parentTitledAF;
	private ArrayList<Layer> newLayers;
	private CompositeLayer newContainerLayer;
	private List<Layer> oldLayers;
	private int afNdx;
	private Layer existingVertLayer;
	private boolean isOnRight;

	public CreateVertLayerAndMoveCmd(ArtifactFragment strataAFToAddToLayer, Layer orgParentLayerAF, ArtifactFragment parentTitledAF) {
		super("Move to Vertical Layer on Right");
		this.orgParentLayerAF = orgParentLayerAF;
		this.strataAFToAddToLayer = strataAFToAddToLayer;
		this.parentTitledAF = parentTitledAF;
		this.repo = strataAFToAddToLayer.getRootArt().getRepo();
		this.afNdx = orgParentLayerAF.getChildren().indexOf(strataAFToAddToLayer);
		this.isOnRight = true;
	}
	public CreateVertLayerAndMoveCmd(ArtifactFragment strataAFToAddToLayer, Layer orgParentLayerAF, ArtifactFragment parentTitledAF, boolean isOnRight) {
		super("Move to Vertical Layer on Left");
		this.orgParentLayerAF = orgParentLayerAF;
		this.strataAFToAddToLayer = strataAFToAddToLayer;
		this.parentTitledAF = parentTitledAF;
		this.repo = strataAFToAddToLayer.getRootArt().getRepo();
		this.afNdx = orgParentLayerAF.getChildren().indexOf(strataAFToAddToLayer);
		this.isOnRight = false;
	}

	public String getStr(ArtifactFragment strataAF) {
		if (strataAF == null) return "{null}";
		return strataAF + "[" + strataAF.getClass() + "]";
	}

	
	@Override
	public void execute() { 
		//remove from cur layer
		orgParentLayerAF.removeShownChild(strataAFToAddToLayer);
		
		oldLayers = LayersDPolicy.getLayers(parentTitledAF);
		// TODO: refactor, this is duplicated in LayerPositionedDPolicy.containsVertLayer()
		List<Layer> existingRightVertLayers = new ArrayList<Layer>();
		List<Layer> existingLeftVertLayers = new ArrayList<Layer>();
		boolean alreadyVertLayer = oldLayers.get(0) instanceof CompositeLayer && !((CompositeLayer) oldLayers.get(0)).getMultiLayer();
		if (alreadyVertLayer)
			collectVertLayers(existingLeftVertLayers, existingRightVertLayers, oldLayers);		
		
		// create left side
		Layer[] leftLayers = new Layer[oldLayers.size()];
		int i = 0;
		for (Layer layer : oldLayers) {
			leftLayers[i] = layer;
			i++;
		}
		
		//create new parent composite layer 
		Layer[] newContainerLayerArray = new Layer[2];
		
		
		// create new vert layer and add selected AF
		Layer newVertLayer = new Layer(repo, false);
		newVertLayer.add(strataAFToAddToLayer);
		
		
		if (isOnRight) { // here we are adding either to an existing layer on the same side, or creating a new one
			if (!existingRightVertLayers.isEmpty()) {
				addToExistingVertLayer(existingRightVertLayers.get(0));
				return;
			}
			newContainerLayerArray[0] = new CompositeLayer(repo, leftLayers, false);
			newContainerLayerArray[1] = newVertLayer;
		} else {
			if (!existingLeftVertLayers.isEmpty()) {
				addToExistingVertLayer(existingLeftVertLayers.get(0));
				return;
			}
			newContainerLayerArray[0] = newVertLayer;
			newContainerLayerArray[1] = new CompositeLayer(repo, leftLayers, false);
		}
		
		newContainerLayer = new CompositeLayer(repo, newContainerLayerArray, true);
		
		if (alreadyVertLayer && oldLayers.size()==1) { //here we are adding a new vertical layer on the opposite side of an existing one
			newContainerLayer = (CompositeLayer) oldLayers.get(0);	
			if (isOnRight)
				newContainerLayer.add(newVertLayer);
			else
				newContainerLayer.getChildren().add(0, newVertLayer);
		}
		newLayers = new ArrayList<Layer>();
		newLayers.add(newContainerLayer);
		LayersDPolicy.setLayers(parentTitledAF, newLayers);
		LayersDPolicy.setLayersNeedBuilding(parentTitledAF.getRootArt(), false);
	}
	
	private void addToExistingVertLayer(Layer existingVertLayer) {
		this.existingVertLayer = existingVertLayer; 
		existingVertLayer.add(strataAFToAddToLayer);
		LayersDPolicy.setLayers(parentTitledAF, oldLayers);
	}
	public static void collectVertLayers(List<Layer> existingLeftVertLayers, List<Layer> existingRightVertLayers, List<Layer> oldLayers) {
		List<Layer> childLayers = ((CompositeLayer)oldLayers.get(0)).getChildrenLayers();
		// if we already have a vert layer on the same level then just add another
		boolean pastMiddle = false;
		for (Layer parentLayer : childLayers) {
			if (parentLayer instanceof CompositeLayer) { 
				pastMiddle = true;
				continue;
			}
			if (parentLayer.getLayout()) continue;
			if (pastMiddle)
				existingRightVertLayers.add(parentLayer);
			else
				existingLeftVertLayers.add(parentLayer);
		}
	}
	
	
	@Override
	public void undo(){
		if (newLayers == null && existingVertLayer!=null)
			existingVertLayer.remove(strataAFToAddToLayer);
		else
			newLayers.remove(newContainerLayer);
		orgParentLayerAF.appendShownChild(strataAFToAddToLayer, afNdx);
		LayersDPolicy.setLayers(parentTitledAF, oldLayers);		
	}
}
