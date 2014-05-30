package com.architexa.intro.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

import com.architexa.intro.AtxaIntroPlugin;


/**
 * @author vineet
 *
 */
public class PreferenceConstants extends AbstractPreferenceInitializer {

    public static final String Java5CheckKey = "Relo.Java5Check5";
    public static final String Java5CheckPrompt = "Check VM Version for Java 5 at Startup";

	//public static final String BuilderCheckConnectionAtStartupKey = "Relo.ConnectionChecked";
	//public static final String BuilderCheckConnectionAtStartupPrompt = "Check Builder Connections at Startup";

//    public static final String LabelItemsWithContextKey = "Relo.LabelItemsWithContext";
    public static final String LabelDetailLevelKey = "LabelDetailLevel";
    //public static final String StretchStrataKey = "StretchStrata";
    public static final String StrataSizeToContentsKey = "StrataSizeToContents";
//    public static final String LabelItemsWithContextPrompt = "Show Context On Labels for Diagrams";
    
    public static final String BUILD_OFF_REMINDER_KEY = "buildOffReminder";
    public static final String BUILD_OFF_REMINDER_CONTEXT = "Alert me if Eclipse Auto Build is turned off";
    
    public static final String TruncateLongMethods = "Chrono.TruncateLongMethods";

    public static final String ShowWhenUpdatesAreAvailableKey = "Relo.ShowWhenUpdatesAreAvailable";
    public static final String ShowWhenUpdatesAreAvailablePrompt = "Alert me via the status bar when Architexa updates are available";
    
    
    ///////////////////////////////
    // Used by the exploration server
    ///////////////////////////////
    
    // group key will need to migrate to ami key or something similar since one such key will apply to multiple machines
    public static final String STORED_GROUP_PREFERENCE_KEY = "StoredGroupKey";
    public static final String STORED_GROUP_PREFERENCE_LABEL = "Enter Group Key: ";

    public static final String WORKSPACE_BASE_PATH_PREFERENCE_KEY = "WorkspaceBasePathKey";
    public static final String WORKSPACE_BASE_PATH_PREFERENCE_LABEL = "Workspace Base Path: ";
    
    
    
    public static boolean showMethodReturnTypes = false;

    
    // RSE OutlineIC Checkbox Prefs
	// stored as a string of binary corresponding to the checkboxes present in
	// the category. By default all options are shown
    public static String FILTERS_ACCESS_LEVEL = "AccessLevelFilters";
	public static String FILTERS_MEMBER_KIND = "MemberKindFilters";
	public static String FILTERS_LIBRARY_CODE = "LibraryCodeFilter";
	
	public static String DEFAULT_FILTERS_ACCESS_LEVEL = "1111";
	public static String DEFAULT_FILTERS_MEMBER_KIND = "1111";
	public static String DEFAULT_FILTERS_LIBRARY_CODE = "1";
    
    
    // Advanced prefs:

    // How library code is shown in diagram
    public static String hide = "Absent";
    public static String showInMenu = "In Menus";
    public static String showInDiagram = "In Diagram";

    public static final String LIBRARY_CODE_STRATA = "libraryCodeStrata";
    public static final String LIBRARY_CODE_RELO = "libraryCodeRelo";
    public static final String LIBRARY_CODE_CHRONO = "libraryCodeChrono";
    
    private static final String DEFAULT_LIBRARY_STRATA = hide;
    private static final String DEFAULT_LIBRARY_RELO = showInDiagram;
    private static final String DEFAULT_LIBRARY_CHRONO = showInDiagram;
	public static final String SHOW_DLG_WHEN_OPENING_NON_JAVA_IN_STRATA = "showDlgWhenOpeningNonJavaItemsInStrata";
	public static final String SHOW_DLG_WHEN_OPENING_NON_JAVA_IN_STRATA_LABEL = "Hide warning shown when attempting to open non-Java Elements";


    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#initializeDefaultPreferences()
     */
    @Override
	public void initializeDefaultPreferences() {
        IPreferenceStore prefStore = AtxaIntroPlugin.getDefault().getPreferenceStore();

        prefStore.setDefault(PreferenceConstants.Java5CheckKey, true);
		//prefStore.setDefault(PreferenceConstants.BuilderCheckConnectionAtStartupKey, true);
        prefStore.setDefault(PreferenceConstants.LabelDetailLevelKey, 0);
        //prefStore.setDefault(PreferenceConstants.StretchStrataKey, false);
        prefStore.setDefault(PreferenceConstants.StrataSizeToContentsKey, false);
        prefStore.setDefault(PreferenceConstants.TruncateLongMethods, true);
        prefStore.setDefault(PreferenceConstants.ShowWhenUpdatesAreAvailableKey, true);
        prefStore.setDefault(PreferenceConstants.BUILD_OFF_REMINDER_KEY, true);

        prefStore.setDefault(FILTERS_ACCESS_LEVEL,DEFAULT_FILTERS_ACCESS_LEVEL);
        prefStore.setDefault(FILTERS_MEMBER_KIND, DEFAULT_FILTERS_MEMBER_KIND);
        prefStore.setDefault(FILTERS_LIBRARY_CODE,DEFAULT_FILTERS_LIBRARY_CODE);
        
        prefStore.setDefault(LIBRARY_CODE_STRATA, DEFAULT_LIBRARY_STRATA);
        prefStore.setDefault(LIBRARY_CODE_RELO, DEFAULT_LIBRARY_RELO);
        prefStore.setDefault(LIBRARY_CODE_CHRONO, DEFAULT_LIBRARY_CHRONO);

        prefStore.setDefault(STORED_GROUP_PREFERENCE_KEY, "");
        prefStore.setDefault(WORKSPACE_BASE_PATH_PREFERENCE_KEY, "");
    }
    
    //private static boolean prefsLoaded = false;

    public static void loadPrefs() {
//    	if (prefsLoaded) return;
    	//prefsLoaded = true;
    	
    	reloadPrefs();
    }
    	
    public static void reloadPrefs() {
    	showMethodReturnTypes = Boolean.valueOf(
    								org.eclipse.jdt.ui.PreferenceConstants.getPreference(
    										org.eclipse.jdt.ui.PreferenceConstants.APPEARANCE_METHOD_RETURNTYPE, 
    										null))
    									.booleanValue();
    }

    protected static IPreferenceStore getPreferenceStore() {
    	return AtxaIntroPlugin.getDefault().getPreferenceStore();
    }
}
