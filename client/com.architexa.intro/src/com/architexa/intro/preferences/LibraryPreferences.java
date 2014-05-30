package com.architexa.intro.preferences;

import org.eclipse.jface.preference.IPreferenceStore;

public class LibraryPreferences {

	public static String getStrataDefaultLibCodeDisplay() {
		return getPreferenceStore().getDefaultString(PreferenceConstants.LIBRARY_CODE_STRATA);
	}

	public static String getReloDefaultLibCodeDisplay() {
		return getPreferenceStore().getDefaultString(PreferenceConstants.LIBRARY_CODE_RELO);
	}

	public static String getChronoDefaultLibCodeDisplay() {
		return getPreferenceStore().getDefaultString(PreferenceConstants.LIBRARY_CODE_CHRONO);
	}

	public static void restoreDefaultLibCodeDisplay() {
		getPreferenceStore().setValue(PreferenceConstants.LIBRARY_CODE_STRATA, getStrataDefaultLibCodeDisplay());
		getPreferenceStore().setValue(PreferenceConstants.LIBRARY_CODE_RELO, getReloDefaultLibCodeDisplay());
		getPreferenceStore().setValue(PreferenceConstants.LIBRARY_CODE_CHRONO, getChronoDefaultLibCodeDisplay());
	}

	public static void setStrataLibraryCodeDisplay(String pref) {
		getPreferenceStore().setValue(PreferenceConstants.LIBRARY_CODE_STRATA, pref);
	}

	public static void setReloLibraryCodeDisplay(String pref) {
		getPreferenceStore().setValue(PreferenceConstants.LIBRARY_CODE_RELO, pref);
	}

	public static void setChronoLibraryCodeDisplay(String pref) {
		getPreferenceStore().setValue(PreferenceConstants.LIBRARY_CODE_CHRONO, pref);
	}

	public static String getStrataLibraryCodeDisplay() {
		return getPreferenceStore().getString(PreferenceConstants.LIBRARY_CODE_STRATA);
	}

	public static String getReloLibraryCodeDisplay() {
		return getPreferenceStore().getString(PreferenceConstants.LIBRARY_CODE_RELO);
	}

	public static String getChronoLibraryCodeDisplay() {
		return getPreferenceStore().getString(PreferenceConstants.LIBRARY_CODE_CHRONO);
	}

	public static boolean isStrataLibCodeHidden() {
		String pref = getPreferenceStore().getString(PreferenceConstants.LIBRARY_CODE_STRATA);
		return PreferenceConstants.hide.equals(pref);
	}

	public static boolean isReloLibCodeHidden() {
		String pref = getPreferenceStore().getString(PreferenceConstants.LIBRARY_CODE_RELO);
		return PreferenceConstants.hide.equals(pref);
	}

	public static boolean isChronoLibCodeHidden() {
		String pref = getPreferenceStore().getString(PreferenceConstants.LIBRARY_CODE_CHRONO);
		return PreferenceConstants.hide.equals(pref);
	}

	public static boolean isStrataLibCodeOnlyInMenu() {
		String pref = getPreferenceStore().getString(PreferenceConstants.LIBRARY_CODE_STRATA);
		return PreferenceConstants.showInMenu.equals(pref);
	}

	public static boolean isReloLibCodeOnlyInMenu() {
		String pref = getPreferenceStore().getString(PreferenceConstants.LIBRARY_CODE_RELO);
		return PreferenceConstants.showInMenu.equals(pref);
	}

	public static boolean isChronoLibCodeOnlyInMenu() {
		String pref = getPreferenceStore().getString(PreferenceConstants.LIBRARY_CODE_CHRONO);
		return PreferenceConstants.showInMenu.equals(pref);
	}

	public static boolean isStrataLibCodeInDiagram() {
		String pref = getPreferenceStore().getString(PreferenceConstants.LIBRARY_CODE_STRATA);
		return PreferenceConstants.showInDiagram.equals(pref);
	}

	public static boolean isReloLibCodeInDiagram() {
		String pref = getPreferenceStore().getString(PreferenceConstants.LIBRARY_CODE_RELO);
		return PreferenceConstants.showInDiagram.equals(pref);
	}

	public static boolean isChronoLibCodeInDiagram() {
		String pref = getPreferenceStore().getString(PreferenceConstants.LIBRARY_CODE_CHRONO);
		return PreferenceConstants.showInDiagram.equals(pref);
	}

	private static IPreferenceStore getPreferenceStore() {
		return PreferenceConstants.getPreferenceStore();
	}

}
