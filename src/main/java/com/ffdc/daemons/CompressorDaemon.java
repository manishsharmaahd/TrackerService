package com.ffdc.daemons;

import java.util.Collection;
import java.util.Date;
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
import com.ffdc.DataAccesObjects.DataCompressionLockDao;
import com.ffdc.models.AggregatedData;
import com.ffdc.models.AggregatedDataId;
import com.ffdc.models.Campaign;
import com.ffdc.stats.DataCompressionTypes;
import com.ffdc.stats.StatsConfig;
import com.ffdc.stats.StatsDataFilter;
import com.ffdc.utility.Constants;

/**
 * This is designed in a manner that multiple instance of daemons with in same
 * JVM or various JVMs can work togather This class performs three task Compress
 * data point with following logic: 1 Week to one month to 15 min interval
 * (configurable) Beyond one - month to hourly interval(configurable)
 * 
 * @author Manish Sharma
 *
 */
public class CompressorDaemon implements Runnable {
	private static final Log log = LogFactory.getLog(CompressorDaemon.class);
	public static CompressorDaemon instance = new CompressorDaemon();
	boolean started = true;

	public boolean isStarted() {
		return started;
	}

	public void setStarted(boolean started) {
		this.started = started;
	}

	/**
	 * Private constructor for singleton
	 */
	private CompressorDaemon() {

	}

	/**
	 * returns instance
	 * 
	 * @return
	 */
	public static CompressorDaemon getInstance() {
		return instance;
	}

	/**
	 * Starts deamon thread
	 */
	public static void start() {
		Thread t = new Thread(instance);
		t.start();
	}

	/**
	 * Cleans up Compress logical lock. this protects against crash of JVM
	 */
	public void compressLockCleanup() {
		DataCompressionLockDao dctDao = new DataCompressionLockDao();
		EntityManagerWrapper.getEntityManager().getTransaction().begin();
		dctDao.cleanup();
		EntityManagerWrapper.getEntityManager().getTransaction().commit();
	}

	/**
	 * Compresses data belonging to 1 week - 1 Month from 5 to 15 min inverval
	 * Steps: 1) Acquire logical log by updating DataCompressionLock 2) Get last
	 * process time stamp from lock. This become start point for the query 3)
	 * Get Max time : Current time - 7 days for 15 min compression and current
	 * time - 30 days 15 min compression 4) Use our sweet StatsDataFiler to give
	 * compress record 5) Delete all low resolution record that are being
	 * processed. 6) Remove all zero records from Output of StatsData filter and
	 * save 7) update last process timestamp for future invokations
	 */
	public void compress(DataCompressionTypes decomressionType) {
		EntityManagerWrapper.getEntityManager().getTransaction().begin();
		CampaignDAO cdao = new CampaignDAO();
		Map<String, Campaign> cmap = cdao.findAllActiveCampaings();
		EntityManagerWrapper.getEntityManager().getTransaction().commit();

		DataCompressionLockDao dctDao = new DataCompressionLockDao();
		long lastProcessTimestamp = 0;
		// Acquire logical lock
		EntityManagerWrapper.getEntityManager().getTransaction().begin();
		lastProcessTimestamp = dctDao.tryAndGetLock(decomressionType);

		EntityManagerWrapper.getEntityManager().getTransaction().commit();

		if (lastProcessTimestamp < 0) {
			log.info("DataCompressionTypes for type " + decomressionType + " is locked");
			return;
		}

		// 15 min to 60 min (it is configurable)
		int interval = decomressionType.getValue();

		// Note - DataCompressionTypes.Beyond.getValue() - 1 We are leaving 2
		// hours to take care of boundary condition.
		// These will picked in next iteration of compressor.
		long currentTimeMillis = System.currentTimeMillis();
		long maxTimeStamp = 0;
		long minTimeStamp = 0;

		long timeStampLastRecord = lastProcessTimestamp;
		EntityManagerWrapper.getEntityManager().getTransaction().begin();
		for (String code : cmap.keySet()) {

			long campaignExpiry = cmap.get(code).getEndDateUnixTs() + StatsConfig.getInstance().getRetentionPerion();

			if (decomressionType == DataCompressionTypes.Month) {
				maxTimeStamp = currentTimeMillis - (7 * 86400 * 1000) - DataCompressionTypes.Beyond.getValue() - 1;
				// 30 days records

				minTimeStamp = currentTimeMillis - (2592000000L); // millsec in
																	// 30 days =
																	// 2592000000L
			} else {
				maxTimeStamp = currentTimeMillis - (2592000000L) - DataCompressionTypes.Beyond.getValue() - 1;
				// 30 days records

				minTimeStamp = cmap.get(code).getStartDateUnixTs(); // millsec
																	// in 30
																	// days =
																	// 2592000000L

			}
			if (lastProcessTimestamp > minTimeStamp)
				minTimeStamp = lastProcessTimestamp;
			AggregatedDataDao dao = new AggregatedDataDao();
			List<AggregatedData> list = dao.getDataToAggregate(code, minTimeStamp, maxTimeStamp);

			// Get the list do comress.
			if (list.size() > 0) {

				Collection<AggregatedData> filterData = StatsDataFilter.filter(list, minTimeStamp, maxTimeStamp,
						interval);

				// Set expiry for high granularity data
				// We have also filtered out records whose timestamp will
				// coincide with timestamp after aggregation
				// This save some databaes calls.
				List<AggregatedData> listToSave = list.stream().filter(obj -> obj.getTimeStamp() % interval != 0)
						.collect(Collectors.toList());

				listToSave.forEach(obj -> {
					// obj.setExpiryInTimeStamp(System.currentTimeMillis() -
					// 50);
					dao.remove(obj);
				});

				// don't want to save zero records in DB
				Collection<AggregatedData> filterDataZeroRemoved = filterData.stream()
						.filter(obj -> (obj.getOpens() > 0 || obj.getClicks() > 0)).collect(Collectors.toList());

				for (AggregatedData obj : filterDataZeroRemoved) {
					obj.setExpiryInTimeStamp(campaignExpiry);
					obj.setForInterval(decomressionType.getValue());
					dao.merge(obj);
					timeStampLastRecord = (timeStampLastRecord < obj.getTimeStamp()) ? obj.getTimeStamp()
							: timeStampLastRecord;

				}

			}

		}
		dctDao.releaseLock(decomressionType, timeStampLastRecord);
		//
		EntityManagerWrapper.getEntityManager().flush();
		EntityManagerWrapper.getEntityManager().getTransaction().commit();

	}

	/**
	 * Deamon thread runs in infinite loop until stops 1) cleans up lock if
	 * process has crashed while doing compression 2) Perform Week - Month
	 * compress 5 - 15 min 3) Perform Month - Campaing start time compression
	 */
	@Override
	public synchronized void run() {
		try {

			while ( started) {

				try {

					// Now start task of data compression
					// Clean the locs
					compressLockCleanup();
					// 15 min compressin
					compress(DataCompressionTypes.Month);
					// 1 hour compression
					compress(DataCompressionTypes.Beyond);

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
					wait(Constants.DAEMON_POLL_PERIOD); // One minute
				}

			}

		} catch (Throwable e) {
			log.error(e);
		}

	}

	/**
	 * Some datae to unit test
	 */
	public static void testHitLogtoAggregateDataMove() {
		// intertTestHitLotToAggregeteData();
		intertTestDataComressionData();
	}

	/**
	 * Some datae to unit test
	 */
	public static void intertTestDataComressionData() {

		long startTimePrefix = ((System.currentTimeMillis() - 60L * 86400000L) / 86400000) * 86400000L - 86400000L;

		AggregatedDataDao dao = new AggregatedDataDao();
		EntityManager em = EntityManagerWrapper.getEntityManager();
		em.getTransaction().begin();
		Query q = EntityManagerWrapper.getEntityManager().createNativeQuery("delete from AggregatedData");
		q.executeUpdate();
		q = EntityManagerWrapper.getEntityManager().createNativeQuery(
				"update DataCompressionLock set TimeStamp =5, InvocatinInstanceId ='Available', AcquireTimeStamp =0");
		q.executeUpdate();

		AggregatedData d = new AggregatedData();

		for (int i = 0; i < 5; i++) {
			d = new AggregatedData();
			d.setId(new AggregatedDataId(startTimePrefix + 15 * 60 * 1000, "WebCampaign"));
			d.setOpens(3);
			d.setClicks(5);
			d.setTimeStatmpStringForTest((new Date(d.getTimeStamp()).toString()));
			d.setForInterval(5 * 60 * 1000);
			dao.merge(d);

			d = new AggregatedData();
			d.setId(new AggregatedDataId(startTimePrefix + 15 * 60 * 1000, "EmailCampaign"));
			d.setOpens(4);
			d.setClicks(5);
			d.setTimeStatmpStringForTest((new Date(d.getTimeStamp()).toString()));
			d.setForInterval(5 * 60 * 1000);
			dao.merge(d);
			startTimePrefix = startTimePrefix + 5 * 60 * 1000;
		}

		startTimePrefix = startTimePrefix + 3600 * 1000; // housr
		d = new AggregatedData();
		d.setId(new AggregatedDataId(startTimePrefix + 15 * 60 * 1000, "WebCampaign"));
		d.setOpens(4);
		d.setClicks(5);
		d.setTimeStatmpStringForTest((new Date(d.getTimeStamp()).toString()));
		d.setForInterval(5 * 60 * 1000);
		dao.merge(d);
		d = new AggregatedData();
		d.setId(new AggregatedDataId(startTimePrefix + 15 * 60 * 1000, "EmailCampaign"));
		d.setOpens(4);
		d.setClicks(5);
		d.setTimeStatmpStringForTest((new Date(d.getTimeStamp()).toString()));
		d.setForInterval(5 * 60 * 1000);
		dao.merge(d);
		startTimePrefix = startTimePrefix + 10 * 60 * 1000; // 10 mintues
		d = new AggregatedData();
		d.setId(new AggregatedDataId(startTimePrefix + 15 * 60 * 1000, "WebCampaign"));
		d.setOpens(4);
		d.setClicks(5);
		d.setTimeStatmpStringForTest((new Date(d.getTimeStamp()).toString()));
		d.setForInterval(5 * 60 * 1000);
		d = new AggregatedData();
		d.setId(new AggregatedDataId(startTimePrefix + 15 * 60 * 1000, "EmailCampaign"));
		d.setOpens(4);
		d.setClicks(5);
		d.setTimeStatmpStringForTest((new Date(d.getTimeStamp()).toString()));
		d.setForInterval(5 * 60 * 1000);

		em.getTransaction().commit();
	}

}
