package com.ffdc.DataAccesObjects;
 

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ffdc.EntityManagerWrapper;
import com.ffdc.models.BadIP;
import com.ffdc.utility.Constants;

/**
 * 
 * 
 * @see com.ffdc.models.hibernate.BadIP
 * @author Manish Sharma
 */

public class BadIDDAO {

	private static final Log log = LogFactory.getLog(BadIDDAO.class);
	/**
	 * Entity manager associated with calling thread
	 */
	private final EntityManager entityManager = EntityManagerWrapper.getEntityManager();

	/**
	 * Deletes the rows that have access count is below MAX_SAFE_COUNT
	 */
	public void cleanold() {
		log.debug("begin cleandold");
		try {
			int cl = 0;

			String querystring = "delete from BadIP where count < " + Constants.MAX_SAFE_COUNT
					+ " and lastAccessTimestamp < " + (System.currentTimeMillis() - Constants.SAFE_COUNT_TIME_INTERVAL);
			Query query = entityManager.createNativeQuery(querystring);
			cl = query.executeUpdate();

			log.debug("begin cleanold success + rows delted = " + cl);

		} catch (RuntimeException re) {
			log.error("get failed", re);
			throw re;
		}
	}

	/**
	 * update time stamp and count of BadIP
	 * 
	 * @param instance
	 */
	public void update(BadIP instance) {
		log.debug("begin update");
		try {
			int cl = 0;

			String querystring = "update BadIP  set count = " + instance.getCount() + ", beginWindowTimestamp = "
					+ instance.getBeginWindowTimestamp() + ",lastAccessTimestamp = " + instance.getLastAccessTimestamp()
					+ " where BrowserFingerPrint = '" + instance.getBrowserFingerPrint() + "'";

			Query query = entityManager.createNativeQuery(querystring);
			cl = query.executeUpdate();

			log.debug("begin cleanold success + rows delted = " + cl);

		} catch (RuntimeException re) {
			log.error("get failed", re);
			throw re;
		}
	}

	/**
	 * find all bad ips
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<BadIP> findAll() {
		log.debug("getting BadIP all BadIPs");
		try {
			List<BadIP> cl = null;

			Query query = entityManager.createNativeQuery("Select * from BadIP", BadIP.class);
			cl = query.getResultList();

			log.debug("findAll successful");
			return cl;
		} catch (RuntimeException re) {
			log.error("get failed", re);
			throw re;
		}
	}

	/**
	 * find by id
	 * 
	 * @param id
	 * @return
	 */
	public BadIP findById(int id) {
		log.debug("getting BadIP instance with id: " + id);
		try {
			BadIP instance = entityManager.find(BadIP.class, id);

			return instance;
		} catch (RuntimeException re) {
			log.error("get failed", re);
			throw re;
		}
	}

	/**
	 * save
	 * 
	 * @param transientInstance
	 */
	public void persist(BadIP transientInstance) {
		log.debug("persisting BadIP instance");
		try {
			entityManager.persist(transientInstance);
			log.debug("persist successful");
		} catch (RuntimeException re) {
			log.error("persist failed", re);
			throw re;
		}
	}

	/**
	 * delete
	 * 
	 * @param persistentInstance
	 */
	public void remove(BadIP persistentInstance) {
		log.debug("removing BadIP instance");
		try {
			((BadIDDAO) entityManager).remove(persistentInstance);
			log.debug("remove successful");
		} catch (RuntimeException re) {
			log.error("remove failed", re);
			throw re;
		}
	}

	/*
	 * insert/update
	 */
	public BadIP merge(BadIP detachedInstance) {
		log.debug("merging BadIP instance");
		try {
			BadIP result = entityManager.merge(detachedInstance);
			log.debug("merge successful");
			return result;
		} catch (RuntimeException re) {
			log.error("merge failed", re);
			throw re;
		}
	}

}
