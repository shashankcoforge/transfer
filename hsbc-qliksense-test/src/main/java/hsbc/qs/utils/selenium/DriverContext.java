package hsbc.qs.utils.selenium;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Used to hold the execution context including scenario, properties and platform/browser combo
 * for each execution thread
 */
public class DriverContext {

	private static List<DriverContext> 	threads = new ArrayList<DriverContext>();
	private Map<String, String> driverStack = null;
	private long threadToEnvID;
	private boolean	keepBrowserOpen=false;
	private Logger logger = LogManager.getLogger(DriverContext.class);

	private boolean browserInstanceKilledAtHooks = false;

	private DriverContext(){}

	private DriverContext(long threadID){
		this.threadToEnvID = threadID;
	}

	public static synchronized DriverContext getInstance(){
		long currentRunningThreadID = Thread.currentThread().getId();
		for(DriverContext thread : threads){
			if (thread.threadToEnvID == currentRunningThreadID){
				return thread;
			}
		}
		DriverContext temp = new DriverContext(currentRunningThreadID);
		threads.add(temp);
		return temp;
	}

	public void setDriverContext(Map<String, String> driverStack) {
		setDriverStack(driverStack);
	}

	public Map<String, String> getDriverStack(){
		return this.driverStack;
	}

	public String getBrowserName(){
		if (driverStack == null) {
			return null;
		}else {
			return this.driverStack.get("browserName")==null?this.driverStack.get("browser"):this.driverStack.get("browserName");
		}
	}

	public String getBrowserVersion(){
		if (driverStack == null) {
			return null;
		}else {
			return this.driverStack.get("version")==null?this.driverStack.get("browser_version"):this.driverStack.get("version");
		}
	}

	public String getPlatform(){
		if (driverStack == null) {
			return null;
		}else {
			return this.driverStack.get("platform")==null?this.driverStack.get("os")+"_"+this.driverStack.get("os_version"):this.driverStack.get("platform");
		}
	}

	public void setDriverStack(Map<String, String> driverStack){
		this.driverStack = driverStack;
	}

	public Boolean getKeepBrowserOpen(){
		return this.keepBrowserOpen;
	}

	public void setKeepBrowserOpen(Boolean val){
		this.keepBrowserOpen=val;
	}

	public Boolean isBrowserInstanceKilledAtHooks(){
		return this.browserInstanceKilledAtHooks;
	}

	public void setBrowserInstanceKilledAtHooks(Boolean val){
		this.browserInstanceKilledAtHooks=val;
	}

}
