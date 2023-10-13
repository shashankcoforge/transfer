package hsbc.ssd.utils.selenium;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.chrome.ChromeOptions;

import java.util.Map;

/**
 * Class to set the desired capabilities
 */
public class Capabilities {

    protected Logger log = LogManager.getLogger(this.getClass().getName());

    private ChromeOptions options;

    /**
     * constructor to set the desired capabilities
     */
    public Capabilities() {
        Map<String, String> map = DriverContext.getInstance().getDriverStack();

        options = new ChromeOptions();
        options.addArguments("headless");
        options.addArguments("--disable-gpu");        
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("disable-infobars");
        options.addArguments("--disable-extensions");
        options.addArguments("--start-maximized");
        options.addArguments("window-size=1920x1080");
        options.addArguments("--no-sandbox");

        if (!map.get("serverType").equalsIgnoreCase("grid")) {
            if (TestParameters.getInstance().frameworkData().containsKey("scenarioName")){
                options.setCapability("name", TestParameters.getInstance().getFrameworkData("scenarioName"));
            }
        }

        if (TestParameters.getInstance().frameworkData().containsKey("projectName"))
        options.setCapability("project", TestParameters.getInstance().getFrameworkData("projectName"));

        if (TestParameters.getInstance().frameworkData().containsKey("buildNumber"))
        options.setCapability("build", TestParameters.getInstance().getFrameworkData("buildNumber"));

        for (Map.Entry<String, String> pair : map.entrySet()) {
            if (!pair.getKey().equalsIgnoreCase("serverType") && !pair.getKey().equalsIgnoreCase("description"))
            options.setCapability(pair.getKey(), pair.getValue());
        }

//        PropertiesConfiguration props = PropertyHelper.getProperties(Constants.DEFAULTSETTINGS);
//        //setting any project specific capabilities
//        for (String variable : props.getStringArray("desiredCapabilities." + DriverContext.getInstance().getBrowserName().replaceAll("\\s", ""))) {
//            String[] par = variable.split("==");
//            if (par[1].trim().equalsIgnoreCase("true") || par[1].trim().equalsIgnoreCase("false"))
//                dc.setCapability(par[0].trim(), Boolean.parseBoolean(par[1].trim()));
//            else
//                dc.setCapability(par[0].trim(), par[1].trim());
//        }
    }

    public ChromeOptions getCap() {
        return options;
    }

}