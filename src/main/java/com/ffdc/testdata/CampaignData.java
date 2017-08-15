package com.ffdc.testdata;

import java.util.List;

import com.ffdc.DataAccesObjects.CampaignDAO;
import com.ffdc.models.Campaign;
import com.ffdc.utility.CacheSingleton;
import com.ffdc.utility.Constants;
/**
 * Loads data in Campaign 
 * EmailCampaign1
 * EmailCampaign2
 * WebCampaign1
 * WebCampaign2
 * 
 * @author Manish Sharma
 *
 */
public class CampaignData {
	public static List<Campaign> create() {

		CampaignDAO dao = new CampaignDAO();
		Campaign c = new Campaign();
		c.setCampaignCode("EmailCampaign1");

		c.setClientName("Test Client Inc");
		c.setDescription("For automated test");
		c.setEndDateUnixTs(System.currentTimeMillis() + 30L * 24L * 86400000L);
		c.setRedirectURL("http://www.google.com");
		c.setStartDateUnixTs(System.currentTimeMillis() - 45L * 24L * 86400000L);
		c.setType(Constants.CAMPAIGN_EMAIL);
		dao.merge(c);

		c = new Campaign();
		c.setCampaignCode("WebCampaign1");
		c.setClientName("Test Client Inc");
		c.setDescription("For automated test");
		c.setEndDateUnixTs(System.currentTimeMillis() + 30L * 24L * 86400000L);
		c.setRedirectURL("http://www.google.com");
		c.setStartDateUnixTs(System.currentTimeMillis() - 45L * 24L * 86400000L);
		c.setType(Constants.CAMPAIGN_WEB);
		dao.merge(c);

		c = new Campaign();
		c.setCampaignCode("EmailCampaign2");
		c.setClientName("Test Client Inc");
		c.setDescription("For automated test");
		c.setEndDateUnixTs(System.currentTimeMillis() + 30L * 24L * 86400000L);
		c.setRedirectURL("http://www.google.com");
		c.setStartDateUnixTs(System.currentTimeMillis() - 45L * 24L * 86400000L);
		c.setType(Constants.CAMPAIGN_EMAIL);
		dao.merge(c);

		c = new Campaign();
		c.setCampaignCode("WebCampaign2");
		c.setClientName("Test Client");
		c.setDescription("For automated test");
		c.setEndDateUnixTs(System.currentTimeMillis() + 30L * 24L * 86400000L);
		c.setRedirectURL("http://www.google.com");
		c.setStartDateUnixTs(System.currentTimeMillis() - 45L * 24L * 86400000L);
		c.setType(Constants.CAMPAIGN_WEB);
		dao.merge(c);
		CacheSingleton.getInstance().remove(CampaignDAO.JCSActiveCampaignKey);
		return (new CampaignDAO()).findAll();

	}

}
