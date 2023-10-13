package hsbc.ssd.utils.api;

/**
 * POJO to hold the current rest api call context data
 */
public class RestContext {
	
	private RestData rest;
	
	public RestData getRestData(){
		if (rest == null){
			rest = new RestData();
		}
		return rest;
	}
	
	public void setRestData(RestData rest){
		this.rest = rest;
	}
		

}
