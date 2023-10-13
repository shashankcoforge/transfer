package hsbc.qs.utils.reporting;

import com.aventstack.extentreports.Status;
import com.aventstack.extentreports.model.AbstractStructure;
import com.aventstack.extentreports.model.Test;
import hsbc.qs.utils.reporting.service.ExtentService;
import org.apache.commons.lang3.StringUtils;
import hsbc.qs.utils.Constants;
import hsbc.qs.utils.helper.PropertyHelper;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.List;

/**
 * class to create the plain test execution report (suitable for email)
 */
public class TextReport {

    //Used to calculate the table padding when printing to the reports
    private int maxName = 0;
    private int maxStatus = 0;
    private int maxDuration = 0;

    //Used for table formatting
    private static String scenario_padding = "  -> ";
    private static String scenario_outline_padding = "  + Scenario Outline: ";
    private String newlineChar = "\n";

    //Collect the data into a table
    private TextReportData reports;
    private StringBuilder sb = new StringBuilder();


    private int countFeature = 0;
    private int countFeatureFail = 0;
    private int countFeaturePass = 0;
    private int countFeatureSkip = 0;
    private int countScenario = 0;
    private int countScenarioFail = 0;
    private int countScenarioPass = 0;
    private int countScenarioSkip = 0;


    //Processes the Extent Reports and extract the test information out so we can generate report latter on.
    public TextReport() {
        reports = new TextReportData("Feature -> Scenario", "Status", "Duration");
        checkMaxName(reports.getName());
        checkMaxStatus(reports.getStatus());
        checkMaxDuration(reports.getDuration());

        List<Test> features = ExtentService.getHTML().getTestList();
        for (Test feature : features) {

            TextReportData fr = new TextReportData(feature.getName(), feature.getStatus().toString(), convertTime(feature.getRunDurationMillis()));
            countFeature++;
            if (feature.getStatus() == Status.PASS) {
                countFeaturePass++;
            }
            if (feature.getStatus() == Status.FAIL) {
                countFeatureFail++;
            }
            if (feature.getStatus() == Status.SKIP) {
                countFeatureSkip++;
            }

            //Checking the padding size
            checkMaxName(fr.getName());
            checkMaxStatus(fr.getStatus());
            checkMaxDuration(fr.getDuration());

            //loops through each scenarios
            AbstractStructure<Test> tests = feature.getNodeContext();

            for (int i = 0; i < tests.size(); i++) {
                Test test = tests.get(i);
                TextReportData tr;

                //checks if ScenarioOutline outline and if so, skip stat as it provides info only. else there be a bug showing an additional entry
                if (test.getBehaviorDrivenType() != com.aventstack.extentreports.gherkin.model.ScenarioOutline.class) {
                    tr = new TextReportData(scenario_padding + test.getName(), test.getStatus().toString(), convertTime(test.getRunDurationMillis()));
                    countScenario++;
                    if (test.getStatus() == Status.PASS) {
                        countScenarioPass++;
                    }
                    if (test.getStatus() == Status.FAIL) {
                        countScenarioFail++;
                    }
                    if (test.getStatus() == Status.SKIP) {
                        countScenarioSkip++;
                    }
                } else {
                    tr = new TextReportData(scenario_outline_padding + test.getName(), "n/a", "n/a");
                }

//				System.out.println(getInfo(test)); //Useful debuging

                //Checks padding again
                checkMaxName(tr.getName());
                checkMaxStatus(tr.getStatus());
                checkMaxDuration(tr.getDuration());
                fr.addChild(tr);
            }

            reports.addChild(fr);
        }
    }

    //used for debugging as above
//	private String getInfo(com.aventstack.extentreports.model.Test test) {
//    	return "Name[" + test.getName() + "] Dur[" + test.getRunDuration() + "] Status[" + test.getStatus().toString() + "]";
//    }

    //Checks the text size to increase table width
    private void checkMaxName(String name) {
        if (name.length() > maxName) maxName = name.length();
    }

    //Checks the text size to increase table width
    private void checkMaxStatus(String status) {
        if (status.length() > maxStatus) maxStatus = status.length();
    }

    //Checks the text size to increase table width
    private void checkMaxDuration(String duration) {
        if (duration.length() > maxDuration) maxDuration = duration.length();
    }

    //prints the report to console
    public void printReport() {
        System.out.println(sb.toString());
    }

    //Generates the table information
    public void generateReport() {
        int table_padding = 10; //chars from the String.format("| %s | %s |...
        //summary results
        int summaryPad = 7;
        String summaryPadding = StringUtils.leftPad("", (4 * (summaryPad + 3) + 14), '-');
        sb.append("Run Result Summary");
        sb.append(newlineChar);
        sb.append(summaryPadding);
        sb.append(newlineChar);
        sb.append(String.format("| %-" + 10 + "s | %-" + summaryPad + "s | %-" + summaryPad + "s | %-" + summaryPad + "s | %-" + summaryPad + "s |", "Status", "Passed", "Failed", "Skipped", "Other"));
        sb.append(newlineChar);
        sb.append(summaryPadding);
        sb.append(newlineChar);
        sb.append(String.format("| %-" + 10 + "s | %-" + summaryPad + "s | %-" + summaryPad + "s | %-" + summaryPad + "s | %-" + summaryPad + "s |", "Features", countFeaturePass, countFeatureFail, countFeatureSkip, countFeature - countFeaturePass - countFeatureFail - countFeatureSkip));
        sb.append(newlineChar);
        sb.append(String.format("| %-" + 10 + "s | %-" + summaryPad + "s | %-" + summaryPad + "s | %-" + summaryPad + "s | %-" + summaryPad + "s |", "Scenarios", countScenarioPass, countScenarioFail, countScenarioSkip, countScenario - countScenarioPass - countScenarioFail - countScenarioSkip));
        sb.append(newlineChar);
        sb.append(summaryPadding);
        sb.append(newlineChar);
        sb.append(newlineChar);
        sb.append("Run Result Listing");
        sb.append(newlineChar);
        //detail results
        String lingPadding = StringUtils.leftPad("", table_padding + maxName + maxDuration + maxStatus, '-');
        String started = "Started: " + ExtentService.getHTML().getStartTime().toString();
        //sb.append("Ended: " + ExtentCucumberFormatter.getExtentHtmlReport().getEndTime().toString() + newlineChar);
        sb.append(lingPadding);
        sb.append(newlineChar);
        sb.append(String.format("| %-" + maxName + "s | %-" + maxStatus + "s | %-" + maxDuration + "s |", reports.getName(), reports.getStatus(), reports.getDuration()));
        sb.append(newlineChar);
        sb.append(lingPadding);
        sb.append(newlineChar);
        for (TextReportData featureChild : reports.getChildren()) {
            printChild(featureChild);
        }
        sb.append(lingPadding);
        sb.append(newlineChar);
        long timepadd = (maxName + maxDuration + maxStatus) - started.length() + 5; //5 the number of char in below format thats not dynamic
//        sb.append(String.format("| %s %" + timepadd + "s |", started, convertTime(ExtentService.getHTML().getRunDuration())));
        sb.append(newlineChar);
        sb.append(lingPadding);
        sb.append(newlineChar);
    }

    //processes the child of the main text report
    private void printChild(TextReportData trd) {
        sb.append(String.format("| %-" + maxName + "s | %-" + maxStatus + "s | %-" + maxDuration + "s |", trd.getName(), trd.getStatus(), trd.getDuration()));
        sb.append(newlineChar);
        for (TextReportData testChild : trd.getChildren()) {
            printChild(testChild);
        }
    }

    //converts long into readable format
    private String convertTime(long diff) {
        long secs = diff / 1000;
        long millis = diff % 1000;
        long mins = secs / 60;
        secs = secs % 60;
        long hours = mins / 60;
        mins = mins % 60;

        return hours + "h " + mins + "m " + secs + "s " + millis + "ms";
    }

    //used if on a different OS or file system
    public void setNewLineChar(String chars) {
        newlineChar = chars;
    }

    //Saves the report to file
    public void saveReport(String path) {
        try (PrintWriter printer = new PrintWriter(path)) {
            printer.println(sb.toString());
        } catch (FileNotFoundException e) {
            System.err.println("Unable to save text report to file!: " + path);
            throw new RuntimeException(e);
        }
    }

    public void saveReport() {
        String filename = ExtentService.getUniqueKey() == null ? "RunSummary":"RunSummary_" +  ExtentService.getUniqueKey();
        String defaultReportPath = Constants.DEFAULTREPORTPATH;
        String reportPath = PropertyHelper.getVariable("textReportPath");
        saveReport((reportPath == null ? defaultReportPath : reportPath) + filename);
    }

    public void createReport(String path) {
        generateReport();
        printReport();
        saveReport(path);
    }

    public void createReport(boolean saveReport) {
        generateReport();
        printReport();

        // true = Save the report to file
        // false = Just print report to console
        if(saveReport){ saveReport(); }
    }
}
