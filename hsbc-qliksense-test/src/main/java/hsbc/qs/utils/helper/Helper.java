package hsbc.qs.utils.helper;

import hsbc.qs.utils.reporting.Reporter;
import hsbc.qs.utils.selenium.Screenshot;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import hsbc.qs.utils.Constants;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static hsbc.qs.utils.reporting.Reporter.addScreenCaptureFromPath;


/**
 * Class to encapsulate utility functions of the framework
 */
public class Helper {

    public Helper() {}

    /**
     * Function to get the separator string to be used for directories and files
     * based on the current OS
     *
     * @return The file separator string
     */
    public static String getFileSeparator() {
        return System.getProperty("file.separator");
    }

    public static String getAbsolutePath() {
        String relativePath = new File(System.getProperty("user.dir"))
                .getAbsolutePath();
        return relativePath;
    }

    public static String getResultsPath() {

        File path = new File(Helper.getAbsolutePath() + Helper.getFileSeparator()
                + "target" + Helper.getFileSeparator() + "Results");

        if (!path.isDirectory()) {
            path.mkdirs();
        }

        return path.toString();
    }

    public static String getOldResultPath() {

        File path = new File(Helper.getAbsolutePath() + Helper.getFileSeparator()
                + "ResultsOld");

        if (!path.isDirectory()) {
            path.mkdirs();
        }

        return path.toString();
    }

    public static String getTargetPath() {

        File targetPath = new File(Helper.getAbsolutePath()
                + Helper.getFileSeparator() + "target" + Helper.getFileSeparator()
                + "cucumber-report");

        return targetPath.toString();
    }

    public static byte[] takeScreenshot(WebDriver driver) {
        if (driver == null) {
            throw new RuntimeException("Report.driver is not initialized!");
        }

        if (driver.getClass().getSimpleName().equals("HtmlUnitDriver")
                || driver
                .getClass()
                .getGenericSuperclass()
                .toString()
                .equals("class org.openqa.selenium.htmlunit.HtmlUnitDriver")) {
            return null; // Screenshots not supported in headless mode
        }

//        if (driver.getClass().getSimpleName().equals("RemoteWebDriver")) {
//            Capabilities capabilities = ((RemoteWebDriver) driver)
//                    .getCapabilities();
//            if (capabilities.getBrowserName().equals("htmlunit")) {
//                return null; // Screenshots not supported in headless mode
//            }
//            WebDriver augmentedDriver = new Augmenter().augment(driver);
//            return ((TakesScreenshot) augmentedDriver)
//                    .getScreenshotAs(OutputType.BYTES);
//        } else {
            return ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
//        }
    }

    /**
     * Function to return the current time
     *
     * @return The current time
     * @see #getCurrentFormattedTime(String)
     */
    public static Date getCurrentTime() {
        Calendar calendar = Calendar.getInstance();
        return calendar.getTime();
    }

    /**
     * Function to return the current time, formatted as per the
     * DateFormatString setting
     *
     * @param dateFormatString The date format string to be applied
     * @return The current time, formatted as per the date format string
     * specified
     * @see #getCurrentTime()
     * @see #getFormattedTime(Date, String)
     */
    public static String getCurrentFormattedTime(String dateFormatString) {
        DateFormat dateFormat = new SimpleDateFormat(dateFormatString);
        Calendar calendar = Calendar.getInstance();
        return dateFormat.format(calendar.getTime());
    }

    /**
     * Function to format the given time variable as specified by the
     * DateFormatString setting
     *
     * @param time             The date/time variable to be formatted
     * @param dateFormatString The date format string to be applied
     * @return The specified date/time, formatted as per the date format string
     * specified
     * @see #getCurrentFormattedTime(String)
     */
    public static String getFormattedTime(Date time, String dateFormatString) {
        DateFormat dateFormat = new SimpleDateFormat(dateFormatString);
        return dateFormat.format(time);
    }

    /**
     * Function to get the time difference between 2 {@link Date} variables in
     * minutes/seconds format
     *
     * @param startTime The start time
     * @param endTime   The end time
     * @return The time difference in terms of hours, minutes and seconds
     */
    public static String getTimeDifference(Date startTime, Date endTime) {
        long timeDifferenceSeconds = (endTime.getTime() - startTime.getTime()) / 1000; // to
        // convert
        // from
        // milliseconds
        // to
        // seconds
        long timeDifferenceMinutes = timeDifferenceSeconds / 60;

        String timeDifferenceDetailed;
        if (timeDifferenceMinutes >= 60) {
            long timeDifferenceHours = timeDifferenceMinutes / 60;

            timeDifferenceDetailed = Long.toString(timeDifferenceHours)
                    + " hour(s), " + Long.toString(timeDifferenceMinutes % 60)
                    + " minute(s), "
                    + Long.toString(timeDifferenceSeconds % 60) + " second(s)";
        } else {
            timeDifferenceDetailed = Long.toString(timeDifferenceMinutes)
                    + " minute(s), "
                    + Long.toString(timeDifferenceSeconds % 60) + " second(s)";
        }

        return timeDifferenceDetailed;
    }

    public static void deleteFolder(String folderName) {
        try {
            deleteDir(new File(folderName));
        } catch (Exception e) {
        }
    }

    private static void deleteDir(File file) {
        try {
            File[] contents = file.listFiles();
            if (contents != null) {
                for (File f : contents) {
                    if (!Files.isSymbolicLink(f.toPath())) {
                        deleteDir(f);
                    }
                }
            }
            file.delete();
        } catch (Exception e) {
        }
    }

    public static String runCommand(String... params) {
        ProcessBuilder pb = new ProcessBuilder(params);
        Process p;
        StringJoiner joiner = new StringJoiner(System.getProperty("line.separator"));
        try {
            p = pb.start();
            final BufferedReader reader = new BufferedReader(
                    new InputStreamReader(p.getInputStream()));
            reader.lines().iterator().forEachRemaining(joiner::add);

            p.waitFor();
            p.destroy();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return joiner.toString().substring(0, joiner.toString().length() - 1).substring(1);
    }


    public static void addScreenshot() {
        File img = new Screenshot().grabScreenshot();
        if (img != null) {
            File file = new Screenshot().saveScreenshot(img, Reporter.getScreenshotPath());
            Reporter.addScreenCaptureFromPath("." + File.separator + "Screenshots_" + Constants.DEFAULTTIMESTAMP + File.separator + file.getName());
        }
    }

    public double subtractDouble(double a, double b) {
        BigDecimal cost1 = new BigDecimal(a);
        BigDecimal cost2 = new BigDecimal(b);
        return new BigDecimal(String.valueOf(cost1.subtract(cost2))).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }

    public double addDouble(double a, double b) {
        BigDecimal cost1 = new BigDecimal(a);
        BigDecimal cost2 = new BigDecimal(b);
        return new BigDecimal(String.valueOf(cost1.add(cost2))).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }

    public String findDifferenceInTime(String time1, String time2) throws ParseException {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ss");
        Date t1 = simpleDateFormat.parse(time1);
        Date t2 = simpleDateFormat.parse(time2);

        long differenceInMilliSeconds = Math.abs(t2.getTime() - t1.getTime());

        long differenceInHours = (differenceInMilliSeconds / (60 * 60 * 1000)) % 24;

        long differenceInMinutes = (differenceInMilliSeconds / (60 * 1000)) % 60;

        long differenceInSeconds = (differenceInMilliSeconds / 1000) % 60;

        return differenceInHours+":"+differenceInMinutes+":"+differenceInSeconds;
    }

    public boolean isNumeric(String text){
        return Stream.of(text)
                .filter(s -> s != null && !s.isEmpty())
                .filter(Pattern.compile("\\D").asPredicate().negate())
                .mapToLong(Long::valueOf)
                .boxed()
                .findAny()
                .isPresent();
    }

    public <K, V> Map<K, V> copyMap(Map<K, V> original) {
        Map<K, V> second_Map = new HashMap<>();
        for (Map.Entry<K, V> entry : original.entrySet()) {
            second_Map.put(entry.getKey(),
                    entry.getValue());
        }
        return second_Map;
    }

}