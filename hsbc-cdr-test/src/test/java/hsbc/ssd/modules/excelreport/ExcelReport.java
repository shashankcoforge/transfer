package hsbc.ssd.modules.excelreport;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;

import hsbc.ssd.utils.Constants;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import hsbc.ssd.utils.helper.ExcelHelper;

public class ExcelReport extends ExcelHelper {
	/**
     * For checking and creating the folder and file for excel report
     * @throws IOException
     */

    public static String checkAndCreateExcelReportFile() throws IOException {
		try {
			File newFile = new File(Constants.EXCELREPORTPATH);
			if (!newFile.isFile()) {
				File file = new File(getExcelDataFileName());
				File theDir = new File(Constants.DEFAULTEXCELPATH);
				if (!theDir.exists())
					theDir.mkdirs();
			 //To verify excel file exists in ExcelReports folder or not
				Files.copy(file.toPath(), newFile.toPath());  // copy the excel from test data to excel reports
			}
		}catch(Exception e){
			e.printStackTrace();
		}
    	return Constants.EXCELREPORTPATH;
		
	}

	public synchronized void deleteRow(String worksheet, int rowNo) {
		try {
			FileInputStream excelFile;
			excelFile= new FileInputStream(checkAndCreateExcelReportFile());
			XSSFWorkbook workbook = new XSSFWorkbook(excelFile);
			XSSFSheet sheet = workbook.getSheet(worksheet);

			XSSFRow row = sheet.getRow(rowNo);
			sheet.removeRow(row);

			FileOutputStream fileOut = new FileOutputStream(Constants.EXCELREPORTPATH);
			workbook.write(fileOut);
			excelFile.close();
		} catch (Exception e) {
//			e.printStackTrace();
		}
	}
}
