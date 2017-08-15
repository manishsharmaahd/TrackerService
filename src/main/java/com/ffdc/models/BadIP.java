package com.ffdc.models;

import static javax.persistence.GenerationType.IDENTITY;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
/**
 * Represents BadIP table
 * @author manish sharma
 *
 */
@Entity
@Table(name = "badip")
public class BadIP implements java.io.Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6931829019287525970L;

	private int id;
	private String devrivedClinetIP;
	private String clientIP;
	private long beginWindowTimestamp;
	private long lastAccessTimestamp;

	private int count = 0;

	public BadIP() {

	}

	@Id
	@GeneratedValue(strategy = IDENTITY)
	@Column(name = "ID", unique = true, nullable = false)
	public int getID() {
		return id;
	}

	public void setID(int id) {
		this.id = id;
	}

	@Column(name = "DerivedClinetIP", length = 45)
	public String getDerivedClinetIP() {
		return devrivedClinetIP;
	}

	public void setDerivedClinetIP(String devrivedClinetIP) {
		this.devrivedClinetIP = devrivedClinetIP;
	}

	@Column(name = "ClientIP", length = 8)
	public String getClientIP() {
		return clientIP;
	}

	public void setClientIP(String remoteIP) {
		this.clientIP = remoteIP;
	}

	@Column(name = "beginWindowTimestamp", length = 8)
	public long getBeginWindowTimestamp() {
		return this.beginWindowTimestamp;
	}

	public void setBeginWindowTimestamp(long serverTS) {
		this.beginWindowTimestamp = serverTS;
	}

	@Column(name = "lastAccessTimestamp", length = 8)
	public long getLastAccessTimestamp() {
		return lastAccessTimestamp;
	}

	public void setLastAccessTimestamp(long serverTS) {
		this.lastAccessTimestamp = serverTS;
	}

	@Column(name = "count", length = 2)
	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (beginWindowTimestamp ^ (beginWindowTimestamp >>> 32));
		result = prime * result + ((clientIP == null) ? 0 : clientIP.hashCode());
		result = prime * result + count;
		result = prime * result + ((devrivedClinetIP == null) ? 0 : devrivedClinetIP.hashCode());
		result = prime * result + (int) (lastAccessTimestamp ^ (lastAccessTimestamp >>> 32));
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
		BadIP other = (BadIP) obj;
		if (beginWindowTimestamp != other.beginWindowTimestamp)
			return false;
		if (clientIP == null) {
			if (other.clientIP != null)
				return false;
		} else if (!clientIP.equals(other.clientIP))
			return false;
		if (count != other.count)
			return false;
		if (devrivedClinetIP == null) {
			if (other.devrivedClinetIP != null)
				return false;
		} else if (!devrivedClinetIP.equals(other.devrivedClinetIP))
			return false;
		if (lastAccessTimestamp != other.lastAccessTimestamp)
			return false;
		return true;
	}

}
