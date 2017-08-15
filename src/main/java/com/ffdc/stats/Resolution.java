package com.ffdc.stats;

/**
 * Convert the interval given by the caller to time interval of 5 min, 15 min, one hour, 1 day or 1 week
 * @author Manish Sharma
 *
 */
public class Resolution {
	//5min, 15 min, one hour, one day, one week
	public static int [] IntervalMillis = {5*60*1000,15*60*1000,60*60*1000,24*60*60*1000,7*24*60*60*1000};
	
	public static int getResoultion(long startTime, long endtime, int interval){
		int next = getNext(interval);
		int resolution = IntervalMillis[getNext(interval)];
		long dataPoints = (endtime - startTime)/resolution;
		if (dataPoints <= StatsConfig.getInstance().getMaxDataPoints())
			return resolution;
		else
		{
			if ( next >= IntervalMillis.length -1 )
				return IntervalMillis[IntervalMillis.length - 1];
			return getResoultion( startTime,   endtime, IntervalMillis[next+1]);
		}
			
			
	}
	
	private static int getNext(int interval){
		for ( int i = IntervalMillis.length  - 1; i >= 0; i--) {
			if ( interval >= IntervalMillis[i]  )
				return i;
			}
		return 0;
	}
	
	public static void testResulution()
	{
		System.out.println(" 400000 for 100,0000 records. Aswer should be 30000 : " + getResoultion(3000 , 3000 + 5*60*1000 * 100000L, 400000));
		System.out.println(" 400000 for 200,0000 records. Aswer should be 90000 : " + getResoultion(3000 , 3000 + 5*60*1000 * 200000L, 400000));
		System.out.println(" 900000 for 500,0000 records. Answer should be 1 day " + getResoultion(3000 , 3000 + 15*60*1000 * 500000L, 900000)/24/60/60/1000);
		System.out.println(" 900000 for 500,0000 records. Answer should be 7 day " + getResoultion(3000 , 3000 + 1000*60*1000 * 500000L, 900000)/24/60/60/1000);

	}
	
	public static void testNext()
	{
		System.out.println("For 0 = " + IntervalMillis[ getNext(0)]/60000);
		System.out.println("For 5 = " +  IntervalMillis[  getNext(0)]/60000);
		System.out.println("For 300 = " +   IntervalMillis[ getNext(0)]/60000);
		System.out.println("For 301 = " +   IntervalMillis[ getNext(0)]/60000);
		System.out.println("For 15*60*1000  +1  =  " +   IntervalMillis[ getNext(15*60*1000+1)]/60000);
		System.out.println("For 15*60*1000  - 1  " +  IntervalMillis[  getNext(15*60*1000-1)]/60000);
		System.out.println("For 3 * 60 *60*1000  - 1 hrs " +   IntervalMillis[ getNext(3 * 60*60*1000-1)]/60000/60);
		System.out.println("For 2* 24*60*60*1000  days =  " +   IntervalMillis[ getNext(2* 24*60*60*1000)]/60000/24/60);
		System.out.println("For 8* 24*60*60*1000 days =  " +   IntervalMillis[ getNext(8* 24*60*60*1000)]/60000/24/60);
		System.out.println("For 1000* 24*60*60*1000 days =  " +   IntervalMillis[ getNext(8* 24*60*60*1000)]/60000/24/60);

	}
}
