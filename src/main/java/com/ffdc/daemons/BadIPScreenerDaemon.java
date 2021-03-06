package com.ffdc.daemons;

import java.util.HashMap;
import java.util.List;
import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnels;

import java.util.Set;

import javax.persistence.EntityManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ffdc.EntityManagerWrapper;
import com.ffdc.DataAccesObjects.BadIDDAO;
import com.ffdc.models.BadIP;
import com.ffdc.utility.Constants;

/**
 * This is designed in a manner that multiple instance of daemons with in same
 * JVM or various JVMs can work togather BadIP has following properties 1) It is
 * kept in memory hashmap and is updated with every request. It has to be fast
 * so not persisted per request 2) In background it is merged with persistant
 * storage. On init Bad Ips are loaded form persistant storage. 3) In persistant
 * storage bad ips might come form more than one JVM. Merge takes care of that.
 * 
 * @author Manish Sharma
 *
 */
public class BadIPScreenerDaemon implements Runnable {
	private static final Log log = LogFactory.getLog(BadIPScreenerDaemon.class);
	public static HashMap<String, BadIP> badIps = new HashMap<>();
	boolean stopped = false;
	
	//Bloom filter to give existance of match with 99% probability
	//1000*1000*1000 is expected number of fileters, 0.99 is probability
	// This bloom filter will store 1 billion items each of 9.6 bit. Total memory usage would be 1.2 GB
	static BloomFilter<CharSequence> bloomFilter99percent = BloomFilter.create(Funnels.unencodedCharsFunnel(), 1000*1000*1000L,0.99); 
			 
	
	
	/**
	 * The set access count of IP greater than MAX_SAFE_COUNT so that is marked
	 * permanently bad
	 * 
	 * @param browserEntproy =  ip addresses and header
	 
	 */
	public static void setBadIPA(String browserFingerPrint) {
		BadIP ip = badIps.get(browserFingerPrint);

		if (ip == null) {
			ip = new BadIP();
		 
			ip.setBrowserFingerPrint(browserFingerPrint);
			ip.setLastAccessTimestamp(System.currentTimeMillis());
			ip.setBeginWindowTimestamp(System.currentTimeMillis());
			ip.setCount(Constants.MAX_SAFE_COUNT + 1);
			badIps.put(browserFingerPrint, ip);

		} else {

			ip.setCount(Constants.MAX_SAFE_COUNT + 1);
		}
	}
	
	 
	public static boolean isBadIPA(String fingerPrint) {
		
		if ( bloomFilter99percent.mightContain(fingerPrint) )
			//Here we are penalizing 1% of geunuine client for performance region.
			return false;
		
		//we cannot avoid has here because we need to update hitcount
		
		BadIP ip = badIps.get(fingerPrint);
		if (ip == null) {
			ip = new BadIP();
		 
			ip.setBrowserFingerPrint(fingerPrint) ;
			ip.setLastAccessTimestamp(System.currentTimeMillis());
			ip.setBeginWindowTimestamp(System.currentTimeMillis());
			ip.setCount(0);
			badIps.put(fingerPrint, ip);
			return false;
		} else {
			if (ip.getCount() > Constants.MAX_SAFE_COUNT)
			{
				// once bad always bad
				bloomFilter99percent.put(fingerPrint);
				badIps.remove(fingerPrint); //no need to maintain in hash
				return true;
			}
			ip.setCount(ip.getCount() + 1);
			ip.setLastAccessTimestamp(System.currentTimeMillis());
			if ((ip.getLastAccessTimestamp() - ip.getBeginWindowTimestamp()) > Constants.SAFE_COUNT_TIME_INTERVAL) {
				ip.setLastAccessTimestamp(System.currentTimeMillis());
				ip.setBeginWindowTimestamp(System.currentTimeMillis());
				ip.setCount(0);
			}
			return false;
		}

	}

	private static void init() {

		try {
			EntityManagerWrapper.getEntityManager().getTransaction().begin();
			BadIDDAO dao = new BadIDDAO();
			List<BadIP> ips = dao.findAll();
			ips.forEach(badip -> badIps.put( badip.getBrowserFingerPrint(), badip));
			if (EntityManagerWrapper.isEntityManagerOpeninCurrentThread()
					&& EntityManagerWrapper.getEntityManager().getTransaction().isActive())
				EntityManagerWrapper.getEntityManager().getTransaction().commit();

			EntityManagerWrapper.closeEntityManager();
		} catch (Throwable e) {
			if (EntityManagerWrapper.isEntityManagerOpeninCurrentThread()
					&& EntityManagerWrapper.getEntityManager().getTransaction().isActive())
				EntityManagerWrapper.getEntityManager().getTransaction().rollback();
			EntityManagerWrapper.closeEntityManager();
		}
	}

	private static void mergeAndClean() {
		HashMap<String, BadIP> badIpsTemp = new HashMap<>();
		try {
			EntityManagerWrapper.getEntityManager().getTransaction().begin();

			BadIDDAO dao = new BadIDDAO();
			dao.cleanold();
			if (EntityManagerWrapper.isEntityManagerOpeninCurrentThread()
					&& EntityManagerWrapper.getEntityManager().getTransaction().isActive())
				EntityManagerWrapper.getEntityManager().getTransaction().commit();

			EntityManagerWrapper.getEntityManager().getTransaction().begin();
			List<BadIP> ips = dao.findAll();
			ips.forEach(badip -> badIpsTemp.put(badip.getBrowserFingerPrint(), badip));

			Set<String> kset = badIps.keySet();
			kset.forEach(keyip -> {
				BadIP formPersitanceSource = badIpsTemp.get(keyip);
				BadIP fromMemory = badIps.get(keyip);
				if (badIpsTemp.containsKey(keyip)) {

					// If count is greater than max safe count we don't care
					if ((formPersitanceSource.getCount() < Constants.MAX_SAFE_COUNT)
							&& (formPersitanceSource.getLastAccessTimestamp() < fromMemory.getLastAccessTimestamp())) {
						badIpsTemp.put(keyip, fromMemory);
						if (!fromMemory.equals(formPersitanceSource))
							dao.update(fromMemory);
					}
				} else {
					if ((fromMemory.getCount() < Constants.MAX_SAFE_COUNT)
							&& fromMemory.getLastAccessTimestamp() > (System.currentTimeMillis()
									- Constants.SAFE_COUNT_TIME_INTERVAL))
						dao.merge(fromMemory);
					// no need to persiste old data
				}

			});
			badIps = badIpsTemp;
			if (EntityManagerWrapper.isEntityManagerOpeninCurrentThread()
					&& EntityManagerWrapper.getEntityManager().getTransaction().isActive())
				EntityManagerWrapper.getEntityManager().getTransaction().commit();
			EntityManagerWrapper.closeEntityManager();
		} catch (Throwable e) {
			if (EntityManagerWrapper.isEntityManagerOpeninCurrentThread()
					&& EntityManagerWrapper.getEntityManager().getTransaction().isActive())
				EntityManagerWrapper.getEntityManager().getTransaction().rollback();
			EntityManagerWrapper.closeEntityManager();
		}
	}

	public static void start() {
		Thread t = new Thread(new BadIPScreenerDaemon());
		t.start();

	}

	@Override
	public void run() {
		try {
			init();

			while (!stopped) {
				try {
					EntityManagerWrapper.getEntityManager().getTransaction().begin();
					mergeAndClean();
					if (EntityManagerWrapper.getEntityManager().getTransaction().getRollbackOnly())
						EntityManagerWrapper.getEntityManager().getTransaction().rollback();
					else
						EntityManagerWrapper.getEntityManager().getTransaction().commit();
					EntityManagerWrapper.closeEntityManager();
				} catch (Throwable e) {
					EntityManager em = EntityManagerWrapper.getEntityManager();
					if (em.isOpen()) {
						if (em.getTransaction().isActive())
							em.getTransaction().rollback();
						EntityManagerWrapper.closeEntityManager();
					}
				}
				synchronized (this) {
					wait(60000); // One minute
				}
			}

		} catch (InterruptedException e) {
			log.error(e);
		}

	}

}
