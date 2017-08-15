package com.ffdc.daemons;

import javax.persistence.EntityManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ffdc.EntityManagerWrapper;
import com.ffdc.DataAccesObjects.HitLogDAO;

/**
 * Cleans up records that are expired When records are saved or compressed their
 * expiry date is also saved with them. Default expiry date is campaign end date
 * + retention period. For duplicate record expiry date is set to in past as
 * soon as they are ingested into Aggregate date table For unique records,
 * expiry date is always campaign end date + retention period.
 * 
 * @author Manish Sharma
 *
 */
public class DataPrugerDaemon implements Runnable {
	private static final Log log = LogFactory.getLog(DataPrugerDaemon.class);
	boolean started = false;

	/**
	 * Starts daemon thread
	 */
	public static void start() {
		Thread t = new Thread(new DataPrugerDaemon());
		t.start();
	}

	@Override
	public void run() {
		try {

			while (!started) {
				try {
					EntityManagerWrapper.getEntityManager().getTransaction().begin();
					HitLogDAO d = new HitLogDAO();
					d.purgeExpiredRecors();
					EntityManagerWrapper.getEntityManager().getTransaction().commit();

				} catch (Throwable e) {

					if (EntityManagerWrapper.isEntityManagerOpeninCurrentThread()) {
						EntityManager em = EntityManagerWrapper.getEntityManager();
						if (em.isJoinedToTransaction())
							em.getTransaction().rollback();
						EntityManagerWrapper.closeEntityManager();
					}
					log.error(e.getMessage());
					Thread.sleep(2000);
				}
				synchronized (this) {
					wait(60000); // One minute
				}
			}

		} catch (Throwable e) {
			log.error(e);
		}

	}

}
