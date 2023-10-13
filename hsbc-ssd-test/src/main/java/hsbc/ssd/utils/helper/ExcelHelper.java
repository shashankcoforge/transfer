package hsbc.ssd.utils.helper;

import hsbc.ssd.utils.Constants;
import hsbc.ssd.utils.selenium.TestParameters;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.testng.Assert;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;

public class ExcelHelper {

    static Logger log = LogManager.getLogger(ExcelHelper.class);

    public static synchronized String getExcelDataFileName(){
        String environmentSettings = PropertyHelper.getVariable("env") != null ? PropertyHelper.getVariable("env") : PropertyHelper.getDefaultProperty("defaultEnvironment");
        String applicationName = PropertyHelper.getVariable("applicationName") != null ? PropertyHelper.getVariable("applicationName") : PropertyHelper.getDefaultProperty("applicationName");
        String excelPath = "";
        if(PropertyHelper.getEnvSpecificAppParameters("dataFromSpecificDataSheet") != null && PropertyHelper.getEnvSpecificAppParameters("dataFromSpecificDataSheet").equalsIgnoreCase("true")){
            excelPath = Constants.TESTDATAPATH+PropertyHelper.getProperty(Constants.ENVIRONMENTPATH+environmentSettings+".properties","TestDataExcel")+".xlsx";
        }else{
            excelPath = Constants.TESTDATAPATH+applicationName+"_TestData.xlsx";
        }
        return excelPath;
    }

    /**
     * reads data from a given sheet in excel returning data in string format.
     * Matches row with the current scenario name and locates data using the column header provided
     * @param worksheet given sheet
     * @param columnName given column
     * @return map list of given data sheet input
     */
    public static synchronized String getData(String worksheet, String columnName) {

        String data = "";

        try {
            FileInputStream file = new FileInputStream(getExcelDataFileName());
            XSSFWorkbook workbook = new XSSFWorkbook(file);
            XSSFSheet sheet = workbook.getSheet(worksheet);

            int rowNo = getCurrentRow(sheet);
            int columnNo = getHeader(sheet, columnName);

            data = rowNo == 0 ? "" : getCellData(sheet.getRow(rowNo).getCell(columnNo)).toString();

            workbook.close();
            file.close();
        } catch (Exception e) {
            log.error("exception processing file", e);
            e.printStackTrace();
        }
        return data;
    }

    /**
     * reads data from a given sheet in excel returning data in string format.
     * Matchs row with the current scenario name and locates data using the column header provided
     * @param worksheet given sheet
     * @param columnName given column
     * @return map list of given data sheet input
     */
    public static synchronized String getSubScenarioData(String worksheet, String columnName, String secondaryKey, int... rowNum) {

        String data = "";

        try {
            FileInputStream file = new FileInputStream(getExcelDataFileName());
            XSSFWorkbook workbook = new XSSFWorkbook(file);
            XSSFSheet sheet = workbook.getSheet(worksheet);
            int rowNo = 0;
            if(rowNum.length > 0){
                rowNo =  rowNum[0] !=0 ? rowNum[0] : getSubRow(sheet, secondaryKey);
            }else{
                rowNo = getSubRow(sheet, secondaryKey);
            }

            int columnNo = getHeader(sheet, columnName);

            Object dat = rowNo == 0 ? "" : getCellData(sheet.getRow(rowNo).getCell(columnNo));
            if(null != dat)
                data = dat.toString();
            workbook.close();
            file.close();
        } catch (Exception e) {
            log.error("exception processing file", e);
            e.printStackTrace();
        }
        return data;
    }

    public static synchronized String getSubScenarioDataUsingFileName(String worksheet, String columnName, String secondaryKey, String fileName) {

        String data = "";

        try {
            FileInputStream excelFile= new FileInputStream(fileName);
            XSSFWorkbook workbook = new XSSFWorkbook(excelFile);
            XSSFSheet sheet = workbook.getSheet(worksheet);
            int rowNo = getSubRow(sheet, secondaryKey);

            int columnNo = getHeader(sheet, columnName);

            data = rowNo == 0 ? "" : getCellData(sheet.getRow(rowNo).getCell(columnNo)).toString();

            workbook.close();
            excelFile.close();
        } catch (Exception e) {
            log.error("exception processing file", e);
            e.printStackTrace();
        }
        return data;
    }

    public static synchronized String getDataOnlyBySecondaryKey(String worksheet, String columnName, String secondaryKey, int... rowNum) {

        String data = "";

        try {
            FileInputStream file = new FileInputStream(getExcelDataFileName());
            XSSFWorkbook workbook = new XSSFWorkbook(file);
            XSSFSheet sheet = workbook.getSheet(worksheet);
            int rowNo = 0;
            if(rowNum.length > 0){
                rowNo =  rowNum[0] !=0 ? rowNum[0] : getSecKeyRow(sheet, secondaryKey);
            }else{
                rowNo = getSecKeyRow(sheet, secondaryKey);
            }

            int columnNo = getHeader(sheet, columnName);

            data = rowNo == 0 ? "" : getCellData(sheet.getRow(rowNo).getCell(columnNo)).toString();

            workbook.close();
            file.close();
        } catch (Exception e) {
            log.error("exception processing file", e);
            e.printStackTrace();
        }
        return data;
    }

    /**
     * To update the column with columName for rowNo from worksheet passed
     * @param worksheet
     * @param columnName
     * @param rowNo
     * @param data
     * @param fileName
     */
    public static synchronized void setData(String worksheet, String columnName, int rowNo, String data, String... fileName) {

        try {
            FileInputStream excelFile;
            if(fileName.length>0)
                excelFile = new FileInputStream(fileName[0]);
            else
                excelFile= new FileInputStream(getExcelDataFileName());
            XSSFWorkbook workbook = new XSSFWorkbook(excelFile);
            XSSFSheet sheet = workbook.getSheet(worksheet);

            int columnNo = getHeader(sheet, columnName);

            XSSFRow row = sheet.getRow(rowNo);
            Cell cell = row.createCell(columnNo);
            cell.setCellValue(data);
            FileOutputStream fileOut = new FileOutputStream(Constants.EXCELREPORTPATH);
            workbook.write(fileOut);
            excelFile.close();
        } catch (Exception e) {
            log.error("exception processing file", e);
            e.printStackTrace();
        }
    }

    /**
     * To update the column with columName for rowNo from worksheet passed
     * @param worksheet
     * @param columnName
     * @param rowNo
     * @param data
     */
    public static synchronized void updateDataSheet(String worksheet, String columnName, int rowNo, String data) {

        try {
            FileInputStream excelFile = new FileInputStream(getExcelDataFileName());
            XSSFWorkbook workbook = new XSSFWorkbook(excelFile);
            XSSFSheet sheet = workbook.getSheet(worksheet);

            int columnNo = getHeader(sheet, columnName);

            XSSFRow row = sheet.getRow(rowNo);
            Cell cell = row.createCell(columnNo);
            cell.setCellValue(data);
            FileOutputStream fileOut = new FileOutputStream(getExcelDataFileName());
            workbook.write(fileOut);
            excelFile.close();
        } catch (Exception e) {
            log.error("exception processing file", e);
            e.printStackTrace();
        }
    }

    /**
     *
     * @param worksheet
     * @param columnName
     * @param secondaryKey
     * @param data
     */
    public static synchronized void setData(String worksheet, String columnName, String secondaryKey, String data, String... fileName) {

        try {
            FileInputStream excelFile;
            if(fileName.length>0)
                excelFile = new FileInputStream(fileName[0]);
            else
                excelFile= new FileInputStream(getExcelDataFileName());
            XSSFWorkbook workbook = new XSSFWorkbook(excelFile);
            XSSFSheet sheet = workbook.getSheet(worksheet);

            int rowNo = getSubRow(sheet, secondaryKey);
            int columnNo = getHeader(sheet, columnName);

            XSSFRow row = sheet.getRow(rowNo);
            Cell cell = row.createCell(columnNo);
            cell.setCellValue(data);

            FileOutputStream fileOut = new FileOutputStream(Constants.EXCELREPORTPATH);
            workbook.write(fileOut);
            excelFile.close();
        } catch (Exception e) {
            log.error("exception processing file", e);
            e.printStackTrace();
        }
    }

    public static synchronized void setSubScenarioData(String worksheet, String columnName, String data, int... rowNum) {

        try {
            FileInputStream file = new FileInputStream(getExcelDataFileName());
            XSSFWorkbook workbook = new XSSFWorkbook(file);
            XSSFSheet sheet = workbook.getSheet(worksheet);

            int rowNo = getCurrentRow(sheet);
            int columnNo = getHeader(sheet, columnName);

            XSSFRow row = sheet.getRow(rowNo);
            Cell cell = row.createCell(columnNo);
            cell.setCellValue(data);

            FileOutputStream fileOut = new FileOutputStream(getExcelDataFileName());
            workbook.write(fileOut);
            file.close();
        } catch (Exception e) {
            log.error("exception processing file", e);
            e.printStackTrace();
        }
    }

    private static synchronized int getCurrentRow(XSSFSheet sheet){

        int rowNum = 0;
        int currentRowNum = 1;

        int lastRow = sheet.getLastRowNum();
        while (currentRowNum < lastRow) {
            if(getCellData(sheet.getRow(currentRowNum).getCell(0)).toString().equalsIgnoreCase(TestParameters.getInstance().getFrameworkData("scenarioName").toString())){
                return currentRowNum;
            }
            currentRowNum++;
        }
        log.warn("Scenario not found in the excel data sheet : "+TestParameters.getInstance().getFrameworkData("scenarioName").toString());
        return rowNum;
    }

    private static synchronized int getSubRow(XSSFSheet sheet, String secondaryKey){

        int rowNum = 0;
        int subRowNum = 0;
        int reqRowNum = 0;
        int currentRowNum = 1;
        int serviceColNo= getHeader(sheet, "Service");
        boolean status = false;

        int lastRow = sheet.getPhysicalNumberOfRows();
        while (currentRowNum < lastRow) {
            if(getCellData(sheet.getRow(currentRowNum).getCell(0)).toString().equalsIgnoreCase(TestParameters.getInstance().getFrameworkData("scenarioName").toString())){
                subRowNum = currentRowNum;
                while (getCellData(sheet.getRow(subRowNum).getCell(0)).toString().equalsIgnoreCase(TestParameters.getInstance().getFrameworkData("scenarioName").toString())) {
                    if (getCellData(sheet.getRow(subRowNum).getCell(serviceColNo)).toString().equalsIgnoreCase(secondaryKey)) {
                        return subRowNum;
                    }
                    subRowNum++;
                }
            }
            currentRowNum++;
        }
        Assert.fail("Scenario not found in the excel data sheet : " + TestParameters.getInstance().getFrameworkData("scenarioName").toString()+"SecondaryKey : "+secondaryKey);
        return reqRowNum;
    }

    private static synchronized int getSubRowByColumn(XSSFSheet sheet, String secondaryKey,String columnName){

        int rowNum = 0;
        int subRowNum = 0;
        int reqRowNum = 0;
        int currentRowNum = 1;
        int serviceColNo= getHeader(sheet, columnName);
        boolean status = false;

        int lastRow = sheet.getPhysicalNumberOfRows();
        while (currentRowNum < lastRow) {
            if(getCellData(sheet.getRow(currentRowNum).getCell(0)).toString().equalsIgnoreCase(TestParameters.getInstance().getFrameworkData("scenarioName").toString())){
                subRowNum = currentRowNum;
                while (getCellData(sheet.getRow(subRowNum).getCell(0)).toString().equalsIgnoreCase(TestParameters.getInstance().getFrameworkData("scenarioName").toString())) {
                    if (null != getCellData(sheet.getRow(subRowNum).getCell(serviceColNo)) && getCellData(sheet.getRow(subRowNum).getCell(serviceColNo)).toString().equalsIgnoreCase(secondaryKey)) {
                        return subRowNum;
                    }
                    subRowNum++;
                }
            }
            currentRowNum++;
        }
        Assert.fail("Scenario not found in the excel data sheet : " + TestParameters.getInstance().getFrameworkData("scenarioName").toString()+"SecondaryKey : "+secondaryKey);
        return reqRowNum;
    }

    public static synchronized int getSubRow(String worksheet, String secondaryKey, String... fileName){
        int rowNo = 0;
        try {
            FileInputStream excelFile;
            if(fileName.length>0)
                excelFile = new FileInputStream(fileName[0]);
            else
                excelFile= new FileInputStream(getExcelDataFileName());
            XSSFWorkbook workbook = new XSSFWorkbook(excelFile);
            XSSFSheet sheet = workbook.getSheet(worksheet);
            rowNo = getSubRow(sheet, secondaryKey);
            workbook.close();
            excelFile.close();
        } catch (Exception e) {
            log.error("exception processing file", e);
            e.printStackTrace();
        }
        return rowNo;
    }

    public static synchronized int getSubRowByColumnName(String worksheet, String secondaryKey,String columnName){
        int rowNo = 0;
        try {
            FileInputStream file = new FileInputStream(getExcelDataFileName());
            XSSFWorkbook workbook = new XSSFWorkbook(file);
            XSSFSheet sheet = workbook.getSheet(worksheet);
            rowNo = getSubRowByColumn(sheet, secondaryKey,columnName);
            workbook.close();
            file.close();
        } catch (Exception e) {
            log.error("exception processing file", e);
            e.printStackTrace();
        }
        return rowNo;
    }

    private static synchronized int getSecKeyRow(XSSFSheet sheet, String secondaryKey){

        int rowNum = 0;
        int subRowNum = 0;
        int reqRowNum = 0;
        int currentRowNum = 1;
        int serviceColNo= getHeader(sheet, "Service");
        boolean status = false;

        int lastRow = sheet.getPhysicalNumberOfRows();
        while (currentRowNum < lastRow) {
            //if(getCellData(sheet.getRow(currentRowNum).getCell(0)).toString().equalsIgnoreCase(TestParameters.getInstance().getFrameworkData("scenarioName").toString())){
            subRowNum = currentRowNum;
            //while (getCellData(sheet.getRow(subRowNum).getCell(0)).toString().equalsIgnoreCase(TestParameters.getInstance().getFrameworkData("scenarioName").toString())) {
            if (getCellData(sheet.getRow(subRowNum).getCell(serviceColNo)).toString().equalsIgnoreCase(secondaryKey)) {
                return subRowNum;
            }
            //subRowNum++;
            //}
            currentRowNum++;
        }

        //}
        Assert.fail("SecondaryKey not found: "+secondaryKey);
        return reqRowNum;
    }

    public static synchronized int getHeader(XSSFSheet sheet, String ColumnName){
        int colNum = 0;
        int currentColNum = 0;
        int lastCol = sheet.getRow(0).getPhysicalNumberOfCells();
        while (currentColNum < lastCol) {
            if(getCellData(sheet.getRow(0).getCell(currentColNum)).toString().equalsIgnoreCase(ColumnName)){
                colNum = currentColNum;
                break;
            }
            currentColNum++;
        }
        return colNum;
    }

    public static synchronized Object getCellData(Cell cell) {
        Object obj = null;

        if (cell == null) return null;

        switch (cell.getCellTypeEnum()) {
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    obj = (new SimpleDateFormat("yyyy-MM-dd").format(cell.getDateCellValue()));
                } else {
                    obj = (cell.getNumericCellValue());
                }
                break;
            case STRING:
                obj = (cell.getStringCellValue());
                break;
            case BLANK:
                obj = "";
                break;
            case BOOLEAN:
                obj = cell.getBooleanCellValue();
                break;
            default:
                obj = (cell.getStringCellValue());
        }
        return obj;
    }

    public static synchronized String getField(String worksheet, String columnName, int rowNo) {
        String data="";
        try {
            FileInputStream file = new FileInputStream(getExcelDataFileName());
            XSSFWorkbook workbook = new XSSFWorkbook(file);
            XSSFSheet sheet = workbook.getSheet(worksheet);
            int columnNo = getHeader(sheet, columnName);
            data= rowNo == 0 ? "" : getCellData(sheet.getRow(rowNo).getCell(columnNo)).toString();
            workbook.close();
            file.close();
        }
        catch (Exception e) {
            log.error("exception processing file", e);
            e.printStackTrace();
        }
        return data;
    }

    public static synchronized String getField(String worksheet, String columnName, int rowNo, String... fileName) {
        String data="";
        try {
            FileInputStream file = new FileInputStream(fileName.length > 0 ? fileName[0] : getExcelDataFileName());
            XSSFWorkbook workbook = new XSSFWorkbook(file);
            XSSFSheet sheet = workbook.getSheet(worksheet);
            int columnNo = getHeader(sheet, columnName);
            data= rowNo == 0 ? "" : getCellData(sheet.getRow(rowNo).getCell(columnNo)).toString();
            workbook.close();
            file.close();
        }
        catch (Exception e) {
            log.error("exception processing file", e);
            e.printStackTrace();
        }
        return data;
    }

}
