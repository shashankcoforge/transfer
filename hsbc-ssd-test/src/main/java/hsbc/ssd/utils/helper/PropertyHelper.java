package hsbc.ssd.utils.helper;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.testng.Assert;
import hsbc.ssd.utils.Constants;

import java.io.File;
import java.io.FileInputStream;

/**
 * utility class based on Apache Commons extension to java properties file.
 * additional capabilities provided by the Apache util include nested property files
 * and multi occurrences of keys (ie. array lists)
 */
public class PropertyHelper {

    /**
     * gets properties file
     */
    public static PropertiesConfiguration getProperties(String propsPath) {
        PropertiesConfiguration props = new PropertiesConfiguration();
        try {
            File propsFile = new File(propsPath);
            FileInputStream inputStream = new FileInputStream(propsFile);
            props.load(inputStream);
            inputStream.close();
        } catch (Exception e) {
            return null;
        }
        return props;
    }

    /**
     * gets default properties file
     */
    public static PropertiesConfiguration getDefaultProperties() {
        PropertiesConfiguration props = new PropertiesConfiguration();
        try {
            File propsFile = new File(Constants.DEFAULTSETTINGS);
            FileInputStream inputStream = new FileInputStream(propsFile);
            props.load(inputStream);
            inputStream.close();
        } catch (Exception e) {
            return null;
        }
        return props;
    }

    /**
     * gets string data from any properties file on given path
     */
    public static String getProperty(String propsPath, String key) {
        String value = getProperties(propsPath).getString(key);
        return value;
    }

    /**
     * gets string data from any properties file on given path
     */
    public static String getEnvSpecificAppParameters(String key) {
        String environmentSettings = PropertyHelper.getVariable("env") != null ? PropertyHelper.getVariable("env") : PropertyHelper.getDefaultProperty("defaultEnvironment");
        String value = getProperties(Constants.ENVIRONMENTPATH + environmentSettings + ".properties").getString(key);
        if (value == null || value.equals("")) {
            Assert.fail("Either Key " + key + " is not available or value is not set in " + environmentSettings + ".properties file!");
        }
        return value;
    }

    public static String getEnvSpecificAppParameterOrDefault(String key) {
        String environmentSettings = PropertyHelper.getVariable("env") != null ? PropertyHelper.getVariable("env") : PropertyHelper.getDefaultProperty("defaultEnvironment");
        String value = getProperties(Constants.ENVIRONMENTPATH + environmentSettings + ".properties").getString(key);
        return value;
    }

    /**
     * gets string data from any properties file on given path
     */
    public static String getDefaultProperty(String key) {
        String value = getProperties(Constants.DEFAULTSETTINGS).getString(key);
        return value;
    }

    /**
     * gets string array data from any properties file on given path
     */
    public static String[] getPropertyArray(String propsPath, String key) {
        return getProperties(propsPath).getStringArray(key);
    }

    /**
     * gets value for variable based on preference of system property first then environment variable
     */
    public static String getVariable(String propname) {
        String val = System.getProperty(propname, null);
        val = (val == null ? System.getenv(propname) : val);
        return val;
    }

    public static boolean isUserPasswordAvailable() {
        String value = getEnvSpecificAppParameterOrDefault("IsDynamicPasswordAvailable");
        if (value != null && value.equalsIgnoreCase("true")) {
            return true;
        } else {
            return false;
        }
    }
}