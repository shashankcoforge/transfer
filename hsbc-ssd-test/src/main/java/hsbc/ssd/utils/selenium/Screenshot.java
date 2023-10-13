package hsbc.ssd.utils.selenium;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.*;
import hsbc.ssd.utils.helper.PropertyHelper;
import ru.yandex.qatools.ashot.AShot;
import ru.yandex.qatools.ashot.shooting.ShootingStrategies;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.io.File;
import java.io.IOException;
import java.util.UUID;

/**
 * Base class providing set of common selenium methods
 */

public class Screenshot extends PageObjectUtil {
    /**
     * capture displayed area or scrolling screenshot and return a file object.
     * to capture scrolling screenshot property scrollingScreenshot = true has to be set in runtime.properties file
     */
    public File grabScreenshot() {
        String screenshotType = PropertyHelper.getDefaultProperty("scrollingScreenshot");
        if (screenshotType != null) {
            return (screenshotType.equals("true") ? grabScrollingScreenshot() : grabDisplayedAreaScreenShot());
        } else {
            return grabDisplayedAreaScreenShot();
        }
    }

    /**
     * capture screenshot for the displayed area and return a file object
     */
    public File grabDisplayedAreaScreenShot() {
        try {
            Thread.sleep(PropertyHelper.getDefaultProperties().getInt("screenshotDelay", 0));
        } catch (InterruptedException | NumberFormatException e) {
            e.printStackTrace();
        }
        return ((TakesScreenshot) getDriver()).getScreenshotAs(OutputType.FILE);

    }

    /**
     * capture scrolling screenshot and return a file object
     */
    public File grabScrollingScreenshot() {
        try {
            Thread.sleep(PropertyHelper.getDefaultProperties().getInt("screenshotDelay", 0));
        } catch (InterruptedException | NumberFormatException e) {
            e.printStackTrace();
        }

        ru.yandex.qatools.ashot.Screenshot screenshot;

        if (System.getProperties().get("os.name").toString().contains("Mac")) {
            screenshot = new AShot().shootingStrategy(ShootingStrategies.viewportRetina(100, 0, 0, 2)).takeScreenshot(getDriver());
        } else {
            screenshot = new AShot().shootingStrategy(ShootingStrategies.viewportPasting(1000)).takeScreenshot(getDriver());
        }

        File file = new File("image.png");

        try {
            ImageIO.write(screenshot.getImage(), "PNG", file);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return file;
    }

    /**
     * grab screenshot snippet
     */
    public File snipScreenshot(File screenshot, By by, Dimension dim, Point point) {

        try {
            BufferedImage buffer = ImageIO.read(screenshot);
            // Crop the entire page screenshot to get only element screenshot
            BufferedImage snippet = buffer.getSubimage(0, point.getY(), point.getX() + dim.width, dim.height);
            ImageIO.write(snippet, "png", screenshot);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return screenshot;
    }

    /**
     * capture screenshot and save to specified location
     */
    public File saveScreenshot(File screenshot, String filePath) {
        UUID uuid = UUID.randomUUID();
        File file = new File(filePath + uuid + ".png");
        try {
            FileUtils.moveFile(screenshot, file);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return file;
    }

    /**
     * capture screenshot and save to specified location in a specified file name
     */
    public File saveScreenshot(File screenshot, String filePath, String fileName) {
        File file = new File(filePath + fileName + ".png");
        try {
            FileUtils.moveFile(screenshot, file);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return file;
    }

    /**
     * Compare the screenshot
     */
    public Boolean compareScreenshot(File fileExpected, File fileActual) throws IOException {

        BufferedImage bufileActual = ImageIO.read(fileActual);
        BufferedImage bufileExpected = ImageIO.read(fileExpected);

        DataBuffer dafileActual = bufileActual.getData().getDataBuffer();
        DataBuffer dafileExpected = bufileExpected.getData().getDataBuffer();

        int sizefileActual = dafileActual.getSize();

        boolean matchFlag = true;

        for (int j = 0; j < sizefileActual; j++) {
            if (dafileActual.getElem(j) != dafileExpected.getElem(j)) {
                matchFlag = false;
                break;
            }
        }

        return matchFlag;
    }



}
