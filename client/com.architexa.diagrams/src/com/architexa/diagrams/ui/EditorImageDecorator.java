package com.architexa.diagrams.ui;

import org.eclipse.jface.resource.CompositeImageDescriptor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;

/**
 * @author Elizabeth L. Murnane
 */

/**
 * This class overlays the given editor image with the given decoration 
 * image. The decoration image is overlayed at the given location. 
 * Currently, the only supported location is the bottom left corner.
 * 
 * After instantiating a ReloImageDecorator, call .createImage() on it 
 * to retrieve the new editor image with overlay
 */
public class EditorImageDecorator extends CompositeImageDescriptor {
	
	public static final int BOTTOM_LEFT = 0;
	public static final int BOTTOM_RIGHT = 1;
	public static final int TOP_LEFT = 2;
	public static final int TOP_RIGHT = 3;
	
	private Image editorImage;
	private Image decorationImage;
	private int location;


	public EditorImageDecorator(ImageDescriptor editorDescr, ImageDescriptor decorationDescr, int location) {
		this.editorImage = ImageCache.calcImageFromDescriptor(editorDescr);
		this.decorationImage = ImageCache.calcImageFromDescriptor(decorationDescr);
		this.location = location;
	}

	@Override
	public int hashCode() {
		return editorImage.hashCode() | decorationImage.hashCode() | location;
	}

	@Override
	protected void drawCompositeImage(int width, int height) {
		drawImage(editorImage.getImageData(), 0, 0);
		if(location==BOTTOM_LEFT) {
			drawBottomLeft();
		} else if (location==BOTTOM_RIGHT) {
			drawBottomRight();
		} else if (location==TOP_LEFT) {
			drawTopLeft();
		} else if (location==TOP_RIGHT) {
			drawTopRight();
		}
	}
	
	@Override
	protected Point getSize() {
		Rectangle editorBounds = editorImage.getBounds();
		Point size = new Point(editorBounds.width, editorBounds.height);
		return size;
	}
	
	private void drawBottomLeft() {
		Point size = getSize();
		ImageData data = decorationImage.getImageData();
		drawImage(data, 0, size.y - data.height);
	}
	
	private void drawBottomRight() {
		Point size = getSize();
		ImageData data = decorationImage.getImageData();
		drawImage(data, size.x - data.width, size.y - data.height);
	}
	
	private void drawTopLeft() {
		ImageData data = decorationImage.getImageData();
		drawImage(data, 0, 0);
	}

	private void drawTopRight() {
		Point size = getSize();
		ImageData data = decorationImage.getImageData();
		drawImage(data, size.x - data.width, 0);
	}
}
