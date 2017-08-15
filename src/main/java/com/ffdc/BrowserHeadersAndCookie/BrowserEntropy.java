package com.ffdc.BrowserHeadersAndCookie;

import javax.servlet.http.HttpServletRequest;

/**
 * Uniqueness of a request can be determined from IP address. But for requests
 * behind Proxy, Private IP, IP behind NAT, it is not possible. So relatively
 * static HTTP header can be used to determine uniqueness. We have used headers
 * Origin, UserAgent, AcceptEncoding, AcceptLanguage, Accept, AccpetCharSet,
 * AcceptDaetime, Form, ProxyAuthorizaton, Vai will provide 2^12 combinations
 * coupling above with IP Address and Derived IP Address (private IP and
 * similar) will help us identify browser uniquely.
 * 
 * Combination of headers and IP address and derived will help us determine
 * browser uniquely This combination is referred as browser finger print.
 * 
 * This class extracts out header from HTTPServletRequest object and store that
 * to compute broserfingerprint
 * 
 * @author Manish Sharma
 *
 */
public class BrowserEntropy {

	// Standard Headers
	private String Origin = "";
	private String UserAgent = "";
	private String AcceptEncoding = "";
	private String AcceptLanguage = "";
	private String Accept = "";
	private String AcceptCharset = "";
	private String AcceptDatetime = "";
	private String Authorization = "";
	private String From = "";
	private String ProxyAuthorization = "";
	private String Via = "";

	// Non-standard Headers
	private String XRequestedWith = "";
	private String XForwardedFor = "";
	private String XForwardedHost = "";

	/**
	 * combination of static headers and ip adderessed can be uniquely identify
	 * browsers. Currently this method returns in string format. It could also
	 * return more formatted version such as json or xml In this implentation
	 * simple toString kind method is used
	 * 
	 * @param clientIP
	 * @param clientDerivedIP
	 * @return
	 */
	public String getfingerPrint(String clientIP, String clientDerivedIP) {

		String fingerPrint = "clientIP" + clientIP + "clientDerivedIP" + clientDerivedIP + " Origin  = " + Origin
				+ "UserAgent = " + UserAgent + "AcceptEncoding = " + AcceptEncoding + "AcceptLanguage = "
				+ AcceptLanguage + "Accept = " + Accept + "AcceptCharset = " + AcceptCharset + "AcceptDatetime = "
				+ AcceptDatetime + "Authorization = " + Authorization + "From = " + From + "ProxyAuthorization	 = "
				+ ProxyAuthorization + "Via = " + Via + "XRequestedWith = " + XRequestedWith + "XForwardedFor = "
				+ XForwardedFor + "XForwardedHost = " + XForwardedHost;

		return fingerPrint;

	}

	/**
	 * Constructor - extracts headers form HttpServletRequest object
	 * 
	 * @param req
	 */
	public BrowserEntropy(HttpServletRequest req) {
		Origin = req.getHeader("Origin");
		UserAgent = req.getHeader("User-Agent");
		AcceptEncoding = req.getHeader("Accept-Encoding");
		AcceptLanguage = req.getHeader("Accept-Language");
		Accept = req.getHeader("Accept");
		AcceptCharset = req.getHeader("Accept-Charset");
		AcceptDatetime = req.getHeader("Accept-Datetime");
		Authorization = req.getHeader("Authorization");
		From = req.getHeader("From");
		ProxyAuthorization = req.getHeader("Proxy-Authorization");
		Via = req.getHeader("Via");
		XRequestedWith = req.getHeader("X-Requested-With");
		XForwardedFor = req.getHeader("X-Forwarded-For");
		XForwardedHost = req.getHeader("X-Forwarded-Host");
	}

	/**
	 * Default constructor
	 */
	public BrowserEntropy() {

	}

	/**
	 * 
	 * @return
	 */
	public String getOrigin() {
		return Origin;
	}

	/**
	 * 
	 * @param origin
	 */
	public void setOrigin(String origin) {
		Origin = origin;
	}

	/**
	 * 
	 * @return
	 */
	public String getUserAgent() {
		return UserAgent;
	}

	/**
	 * 
	 * @param userAgent
	 */
	public void setUserAgent(String userAgent) {
		UserAgent = userAgent;
	}

	/**
	 * 
	 * @return
	 */
	public String getAcceptEncoding() {
		return AcceptEncoding;
	}

	/**
	 * 
	 * @param acceptEncoding
	 */
	public void setAcceptEncoding(String acceptEncoding) {
		AcceptEncoding = acceptEncoding;
	}

	/**
	 * 
	 * @return
	 */
	public String getAcceptLanguage() {
		return AcceptLanguage;
	}

	/**
	 * 
	 * @param acceptLanguage
	 */
	public void setAcceptLanguage(String acceptLanguage) {
		AcceptLanguage = acceptLanguage;
	}

	/**
	 * 
	 * @return
	 */
	public String getAccept() {
		return Accept;
	}

	/**
	 * 
	 * @param accept
	 */
	public void setAccept(String accept) {
		Accept = accept;
	}

	/**
	 * 
	 * @return
	 */
	public String getAcceptCharset() {
		return AcceptCharset;
	}

	/**
	 * 
	 * @param acceptCharset
	 */
	public void setAcceptCharset(String acceptCharset) {
		AcceptCharset = acceptCharset;
	}

	/**
	 * 
	 * @return
	 */
	public String getAcceptDatetime() {
		return AcceptDatetime;
	}

	/**
	 * 
	 * @param acceptDatetime
	 */
	public void setAcceptDatetime(String acceptDatetime) {
		AcceptDatetime = acceptDatetime;
	}

	/**
	 * 
	 * @return
	 */
	public String getAuthorization() {
		return Authorization;
	}

	/**
	 * 
	 * @param authorization
	 */
	public void setAuthorization(String authorization) {
		Authorization = authorization;
	}

	/**
	 * 
	 * @return
	 */
	public String getFrom() {
		return From;
	}

	/**
	 * 
	 * @param from
	 */
	public void setFrom(String from) {
		From = from;
	}

	/**
	 * 
	 * @return
	 */
	public String getProxyAuthorization() {
		return ProxyAuthorization;
	}

	/**
	 * 
	 * @param proxyAuthorization
	 */
	public void setProxyAuthorization(String proxyAuthorization) {
		ProxyAuthorization = proxyAuthorization;
	}

	/**
	 * 
	 * @return
	 */
	public String getVia() {
		return Via;
	}

	/**
	 * 
	 * @param via
	 */
	public void setVia(String via) {
		Via = via;
	}

	/**
	 * 
	 * @return
	 */
	public String getXRequestedWith() {
		return XRequestedWith;
	}

	/**
	 * 
	 * @param xRequestedWith
	 */
	public void setXRequestedWith(String xRequestedWith) {
		XRequestedWith = xRequestedWith;
	}

	/**
	 * 
	 * @return
	 */
	public String getXForwardedFor() {
		return XForwardedFor;
	}

	/**
	 * 
	 * @param xForwardedFor
	 */
	public void setXForwardedFor(String xForwardedFor) {
		XForwardedFor = xForwardedFor;
	}

	/**
	 * 
	 * @return
	 */
	public String getXForwardedHost() {
		return XForwardedHost;
	}

	/**
	 * 
	 * @param xForwardedHost
	 */
	public void setXForwardedHost(String xForwardedHost) {
		XForwardedHost = xForwardedHost;
	}
}
