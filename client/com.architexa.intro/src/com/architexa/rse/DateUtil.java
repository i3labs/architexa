package com.architexa.rse;

import java.util.Date;

import org.apache.commons.codec.binary.Base64;

public class DateUtil {

	public static Date getLicenseValidity(String licenseKey) {
		String[] parts = licenseKey.split("::");
		if (parts.length != 2 || !parts[0].equals("atxa")) return null;
		
		String newPass = new String (Base64.decodeBase64((parts[1]).getBytes()));
		String[] passParts = newPass.split("::");
		
		long expiry = Long.parseLong(passParts[2]);
		return new Date(expiry);
	}
	
	public static String getLicenseValidityStr(String license) {
		Date date = getLicenseValidity(license);
		if (date == null) return "";
		return getGapInHoursAndDays(date);
	}
	
	public static Date now() {
		return new Date();
	}
	
	public static boolean isExpired(String genPassword) {
		Date expireDate = getLicenseValidity(genPassword);
		if (now().getTime() > expireDate.getTime())
			return true;
		return false;
	}
	
	public static String getGapInHoursAndDays(Date futureEvent) {
		long timeToEvent = futureEvent.getTime() - now().getTime();
		int hoursToEvent = (int) (timeToEvent/1000/60/60);
		int daysToExpire = hoursToEvent/24;

		String retVal = daysToExpire + " days";
		hoursToEvent -= daysToExpire*24;
		if (hoursToEvent > 0)
			retVal += " " + hoursToEvent + " hours";
		return retVal;
	}
}
