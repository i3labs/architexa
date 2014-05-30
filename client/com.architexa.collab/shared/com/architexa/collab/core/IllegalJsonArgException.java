package com.architexa.collab.core;


public class IllegalJsonArgException extends IllegalArgumentException {

	private static final long serialVersionUID = 6843253257110557013L;
	
	private final int httpStatusCode;

	public IllegalJsonArgException(int httpStatusCode, String message) {
		super(message);
		this.httpStatusCode = httpStatusCode;
		
	}

	public int getHttpStatusCode() {
		return httpStatusCode;
	}
	//TODO Remove this???
//	public String getHttpStatusText() {
//		return HttpStatus.getStatusText(httpStatusCode);
//	}
	public String getErrMsg() {
		return getMessage();
	}
	

}
