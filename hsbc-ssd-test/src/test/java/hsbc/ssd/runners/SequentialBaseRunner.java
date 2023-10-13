package hsbc.ssd.runners;

import hsbc.ssd.utils.reporting.TextReport;
import io.cucumber.testng.CucumberOptions;
import org.testng.annotations.AfterTest;


@CucumberOptions(
        plugin = {"hsbc.ssd.utils.reporting.adapter.ExtentCucumberAdapter:", "hsbc.ssd.utils.eventHandler.TestEventHandlerPlugin"},
        glue = {"steps", "hooks"}
)

public class SequentialBaseRunner extends RunnerClassSequential {

    //TestNG after hook
    @AfterTest
    public void teardown() {
        TextReport tr = new TextReport();
        tr.createReport(true);
    }
}