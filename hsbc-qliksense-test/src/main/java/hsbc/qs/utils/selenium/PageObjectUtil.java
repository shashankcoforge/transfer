package hsbc.qs.utils.selenium;

import hsbc.qs.utils.helper.PropertyHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Action;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

public class PageObjectUtil {

    protected WebDriver driver;
    protected Logger log = LogManager.getLogger(this.getClass().getName());
    private String appName;

    public PageObjectUtil() {
        this.driver = DriverFactory.getInstance().getDriver();
    }

    /**
     * return web driver for the current thread
     */
    public WebDriver getDriver() {
        log.debug("Getting the driver object for current thread");
        return this.driver;
    }

    public void launchApp(String app){
        this.appName = app;
        log.debug("browser launched");
        String url = PropertyHelper.getEnvSpecificAppParameters(app);
        log.debug("Navigating to url: "+url);
        getDriver().manage().timeouts().implicitlyWait(Duration.ofSeconds(getWaitDuration(), 1));
        getDriver().get(url);
        getDriver().manage().window().maximize();
        waitPageToLoad();
//        waitInSeconds(3);
    }

    /** Returns duration for specified waits */
    @SuppressWarnings("unused")
    protected int getWaitDuration(){
        final int defaultWait = 10;
        int duration;
        try {
            duration = Integer.parseInt(PropertyHelper.getDefaultProperty("defaultWait"));
            log.debug("selenium driver wait time set from environment properties");
        } catch (Exception e) {
            duration = defaultWait;
            log.debug("selenium driver wait time not available from environment properties...default applied="+defaultWait);
        }
        return duration;
    }

    /**
     * return web driver wait for the current thread
     */
    public WebDriverWait getWait(long... maxWaitLimit) {
        log.debug("obtaining the wait object for current thread");
        return new WebDriverWait(getDriver(), Duration.ofSeconds(maxWaitLimit.length>0 ? maxWaitLimit[0]: getWaitDuration(),1));
    }

    public void initialise(Object obj) {
        PageFactory.initElements(getDriver(), obj);
    }


    /**
     * Set of common methods for Page Objects which are defined with either
     * standard By locators or PageFactory
     * @param url
     * @return
     */
    public PageObjectUtil gotoURL(String url) {
        log.debug("navigating to url:" + url);
        getDriver().get(url);
        return this;
    }

    /**
     * Wait for page to load
     */
    public PageObjectUtil waitPageToLoad() {
        domLoaded();
        jqueryLoaded();
        return this;
    }

    /**
     * Wait for page to load based on document.readyState=complete
     */
    public void domLoaded() {
        log.debug("checking that the DOM is loaded");
        final JavascriptExecutor js = (JavascriptExecutor) getDriver();
        boolean domReady = js.executeScript("return document.readyState").equals("complete");

        if (!domReady) {
            getWait().until(new ExpectedCondition<Boolean>() {
                public Boolean apply(WebDriver d) {
                    return (js.executeScript("return document.readyState").equals("complete"));
                }
            });
        }
    }

    /**
     * Wait for JQuery to load
     */
    private void jqueryLoaded() {
        log.debug("checking that any JQuery operations complete");
        final JavascriptExecutor js = (JavascriptExecutor) getDriver();

        if ((Boolean) js.executeScript("return typeof jQuery != 'undefined'")) {
            boolean jqueryReady = (Boolean) js.executeScript("return jQuery.active==0");

            if (!jqueryReady) {
                getWait().until(new ExpectedCondition<Boolean>() {
                    public Boolean apply(WebDriver d) {
                        return (Boolean) js.executeScript("return window.jQuery.active === 0");
                    }
                });
            }
        }
    }


    /**
     * searches again for the element using the by
     *
     * @param by locate element by
     * @param retryFindElement number of retries
     * @return Element
     */
    public WebElement findElement(By by, boolean... retryFindElement) {
        WebElement element = null;
        log.info("Attempting to find the element: " + by.toString());
        int attempts = 0;
        boolean retry = retryFindElement.length > 0 && retryFindElement[0];
        int maxRetry = getFindRetries();
        if(retry){
            try {
                element = getWait()
                        .until(ExpectedConditions.presenceOfElementLocated(by));
                element.getTagName();
            } catch (Exception e) {
                while (attempts < maxRetry && retry) {
                    try {
                        log.info("Attempting to re-find the element: " + by.toString() + " Attempt No: " + attempts);
                        element = getWait().ignoring(StaleElementReferenceException.class)
                                .until(ExpectedConditions.presenceOfElementLocated(by));
                        element.getTagName();
                        retry = false;
                    } catch (Exception ex) {
                        waitFor(500);
                    }
                    attempts++;
                }
            }
        }else {
            while (attempts < maxRetry && retry) {
                try {
                    log.info("Attempting to re-find the element: " + by.toString()+" Attempt No: "+attempts);
                    element = getWait().ignoring(StaleElementReferenceException.class)
                            .until(ExpectedConditions.presenceOfElementLocated(by));
                    element.getTagName();
                    retry = false;
                } catch (Exception e) {
                    waitFor(500);
                }
                attempts++;
            }
        }
        return element;
    }

    /**
     * searches again for the element using the by
     *
     * @param by locate element by
     * @param retries number of retries
     * @return Element
     */
    public WebElement findElement(By by, int... retries) {
        WebElement element = null;
        log.info("Attempting to find the element with retry: " + by.toString());
        int attempts = 0;
        boolean retry = true;
        int maxRetry = retries.length > 0 ? retries[0] : getFindRetries();
       while (attempts < maxRetry && retry) {
            try {
                log.info("Attempting to re-find the element: " + by.toString()+" Attempt No: "+(attempts+1));
                element = getWait().ignoring(StaleElementReferenceException.class)
                        .until(ExpectedConditions.visibilityOfElementLocated(by));
                element.getTagName();
                retry = false;
            } catch (Exception e) {
                waitFor(500);
            }
            attempts++;
        }
        return element;
    }

    public WebElement findElement(By by){
        WebElement element = getWait().ignoring(StaleElementReferenceException.class)
                .until(ExpectedConditions.presenceOfElementLocated(by));
        element.getTagName();
        return element;
    }

    public WebElement findElementUsingJQuery(String queryString) {
        WebElement tmpElement = (WebElement)((JavascriptExecutor) getDriver()).executeScript(queryString, new Object[0]);
        return tmpElement;
    }

    public WebElement quickLocateElement(By by){
        WebElement element = null;
        try{
            getDriver().manage().timeouts().implicitlyWait(Duration.ofSeconds(2));
            element = getDriver().findElement(by);
            getDriver().manage().timeouts().implicitlyWait(Duration.ofSeconds(getWaitDuration()));
        }catch(Exception e){}
        return element;
    }

    public List<WebElement> quickLocateElementList(By by){
        List<WebElement> elementList = null;
        try{
            getDriver().manage().timeouts().implicitlyWait(Duration.ofSeconds(2));
            elementList = getDriver().findElements(by);
            getDriver().manage().timeouts().implicitlyWait(Duration.ofSeconds(getWaitDuration()));
        }catch(Exception e){}
        return elementList;
    }

    private static int getFindRetries() {
        final int defaultFindRetries = 10;
        int refind;
        try {
            refind = Integer.parseInt(PropertyHelper.getDefaultProperty("defaultElementRetries"));
        } catch (Exception e) {
            refind = defaultFindRetries;
        }
        return refind;
    }

    /**
     * Function to pause the execution for the specified time period
     *
     * @param milliSeconds
     *            The wait time in milliseconds
     */
    public void waitFor(long milliSeconds) {
        try {
            Thread.sleep(milliSeconds);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Function to pause the execution for the specified time period
     *
     * @param seconds
     *            The wait time in milliseconds
     */
    public void waitInSeconds(long seconds) {
        try {
            Thread.sleep(seconds * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Function to wait until the specified element is located
     *
     * @param by
     *            The {@link WebDriver} locator used to identify the element
     * @param timeOutInSeconds
     *            The wait timeout in seconds
     */
    public void waitUntilElementLocated(By by, long timeOutInSeconds) {
        (new WebDriverWait(DriverFactory.getInstance().getDriver(), Duration.ofSeconds(timeOutInSeconds, 1)))
                .ignoring(StaleElementReferenceException.class)
                .until(ExpectedConditions
                .presenceOfElementLocated(by));
    }

    /**
     * Function to wait until the specified element is located with default wait
     *
     * @param by
     *            The {@link WebDriver} locator used to identify the element
     *            The wait timeout in seconds
     */
    public void waitUntilElementLocated(By by) {
        getWait().ignoring(StaleElementReferenceException.class)
                .until(ExpectedConditions.presenceOfElementLocated(by));
    }

    /**
     * Function to wait until the specified element is visible
     *
     * @param by
     *            The {@link WebDriver} locator used to identify the element
     * @param timeOutInSeconds
     *            The wait timeout in seconds
     */
    public void waitUntilElementVisible(By by, long timeOutInSeconds) {
        (new WebDriverWait(DriverFactory.getInstance().getDriver(), Duration.ofSeconds(timeOutInSeconds, 1)))
                .ignoring(StaleElementReferenceException.class)
                .until(ExpectedConditions
                .visibilityOfElementLocated(by));
    }

    /**
     * Function to wait until the specified element is visible with default wait
     *
     * @param by
     *            The {@link WebDriver} locator used to identify the element
     *            The wait timeout in seconds
     */
    public void waitUntilElementVisible(By by) {
        getWait().ignoring(StaleElementReferenceException.class)
                .until(ExpectedConditions.visibilityOfElementLocated(by));
    }

    /**
     * Function to wait until the specified element is visible
     *
     * @param element
     *            The {@link WebDriver} locator used to identify the element
     * @param timeOutInSeconds
     *            The wait timeout in seconds
     */
    public WebElement waitUntilElementVisible(WebElement element, long timeOutInSeconds) {
        (new WebDriverWait(DriverFactory.getInstance().getDriver(), Duration.ofSeconds(timeOutInSeconds, 1)))
                .ignoring(StaleElementReferenceException.class)
                .until(ExpectedConditions
                .visibilityOf(element));
        return element;
    }

    /**
     * Function to wait until the specified element is visible with default wait
     *
     * @param element
     *            The {@link WebDriver} locator used to identify the element
     *            The wait timeout in seconds
     */
    public WebElement waitUntilElementVisible(WebElement element) {
        getWait().ignoring(StaleElementReferenceException.class)
                .until(ExpectedConditions.visibilityOf(element));
        return element;
    }

    /**
     * Function to wait until the specified element is clickable
     *
     * @param by
     *            The {@link WebDriver} locator used to identify the element
     * @param timeOutInSeconds
     *            The wait timeout in seconds
     */
    public void waitUntilElementClickable(By by, long timeOutInSeconds) {
        (new WebDriverWait(DriverFactory.getInstance().getDriver(), Duration.ofSeconds(timeOutInSeconds, 1)))
                .ignoring(StaleElementReferenceException.class)
                .until(ExpectedConditions
                .elementToBeClickable(by));
    }

    /**
     * Function to wait until the specified element is clickable  with default wait
     *
     * @param by
     *            The {@link WebDriver} locator used to identify the element
     *            The wait timeout in seconds
     */
    public void waitUntilElementClickable(By by) {
       getWait().ignoring(StaleElementReferenceException.class)
               .until(ExpectedConditions.elementToBeClickable(by));
    }

    /**
     * Function to wait until the specified element is clickable
     *
     * @param element
     *            The {@link WebDriver} locator used to identify the element
     * @param timeOutInSeconds
     *            The wait timeout in seconds
     */
    public WebElement waitUntilElementClickable(WebElement element, long timeOutInSeconds) {
        (new WebDriverWait(DriverFactory.getInstance().getDriver(), Duration.ofSeconds(timeOutInSeconds, 1)))
                .ignoring(StaleElementReferenceException.class)
                .until(ExpectedConditions
                .elementToBeClickable(element));
        return element;
    }

    /**
     * Function to wait until the specified element is clickable  with default wait
     *
     * @param element
     *            The {@link WebDriver} locator used to identify the element
     */
    public WebElement waitUntilElementClickable(WebElement element) {
        getWait().ignoring(StaleElementReferenceException.class)
                .until(ExpectedConditions.elementToBeClickable(element));
        return element;
    }

    /**
     * Function to wait until the specified element is visible and clickable with default wait
     *
     * @param element
     *            The {@link WebDriver} locator used to identify the element
     */
    public WebElement waitUntilElementVisibleAndClickable(WebElement element) {
        getWait()
                .ignoring(StaleElementReferenceException.class)
                .until(ExpectedConditions.visibilityOf(element));
        getWait()
                .ignoring(StaleElementReferenceException.class)
                .until(ExpectedConditions.elementToBeClickable(element));
        return element;
    }

    /**
     * Function to wait until the specified element is visible and clickable
     *
     * @param element
     * @param timeOutInSeconds
     *            The {@link WebDriver} locator used to identify the element
     */
    public WebElement waitUntilElementVisibleAndClickable(WebElement element, long timeOutInSeconds) {
        (new WebDriverWait(DriverFactory.getInstance().getDriver(), Duration.ofSeconds(timeOutInSeconds, 1)))
                .ignoring(StaleElementReferenceException.class)
                .until(ExpectedConditions.visibilityOf(element));
        (new WebDriverWait(DriverFactory.getInstance().getDriver(), Duration.ofSeconds(timeOutInSeconds, 1)))
                .ignoring(StaleElementReferenceException.class)
                .until(ExpectedConditions.elementToBeClickable(element));
        return element;
    }

    /**
     * Function to wait until the specified element is disabled
     *
     * @param by
     *            The {@link WebDriver} locator used to identify the element
     * @param timeOutInSeconds
     *            The wait timeout in seconds
     */
    public void waitUntilElementDisabled(By by, long timeOutInSeconds) {
        (new WebDriverWait(DriverFactory.getInstance().getDriver(), Duration.ofSeconds(timeOutInSeconds, 1)))
                .ignoring(StaleElementReferenceException.class)
                .until(ExpectedConditions
                .not(ExpectedConditions.elementToBeClickable(by)));
    }

    /**
     * Function to wait until the specified element is disabled with default wait
     *
     * @param by
     *            The {@link WebDriver} locator used to identify the element
     *            The wait timeout in seconds
     */
    public void waitUntilElementDisabled(By by) {
        getWait().ignoring(StaleElementReferenceException.class)
                .until(ExpectedConditions.not(ExpectedConditions.elementToBeClickable(by)));
    }

    /**
     * Function to wait until the specified element is disabled
     *
     * @param element
     *            The {@link WebDriver} locator used to identify the element
     * @param timeOutInSeconds
     *            The wait timeout in seconds
     */
    public void waitUntilElementDisabled(WebElement element, long timeOutInSeconds) {
        (new WebDriverWait(DriverFactory.getInstance().getDriver(), Duration.ofSeconds(timeOutInSeconds, 1)))
                .ignoring(StaleElementReferenceException.class)
                .until(ExpectedConditions
                .not(ExpectedConditions.elementToBeClickable(element)));
    }

    /**
     * Function to wait until the specified element is disabled with default wait
     *
     * @param element
     *            The {@link WebDriver} locator used to identify the element
     *            The wait timeout in seconds
     */
    public void waitUntilElementDisabled(WebElement element) {
        getWait().ignoring(StaleElementReferenceException.class)
                .until(ExpectedConditions.not(ExpectedConditions.elementToBeClickable(element)));
    }


    /**
     * Checks for element existence
     *
     * @param by
     * @param retries
     * @return
     */
    public boolean isElementExists(By by, int... retries) {
        WebElement element = findElement(by, retries);
        return element != null;
    }

    /**
     * Checks for element existence
     *
     * @param element
     * @return
     */
    public boolean isElementExists(WebElement element) {
        return element != null;
    }

    /**
     * is this element displayed or not?
     *
     * @param by
     * @return
     */
    public boolean isElementVisible(By by, int... retries) {
        return findElement(by, retries).isDisplayed();
    }

    /**
     * is this element displayed or not?
     *
     * @param element
     * @return
     */
    public boolean isElementVisible(WebElement element) {
        boolean status = false;
        try{
            status = element.isDisplayed();
        }catch(Exception e){
        }
        return status;
    }

    /**
     * is this element displayed or not?
     *
     * @param by
     * @return
     */
    public boolean isElementDisplayed(By by, int... retries) {
    	boolean status=findElement(by,retries)==null;
    	if(status)
    		return false;
    	else
        return findElement(by, retries).isDisplayed();
    }

    /**
     * is this element displayed or not?
     *
     * @param element
     * @return
     */
    public boolean isElementDisplayed(WebElement element) {
        return element.isDisplayed();
    }

    /**
     * is this element enabled or not?
     *
     * @param by locator of element
     * @return true if enabled
     */
    public boolean isElementEnabled(By by, int... retries) {
        return findElement(by, retries).isEnabled();
    }

    /**
     * is this element enabled or not?
     *
     * @param element locator of element
     * @return true if enabled
     */
    public boolean isElementEnabled(WebElement element) {
        boolean status = false;
        if(isElementExists(element)){
            status = element.isEnabled();
        }
        return status;
    }

    /**
     * Function to select the specified value from a dropdown
     *
     * @param by
     *            The {@link WebDriver} locator used to identify the listbox
     * @param item
     *            The value to be selected within the listbox
     */
    public void selectListItem(By by, String item) {
        Select dropDownList = new Select(DriverFactory.getInstance().getDriver().findElement(by));
        dropDownList.selectByVisibleText(item);
    }

    /**
     * Function to select the specified value from a dropdown
     *
     * @param element
     *            The {@link WebDriver} locator used to identify the listbox
     * @param item
     *            The value to be selected within the listbox
     */
    public void selectListItem(WebElement element, String item) {
        Select dropDownList = new Select(element);
        dropDownList.selectByVisibleText(item);
    }

    /**
     * Function to return the list of items displayed in drop down
     * @param element
     * @return
     */

    public List<WebElement> returnOptions(WebElement element){
        Select dropDownList = new Select(element);
        return dropDownList.getOptions();
    }

    /**
     * Function to verify whether the specified object exists within the current
     * page
     *
     * @param by
     *            The {@link WebDriver} locator used to identify the element
     * @return Boolean value indicating whether the specified object exists
     */
    public Boolean objectExists(By by) {
        if (DriverFactory.getInstance().getDriver().findElements(by).size() == 0) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * Function to verify whether the specified object exists within the current
     * page
     *
     * @param elementList
     *            The {@link WebDriver} locator used to identify the element
     * @return Boolean value indicating whether the specified object exists
     */
    public Boolean objectExists(List<WebElement> elementList) {
        if (elementList.size() == 0) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * Function to verify whether the specified text is present within the
     * current page
     *
     * @param textPattern
     *            The text to be verified
     * @return Boolean value indicating whether the specified test is present
     */
    public Boolean isTextPresent(String textPattern) {
        if (DriverFactory.getInstance().getDriver().findElement(By.cssSelector("BODY")).getText()
                .matches(textPattern)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Function to verify whether the specified text is present within the
     * current page
     *
     * @param element
     * @param textPattern
     *            The text to be verified
     * @return Boolean value indicating whether the specified test is present
     */
    public Boolean isTextPresent(WebElement element, String textPattern) {
        if (element.getText().matches(textPattern)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Function to check if an alert is present on the current page
     *
     * @param timeOutInSeconds
     *            The number of seconds to wait while checking for the alert
     * @return Boolean value indicating whether an alert is present
     */
    public Boolean isAlertPresent(long timeOutInSeconds) {
        try {
            new WebDriverWait(DriverFactory.getInstance().getDriver(), Duration.ofSeconds(timeOutInSeconds, 1))
                    .until(ExpectedConditions.alertIsPresent());
            return true;
        } catch (TimeoutException ex) {
            return false;
        }
    }

    /**
     * Function to check if an alert is present on the current page
     *
     *            The number of seconds to wait while checking for the alert
     * @return Boolean value indicating whether an alert is present
     */
    public Boolean isAlertPresent() {
        try {
           getWait().until(ExpectedConditions.alertIsPresent());
            return true;
        } catch (TimeoutException ex) {
            return false;
        }
    }

    /**
     * Performs mouse click action on the element using javascript where native click does not work
     */
    public void clickJS(WebElement element) {
        JavascriptExecutor executor = (JavascriptExecutor) getDriver();
        executor.executeScript("arguments[0].click();", element);
    }

    public void setOrClearInputElementValueByJS(WebElement element, String... value) {
        try {
            JavascriptExecutor executor = (JavascriptExecutor) getDriver();
            executor.executeScript("arguments[0].value='" + (value.length>0?value[0]:"") + "'", element);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * send text using javascript
     */
    public void sendKeysJS(WebElement element, String val) {
        waitInSeconds(2);
        JavascriptExecutor executor = (JavascriptExecutor) getDriver();
        executor.executeScript("arguments[0].setAttribute('value', '" + val + "')", element);
    }

    public void clearInputField(WebElement element, boolean... applyWait) {
        String inputText = element.getAttribute("value");
        if(applyWait.length>0) {
            waitInSeconds(3);
        }
        if( inputText != null ) {
            for(int i=0; i<inputText.length();i++) {
                waitUntilElementVisible(element, 10);
                element.sendKeys(Keys.BACK_SPACE);
            }
        }
    }

    /**
     * Scrolls to element to avoid issues with element location being unclickable
     */
    public void scroll(WebElement element) {
        try {
            ((JavascriptExecutor) getDriver()).executeScript("arguments[0].scrollIntoView(true);", element);
        } catch (Exception e) {
            log.warn("scrolling to element failed", e);
        }
    }

    public void moverToElement(WebElement element){
        try {
            Actions action = new Actions(getDriver());
            action.moveToElement(element);
            action.release();
        } catch (Exception e) {
            log.warn("Moving focus to element failed", e);
        }
    }

    public void clickElementUsingAction(WebElement element){
        Actions ob = new Actions(getDriver());
        scroll(element);
        ob.moveToElement(element).build().perform();
        ob.click(element);
        Action action  = ob.build();
        action.perform();
    }

    public void scrollToTop(){
        ((JavascriptExecutor) driver)
                .executeScript("window.scrollTo(0, -document.body.scrollHeight)");
    }

    /**
     * used for scrolling down to the bottom of popup window which is passed as argument
     * @param element
     */

    public void scrollBottomOfPopup(WebElement element) {
        try {
    		((JavascriptExecutor) getDriver()).executeScript("arguments[0].scrollTop = arguments[0].scrollHeight",element);
        } catch (Exception e) {
            log.warn("scrolling to element failed", e);
        }
    }

    /**
     * Scrolls to element to avoid issues with element location being unclickable
     *
     * @param hAlign defines vertical alignment
     *               One of start, center, end or nearest. Defaults to start
     * @param vAlign vertical alignment
     *               One of start, center, end or nearest. Defaults to start
     */
    public void scroll(WebElement element, String hAlign, String vAlign) {
        try {
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({behavior: \"smooth\", block: \"" + vAlign + "\", inline: \"" + hAlign + "\"}););", element);
        } catch (Exception e) {
            log.warn("scrolling to element failed", e);
        }
    }

    /**
     * Switch the focus of future commands for this driver to the window with the given handle.
     *
     * @param parent he name of the window or the handle which can be used to iterate over all open windows
     */
    public void switchWindow(String parent) {
        log.debug("parent window handle:" + parent);
        switching:
        while (true) {
            for (String handle : getDriver().getWindowHandles()) {
                if (!handle.equals(parent)) {
                    log.debug("switching to window handle:" + handle);
                    getDriver().switchTo().window(handle);
                    break switching;
                }
            }
        }
    }

    /**
     * An expectation for checking whether the given frame is available to switch to. <p> If the frame
     * is available it switches the given driver to the specified frameIndex.
     *
     * @param frameLocator used to find the frame (index)
     */
    public void switchFrame(int frameLocator) {
        getWait().until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(frameLocator));
    }

    /**
     * An expectation for checking whether the given frame is available to switch to. <p> If the frame
     * is available it switches the given driver to the specified frame.
     *
     * @param frameLocator used to find the frame (id or name)
     */
    public void switchFrame(String frameLocator) {
        getWait().until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(frameLocator));
    }

    /**
     * An expectation for checking whether the given frame is available to switch to. <p> If the frame
     * is available it switches the given driver to the specified frame.
     *
     * @param by used to find the frame
     */
    public void switchFrame(By by) {
        getWait().until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(by));
    }

    /**
     * An expectation for checking whether the given frame is available to switch to. <p> If the frame
     * is available it switches the given driver to the specified webelement.
     *
     * @param element used to find the frame (webelement)
     */
    public void switchFrame(WebElement element) {
        getWait().until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(element));
        waitInSeconds(3);
    }

    /**
     * Selects either the first frame on the page, or the main document when a page contains iframes.
     */
    public void switchToDefaultContent() {
        getDriver().switchTo().defaultContent();
    }

    /**
     * method to get xpath of the element
     * @param element
     * @return xpath if we use xpath while creating element only
     */
    public String getXpath(WebElement element) {
    	String str=element.toString();
    	if(str.contains("xpath")) {
    		String xpath=str.split("xpath:")[1].trim();
    		return xpath.substring(0,xpath.length()-1);
    	}
    	else
    		return "element does not have xpath";

    }

    public void waitForLoadingImage(WebElement element) {
		int counter=0;
		Boolean status=false;
		while(counter<6) {
				status=isElementDisplayed(element);
				if(status==false)
					break;
				else
					counter++;
		}
	}

    @FindBy(xpath = "//div[@data-testid='testImages']") private WebElement spinner;

    public void clickAndWait(WebElement element) {
        waitPageToLoad();
        waitUntilElementVisibleAndClickable(element).click();
        waitPageToLoad();
        getWait().until(ExpectedConditions.invisibilityOfElementLocated(By.xpath("//div[@data-testid='testImages']")));
    }

    public void waitUntilLoadingInvisible(long... maxWaitLimit){
        if(isElementExists(quickLocateElement(By.xpath("//div[@data-testid='testImages']")))) {
            if(isElementVisible(quickLocateElement(By.xpath("//div[@data-testid='testImages']")))) {
                getWait(maxWaitLimit.length > 0 ? maxWaitLimit[0] : 15).until(ExpectedConditions.invisibilityOfElementLocated(By.xpath("//div[@data-testid='testImages']")));
            }
        }
    }

    public void passValueandEnter(WebElement element){
        getWait()
                .ignoring(StaleElementReferenceException.class)
                .until(ExpectedConditions.and(
                        ExpectedConditions.visibilityOf(element),
                        ExpectedConditions.elementToBeClickable(element)
                ));
        element.sendKeys(Keys.ENTER);
        getWait().until(ExpectedConditions.invisibilityOfElementLocated(By.xpath("//div[@data-testid='testImages']")));
    }

    public void clearCookies(){
        getDriver().manage().deleteAllCookies();
        getDriver().get("chrome://settings/clearBrowserData");
        getDriver().findElement(By.xpath("//settings-ui")).sendKeys(Keys.ENTER);
    }

    public void setAttributeValue(WebElement element, String attributeName, String value){
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("arguments[0].setAttribute(arguments[1],arguments[2])",
                element, attributeName, value
        );
    }

}
    
