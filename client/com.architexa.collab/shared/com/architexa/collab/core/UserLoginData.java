package com.architexa.collab.core;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;


public class UserLoginData {
	private String authEmail = null;
	private String authPassw = null;
	public static boolean testMode = false; 
		
	private static String localMyAtxa = "http://localhost:8082/";
	private static String localCodeMaps = "http://localhost:8081/";
	private static String localDocMaps = "http://localhost:8080/";
	private static final String myArchitexaServerUrl = "http://my.architexa.com/";
	private static final String codeMapsServerUrl = "http://www.codemaps.org/";
	private static final String docMapsServerUrl = "http://codemaps.io/mvn/"; 

	public UserLoginData(String _email, String _passw) {
		authEmail = _email;
		authPassw = _passw;
	}

	public static String getMyArchitexaURL() {
		if (testMode)
			return localMyAtxa;
		return myArchitexaServerUrl;
	}
	
	public static String getHost() {
		if (testMode)
			return localMyAtxa;
		return myArchitexaServerUrl;
	}

	public String getEmail() {
		return authEmail;
	}

	public void setPass(String _passw) {
		authPassw = _passw;
	}

	// TODO Use this only for testing
	// public String getPass(){
	// return authPassw;
	// }

	public Credentials getCredentials() {
		return new UsernamePasswordCredentials(authEmail, authPassw);
	}

	public String getBasicAuthString() {
		return "Basic "
		+ new String(Base64.encodeBase64((authEmail + ":" + authPassw)
				.getBytes()));
	}

	public static String getCodemapsServerUrl() {
		if (testMode)
			return localCodeMaps;
		return codeMapsServerUrl;
	}
	
	public static String getdocMapsServerUrl() {
		if (testMode)
			return localDocMaps;
		return docMapsServerUrl;
	}
}
