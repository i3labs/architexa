/**
 * 
 */
package com.architexa.diagrams.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.internal.registry.IWorkbenchRegistryConstants;
import org.eclipse.ui.views.properties.ComboBoxLabelProvider;
import org.eclipse.ui.views.properties.ComboBoxPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertyDescriptor;

import com.architexa.diagrams.Activator;
import com.architexa.diagrams.ColorScheme;
import com.architexa.diagrams.commands.HideRelCommand;
import com.architexa.diagrams.eclipse.gef.ContextMenuProvider2;
import com.architexa.diagrams.eclipse.gef.MenuButton;
import com.architexa.diagrams.model.ArtifactFragment;
import com.architexa.diagrams.model.ArtifactRel;
import com.architexa.diagrams.model.RootArtifact;
import com.architexa.diagrams.parts.AbstractRelationPart;
import com.architexa.diagrams.parts.RSEEditPart;
import com.architexa.diagrams.utils.RootEditPartUtils;
import com.architexa.intro.AtxaIntroPlugin;
import com.architexa.intro.preferences.PreferenceConstants;
import com.architexa.org.eclipse.draw2d.ColorConstants;
import com.architexa.org.eclipse.draw2d.Label;
import com.architexa.org.eclipse.gef.EditPart;
import com.architexa.org.eclipse.gef.EditPartViewer;
import com.architexa.org.eclipse.gef.RootEditPart;
import com.architexa.org.eclipse.gef.commands.Command;
import com.architexa.org.eclipse.gef.editparts.AbstractEditPart;
import com.architexa.org.eclipse.gef.extensions.ServerExtensionContextMenuProvider;
import com.architexa.org.eclipse.gef.ui.actions.ActionRegistry;
import com.architexa.org.eclipse.gef.ui.actions.GEFActionConstants;

/**
 * Adds all actions in the registry to the context menu, and also checks if the
 * root edit part has any property sources - if it does then it shows them in
 * the context menu as well.<br>
 * <br>
 * This class needs to become RSEContextMenuProvider - strata does not seem to
 * need any other customizations.
 * 
 * @author vineet
 */
public class RSEContextMenuProvider extends ContextMenuProvider2 implements ServerExtensionContextMenuProvider {

	static final Logger logger = Activator.getLogger(RSEContextMenuProvider.class);

	private final ActionRegistry registry;

	public RSEContextMenuProvider(EditPartViewer viewer, ActionRegistry registry) {
		super(viewer);
		this.registry = registry;
	}

	/**
	 * Returns a nav aid button that when pushed opens up a menu 
	 * containing the same items found in the context menu. This will make
	 * it easier for a user to realize and find the capabilities available
	 * to him and the actions that can be performed on a diagram item.
	 */
	public static MenuButton getContextMenuNavAid(final EditPartViewer viewer) {
		MenuButton button = new MenuButton(new Label("..."), viewer) {
			@Override
			public void buildMenu(IMenuManager menu) {

				// Add all the items that are added to the
				// context menu by the RSEContextMenuProvider
				RSEContextMenuProvider menuProvider = 
					(RSEContextMenuProvider) viewer.getContextMenu();
				menuProvider.buildContextMenu(menu);

				// The Open in Diagram menu is added to the context menu via
				// a plugin contribution, so handle adding it explicitly since 
				// menuProvider.buildContextMenu(menu) will not add it.
				addOpenInDiagramPluginContribution(menu);
			}
		};
		button.setBackgroundColor(ColorConstants.listBackground);
		button.setToolTip(new Label(" More Actions "));
		return button;
	}

	/** * Context menu group for diagram manipulation actions. */
	public static final String GROUP_RSE_TOOLS = "rseToolsGroup";

	/** * Context menu group for actions related to editing diagram component appearance. */
	public static final String GROUP_RSE_EDIT_APPEARANCE = "rseEditAppearanceGroup";
	
	/** * Context menu group for actions related to setting diagram properties. */
	public static final String GROUP_RSE_SETTINGS = "rseSettingsGroup";

	/** * Context menu group for editor switching actions. */
	// NOTE: The value of GROUP_RSE_EDITORS must match the menubarPath
	// value for "com.architexa.diagrams.OpenInDiagramMenuAction"
	// in com.architexa.diagrams/plugin.xml
	public static final String GROUP_RSE_EDITORS = "group.open";

	// Detail Level
	private static final String CONTEXT_PROP = "DetailLevelSetting";
	private static final String[] cntxStateLabels = new String[] {
		"High {fully qualified return types}", 
		"Medium {abbreviated return types}",
		"Low {hide parameters and return types}"};
	private static ComboBoxPropertyDescriptor descriptor = new ComboBoxPropertyDescriptor(CONTEXT_PROP, "Detail Level", cntxStateLabels);
	
	// Themes
	private static final String THEME_PROP = "ThemeSetting";
	private static final String[] themeLabels = new String[] {
		"Color", 
		"Black and White"};
	private static ComboBoxPropertyDescriptor themeDescriptor = new ComboBoxPropertyDescriptor(THEME_PROP, "Display Theme", themeLabels);

	
	@Override
	public void buildContextMenu(IMenuManager menu) {
		
		// add the standard group separators to the menu, create our custom menu
		// groups and separators, and add the standard actions (undo, print, etc) 
		createMenuGroupsAndAddStandardActions(menu);
		
		// standard actions have been added to the menu, so remaining registry actions 
		// should be for editing the diagram and therefore added to the tools group
		addToolActions(menu);

		// add items for setting properties
		addPropertySettingActions(menu);
		
		// perform any misc modifications to the menu entries depending on selection
		performMods(menu);
	}

	public Map<String, String> getMenuMapToSendToServer(IMenuManager menu) {
		buildContextMenu(menu);
		IContributionItem[] items = menu.getItems();
		Map<String, String> itemsMap = new LinkedHashMap<String, String>();
		for (IContributionItem item : items) {
			itemsMap.put(item.getId(), item.getId());
		}
		return itemsMap;
	}
	
	private void createMenuGroupsAndAddStandardActions(IMenuManager menu) {

		GEFActionConstants.addStandardActionGroups(menu);

		// put Undo, Redo at top of menu
		menu.appendToGroup(GEFActionConstants.GROUP_UNDO, registry.getAction(ActionFactory.UNDO.getId()));
		menu.appendToGroup(GEFActionConstants.GROUP_UNDO, registry.getAction(ActionFactory.REDO.getId()));

		// then RSE actions that manipulate the parts in the diagram (for 
		// example, "Break" in Strata or "Show Referencing Types" in Relo)
		menu.appendToGroup(GEFActionConstants.GROUP_EDIT, new Separator(GROUP_RSE_TOOLS));

		// then RSE actions that manipulate the appearance of components
		// in the diagram, for example options to Highlight
		menu.appendToGroup(GROUP_RSE_TOOLS, new Separator(GROUP_RSE_EDIT_APPEARANCE));

		// then Delete, Select All, Zoom In, Zoom Out
		menu.appendToGroup(GEFActionConstants.GROUP_VIEW, registry.getAction(ActionFactory.DELETE.getId()));
		menu.appendToGroup(GEFActionConstants.GROUP_VIEW, registry.getAction(ActionFactory.SELECT_ALL.getId()));
		menu.appendToGroup(GEFActionConstants.GROUP_VIEW, registry.getAction(GEFActionConstants.ZOOM_IN));
		menu.appendToGroup(GEFActionConstants.GROUP_VIEW, registry.getAction(GEFActionConstants.ZOOM_OUT));

		// then a separator and Save, Print 
		menu.appendToGroup(GEFActionConstants.GROUP_VIEW, new Separator());
		menu.appendToGroup(GEFActionConstants.GROUP_VIEW, registry.getAction(ActionFactory.SAVE.getId()));
		menu.appendToGroup(GEFActionConstants.GROUP_VIEW, registry.getAction(ActionFactory.PRINT.getId()));

		// then items for setting "properties", like whether to 
		// show context in diagrams, go near the end of the menu
		menu.appendToGroup(GEFActionConstants.GROUP_VIEW, new Separator(GROUP_RSE_SETTINGS));

		// finally put items for view switching, like switching to a java 
		// editor or opening in a different diagram, at the end of the menu
		// note: adding it to GROUP_REST because when use GROUP_VIEW, it's going
		// above the properties section for some reason, but we want it below
		menu.appendToGroup(GEFActionConstants.GROUP_REST, new Separator(GROUP_RSE_EDITORS));
	}

	private void addToolActions(IMenuManager menu) {
		Iterator<?> actionsIt = registry.getActions();
		List<IAction> actionsList = new ArrayList<IAction>(10);
		while (actionsIt.hasNext()) {
			IAction registryAction = (IAction) actionsIt.next();
			if (!(registryAction instanceof RSENestedAction) && menu.find(registryAction.getId()) == null)
				actionsList.add(registryAction);
		}
		Collections.sort(actionsList, new Comparator<IAction> () {
			public int compare(IAction a1, IAction a2) {
				if (a1 == null || a2 == null) return 0;
				return a1.getText().compareTo(a2.getText());
			}});
		for (IAction act : actionsList) {
			String group = (act instanceof RSEContextMenuEntry) ? 
					((RSEContextMenuEntry)act).getContextMenuGroup() : GROUP_RSE_TOOLS;
			menu.appendToGroup(group, act);
		}
	}

	private void addPropertySettingActions(IMenuManager menu) {

		// add preference for showing context to the properties group
		addContextPref(menu);

		addThemePref(menu);
		
		EditPart editPart = getViewer().getRootEditPart().getContents();
		if (editPart instanceof RSEEditPart)
			// settings for when to show strata's arrows, so belongs in properties group
			addPropertyEntries(menu, (RSEEditPart)editPart);
	}

	protected void addContextPref(IMenuManager menu) {
		MenuManager childMenu = new MenuManager(descriptor.getDisplayName());
		ComboBoxLabelProvider cblp = (ComboBoxLabelProvider) descriptor.getLabelProvider();
		String[] cbVals = cblp.getValues();
		for (int i = 0; i < cbVals.length; i++) {
			final Integer cbNdx = i;
			IWorkbenchPart editor = RootEditPartUtils.getEditorFromRSEMultiPageEditor(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor());
			if (!(editor instanceof RSEEditor)) return;
			RootEditPart viewEditorCommon = ((RSEEditor)editor).getRootEditPart();
			if (!(viewEditorCommon instanceof AbstractEditPart)) return;
			Object root = viewEditorCommon.getChildren().get(0);
			if (!(root instanceof AbstractEditPart)) return;
			final Object model = ((AbstractEditPart) root).getModel();
			IAction act = new Action(cbVals[i], Action.AS_RADIO_BUTTON){
				@Override
				public void run() {
					//when detail level preference is changed we want to change the default level preference and update the 'current' diagram
					getPreferenceStore().setValue(PreferenceConstants.LabelDetailLevelKey, cbNdx);
					int detailLvl = cbNdx;
					((RootArtifact)model).setDetailLevel(detailLvl);
				}
			};
			if (cbNdx == 0 && ((RootArtifact)model).getDetailLevel()==0)//getPreferenceStore().getInt(PreferenceConstants.LabelDetailLevelKey) == 0)
				act.setChecked(true);
			if (cbNdx == 1 && ((RootArtifact)model).getDetailLevel()==1)// getPreferenceStore().getInt(PreferenceConstants.LabelDetailLevelKey) == 1)
				act.setChecked(true);
			if (cbNdx == 2 &&  ((RootArtifact)model).getDetailLevel()==2)//getPreferenceStore().getInt(PreferenceConstants.LabelDetailLevelKey) == 2)
				act.setChecked(true);
			childMenu.add(act);
		}
		menu.appendToGroup(GROUP_RSE_EDIT_APPEARANCE, childMenu);
	}
	
	
	protected void addThemePref(IMenuManager menu) {
		MenuManager childMenu = new MenuManager(themeDescriptor.getDisplayName());
		ComboBoxLabelProvider cblp = (ComboBoxLabelProvider) themeDescriptor.getLabelProvider();
		String[] cbVals = cblp.getValues();
		for (int i = 0; i < cbVals.length; i++) {
			final Integer cbNdx = i;
			IWorkbenchPart editor = RootEditPartUtils.getEditorFromRSEMultiPageEditor(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor());
			if (!(editor instanceof RSEEditor)) return;
			RootEditPart viewEditorCommon = ((RSEEditor)editor).getRootEditPart();
			if (!(viewEditorCommon instanceof AbstractEditPart)) return;
			Object root = viewEditorCommon.getChildren().get(0);
			if (!(root instanceof AbstractEditPart)) return;
			final Object model = ((AbstractEditPart) root).getModel();
			IAction act = new Action(cbVals[i], Action.AS_RADIO_BUTTON){
				@Override
				public void run() {
					Command c = new Command("Change Color Theme") {
						@Override
						public void execute() {
							ColorScheme.setTheme(cbNdx);
							if (model instanceof ArtifactFragment)
								((RootArtifact)model).setColorTheme(cbNdx);
							super.execute();
						}
					};
					c.execute();
				}
			};
			if (cbNdx == 0  && ((RootArtifact)model).getColorTheme()==0)
				act.setChecked(true);
			if (cbNdx == 1  && ((RootArtifact)model).getColorTheme()==1)
				act.setChecked(true);
			childMenu.add(act);
		}
		menu.appendToGroup(GROUP_RSE_EDIT_APPEARANCE, childMenu);
	}

	private void addPropertyEntries(IMenuManager menu, RSEEditPart ep) {
		IPropertyDescriptor[] descriptors = ep.getProperties();
		for (IPropertyDescriptor propertyDescriptor : descriptors) {
			// can't handle other types of descriptors (for now)
			if (!(propertyDescriptor instanceof ComboBoxPropertyDescriptor)) continue;

			addEntryFromPropertyDescriptor(menu, ep, (ComboBoxPropertyDescriptor) propertyDescriptor);
		}
	}

	private void addEntryFromPropertyDescriptor(IMenuManager menu, 
			final RSEEditPart ep, final ComboBoxPropertyDescriptor pd) {
		MenuManager childMenu = new MenuManager(pd.getDisplayName());
		menu.appendToGroup(GROUP_RSE_SETTINGS, childMenu);

		ComboBoxLabelProvider cblp = (ComboBoxLabelProvider) pd.getLabelProvider();
		String[] cbVals = cblp.getValues();
		
		for (int i = 0; i < cbVals.length; i++) {
			final Integer cbNdx = i;
			IAction act = new Action(cbVals[i], Action.AS_RADIO_BUTTON){
				@Override
				public void run() {
					ep.setPropertyValue(pd.getId(), cbNdx);
				}
			};
			if (ep.getPropertyValue(pd.getId()) == cbNdx)
				act.setChecked(true);
			childMenu.add(act);
		}
	}

	private void performMods(IMenuManager menu) {
		List<?> sel = getViewer().getSelectedEditParts();
		if (sel.isEmpty()) return;
		EditPart ep = (EditPart)sel.get(0);
		if (ep!=null && ep.getModel() instanceof ArtifactRel 
				&& ep instanceof AbstractRelationPart) {
			handleRelSelection((AbstractRelationPart)ep, menu);
		}
	}

	private void handleRelSelection(final AbstractRelationPart relEP, IMenuManager menu) {
		// replace the Delete action when selection is a connection
		IAction action = new Action("Delete Relationship") {
			@Override
			public void run() {
				try {
					relEP.getViewer().getEditDomain().getCommandStack().execute(new HideRelCommand((ArtifactRel) relEP.getModel()));
				} catch (Exception e) {
					Logger logger = Activator.getLogger(RSEContextMenuProvider.class);
					logger.error("Unexpected exception", e);
				}
			}
		};
		action.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_TOOL_DELETE));
		menu.insertAfter(ActionFactory.DELETE.getId(), action);
		menu.remove(ActionFactory.DELETE.getId());
	}

	private static void addOpenInDiagramPluginContribution(IMenuManager menu) {
		IExtensionRegistry extRegistry = Platform.getExtensionRegistry(); 

		// Find items added to the context menu via plugin contributions
		IConfigurationElement[] popupMenuExtElmts = 
			extRegistry.getConfigurationElementsFor(IWorkbenchRegistryConstants.EXTENSION_POPUP_MENUS);

		// Find <objectContribution> with the Open in Diagram contribution's id 
		for(IConfigurationElement element : popupMenuExtElmts) {
			String elmtName = element.getName();
			String elmtId = element.getAttribute(IWorkbenchRegistryConstants.ATT_ID);
			if(!(IWorkbenchRegistryConstants.TAG_OBJECT_CONTRIBUTION.equals(elmtName)
					&& "com.architexa.diagrams.DiagramObjectContrib".equals(elmtId)))
				continue;

			// Find the OpenInDiagramMenuAction
			IConfigurationElement[] actions = 
				element.getChildren(IWorkbenchRegistryConstants.TAG_ACTION);
			for(IConfigurationElement action : actions) {
				String actionId = action.getAttribute(IWorkbenchRegistryConstants.ATT_ID);
				String actionClass = action.getAttribute(IWorkbenchRegistryConstants.ATT_CLASS);
				if(!"com.architexa.diagrams.OpenInDiagramMenuAction".equals(actionId)
						|| actionClass==null) continue;
				try {
					Object o = action.createExecutableExtension(IWorkbenchRegistryConstants.ATT_CLASS);
					if(!(o instanceof RSEMenuAction)) continue;
					RSEMenuAction openInDiagramAction = (RSEMenuAction) o;

					// Set the action's id, text, and icon 
					// based on its plugin extension info
					String actionText = action.getAttribute(IWorkbenchRegistryConstants.ATT_LABEL);
					String actionIconPath = action.getAttribute(IWorkbenchRegistryConstants.ATT_ICON);
					openInDiagramAction.setId(actionId);
					openInDiagramAction.setText(actionText);
					openInDiagramAction.setImageDescriptor(Activator.getImageDescriptor(actionIconPath));

					// Add the submenu items
					openInDiagramAction.setMenuCreator(openInDiagramAction);

					// Add to the group of editor switching actions. Prepend so it's above
					// the "Open in Java Editor" and matches the order in the context menu
					menu.prependToGroup(GROUP_RSE_EDITORS, openInDiagramAction);

				} catch (CoreException e) {
					logger.error("Unexpected exception while trying to " +
							"create Open in Diagram submenu ", e);
				}
			}
		}
	}

	private static IPreferenceStore getPreferenceStore() {
		return AtxaIntroPlugin.getDefault().getPreferenceStore();
	}
}