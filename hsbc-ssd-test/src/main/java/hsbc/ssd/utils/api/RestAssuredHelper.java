package hsbc.ssd.utils.api;

import com.google.gson.Gson;
import hsbc.ssd.utils.helper.JsonHelper;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.hamcrest.core.IsEqual;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import hsbc.ssd.utils.Constants;
import hsbc.ssd.utils.selenium.TestParameters;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static io.restassured.matcher.RestAssuredMatchers.matchesXsd;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * Helper class with wrapper methods to leverage the Rest Assured library.
 * Allows rest api calls to be configured and invoked followed by response validation.
 */
public class RestAssuredHelper {

    public static RequestSpecification setBasicAuth(RequestSpecification request, String uname, String pword) {
        return request.auth().preemptive().basic(uname, pword);
    }

    public static RequestSpecification setChallengedBasicAuth(RequestSpecification request, String uname, String pword) {
        return request.auth().basic(uname, pword);
    }

    public static RequestSpecification setBaseURI(RequestSpecification request, String uri) {
        return request.baseUri(uri);
    }

    public static RequestSpecification setBasePath(RequestSpecification request, String basepath) {
        return request.basePath(basepath);
    }

    public static RequestSpecification setPort(RequestSpecification request, int port) {
        return request.port(port);
    }

    public static RequestSpecification setHeader(RequestSpecification request, String key, String val) {
        return request.header(key, val);
    }

    public static RequestSpecification setParam(RequestSpecification request, String type, String key, String val) {
        switch (type) {
            case "parameters":
                request.param(key, val);
                break;
            case "form parameters":
                request.formParam(key, val);
                break;
            case "query parameters":
                request.queryParam(key, val);
                break;
            case "path parameters":
                request.pathParam(key, val);
                break;
        }

        return request;
    }

    public static RequestSpecification setMultiPart(RequestSpecification request, File file){
        request.multiPart(file);
        return request;
    }

    public static RequestSpecification setMultiPart(RequestSpecification request,String controlName, File file){
        request.multiPart(controlName, file);
        return request;
    }
    public static RequestSpecification setMultiPart(RequestSpecification request,String controlName, String contentBody){
        request.multiPart(controlName, contentBody);
        return request;
    }

    public static RequestSpecification setMultiPart(RequestSpecification request,String controlName, File file, String mimeType){
        request.multiPart(controlName, file, mimeType);
        return request;
    }
    public static RequestSpecification setMultiPart(RequestSpecification request,String controlName, String contentBody, String mimeType){
        request.multiPart(controlName, contentBody, mimeType);
        return request;
    }


    public static RequestSpecification setParamList(RequestSpecification request, String key, List<String> val) {
        return request.param(key, val);
    }

    public static RequestSpecification setBody(RequestSpecification request, String content) {
        return request.body(content);
    }

    public static RequestSpecification setBody(RequestSpecification request, Object content) {
        return request.body(content);
    }

    public static Response callAPI(RequestSpecification request, String method, String path) {
        Response response = null;
        switch (method) {
            case "GET":
                response = request.get(path);
                break;
            case "PUT":
                response = request.put(path);
                break;
            case "POST":
                response = request.post(path);
                break;
            case "PATCH":
                response = request.patch(path);
                break;
            case "DELETE":
                response = request.delete(path);
                break;
        }

        return response;
    }

    public static void checkStatus(RestData restData, int statusCode) {
        restData.getRespValidator().assertThat().statusCode(statusCode);
    }

    public static void checkStatus(RestData restData, String msg) {
        restData.getRespValidator().assertThat().statusLine(containsString(msg));
    }

    public static void checkSchema(RestData restData, String type, String path) {
        switch (type) {
            case "json":
                restData.getRespValidator().body(matchesJsonSchemaInClasspath(path));
                break;
            case "xml":
                restData.getRespValidator().body(matchesXsd(path));
                break;
        }

    }

    public static void checkResponseTime(RestData restData, long duration) {
        restData.getRespValidator().assertThat().time(lessThan(duration));
    }

    public static void checkHeader(RestData restData, String key, String matcher, Object val) {
        String act = restData.getResponse().header(key);

        switch (matcher) {
            case "equals":
                assertThat(act, equalTo(val));
                break;
            case "regex":
                assertThat("value " + act.toString() + " does not match regex " + val.toString(), act.toString().matches(val.toString()), is(true));
                break;
            case "isNull":
                assertThat(act, nullValue());
                break;
            case "!isNull":
                assertThat(act, not(nullValue()));
                break;
        }
    }

    public static void checkBody(JsonPath jsonPath, Object[] obj, String element, String matcher) {
        Object act = jsonPath.get(element);//from(respString).get(element);
        Object nullObj = null;
        List<Object> list = null;

        switch (matcher) {
            case "is":
                assertThat(act, is(obj[0]));
                break;
            case "equals":
                assertThat(act, equalTo(obj[0]));
                break;
            case "hasItem":
                list = jsonPath.get(element);
                assertThat(list, hasItem(obj[0]));
                break;
            case "hasItems":
                list = jsonPath.get(element);
                assertThat(list, hasItems(obj));
                break;
            case "contains":
                list = jsonPath.get(element);
                assertThat(list, contains(obj));
                break;
            case "containsAnyOrder":
                list = jsonPath.get(element);
                assertThat(list, containsInAnyOrder(obj));
                break;
            case "hasSize":
                if (act instanceof List) {
                    list = jsonPath.get(element);
                    assertThat(list, hasSize((Integer) obj[0]));
                } else {
                    assertThat(new JSONObject(new Gson().toJson(act)).length(), IsEqual.equalTo(obj[0]));
                }
                break;
            case "isNull":
                if (act instanceof List) {
                    list = jsonPath.get(element);
                    assertThat(list, contains(nullObj));
                } else {
                    assertThat(act, nullValue());
                }
                break;
            case "isEmpty":
                if (act instanceof List) {
                    list = jsonPath.get(element);
                    assertThat(list, is(empty()));
                } else {
                    assertThat(act.toString().trim(), equalTo("{}"));
                }
                break;
            case "startsWith":
                assertThat(act.toString(), startsWith(obj[0].toString()));
                break;
            case "endsWith":
                assertThat(act.toString(), endsWith(obj[0].toString()));
                break;
            case "containsString":
                assertThat(act.toString(), containsString(obj[0].toString()));
                break;    //use when working on single element
            case "containsStringArray":
                assertThat(act.toString(), containsString(obj[0].toString()));
                break;    //use when working on string array
            case "regex":
                assertThat("value " + act.toString() + " does not match regex " + obj[0].toString(), act.toString().matches(obj[0].toString()), is(true));
                break;


            case "!is":
                assertThat(act, not(is(obj[0])));
                break;
            case "!equals":
                if (act instanceof List) {
                    list = jsonPath.get(element);
                    assertThat(list, not(equalTo(obj)));
                } else {
                    assertThat(act, not(equalTo(obj[0])));
                }
                break;
            case "!hasItem":
                list = jsonPath.get(element);
                assertThat(list, not(hasItem(obj[0])));
                break;
            case "!hasItems":
                list = jsonPath.get(element);
                assertThat(list, not(hasItems(obj)));
                break;
            case "!contains":
                list = jsonPath.get(element);
                assertThat(list, not(contains(obj)));
                break;
            case "!containsAnyOrder":
                list = jsonPath.get(element);
                assertThat(list, not(containsInAnyOrder(obj)));
                break;
            case "!hasSize":
                if (act instanceof List) {
                    list = jsonPath.get(element);
                    assertThat(list, not(hasSize((Integer) obj[0])));
                } else {
                    assertThat(new JSONObject(new Gson().toJson(act)).length(), not(IsEqual.equalTo(obj[0])));
                }
                break;
            case "!isNull":
                if (act instanceof List) {
                    list = jsonPath.get(element);
                    assertThat(list, not(contains(nullObj)));
                } else {
                    assertThat(act, not(nullValue()));
                }
                break;
            case "!isEmpty":
                if (act instanceof List) {
                    list = jsonPath.get(element);
                    assertThat(list, not(is(empty())));
                } else {
                    assertThat(act.toString().trim(), not(equalTo("{}")));
                }
                break;
            case "!startsWith":
                assertThat(act.toString(), not(startsWith(obj[0].toString())));
                break;
            case "!endsWith":
                assertThat(act.toString(), not(endsWith(obj[0].toString())));
                break;
            case "!containsString":
                assertThat(act.toString(), not(containsString(obj[0].toString())));
                break;    //use when working on single element
            case "!containsStringArray":
                assertThat(act.toString(), not(containsString(obj[0].toString())));
                break;    //use when working on string array
            case "!regex":
                assertThat("value " + act.toString() + " should not match regex " + obj[0].toString(), act.toString().matches(obj[0].toString()), is(false));
                break;
        }
    }


    public static void responseBodyValid(List<ResponseValidator> responseValidator, String responseString) throws IOException, JSONException {
        responseContains(responseValidator, responseString);
    }

    public static void responseBodyValid(String api, String method, String path, Map<String,String> map, String responseString) throws IOException, JSONException {

        JSONObject obj = JsonHelper.getJSONData(Constants.APISTRUCTUREPATH + api + ".json");
        JSONObject json = obj.has(method + " " + path) ? obj.getJSONObject(method + " " + path)
                : obj.has(method) ? obj.getJSONObject(method)
                : obj.has(path) ? obj.getJSONObject(path)
                : obj;

        List<ResponseValidator> list = new ArrayList<ResponseValidator>();
        map.forEach((k, v) -> {
            ResponseValidator rv = new ResponseValidator();
            try {
                rv.setElement(((JSONObject) json.get(k)).getString("element"));
                rv.setMatcher(((JSONObject) json.get(k)).getString("matcher"));
                rv.setType(((JSONObject) json.get(k)).getString("type"));
                rv.setValue(v);
                list.add(rv);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        });
        responseContains(list, responseString);
    }

    public static void responseBodyValid(String mode, String filename, String dataset, String responseString) throws IOException, JSONException {

        JSONCompareMode compareMode = (mode.equals("strictly") ? JSONCompareMode.STRICT : JSONCompareMode.LENIENT);

        Map<String, String> map;
        JSONObject obj = JsonHelper.getJSONData("src/test/resources/testdata/outputs/" + filename + ".json");
        //JSONArray or JSONObject casting to preserve data ordering
        if (responseString.startsWith("[")) {
            JSONArray responseArray = null;
            responseArray = new JSONArray(responseString);
            //assertEquals(responseArray.toString(), obj.get(filename[1]).toString(),"Expected response body differs from actual");
            JSONAssert.assertEquals("Expected response body differs from actual", (JSONArray) obj.get(dataset), responseArray, compareMode);
        } else {
            JSONObject responseJsonObject = null;
            responseJsonObject = new JSONObject(responseString);
            //assertEquals(responseJsonObject.toString(), obj.get(filename[1]).toString(),"Expected response body differs from actual");
            JSONAssert.assertEquals("Expected response body differs from actual", (JSONObject) obj.get(dataset), responseJsonObject, compareMode);
        }
    }

    public static void responseContains(List<ResponseValidator> table, String responseString) {

        table.forEach((data) -> {
            ArrayList<Object> exp = new ArrayList<Object>();
            String[] str = null;
            if (!data.getMatcher().equalsIgnoreCase("regex") && data.getValue().length() > 1 && data.getValue().substring(0, 1).equalsIgnoreCase("[")) {
                str = data.getValue().substring(1, data.getValue().length() - 1).split(",");
            } else {
                str = new String[1];
                str[0] = data.getValue();
            }

            for (String s : str) {
                if (data.getType().equals("int")) {
                    exp.add(Integer.parseInt(s));
                } else if (data.getType().equals("num")) {
                    exp.add(Float.parseFloat(s));
                } else if (data.getType().equals("boolean")) {
                    exp.add(Boolean.parseBoolean(s));
                } else {
                    exp.add(s);
                }
            }

            Object[] obj = exp.toArray(new Object[exp.size()]);
            JsonPath jsonPath = new JsonPath(responseString);
            try {
                checkBody(jsonPath, obj, data.getElement(), data.getMatcher());
            } catch (AssertionError e) {
                TestParameters.getInstance().sa().assertNull(e.getMessage());
            }
        });
        TestParameters.getInstance().sa().assertAll();


    }

    public static String createJson(String testdata, JSONObject...basedata) throws FileNotFoundException, JSONException {
        String jsonData = null;
        JSONObject jsonTest = null;
        JSONObject jsonBase = null;
        FileReader reader;
        JSONTokener token;

        if (testdata.substring(0, 2).equalsIgnoreCase("<<")) {
            String[] str = testdata.replace("<<", "").replace(">>", "").split("\\.");
            String testdatafile = str[0].substring(0, str[0].length());
            String testdataset = str[1].substring(0, str[1].length());
            String path = "src/test/resources/testdata/inputs/" + testdatafile + ".json";
            jsonTest = JsonHelper.getJSONData(path, testdataset);
        } else {
            jsonTest = new JSONObject(testdata);
        }

        if (basedata.length >0 ){
            jsonTest = JsonHelper.jsonMerge(jsonTest, basedata[0]);
        }

        jsonData = jsonTest.toString();

        return jsonData;
    }
}