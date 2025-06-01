package com.kazurayam.chrome4testing;

import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public enum Installation {

    mac_116_0_5793_0_mac_x64(
            String.format("%s/chrome/mac-116.0.5793.0/chrome-mac-x64/Google Chrome for Testing.app/Contents/MacOS/Google Chrome for Testing", System.getProperty("user.home")),
            String.format("%s/chromedriver/mac-116.0.5793.0/chromedriver-mac-x64/chromedriver", System.getProperty("user.home"))
    ),
    mac_136_0_7103_113_mac_x64(
            String.format("%s/chrome/mac-136.0.7103.113/chrome-mac-x64/Google Chrome for Testing.app/Contents/MacOS/Google Chrome for Testing",System.getProperty("user.home")),
            String.format("%s/chromedriver/mac-136.0.7103.113/chromedriver-mac-x64/chromedriver", System.getProperty("user.home"))
    )
    ;

    private final String browserPath;
    private final String driverPath;

    Installation(String browserPath, String driverPath) {
        this.browserPath = browserPath;
        this.driverPath = driverPath;
    }

    public String getBrowserPath() {
        return browserPath;
    }

    public String getDriverPath() {
        return driverPath;
    }

    public String toString() {
        return String.format("\"browserPath\":\"%s\", \"driverPath\":\"%s\"}",
                browserPath, driverPath);
    }

    public void check() throws FileNotFoundException {
        checkIfFileExists(this.getBrowserPath());
        checkIfFileExists(this.getDriverPath());
    }

    private void checkIfFileExists(String path) throws FileNotFoundException {
        Path f = Paths.get(path);
        boolean exists = Files.exists(f);
        if (!exists) {
            throw new FileNotFoundException(path + " is not present");
        }
    }
}