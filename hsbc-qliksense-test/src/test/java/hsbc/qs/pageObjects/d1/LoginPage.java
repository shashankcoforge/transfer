
package hsbc.qs.pageObjects.d1;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import hsbc.qs.utils.selenium.PageObjectUtil;

public class LoginPage extends PageObjectUtil {

    public LoginPage() {
    	initialise(this);     
    }
    @FindBy(id="username") private WebElement username;
    @FindBy(id="password") private WebElement password;
    @FindBy(id="submit") private WebElement loginBtn;

	public void login(String userName, String passWord) {		
		this.username.sendKeys(userName);
   	    this.password.sendKeys(passWord);
   	    this.loginBtn.click();
   	    waitPageToLoad();
	}

}


