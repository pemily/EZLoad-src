package com.pascal.ezload.service.util;

import com.pascal.ezload.service.sources.Reporting;

import java.io.File;
import java.io.IOException;
import java.util.Locale;

public class ChromeDriverTools {

    public static void setup(Reporting reporting, String driverPath) throws Exception {
        if (driverPath.toLowerCase(Locale.ROOT).endsWith(".zip")){
            String dir = new File(driverPath).getParentFile().getAbsolutePath();

            ZipFileCompressUtils zipFileCompressUtils = new ZipFileCompressUtils();

            String extractedDriver = zipFileCompressUtils.extractOneFile(driverPath, dir);
            reporting.info("zip file detected, unzip it and use driver: "+extractedDriver);
            System.setProperty("webdriver.chrome.driver", extractedDriver);
        }
        else {
            //Setting system properties of ChromeDriver
            System.setProperty("webdriver.chrome.driver", driverPath);
        }
    }

    public static String downloadChromeDriver(Reporting reporting, String chromeVersion, String currentChromeDriverPath) throws IOException {
        String newDir = new File(currentChromeDriverPath).getParent().concat(File.separator+chromeVersion);
        new File(newDir).mkdirs();
        String newChromeDriver = newDir+File.separator+"chromeDriver.zip";

        try(Reporting rep = reporting.pushSection("Download Chrome Driver for version "+chromeVersion)) {
            String majorVersion = StringUtils.divide(chromeVersion, '.')[0];
            reporting.info("Major Version: "+majorVersion);
            String version = HttpUtil.urlContent("https://chromedriver.storage.googleapis.com/LATEST_RELEASE_" + majorVersion);
            reporting.info("Latest Release: "+version);
            reporting.info("OS is: "+getOsName());
            reporting.info("Downloading into: "+newChromeDriver);
            HttpUtil.download("https://chromedriver.storage.googleapis.com/" + version + "/chromedriver_" + getOsName() + ".zip",
                    new File(newChromeDriver));
        }
        return newChromeDriver;
    }

    private static String getOsName(){
        switch (OSUtil.getOS()) {
            case WINDOWS: return "win32";
            case MAC: return "mac64";
            case LINUX: return "linux64";
            case SOLARIS:
            case OTHER:
            default:
                throw new IllegalStateException("Your OS is not supported!");
        }
    }
}