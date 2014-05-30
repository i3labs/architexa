package com.architexa.diagrams.figures;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.widgets.Display;

import com.architexa.org.eclipse.draw2d.Figure;
import com.architexa.org.eclipse.draw2d.FlowLayout;
import com.architexa.org.eclipse.draw2d.ImageFigure;
import com.architexa.org.eclipse.draw2d.Label;
import com.architexa.org.eclipse.draw2d.PositionConstants;
import com.architexa.org.eclipse.draw2d.ToolbarLayout;
import com.architexa.org.eclipse.draw2d.geometry.Rectangle;

public class EntityFigure extends Figure implements CommentFigure {

	ImageFigure imgFig;
	Label text;

	public EntityFigure(String s, Image i) {
		imgFig = new ImageFigure(i);
		text = new Label(s);
		text.setTextPlacement(PositionConstants.SOUTH);

		ToolbarLayout layout = new ToolbarLayout(false);
		layout.setMinorAlignment(FlowLayout.ALIGN_CENTER);
		layout.setStretchMinorAxis(false);
		setLayoutManager(layout);
		add(imgFig);
		add(text);
	}

	public void resizeImg(Rectangle newBounds) {
		setBounds(newBounds);
		Image orgImg = imgFig.getImage();
		ImageData origData = orgImg.getImageData();
		ImageData resizedData = origData.scaledTo(newBounds.width, newBounds.height);
		imgFig.setImage(new Image(Display.getCurrent(), resizedData));
		orgImg.dispose();
		revalidate();
	}

	public String getText() {
		return text.getText();
	}

	public void setText(String newText) {
		text.setText(newText);
	}

}
