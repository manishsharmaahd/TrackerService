package com.ffdc.DataAccesObjects;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ffdc.EntityManagerWrapper;
import com.ffdc.models.Campaign;
import com.ffdc.models.CampaignStats;
import com.ffdc.utility.CacheSingleton;
import com.ffdc.utility.Constants;

/**
 * Data Access object for domain model class Campaign. Active campaign are
 * cached using JCSCache to avoid multiple databse hits.
 * 
 * @see com.ffdc.testdata.CampaignDate.ffdc.utility.hibernate.Campaign Evicted
 *      on Campaing update or timeout
 * @author Manish Sharma
 */

public class CampaignDAO {

	private static final Log log = LogFactory.getLog(CampaignDAO.class);

	private final EntityManager entityManager = EntityManagerWrapper.getEntityManager();

	/**
	 * This query used for Campaign statistics. Note that it has separate DTO
	 * class entity CampaignStats to just expose stats related data.
	 * 
	 * @param code
	 * @return
	 */
	public CampaignStats findStatByCampaignCode(String code) {
		// TODO Auto-generated method stub
		log.debug("getting Campaign instance with code: " + code);
		try {
			String querystr = "Select * from Campaign where CampaignCode = '" + code + "'";
			Query query = entityManager.createNativeQuery(querystr, CampaignStats.class);
			@SuppressWarnings("unchecked")
			List<CampaignStats> l = query.getResultList();

			log.debug("findStatByCampaignCode Successful");
			return !l.isEmpty() ? l.get(0) : null;

		} catch (RuntimeException re) {
			log.error("get failed", re);
			throw re;
		}
	}

	/**
	 * Find all
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<Campaign> findAll() {
		log.debug("getting Campaign all Campaigns");
		try {
			List<Campaign> cl = null;

			Query query = entityManager.createNativeQuery("Select * from Campaign", Campaign.class);
			cl = query.getResultList();

			log.debug("findAll successful");
			return cl;
		} catch (RuntimeException re) {
			log.error("get failed", re);
			throw re;
		}
	}

	/**
	 * All active campaigns are cached for 15 minutes in JCS memroy cache for
	 * quick validation.
	 * 
	 * @return
	 */
	public static final String JCSActiveCampaignKey = "JCS";

	@SuppressWarnings("unchecked")
	public Map<String, Campaign> findAllActiveCampaings() {
		log.debug("getting Campaign active  Campaigns");
		try {
			Map<String, Campaign> activeCampaignMap = (Map<String, Campaign>) CacheSingleton.getInstance()
					.get(JCSActiveCampaignKey);
			if (activeCampaignMap == null) {
				List<Campaign> cl = null;
				Query query = entityManager.createNativeQuery("Select * from Campaign where startDateUnixTs < "
						+ System.currentTimeMillis() + " and endDateUnixTs > " + System.currentTimeMillis()
						+ Constants.ActiveGracePeriod * -1, Campaign.class);
				cl = query.getResultList();
				HashMap<String, Campaign> activeCampaignMap2 = new HashMap<>();
				cl.forEach(campainobj -> activeCampaignMap2.put(campainobj.getCampaignCode(), campainobj));
				CacheSingleton.getInstance().put(JCSActiveCampaignKey, activeCampaignMap2);
				// following is wierd thing to do but forEach required new
				// declaration.
				activeCampaignMap = activeCampaignMap2;

			}

			log.debug("findAll successful");
			return activeCampaignMap;
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
	public Campaign findById(int id) {
		log.debug("getting Campaign instance with id: " + id);
		try {
			Campaign instance = entityManager.find(Campaign.class, id);

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
	public Campaign findByCode(String code) {
		log.debug("getting Campaign instance with code: " + code);
		try {
			String querystr = "Select * from Campaign where CampaignCode = '" + code + "'";
			Query query = entityManager.createNativeQuery(querystr, Campaign.class);
			@SuppressWarnings("unchecked")
			List<Campaign> l = query.getResultList();
			log.debug("Finshed getting Campaign instance with code: " + code + "size = " + l.size());
			return !l.isEmpty() ? l.get(0) : null;

		} catch (RuntimeException re) {
			log.error("get failed", re);
			throw re;
		}
	}

	/**
	 * 
	 * @param code
	 */
	public void updateGeneratedOpen(String code) {
		updateCount(code, UpdateCountType.GeneratedOpenTokens);
	}

	/**
	 * 
	 * @param code
	 */
	public void updateGeneratedClick(String code) {
		updateCount(code, UpdateCountType.GeneratedClickTokens);
	}

	/**
	 * 
	 * @param code
	 */
	public void updateUsedOpen(String code) {
		updateCount(code, UpdateCountType.USedOpenTokens);
	}

	/**
	 * 
	 * @param code
	 */
	public void updateUsedClicks(String code) {
		updateCount(code, UpdateCountType.UsedClickTokens);
	}

	/**
	 * 
	 * @author manish sharma
	 *
	 */
	enum UpdateCountType {
		GeneratedOpenTokens, GeneratedClickTokens, USedOpenTokens, UsedClickTokens
	};

	/**
	 * 
	 * @param code
	 * @param typ
	 */
	private void updateCount(String code, UpdateCountType typ) {
		log.debug("getting update count for : " + code);

		try {
			String columnstr = "";
			switch (typ) {
			case GeneratedOpenTokens:
				columnstr = "GeneratedOpenTokens = GeneratedOpenTokens + 1 ";
				break;
			case GeneratedClickTokens:
				columnstr = "GeneratedClickTokens = GeneratedClickTokens + 1 ";
				break;
			case USedOpenTokens:
				columnstr = "UsedOpenTokens = UsedOpenTokens + 1 ";
				break;
			case UsedClickTokens:
				columnstr = "UsedClickTokens = UsedClickTokens + 1 ";
				break;
			default:
				throw new IllegalArgumentException("Appropriate type not supplied");

			}

			String querystr = "update Campaign set " + columnstr + " where CampaignCode = '" + code + "'";
			Query query = entityManager.createNativeQuery(querystr, Campaign.class);

			int result = query.executeUpdate();
			if (result != 1)
				throw new IllegalArgumentException("campaign code " + code + " doesn't exist");

			log.debug("Finished update count for : " + code);

		} catch (RuntimeException re) {
			log.error("get failed", re);
			throw re;
		}
	}

	/**
	 * 
	 * @param transientInstance
	 */
	public void persist(Campaign transientInstance) {
		log.debug("persisting Campaign instance");
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
	public void remove(Campaign persistentInstance) {
		log.debug("removing Campaign instance");
		try {
			((CampaignDAO) entityManager).remove(persistentInstance);
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

	public Campaign merge(Campaign detachedInstance) {
		log.debug("merging Campaign instance");
		try {
			Campaign result = entityManager.merge(detachedInstance);
			log.debug("merge successful");
			return result;
		} catch (RuntimeException re) {
			log.error("merge failed", re);
			throw re;
		}
	}

	/**
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<CampaignStats> findAllStats() {
		log.debug("getting Campaign all Campaigns Stats");
		try {
			List<CampaignStats> cl = null;

			Query query = entityManager.createNativeQuery("Select * from Campaign", CampaignStats.class);
			cl = query.getResultList();

			log.debug("findAllStats successful");
			return cl;
		} catch (RuntimeException re) {
			log.error("get failed", re);
			throw re;
		}
	}

}
