package com.ffdc.dto;

import java.util.Date;

import com.ffdc.models.AggregatedData;

/**
 * Aggregated Data in Database and that should be presented to the caller
 * require little adaptation such as date must in human readable format, must
 * have error message, intervals in human readable format The purpose of DTO is
 * do perform the data adaptatin
 * 
 * @author Manish Sharma
 *
 */
public class AggregatedDataDTO {
	/**
	 * Time stamp since 1 jan 1970 GMT
	 */
	public long TimeStampSinceEpoch = -1;
	/**
	 * Human readable timestamp
	 */
	public String TimeStamp = "";
	private String CampaignCode = "";
	private String IntervalInMin = "";
	private String IntervalInHour = "";
	private int Opens = 0;
	private int UniqueOpens = 0;
	private int MobileOpens = 0;
	private int TabOpens = 0;
	private int PCOpens = 0;
	private int Clicks = 0;
	private int UniqueClicks = 0;
	private int MobileClicks = 0;
	private int TabClicks = 0;
	private int PCClicks = 0;
	private String ErrorMessage = "";

	/**
	 * 
	 */
	public AggregatedDataDTO() {

	}

	/**
	 * Convert data from AggregatedData model to this DTO
	 * 
	 * @param data
	 */
	public AggregatedDataDTO(AggregatedData data) {
		TimeStampSinceEpoch = data.getTimeStamp();

		TimeStamp = (new Date(TimeStampSinceEpoch)).toString();

		CampaignCode = data.getId().getCampaignCode();
		IntervalInMin = "" + data.getForInterval() / 1000 / 60;
		IntervalInHour = "" + ((float) data.getForInterval() / 1000) / 3600;
		;
		Opens = data.getOpens();
		UniqueOpens = data.getUniqueOpens();
		MobileOpens = data.getMobileOpens();
		TabOpens = data.getTabOpens();
		PCOpens = data.getNormalOpens();
		Clicks = data.getClicks();
		UniqueClicks = data.getUniqueClicks();
		MobileClicks = data.getMobileClicks();
		TabClicks = data.getTabClicks();
		PCClicks = data.getNormalClicks();

	}

	/**
	 * 
	 * @return
	 */
	public long getTimeStampSinceEpoch() {
		return TimeStampSinceEpoch;
	}

	/**
	 * 
	 * @param timeStampSinceEpoch
	 */
	public void setTimeStampSinceEpoch(long timeStampSinceEpoch) {
		TimeStampSinceEpoch = timeStampSinceEpoch;
	}

	/**
	 * 
	 * @return
	 */
	public String getTimeStamp() {
		return TimeStamp;
	}

	/**
	 * 
	 * @param timeStamp
	 */
	public void setTimeStamp(String timeStamp) {
		TimeStamp = timeStamp;
	}

	/**
	 * 
	 * @return
	 */
	public String getCampaignCode() {
		return CampaignCode;
	}

	/**
	 * 
	 * @param campaignCode
	 */
	public void setCampaignCode(String campaignCode) {
		CampaignCode = campaignCode;
	}

	/**
	 * 
	 * @return
	 */
	public String getIntervalInMin() {
		return IntervalInMin;
	}

	/**
	 * 
	 * @param intervalInMin
	 */
	public void setIntervalInMin(String intervalInMin) {
		IntervalInMin = intervalInMin;
	}

	/**
	 * 
	 */
	public String getIntervalInHour() {
		return IntervalInHour;
	}

	/**
	 * 
	 * @param intervalInHour
	 */
	public void setIntervalInHour(String intervalInHour) {
		IntervalInHour = intervalInHour;
	}

	/**
	 * 
	 * @return
	 */
	public int getOpens() {
		return Opens;
	}

	/**
	 * 
	 * @param opens
	 */
	public void setOpens(int opens) {
		Opens = opens;
	}

	/**
	 * 
	 * @return
	 */
	public int getUniqueOpens() {
		return UniqueOpens;
	}

	/**
	 * 
	 * @param uniqueOpens
	 */
	public void setUniqueOpens(int uniqueOpens) {
		UniqueOpens = uniqueOpens;
	}

	/**
	 * 
	 * @return
	 */
	public int getMobileOpens() {
		return MobileOpens;
	}

	/**
	 * 
	 * @param mobileOpens
	 */
	public void setMobileOpens(int mobileOpens) {
		MobileOpens = mobileOpens;
	}

	/**
	 * 
	 * @return
	 */
	public int getTabOpens() {
		return TabOpens;
	}

	/**
	 * 
	 * @param tabOpens
	 */
	public void setTabOpens(int tabOpens) {
		TabOpens = tabOpens;
	}

	/**
	 * 
	 * @return
	 */
	public int getPCOpens() {
		return PCOpens;
	}

	/**
	 * 
	 * @param PCOpens
	 */
	public void setPCOpens(int PCOpens) {
		this.PCOpens = PCOpens;
	}

	/**
	 * 
	 * @return
	 */
	public int getClicks() {
		return Clicks;
	}

	/**
	 * 
	 * @param clicks
	 */
	public void setClicks(int clicks) {
		Clicks = clicks;
	}

	/**
	 * 
	 * @return
	 */
	public int getUniqueClicks() {
		return UniqueClicks;
	}

	/**
	 * 
	 * @param uniqueClicks
	 */
	public void setUniqueClicks(int uniqueClicks) {
		UniqueClicks = uniqueClicks;
	}

	/**
	 * 
	 * @return
	 */
	public int getMobileClicks() {
		return MobileClicks;
	}

	/**
	 * 
	 * @param mobileClicks
	 */
	public void setMobileClicks(int mobileClicks) {
		MobileClicks = mobileClicks;
	}

	/**
	 * 
	 * @return
	 */
	public int getTabClicks() {
		return TabClicks;
	}

	/**
	 * 
	 * @param tabClicks
	 */
	public void setTabClicks(int tabClicks) {
		TabClicks = tabClicks;
	}

	/**
	 * 
	 * @return
	 */
	public int getPCClicks() {
		return PCClicks;
	}

	/**
	 * 
	 * @param PCClicks
	 */
	public void setPCClicks(int PCClicks) {
		this.PCClicks = PCClicks;
	}

	/**
	 * 
	 * @return
	 */
	public String getErrorMessage() {
		return ErrorMessage;
	}

	/**
	 * 
	 * @param errorMessage
	 */
	public void setErrorMessage(String errorMessage) {
		ErrorMessage = errorMessage;
	}

	/**
	 * 
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((CampaignCode == null) ? 0 : CampaignCode.hashCode());
		result = prime * result + Clicks;
		result = prime * result + ((ErrorMessage == null) ? 0 : ErrorMessage.hashCode());
		result = prime * result + ((IntervalInHour == null) ? 0 : IntervalInHour.hashCode());
		result = prime * result + ((IntervalInMin == null) ? 0 : IntervalInMin.hashCode());
		result = prime * result + MobileClicks;
		result = prime * result + MobileOpens;
		result = prime * result + PCClicks;
		result = prime * result + PCOpens;
		result = prime * result + Opens;
		result = prime * result + TabClicks;
		result = prime * result + TabOpens;
		result = prime * result + ((TimeStamp == null) ? 0 : TimeStamp.hashCode());
		result = prime * result + (int) (TimeStampSinceEpoch ^ (TimeStampSinceEpoch >>> 32));
		result = prime * result + UniqueClicks;
		result = prime * result + UniqueOpens;
		return result;
	}

	/**
	 * 
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AggregatedDataDTO other = (AggregatedDataDTO) obj;
		if (CampaignCode == null) {
			if (other.CampaignCode != null)
				return false;
		} else if (!CampaignCode.equals(other.CampaignCode))
			return false;
		if (Clicks != other.Clicks)
			return false;
		if (ErrorMessage == null) {
			if (other.ErrorMessage != null)
				return false;
		} else if (!ErrorMessage.equals(other.ErrorMessage))
			return false;
		if (IntervalInHour == null) {
			if (other.IntervalInHour != null)
				return false;
		} else if (!IntervalInHour.equals(other.IntervalInHour))
			return false;
		if (IntervalInMin == null) {
			if (other.IntervalInMin != null)
				return false;
		} else if (!IntervalInMin.equals(other.IntervalInMin))
			return false;
		if (MobileClicks != other.MobileClicks)
			return false;
		if (MobileOpens != other.MobileOpens)
			return false;
		if (PCClicks != other.PCClicks)
			return false;
		if (PCOpens != other.PCOpens)
			return false;
		if (Opens != other.Opens)
			return false;
		if (TabClicks != other.TabClicks)
			return false;
		if (TabOpens != other.TabOpens)
			return false;
		if (TimeStamp == null) {
			if (other.TimeStamp != null)
				return false;
		} else if (!TimeStamp.equals(other.TimeStamp))
			return false;
		if (TimeStampSinceEpoch != other.TimeStampSinceEpoch)
			return false;
		if (UniqueClicks != other.UniqueClicks)
			return false;
		if (UniqueOpens != other.UniqueOpens)
			return false;
		return true;
	}

	/**
	 * 
	 */
	@Override
	public String toString() {
		return "AggregatedDataDTO [TimeStampSinceEpoch=" + TimeStampSinceEpoch + ", TimeStamp=" + TimeStamp
				+ ", CampaignCode=" + CampaignCode + ", IntervalInMin=" + IntervalInMin + ", IntervalInHour="
				+ IntervalInHour + ", Opens=" + Opens + ", UniqueOpens=" + UniqueOpens + ", MobileOpens=" + MobileOpens
				+ ", TabOpens=" + TabOpens + ", PCOpens=" + PCOpens + ", Clicks=" + Clicks + ", UniqueClicks="
				+ UniqueClicks + ", MobileClicks=" + MobileClicks + ", TabClicks=" + TabClicks + ", PCClicks="
				+ PCClicks + ", ErrorMessage=" + ErrorMessage + "]";
	}

}
