package com.ffdc.models;
//Generated 25 Apr, 2017 11:41:45 AM by Hibernate Tools 5.2.1.Final

import javax.persistence.Column;
import javax.persistence.Embeddable;

/**
 * Primary key for AggregatedData table
 * 
 * @author manish sharma
 *
 */
@Embeddable
public class AggregatedDataId implements java.io.Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7728200177335341897L;
	private long timeStamp;
	private String campaignCode;

	public AggregatedDataId() {
	}

	public AggregatedDataId(long timeStamp, String campaignCode) {
		super();
		this.timeStamp = timeStamp;
		this.campaignCode = campaignCode;
	}

	@Column(name = "TimeStamp", nullable = false)
	public long getTimeStamp() {
		return timeStamp;
	}

	public void setTimeStamp(long timeStamp) {
		this.timeStamp = timeStamp;
	}

	@Column(name = "CampaignCode", nullable = false, length = 45)
	public String getCampaignCode() {
		return campaignCode;
	}

	public void setCampaignCode(String campaignCode) {
		this.campaignCode = campaignCode;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((campaignCode == null) ? 0 : campaignCode.hashCode());
		result = prime * result + (int) (timeStamp ^ (timeStamp >>> 32));
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
		AggregatedDataId other = (AggregatedDataId) obj;
		if (campaignCode == null) {
			if (other.campaignCode != null)
				return false;
		} else if (!campaignCode.equals(other.campaignCode))
			return false;
		if (timeStamp != other.timeStamp)
			return false;
		return true;
	}

}
