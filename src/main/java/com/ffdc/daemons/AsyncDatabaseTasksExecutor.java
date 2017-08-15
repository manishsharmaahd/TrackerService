package com.ffdc.daemons;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 
 * Executes database tasks in a thread pool. For sqlite in memory we must have
 * thread pool of two/three max because of JDBC limitations
 * 
 * @author Manish Sharma
 *
 */
public class AsyncDatabaseTasksExecutor {

	private static final int NTHREDS = 2;
	public static final ExecutorService EXECUTOR = Executors.newFixedThreadPool(NTHREDS);
}
