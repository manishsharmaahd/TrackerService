package com.ffdc.stats;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.LongStream;

import com.ffdc.models.AggregatedData;
import com.ffdc.models.AggregatedDataId;

/**
 * This class is used by two clients
 * 1) Stats data: It is fetched from database and process by this logic to inflate for deflate the intervals, insert zero points
 * 2) This is also use used for compressing daemon.  Converting intervals from 5 - 15 min or 15 min to 60 min etc
 * 
 * 
 * This are many way to write this. Since the data can be large, we chose to put
 * less load on CPU and memory by not using duplication, avoiding sorting,
 * merging etc. The logic can be followed by any java programmer -so that it can
 * be easily maintained.
 * 
 * This method does three tasks
 * 
 * 1) Determine interval of data based interval and range provided by the
 * caller. Caller's interval might not be supported by us. We support
 * pre-formated intervals such 5 min, 15 min, 1 hour , 1 days defined in config
 * 
 * 
 * 2) If hit count or click counts are zero then those entry will be missing in
 * the database. It creates those with zero count value. These must be returned
 * to the caller
 * 
 * 3) Aggregate data if interval is larger for data point, eg if data is
 * available in 5 min interval but user asked for 15 minutes. It will sum-up
 * those values
 * 
 * 4) Average if use has lower resolution but data is available in higher
 * resolution. Eg if we have count = 81 and for 15 min interval. User has asked
 * for 5 min. We make three points with 81/3 =27 count each.
 * 
 * @author Manish Sharma
 *
 */

public class StatsDataFilter {

	/**
	 * @param raw
	 * @param startTime
	 * @param endtime
	 * @param interval
	 * @return
	 */

	public static Collection<AggregatedData> filter(List<AggregatedData> raw, long startTime, long endtime,
			int interval) {

		String campaignCode = !raw.isEmpty() ? raw.get(0).getId().getCampaignCode() : "";

		int useInterval = Resolution.getResoultion(startTime, endtime, interval);
		long useStartTime = (startTime / useInterval) * useInterval;
		long useEndTime = (endtime / useInterval) * useInterval;

		// Linked Hash Map is used so that order of insertion is preserved like
		// a linked list.
		// sorting will avoided at same time we get direct hit functionality of
		// hashMap
		LinkedHashMap<Long, AggregatedData> returnDataShell = new LinkedHashMap<>();

		// Get the Stream of from userstarttime to end time and add Shell
		// DataAgregator to the Arraylist
		// We are just putting null as value no memory other that for string
		// Long is being taken.
		LongStream.range(useStartTime / useInterval, useEndTime / useInterval).map(i -> i * useInterval)
				.forEach(a -> returnDataShell.put(new Long(a), null));

		// Input 15 - 4 , 20 - 5, 25 - 6, 30 - 3, 40 3, 50 - 6,55 - 6, 65 - 1,
		// 75(15) - 12, 90 - 6,
		// 120 -18 , 180 (1 hr) - 48 , 360 (1 hr) - 60 , 2*24* - 2748
		// Output should be range of 15 min and start time 15 min to 5 days
		// 15 - 15, 30 - 5, 45 - 12 60 - 1, 75 - 12 (65 will be ignored), 90
		// -6,105 - 0, 120 - 18, 135 - 0, 150 - 0 , 165 - 0, 180 - 12,
		// 195 12, 210 12, 225 - 12, 240 - 0 , 255 - 0, 270 - 0, 285 - 0 , 300 -
		// 0, 315 -0 , 330- 0 345 - 0 ,360 - 15, 375 - 15, 390 - 15, 405 -15,
		// 420 to 2865 - 0, 2880 - 12, 2880 - 12, 2880 - 12

		Iterator<AggregatedData> itr = raw.iterator();

		while (itr.hasNext()) {

			AggregatedData d = itr.next();

			if (d.getTimeStamp() < useStartTime || d.getTimeStamp() >= useEndTime)
				continue;
			else if (d.getForInterval() < useInterval) {
				// We will have to combine data
				Long willFallintoBucket = (d.getTimeStamp() / useInterval) * useInterval;

				AggregatedData additonalDataPoint = returnDataShell.get(willFallintoBucket);
				if (additonalDataPoint == null) {
					additonalDataPoint = new AggregatedData();
					additonalDataPoint.setId(new AggregatedDataId(willFallintoBucket, campaignCode));
					additonalDataPoint.setTimeStatmpStringForTest((new Date(willFallintoBucket)).toString());
					additonalDataPoint.setForInterval(useInterval);
					returnDataShell.put(willFallintoBucket, additonalDataPoint);
				}
				additonalDataPoint.setOpens(d.getOpens() + additonalDataPoint.getOpens());
				additonalDataPoint.setUniqueOpens(d.getUniqueOpens() + additonalDataPoint.getUniqueOpens());
				additonalDataPoint.setMobileOpens(d.getMobileOpens() + additonalDataPoint.getMobileOpens());
				additonalDataPoint.setTabOpens(d.getTabOpens() + additonalDataPoint.getTabOpens());
				additonalDataPoint.setNormalOpens(d.getNormalOpens() + additonalDataPoint.getNormalOpens());
				additonalDataPoint.setClicks(d.getClicks() + additonalDataPoint.getClicks());
				additonalDataPoint.setUniqueClicks(d.getUniqueClicks() + additonalDataPoint.getUniqueClicks());
				additonalDataPoint.setMobileClicks(d.getMobileClicks() + additonalDataPoint.getMobileClicks());
				additonalDataPoint.setTabClicks(d.getTabClicks() + additonalDataPoint.getTabClicks());
				additonalDataPoint.setNormalClicks(d.getNormalClicks() + additonalDataPoint.getNormalClicks());

			} else if (d.getForInterval() == useInterval) {
				returnDataShell.put(d.getTimeStamp(), d);
			} else if (d.getForInterval() > useInterval) {
				// For interval of d is 60 min count 16 and useInterval is 15
				// min
				// then four node will be constructed. Each node will get value
				// of 16/4
				if (d.getForInterval() % useInterval != 0)
					throw new IllegalArgumentException(
							" for inteval is not completely divisible by useInterval. Get For Interval is  "
									+ d.getForInterval() + " and useInteval is " + useInterval);
				int ratio = d.getForInterval() / useInterval;
				returnDataShell.put(new Long(d.getTimeStamp()), d);

				ArrayList<AggregatedData> newNodes = new ArrayList<>();

				// Create new nodes and initialize primary key of timestamp and
				// campaign
				for (int i = 0; i < ratio; i++) {
					if (!returnDataShell.containsKey(new Long(d.getTimeStamp() + i * useInterval)))
						// we have over shot the interval so time to break out
						// of
						// the loop
						break;
					AggregatedData additonalDataPoint = new AggregatedData();
					additonalDataPoint.setId(new AggregatedDataId(d.getTimeStamp() + i * useInterval, campaignCode));
					returnDataShell.put(d.getTimeStamp() + i * useInterval, additonalDataPoint);
					newNodes.add(additonalDataPoint);
				}
				setOpenDataInSplittedNode(newNodes, d, ratio, useInterval);
				setUniqueOpenDataInSplittedNode(newNodes, d, ratio, useInterval);
				setTabOpenDataInSplittedNode(newNodes, d, ratio, useInterval);
				setMobileOpenDataInSplittedNode(newNodes, d, ratio, useInterval);
				setNormalOpenDataInSplittedNode(newNodes, d, ratio, useInterval);
				setClickDataInSplittedNode(newNodes, d, ratio, useInterval);
				setUniqueClickDataInSplittedNode(newNodes, d, ratio, useInterval);
				setTabClickDataInSplittedNode(newNodes, d, ratio, useInterval);
				setMobileClickDataInSplittedNode(newNodes, d, ratio, useInterval);
				setNormalClickDataInSplittedNode(newNodes, d, ratio, useInterval);
				// Now do maths to set Data

			}

		}

		// insert zero object for null ... ie for those object measurement
		// doesn't exist.
		returnDataShell.keySet().forEach(val -> {
			if (returnDataShell.get(val) == null) {
				AggregatedData additonalDataPoint = new AggregatedData();
				additonalDataPoint.setId(new AggregatedDataId(val, campaignCode));
				additonalDataPoint.setForInterval(useInterval);
				returnDataShell.put(val, additonalDataPoint);
			}

		});

		return returnDataShell.values();
	}

	public static void setOpenDataInSplittedNode(List<AggregatedData> newNodes, AggregatedData currentNode, int ratio,
			int useInterval) {

		int openCountOfSplittedNodes = currentNode.getOpens() / ratio;
		int modOpen = currentNode.getOpens() % ratio;
		for (AggregatedData additonalDataPoint : newNodes) {
			if (modOpen > 0)
				additonalDataPoint.setOpens(openCountOfSplittedNodes + 1);
			else
				additonalDataPoint.setOpens(openCountOfSplittedNodes);
			modOpen--;
			additonalDataPoint.setForInterval(useInterval);
		}

	}

	public static void setUniqueOpenDataInSplittedNode(List<AggregatedData> newNodes, AggregatedData currentNode,
			int ratio, int useInterval) {

		int UniqueOpenCountOfSplittedNodes = currentNode.getUniqueOpens() / ratio;
		int modUniqueOpen = currentNode.getUniqueOpens() % ratio;
		for (AggregatedData additonalDataPoint : newNodes) {
			if (modUniqueOpen > 0)
				additonalDataPoint.setUniqueOpens(UniqueOpenCountOfSplittedNodes + 1);
			else
				additonalDataPoint.setUniqueOpens(UniqueOpenCountOfSplittedNodes);
			modUniqueOpen--;
			additonalDataPoint.setForInterval(useInterval);
		}

	}

	public static void setMobileOpenDataInSplittedNode(List<AggregatedData> newNodes, AggregatedData currentNode,
			int ratio, int useInterval) {

		int MobileOpenCountOfSplittedNodes = currentNode.getMobileOpens() / ratio;
		int modMobileOpen = currentNode.getMobileOpens() % ratio;
		for (AggregatedData additonalDataPoint : newNodes) {
			if (modMobileOpen > 0)
				additonalDataPoint.setMobileOpens(MobileOpenCountOfSplittedNodes + 1);
			else
				additonalDataPoint.setMobileOpens(MobileOpenCountOfSplittedNodes);
			modMobileOpen--;
			additonalDataPoint.setForInterval(useInterval);
		}

	}

	public static void setTabOpenDataInSplittedNode(List<AggregatedData> newNodes, AggregatedData currentNode,
			int ratio, int useInterval) {

		int TabOpenCountOfSplittedNodes = currentNode.getTabOpens() / ratio;
		int modTabOpen = currentNode.getTabOpens() % ratio;
		for (AggregatedData additonalDataPoint : newNodes) {
			if (modTabOpen > 0)
				additonalDataPoint.setTabOpens(TabOpenCountOfSplittedNodes + 1);
			else
				additonalDataPoint.setTabOpens(TabOpenCountOfSplittedNodes);
			modTabOpen--;
			additonalDataPoint.setForInterval(useInterval);
		}

	}

	public static void setNormalOpenDataInSplittedNode(List<AggregatedData> newNodes, AggregatedData currentNode,
			int ratio, int useInterval) {

		int NormalOpenCountOfSplittedNodes = currentNode.getNormalOpens() / ratio;
		int modNormalOpen = currentNode.getNormalOpens() % ratio;
		for (AggregatedData additonalDataPoint : newNodes) {
			if (modNormalOpen > 0)
				additonalDataPoint.setNormalOpens(NormalOpenCountOfSplittedNodes + 1);
			else
				additonalDataPoint.setNormalOpens(NormalOpenCountOfSplittedNodes);
			modNormalOpen--;
			additonalDataPoint.setForInterval(useInterval);
		}

	}

	public static void setClickDataInSplittedNode(List<AggregatedData> newNodes, AggregatedData currentNode, int ratio,
			int useInterval) {

		int ClickCountOfSplittedNodes = currentNode.getClicks() / ratio;
		int modClick = currentNode.getClicks() % ratio;
		for (AggregatedData additonalDataPoint : newNodes) {
			if (modClick > 0)
				additonalDataPoint.setClicks(ClickCountOfSplittedNodes + 1);
			else
				additonalDataPoint.setClicks(ClickCountOfSplittedNodes);
			modClick--;
			additonalDataPoint.setForInterval(useInterval);
		}

	}

	public static void setUniqueClickDataInSplittedNode(List<AggregatedData> newNodes, AggregatedData currentNode,
			int ratio, int useInterval) {

		int UniqueClickCountOfSplittedNodes = currentNode.getUniqueClicks() / ratio;
		int modUniqueClick = currentNode.getUniqueClicks() % ratio;
		for (AggregatedData additonalDataPoint : newNodes) {
			if (modUniqueClick > 0)
				additonalDataPoint.setUniqueClicks(UniqueClickCountOfSplittedNodes + 1);
			else
				additonalDataPoint.setUniqueClicks(UniqueClickCountOfSplittedNodes);
			modUniqueClick--;
			additonalDataPoint.setForInterval(useInterval);
		}

	}

	public static void setMobileClickDataInSplittedNode(List<AggregatedData> newNodes, AggregatedData currentNode,
			int ratio, int useInterval) {

		int MobileClickCountOfSplittedNodes = currentNode.getMobileClicks() / ratio;
		int modMobileClick = currentNode.getMobileClicks() % ratio;
		for (AggregatedData additonalDataPoint : newNodes) {
			if (modMobileClick > 0)
				additonalDataPoint.setMobileClicks(MobileClickCountOfSplittedNodes + 1);
			else
				additonalDataPoint.setMobileClicks(MobileClickCountOfSplittedNodes);
			modMobileClick--;
			additonalDataPoint.setForInterval(useInterval);
		}

	}

	public static void setTabClickDataInSplittedNode(List<AggregatedData> newNodes, AggregatedData currentNode,
			int ratio, int useInterval) {

		int TabClickCountOfSplittedNodes = currentNode.getTabClicks() / ratio;
		int modTabClick = currentNode.getTabClicks() % ratio;
		for (AggregatedData additonalDataPoint : newNodes) {
			if (modTabClick > 0)
				additonalDataPoint.setTabClicks(TabClickCountOfSplittedNodes + 1);
			else
				additonalDataPoint.setTabClicks(TabClickCountOfSplittedNodes);
			modTabClick--;
			additonalDataPoint.setForInterval(useInterval);
		}

	}

	public static void setNormalClickDataInSplittedNode(List<AggregatedData> newNodes, AggregatedData currentNode,
			int ratio, int useInterval) {

		int NormalClickCountOfSplittedNodes = currentNode.getNormalClicks() / ratio;
		int modNormalClick = currentNode.getNormalClicks() % ratio;
		for (AggregatedData additonalDataPoint : newNodes) {
			if (modNormalClick > 0)
				additonalDataPoint.setNormalClicks(NormalClickCountOfSplittedNodes + 1);
			else
				additonalDataPoint.setNormalClicks(NormalClickCountOfSplittedNodes);
			modNormalClick--;
			additonalDataPoint.setForInterval(useInterval);
		}

	}

	public static void test() {

		ArrayList<AggregatedData> l = new ArrayList<>();
		// Input 15 - 4 , 20 - 5, 25 - 6, 30 - 3, 40 3, 50 - 6,55 - 6, 65 - 1,
		// 75(15) - 12, 90 - 6,
		// 120 -18 , 180 (1 hr) - 48 , 360 (1 hr) - 60 , 2*24* - 2748
		// Output should be range of 15 min and start time 15 min to 5 days
		// 15 - 15, 30 - 5, 45 - 12 60 - 1, 75 - 12 (65 will be ignored), 90
		// -6,105 - 0, 120 - 18, 135 - 0, 150 - 0 , 165 - 0, 180 - 12,
		// 195 12, 210 12, 225 - 12, 240 - 0 , 255 - 0, 270 - 0, 285 - 0 , 300 -
		// 0, 315 -0 , 330- 0 345 - 0 ,360 - 15, 375 - 15, 390 - 15, 405 -15,
		// 420 to 2865 - 0, 2880 - 12, 2880 - 12, 2880 - 12

		long startTimePrefix = ((System.currentTimeMillis() - 10 * 86400 * 1000) / 86400 * 1000) * 86400 * 1000;
		AggregatedData d = new AggregatedData();
		d.setId(new AggregatedDataId(startTimePrefix + 15 * 60 * 1000, "Manish"));
		d.setOpens(4);
		d.setForInterval(5 * 60 * 1000);
		l.add(d);
		d = new AggregatedData();
		d.setId(new AggregatedDataId(startTimePrefix + 20 * 60 * 1000, "Manish"));
		d.setOpens(5);
		d.setForInterval(5 * 60 * 1000);
		l.add(d);
		d = new AggregatedData();
		d.setId(new AggregatedDataId(startTimePrefix + 25 * 60 * 1000, "Manish"));
		d.setOpens(6);
		d.setForInterval(5 * 60 * 1000);
		l.add(d);
		d = new AggregatedData();
		d.setId(new AggregatedDataId(startTimePrefix + 30 * 60 * 1000, "Manish"));
		d.setOpens(3);
		d.setForInterval(5 * 60 * 1000);
		l.add(d);

		d = new AggregatedData();
		d.setId(new AggregatedDataId(startTimePrefix + 40 * 60 * 1000, "Manish"));
		d.setOpens(3);
		d.setForInterval(5 * 60 * 1000);
		l.add(d);

		d = new AggregatedData();
		d.setId(new AggregatedDataId(startTimePrefix + 50 * 60 * 1000, "Manish"));
		d.setOpens(6);
		d.setForInterval(5 * 60 * 1000);
		l.add(d);

		d = new AggregatedData();
		d.setId(new AggregatedDataId(startTimePrefix + 55 * 60 * 1000, "Manish"));
		d.setOpens(6);
		d.setForInterval(5 * 60 * 1000);
		l.add(d);

		d = new AggregatedData();
		d.setId(new AggregatedDataId(startTimePrefix + 65 * 60 * 1000, "Manish"));
		d.setOpens(1);
		d.setForInterval(5 * 60 * 1000);
		l.add(d);

		d = new AggregatedData();
		d.setId(new AggregatedDataId(startTimePrefix + 75 * 60 * 1000, "Manish"));
		d.setOpens(12);
		d.setForInterval(15 * 60 * 1000);
		l.add(d);

		d = new AggregatedData();
		d.setId(new AggregatedDataId(startTimePrefix + 90 * 60 * 1000, "Manish"));
		d.setOpens(6);
		d.setForInterval(15 * 60 * 1000);
		l.add(d);

		d = new AggregatedData();
		d.setId(new AggregatedDataId(startTimePrefix + 120 * 60 * 1000, "Manish"));
		d.setOpens(18);
		d.setForInterval(15 * 60 * 1000);
		l.add(d);

		d = new AggregatedData();
		d.setId(new AggregatedDataId(startTimePrefix + 180 * 60 * 1000, "Manish"));
		d.setOpens(48);
		d.setForInterval(60 * 60 * 1000);
		l.add(d);

		d = new AggregatedData();
		d.setId(new AggregatedDataId(startTimePrefix + 360 * 60 * 1000, "Manish"));
		d.setOpens(60);
		d.setForInterval(60 * 60 * 1000);
		l.add(d);

		d = new AggregatedData();
		d.setId(new AggregatedDataId(startTimePrefix + 2 * 24 * 60 * 60 * 1000, "Manish")); // 2
																							// days
		d.setOpens(1152);
		d.setForInterval(24 * 60 * 60 * 1000);
		l.add(d);

		Collection<AggregatedData> result = filter(l, startTimePrefix + 15 * 60 * 1000,
				startTimePrefix + 2 * 24 * 60 * 60 * 1000 + 3 * 15 * 60 * 1000, 15 * 60 * 1000);
		result.forEach(a -> System.out.println(
				(a.getTimeStamp() - startTimePrefix) / 1000 + " sec " + (a.getTimeStamp() - startTimePrefix) / 60000
						+ " min " + (a.getTimeStamp() - startTimePrefix) / 60000 / 60 + " hrs "
						+ (a.getTimeStamp() - startTimePrefix) / 60000 / 60 / 24 + " days Opens: " + a.getOpens()));

	}
}
