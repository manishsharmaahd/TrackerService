package com.ffdc.utility;

import org.apache.commons.jcs.JCS;
import org.apache.commons.jcs.access.CacheAccess;
/**
 * JCS cache singleton to save campaign. TTL etc are configured in cache.ccf file
 * @author manish sharma
 *
 */
public class CacheSingleton {
	final static private CacheAccess<String, Object> jcsCache = JCS.getInstance("default");
	 
	public static CacheAccess<String, Object> getInstance() {
		return jcsCache;
	}
}
