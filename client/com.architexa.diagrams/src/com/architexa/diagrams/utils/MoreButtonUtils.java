package com.architexa.diagrams.utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JPopupMenu.Separator;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.swt.graphics.Font;

import com.architexa.diagrams.eclipse.gef.MenuButton;
import com.architexa.diagrams.model.Artifact;
import com.architexa.diagrams.model.ArtifactFragment;
import com.architexa.diagrams.parts.BasicRootController;
import com.architexa.diagrams.ui.MultiAddCommandAction;
import com.architexa.org.eclipse.draw2d.ColorConstants;
import com.architexa.org.eclipse.draw2d.Figure;
import com.architexa.org.eclipse.draw2d.IFigure;
import com.architexa.org.eclipse.draw2d.Label;
import com.architexa.org.eclipse.draw2d.LineBorder;
import com.architexa.org.eclipse.draw2d.PositionConstants;
import com.architexa.org.eclipse.draw2d.ToolbarLayout;
import com.architexa.org.eclipse.draw2d.Triangle2;
import com.architexa.org.eclipse.draw2d.geometry.Insets;
import com.architexa.org.eclipse.draw2d.geometry.Rectangle;
import com.architexa.org.eclipse.gef.commands.CompoundCommand;

public class MoreButtonUtils {
	public static final String showAllId = "com.architexa.diagrams.NavAidsEditPolicy.showAll";

	public static void addShowAllItem(final IMenuManager menu, final BasicRootController rc) {
		IAction showAllAction = new Action("Show All") {
	        @Override
	        public void run() {
	        	CompoundCommand showAllCmd = new CompoundCommand("Show All");
		    	Map<Artifact, ArtifactFragment> addedArtToAFMap = new HashMap<Artifact, ArtifactFragment>();
	        	recursiveAddMenuToCmd(menu, showAllCmd, addedArtToAFMap);
	        	rc.execute(showAllCmd);
	        }
			private void recursiveAddMenuToCmd(IMenuManager menu, CompoundCommand showAllCmd, Map<Artifact, ArtifactFragment> addedArtToAFMap) {
				for (IContributionItem menuItem : menu.getItems()) {
	        		MultiAddCommandAction maca = getMultiAddCommandAction(menuItem);
	        		addCommand(maca, showAllCmd, addedArtToAFMap);
	        		if (menuItem instanceof IMenuManager && !(menuItem instanceof Separator))
						recursiveAddMenuToCmd((IMenuManager) menuItem, showAllCmd, addedArtToAFMap);
				}
				
			}
			private MultiAddCommandAction getMultiAddCommandAction(IContributionItem menuItem) {
	    		if (!(menuItem instanceof ActionContributionItem)) return null;
	
				IAction action = ((ActionContributionItem)menuItem).getAction();
				if (action instanceof MultiAddCommandAction) 
					return (MultiAddCommandAction) action;
				else
					return null;
			}
		};
		showAllAction.setId(showAllId);
		menu.add(showAllAction);
	}

	public static void addCommand(MultiAddCommandAction maca, CompoundCommand showAllCmd, 
			Map<Artifact, ArtifactFragment> addedArtToAFMap) {
		if (maca != null)
			showAllCmd.add(maca.getCommand(addedArtToAFMap));
	}

	public static IFigure getMoreButtonTriangle() {
		Figure dblArrowFig = new Figure();
		dblArrowFig.add(MoreButtonUtils.drawMoreButtonTriangle(1,2));
		dblArrowFig.add(MoreButtonUtils.drawMoreButtonTriangle(2,2));
		dblArrowFig.add(MoreButtonUtils.drawMoreButtonTriangle(5,2));
		dblArrowFig.add(MoreButtonUtils.drawMoreButtonTriangle(6,2));
		dblArrowFig.setPreferredSize(11,8);
	return dblArrowFig;
	}
	
	private static Triangle2 drawMoreButtonTriangle(int x, int y) {
		Triangle2 t;
		t = new Triangle2();
		t.setClosed(false);
		t.setFill(false);
		t.setBounds(new Rectangle(x, y, 5, 5));
		t.setBackgroundColor(com.architexa.org.eclipse.draw2d.ColorConstants.black);
		t.setDirection(PositionConstants.EAST);
		return t;
	}
	public static void setMoreButtonBounds(IFigure moreButton, Rectangle parentBounds) {
		Rectangle bounds = Rectangle.SINGLETON;
		if (parentBounds.width >= moreButton.getPreferredSize().width)
			bounds = new Rectangle(parentBounds.width +parentBounds.x - moreButton.getPreferredSize().width,
				parentBounds.height +parentBounds.y,
				moreButton.getPreferredSize().width,
				moreButton.getPreferredSize().height);
		else
			bounds = new Rectangle(parentBounds.x,
					parentBounds.height +parentBounds.y,
					moreButton.getPreferredSize().width,
					moreButton.getPreferredSize().height);
		
		
		moreButton.setBounds(bounds);
		
	}

	public static void setMoreButtonBounds(IFigure moreButton, IFigure parentFigure) {
		// gets the bounds from the parent figure 
		Rectangle bounds = parentFigure.getBounds().getCopy();
		
		if (parentFigure.getBorder() != null) {
			// crop the parent figures border
			Insets parentBorderInsets = parentFigure.getBorder().getInsets(parentFigure);
			bounds.crop(parentBorderInsets);
		}

		MoreButtonUtils.setMoreButtonBounds(moreButton, bounds);
	}

	public static Figure setProperties(MenuButton menuButton, Font font, String toolTip) {
		Figure moreButton = new Figure();
		moreButton.add(menuButton);
		moreButton.setBackgroundColor(ColorConstants.button);
		moreButton.setOpaque(true);
		moreButton.setLayoutManager(new ToolbarLayout() );
		moreButton.setFont(font);
		moreButton.setBorder(new LineBorder());
		moreButton.setToolTip(new Label(toolTip));
		return moreButton;
		
	}
	
	public static int containsAnonThis(List<Artifact> list) {
		int retVal = 0;
		for (Artifact art : list) {
			if (art.elementRes.toString().contains("this$")) 
				retVal++;
		}
		return retVal;
	}
	
    public static String fixAnonFinalVars(String label) {
    	if (label.contains("val$")) {
			label = label.replace("val$", "");
			if (label.contains(":"))
				label = label.replace(":", " (final variable) :");
		}
    	if (label.contains("##")) {
    		label = label.replace("##", "#");
    	}
    	return label;
	}
}
