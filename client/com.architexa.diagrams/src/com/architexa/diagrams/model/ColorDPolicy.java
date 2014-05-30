package com.architexa.diagrams.model;

import java.io.IOException;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.rio.RdfDocumentWriter;

import com.architexa.diagrams.RSECore;
import com.architexa.store.ReloRdfRepository;
import com.architexa.store.StoreUtil;

public class ColorDPolicy extends DiagramPolicy{

	////
	// basic setup
	////
	public static final String DefaultKey = "ColorDiagramPolicy";
	public static final ColorDPolicy Type = new ColorDPolicy();
	
	////
	// Policy Fields, Constructors and Methods 
	////
	public static final URI colorURI = RSECore.createRseUri("core#color");
	Color afColor = null;
	String separator = ";;";
	
	public ColorDPolicy() {}
	
	@Override
	public void writeRDF(RdfDocumentWriter rdfWriter) throws IOException {
		if (afColor != null)
			rdfWriter.writeStatement(getHostAF().getInstanceRes(), colorURI,
					StoreUtil.createMemLiteral(getColorValueStrings(afColor)));
		
	}

	private String getColorValueStrings(Color color) {
		RGB rgb = color.getRGB();
		String red = Integer.toHexString(rgb.red);
		String green = Integer.toHexString(rgb.green);
		String blue = Integer.toHexString(rgb.blue);
		return red + separator + green + separator + blue;
	}
	
	@Override
	public void readRDF(ReloRdfRepository queryRepo) {
		Value col = queryRepo.getStatement(getHostAF().getInstanceRes(), colorURI, null).getObject();
		if (col != null) {
			setColor(createColorFromString(((Literal)col).getLabel()));
		}
	}
	
	private Color createColorFromString(String string) {
//		Color Format --- "Hex-Red;;Hex-Green;;Hex-Blue"
		String[] rgb = string.split(separator);
		if (rgb.length != 3) return null;
		
		// For hex values radix is 16 -- Integer.parseInt(value, 16)
		return new Color(null, Integer.parseInt(rgb[0], 16), Integer.parseInt(rgb[1], 16), Integer.parseInt(rgb[2], 16)); 
	}

	private void setColor(Color color) {
		afColor = color;
		getHostAF().firePolicyContentsChanged();
	}
	
	public static void setColor(Color color, ArtifactFragment af) {
		ColorDPolicy pol = af.getTypedDiagramPolicy(Type, DefaultKey);
		if (pol != null)
			pol.setColor(color);
	}
	
	public Color getColor() {
		return afColor;
	}
	
	public static Color getColor(ArtifactFragment af) {
		ColorDPolicy pol = af.getTypedDiagramPolicy(Type, DefaultKey);
		if (pol != null)
			return pol.getColor();
		return null;
	}

	// utility for checking if this AF has been highlighted. Default is true
	public static boolean isDefaultColor(Object model, Color defaultColor) {
		if (model instanceof ArtifactFragment) {
			ArtifactFragment af = (ArtifactFragment) model;
			ColorDPolicy cdp = (ColorDPolicy) af.getDiagramPolicy(ColorDPolicy.DefaultKey);
			if ( cdp==null || cdp.getColor()==null) return true;
			else return cdp.getColor().equals(defaultColor);
		}
		return true;
	}
	
}
