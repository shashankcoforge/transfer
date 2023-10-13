package hsbc.ssd.runners;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import hsbc.ssd.utils.Constants;
import hsbc.ssd.utils.helper.JsonHelper;
import hsbc.ssd.utils.helper.PropertyHelper;
import hsbc.ssd.utils.selenium.BaseTest;
import io.cucumber.testng.CucumberFeatureWrapper;
import io.cucumber.testng.CucumberOptions;
import io.cucumber.testng.PickleEventWrapper;
import io.cucumber.testng.TestNGCucumberRunner;
import org.apiguardian.api.API;
import org.json.JSONArray;
import org.json.JSONObject;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Map;

@CucumberOptions(
        strict = true,
        glue = {"hsbc.ssd"}
)

/**
 * The runner class to includes the driver stack with the scenario in test.
 * instead of extending io.cucumber.testng.AbstractTestNGCucumberTests, annotations is used to include the browser
 * details for the scenario found in the features as separated test
 *
 * Extending this class will run the test sequentially.
 */
@API(status = API.Status.STABLE)
public class RunnerClassSequential extends BaseTest {

    private TestNGCucumberRunner testNGCucumberRunner;

    @BeforeClass(alwaysRun = true)
    public void setUpClass() {
        testNGCucumberRunner = new TestNGCucumberRunner(this.getClass());
    }

    @Test(groups = "cucumber", description = "Runs Cucumber Scenarios", dataProvider = "scenarios")
    public void runScenario(PickleEventWrapper pickleWrapper, CucumberFeatureWrapper featureWrapper, Map<String, String> map) throws Throwable {
        // the 'featureWrapper' parameter solely exists to display the feature file in a test report
        // the 'map' parameter is for setting the driver context
        testNGCucumberRunner.runScenario(pickleWrapper.getPickleEvent());
    }

    /**
     * Returns two dimensional array of PickleEventWrapper scenarios with their associated CucumberFeatureWrapper feature.
     * and required browser driver stack for the test execution.
     * The two object is combined here and used further. Each scenario has its browser details.
     * @return a two dimensional array of scenarios features.
     */
    @DataProvider(name = "scenarios")
    public Object[][] scenarios() {
        //list of scenario
        Object[][] x = dpScenario();

        //browser driver stack
        JSONArray jsonArr = dpDriverStackJSON();

        //creating object with scenario and browser stack combination

        Object[][] obj = new Object[x.length * jsonArr.length()][3];
        Gson gson = new GsonBuilder().create();

        int k = 0;

        for (int j = 0; j < jsonArr.length(); j++) {
            JSONObject jsonObj = jsonArr.getJSONObject(j);
            @SuppressWarnings("unchecked")
            Map<String, String> map = gson.fromJson(jsonObj.toString(), Map.class);
            for (int i = 0; i < x.length; i++) {
                obj[k][0] = x[i][0];
                obj[k][1] = x[i][1];
                obj[k][2] = map;
                k++;
            }
        }
        return obj;
    }

    @AfterClass(alwaysRun = true)
    public void tearDownClass() {
        if (testNGCucumberRunner == null) {
            return;
        }
        testNGCucumberRunner.finish();
    }

    /**
     * returns the scenarios
     * @return list of scenario
     */
    public Object[][] dpScenario() {
        if (testNGCucumberRunner == null) {
            return new Object[0][0];
        }

        return testNGCucumberRunner.provideScenarios();
    }

    /**
     * returns the driver stack json input as json array
     * @return driver stack
     */
    public JSONArray dpDriverStackJSON() {
        log.debug("spinning up sequential execution");
        return JsonHelper.getJSONArray(PropertyHelper.getVariable("driverStack") != null
                ? Constants.DRIVERSTACKSPATH + PropertyHelper.getVariable("driverStack") + ".json"
                : Constants.DRIVERSTACKSPATH + PropertyHelper.getDefaultProperty("driverStack") + ".json");
    }
}
