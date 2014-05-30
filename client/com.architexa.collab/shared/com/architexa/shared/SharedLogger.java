package com.architexa.shared;

public class SharedLogger {

	public void info(String string) {
		System.out.println(string);
	}

	public void error(String string) {
		System.err.println(string);
	}

	public void error(String string, Exception exception) {
		System.err.println(string);
		exception.printStackTrace();
	}
}
