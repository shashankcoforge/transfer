
package hsbc.qs.pageObjects.d1;

import hsbc.qs.utils.selenium.PageObjectUtil;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class TPALoginPage extends PageObjectUtil {

    public TPALoginPage() {
    	initialise(this);     
    }
	@FindBy(id="generate") private WebElement generatePwdBtn;
    @FindBy(id="username") private WebElement username;
    @FindBy(id="password") private WebElement password;
    @FindBy(id="submit") private WebElement loginBtn;
	@FindBy(id="dynamicPassword") private WebElement dynamicPassword;

	public String generateDynamicPassword(String userName, String passWord)
	{
		generatePwdBtn.click();
		waitPageToLoad();
		login(userName, passWord);
		return dynamicPassword.getText().trim();
	}
	public void login(String userName, String passWord) {		
		this.username.sendKeys(userName);
   	    this.password.sendKeys(passWord);
   	    this.loginBtn.click();
   	    waitPageToLoad();
	}

}


