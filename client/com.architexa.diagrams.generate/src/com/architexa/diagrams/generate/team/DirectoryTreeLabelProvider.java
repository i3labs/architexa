package com.architexa.diagrams.generate.team;

import org.eclipse.jdt.ui.ISharedImages;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.PlatformUI;

public class DirectoryTreeLabelProvider extends LabelProvider{
	//private Image IMG_FOLDER;

	public DirectoryTreeLabelProvider() {
//		IMG_FOLDER = Activator.getImageDescriptor("icons/tool/folder16.gif")
//				.createImage();
	}

	@Override
	public Image getImage(Object element) {
		return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJS_DEFAULT);
	}

	@Override
	public String getText(Object element) {
		return element.toString();
	}

	@Override
	public void dispose() {
//		IMG_FOLDER.dispose();
		super.dispose();
	}
}
