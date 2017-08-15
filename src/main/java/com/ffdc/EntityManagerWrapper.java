package com.ffdc;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.NoResultException;
import javax.persistence.Persistence;
import javax.persistence.Query;

/**
 * Since we are using JPA Entity Manager in Daemon threads, we can rely on
 * container managed cycle. Life cycle of EntityManager using thread local
 * variables is provided through this Wrapper.
 * 
 * Makes it convenient for methods in thread to use single instance of Entity
 * Manager, throughout the flow
 * 
 * @author Manish Sharma
 *
 */
public class EntityManagerWrapper {
	private static final String PERSISTENCE_UNIT_NAME = "PERSISTENCE";
	private static final EntityManagerFactory myFactory;
	private static final ThreadLocal<EntityManager> threadLocal;

	static {
		myFactory = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME);
		threadLocal = new ThreadLocal<EntityManager>();
	}

	/**
	 * Create thread new Entity manager and stores in thread local variable. In
	 * subsequent request return same entity manager unless entity manageris
	 * explicitly colsed
	 * 
	 * @return
	 */
	public static EntityManager getEntityManager() {
		EntityManager myentitymanager = threadLocal.get();

		if (myentitymanager == null) {
			myentitymanager = myFactory.createEntityManager();
			// set your flush mode here
			threadLocal.set(myentitymanager);
		}
		return myentitymanager;
	}

	/**
	 * checks if enity manager is currently active
	 * 
	 * @return
	 */
	public static boolean isEntityManagerOpeninCurrentThread() {
		EntityManager em = threadLocal.get();
		if (em != null) {
			return true;
		}
		return false;
	}

	/**
	 * closes entity manager and clears Tthat from thread local variable
	 */
	public static void closeEntityManager() {
		EntityManager em = threadLocal.get();
		threadLocal.set(null);
		if (em != null) {
			if (em.isOpen())
				em.close();

		}
	}

	public static void shutdown() {
		myFactory.close();
	}

	@SuppressWarnings("unchecked")
	public static <T> T callNativeQuerySingleResult(Query q) {
		try {
			return (T) q.getSingleResult();
		} catch (NoResultException e) {
			return null;
		}

	}
}
