/**
 * ezService - EZLoad an automatic loader for EZPortfolio
 * Copyright © 2021 EMILY Pascal (pascal.emily@gmail.com)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
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
        String dir = new File(currentChromeDriverPath).getParent();
        new File(dir).mkdirs();
        String newChromeDriver = currentChromeDriverPath;

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
