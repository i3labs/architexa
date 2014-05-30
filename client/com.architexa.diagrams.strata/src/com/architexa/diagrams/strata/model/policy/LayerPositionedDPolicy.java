package com.architexa.diagrams.strata.model.policy;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.rio.RdfDocumentWriter;

import com.architexa.diagrams.RSECore;
import com.architexa.diagrams.model.ArtifactFragment;
import com.architexa.diagrams.model.DiagramPolicy;
import com.architexa.diagrams.strata.commands.CreateVertLayerAndMoveCmd;
import com.architexa.diagrams.strata.model.CompositeLayer;
import com.architexa.diagrams.strata.model.Layer;
import com.architexa.diagrams.strata.model.StrataFactory;
import com.architexa.store.ReloRdfRepository;
import com.architexa.store.StoreUtil;

public class LayerPositionedDPolicy extends DiagramPolicy{
	////
	// basic setup
	////
	public static final String DefaultKey = "LayerPositionedDPolicy";
	public static final LayerPositionedDPolicy Type = new LayerPositionedDPolicy();
	
	public class LayerPolicyInfo {
		public boolean isHorz;
		public boolean multiLayer;
		public int index;
		public boolean composite;

		LayerPolicyInfo(int index, boolean multiLayer, boolean composite, boolean isHorz) {
			this.index = index;
			this.multiLayer = multiLayer;
			this.isHorz = isHorz;
			this.composite = composite;
		}
	}
	
	
	////
	// Policy Fields, Constructors and Methods 
	////
	public static final URI layerIndexURI = RSECore.createRseUri("strata#layerIndex");
	public static final URI indexWithinLayerURI = RSECore.createRseUri("strata#indexWithinLayer");
	public static final URI nestedLayerIndexesURI = RSECore.createRseUri("strata#nestedLayerIndexes");
	
	private ArrayList<LayerPolicyInfo> layerInfoToWrite;
	
	public LayerPositionedDPolicy() {}
	
	@Override
	public void writeRDF(RdfDocumentWriter rdfWriter) throws IOException {
		int layerIndex = 0;
		int indexWithinLayer = 0;
		// get parent LayerDPolicy
		// for each child layer find host
		// layer = index
		// this index = layers.getIndex(host)
		LayersDPolicy parentLayers = (LayersDPolicy) getHostAF().getParentArt().getDiagramPolicy(LayersDPolicy.DefaultKey);
		layerInfoToWrite = new ArrayList<LayerPolicyInfo>();
		findLayerIndexes(parentLayers.getLayers(), layerInfoToWrite);

		for (Layer layer : parentLayers.getLayers()) {
			if (layer.contains(getHostAF())) {
				layerIndex = parentLayers.getLayers().indexOf(layer);
				indexWithinLayer = layer.getChildren().indexOf(getHostAF());
			}
		}
		
		rdfWriter.writeStatement(getHostAF().getInstanceRes(), nestedLayerIndexesURI, StoreUtil.createMemLiteral(createStringToWrite(layerInfoToWrite)));
		rdfWriter.writeStatement(getHostAF().getInstanceRes(), layerIndexURI, StoreUtil.createMemLiteral(Integer.toString(layerIndex)));
		rdfWriter.writeStatement(getHostAF().getInstanceRes(), indexWithinLayerURI, StoreUtil.createMemLiteral(Integer.toString(indexWithinLayer)));
	}

	private String createStringToWrite( ArrayList<LayerPolicyInfo> layerInfoToWrite) {
		String layerStatement = "";
		for (LayerPolicyInfo lpi : layerInfoToWrite) {
			layerStatement = layerStatement + ":"+lpi.index+","+lpi.multiLayer+","+lpi.composite+","+lpi.isHorz;
		}
		return layerStatement;
	}

	private boolean findLayerIndexes(List<Layer> parentLayers, List<LayerPolicyInfo> layerInfoToWrite) {
		for (Layer layer : parentLayers) {
			if (layer instanceof CompositeLayer) {
				boolean retVal = findLayerIndexes(((CompositeLayer) layer).getChildrenLayers(), layerInfoToWrite);
				if (retVal) {
					layerInfoToWrite.add(new LayerPolicyInfo(parentLayers.indexOf(layer), ((CompositeLayer) layer).getMultiLayer(), true, layer.getLayout()));
					return true;
				}
			} else if (layer instanceof Layer) {
				if (!layer.contains(getHostAF())) continue;
				
				layerInfoToWrite.add(new LayerPolicyInfo(layer.getChildren().indexOf(getHostAF()), false, false, layer.getLayout()));
				layerInfoToWrite.add(new LayerPolicyInfo(parentLayers.indexOf(layer), false, false, layer.getLayout()));
				return true;
			}
		}
		return false;
	}

	
	@Override
	public void readRDF(ReloRdfRepository queryRepo) {
		ReloRdfRepository repo = queryRepo;//getHost().getRootArt().getRepo();
		Value newLayerNdxValue = queryRepo.getStatement(getHostAF().getInstanceRes(), layerIndexURI, null).getObject();
		Value newLayerChildNdxValue = queryRepo.getStatement(getHostAF().getInstanceRes(), indexWithinLayerURI, null).getObject();
		Value nestedLayerIndexesValue = queryRepo.getStatement(getHostAF().getInstanceRes(), nestedLayerIndexesURI, null).getObject();
		LayersDPolicy parentLayers = (LayersDPolicy) getHostAF().getParentArt().getDiagramPolicy(LayersDPolicy.DefaultKey);
		boolean gotNestedLayers = false;
		
		// since layers can be nested within compositelayers/multilayers we must
		// make sure we keep track of their indicies and types so we can rebuild
		// them when we load
		if (nestedLayerIndexesValue != null) {
			String label = ((Literal)nestedLayerIndexesValue).getLabel();
			label = label.replaceFirst(":", "");
			
			String[] spiList = label.split(":");
			reverseArray(spiList);
			gotNestedLayers = readNestedLayerInfo(repo, getHostAF().getParentArt(), spiList, null);
		} 
		if (!gotNestedLayers) {
			
			if (newLayerChildNdxValue == null || newLayerNdxValue == null) return;
			int newLayerNdx = Integer.parseInt(((Literal)newLayerNdxValue).getLabel());
			int newLayerChildNdx = Integer.parseInt(((Literal)newLayerChildNdxValue).getLabel());
			if (newLayerNdx == -1) newLayerNdx = 0;
			if (newLayerChildNdx == -1) newLayerChildNdx = 0;
			
			
			if (parentLayers.getLayers().isEmpty()) 
				parentLayers.getLayers().add(new Layer(repo));
			
			if (parentLayers.getLayers().size() > newLayerNdx) {
				List<ArtifactFragment> thisLayer = parentLayers.getLayers().get(newLayerNdx).getChildren();
				if (thisLayer.size() > newLayerChildNdx)
					thisLayer.add(newLayerChildNdx, getHostAF());
				else thisLayer.add(getHostAF());
				
			} else {
				
				while (parentLayers.getLayers().size() <= newLayerNdx ) {
					parentLayers.getLayers().add(new Layer(repo));
				}
				List<ArtifactFragment> thisLayer = parentLayers.getLayers().get(newLayerNdx).getChildren();
				if (thisLayer.size() > newLayerChildNdx)
					thisLayer.add(newLayerChildNdx, getHostAF());
				else thisLayer.add(getHostAF());	
			}
		}
	}

	

	private boolean readNestedLayerInfo(ReloRdfRepository repo, ArtifactFragment parent, String[] spiList, ArtifactFragment grandParent) {
		String spi = spiList[0];
		if (spi  == null || spi.equals("")) { // we are a child of the root
			return false;
		}
		String[] spiData = spi.split(",");
		
		int index = Integer.parseInt(spiData[0]);
		boolean isMultiLayer = Boolean.parseBoolean(spiData[1]);
		boolean isComposite = Boolean.parseBoolean(spiData[2]);
		boolean isHorz = Boolean.parseBoolean(spiData[3]);
		if (index == -1) index = 0;

		// we have reached the bottom layer
		if (spiList.length == 1) {
			if (!(parent instanceof Layer)) return false;
			// if we are adding directly to a compositeLayer then it is because
			// we had to add it as a placeholder. Remove it and replace it with
			// a regular layer
			if (parent instanceof CompositeLayer && grandParent!=null) {
				LayersDPolicy gpLayers = (LayersDPolicy) grandParent.getDiagramPolicy(LayersDPolicy.DefaultKey);
				int ndx = gpLayers.getLayers().indexOf(parent);
				boolean layout = ((CompositeLayer) parent).getLayout();
				gpLayers.getLayers().remove(parent);
				
				parent = new Layer(repo, layout);
				StrataFactory.initAF(parent);
				((Layer)parent).add(getHostAF());
				if (ndx !=-1)
					gpLayers.getLayers().add(ndx, (Layer) parent);
				else
					gpLayers.getLayers().add((Layer) parent);
			} else {
				if (parent.getShownChildrenCnt() > index)
					((Layer)parent).add(getHostAF(),index);
				else	
					((Layer)parent).add(getHostAF());
			}
			return true;
		}

		// get the children of the CompositeLayer / ArtFrag
		List<Layer> children = new ArrayList<Layer>();
		if (parent instanceof CompositeLayer) {
			children = ((CompositeLayer) parent).getChildrenLayers();
		} else { // instance of root or TitledEP
			LayersDPolicy parentLayers = (LayersDPolicy) parent.getDiagramPolicy(LayersDPolicy.DefaultKey);	
			// parentLayers may be null if parent is somehow a layer? TODO: Could be a bug with vertical layers 
			if (parentLayers != null ) 
				children = parentLayers.getLayers();
		}
		
		// add layers if necessary
		if (children.size() <= index) {
			while (children.size() <= index ) {
				addLayer(repo, children, isComposite, isHorz, isMultiLayer, index);
			}
		} else if(!(children.get(index) instanceof CompositeLayer) && isMultiLayer) { // if we have added a placeholder layer and it needs to be a multi layer, switch it
			children.remove(index);
			addLayer(repo, children, isComposite, isHorz, isMultiLayer, index);
		}
		
		// recurse
		spiList = popArray(spiList);
		return readNestedLayerInfo(repo, (ArtifactFragment) children.get(index), spiList, parent);
	}
	private void addLayer(ReloRdfRepository repo, List<Layer> children, boolean isComposite, boolean isHorz, boolean isMultiLayer, int index) {
		Layer newLayer;
		if (isComposite) {
			newLayer= new CompositeLayer(repo, new Layer[]{});
			newLayer.setLayout(isHorz);
			((CompositeLayer) newLayer).setMultiLayer(isMultiLayer);
		} else
			newLayer = new Layer(repo, isHorz);
		if (children.size() > index && index!=-1)
			children.add(index, newLayer);
		else
			children.add(newLayer);
	}

	private String[] popArray(String[] spiList) {
		String[] retArr = new String[spiList.length-1];
		for (int i=0; i<spiList.length-1; i++ ) {
			retArr[i] = spiList[i+1];
		}
		return retArr;
	}

	private void reverseArray(String[] array) {
		 int left  = 0;          // index of leftmost element
		   int right = array.length-1; // index of rightmost element
		  
		   while (left < right) {
		      // exchange the left and right elements
		      String temp = array[left]; 
		      array[left]  = array[right]; 
		      array[right] = temp;
		     
		      // move the bounds toward the center
		      left++;
		      right--;
		   }
	}
	
	public static boolean containsVertLayer(ArtifactFragment artFrag, boolean onRight) {
		List<Layer> oldLayers = LayersDPolicy.getLayers(artFrag);
		boolean alreadyVertLayer = oldLayers.get(0) instanceof CompositeLayer && !((CompositeLayer) oldLayers.get(0)).getMultiLayer();
		List<Layer> existingRightVertLayers = new ArrayList<Layer>();
		List<Layer> existingLeftVertLayers = new ArrayList<Layer>();
		if (alreadyVertLayer)
			CreateVertLayerAndMoveCmd.collectVertLayers(existingLeftVertLayers, existingRightVertLayers, oldLayers);
		if (onRight)
			return !existingRightVertLayers.isEmpty();
		return !existingLeftVertLayers.isEmpty();
	}
}
