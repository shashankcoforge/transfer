package hsbc.qs.utils.selenium.driverManagers;

import hsbc.qs.utils.Constants;
import hsbc.qs.utils.helper.PropertyHelper;
import hsbc.qs.utils.selenium.Capabilities;
import hsbc.qs.utils.selenium.DriverContext;
import hsbc.qs.utils.selenium.DriverManager;
import hsbc.qs.utils.selenium.TestParameters;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.util.Map;


public class ChromeDriverManager extends DriverManager {

    protected Logger log = LogManager.getLogger(this.getClass().getName());


    @Override
    public void createDriver() {

        System.out.println(WebDriverManager.chromedriver().getBrowserPath());
        WebDriverManager.chromedriver().clearDriverCache().setup();
        driver = WebDriverManager.chromedriver().clearDriverCache().create();

        driver  = new ChromeDriver();

//        Capabilities cap = new Capabilities();
//        PropertiesConfiguration props = PropertyHelper.getProperties(Constants.DEFAULTSETTINGS);
//        String webDriverManager = PropertyHelper.getVariable("useWebDriverManager") != null
//                ? PropertyHelper.getVariable("useWebDriverManager") : PropertyHelper.getDefaultProperty("useWebDriverManager");
//        if (webDriverManager.equalsIgnoreCase("true")) {
//            WebDriverManager.chromedriver().setup();
//        } else {
//            System.setProperty("webdriver.chrome.driver", getDriverPath("chromedriver"));
//            System.out.println("chromedriver path " + getDriverPath("chromedriver"));
//        }
//        System.setProperty("webdriver.chrome.silentOutput", "true");
//        ChromeOptions options = new ChromeOptions();
//        options.addArguments("--remote-allow-origins=*");
//        options.addArguments("start-maximized");
//        options.addArguments("disable-infobars");
//        options.addArguments("--disable-extensions");
//        options.addArguments("--no-sandbox");
//        options.addArguments("--disable-dev-shm-usage");
//
//
//        options.addArguments("--disable-extensions");
//        options.addArguments("--disable-dev-shm-usage");
//        options.addArguments("--ignore-ssl-errors=yes");
//        options.addArguments("--ignore-certificate-errors");
//
//        // If enableHar2Jmx is true then an extra capability will be added to allow for 'untrusted' certificates.
//        // This is needed for when using a proxy to capture network traffic when recording HAR data.
//        // if(PropertyHelper.getVariable("enableHar2Jmx") != null && PropertyHelper.getVariable("enableHar2Jmx").equalsIgnoreCase("true")) {
//        //options.addArguments("--ignore-ssl-errors=yes");
//        //options.addArguments("--ignore-certificate-errors");
//
//        //}
//
//
//        for (String variable : props.getStringArray("options." + DriverContext.getInstance().getBrowserName().replaceAll("\\s", ""))) {
//            options.addArguments(variable);
//        }
//
//        log.debug("chrome.options="+ PropertyHelper.getVariable("chrome.options"));
//        if (PropertyHelper.getVariable("chrome.options")!=null){
//            options.addArguments(PropertyHelper.getVariable("chrome.options"));
//        }
//
//        if (DriverContext.getInstance().getBrowserName().contains("kiosk")) {
//            options.addArguments("--kiosk");
//        }
//
//        Map<String, String> map = DriverContext.getInstance().getDriverStack();
//
//
//        if (!map.get("serverType").equalsIgnoreCase("grid")) {
//            if (TestParameters.getInstance().frameworkData().containsKey("scenarioName")){
//                options.setCapability("name", TestParameters.getInstance().getFrameworkData("scenarioName"));
//            }
//        }
//
//        if (TestParameters.getInstance().frameworkData().containsKey("projectName"))
//            options.setCapability("project", TestParameters.getInstance().getFrameworkData("projectName"));
//
//        if (TestParameters.getInstance().frameworkData().containsKey("buildNumber"))
//            options.setCapability("build", TestParameters.getInstance().getFrameworkData("buildNumber"));
//
//        for (Map.Entry<String, String> pair : DriverContext.getInstance().getDriverStack().entrySet()) {
//            if (!pair.getKey().equalsIgnoreCase("serverType") && !pair.getKey().equalsIgnoreCase("description"))
//                options.setCapability(pair.getKey(), pair.getValue());
//        }
//
//        driver = new ChromeDriver(options);
    }

    @Override
    public void updateResults(String result) {
        //do nothing
    }
}