package com.architexa.diagrams.generate.debugger;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.IDebugEventSetListener;
import org.eclipse.debug.core.Launch;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.ISourceLocator;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.IThread;
import org.eclipse.debug.internal.ui.views.launch.LaunchView;
import org.eclipse.debug.ui.contexts.AbstractDebugContextProvider;
import org.eclipse.debug.ui.contexts.DebugContextEvent;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.debug.core.IJavaDebugTarget;
import org.eclipse.jdt.debug.core.IJavaStackFrame;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.ListSelectionDialog;
import org.openrdf.model.Resource;
import org.openrdf.model.Value;

import com.architexa.diagrams.RSECore;
import com.architexa.diagrams.chrono.models.ConnectionModel;
import com.architexa.diagrams.chrono.models.DiagramModel;
import com.architexa.diagrams.chrono.models.InstanceModel;
import com.architexa.diagrams.chrono.models.MethodBoxModel;
import com.architexa.diagrams.chrono.models.MethodInvocationModel;
import com.architexa.diagrams.chrono.models.NodeModel;
import com.architexa.diagrams.chrono.ui.JDTLinkedTracker;
import com.architexa.diagrams.chrono.ui.SeqDiagramEditorInput;
import com.architexa.diagrams.chrono.ui.SeqEditor;
import com.architexa.diagrams.chrono.ui.SeqEditorFilter;
import com.architexa.diagrams.chrono.util.ConnectionUtil;
import com.architexa.diagrams.chrono.util.MethodUtil;
import com.architexa.diagrams.editors.RSEMultiPageEditor;
import com.architexa.diagrams.generate.GeneratePlugin;
import com.architexa.diagrams.jdt.RJCore;
import com.architexa.diagrams.jdt.builder.asm.AsmUtil;
import com.architexa.diagrams.jdt.ui.JDTDebugIdSupport;
import com.architexa.diagrams.jdt.utils.UIUtils;
import com.architexa.diagrams.model.ArtifactFragment;
import com.architexa.diagrams.model.ColorDPolicy;
import com.architexa.org.eclipse.draw2d.MouseEvent;
import com.architexa.org.eclipse.draw2d.MouseListener;
import com.architexa.store.ReloRdfRepository;
import com.architexa.store.StoreUtil;


/**
 * 
 * @author Elizabeth L. Murnane
 *
 */
public class DebuggerDiagramGenerator extends Action implements IDebugEventSetListener {
	
	private static final Logger logger = GeneratePlugin.getLogger(DebuggerDiagramGenerator.class);

	RSEMultiPageEditor debugDiagramEditor = null;
	Map<IJavaStackFrame, MethodBoxModel> framesInCurrentDiagram = new HashMap<IJavaStackFrame, MethodBoxModel>();
	boolean filter = false; // true if the diagram should not include library code, false otherwise
	boolean group  = false; // true if library code needs to be grouped

	@Override
	public void run() {
		JDTLinkedTracker.setDebugListener(this);

		IThread thread = null;
		IWorkbenchWindow activeWorkbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		ISelection selection = activeWorkbenchWindow.getSelectionService().getSelection();
		
		try {
			Object selectedElmt = null;
			if(selection instanceof TreeSelection)
				selectedElmt = ((TreeSelection)selection).getFirstElement();
			
			if(selectedElmt instanceof Launch) {
				List<IThread> threads = new ArrayList<IThread>();
				IDebugTarget[] targets = ((Launch)selectedElmt).getDebugTargets();
				for(IDebugTarget target : targets)
					threads.addAll(Arrays.asList(target.getThreads()));
				thread = selectThread(threads);
			} else if(selectedElmt instanceof IJavaDebugTarget) {
				IThread[] threadsToChooseFrom = ((IJavaDebugTarget)selectedElmt).getThreads();
				thread = selectThread(Arrays.asList(threadsToChooseFrom));
			} else if(selectedElmt instanceof IThread) {
				thread = (IThread) selectedElmt;
			} else if(selectedElmt instanceof IJavaStackFrame) {
				thread = ((IJavaStackFrame)selectedElmt).getThread();
			}

		} catch(DebugException e) {
			e.printStackTrace();
		}

		try {
			if(thread==null || !thread.hasStackFrames()) {
				MessageDialog.openInformation(
						null, "Open Stack Trace in Sequence Diagram", 
						"Cannot open diagram because there is no thread " +
						"that contains stack frames.");
				return;
			}
		} catch (DebugException e1) {
			logger.error("Exception while testing if thread contains stack frames. ", e1);
		}

		final List<IJavaStackFrame> frames = new ArrayList<IJavaStackFrame>();
		try {
			for(IStackFrame stackFrame : thread.getStackFrames()) {
				// Not sure why we needed this but it was causing items with no
				// parameters to be removed from the stack trace diagram
				//// if(!stackFrame.hasVariables() || !(stackFrame instanceof IJavaStackFrame)) continue;
				// Using the below instead
				if( !(stackFrame instanceof IJavaStackFrame)) continue;
				IJavaStackFrame frame = (IJavaStackFrame) stackFrame;
				frames.add(frame);
			}
		} catch (DebugException e1) {
			System.out.println("Exception while creating diagram during debugging process. " + e1.getMessage());
			e1.printStackTrace();
		}
		Collections.reverse(frames);
		if(frames.size() > 0) {
			filter = "generate.OpenFilteredTraceInChronoViewerAction".equals(getId());
			group = "generate.OpenGroupedTraceInChronoViewerAction".equals(getId());
			PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
				public void run() {
					generateDiagram(frames);
				}
			});
		}
	}

	public void handleDebugEvents(DebugEvent[] events) {
		final List<IJavaStackFrame> frames = new ArrayList<IJavaStackFrame>();
		for(int i=0; i<events.length; i++) {
			DebugEvent event = events[i];
			Object source = event.getSource();
			// source could be instance of IProcess, IDebugTarget, IThread, IStackFrame, IVariable, IValue
			if(!(source instanceof IThread)) continue;
			IThread thread = (IThread) source;
			try {
				if(!thread.hasStackFrames()) continue;
				for(IStackFrame stackFrame : thread.getStackFrames()) {
					if(!stackFrame.hasVariables() || !(stackFrame instanceof IJavaStackFrame)) continue;
					IJavaStackFrame frame = (IJavaStackFrame) stackFrame;
					frames.add(frame);
				}
			} catch (DebugException e1) {
				System.out.println("Exception while creating diagram during debugging process. " + e1.getMessage());
				e1.printStackTrace();
			}
		}

		if(frames.equals(framesInCurrentDiagram)) return; // if stack trace hasn't changed just leave diagram alone

		Collections.reverse(frames);
		if(frames.size()>0) {
			PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
				public void run() {
					generateDiagram(frames);
				}
			});
		}
	}

	/**
	 * Generates a diagram based on the given stack frames and 
	 * draws it in the Chrono editor containing a debug diagram 
	 * or a new editor if no such Chrono editor is already open
	 *
	 */
	public void generateDiagram(final List<IJavaStackFrame> newFrames) {

		try {
			new ProgressMonitorDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell()).run(true, false, new IRunnableWithProgress(){

				public void run(IProgressMonitor monitor)
				throws InvocationTargetException, InterruptedException {
					monitor.beginTask("Generating diagram from stack trace...", IProgressMonitor.UNKNOWN);

					PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable(){

						public void run() {
							if(debugDiagramEditor==null || !isDiagramAlreadyOpen()) {
								framesInCurrentDiagram.clear();
								IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
								try {
									debugDiagramEditor = (RSEMultiPageEditor) window.getActivePage().openEditor(new SeqDiagramEditorInput(), RSEMultiPageEditor.editorId);
									PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().activate(debugDiagramEditor);
								} catch (PartInitException e) {
									System.out.println("Could not open the sequence diagram in an editor. " + e.getMessage());
									e.printStackTrace();
								}
							}

							if(debugDiagramEditor==null) return;
							
							// Methods (frames) in diagram are about to be updated, so remove
							// highlighting from what will now be the "old" methods so that
							// the "new" methods corresponding to the frames currently in the
							// stack trace can be highlighted
							for(IJavaStackFrame frame : framesInCurrentDiagram.keySet()) {
								MethodBoxModel method = framesInCurrentDiagram.get(frame);
								//method.removeDebugHighlight();
							}

							DiagramModel diagram = ((SeqEditor)debugDiagramEditor.getRseEditor()).getModel();
							List<MethodBoxModel> newMethods = new ArrayList<MethodBoxModel>();
							for(IJavaStackFrame frame : newFrames) {
								if(framesInCurrentDiagram.containsKey(frame)) continue;

								try {
									ReloRdfRepository repo = StoreUtil.getDefaultStoreRepository();
									String id =  JDTDebugIdSupport.getId(frame.getReferenceType());
									Resource classRes = RSECore.idToResource(repo, RJCore.jdtWkspcNS, id);
									String className = frame.getDeclaringTypeName().substring(frame.getDeclaringTypeName().lastIndexOf(".")+1);

									MethodBoxModel previousMethod = null;
									if(newMethods.size()!=0) {
										previousMethod = newMethods.get(newMethods.size()-1);
									} else if(newFrames.indexOf(frame)>0 && framesInCurrentDiagram.containsKey(newFrames.get(newFrames.indexOf(frame)-1))) {
										previousMethod = framesInCurrentDiagram.get(newFrames.get(newFrames.indexOf(frame)-1));
									}

									NodeModel nodeModel;
									if(previousMethod==null) {
										nodeModel = new InstanceModel(null, className, classRes);
										diagram.addChild(nodeModel);
									} else {
										InstanceModel previousInstance = previousMethod.getInstanceModel();
										if(previousInstance.getClassName().equals(className) && previousInstance.getResource().equals(classRes)) {
											// call to the same class
											nodeModel = previousMethod;
										} else {
											nodeModel = new InstanceModel(null, className, classRes);
											diagram.addChild(nodeModel);
										}
									}

									String methodName = frame.getMethodName();
									String argumentNames = "(";
									for(Iterator<?> argIter = frame.getArgumentTypeNames().iterator(); argIter.hasNext();) {
										String arg = (String)argIter.next().toString();
										argumentNames = argumentNames.concat(arg + ", ");
									}
									argumentNames = argumentNames.concat(")").replace(", )", ")");
									methodName = id.concat(".").concat(methodName).concat(argumentNames);
									Resource method = RSECore.idToResource(repo, RJCore.jdtWkspcNS, methodName);//AsmUtil.getClassKey(methodName, repo);

									InstanceModel instance = (nodeModel instanceof InstanceModel) ? (InstanceModel)nodeModel : ((MethodBoxModel)nodeModel).getInstanceModel();
									MethodBoxModel calledMethodBox = new MethodBoxModel(instance, method, MethodBoxModel.declaration);
									ArtifactFragment.ensureInstalledPolicy(calledMethodBox, ColorDPolicy.DefaultKey, ColorDPolicy.class);
									framesInCurrentDiagram.put(frame, calledMethodBox);

									if(previousMethod==null) {
										newMethods.add(calledMethodBox);
										nodeModel.addChild(calledMethodBox);
										continue;
									}

									MethodInvocationModel callerMethodBox = new MethodInvocationModel(previousMethod.getInstanceModel(), method, MethodBoxModel.access);

									previousMethod.addChild(callerMethodBox);
									if(nodeModel.equals(previousMethod)) callerMethodBox.addChild(calledMethodBox);
									else nodeModel.addChild(calledMethodBox);

									String message = MethodUtil.getMethodName(method, calledMethodBox.getASTNode(), true);
									ConnectionUtil.createConnection(message, callerMethodBox, calledMethodBox, ConnectionModel.CALL);

									Value returnValue = repo.getStatement((Resource)method, RJCore.returnType, null).getObject();
									String returnMessage = MethodUtil.getMethodName(returnValue, callerMethodBox.getASTNode(), true);
									ConnectionUtil.createConnection(returnMessage, calledMethodBox, callerMethodBox, ConnectionModel.RETURN);

									newMethods.add(calledMethodBox);

									if(frame.getLaunch()==null) continue;
									ISourceLocator locator = frame.getLaunch().getSourceLocator();
									if(locator == null) continue;

									Object sourceElement = locator.getSourceElement(frame);
									if(!(sourceElement instanceof IJavaElement) && sourceElement instanceof IAdaptable) {
										sourceElement = ((IAdaptable)sourceElement).getAdapter(IJavaElement.class);
									}
									if(sourceElement instanceof IJavaElement && 
											((IJavaElement)sourceElement).getResource() instanceof IFile) {

										calledMethodBox.getInstanceModel().setResource(AsmUtil.toWkspcResource(StoreUtil.getDefaultStoreRepository(), UIUtils.getName(((IJavaElement)sourceElement))));

										if(newFrames.indexOf(frame)>0) {
											IJavaStackFrame previousFrame = newFrames.get(newFrames.indexOf(frame)-1);
											callerMethodBox.setLineNum(previousFrame.getLineNumber());
										}
									}
								} catch (DebugException e) {
									System.out.println("Exception while creating diagram during debugging process. " + e.getMessage());
									e.printStackTrace();
								}
							}
							
							SeqEditorFilter.setActiveEditor((SeqEditor)debugDiagramEditor.getRseEditor());
							
							// since we are not setting an editor with an action we do not need to check the action since it will be null
							// SeqEditorFilter.setActionChecked(filter);
							
							if(filter) SeqEditorFilter.hideFigures();
							if(group) SeqEditorFilter.groupFigures();
							
							// disable highlighting stack trace elements for now. Needs more testing
							//highlightMethodsInCurrentStacktrace();
							linkDiagramToVariablesView();
							
						}
					
					});
				}
			});
		} catch (InvocationTargetException e1) {
			e1.printStackTrace();
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}

		
	}

	private boolean isDiagramAlreadyOpen() {
		if(debugDiagramEditor==null) return false;
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		for(IEditorReference ref : window.getActivePage().getEditorReferences()) {
			if(debugDiagramEditor.equals(ref.getEditor(false))) {
				return true;
			}
		}
		return false;
	}
	
	private IThread selectThread(List<IThread> threads) throws DebugException {
		
		Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
		
		List<IThread> threadsWithTrace = new ArrayList<IThread>();
		for(IThread thread : threads) {
			if(!thread.hasStackFrames()) continue;
			threadsWithTrace.add(thread);
		}
		if(threadsWithTrace.size()==0) {
			return null;
		}
		
		LabelProvider labelProvider = new LabelProvider() {
			@Override
			public String getText(Object element) {
				try {
					return ((IThread)element).getName();
				} catch(Exception e) {
					e.printStackTrace();
				}
				return super.getText(element);
			}
		};
		ListSelectionDialog threadSelectDialog = new ListSelectionDialog(
				shell, 
				threadsWithTrace, 
				new ArrayContentProvider(), 
				labelProvider, 
				"Choose Threads:");
		threadSelectDialog.setTitle("Which thread do you want to open in a diagram?");
		if(threadSelectDialog.open()==Window.OK) {
			Object[] selections = threadSelectDialog.getResult();
			if(selections==null || selections.length==0) return null;
			
			return (IThread) selections[0];
		}
		return null;
	}

	private void highlightMethodsInCurrentStacktrace() {
		for(IJavaStackFrame frame : framesInCurrentDiagram.keySet()) {
			MethodBoxModel method = framesInCurrentDiagram.get(frame);
			if(method==null || method.getFigure()==null) continue;
			method.getFigure().addDebugHighlight();
		}
	}

	// Listen for selections to methods in the diagram, and when a selection
	// happens, open in the Variables view the frame that corresponds to that method
	private void linkDiagramToVariablesView() {

		// Get the provider that can be used to tell
		// the Variables view what information to show
		AbstractDebugContextProvider provider = null;
		IWorkbenchWindow activeWorkbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		IViewReference[] viewRefs = activeWorkbenchWindow .getActivePage().getViewReferences();
		for(IViewReference viewRef : viewRefs) {
			if(viewRef instanceof LaunchView) {
				LaunchView debugView = (LaunchView) viewRef;
				provider = lookupContextProvider(debugView);
				break;
			}
		}
		if(provider==null) {
			logger.warn("Unable to link diagram with variables view");
			return;
		}
		final AbstractDebugContextProvider contextProvider = provider;

		for(final IJavaStackFrame frame : framesInCurrentDiagram.keySet()) {
			MethodBoxModel method = framesInCurrentDiagram.get(frame);
			if(method==null || method.getFigure()==null) continue;
			method.getFigure().addMouseListener(new MouseListener() {
				public void mousePressed(MouseEvent me) {

					// Make an IStructured selection for the frame
					// that corresponds to the selected method
					StructuredSelection selectedFrame = new StructuredSelection(frame);

					// Fire an event that will notify the Variables view that a method 
					// selection has been made and the contained variables should be shown
					DebugContextEvent event = new DebugContextEvent(contextProvider, selectedFrame, DebugContextEvent.ACTIVATED);
					try {
						Method[] methods = AbstractDebugContextProvider.class.getDeclaredMethods();
						for (Method m : methods) {
							if (!"fire".equals(m.getName())) continue;

							m.setAccessible(true);
							m.invoke(contextProvider, new Object[]{event});
							break;
						}
					} catch (Exception e) {
						logger.error("Issue stemming from attempted invocation of " +
								"method fire(DebugContextEvent) in AbstractDebugContextProvider: ", e);
					} 
				}
				public void mouseDoubleClicked(MouseEvent me) {}
				public void mouseReleased(MouseEvent me) {}
			});
		}
	}

	private static AbstractDebugContextProvider lookupContextProvider(LaunchView debugView) {
		Field[] fields = LaunchView.class.getDeclaredFields();
		for (Field f : fields) {
			if (!"fProvider".equals(f.getName())) continue;
			f.setAccessible(true);
			try {
				Object retVal = f.get(debugView);
				return AbstractDebugContextProvider.class.cast(retVal);
			} catch (Exception e) {}
		}
		return null;
	}

}