package hsbc.ssd.utils.selenium.driverManagers;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.remote.RemoteWebDriver;
import hsbc.ssd.utils.helper.PropertyHelper;
import hsbc.ssd.utils.selenium.Capabilities;
import hsbc.ssd.utils.selenium.DriverManager;

import java.net.URL;

public class GridDriverManager extends DriverManager {

	protected Logger log = LogManager.getLogger(this.getClass().getName());

	/**
	 * create driver for the selenium grid - get the grid URL (gridURL) from
	 * system property or environment variable
	 * or src/test/resources/config/defaultSettings.properties
	 */
	@Override
	public void createDriver(){
		Capabilities cap = new Capabilities();
		try {

			String url = PropertyHelper.getVariable("gridURL");
			if(url==null) url = PropertyHelper.getDefaultProperty("gridURL");

			driver = new RemoteWebDriver(new URL(url), cap.getCap());

		} catch (Exception e) {
			log.debug("Could not connect to Selenium Grid: " + e.getMessage());
		}
	}

	@Override
	public void updateResults(String result){
		//do nothing
	}
} 