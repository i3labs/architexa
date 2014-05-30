package com.architexa.diagrams.chrono.ui;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IInitializer;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.internal.core.ClassFile;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;

import com.architexa.diagrams.chrono.editparts.DiagramEditPart;
import com.architexa.diagrams.chrono.editparts.InstanceEditPart;
import com.architexa.diagrams.chrono.models.ConnectionModel;
import com.architexa.diagrams.chrono.models.DiagramModel;
import com.architexa.diagrams.chrono.models.InstanceModel;
import com.architexa.diagrams.chrono.models.MethodBoxModel;
import com.architexa.diagrams.chrono.models.NodeModel;
import com.architexa.diagrams.chrono.sequence.ChronoDiagramEngine;
import com.architexa.diagrams.chrono.sequence.SeqPlugin;
import com.architexa.diagrams.chrono.util.ConnectionUtil;
import com.architexa.diagrams.chrono.util.InstanceUtil;
import com.architexa.diagrams.chrono.util.MethodUtil;
import com.architexa.diagrams.chrono.util.SeqUtil;
import com.architexa.diagrams.editors.RSEMultiPageEditor;
import com.architexa.diagrams.jdt.InitializerWrapper;
import com.architexa.diagrams.jdt.Invocation;
import com.architexa.diagrams.jdt.JDTSelectionUtils;
import com.architexa.diagrams.jdt.RJCore;
import com.architexa.diagrams.jdt.actions.OpenVizAction;
import com.architexa.diagrams.jdt.builder.ResourceQueue;
import com.architexa.diagrams.jdt.model.CodeUnit;
import com.architexa.diagrams.model.ArtifactFragment;
import com.architexa.diagrams.model.ArtifactRel;
import com.architexa.diagrams.utils.OpenItemUtils;
import com.architexa.org.eclipse.gef.EditPart;
import com.architexa.rse.BuildStatus;
import com.architexa.store.ReloRdfRepository;
import com.architexa.store.StoreUtil;


/**
 * 
 * @author Elizabeth L. Murnane
 *
 */
public class OpenInChronoAction extends OpenVizAction {
	private static final Logger logger = SeqPlugin.getLogger(OpenInChronoAction.class);

	public OpenInChronoAction() {
		setText(ChronoDiagramEngine.diagramTypeText);
		setImageDescriptor(SeqPlugin.getImageDescriptor("icons/chrono-document.png"));
	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		// dont open projects, or packages
		IJavaElement selectedElt = JDTSelectionUtils.getSelectedJDTElement();
		if(selectedElt==null || selectedElt instanceof IJavaProject || 
				selectedElt instanceof IPackageFragment || 
				(selection instanceof IStructuredSelection 
						&& (OpenItemUtils.containsProject((IStructuredSelection) selection) || OpenItemUtils.containsPackage((IStructuredSelection) selection)))
						|| selectedElt instanceof IPackageFragmentRoot) { 
			action.setEnabled(false);
		} else {
			action.setEnabled(true);
		}
	}

	@Override
	public void openViz(IWorkbenchWindow activeWorkbenchWindow, List<?> selList) {
		openViz(activeWorkbenchWindow, selList, null, null, null);
	}

	public static void openChronoViz(final IWorkbenchWindow activeWorkbenchWindow, final Collection<ArtifactFragment> collection, final Map<IDocument, IDocument> lToRDocMap, final Map<IDocument, String> lToPathMap, final StringBuffer docBuff)  {
		try {
			new ProgressMonitorDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell()).run(true, false, new IRunnableWithProgress(){

				public void run(IProgressMonitor monitor)
				throws InvocationTargetException, InterruptedException {
					monitor.beginTask("Creating Sequence Diagram...", IProgressMonitor.UNKNOWN);

					PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable(){

						public void run() {
							final IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
							SeqEditor seqEditor = null;
							RSEMultiPageEditor mpe = null;
							Map<ArtifactFragment, NodeModel> artFragToNodeModelMap = new HashMap<ArtifactFragment, NodeModel>();
							DiagramModel diagram = null;
							// Use View if one exists
							SeqView view = (SeqView) activeWorkbenchWindow.getActivePage().findView(SeqView.viewId);
							if (view != null) {
								// make sure the view is shown
								activeWorkbenchWindow.getActivePage().activate(view);
								diagram = view.getModel();
								if (!ResourceQueue.isEmpty())
									view.getDiagramController().addUnbuiltWarning();
							} else {
								try {
									if (lToRDocMap == null || lToPathMap == null)
										mpe = (RSEMultiPageEditor) page.openEditor(new SeqDiagramEditorInput(), RSEMultiPageEditor.editorId);
									else
										mpe = (RSEMultiPageEditor) page.openEditor(new SeqDiagramEditorInput(lToRDocMap, lToPathMap, docBuff), RSEMultiPageEditor.editorId);
									
									seqEditor = (SeqEditor) mpe.getRseEditor();
									
								} catch (PartInitException e) {
									logger.error("Unexpected Exception.", e);
								}
								if (seqEditor == null) return;
								diagram = seqEditor.getModel(); 
								if (!ResourceQueue.isEmpty())
									seqEditor.getDiagramController().addUnbuiltWarning();
							}

							for(ArtifactFragment frag : collection) {
								addNodesToChrono(frag, diagram, artFragToNodeModelMap);
							}
							addConnectionsToChrono(artFragToNodeModelMap);
							
							// Select the first instance box 
							DiagramEditPart diagEP;
							if (view != null)diagEP = view.getDiagramController();
							else if (seqEditor != null) diagEP = seqEditor.getDiagramController();
							else return;
							for (Object instanceChild : diagEP.getChildren()) {
								if (instanceChild instanceof InstanceEditPart) {
									diagEP.getViewer().getSelectionManager().appendSelection((EditPart) instanceChild);
									break;
								}
							}

						}
					});
				}
			});
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private static void addNodesToChrono(ArtifactFragment child, DiagramModel diagram, Map<ArtifactFragment, NodeModel> artFragToNodeModelMap) {

		// Need to determine the appropriate index for the instance here so that if any of
		// its nested classes are added to the diagram by SeqUtil.convertToChronoModels(), 
		// it can be added before them 
		int indexForChild = diagram.getChildren().size(); 
		if(child instanceof CodeUnit) {
			NodeModel node = SeqUtil.convertToChronoModels((CodeUnit)child, diagram, diagram, artFragToNodeModelMap);
			if(node==null) {
				// A Relo or Strata diagram is being converted to a Chrono one, and 
				// there is no diagram component in Chrono that corresponds to
				// this Relo or Strata component. Therefore, it cannot be represented
				// in Chrono, but its children could be
				for(Object artFrag : child.getShownChildren()) {
					if(!(artFrag instanceof ArtifactFragment)) continue;
					addNodesToChrono((ArtifactFragment)artFrag, diagram, artFragToNodeModelMap);
				}
			} else {
			}
			child = node;
		}
		diagram.addChild(child, indexForChild);
	}

	private static void addConnectionsToChrono(Map<ArtifactFragment, NodeModel> artFragToNodeModelMap) {
		for(ArtifactFragment artFrag : artFragToNodeModelMap.keySet()) {
			for(Object conn : artFrag.getSourceConnections()) {
				if(!(conn instanceof ArtifactRel)) continue;

				URI connType = ((ArtifactRel)conn).relationRes;
				if(RJCore.calls.equals(connType)) {
					addInvocationConnectionToChrono(artFrag, (ArtifactRel)conn, artFragToNodeModelMap);
				} else if(RJCore.overrides.equals(connType)) {
					MethodBoxModel overriderMethod = (MethodBoxModel) artFragToNodeModelMap.get(artFrag);
					MethodBoxModel superClassMethod = (MethodBoxModel) artFragToNodeModelMap.get(((ArtifactRel)conn).getDest());
					ConnectionUtil.createConnection("overrides", overriderMethod, superClassMethod, ConnectionModel.OVERRIDES);
				} else if(RJCore.inherits.equals(connType)) {
					NodeModel source = artFragToNodeModelMap.get(artFrag);
					NodeModel target = artFragToNodeModelMap.get(((ArtifactRel)conn).getDest());
					ConnectionUtil.createConnection("inherits", source, target, connType);
				}
			}
		}
	}

	private static void addInvocationConnectionToChrono(ArtifactFragment source, ArtifactRel conn, Map<ArtifactFragment, NodeModel> artFragToNodeModelMap) {
		ArtifactFragment target = ((ArtifactRel)conn).getDest();
		MethodBoxModel declarationOfInvokedMethod = (MethodBoxModel) artFragToNodeModelMap.get(target);

		if(declarationOfInvokedMethod==null) return;
		if(declarationOfInvokedMethod.getPartner()!=null) return; // invocation partner already in diagram

		Invocation invocation = null;
		MethodBoxModel declarationContainingInvocation = (MethodBoxModel) artFragToNodeModelMap.get(source);
		for(Invocation call : declarationContainingInvocation.getCallsMade(null)) {

			// Added null check for library methods which have null IMethods.
			if(call.getMethodElement()==null || (declarationOfInvokedMethod.getMethod() != null &&
					!call.getMethodElement().equals(declarationOfInvokedMethod.getMethod()))) continue;

			invocation = call;
		}
		if(invocation==null) return;

		MethodBoxModel invocationModel = MethodUtil.createInvocationModel(invocation, declarationContainingInvocation.getInstanceModel());
		declarationContainingInvocation.addChild(invocationModel);

		ConnectionUtil.createConnection(MethodUtil.getMethodName(invocationModel.getMethodRes(), invocation), invocationModel, declarationOfInvokedMethod, ((ArtifactRel)conn).getType());
		ConnectionUtil.createConnection(MethodUtil.getReturnMessage(invocation), declarationOfInvokedMethod, invocationModel, ConnectionModel.RETURN);

		boolean callToSameClass = invocationModel.getInstanceModel().equals(declarationOfInvokedMethod.getInstanceModel());
		if(callToSameClass) {
			declarationOfInvokedMethod.getParent().removeChild(declarationOfInvokedMethod);
			invocationModel.addChild(declarationOfInvokedMethod);
		}
	}

	@Override
	public void openViz(IWorkbenchWindow activeWorkbenchWindow,
			List<?> selList, Map<IDocument, IDocument> lToRDocMap, Map<IDocument, String> lToPathMap, StringBuffer docBuff) {
		BuildStatus.addUsage("Chrono");
		if (selList.isEmpty() ) return;

		ReloRdfRepository repo = StoreUtil.getDefaultStoreRepository();
		Map<Resource, ArtifactFragment> toBeAddedResToAF = new LinkedHashMap<Resource, ArtifactFragment>();
		for (Object selectedElt : selList) {
			if(selectedElt==null) continue; 

			if(selectedElt instanceof ArtifactFragment) {
				toBeAddedResToAF.put(
						((ArtifactFragment)selectedElt).getArt().elementRes, 
						((ArtifactFragment)selectedElt));
			}
			if(!(selectedElt instanceof IJavaElement)) continue;

			if(selectedElt instanceof IInitializer)
				selectedElt = new InitializerWrapper((IInitializer)selectedElt);

			if(selectedElt instanceof IMethod && ((IMethod)selectedElt).getParent() instanceof IType) {
				IMethod method = (IMethod)selectedElt;
				IType containerClass = (IType) method.getParent();
				Resource classRes = RJCore.jdtElementToResource(repo, containerClass);
				InstanceModel instance = new InstanceModel(null, InstanceUtil.getClassName(classRes, repo), classRes);
				if (toBeAddedResToAF.keySet().contains(classRes))
					instance = (InstanceModel) toBeAddedResToAF.get(classRes); 
				else toBeAddedResToAF.put(classRes, instance);

				MethodBoxModel methodModel = new MethodBoxModel(instance, method, MethodBoxModel.declaration);
				instance.addChild(methodModel);
			} else if(selectedElt instanceof IType || selectedElt instanceof ICompilationUnit || selectedElt instanceof ClassFile) {
				Resource classRes = RJCore.jdtElementToResource(repo, (IJavaElement) selectedElt);
				InstanceModel instance = new InstanceModel(null, InstanceUtil.getClassName(classRes, repo), classRes);
				toBeAddedResToAF.put(instance.getInstanceRes(), instance);
			}
		}
		openChronoViz(activeWorkbenchWindow, toBeAddedResToAF.values(), lToRDocMap, lToPathMap, docBuff);
	}

}
