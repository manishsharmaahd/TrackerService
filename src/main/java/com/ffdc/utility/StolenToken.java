package com.ffdc.utility;

 

import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnels;
/**
 * The hacker will only steel few tokens and use them for attack.
 * Here we store the tokens that are stolen
 * A Huge TODO: 
 * Update from DB to make it durable and get stolen tokens from other instances 
 * 
 * @author Manish Sharma
 *
 */
public class StolenToken {
 
	static BloomFilter<CharSequence> bloomFilter99percent = BloomFilter.create(Funnels.unencodedCharsFunnel(), 1000*1000,0.99); 

 public static void setStolenToken(String token) {
	 bloomFilter99percent.put(token);
 }
 
 public static boolean isStolen(String token)
 {
	 return bloomFilter99percent.mightContain(token);
	 
 }
}
