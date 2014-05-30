package com.architexa.collab.core;

public interface IProxyDataSource {

	public String getHttpProxyHost();
	public int getHttpProxyPort();
	public String getHttpProxyUser();
	public String getHttpProxyPassword();
	public String getHttpProxyDomain();
}
