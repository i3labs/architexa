package com.architexa.diagrams.chrono.util;



import com.architexa.diagrams.chrono.figures.MemberFigure;
import com.architexa.diagrams.chrono.models.MemberModel;
import com.architexa.org.eclipse.draw2d.IFigure;
import com.architexa.org.eclipse.draw2d.geometry.Rectangle;


/**
 * 
 * @author Elizabeth L. Murnane
 *
 */
public class LayoutUtil {

	public static void refresh(IFigure figure) {
		if(figure==null || figure.getParent()==null) return;
		IFigure parent = figure.getParent();
		int index = parent.getChildren().indexOf(figure);
		parent.remove(figure);
		if(parent.getChildren().size() < index) index = -1;
		parent.add(figure, index);
	}

	/**
	 * 
	 * @return the minimum height that can hold all of the given figure's children 
	 */
	public static int getHeightToContainAllChildren(IFigure figure) {
		Rectangle bounds = new Rectangle().setLocation(figure.getClientArea().getLocation());

		for (Object child : figure.getChildren()) {
			Rectangle childBounds = ((IFigure)child).getBounds().getCopy();
			if(!(child instanceof MemberFigure) ||
					MemberModel.declaration!=((MemberFigure)child).getType() ||
					((MemberFigure)child).getPartner()==null) {
				bounds.union(childBounds);
				continue;
			}

			Rectangle childPartnerBounds = ((MemberFigure)child).getPartner().getBounds().getCopy();
			if(childPartnerBounds.y <= childBounds.y) {
				bounds.union(childBounds);
			} else {
				Rectangle alignedChildBounds = new Rectangle(childBounds.x, childPartnerBounds.y, childBounds.height, childBounds.width);
				bounds.union(alignedChildBounds);
			}
		}
		bounds.resize(0, figure.getInsets().getHeight());

		return bounds.height;
	}

}
