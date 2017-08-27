package com.ffdc;

import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.stereotype.Component;

import com.ffdc.daemons.BloomFilterBasedBadRequestScreener;
import com.ffdc.daemons.CompressorDaemon;
import com.ffdc.daemons.IngestorDaemon;

/**
 * Does following tasks 1) Initialize config date 2) Start BAD IP Screener --
 * Store, persist and recognize bad IP address 3) Starts Ingestor Deamon --
 * Ingest raw data in 5 min buckets in Aggregated Data 4) Data Compressor --
 * Compress 5 min bucket date in 15 min bucket and 15 min bucket in 60 min
 * 
 * @author Manish Sharma
 *
 */
@SpringBootApplication
@Component
@Configurable

@EnableAutoConfiguration
@EnableCaching
public class App {

	public static void main(String[] args) {

		InitDB.intialize();
		// start bad ip daemon
		// BadIPScreenerDaemon.start();
		BloomFilterBasedBadRequestScreener.start();
		// Consume raw hit data and bucket it in 5 min aggregates
		IngestorDaemon.start();
		// start daemon to agregate data
		CompressorDaemon.start();

		// star sprint boot application
		SpringApplication.run(App.class, args);

		// StatsDataFilter.test();
		// Resolution.testNext();
		// Resolution.testResulution();
		// DataCompressorDaemon.testHitLogtoAggregateDataMove();

		// (new DataAgrregatorDaemon()).run();

		// while(true)
		// {
		// long s = System.currentTimeMillis();
		// Date d = new Date(s);
		// System.out.println( new Date( 30000 * (s/30000) )+ " " + d);
		// try {
		// Thread.sleep(2000);
		// } catch (InterruptedException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
		// if (s < 0)
		// break;
		// }

	}

}
