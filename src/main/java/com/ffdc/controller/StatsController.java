package com.ffdc.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.ffdc.DataAccesObjects.AggregatedDataDao;
import com.ffdc.DataAccesObjects.CampaignDAO;
import com.ffdc.dto.AggregatedDataDTO;
import com.ffdc.dto.CampaignStatsDTO;
import com.ffdc.dto.StatsQueryDTO;
import com.ffdc.models.AggregatedData;
import com.ffdc.models.CampaignStats;
import com.ffdc.stats.StatsDataFilter;
import java.util.List;

/*
 * 
 * Route for token generation
Route for accessing stats
Route visited by end-clients
Route for accessing detailed event logs (for one token) 

 * 
 */

/**
 * Create route for generation of following Stats 1) Timeseries stats for given
 * campaign 2) Generated token, Used token for open and click for all campaigns
 * 3) Generated token, Used token for open and click for given Campaign
 * 
 * @author Manish Sharma
 *
 */

@RestController
@RequestMapping("/Stats")
public class StatsController {
	private static final Log log = LogFactory.getLog(StatsController.class);

	/**
	 * Give the detailed time series statistics for given campaign. For each
	 * data points following parameter are incuded open UniqueOpen Clicks
	 * UniqueClicks Mobile Open and Clicks Tab Open and Clicks PC open and
	 * clicks
	 * 
	 * 
	 * If date is not present for a time period, zero value return for each
	 * parameter
	 * 
	 * Validations are done for campaign code, StartDate and Enddate Example:
	 * /Stats/TimeSeries?CampaignCode=EmailCampaign1&StartTime=2017-08-03%2004:04:23&EndTime=2017-08-12%2009:23:23&SuggestedIntervalInMin=60
	 * 
	 * @param campaingCode
	 *            Campaign for which stats are requested
	 * @param startTime
	 *            Start time for stats
	 * @param endTime
	 *            End time for stats
	 * @param suggestedIntevalInMin
	 *            The caller can suggest interval in minutes of the data bucket.
	 *            The resut is returned as per best matching interval to the
	 *            suggested one.
	 * 
	 * @return
	 * @throws IOException
	 */
	@RequestMapping(value = "/TimeSeries", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseEntity<?> timeSeries(@RequestParam("CampaignCode") String campaingCode,
			@RequestParam("StartTime") String startTime, @RequestParam("EndTime") String endTime,
			@RequestParam(value = "SuggestedIntervalInMin", required = false) String suggestedIntevalInMin)
			throws IOException {
		// will return
		// ResponseEntity<Campaign>(model,HttpStatus.EXPECTATION_FAILED)
		try {
			StatsQueryDTO model = new StatsQueryDTO();

			model.setCampaignCode(campaingCode);
			model.setStartTime(startTime);
			model.setEndTime(endTime);
			model.setSuggestedIntervalMin(suggestedIntevalInMin);
			// model.setSuggestedIntervalHours(intevalInHour);
			log.debug("Entering time series for campaing = " + model);
			model.validate();

			// Query data from persistent store.
			// The interval of data will be different
			List<AggregatedData> dataList = (new AggregatedDataDao()).getDataToAggregate(campaingCode,
					model.getStartTime().getTime(), model.getEndTime().getTime());
			int useSuggestedInterval = model.getIntervalMininMillis();
			if (useSuggestedInterval == 0)
				useSuggestedInterval = model.getIntervalHourinMillis();
			if (dataList.isEmpty()) {
				List<AggregatedDataDTO> errorList = new ArrayList<AggregatedDataDTO>();
				AggregatedDataDTO dto = new AggregatedDataDTO();
				dto.setErrorMessage("There are no data points");
				log.debug("Failed. No data points Exiting time series for campaing = " + model);
				errorList.add(dto);
				return new ResponseEntity<List<AggregatedDataDTO>>(errorList, HttpStatus.NO_CONTENT);
			}

			// Apply Stats filter. It normalizes the interval also put zero
			// count data points for non existing datapoints
			Collection<AggregatedData> retData = StatsDataFilter.filter(dataList, model.getStartTime().getTime(),
					model.getEndTime().getTime(), useSuggestedInterval);
			List<AggregatedDataDTO> arr = new ArrayList<>(retData.size());
			retData.forEach(a -> arr.add(new AggregatedDataDTO(a)));
			log.debug("Succes Exiting time series for campaing = " + model);

			return new ResponseEntity<List<AggregatedDataDTO>>(arr, HttpStatus.OK);

		} catch (Exception e) {
			log.error(e.getMessage());

			List<AggregatedDataDTO> errorList = new ArrayList<AggregatedDataDTO>();
			AggregatedDataDTO dto = new AggregatedDataDTO();
			errorList.add(dto);
			dto.setErrorMessage(e.getMessage() + " " + campaingCode);
			return new ResponseEntity<List<AggregatedDataDTO>>(errorList, HttpStatus.BAD_REQUEST);
		}
	}

	/**
	 * Queries database and give very high level stats for all campaings. The
	 * stats include Generated Open , Used Open , Generated click and used
	 * clicks
	 * 
	 * @return List of campaigns along with stats
	 */
	@RequestMapping(value = "/AllCampaigns", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseEntity<?> list() {
		log.debug("Entering list");
		CampaignDAO dao = new CampaignDAO();
		List<CampaignStats> l = dao.findAllStats();
		List<CampaignStatsDTO> ldto = new ArrayList<CampaignStatsDTO>();

		l.forEach(obj -> ldto.add(new CampaignStatsDTO(obj)));
		if (l.isEmpty()) {
			log.debug("No content exiting list");
			new ResponseEntity<List<CampaignStatsDTO>>(ldto, HttpStatus.NO_CONTENT);
		}
		log.debug("Successfully exiting list");
		return new ResponseEntity<List<CampaignStatsDTO>>(ldto, HttpStatus.OK);

	}

	/**
	 * Queries database and give very high level stats for given campaigns. The
	 * stats include Generated Open , Used Open , Generated click and used
	 * clicks
	 * 
	 * @param campaignCode
	 *            Campaign for which data has been requested
	 * @return List of campaigns along with stats
	 * 
	 */
	@RequestMapping(value = "/Campaign", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseEntity<?> campaignStats(@RequestParam("CampaignCode") String campaignCode) {
		log.debug("Entering list for " + campaignCode);
		CampaignDAO dao = new CampaignDAO();
		CampaignStats l = dao.findStatByCampaignCode(campaignCode);

		if (l == null) {
			log.debug("Failed Exiting campaignSats for  " + campaignCode);
			return new ResponseEntity<CampaignStatsDTO>(new CampaignStatsDTO(), HttpStatus.NOT_FOUND);

		}
		CampaignStatsDTO dto = new CampaignStatsDTO(l);
		log.debug("Success Exiting campaignSats for  " + campaignCode);
		return new ResponseEntity<CampaignStatsDTO>(dto, HttpStatus.OK);
	}

}
