package com.ffdc.controller;

import java.io.IOException;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mobile.device.Device;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.ffdc.BrowserHeadersAndCookie.BrowserEntropy;
import com.ffdc.BrowserHeadersAndCookie.DerivedClientIP;
import com.ffdc.BrowserHeadersAndCookie.UniqueHitTrackerCookie;
import com.ffdc.DataAccesObjects.CampaignDAO;
import com.ffdc.daemons.BloomFilterBasedBadRequestScreener;
import com.ffdc.daemons.GeneratedTokenUpdateWorker;
import com.ffdc.daemons.AsycHitRecorder;
import com.ffdc.daemons.AsyncDatabaseTasksExecutor;
import com.ffdc.models.Campaign;
import com.ffdc.utility.AESEncryptDecrypt;
import com.ffdc.utility.Constants;
import com.ffdc.utility.StolenToken;
import java.util.Map;

/**
 * It has routes to create : 1) Email Open Tracker Token 2) Web Open Tracker
 * Token
 * 
 * 3) To be end point for hit for both Eamil and web. It returns 1 by 1 pixel
 * image to the caller and before that captures various parameters of request in
 * hitLog
 * 
 * 
 * Here we would have to give response pretty fast to the client so we do any
 * process in a Async queue or thread pool. IO operation might fail after the
 * request is responded to the user. However, looking at the nature of the
 * application, this is a good compromise to make. The risk can be mitigated by
 * using persistent robust queue
 * 
 * @author Manish Sharma
 *
 */

@RestController
@RequestMapping("/Open")
public class OpenController {
	private static final Log log = LogFactory.getLog(OpenController.class);
	private static final String transparentImageGif = "R0lGODlhAQABAIAAAAAAAP///yH5BAEAAAAALAAAAAABAAEAAAIBRAA7";
	private static final byte[] transparentImageGifByte = Base64.getDecoder().decode(transparentImageGif);

	/**
	 * This method creates a Opaque token containing Email id and redirect URL.
	 * 
	 * Before generating the token validation are done for email id, URL, active
	 * campaigns etc. For invalid input 400 Bad request is returned For good
	 * request token in string format and status code 200 is returned
	 * 
	 * @param campaignCode
	 *            : Code of campaign for which the token has to created.
	 * @param forEmailID
	 *            : Email id for which token is created
	 * @return : URI to included in the email. Output is in String format (not
	 *         in JSON)
	 */
	@RequestMapping(value = "/CreateEmailTracker", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<?> CreateEmailTracker(@RequestParam("CampaignCode") String campaignCode,
			@RequestParam("ForEmailID") String forEmailID) {
		log.debug("Entering CreateEmailTracker CampaingCode " + campaignCode + "ForEmailID = " + forEmailID + ":::");
		// Preliminary check for the validity of Email
		Pattern pattern = Pattern.compile("^(.+)@([^@]+[^.])$");
		Matcher matcher = pattern.matcher(forEmailID);
		if (!matcher.find()) {
			log.info(" CreateEmailTracker Invalid Email id for email type of campaign " + campaignCode + "ForEmailID = "
					+ forEmailID + ":::");
			return new ResponseEntity<String>("Invalid Email id for email type of campaign", HttpStatus.BAD_REQUEST);

		}

		CampaignDAO campdao = new CampaignDAO();
		Map<String, Campaign> activemap = campdao.findAllActiveCampaings();
		if (!activemap.containsKey(campaignCode)) {
			log.info(" CreateEmailTracker Invlaid/Expired/Premature CampaingCode " + campaignCode + "ForEmailID = "
					+ forEmailID + ":::");
			return new ResponseEntity<String>("Invalid/Premature/Expired Campaign", HttpStatus.BAD_REQUEST);
		}
		Campaign c = activemap.get(campaignCode);
		if (!c.getType().equals(Constants.CAMPAIGN_EMAIL)) {
			log.info(" CreateEmailTracker wrong campaign type " + campaignCode + "ForEmailID = " + forEmailID + ":::");
			return new ResponseEntity<String>("Wrong Campaign type", HttpStatus.BAD_REQUEST);
		}

		// Encrypting the token ensures that attacker cannot conduct dictionary
		// attack
		// Unique param such as emailid ensure uniqueness of param
		String token = AESEncryptDecrypt.encrypt(campaignCode + Constants.TokenDelimiter + forEmailID);

		GeneratedTokenUpdateWorker worker = new GeneratedTokenUpdateWorker(true, campaignCode);
		AsyncDatabaseTasksExecutor.EXECUTOR.execute(worker);
		log.debug("Finished successfully CreateEmailTracker CampaingCode " + campaignCode + "ForEmailID = " + forEmailID
				+ ":::");
		return new ResponseEntity<String>("/Open/EmailImage.gif?tracker=" + token, HttpStatus.CREATED);
	}

	/**
	 * This method creates a Opaque token containing redirect URL and timeout.
	 * 
	 * Before generating the token validation are done for URL, active campaigns
	 * etc. For invalid input 400 Bad request is returned For good request token
	 * in string format and status code 200 is returned
	 * 
	 * @param campaignCode
	 *            : Code of campaign for which the token has to created.
	 * @return : URI to included in the email. Output is in String format (not
	 *         in JSON)
	 * @param campaignCode
	 * @return
	 */
	@RequestMapping(value = "/CreateWebTracker", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<?> CreateWebTracker(@RequestParam("CampaignCode") String campaignCode) {
		log.debug("Entering CreateWebTracker CampaingCode " + campaignCode + ":::");

		CampaignDAO campdao = new CampaignDAO();
		Map<String, Campaign> activemap = campdao.findAllActiveCampaings();
		if (!activemap.containsKey(campaignCode)) {
			log.info(" CreateWebTracker Invlaid/Expired/Premature CampaingCode " + campaignCode + ":::");
			return new ResponseEntity<String>("Invalid/Premature/Expired Campaign", HttpStatus.BAD_REQUEST);
		}

		Campaign c = activemap.get(campaignCode);
		if (!c.getType().equals(Constants.CAMPAIGN_WEB)) {
			log.info(" CreateWebTracker wrong campaign type " + campaignCode);
			return new ResponseEntity<String>("Wrong campaign type", HttpStatus.BAD_REQUEST);
		}

		// Encrypting the token ensures that attacker cannot conduct dictionary
		// attack
		// Time stamp is added in the token. If token is used long time after
		// generatio means the token has been stolen
		String token = AESEncryptDecrypt.encrypt(campaignCode + Constants.TokenDelimiter + System.currentTimeMillis());
		GeneratedTokenUpdateWorker worker = new GeneratedTokenUpdateWorker(true, campaignCode);
		AsyncDatabaseTasksExecutor.EXECUTOR.execute(worker);

		log.debug("Finished successfully CreateWebTracker CampaingCode " + campaignCode + ":::"
				+ System.currentTimeMillis());
		return new ResponseEntity<String>("/Open/WebImage.gif?tracker=" + token, HttpStatus.CREATED);
	}

	/**
	 * This is route to record various parameters of click and the redirect to
	 * redirect URL for clicks from email The priority is to give the response
	 * fast to the caller so no database transaction take place here rather it
	 * is submitted for a Async job.
	 * 
	 * Flow is as following: 1) Token is parsed for redirect url, timeout,
	 * campaign code, email id 2) Create HitLog object and capture request
	 * parameters 3) Submit hitlog object for persistence in Asych job queue 4)
	 * Return 301 temporary redirect to the URL. 5) Before any task it checks
	 * for stolen token or Bad IP.
	 * 
	 * Parameter of request recorded are 1) IP Address 2) Derived IP Address
	 * from various forward headers 3) EmailID from the token 4) Important
	 * headers for Browser Entropy 5) Refer URL 6) Device type - Mobile, PC or
	 * Tab
	 * 
	 * @param device
	 *            : Object encapsulating device type info
	 * @param req
	 *            : Object to get various headers
	 * @param response:
	 *            Response to redirect to refer URL
	 * @param token:
	 *            Token came with request
	 * @return: 400 for bad requests and 301 to redirect
	 * @throws IOException
	 * 
	 */

	@RequestMapping(value = "/EmailImage.gif", method = RequestMethod.GET, produces = MediaType.IMAGE_GIF_VALUE)
	@ResponseBody
	public byte[] hitEmail(Device device, final HttpServletRequest req, final HttpServletResponse response,
			@RequestParam("tracker") String token) throws IOException {

		// This token has been previously determined to be stolen
		// Stolen token is set when it is used from BadIP
		// Not obligated to return anything to the caller
		if (StolenToken.isStolen(token)) {
			return null;
		}

		// User headers of request to get browser IP behind nat, proxy etc user
		// various http headers
		String derivedClientIP = DerivedClientIP.getDerivedClientIPFromRequest(req, false);
		String clientIP = req.getRemoteAddr();

		BrowserEntropy browserEntropy = new BrowserEntropy(req);
		String fingerPrint = browserEntropy.getfingerPrint(clientIP, derivedClientIP);

		// Checks if IP is bad. Also increament count of current ip
		if (BloomFilterBasedBadRequestScreener.isBadIP(fingerPrint)) {
			// Isn't the token stolen and being re-used
			StolenToken.setStolenToken(token);
			return null;
		}

		String cleartexttoken = AESEncryptDecrypt.decrypt(token);
		if (cleartexttoken == null) {
			// This is not our token
			// We don't want to process any further
			// not even obligated to give nice reply to the caller

			BloomFilterBasedBadRequestScreener.setBadIP(fingerPrint);
			return null;
		}

		String deviceType = Constants.DeviceTypePC;
		if (device.isNormal())
			deviceType = Constants.DeviceTypePC;
		else if (device.isTablet())
			deviceType = Constants.DeviceTypeTablet;
		else if (device.isMobile())
			deviceType = Constants.DeviceTypeMobile;

		String referer = req.getHeader("Referer");


		// if cleartext token is corrupted. It is our mistake. We cannot
		// penalize the caller for that
		// Parse cleartoken to get useful information
		String[] splittedStrings = cleartexttoken.split(Constants.TokenDelimiter);
		if (splittedStrings.length == 2) {
			String campaingCode = splittedStrings[0];
			String email = splittedStrings[1];
			AsycHitRecorder.saveOpenHit(campaingCode, email, token, deviceType, -1, derivedClientIP, clientIP,
					referer, fingerPrint);
		}

		return transparentImageGifByte;
	}

	/**
	 * The main difference from email click hit and web hit is that there is no
	 * email to check uniqueness A cookie is dropped to track duplicate request.
	 * If the cookie is present, the request is definitely duplicate If cookie
	 * is not present - Browser Entropy(varouis static headers such as
	 * user-agent etc), IP Addresses an Derived IP address etc are used
	 * 
	 * This is route to record various parameters of click and the redirect to
	 * redirect URL for clicks from web The priority is to give the response
	 * fast to the caller so no database transaction take place here rather it
	 * is submitted for a Async job.
	 * 
	 * Flow is as following: 1) Token is parsed for redirect url, timeout,
	 * campaign code, email id 2) Create HitLog object and capture request
	 * parameters 3) Submit hitlog object for persistence in Asych job queue 4)
	 * Return 301 temporary redirect to the URL. 5) Before any task it checks
	 * for stolen token or Bad IP.
	 * 
	 * Parameter of request recorded are 1) IP Address 2) Derived IP Address
	 * from various forward headers 3) Important headers for Browser Entropy 4)
	 * Refer URL 5) Device type - Mobile, PC or Tab
	 * 
	 * @param device
	 *            : Object encapsulating device type info
	 * @param req
	 *            : Object to get various headers
	 * @param response:
	 *            Response to redirect to refer URL
	 * @param token:
	 *            Token came with request
	 * @return: 400 for bad requests and 301 to redirect
	 * @param device
	 * @param req
	 * @param response
	 * @param token
	 * @return
	 * @throws IOException
	 */

	@RequestMapping(value = "/WebImage.gif", method = RequestMethod.GET, produces = MediaType.IMAGE_GIF_VALUE)
	@ResponseBody
	public byte[] hitWeb(Device device, final HttpServletRequest req, final HttpServletResponse response,
			@RequestParam("tracker") String token) throws IOException {

		// This token has been previously determined to be stolen
		// Stolen token is set when it is used from BadIP
		// Not obligated to return anything to the caller
		if (StolenToken.isStolen(token)) {
			return null;
		}

		String derivedClientIP = DerivedClientIP.getDerivedClientIPFromRequest(req, false);
		String clientIP = req.getRemoteAddr();
		BrowserEntropy browserEntropy = new BrowserEntropy(req);
		String fingerPrint = browserEntropy.getfingerPrint(clientIP, derivedClientIP);

		
		// Checks if IP is bad. Also increament count of current ip
		if (BloomFilterBasedBadRequestScreener.isBadIP(fingerPrint)) {
			// Isn't the token stolen and being re-used
			StolenToken.setStolenToken(token);
			return null;
		}

		String cleartexttoken = AESEncryptDecrypt.decrypt(token);
		if (cleartexttoken == null) {
			// This is not our token
			// We don't want to process any further
			// not even obligated to give nice reply to the caller

			BloomFilterBasedBadRequestScreener.setBadIP(fingerPrint);
			return null;
		}

		String deviceType = "";
		if (device.isNormal())
			deviceType = Constants.DeviceTypePC;
		else if (device.isTablet())
			deviceType = Constants.DeviceTypeTablet;
		else if (device.isMobile())
			deviceType = Constants.DeviceTypeMobile;
		else
			deviceType = Constants.DeviceTypePC;

		String referer = req.getHeader("Referer");


		// Parse cleartoken to get useful information
		String[] splittedStrings = cleartexttoken.split(Constants.TokenDelimiter);
		if (splittedStrings.length == 2) {
			String campaingCode = splittedStrings[0];
			String timestamp = splittedStrings[1];

			// Check if cooke has been previously dropped.
			int countfromcookie = UniqueHitTrackerCookie.readCookieAndGetCount(req, campaingCode);
			AsycHitRecorder.saveOpenHit(campaingCode, timestamp, token, deviceType, countfromcookie,
					derivedClientIP, clientIP, referer, fingerPrint);

			// Drop the cookie
			UniqueHitTrackerCookie.setCookie(response, campaingCode, countfromcookie + 1);

		}

		return transparentImageGifByte;
	}

}
