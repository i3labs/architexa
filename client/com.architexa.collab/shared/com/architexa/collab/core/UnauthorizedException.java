package com.architexa.collab.core;

public class UnauthorizedException extends Exception {
	public UnauthorizedException(String msg) {
		super(msg);
	}
	public UnauthorizedException(String msg, Throwable cause) {
		super(msg, cause);
	}

	private static final long serialVersionUID = -5095319907841066969L;

}
