package com.architexa.collab.core;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.ConnectException;
import java.net.SocketException;
import java.net.UnknownHostException;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.AuthenticationException;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.NTCredentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.params.CookiePolicy;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import com.architexa.shared.SharedLogger;


/**
 * Talks to the server via json calls
 * 
 * @author vineet
 */
public class Srvr {
	public static SharedLogger logger = new SharedLogger();

	public static boolean debugMode = false;
	public static final String jsonFmt = "";

	
	
	
	//************************************
	//When an instance is started a respective entry is put in the awsServerData with AWSSRVR_STOPPED status
	//Once the remote machine is up and running and has started eclipse, it sends a message to Codemaps with its
	//PublicDNS and InstanceID. The entry is updated with the PublicDNS and status is changed to AWSSRVR_RUNNING.
	//Now Codemaps send the request to open a diagram and changes the status to AWSSRVR_DIAGRAM_LOADING.
	//Once the diagram is opened in eclipse the remote server sends the message and updates the status to AWSSRVR_DIAGRAM_LOADED
	//Now the explore page is shown to the user
	//************************************
	public static final String AWSSRVR_STOPPED = "stopped";
	public static final String AWSSRVR_RUNNING = "running";
	public static final String AWSSRVR_DIAGRAM_LOADING = "loading";
	public static final String AWSSRVR_DIAGRAM_LOADED = "loaded";
	public static final String AWSSRVR_TYPE_MANAGED = "managed";
	public static final String AWSSRVR_TYPE_UNMANAGED = "unManaged";
	
	public static String get(UserLoginData api, String controllerUrl, String action) throws UnauthorizedException, ConnectException {
		return get(api, controllerUrl + action + jsonFmt);
	}
	public static String get(UserLoginData api, String requestUrl) throws UnauthorizedException, ConnectException {
		return callServer(api, new HttpGet(requestUrl), true);
	}
	public static String get(String requestUrl) throws UnauthorizedException, ConnectException {
		return callServer(null, new HttpGet(requestUrl), false);
	}
	public static void put(UserLoginData api, String controllerUrl, String action, JSONObject jsonParam) throws UnauthorizedException, ConnectException {
		HttpPut method = new HttpPut(controllerUrl + action + jsonFmt);
		setParamAndCallServer(api, method, jsonParam, true);
	}
	public static String post(UserLoginData api, String controllerUrl, String action, JSONObject jsonParam) throws UnauthorizedException, ConnectException {
		HttpPost method = new HttpPost(controllerUrl + action + jsonFmt);
		return setParamAndCallServer(api, method, jsonParam, true);
	}
	public static JSONObject postObj(UserLoginData api, String controllerUrl, String action, JSONObject jsonParam) throws UnauthorizedException, ConnectException {
		return JSONObject.fromObject(post(api, controllerUrl, action, jsonParam));
	}
	
	public static String postObj(String controllerUrl, String action, JSONObject jsonParam) throws ConnectException, UnauthorizedException {
		HttpPost method = new HttpPost(controllerUrl + action + jsonFmt);
		return setParamAndCallServer(null, method, jsonParam, false);
	}
	private static String setParamAndCallServer(UserLoginData api, HttpEntityEnclosingRequestBase method, JSONObject jsonParam, boolean requiresCred) throws UnauthorizedException, ConnectException {
		try {
			StringEntity entity = new StringEntity(jsonParam.toString());
			entity.setContentType("application/json");
			method.setEntity(entity);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (Throwable t){
			System.err.println("Param problem: "+ t.getMessage());
		}
		return callServer(api, method, requiresCred);
	}

	public static JSONObject getObj(UserLoginData api, String controllerUrl, String action) throws UnauthorizedException, ConnectException {
		return JSONObject.fromObject(get(api, controllerUrl, action));
	}
	public static JSONObject getObj(UserLoginData api, String requestUrl) throws UnauthorizedException, ConnectException {
		return JSONObject.fromObject(get(api, requestUrl));
	}
	public static JSONArray getArr(UserLoginData api, String controllerUrl, String action) throws UnauthorizedException, ConnectException {
		return JSONArray.fromObject(get(api, controllerUrl, action));
	}

	public static JSONArray getObj(String requestUrl) throws UnauthorizedException, ConnectException {
		return JSONArray.fromObject(get(requestUrl));
	}
		
	//We don't use this anymore
//	final private static int serverResponseMaxLen = 1024 * 1024; // 1Mb

	private static int DEFAULT_CONNECTION_TIMEOUT = 120000;
	// synchronized - we don't want multiple requests going to the server at the
	// same time (otherwise we get internal server errors)
	private synchronized static String callServer(UserLoginData api, HttpRequestBase methodReq, boolean requiresCred) throws UnauthorizedException, ConnectException, IllegalJsonArgException {
		
		logger.info(methodReq.getMethod() + ": " + methodReq.getURI());

		HttpParams params = getDefaultParams();
		ClientConnectionManager connMngr = getConnectionMngr(params);
		
		DefaultHttpClient httpClient = new DefaultHttpClient(connMngr, params);
		httpClient.getParams().setParameter(ClientPNames.COOKIE_POLICY, CookiePolicy.BROWSER_COMPATIBILITY);
		methodReq.setHeader("Accept", "application/json");
		
		if (injectedProxyDataSourceGenerator != null) 
			setupProxy(httpClient);
			
		if (requiresCred) 
			setAuthentication(httpClient, methodReq, api.getCredentials());
		
		HttpResponse responseStatus = null;
		try {
			if (debugMode)
				System.out.println("*** Requesting - " + methodReq.getMethod() + ": " + methodReq.getURI());
			
			responseStatus = httpClient.execute(methodReq);
			int statusCode = responseStatus.getStatusLine().getStatusCode();
			String statusText = responseStatus.getStatusLine().toString();  
			String response = EntityUtils.toString(responseStatus.getEntity());
			if (debugMode)
				System.out.println("*** Response: >" + response + "<");
			switch (statusCode) {
			case HttpStatus.SC_OK:
			case HttpStatus.SC_CREATED:
				return response;
			case HttpStatus.SC_UNAUTHORIZED:
				throw new UnauthorizedException(statusText + " - " + response);
			case HttpStatus.SC_BAD_REQUEST:
			case HttpStatus.SC_UNPROCESSABLE_ENTITY:
				if (debugMode)
					System.err.println(returnErrStr(statusCode, methodReq, responseStatus, response));
				throw new IllegalJsonArgException(statusCode, response);
			case HttpStatus.SC_INTERNAL_SERVER_ERROR:
				logger.error("Server Error Request: " + methodReq.getMethod() + ": " + methodReq.getURI());
				return null;
			case HttpStatus.SC_NOT_FOUND:
				throw new ConnectException(statusText);
			default:
				logger.error(returnErrStr(statusCode, methodReq, responseStatus, response), new Exception());
				return null;
			}
		} catch (ConnectException e) {
			throw e;
		} catch (UnknownHostException e) {
			throw new ConnectException("UnknownHostException: " + e.getMessage());
		} catch (SocketException e) {
			throw new ConnectException("SocketException: " + e.getMessage());
		} catch (UnauthorizedException e) {
			throw e;
		} catch (IllegalJsonArgException e) {
			throw e;
		} catch (IOException e) {
			logger.error(e.getMessage());
		} catch(Throwable t){
			logger.error(t.getMessage());
		} finally {
				try {
				if (responseStatus != null) {
					HttpEntity entity = responseStatus.getEntity();
					if (entity != null) 
						entity.consumeContent();
				}
				} catch (IOException e) {
					logger.error(e.getMessage());
				}
		}
		return null;
	}

//	Field used to inject the connection manager by the child servers 
	public static Class injectedConnectionManager = null;
	private static ClientConnectionManager getConnectionMngr(HttpParams params) {
		ClientConnectionManager cm = null;
		if (injectedConnectionManager != null) {
			try {
				cm = (ClientConnectionManager) injectedConnectionManager.newInstance();
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		} else { // If no client manager is injected Default child manager is a
					// ThreadSafeClientConnectionManager
			SchemeRegistry schemeRegistry = new SchemeRegistry();
			schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
//			schemeRegistry.register(new Scheme("https", SSLSocketFactory.getSocketFactory(), 443));
			cm = new ThreadSafeClientConnManager(params, schemeRegistry);
		}
		
		return cm;
	}
	
	private static HttpParams getDefaultParams() {
		HttpParams params = new BasicHttpParams();
		
		HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
        HttpProtocolParams.setContentCharset(params, HTTP.DEFAULT_CONTENT_CHARSET);
        // Causes call from child server to parent server to break.
//        HttpProtocolParams.setUseExpectContinue(params, true);
        HttpConnectionParams.setTcpNoDelay(params, true);
        HttpConnectionParams.setSocketBufferSize(params, 8192);

		HttpConnectionParams.setConnectionTimeout(params, DEFAULT_CONNECTION_TIMEOUT);
		HttpConnectionParams.setSoTimeout(params, DEFAULT_CONNECTION_TIMEOUT);
		ConnManagerParams.setTimeout(params, DEFAULT_CONNECTION_TIMEOUT);
		return params;
	}
	
	public static IProxyDataSourceGenerator injectedProxyDataSourceGenerator = null;
	private static void setupProxy(HttpClient httpClient) {
		if (injectedProxyDataSourceGenerator == null) return;
		
		IProxyDataSource proxyDataSource = injectedProxyDataSourceGenerator.getProxyDataSource();
		
		if (proxyDataSource == null || proxyDataSource.getHttpProxyHost() == null) return; //Direct connection
		HttpHost httpProxy = new HttpHost(proxyDataSource.getHttpProxyHost(), proxyDataSource.getHttpProxyPort());
		httpClient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, httpProxy);
		
//		Proxy Authentication
		if (proxyDataSource.getHttpProxyUser() != null && proxyDataSource.getHttpProxyPassword() != null) {
			if (proxyDataSource.getHttpProxyDomain() == null)
				((DefaultHttpClient)httpClient).getCredentialsProvider().setCredentials(new AuthScope(proxyDataSource.getHttpProxyHost(), proxyDataSource.getHttpProxyPort()), new UsernamePasswordCredentials(proxyDataSource.getHttpProxyUser(), proxyDataSource.getHttpProxyPassword()));
			else 
				((DefaultHttpClient)httpClient).getCredentialsProvider().setCredentials(new AuthScope(proxyDataSource.getHttpProxyHost(), proxyDataSource.getHttpProxyPort(), AuthScope.ANY_REALM, "ntlm"), new NTCredentials(proxyDataSource.getHttpProxyUser(), proxyDataSource.getHttpProxyPassword(), proxyDataSource.getHttpProxyHost(), proxyDataSource.getHttpProxyDomain()));
		}
	}
	
	private static void setAuthentication(DefaultHttpClient httpClient, HttpRequest methodReq, Credentials credentials) {
//		AuthScope authScope = new AuthScope("architexa.com", 80, AuthScope.ANY_REALM);
		AuthScope authScope = AuthScope.ANY;
		((DefaultHttpClient)httpClient).getCredentialsProvider().setCredentials(authScope, credentials);
		BasicScheme basicAuth = new BasicScheme();
		try {
			Header authHeader = basicAuth.authenticate(credentials, methodReq);
			methodReq.addHeader(authHeader);
		} catch (AuthenticationException e) {
			e.printStackTrace();
		}
	}
	
	private static String returnErrStr(int statusCode, HttpRequestBase method, HttpResponse httpReponse, String response) throws IOException {
		return "Expected 200 OK. Received " + statusCode + " "
					+ httpReponse.getStatusLine() + "."  
					+ " Request: " + method.getMethod() + ": " + method.getURI() + " , "
					+ " Body: " + response;
	}

}
