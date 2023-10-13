package hsbc.ssd.pageObjects.bo;

import hsbc.ssd.utils.selenium.DriverContext;
import hsbc.ssd.utils.selenium.PageObjectUtil;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.testng.Assert;

public class HomePage extends PageObjectUtil {
	
	/**
	 * This has the methods and elements which are in menu
	 */

    public HomePage() {initialise(this);}
    
    //task list
    @FindBy(xpath="//li[text()='Task List']") private WebElement taskListBtn;
    
    //Hamburger button
    @FindBy(xpath = "(//*[name()='svg'])[1]") private WebElement hambergerbutton ;
    
    //Cash Management 
    @FindBy(xpath = "//button[contains(text(),'Cash Management')]") private WebElement CashManagementbutton ;
    
    //Branch Overview
    @FindBy(xpath = "(//div[@id='sidebar']//li//div//div//ul//li[contains(text(),'Branch Overview')])[1]") private WebElement Branchoverview ;

	//User button
    @FindBy(xpath="//i[contains(@class,'fa fa-user-circle')]") private WebElement userButton;
    
    //logout button
    @FindBy(xpath="//a[text()='Log Out']") private WebElement logOutButton;
    
    //Cash Movement history
    @FindBy(xpath="//li[text()='Cash Movement History']") private WebElement cashMovementHistoryBtn;
    
    //cash balances history
    @FindBy(xpath="//li[text()='Cash Balance History']") private WebElement cashBalancesHistory;
    
    //pouch preparation sub menu
    @FindBy(xpath="//li[text()='Pouch Preparation']") private WebElement pouchPreperation;
    
    //Pouch Preparation History sub menu
    @FindBy(xpath="//li[text()='Pouch History']") private WebElement pouchPreparationHistoryBtn;
    
  //Branch Management 
    @FindBy(xpath = "//button[contains(text(),'Branch Management')]") private WebElement branchManagementbutton ;
   
  //Manage Cash Drawer
    @FindBy(xpath = "//li[normalize-space()='Manage Cash Drawer']") private WebElement manageCashDrawer ;
    
    //Report Known Variance
    @FindBy(xpath="//li[normalize-space()='Record a known Variance']") private WebElement recordKnownVariance;
    
    //Discrepancy Account
    @FindBy(xpath="//li[normalize-space()='Discrepancy Account']") private WebElement discrepancyAccount;
    
  //Trading Period
    @FindBy(xpath = "//li[normalize-space()='Trading Period']") private WebElement tradingPeriod;
    
  //Balance Period
    @FindBy(xpath = "//li[normalize-space()='Balance Period']") private WebElement balancePeriod;
    
  //Transaction Log
    @FindBy(xpath = "//li[normalize-space()='Transaction Log']") private WebElement transactionLog ;
    
  //BPTPHistory Button
    @FindBy(xpath="(//*[contains(text(),\"Balance / Trading Period History\")])[1]") 
    private WebElement bpTpBtn;
    
    //loading image
    @FindBy(xpath="//h2[text()='Loading']") private WebElement loading;
  
  
   
    /**
     * To click on menu button
     */
  
    public void clickhambergerbutton(){
    	waitPageToLoad();
    	waitInSeconds(3);
    	waitUntilElementVisibleAndClickable(this.hambergerbutton);
    	waitInSeconds(5);
    	this.hambergerbutton.click();
    	waitPageToLoad();
    }

    
    /**
     * To click on branch Overview
     */
    public void clickBranchoverviewtbutton(){
    	waitUntilElementVisibleAndClickable(this.Branchoverview).click();
    	waitInSeconds(5);
        waitPageToLoad();
    }
    
    /**
     * To click on cash Management
     */
    
    public void clickCashManagementbutton(){
		clickJS(this.CashManagementbutton);
        waitPageToLoad();
    }
    
    public void clickTaskList() {
    	waitUntilElementVisibleAndClickable(this.taskListBtn).click();
    }
    
    /**
     * To check whether the title of page is matching the pageName passed
     * @param pageName
     * @return
     */

	public Boolean validatePages(String pageName) {
		Assert.assertNotNull(pageName, "Page Name should not be null");
		Boolean pageFound=false;
		String pageUrl = getDriver().getCurrentUrl();
		if(pageUrl.endsWith(pageName)) {
			pageFound=true;
		}
			else
				pageFound=false;
		return pageFound;
	}
	
	/**
	 * To check whether the main menu exists or not
	 * @return
	 */

	public boolean isMainMenuExists() {
		return isElementDisplayed(this.hambergerbutton);		
	}
	
	/**
	 * To check the logout button after clicking on user button
	 */

	public void checkLogoutButton() {
		waitUntilElementVisibleAndClickable(this.userButton).click();
		waitInSeconds(2);
		if(isElementDisplayed(this.logOutButton))
			log.info("logout button is displayed");
		else
			Assert.fail("logout button is not displayed");
	}
	
	/**
	 * To click logout button
	 */

	public void clickLogoutButton() {
		DriverContext.getInstance().setKeepBrowserOpen(false);   	     //this is to close browser after the scenario
		this.logOutButton.click();
		waitPageToLoad();
		waitInSeconds(5);
	}


	public void checkCashMovementHistory() {
		if(isElementDisplayed(this.cashMovementHistoryBtn))
			log.info("cash movement history is displayed under cash management");
		else
			Assert.fail("cash movement history is not displayed under cash management");
				
	}


	public void clickCashMovmentHistory() {
		this.cashMovementHistoryBtn.click();
	}


	public void clickCashBalancesHistory() {
		this.cashBalancesHistory.click();
		
	}


	public void checkCashBalancesHistoryBtn() {
		if(isElementDisplayed(this.cashBalancesHistory))
			log.info("Cash Balances History is displayed under Cash Management button");
		else
		    Assert.fail("Cash Balances History is not displayed under Cash Management button");
	}


	public void checkPouchPreperation() {
		if(isElementDisplayed(this.pouchPreperation))
			log.info("Pouch Preperation sub menu is displayed under Cash Management button");
		else
		    Assert.fail("Pouch Preperation sub menu is not displayed under Cash Management button");
	}


	public void clickPouchPreperation() {
		this.pouchPreperation.click();
		waitPageToLoad();
	}


	public void checkPouchPreparationHistory() {
		if(isElementDisplayed(this.pouchPreparationHistoryBtn))
			log.info("Pouch Preperation History sub menu is displayed under Cash Management button");
		else
		    Assert.fail("Pouch Preperation History sub menu is not displayed under Cash Management button");
	}


	public void clickPouchPreparationHistory() {
		this.pouchPreparationHistoryBtn.click();
		waitPageToLoad();
		waitInSeconds(5);
	}


	public void clickBranchManagementbutton() {
		waitInSeconds(2);
		waitUntilElementVisibleAndClickable(this.branchManagementbutton).click();
        waitPageToLoad();
	}


	public void checkManageCD() {
		if(isElementDisplayed(this.manageCashDrawer))
			log.info("Manage Cash Drawer is displayed under Branch Management button");
		else
		    Assert.fail("Manage Cash Drawer is not displayed under Branch Management button");		
	}


	public void clickManageCD() {
		this.manageCashDrawer.click();
		//waitForPageToLoad();
		waitInSeconds(2);
	}


	public void checkReportKnownVariance() {
		if(isElementDisplayed(this.recordKnownVariance))
			log.info("Record Known Variance sub menu is displayed under Branch Management button");
		else
		    Assert.fail("Record Known Variance sub menu is not displayed under Branch Management button");		
	}


	public void clickReportKnownVariance() {
		waitUntilElementVisibleAndClickable(this.recordKnownVariance).click();
        waitPageToLoad();		
	}


	public void clickDiscrepancyAccount() {
		waitUntilElementVisibleAndClickable(this.discrepancyAccount).click();
		waitPageToLoad();
		/*WebElement web=findElement(By.tagName("html"));
		web.sendKeys(Keys.chord(Keys.CONTROL,Keys.SUBTRACT));
		web.sendKeys(Keys.chord(Keys.CONTROL,Keys.SUBTRACT));
		JavascriptExecutor executor=(JavascriptExecutor)(getDriver());
		executor.executeScript("document.body.style.zoom='0.9'");
		executor.executeScript("document.body.style.zoom='1'");*/
	}


	public void checkTPMenu() {
		if(isElementDisplayed(this.tradingPeriod))
			log.info("Trading Period is displayed under Branch Management button");
		else
		    Assert.fail("Trading Period is not displayed under Branch Management button");
	}


	public void clickTPMenu() {
		this.tradingPeriod.click();
		waitPageToLoad();
		waitInSeconds(5);
	}
	
	public void checkBPMenu() {
		if(isElementDisplayed(this.balancePeriod))
			log.info("Balance Period is displayed under Branch Management button");
		else
		    Assert.fail("Balance Period is not displayed under Branch Management button");
	}


	public void clickBPMenu() {
		this.balancePeriod.click();
		waitPageToLoad();
		waitInSeconds(5);
	}


	private void waitForPageToLoad() {
		waitForLoadingImage(this.loading);		
	}


	public void checkTransactionLogMenu() {
		if(isElementDisplayed(this.transactionLog))
			log.info("Transaction Log is displayed under Branch Management button");
		else
		    Assert.fail("Transaction Log is not displayed under Branch Management button");		
	}
	
	public void clickTransactionLogMenu() {
		waitPageToLoad();
		waitInSeconds(3);
		this.transactionLog.click();
	}


	public void checkBPTPHistory() {
		if(isElementDisplayed(this.bpTpBtn))
			log.info("Balance/Trading Period history is displayed under Branch management");
		else
			Assert.fail("Balance/Trading Period history is not displayed under Branch management");		
	}


	public void clickBPTPHistory() {
		this.bpTpBtn.click();
		waitPageToLoad();		
	}

}

