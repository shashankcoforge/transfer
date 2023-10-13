package hsbc.qs.utils.selenium;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;
import hsbc.qs.utils.helper.Helper;
import hsbc.qs.utils.helper.PropertyHelper;

import java.time.Duration;

/**
 * Class to get the driver path/quit manager and get the wait duration
 */
public abstract class DriverManager {
	
	protected WebDriver driver;
	protected WebDriverWait wait;

	public WebDriver getDriver(){
		if (driver == null){
			createDriver();
		}
		return driver;
	}

	public void quitDriver(){
		if (driver != null){
			driver.quit();
			driver = null;
		}
	}

	public WebDriverWait getWait() {
		if (wait == null){
			wait = new WebDriverWait(driver,  Duration.ofSeconds(getWaitDuration(),1));
		}
		return wait;
	}
	
	public String getDriverPath(String driverName){
    	String driver = driverName+(PropertyHelper.getVariable("os.name").split(" ")[0].toLowerCase().contains("windows")?".exe":"");
    	String path = PropertyHelper.getVariable("driverPath");
		String os = PropertyHelper.getVariable("os.name").split(" ")[0].toLowerCase().contains("_")?
				PropertyHelper.getVariable("os.name").split(" ")[0].toLowerCase().split("_")[0]:
				PropertyHelper.getVariable("os.name").split(" ")[0].toLowerCase();
    	return (path==null? Helper.getAbsolutePath() +Helper.getFileSeparator()+"drivers"+Helper.getFileSeparator():path)+os+Helper.getFileSeparator()+driver;
    }
	
	/** Returns duration for specified waits */
	public int getWaitDuration(){		
		final int defaultWait = 10;	
		int duration;
		try {
			duration = Integer.parseInt(PropertyHelper.getDefaultProperty("defaultWait"));
		} catch (Exception e) {
			duration = defaultWait;
		}
		return duration; 
	}

	protected abstract void createDriver();

	public abstract void updateResults(String result);
}