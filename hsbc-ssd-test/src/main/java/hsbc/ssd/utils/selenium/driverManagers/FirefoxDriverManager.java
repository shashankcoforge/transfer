//package hsbc.ssd.utils.selenium.driverManagers;
//
//import io.github.bonigarcia.wdm.WebDriverManager;
//import org.apache.commons.configuration.PropertiesConfiguration;
//import org.apache.logging.log4j.LogManager;
//import org.apache.logging.log4j.Logger;
//import org.openqa.selenium.firefox.FirefoxDriver;
//import org.openqa.selenium.firefox.FirefoxOptions;
//import hsbc.ssd.utils.Constants;
//import hsbc.ssd.utils.helper.PropertyHelper;
//import hsbc.ssd.utils.selenium.Capabilities;
//import hsbc.ssd.utils.selenium.DriverContext;
//import hsbc.ssd.utils.selenium.DriverManager;
//
//public class FirefoxDriverManager extends DriverManager {
//	protected Logger log = LogManager.getLogger(this.getClass().getName());
//
//	@Override
//	public void createDriver(){
//		Capabilities cap = new Capabilities();
//    	PropertiesConfiguration props = PropertyHelper.getProperties(Constants.DEFAULTSETTINGS);
//
//		String webDriverManager = PropertyHelper.getVariable("useWebDriverManager") != null
//				? PropertyHelper.getVariable("useWebDriverManager") : PropertyHelper.getDefaultProperty("useWebDriverManager");
//		if (webDriverManager.equalsIgnoreCase("true")) {
//				WebDriverManager.firefoxdriver().setup();
//    	}else {
//    		System.setProperty("webdriver.gecko.driver",getDriverPath("geckodriver"));
//    	}
//
//		FirefoxOptions options = new FirefoxOptions();
//		for (String variable : props.getStringArray("options."+ DriverContext.getInstance().getBrowserName().replaceAll("\\s",""))){
//    		options.addArguments(variable);
//		}
//
//		log.debug("firefox.options="+ PropertyHelper.getVariable("firefox.options"));
//		if (PropertyHelper.getVariable("firefox.options")!=null){
//			options.addArguments(PropertyHelper.getVariable("firefox.options"));
//		}
////		options.addCapabilities(cap.getCap()); 	//TEMP
//		driver = new FirefoxDriver(options);
//	}
//
//	@Override
//	public void updateResults(String result){
//		//do nothing
//	}
//}