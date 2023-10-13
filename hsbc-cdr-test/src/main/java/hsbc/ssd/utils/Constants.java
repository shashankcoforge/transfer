package hsbc.ssd.utils;

import hsbc.ssd.utils.helper.Helper;
import hsbc.ssd.utils.helper.PropertyHelper;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * POJO used to define constants of selenium
 */
public class Constants {

    private static final String targetPath = Helper.getAbsolutePath() + Helper.getFileSeparator() +"target"+Helper.getFileSeparator()+"test-classes";
    public static final String BASEPATH = Files.isDirectory(Paths.get(targetPath)) ? targetPath + Helper.getFileSeparator() : Helper.getAbsolutePath() + Helper.getFileSeparator();
    public static final String DEFAULTSETTINGS = BASEPATH + "config" +Helper.getFileSeparator() + "defaultSettings.properties";
    public static final String ENVIRONMENTNAME = PropertyHelper.getVariable("env") != null ?
            PropertyHelper.getVariable("env")
            : PropertyHelper.getDefaultProperty("defaultEnvironment");
    public static final String ENVIRONMENTPATH = BASEPATH + "config" +Helper.getFileSeparator() + "environments"+Helper.getFileSeparator();
    public static final String DRIVERSTACKSPATH = BASEPATH + "config" + Helper.getFileSeparator()+ "driver" + Helper.getFileSeparator();
    public static final String TESTDATAPATH = BASEPATH + "testData" + Helper.getFileSeparator();
    public static final String APISTRUCTUREPATH =  BASEPATH + "apischema"+ Helper.getFileSeparator();
    public static final String BACKENDLOGS = Helper.getAbsolutePath() + Helper.getFileSeparator()+"aws"+Helper.getFileSeparator();
    public static final String DEFAULTREPORTPATH = PropertyHelper.getVariable("reportPath") != null ? PropertyHelper.getVariable("reportPath") : System.getProperty("user.dir")+Helper.getFileSeparator()+"Reports"+Helper.getFileSeparator();
    public static final String DEFAULTEXCELPATH = DEFAULTREPORTPATH+"ExcelReports"+File.separator;
    public static final String DEFAULTTIMESTAMP = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
    public static final String SCREENSHOTPATH = DEFAULTREPORTPATH+"Screenshots_"+DEFAULTTIMESTAMP+File.separator;

    public static final String CREDENCEITEMSCHEMAFILE = TESTDATAPATH + File.separator + "credence" +File.separator + "itemSchema.csv";
    public static final String EXCELREPORTNAME = PropertyHelper.getDefaultProperty("StaticExcelReportFileName").equalsIgnoreCase("true") ?
            PropertyHelper.getDefaultProperty("ExcelReportFileName")
            : ENVIRONMENTNAME+"_ExcelReport_"+ DEFAULTTIMESTAMP;
    public static final String EXCELREPORTPATH = DEFAULTEXCELPATH + EXCELREPORTNAME +".xlsx";
    public static final String DEFAULTZAPPATH = DEFAULTREPORTPATH+"ZapReports"+File.separator;
    public static final String ZAPREPORTPATH = PropertyHelper.getVariable("env") != null ? 
            DEFAULTZAPPATH+PropertyHelper.getVariable("env")+"_ZAPReport_"+ DEFAULTTIMESTAMP +".html" :
            DEFAULTZAPPATH+PropertyHelper.getDefaultProperty("defaultEnvironment")+"_ZAPReport_"+ DEFAULTTIMESTAMP +".html";

    public static final String DEFAULTDBENTRIESCSVPATH=DEFAULTEXCELPATH +"EB_Entires_"+DEFAULTTIMESTAMP+".csv";


}
