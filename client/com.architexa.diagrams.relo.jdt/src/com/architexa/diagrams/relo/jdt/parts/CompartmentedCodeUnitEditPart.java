/* 
 * Copyright (c) 2004-2005 Massachusetts Institute of Technology. This code was
 * developed as part of the Haystack (http://haystack.lcs.mit.edu/) research 
 * project at MIT. Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation files (the 
 * "Software"), to deal in the Software without restriction, including without 
 * limitation the rights to use, copy, modify, merge, publish, distribute, 
 * sublicense, and/or sell copies of the Software, and to permit persons to whom
 * the Software is furnished to do so, subject to the following conditions: 
 * 
 * The above copyright notice and this permission notice shall be included in 
 * all copies or substantial portions of the Software. 
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR 
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, 
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE 
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER  
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, 
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE. 
 */
/*
 * Created on Jun 13, 2004
 *
 */
package com.architexa.diagrams.relo.jdt.parts;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Transformer;
import org.apache.log4j.Logger;
import org.eclipse.swt.graphics.Color;
import org.openrdf.model.Resource;

import com.architexa.diagrams.draw2d.NonEmptyFigureSupport;
import com.architexa.diagrams.jdt.model.UserCreatedFragment;
import com.architexa.diagrams.model.ArtifactFragment;
import com.architexa.diagrams.model.ColorDPolicy;
import com.architexa.diagrams.model.DerivedArtifact;
import com.architexa.diagrams.relo.figures.CodeUnitFigure;
import com.architexa.diagrams.relo.jdt.ReloJDTPlugin;
import com.architexa.diagrams.relo.modelBridge.ControllerDerivedAF;
import com.architexa.diagrams.relo.parts.ArtifactEditPart;
import com.architexa.diagrams.relo.parts.MoreItemsEditPart;
import com.architexa.org.eclipse.draw2d.IFigure;
import com.architexa.org.eclipse.gef.EditPart;


/**
 * This class should really be able to go up higher in the chain, however, we
 * don't have multiple inheritance (and we need CodeUnitEditPart functionality)
 * so we are putting it after CodeUnitEditPart
 * 
 * @author vineet
 */
public abstract class CompartmentedCodeUnitEditPart extends CodeUnitEditPart {
	static final Logger logger = ReloJDTPlugin.getLogger(CompartmentedCodeUnitEditPart.class);


    public static abstract class CompartmentCodeUnit extends ControllerDerivedAF {
        public CompartmentCodeUnit(ArtifactFragment af) {
            super(af.getArt());
        }
        public abstract void registerTypes(Map<Resource, CompartmentCodeUnit> compartmentMap);
    }

    public static abstract class CompartmentCUEditPart extends MoreItemsEditPart {
        @Override
    	public List<ArtifactFragment> getModelChildren() {
        	CompartmentCodeUnit myCCU = (CompartmentCodeUnit) this.getModel();
        	Map<Resource, CompartmentCodeUnit> parentCompartmentMap = ((CompartmentedCodeUnitEditPart)this.getParent()).compartmentMap;
        	Set<Resource> tgtTypes = new HashSet<Resource>(parentCompartmentMap.size());
        	
        	// get all types that the compartment applies to
        	for (Map.Entry<Resource, CompartmentCodeUnit> compartmentMapEntry : parentCompartmentMap.entrySet()) {
				if (!compartmentMapEntry.getValue().equals(myCCU)) continue;
				tgtTypes.add(compartmentMapEntry.getKey());
			}
        	
        	// add all children of gotten types to the return
        	ArtifactFragment compartmentedCU = ((CompartmentedCodeUnitEditPart)this.getParent()).getArtifact();
        	List<ArtifactFragment> possChildren = compartmentedCU.getShownChildren();
        	List<ArtifactFragment> retChildren = new ArrayList<ArtifactFragment> ();
        	for (ArtifactFragment possChildAF : possChildren) {
        		if (possChildAF instanceof DerivedArtifact) continue;
        		//User Created
        		if (possChildAF instanceof UserCreatedFragment &&
						tgtTypes.contains(((UserCreatedFragment)possChildAF).queryType(getRepo())))
					retChildren.add(possChildAF);
				else if (tgtTypes.contains(possChildAF.getArt().queryType(possChildAF.getRootArt().getRepo()))) 
					retChildren.add(possChildAF);
			}
            return retChildren;
        }
    }
    
    // detail level
    final int labelWithCompartments = getMinimalDL();

    /* (non-Javadoc)
     * @see com.architexa.diagrams.relo.parts.ArtifactEditPart#getDLStr(int)
     */
    @Override
    public String getDLStr(int dl) {
        if (dl == labelWithCompartments)
            return "labelWithCompartments";
        return super.getDLStr(dl);
    }

    @Override
    protected void updateMembers(int newDL) {
        if (newDL >= currDL) {
            if (newDL >= labelWithCompartments) realizeCompartments();
        }
        if (newDL < currDL) {
            if (newDL == labelWithCompartments) {
                emptyCompartments();
            } else if (newDL < labelWithCompartments) {
                hideCompartments();
            }
        }
    }

    // in the below we only store ComparmentTypes, each of which can support
    // one or more types of Artifacts, note the order in compartmentTypes matters
    List<Resource> compartmentTypes = new ArrayList<Resource> (5);
    public abstract CompartmentCodeUnit generateCompartment(Resource compartmentType, ArtifactFragment compartmentParent);

    Map<Resource, CompartmentCodeUnit> compartmentMap = new HashMap<Resource, CompartmentCodeUnit> (5);

    private CompartmentCodeUnit getCompartmentForType(Resource compartmentType) {
		CompartmentCodeUnit compartmentTypeCCU = compartmentMap.get(compartmentType);
		return compartmentTypeCCU;
    }
    private CompartmentCUEditPart getCompartmentEPForType(Resource compartmentType) {
		CompartmentCodeUnit compartmentCU = getCompartmentForType(compartmentType);
		if (compartmentCU != null) 
			return (CompartmentCUEditPart) this.findEditPart(compartmentCU);
		return null;
    }
    
    // this is called by getModelChildren ... it can't depend on EP structure
	// because this is called during refresh to create the EP structure! ...and
	// if we depend on EP structure, GEF thinks that the model has no elements
	// ... hmm... shouldn't this just be a model accessor? ... perhaps this is
	// the cause of the problem
    protected List<CompartmentCodeUnit> getComparments() {
    	// we can have repeats, but order matters
    	List<CompartmentCodeUnit> retVal = new ArrayList<CompartmentCodeUnit> (compartmentMap.size());
    	for (Resource compartmentType : compartmentTypes) {
    		CompartmentCodeUnit compartmentTypeContainer = getCompartmentForType(compartmentType);
			if (compartmentTypeContainer != null && !retVal.contains(compartmentTypeContainer))
				retVal.add(compartmentTypeContainer);
		}
    	return retVal;
    }
    

    
    protected void realizeCompartments() {
    	try {
	    	for (Resource compartmentType : compartmentTypes) {
	    		CompartmentCUEditPart compartmentEP = getCompartmentEPForType(compartmentType);
	            if (compartmentEP == null) {
	                compartmentEP = realizeCompartment(compartmentType);
	            } else if (!this.getChildren().contains(compartmentEP)) {
	                // it was realized but has since been removed
	            	logger.error("Should not happen");
	            } else {
	                // already been realized...
	            }
	        }
        } catch (Throwable e) {
            logger.error("Unexpected exception", e);
        }
    }
    
    public void colorFigure(Color color) {
	}
    
    private CompartmentCUEditPart realizeCompartment(Resource compartmentType) {
    	// create the compartments
    	final CompartmentCodeUnit ccu = generateCompartment(compartmentType, getCU());
        ccu.registerTypes(compartmentMap);

        // install any needed policies
    	ArtifactFragment compAF = getRootController().getRootArtifact().getArtifact(ccu);
    	
    	// initialize the compartments
        getArtifact().addPropertyChangeListener(new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent evt) {
				ccu.firePropChang(evt.getPropertyName());
				String propName = evt.getPropertyName();
				if (ArtifactFragment.Policy_Contents_Changed.equals(propName)) {
					colorFigure(ColorDPolicy.getColor(getCU()));
				}
			}});
        
    	// update children
        this.refreshChildren();

        CompartmentCUEditPart cep = (CompartmentCUEditPart) getRootController().findArtifactEditPart(compAF);
        
        final IFigure classFigure = (IFigure) CompartmentedCodeUnitEditPart.super.getFigure().getChildren().get(0);
        if (classFigure!=null)
        	NonEmptyFigureSupport.listenToModel(getArtFrag(), ((CodeUnitFigure)classFigure).nonEmptyFigure);
        return cep;
    }

    protected void hideCompartments() {
        getModelChildren().clear();
        refresh();
    }

    protected void emptyCompartments() {
        List<EditPart> visChildren = getVisibleEditPartChildren();
        List<Object> txVisChildren = new ArrayList<Object>(visChildren);
        CollectionUtils.transform(txVisChildren, new Transformer(){
            public Object transform(Object input) {
                if (input instanceof ArtifactEditPart) {
                    return ((ArtifactEditPart)input).getArtifact();
                } else
                    return null;
            }});
        removeChildrenArtifacts(txVisChildren);
    }

    @Override
    protected List<EditPart> getVisibleEditPartChildren() {
        List<EditPart> childrenEP = new ArrayList<EditPart> (20);
        // use hashset since the map can 
        for (CompartmentCodeUnit ccu : getComparments() ) {
        	CompartmentCUEditPart cep = (CompartmentCUEditPart) this.findEditPart(ccu);
            childrenEP.addAll(cep.getChildrenAsTypedList());
        }
        return childrenEP;
    }
    
    @Override
	public List<ArtifactFragment> getModelChildren() {
        return new ArrayList<ArtifactFragment> (getComparments());
    }

    
}
