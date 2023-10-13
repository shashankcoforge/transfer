package hsbc.ssd.steps;

import com.jayway.jsonpath.DocumentContext;
import hsbc.ssd.steps.bo.BO_RefactoredSteps;
import hsbc.ssd.utils.Constants;
import hsbc.ssd.utils.api.ResponseValidator;
import hsbc.ssd.utils.api.RestAssuredHelper;
import hsbc.ssd.utils.api.RestContext;
import hsbc.ssd.utils.aws.AWSBase;
import hsbc.ssd.utils.helper.ExcelHelper;
import hsbc.ssd.utils.helper.JsonHelper;
import hsbc.ssd.utils.helper.PropertyHelper;
import hsbc.ssd.utils.reporting.Reporter;
import hsbc.ssd.utils.selenium.TestParameters;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.testng.Assert;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import static io.restassured.RestAssured.given;
import static org.testng.Assert.fail;

/**
 * Common Steps class which invokes the RestAssuredHelper class and enables
 * rest api calls to be configured and invoked from cucumber features along
 * with detailed response validation.
 */
public class RestAssuredSteps {
    RestContext restContext;
    RequestSpecification request = given();
    protected Logger log = LogManager.getLogger(this.getClass().getName());

    public RestAssuredSteps(RestContext restContext) {
        this.restContext = restContext;
    }

    JSONObject basedata = null;
    private String api = null;
    private String path = null;
    private String method = null;

    private static boolean apiTest;

    public static boolean isApiTest() {
        return apiTest;
    }

    public static void setApiTest(boolean api) {
        apiTest = api;
    }

    public void resetRest() {
        request = given();
        restContext.getRestData().setRequest(request);
    }

    @Given("^a rest api \"(.*)\"$")
    public void setAPI(String api) {
        TestParameters.getInstance().resetSoftAssert();
        restContext.getRestData().setRequest(request);
        this.api = api;
        String envURI = PropertyHelper.getEnvSpecificAppParameters(api);
        RestAssuredHelper.setBaseURI(restContext.getRestData().getRequest(), envURI);
        System.out.println("envURI " + envURI);
        setApiTest(true);
    }

    @And("^run the token generator and get JWT token for the device \"(.*)\"$")
    public void run_the_token_generator_and_get_jwt_token(String deviceName) throws Throwable {
        String jwtToken = new AWSBase().getJWTToken(PropertyHelper.getEnvSpecificAppParameters(deviceName), PropertyHelper.getEnvSpecificAppParameters("Device_Password"));
        TestParameters.getInstance().setTestData("JWTToken", jwtToken);
        TestParameters.getInstance().setTestData("deviceID", PropertyHelper.getEnvSpecificAppParameters(deviceName));
        TestParameters.getInstance().setTestData("deviceName", deviceName);
    }
    @And("^run the user token generator and get User JWT token for the device \"(.*)\"$")
    public void run_the_user_token_generator_and_get_jwt_token(String deviceName) throws Throwable {
        String jwtToken = new AWSBase().getUserJWTToken(PropertyHelper.getEnvSpecificAppParameters(deviceName), PropertyHelper.getEnvSpecificAppParameters("UserDevice_Password"));
        TestParameters.getInstance().setTestData("UserJWTToken", jwtToken);
        TestParameters.getInstance().setTestData("smartId", PropertyHelper.getEnvSpecificAppParameters(deviceName));
        TestParameters.getInstance().setTestData("userDeviceName", deviceName);
    }

    @And("^reset password for device \"([^\"]*)\" to \"([^\"]*)\"$")
    public void reset_password_for_device_something_to_something(String deviceName, String password) throws Throwable {
        new AWSBase().resetPasswordForDevice(deviceName, password);
    }

    @Then("^fetch the basketID using api$")
    public void fetch_the_basketid_using_api() throws Throwable {
        String deviceName = TestParameters.getInstance().getTestData("deviceID");
        restContext.getRestData().setRequest(request);
        String envURI = PropertyHelper.getEnvSpecificAppParameters("endpoint");
        RestAssuredHelper.setBaseURI(restContext.getRestData().getRequest(), envURI);
        log.info("apiURI " + envURI);
        setApiTest(true);
        String jwtToken = new AWSBase().getJWTToken(PropertyHelper.getEnvSpecificAppParameters(deviceName), PropertyHelper.getEnvSpecificAppParameters("Device_Password"));
        restContext.getRestData().setContextType("application/json");
        RestAssuredHelper.setHeader(restContext.getRestData().getRequest(), "Authorization", "Bearer " + jwtToken);
        System.out.println("request " + restContext.getRestData().getRequest().given().log().everything().toString());
        Response response = RestAssuredHelper.callAPI(restContext.getRestData().getRequest(), "GET", "/branch/" + PropertyHelper.getEnvSpecificAppParameters(deviceName + "_FAD") + "/node/" + PropertyHelper.getEnvSpecificAppParameters(deviceName + "_NODE") + "/lastBasket");
        System.out.println("********************************response");
        System.out.println(response.asString());
        restContext.getRestData().setResponse(response);
        RestAssuredHelper.checkStatus(restContext.getRestData(), "200");
        DocumentContext jsonContext = com.jayway.jsonpath.JsonPath.parse(restContext.getRestData().getResponse().body().asString());
        TestParameters.getInstance().setTestData("basketID", jsonContext.read("$.basket.basketCore.basketID").toString());
        log.info("********************basketID : " + jsonContext.read("$.basket.basketCore.basketID").toString());
        String basketClosedTime = "";
        try {
            basketClosedTime = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date(Long.parseLong(jsonContext.read("$.basket.BasketClosedTime").toString()) * 1000));
        }catch(Exception e){
            basketClosedTime = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date());
        }
        TestParameters.getInstance().setTestData("basketTime", basketClosedTime);
        resetRest();
    }

    @And("^User get the balance details of the \"([^\"]*)\" using api call for \"([^\"]*)\" and \"([^\"]*)\" from \"(.*?)\"$")
    public void fetch_balance(String accLocation, String branchType,String service,String sheetName) throws IOException {
    	//fetch_balance(ExcelHelper.getDataOnlyBySecondaryKey(sheetName, accLocation, service),ExcelHelper.getDataOnlyBySecondaryKey(sheetName, branchType, service));
        new BO_RefactoredSteps().getTheAmountInSafeBeforeUsingSheet(accLocation,ExcelHelper.getDataOnlyBySecondaryKey(sheetName, branchType, service),service,sheetName);
    }

    
    @And("^User get the balance details of the \"([^\"]*)\" using api call for \"([^\"]*)\" branch$")
    public void fetch_balance(String accLocation, String branchType) {
    	//if(accLocation.equals("Safe"))
    		//accLocation="safe";
    restContext.getRestData().setRequest(request);
    String envURI = PropertyHelper.getEnvSpecificAppParameters("baseUrl");
    RestAssuredHelper.setBaseURI(restContext.getRestData().getRequest(), envURI);
    log.info("apiURI "+envURI);
    setApiTest(true);
    String jwtToken = new AWSBase().getJWTToken("forgerock_"+PropertyHelper.getDefaultProperty(branchType+"BackOfficeUserName"), PropertyHelper.getDefaultProperty(branchType+"BackOfficePassWord"));
    restContext.getRestData().setContextType("application/json");
    RestAssuredHelper.setHeader(restContext.getRestData().getRequest(), "Authorization", "Bearer "+jwtToken);
    RestAssuredHelper.setHeader(restContext.getRestData().getRequest(), "fadcode", PropertyHelper.getDefaultProperty(branchType+"fadcode"));
    System.out.println("request "+restContext.getRestData().getRequest().given().log().everything().toString());
    Response response = RestAssuredHelper.callAPI(restContext.getRestData().getRequest(), "GET", "/BboCashManagementService/v1/system-cash/accounting-location");
    System.out.println("********************************response");
    System.out.println(response.asString());
    restContext.getRestData().setResponse(response);
    RestAssuredHelper.checkStatus(restContext.getRestData(), "200");
    String balance=response.asString().split(accLocation)[1].split("systemValue")[1].split(",")[0].split(":")[1];
    TestParameters.getInstance().setTestData(accLocation.replaceAll("\\s","")+"Balance", balance);
    TestParameters.getInstance().setTestData("lastBalance", balance);
    System.out.println(balance);
    resetRest();
    }


    @Given("^(challenged basic|basic) authorisation$")
    public void setBasicAuth(String type) {
        String[] auth = {};
        if (PropertyHelper.getVariable("apiCredentials") == null) {
            auth = PropertyHelper.getEnvSpecificAppParameters(api + "_credentials").split(":");
        } else {
            auth = PropertyHelper.getVariable("apiCredentials").split(":");
        }
        if (type.equalsIgnoreCase("basic")) {
            RestAssuredHelper.setBasicAuth(restContext.getRestData().getRequest(), auth[0], auth[1]);
        } else {
            RestAssuredHelper.setChallengedBasicAuth(restContext.getRestData().getRequest(), auth[0], auth[1]);
        }
    }

    @Given("^a base uri \"(.*)\"$")
    public void setBaseURI(String uri) {
        RestAssuredHelper.setBaseURI(restContext.getRestData().getRequest(), uri);
    }

    @Given("^a base path \"(.*)\"$")
    public void setBasePath(String basepath) {
        RestAssuredHelper.setBasePath(restContext.getRestData().getRequest(), basepath);
    }

    @Given("^a port (\\d+)$")
    public void setPort(int port) {
        RestAssuredHelper.setPort(restContext.getRestData().getRequest(), port);
    }

    @Given("^a header$")
    public void setHeader(Map<String, String> map) {
        System.out.println("*******Setting header******");
            map.forEach((key, val) -> {
                    if (val.contains("<<") && val.contains(">>")) {
                        String[] valu = val.split("<<");
                        assignVar(key, "<<" + valu[1]);
                        System.out.println("*******Key******" + valu[0] + TestParameters.getInstance().getTestData(key));
                        RestAssuredHelper.setHeader(restContext.getRestData().getRequest(), key, valu[0] + TestParameters.getInstance().getTestData(key));
                    } else {
                        String value = patternSearchAndEvaluate(val);
                        RestAssuredHelper.setHeader(restContext.getRestData().getRequest(), key, val);
                        if (key.equalsIgnoreCase("Content-Type")) restContext.getRestData().setContextType(value);
                    }
            });

    }

    @Given("^(form parameters|query parameters|path parameters|parameters)$")
    public void withParams(String type, Map<String, String> map) {
        map.forEach((key, val) -> {
            boolean list = false;
            List<String> vals = new ArrayList<>();
            String v = patternSearchAndEvaluate(val);
            if (v.contains("::")) {
                list = true;
                vals = Arrays.asList(v.split("::"));
            }

            if (!list)
                RestAssuredHelper.setParam(restContext.getRestData().getRequest(), type, key, v);
            else
                RestAssuredHelper.setParamList(restContext.getRestData().getRequest(), key, vals);
        });
    }

    @And("multipart")
    public void multipart(List<Map<String, String>> list) {
        list.forEach(map -> {
            if (map.size() == 1) {
                String filepath = Constants.BASEPATH + "testdata/inputs/" + map.get("file");
                RestAssuredHelper.setMultiPart(restContext.getRestData().getRequest(), new File(filepath));
            } else if (map.size() == 2) {
                String controlName = map.get("controlName");
                if (map.containsKey("file")) {
                    String filepath = Constants.BASEPATH + "testdata/inputs/" + map.get("file");
                    RestAssuredHelper.setMultiPart(restContext.getRestData().getRequest(), controlName, new File(filepath));

                } else if (map.containsKey("contentBody")) {
                    RestAssuredHelper.setMultiPart(restContext.getRestData().getRequest(), controlName, map.get("contentBody"));
                }
            } else if (map.size() == 3) {
                String controlName = map.get("controlName");
                String mimeType = map.get("mimeType");
                if (map.containsKey("file")) {
                    String filepath = Constants.BASEPATH + "testdata/inputs/" + map.get("file");
                    RestAssuredHelper.setMultiPart(restContext.getRestData().getRequest(), controlName, new File(filepath), mimeType);

                } else if (map.containsKey("contentBody")) {
                    RestAssuredHelper.setMultiPart(restContext.getRestData().getRequest(), controlName, map.get("contentBody"), mimeType);
                }
            }
        });
    }


    @Given("^base input data \"([^\"]*)\"$")
    public void setBaseInputData(String arg1) {
        if (arg1.substring(0, 2).equalsIgnoreCase("<<")) {
            String[] str = arg1.replace("<<", "").replace(">>", "").split("\\.");
            String testdatafile = str[0];
            String testdataset = str[1];
            String path = "src/test/resources/testdata/inputs/" + testdatafile + ".json";
            basedata = JsonHelper.getJSONData(path, testdataset);
        } else {
            basedata = new JSONObject(arg1);
        }
    }

    @When("^a request body$")
    @Given("^a request body \"(.*)\"$")
    public void requestBody(String data) throws Exception {
        String body;

        if (!data.substring(0, 2).equalsIgnoreCase("<<")) {
            data = patternSearchAndEvaluate(data);
        }
        if (basedata == null) {
            body = RestAssuredHelper.createJson(data);
        } else {
            body = RestAssuredHelper.createJson(data, basedata);
        }

        RestAssuredHelper.setBody(restContext.getRestData().getRequest(), body);
    }

    @And("^a request body using POJO class and Parameters$")
    public void a_request_body_using_pojo_class_and_parameters(Map<String, String> map) throws Throwable {
        final String[] classNm = {""};

        map.forEach((key, val) -> {
            if (key.toLowerCase().equals("pojoclass")) {
                classNm[0] = "pol.bde.modules.api.pojo." + val.toLowerCase() + "." + val;
            } else {
                TestParameters.getInstance().setTestData(key, val);
            }
        });

        Class<?> payloadClass = Class.forName(classNm[0] + "Payload");
        Class<?> pojoClass = Class.forName(classNm[0] + "POJO");
        Method method = payloadClass.getMethod("getPayload");
        RestAssuredHelper.setBody(restContext.getRestData().getRequest(),
                pojoClass.cast(method.invoke(payloadClass.getDeclaredConstructor().newInstance())));
    }

    @And("^a request body using class and method$")
    public void a_request_body_using_class_and_method(Map<String, String> map) throws Throwable {
        final String[] classNm = {""};
        final String[] methNm = {""};

        map.forEach((key, val) -> {
            if (key.toLowerCase().equals("pojoclass")) {
                classNm[0] = "pol.bde.modules.api.pojo." + val.toLowerCase() + "." + val;
            } else if (key.toLowerCase().equals("methodname")) {
                methNm[0] = val;
            } else {
                TestParameters.getInstance().setTestData(key, val);
            }
        });

        Class<?> payloadClass = Class.forName(classNm[0] + "Payload");
        Class<?> pojoClass = Class.forName(classNm[0] + "POJO");
        Method method = payloadClass.getMethod(methNm[0]);
        RestAssuredHelper.setBody(restContext.getRestData().getRequest(),
                pojoClass.cast(method.invoke(payloadClass.getDeclaredConstructor().newInstance())));
    }

    @Given("^Extract and Save \"(.*)\" from response$")
    public void extractAndSave(String jsonPathFromResponse) {
        String contentType = restContext.getRestData().getResponse().header("Content-Type");
        String responseString = restContext.getRestData().getRespString();
        String value = "";
        if (contentType.contains("application/json")) {
            JsonPath jsonPath = new JsonPath(responseString);
            value = jsonPath.get(jsonPathFromResponse).toString();
        } else if (contentType.contains("text/plain")) {
            value = responseString;
        }
        TestParameters.getInstance().setTestData(jsonPathFromResponse, value);
    }

    @When("^the system requests (GET|PUT|POST|PATCH|DELETE) \"(.*)\"$")
    public void apiGetRequest(String apiMethod, String path) {
        if (path.contains("<<") && path.contains(">>")) {
            String[] splitPath;
            splitPath = path.replaceAll("<<", "").split("/");
            StringBuilder pathBuilder = new StringBuilder();
            for (int i = 0; i < splitPath.length; i++) {
                if (splitPath[i].contains(">>")) {
                    pathBuilder.append(TestParameters.getInstance().getTestData(splitPath[i].replace(">>", "")) + (i == splitPath.length - 1 ? "" : "/"));
                } else {
                    pathBuilder.append(splitPath[i] + (i == splitPath.length - 1 ? "" : "/"));
                }
            }
            path = pathBuilder.toString();
        } else {
            path = patternSearchAndEvaluate(path);
        }
        this.path = path;
        this.method = apiMethod;
        System.out.println("request " + restContext.getRestData().getRequest().given().log().everything().toString());
        Response response = RestAssuredHelper.callAPI(restContext.getRestData().getRequest(), apiMethod, path);
        System.out.println("response::::"+ response.getBody());
        restContext.getRestData().setResponse(response);
        resetRest();        //enables multiple api calls within single scenario
    }


    @Then("^the response code is (\\d+)$")
    public void verify_status_code(int code) throws NumberFormatException {
        RestAssuredHelper.checkStatus(restContext.getRestData(), code);
        TestParameters.getInstance().sa().assertAll();
    }

    @And("^get values from the response$")
    public void get_values_from_the_response(Map<String, String> map) throws Throwable {
        map.forEach((key, val) -> {
            DocumentContext jsonContext = com.jayway.jsonpath.JsonPath.parse(restContext.getRestData().getResponse().body().asString());
            TestParameters.getInstance().setTestData(key, jsonContext.read(val).toString());
        });
    }

    @Then("^the response status is \"(.*)\"$")
    public void verify_status_message(String msg) throws NumberFormatException {
        RestAssuredHelper.checkStatus(restContext.getRestData(), msg);
        TestParameters.getInstance().sa().assertAll();
    }

    @Then("^the response time is less than (\\d+) milliseconds$")
    public void verifyResponseTime(long duration) {
        RestAssuredHelper.checkResponseTime(restContext.getRestData(), duration);
    }

    @Then("^the response header contains$")
    public void verifyHeader(List<List<String>> list) {
        Object val;
        String key;
        String matcher;
        for (List<String> row : list) {
            if (row.size() == 2) {
                matcher = "equals";
                key = row.get(0);
                val = row.get(1);
            } else {
                matcher = row.get(1);
                key = row.get(0);
                val = row.get(2);
            }
            RestAssuredHelper.checkHeader(restContext.getRestData(), key, matcher, val);
        }
    }

    @And("^the response body is empty$")
    public void responseBodyEmpty() {
        if (!(restContext.getRestData().getRespString().equalsIgnoreCase("{}")
                || restContext.getRestData().getRespString().equalsIgnoreCase("[]"))) {
            fail("response body not empty....contains: " + restContext.getRestData().getRespString());
        }
    }

    @And("^the (json|response) body( strictly|) contains$")
    public void responseBodyValid(String type, String mode, DataTable table) throws IOException, JSONException {
        List<List<String>> temp = table.cells();

        String responseString;
        if (type.equalsIgnoreCase("response")) {
            responseString = restContext.getRestData().getRespString();
        } else {
            responseString = TestParameters.getInstance().getTestData("jsonBody").toString();
        }

        if (temp.get(0).size() == 1) {
            String[] filename = temp.get(1).get(0).replace("<<", "").replace(">>", "").split("\\.");
            RestAssuredHelper.responseBodyValid(mode, filename[0], filename[1], responseString);
        } else if (temp.get(0).size() == 2) {
            RestAssuredHelper.responseBodyValid(this.api, this.method, this.path, table.asMap(String.class, String.class), responseString);
        } else {
            RestAssuredHelper.responseContains(table.asList(ResponseValidator.class), responseString);
        }
    }


    @And("^the response matches the (json|xml) schema \"(.*)\"$")
    public void matchJSONSchema(String type, String path) {
        RestAssuredHelper.checkSchema(restContext.getRestData(), type, "testdata/schemas/" + path);
    }


    @And("^trace out request response$")
    public void traceOut() {
        System.out.println("request: " + restContext.getRestData().getRequestString());
        System.out.println("response: " + restContext.getRestData().getRespString());
    }

    @And("^wait (.*) milliseconds for request to process$")
    public void WaitDevice(long time) throws InterruptedException {
        Thread.sleep(time);
    }

    @And("^response body does not contain \"(.*)\"$")
    public void ResponseDoesNotContain(String path) {
        String response = restContext.getRestData().getRespString();
        JSONObject obj = new JSONObject(response);
        Assert.assertTrue(obj.isNull(path));
    }

    @Given("set urlEncodingEnabled to false")
    public void urlEncodingEnabled() {
        restContext.getRestData().getRequest().urlEncodingEnabled(false);
    }


    @And("assign (.*) = (.*)")
    public void assignVar(String var, String val) {
        if (val.contains("<<") && val.contains(">>")) {
            String[] splitVal;
            splitVal = val.split(">>");
            StringBuilder pathBuilder = new StringBuilder();
            for (String s : splitVal) {
                if (s.contains("<<")) {
                    String[] n = s.split("<<");
                    if (n.length > 1) {
                        pathBuilder.append(n[0]).append(TestParameters.getInstance().getTestData(n[1]));
                    } else {
                        pathBuilder.append(TestParameters.getInstance().getTestData(n[0]));
                    }
                } else {
                    pathBuilder.append(s);
                }
            }
            val = pathBuilder.toString();
        }
        String v = patternSearchAndEvaluate(val);
        TestParameters.getInstance().setTestData(var, v.trim());
        log.info(var + " = " + v);
    }

    @And("set from properties file (.*) = (.*)")
    public void assignVar_from_propFile(String var, String val) {
        String v = patternSearchAndEvaluate(val);
        String value = PropertyHelper.getEnvSpecificAppParameters(v);
        TestParameters.getInstance().setTestData(var, value);
        log.info(var + " = " + value);
    }


    private String patternSearchAndEvaluate(String str) {
        String s = "<$";
        String e = "$>";

        int i = str.indexOf(s);

        int j = 1;
        Map<String, Integer> map = new HashMap<>();
        while (i >= 0) {
            map.put("s" + j, i);
            j++;
            i = str.indexOf(s, i + 1);
        }

        i = str.indexOf(e);
        j = 1;
        while (i >= 0) {
            map.put("e" + j, i);
            j++;
            i = str.indexOf(e, i + 1);
        }

        ///checking if proper closure of <$....$> is done.
        if (map.size() % 2 == 1) {
            log.info("Error: Incorrect closure of expression");
        }

        //making sure that we have got the pattern to evaluate
        if ((map.size() % 2 == 0) && (map.size() != 0)) {
            final Map<String, Integer> smap = map.entrySet()
                    .stream()
                    .sorted((Map.Entry.<String, Integer>comparingByValue()))
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));

            Set<String> keys = smap.keySet();

            ArrayList<String> groups = new ArrayList();
            while (!keys.isEmpty()) {
                Object[] x = keys.toArray();

                for (int m = 0; m < x.length - 1; m++) {
                    if (!x[m].toString().substring(0, 1).
                            equalsIgnoreCase(
                                    x[m + 1].toString().substring(0, 1))
                    ) {
                        groups.add(str.substring(map.get(x[m]) + 2, map.get(x[m + 1])));
                        keys.remove(x[m]);
                        keys.remove(x[m + 1]);
                        break;
                    }
                }
            }

            for (int y = 0; y < groups.size(); y++) {
                String exp = groups.get(y);
                String value = stepDataFindReplace2(exp);

                for (int z = 0; z < groups.size(); z++) {
                    String oldExp = groups.get(z);
                    String newExp = oldExp.replace("<$" + exp + "$>", value);
                    str = str.replace("<$" + exp + "$>", value);
                    groups.set(z, newExp);

                }
            }
        }
        return str;
    }

    private String stepDataFindReplace2(String val) {
        boolean changed = false;
        String reportMessage = val;
        String jsonElement;
        String valFromRes = "";
        if (val.startsWith("response.")) {
            jsonElement = val.replace("response.", "");

            String contentType = restContext.getRestData().getResponse().header("Content-Type");
            String responseString = restContext.getRestData().getRespString();

            if (contentType.contains("json")) {
                try {
                    JsonPath jsonPath = new JsonPath(responseString);
                    valFromRes = jsonPath.get(jsonElement).toString();
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                    System.out.println("assigning the entire response");
                    valFromRes = responseString;
                }

            } else if (contentType.contains("text/plain")) {
                valFromRes = responseString;
            }
            changed = true;
        } else {
            if (TestParameters.getInstance().testData().containsKey(val)) {
                valFromRes = TestParameters.getInstance().getTestData(val);
                changed = true;
            } else {
                //get from input json file //TODO
            }
        }

        if (changed) log.info("processed: " + reportMessage + " = " + valFromRes);
        return valFromRes;
    }

    @And("^validate values from the response$")
    public void validate_values_from_the_response(Map<String, String> map) throws Throwable {
        map.forEach((key, val) -> {
            DocumentContext jsonContext = com.jayway.jsonpath.JsonPath.parse(restContext.getRestData().getResponse().body().asString());
            TestParameters.getInstance().sa().assertEquals(val, jsonContext.read(key).toString());
        });
    }

    @Then("^fetch the basketID using api calls for CT$")
    public void fetch_the_basketid_using_api_calls_using_device_something_CT() throws Throwable {
        String deviceName = TestParameters.getInstance().getTestData("deviceID");
        restContext.getRestData().setRequest(request);
        String envURI = PropertyHelper.getEnvSpecificAppParameters("endpoint");
        RestAssuredHelper.setBaseURI(restContext.getRestData().getRequest(), envURI);
        log.info("apiURI " + envURI);
        setApiTest(true);
        String jwtToken = new AWSBase().getJWTToken(PropertyHelper.getEnvSpecificAppParameters(deviceName), PropertyHelper.getEnvSpecificAppParameters("Device_Password"));
        restContext.getRestData().setContextType("application/json");
        RestAssuredHelper.setHeader(restContext.getRestData().getRequest(), "Authorization", "Bearer " + jwtToken);
        System.out.println("request " + restContext.getRestData().getRequest().given().log().everything().toString());
        Response response = RestAssuredHelper.callAPI(restContext.getRestData().getRequest(), "GET", "/branch/" + PropertyHelper.getEnvSpecificAppParameters(deviceName + "_FAD") + "/node/" + PropertyHelper.getEnvSpecificAppParameters(deviceName + "_NODE") + "/lastBasket");
        System.out.println("********************************response" + response);
        System.out.println(response.asString());
        restContext.getRestData().setResponse(response);
        RestAssuredHelper.checkStatus(restContext.getRestData(), "200");
        DocumentContext jsonContext = com.jayway.jsonpath.JsonPath.parse(restContext.getRestData().getResponse().body().asString());
        String basketID = jsonContext.read("$.basket.basketCore.basketID").toString();
        log.info("********************Recently opened basketID : " + basketID);
        String[] basketIDSplitted = basketID.split("-");
        String lastSeqNum = basketIDSplitted[basketIDSplitted.length - 1];
        log.info("********************lastSeqNum :" + lastSeqNum);
        TestParameters.getInstance().setTestData("basketID", basketID.split("-")[0] + "-" + basketID.split("-")[1] + "-" + (Integer.parseInt(lastSeqNum) - 1));
        log.info("********************Required basketID : " + TestParameters.getInstance().getTestData("basketID"));
        TestParameters.getInstance().setTestData("basketTime", new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date()));
        resetRest();
    }

    @And("^validate values from the api response and compare with excel data sheet$")
    public void validate_values_from_the_api_respons_and_compare_with_excel_data_sheet(DataTable dataTable) throws Throwable {
        List<Map<String, String>> dataList = dataTable.asMaps(String.class, String.class);
        for (int i = 0; i < dataList.size(); i++) {
            DocumentContext jsonContext = com.jayway.jsonpath.JsonPath.parse(restContext.getRestData().getResponse().body().asString());
            TestParameters.getInstance().sa().assertEquals(ExcelHelper.getSubScenarioData(dataList.get(i).get("sheetName"), dataList.get(i).get("columnName"), dataList.get(i).get("service")), jsonContext.read(dataList.get(i).get("JsonPath")).toString());
        }
    }

    @And("^API Validations and Reporting$")
    public void API_Validations_and_Reporting(Map<String, String> map) throws Throwable {
        map.forEach((key, val) -> {
            DocumentContext jsonContext = com.jayway.jsonpath.JsonPath.parse(restContext.getRestData().getResponse().body().asString());
            TestParameters.getInstance().setTestData(key, jsonContext.read(val).toString());
            Reporter.addStepLog(" Vales requested from the API :  " + jsonContext.read(val).toString());
        });
    }

    @And("^assert all api validations$")
    public void assert_all_api_validations() throws Throwable {
        TestParameters.getInstance().sa().assertAll();
        TestParameters.getInstance().resetSoftAssert();
        if(TestParameters.getInstance().testData().containsKey("basketID")) {
            TestParameters.getInstance().setTestData("comments",TestParameters.getInstance().getTestData("basketID"));
        }
    }

    @And("^assert all validations$")
    public void assert_all_validations() throws Throwable {
        TestParameters.getInstance().sa().assertAll();
        TestParameters.getInstance().resetSoftAssert();
    }

    @And("^delete the test data \"(.*)\"$")
    public void deleteTestDataParam(String key) {
        TestParameters.getInstance().deleteTestData(key);
    }

    @Then("^check if Last Basket is open for version \"(.*)\"$")
    public void lastBasketIsOpen(String version) throws Throwable {
        String branchId= TestParameters.getInstance().getTestData("branchID");
        String nodeId= TestParameters.getInstance().getTestData("nodeID");
        String envURI = PropertyHelper.getEnvSpecificAppParameters("endpoint");
        RestAssuredHelper.setBaseURI(restContext.getRestData().getRequest(), envURI);
        log.info("apiURI "+envURI);
        setApiTest(true);
        restContext.getRestData().setContextType("application/json");
        RestAssuredHelper.setHeader(restContext.getRestData().getRequest(), "Content-Type", "application/json");
        RestAssuredHelper.setHeader(restContext.getRestData().getRequest(), "Authorization", "Bearer "+TestParameters.getInstance().getTestData("JWTToken"));
        if(null!=TestParameters.getInstance().getTestData("UserJWTToken")) {
            RestAssuredHelper.setHeader(restContext.getRestData().getRequest(), "User", TestParameters.getInstance().getTestData("UserJWTToken"));
        }
        apiGetRequest("GET","/transactions/"+version+"/branch/"+ branchId + "/node/"+nodeId + "/lastBasket");

        Map<String, String> map= new HashMap<>();
        map.put("numOfentries","$.basket.basketCore.NumberOfEntries");
        map.put("basketID","$.basket.basketCore.basketID");
        get_values_from_the_response(map);
        resetRest();
        Map<String, String> closeBasketmap = new HashMap<>();
        closeBasketmap.put("POJOClass", "Basket");
        closeBasketmap.put("basketState", "BKC");
        closeBasketmap.put("basketFulfilmentState", "success");
        a_request_body_using_pojo_class_and_parameters(closeBasketmap);
        RestAssuredHelper.setBaseURI(restContext.getRestData().getRequest(), envURI);
        log.info("apiURI "+envURI);
        setApiTest(true);
        restContext.getRestData().setContextType("application/json");
        RestAssuredHelper.setHeader(restContext.getRestData().getRequest(), "Content-Type", "application/json");
        RestAssuredHelper.setHeader(restContext.getRestData().getRequest(), "Authorization", "Bearer "+TestParameters.getInstance().getTestData("JWTToken"));
        if(null!=TestParameters.getInstance().getTestData("UserJWTToken")) {
            RestAssuredHelper.setHeader(restContext.getRestData().getRequest(), "User", TestParameters.getInstance().getTestData("UserJWTToken"));
        }
        System.out.println("request "+restContext.getRestData().getRequest().given().log().everything().toString());
        Response response = RestAssuredHelper.callAPI(restContext.getRestData().getRequest(), "PUT", "/transactions/"+version+"/basket");
        System.out.println("********************************response"+ response);
        restContext.getRestData().setResponse(response);
        resetRest();
        TestParameters.getInstance().deleteTestData("numOfentries");
    }

}