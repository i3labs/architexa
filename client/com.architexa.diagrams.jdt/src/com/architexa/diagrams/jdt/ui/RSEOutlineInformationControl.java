package com.architexa.diagrams.jdt.ui;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.eclipse.jdt.internal.ui.text.AbstractInformationControl;
import org.eclipse.jdt.ui.ISharedImages;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.dialogs.PopupDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.TreeEvent;
import org.eclipse.swt.events.TreeListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.internal.SWTEventListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.swt.widgets.TypedListener;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.PlatformUI;
import org.openrdf.model.Resource;

import com.architexa.collab.proxy.PluginUtils;
import com.architexa.compat.CompatUtils;
import com.architexa.diagrams.RSECore;
import com.architexa.diagrams.commands.AddNodeAndRelCmd;
import com.architexa.diagrams.commands.AddNodeCommand;
import com.architexa.diagrams.commands.AddResCommand;
import com.architexa.diagrams.jdt.Activator;
import com.architexa.diagrams.jdt.RJCore;
import com.architexa.diagrams.jdt.model.CodeUnit;
import com.architexa.diagrams.model.Artifact;
import com.architexa.diagrams.model.ArtifactFragment;
import com.architexa.diagrams.ui.FontCache;
import com.architexa.diagrams.ui.ImageCache;
import com.architexa.diagrams.ui.MultiAddCommandAction;
import com.architexa.intro.AtxaIntroPlugin;
import com.architexa.intro.preferences.PreferenceConstants;
import com.architexa.org.eclipse.draw2d.ColorConstants;
import com.architexa.org.eclipse.gef.commands.CompoundCommand;
import com.architexa.store.ReloRdfRepository;

/**
 * 
 * @author Elizabeth L. Murnane
 *
 */
@SuppressWarnings("restriction")
public class RSEOutlineInformationControl extends AbstractInformationControl {

	public static String FILTERS_ACCESS_LEVEL = "AccessLevelFilters";
	public static String FILTERS_MEMBER_KIND = "MemberKindFilters";
	public static String FILTERS_LIBRARY_CODE = "LibraryCodeFilter";
	public static String[] FILTERS_ALL = 
		new String[] {FILTERS_ACCESS_LEVEL, FILTERS_MEMBER_KIND, FILTERS_LIBRARY_CODE};

	public static Map<String, Boolean> filterActiveMap = new HashMap<String, Boolean>(); 
	
	static final Logger logger = Activator.getLogger(RSEOutlineInformationControl.class);

	// Holds the left column (member tree and Add All button)
	// and the right column (buttons for filtering access level)
	private Composite leftAndRightColumn;
	// Holds the member tree and the Add All button below it
	private Composite leftColumn;
	// Holds the area with buttons for filtering
	// members based on access level and member type
	private Composite rightColumn;

	private Label titleLabel;

	// A mapping of parent node -> list of member leaf nodes that 
	// clients should inject in order to include subtrees in the menu.
	// If want a non-selectable parent node that is just a label, key
	// should be a String. If want parent node that is a selectable 
	// invocation itself, key should be a MultiAddCommandAction
	private Map<Object, List<MultiAddCommandAction>> subTreeMap = 
		new HashMap<Object, List<MultiAddCommandAction>>();

	// Buttons below the tree that when pushed add items to the diagram 
	// (for example an Add All button or Add Callee Hierarchy button)
	List<Button> addButtons = new ArrayList<Button>();

	private KeyAdapter fKeyAdapter;
	
	// A list of the attributes that will still have filter buttons 
	// created when all members have the same attribute value
	private List<String> filters = new ArrayList<String>();

	// A sorted map from access level Resource -> menu's String rep for the label of that level
	// LinkedHashMap so that access level filters consistently appear in the same order
	private LinkedHashMap<Resource, String> accessLevelMap = new LinkedHashMap<Resource, String>();

	// A sorted list of possible member kinds, which will appear as filter buttons in the menu
	// LinkedList so that member kind filters consistently appear in the same order
	private LinkedList<Resource> possibleMemberKinds = new LinkedList<Resource>();

	private List<Resource> includedAccessLevels; // access levels not filtered from tree
	private List<Resource> includedMemberKinds; // kinds of members not filtered from the tree
	private List<Resource> includeLibraryCode; // will be non-empty if not filtering lib code from tree
	private List<Object> overridenSubTree;
	private static Map<MultiAddCommandAction, Artifact> actionToArtMap = new HashMap<MultiAddCommandAction, Artifact>();
	private Map<Artifact, Resource> artToResMap = new HashMap<Artifact, Resource>();
	public boolean active = false;
	private final ReloRdfRepository repo;
	
	private static String FILTERS_ACCESS_STATE = AtxaIntroPlugin.getDefault().getPreferenceStore().getString(PreferenceConstants.FILTERS_ACCESS_LEVEL);
	private static String FILTERS_TYPE_STATE = AtxaIntroPlugin.getDefault().getPreferenceStore().getString(PreferenceConstants.FILTERS_MEMBER_KIND);
	private static String FILTERS_LIBRARY_STATE = AtxaIntroPlugin.getDefault().getPreferenceStore().getString(PreferenceConstants.FILTERS_LIBRARY_CODE);
	
	
	/**
	 * 
	 * @param titleText The text that will appear above the member tree and indicate
	 * the purpose of the menu
	 * @param subTreeMap A mapping of parent node -> list of member leaf nodes that 
	 * will be included as subtrees in the menu
	 * @param filtersToAdd An array of the filters that should always be visible in the
	 * menu, even when all members have the same attribute value
	 * 
	 */
	public RSEOutlineInformationControl(
			ReloRdfRepository repo,
			String titleText, Map<Object, List<MultiAddCommandAction>> subTreeMap, 
			String[] filtersToAdd) {
		super(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), SWT.RESIZE, SWT.V_SCROLL | SWT.H_SCROLL);
		
		this.repo = repo;
		
		active= true;
		titleLabel.setText(titleText);
		// Populate the sorted map from potential access level types to the String
		// that should show for that access level label in the menu's filter column
		// (It's a sorted map so that the access levels always appear in the menu
		// in the order we put them in here: Public Protected Package Private)
		accessLevelMap.put(RJCore.publicAccess, "Public");
		accessLevelMap.put(RJCore.protectedAccess, "Protected");
		// (If no access level is specified, ie "boolean field = false;", it will 
		// be RJCore.noAccess, which Java treats as access level "package" by default):
		accessLevelMap.put(RJCore.noAccess, "Package");
		accessLevelMap.put(RJCore.privateAccess, "Private");

		// Add potential member kinds to the sorted list. (It's a sorted list
		// so that member kinds always appear in the order:
		// Interface Class Method Field
		possibleMemberKinds = new LinkedList<Resource>(Arrays.asList(
				new Resource[] {
						RJCore.interfaceType, RJCore.classType, 
						RJCore.methodType, RJCore.fieldType }));

		// List of the attribute filters that should have buttons in the menu
		filters = new ArrayList<String>(Arrays.asList(filtersToAdd));

		// Map that will be used to put tree items into desired subtrees
		if(subTreeMap!=null) this.subTreeMap = subTreeMap;

		try {
			// Don't want the persist option(s) 
			// (Remember Size / Location) in the dialog menu
			double jdtUIVer = PluginUtils.getPluginVer("org.eclipse.jdt.ui");
			String fieldName;
			if(jdtUIVer < 3.5)
				// Field is showPersistAction because there is one
				// "Remember Size and Location" action in the dialog menu
				fieldName = "showPersistAction";
			else
				// Field is showPersistActions because there are two separate
				// "Remember Location" and "Remember Size" actions in the dialog menu
				fieldName = "showPersistActions";

			// Set the field to false
			Field persistField = PopupDialog.class.getDeclaredField(fieldName);
			persistField.setAccessible(true);
			persistField.set(this, false);
		} catch (Exception e) {
			logger.error("Unexpected exception while removing " +
					"persist action(s) from dialog menu ", e);
		}
	}

	public RSEOutlineInformationControl(
			ReloRdfRepository repo,
			String titleText,
			Map<Object, List<MultiAddCommandAction>> subTreeMap2, 
			List<Object> overridenSubTree, String[] filters2) {
		this(repo, titleText, subTreeMap2, filters2);
		this.overridenSubTree = overridenSubTree;
	}

	@Override
	protected Control createContents(Composite parent) {

		// Create contents, including member tree
		Composite composite = (Composite) super.createContents(parent);

		Tree tree = getTreeViewer().getTree();
		removeTreeListeners(tree); // Remove listeners super added and will duplicate ours
		addTreeListeners(tree); // Listen for tree selections to add them to diagram

		// Make sure everything has the proper background color
		applyBackgroundColor(composite);
		if (RSEOICUtils.getSavedWidth()!=0 && RSEOICUtils.getSavedHeight()!=0)
			setSize(RSEOICUtils.getSavedWidth(), RSEOICUtils.getSavedHeight());
		return composite;
	}
	
	@Override
	public boolean close() {
		// need to add 14px or window will keep shrinking
		if (!hasContents()) return true;
		RSEOICUtils.setSavedWidthAndHeight(getContents().getBounds().width + 14, getContents().getBounds().height + 14);
		return super.close();
	}
	
	@Override
	protected Control getFocusControl() {
		return getFilterText();
	}
	@Override
	protected TreeViewer createTreeViewer(Composite parent, int style) {

		// Make 3 columns - one for the tree and Add All button column on 
		// the left and one for the area of access level and member kind
		// filter buttons on the right, with a vertical separator in between
		leftAndRightColumn = new Composite(parent, SWT.NONE);
		leftAndRightColumn.setLayout(new GridLayout(3, false));
		leftAndRightColumn.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		// Left column - Add All button will go below the member tree
		leftColumn = new Composite(leftAndRightColumn, SWT.NONE);
		leftColumn.setLayout(new GridLayout(1, false));

		titleLabel = new Label(leftColumn, SWT.LEFT);
		titleLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		titleLabel.setFont(FontCache.fontArial10Bold);
		// TODO: Implement possibility of multiple selection using check boxes here
		Tree tree= new Tree(leftColumn, SWT.SINGLE | (style & ~SWT.MULTI));
		GridData gd= new GridData(GridData.FILL_BOTH);
		gd.heightHint= tree.getItemHeight() * 12;
		// make tree wide enough to view most members' full names without scrolling right:
		gd.widthHint = 200;
		leftColumn.setLayoutData(gd);

		TreeViewer treeViewer = new TreeViewer(tree);
		treeViewer.setAutoExpandLevel(TreeViewer.ALL_LEVELS); // expand subtrees by default

		// Set the tree's content provider, which expects the
		// tree's input to be a list of MultiAddCommandAction
		ITreeContentProvider treeContentProvider = new ITreeContentProvider() {
			public Object[] getChildren(Object parentElement) {
				if(subTreeMap.containsKey(parentElement)) // parentElement is a subtree heading
					return subTreeMap.get(parentElement).toArray(); // so return its subtree members
				return null;
			}
			public Object getParent(Object element) {return null;}
			public boolean hasChildren(Object element) {
				if(subTreeMap.containsKey(element)) 
					return !subTreeMap.get(element).isEmpty(); // parentElement is a parent tree node and can have children
				return false; // element is a member leaf
			}
			public Object[] getElements(Object inputElement) {
				if(!(inputElement instanceof List)) return null;
				return ((List<?>)inputElement).toArray();
			}

			public void dispose() {
				active = false;
			}
			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {}
		};
		treeViewer.setContentProvider(treeContentProvider);

		// Likewise, set the tree's label provider, which gets a tree item's
		// text and image from the text and image of its corresponding action
		treeViewer.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element) {
				if(element instanceof String) return (String) element; // a subtree heading
				if(element instanceof MultiAddCommandAction)
					return ((MultiAddCommandAction)element).getText(); // a member
				return ""; // unknown item
			}
			@Override
			public Image getImage(Object element) {
				if (!(element instanceof MultiAddCommandAction)) return null;
				ImageDescriptor id = ((MultiAddCommandAction)element).getImageDescriptor();
				if(element instanceof MultiAddCommandAction &&
						((MultiAddCommandAction)element).getImageDescriptor()!=null) {
					
					return ImageCache.calcImageFromDescriptor(id);
				}
				return null;
			}
		});

		// When text is typed in the dialog area of the menu, the
		// tree will update to only show items matching the text
		treeViewer.addFilter(new NameFilter());
		
		// When the user selects one of the Show: Public Protected Private Package 
		// options, the tree will update to only show items with that access level
		treeViewer.addFilter(new AccessLevelFilter());
		
		// When the user selects one of the Show: Class Interface Field Method
		// options, the tree will update to only show items with that member kind
		treeViewer.addFilter(new MemberKindFilter());
		
		// When the user selects or deselects the Show: Library Code option, the tree
		// will update to include or remove library code items. The initial checked state
		// of the option will match the lib code setting in the preference store.
		treeViewer.addFilter(new LibraryCodeFilter());

		return treeViewer;
	}

	/**
	 * Create a vertical separator for the given parent.
	 * 
	 * @param parent
	 *            The parent composite.
	 */
	private void createVerticalSeparator(Composite parent) {
		Label separator= new Label(parent, SWT.SEPARATOR | SWT.VERTICAL | SWT.LINE_DOT);
		separator.setLayoutData(new GridData(GridData.FILL_VERTICAL));
	}

	@Override
	protected void fillViewMenu(IMenuManager viewMenu) {
		// overriding so that the "Filters..." action we don't need isn't added
	}

	@Override
	protected String getId() {
		return "com.architexa.diagrams.RSEMenuQuickOutline";
	}

	@Override
	public TreeViewer getTreeViewer() {
		return super.getTreeViewer();
	}

	@Override
	public void setInput(Object information) {
		getTreeViewer().setInput(information);

		// Update the filter options based on attributes of the member input
		setFilterButtons();

		// Disable the tree's items based on the client's instructions
		disableItems();

		// Expand subtrees by default
		getTreeViewer().expandAll();
		getTreeViewer().expandAll();
	}

	/**
	 * @param addActions Actions that will each have a button created 
	 * below the tree that when pushed will add items to the diagram 
	 * (for example an Add All action or show lifecycle action)
	 */
	public void setButtonInput(List<IAction> addActions) {
		// Create buttons for each action
		createAddButtons(addActions);
	}

	@Override
	// Overriding because we placed the tree in a container composite, so need to 
	// pass that container (leftAndRightColumn) instead of just fTreeViewer.getTree()
	protected void setTabOrder(Composite composite) {
		Text fFilterText = getFilterText();
		//TreeViewer fTreeViewer = getTreeViewer();

		Composite fViewMenuButtonComposite = null;
		try {
			Field fViewMenuButtonCompositeField = AbstractInformationControl.class.getDeclaredField("fViewMenuButtonComposite");
			fViewMenuButtonCompositeField.setAccessible(true);
			fViewMenuButtonComposite = (Composite) fViewMenuButtonCompositeField.get(this);
		} catch(Exception e) {
			logger.error("Unexpected exception while accessing field fViewMenuButtonComposite", e);
		}

		if (hasHeader()) {
			composite.setTabList(new Control[] { fFilterText, leftAndRightColumn });
		} else {
			fViewMenuButtonComposite.setTabList(new Control[] { fFilterText });
			composite.setTabList(new Control[] { fViewMenuButtonComposite, leftAndRightColumn });
		}
	}

	/**
	 * packs the menu's shell, making it at least but no more than 500 width.
	 * This ensures that the menu is wide enough to show the filtering checkboxes 
	 * on the right, maintains a minimum tree width so the menu list doesn't become 
	 * very small after a filter checkbox is unchecked and rechecked, and doesn't 
	 * stretch too wide if a menu item has a very long name.
	 */ 
	public void pack() {
		Point orgMenuSize = getShell().getSize();
		getShell().pack(); // set the shell's size to appropriately computed size
		setSize(orgMenuSize.x, orgMenuSize.y);
	}
	
	public void setInitSize() {
		Point menuSize = getShell().getSize();
		// then make sure it's at least but no more than 500 width
		setSize(Math.max(menuSize.x, 500), menuSize.y);
	}
	
	@Override
	public void setSize(int width, int height) {
		if (RSEOICUtils.getSavedWidth()!=0 && RSEOICUtils.getSavedHeight() !=0) {
			width =RSEOICUtils.getSavedWidth();
			height=RSEOICUtils.getSavedHeight();
		}	
		super.setSize(width, height);
	}
	
	private void applyBackgroundColor(Control control) {
		control.setBackground(getShell().getDisplay().getSystemColor(
				SWT.COLOR_INFO_BACKGROUND));
		if(control instanceof Composite) {
			for(Control child : ((Composite)control).getChildren())
				applyBackgroundColor(child);
		}
	}

	/*
	 * Removes the tree listeners added by AbstractInformationControl that we need to 
	 * add ourselves instead in order to properly handle addition of selected items to 
	 * the diagram. Need to remove the listeners added by the super or else they will 
	 * try to handle events after we have already disposed the tree, causing errors.
	 */
	private void removeTreeListeners(final Tree tree) {
		try {
			Field eventTableField = Widget.class.getDeclaredField("eventTable");
			eventTableField.setAccessible(true);
			Object eventTable = eventTableField.get(tree);

			Field listenersField = eventTable.getClass().getDeclaredField("listeners");
			listenersField.setAccessible(true);
			Listener [] listeners = (Listener []) listenersField.get(eventTable);

			for(Listener listener : listeners.clone()) {
				if(!(listener instanceof TypedListener)) continue;

				TypedListener typedListener = (TypedListener) listener;
				SWTEventListener eventListener = typedListener.getEventListener();

				// Line 209 of AbstractInformationControl.createDialogArea(Composite parent)
				// adds a selection listener to the tree, specifically
				// Tree.addSelectionListener(listener) adds 2 listeners
				// addListener (SWT.Selection, typedListener);
				// addListener (SWT.DefaultSelection, typedListener);
				// So remove these 2 listeners:
				if(eventListener instanceof SelectionListener) {
					tree.removeListener(SWT.Selection, typedListener);
					tree.removeListener(SWT.DefaultSelection, typedListener);
				}

				// Line 249 of AbstractInformationControl.createDialogArea(Composite parent)
				// adds a mouse listener to the tree, specifically
				// Tree.addMouseListener(MouseListener listener) adds 3 listeners
				// addListener (SWT.MouseDown,typedListener);
				// addListener (SWT.MouseUp,typedListener);
				// addListener (SWT.MouseDoubleClick,typedListener);
				// So remove these 3 listeners:
				if(eventListener instanceof MouseListener) {
					tree.removeListener(SWT.MouseDown, typedListener);
					tree.removeListener(SWT.MouseUp, typedListener);
					tree.removeListener(SWT.MouseDoubleClick, typedListener);
				}
			}
		} catch(Exception e) {
			logger.error("Unexpected exception while removing tree listeners ", e);
		}
	}

	private void addTreeListeners(final Tree tree) {

		tree.addKeyListener(getKeyAdapter());

		tree.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				// do nothing
			}
			public void widgetDefaultSelected(SelectionEvent e) {
				gotoSelectedElement();
			}
		});

		tree.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent e) {

				if (tree.getSelectionCount() < 1)
					return;

				if (e.button != 1)
					return;

				if (tree.equals(e.getSource())) {
					Object o= tree.getItem(new Point(e.x, e.y));
					TreeItem selection= tree.getSelection()[0];
					if (selection.equals(o))
						gotoSelectedElement();
				}
			}
		});

		// When visible menu items change due to a collapse or expand, check
		// whether any items are enabled and visible (ie add-able to the diagram)
		// and if so enable the add all buttons, otherwise disable them
		tree.addTreeListener(new TreeListener() {
			public void treeCollapsed(TreeEvent e) {
				// Refresh expanded item's expanded state 
				if(e.item instanceof TreeItem) ((TreeItem)e.item).setExpanded(false);
				// then update add buttons accordingly
				updateAddBtnsEnablement();
			}
			public void treeExpanded(TreeEvent e) {
				// Refresh expanded item's expanded state 
				if(e.item instanceof TreeItem) ((TreeItem)e.item).setExpanded(true);
				// then update add buttons accordingly
				updateAddBtnsEnablement();
			}
		});
	}

	private KeyAdapter getKeyAdapter() {
		if (fKeyAdapter == null) {
			fKeyAdapter= new KeyAdapter() {
				@SuppressWarnings("deprecation")
				@Override
				public void keyPressed(KeyEvent e) {
					int accelerator = org.eclipse.ui.keys.SWTKeySupport.convertEventToUnmodifiedAccelerator(e);
					org.eclipse.ui.keys.KeySequence keySequence = org.eclipse.ui.keys.KeySequence.getInstance(org.eclipse.ui.keys.SWTKeySupport.convertAcceleratorToKeyStroke(accelerator));
					org.eclipse.ui.keys.KeySequence[] sequences= getInvokingCommandKeySequences();
					if (sequences == null)
						return;
					for (int i= 0; i < sequences.length; i++) {
						if (sequences[i].equals(keySequence)) {
							e.doit= false;
							return;
						}
					}
				}
			};
		}
		return fKeyAdapter;
	}

	// When member from tree is selected, add it as child to frag
	private void gotoSelectedElement() {
		Object selectedElement= getSelectedElement();
		if (!(selectedElement instanceof MultiAddCommandAction)) return;
		MultiAddCommandAction action = (MultiAddCommandAction) selectedElement;

		// No way to disable tree items themselves, so we simply grayed out tree items
		// to make them look disabled, which means they'll still be selectable and we 
		// need to check that the action is actually enabled before running it
		if(!action.isEnabled()) return;

		try {
			dispose();
			action.run();
		} catch (Exception ex) {
			logger.error("Unable to add selected element: " + selectedElement, ex);
		}
	}

	@Override
	protected void selectFirstMatch() {
		Tree tree= getTreeViewer().getTree();
		Object element= findElement(tree.getItems());
		if (element != null)
			getTreeViewer().setSelection(new StructuredSelection(element), true);
		else
			getTreeViewer().setSelection(StructuredSelection.EMPTY);
	}

	private MultiAddCommandAction findElement(TreeItem[] items) {
		ILabelProvider labelProvider= (ILabelProvider)getTreeViewer().getLabelProvider();
		for (int i= 0; i < items.length; i++) {
			Object o = items[i].getData();
			MultiAddCommandAction element = null;
			if (o instanceof MultiAddCommandAction) element = (MultiAddCommandAction) o;
//			StringMatcher fStringMatcher = getMatcher();
//			if (getMatcher() == null && element != null)
//				return element;
//
//			if (element != null) {
//				String label= labelProvider.getText(element);
//				if (getMatcher().match(label))
//					return element;
//			}
			if (CompatUtils.findElementMatchForOICSearch(getMatcher(), element, labelProvider)!=null)
				return element;

			element= findElement(items[i].getItems());
			if (element != null)
				return element;
		}
		return null;
	}
	
	/*
	 * Create buttons below the tree for the actions the
	 * client provided to add tree items to the diagram
	 */
	private void createAddButtons(List<IAction> addActions) {
		if(addActions==null || addActions.isEmpty()) return;

		for(final IAction action : addActions) {
			Button button = new Button(leftColumn, SWT.PUSH);
			addButtons.add(button);

			button.setText(action.getText());
			if (action.getImageDescriptor() != null) {
				Image icon = ImageCache.calcImageFromDescriptor(action.getImageDescriptor());
				button.setImage(icon);
			}
				

			button.addSelectionListener(new SelectionListener() {
				public void widgetDefaultSelected(SelectionEvent e) {}
				public void widgetSelected(SelectionEvent e) {
					try {
						action.run();
					} catch (Exception ex) {
						logger.error("Unexpected exception when " +
								"adding item to diagram ", ex);
					}
				}
			});

			// If action is disabled (ie Add All is disabled because there are no
			// enabled items in the menu that can be added), disable the button
			button.setEnabled(action.isEnabled());
		}

		// Make sure everything has the proper background color
		applyBackgroundColor(leftColumn);
	}

	/*
	 * Place buttons to filter members based on access level 
	 * (public/protected/private/package) and buttons to filter 
	 * members based on kind (class/interface/field/method etc)
	 * in an area to the right of the tree
	 */
	private void setFilterButtons() {

		// Get a list of the access levels that belong to at least one member
		List<Resource> accessLevels = new ArrayList<Resource>();
		// Get a list of the kinds (class, interface, field, 
		// method, etc) that correspond to at least one member
		List<Resource> kinds = new ArrayList<Resource>();
		// Determine if at least one member is library code
		boolean hasLibCodeItem = false;

		List<MultiAddCommandAction> addActions = getMemberAddActions();
		for(MultiAddCommandAction action : addActions) {
			Artifact art = getArtifact(action);
			
			Resource artAccess = getAccess(art);
			if(artAccess==null) 
				logger.info("Unable to determine access level of Artifact "+art);
			else if(!accessLevels.contains(artAccess)) accessLevels.add(artAccess);

			Resource type = art.queryType(repo);
			if(!kinds.contains(type)) kinds.add(type);

			if(!RSECore.isInitialized(repo, art.elementRes)) hasLibCodeItem = true;
		}

		// Remove unrecognized any access levels
		for(Resource accessLevel : new ArrayList<Resource>(accessLevels)) {
			String text = accessLevelMap.get(accessLevel);
			if(text==null) {
				logger.info("Unrecognized access level: " + accessLevel);
				accessLevels.remove(accessLevel);
			}
		}
		// Remove any unrecognized member kinds
		for(Resource kind : new ArrayList<Resource>(kinds)) {
			// TODO Why would this happen??
			if (kind == null) continue;
			
			int typeStringStart = kind.toString().indexOf("#");
			if(typeStringStart==-1 || typeStringStart==kind.toString().length()-1) {
				logger.info("Unrecognized member kind: " + kind);
				kinds.remove(kind);
			}
		}

		if(accessLevels.isEmpty())
			logger.info("No recognizable access level found for any member. "+
					"Filter column will therefore contain no enabled buttons for filtering " +
			"members based on access level.");
		if(kinds.isEmpty())
			logger.info("No recognizable kind found for any member. "+
					"Filter column will therefore contain no buttons for " +
					"filtering members based on their kind (class, " +
			"interface, field, method, etc).");
		
		// If members have more than one type of access level, add all the access level 
		// filter buttons and disable the ones corresponding to levels no member has.
		// However, if the members are all the same access level or have no recognizable
		// access levels, only add the access level filter buttons if the client has 
		// indicated to do so
		boolean addAccessLevelFilterButtons = (accessLevels.size()>1 || 
				filters.contains(FILTERS_ACCESS_LEVEL));
		// If members have more than one kind, add all the member kind filter buttons 
		// and disable the ones corresponding to kinds no member has. However, if the 
		// members are all the same kind or have no recognizable kinds, only add the 
		// member kind filter buttons if the client has indicated to do so
		boolean addMemberKindFilterButtons = (kinds.size()>1 || 
				filters.contains(FILTERS_MEMBER_KIND));
		// If there is at least one library code item in the tree, show the button
		// to filter it. Otherwise, only show the disabled library code button if
		// the client has indicated to do so
		boolean addLibraryCodeFilterButton = (hasLibCodeItem || 
				filters.contains(FILTERS_LIBRARY_CODE));

		// If no buttons will be created, no need to create the filter column
		if(!addAccessLevelFilterButtons && 
				!addMemberKindFilterButtons && 
				!addLibraryCodeFilterButton) 
			return;

		// Create vertical separator between the left column (which holds 
		// the tree and the Add All button) and the right column of filter buttons
		createVerticalSeparator(leftAndRightColumn);

		rightColumn = new Composite(leftAndRightColumn, SWT.NONE);
		GridLayout layout = new GridLayout(1, false);
		layout.marginHeight = 0;
		rightColumn.setLayout(layout);
		rightColumn.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, true));

		org.eclipse.swt.widgets.Label showLabel = new org.eclipse.swt.widgets.Label(rightColumn, SWT.LEFT);
		showLabel.setText("Show: ");
		showLabel.setLayoutData(new GridData());

		if(addAccessLevelFilterButtons) {
			setAccessLevelFilterButtons(accessLevels);
		}

		if(addMemberKindFilterButtons) {
			if(addAccessLevelFilterButtons) createHorizontalSeparator(rightColumn);
			setMemberKindFilterButtons(kinds);
		}

		if(addLibraryCodeFilterButton) {
			if(addMemberKindFilterButtons || addAccessLevelFilterButtons)
				createHorizontalSeparator(rightColumn);
			setLibraryCodeFilterButton(hasLibCodeItem);
		}

		applyBackgroundColor(rightColumn); // Make sure everything has proper color
		getShell().pack(); // Refresh to resize and show everything in the menu properly
	}

	/*
	 * Create buttons to include/filter public, protected, 
	 * private, and/or package members in the tree
	 */
	private void setAccessLevelFilterButtons(List<Resource> accessLevels) {

		if(includedAccessLevels==null) includedAccessLevels = new ArrayList<Resource>();

		Composite accessLevelButtons = new Composite(rightColumn, SWT.NONE);
		accessLevelButtons.setLayout(new GridLayout(1, false));
		accessLevelButtons.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, true));

		for(Resource accessLevel : accessLevelMap.keySet()) {
			String text = accessLevelMap.get(accessLevel);
			String iconKey = CodeUnit.getMethodAcessIconKey(accessLevel);
			Button b = createFilterButton(accessLevelButtons, text, iconKey, accessLevel, includedAccessLevels);

			// Disable and uncheck the button if no member has that access level
			if(!accessLevels.contains(accessLevel)) {
				b.setEnabled(false);
				b.setSelection(false);
			}
		}
	}

	/*
	 * Create buttons to include/filter members in the tree based on 
	 * their kind (class, interface, field, method, etc.)
	 */
	private void setMemberKindFilterButtons(List<Resource> kinds) {

		if(includedMemberKinds==null) includedMemberKinds = new ArrayList<Resource>();

		Composite memberKindButtons = new Composite(rightColumn, SWT.NONE);
		memberKindButtons.setLayout(new GridLayout(1, false));
		memberKindButtons.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, true));

		for(Resource kind : possibleMemberKinds) {
			int typeStringStart = kind.toString().indexOf("#");
			String text = kind.toString().substring(typeStringStart+1);
			// capitalize first letter (ie "Field" looks better than "field")
			text = text.substring(0, 1).toUpperCase()+text.substring(1);
			// pluralize (ie "Fields" makes more sense than "Field")
			text = "s".equals(text.substring(text.length()-1)) ? text+"es" : text+"s";
			String iconKey = CodeUnit.getTypeIconKey(kind);

			Button b = createFilterButton(memberKindButtons, text, iconKey, kind, includedMemberKinds);

			// Disable and uncheck the button if no member has that kind
			if(!kinds.contains(kind)) {
				b.setEnabled(false);
				b.setSelection(false);
			}
		}
	}

	/*
	 * Create button to include/filter members in the tree based on 
	 * whether or not they are library code
	 */
	private void setLibraryCodeFilterButton(boolean atLeastOneLibCodeItem) {

		if(includeLibraryCode==null) includeLibraryCode = new ArrayList<Resource>();
		
		// Put button in its own composite so that 
		// it aligns with the other filter buttons
		Composite libCodeComposite = new Composite(rightColumn, SWT.NONE);
		libCodeComposite.setLayout(new GridLayout(1, false));
		libCodeComposite.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, true));

		String text = "Library Code";
		String iconKey = ISharedImages.IMG_OBJS_EXTERNAL_ARCHIVE;
		Resource attribute = RSECore.initialized;
		Button b = createFilterButton(libCodeComposite, text, iconKey, attribute, includeLibraryCode);

		// Disable and uncheck the button if no member is library code
		if(!atLeastOneLibCodeItem) {
			b.setEnabled(false);
			b.setSelection(false);
		}
	}

	private Button createFilterButton(Composite filterColumn, String text, String iconKey, 
			Resource memberAttribute, final List<Resource> listOfAttributesNotToFilter) {

		Button b = new Button(filterColumn, SWT.CHECK);
		b.setData(memberAttribute);

		b.setText(text);
		Image image = ImageCache.calcImageFromDescriptor(CodeUnit.getImageDescriptorFromKey(iconKey));
		b.setImage(image);

		// Listen for clicks on the button so can update the tree appropriately
		b.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {}
			public void widgetSelected(SelectionEvent e) {
				Button b = (Button) e.getSource();
				handleButtonSelection(b, listOfAttributesNotToFilter);
				Resource memberAttribute = (Resource) b.getData();
				updateFilterStoredState(memberAttribute, b.getSelection());
			}
		});

		// Initially check all buttons to include all members
		// in the tree regardless of access level or member kind
		int i = possibleMemberKinds.indexOf(memberAttribute);
		if (i == -1) {
			i = new ArrayList<Resource>(accessLevelMap.keySet()).indexOf(memberAttribute);
			if (i == -1) {
				b.setSelection(FILTERS_LIBRARY_STATE.equals("1"));
			} else
				b.setSelection(Integer.valueOf(FILTERS_ACCESS_STATE.charAt(i)).equals(49));
		} else 
			b.setSelection(Integer.valueOf(FILTERS_TYPE_STATE.charAt(i)).equals(49));
		handleButtonSelection(b, listOfAttributesNotToFilter);

		return b;
	}

	private void handleButtonSelection(Button b, List<Resource> includedAttributes) {
		Resource memberAttribute = (Resource) b.getData();
		if(b.getSelection()) includedAttributes.add(memberAttribute);
		else includedAttributes.remove(memberAttribute);

		getTreeViewer().refresh();
		// pack to make sure scroll bar stays at the top 
		// and not scrolled half way down after expand:
		getTreeViewer().getTree().pack();

		// New items may be in tree that correspond to disabled actions 
		// and therefore need to have their font and image grayed out
		disableItems(); 

		// The items present in the tree have changed, so update the enablement of any
		// buttons that add multiple calls (for example Add All or Add Callee Hierarchy). 
		// Specifically, if the tree currently contains at least one item that is visible 
		// and enabled (ie is add-able to the diagram), enable these buttons; otherwise, 
		// disable the buttons because there are now no add-able items present in the menu.
		updateAddBtnsEnablement();

		// make sure menu is proper size to show all components but not stretch too wide
		pack();
	}

	private void updateFilterStoredState(Resource memberAttribute, boolean isSelected) {
		if (accessLevelMap.containsKey(memberAttribute)) { //Access Level Checked
			String filterAccess = FILTERS_ACCESS_STATE;			
			List<Resource> accessList = new ArrayList<Resource>(accessLevelMap.keySet());
			char isSelectedChar = '0';
			if (isSelected)
				isSelectedChar = '1';
			char[] charArray = filterAccess.toCharArray();
			charArray[accessList.indexOf(memberAttribute)] = isSelectedChar;
			FILTERS_ACCESS_STATE = new String(charArray);
		} else if (possibleMemberKinds.contains(memberAttribute)) { // type checked
			String filterMemberKind = FILTERS_TYPE_STATE;
			ArrayList<Resource> memberTypesList = new ArrayList<Resource>(possibleMemberKinds);
			char isSelectedChar = '0';
			if (isSelected)
				isSelectedChar = '1';
			char[] charArray = filterMemberKind.toCharArray();
			charArray[memberTypesList.indexOf(memberAttribute)] = isSelectedChar;
			FILTERS_TYPE_STATE = new String(charArray);
		} else { // probably library code
			FILTERS_LIBRARY_STATE = "0";
			if (isSelected)
				FILTERS_LIBRARY_STATE = "1";
		}

		
		
	}

	// Iterates through the items in the tree and disables the 
	// ones that the client has specified should be disabled
	private void disableItems() {
		Tree tree = getTreeViewer().getTree();
		for(TreeItem item : tree.getItems()) {
			Object data = item.getData();
			if(data instanceof MultiAddCommandAction &&
					!((MultiAddCommandAction)data).isEnabled()) {
				// no way to disable items in a jface tree, so we just
				// change the item's font and icon color to gray and make sure
				// later that if the item is selected, nothing happens
				item.setForeground(ColorConstants.gray);
				Image oldImg = item.getImage();
				Image disabledImage = new Image(Display.getCurrent(), oldImg , SWT.IMAGE_DISABLE);
				item.setImage(disabledImage);
			}
		}
	}

	// Iterates through the items in the tree until finds ones that is add-able to 
	// the diagram (ie is visible in the tree, not in a collapsed subtree, and is 
	// enabled for selection). If one such item exists, enabling all add buttons,
	// otherwise disabling them.
	private void updateAddBtnsEnablement() {
		if(addButtons.size()==0) return; // no buttons to update

		TreeItem[] visibleItems = getTreeViewer().getTree().getItems();
		boolean enable = atLeastOneAddableItem(visibleItems);
		for(Button button : addButtons) button.setEnabled(enable);
	}
	// Iterates over the items in the menu tree, returning true as soon as
	// an item that is visible (unfiltered and not in a collapsed subtree) and
	// enabled is encountered. If no such item is found in the tree, returns false
	private boolean atLeastOneAddableItem(TreeItem[] visibleItems) {
		for(TreeItem visibleItem : visibleItems) {
			if(atLeastOneAddableItem(visibleItem)) return true;
		}
		return false;
	}
	private boolean atLeastOneAddableItem(TreeItem visibleItem) {
		Object data = visibleItem.getData();

		// This item is visible (ie unfiltered and not in a collapsed 
		// subtree), so if it is also enabled, it is add-able so return true
		if(data instanceof MultiAddCommandAction &&
				((MultiAddCommandAction)data).isEnabled())
			return true;

		// Do not iterate through collapsed subtrees; if user has collapsed a
		// subtree we don't add those subtree items when an add button is pushed
		boolean collapsed = !visibleItem.getExpanded();
		if(collapsed) return false;

		// Item not enabled itself but is parent of an
		// expanded subtree, which may contain an add-able item
		return atLeastOneAddableItem(visibleItem.getItems());
	}

	/*
	 * Returns a list of the MultiAddCommandActions
	 * that correspond to each member item in the tree
	 */
	private List<MultiAddCommandAction> getMemberAddActions() {
		List<MultiAddCommandAction> addActions = new ArrayList<MultiAddCommandAction>();
		addMemberAddActions(addActions, getTreeViewer().getTree().getItems());
		return addActions;
	}
	private List<MultiAddCommandAction> addMemberAddActions(
			List<MultiAddCommandAction> addActions, TreeItem[] items) {
		if(items==null) 
			return addActions;
		for (TreeItem item : items) {
			Object data = item.getData();
			if(data instanceof MultiAddCommandAction)
				addActions.add((MultiAddCommandAction)data);
			else if (data instanceof String) {
				addMemberAddActions(addActions, item.getItems());
			}
			// Ignoring the sub trees for filters
			// go through subtree (if there is one)
//			addMemberAddActions(addActions, item.getItems());
		}
		return addActions;
	}

	/*
	 * Returns an Artifact for the member item 
	 * that has the given MultiAddCommandAction
	 */
	public static Artifact getArtifact(MultiAddCommandAction addAction) {
		if (addAction.getInvokedModelArtifact() != null)
			return addAction.getInvokedModelArtifact();
		
		if (actionToArtMap.containsKey(addAction))
			return actionToArtMap.get(addAction);
		
		Artifact art = null;
		CompoundCommand tgtCmd = (CompoundCommand) addAction.getCommand(new HashMap<Artifact, ArtifactFragment>());
		for (Object o : tgtCmd.getCommands()) {
			if (o instanceof AddNodeAndRelCmd)
				art = ((AddNodeAndRelCmd)o).getNewArtFrag().getArt();
			if (o instanceof AddNodeCommand)
				art = ((AddNodeCommand)o).getNewArtFrag().getArt();
			if (o instanceof AddResCommand)
				art = ((AddResCommand)o).getNewArt();
		}
		actionToArtMap.put(addAction, art);
		return art;
	}

	/*
	 * Returns the access level of the given Artifact and tries to handle
	 * Artifacts for which the access level is unavailable, such as library code
	 */
	private Resource getAccess(Artifact art) {
		if (artToResMap.containsKey(art))
			return artToResMap.get(art);
		Resource artAccess = CodeUnit.getAccess(repo, art);

		// If art is library code, it gets the default access icon.
		// TODO: lib code is actually probably public usually (or else
		// it wouldn't be accessible) unless it's protected or package 
		// level and accessible via inheritance
		if(artAccess==null && !RSECore.isInitialized(repo, art.elementRes))
			artAccess = RJCore.noAccess;

		artToResMap.put(art, artAccess);
		return artAccess;
	}

	/**
	 * The NameFilter selects the elements which match the given string patterns.
	 * We extend NamePatternFilter in order to handle matching with items in subtrees.
	 */
	private class NameFilter extends NamePatternFilter {

		@Override
		public boolean select(Viewer viewer, Object parentElement, Object element) {
			
			TreeViewer treeViewer= (TreeViewer) viewer;
			if (CompatUtils.findBooleanMatchForOICSearch(getMatcher(), treeViewer, element))
				return true;

			return hasUnfilteredChild(treeViewer, element);
		}

		private boolean hasUnfilteredChild(TreeViewer viewer, Object element) {
			if(!subTreeMap.containsKey(element)) return false;
			Object[] children = ((ITreeContentProvider) viewer.getContentProvider()).getChildren(element);
			for(Object child : children) {
				if(select(viewer, element, child))
					return true; // has at least one matching subtree item
			}
			return false; 
		}
	}

	/**
	 * The AccessLevelFilter selects the elements with the given access level
	 */
	private class AccessLevelFilter extends ViewerFilter {
		public AccessLevelFilter() {}

		@Override
		public boolean select(Viewer viewer, Object parentElement, Object element) {

			if (overridenSubTree != null && overridenSubTree.contains(element)) return true;
			// If element is a String heading of a submenu, then if at least one
			// item in its subtree will be visible, show the heading. Otherwise 
			// if all the subitems are being filtered out, hide the heading too.
			if (element instanceof String && subTreeMap.containsKey(element))
				return hasUnfilteredChild((TreeViewer)viewer, element);

			if (includedAccessLevels == null) return true; // not initialized yet, so filter nothing

			// Would expect element to be a MultiAddCommandAction 
			// at this point, so if it's not, don't show it
			if (!(element instanceof MultiAddCommandAction)) return false;
			Artifact art = getArtifact((MultiAddCommandAction)element);
			Resource artAccess = getAccess(art);

			// If its access level is unknown (null), just show it (a super
			// class that is library code will have null access level and
			// we want it to show in the inheritance nav aid).
			if (artAccess == null) return true;

			if (includedAccessLevels.isEmpty()) return false; // everything being filtered

			// If the member has an access level that matches one of the
			// levels the user has selected to show, include the member
			// in the tree. Otherwise if it doesn't match, filter it out.
			return includedAccessLevels.contains(artAccess);
		}

		private boolean hasUnfilteredChild(TreeViewer viewer, Object element) {
			if (!subTreeMap.containsKey(element)) return false;
			Object[] children = ((ITreeContentProvider) viewer.getContentProvider()).getChildren(element);
			for (Object child : children) {
				if (select(viewer, element, child))
					return true; // has at least one visible subtree item
			}
			return false; 
		}
	}

	/**
	 * The MemberKindFilter selects the elements with the given kind
	 */
	private class MemberKindFilter extends ViewerFilter {
		public MemberKindFilter() {}

		@Override
		public boolean select(Viewer viewer, Object parentElement, Object element) {

			if (overridenSubTree != null && overridenSubTree.contains(element)) return true;
			// If element is a String heading of a submenu, then if at least one
			// item in its subtree will be visible, show the heading. Otherwise 
			// if all the subitems are being filtered out, hide the heading too.
			if (element instanceof String && subTreeMap.containsKey(element))
				return hasUnfilteredChild((TreeViewer)viewer, element);

			if (includedMemberKinds == null) return true; // not initialized yet, so filter nothing
			if (includedMemberKinds.isEmpty()) return false; // everything being filtered

			// Would expect element to be a MultiAddCommandAction 
			// at this point, so if it's not, don't show it
			if (!(element instanceof MultiAddCommandAction)) return false;

			// If the member has a type that matches one of the types
			// the user has selected to show, include the member in
			// the tree. Otherwise if it doesn't match, filter it out.
			Artifact art = getArtifact((MultiAddCommandAction)element);
			Resource artType = art.queryType(repo);
			return includedMemberKinds.contains(artType);
		}

		private boolean hasUnfilteredChild(TreeViewer viewer, Object element) {
			if(!subTreeMap.containsKey(element)) return false;
			Object[] children = ((ITreeContentProvider) viewer.getContentProvider()).getChildren(element);
			for (Object child : children) {
				if (select(viewer, element, child))
					return true; // has at least one visible subtree item
			}
			return false; 
		}
	}

	/**
	 * The LibraryCodeFilter selects all elements if the library code button is
	 * selected and selects non-library code elements if the button is not selected
	 */
	private class LibraryCodeFilter extends ViewerFilter {
		public LibraryCodeFilter() {}

		@Override
		public boolean select(Viewer viewer, Object parentElement, Object element) {

			if (overridenSubTree != null && overridenSubTree.contains(element)) return true;
			// If element is a String heading of a submenu, then if at least one
			// item in its subtree will be visible, show the heading. Otherwise 
			// if all the subitems are being filtered out, hide the heading too.
			if (element instanceof String && subTreeMap.containsKey(element))
				return hasUnfilteredChild((TreeViewer)viewer, element);

			if (includeLibraryCode == null) return true; // not initialized yet, so filter nothing
			if (!includeLibraryCode.isEmpty()) 
				return true; // including library code, so no need to filter anything 

			// Would expect element to be a MultiAddCommandAction 
			// at this point, so if it's not, don't show it
			if (!(element instanceof MultiAddCommandAction)) return false;

			// If the member is not library code, include it in the tree.
			// Otherwise, filter it out.
			Artifact art = getArtifact((MultiAddCommandAction)element);
			return RSECore.isInitialized(repo, art.elementRes);
		}

		private boolean hasUnfilteredChild(TreeViewer viewer, Object element) {
			if (!subTreeMap.containsKey(element)) return false;
			Object[] children = ((ITreeContentProvider) viewer.getContentProvider()).getChildren(element);
			for (Object child : children) {
				if (select(viewer, element, child))
					return true; // has at least one visible subtree item
			}
			return false; 
		}
	}

	public static void savePrefs() {
		AtxaIntroPlugin.getDefault().getPreferenceStore().setValue(PreferenceConstants.FILTERS_LIBRARY_CODE, FILTERS_LIBRARY_STATE);
		AtxaIntroPlugin.getDefault().getPreferenceStore().setValue(PreferenceConstants.FILTERS_ACCESS_LEVEL, FILTERS_ACCESS_STATE);
		AtxaIntroPlugin.getDefault().getPreferenceStore().setValue(PreferenceConstants.FILTERS_MEMBER_KIND, FILTERS_TYPE_STATE);
	}

}