package com.architexa.diagrams.strata.figures;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.graphics.Font;

import com.architexa.diagrams.draw2d.UnderlinableLabel;
import com.architexa.diagrams.jdt.builder.asm.LogicalContainmentHeirarchyProcessor;
import com.architexa.diagrams.model.Artifact;
import com.architexa.diagrams.model.ArtifactFragment;
import com.architexa.diagrams.strata.commands.AddChildrenCommand;
import com.architexa.diagrams.strata.commands.DeleteCommand;
import com.architexa.diagrams.strata.model.Layer;
import com.architexa.diagrams.strata.model.StrataRootDoc;
import com.architexa.diagrams.strata.model.policy.ClosedContainerDPolicy;
import com.architexa.diagrams.strata.model.policy.LayersDPolicy;
import com.architexa.diagrams.strata.parts.ContainableEditPart;
import com.architexa.diagrams.strata.parts.StrataRootEditPart;
import com.architexa.diagrams.utils.UILinkUtils;
import com.architexa.org.eclipse.draw2d.ColorConstants;
import com.architexa.org.eclipse.draw2d.Figure;
import com.architexa.org.eclipse.draw2d.MouseEvent;
import com.architexa.org.eclipse.draw2d.MouseListener;
import com.architexa.org.eclipse.draw2d.MouseMotionListener;
import com.architexa.org.eclipse.gef.commands.CompoundCommand;
import com.architexa.store.ReloRdfRepository;

/*
 * Utility Class for adding Links to Label Figures. 
 * Used in strata to add links to the root figure context label, 
 * and the labels of 'merged packages'
 */
public class LinkUtils {
	
	/**
	 * called by rootEP - it does not have a font parameter. root links do not
	 * need to change size based on number of connections
	 */
	public static void convertToLabelLinks(Figure parentFig, String currPackageCntx, String contextLbl, ReloRdfRepository repo, final ContainableEditPart ep) {
		convertToLabelLinks(parentFig, currPackageCntx, contextLbl, repo, ep, null);
	}
	
	/**
	 * converts the contextLabel into a group of labels separated by '.'s and
	 * added to parentFig cannot return the Figure because the figure has
	 * already been initialized and added to the root
	 */
	public static void convertToLabelLinks(Figure parentFig, String currPackageCntx, String contextLbl, ReloRdfRepository repo, final ContainableEditPart ep, final Font font) {
		parentFig.removeAll();
		if (currPackageCntx == null )return;
		String parentPackages = "";
		// remove whitespace
		currPackageCntx= currPackageCntx.trim();
		
		// separate full context into its parts
		String[] splitCntx = currPackageCntx.split("\\.");
		
		// if this is a package within a packageFolder of the same name switch label to "(this)"
		if (!contextLbl.equals("") && contextLbl.substring(0, contextLbl.length()-1).equals(currPackageCntx) ) { 
			parentFig.add(new UnderlinableLabel("(this)"));
			return;
		}
		
		// if context is empty we are a root item with no context
		// still need to add a label. so get context from full path
		if (splitCntx.length == 0 || splitCntx[0].equals("")) {
			// Need to process context label specially for root elements
			if (ep.getRootModel().getShownChildrenCnt()==1) {
				if (contextLbl.contains(".*"))
					contextLbl = contextLbl.substring(0, contextLbl.indexOf(".*"));
				if (contextLbl.contains("."))
					contextLbl = contextLbl.substring(contextLbl.lastIndexOf(".")+1);
				
				parentFig.add(new UnderlinableLabel(contextLbl));
			} else if (ep instanceof StrataRootEditPart)
				parentFig.add(new UnderlinableLabel(""));
			else if (ep.getArtFrag().getParentArt() instanceof StrataRootDoc)
				parentFig.add(new UnderlinableLabel("(this)"));
			else
				parentFig.add(new UnderlinableLabel(".*"));
			return;
		}
		
		for (int i = 0; i < splitCntx.length; i++) {
			final String packagePart  = splitCntx [i];
			// ignore *s
			if (packagePart.equals("*")) continue;
			UnderlinableLabel packagePartLabel = new UnderlinableLabel(packagePart);
			final CompoundCommand cc = new CompoundCommand("Add Parent Context");
			
			// do not add final item in context or *s as links since these are already in the diagram
			// font is null for root elements
			if (currPackageCntx.contains("*") && i == splitCntx.length-2 && font!=null) {
				parentFig.add(packagePartLabel);
				continue;
			} else if (i == splitCntx.length-1 && font!=null) {
				parentFig.add(packagePartLabel);
				continue;
			} 
			
			// create the string of the package that will be added. 
			String packageToAddString = createPckgString(ep, contextLbl, currPackageCntx, repo);
			packageToAddString += parentPackages + packagePart + ".*";
			LinkUtils.getContextAddCmd(cc, ep, packageToAddString, repo);
			packagePartLabel.setToolTip(new UnderlinableLabel("Select a parent to see package: " + packageToAddString));
			
			// add listeners
			addLinksAndListeners(packagePartLabel, ep, cc);

			parentFig.add(packagePartLabel);
			parentFig.add(new UnderlinableLabel("."));
			
			// maintain context as we add each label
			parentPackages += packagePart + ".";
		}
			
	}

	
	
	// contextLbl is used to add the current context to the beginning of
	// the string since the command needs the full context
	
	// we need to add the context to the link in cases where the label is not showing the full context.
	// 1: when parent is the root (context is shown in rootFigure Links
	// mp on root with context (context) 
	// 2: when one of our parents is already in the diagram and we are a child
	// merged package within correct parent (maps.a.b.c within lapis.*) (context)

	// root links  (no context)
	// mp on root with no context (no context)
	// mp in incorrect parent package (no context)
	private static String createPckgString(ContainableEditPart ep, String contextLbl, String currPackageCntx, ReloRdfRepository repo) {
		String packageToAddString = "";
		Artifact queriedParentArt = ep.getArtFrag().getArt().queryParentArtifact(repo);
		String resString = ep.getArtFrag().getArt().elementRes.toString();
		resString = resString.substring(resString.indexOf("#")+1);
		resString = resString.replace("$", ".");
		// we were originally testing for cases where the parent was a project.
		// This causes problems in the case of opening the lapis package and
		// attempting to add the 'maps' of 'maps.a.b'. Pehaps broke when
		// changing how context labels were calculated
		if (queriedParentArt!= null && /*!queriedParentArt.elementRes.toString().contains("rse-eclipseProj#")) &&*/ !resString.startsWith(currPackageCntx))
			packageToAddString += contextLbl;
		else if (!(ep.getArtFrag().getParentArt() instanceof StrataRootDoc) && !resString.startsWith(currPackageCntx))
			packageToAddString += contextLbl;
		
		// we need to make sure we join strings with one and only one '.'
		if (!packageToAddString.equals("") && !packageToAddString.endsWith(".")) 
			packageToAddString += ".";
		
		return packageToAddString; 
	}
	
	
	private static void addLinksAndListeners(UnderlinableLabel label, final ContainableEditPart ep, final CompoundCommand cc) {
		label.addMouseListener(new MouseListener() {
			public void mouseReleased(MouseEvent me) {}
			public void mousePressed(MouseEvent me) {
				ep.execute(cc);
			}
			public void mouseDoubleClicked(MouseEvent me) {}
		});
		label.addMouseMotionListener(new MouseMotionListener.Stub() {
			@Override
			public void mouseEntered(MouseEvent me) {
				if(!(me.getSource() instanceof UnderlinableLabel)) return;
				((UnderlinableLabel)me.getSource()).setForegroundColor(ColorConstants.blue);//ColorScheme.chainedCallPieceHighlight);
				UILinkUtils.setUnderline((UnderlinableLabel)me.getSource(), true);
			}
			@Override
			public void mouseExited(MouseEvent me) {
				if(!(me.getSource() instanceof UnderlinableLabel)) return;
				UnderlinableLabel label = (UnderlinableLabel) me.getSource();
				label.setForegroundColor(ColorConstants.black);
				UILinkUtils.setUnderline(label, false);
			}
		});
	}

	private static void getContextAddCmd(CompoundCommand cc, ContainableEditPart ep, String packageToAddId, ReloRdfRepository repo) {
		StrataRootDoc rootModel = ep.getRootModel();

		Artifact childArt = new Artifact(LogicalContainmentHeirarchyProcessor.packageDirToResource(repo, packageToAddId));
		ArtifactFragment childAF = rootModel.createArtFrag(childArt);
		
		ArrayList<ArtifactFragment> originalChildren = new ArrayList<ArtifactFragment>();
        ArrayList<Layer> originalLayers = null;
		if (ep instanceof StrataRootEditPart) {
			originalChildren.addAll(ep.getArtFrag().getShownChildren());
			originalLayers = new ArrayList<Layer>(LayersDPolicy.getLayers(ep.getArtFrag()));
		} else
			originalChildren.add(ep.getArtFrag());

		// delete the old children
		for (ArtifactFragment orgAF : originalChildren) {
			cc.add(new DeleteCommand("", orgAF, rootModel, ep.getArtFrag().getParentArt()));	
		}

		// we need the current layer so that the node remains in position after we add the parent
		Layer layer = getEPLayer(ep, repo);
		int layerNdx = layer.getChildren().indexOf(ep.getArtFrag());
		ClosedContainerDPolicy.addAndShowChild(cc, ep.getArtFrag().getParentArt(), childAF, rootModel, layer, layerNdx);

        // add the children (and the layers)
		cc.add(new AddChildrenCommand(originalChildren, originalLayers, childAF, ep.getArtFrag(), rootModel));

		// open the clicked parent and make sure it doesnt break 
		ClosedContainerDPolicy.showChildren(cc, childAF, rootModel);
		ClosedContainerDPolicy.setBreakable(childAF, false);
	}
	
	
	private static Layer getEPLayer(ContainableEditPart ep, ReloRdfRepository repo) {
		if (ep.getParent().getModel() instanceof Layer) return (Layer) ep.getParent().getModel() ;
		// Not sure why the below was needed. The above case should always return 
		List<Layer> layers = LayersDPolicy.getLayers(ep.getArtFrag().getParentArt());
		for (Layer layer : layers) {
			if (layer.contains(ep.getArtFrag())) return layer; 
		}
		return new Layer(repo);
	}

}
