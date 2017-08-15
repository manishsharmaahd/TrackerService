package com.ffdc.models;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import static javax.persistence.GenerationType.IDENTITY;

/**
 * Represent hitlog table. 
 * When open or click hit, one entry is made in this table
 * @author manish sharma
 *
 */

@Entity
@Table(name = "hitlog")
public class HitLog implements java.io.Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6931829019287525970L;

	private int ID;

	private String campaignCode;
	private String email;
	private String deviceType;
	private int cookieValue;
	private long serverTS;
	private String processingInstanceUID = "";
	private long processingTimoutTime = 0;
	private String derivedClinetIP;
	private String clientIP;
	private String browserFingerPrint;
	private String referrerUrl;
	private int isOpenHit = 1;
	private int processed = 0;

	public HitLog() {

	}

	@Id
	@GeneratedValue(strategy = IDENTITY)
	@Column(name = "ID", unique = true, nullable = false)
	public int getID() {
		return ID;
	}

	public void setID(int ID) {
		this.ID = ID;
	}

	@Column(name = "email", length = 45)
	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	@Column(name = "DeviceType", length = 45)
	public String getDeviceType() {
		return deviceType;
	}

	public void setDeviceType(String deviceType) {
		this.deviceType = deviceType;
	}

	@Column(name = "cookieValue", length = 4)
	public int getCookieValue() {
		return cookieValue;
	}

	public void setCookieValue(int cookieValue) {
		this.cookieValue = cookieValue;
	}

	@Column(name = "serverTS", length = 8)
	public long getServerTS() {
		return serverTS;
	}

	public void setServerTS(long serverTS) {
		this.serverTS = serverTS;
	}

	@Column(name = "DerivedClinetIP", length = 45)
	public String getDerivedClinetIP() {
		return derivedClinetIP;
	}

	public void setDerivedClinetIP(String devrivedClinetIP) {
		this.derivedClinetIP = devrivedClinetIP;
	}

	@Column(name = "ClientIP", length = 8)
	public String getClientIP() {
		return clientIP;
	}

	public void setClientIP(String remoteIP) {
		this.clientIP = remoteIP;
	}

	@Column(name = "BrowserFingerPrint", length = 1024)
	public String getBrowserFingerPrint() {
		return browserFingerPrint;
	}

	public void setBrowserFingerPrint(String browserFingerPrint) {
		this.browserFingerPrint = browserFingerPrint;
	}

	@Column(name = "Processed", length = 2)
	public int getProcessed() {
		return processed;
	}

	public void setProcessed(int processed) {
		this.processed = processed;
	}

	@Column(name = "IsOpenHit", length = 2)
	public int getIsOpenHit() {
		return isOpenHit;
	}

	public void setIsOpenHit(int isOpenHit) {
		this.isOpenHit = isOpenHit;
	}

	@Column(name = "CampaignCode", length = 45)
	public String getCampaignCode() {

		return campaignCode;
	}

	@Override
	public String toString() {
		return "HitLog [ID=" + ID + ", campaignCode=" + campaignCode + ", email=" + email + ", deviceType=" + deviceType
				+ ", cookieValue=" + cookieValue + ", serverTS=" + serverTS + ", processingInstanceUID="
				+ processingInstanceUID + ", processingTimoutTime=" + processingTimoutTime + ", derivedClinetIP="
				+ derivedClinetIP + ", clientIP=" + clientIP + ", browserFingerPrint=" + browserFingerPrint
				+ ", referrerUrl=" + referrerUrl + ", isOpenHit=" + isOpenHit + ", processed=" + processed
				+ ", iSDuplicalte=" + iSDuplicalte + ", expireTimeStamp=" + expireTimeStamp + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ID;
		result = prime * result + ((browserFingerPrint == null) ? 0 : browserFingerPrint.hashCode());
		result = prime * result + ((campaignCode == null) ? 0 : campaignCode.hashCode());
		result = prime * result + ((clientIP == null) ? 0 : clientIP.hashCode());
		result = prime * result + cookieValue;
		result = prime * result + ((derivedClinetIP == null) ? 0 : derivedClinetIP.hashCode());
		result = prime * result + ((deviceType == null) ? 0 : deviceType.hashCode());
		result = prime * result + ((email == null) ? 0 : email.hashCode());
		result = prime * result + (int) (expireTimeStamp ^ (expireTimeStamp >>> 32));
		result = prime * result + iSDuplicalte;
		result = prime * result + isOpenHit;
		result = prime * result + processed;
		result = prime * result + ((processingInstanceUID == null) ? 0 : processingInstanceUID.hashCode());
		result = prime * result + (int) (processingTimoutTime ^ (processingTimoutTime >>> 32));
		result = prime * result + ((referrerUrl == null) ? 0 : referrerUrl.hashCode());
		result = prime * result + (int) (serverTS ^ (serverTS >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		HitLog other = (HitLog) obj;
		if (ID != other.ID)
			return false;
		if (browserFingerPrint == null) {
			if (other.browserFingerPrint != null)
				return false;
		} else if (!browserFingerPrint.equals(other.browserFingerPrint))
			return false;
		if (campaignCode == null) {
			if (other.campaignCode != null)
				return false;
		} else if (!campaignCode.equals(other.campaignCode))
			return false;
		if (clientIP == null) {
			if (other.clientIP != null)
				return false;
		} else if (!clientIP.equals(other.clientIP))
			return false;
		if (cookieValue != other.cookieValue)
			return false;
		if (derivedClinetIP == null) {
			if (other.derivedClinetIP != null)
				return false;
		} else if (!derivedClinetIP.equals(other.derivedClinetIP))
			return false;
		if (deviceType == null) {
			if (other.deviceType != null)
				return false;
		} else if (!deviceType.equals(other.deviceType))
			return false;
		if (email == null) {
			if (other.email != null)
				return false;
		} else if (!email.equals(other.email))
			return false;
		if (expireTimeStamp != other.expireTimeStamp)
			return false;
		if (iSDuplicalte != other.iSDuplicalte)
			return false;
		if (isOpenHit != other.isOpenHit)
			return false;
		if (processed != other.processed)
			return false;
		if (processingInstanceUID == null) {
			if (other.processingInstanceUID != null)
				return false;
		} else if (!processingInstanceUID.equals(other.processingInstanceUID))
			return false;
		if (processingTimoutTime != other.processingTimoutTime)
			return false;
		if (referrerUrl == null) {
			if (other.referrerUrl != null)
				return false;
		} else if (!referrerUrl.equals(other.referrerUrl))
			return false;
		if (serverTS != other.serverTS)
			return false;
		return true;
	}

	public void setCampaignCode(String campaignCode) {
		this.campaignCode = campaignCode;
	}

	@Column(name = "referrerUrl", length = 512)
	public String getReferrerUrl() {
		return referrerUrl;
	}

	public void setReferrerUrl(String referrerUrl) {
		this.referrerUrl = referrerUrl;
	}

	// ISDuplicate NOT NULL default 0, `expireTimeStamp` BIGINT NOT NULL default
	// 0
	private int iSDuplicalte;
	private long expireTimeStamp;

	@Column(name = "ISDuplicate", length = 2)
	public int getiSDuplicalte() {
		return iSDuplicalte;
	}

	public void setiSDuplicalte(int iSDuplicalte) {
		this.iSDuplicalte = iSDuplicalte;
	}

	@Column(name = "ExpireTimeStamp", length = 8)
	public long getExpireTimeStamp() {
		return expireTimeStamp;
	}

	public void setExpireTimeStamp(long expireTimeStamp) {
		this.expireTimeStamp = expireTimeStamp;
	}

	@Column(name = "ProcessingInstanceUID", length = 45)
	public String getProcessingInstanceUID() {
		return processingInstanceUID;
	}

	public void setProcessingInstanceUID(String processingInstanceUID) {
		this.processingInstanceUID = processingInstanceUID;
	}

	@Column(name = "ProcessingTimoutTime", length = 8)

	public long getProcessingTimoutTime() {
		return processingTimoutTime;
	}

	public void setProcessingTimoutTime(long processingTimoutTime) {
		this.processingTimoutTime = processingTimoutTime;
	}

}
