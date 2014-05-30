package com.architexa.diagrams.chrono.editparts;

import java.util.List;

import org.eclipse.jdt.ui.ISharedImages;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;

import com.architexa.diagrams.chrono.figures.InstanceFigure;
import com.architexa.diagrams.chrono.models.InstanceModel;
import com.architexa.diagrams.chrono.models.UserCreatedInstanceModel;
import com.architexa.diagrams.chrono.util.SeqUtil;
import com.architexa.diagrams.jdt.model.CodeUnit;
import com.architexa.diagrams.model.ArtifactFragment;
import com.architexa.diagrams.ui.ImageCache;
import com.architexa.org.eclipse.draw2d.IFigure;
import com.architexa.org.eclipse.draw2d.Label;
import com.architexa.store.StoreUtil;

public class UserCreatedInstanceEditPart extends InstanceEditPart{

	public UserCreatedInstanceEditPart(String instanceName, String className) {
		super(instanceName, className);
	}

	@Override
	protected IFigure createFigure() {
		figure = new InstanceFigure(instanceName, className, null, getIcon()) {
			@Override
			public void setInstanceName(String text) {
				if (text == null || text.length()==0) return;
				className = text;
				classNameLabel.setText(className);
				if (instanceNameLabel != null && instanceNameLabel.getParent() != null) {
					instanceNameLabel.setText("");
				}
			}
		};

		IFigure tooltip = createTooltipFigure();
		figure.setToolTipLabel((Label) tooltip);
		figure.setToolTip(tooltip);
		figure.addFigureListener(this);
		return figure;
	}

	protected Image getIcon() {
		ImageDescriptor icon = SeqUtil.getImageDescriptorFromKey(ISharedImages.IMG_OBJS_CLASS);
		return ImageCache.calcImageFromDescriptor(
				CodeUnit.getDecoratedIcon(StoreUtil.getDefaultStoreRepository(), ((InstanceModel)getModel()).getArt(), icon)
				);
	}

	protected IFigure createTooltipFigure() {
		return new Label(instanceName + className);
	}
	
	@Override
	protected void createEditPolicies() {
		super.createEditPolicies();
	}
	
	@Override
	public List<ArtifactFragment> getModelChildren() {
		return ((UserCreatedInstanceModel)getModel()).getChildren();
	}
	
	@Override
	/**
	 * SeqEditorContextMenuProvider.buildContextMenu() does not add this
	 * action for grouped instances or grouped methods, but just in case
	 * it does get accidentally added, disabling 'Open in Java Editor' 
	 * for grouped instances since they don't correspond to any one 
	 * particular code component that could be opened.
	 * 
	 * (Disabling rather than returning an empty action since that would
	 * make an empty line in the context menu that looks like a bug).
	 */
	public IAction getOpenInJavaEditorAction(String actionName,
			ImageDescriptor image) {
		IAction action = super.getOpenInJavaEditorAction(actionName, image);
		action.setEnabled(false);
		return action;
	}
}
