package com.ffdc.BrowserHeadersAndCookie;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.StringTokenizer;

import javax.servlet.http.HttpServletRequest;
/**
 * Sometime for private IP Address, 
 * Translated from php code in java
 * // based on
 * // http://www.grantburton.com/2008/11/30/fix-for-incorrect-ip-addresses-in-wordpress-comments/
* It uses following IP addresses to determine Derive IP address
* HTTP_X_FORWARDED_FOR
* HTTP_X_CLUSTER_CLIENT_IP
* HTTP_CLIENT_IP
* HTTP_X_FORWARDED_FOR
* HTTP_X_FORWARDED
* HTTP_FORWARDED
* HTTP_VIA
* HTTP_FORWARDED_FOR
* 
 * @author Manish Sharma
 *
 */
public class DerivedClientIP {

	 
    /**
     * Private IP range
     */
	private static IpRange[] s_PrivateRanges = new IpRange[] { new IpRange("0.0.0.0", "2.255.255.255"),
			new IpRange("10.0.0.0", "10.255.255.255"), new IpRange("127.0.0.0", "127.255.255.255"),
			new IpRange("169.254.0.0", "169.254.255.255"), new IpRange("172.16.0.0", "172.31.255.255"),
			new IpRange("192.0.2.0", "192.0.2.255"), new IpRange("192.168.0.0", "192.168.255.255"),
			new IpRange("255.255.255.0", "255.255.255.255") };

	 
	/**
	 * Headers to be used
	 * order is in trust/use order top to bottom
	 */
	private static HeaderItem[] s_HeaderItems = new HeaderItem[] { new HeaderItem("HTTP_CLIENT_IP", false),
			new HeaderItem("HTTP_X_FORWARDED_FOR", true), new HeaderItem("HTTP_X_FORWARDED", false),
			new HeaderItem("HTTP_X_CLUSTER_CLIENT_IP", false), new HeaderItem("HTTP_FORWARDED_FOR", false),
			new HeaderItem("HTTP_FORWARDED", false), new HeaderItem("HTTP_VIA", false),
			new HeaderItem("REMOTE_ADDR", false) };

	/**
	 // based on
	// http://www.grantburton.com/2008/11/30/fix-for-incorrect-ip-addresses-in-wordpress-comments/ 
	 * @param request
	 * @param skipPrivate 
	 * @return
	 */
	
	public static String getDerivedClientIPFromRequest(HttpServletRequest request, boolean skipPrivate) {

		for (int i = 0; i < s_HeaderItems.length; i++) {
			HeaderItem item = s_HeaderItems[i];
			String ipString = request.getHeader(item.Key);
			if (ipString == null || ipString.isEmpty()) {
				continue;
			}
			if (item.Split) {
				StringTokenizer tok = new StringTokenizer(ipString, "'");
				while (tok.hasMoreTokens()) {
					String ip = tok.nextToken();
					if (isValidIP(ip, skipPrivate))
						return ip;

				}

			} else {
				if (isValidIP(ipString, skipPrivate))
					return ipString;
			}

		}
		return request.getRemoteAddr();
	}

	/**
	 * Checks wether ip address is valid or not
	 * @param ip
	 * @param skipPrivate
	 * @return
	 */
	private static boolean isValidIP(String ip, boolean skipPrivate) {
		InetAddress ipAddr;

		ip = ip == null ? "" : ip.trim();
		if (0 == ip.length())
			return false;
		try {
			ipAddr = InetAddress.getByName(ip);
		} catch (UnknownHostException e) {
			// Invalid ip
			return false;
		}
		

		if (skipPrivate) {
			long addr = IpRange.AddrToLong(ipAddr);
			for (int i = 0; i < s_PrivateRanges.length; i++) {
				IpRange range = s_PrivateRanges[i];
				if (range.Encompasses(addr))
					return false;
			}

		}

		return true;
	}

}

/**
 * 
 * /// <summary>
/// Describes a header item (key) and if it is expected to be
/// a comma-delimited string
/// </summary>
/**
 * @author manish sharma
 *
 */
final class HeaderItem {

	public String Key;

	public boolean Split;

	public HeaderItem(String key, boolean split) {
		this.Key = key;
		this.Split = split;
	}
}
/*
 * Class define IP range
 */
final class IpRange {

	private long _start;

	private long _end;

	public IpRange(String startStr, String endStr) {

		try {
			this._start = AddrToLong(InetAddress.getByName(startStr));
			this._end = AddrToLong(InetAddress.getByName(endStr));
		} catch (UnknownHostException e) {

		}

	}

	public static long AddrToLong(InetAddress ip) {

		byte[] ipBytes = ip.getAddress();
		long value = 0;
		for (int i = 0; i < ipBytes.length; i++) {
			value <<= 8;
			value += ipBytes[i];
		}

		return value;

	}

	public boolean Encompasses(long addrValue) {
		return _start <= addrValue && addrValue <= _end;
	}

	public final boolean Encompasses(InetAddress addr) {
		long value = IpRange.AddrToLong(addr);
		return this.Encompasses(value);
	}
}
