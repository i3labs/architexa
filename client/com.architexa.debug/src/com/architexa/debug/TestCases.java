package com.architexa.debug;

import java.lang.reflect.Method;

public class TestCases {

	
	public static boolean testOICMenus(Method m, Object obj, Object args) {
		try {
			for (int i=0; i<100; i++) {
				m.invoke(obj, args);
			}
		} catch (Throwable t) {
			return false;
		}
		
		return true;
	}
	
}
