package com.ffdc.stats;

import java.util.UUID;
/**
 * 
 * @author Manish Sharma
 *
 */
public class StatsConfig {

	private static StatsConfig instance = new StatsConfig();
	private int maxDataPoints = 100000; 
	 
	 
	private int retentionPerion = 92 * 24 * 60 * 60 * 1000 ; //3 months beyond campaign end date
	
	private  String JVMInstaceInvokationID = UUID.randomUUID().toString();
	private int hitlogProcessingtimeout = 60000; //1 min 
	private int DataCompressionLocktimeout = 3600000; //one hour. We have to be very conservative here
	
	/**
	 * Number of record picked up for prorcesing from hitlog
	 */
	private int  hitProcesBatchSizeLimit  = 500;
	
	public static StatsConfig getInstance() {
			return instance;
	}
	 
	public int getRetentionPerion() {
		return retentionPerion;
	}
	public void setRetentionPerion(int retentionPerion) {
		this.retentionPerion = retentionPerion;
	}


	public int getHitProcesBatchSizeLimit() {
		return hitProcesBatchSizeLimit;
	}


	public void setHitProcesBatchSizeLimit(int hitProcesBatchSizeLimit) {
		this.hitProcesBatchSizeLimit = hitProcesBatchSizeLimit;
	}


	public int getMaxDataPoints() {
		return maxDataPoints;
	}
	public void setMaxDataPoints(int maxDataPoints) {
		this.maxDataPoints = maxDataPoints;
	}



	public String getJVMInstaceInvokationID() {
		return JVMInstaceInvokationID;
	}



	 


	public int getHitlogProcessingtimeout() {
		return hitlogProcessingtimeout;
	}



	public void setHitlogProcessingtimeout(int hitlogProcessingtimeout) {
		this.hitlogProcessingtimeout = hitlogProcessingtimeout;
	}

	public int getDataCompressionLocktimeout() {
		return DataCompressionLocktimeout;
	}

	public void setDataCompressionLocktimeout(int dataCompressionLocktimeout) {
		DataCompressionLocktimeout = dataCompressionLocktimeout;
	}
	
	
}
