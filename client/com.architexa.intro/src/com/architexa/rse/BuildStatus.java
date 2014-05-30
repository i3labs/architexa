package com.architexa.rse;

import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.widgets.Display;

import com.architexa.intro.AtxaIntroPlugin;

public class BuildStatus {
	
    static final Logger logger = AtxaIntroPlugin.getLogger(BuildStatus.class);

    public static IPreferenceStore getPreferenceStore() {
    	return AtxaIntroPlugin.getDefault().getPreferenceStore();
    }

    
	public static void setMenuToDisable() {
		BuildStatus.setClientDisabled(false);
	}
	public static void setMenuToEnable() {
		BuildStatus.setClientDisabled(true);
	}

    
	public static final String ClientDisabled = "ClientDisabled";
    
    public static Boolean clientDisabledBool = null;
	public static void setClientDisabled(boolean disabled) {
		clientDisabledBool = new Boolean(disabled);
		getPreferenceStore().setValue(ClientDisabled, disabled);
	}
	public static boolean getClientDisabled() {
		getPreferenceStore().setDefault(ClientDisabled, false);
		if (clientDisabledBool == null) {
			clientDisabledBool = new Boolean(getPreferenceStore().getBoolean(ClientDisabled));
			if (clientDisabledBool.booleanValue()) BuildStatus.setMenuToEnable();
			else BuildStatus.setMenuToDisable();
			
		}
		return clientDisabledBool.booleanValue();
	}
    

	private static ArrayList<Runnable> runnables = new ArrayList<Runnable>();	

	public static void addRunnable(Runnable runnable) {
		runnables.add(runnable);
	}
	
	public static void runRunnables() {
		for (int i=0; i<BuildStatus.runnables.size(); i++) {
			Display.getDefault().asyncExec(((Runnable)BuildStatus.runnables.get(i)));
		}
		BuildStatus.runnables.clear();
	}
	
	public static void addUsage(String key) {}
	public static void addDiagramType(String id, String action) {}
	public static void updateDiagramActionMap(String id, String action) {}	
	public static void updateDiagramItemMap(String id, int items) {}
	public static void addClientInfo(String key, Integer i) {}
	
}
