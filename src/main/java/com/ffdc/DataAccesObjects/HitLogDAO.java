package com.ffdc.DataAccesObjects;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ffdc.EntityManagerWrapper;
import com.ffdc.models.HitLog;
import com.ffdc.stats.StatsConfig;
import com.ffdc.utility.Constants;

/**
 * Home object for domain model class Hit.
 * 
 * @see com.ffdc.models.HitLog.Hit
 * @author Manish Sharma
 */

public class HitLogDAO {

	private static final Log log = LogFactory.getLog(HitLogDAO.class);

	private final EntityManager entityManager = EntityManagerWrapper.getEntityManager();

	/**
	 * In case of crash of process of thread, hitlog records will remain in
	 * PROCESSING state. When timeout is expired, the status of these threads is
	 * set back to unprocessed.
	 */
	public void cleanupTimedout() {

		log.debug("inside cleanupTimedout");

		EntityManager em = EntityManagerWrapper.getEntityManager();
		String acquireQuery = " UPDATE hitlog SET processed = " + Constants.HIT_RECORD_UNPROCESSED
				+ "  , ProcessingInstanceUID  =  ''" + ", ProcessingTimoutTime   = null" + " WHERE processed= "
				+ Constants.HIT_RECORD_PROCESSING + "  and ProcessingTimoutTime < " + System.currentTimeMillis();
		Query q = em.createNativeQuery(acquireQuery);

		q.executeUpdate();
		log.debug("exiting cleanupTimedout success");
	}

	/**
	 * This put a logical locks on the rows so that they are not processed by
	 * other instance. Changes the status to Processing Updates processing
	 * timeout for cleanup Updates Processing Instance id as uuid of this
	 * invocation Processing Instance Id plays a key role in logical lock
	 * 
	 * @param isOpen
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<HitLog> acquireForProcessing(int isOpen) {

		EntityManager em = EntityManagerWrapper.getEntityManager();

		String acquireQuery = " UPDATE hitlog SET processed = " + Constants.HIT_RECORD_PROCESSING
				+ "  , ProcessingInstanceUID  =  '" + StatsConfig.getInstance().getJVMInstaceInvokationID()
				+ "', ProcessingTimoutTime   = "
				+ (System.currentTimeMillis() + StatsConfig.getInstance().getHitlogProcessingtimeout())
				+ " WHERE  id IN (  SELECT id FROM ( " + " SELECT id FROM hitlog WHERE IsOpenHit = " + isOpen
				+ " and  processed = " + Constants.HIT_RECORD_UNPROCESSED + " ORDER BY id ASC  " + " LIMIT 0,  "
				+ StatsConfig.getInstance().getHitProcesBatchSizeLimit() + " ) tmp" + ")";
		Query q = em.createNativeQuery(acquireQuery);

		int rowstobeprocessed = q.executeUpdate();

		if (rowstobeprocessed > 0) {

			String selectQuery = "Select * from hitlog where processed  = " + Constants.HIT_RECORD_PROCESSING
					+ " and ProcessingInstanceUID = '" + StatsConfig.getInstance().getJVMInstaceInvokationID()
					+ "' and ProcessingTimoutTime >  " + (System.currentTimeMillis() + 30);

			q = em.createNativeQuery(selectQuery, HitLog.class);
			@SuppressWarnings("rawtypes")
			List retlist = q.getResultList();

			return retlist;

		} else
			return new ArrayList<HitLog>();

	}

	/**
	 * This find duplicate based on device fingerprint or email id. It check
	 * from campaign start time to the request time for duplicate It marks the
	 * setIsDuplicate flag in the HitLog object - doesn't persist it. It is the
	 * responsibility of the caller
	 * 
	 * @param listToProcess
	 *            : List to fetch duplicate from
	 * @param isOpen
	 *            0 is click 1 is click
	 */

	public void findAndMarkDuplicate(List<HitLog> listToProcess, int isOpen) {

		log.debug("Start findAndMarkDuplicate");

		// For email campaign
		List<HitLog> emailList = listToProcess.stream().filter(a -> (!a.getEmail().isEmpty()))
				.collect(Collectors.toList());
		for (HitLog emailhit : emailList) {
			// Using LIMIT 0. It would be more efficient than using Exists or
			// even aggregating the queries.
			String query = "SELECT *  FROM hitlog where isOpenHit = " + isOpen + " and CampaignCode = '"
					+ emailhit.getCampaignCode() + "' and email = '" + emailhit.getEmail() + "' and ServerTS < "
					+ emailhit.getServerTS() + " LIMIT 1 ";

			Query q = entityManager.createNativeQuery(query, HitLog.class);
			@SuppressWarnings("rawtypes")
			List retlist = q.getResultList();
			if (retlist.size() > 0) {
				emailhit.setiSDuplicalte(1);
			}

		}
		log.debug("Finished findAndMarkDuplicate Email Campaign part");
		// For email campaign
		List<HitLog> webList = listToProcess.stream().filter(a -> (a.getEmail().isEmpty()))
				.collect(Collectors.toList());
		for (HitLog webhit : webList) {
			// Using LIMIT 0. It would be more efficient than using Exists or
			// even aggregating the queries.
			String query = "SELECT *  FROM hitlog where isOpenHit = " + isOpen + " and CampaignCode = '"
					+ webhit.getCampaignCode() + "' and Email = '' and BrowserFingerPrint = '"
					+ webhit.getBrowserFingerPrint() + "' and ServerTS < " + webhit.getServerTS() + " LIMIT 1 ";

			Query q = entityManager.createNativeQuery(query, HitLog.class);
			@SuppressWarnings("rawtypes")
			List retlist = q.getResultList();
			if (retlist.size() > 0) {
				webhit.setiSDuplicalte(1);
			}

		}
		log.debug("Finished findAndMarkDuplicate Web Campaign part");
		log.debug("Exiting findAndMarkDuplicate Success");

	}

	/**
	 * Delete the record that are expired
	 */
	public void purgeExpiredRecors() {

		EntityManager em = EntityManagerWrapper.getEntityManager();
		String acquireQuery = " delete form hitlog where ExpireTimeStamp < " + (System.currentTimeMillis() - 30000);
		Query q = em.createNativeQuery(acquireQuery);
		q.executeUpdate();

	}

	/**
	 * 
	 * @param limit
	 *            : max number of record required
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<HitLog> findAll(int limit) {
		log.debug("getting Hit all Hits");
		try {
			List<HitLog> cl = null;

			String qr = "Select * from HitLog ";
			if (limit > 0)
				qr = qr + " Limit " + limit;

			Query query = entityManager.createNativeQuery(qr, HitLog.class);
			cl = query.getResultList();

			log.debug("findAll successful");
			return cl;
		} catch (RuntimeException re) {
			log.error("get failed", re);
			throw re;
		}
	}

	/**
	 * All active Hits are cached for 15 minutes in JCS memroy cache for quick
	 * validation.
	 * 
	 * @return
	 */

	public HitLog findById(int id) {
		log.debug("getting Hit instance with id: " + id);
		try {
			HitLog instance = entityManager.find(HitLog.class, id);

			return instance;
		} catch (RuntimeException re) {
			log.error("get failed", re);
			throw re;
		}
	}

	/**
	 * 
	 * @param code
	 * @return
	 */
	public HitLog findByCode(String code) {
		log.debug("getting Hit instance with code: " + code);
		try {
			String querystr = "Select * from Hit where CampaignCode = '" + code + "'";
			Query query = entityManager.createNativeQuery(querystr, HitLog.class);
			@SuppressWarnings("unchecked")
			List<HitLog> l = query.getResultList();
			log.debug("Finshed getting Hit instance with code: " + code + "size = " + l.size());
			return !l.isEmpty() ? l.get(0) : null;

		} catch (RuntimeException re) {
			log.error("get failed", re);
			throw re;
		}
	}

	/**
	 * 
	 * @param transientInstance
	 */
	public void persist(HitLog transientInstance) {
		log.debug("persisting Hit instance");
		try {
			entityManager.persist(transientInstance);
			log.debug("persist successful");
		} catch (RuntimeException re) {
			log.error("persist failed", re);
			throw re;
		}
	}

	/**
	 * 
	 * @param persistentInstance
	 */
	public void remove(HitLog persistentInstance) {
		log.debug("removing Hit instance");
		try {
			((HitLogDAO) entityManager).remove(persistentInstance);
			log.debug("remove successful");
		} catch (RuntimeException re) {
			log.error("remove failed", re);
			throw re;
		}
	}

	/**
	 * 
	 * @param detachedInstance
	 * @return
	 */
	public HitLog merge(HitLog detachedInstance) {
		log.debug("merging Hit instance");
		try {
			HitLog result = entityManager.merge(detachedInstance);
			log.debug("merge successful");
			return result;
		} catch (RuntimeException re) {
			log.error("merge failed", re);
			throw re;
		}
	}

}
