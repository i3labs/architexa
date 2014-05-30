package com.architexa.diagrams.strata.ui;


import com.architexa.diagrams.model.ArtifactFragment;
import com.architexa.diagrams.strata.cache.PartitionerSupport;
import com.architexa.diagrams.strata.model.Layer;
import com.architexa.diagrams.strata.model.StrataRootDoc;
import com.architexa.diagrams.strata.model.policy.LayersDPolicy;
import com.architexa.org.eclipse.gef.commands.Command;

public class ReLayerCommand extends Command {
	
	private ArtifactFragment parentAF;
	private StrataRootDoc root;
	public ReLayerCommand(ArtifactFragment _af, StrataRootDoc root) {
		this.root = root;
		parentAF = _af;
	}

	@Override
	public void execute() {
		  LayersDPolicy.flushLayers(parentAF);
	      ArtifactFragment artFrag = (ArtifactFragment) parentAF;
	      PartitionerSupport.partitionLayers(root, artFrag);
	      LayersDPolicy.setLayersNeedBuilding(artFrag.getRootArt(), true);
	      artFrag.fireContentsChanged();
	      for (Layer l : LayersDPolicy.getLayers(artFrag)) {
	      	l.fireContentsChanged();
	      }
	      
	      return;
	}
	@Override
	public void undo() {
	}
}
