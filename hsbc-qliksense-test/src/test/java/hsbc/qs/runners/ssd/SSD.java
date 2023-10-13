package hsbc.qs.runners.ssd;

import io.cucumber.testng.CucumberOptions;
import hsbc.qs.runners.SequentialBaseRunner;

@CucumberOptions(tags = {"@Shashank"},
                 features = "classpath:features/Set1"
                // , dryRun=true
)

public class SSD extends SequentialBaseRunner {}