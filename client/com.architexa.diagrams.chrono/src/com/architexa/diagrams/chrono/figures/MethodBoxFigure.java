package com.architexa.diagrams.chrono.figures;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.graphics.Color;

import com.architexa.diagrams.chrono.animation.AnimationLayoutManager;
import com.architexa.diagrams.chrono.editparts.DiagramEditPart;
import com.architexa.diagrams.chrono.models.MethodBoxModel;
import com.architexa.diagrams.chrono.ui.ColorScheme;
import com.architexa.diagrams.chrono.util.ConnectionUtil;
import com.architexa.diagrams.chrono.util.LayoutUtil;
import com.architexa.diagrams.chrono.util.SeqUtil;
import com.architexa.org.eclipse.draw2d.ColorConstants;
import com.architexa.org.eclipse.draw2d.ConnectionLayer;
import com.architexa.org.eclipse.draw2d.Figure;
import com.architexa.org.eclipse.draw2d.Graphics;
import com.architexa.org.eclipse.draw2d.IFigure;
import com.architexa.org.eclipse.draw2d.Label;
import com.architexa.org.eclipse.draw2d.MarginBorder;
import com.architexa.org.eclipse.draw2d.RectangleFigure;
import com.architexa.org.eclipse.draw2d.StackLayout;
import com.architexa.org.eclipse.draw2d.ToolbarLayout;
import com.architexa.org.eclipse.draw2d.geometry.Dimension;
import com.architexa.org.eclipse.draw2d.geometry.Point;
import com.architexa.org.eclipse.draw2d.geometry.Rectangle;

/**
 * 
 * @author Elizabeth L. Murnane
 *
 */
public class MethodBoxFigure extends MemberFigure {

	public void addDebugHighlight() {
		methodBox.setBackgroundColor(ColorScheme.debugHighlight);
	}

	public void removeDebugHighlight() {
		methodBox.setBackgroundColor(ColorScheme.methodDeclarationFigureBackground);
	}

	public static String PROPERTY_PARTNER = "partner";

	public static Dimension DEFAULT_SIZE = new Dimension(16, 32);

	private Figure container;
	protected RectangleFigure methodBox;
	protected Figure methodBoxWithoutBorder = null;

	private GapFigure innerSizerGap;

	private Label noConnectionsBoxLabel;
	private String noConnectionsBoxLabelText;
	private RectangleFigure overridesIndicator;
	private RectangleFigure overriddenIndicator;

	private int nestingLevel = 0;

	public MethodBoxFigure(int type, Label tooltip, String noTypeNoArgsName) {
		noConnectionsBoxLabelText = noTypeNoArgsName;
		setLayoutManager(new StackLayout());

		methodBox = new CutCornerFigure(this);
		methodBox.setMinimumSize(DEFAULT_SIZE);
		methodBox.setPreferredSize(DEFAULT_SIZE);
		if(type == MethodBoxModel.access) {
			methodBox.setLineStyle(Graphics.LINE_DASH);
			methodBox.setBackgroundColor(ColorScheme.methodInvocationFigureBackground);
			methodBox.setOpaque(true);
		} else if(type == MethodBoxModel.declaration) {
			methodBox.setLineStyle(Graphics.LINE_SOLID);
			methodBox.setBackgroundColor(ColorScheme.methodDeclarationFigureBackground);
			methodBox.setOpaque(true);
		}

		ToolbarLayout tb = new ToolbarLayout(false) {
			@Override
			protected Dimension calculatePreferredSize(IFigure container, int wHint, int hHint) {
				Dimension size = super.calculatePreferredSize(container, wHint, hHint);
				return new Dimension(DEFAULT_SIZE.width, Math.max(size.height, DEFAULT_SIZE.height));
			}
		};

		methodBox.setLayoutManager(tb);
		add(methodBox);

		container = new Figure();
		int marginWidth = DEFAULT_SIZE.width;
		int topAndBottomMargin = DEFAULT_SIZE.width;//(type==MethodBoxModel.invocation) ? DEFAULT_SIZE.width/2 : DEFAULT_SIZE.width;
		container.setBorder(new MarginBorder(topAndBottomMargin, marginWidth, topAndBottomMargin, 0));
		container.setBackgroundColor(methodBox.getBackgroundColor());
		ToolbarLayout containerLayout = new AnimationLayoutManager(false);
		containerLayout.setStretchMinorAxis(true);
		container.setLayoutManager(containerLayout);
		container.setToolTip(tooltip);
		add(container);

		noConnectionsBoxLabel = new Label(noTypeNoArgsName);
		noConnectionsBoxLabel.setForegroundColor(ColorScheme.noConnectionsBoxLabelText);

		innerSizerGap = new GapFigure("inner sizer");
		innerSizerGap.setSize(3, 0);
		if(SeqUtil.debugHighlightingOn) {
			innerSizerGap.setBorder(new ParticularSidesBorder(false, true, false, false));
			innerSizerGap.setBackgroundColor(ColorConstants.cyan);
			innerSizerGap.setOpaque(true);
		}
		getContainer().add(innerSizerGap);

		overridesIndicator = createOverrideIndicator();
		overriddenIndicator = createOverrideIndicator();

		resetGapToDefault();
	}	

	@Override
	public Color getBackgroundColor() {
		if (getMethodBox() == null)
			return super.getBackgroundColor();
		return getMethodBox().getBackgroundColor();
	}
	
	public void colorFigure(Color color) {
		if (color == null)
			color = ColorScheme.methodDeclarationFigureBackground;
		getMethodBox().setBackgroundColor(color);
	}
	
	private void addMethodBoxWithoutBorder(){
		methodBoxWithoutBorder = new Figure();
		methodBox.add(methodBoxWithoutBorder);
	}

	private RectangleFigure createOverrideIndicator() {
		RectangleFigure overrideIndicator = new RectangleFigure();
		overrideIndicator.setSize(5, 5);
		overrideIndicator.setBackgroundColor(ColorScheme.overrideIndicatorBackground);
		overrideIndicator.setOpaque(true);
		return overrideIndicator;
	}

	public IFigure getContainer() {
		return container;
	}

	public IFigure getMethodBox() {
		return methodBox;
	}

	public int getBorderWidth() {
		if(methodBox.getBorder()==null) return 0;
		return ((LeftRightBorder)methodBox.getBorder()).getWidth();	
	}

	public int getLineStyle() {
		return methodBox.getLineStyle();
	}

	public int getNestingLevel() {
		return nestingLevel;
	}

	@Override
	public void setGap(GapFigure gap) {
		GapFigure originalGap = this.gap;
		super.setGap(gap);

		if(originalGap==null) return;

		// Any children of this that were using this figure's gap figure as their
		// own need to have their gap figures set to this figure's new gap
		for(MethodBoxFigure child : getContainedMethodBoxes()) {
			if(originalGap.equals(child.getGap())) child.setGap(gap);
		}
	}

	public void setNoConnectionsBoxLabel(String name) {
		noConnectionsBoxLabel.setText(name);
	}
	
	public Label getNoConnectionsBoxLabel() {
		return noConnectionsBoxLabel;
	}

	public void hideNoConnectionsBoxLabel() {
		noConnectionsBoxLabel.setText("");
	}

	public void showNoConnectionsBoxLabel() {
		noConnectionsBoxLabel.setText(noConnectionsBoxLabelText);
	}

	public void setInnerSizerGap(GapFigure gap) {
		innerSizerGap = gap;
	}

	public GapFigure getInnerSizerGap() {
		return innerSizerGap;
	}

	@Override
	public int getType() {
		if(Graphics.LINE_DASH==methodBox.getLineStyle()) return MethodBoxModel.access;
		else return MethodBoxModel.declaration;
	}

	public RectangleFigure getOverridesIndicator() {
		return overridesIndicator;
	}

	public RectangleFigure getOverriddenIndicator() {
		return overriddenIndicator;
	}

	public void setColors(Color newBackgroundColor, Color newTextColor) {
		methodBox.setBackgroundColor(newBackgroundColor);
		noConnectionsBoxLabel.setForegroundColor(newTextColor);
	}

	public void addToMethodBox(IFigure child, int index) {
		IFigure parent = getParent().getParent();

		if(child instanceof MethodBoxFigure) {
			if(0==nestingLevel) nestingLevel=1;
			if(parent instanceof MethodBoxFigure && nestingLevel >= ((MethodBoxFigure)parent).getNestingLevel()) {
				((MethodBoxFigure)parent).increaseNestingLevel();
			}
		}

		if(index==0 || 
				(index==1 && container.getChildren().get(0).equals(innerSizerGap)) || 
				container.getChildren().size()==0 || 
				(container.getChildren().size()==1 && container.getChildren().get(0).equals(innerSizerGap))) {
			if(child instanceof FigureWithGap) {
				if(container.getChildren().size()>1 
						&& container.getChildren().get(1) instanceof FigureWithGap 
						&& ((FigureWithGap)container.getChildren().get(1)).isUsingContainerGap()) {
					// The child is a member that is being added as the first member in the container.
					// The member that was originally first inside the container can no longer
					// use the gap of the container (since the child being added will now use it since 
					// it is now the first member) and will need to have a gap inside the container instead.
					GapFigure gapFig = ((FigureWithGap)child).getGap();
					((FigureWithGap)container.getChildren().get(1)).setGap(gapFig);
					((FigureWithGap)container.getChildren().get(1)).setIsUsingContainerGap(false);
					container.add(gapFig, index);
				}
				((FigureWithGap)child).setGap(getGap()); // this sets the gap of the first method box inside this method box to be the gap of the container
				((FigureWithGap)child).setIsUsingContainerGap(true);
			}
			container.add(child, index);
		} else if((child instanceof FigureWithGap)) {
			GapFigure gapFig = ((FigureWithGap)child).getGap();
			container.add(gapFig, index);
			container.add(child, index);

			if(container.getChildren().indexOf(gapFig) > container.getChildren().indexOf(child)) {
				container.remove(gapFig);
				container.add(gapFig, index);
			}
		} else {
			//TODO When does this get called?
			container.add(child, index);
		}

		if(parent instanceof MethodBoxFigure) { // SELF CALL
			IFigure parentCopy = parent;
			while(parentCopy instanceof MethodBoxFigure) {
				parent = parentCopy;
				parentCopy = parentCopy.getParent().getParent();
			}
			handleNesting((MethodBoxFigure)parent);
		} else if(methodBox.getBorder()==null){
			methodBox.setBorder(new LeftRightBorder(getParent().getBackgroundColor(), DEFAULT_SIZE.width/2, methodBox.getLineStyle()));
		} else if(child instanceof MethodBoxFigure) { // Invocation
			handleNesting(this);
		}
	}

	@Override
	public void remove(IFigure figure) {
		if((figure instanceof MemberFigure) && (getGap().equals(((MemberFigure)figure).getGap()))) {
			// Removing the first member in the container
			getContainer().remove(figure);

			// So need to set the next member as the new first member in the container
			MemberFigure nextMemberChild = null;
			for(int i=0; i<getContainer().getChildren().size(); i++) {
				Object child = getContainer().getChildren().get(i);

				if(child instanceof HiddenFigure) break;

				if(child instanceof MemberFigure) {
					nextMemberChild = (MemberFigure)getContainer().getChildren().get(i);
					break;
				}
			}
			if(nextMemberChild!=null) {
				getContainer().remove(nextMemberChild.getGap());
				nextMemberChild.setGap(getGap());
				nextMemberChild.setIsUsingContainerGap(true);
			}

		} else if((figure instanceof MemberFigure) && (!getGap().equals(((MemberFigure)figure).getGap()))) {
			getContainer().remove(((MemberFigure)figure).getGap());
			getContainer().remove(figure);
		} else if(figure instanceof HiddenFigure) {
			// if the hidden model is the method's first child (at
			// index 0 in the method model's children list) then the  
			// hidden figure's gap was not added with the hidden figure
			// (and therefore doesn't need to be removed)
			if(getContainer().getChildren().contains(((HiddenFigure)figure).getGap())) {
				getContainer().remove(((HiddenFigure)figure).getGap());
			}
			getContainer().remove(figure);
		}

		if(figure instanceof MethodBoxFigure) {

			IFigure parent = getParent().getParent();
			if(parent instanceof MethodBoxFigure) {
				IFigure parentCopy = parent;
				while(parentCopy instanceof MethodBoxFigure) {
					parent = parentCopy;
					parentCopy = parentCopy.getParent().getParent();
				}
				//((MethodBoxFigure)parent).nestingLevel = ((MethodBoxFigure)parent).calculateNestingLevel();
				//System.out.println("nesting level for " + parent + " is " + nestingLevel);
				//handleNesting((MethodBoxFigure)parent);
			} else {
				//nestingLevel = calculateNestingLevel();
				//System.out.println("nesting level for " + this + " is " + nestingLevel);
				//handleNesting(this);
			}
		}
	}

	private void testForOverlaps(DiagramEditPart diagram, MethodBoxModel model) {
		ConnectionLayer connectionLayer = diagram.getConnectionLayer();
		for(Object conn : connectionLayer.getChildren()) {

			if(!(conn instanceof ConnectionFigure)) continue;
			ConnectionFigure connection = (ConnectionFigure) conn;
			if(connection.getStart().y!=connection.getEnd().y) continue;

			if(ConnectionUtil.overlaps(connection, model)) {
				if(model.getIncomingConnection()!=null) {
					MethodBoxModel sourceModel = (MethodBoxModel) model.getIncomingConnection().getSource();
					GapFigure sourceGap = sourceModel.getFigure().getGap();
					if(sourceGap==null) return;
					if(SeqUtil.debugHighlightingOn) {
						sourceGap.setBackgroundColor(new Color(null, 205, 104, 137));
					}
					sourceGap.setSize(5, sourceGap.getSize().height+MethodBoxFigure.METHOD_BOX_GAP);
					LayoutUtil.refresh(sourceGap);
				} else {
					GapFigure gap = getGap();
					if(gap==null) return;
					if(SeqUtil.debugHighlightingOn) {
						gap.setBackgroundColor(new Color(null, 205, 104, 137));
					}
					gap.setSize(5, gap.getSize().height+MethodBoxFigure.METHOD_BOX_GAP);
					LayoutUtil.refresh(gap);
				}
				return;
			}
		}
	}

	private void handleNesting(MethodBoxFigure parent) {
		int width = DEFAULT_SIZE.width/2*parent.getNestingLevel();
		parent.getMethodBox().setBorder(new LeftRightBorder(parent.getParent().getBackgroundColor(), width, parent.getLineStyle()));

		for(Object obj : parent.getContainer().getChildren()) {
			if(!(obj instanceof MethodBoxFigure)) continue;
			MethodBoxFigure child = (MethodBoxFigure) obj;
			handleNestingOfEachChild(child, parent.getNestingLevel()-1);
		}
	}

	private void handleNestingOfEachChild(MethodBoxFigure child, int nesting) {
		int width = DEFAULT_SIZE.width/2*nesting;
		child.getMethodBox().setBorder(new LeftRightBorder(((MethodBoxFigure)child.getParent().getParent()).getMethodBox().getBackgroundColor(), width, child.getLineStyle()));

		nesting = nesting - 1;
		for(Object obj : child.getContainer().getChildren()) {
			if(!(obj instanceof MethodBoxFigure)) continue;
			MethodBoxFigure box = (MethodBoxFigure) obj;
			handleNestingOfEachChild(box, nesting);
		}
	}

	private void increaseNestingLevel() {
		nestingLevel++;
		IFigure parent = getParent().getParent();
		if(!(parent instanceof MethodBoxFigure) || ((MethodBoxFigure)parent).getNestingLevel() > nestingLevel) return;
		((MethodBoxFigure)parent).increaseNestingLevel();
	}

	private int calculateNestingLevel() {
		List<MethodBoxFigure> deepestChildren = new ArrayList<MethodBoxFigure>();
		getDeepestChildren(deepestChildren);
		int maxNesting = 0;
		for(MethodBoxFigure child : deepestChildren) {
			int childNesting = 0;
			IFigure parentBox = child.getParent().getParent();
			while(parentBox instanceof MethodBoxFigure && parentBox.getParent()!=null) {
				parentBox = parentBox.getParent().getParent();
				childNesting = childNesting + 1;
			}
			if(childNesting > maxNesting) maxNesting = childNesting;
		}
		return maxNesting;
	}

	private void getDeepestChildren(List<MethodBoxFigure> deepestChildren) {
		for(MethodBoxFigure child : getContainedMethodBoxes()) {
			if(!child.containsMethodBoxes()) deepestChildren.add(child);
			else child.getDeepestChildren(deepestChildren);
		}
	}

	private boolean containsMethodBoxes() {
		for(Object child : container.getChildren()) {
			if(child instanceof MethodBoxFigure) return true;
		}
		return false;
	}

	private List<MethodBoxFigure> getContainedMethodBoxes() {
		List<MethodBoxFigure> boxes = new ArrayList<MethodBoxFigure>();
		for(Object child : container.getChildren()) {
			if(child instanceof MethodBoxFigure) boxes.add((MethodBoxFigure)child);
		}
		return boxes;
	}

	@Override
	public String toString() {
		return super.toString() + " " + getToolTip();
	}

	@Override
	public MethodBoxFigure getPartner() {
		return (MethodBoxFigure) partner;
	}

	@Override
	public void setPartner(MemberFigure partner) {
		if(!(partner instanceof MethodBoxFigure)) this.partner = null;
		else super.setPartner(partner);
	}

	public Figure getMethodBoxWithoutBorder(){
		createOrUpdateMethodBoxWithoutBorder();
		return methodBoxWithoutBorder;
	}

	@Override
	public void setBounds(Rectangle rect) {
		super.setBounds(rect);
		createOrUpdateMethodBoxWithoutBorder();
	}

	public void createOrUpdateMethodBoxWithoutBorder() {
		if(methodBoxWithoutBorder == null){
			addMethodBoxWithoutBorder();
		}
		Rectangle borderIncludedBounds = methodBox.getBounds().getCopy();
		int borderWidth = getBorderWidth();

		Point upperLeft = new Point(borderIncludedBounds.x + borderWidth, borderIncludedBounds.y);
		//TODO In which case is the diff used?
		int diff = ((this.getChildren().size() == 0) &&
				(this.getParent() instanceof MethodBoxFigure ))  ? 1 : 0; 
		Point lowerRight = new Point(borderIncludedBounds.getRight().x - borderWidth - diff, borderIncludedBounds.getBottom().y - 1);
		Rectangle noBorderBounds = new Rectangle(upperLeft, lowerRight);
		methodBoxWithoutBorder.setBounds(noBorderBounds);
		methodBoxWithoutBorder.translateToRelative(noBorderBounds);
	}

}