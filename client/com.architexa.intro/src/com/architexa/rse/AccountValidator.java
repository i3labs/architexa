package com.architexa.rse;

public abstract class AccountValidator {
	
	/**
	 * @param forceCheck - by default a validation will only happen once every 24 hours, however it is not currently implemented  
	 */
	public abstract void validateLicense(boolean forceCheck);
}
