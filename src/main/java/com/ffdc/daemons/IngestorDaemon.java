package com.ffdc.daemons;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import java.util.stream.Collectors;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ffdc.EntityManagerWrapper;
import com.ffdc.DataAccesObjects.AggregatedDataDao;
import com.ffdc.DataAccesObjects.CampaignDAO;
import com.ffdc.DataAccesObjects.HitLogDAO;
import com.ffdc.models.AggregatedData;
import com.ffdc.models.AggregatedDataId;
import com.ffdc.models.Campaign;
import com.ffdc.models.HitLog;
import com.ffdc.stats.DataCompressionTypes;
import com.ffdc.stats.StatsConfig;
import com.ffdc.utility.Constants;

/**
 * This is designed in a manner that multiple instance of daemons with in same
 * JVM or various JVMs can work togather This class performs three task 1)
 * Identifies and mark HitLog entry as unique and duplicate 2) Bucket Entry of
 * HitLog to AggregateData table with interval of 5 Mins
 * 
 * @author Manish Sharma
 *
 */
public class IngestorDaemon implements Runnable {
	private static final Log log = LogFactory.getLog(IngestorDaemon.class);

	private static IngestorDaemon instance = new IngestorDaemon();

	/**
	 * for singleton pattern
	 * 
	 * @return Singleton instance
	 */
	public static IngestorDaemon getInstance() {
		return instance;
	}

	/**
	 * private constructor
	 */
	private IngestorDaemon() {

	}

	/**
	 * Method to start daemon thread
	 */
	public static void start() {
		Thread t = new Thread(instance);
		t.start();
	}

	/**
	 * It analyzes and mark duplicate HitLog within memory. This save database
	 * hit in case there are duplicate. If this method were not implemented,
	 * findAndMark Duplicate form database will work, This method is written for
	 * optimization.
	 * 
	 * @param listtoprocess
	 *            List of Hotlog ojects to ingested.
	 */
	public void findAndMarkDuplicateMemory(List<HitLog> listtoprocess) {
		// First letus do inMemory
		// Process duplicate for Email
		// Here uniqueness identifier is Email
		Map<String, Map<String, List<HitLog>>> dupMapEmail = listtoprocess.stream().filter(a -> !a.getEmail().isEmpty())
				.collect(Collectors.groupingBy(HitLog::getCampaignCode, Collectors.groupingBy(HitLog::getEmail)));
		dupMapEmail.values().forEach(mapobj -> {
			mapobj.values().forEach(hitObjList -> {
				Iterator<HitLog> it = hitObjList.iterator();
				if (it.hasNext()) {
					// leaving first item as non-duplicate
					it.next();
				}
				while (it.hasNext()) { // Marking all duplicate
					it.next().setiSDuplicalte(1);

				}
			});
		});

		// ProcessDuplcate for Web
		// Here uniqueness identifier is BroserFingerPrint
		// Note that Client IP and Browser IP are part of browser figerprint.

		// note that that is no ! in from of getEmail.isEmpty and additional
		// finler a->getIsDuplicate()
		Map<String, Map<String, List<HitLog>>> dupMapWeb = listtoprocess.stream()
				.filter(a -> (a != null && a.getEmail().isEmpty() && a.getiSDuplicalte() == 0)).collect(Collectors
						.groupingBy(HitLog::getCampaignCode, Collectors.groupingBy(HitLog::getBrowserFingerPrint)));
		dupMapWeb.values().forEach(mapobj -> {
			mapobj.values().forEach(hitObjList -> {
				Iterator<HitLog> it = hitObjList.iterator();
				if (it.hasNext()) {
					// leaving first item as non-duplicate
					it.next();
				}
				while (it.hasNext()) { // Marking all duplicate
					it.next().setiSDuplicalte(1);

				}
			});
		});

	}

	/**
	 * Does following: 1) cleans out old crashed processHitLog instances 2)
	 * Acquire the logical lock Update the hitlog in list with status =
	 * processing and with invocation id of this instance In same transaction
	 * query processing and with invocaiton id of this instance. 3) Mark
	 * duplicate in memory to minimize database calls 4) For each non -
	 * duplicate request, query database to determine where the request is
	 * unique or duplicate 5) Bucket the hitlogs in 5 min buckets using our
	 * sweet StatsDataFilter
	 * 
	 * Other processes/thread might be inserting/updating the AggregateData 
	 * We have to be very careful that we don't overwrite the open counts, click count and other stats.
	 * @see mergeOpenHitWithAggreData of AggregatedDataDao 
	 * 
	 */
	public void processHitLog() {
		// Step 1 clean-up all hung/timedout/ jobs so that they cant
		// be picked up again
		EntityManager em = EntityManagerWrapper.getEntityManager();
		HitLogDAO dao = new HitLogDAO();
		em.getTransaction().begin();
		dao.cleanupTimedout();
		em.getTransaction().commit();
		try {
			Thread.sleep(2);
		} catch (InterruptedException e) {

			log.info("Process it got intrupped");
		}
		for (int ind = 0; ind < 20; ind++) {
			int toProcessOpen = 0;

			if (ind % 2 == 0)
				// Process clinc
				toProcessOpen = 1;
			// Step 2 acquire the logs to process
			// Here we put the logical lock on the records so that
			// resourced in DB(if any) are not locked
			em.getTransaction().begin();
			// 1 stands for is Open
			List<HitLog> listToProcess = dao.acquireForProcessing(toProcessOpen);
			em.getTransaction().commit();

			if (listToProcess.size() == 0)
				continue;

			// Step 3
			// Following method identify duplicate in the
			// listotprocess and mark them duplicate
			// so no database calls have to be made
			findAndMarkDuplicateMemory(listToProcess);

			// Step 4 find duplicate from database
			// Here were are just querying only and marking memory
			// not saving in the DB
			em.getTransaction().begin();
			// 1 signifies open
			dao.findAndMarkDuplicate(
					listToProcess.stream().filter(a -> (a.getiSDuplicalte() == 0)).collect(Collectors.toList()),
					toProcessOpen);
			em.getTransaction().commit();

			em.getTransaction().begin();
			bucketHitLogs(listToProcess, toProcessOpen);

			em.getTransaction().commit();

		}

	}

	/**
	 * For each request : Find the 5 min bucket for which the item belongs If
	 * duplicate set expiry = now + 30 else campaing end periond + rentention
	 * Aggregate statistics for items belonging to bucket Persist the bucket
	 * data
	 * 
	 * @param listtoprocess
	 *            : Items to bucketed
	 * @param isOpen
	 *            : 1= Open 0 = click
	 */
	public void bucketHitLogs(List<HitLog> listtoprocess, int isOpen) {

		StatsConfig statIns = StatsConfig.getInstance();
		CampaignDAO cdap = new CampaignDAO();
		Map<String, Campaign> activCampaign = cdap.findAllActiveCampaings();
		Map<String, HashMap<Long, AggregatedData>> campCodeBucketsMap = new HashMap<String, HashMap<Long, AggregatedData>>();

		for (HitLog hitlog : listtoprocess) {
			hitlog.setProcessed(Constants.HIT_RECORD_PROCESSED);
			if (hitlog.getiSDuplicalte() == 1)
				// Delete from database after 30 sec.
				hitlog.setExpireTimeStamp(System.currentTimeMillis() + 30000);
			else {
				// Delete from database sometime campaign end
				// We will need them to find unique
				Campaign c = activCampaign.get(hitlog.getCampaignCode());
				long exptime = (c != null) ? c.getEndDateUnixTs() + statIns.getRetentionPerion()
						: 2 * StatsConfig.getInstance().getRetentionPerion();
				hitlog.setExpireTimeStamp(exptime);
			}
			(new HitLogDAO()).persist(hitlog);
			// The bucket where it will go in Aggregated Data Table
			Long willFallintoBucket = (hitlog.getServerTS() / DataCompressionTypes.Week.getValue())
					* DataCompressionTypes.Week.getValue();
			HashMap<Long, AggregatedData> buckets = campCodeBucketsMap.get(hitlog.getCampaignCode());
			if (buckets == null) {
				buckets = new HashMap<Long, AggregatedData>();
				campCodeBucketsMap.put(hitlog.getCampaignCode(), buckets);
			}
			AggregatedData aggDataObj = buckets.get(willFallintoBucket);
			if (aggDataObj == null) {
				AggregatedDataId idobj = new AggregatedDataId(willFallintoBucket.longValue(), hitlog.getCampaignCode());
				aggDataObj = new AggregatedData();
				aggDataObj.setId(idobj);
				buckets.put(willFallintoBucket, aggDataObj);
			}
			if (isOpen == 1) {
				aggDataObj.setOpens(aggDataObj.getOpens() + 1);
				if (hitlog.getiSDuplicalte() == 0)
					aggDataObj.setUniqueOpens(aggDataObj.getUniqueClicks() + 1);
				if (hitlog.getDeviceType().equals(Constants.DeviceTypeMobile))
					aggDataObj.setMobileOpens(aggDataObj.getMobileClicks() + 1);
				if (hitlog.getDeviceType().equals(Constants.DeviceTypeTablet))
					aggDataObj.setTabOpens(aggDataObj.getTabClicks() + 1);
				if (hitlog.getDeviceType().equals(Constants.DeviceTypePC))
					aggDataObj.setNormalOpens(aggDataObj.getNormalOpens() + 1);
			} else {
				aggDataObj.setClicks(aggDataObj.getClicks() + 1);
				if (hitlog.getiSDuplicalte() == 0)
					aggDataObj.setUniqueClicks(aggDataObj.getUniqueClicks() + 1);
				if (hitlog.getDeviceType().equals(Constants.DeviceTypeMobile))
					aggDataObj.setMobileClicks(aggDataObj.getMobileClicks() + 1);
				if (hitlog.getDeviceType().equals(Constants.DeviceTypeTablet))
					aggDataObj.setTabClicks(aggDataObj.getTabClicks() + 1);
				if (hitlog.getDeviceType().equals(Constants.DeviceTypePC))
					aggDataObj.setNormalClicks(aggDataObj.getNormalClicks() + 1);
			}
		}

		// Now persist bucket data
		AggregatedDataDao dao = new AggregatedDataDao();

		for (String campaignCode : campCodeBucketsMap.keySet()) {
			HashMap<Long, AggregatedData> buckets = campCodeBucketsMap.get(campaignCode);
			long expiryTime = activCampaign.get(campaignCode) != null
					? activCampaign.get(campaignCode).getEndDateUnixTs() + statIns.getRetentionPerion()
					: 2 * statIns.getRetentionPerion();
			for (Long timestamp : buckets.keySet()) {
				AggregatedData d = buckets.get(timestamp);
				dao.mergeOpenHitWithAggreData(d.getTimeStamp(), campaignCode, d.getOpens(), d.getUniqueOpens(),
						d.getMobileOpens(), d.getTabOpens(), d.getNormalOpens(), d.getClicks(), d.getUniqueClicks(),
						d.getMobileClicks(), d.getTabClicks(), d.getNormalClicks(), expiryTime);
			}
		}

	}

	@Override
	public synchronized void run() {
		try {
			boolean interrupted = false;
			while (!interrupted) {

				try {

					// Part1: Process Hit logs and place them in appropriate
					// bucket
					processHitLog();

				} catch (Throwable e) {

					if (EntityManagerWrapper.isEntityManagerOpeninCurrentThread()) {
						EntityManager em = EntityManagerWrapper.getEntityManager();
						if (em.isJoinedToTransaction())
							em.getTransaction().rollback();
						EntityManagerWrapper.closeEntityManager();
					}
					log.error(e.getMessage(),e);
					Thread.sleep(2000);
				}
				synchronized (this) {
					wait(Constants.DAEMON_POLL_PERIOD);  
				}

			}

		} catch (Throwable e) {
			log.error(e.getMessage(), e);
		}

	}

	public static void testHitLogtoAggregateDataMove() {
		intertTestHitLotToAggregeteData();
		// intertTestDataComressionData();
	}

	/**
	 * This date is created to test bucketing and unique hit count
	 */
	public static void intertTestHitLotToAggregeteData() {
		EntityManagerWrapper.getEntityManager().getTransaction().begin();
		Query q = EntityManagerWrapper.getEntityManager().createNativeQuery("delete from HitLog");

		q.executeUpdate();
		// q = EntityManagerWrapper.getEntityManager().createNativeQuery("delete
		// from AggregatedData");
		// q.executeUpdate();
		CampaignDAO dao = new CampaignDAO();
		Campaign c = new Campaign();
		c.setCampaignCode("EmailCampaign");

		c.setClientName("Test Client");
		c.setDescription("For automated test");
		c.setEndDateUnixTs(System.currentTimeMillis() + 30L * 24L * 86400000L);

		c.setRedirectURL("http://www.google.com");
		c.setStartDateUnixTs(System.currentTimeMillis() - 45L * 24L * 86400000L);
		c.setType(Constants.CAMPAIGN_EMAIL);
		dao.merge(c);
		c = new Campaign();
		c.setCampaignCode("WebCampaign");
		c.setClientName("Test Client");
		c.setDescription("For automated test");
		c.setEndDateUnixTs(System.currentTimeMillis() + 30L * 24L * 86400000L);

		c.setRedirectURL("http://www.google.com");
		c.setStartDateUnixTs(System.currentTimeMillis() - 45L * 24L * 86400000L);
		c.setType(Constants.CAMPAIGN_WEB);

		dao.merge(c);

		long startTime = System.currentTimeMillis() - 2L * 86400L * 1000L;
		for (int i = 0; i < 10; i++) {
			HitLog log = new HitLog();

			startTime = startTime + 40000;
			log.setServerTS(startTime);

			if (System.currentTimeMillis() % 2 == 0) {
				log.setEmail("sharmamaish" + System.currentTimeMillis() % 10 + "yahoo.com");
				log.setCampaignCode("EmailCampaign");
				log.setCookieValue(0);

			} else {
				log.setEmail("");
				log.setCampaignCode("WebCampaign");
				if (System.currentTimeMillis() % 3 == 1)
					log.setCookieValue(0);
				else {
					// log.setiSDuplicalte(1);
					// log.setCookieValue(1);
					log.setCookieValue(0);
				}
			}

			if (System.currentTimeMillis() % 4 == 1)
				log.setDeviceType(Constants.DeviceTypeMobile);
			else if (System.currentTimeMillis() % 4 == 2)
				log.setDeviceType(Constants.DeviceTypeTablet);
			else if (System.currentTimeMillis() % 4 == 3)
				log.setDeviceType(Constants.DeviceTypePC);
			else if (System.currentTimeMillis() % 4 == 4)
				log.setDeviceType(Constants.DeviceTypePC);
			else
				log.setDeviceType(Constants.DeviceTypePC);

			log.setDerivedClinetIP("DevIP" + System.currentTimeMillis() % 4);
			log.setClientIP("ClientIP");
			log.setBrowserFingerPrint("fp" + System.currentTimeMillis() % 4);
			log.setReferrerUrl("");
			if (System.currentTimeMillis() % 3 == 1)
				log.setIsOpenHit(1);
			else
				log.setIsOpenHit(0);
			log.setProcessed(Constants.HIT_RECORD_UNPROCESSED);
			EntityManagerWrapper.getEntityManager().merge(log);
			try {
				Thread.sleep(Constants.DAEMON_POLL_PERIOD);
			} catch (InterruptedException e) {

				e.printStackTrace();
			}

		}
		EntityManagerWrapper.getEntityManager().getTransaction().commit();
		EntityManagerWrapper.closeEntityManager();
	}

}
