package com.ffdc.dto;

import java.util.Date;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.ffdc.DataAccesObjects.CampaignDAO;
import com.ffdc.models.Campaign;
import com.ffdc.utility.Utility;

/**
 * Object to hold various parameters of Stats data query
 * 
 * @author Manish Sharma
 *
 */
public class StatsQueryDTO {

	String campaignCode;
	String suggestedIntervalMin = null;
	String suggestedIntervalHours = null;
	Date startTime;
	Date endTime;

	@JsonIgnore
	int intervalMininMillis = 0;
	@JsonIgnore
	int intervalHourinMillis = 0;

	/**
	 * Validation that should be done before issuing query
	 */
	public void validate() {
		if ((startTime == null)) {
			throw new IllegalArgumentException(
					"Invalid Start date format. It should be  format (2017-06-09 23:23:23)   ");
		}
		if ((endTime == null)) {
			throw new IllegalArgumentException(
					"Invalid Start date format. It should be  format (2017-06-09 23:23:23) ");
		}
		if (startTime.compareTo(endTime) > -1) {

			throw new IllegalArgumentException("Start date should be before end date");
		}
		if (campaignCode == null || campaignCode.isEmpty()) {
			throw new IllegalArgumentException("campaign code is required argument");
		}

		if ((suggestedIntervalMin == null || suggestedIntervalMin.isEmpty())
				&& (suggestedIntervalHours == null || suggestedIntervalHours.isEmpty())) {
			throw new IllegalArgumentException("Either interval minute or interval hour has to be present");

		}

		if (suggestedIntervalMin != null && !suggestedIntervalMin.isEmpty()) {
			intervalMininMillis = Utility.tryParseInt(suggestedIntervalMin) * 60 * 1000;
			if (intervalMininMillis == 0)
				throw new IllegalArgumentException("Interval Min has to be numeric");

		}
		if (suggestedIntervalHours != null && !suggestedIntervalHours.isEmpty()) {
			intervalHourinMillis = Utility.tryParseInt(suggestedIntervalMin) * 60 * 1000;
			if (intervalHourinMillis == 0)
				throw new IllegalArgumentException("Interval Min has to be numeric");

		}

		CampaignDAO cdao = new CampaignDAO();
		Map<String, Campaign> map = cdao.findAllActiveCampaings();
		if (map.get(campaignCode) == null)
			throw new IllegalArgumentException("Inactive/Invalid Campaigncode");

	}

	/**
	 * 
	 * @return
	 */
	public String getCampaignCode() {
		return campaignCode;
	}

	/**
	 * 
	 * @param campaignCode
	 */
	public void setCampaignCode(String campaignCode) {
		this.campaignCode = campaignCode;
	}

	/**
	 * 
	 * @return
	 */
	public Date getStartTime() {
		return startTime;
	}

	/**
	 * 
	 * @param startTime
	 */
	public void setStartTime(String startTime) {

		this.startTime = Utility.parseTime(startTime);

	}

	/**
	 * 
	 * @return
	 */
	public Date getEndTime() {
		return this.endTime;
	}

	/**
	 * 
	 * @param endTime
	 */
	public void setEndTime(String endTime) {
		this.endTime = Utility.parseTime(endTime);
	}

	/**
	 * 
	 * @return
	 */
	public String getSuggestedIntervalMin() {
		return suggestedIntervalMin;
	}

	/**
	 * 
	 * @param suggestedIntervalMin
	 */
	public void setSuggestedIntervalMin(String suggestedIntervalMin) {
		this.suggestedIntervalMin = suggestedIntervalMin;
	}

	/**
	 * 
	 * @return
	 */
	public String getSuggestedIntervalHours() {
		return suggestedIntervalHours;
	}

	/**
	 * 
	 * @param suggestedIntervalHours
	 */
	public void setSuggestedIntervalHours(String suggestedIntervalHours) {
		this.suggestedIntervalHours = suggestedIntervalHours;
	}

	/**
	 * 
	 * @return
	 */
	public int getIntervalMininMillis() {
		return intervalMininMillis;
	}

	/**
	 * 
	 * @param intervalMininMillis
	 */
	public void setIntervalMininMillis(int intervalMininMillis) {
		this.intervalMininMillis = intervalMininMillis;
	}

	/**
	 * 
	 * @return
	 */
	public int getIntervalHourinMillis() {
		return intervalHourinMillis;
	}

	/**
	 * 
	 * @param intervalHourinMillis
	 */
	public void setIntervalHourinMillis(int intervalHourinMillis) {
		this.intervalHourinMillis = intervalHourinMillis;
	}

	/**
	 * 
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((campaignCode == null) ? 0 : campaignCode.hashCode());
		result = prime * result + ((endTime == null) ? 0 : endTime.hashCode());
		result = prime * result + intervalHourinMillis;
		result = prime * result + intervalMininMillis;
		result = prime * result + ((startTime == null) ? 0 : startTime.hashCode());
		result = prime * result + ((suggestedIntervalHours == null) ? 0 : suggestedIntervalHours.hashCode());
		result = prime * result + ((suggestedIntervalMin == null) ? 0 : suggestedIntervalMin.hashCode());
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
		StatsQueryDTO other = (StatsQueryDTO) obj;
		if (campaignCode == null) {
			if (other.campaignCode != null)
				return false;
		} else if (!campaignCode.equals(other.campaignCode))
			return false;
		if (endTime == null) {
			if (other.endTime != null)
				return false;
		} else if (!endTime.equals(other.endTime))
			return false;
		if (intervalHourinMillis != other.intervalHourinMillis)
			return false;
		if (intervalMininMillis != other.intervalMininMillis)
			return false;
		if (startTime == null) {
			if (other.startTime != null)
				return false;
		} else if (!startTime.equals(other.startTime))
			return false;
		if (suggestedIntervalHours == null) {
			if (other.suggestedIntervalHours != null)
				return false;
		} else if (!suggestedIntervalHours.equals(other.suggestedIntervalHours))
			return false;
		if (suggestedIntervalMin == null) {
			if (other.suggestedIntervalMin != null)
				return false;
		} else if (!suggestedIntervalMin.equals(other.suggestedIntervalMin))
			return false;
		return true;
	}

	/**
	 * 
	 */
	@Override
	public String toString() {
		return "StatsQueryDTO [campaignCode=" + campaignCode + ", suggestedIntervalMin=" + suggestedIntervalMin
				+ ", suggestedIntervalHours=" + suggestedIntervalHours + ", startTime=" + startTime + ", endTime="
				+ endTime + ", intervalMininMillis=" + intervalMininMillis + ", intervalHourinMillis="
				+ intervalHourinMillis + "]";
	}

}
