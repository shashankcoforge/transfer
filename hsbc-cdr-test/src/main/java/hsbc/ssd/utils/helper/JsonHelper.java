package hsbc.ssd.utils.helper;

import com.google.gson.Gson;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.*;
import java.util.*;

public class JsonHelper {

    static Logger log = LogManager.getLogger(JsonHelper.class);

    /**
     * Reads JSON file and returns a specific json object from that file based on the root element value
     */
    public static JSONObject getJSONData(String filepath, String...key) {
        try {
            FileReader reader = new FileReader(filepath);
            JSONTokener token = new JSONTokener(reader);
            JSONObject json = (JSONObject) (key.length>0?new JSONObject(token).get(key[0]):new JSONObject(token));
            return json;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }

    }

    /**
     * Reads JSON file and returns a specific json object from that file based on the root element value
     */
    public static JSONArray getJSONArray(String filepath, String... key) {
        try {
            FileReader reader = new FileReader(filepath);
            JSONTokener token = new JSONTokener(reader);
            JSONArray json = (JSONArray) (key.length > 0 ? new JSONObject(token).get(key[0]) : new JSONArray(token));
            return json;
        } catch (FileNotFoundException e) {
            log.error("file not found", e);
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Reads JSON file and returns it as String
     */
    public static String getJSONString(String filepath) {
        try {
            FileReader reader = new FileReader(filepath);
            JSONTokener token = new JSONTokener(reader);
            JSONObject json = new JSONObject(token);
            return json.toString();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }

    }

    /**
     * Convert json string object to map
     * @param jsonString input JSON object
     * @return map
     */
    public static Map<String, String> getJSONStringToMap(String jsonString) {

        JSONObject json = new JSONObject(jsonString);
        Map<String, String> map = new HashMap<String, String>();
        String[] keys = JSONObject.getNames(json);
        for (String key : keys) {
            map.put(key, json.get(key).toString());
        }
        return map;

    }

    /**
     * Reads JSON string and returns a string list from that string based on the root element value
     */
    public static List<String> getJSONArrayFromString(String jsonString) {
            JSONObject json = new JSONObject(jsonString);
            List<String> list = new ArrayList<>();
            String[] keys = JSONObject.getNames(json);
            for (String key : keys) {
                list.add(json.get(key).toString());
            }
            return list;
    }

    /**
     * reads data from json files and casts to supplied pojo format
     */
    public static <T> T getDataPOJO(String filepath, Class<T> clazz) throws IOException {
        Gson gson = new Gson();
        File file = new File(filepath);
        T dataObj = null;
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            dataObj = gson.fromJson(br, clazz);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return dataObj;
    }

    /**
     * reads data from json files and casts to supplied pojo format
     */
    public static <T> T getData(String path, String dataGroup, Class<T> clazz) throws IOException {
        String filePath=path+dataGroup+".json";
        Gson gson = new Gson();
        File file = new File(filePath);
        T dataObj = null;
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            dataObj = gson.fromJson(br, clazz);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return dataObj;
    }

    /**
     * Convert json object to map
     * @param json input JSON object
     * @return map
     */
    public static Map<String, String> getJSONToMap(JSONObject json) {

        Map<String, String> map = new HashMap<String, String>();
        String[] keys = JSONObject.getNames(json);
        for (String key : keys) {
            map.put(key, json.get(key).toString());
        }

        return map;

    }

    /**
     * Performs a recursive merge between 2 json objects.
     * When the json includes an array then will loop through this as
     * part of the recursive merge.
     */
    public static JSONObject jsonMerge(JSONObject source, JSONObject target) {
        String[] keys = JSONObject.getNames(source);
        if (keys !=null){
            for (String key: keys) {
                Object value = source.get(key);
                if (!target.has(key)) {
                    target.put(key, value);
                } else if (value instanceof JSONArray) {
                    JSONArray array = (JSONArray) value;
                    JSONArray targetarray = (JSONArray) target.get(key);
                    for (int i=0;i<array.length();i++){
                        Object arrayvalue = array.get(i);
                        Object targetarrayvalue = targetarray.get(i);
                        if (arrayvalue instanceof JSONObject) {
                            JSONObject valueJson = (JSONObject)arrayvalue;
                            JSONObject targetvalueJson = (JSONObject)targetarrayvalue;
                            jsonMerge(valueJson, targetvalueJson);
                        }else {
                            target.put(key, value);
                        }
                    }
                } else if (value instanceof JSONObject) {
                    JSONObject valueJson = (JSONObject)value;
                    jsonMerge(valueJson, target.getJSONObject(key));
                } else {
                    target.put(key, value);
                }
            }
        }

        return target;
    }

    public static JSONArray loadJsonArrayFile(JSONArray source) {
        JSONArray targetArray = new JSONArray();
        for (int i = 0; i < source.length(); i++) {
            Object arrayValueSource = source.get(i);
            JSONObject targetObj = new JSONObject();
            JSONObject arrayvalsource = (JSONObject) arrayValueSource;
            Iterator<String> keys = arrayvalsource.keys();
                while (keys.hasNext()) {
                    String key = keys.next();
                    Object value = arrayvalsource.get(key);
                    if (value instanceof JSONObject) {
                        targetObj.put(key, value);
                    } else if (value instanceof JSONArray) {
                        JSONArray array = (JSONArray) value;
                        JSONArray targetSubArray = new JSONArray();
                        for (int j = 0; j < array.length(); j++) {
                            JSONObject targetSubObj = new JSONObject();
                            Object arrayvalue = array.get(j);
                            JSONObject arraysubval = (JSONObject) arrayvalue;
                            Iterator<String> subkeys = arraysubval.keys();
                                while (subkeys.hasNext()) {
                                    String subkey = subkeys.next();
                                    Object subvalue = arraysubval.get(subkey);
                                    targetSubObj.put(subkey, subvalue);
                                }
                            targetSubArray.put(targetSubObj);
                        }
                        targetObj.put(key, targetSubArray);
                    }else{
                        targetObj.put(key, value);
                    }
                }
            targetArray.put(targetObj);
        }
        return targetArray;
    }


}
