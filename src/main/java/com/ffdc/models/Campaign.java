package com.ffdc.models;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.ffdc.dto.CampaignDTO;
/**
 * Represents Campaign Table
 * @author LENOVO
 *
 */
@Entity
@Table(name = "Campaign")
public class Campaign implements java.io.Serializable {
	/**
	* 
	*/
	private static final long serialVersionUID = 2605338811925325429L;

	private Long startDateUnixTs;
	private Long endDateUnixTs;
	private String CampaignCode;

	private String ClientName;
	private String Description;
	private String Type;

	private String redirectURL;

	public Campaign() {

	}
 /**
  * Constructor to convert dto to campaign object
  * @param dto
  */
	public Campaign(CampaignDTO dto) {

		ClientName = dto.getClientName();
		Description = dto.getDescription();
		Type = dto.getType();
		CampaignCode = dto.getCampaignCode();
		startDateUnixTs = dto.getStartDate().getTime();
		endDateUnixTs = dto.getEndDate().getTime();

		redirectURL = dto.getRedirectURL();

	}

	public Campaign(String CampaignCode, Long startDate, Long endDate, String clientName, String description,
			String type) {
		super();

		this.CampaignCode = CampaignCode;
		this.startDateUnixTs = startDate;
		this.endDateUnixTs = endDate;
		ClientName = clientName;
		Description = description;
		Type = type;

	}

	@Id
	@Column(name = "CampaignCode", length = 45)
	public String getCampaignCode() {
		return CampaignCode;
	}

	// @Temporal(TemporalType.TIMESTAMP)
	@Column(name = "StartDateUnixTs")
	public Long getStartDateUnixTs() {
		return startDateUnixTs;
	}

	public void setStartDateUnixTs(long startDateUnixTs) {
		this.startDateUnixTs = startDateUnixTs;
	}

	// @Temporal(TemporalType.TIMESTAMP)
	@Column(name = "EndDateUnixTs", length = 8)
	public Long getEndDateUnixTs() {
		return endDateUnixTs;
	}

	public void setEndDateUnixTs(Long endDateUnixTs) {
		this.endDateUnixTs = endDateUnixTs;
	}

	@Column(name = "ClinentName", length = 45)
	public String getClientName() {
		return ClientName;
	}

	public void setClientName(String clientName) {
		ClientName = clientName;
	}

	@Column(name = "Description", length = 45)
	public String getDescription() {
		return Description;
	}

	public void setDescription(String description) {
		Description = description;
	}

	@Column(name = "Type", length = 45)
	public String getType() {
		return Type;
	}

	public void setType(String type) {
		Type = type;
	}

	public void setCampaignCode(String campaignCode) {
		CampaignCode = campaignCode;
	}

	@Column(name = "redirectURL", length = 128)
	public String getRedirectURL() {
		return redirectURL;
	}

	@Override
	public String toString() {
		return "Campaign [startDateUnixTs=" + startDateUnixTs + ", endDateUnixTs=" + endDateUnixTs + ", CampaignCode="
				+ CampaignCode + ", ClientName=" + ClientName + ", Description=" + Description + ", Type=" + Type
				+ ", redirectURL=" + redirectURL + "]";
	}
	public void setRedirectURL(String redirectURL) {
		this.redirectURL = redirectURL;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((CampaignCode == null) ? 0 : CampaignCode.hashCode());
		result = prime * result + ((ClientName == null) ? 0 : ClientName.hashCode());
		result = prime * result + ((Description == null) ? 0 : Description.hashCode());
		result = prime * result + ((Type == null) ? 0 : Type.hashCode());
		result = prime * result + ((endDateUnixTs == null) ? 0 : endDateUnixTs.hashCode());
		result = prime * result + ((redirectURL == null) ? 0 : redirectURL.hashCode());
		result = prime * result + ((startDateUnixTs == null) ? 0 : startDateUnixTs.hashCode());
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
		Campaign other = (Campaign) obj;
		if (CampaignCode == null) {
			if (other.CampaignCode != null)
				return false;
		} else if (!CampaignCode.equals(other.CampaignCode))
			return false;
		if (ClientName == null) {
			if (other.ClientName != null)
				return false;
		} else if (!ClientName.equals(other.ClientName))
			return false;
		if (Description == null) {
			if (other.Description != null)
				return false;
		} else if (!Description.equals(other.Description))
			return false;
		if (Type == null) {
			if (other.Type != null)
				return false;
		} else if (!Type.equals(other.Type))
			return false;
		if (endDateUnixTs == null) {
			if (other.endDateUnixTs != null)
				return false;
		} else if (!endDateUnixTs.equals(other.endDateUnixTs))
			return false;
		if (redirectURL == null) {
			if (other.redirectURL != null)
				return false;
		} else if (!redirectURL.equals(other.redirectURL))
			return false;
		if (startDateUnixTs == null) {
			if (other.startDateUnixTs != null)
				return false;
		} else if (!startDateUnixTs.equals(other.startDateUnixTs))
			return false;
		return true;
	}

}
