package tut.util;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import ru.yandex.qatools.ashot.AShot;
import ru.yandex.qatools.ashot.Screenshot;
import ru.yandex.qatools.ashot.coordinates.WebDriverCoordsProvider;
import ru.yandex.qatools.ashot.shooting.ShootingStrategies;

import java.awt.image.BufferedImage;

public class ScreenshotUtil {

    public static BufferedImage takeEntirePageImage(WebDriver driver) {
        int timeout = 500; // scrolling timeout default to 500 milli-seconds
        float dpr = resolveDevicePixelRatio(driver);
        AShot aShot = new AShot()
                .coordsProvider(new WebDriverCoordsProvider())
                .shootingStrategy(
                        ShootingStrategies.viewportPasting(
                                ShootingStrategies.scaling(dpr), timeout));
        Screenshot screenshot = aShot.takeScreenshot(driver);
        return screenshot.getImage();
    }

    private static float resolveDevicePixelRatio(WebDriver driver) {
        JavascriptExecutor js = (JavascriptExecutor)driver;
        Long value = (Long)js.executeScript("return window.devicePixelRatio;");
        return (float)value;
    }

}
