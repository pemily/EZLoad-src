package com.pascal.bientotrentier.util;

import com.pascal.bientotrentier.config.MainSettings;
import com.pascal.bientotrentier.sources.Reporting;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class BaseSelenium {

    private static final int DOWNLOAD_TIMEOUT_IN_SECONDS = 30;
    protected final Reporting reporting;
    private WebDriver driver;
    private int defaultTimeoutInSec;
    private String chromeDownloadDir;

    protected BaseSelenium(Reporting reporting){
        this.reporting = reporting;
    }

    public void init(MainSettings.ChromeSettings chromeSettings, int defaultTimeoutInSec) throws IOException {
        this.defaultTimeoutInSec = defaultTimeoutInSec;
        this.chromeDownloadDir = Files.createTempDirectory("BientotRentier-Tmp").toFile().getAbsolutePath();

        reporting.info("Chrome driver path: " + chromeSettings.getDriverPath());
        //Setting system properties of ChromeDriver
        System.setProperty("webdriver.chrome.driver", chromeSettings.getDriverPath());

        //Creating an object of ChromeDriver
        ChromeOptions options = new ChromeOptions();

        reporting.info("Chrome user data dir: " + chromeSettings.getUserDataDir());
        reporting.info("Chrome download dir: " + chromeDownloadDir);

        options.addArguments("user-data-dir="+ chromeSettings.getUserDataDir());
        options.addArguments("profile-directory=Default"); // only Default works to change the download.default_directory
  //      options.addArguments("--enable-automation"); // sinon les creds service mache pas

        Map<String, Object> prefs = new HashMap<>();
        prefs.put("download.default_directory", chromeDownloadDir); //ok
        prefs.put("download.prompt_for_download", false); // ok
        prefs.put("directory_upgrade", true); // ok
//        prefs.put("credentials_enable_service", true);
//        prefs.put("profile.password_manager_enabled", true);
        prefs.put("profile.name", "BientotRentier");
        prefs.put("profile.using_default_name", false);
/*        prefs.put("autofill.enabled", true);
        prefs.put("autofill.profile_enabled", true);

        prefs.put("browser.clear_data.cookies_basic", false);
        prefs.put("browser.clear_data.cookies", false);
        prefs.put("browser.clear_data.passwords", false);
        prefs.put("profile.content_settings.enable_quiet_permission_ui_enabling_method.notification", 1);
        prefs.put("profile.password_account_storage_exists", true);
        prefs.put("was_auto_sign_in_first_run_experience_shown", true);
*/
        prefs.put("useAutomationExtension", false);  // desactive la baniere: "chrome is controller by an automated test software"

        // prefs.put("deleteDataPostSession", false);

//        prefs.put("profile.default_content_settings.popups", 1);

        options.setExperimentalOption("prefs", prefs);

        driver = new ChromeDriver(options);
        //Specifiying pageLoadTimeout and Implicit wait
        driver.manage().timeouts().pageLoadTimeout(defaultTimeoutInSec, TimeUnit.SECONDS);
        driver.manage().timeouts().implicitlyWait(defaultTimeoutInSec, TimeUnit.SECONDS);
    }

    protected void closeChrome(){
        if (driver != null) driver.close();
    }

    public WebDriver getDriver(){
        return driver;
    }

    public void get(String url) {
        reporting.info("Get Page "+url);

        //launching the specified URL
        driver.get(url);
    }

    public List<WebElement> getAllElements(String element, String className){
        return driver.findElements(By.xpath("//"+element+"[@class='"+className+"']"));
    }

    public List<WebElement> getAllSubElements(WebElement elemnt, String className){
        return elemnt.findElements(new By.ByClassName(className));
    }

    public WebElement findByContainsText(String htmlElement, String text){
        return driver.findElement(By.xpath("//"+htmlElement+"[contains(text(), '" + text + "')]"));
    }

    public WebElement getParent(WebElement childElement){
        return childElement.findElement(By.xpath("./.."));
    }

    public WebElement nextSibling(WebElement element, int n){
        WebElement elem = element;
        for (int i = 0; i < n; i++){
            elem = elem.findElement(By.xpath("./next-sibling"));
        }
        return elem;
    }

    public List<WebElement> getChildren(WebElement element, String tag) {
        return element.findElements(By.tagName(tag));
    }

    public WebElement findByHref(String hrefSubstring){
        reporting.info("Find by href: "+hrefSubstring);
        return driver.findElement(By.xpath("//a[contains(@href, '"+hrefSubstring+"')]"));
    }

    public void waitUrlIsNot(String url){
        reporting.info("Waiting that url is no more: "+url);
        new WebDriverWait(driver, defaultTimeoutInSec).until(ExpectedConditions.not(ExpectedConditions.urlToBe(url)));
    }

    public WebElement findById(String id){
        reporting.info("Find by Id: "+id);
        return driver.findElement(By.id(id));
    }

    public void click(WebElement element){
        reporting.info("Click");
        if (!element.isEnabled())
            throw new SeleniumError("The element is not clickable");
        element.click();
    }


    public static class SeleniumError extends RuntimeException{
        SeleniumError(String msg){
            super(msg);
        }
    }

    public void download(String downloadUrl, String outputFile) {
        File downloadDir = new File(chromeDownloadDir);
        Set<String> oldFiles = new HashSet<>(Arrays.asList(downloadDir.list()));
        getDriver().get(downloadUrl);

        Sleep.wait(3);
        // search for the name of the new file in the downloaded directory (ignore the tmp file)
        String downloadFilename = null;
        long time = Sleep.time();
        do{
            Set<String> newFiles = new HashSet<>(Arrays.asList(downloadDir.list()));
            if (newFiles.size() > oldFiles.size()) {
                newFiles.removeAll(oldFiles);
                if (newFiles.size() != 1)
                    throw new BRException("Multiple files are downloaded at the same time!!! => "+newFiles.toString());

                downloadFilename = newFiles.stream().findFirst().get();
                if (downloadFilename.endsWith(".tmp") || downloadFilename.endsWith(".crdownload"))
                    downloadFilename = null;
            }
            Sleep.wait(1);
        }
        while(downloadFilename == null && Sleep.isBelow(time, DOWNLOAD_TIMEOUT_IN_SECONDS)); // seconds max to download

        if (downloadFilename == null) throw new BRException("Not able to download file: "+downloadUrl);

        Sleep.wait(1);

        File downloadedFile = new File(downloadDir + File.separator + downloadFilename);
        File destFile = new File(outputFile);
        destFile.getParentFile().mkdirs();
        if (!downloadedFile.renameTo(destFile))
            throw new BRException("Problem when moving "+ downloadedFile.getAbsolutePath()+" to "+destFile.getAbsolutePath());

    }

}
