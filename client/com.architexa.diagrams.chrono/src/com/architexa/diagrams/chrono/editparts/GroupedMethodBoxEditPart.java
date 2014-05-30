package com.architexa.diagrams.chrono.editparts;

import java.util.List;

import org.eclipse.swt.graphics.Image;

import com.architexa.diagrams.chrono.figures.UserCreatedMethodBoxFigure;
import com.architexa.diagrams.chrono.models.GroupedInstanceModel;
import com.architexa.diagrams.chrono.models.GroupedMethodBoxModel;
import com.architexa.diagrams.chrono.models.MethodBoxModel;
import com.architexa.diagrams.chrono.ui.ColorScheme;
import com.architexa.diagrams.chrono.util.MethodUtil;
import com.architexa.diagrams.model.ArtifactFragment;
import com.architexa.diagrams.ui.ImageCache;
import com.architexa.org.eclipse.draw2d.IFigure;
import com.architexa.org.eclipse.draw2d.Label;
import com.architexa.org.eclipse.draw2d.ToolbarLayout;

public class GroupedMethodBoxEditPart extends UserCreatedMethodBoxEditPart{

	@Override
	protected IFigure createFigure() {
		IFigure fig = super.createFigure();

		if(!(fig instanceof UserCreatedMethodBoxFigure)) return fig;
		UserCreatedMethodBoxFigure userCreatedFig = (UserCreatedMethodBoxFigure) fig;
		if(userCreatedFig.getType() == MethodBoxModel.declaration)
			userCreatedFig.getMethodBox().setBackgroundColor(ColorScheme.groupedMethodDeclarationFigureBackground);

		return fig;
	}

	@Override
	protected Label createTooltipFigure() {
		Label allContainedMembersLabel = new Label();
		ToolbarLayout tooltipLabelLayout = new ToolbarLayout(false);
		tooltipLabelLayout.setMinorAlignment(ToolbarLayout.ALIGN_TOPLEFT);
		allContainedMembersLabel.setLayoutManager(tooltipLabelLayout);

		GroupedMethodBoxModel model = (GroupedMethodBoxModel) getModel();
		List<MethodBoxModel> methodChildren = model.getSavedMethodChildrenList();
		for(MethodBoxModel methodChild:methodChildren){
			String className = ((GroupedInstanceModel)model.getInstanceModel()).getChildToInstanceMap().get(methodChild).getClassName();
			String methodName = MethodUtil.getMethodName(methodChild.getMethod(), null, false);
			String methodNameWithClassContext = className+"."+methodName;
			Image icon = ImageCache.calcImageFromDescriptor(MethodUtil.getMethodIconDescriptor(methodChild, false));
			Label groupedMethodLabel = new Label(methodNameWithClassContext, icon);

			allContainedMembersLabel.add(groupedMethodLabel);
		}

		return allContainedMembersLabel;
	}

	@Override
	protected void createEditPolicies() {
		super.createEditPolicies();
	}
	
	@Override
	public List<ArtifactFragment> getModelChildren(){
		return ((GroupedMethodBoxModel)getModel()).getChildren();
	}
}
