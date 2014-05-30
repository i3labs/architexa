package com.architexa.diagrams.jdt.ui;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IExecutionListener;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.commands.NotHandledException;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.internal.ui.JavaUIMessages;
import org.eclipse.jface.bindings.Binding;
import org.eclipse.jface.bindings.TriggerSequence;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.SelectionDialog;

import com.architexa.collab.proxy.PluginUtils;
import com.architexa.diagrams.jdt.Activator;
import com.architexa.diagrams.ui.RSEEditor;
import com.architexa.diagrams.utils.RootEditPartUtils;
import com.architexa.org.eclipse.draw2d.geometry.Point;
import com.architexa.org.eclipse.gef.requests.CreateRequest;
import com.architexa.org.eclipse.gef.requests.CreationFactory;

/**
 * If an Architexa diagram is the active editor when the user opens the
 * Open Type dialog, any type(s) selected are added to that diagram 
 * rather than a Java editor (which is the behavior when the active
 * editor is not a diagram).
 *
 */
public class RSEOpenTypeHandler extends RSEBindingServiceHandler {
	static final Logger logger = Activator.getLogger(RSEOpenTypeHandler.class);

	/**
	 * Finds the Command that opens the Open Type dialog and replaces its handler
	 * responsible for execution behavior with this custom handler if the currently 
	 * active editor is an Architexa diagram.
	 */
	@Override
	public void initialize() {

		// Get the Command that opens the Open Type dialog and its normal handler
//		IBindingService bindingService = (IBindingService) PlatformUI.getWorkbench().getService(IBindingService.class);
		super.initialize();
		TriggerSequence[] triggerArray = getBindingService().getActiveBindingsFor("org.eclipse.jdt.ui.navigate.open.type");
		if(triggerArray.length==0) return;

		TriggerSequence openTypeTrigger = triggerArray[0];
		Binding openTypeBinding = getBindingService().getPerfectMatch(openTypeTrigger);
		if(openTypeBinding==null ||
				openTypeBinding.getParameterizedCommand()==null) return;

		final Command openTypeCmd = openTypeBinding.getParameterizedCommand().getCommand();

		// Command.getHandler() isn't visible in 3.2
		IHandler h = null;
		try {
			Method mth = Command.class.getDeclaredMethod("getHandler");
			mth.setAccessible(true);
			h = (IHandler) mth.invoke(openTypeCmd);
		} catch (Exception e) {
			logger.error("Exception stemming from attempt to " +
					"invoke method Command.getHandler() ", e);
		} 
		final IHandler normalHandler = h;
		final IHandler rseHandler = this;

		openTypeCmd.addExecutionListener(new IExecutionListener() {
			public void preExecute(String commandId, ExecutionEvent event) {
				// Command is about to execute. If a diagram is the active editor
				// and has focus, use our own handler to execute it. (Testing for
				// focus as well so that do not override default handler if rse
				// editor is active but another part, say Package Explorer, is selected).
				IEditorPart editor = getRSEEditor();
				if(editor instanceof RSEEditor && ((RSEEditor)editor).hasFocus()) {
					openTypeCmd.setHandler(rseHandler);
				}
			}
			public void notHandled(String commandId, NotHandledException exception) {}
			public void postExecuteFailure(String commandId, ExecutionException exception) {}
			public void postExecuteSuccess(String commandId, Object returnValue) {
				// Command has executed, so set its handler back to original default one
				openTypeCmd.setHandler(normalHandler);
			}
		});
	}

	public static IEditorPart getRSEEditor() {
		IEditorPart mpEditor = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
		return (IEditorPart) RootEditPartUtils.getEditorFromRSEMultiPageEditor(mpEditor);
	}

	// Do not put override here as method is present in an interface (Possible bug in eclipse 3.3)
	@Override
	public Object execute(ExecutionEvent arg0) throws ExecutionException {

		IEditorPart activeEditor = getRSEEditor();
		RSEEditor diagramEditor = (RSEEditor) activeEditor;
		if(diagramEditor.getRootController()==null) return null;

		// TODO Consider: don't include library types in list unless lib 
		// preference for this diagram is set to show lib code.
		// (Use ((RootArtifact)diagramEditor.getRootController().getModel()).isLibCodeInDiagram()
		// to get library code preference)
		Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
		SelectionDialog dialog = 
			getOpenTypeSelectionDialog(shell, true, PlatformUI.getWorkbench().getProgressService(), null, IJavaSearchConstants.TYPE);
		if(dialog==null) return null;
		dialog.setTitle("Add Type to Diagram");
		dialog.setMessage(JavaUIMessages.OpenTypeAction_dialogMessage);

		int result = dialog.open();
		if (result != IDialogConstants.OK_ID) return null;

		Object[] types = dialog.getResult();
		if(types==null || types.length==0) return null;
		final List<Object> typeList = Arrays.asList(types);

		// Open selected types in diagram
		CreateRequest request = new CreateRequest();
		request.setLocation(new Point(0, 0));
		request.setFactory(new CreationFactory() {
			public Object getNewObject() {
				// user might have selected multiple
				// types, so we return the list itself
				return typeList;
			}
			public Object getObjectType() {
				return List.class;
			}});
		com.architexa.org.eclipse.gef.commands.Command command = diagramEditor.getRootController().getCommand(request);
		if (command != null && command.canExecute())
			diagramEditor.getRootController().getViewer().getEditDomain().getCommandStack().execute(command);

		return null;
	}

	private SelectionDialog getOpenTypeSelectionDialog(Shell parent, boolean multi,
			IRunnableContext context, IJavaSearchScope scope, int elementKinds) {
		// Since 3.3, have class OpenTypeSelectionDialog
		// In 3.2, need to use OpenTypeSelectionDialog2
		try {
			Class openTypeSelectionDialog;
			double jdtUIVer = PluginUtils.getPluginVer("org.eclipse.jdt.ui");
			if(jdtUIVer >= 3.3) {
				openTypeSelectionDialog = Class.forName("org.eclipse.jdt.internal.ui.dialogs.OpenTypeSelectionDialog");
			} else {
				openTypeSelectionDialog = Class.forName("org.eclipse.jdt.internal.ui.dialogs.OpenTypeSelectionDialog2");
			}
			Class[] argsClasses = new Class[] {Shell.class, boolean.class, 
					IRunnableContext.class, IJavaSearchScope.class, int.class};
			Object[] args = new Object[] { parent, multi, context, scope, elementKinds };

			Constructor dialogConstructor = openTypeSelectionDialog.getConstructor(argsClasses);
			SelectionDialog dialog = (SelectionDialog) dialogConstructor.newInstance(args);
			return dialog;

		} catch(Exception e) {
			logger.error("Exception stemming from attempt to " +
					"construct new Open Type dialog ", e);
		}
		return null;
	}

}
