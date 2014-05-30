package com.architexa.diagrams.chrono.sequence;

import java.util.Collection;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IStartup;
import org.eclipse.ui.IWorkbenchWindow;

import com.architexa.diagrams.RSECore;
import com.architexa.diagrams.chrono.models.DiagramModel;
import com.architexa.diagrams.chrono.ui.OpenInChronoAction;
import com.architexa.diagrams.chrono.ui.SeqDiagramEditorInput;
import com.architexa.diagrams.chrono.ui.SeqEditor;
import com.architexa.diagrams.model.ArtifactFragment;
import com.architexa.diagrams.model.Comment;
import com.architexa.diagrams.model.NamedRel;
import com.architexa.diagrams.parts.CommentEditPart;
import com.architexa.diagrams.parts.NamedRelationPart;
import com.architexa.diagrams.services.PluggableTypes;
import com.architexa.diagrams.services.PluggableTypes.PluggableTypeInfo;
import com.architexa.diagrams.ui.PluggableDiagramsSupport;
import com.architexa.diagrams.ui.RSEAction;
import com.architexa.diagrams.ui.RSEEditor;
import com.architexa.diagrams.ui.SelectableAction;

public class ChronoDiagramEngine implements IStartup, PluggableDiagramsSupport.IRSEDiagramEngine {
	static final Logger logger = SeqPlugin.getLogger(ChronoDiagramEngine.class);

	public static String diagramTypeText = "Sequence Diagram";

	public void earlyStartup() {
		try {
			earlyStartupInternal();
		} catch (Throwable t) {
			logger.error("Unexpected Error", t);
		}
	}
	private void earlyStartupInternal() {
		PluggableDiagramsSupport.registerDiagram(this);
		PluggableTypes.registerType(new PluggableTypeInfo(RSECore.commentType, "Comment", Comment.class, CommentEditPart.class));
        PluggableTypes.registerType(new PluggableTypeInfo(RSECore.namedRel, "named relationship", NamedRel.class, NamedRelationPart.class));
	}

	public String diagramType() {
		return diagramTypeText;
	}

	public String diagramUsageName() {
		return "Chrono";
	}

	public int getMenuPosition() {
		return 3;
	}

	public String editorId() {
		return SeqEditor.editorId;
	}

	public Class<? extends SelectableAction> getOpenActionClass() {
		return OpenInChronoAction.class;
	}

	public ImageDescriptor getImageDescriptor() {
		return SeqPlugin.getImageDescriptor("icons/chrono-document.png");
	}

	public ImageDescriptor getNewEditorImageDescriptor() {
		return SeqPlugin.getImageDescriptor("icons/new_chrono.png");
	}

	public IEditorInput newEditorInput() {
		return new SeqDiagramEditorInput();
	}

	public Class<? extends RSEEditor> getEditorClass() {
		return SeqEditor.class;
	}

	public Collection<? extends ArtifactFragment> getShownChildren(RSEEditor activeEditor) {
		return ((DiagramModel)((SeqEditor)activeEditor).getModel()).getChildren();
	}

	public void openSwitchedDiagramEditor(List<ArtifactFragment> shownAF) {
		IWorkbenchWindow activeWorkbenchWindow = SeqPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow();
		OpenInChronoAction.openChronoViz(activeWorkbenchWindow, shownAF, null, null, null);
	}

	public void openDiagramFromNavigatedTabs(
			List<ArtifactFragment> navigatedTabFragsWithChildren, 
			List<ArtifactFragment> allNavigatedTabs) {
		IWorkbenchWindow activeWorkbenchWindow = SeqPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow();
		OpenInChronoAction.openChronoViz(activeWorkbenchWindow, navigatedTabFragsWithChildren, null, null, null);
	}

	public RSEAction getTracker() {
		return new com.architexa.diagrams.chrono.ui.JDTLinkedTracker.LinkedExploration();
	}

}
