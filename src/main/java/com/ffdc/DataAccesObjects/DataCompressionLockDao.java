package com.ffdc.DataAccesObjects;

import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ffdc.EntityManagerWrapper;
import com.ffdc.models.DataCompressionLock;
import com.ffdc.models.HitLog;
import com.ffdc.stats.DataCompressionTypes;
import com.ffdc.stats.StatsConfig;

/**
 * Data Access object for domain model class Hit.
 * 
 * @see com.ffdc.models.HitLog.Hit
 * @author Manish Sharma
 */

public class DataCompressionLockDao {

	private static final Log log = LogFactory.getLog(DataCompressionLockDao.class);

	private final EntityManager entityManager = EntityManagerWrapper.getEntityManager();

	/**
	 * Puts a logical lock using InvocatinInstanceId "Available' means instance
	 * can be locked If invocation uuid is present means instance has been
	 * locked by other caller Set invocation id to current instance and acquire
	 * time stamp to curren time
	 * 
	 * @param isOpen
	 * @return -1 means lock failed else return the time stamp of last processed
	 *         record
	 */
	public long tryAndGetLock(DataCompressionTypes type) {

		EntityManager em = EntityManagerWrapper.getEntityManager();
		log.debug("Inside tyrAndgetLock for " + type);

		// List<DataCompressionLock> dlist = findAll();
		// dlist.forEach(a->System.out.println(a));

		String acquireQuery = "update DataCompressionLock set InvocatinInstanceId = '"
				+ StatsConfig.getInstance().getJVMInstaceInvokationID() + "', AcquireTimeStamp = "
				+ System.currentTimeMillis() + " where InvocatinInstanceId = 'Available' and Type = " + type.getValue();
		Query q = em.createNativeQuery(acquireQuery);
		log.debug("Finsihed update tyrAndgetLock for " + type);

		int rowstobeprocessed = q.executeUpdate();

		if (rowstobeprocessed == 1) {

			String selectQuery = "Select * from DataCompressionLock where InvocatinInstanceId = '"
					+ StatsConfig.getInstance().getJVMInstaceInvokationID() + "' and Type  =" + type.getValue();

			q = em.createNativeQuery(selectQuery, DataCompressionLock.class);
			DataCompressionLock lock = (DataCompressionLock) q.getSingleResult();

			entityManager.refresh(lock);
			log.debug("Finsihed select and exiting tyrAndgetLock for " + type);
			return lock.getTimeStamp();

		} else
			return -1;

	}

	/**
	 * Sets invocation id to Available and also set Last record time stamp
	 * 
	 * @param type
	 * @return
	 */

	public void releaseLock(DataCompressionTypes type, long lastrecordtimestamp) {

		EntityManager em = EntityManagerWrapper.getEntityManager();

		String acquireQuery = "update DataCompressionLock set InvocatinInstanceId = 'Available', Timestamp = "
				+ lastrecordtimestamp + " where InvocatinInstanceId = '"
				+ StatsConfig.getInstance().getJVMInstaceInvokationID() + "' and Type = " + type.getValue();
		Query q = em.createNativeQuery(acquireQuery);
		log.debug("Inside  releaseLock " + type);
		int rowstobeprocessed = q.executeUpdate();
		log.debug("Exiting  releaseLock " + type);
		if (rowstobeprocessed != 1)
			throw new Error("releaseLock : There should be exaclty one row something is wrong");

	}

	/**
	 * If DataCompression thread crashes.. Clean up timeup frees the lock so
	 * that other instance can pick up the task
	 */
	public void cleanup() {

		 log.debug("Inside  cleanup " );
		EntityManager em = EntityManagerWrapper.getEntityManager();

		String acquireQuery = "update DataCompressionLock set InvocatinInstanceId = 'Available' where InvocatinInstanceId = '"
				+ StatsConfig.getInstance().getJVMInstaceInvokationID() + "' and TimeStamp <  "
				+ (System.currentTimeMillis() - StatsConfig.getInstance().getDataCompressionLocktimeout());
		
		Query q = em.createNativeQuery(acquireQuery);

		q.executeUpdate();
		log.debug("Exiting cleanup " );
	}

	/**
	 * fina all
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<DataCompressionLock> findAll() {
		log.debug("getting Hit all DataCompressionLock");
		try {
			List<DataCompressionLock> cl = null;

			Query query = entityManager.createNativeQuery("Select * from DataCompressionLocK",
					DataCompressionLock.class);
			cl = query.getResultList();

			log.debug("findAll successful");
			return cl;
		} catch (RuntimeException re) {
			log.error("get failed", re);
			throw re;
		}
	}

	/**
	 * 
	 * @param id
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
			((DataCompressionLockDao) entityManager).remove(persistentInstance);
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