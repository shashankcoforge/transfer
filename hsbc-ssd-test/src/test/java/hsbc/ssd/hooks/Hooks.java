package hsbc.ssd.hooks;

import hsbc.ssd.utils.selenium.DriverContext;
import hsbc.ssd.utils.selenium.DriverFactory;
import hsbc.ssd.utils.selenium.Screenshot;
import hsbc.ssd.utils.selenium.TestParameters;
import io.cucumber.core.api.Scenario;
import io.cucumber.java.After;
import io.cucumber.java8.En;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import hsbc.ssd.utils.Constants;
import hsbc.ssd.utils.helper.PropertyHelper;
import hsbc.ssd.utils.reporting.Reporter;
import hsbc.ssd.utils.zephyr.ZephyrScale;

import java.io.File;

public class Hooks implements En{

	protected Logger log = LogManager.getLogger(this.getClass().getName());

	public Hooks() {
		Before(10, (Scenario scenario) -> {
			String[] tab = scenario.getId().split("/");
			int rawFeatureNameLength = tab.length;
			String featureName = tab[rawFeatureNameLength - 1].split(":")[0];
			TestParameters.getInstance().putFrameworkData("featureName", featureName);
			String scenarioName = scenario.getName();
			TestParameters.getInstance().putFrameworkData("scenarioName", scenarioName);
			TestParameters.getInstance().putFrameworkData("fullScenarioName", featureName + "-" + scenarioName);
			TestParameters.getInstance().putFrameworkData("cucumberTest", "true");
			log.info("**********************************************************");
			log.info(featureName + " : " + scenarioName);
			log.info("**********************************************************");
			TestParameters.getInstance().deleteTestData("comments");
				scenario.getSourceTagNames().forEach(tag -> {
					if (tag.toLowerCase().contains("device")) {
						TestParameters.getInstance().setTestData("deviceID", tag.replaceAll("@", ""));
					}
				});
		});

	}

	@After(order = 30)
	public void checkScenarioStatus(Scenario scenario) {
		try {
			String absolutePath = "";
			if (DriverContext.getInstance().getBrowserName() != null) {
					if (scenario.isFailed()) {
						boolean errorScreenShotTaken = false;
						String screenshotOnFailure = PropertyHelper.getVariable("screenshotOnFailure") != null
								? PropertyHelper.getVariable("screenshotOnFailure")
								: PropertyHelper.getDefaultProperty("screenshotOnFailure");

						if (Boolean.parseBoolean(screenshotOnFailure)) {
							try {
								File img = new Screenshot().grabScreenshot();
								if (img != null) {
									File file = new Screenshot().saveScreenshot(img, Reporter.getScreenshotPath());
									String relativePath = "." + File.separator + "Screenshots_" + Constants.DEFAULTTIMESTAMP + File.separator + file.getName();
									absolutePath = file.getAbsolutePath();
									TestParameters.getInstance().putFrameworkData("screenshotRelativePath", relativePath);
									TestParameters.getInstance().putFrameworkData("screenshotAbsolutePath", absolutePath);
									errorScreenShotTaken = true;
								}
							} catch (Exception e) {
								e.printStackTrace();
							}
							if (!errorScreenShotTaken) log.debug("no screenshot taken");
						}
					}

					DriverFactory.getInstance().driverManager().updateResults(scenario.isFailed() ? "failed" : "passed");
					TestParameters.getInstance().setTestData("PreviousScenarioStatus", scenario.isFailed() ? "failed" : "passed");
					if (!DriverContext.getInstance().getKeepBrowserOpen() || scenario.isFailed()) {
						DriverFactory.getInstance().quitAll();
						TestParameters.getInstance().setTestData("DriverTerminated","yes");
						DriverContext.getInstance().setBrowserInstanceKilledAtHooks(true);
						TestParameters.getInstance().deleteTestData("deviceActivated", "true");
						DriverContext.getInstance().setKeepBrowserOpen(false);
					}
			}

			new ZephyrScale().executeScenario(scenario, absolutePath);
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	@After(value= "@end")
	public void endItAll() {
		DriverFactory.getInstance().quitAll();
	}

}
