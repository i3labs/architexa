package com.architexa.diagrams.ui;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;

import com.architexa.diagrams.model.ArtifactFragment;

public class PluggableDiagramsSupport {

	public interface IRSEDiagramEngine {
		Class<? extends SelectableAction> getOpenActionClass();
		String diagramType();
		String diagramUsageName();
		int getMenuPosition();
		ImageDescriptor getImageDescriptor();
		ImageDescriptor getNewEditorImageDescriptor();
		IEditorInput newEditorInput();
		String editorId();
		Class<? extends RSEEditor> getEditorClass();
		Collection<? extends ArtifactFragment> getShownChildren(RSEEditor activeEditor);
		void openSwitchedDiagramEditor(List<ArtifactFragment> shownAF);
		void openDiagramFromNavigatedTabs(
				List<ArtifactFragment> navigatedTabFragsWithChildren, 
				List<ArtifactFragment> allNavigatedTabs);
		RSEAction getTracker();
	};

	private static Set<IRSEDiagramEngine> registeredDiagramEngines = 
		new TreeSet<IRSEDiagramEngine>(
				new Comparator<IRSEDiagramEngine>() {
					public int compare(IRSEDiagramEngine eng0, IRSEDiagramEngine eng1) {
						return eng0.getMenuPosition() - eng1.getMenuPosition();
					}
				}
		);

	public static void registerDiagram(IRSEDiagramEngine diagEngine) {
		registeredDiagramEngines.add(diagEngine);
	}

	public static Set<IRSEDiagramEngine> getRegisteredDiagramEngines() {
		return registeredDiagramEngines;
	}

	public static boolean isRegistered(IRSEDiagramEngine diagEngine) {
		for (IRSEDiagramEngine registeredDiagram : registeredDiagramEngines) {
			if (registeredDiagram.getClass().equals(diagEngine.getClass())) return true;
		}
		return false;
	}

}
