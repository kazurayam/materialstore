package com.kazurayam.chrome4testing;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.io.IOException;

public class ChromeForTestingDriverFactory {

    private final Installation installation;

    public ChromeForTestingDriverFactory() {
        this(Installation.mac_116_0_5793_0_mac_x64);
    }

    public ChromeForTestingDriverFactory(Installation installation) {
        this.installation = installation;
    }

    public WebDriver newChromeForTestingDriver() throws IOException {
        ChromeOptions chromeOptions = new ChromeOptions();
        return newChromeForTestingDriver(chromeOptions);
    }

    public WebDriver newChromeForTestingDriver(ChromeOptions chromeOptions) throws IOException {
        // check if the path information specified is OK or not
        installation.check();

        // set the path of ChromeDriver binary
        System.setProperty("webdriver.chrome.driver", installation.getDriverPath());

        // set the path of "Chrome for Testing" binary
        chromeOptions.setBinary(this.installation.getBrowserPath());

        // System.out.println("Chrome installation: " + installation);

        // open a browser window
        return new ChromeDriver(chromeOptions);
    }

}