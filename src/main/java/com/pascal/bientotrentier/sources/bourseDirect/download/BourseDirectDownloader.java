package com.pascal.bientotrentier.sources.bourseDirect.download;

import com.pascal.bientotrentier.MainSettings;
import com.pascal.bientotrentier.model.BRDate;
import com.pascal.bientotrentier.sources.Reporting;
import com.pascal.bientotrentier.sources.bourseDirect.BourseDirectAccountDeclaration;
import com.pascal.bientotrentier.sources.bourseDirect.BourseDirectSettings;
import com.pascal.bientotrentier.util.*;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.InvalidArgumentException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

import java.io.File;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;


public class BourseDirectDownloader extends SeleniumUtil {

    private final MainSettings mainSettings;
    private final BourseDirectSettings bourseDirectSettings;

    public static final String BOURSE_DIRECT_PDF_PREFIX = "boursedirect-";
    public static final String BOURSE_DIRECT_PDF_SUFFIX = ".pdf";

    public BourseDirectDownloader(Reporting reporting, MainSettings mainSettings) {
        super(reporting);
        this.mainSettings = mainSettings;
        this.bourseDirectSettings = mainSettings.getBourseDirect();
    }

    public static Predicate<File> fileFilter(){
        return file -> file.getName().startsWith(BourseDirectDownloader.BOURSE_DIRECT_PDF_PREFIX)
                && file.getName().endsWith(BourseDirectDownloader.BOURSE_DIRECT_PDF_SUFFIX)
                && getDateFromPdfFilePath(file.getName()) != null;
    }

    public static Predicate<File> dirFilter(MainSettings mainSettings){
        return dir -> mainSettings.getBourseDirect().getAccounts().stream().anyMatch(acc -> dir.getAbsolutePath().contains(acc.getName()));
    }

    public void start() {
        try(Reporting rep = reporting.pushSection("Downloading BourseDirect Reports...")) {
            WebDriver driver = null;
            try {
                driver = getLocalhostWebDriver(mainSettings);
                init(driver, mainSettings.getBourseDirect().getExtractor().getDefaultTimeout(), mainSettings.getChrome().getDownloadDir());
                downloadUpdates();
            } catch (Exception e) {
                if (e instanceof InvalidArgumentException)
                    reporting.error("Impossible de controller Chrome. Verifiez qu'il n'est pas déjà ouvert, si c'est la cas fermez toutes les fenetres et recommencez");
                else
                    reporting.error(e);

                if (driver != null)
                    driver.quit();
            }
        }
    }

    private void downloadUpdates(){
        get("https://www.boursedirect.fr/fr/login");

        if (bourseDirectSettings.getExtractor().isAutoLogin()) {
            String login = findById("bd_auth_login_type_login").getText();

            if (!StringUtils.isBlank(login)) findById("bd_auth_login_type_submit").click();
            else reporting.info("Please Enter your login/password then click on Connect");
        }

        waitUrlIsNot("https://www.boursedirect.fr/fr/login", mainSettings.getBourseDirect().getExtractor().getDefaultTimeout()*2);

        get("https://www.boursedirect.fr/priv/avis-operes.php");

        for (BourseDirectAccountDeclaration account : bourseDirectSettings.getAccounts()) {
            reporting.info("Extraction started for account: " + account.getName());

            selectAccount(account);

            for (Month nextMonth = extractDateFromPage(); nextMonth != null; nextMonth = clickMoisPrecedent()) {
                if (extractMonthActivities(account, nextMonth))
                    break;
            }

            reporting.info("Extraction done for account: " + account.getName());
        }
    }

    // return true if we must stop the download of the previous monthes
    // return false if we should continue
    private boolean extractMonthActivities(BourseDirectAccountDeclaration account, Month month) {
        List<WebElement> allDayActivities = getAllElements("a", "linkE");

        List<Day> allDays = allDayActivities.stream()
                .map(webElt -> {
                    Day d = new Day();
                    d.setDay(Integer.parseInt(webElt.getText()));
                    d.setMonth(month.getMonth());
                    d.setYear(month.getYear());
                    return d;
                })
                .sorted((d1, d2) -> {
                        String dStr1 = d1.getYear()+""+leadingZero(d1.getMonth())+""+leadingZero(d1.getDay());
                        String dStr2 = d2.getYear()+""+leadingZero(d2.getMonth())+""+leadingZero(d2.getDay());
                        return dStr2.compareTo(dStr1);
                })
                .collect(Collectors.toList());

        boolean stop = false;
        for (Day d : allDays) {
            if (new File(getNewFilename(account, d)).exists()) {
                stop = true;
                break;
            }
            downloadPdf(account, d);
        }
        return stop;
    }

    private void selectAccount(BourseDirectAccountDeclaration account) {
        WebElement option = findByContainsText("option", account.getNumber());
        reporting.info("Account "+account.getNumber()+" found");
        String cptIndex = option.getAttribute("value");
        Select select = new Select (getParent(option));
        select.selectByValue(cptIndex);
    }

    private Month clickMoisPrecedent() {
        // Mois Précedent
        String currentUrl = getDriver().getCurrentUrl();
        click(findByHref("javascript:MoisPrecedent"));
        waitUrlIsNot(currentUrl);
        Month dateFromPage = extractDateFromPage();
        Month dateFromUrl = extractMonthYearFromUrl();
        if (dateFromPage.getMonth() != dateFromUrl.getMonth() || dateFromPage.getYear() != dateFromPage.getYear())
            return null; // impossible d'aller plus loin dans le passé

        return dateFromPage;
    }

    private Month extractDateFromPage(){
        WebElement elem = findByHref("javascript:AnneePrecedente");
        String ref = elem.getAttribute("href"); // href="javascript:AnneePrecedente('RO',3,08,2021)"
        String s[] = ref.split(",");
        String monthStr = s[2];
        String yearStr = s[3].substring(0, 4);
        Month d = new Month();
        d.setYear(Integer.parseInt(yearStr));
        d.setMonth(Integer.parseInt(monthStr));
        return d;
    }

    private Month extractMonthYearFromUrl(){
        String currentUrl = getDriver().getCurrentUrl();
        String[] allParamsCouple = currentUrl.split("&");
        String monthStr = null;
        String year = null;
        for(String param : allParamsCouple){
            if (param.contains("=")) {
                String[] paramValue = param.split("=");
                if (paramValue[0].equals("month")) monthStr = paramValue[1];
                else if (paramValue[0].equals("year")) year = paramValue[1];
            }
        }
        if (monthStr == null || year == null){
            reporting.error("Year and Month cannot be extracted from the url: "+currentUrl);
            throw new BRException("Cannot extract month & year from the url: "+currentUrl);
        }

        Month month = new Month();
        month.setMonth(Integer.parseInt(monthStr));
        month.setYear(Integer.parseInt(year));
        return month;
    }


    private void downloadPdf(BourseDirectAccountDeclaration account, Day d) {
        String month = leadingZero(d.getMonth());
        String day = leadingZero(d.getDay());
        String downloadUrl = "https://www.boursedirect.fr/priv/releveOpe.php?nc=3&type=RO&year="+d.getYear()+"&month="+month+"&day="+day+"&trash=/avis.pdf&pdf=1";

        String newFile = getNewFilename(account, d);
        reporting.info("Downloading file "+newFile);
        download(downloadUrl, newFile);
    }

    private String leadingZero(int n){
        return n < 10 ? "0"+n : n+"";
    }

    private String getNewFilename(BourseDirectAccountDeclaration account, Day d){
        String month = leadingZero(d.getMonth());
        String day = leadingZero(d.getDay());
        return bourseDirectSettings.getPdfOutputDir()
                + File.separator + account.getName() // if this change, review the method  getAccountNameFromPdfFilePath
                + File.separator + d.getYear()
                + File.separator +
                BOURSE_DIRECT_PDF_PREFIX+d.getYear()+"-"+month+"-"+day+BOURSE_DIRECT_PDF_SUFFIX; // if this change, review the method getDateFromPdfFilePath
    }

    public static BRDate getDateFromPdfFilePath(String pdfFilePath) {
        try {
            String s = new File(pdfFilePath).getName().substring(BOURSE_DIRECT_PDF_PREFIX.length());
            String s2 = s.substring(0, s.length() - BOURSE_DIRECT_PDF_SUFFIX.length());
            String[] elem = s2.split("-");
            BRDate date = new BRDate(Integer.parseInt(elem[0]), Integer.parseInt(elem[1]), Integer.parseInt(elem[2]));
            if (date.isValid()) {
                return date;
            }
        }
        catch(Throwable t) {
            return null;
        }
        return null;
    }

    public BourseDirectAccountDeclaration getAccountFromPdfFilePath(String pdfFile){
        String[] section = pdfFile.replace('\\', '/') // for windows
                                    .split("/");
        String account = section.length >=3 ? section[section.length-3] : null; // the pdfFile is: /path/AccountDeclarationName/Year/file.pdf => extract the AccountDeclarationName
        return  mainSettings.getBourseDirect().getAccounts()
                .stream()
                .filter(acc -> acc.getName().equals(account))
                .findFirst()
                .orElse(null);
    }


}
