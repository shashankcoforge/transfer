package hsbc.ssd.runners.ssd;

import io.cucumber.testng.CucumberOptions;
import hsbc.ssd.runners.SequentialBaseRunner;

@CucumberOptions(tags = {"@Smoke" , "not @ignore"},
                 features = "classpath:features/BackOffice/BackOffice.feature"
                // , dryRun=true
)

public class SSDSmoke extends SequentialBaseRunner {}