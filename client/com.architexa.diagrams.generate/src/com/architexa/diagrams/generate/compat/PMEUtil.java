package com.architexa.diagrams.generate.compat;

/*
 * Deal with compatibility issue from PopupMenuExtender class
 */

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.eclipse.jface.action.MenuManager;
import org.eclipse.team.internal.ui.Utils;
import org.eclipse.ui.internal.PopupMenuExtender;

import com.architexa.collab.proxy.PluginUtils;


public class PMEUtil {
	
	private static double jdtUIVer = PluginUtils.getPluginVer("org.eclipse.jdt.ui");
	
	//Version 3.3+ includes getManager() method in PopupMenuExtender class
	//Version 3.2 No such access method, desired accessed variable named "menu"
	public static MenuManager getManager(PopupMenuExtender pm) {
		
		//3.3 and above supports
		if (jdtUIVer >= 3.3 ){
			//get the method
			try {
				Method mth = PopupMenuExtender.class.getMethod("getManager");
				return (MenuManager) mth.invoke(pm);
			} catch (Exception e) {
				System.err.println("Issue stemming from method getManager in PopupMenuEvent" + e);
			} 
		}
		
		MenuManager theManager = null;
		
		//Below suited for eclipse 3.2
		try{
			Field theMenu = PopupMenuExtender.class.getDeclaredField("menu");
			theMenu.setAccessible(true);
			
			theManager = (MenuManager) theMenu.get(pm);
		}catch(Exception e){
			System.err.println("Issue with menu variable in PopupMenuExtender class: " + e);
		}
		return theManager;
	}
}
