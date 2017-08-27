package com.ffdc.daemons;

import javax.persistence.EntityManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ffdc.EntityManagerWrapper;
import com.ffdc.DataAccesObjects.CampaignDAO;
import com.ffdc.DataAccesObjects.HitLogDAO;
import com.ffdc.models.HitLog;
import com.ffdc.utility.StolenToken;
import com.ffdc.utility.Utility;

/**
 * This class Asynchronously saves hit object in HitLog table. This helps avoid
 * IO delay for incoming tracker requests.
 * 
 * @author Manish Sharma
 *
 */
public class AsycHitRecorder implements Runnable {

	private HitLog hitToPersist = null;
	private static final Log log = LogFactory.getLog(AsycHitRecorder.class);

	/**
	 * Job that inserts data in Hitlog through Executor framework thread pool
	 */
	@Override
	public void run() {
		if (hitToPersist == null)
			return;
		log.debug(" Inside run Persisit Log  Form clinet " + hitToPersist.getClientIP());

		EntityManager manager = EntityManagerWrapper.getEntityManager();
		manager.getTransaction().begin();
		try {
			if (hitToPersist.getIsOpenHit() == 1)
				(new CampaignDAO()).updateUsedOpen(hitToPersist.getCampaignCode());
			else
				(new CampaignDAO()).updateUsedClicks(hitToPersist.getCampaignCode());
			// Save to DB
			(new HitLogDAO()).merge(hitToPersist);
			manager.getTransaction().commit();
			log.debug(" Finished run Persisit Log  Form clinet " + hitToPersist.getClientIP());
		} catch (Throwable e) {
			if (manager.isJoinedToTransaction())
				manager.getTransaction().rollback();
			log.info(e.getMessage());
			log.error(e.getMessage(), e);
		} finally {
			if (EntityManagerWrapper.isEntityManagerOpeninCurrentThread()) {
				EntityManagerWrapper.closeEntityManager();
			}
		}
	}

	/**
	 * Wrapper to save open hit
	 * 
	 * @param campaingCode
	 *            : Campaing code
	 * @param secondtoken
	 *            : It is either email for email campaign and timestamp for web
	 *            campaign
	 * @param token
	 *            : Token came along with hit request
	 * @param deviceType
	 *            : Device type whether it is mobile, PC or tab
	 * @param cookieValue
	 *            : Unique tracker cookie value
	 * @param derivedClientIP
	 *            : IP address derived from headers
	 * @param clientIP
	 *            : IP of client
	 * @param referrerUrl
	 *            : referrerURL if any
	 * @param fingerPrint
	 *            : Browser fingerprint
	 */
	public static void saveOpenHit(String campaingCode, String secondtoken, String token, String deviceType,
			int cookieValue, String derivedClientIP, String clientIP, String referrerUrl, String fingerPrint) {
		saveHit(campaingCode, secondtoken, token, deviceType, cookieValue, derivedClientIP, clientIP, referrerUrl,
				fingerPrint, 1);

	}

	/**
	 * Wrapper for saving date in hitlog for click hit
	 * 
	 * @param campaingCode
	 * @param secondtoken
	 * @param token
	 * @param deviceType
	 * @param cookieValue
	 * @param derivedClientIP
	 * @param clientIP
	 * @param referrerUrl
	 * @param fingerPrint
	 */
	public static void saveClickHit(String campaingCode, String secondtoken, String token, String deviceType,
			int cookieValue, String derivedClientIP, String clientIP, String referrerUrl, String fingerPrint) {
		saveHit(campaingCode, secondtoken, token, deviceType, cookieValue, derivedClientIP, clientIP, referrerUrl,
				fingerPrint, 0);

	}

	/**
	 * Saves data in HitLog table for both click hit or open hit It also mark
	 * bad ip for webrequest if token is really old
	 * 
	 * @param campaingCode
	 * @param secondtoken
	 * @param token
	 * @param deviceType
	 * @param cookieValue
	 * @param derivedClientIP
	 * @param clientIP
	 * @param referrerUrl
	 * @param fingerPrint
	 * @param isOpen
	 */
	private static void saveHit(String campaingCode, String secondtoken, String token, String deviceType,
			int cookieValue, String derivedClientIP, String clientIP, String referrerUrl, String fingerPrint,
			int isOpen) {

		HitLog newhit = new HitLog();
		if (cookieValue == -1) {
			// It's email type campaign and secondtoken is email id
			newhit.setEmail(secondtoken);
		} else {
			// It web type campaign. second token is timestamp
			long tokentimestamp = Utility.tryParseLong(secondtoken);
			if (tokentimestamp != 0 && (System.currentTimeMillis() - tokentimestamp) > 3600 * 1000) {
				// why would someone use old token.
				StolenToken.setStolenToken(token);
				BloomFilterBasedBadRequestScreener.setBadIP(fingerPrint);
				// not obligated to return anything
				return;
			}
			newhit.setEmail("");

		}

		newhit.setBrowserFingerPrint(fingerPrint);
		newhit.setClientIP(clientIP);
		newhit.setDerivedClinetIP(derivedClientIP);
		newhit.setCookieValue(cookieValue);
		newhit.setDeviceType(deviceType);
		newhit.setProcessed(0);
		newhit.setCampaignCode(campaingCode);
		newhit.setServerTS(System.currentTimeMillis());

		newhit.setReferrerUrl(referrerUrl == null ? "" : referrerUrl);
		newhit.setIsOpenHit(isOpen);

		if (cookieValue > 0)
			// This is guaranteed duplicate
			// for other we will find duplicate latter
			newhit.setiSDuplicalte(1);
		else
			newhit.setiSDuplicalte(0);
		AsycHitRecorder worker = new AsycHitRecorder();
		worker.hitToPersist = newhit;

		AsyncDatabaseTasksExecutor.EXECUTOR.execute(worker);
	}

}
