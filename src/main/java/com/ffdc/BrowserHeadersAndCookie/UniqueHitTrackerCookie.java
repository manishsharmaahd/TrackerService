package com.ffdc.BrowserHeadersAndCookie;
 

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.ffdc.utility.Utility;


/**
 * Create and with name campaingcode + imagtracker. Presence of this cookie help us to determine duplicate hit
 * If cookie is not present we fall back on fingerprint
 * @author manish sharma 
 *
 */
public class UniqueHitTrackerCookie {

	/**
	 * Format of cookie campaigncode,2;itsneelathomail.com=1;
	 * 
	 * @param res
	 * @param uniqueParam
	 * @param count
	 */
	public static void setCookie(HttpServletResponse res,  String campaignCode, int value) {
		 
		String cookieVal = "" + value;
		 
		Cookie c = new Cookie(campaignCode + "impagtracker", cookieVal);
		c.setMaxAge(30 * 24 * 3600); // For one month
		c.setPath("/");
		res.addCookie(c);

	}
	/**
	 * Reads cookie and return count
	 * @param request
	 * @param campaignCode
	 * @return
	 */
	public static int readCookieAndGetCount(HttpServletRequest request, String campaignCode) {
		Cookie[] cookies = request.getCookies();
		if (cookies == null )
			return 0;
		String cookieVal = "";
		for (int i = 0; i < cookies.length; i++) {
			Cookie cookie1 = cookies[i];
			if (cookie1.getName().equals(campaignCode + "impagtracker")) {
				cookieVal = cookie1.getValue();

			}
		}
		return Utility.tryParseInt(cookieVal);
		
	}
 }
