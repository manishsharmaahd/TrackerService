package com.ffdc.dto;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ffdc.models.Campaign;
import com.ffdc.utility.Constants;

/**
 * This class has been created primarily to convert unix format dates to human
 * readable while presenting to the user Secondly, we don't want to expose
 * Statistics data such as token generated and used etc when campaing are being
 * created or saved.
 * 
 * @author Manish Sharma
 */
public class CampaignDTO {

	 
	@SuppressWarnings("unused")
	private static final long serialVersionUID = 2605338811925325429L;

	@JsonFormat(pattern = "dd/MM/yyyy hh:mm:ss")
	private Date startDate = new Date();
	@JsonFormat(pattern = "dd/MM/yyyy hh:mm:ss")
	private Date endDate = new Date();
	private String clientName;
	private String description;
	private String type;
	private String campaignCode;

	private String redirectURL;

	private String errorMessage = Constants.None;

	/**
	 * 
	 */
	public CampaignDTO() {
	}

	/**
	 * Adapts Campaign model to DTO
	 * 
	 * @param d
	 */
	public CampaignDTO(Campaign d) {

		startDate = new Date(d.getStartDateUnixTs());
		endDate = new Date(d.getEndDateUnixTs());
		clientName = d.getClientName();
		this.campaignCode = d.getCampaignCode();
		description = d.getDescription();
		type = d.getType();
		redirectURL = d.getRedirectURL();

	}

	/**
	 * Performs validation that must be done save /update of campaing
	 */
	public void validate() {
		if ((startDate == null)) {
			throw new IllegalArgumentException("Start date can't be null   ");
		}
		if ((endDate == null) || (endDate.compareTo(new Date()) < 0)) {
			throw new IllegalArgumentException("end date can't be null or must be greater than current date");
		}
		if (startDate.compareTo(endDate) > -1) {

			throw new IllegalArgumentException("Start date should be before end date");
		}
		if (campaignCode == null || campaignCode.isEmpty()) {
			throw new IllegalArgumentException("campaign code is required argument");
		}

		if (description == null || description.isEmpty()) {
			throw new IllegalArgumentException("description  is required argument");
		}
		if (clientName == null || clientName.isEmpty()) {
			throw new IllegalArgumentException("Client Name  is required argument");
		}
		if (type == null || type.isEmpty()) {
			throw new IllegalArgumentException("campaign type  is required argument");
		}
		if (!type.equals(Constants.CAMPAIGN_EMAIL) && !type.equals(Constants.CAMPAIGN_WEB)) {
			throw new IllegalArgumentException(
					"Valid value for campaign are" + Constants.CAMPAIGN_EMAIL + " and " + Constants.CAMPAIGN_WEB);
		}
	}

	/**
	 * 
	 * @return
	 */
	public final Date getStartDate() {
		return startDate;
	}

	/**
	 * 
	 * @param startDate
	 */
	public final void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	/**
	 * 
	 * @return
	 */
	public final Date getEndDate() {
		return endDate;
	}

	/**
	 * 
	 * @param endDate
	 */
	public final void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

	/**
	 * 
	 * @return
	 */
	public final String getClientName() {
		return clientName;
	}

	/**
	 * 
	 * @param clientName
	 */
	public final void setClientName(String clientName) {
		this.clientName = clientName;
	}

	/**
	 * 
	 * @return
	 */
	public final String getDescription() {
		return description;
	}

	/**
	 * 
	 * @param description
	 */
	public final void setDescription(String description) {
		this.description = description;
	}

	/**
	 * 
	 * @return
	 */
	public final String getType() {
		return type;
	}

	/**
	 * 
	 * @param type
	 */
	public final void setType(String type) {
		this.type = type;
	}

	/**
	 * 
	 * @return
	 */
	public final String getCampaignCode() {
		return campaignCode;
	}

	/**
	 * 
	 * @param campaignCode
	 */
	public final void setCampaignCode(String campaignCode) {
		this.campaignCode = campaignCode;
	}

	/**
	 * 
	 * @return
	 */
	public final String getErrorMessage() {
		return errorMessage;
	}

	/**
	 * 
	 * @param errorMessage
	 */
	public final void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

	/**
	 * 
	 * @return
	 */
	public String getRedirectURL() {
		return redirectURL;
	}

	/**
	 * 
	 * @param redirectURL
	 */
	public void setRedirectURL(String redirectURL) {
		this.redirectURL = redirectURL;
	}

	/**
	 * 
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((campaignCode == null) ? 0 : campaignCode.hashCode());
		result = prime * result + ((clientName == null) ? 0 : clientName.hashCode());
		result = prime * result + ((description == null) ? 0 : description.hashCode());
		result = prime * result + ((endDate == null) ? 0 : endDate.hashCode());
		result = prime * result + ((errorMessage == null) ? 0 : errorMessage.hashCode());
		result = prime * result + ((redirectURL == null) ? 0 : redirectURL.hashCode());
		result = prime * result + ((startDate == null) ? 0 : startDate.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
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
		CampaignDTO other = (CampaignDTO) obj;
		if (campaignCode == null) {
			if (other.campaignCode != null)
				return false;
		} else if (!campaignCode.equals(other.campaignCode))
			return false;
		if (clientName == null) {
			if (other.clientName != null)
				return false;
		} else if (!clientName.equals(other.clientName))
			return false;
		if (description == null) {
			if (other.description != null)
				return false;
		} else if (!description.equals(other.description))
			return false;
		if (endDate == null) {
			if (other.endDate != null)
				return false;
		} else if (!endDate.equals(other.endDate))
			return false;
		if (errorMessage == null) {
			if (other.errorMessage != null)
				return false;
		} else if (!errorMessage.equals(other.errorMessage))
			return false;
		if (redirectURL == null) {
			if (other.redirectURL != null)
				return false;
		} else if (!redirectURL.equals(other.redirectURL))
			return false;
		if (startDate == null) {
			if (other.startDate != null)
				return false;
		} else if (!startDate.equals(other.startDate))
			return false;
		if (type == null) {
			if (other.type != null)
				return false;
		} else if (!type.equals(other.type))
			return false;
		return true;
	}

}
