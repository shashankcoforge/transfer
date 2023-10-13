package hsbc.qs.runners.ssd;

import io.cucumber.testng.CucumberOptions;
import hsbc.qs.runners.SequentialBaseRunner;

@CucumberOptions(tags = {"@Regression" , "not @ignore"},
                 features = "classpath:features/BackOffice"
                // , dryRun=true
)

public class SSDRegression extends SequentialBaseRunner {}