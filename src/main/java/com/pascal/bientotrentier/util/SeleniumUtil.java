package com.pascal.bientotrentier.util;

import com.pascal.bientotrentier.MainSettings;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
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

public class SeleniumUtil {
    private static Logger logger = Logger.getLogger(SeleniumUtil.class);

    private WebDriver driver;
    private String chromeDownloadDir;
    private int defaultTimeout;

    public void init(WebDriver driver, int defaultTimeout, String chromeDownloadDir){
        this.driver = driver;
        this.chromeDownloadDir = chromeDownloadDir;
        this.defaultTimeout = defaultTimeout;
        //Specifiying pageLoadTimeout and Implicit wait
        driver.manage().timeouts().pageLoadTimeout(defaultTimeout, TimeUnit.SECONDS);
        driver.manage().timeouts().implicitlyWait(defaultTimeout, TimeUnit.SECONDS);
    }


    public static WebDriver getLocalhostWebDriver(MainSettings mainSettings) throws IOException {
        logger.info("Chrome driver path: " + mainSettings.getChrome().getDriverPath());
        //Setting system properties of ChromeDriver
        System.setProperty("webdriver.chrome.driver", mainSettings.getChrome().getDriverPath());

        //Creating an object of ChromeDriver
        ChromeOptions options = new ChromeOptions();
        options.addArguments("chrome.switches", "--disable-extensions");

        logger.info("Chrome user data dir: " + mainSettings.getChrome().getUserDataDir());
        logger.info("Chrome Profile: " + mainSettings.getChrome().getProfile());
        options.addArguments("user-data-dir="+ mainSettings.getChrome().getUserDataDir());
        if (mainSettings.getChrome().getProfile() != null && !StringUtils.isBlank(mainSettings.getChrome().getProfile()))
            options.addArguments("profile-directory="+ mainSettings.getChrome().getProfile());

        Map<String, Object> prefs = new HashMap<>();
        if (mainSettings.getChrome().getDownloadDir() == null || StringUtils.isBlank(mainSettings.getChrome().getDownloadDir())){
            mainSettings.getChrome().setDownloadDir(Files.createTempDirectory("BientotRentier-Tmp").toFile().getAbsolutePath());
        }
        prefs.put("download.default_directory", mainSettings.getChrome().getDownloadDir());
        prefs.put("download.prompt_for_download", false);
        options.setExperimentalOption("prefs", prefs);


        WebDriver driver = new ChromeDriver(options);

        return driver;
    }

    public WebDriver getDriver(){
        return driver;
    }

    public void get(String url) {
        logger.info("Get Page "+url);

        //launching the specified URL
        driver.get(url);
    }

    public List<WebElement> getAllElements(String element, String className){
        return driver.findElements(By.xpath("//"+element+"[@class='"+className+"']"));
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
        logger.info("Find by href: "+hrefSubstring);
        return driver.findElement(By.xpath("//a[contains(@href, '"+hrefSubstring+"')]"));
    }

    public void waitUrlIsNot(String url){
        logger.info("Waiting that url is no more: "+url);
        new WebDriverWait(driver, defaultTimeout).until(ExpectedConditions.not(ExpectedConditions.urlToBe(url)));
    }

    public WebElement findById(String id){
        logger.info("Find by Id: "+id);
        return driver.findElement(By.id(id));
    }

    public void click(WebElement element){
        logger.info("Click");
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

        sleep(3);
        // search for the name of the new file in the downloaded directory (ignore the tmp file)
        String downloadFilename = null;
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
            sleep(1);
        }
        while(downloadFilename == null);

        sleep(1);

        File downloadedFile = new File(downloadDir + File.separator + downloadFilename);
        File destFile = new File(outputFile);
        destFile.getParentFile().mkdirs();
        if (!downloadedFile.renameTo(destFile)){
            throw new BRException("Problem when moving "+ downloadedFile.getAbsolutePath()+" to "+destFile.getAbsolutePath());
        }

    }

    public void sleep(int seconds) {
        try {
            Thread.sleep(seconds*1000);
        } catch (InterruptedException e) {
        }
    }

}
