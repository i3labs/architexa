package com.architexa.diagrams.chrono.figures;

import org.eclipse.jdt.ui.ISharedImages;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;

import com.architexa.diagrams.chrono.animation.AnimationLayoutManager;
import com.architexa.diagrams.chrono.animation.Animator;
import com.architexa.diagrams.chrono.models.DiagramModel;
import com.architexa.diagrams.chrono.ui.ColorScheme;
import com.architexa.diagrams.chrono.util.LayoutUtil;
import com.architexa.diagrams.chrono.util.SeqUtil;
import com.architexa.diagrams.ui.ImageCache;
import com.architexa.org.eclipse.draw2d.BendpointConnectionRouter;
import com.architexa.org.eclipse.draw2d.ChopboxAnchor;
import com.architexa.org.eclipse.draw2d.CompoundBorder;
import com.architexa.org.eclipse.draw2d.Connection;
import com.architexa.org.eclipse.draw2d.Figure;
import com.architexa.org.eclipse.draw2d.FlowLayout;
import com.architexa.org.eclipse.draw2d.IFigure;
import com.architexa.org.eclipse.draw2d.Label;
import com.architexa.org.eclipse.draw2d.LineBorder;
import com.architexa.org.eclipse.draw2d.MarginBorder;
import com.architexa.org.eclipse.draw2d.PolylineConnection;
import com.architexa.org.eclipse.draw2d.PositionConstants;
import com.architexa.org.eclipse.draw2d.ToolbarLayout;
import com.architexa.org.eclipse.draw2d.geometry.Dimension;
import com.architexa.org.eclipse.draw2d.geometry.Point;
import com.architexa.org.eclipse.draw2d.geometry.Rectangle;


/**
 * 
 * @author Elizabeth L. Murnane
 *
 */

public class InstanceFigure extends AbstractSeqFigure {

	public static Dimension DEFAULT_SIZE = new Dimension(100, 30);
	public static int lifeLineTailLength = 75;

	protected String instanceName;
	private String className;
	private Label toolTipLabel;
	private Image icon;

	public Label instanceNameLabel;
	public Label classNameLabel;
	public Label iconLabel;
	public Figure nameAndIconContainer;
	private Figure labelContainer;
	private LineBorder instanceFigureBorder;

	private InstanceChildrenContainer childrenContainer;

	private PolylineConnection lifeLineInInstancePanel;
	private PolylineConnection lifeLine;
	public static String bottomInstanceBox = "bottomInstanceBox";
	public static String bottomChildrenContainer = "bottomChildrenContainer";
	public boolean isGrouped = false;
	private boolean isHighlighted = false;

	private Figure labelContainerSpacerLeft = new Figure() {
		@Override
		public void setSize(int w, int h) {
			super.setSize(w, h);
			LayoutUtil.refresh(labelContainerSpacerLeft);
		}
	};
	private Figure labelContainerSpacerRight = new Figure() {
		@Override
		public void setSize(int w, int h) {
			super.setSize(w, h);
			LayoutUtil.refresh(labelContainerSpacerRight);
		}
	};
	private Figure childrenContainerSpacer = new Figure() {
		@Override
		public void setSize(int w, int h) {
			super.setSize(w, h);
			LayoutUtil.refresh(childrenContainerSpacer);
		}
	};
	public Label spacerLabel;


	public InstanceFigure(String instanceName, String className, String packageName,  Image icon) {
		this.instanceName = (instanceName==null ? "" : instanceName);
		this.className = (className==null ? "" : className);
		this.icon = icon;

		setLayoutManager(new FlowLayout(false));
		((FlowLayout)super.getLayoutManager()).setMajorSpacing(0);

		super.add(labelContainerSpacerLeft, null, -1);

		labelContainer = makeInstanceBox();
		if(packageName!=null && !packageName.equals("")) {
			setToolTipLabel(new Label(" " + packageName + " ", ImageCache.calcImageFromDescriptor(SeqUtil.getImageDescriptorFromKey(ISharedImages.IMG_OBJS_PACKAGE))));
			labelContainer.setToolTip(getToolTipLabel());	
		}

		super.add(labelContainer, null, -1);

		super.add(labelContainerSpacerRight, null, -1);

		childrenContainer = new InstanceChildrenContainer(this);

		// Giving the children container a bottom margin allows the life line to
		// extend a bit beyond the last method box and allows for a method box
		// to be re-ordered to the bottom-most method box position
		childrenContainer.setBorder(new MarginBorder(0, 0, lifeLineTailLength, 0));
//		final int instanceWidth = getBounds().width;

		ToolbarLayout layout = new AnimationLayoutManager(false) {
			@Override
			protected Dimension calculatePreferredSize(IFigure container,
					int hint, int hint2) {
				Dimension size = super.calculatePreferredSize(container, hint, hint2);
				return new Dimension(Math.max(size.width, DEFAULT_SIZE.width), size.height);
			}
		};
		layout.setMinorAlignment(ToolbarLayout.ALIGN_CENTER);
		layout.setStretchMinorAxis(false);
		childrenContainer.setLayoutManager(layout);

		childrenContainer.add(childrenContainerSpacer);

		// The life line extends from the bottom of the instance box to 
		// the bottom of the children container, which is a bit beyond 
		// the bottom of the last method box

		// This is the portion that extends from the bottom of the instance box to
		// the the bottom of the instance panel
		lifeLineInInstancePanel = new PolylineConnection();
		lifeLineInInstancePanel.setForegroundColor(ColorScheme.lifeLine);

		// This is the portion that extends from the bottom of the instance panel
		// to the bottom of the children container, which is a bit beyond the
		// bottom of the last method box
		lifeLine = new PolylineConnection();
		lifeLine.setForegroundColor(ColorScheme.lifeLine);

		ChopboxAnchor bottomInstanceBoxAnchor = new ChopboxAnchor(this) {
			@Override
			public Point getLocation(Point reference) {
				return findLocationForAnchor(bottomInstanceBox);
			}
		};
		ChopboxAnchor bottomInstancePanelAnchor = new ChopboxAnchor(childrenContainer) {
			@Override
			public Point getLocation(Point reference) {
				Point bottomOfInstanceBox = findLocationForAnchor(bottomInstanceBox);
				return new Point(bottomOfInstanceBox.x, bottomOfInstanceBox.y + DiagramModel.INSTANCE_PANEL_MARGINS);
			}
		};
		ChopboxAnchor absoluteDiagramTopAnchor = new ChopboxAnchor(childrenContainer) {
			@Override
			public Point getLocation(Point reference) {
				Point bottomOfInstanceBox = findLocationForAnchor(bottomInstanceBox);
				return new Point(bottomOfInstanceBox.x, -(Math.abs(bottomOfInstanceBox.y)+DiagramModel.INSTANCE_PANEL_MARGINS+DiagramModel.TOP_MARGIN));
			}
		};
		ChopboxAnchor bottomChildrenContainerAnchor = new ChopboxAnchor(childrenContainer) {
			@Override
			public Point getLocation(Point reference) {
				return findLocationForAnchor(bottomChildrenContainer);
			}
		};
		lifeLineInInstancePanel.setSourceAnchor(bottomInstanceBoxAnchor);
		lifeLineInInstancePanel.setTargetAnchor(bottomInstancePanelAnchor);
		lifeLine.setSourceAnchor(absoluteDiagramTopAnchor);
		lifeLine.setTargetAnchor(bottomChildrenContainerAnchor);

		BendpointConnectionRouter lifeLineRouter  = new BendpointConnectionRouter() {
			@Override
			public void route(Connection conn) {
				Animator.recordInitialState(conn);
				if(!Animator.playbackState(conn)) super.route(conn);
			}
		};
		lifeLineInInstancePanel.setConnectionRouter(lifeLineRouter);
		lifeLine.setConnectionRouter(lifeLineRouter);
	}

	public Figure getInstanceBox() {
		return labelContainer;
	}

	public PolylineConnection getLifeLineInInstancePanel() {
		return lifeLineInInstancePanel;
	}

	public PolylineConnection getLifeLine() {
		return lifeLine;
	}

	public InstanceChildrenContainer getChildrenContainer() {
		return childrenContainer;
	}

	public void setColors(Color newBackgroundColor, Color newTextColor, Color newBorderColor) {

		iconLabel.setBackgroundColor(newBackgroundColor);
		classNameLabel.setBackgroundColor(newBackgroundColor);
		nameAndIconContainer.setBackgroundColor(newBackgroundColor);

		instanceNameLabel.setForegroundColor(newTextColor);
		classNameLabel.setForegroundColor(newTextColor);

		instanceFigureBorder.setColor(newBorderColor);
	}

	public  boolean isGrouped(){
		return isGrouped;
	}
	
	public  boolean isHighlighted(){
		return isHighlighted;
	}
	
	@Override
	public Color getBackgroundColor() {
		if (nameAndIconContainer == null)
			return super.getBackgroundColor();
		return nameAndIconContainer.getBackgroundColor();
	}
	
	public void colorFigure(Color color) {
		if (color == null)
			color = ColorScheme.instanceFigureBackground;
		iconLabel.setBackgroundColor(color);
		classNameLabel.setBackgroundColor(color);
		nameAndIconContainer.setBackgroundColor(color);
	}
	
	public void highlightGroupedInstance(){
		iconLabel.setBackgroundColor(ColorScheme.groupedInstanceFigureBackground);
		classNameLabel.setBackgroundColor(ColorScheme.groupedInstanceFigureBackground);
		nameAndIconContainer.setBackgroundColor(ColorScheme.groupedInstanceFigureBackground);
		isGrouped = true;
	}
	
	public void unHighlight(){
		iconLabel.setBackgroundColor(ColorScheme.instanceFigureBackground);
		classNameLabel.setBackgroundColor(ColorScheme.instanceFigureBackground);
		nameAndIconContainer.setBackgroundColor(ColorScheme.instanceFigureBackground);
		isGrouped = false;
		isHighlighted = false;
	}
	
	public void setInstanceName(String text) {
		instanceName = text;
		instanceNameLabel.setText(instanceName + (instanceName.equals("") ? "" : " : "));
	}

	public Figure getInstanceNameLabel() {
		return instanceNameLabel;
	}
	
	public Figure makeInstanceBox() {
		Color iconAndClassColor = ColorScheme.instanceFigureBackground;

		instanceNameLabel = new Label(instanceName + (instanceName.equals("") ? "" : " : ")){
			@Override
			public Rectangle getTextBounds() {
				Dimension dim = new Dimension(getTextSize());
				if (dim.width > 0) {
					dim.expand(1, 0);
					if (classNameLabel == null)
						dim.expand(10, 0);
				}
					
				return new Rectangle(getBounds().getLocation().translate(getTextLocation()), dim);
			}
			
			@Override
			public Rectangle getClientArea(Rectangle rect) {
				Rectangle origArea = new Rectangle(super.getClientArea(rect));
				origArea.expand(1, 0);
				if (classNameLabel == null)
					origArea.expand(10, 0);
				return origArea;
			}
		};
		spacerLabel = new Label("");
		instanceNameLabel.setForegroundColor(ColorScheme.instanceFigureText);
		instanceNameLabel.setToolTip(new Label("Double click to edit instance name of this " + className + " Object"));

		iconLabel = new Label(icon);
		iconLabel.setIconAlignment(PositionConstants.BOTTOM);
		iconLabel.setBorder(new MarginBorder(4, 0, 0, 0));
		iconLabel.setBackgroundColor(iconAndClassColor);
		iconLabel.setOpaque(true);

		classNameLabel = new Label(" " + className);
		classNameLabel.setBackgroundColor(iconAndClassColor);
		classNameLabel.setForegroundColor(ColorScheme.instanceFigureText);
		classNameLabel.setOpaque(true);

		Figure nameAndIcon = new Figure();
		ToolbarLayout labelLayout = new ToolbarLayout(true);
		labelLayout.setMinorAlignment(ToolbarLayout.ALIGN_CENTER);
		labelLayout.setStretchMinorAxis(true);
		nameAndIcon.setLayoutManager(labelLayout);
		nameAndIcon.add(instanceNameLabel);
		nameAndIcon.add(iconLabel);
		nameAndIcon.add(classNameLabel);
		nameAndIcon.add(spacerLabel);
		
		nameAndIconContainer = new Figure();
		nameAndIconContainer.setBackgroundColor(ColorScheme.instanceFigureBackground);
		nameAndIconContainer.setOpaque(true);
		instanceFigureBorder = new LineBorder(1);
		instanceFigureBorder.setColor(ColorScheme.instanceFigureBorder);
		nameAndIconContainer.setBorder(new CompoundBorder(instanceFigureBorder, new MarginBorder(4, 8, 0, 8)));
		ToolbarLayout nameAndIconContainerLayout = new ToolbarLayout(false) {
			@Override
			protected Dimension calculatePreferredSize(IFigure container, int wHint, int hHint) {
				Dimension size = super.calculatePreferredSize(container, wHint, hHint);
				return new Dimension(Math.max(size.width, DEFAULT_SIZE.width), Math.max(size.height, DEFAULT_SIZE.height));
			}
		};
		nameAndIconContainerLayout.setMinorAlignment(ToolbarLayout.ALIGN_CENTER);
		nameAndIconContainer.setLayoutManager(nameAndIconContainerLayout);
		nameAndIconContainer.add(nameAndIcon);

		Figure container = new Figure() {
			@Override
			public void setBounds(Rectangle rect) {
				super.setBounds(rect);
				setSpacer(rect.width);
			}
			@Override
			public void setSize(int w, int h) {
				super.setSize(w, h);
				setSpacer(w);
			}
			private void setSpacer(int width) {
				if(childrenContainerSpacer.getSize().width == width) return; //spacer already proper size
				childrenContainerSpacer.setSize(width, 5);
			}
		};

		ToolbarLayout containerLayout = new ToolbarLayout();
		containerLayout.setMinorAlignment(ToolbarLayout.ALIGN_BOTTOMRIGHT);
		containerLayout.setStretchMinorAxis(true);
		container.setLayoutManager(containerLayout);
		container.add(nameAndIconContainer);

		return container;
	}

	@Override
	public void add(IFigure figure, Object constraint, int index) {
		if(figure.equals(labelContainerSpacerLeft)) {
			// add the left spacer to the left of the label
			super.add(figure, constraint, 0);
			return;
		}
		if(figure.equals(labelContainerSpacerRight)) {
			// add the right spacer to the right of the label
			super.add(figure, constraint, -1);
			return;
		}

		if((!(figure instanceof MemberFigure))) {
			childrenContainer.add(figure, constraint, index);
			return;
		}

		GapFigure gapFig = ((MemberFigure)figure).getGap();
		childrenContainer.add(gapFig, index);

		Label noConnectionsBoxLabel = null;
		if(figure instanceof MethodBoxFigure) {
			noConnectionsBoxLabel = ((MethodBoxFigure)figure).getNoConnectionsBoxLabel();
			if(((MethodBoxFigure)figure).getPartner()!=null) ((MethodBoxFigure)figure).hideNoConnectionsBoxLabel();
		}
		if(noConnectionsBoxLabel!=null) childrenContainer.add(noConnectionsBoxLabel, index);

		childrenContainer.add(figure, constraint, index);

		if(noConnectionsBoxLabel!=null && childrenContainer.getChildren().indexOf(noConnectionsBoxLabel) > childrenContainer.getChildren().indexOf(figure)) {
			childrenContainer.remove(noConnectionsBoxLabel);
			childrenContainer.add(noConnectionsBoxLabel, index);
		}
		if(childrenContainer.getChildren().indexOf(gapFig) > childrenContainer.getChildren().indexOf(figure)) {
			childrenContainer.remove(gapFig);
			childrenContainer.add(gapFig, index);
		}
	}

	@Override
	public void remove(IFigure figure) {
		if(figure.getParent().equals(this)) {
			super.remove(figure);
			return;
		}
		if((figure instanceof MemberFigure) && ((MemberFigure)figure).getGap()!=null) childrenContainer.remove(((MemberFigure)figure).getGap());
		if((figure instanceof MethodBoxFigure)) childrenContainer.remove(((MethodBoxFigure)figure).getNoConnectionsBoxLabel());
		childrenContainer.remove(figure);
	}

	public Point findLocationForAnchor(String position) {

		if(instanceBoxBelowChildrenContainer()) {

			// Hide the instance figure if its entire life span has been scrolled out
			// of view because it is no longer a participating element in the diagram
			setVisible(false);

			// The user has scrolled to make the container no longer visible, 
			// so the life line should also not be visible
			//			return new Point(-1,-1);
		}

		// Show the instance figure because at least part of its life span is 
		// still visible in the diagram
		setVisible(true);

		Rectangle lineBounds = Rectangle.SINGLETON;
		lineBounds.setBounds(getInstanceBox().getBounds());
		getInstanceBox().translateToAbsolute(lineBounds);

		int topLeftCorner = getParent().getBounds().getTopLeft().y;
		int x = lineBounds.getCenter().x - 2;
		int y = -1;
		if(bottomInstanceBox.equals(position)) {
			y = getInstanceBox().getBounds().getBottom().y;
		} else if(bottomChildrenContainer.equals(position)) {
			y = childrenContainer.getBounds().getBottom().y - topLeftCorner;
		}
		return new Point(x, y);
	}

	private boolean instanceBoxBelowChildrenContainer() {
		return getBounds().getBottom().y - DiagramModel.TOP_MARGIN > childrenContainer.getBounds().getBottom().y;
	}

	public void setToolTipLabel(Label toolTip) {
		this.toolTipLabel = toolTip;
	}

	public Label getToolTipLabel() {
		return toolTipLabel;
	}

	public class InstanceChildrenContainer extends Figure {

		InstanceFigure instanceFigure;

		public InstanceChildrenContainer(InstanceFigure instanceFigure) {
			super();
			this.instanceFigure = instanceFigure;
		}

		public InstanceFigure getInstanceFigure() {
			return instanceFigure;
		}

		@Override
		public void setBounds(Rectangle rect) {
			super.setBounds(rect);
			setSpacers(rect.width);
		}

		@Override
		public void setSize(int w, int h) {
			super.setSize(w, h);
			setSpacers(w);
		}

		public void setSpacers(int width) {
			if(labelContainer.getBounds().width <= 0) return;

			int difference = width - labelContainer.getBounds().width;
			if(labelContainerSpacerLeft.getSize().width == difference/2) return; // spacers already proper size
			labelContainerSpacerLeft.setSize(difference/2, 5);
			labelContainerSpacerRight.setSize(difference/2, 5);
		}
	}

}