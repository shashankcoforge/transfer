package hsbc.qs.modules.backOffice;

import hsbc.qs.utils.api.RestAssuredHelper;
import hsbc.qs.utils.api.RestContext;
import hsbc.qs.utils.aws.AWSBase;
import hsbc.qs.utils.helper.PropertyHelper;
import hsbc.qs.utils.selenium.TestParameters;
import io.restassured.response.Response;

public class ItemDetails {
	public void fetch_balance(String accLocation, RestContext restContext) {
	    String jwtToken = new AWSBase().getJWTToken(PropertyHelper.getDefaultProperty("BackOfficeUserName"), PropertyHelper.getDefaultProperty("BackOfficePassWord"));
	    restContext.getRestData().setContextType("application/json");
	    RestAssuredHelper.setHeader(restContext.getRestData().getRequest(), "Authorization", "Bearer "+jwtToken);
	    System.out.println("request "+restContext.getRestData().getRequest().given().log().everything().toString());
	    Response response = RestAssuredHelper.callAPI(restContext.getRestData().getRequest(), "GET", "/cash-management/cash-details/2314010");
	    System.out.println("********************************response");
	    System.out.println(response.asString());
	    restContext.getRestData().setResponse(response);
	    RestAssuredHelper.checkStatus(restContext.getRestData(), "200");
	    String balance=response.asString().split(accLocation)[1].split("balance")[1].split(",")[0].split(":")[1];
	    TestParameters.getInstance().setTestData("lastBalance", balance);
	    System.out.println(balance);
	    }

}
