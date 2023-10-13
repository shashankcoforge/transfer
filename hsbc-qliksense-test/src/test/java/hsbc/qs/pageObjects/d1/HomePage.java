package hsbc.qs.pageObjects.d1;

import hsbc.qs.utils.selenium.PageObjectUtil;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class HomePage extends PageObjectUtil {
	
	/**
	 * This has the methods and elements which are in menu
	 */

    public HomePage() {initialise(this);}

    @FindBy(xpath="//h2[id='datetime']") private WebElement dateTime;
  
    public String getDateTimeVisible(){
    	waitPageToLoad();
		waitUntilElementVisible(dateTime);
		return dateTime.getText();
    }

}

