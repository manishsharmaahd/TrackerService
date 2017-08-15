package com.ffdc.DataAccesObjects;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.ffdc.EntityManagerWrapper;
import com.ffdc.models.AggregatedData;
import com.ffdc.models.AggregatedDataId;
import com.ffdc.stats.DataCompressionTypes;

/**
 * Dataaccesss mediator for AggregatedData
 * 
 * @author Manish Sharma
 *
 */
public class AggregatedDataDao {

	private static final Log log = LogFactory.getLog(AggregatedDataDao.class);
	/**
	 * Entity manager associated with calling thread
	 */
	private final EntityManager entityManager = EntityManagerWrapper.getEntityManager();

	/**
	 * We have to be very careful that two simultaneous thread processing HitLog
	 * doesn't corrupt or overwrite Aggregated Data count
	 * 
	 * 1) we do in transaction 2) we use construct update xxx set yyy = yyy + 1
	 * for update to ensure that we are not throwing away counts from previous
	 * run or other thread /process
	 * 
	 * @param timeStamp
	 * @param campaignCode
	 * @param opens
	 * @param uniqueOpens
	 * @param mobileOpens
	 * @param tabOpens
	 * @param normalOpens
	 * @param clicks
	 * @param uniqueClicks
	 * @param mobileClicks
	 * @param tabClicks
	 * @param normalClicks
	 * @param exirytimestamp
	 */
	public void mergeOpenHitWithAggreData(long timeStamp, String campaignCode, int opens, int uniqueOpens,
			int mobileOpens, int tabOpens, int normalOpens, int clicks, int uniqueClicks, int mobileClicks,
			int tabClicks, int normalClicks, long exirytimestamp) {

		
		String findQuery = "Select * from AggregatedData where Timestamp =  " + timeStamp + " and CampaignCode = '"
				+ campaignCode + "'";
		String insQuery = "insert into  AggregatedData (Timestamp  ,CampaignCode ,Opens  ,UniqueOpens  ,MobileOpens  ,TabOpens  ,NormalOpens  , "
				+ "Clicks  ,UniqueClicks  ,MobileClicks  ,TabClicks  ,NormalClicks  , "

				+ "ExpiryInTimeStamp  ,ForInterval   ) values (" + timeStamp + ", '" + campaignCode + "', " + opens
				+ "," + uniqueOpens + "," + mobileOpens + "," + tabOpens + "," + normalOpens + "," + clicks + ","
				+ uniqueClicks + "," + mobileClicks + "," + tabClicks + "," + normalClicks + "," + exirytimestamp + ","
				+ DataCompressionTypes.Week.getValue() + ")";
		log.debug("Begin mergeOpenHitWithAggreData" + findQuery);
		String updateQuery = "update AggregatedData set (Opens  ,UniqueOpens  ,MobileOpens  ,TabOpens  ,NormalOpens  ,Clicks  ,UniqueClicks  ,MobileClicks  ,TabClicks  ,"
				+ " NormalClicks ) = (Opens + " + opens + "  ,UniqueOpens + " + uniqueOpens + " ,MobileOpens + "
				+ mobileOpens + " ,TabOpens +  " + tabOpens + ",NormalOpens + " + normalOpens + ",Clicks + " + clicks
				+ ",UniqueClicks + " + uniqueClicks + " ,MobileClicks + " + mobileClicks + " ," + "TabClicks + "
				+ tabClicks + " ,NormalClicks= " + normalClicks + ") where CampaignCode ='" + campaignCode
				+ "' and Timestamp =  " + timeStamp;

		Query q = entityManager.createNativeQuery(findQuery, AggregatedData.class);
		if (q.getResultList().isEmpty()) {
			Query qry = entityManager.createNativeQuery(insQuery);
			int res = qry.executeUpdate();
			log.debug("Fine done mergeOpenHitWithAggreData"  );
			if (res != 1) {
				log.error("DataBase Error in inserting record in AggregatedData table for  " + campaignCode
						+ "' Timestamp =  " + timeStamp);
				throw new Error("DataBase Error in inserting record in AggregatedData table for  " + campaignCode
						+ "' Timestamp =  " + timeStamp);
			}
		} else {
			Query qry = entityManager.createNativeQuery(updateQuery);

			int res = qry.executeUpdate();
			log.debug("Update done exiting mergeOpenHitWithAggreData. " );
			if (res != 1) {
				log.error("DataBase Error in updating record in AggregatedData table for  " + campaignCode
						+ "' Timestamp =  " + timeStamp);
				throw new Error("DataBase Error in updating record in AggregatedData table for  " + campaignCode
						+ "' Timestamp =  " + timeStamp);

			}
		}

	}

	/**
	 * Give list of Aggregate data object to be compressed
	 * 
	 * @param campaignCode
	 * @param min
	 * @param max
	 * @return
	 */
	public List<AggregatedData> getDataToAggregate(String campaignCode, long min, long max) {
		String sQuery = "Select * from AggregatedData where TimeStamp > " + min + " and TimeStamp <= " + max
				+ " and campaignCode = '" + campaignCode + "' order by TimeStamp";// +
		log.debug("inside getDatatoAggregate");									
		Query qry = entityManager.createNativeQuery(sQuery, AggregatedData.class);
		@SuppressWarnings("unchecked")
		List<AggregatedData> lst = qry.getResultList();
		log.debug("success exiting getDatatoAggregate");		
		return lst;
	}

	/**
	 * Retrieve all object fo given limit
	 * 
	 * @param limit
	 * @return
	 */

	@SuppressWarnings("unchecked")
	public List<AggregatedData> findAll(int limit) {
		log.debug("getting all Aggregated Data");

		try {
			List<AggregatedData> cl = null;

			String qr = "Select * from AggregatedData ";
			if (limit > 0)
				qr = qr + " Limit " + limit;

			Query query = entityManager.createNativeQuery(qr, AggregatedData.class);
			cl = query.getResultList();

			log.debug("findAll successful");
			return cl;
		} catch (RuntimeException re) {
			log.error("get failed", re);
			throw re;
		}
	}

	/**
	 * Saves object
	 * 
	 * @param transientInstance
	 */
	public void persist(AggregatedData transientInstance) {
		log.debug("persisting AgregatedData instance");
		try {
			entityManager.persist(transientInstance);
			log.debug("persist successful");
		} catch (RuntimeException re) {
			log.error("persist failed", re);
			throw re;
		}
	}

	/**
	 * Deletes object
	 * 
	 * @param persistentInstance
	 */
	public void remove(AggregatedData persistentInstance) {
		log.debug("removing AgregatedData instance");
		try {
			entityManager.remove(persistentInstance);
			log.debug("remove successful");
		} catch (RuntimeException re) {
			log.error("remove failed", re);
			throw re;
		}
	}

	/**
	 * Insert or update object
	 * 
	 * @param detachedInstance
	 * @return
	 */
	public AggregatedData merge(AggregatedData detachedInstance) {
		log.debug("merging AgregatedData instance");
		try {
			AggregatedData result = entityManager.merge(detachedInstance);
			log.debug("merge successful");
			return result;
		} catch (RuntimeException re) {
			log.error("merge failed", re);
			throw re;
		}
	}

	/**
	 * Find by ID
	 * 
	 * @param id
	 * @return
	 */
	public AggregatedData findById(AggregatedDataId id) {
		log.debug("getting AgregatedData instance with id: " + id);
		try {
			AggregatedData instance = entityManager.find(AggregatedData.class, id);
			log.debug("get successful");
			return instance;
		} catch (RuntimeException re) {
			log.error("get failed", re);
			throw re;
		}
	}

}
