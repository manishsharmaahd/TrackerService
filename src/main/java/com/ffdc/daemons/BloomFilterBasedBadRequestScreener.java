package com.ffdc.daemons;

import java.util.HashMap;
import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnels;

import orestes.bloomfilter.FilterBuilder;
import orestes.bloomfilter.memory.CountingBloomFilterMemory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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
public class BloomFilterBasedBadRequestScreener implements Runnable {
	private static final Log log = LogFactory.getLog(BadIPScreenerDaemon.class);
	public static HashMap<String, BadIP> badIps = new HashMap<>();
	boolean stopped = false;

	// Bloom filter to give existance of match with 99% probability
	// 1000*1000*1000 is expected number of fileters, 0.99 is probability
	// This bloom filter will store 1 billion items each of 9.6 bit. Total
	// memory usage would be 1.2 GB
	static BloomFilter<CharSequence> permanetBackListBloomFilter = BloomFilter.create(Funnels.unencodedCharsFunnel(),
			1000 * 1000 * 1000L, 0.99);

	// Counting Bloom filter for counting hits in a given interval.
	// this filter is would work almost without collisions for almost 1 million
	// hists every five minutes on a single commodity server
	static FilterBuilder fb = new FilterBuilder(10000000, 0.005).countingBits(16);
	static CountingBloomFilterMemory<String> countingFilter = new CountingBloomFilterMemory<>(fb);

	/**
	 * The set access count of IP greater than MAX_SAFE_COUNT so that is marked
	 * permanently bad
	 * 
	 * @param browserEntproy
	 *            = ip addresses and header
	 * 
	 */
	public static void setBadIP(String browserFingerPrint) {
		permanetBackListBloomFilter.put(browserFingerPrint);
	}

	public static boolean isBadIP(String fingerPrint) {

		if (permanetBackListBloomFilter.mightContain(fingerPrint))
			// Here we are penalizing 1% of genuine client for performance
			// region.
			return true;

		if (countingFilter.addAndEstimateCount(fingerPrint) > Constants.MAX_SAFE_COUNT) {
			permanetBackListBloomFilter.put(fingerPrint);
			return true;
		}
		return false;

	}

	public static void start() {
		Thread t = new Thread(new BadIPScreenerDaemon());
		t.start();

	}

	@Override
	public void run() {
		try {

			while (!stopped) {
				synchronized (this) {
					wait(Constants.SAFE_COUNT_TIME_INTERVAL);
					countingFilter.clear();

				}
			}

		} catch (InterruptedException e) {
			log.error(e);
		}

	}

	private static final String ALPHA_NUMERIC_STRING = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

	public static String randomAlphaNumeric(int count) {
		StringBuilder builder = new StringBuilder();
		while (count-- != 0) {
			int character = (int) (Math.random() * ALPHA_NUMERIC_STRING.length());
			builder.append(ALPHA_NUMERIC_STRING.charAt(character));
		}
		return builder.toString();
	}

	public static void tuneCBF() {
		FilterBuilder fb = new FilterBuilder(10000000, 0.005).countingBits(16);
		CountingBloomFilterMemory<String> filter = new CountingBloomFilterMemory<>(fb);
		for (int i = 0; i < 20000000 / 3; i++) {
			filter.add(randomAlphaNumeric(5));

		}
		System.out.println("Estimated count = " + filter.getEstimatedCount("test"));
		for (int i = 0; i < 10000; i++) {
			filter.add("test");
			if (i % 100 == 0)
				System.out.println("Estimated count = " + filter.getEstimatedCount("test"));
		}
	}
}