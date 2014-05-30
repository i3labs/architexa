package com.architexa.diagrams.model;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.apache.commons.codec.binary.Base64;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.rio.RdfDocumentWriter;

import com.architexa.diagrams.RSECore;
import com.architexa.diagrams.ui.RSEEditor;
import com.architexa.org.eclipse.draw2d.geometry.Dimension;
import com.architexa.store.ReloRdfRepository;
import com.architexa.store.StoreUtil;

public class Entity extends Comment {

	String entityName;
	ImageDescriptor iconDescriptor;
	private ImageData imageData = null;
	Dimension size = null;
	private boolean isImportedImage = false;
	
	// entity URIs
	public static final URI entityIconType = createReloURI("core#entityIconType");
	public static final URI entityIconHeight = createReloURI("core#entityIconHeight");
	public static final URI entityIconWidth = createReloURI("core#entityIconWidth");
	public static final URI importImage = createReloURI("core#importImage");

	public Entity(String entityName, ImageDescriptor entityIcon) {
		this.entityName = entityName;
		setAnnoLabelText(entityName);
		// import images should remain the proper size
		if (entityName.contains("Import Image")) {
			int width = entityIcon.getImageData().width;
			int height = entityIcon.getImageData().height;
			isImportedImage  = true;
			setSize(width, height);
			this.imageData = entityIcon.getImageData();
		} else {
			this.iconDescriptor = entityIcon;
		}
		
	}
	public Entity() {
		super();
	}

	public String getEntityName() {
		return entityName;
	}

	public ImageDescriptor getIconDescriptor() {
		return iconDescriptor;
	}

	@Override
	public void writeRDFNode(RdfDocumentWriter rdfWriter,Resource parentInstance) throws IOException {
		rdfWriter.writeStatement(getArt().elementRes,getRootArt().getRepo().rdfType, RSECore.entityType);
		
		int imageFormat = SWT.IMAGE_PNG;
		if (isImportedImage) {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			ImageData[] imgData = new ImageData[1];
			imgData[0] = getImageData();
			ImageLoader imgLoader = new ImageLoader();
			imgLoader.data = imgData;
			imgLoader.save(out, imageFormat);
			
			byte[] imageBytes = Base64.encodeBase64(out.toByteArray());
			
			String base64Image = new String(imageBytes);
			rdfWriter.writeStatement(getInstanceRes(), importImage, StoreUtil.createMemLiteral(base64Image));
		} else {
			rdfWriter.writeStatement(getInstanceRes(), entityIconType, StoreUtil.createMemLiteral(iconDescriptor.toString()));
		}
			
		rdfWriter.writeStatement(getInstanceRes(), entityIconWidth, StoreUtil.createMemLiteral(String.valueOf(getSize().width)));
		rdfWriter.writeStatement(getInstanceRes(), entityIconHeight, StoreUtil.createMemLiteral(String.valueOf(getSize().height)));
		super.writeRDFNode(rdfWriter, parentInstance);
	}
		

	@Override
	public void readRDFNode(final ReloRdfRepository queryRepo) {
		
		Statement heightStatement = queryRepo.getStatement(getInstanceRes(), entityIconHeight,null);
		int height= Integer.parseInt(heightStatement.getObject().toString());
		int width = Integer.parseInt(queryRepo.getStatement(getInstanceRes(), entityIconWidth,null).getObject().toString());
		size = new Dimension(width, height);

		Statement iconStmt = queryRepo.getStatement(getInstanceRes(), entityIconType,null);
		if (iconStmt==null || iconStmt.equals(ReloRdfRepository.nullStatement)) {
			String importImageStr = queryRepo.getStatement(getInstanceRes(), importImage ,null).getObject().toString();
			byte[] imgBytes = Base64.decodeBase64(importImageStr.getBytes());
			
			ByteArrayInputStream in = new ByteArrayInputStream(imgBytes);
			ImageLoader imgLoader = new ImageLoader();
			ImageData[] imageDatas = imgLoader.load(in);
			isImportedImage = true;
			setImageData(imageDatas[0]);
		} else {
			
			String iconDesc = iconStmt.getObject().toString();
			
			if (iconDesc.contains("palette_database"))
				iconDescriptor = RSEEditor.getDatabasePaletteEntry().getLargeIcon();
			else if (iconDesc.contains("palette_actor"))
				iconDescriptor = RSEEditor.getActorPaletteEntry().getLargeIcon();
		}
		
		super.readRDFNode(queryRepo);
		entityName = getAnnoLabelText();
	}

	public void setSize(int width, int height) {
		size = new Dimension(width, height);
	}
	public Dimension getSize() {
		if (size == null)
			size = new Dimension(24,14);
		return size;
	}

	public ImageData getImageData() {
		return imageData;
	}

	public void setImageData(ImageData imageData) {
		this.imageData = imageData;
	}
}
