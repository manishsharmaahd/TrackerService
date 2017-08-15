package com.ffdc.testdata;

import java.security.SecureRandom;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ffdc.EntityManagerWrapper;
import com.ffdc.DataAccesObjects.CampaignDAO;
import com.ffdc.daemons.IngestorDaemon;
import com.ffdc.models.Campaign;
import com.ffdc.models.HitLog;
import com.ffdc.utility.Constants;

/**
 * This class simulates the HitLogs and create test data that can be used to
 * test aggregation
 * 
 * @author Manish Sharma
 *
 */
public class HitLogData {
	private static final Log log = LogFactory.getLog(HitLogData.class);
	private static String useragentPC = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/60.0.3112.90 Safari/537.36";
	private static String useragentMobile = "Mozilla/5.0 (iPhone; CPU iPhone OS 6_0 like Mac OS X) AppleWebKit/536.26 (KHTML, like Gecko) Version/6.0 Mobile/10A5376e Safari/8536.25";
	private static String useragentTab = "Mozilla/5.0 (iPad; CPU OS 6_0 like Mac OS X) AppleWebKit/536.26 (KHTML, like Gecko) Version/6.0 Mobile/10A5376e Safari/8536.25";

	Date minDate;
	Date maxDate;

	public int create(int days) throws Exception {
		List<Campaign> campaignList = (new CampaignDAO()).findAll();
		if (campaignList.isEmpty())
			throw new Exception("No Registred Campaign. Can't continue");

		// We cannot go beyound 60 days
		//
		if (days > 60) {
			days = 60;
		}

		long startTime = System.currentTimeMillis() - 86400L * days * 1000L; // two
																				// months
																				// back
		long currTime = System.currentTimeMillis() - 120000;
		maxDate = new Date(currTime);
		minDate = new Date(startTime);

		SecureRandom rand = SecureRandom.getInstanceStrong();

		// Have to commit transaction after 1000 records
		int i = 0;
		while (startTime < currTime) {
			HitLog htlog = new HitLog();

			i++;
			if (i % 2000 == 0) {
				try {
					EntityManagerWrapper.getEntityManager().getTransaction().commit();
				} catch (Exception e) {
					if (EntityManagerWrapper.getEntityManager().getTransaction().isActive())
						EntityManagerWrapper.getEntityManager().getTransaction().rollback();
					if (EntityManagerWrapper.isEntityManagerOpeninCurrentThread())
						EntityManagerWrapper.closeEntityManager();
					EntityManagerWrapper.getEntityManager();
				}

				EntityManagerWrapper.getEntityManager().getTransaction().begin();
				log.info("Commited  batch of hitlogs = " + i);
				i = 0;
			}
			long toIncrease = getPositive(rand.nextInt() % 199589);

			startTime = startTime + toIncrease;

			htlog.setServerTS(startTime);

			Campaign useCampaing = campaignList.get(i % campaignList.size());
			if (useCampaing == null || useCampaing.getCampaignCode() == null)
				continue;

			htlog.setCampaignCode(useCampaing.getCampaignCode());

			if (useCampaing.getType().equals(Constants.CAMPAIGN_EMAIL)) {
				htlog.setEmail("sharmamaish" + System.currentTimeMillis() % days * 500 + "yahoo.com");

				htlog.setCookieValue(0);

			} else {
				htlog.setEmail("");

				if (System.currentTimeMillis() % 3 == 1)
					htlog.setCookieValue(0);
				else {
					// log.setiSDuplicalte(1);
					// log.setCookieValue(1);
					htlog.setCookieValue(0);
				}
			}

			if (System.currentTimeMillis() % 4 == 0) {
				htlog.setDeviceType(Constants.DeviceTypeMobile);
				htlog.setBrowserFingerPrint(useragentMobile + System.currentTimeMillis() % 6);
			} else if (System.currentTimeMillis() % 4 == 1) {
				htlog.setDeviceType(Constants.DeviceTypeTablet);
				htlog.setBrowserFingerPrint(useragentTab + System.currentTimeMillis() % 6);

			} else if (System.currentTimeMillis() % 4 == 2) {
				htlog.setDeviceType(Constants.DeviceTypePC);
				htlog.setBrowserFingerPrint(useragentPC + System.currentTimeMillis() % 6);
			} else if (System.currentTimeMillis() % 4 == 3) {
				htlog.setDeviceType(Constants.DeviceTypePC);
				htlog.setBrowserFingerPrint(useragentPC + System.currentTimeMillis() % 6);

			}

			htlog.setDerivedClinetIP(getPositive(rand.nextInt() % 11) + "." + getPositive(rand.nextInt() % 11) + "."
					+ getPositive(rand.nextInt() % 11));

			htlog.setClientIP(getPositive(rand.nextInt() % 11) + "." + getPositive(rand.nextInt() % 11) + "."
					+ getPositive(rand.nextInt() % 11));

			htlog.setReferrerUrl("");
			if (System.currentTimeMillis() % 3 == 1)
				htlog.setIsOpenHit(1);
			else
				htlog.setIsOpenHit(0);
			htlog.setProcessed(Constants.HIT_RECORD_UNPROCESSED);
			if (htlog.getCampaignCode() != null)
				EntityManagerWrapper.getEntityManager().merge(htlog);
			else
				System.out.println("Null campaign code");
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {

				e.printStackTrace();
			}

		}
		// wake up ingestor daemon so that work can start

		synchronized (IngestorDaemon.getInstance()) {
			IngestorDaemon.getInstance().notify();

		}
		return i;

	}

	public Date getMinDate() {
		return minDate;
	}

	public void setMinDate(Date minDate) {
		this.minDate = minDate;
	}

	public Date getMaxDate() {
		return maxDate;
	}

	public void setMaxDate(Date maxDate) {
		this.maxDate = maxDate;
	}

	private long getPositive(long a) {
		return a > 0 ? a : -1 * a;
	}

}
