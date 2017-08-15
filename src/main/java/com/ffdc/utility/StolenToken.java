package com.ffdc.utility;

 
import java.util.HashSet;
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
 private static HashSet<String> stolenToken = new HashSet<>();
 
 public static void setStolenToken(String token) {
	 stolenToken.add(token);
 }
 
 public static boolean isStolen(String token)
 {
	 return stolenToken.contains(token);
	 
 }
}
