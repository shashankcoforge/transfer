package hsbc.qs.utils.selenium;

import hsbc.qs.utils.selenium.driverManagers.ChromeDriverManager;
import hsbc.qs.utils.selenium.driverManagers.EdgeDriverManager;
//import hsbc.ssd.utils.selenium.driverManagers.FirefoxDriverManager;
import hsbc.qs.utils.selenium.driverManagers.GridDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;
import hsbc.qs.utils.helper.PropertyHelper;

/**
 * Threadlocal instance of WebDriver using Factory pattern
 * Supports remote execution via Selenium Grid or local execution.
 * <p>
 * The use of either grid/local is set in the Context instance for
 * the execution thread (via the TestNG DataProvider). For grid then the address
 * of the target host and any credentials are specified in the project properties file.
 * <p>
 * For local execution the browsers currently supported are Firefox, Chrome and Edge,
 * but this can be extended as needed by adding further
 * DriverManager classes.
 * <p>
 * The locations of the required drivers/binaries are specified in the project properties file.
 */

public class DriverFactory {

    public enum ServerType {
        local, grid;
    }

    public enum BrowserType {
        chrome, edge;
    }

    protected DriverFactory() {
    }

    private static DriverFactory instance = new DriverFactory();

    public static DriverFactory getInstance() {
        return instance;
    }

    ThreadLocal<DriverManager> driverManager = new ThreadLocal<DriverManager>() {
        protected DriverManager initialValue() {
            return setDM();
        }
    };

    public DriverManager driverManager() {
        return driverManager.get();
    }

    public WebDriver getDriver() {
        return driverManager.get().getDriver();
    }

    public WebDriverWait getWait() {
        return driverManager.get().getWait();
    }
    public void quit() {
        driverManager.get().quitDriver();
    }

    public void quitAll() {
        quit();
        driverManager.remove();
    }

    public DriverManager setDM() {
        ServerType serverType = ServerType.valueOf(PropertyHelper.getVariable("serverType") != null
                ? PropertyHelper.getVariable("serverType")
                : PropertyHelper.getDefaultProperty("serverType"));
        String browserType = DriverContext.getInstance().getBrowserName();

        switch (serverType) {
            case grid:
                driverManager.set(new GridDriverManager());
                break;
                default:
                switch (browserType) {
                    case "chrome":
//                        driverManager.set(new ChromeDriverManager());
                        break;
//                    case "firefox":
//                        driverManager.set(new FirefoxDriverManager());
//                        break;
                    case "MicrosoftEdge":
                        driverManager.set(new EdgeDriverManager());
                        break;
                }
                break;
        }

        return driverManager.get();

    }
} 