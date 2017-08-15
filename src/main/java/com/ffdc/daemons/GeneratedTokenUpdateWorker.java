package com.ffdc.daemons;

import javax.persistence.EntityManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ffdc.EntityManagerWrapper;
import com.ffdc.DataAccesObjects.CampaignDAO;

/**
 * Worker for Executor to Async update generated open and click token and used
 * open and click tokens
 * 
 * Update xxx set yy = yy + 1 where something is used here.
 * 
 * @author Manish Sharma
 *
 */
public class GeneratedTokenUpdateWorker implements Runnable {
	private boolean isOpen = false;
	private String campaignCode;
	private static final Log log = LogFactory.getLog(GeneratedTokenUpdateWorker.class);

	/**
	 * Worker
	 */
	@Override
	public void run() {

		log.debug(" Inside run");

		EntityManager manager = EntityManagerWrapper.getEntityManager();
		manager.getTransaction().begin();
		try {
			CampaignDAO campdao = new CampaignDAO();
			if (isOpen)
				campdao.updateGeneratedOpen(campaignCode);
			else
				campdao.updateGeneratedClick(campaignCode);
			manager.getTransaction().commit();
			log.debug(" Finished run Successfully");
		} catch (Throwable e) {
			if (manager.isJoinedToTransaction())
				manager.getTransaction().rollback();
			log.info(e.getMessage());
			log.error(e.getMessage(), e);
		} finally {
			if (EntityManagerWrapper.isEntityManagerOpeninCurrentThread()) {
				EntityManagerWrapper.closeEntityManager();
			}
		}
	}

	/**
	 * Constructor
	 * 
	 * @param isOpen
	 *            Type of campaign
	 * @param campaignCode
	 *            :
	 */
	public GeneratedTokenUpdateWorker(boolean isOpen, String campaignCode) {
		super();
		this.isOpen = isOpen;
		this.campaignCode = campaignCode;
	}

}
