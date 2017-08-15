package com.ffdc.models;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Represent Data Cmoression table. This table is used to acquire lock and get
 * last process timestamp
 * 
 * @author Manish Sharma
 *
 */

@Entity
@Table(name = "DataCompressionLock")
public class DataCompressionLock implements java.io.Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -290848150585061256L;

	private int type;

	private String invocatinInstanceId;

	private long timeStamp;

	private long AcquireTimeStamp;

	public DataCompressionLock() {

	}

	@Id
	@Column(name = "Type", unique = true, nullable = false)
	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	@Column(name = "TimeStamp", length = 8)
	public long getTimeStamp() {
		return timeStamp;
	}

	public void setTimeStamp(long TimeStamp) {
		this.timeStamp = TimeStamp;
	}

	@Column(name = "InvocatinInstanceId", length = 45)
	public String getInvocatinInstanceId() {
		return invocatinInstanceId;
	}

	public void setInvocatinInstanceId(String invocatinInstanceId) {
		this.invocatinInstanceId = invocatinInstanceId;
	}

	@Column(name = "AcquireTimeStamp", length = 45)
	public long getAcquireTimeStamp() {
		return AcquireTimeStamp;
	}

	public void setAcquireTimeStamp(long acquireTimeStamp) {
		AcquireTimeStamp = acquireTimeStamp;
	}

	@Override
	public String toString() {
		return "DataCompressionLock [type=" + type + ", invocatinInstanceId=" + invocatinInstanceId + ", timeStamp="
				+ timeStamp + ", AcquireTimeStamp=" + AcquireTimeStamp + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (AcquireTimeStamp ^ (AcquireTimeStamp >>> 32));
		result = prime * result + ((invocatinInstanceId == null) ? 0 : invocatinInstanceId.hashCode());
		result = prime * result + (int) (timeStamp ^ (timeStamp >>> 32));
		result = prime * result + type;
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
		DataCompressionLock other = (DataCompressionLock) obj;
		if (AcquireTimeStamp != other.AcquireTimeStamp)
			return false;
		if (invocatinInstanceId == null) {
			if (other.invocatinInstanceId != null)
				return false;
		} else if (!invocatinInstanceId.equals(other.invocatinInstanceId))
			return false;
		if (timeStamp != other.timeStamp)
			return false;
		if (type != other.type)
			return false;
		return true;
	}

}
