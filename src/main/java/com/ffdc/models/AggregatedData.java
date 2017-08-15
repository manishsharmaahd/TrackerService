package com.ffdc.models;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;

import javax.persistence.Table;
import javax.persistence.Transient;

/**
 * Represents AggregatedData Table
 * 
 * @author manish sharma
 *
 */

@Entity
@Table(name = "AggregatedData")
public class AggregatedData implements java.io.Serializable {

	@Override
	public String toString() {
		return "AggregatedData [id=" + id + ", opens=" + opens + ", uniqueOpens=" + uniqueOpens + ", mobileOpens="
				+ mobileOpens + ", tabOpens=" + tabOpens + ", normalOpens=" + normalOpens + ", clicks=" + clicks
				+ ", uniqueClicks=" + uniqueClicks + ", mobileClicks=" + mobileClicks + ", tabClicks=" + tabClicks
				+ ", normalClicks=" + normalClicks + ", expiryInTimeStamp=" + expiryInTimeStamp + ", forInterval="
				+ forInterval + ", timeStatmpStringForTest=" + timeStatmpStringForTest + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + clicks;
		result = prime * result + (int) (expiryInTimeStamp ^ (expiryInTimeStamp >>> 32));
		result = prime * result + forInterval;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + mobileClicks;
		result = prime * result + mobileOpens;
		result = prime * result + normalClicks;
		result = prime * result + normalOpens;
		result = prime * result + opens;
		result = prime * result + tabClicks;
		result = prime * result + tabOpens;
		result = prime * result + ((timeStatmpStringForTest == null) ? 0 : timeStatmpStringForTest.hashCode());
		result = prime * result + uniqueClicks;
		result = prime * result + uniqueOpens;
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
		AggregatedData other = (AggregatedData) obj;
		if (clicks != other.clicks)
			return false;
		if (expiryInTimeStamp != other.expiryInTimeStamp)
			return false;
		if (forInterval != other.forInterval)
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (mobileClicks != other.mobileClicks)
			return false;
		if (mobileOpens != other.mobileOpens)
			return false;
		if (normalClicks != other.normalClicks)
			return false;
		if (normalOpens != other.normalOpens)
			return false;
		if (opens != other.opens)
			return false;
		if (tabClicks != other.tabClicks)
			return false;
		if (tabOpens != other.tabOpens)
			return false;
		if (timeStatmpStringForTest == null) {
			if (other.timeStatmpStringForTest != null)
				return false;
		} else if (!timeStatmpStringForTest.equals(other.timeStatmpStringForTest))
			return false;
		if (uniqueClicks != other.uniqueClicks)
			return false;
		if (uniqueOpens != other.uniqueOpens)
			return false;
		return true;
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 3546834548967337866L;
	private AggregatedDataId id;
	private int opens = 0;
	private int uniqueOpens = 0;
	private int mobileOpens = 0;
	private int tabOpens = 0;
	private int normalOpens = 0;
	private int clicks = 0;

	private int uniqueClicks = 0;
	private int mobileClicks = 0;
	private int tabClicks = 0;
	private int normalClicks = 0;
	private long expiryInTimeStamp = 0;
	private int forInterval = 300000;
	private String timeStatmpStringForTest;

	public AggregatedData() {
	}

	@Transient
	public long getTimeStamp() {
		return id != null ? id.getTimeStamp() : -1;
	}

	@EmbeddedId
	@AttributeOverrides({
			@AttributeOverride(name = "timeStamp", column = @Column(name = "TimeStamp", nullable = false)),
			@AttributeOverride(name = "campaignCode", column = @Column(name = "campaignCode", nullable = false, length = 45)) })
	public AggregatedDataId getId() {
		return this.id;
	}

	public void setId(AggregatedDataId id) {
		this.id = id;
	}

	@Column(name = "Opens", length = 2)
	public int getOpens() {
		return opens;
	}

	public void setOpens(int opens) {
		this.opens = opens;
	}

	@Column(name = "UniqueOpens", length = 2)
	public int getUniqueOpens() {
		return uniqueOpens;
	}

	public void setUniqueOpens(int uniqueOpens) {
		this.uniqueOpens = uniqueOpens;
	}

	@Column(name = "MobileOpens", length = 2)
	public int getMobileOpens() {
		return mobileOpens;
	}

	public void setMobileOpens(int mobileOpens) {
		this.mobileOpens = mobileOpens;
	}

	@Column(name = "TabOpens", length = 2)
	public int getTabOpens() {
		return tabOpens;
	}

	public void setTabOpens(int tabOpens) {
		this.tabOpens = tabOpens;
	}

	@Column(name = "NormalOpens", length = 2)
	public int getNormalOpens() {
		return normalOpens;
	}

	public void setNormalOpens(int normalOpens) {
		this.normalOpens = normalOpens;
	}

	@Column(name = "Clicks", length = 2)
	public int getClicks() {
		return clicks;
	}

	public void setClicks(int clicks) {
		this.clicks = clicks;
	}

	@Column(name = "UniqueClicks", length = 2)
	public int getUniqueClicks() {
		return uniqueClicks;
	}

	public void setUniqueClicks(int uniqueClicks) {
		this.uniqueClicks = uniqueClicks;
	}

	@Column(name = "MobileClicks", length = 2)
	public int getMobileClicks() {
		return mobileClicks;
	}

	public void setMobileClicks(int mobileClicks) {
		this.mobileClicks = mobileClicks;
	}

	@Column(name = "TabClicks", length = 2)
	public int getTabClicks() {
		return tabClicks;
	}

	public void setTabClicks(int tabClicks) {
		this.tabClicks = tabClicks;
	}

	@Column(name = "NormalClicks", length = 2)
	public int getNormalClicks() {
		return normalClicks;
	}

	public void setNormalClicks(int normalClicks) {
		this.normalClicks = normalClicks;
	}

	@Column(name = "ExpiryInTimeStamp", length = 8)
	public long getExpiryInTimeStamp() {
		return expiryInTimeStamp;
	}

	public void setExpiryInTimeStamp(long expiryInTimeStamp) {
		this.expiryInTimeStamp = expiryInTimeStamp;
	}

	@Column(name = "ForInterval", length = 2)
	public int getForInterval() {
		return forInterval;
	}

	public void setForInterval(int forInterval) {
		this.forInterval = forInterval;
	}

	@Column(name = "TimeStatmpStringForTest", length = 45)
	public String getTimeStatmpStringForTest() {
		return timeStatmpStringForTest;
	}

	public void setTimeStatmpStringForTest(String timeStatmpStringForTest) {
		this.timeStatmpStringForTest = timeStatmpStringForTest;
	}

}
