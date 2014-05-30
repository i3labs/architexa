package com.architexa.diagrams.chrono.ui;


import org.eclipse.jface.resource.ImageDescriptor;

import com.architexa.diagrams.chrono.models.UserCreatedInstanceModel;
import com.architexa.diagrams.chrono.models.UserCreatedMethodBoxModel;
import com.architexa.diagrams.chrono.sequence.SeqPlugin;
import com.architexa.diagrams.jdt.RJCore;
import com.architexa.diagrams.model.NamedRel;
import com.architexa.diagrams.ui.DefaultAfterConnectionCreationTool;
import com.architexa.diagrams.ui.RSEEditor;
import com.architexa.org.eclipse.gef.palette.CombinedTemplateCreationEntry;
import com.architexa.org.eclipse.gef.palette.ConnectionCreationToolEntry;
import com.architexa.org.eclipse.gef.palette.PaletteContainer;
import com.architexa.org.eclipse.gef.palette.PaletteGroup;
import com.architexa.org.eclipse.gef.palette.PaletteRoot;
import com.architexa.org.eclipse.gef.palette.PaletteSeparator;
import com.architexa.org.eclipse.gef.palette.SelectionToolEntry;
import com.architexa.org.eclipse.gef.palette.ToolEntry;
import com.architexa.org.eclipse.gef.requests.SimpleFactory;

/**
 * 
 * @author Elizabeth L. Murnane
 *
 */
public class SeqEditorPaletteFactory {

	public static PaletteRoot createPalette(SeqEditor editor) {
		PaletteRoot palette = new PaletteRoot();
		palette.add(createControlGroup(palette, editor));
//		palette.add(createComponentsDrawer());
		return palette;
	}

	private static PaletteContainer createControlGroup(PaletteRoot root, SeqEditor editor) {
		PaletteGroup toolGroup = new PaletteGroup("Control Group");

		ToolEntry tool = new SelectionToolEntry();
		toolGroup.add(tool);
		root.setDefaultEntry(tool);

		toolGroup.add(new PaletteSeparator("Entities"));
		
		toolGroup.add(RSEEditor.getImportImagePaletteEntry());
        toolGroup.add(RSEEditor.getActorPaletteEntry());
        toolGroup.add(RSEEditor.getDatabasePaletteEntry());
        toolGroup.add(RSEEditor.getCommentPaletteEntry());
        
        toolGroup.add(new PaletteSeparator("Code Components"));

		SimpleFactory factory = editor.getCreationFactory(UserCreatedInstanceModel.class);
		ImageDescriptor img = SeqPlugin.getImageDescriptor("icons_palette/palette_chrono_class.PNG");
		toolGroup.add(new CombinedTemplateCreationEntry(
				"Class", 
				"Press here, then click in the diagram to add a class",
				factory,
				factory,
				img, 
				img));
		
		img = SeqPlugin.getImageDescriptor("icons_palette/palette_chrono_method.PNG");
		toolGroup.add(new CombinedTemplateCreationEntry(
				"Method", 
				"Press here, then click below a class in the diagram to add a method to it",
				new SimpleFactory(UserCreatedMethodBoxModel.class),
				new SimpleFactory(UserCreatedMethodBoxModel.class),
				img, 
				img));
		
		toolGroup.add(new PaletteSeparator("Relationships"));

		String relDesc = "Press here, then click any item in the diagram and then " +
		"click another item in the diagram to draw this connection between them";

		img = SeqPlugin.getImageDescriptor("icons_palette/palette_chrono_call.png");
		ToolEntry callEntry = new ConnectionCreationToolEntry(
				"Method Call", 
				relDesc,
				new SimpleFactory(NamedRel.class), 
				img, img);
		callEntry.setToolClass(DefaultAfterConnectionCreationTool.class);
		toolGroup.add(callEntry);

		img = SeqPlugin.getImageDescriptor("icons_palette/palette_chrono_overrides.png");
		ToolEntry overrideEntry = new ConnectionCreationToolEntry(
				"Method Override", 
				relDesc,
				new SimpleFactory(NamedRel.class) {
					@Override
					public Object getNewObject() {
						NamedRel rel = (NamedRel) super.getNewObject();
						rel.setType(RJCore.overrides);
						rel.setUserCreated(true);
						return rel;
					}
				}, 
				img, img);
		overrideEntry.setToolClass(DefaultAfterConnectionCreationTool.class);
		toolGroup.add(overrideEntry);

		toolGroup.add(RSEEditor.getGeneralConnectionPaletteEntry());

		return toolGroup;
	}

//	private static PaletteContainer createComponentsDrawer() {
//		PaletteDrawer componentsDrawer = new PaletteDrawer("Nodes");
//
//		CombinedTemplateCreationEntry component = new CombinedTemplateCreationEntry(
//				"Blob", 
//				"Make a blob", 
//				InstanceModel.class,
//				new SimpleFactory(InstanceModel.class), 
//				ImageDescriptor.createFromFile(InstanceModel.class, "something.gif"), 
//				ImageDescriptor.createFromFile(InstanceModel.class, "something.gif"));
//		componentsDrawer.add(component);
//
//		return componentsDrawer;
//	}

}
