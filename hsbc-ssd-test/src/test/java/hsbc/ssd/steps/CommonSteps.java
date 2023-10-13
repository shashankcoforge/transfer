package hsbc.ssd.steps;

import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import hsbc.ssd.pageObjects.bo.HomePage;
import hsbc.ssd.pageObjects.bo.LoginPage;
import hsbc.ssd.pageObjects.bo.TPALoginPage;
import hsbc.ssd.utils.helper.PropertyHelper;
import hsbc.ssd.utils.scp.FileTransfer;
import hsbc.ssd.utils.scp.SSHJobs;
import hsbc.ssd.utils.selenium.TestParameters;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import hsbc.ssd.utils.selenium.PageObjectUtil;

import java.io.IOException;
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

    @Given("Dynamic User password is available")
    public void dynamicPasswordIsAvailable() {
        if (PropertyHelper.isUserPasswordAvailable()) {
            TestParameters.getInstance().setTestData("dynamicPassword", PropertyHelper.getEnvSpecificAppParameters("dynamicPassword"));
        } else {
            launchApp("TPA");
            String dynamicPassword = tpaLoginPage.generateDynamicPassword(PropertyHelper.getEnvSpecificAppParameters("username"), PropertyHelper.getEnvSpecificAppParameters("password"));
            TestParameters.getInstance().setTestData("dynamicPassword", dynamicPassword);
        }
    }

    @When("the user login into the application")
    public void theUserLoginIntoTheApplication() {
        loginPage.login(PropertyHelper.getEnvSpecificAppParameters("username"), TestParameters.getInstance().getTestData("dynamicPassword"));
    }

    @When("^the user click on Logout button$")
    public void clickLogout() {
        homepage.clickLogoutButton();
    }

    @And("the user adds {string} file into Task Server")
    public void theUserAddsFileIntoTaskServer(String feedType) throws JSchException, IOException {
        Session session = FileTransfer.createSession(PropertyHelper.getEnvSpecificAppParameters("tsUser"),
                PropertyHelper.getEnvSpecificAppParameters("hostname"), Integer.parseInt(PropertyHelper.getEnvSpecificAppParameters("port")), TestParameters.getInstance().getTestData("dynamicPassword"));
        switch (feedType) {
            case "XML Feed":
                FileTransfer.copyLocalToRemote(session, PropertyHelper.getEnvSpecificAppParameters("xmlFeed"), PropertyHelper.getEnvSpecificAppParameters("taskServerDropLocation"));
                break;
            default:
                break;
        }
    }

    @And("the user runs file mover job")
    public void theUserRunsFileMoverJob() throws JSchException, IOException {
        Session session = SSHJobs.createSession(PropertyHelper.getEnvSpecificAppParameters("tsUser"),
                PropertyHelper.getEnvSpecificAppParameters("hostname"), Integer.parseInt(PropertyHelper.getEnvSpecificAppParameters("port")), TestParameters.getInstance().getTestData("dynamicPassword"));
        SSHJobs.runCommandOnRemote(session, "File Mover");
    }

    @And("the user runs feed loader job")
    public void theUserRunsFeedLoaderJob() throws JSchException, IOException {
        Session session = SSHJobs.createSession(PropertyHelper.getEnvSpecificAppParameters("tsUser"),
                PropertyHelper.getEnvSpecificAppParameters("hostname"), Integer.parseInt(PropertyHelper.getEnvSpecificAppParameters("port")), TestParameters.getInstance().getTestData("dynamicPassword"));
        SSHJobs.runCommandOnRemote(session, "Feed Loader");
    }
}
