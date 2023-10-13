package hsbc.ssd.utils.selenium.driverManagers;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import hsbc.ssd.utils.helper.PropertyHelper;
import hsbc.ssd.utils.selenium.Capabilities;
import hsbc.ssd.utils.selenium.DriverManager;
import org.openqa.selenium.edge.EdgeDriver;

public class EdgeDriverManager extends DriverManager {

	protected Logger log = LogManager.getLogger(this.getClass().getName());


	@Override
	public void createDriver(){
//		Capabilities cap = new Capabilities();
//		String webDriverManager = PropertyHelper.getVariable("useWebDriverManager") != null
//				? PropertyHelper.getVariable("useWebDriverManager") : PropertyHelper.getDefaultProperty("useWebDriverManager");
//		if (webDriverManager.equalsIgnoreCase("true")) {
//				WebDriverManager.edgedriver().setup();
//    	}else {
//			System.setProperty("webdriver.edge.driver", getDriverPath("MicrosoftWebDriver"));
//    	}
//		driver = new EdgeDriver(cap.getCap());
		driver = new EdgeDriver();
	}

	@Override
	protected void createTPADriver() {
		tpaDriver = new EdgeDriver();
	}

	@Override
	public void updateResults(String result){
		//do nothing
	}
} 