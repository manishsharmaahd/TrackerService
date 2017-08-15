package com.ffdc.test.controller;

import java.io.IOException;
import java.util.ArrayList;
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
import com.ffdc.DataAccesObjects.HitLogDAO;
import com.ffdc.dto.CampaignDTO;
import com.ffdc.models.AggregatedData;
import com.ffdc.models.Campaign;
import com.ffdc.models.HitLog;
import com.ffdc.testdata.CampaignData;
import com.ffdc.testdata.HitLogData;
import java.util.List;

/**
 * This also provide interface to query important Database tables such as
 * campaign, HotLog and AggregatedData
 * 
 * @author Manish Sharma
 *
 */
@RestController
@RequestMapping("/REST/Test/")
public class TestController {
	private static final Log log = LogFactory.getLog(TestController.class);

	/**
	 * Created four Campaigns for Test in DataIngestor and DataCompression
	 * Services EmailCampaign1 EmailCampaign2 WebCampaign1 WebCampaign2
	 * 
	 * @return
	 */
	@RequestMapping(value = "/CreateCampaigns", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseEntity<?> list() {

		try {
			List<Campaign> l = CampaignData.create();

			List<CampaignDTO> ldto = new ArrayList<CampaignDTO>();

			l.forEach(obj -> ldto.add(new CampaignDTO(obj)));
			if (l.isEmpty()) {
				new ResponseEntity<List<CampaignDTO>>(ldto, HttpStatus.NO_CONTENT);
			}
			return new ResponseEntity<List<CampaignDTO>>(ldto, HttpStatus.OK);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			return new ResponseEntity<String>("Faile to Create Campaigs" + e.getMessage(), HttpStatus.OK);
		}
	}

	/**
	 * Simulates hit record creation. Inserts records in HitLog table. The
	 * timestamp of record insert is random within bound of current time – days.
	 * The IPAdreeses, UserAgenet etc are created in such a manger that there
	 * unique, duplicate, mobile, tab and PC request. Coikies are aslo simulated
	 * for web requests
	 * 
	 * @param days
	 *            : Records are created for now() - days to now - 10 sec
	 * @return
	 * @throws IOException
	 */
	@RequestMapping(value = "/CreateHitLogs", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseEntity<?> createHitLogs(@RequestParam("days") int days) throws IOException {

		try {
			HitLogData data = new HitLogData();

			data.create(days);
			return new ResponseEntity<String>(
					"Hit Log  Min time = " + data.getMinDate() + " Max time " + data.getMaxDate(), HttpStatus.OK);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			return new ResponseEntity<String>("Failed to Create HitLogs" + e.getMessage(), HttpStatus.OK);
		}
	}

	/**
	 * Gives list JSON representation of list of rows in HitLog table
	 * 
	 * @param limit:
	 *            This parameter is passed as limit in select query
	 * @return
	 */
	@RequestMapping(value = "/ListRawData", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseEntity<?> listHitLogs(@RequestParam("limit") int limit) {
		HitLogDAO dao = new HitLogDAO();
		return new ResponseEntity<List<HitLog>>(dao.findAll(limit), HttpStatus.OK);
	}

	/**
	 * Gives list JSON representation of list of rows in HitLog table
	 * 
	 * @param limit:
	 *            This parameter is passed as limit in select query
	 * @return
	 */
	@RequestMapping(value = "/ListAggregatedData", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseEntity<?> listAggregatedData(@RequestParam("limit") int limit) {
		AggregatedDataDao dao = new AggregatedDataDao();
		return new ResponseEntity<List<AggregatedData>>(dao.findAll(limit), HttpStatus.OK);
	}

}
