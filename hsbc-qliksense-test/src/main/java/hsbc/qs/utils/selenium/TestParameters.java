package hsbc.qs.utils.selenium;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.asserts.SoftAssert;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Used to hold the execution context including scenario, properties and platform/browser combo
 * for each execution thread
 */
public class TestParameters {

	private static List<TestParameters> 	threads = new ArrayList<TestParameters>();
	private Map<String,Object> 			testDataClass = null;
	private Map<String,String> 			testData = null;
	private Map<String,Object>			frameworkData = null;
	private long 						threadToEnvID;
	private SoftAssert sa = null;
	private static Logger logger = LogManager.getLogger(TestParameters.class);

	private TestParameters(){}

	private TestParameters(long threadID){
		this.threadToEnvID = threadID;
	}

	/**
	 * Singleton class implementation, holding thread specific TestData objects.
	 * @return the instance of TestParameters class.
	 */
	public static synchronized TestParameters getInstance(){
		long currentRunningThreadID = Thread.currentThread().getId();
		for(TestParameters thread : threads){
			if (thread.threadToEnvID == currentRunningThreadID){
				logger.info("Test Parameter set/get request for the threadID: "+currentRunningThreadID);
				return thread;
			}
		}
		logger.info("Test Parameter set/get request for the threadID: "+currentRunningThreadID);
		TestParameters temp = new TestParameters(currentRunningThreadID);
		threads.add(temp);
		return temp;
	}

	public static synchronized long getThreadID() {
		long currentRunningThreadID = Thread.currentThread().getId();
		logger.info("Request for current threadID: "+currentRunningThreadID);
		return currentRunningThreadID;
	}

	public static synchronized TestParameters setThreadID(long threadID){
		for(TestParameters thread : threads){
			if (thread.threadToEnvID == threadID){
				logger.info("Test Parameter set/get request for a particular threadID: "+threadID);
				return thread;
			}
		}
		logger.info("Test Parameter set/get request for a particular threadID: "+threadID);
		TestParameters temp = new TestParameters(threadID);
		threads.add(temp);
		return temp;
	}

	/**
	 * @return SoftAssert object within the test context for the current thread
	 */
	public SoftAssert sa(){
		if (sa == null)
			sa = new SoftAssert();
		return sa;
	}

	public void resetSoftAssert(){
		sa = null;
	}

	/**
	 * @return Map(String, Object) testdata object for the current thread
	 */
	public Map<String, Object> testDataClass(){
		if (testDataClass ==null )
			testDataClass = new HashMap<String,Object>();
		return testDataClass;
	}

	/**
	 * @return Map(String, Object) testdata string for the current thread
	 */
	public Map<String, String> testData(){
		if (testData ==null )
			testData = new HashMap<String,String>();
		return testData;
	}

	/**
	 * @return value of the key stored in testdata object for the current thread (casting to required object type)
	 */
	public <T> T getTestDataObject(String key, Class<T> type) {
		return testDataToClass(key, type);
	}

	/**
	 * @return value of the key stored in testdata string for the current thread (casting to required object type)
	 */
	public String getTestData(String key) {

		return testData.get(key);
	}

	/**
	 *
	 * @return To pull back test data from the test data for the current thread cast to provided class
	 */
	public <T> T testDataToClass(String key, Class<T> type){
		return type.cast(testDataClass.get(key));
	}

	/**
	 * store the data in the testdata Map object for the current thread. The key-value can be asses by testdataGet(String key) or
	 * complete object by testdata() method
	 */
	public void setTestDataObject(String key, Object data) {
		testDataClass().put(key, data);
	}

	/**
	 * store the data in the testdata string for the current thread. The key-value can be asses by testdataGet(String key) or
	 * complete object by testdata() method
	 */
	public void setTestData(String key, String data) {
		testData().put(key, data);
	}


	/**
	 * @return Map(String, Object) fwSpecificData object for the current thread
	 */
	public Map<String, Object> frameworkData(){
		if (frameworkData ==null )
			frameworkData = new HashMap<String,Object>();
		return frameworkData;
	}

	/**
	 * @return value of the key stored in fwSpecificData object for the current thread (casting to required object type)
	 * - used by framework for test execution
	 */
	public Object getFrameworkData(String key) {
		return frameworkData.get(key);
	}

	/**
	 * store the data in the getFwSpecificData Map object for the current thread. The key-value can be asses by getFwSpecificData(String key)
	 */
	public void putFrameworkData(String key, Object data) {
		frameworkData().put(key, data);
	}

	/**
	 * remove the data in the testdata string for the current thread. The key-value can be asses by testdataGet
	 * @param key
	 * @param data
	 */
	
	public void deleteTestData(String key, String data) {
		testData().remove(key, data);
	}

	public void deleteTestData(String key) {
		if(testData().containsKey(key)) {
			testData().remove(key);
		}
	}

}
