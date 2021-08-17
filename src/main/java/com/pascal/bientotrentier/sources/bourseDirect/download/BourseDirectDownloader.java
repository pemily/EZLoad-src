package com.pascal.bientotrentier.sources.bourseDirect.download;

import com.pascal.bientotrentier.MainSettings;
import com.pascal.bientotrentier.sources.Reporting;
import com.pascal.bientotrentier.sources.bourseDirect.BourseDirectAccount;
import com.pascal.bientotrentier.sources.bourseDirect.BourseDirectSettings;
import com.pascal.bientotrentier.util.BRException;
import com.pascal.bientotrentier.util.Day;
import com.pascal.bientotrentier.util.Month;
import com.pascal.bientotrentier.util.SeleniumUtil;
import org.openqa.selenium.InvalidArgumentException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;


public class BourseDirectDownloader extends SeleniumUtil {

    private final MainSettings mainSettings;
    private final BourseDirectSettings bourseDirectSettings;
    private final Reporting reporting;

    public BourseDirectDownloader(Reporting reporting, MainSettings mainSettings) {
        this.mainSettings = mainSettings;
        this.reporting = reporting;
        this.bourseDirectSettings = mainSettings.getBourseDirect();
    }

    public void start() {
        WebDriver driver = null;
        try {
            driver = getLocalhostWebDriver(mainSettings);
            init(driver, mainSettings.getBourseDirect().getExtractor().getDefaultTimeout(), mainSettings.getChrome().getDownloadDir());
            downloadUpdates();
        }
        catch(Exception e) {
            if (e instanceof InvalidArgumentException)
                reporting.error("Impossible de controller Chrome. Verifiez qu'il n'est pas déjà ouvert, si c'est la cas fermez toutes les fenetres et recommencez");
            else
                reporting.error(e);

            if (driver != null)
                driver.quit();
        }
    }

    private void downloadUpdates(){
        reporting.pushSection("Downloading BourseDirect Reports...");
        try {
            get("https://www.boursedirect.fr/fr/login");

            if (bourseDirectSettings.getExtractor().isAutoLogin())
                findById("bd_auth_login_type_submit").click();

            waitUrlIsNot("https://www.boursedirect.fr/fr/login");

            get("https://www.boursedirect.fr/priv/avis-operes.php");

            for (BourseDirectAccount account : bourseDirectSettings.getAccounts()) {
                reporting.info("Extraction started for account: " + account.getName());

                selectAccount(account);

                for (Month nextMonth = extractDateFromPage(); nextMonth != null; nextMonth = clickMoisPrecedent()) {
                    if (extractMonthActivities(account, nextMonth))
                        break;
                }

                reporting.info("Extraction done for account: " + account.getName());
            }
        }
        finally {
            reporting.popSection();
        }
    }

    // return true if we must stop the download of the previous monthes
    // return false if we should continue
    private boolean extractMonthActivities(BourseDirectAccount account, Month month) {
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

    private void selectAccount(BourseDirectAccount account) {
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


    private void downloadPdf(BourseDirectAccount account, Day d) {
        String month = leadingZero(d.getMonth());
        String day = leadingZero(d.getDay());
        String downloadUrl = "https://www.boursedirect.fr/priv/releveOpe.php?nc=3&type=RO&year="+d.getYear()+"&month="+month+"&day="+day+"&trash=/avis.pdf&pdf=1";

        String newFile = getNewFilename(account, d);
        reporting.info("Downloading file "+newFile);
        download(downloadUrl, newFile);
    }

    private String getNewFilename(BourseDirectAccount account, Day d){
        String month = leadingZero(d.getMonth());
        String day = leadingZero(d.getDay());
        return bourseDirectSettings.getPdfOutputDir() + File.separator + account.getName() + File.separator + d.getYear() + File.separator + "boursedirect-"+d.getYear()+"-"+month+"-"+day+".pdf";
    }

    private String leadingZero(int n){
        return n < 10 ? "0"+n : n+"";
    }
}
