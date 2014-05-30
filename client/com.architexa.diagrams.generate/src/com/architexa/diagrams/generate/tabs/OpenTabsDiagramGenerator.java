package com.architexa.diagrams.generate.tabs;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.internal.core.SourceType;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowPulldownDelegate;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.CompoundContributionItem;
import org.eclipse.ui.internal.WorkbenchPage;
import org.eclipse.ui.part.FileEditorInput;
import org.openrdf.model.Resource;

import com.architexa.diagrams.chrono.util.InstanceUtil;
import com.architexa.diagrams.chrono.util.MethodDeclarationFinder;
import com.architexa.diagrams.chrono.util.MethodUtil;
import com.architexa.diagrams.chrono.util.SeqUtil;
import com.architexa.diagrams.generate.GenerateUtil;
import com.architexa.diagrams.jdt.IJEUtils;
import com.architexa.diagrams.jdt.Invocation;
import com.architexa.diagrams.jdt.RJCore;
import com.architexa.diagrams.jdt.compat.ASTUtil;
import com.architexa.diagrams.jdt.model.CodeUnit;
import com.architexa.diagrams.model.ArtifactFragment;
import com.architexa.diagrams.model.ArtifactRel;
import com.architexa.diagrams.ui.PluggableDiagramsSupport;
import com.architexa.diagrams.ui.PluggableDiagramsSupport.IRSEDiagramEngine;
import com.architexa.rse.BuildStatus;
import com.architexa.store.StoreUtil;


/**
 * 
 * @author Elizabeth L. Murnane
 *
 */
public class OpenTabsDiagramGenerator extends CompoundContributionItem implements IWorkbenchWindowPulldownDelegate {
	public static String tabGenSubMenuId = "seq.tabGenSubMenu";

	public static int numNavigationsToInclude = 10;

	private static List<IType> openTabs = new ArrayList<IType>();

	@Override
	public void dispose() {}
	public void selectionChanged(IAction action, ISelection selection) {}
	public void run(IAction action) {}

	public void initialize() {
		final IWorkbench workbench = PlatformUI.getWorkbench();
		workbench.getDisplay().asyncExec(new Runnable() {
			public void run() {
				init(workbench.getActiveWorkbenchWindow());
			}
		});
	}

	// This is no longer needed since we are adding items to the menu in the plugin.xml.
	// But we need to test with 3.2 and make sure this isnt necessary for backwards compatibility 
	public void init(IWorkbenchWindow window) {
		
//		MenuManager stdEditorMenuManager = UIUtil.getStdEditorMenuManager(window);
//		if(stdEditorMenuManager==null) return;
//		if(UIUtil.menuAlreadyContainsContribution(stdEditorMenuManager, tabGenSubMenuId)) return;
//
//		stdEditorMenuManager.add(new Separator());
//
//		ImageDescriptor rseIcon = GeneratePlugin.getImageDescriptor("icons/rse-document.png");
//		RSEMenuAction tabsGenSubMenu = new RSEMenuAction(tabGenSubMenuId, "Open All Java Editor Tabs As ", rseIcon) {
//			@Override
//			protected List<IAction> getMenuActions() {
//				List<IAction> actions = new ArrayList<IAction>();
//				Set<IRSEDiagramEngine> diagEngines = PluggableDiagramsSupport.getRegisteredDiagramEngines();
//				for (final IRSEDiagramEngine diagramEngine : diagEngines) {
//					SystemMenuGenerateNavHistory action = new SystemMenuGenerateNavHistory(diagramEngine);
//					actions.add(action);
//				}
//				return actions;
//			}
//		};
//		tabsGenSubMenu.setMenuCreator(tabsGenSubMenu);
//
//		stdEditorMenuManager.add(tabsGenSubMenu);
	}

	public static void generateDiagram(final IRSEDiagramEngine diagramEngine) {
		getOpenTabs(PlatformUI.getWorkbench().getActiveWorkbenchWindow());

		try{
			final IRunnableWithProgress op=new IRunnableWithProgress(){

				public void run(IProgressMonitor monitor)
				throws InvocationTargetException, InterruptedException {
					monitor.beginTask("Generating diagram based on open java editors...", IProgressMonitor.UNKNOWN);
					Display.getDefault().asyncExec(new Runnable(){
						public void run() {
							generate(diagramEngine);
						}
					});
				}

			};
			new ProgressMonitorDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell()).run(true, false, op);
		}catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public static boolean tabsAreOpen() {
		return openTabs.size()!=0;
	}

	private static void getOpenTabs(IWorkbenchWindow window) {
		openTabs.clear();
		WorkbenchPage activePage = (WorkbenchPage) window.getActivePage();
		IEditorReference[] editorRefs = ((WorkbenchPage)activePage).getSortedEditors();

		// WorkbenchPage.getSortedEditors() returns the editors in activation order (oldest
		// first). We want most recently activated to be first, so reverse the list.
		List<IEditorReference> editorRefsAsList = Arrays.asList(editorRefs);
		Collections.reverse(editorRefsAsList);

		try {
			for(IEditorReference ref : editorRefsAsList) {
				if(ref.getEditorInput() instanceof FileEditorInput) {
					IFile file = ((FileEditorInput)ref.getEditorInput()).getFile();
					IJavaElement editorElt = JavaCore.create(file);
					if(editorElt instanceof ICompilationUnit) {
						openTabs.addAll(Arrays.asList(((ICompilationUnit)editorElt).getAllTypes()));
					}
				}
			}
		} catch (PartInitException e) {
			System.out.print("Exception while getting all open tabs. " + e.getMessage());
			e.printStackTrace();
		} catch (JavaModelException e) {
			System.out.print("Exception while getting all open tabs. " + e.getMessage());
			e.printStackTrace();
		}
	}

	private static void generate(IRSEDiagramEngine diagramEngine) {
		if(diagramEngine==null) return;

		LinkedHashMap<IType, List<Invocation>> history = SeqUtil.getNavigationHistory(numNavigationsToInclude);
		List<CodeUnit> classUnits = new ArrayList<CodeUnit>();
		List<CodeUnit> methodUnits = new ArrayList<CodeUnit>();
		List<ArtifactFragment> toAddToDiagram = new ArrayList<ArtifactFragment>();

		for(IType type : history.keySet()) {
			if(!openTabs.contains(type)) continue;

			Resource classOfNavigationRes = RJCore.jdtElementToResource(StoreUtil.getDefaultStoreRepository(), type);
			CodeUnit classOfNavigationCU = GenerateUtil.getCodeUnitForRes(classOfNavigationRes, null, classUnits, null);

			// Add method calls
			for(Invocation invocation : history.get(type)) {

				IType container = null;
				if(invocation.getMethodElement()!=null) {
					IMethod method = invocation.getMethodElement();
					container = method.getDeclaringType();
				}
				if(container==null) continue;

				boolean containerIsAnOpenTab = false;
				for(IType navigatedType : openTabs) {
					if(navigatedType.equals(container)) {
						containerIsAnOpenTab = true;
						break;
					}
				}
				if(!containerIsAnOpenTab) continue;

				// Find the method declaration in which the invocation is made
				CompilationUnit cu = ASTUtil.getAst(IJEUtils.ije_getTypeRoot(type));
				MethodDeclarationFinder finder = new MethodDeclarationFinder(cu);
				MethodDeclaration declaration = finder.findDeclaration(invocation.getStartPosition());
				if(declaration==null || declaration.resolveBinding()==null || declaration.resolveBinding().getJavaElement()==null) continue;

				Resource declarationRes = RJCore.jdtElementToResource(StoreUtil.getDefaultStoreRepository(), declaration.resolveBinding().getJavaElement());
				CodeUnit declarationThatMakesInvocation = GenerateUtil.getCodeUnitForRes(declarationRes, null, methodUnits, classOfNavigationCU);

				String instanceName = (invocation.getInvocation() instanceof SuperMethodInvocation) ? null : MethodUtil.getInstanceCalledOn(invocation);
				CodeUnit containerOfDeclOfInvokedMethod = GenerateUtil.getCodeUnitForRes(RJCore.jdtElementToResource(StoreUtil.getDefaultStoreRepository(), container), instanceName, classUnits, null);
				Resource declOfInvokedMethodRes = RJCore.jdtElementToResource(StoreUtil.getDefaultStoreRepository(), invocation.getMethodElement());
				CodeUnit declOfInvokedMethodCU = GenerateUtil.getCodeUnitForRes(declOfInvokedMethodRes, null, methodUnits, containerOfDeclOfInvokedMethod);

				ArtifactRel rel = new ArtifactRel(declarationThatMakesInvocation, declOfInvokedMethodCU, RJCore.calls);

				declarationThatMakesInvocation.addSourceConnection(rel);
				declOfInvokedMethodCU.addTargetConnection(rel);
			}

			// Add inheritance relationships among classes
			for(Resource superClassRes : InstanceUtil.getSuperClasses(classOfNavigationRes)) {
				CodeUnit superClassCU = GenerateUtil.getCodeUnitForRes(superClassRes, null, classUnits, null);

				IJavaElement superClassElt = RJCore.resourceToJDTElement(StoreUtil.getDefaultStoreRepository(), superClassRes);
				if(!history.keySet().contains(superClassElt)) continue; // superclass has not been recently navigated
				if(!openTabs.contains(superClassElt)) continue; // superclass is not an open tab

				ArtifactRel inheritanceRel = new ArtifactRel(classOfNavigationCU, superClassCU, RJCore.inherits);
				classOfNavigationCU.addSourceConnection(inheritanceRel);
				superClassCU.addTargetConnection(inheritanceRel);
			}
		}

		for(CodeUnit classUnit : classUnits) {
			if(classUnit.getShownChildren().size()>0) toAddToDiagram.add(classUnit);
		}

		int numBeingAdded = toAddToDiagram.size();
		if(numBeingAdded<numNavigationsToInclude) { 
			// Diagram going to be small, so add a reasonable number 
			// of the open tabs that were most recently activated
			// (openTabs list is ordered with most recently activated first) 
			for(int i=0; i<openTabs.size()&&numBeingAdded<numNavigationsToInclude; i++) {
				IType type = openTabs.get(i);

				// only adding top level tabs here, not any nested classes
				if (!(type instanceof SourceType) 
						|| ((SourceType)type).getParent().getElementType()== IJavaElement.TYPE) 
					continue;

				Resource classOfNavigationRes = RJCore.jdtElementToResource(StoreUtil.getDefaultStoreRepository(), type);
				CodeUnit classOfNavigationCU = GenerateUtil.getCodeUnitForRes(classOfNavigationRes, null, classUnits, null);
				if(!toAddToDiagram.contains(classOfNavigationCU)) {
					toAddToDiagram.add(classOfNavigationCU);
					numBeingAdded++;
				}
			}
		} 
		diagramEngine.openDiagramFromNavigatedTabs(toAddToDiagram, new ArrayList<ArtifactFragment>(classUnits));
	}

	public Menu getMenu(Control parent) {
		Menu menu = new Menu(parent);
		for (ActionContributionItem action : getActions()) {
			action.fill(menu, -1);
		}
		return menu;
	}

	@Override
	protected IContributionItem[] getContributionItems() {
		
		int i = 0;
		ArrayList<ActionContributionItem> actions = new ArrayList<ActionContributionItem >(getActions());
		IContributionItem[] list = new IContributionItem[actions.size()];
		for (ActionContributionItem obj : actions) {
			IContributionItem item = ((IContributionItem) obj);
			if (item == null) continue;
			list[i] = item;
			i++;
		}
		return list;
	}
	
	private List<ActionContributionItem> getActions() {
		List<ActionContributionItem> actions = new ArrayList<ActionContributionItem>();
		List<IRSEDiagramEngine> diagEngines = new ArrayList<IRSEDiagramEngine> (PluggableDiagramsSupport.getRegisteredDiagramEngines());
		for (final IRSEDiagramEngine diagramEngine : diagEngines) {
			IAction action = new Action(diagramEngine.diagramType(), diagramEngine.getImageDescriptor()) {
				@Override
				public void run() {
					BuildStatus.addUsage(diagramEngine.diagramUsageName());

					try {
						generateDiagram(diagramEngine); 
					} catch(Exception e) {
						System.out.println("Exception while generating diagram based on navigation history. " + e.getMessage());
						e.printStackTrace();
					}
				}
			};
			ActionContributionItem newActionItem = new ActionContributionItem(action);
			actions.add(newActionItem);
		}
		return actions;
	}

}
