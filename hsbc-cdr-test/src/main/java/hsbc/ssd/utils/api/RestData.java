package hsbc.ssd.utils.api;

import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import org.json.JSONObject;

/**
 * Holds the data for the current rest api call.
 */
public class RestData {

	private RequestSpecification request=null;
	private JSONObject requestJSON = null;
	private String requestString = null;
	
	private Response response = null;
	private ValidatableResponse respValidator = null;
	private JsonPath respJsonPath = null;
	private String respString = null;


	private String contextType = null;

	/**
	 * @return RequestSpecification instance for the current api call.
	 */
	public RequestSpecification getRequest(){
		return request;
	}

	/**
	 * @return Response reference for the current api call.
	 */
	public Response getResponse(){
		return response;
	}

	/**
	 * @return ValidatableResponse reference for the current api call.
	 */
	public ValidatableResponse getRespValidator(){
		return respValidator;
	}

	/**
	 * @return JsonPath instance of the current api response.
	 */
	public JsonPath getRespJsonPath(){
		return respJsonPath;
	}

	/**
	 * @return String instance of the current api response.
	 */
	public String getRespString(){
		return respString;
	}

	/**
	 * @return JSONObject instance of the current api request body.
	 */
	public JSONObject getRequestJSON() {
		return requestJSON;
	}

	/**
	 * @return String instance of the current api request body.
	 */
	public String getRequestString() {
		return requestString;
	}

	/**
	 * Sets the request reference with the supplied request.
	 * @param request RequestSpecification instance to set.
	 */
	public void setRequest(RequestSpecification request){
		this.request = request;
	}

	/**
	 * Sets the response with the supplied response.
	 * @param response Response instance to set.
	 */
	public void setResponse(Response response) {
		this.response = response;
		setRespValidator(response);
		setRespJsonPath(response);
		setRespString(response);
	}

	/**
	 * Sets respValidator with the supplied response.
	 * @param response Response instance to use.
	 */
	public void setRespValidator(Response response) {
		this.respValidator = response.then();
	}

	/**
	 * Sets respJsonPath with the supplied response.
	 * @param response Response instance to use.
	 */
	public void setRespJsonPath(Response response) {	
		this.respJsonPath = new JsonPath(response.asString());
	}

	/**
	 * Sets respString with the supplied response.
	 * @param response Response instance to use.
	 */
	public void setRespString(Response response) {
		this.respString = response.asString();
	}

	/**
	 * Sets requestJSON with supplied requestJSON.
	 * @param requestJSON input request body
	 */
	public void setRequestJSON(JSONObject requestJSON) {
		this.requestJSON = requestJSON;
	}

	/**
	 * Sets requestString with supplied requestString.
	 * @param requestString input request body
	 */
	public void setRequestString(String requestString) {
		this.requestString = requestString;
	}

	public String getContextType() {
		return contextType;
	}

	public void setContextType(String contextType) {
		this.contextType = contextType;
	}


}
