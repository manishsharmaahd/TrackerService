package com.ffdc.utility;
/**
 * Defines constants used across applicaiton
 * @author manish sharma
 *
 */
public class Constants {
	public static final String None = "None";
	public static final String CAMPAIGN_EMAIL = "Email";
	public static final String CAMPAIGN_WEB = "Web";
	//public static final String  TokenDelimiter= "||@";
	public static final String  TokenDelimiter= "#%##";
	public static final String  DeviceTypeMobile = "Mobile";
	public static final String  DeviceTypeTablet = "Tablet";
	public static final String  DeviceTypePC = "PC";
	
	// configuration for BadIPScreener
	public static long SAFE_COUNT_TIME_INTERVAL = 300 * 1000; // 5 minutes
	public static int MAX_SAFE_COUNT = 500;
	
	// Status of Hitlog record
	public static int HIT_RECORD_PROCESSING = 3;
	public static int HIT_RECORD_UNPROCESSED = 0 ;
	public static int HIT_RECORD_PROCESSED = 2 ;
    
	//For how long afer campaign expiry date, campaign is considerd to be active
	public static int ActiveGracePeriod = 120 * 86400 * 100 ; //60 days
	
	public static int DAEMON_POLL_PERIOD = 500;
}
