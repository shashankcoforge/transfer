package hsbc.qs.utils.selenium;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import hsbc.qs.utils.Constants;
import hsbc.qs.utils.helper.JsonHelper;
import hsbc.qs.utils.helper.PropertyHelper;

import java.lang.reflect.Method;
import java.util.Map;

/**
 * Class to hold start up (set test context) and tear down (shut down browser) for selenium test. This class should be
 * extended by each test class in the test project
 */
public class BaseTest {

    protected Logger log = LogManager.getLogger(this.getClass().getName());
    protected TestParameters context = TestParameters.getInstance();

    /**
     * return web driver for the current thread - can be used when running using TestNG
     */
    public WebDriver getDriver() {
        log.debug("obtaining the driver for current thread");
        return DriverFactory.getInstance().getDriver();
    }

    /**
     * return web driver wait for the current thread - can be used when running using TestNG
     */
    public WebDriverWait getWait() {
        log.debug("obtaining the wait for current thread");
        return DriverFactory.getInstance().getWait();
    }

    /**
     * Read the 'driver stack' for a given test run and enable parallel execution from json file
     */
    @DataProvider(name = "driverStackJSON", parallel = true)
    public Object[][] driverStackJSON() throws Exception {
        log.debug("spinning up parallel execution threads for multi browser testing");
        JSONArray jsonArr = JsonHelper.getJSONArray(PropertyHelper.getVariable("driverStack") != null
                ? Constants.DRIVERSTACKSPATH + PropertyHelper.getVariable("driverStack") + ".json"
                : Constants.DRIVERSTACKSPATH + PropertyHelper.getDefaultProperty("driverStack") + ".json");
        Object[][] obj = new Object[jsonArr.length()][1];
        Gson gson = new GsonBuilder().create();

        for (int i = 0; i < jsonArr.length(); i++) {
            JSONObject jsonObj = jsonArr.getJSONObject(i);
            @SuppressWarnings("unchecked")
            Map<String, String> map = gson.fromJson(jsonObj.toString(), Map.class);
            obj[i][0] = map;
        }

        return obj;
    }

    /**
     * set the test context information
     */
    @BeforeMethod
    public void startUp(Method method, Object[] args) {

        Map<String, String> map = (Map<String, String>) args[args.length - 1];

        if (PropertyHelper.getVariable("PROJECT_NAME") != null && !PropertyHelper.getVariable("PROJECT_NAME").isEmpty())
            TestParameters.getInstance().putFrameworkData("projectName", PropertyHelper.getVariable("PROJECT_NAME"));
        if (PropertyHelper.getVariable("BUILD_NUMBER") != null && !PropertyHelper.getVariable("BUILD_NUMBER").isEmpty())
            TestParameters.getInstance().putFrameworkData("buildNumber", PropertyHelper.getVariable("BUILD_NUMBER"));
        DriverContext.getInstance().setDriverContext(map);
    }

    /**
     * if cucumber test the update the status and removes the current thread's value for this thread-local
     */
    @AfterMethod(groups = {"quitDriver"})
    public void closeDown(ITestResult result) {
        if (!TestParameters.getInstance().frameworkData().containsKey("cucumberTest")) {
            DriverFactory.getInstance().driverManager().updateResults(result.isSuccess() ? "passed" : "failed");
            DriverFactory.getInstance().quitTPA();
        }
    }

 }
