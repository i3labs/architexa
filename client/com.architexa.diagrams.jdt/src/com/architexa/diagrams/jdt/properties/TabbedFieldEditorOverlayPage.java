package com.architexa.diagrams.jdt.properties;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.ui.ISharedImages;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.IWorkbenchPropertyPage;

import com.architexa.diagrams.jdt.Activator;
import com.architexa.diagrams.jdt.builder.asm.ClassExtensionBuilder;
import com.architexa.rse.BuildSettings;
import com.architexa.rse.PreferenceUtils;


public abstract class TabbedFieldEditorOverlayPage
					extends FieldEditorPreferencePage
                    implements IWorkbenchPropertyPage {

//	private String pageId;
	private IAdaptable element;
	private Composite page;
	private TabFolder folder;
	private Table jarTable;
	private Button selectAll;
	private Button deselectAll;
	private static final int BUTTON_WIDTH = 125;
	private static final int BUTTON_HEIGHT = 23;
	private static final int BUTTON_X = 1;
	private static final int BUTTON_Y = 30;
	private List<URL> classPathURLs;
	private List<URL> jreSysLib = new ArrayList<URL>();
	final ImageRegistry imageRegistry = new ImageRegistry();
	private TabFolder tabbedFolder;
	private TabItem packageFilterItem;
	private TabItem jarFilterItem;
	private Table packgTable;
	private static final String IMG_DEFAULT = "default";
	private static final String IMG_JAR = "jar";
	private static final String IMG_JAR_ALTERNATE = "jarAlternate";
	private static final String IMG_ZIP = "zip";
	private static final String IMG_JRE = "jre";
	private static final String FALSE = "false";
	private static final String TRUE = "true";
	private static final String JRE_SYSTEM_LIBRARY = "JRE System Library (Not currently supported)";
	//Not supported because building these jars results in an OutOfMemoryException
	
	static final Logger logger = Activator.getLogger(TabbedFieldEditorOverlayPage.class);
	
	public TabbedFieldEditorOverlayPage(int style) {
		super(style);
	}
	
	public TabbedFieldEditorOverlayPage(String title, int style) {
		super(title, style);
	}

	public TabbedFieldEditorOverlayPage(String title, ImageDescriptor image, int style) {
	  super(title, image, style);
	}
	
	/**
	 * Create contents of property page
	 */
	@Override
	public Control createContents(Composite composite) {
		
		tabbedFolder = new TabFolder(composite, SWT.TOP);
		
		packageFilterItem = new TabItem(tabbedFolder, SWT.NONE);
		packageFilterItem.setText("Package Filters");
		Control packgFilter = createPackgFilterContents(tabbedFolder);
		packageFilterItem.setControl(packgFilter);
		
		jarFilterItem = new TabItem(tabbedFolder, SWT.NONE);
		jarFilterItem.setText("Jar Filters");
		Control jarFilter = createJarFilterContents(tabbedFolder);
		jarFilterItem.setControl(jarFilter);
		tabbedFolder.pack();
		
		return tabbedFolder;
	}
	
	private Control createJarFilterContents(TabFolder tabbedFolder2) {
//		pageId = "Architexa Build Path Properties";
		page = new Composite(tabbedFolder,SWT.NONE);
		GridLayout layout = new GridLayout(2, false);
		layout.marginHeight=0;
		page.setLayout(layout);
		GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
		data.heightHint=350;
		page.setLayoutData(data);
		try {
    		IProject project = ((IResource)element).getProject();
    		project.setSessionProperty(new QualifiedName(ClassExtensionBuilder.JARS_TO_BUILD, ClassExtensionBuilder.JARS_TO_BUILD), new ArrayList<URL>());
    		project.setSessionProperty(new QualifiedName(ClassExtensionBuilder.JARS_TO_REMOVE, ClassExtensionBuilder.JARS_TO_REMOVE), new ArrayList<URL>());
    		if (project.getPersistentProperty(new QualifiedName(ClassExtensionBuilder.JARS_SELECTED, ClassExtensionBuilder.JARS_SELECTED)) == null)
    			project.setPersistentProperty(new QualifiedName(ClassExtensionBuilder.JARS_SELECTED, ClassExtensionBuilder.JARS_SELECTED), new ArrayList<URL>().toString());
		} catch (CoreException e) {
			logger.error("Uexpected Error", e);
		} finally {
			
		}
		
		Label desc = new Label(page, SWT.NONE);
		desc.setText("\nJARs and class folders to include in Architexa RSE build: " +
				"\n (Classes in selected jars will be displayed in visualizations)\n");
		desc.setLayoutData(new GridData(SWT.LEFT, SWT.NONE, false, false, 2, 1));
		addJarSelectionArea();
		return page;
	}

	private Control createPackgFilterContents(TabFolder tabbedFolder) {
		Composite container = new Composite(tabbedFolder,SWT.NONE);
		GridLayout layout = new GridLayout(2, false);
		layout.marginHeight=0;
		container.setLayout(layout);
		GridData data = new GridData(SWT.FILL, SWT.FILL, true, false);
		data.heightHint=350;
		container.setLayoutData(data);
		
		Label desc = new Label(container, SWT.NONE);
		desc.setText("\nSelect the packages below to include in the Architexa build. " +
		"\nWe recommend you remove non-relevant packages to make the build faster.\n");
		desc.setLayoutData(new GridData(SWT.LEFT, SWT.NONE, false, false, 2, 1));
		
		addProjectSelectionArea(container);
		return container;
	}

	
	
	private void addProjectSelectionArea(Composite container) {
		addProjectTable(container);
		addSelectButtons(container, packgTable);
	}

	private static ISharedImages isi = JavaUI.getSharedImages();

	@SuppressWarnings("restriction")
	private void addProjectTable(Composite container) {
		
		packgTable = new Table(container, SWT.CHECK | SWT.V_SCROLL | SWT.H_SCROLL | SWT.BORDER | SWT.MULTI);
		GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
		gridData.heightHint = 350;
		gridData.widthHint = 250;
		packgTable.setLayoutData(gridData);
		packgTable.setBackground(packgTable.getDisplay().getSystemColor(SWT.COLOR_WHITE));	
		
		try {
			IResource res = (IResource) getElement();
			IProject iProject = res.getProject();
			IJavaProject project = JavaCore.create(iProject);
			for (IPackageFragment pckg : project.getPackageFragments()) {
				if (pckg.getParent() instanceof org.eclipse.jdt.internal.core.JarPackageFragmentRoot
						|| !pckg.containsJavaResources())
					continue;
				TableItem packgItem = new TableItem(packgTable, SWT.NONE);
				
				if (pckg.getElementName().equals(""))
					packgItem.setText("(default)");
				else // add src folder context so we can tell the difference between two packages with the same name in different src folders
					packgItem.setText(pckg.getParent().getElementName() + "/" + pckg.getElementName());
				packgItem.setImage(isi.getImage(ISharedImages.IMG_OBJS_PACKAGE));
				packgItem.setChecked(true);
			}
			setUpTableFromPreferences(project.getElementName(), packgTable);
		} catch (JavaModelException e) {
			e.printStackTrace();
		} 

		packgTable.pack();
		packgTable.update();
	}
	
	private void setUpTableFromPreferences(String projName, Table table) {
		// Setting up the menu from the preference
		Map<String, String> unselPackagesMap = BuildSettings.getUnselectedPackagesToSrcFldrMapFromProj(projName);
		
		if (unselPackagesMap==null) return; //projJE is completely checked
		if (unselPackagesMap.isEmpty()) {//projJE is completely unchecked
			for (TableItem item : table.getItems()) 
				item.setChecked(false);
		}
		
		for (TableItem item : table.getItems()) {
			// If Parent is unchecked uncheck all children
			
			String pckgName = item.getText();
			String srcFldrName = null;
			if (item.getText().contains("/")) {
				pckgName = item.getText().substring(item.getText().indexOf("/")+1);
				srcFldrName = item.getText().substring(0, item.getText().indexOf("/"));
			}
			if (srcFldrName != null 
					&& unselPackagesMap.get(pckgName) != null
					&& unselPackagesMap.get(pckgName).equals(srcFldrName)) // project checked but package is not
				item.setChecked(false);
		}
		
	}

	public void addSelectButtons(Composite container, final Table table) {	
		
		Composite buttonColumn = new Composite(container, SWT.NONE);
		GridLayout buttonColumnLayout = new GridLayout(1, true);
		buttonColumn.setLayout(buttonColumnLayout);
		buttonColumn.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, true));	
		
		Button selectAll = new Button(buttonColumn, SWT.PUSH);
		selectAll.setText("Select All");
		selectAll.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		selectAll.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				selectDeselectAllInTree(table.getItems(), true);
			}
		});

		Button deselectAll = new Button(buttonColumn, SWT.PUSH);
		deselectAll.setText("Deselect All");
		deselectAll.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		deselectAll.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				selectDeselectAllInTree(table.getItems(), false);
			}
			
		});
		
		buttonColumn.pack();
		buttonColumn.update();
	}

	private void selectDeselectAllInTree(TableItem[] tableItems, boolean select) {
		for (TableItem item : tableItems) {
			item.setChecked(select);
		}
	}

	/**
	 * Called when property page is opened
	 */
	public void setElement(IAdaptable element) {
		this.element = element;
	}

	public IAdaptable getElement() {
		return element;
	}
	
	/**
	 * Returns true if page opened was a property page
	 */
	public boolean isPropertyPage() {
		return getElement() != null;
	}	

	/**
	 * Determines where the newly constructed field editor will be placed
	 */
	@Override
	protected Composite getFieldEditorParent() {
		if(folder==null || folder.getItemCount()==0) {
			return super.getFieldEditorParent();
		}
		return (Composite) folder.getItem(folder.getItemCount()-1).getControl();
	}
	
	/** 
	 * Creates Select All and Deselect All buttons
	 */
	public void addSelectButtons() {	
		Composite composite = new Composite(page, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.NONE, SWT.FILL, false, true));
			
		selectAll = new Button(composite, SWT.PUSH);
		selectAll.setSize(BUTTON_WIDTH, BUTTON_HEIGHT);
		selectAll.setText("Select All");
		GridData selectAllData = new GridData(GridData.FILL_HORIZONTAL);
		selectAll.setLayoutData(selectAllData);
		selectAll.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				TableItem[] items = jarTable.getItems();
				for(TableItem item : items) {
					if(!(item.getText().equals(JRE_SYSTEM_LIBRARY))) {
						item.setChecked(true); // take this out of if statement once jdk supported
					}
				}
			}
		});

		deselectAll = new Button(composite, SWT.PUSH);
		deselectAll.setBounds(BUTTON_X, BUTTON_Y, BUTTON_WIDTH, BUTTON_HEIGHT);
		deselectAll.setText("Deselect All");
		deselectAll.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				TableItem[] items = jarTable.getItems();
				for(TableItem item : items) {
					if(!(item.getImage().equals(imageRegistry.get(IMG_DEFAULT)))) {
						item.setChecked(false);
					}
				}
			}
		});
		composite.pack();
		composite.update();
	}
	
	/**
	 * Determines URLs in project's classpath and 
	 * adds them to a list for user selection
	 */
	public void addJarSelectionArea() {			
		IProject proj = ((IResource)element).getProject();
		IJavaProject project = JavaCore.create(proj);
		URL[] classPathURLArray = PreferenceUtils.getProjectClasspath(project);
		classPathURLs = new ArrayList<URL>();
		for(URL url : classPathURLArray) {
			classPathURLs.add(url);
			if(url.toString().contains("jre")) {
				jreSysLib.add(url);
			}
		}
		addJarList();
		addSelectButtons();
	}

	/**
	 * Create the table that holds the list of jars for project
	 * Bug in eclipse: a 25x25 pixel or smaller image will cause 
	 * checkbox to decrease in size. Used own images that were 
	 * resized to be big enough to allow correctly sized checkboxes
	 */
	public void addJarList() {	
		final Table table = new Table(page, SWT.CHECK | SWT.V_SCROLL | SWT.H_SCROLL | SWT.BORDER | SWT.MULTI);
		GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
		gridData.heightHint = 350;
		gridData.widthHint = 250;
		table.setLayoutData(gridData);
		table.setBackground(table.getDisplay().getSystemColor(SWT.COLOR_WHITE));
						
		Class<TabbedFieldEditorOverlayPage> thisClass = TabbedFieldEditorOverlayPage.class;
		ImageDescriptor defaultIcon = ImageDescriptor.createFromFile(thisClass, "default.png");
		ImageDescriptor jar = ImageDescriptor.createFromFile(thisClass, "jar.png");
		ImageDescriptor jarAlternate = ImageDescriptor.createFromFile(thisClass, "jarAlternate.png");
		ImageDescriptor zip = ImageDescriptor.createFromFile(thisClass, "zip.png");
		ImageDescriptor jre = ImageDescriptor.createFromFile(thisClass, "jre.png");
		imageRegistry.put(IMG_DEFAULT, defaultIcon);
		imageRegistry.put(IMG_JAR, jar);
		imageRegistry.put(IMG_JAR_ALTERNATE, jarAlternate);
		imageRegistry.put(IMG_ZIP, zip);
		imageRegistry.put(IMG_JRE, jre);
		
		List<String> jarFileNames = new ArrayList<String>();
		String token = new String();
		for(URL url : classPathURLs) {
			StringTokenizer tokenizer = new StringTokenizer(url.toString(), "/");
			while(tokenizer.hasMoreTokens()) {
	          token = tokenizer.nextToken();
			}
			jarFileNames.add(token);
		}
		
		int numberOfJars = jarFileNames.size();
		boolean addedJreItem = false;
		
		
		for(int i=0; i<numberOfJars; i++) {
			if(jreSysLib.contains(classPathURLs.get(i))) {
				if(!addedJreItem) {
					TableItem item = new TableItem(table, SWT.NONE);
					item.setImage(imageRegistry.get(IMG_JRE));
					item.setText(JRE_SYSTEM_LIBRARY);
					addedJreItem = true;
					
					// remove these two lines once jdk is supported		
					item.setChecked(false);
					item.setGrayed(true);
				}
			} 
			else {				
				TableItem  item = new TableItem(table, SWT.NONE);
				Image image = null;
				String name = jarFileNames.get(i);
				if(name.endsWith(".jar")) {
					image = imageRegistry.get(IMG_JAR);
				} else if (name.endsWith(".zip")) {
					image = imageRegistry.get(IMG_ZIP);
				} else {
					image = imageRegistry.get(IMG_DEFAULT);
					item.setChecked(true);
					item.setGrayed(true);
				}
				item.setImage(image);
				item.setText(name);	
				
				// Set checkboxes based on previously saved settings
				try {
					String check = ((IResource) getElement()).getPersistentProperty(new QualifiedName(PreferenceUtils.pageId, name));
					if (TRUE.equals(check) || item.getImage().equals(imageRegistry.get(IMG_DEFAULT))) {
						item.setChecked(true);
					} else {
						item.setChecked(false);
					}
				} catch (CoreException e) {
					logger.error("Could not get property.", e);
				}
			}
		}
		
		final CheckboxTableViewer checkView = new CheckboxTableViewer(table);		
		checkView.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				for(TableItem item : table.getItems()) {
					if(item.getImage().equals(imageRegistry.get(IMG_DEFAULT))) {
						item.setChecked(true);
						item.setGrayed(true);
					}
					
					//remove this once jdk supported
					if(item.getText().equals(JRE_SYSTEM_LIBRARY)) {
						item.setChecked(false);
						item.setGrayed(true);
					}
				}
			}
		});
		
		table.pack();
		table.update();
		jarTable = table;
	}
	
	@Override
	protected void performDefaults() {
		super.performDefaults();
		if(tabbedFolder.getSelection().length==0) {
			logger.error("Unable to restore defaults on collab " +
			"preference page because no tab selected.");
			return;
		}
		
		TabItem selectedTab = tabbedFolder.getSelection()[0];
		
		if (packageFilterItem.equals(selectedTab)) {
			selectDeselectAllInTree(packgTable.getItems(), true);
			BuildSettings.setDefaultStoredUnselectedProjPackgMap();
		}
	}
	
	/**
	 * When user clicks okay, build the selected jars
	 */
	@Override
	public boolean performOk() {
		boolean result = super.performOk();
		saveCheckedPackages();
	    if (result && isPropertyPage()) {
			IResource resource = (IResource) getElement();
	    	List<URL> mustBeBuilt = new ArrayList<URL>();
	    	List<URL> mustBeRemoved = new ArrayList<URL>();
	    	List<URL> currSelected = new ArrayList<URL>();
	    	
	    	IProject projectRes = ((IResource)element).getProject();
	    	try {
	    		mustBeBuilt.addAll(getURLCollSessionProperty(projectRes, ClassExtensionBuilder.JARS_TO_BUILD));
	    		mustBeRemoved.addAll(getURLCollSessionProperty(projectRes, ClassExtensionBuilder.JARS_TO_REMOVE));
				
	    		String prop = ClassExtensionBuilder.getPersistentProperty(projectRes);
	    		Collection<? extends URL> parsedProp = ClassExtensionBuilder.parsePersistent(prop);
	    		if (parsedProp != null)
	    			currSelected.addAll(parsedProp);
	    	} catch (CoreException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
	    	
	    	
	    	TableItem[] items = jarTable.getItems();
	    	
	    	for(TableItem item : items) {
	    		boolean itemSelected = false;
				String text = item.getText();
				String check = "";
				try {
					check = ((IResource) getElement()).getPersistentProperty(new QualifiedName(PreferenceUtils.pageId, text));
				} catch (CoreException e) {
					logger.error("Could not set property; ", e);
				}
	    		if(item.getChecked() && !item.getGrayed()) {
	    			itemSelected = true;
	    			if(!(TRUE.equals(check))) {
						//Always add items to mustBeAdded/mustBeRemoved since the state of 'check' can change between clicking apply and ok
	    				if(text.equals(JRE_SYSTEM_LIBRARY)) {
	    					for(URL url : jreSysLib) {
	    						mustBeBuilt.add(url);
	    						currSelected.add(url);
	    					}    					
	    				} else {
	    					for(URL url : classPathURLs) {
	    						String texts1 = text + "/";
	    						String texts2 = text + "\\";
	    						if(url.toString().endsWith(text) || url.toString().endsWith(texts1) 
	    								|| url.toString().endsWith(texts2)) {
	    							mustBeBuilt.add(url);
	    							currSelected.add(url);
	    						}
	    					}	    			
	    				}
	    			}
				} else if(!item.getChecked() && !item.getGrayed()) {
					if(TRUE.equals(check)) {
						//Always add items to mustBeAdded/mustBeRemoved since the state of 'check' can change between clicking apply and ok
	    				if(text.equals(JRE_SYSTEM_LIBRARY)) {
	    					for(URL url : jreSysLib) {
	    						mustBeRemoved.add(url);
	    						currSelected.remove(url);
	    					}    					
	    				} else {
	    					for(URL url : classPathURLs) {
	    						String texts1 = text + "/";
	    						String texts2 = text + "\\";
	    						if(url.toString().endsWith(text) || url.toString().endsWith(texts1) 
	    								|| url.toString().endsWith(texts2)) {
	    							mustBeRemoved.add(url);
	    							currSelected.remove(url);
	    						}
	    					}	    			
	    				}
	    			}
				}
				// Save state of checkboxes in project properties
				try {
					if(itemSelected) {
						resource.setPersistentProperty(new QualifiedName(PreferenceUtils.pageId, text), TRUE);
					} else {
						resource.setPersistentProperty(new QualifiedName(PreferenceUtils.pageId, text), FALSE);
					}
				} catch (CoreException e) {
					logger.error("Could not set property; ", e);
				}
	    	}
	    	
	    	try {
	    		IProject project = ((IResource)element).getProject();
	    		project.setSessionProperty(new QualifiedName(ClassExtensionBuilder.JARS_TO_BUILD, ClassExtensionBuilder.JARS_TO_BUILD), mustBeBuilt);
	    		project.setSessionProperty(new QualifiedName(ClassExtensionBuilder.JARS_TO_REMOVE, ClassExtensionBuilder.JARS_TO_REMOVE), mustBeRemoved);
	    		//project.setSessionProperty(new QualifiedName(ClassExtensionBuilder.JARS_SELECTED, ClassExtensionBuilder.JARS_SELECTED), currSelected);
	    		PreferenceUtils.setPersistentProperty(project, currSelected);
	    		//project.touch(null);
	    		project.build(IncrementalProjectBuilder.FULL_BUILD, null);
	    	} catch(Exception e) {
	    		logger.error("Could not build. ", e);
	    	}
	    }
	    return result;
	  }

	private void saveCheckedPackages() {
		IResource resource = (IResource) getElement();
		IProject iProject = resource.getProject();
		IJavaProject project = JavaCore.create(iProject);
		Map<String, String> packagetoSrcFldrMap = new HashMap<String,String>();
		String srcFldrName = null;
		for (TableItem item : packgTable.getItems()) {
			String pckgName = item.getText();
			
			if (item.getText().contains("/")) {
				pckgName = item.getText().substring(item.getText().indexOf("/")+1);
				srcFldrName = item.getText().substring(0, item.getText().indexOf("/"));
			}
			if (!item.getGrayed() && !item.getChecked())
				packagetoSrcFldrMap.put(pckgName,srcFldrName);
		}

		// All packages selected send null
		if (packagetoSrcFldrMap.isEmpty()) {
			BuildSettings.addProjectSettings(project.getElementName(), null);
			return;
		}
		
		// All packages unselected send empty string
		if (packagetoSrcFldrMap.size() == packgTable.getItems().length) {
			BuildSettings.addProjectSettings(project.getElementName(), new HashMap<String,String>());
			return;
		}
		
		// Some packages selected send unselected list
		BuildSettings.addProjectSettings(project.getElementName(), packagetoSrcFldrMap);
	}

	@SuppressWarnings("unchecked")
	private Collection<? extends URL> getURLCollSessionProperty(IProject projectRes, String propName) throws CoreException {
		return (Collection<? extends URL>) projectRes.getSessionProperty(new QualifiedName(propName, propName));	
	}

}