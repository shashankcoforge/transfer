package hsbc.ssd.utils.zephyr;

import hsbc.ssd.utils.reporting.Reporter;
import hsbc.ssd.utils.zephyr.ZephyrPOJO.TestCyclePOJO;
import hsbc.ssd.utils.zephyr.ZephyrPOJO.TestExecutionPOJO;
import hsbc.ssd.utils.zephyr.ZephyrPOJO.TestScriptResults;
import io.cucumber.core.api.Scenario;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import hsbc.ssd.utils.Constants;
import hsbc.ssd.utils.api.RestAssuredHelper;
import hsbc.ssd.utils.api.RestContext;
import hsbc.ssd.utils.helper.PropertyHelper;
import hsbc.ssd.utils.selenium.TestParameters;

import java.text.SimpleDateFormat;
import java.util.*;

import static io.restassured.RestAssured.given;
import static io.restassured.config.EncoderConfig.encoderConfig;

public class ZephyrScale {
    RestContext restContext;
    RequestSpecification request = given();
    protected Logger log = LogManager.getLogger(this.getClass().getName());
    String zephyrAuthorizationKey = "";
    String zephyrProjectKey = "";
    String zephyrBaseURI = "";
    String testCycleFolderName = "";

    public ZephyrScale() {
        this.restContext = new RestContext();
        zephyrAuthorizationKey = (PropertyHelper.getVariable("zephyrAuthorizationKey") != null && !PropertyHelper.getVariable("zephyrAuthorizationKey").equals(""))
                ? PropertyHelper.getVariable("zephyrAuthorizationKey") : PropertyHelper.getDefaultProperty("zephyrAuthorizationKey");
        zephyrProjectKey = (PropertyHelper.getVariable("zephyrProjectKey") != null && !PropertyHelper.getVariable("zephyrProjectKey").equals(""))
                ? PropertyHelper.getVariable("zephyrProjectKey") : PropertyHelper.getDefaultProperty("zephyrProjectKey");
        zephyrBaseURI = (PropertyHelper.getVariable("zephyrBaseURI") != null && !PropertyHelper.getVariable("zephyrBaseURI").equals(""))
                ? PropertyHelper.getVariable("zephyrBaseURI") : PropertyHelper.getDefaultProperty("zephyrBaseURI");
        testCycleFolderName = (PropertyHelper.getVariable("testCycleFolderName") != null && !PropertyHelper.getVariable("testCycleFolderName").equals(""))
                ? PropertyHelper.getVariable("testCycleFolderName") : PropertyHelper.getDefaultProperty("testCycleFolderName");
    }

    public void executeScenario(Scenario scenario, String screenShotPath){
        String zephyrFolderId = (PropertyHelper.getVariable("zephyrFolderId") != null && !PropertyHelper.getVariable("zephyrFolderId").equals(""))
                ? PropertyHelper.getVariable("zephyrFolderId") : PropertyHelper.getDefaultProperty("zephyrFolderId");
        String zephyrCycleID = (PropertyHelper.getVariable("zephyrCycleID") != null && !PropertyHelper.getVariable("zephyrCycleID").equals(""))
                ? PropertyHelper.getVariable("zephyrCycleID") : PropertyHelper.getDefaultProperty("zephyrCycleID");

        if(zephyrFolderId != null && !zephyrFolderId.equals("")){
            ZephyrParams.setZephyrCycleFolderID(zephyrFolderId);
        }

        if(zephyrCycleID != null && !zephyrCycleID.equals("")){
            ZephyrParams.setZephyrCycleID(zephyrCycleID);
        }

        String[] testCaseID = {""};
        try {
            scenario.getSourceTagNames().forEach(tag -> {
                if (tag.contains("TestCaseID=")) {
                    String testCsId = tag.replaceAll("@", "").replace("TestCaseID=", "");
                    if(testCsId.split("-")[0].equals(zephyrProjectKey)) {
                        testCaseID[0] = testCsId;
                    }
                }
            });
               testCaseID[0] = testCaseID[0].contains(" ") ? testCaseID[0].split("\\s+")[0].split(",")[0].split("]")[0] : testCaseID[0].split(",")[0].split("]")[0];
            log.info("testCaseID: "+ testCaseID[0]);
        }catch(Exception e){
            Reporter.addStepLog("Unable to fetch test case ID from the scenario tag! "+ scenario.getSourceTagNames().toString());
            log.info("Unable to fetch test case ID from the scenario tag! "+ scenario.getSourceTagNames().toString());
        }

        String enableZephyrExecution = (PropertyHelper.getVariable("enableZephyrExecution") != null && !PropertyHelper.getVariable("enableZephyrExecution").equals(""))
                ? PropertyHelper.getVariable("enableZephyrExecution") : PropertyHelper.getDefaultProperty("enableZephyrExecution");
        if(enableZephyrExecution.equalsIgnoreCase("true")){
            if(testCaseID[0].split("-")[0].equals(zephyrProjectKey)) {
                if(ZephyrParams.getZephyrCycleID() == null || ZephyrParams.getZephyrCycleID().equalsIgnoreCase("")) {
                    log.info("cycleID not available in system prop");
                    Random rand = new Random();
                    try {
                        int wait = rand.nextInt((15 - 1) + 1) + 1;
                        log.info("wait1: " + (15 + wait));
                        Thread.sleep(1000 * (wait));
                    } catch (Exception ignored) {
                    }

                    if (ZephyrParams.isCreatingTestCycle()) {
                        try {
                            int wait = rand.nextInt((10 - 1) + 1) + 1;
                            log.info("wait2: " + (15 + wait));
                            Thread.sleep(1000 * 15 + (wait));
                        } catch (Exception ignored) {
                        }
                    }
                    if (ZephyrParams.getZephyrCycleID() == null || ZephyrParams.getZephyrCycleID().equalsIgnoreCase("")) {
                        ZephyrParams.setCreatingTestCycle(true);
                        new ZephyrScale().createTestCycle();
                        ZephyrParams.setCreatingTestCycle(false);
                    }
                }
                executeTestCase(testCaseID[0], scenario.isFailed() ? "Fail" : "Pass");
            }else{
                Reporter.addStepLog("Project ID "+zephyrProjectKey +" is not matching with Test Case ID : "+testCaseID[0]);
            }
        }
    }

    public void setHeader(){
        RestAssuredHelper.setHeader(restContext.getRestData().getRequest(), "Content-Type", "application/json");
        RestAssuredHelper.setHeader(restContext.getRestData().getRequest(), "Authorization", zephyrAuthorizationKey);
    }

    public void getFolderID(){
        try {
            restContext.getRestData().setRequest(given());
            RestAssuredHelper.setBaseURI(restContext.getRestData().getRequest(), zephyrBaseURI);
            setHeader();
            log.info("request " + restContext.getRestData().getRequest().given().log().everything().toString());
            Response response = RestAssuredHelper.callAPI(restContext.getRestData().getRequest(), "GET", zephyrBaseURI+"/folders?projectKey="+zephyrProjectKey+"&folderType=TEST_CYCLE&maxResults=10000");
            restContext.getRestData().setResponse(response);
            log.info("folder create response code is >>" + response.getStatusCode());
            log.info("folder create Body IS>>" + response.getBody().asString());
            String folder = response.getBody().asString().split(testCycleFolderName)[0];
            log.info("List after first split is>>" + folder);
            List<String> Id = Arrays.asList(folder.split("id"));
            String folderId = Id.get(Id.size() - 1).split(",")[0].split(":")[1];
            log.info("Folder ID List is>>" + folderId);
            ZephyrParams.setZephyrCycleFolderID(folderId);
            Reporter.addStepLog("Zephyr Test Cycle Folder Name: "+testCycleFolderName);
        }catch(Exception e){
            Reporter.addStepLog("Unable to fetch the folder ID from Zephyr Scale with folder name : "+testCycleFolderName);
            e.printStackTrace();
        }
    }

    public void createTestCycle(){
        try {
            restContext.getRestData().setRequest(given());
            RestAssuredHelper.setBaseURI(restContext.getRestData().getRequest(), zephyrBaseURI);
            setHeader();
            TestCyclePOJO body = new TestCyclePOJO();
            body.setProjectKey(zephyrProjectKey);
            String env = PropertyHelper.getVariable("env") != null ? PropertyHelper.getVariable("env") : PropertyHelper.getDefaultProperty("defaultEnvironment");
            body.setName(env.toUpperCase()+"-"+Constants.DEFAULTTIMESTAMP);
            if(ZephyrParams.getZephyrCycleFolderID() == null) {
                getFolderID();
            }else{
                if(ZephyrParams.getZephyrCycleFolderID().equalsIgnoreCase("")){
                    getFolderID();
                }
            }
            body.setFolderId(Integer.parseInt(ZephyrParams.getZephyrCycleFolderID()));
            RestAssuredHelper.setBody(restContext.getRestData().getRequest(), body);
            log.info("request " + restContext.getRestData().getRequest().given().log().everything().toString());
            Response response = RestAssuredHelper.callAPI(restContext.getRestData().getRequest(), "POST", zephyrBaseURI+"/testcycles");
            restContext.getRestData().setResponse(response);
            log.info("Create Test Cycle Body IS>>" + response.getBody().asString());
            log.info("Create Test Cycle status code IS>>" + response.getStatusCode());
            String cycleID = response.getBody().asString().split("\"key\":")[1];
            cycleID = cycleID.contains("\\s+")?cycleID.split(":")[1]:cycleID.substring(1,cycleID.length()-2);
            log.info("Test Cycle ID is>>" + cycleID);
            ZephyrParams.setZephyrCycleID(cycleID);
            log.info("cycleID from system prop "+ ZephyrParams.getZephyrCycleID());
        }catch(Exception e){
            Reporter.addStepLog("Unable to create a Test Cycle in Zephyr Scale");
            e.printStackTrace();
        }
    }

    public String executeTestCase(String testCaseID, String testCaseStatus) {
        String testResultID = "";
        String responseString = "";
        try {
            restContext.getRestData().setRequest(given());
            RestAssuredHelper.setBaseURI(restContext.getRestData().getRequest(), zephyrBaseURI);
            setHeader();

            TestExecutionPOJO body = new TestExecutionPOJO();
            body.setProjectKey(zephyrProjectKey);
            body.setTestCaseKey(testCaseID);
            body.setTestCycleKey(ZephyrParams.getZephyrCycleID());
            body.setExecStatus(testCaseStatus);
            body.setEnvironmentName("SPM-"+(PropertyHelper.getVariable("env") != null ? PropertyHelper.getVariable("env").toUpperCase()
                    : PropertyHelper.getDefaultProperty("defaultEnvironment").toUpperCase()));
            TestScriptResults testScriptResults = new TestScriptResults();
            String comments =  TestParameters.getInstance().testData().containsKey("comments") ? TestParameters.getInstance().getTestData("comments") : "";
            testScriptResults.setActualResult(testCaseStatus.equalsIgnoreCase("Pass")? "Worked as expected | "+comments : Reporter.getErrorMsg());
            body.setComment(comments);
            testScriptResults.setActualEndDate(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.ENGLISH).format(new Date()));
            testScriptResults.setStatusName(testCaseStatus);
            body.setTestScriptResults(new TestScriptResults[]{testScriptResults});
            RestAssuredHelper.setBody(restContext.getRestData().getRequest(), body);
            log.info("request " + restContext.getRestData().getRequest().given().log().everything().toString());
            Response response = RestAssuredHelper.callAPI(restContext.getRestData().getRequest(), "POST", zephyrBaseURI+"/testexecutions");
            restContext.getRestData().setResponse(response);
            responseString = response.asString();
            log.info("request Body IS>>" + response.asString());
            Reporter.addStepLog("Zephyr Test Cycle Folder ID: "+ZephyrParams.getZephyrCycleFolderID());
            Reporter.addStepLog("Zephyr Test Cycle ID: "+ZephyrParams.getZephyrCycleID());
            Reporter.addStepLog("Zephyr Test Case ID: "+testCaseID);
        }catch(Exception e){
            Reporter.addStepLog("Unable to execute test case with ID "+testCaseID+" in Zephyr Scale");
            e.printStackTrace();
        }
        try{
            testResultID = responseString.split("\"id\":")[1].split(",")[0];
        }catch(Exception e){}

        return testResultID;
    }

    public void addAttachment(String testCaseID, String testResultID, String screenShotPath){
        try {
            restContext.getRestData().setRequest(given().config(RestAssured.config().encoderConfig(encoderConfig().encodeContentTypeAs("multipart/form-data", ContentType.TEXT))));

            RestAssuredHelper.setBaseURI(restContext.getRestData().getRequest(), "https://api.zephyrscale.smartbear.com/v1");
            RestAssuredHelper.setHeader(restContext.getRestData().getRequest(), "Content-Type", "multipart/form-data");
            RestAssuredHelper.setHeader(restContext.getRestData().getRequest(), "Authorization", zephyrAuthorizationKey);
            RestAssuredHelper.setParam(restContext.getRestData().getRequest(),"form parameters", "file", "@"+screenShotPath);
            log.info("request " + restContext.getRestData().getRequest().given().log().everything().toString());
            Response response = RestAssuredHelper.callAPI(restContext.getRestData().getRequest(), "POST", zephyrBaseURI+"/testresult/"+testResultID+"/attachments");
            restContext.getRestData().setResponse(response);
            log.info("request Body IS>>" + response.asString());

        }catch(Exception e){
            Reporter.addStepLog("Unable to add attachment to test case "+testCaseID+" in Zephyr Scale. TestResultID : "+testResultID);
            e.printStackTrace();
        }
    }


}
