package com.architexa.diagrams.model;

import java.io.IOException;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.rio.RdfDocumentWriter;

import com.architexa.diagrams.RSECore;
import com.architexa.diagrams.commands.WidthChangeActionCommand;
import com.architexa.diagrams.parts.NamedRelationPart;
import com.architexa.diagrams.ui.RSEContextMenuProvider;
import com.architexa.org.eclipse.gef.EditPart;
import com.architexa.org.eclipse.gef.GraphicalViewer;
import com.architexa.org.eclipse.gef.commands.CompoundCommand;
import com.architexa.store.ReloRdfRepository;
import com.architexa.store.StoreUtil;

public class WidthDPolicy extends DiagramPolicy{

	////
	// basic setup
	////
	public static final String DefaultKey = "WidthDiagramPolicy";
	public static final WidthDPolicy Type = new WidthDPolicy();
	
	////
	// Policy Fields, Constructors and Methods 
	////
	public static final URI widthURI = RSECore.createRseUri("core#width");
	int relWidth = 2;
	String separator = ";;";
	
	public WidthDPolicy() {}
	
	@Override
	public void writeRDF(RdfDocumentWriter rdfWriter) throws IOException {
		rdfWriter.writeStatement(getHostRel().getInstanceRes(), widthURI,
				StoreUtil.createMemLiteral(Integer.toString(relWidth)));
	}

	
	@Override
	public void readRDF(ReloRdfRepository queryRepo) {
		Value col = queryRepo.getStatement(getHostRel().getInstanceRes(), widthURI, null).getObject();
		if (col != null) {
			setWidth(Integer.parseInt(((Literal)col).getLabel()));
		}
	}
	
	private void setWidth(int width) {
		relWidth = width;
		getHostRel().firePropChang(DefaultKey);
	}
	
	public static void setWidth(int width, NamedRel af) {
		WidthDPolicy pol = (WidthDPolicy) af.getDiagramPolicy(DefaultKey);
		if (pol != null)
			pol.setWidth(width);
	}
	
	public int getWidth() {
		return relWidth;
	}
	
	public static int getWidth(ArtifactRel af) {
		WidthDPolicy pol = (WidthDPolicy) af.getDiagramPolicy(DefaultKey);
		if (pol != null)
			return pol.getWidth();
		return -1;
	}
	
	
	public static void addConnectionWidthChangeAction(final List<?> sel, IMenuManager menu, 
			GraphicalViewer graphicalViewer) {
		for (Object item : sel) {
			if (!(item instanceof NamedRelationPart)) return;
		}
		MenuManager subMenu = new MenuManager("Connection Width");
		subMenu.add(getWidthChangeAction("Decrease Width", sel, graphicalViewer));
		subMenu.add(getWidthChangeAction("Increase Width", sel, graphicalViewer));
		subMenu.add(new Separator());
		subMenu.add(getWidthChangeAction("Default", sel, graphicalViewer));

		// Action to change connection width goes in the edit appearance section
		menu.appendToGroup(RSEContextMenuProvider.GROUP_RSE_EDIT_APPEARANCE, subMenu);
	}
	
	private static IAction getWidthChangeAction(final String actnText, final List<?> sel, final GraphicalViewer graphicalViewer) {
		return new Action(actnText) {
			@Override
			public void run() {
				CompoundCommand cmd = new CompoundCommand("Changing Width: " + actnText);
					for (Object part : sel) {
						if (part instanceof NamedRelationPart)
							cmd.add(new WidthChangeActionCommand(actnText, (NamedRel) ((EditPart) part).getModel()));
					}
					
				graphicalViewer.getEditDomain().getCommandStack().execute(cmd);
			}
		};
	}
	
}
