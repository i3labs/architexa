package com.architexa.diagrams.chrono.editparts;

import java.util.List;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;

import com.architexa.diagrams.chrono.figures.GroupedInstanceFigure;
import com.architexa.diagrams.chrono.models.GroupedInstanceModel;
import com.architexa.diagrams.chrono.models.InstanceModel;
import com.architexa.diagrams.chrono.ui.ColorScheme;
import com.architexa.diagrams.chrono.util.InstanceUtil;
import com.architexa.diagrams.model.ArtifactFragment;
import com.architexa.diagrams.ui.ImageCache;
import com.architexa.org.eclipse.draw2d.IFigure;
import com.architexa.org.eclipse.draw2d.Label;
import com.architexa.org.eclipse.draw2d.ToolbarLayout;

public class GroupedInstanceEditPart extends UserCreatedInstanceEditPart{

	public GroupedInstanceEditPart(String instanceName, String className) {
		super(instanceName, className);
	}

	@Override
	protected IFigure createFigure() {
		IFigure fig = super.createFigure();

		if(!(fig instanceof GroupedInstanceFigure)) return fig;
		
		GroupedInstanceFigure userCreatedFig = (GroupedInstanceFigure) fig;
		Color bg = ColorScheme.groupedInstanceFigureBackground;
		userCreatedFig.iconLabel.setBackgroundColor(bg);
		userCreatedFig.classNameLabel.setBackgroundColor(bg);
		userCreatedFig.nameAndIconContainer.setBackgroundColor(bg);

		return fig;
	}

	/**
	 * Don't want any icon for grouped figures.
	 */
	@Override
	protected Image getIcon() {
		return null;
	}

	@Override
	protected IFigure createTooltipFigure() {
		Label allContainedClassesLabel = new Label();
		ToolbarLayout tooltipLabelLayout = new ToolbarLayout(false);
		tooltipLabelLayout.setMinorAlignment(ToolbarLayout.ALIGN_TOPLEFT);
		allContainedClassesLabel.setLayoutManager(tooltipLabelLayout);

		for(InstanceModel child:((GroupedInstanceModel)getModel()).getInstanceChildren()){
			String instanceName = child.getInstanceName()!=null ? child.getInstanceName()+":" : "";
			String className = child.getClassName();
			ImageDescriptor desc = InstanceUtil.getInstanceIconDescriptor(child);
			Label groupedClassLabel = new Label(instanceName+className);
			if (desc != null) {
				groupedClassLabel.setIcon(ImageCache.calcImageFromDescriptor(desc));
			}
			allContainedClassesLabel.add(groupedClassLabel);
		}

		return allContainedClassesLabel;
	}

	@Override
	protected void createEditPolicies() {
		super.createEditPolicies();
	}

	@Override
	public List<ArtifactFragment> getModelChildren() {
		return ((GroupedInstanceModel)getModel()).getChildren();
	}
}

