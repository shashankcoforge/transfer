package hsbc.qs.steps;

import hsbc.qs.pageObjects.d1.HomePage;
import hsbc.qs.pageObjects.d1.LoginPage;
import hsbc.qs.pageObjects.d1.TPALoginPage;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import hsbc.qs.utils.selenium.PageObjectUtil;

import java.time.Duration;

public class CommonSteps extends PageObjectUtil {

    private TPALoginPage tpaLoginPage = new TPALoginPage();
    private LoginPage loginPage = new LoginPage();
    private HomePage homepage = new HomePage();

    @Given("^the application \"([^\"]*)\"$")
    public void the_application_something(String app) throws Throwable {
        launchApp(app);
    }

    @Given("^the url \"([^\"]*)\"$")
    public void the_url_something(String url) throws Throwable {
        getDriver().manage().window().maximize();
        getDriver().manage().timeouts().implicitlyWait(Duration.ofSeconds(getWaitDuration(), 1));
        log.debug("browser launched");
        log.debug("Navigating to url: "+url);
        getDriver().get(url);
        getDriver().manage().window().maximize();
        waitPageToLoad();
        waitInSeconds(3);
    }

    @When("^browser is navigated back$")
    public void browser_is_navigated_back() throws Throwable {
        getDriver().navigate().back();
    }

    @When("^browser is navigated forward$")
    public void browser_is_navigated_forward() throws Throwable {
        getDriver().navigate().forward();
    }

    @Given("I navigate to the login page")
    public void iNavigateToTheLoginPage() {

    }


    @Then("the user should see date and time")
    public void theUserShouldSeeDateAndTime() {

    }
}
