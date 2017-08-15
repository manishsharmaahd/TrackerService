package com.ffdc.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.ffdc.DataAccesObjects.CampaignDAO;
import com.ffdc.dto.CampaignDTO;
import com.ffdc.models.Campaign;
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
 * Provides route to Create Campaign and update existig Campaigns
 * 
 * @author Manish Sharma
 *
 */

@RestController
@RequestMapping("/REST/Campaign")
public class CampaignController {
	private static final Log log = LogFactory.getLog(CampaignController.class);

	/**
	 * List all registered campaigns
	 * 
	 * @return: Request Entity with List of Campaign models . Return 204 if no
	 *          campaign exists
	 * @throws IOException
	 */
	@RequestMapping(value = "/list", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseEntity<?> list() throws IOException {

		CampaignDAO dao = new CampaignDAO();
		List<Campaign> l = dao.findAll();
		List<CampaignDTO> ldto = new ArrayList<CampaignDTO>();

		l.forEach(obj -> ldto.add(new CampaignDTO(obj)));
		if (l.isEmpty()) {
			new ResponseEntity<List<CampaignDTO>>(ldto, HttpStatus.NO_CONTENT);
		}
		return new ResponseEntity<List<CampaignDTO>>(ldto, HttpStatus.OK);
	}

 

	/**
	 * This creates a campaign with given campaign code returns campaign code
	 * JSON object to the caller. In case of new campaign - newly created
	 * campaignid is also returned to the caller. Existing campaigns are updated
	 * Returns error code Http.OK 201 if code is created successfully Return
	 * error code of 200 if campaign already exists. return 400
	 * 
	 * The fields in CampaignDTO models are validated before save. If parameters
	 * are not valid 400 bad request is returned to the caller with error message
	 * in Error message field.
	 * 
	 * @param model
	 *            : Campaign Model mapped from JSON pay load of request
	 * @return Response
	 */
	@RequestMapping(value = "/Create", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseEntity<?> Create(@RequestBody CampaignDTO model) {
		// will return
		// ResponseEntity<Campaign>(model,HttpStatus.EXPECTATION_FAILED)
		try {
			log.debug("Enter Campaing Create " + model.toString());
			model.validate();

			String regex = "^(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]";

			if (model.getRedirectURL() != null && !model.getRedirectURL().isEmpty()) {
				Pattern patt = Pattern.compile(regex);
				Matcher matcher = patt.matcher(model.getRedirectURL());
				if (!matcher.matches()) {
					throw new IllegalArgumentException("Invalid URL" + model.getRedirectURL());
				}

			}

			CampaignDAO campdao = new CampaignDAO();
			Campaign c = new Campaign(model);

			c = campdao.merge(c);
			log.debug("Calling Campaing Create " + model.toString());

			return new ResponseEntity<CampaignDTO>(new CampaignDTO(c), HttpStatus.OK);

		} catch (IllegalArgumentException e) {
			log.error(e.getMessage());

			CampaignDTO md = new CampaignDTO();
			md.setErrorMessage(e.getMessage());
			return new ResponseEntity<CampaignDTO>(md, HttpStatus.BAD_REQUEST);
		}
	}

}
