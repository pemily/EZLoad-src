package com.pascal.bientotrentier.sources.bourseDirect.download;

import com.pascal.bientotrentier.MainSettings;
import com.pascal.bientotrentier.SettingsManager;
import com.pascal.bientotrentier.exporter.ezPortfolio.EZPortfolio;
import com.pascal.bientotrentier.model.BRDate;
import com.pascal.bientotrentier.model.EnumBRCourtier;
import com.pascal.bientotrentier.sources.Reporting;
import com.pascal.bientotrentier.sources.bourseDirect.BourseDirectAccountDeclaration;
import com.pascal.bientotrentier.sources.bourseDirect.BourseDirectSettings;
import com.pascal.bientotrentier.util.*;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.Select;

import java.io.File;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;


public class BourseDirectDownloader extends BaseSelenium {

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

    public void start(EZPortfolio ezPortfolio) {
        try(Reporting ignored = reporting.pushSection("Downloading BourseDirect Reports...")) {

            try {
                init(mainSettings.getChrome(), mainSettings.getChrome().getDefaultTimeout());
                downloadUpdates(ezPortfolio);
            } catch (Exception e) {
                if (e instanceof InvalidArgumentException)
                    reporting.error("Impossible de controller Chrome. Verifiez qu'il n'est pas déjà ouvert, si c'est la cas fermez toutes les fenetres et recommencez");
                else
                    reporting.error(e);
            }
            finally {
                closeChrome();
            }
        }
    }

    private void downloadUpdates(EZPortfolio ezPortfolio) throws Exception {
        get("https://www.boursedirect.fr/fr/login");

        try {
            // reject cookies
            findById("didomi-notice-disagree-button").click();
            Sleep.wait(1);
            reporting.info("Cookies Rejected");
        }
        catch (NoSuchElementException ignored){}

        WebElement login = findById("bd_auth_login_type_login");
        WebElement password = findById("bd_auth_login_type_password");

        MainSettings.AuthInfo authInfo = SettingsManager.getAuthManager(mainSettings).getAuthInfo(EnumBRCourtier.BourseDirect);
        if (StringUtils.isBlank(login.getText())){
            login.sendKeys(authInfo.getUsername());
            password.sendKeys(authInfo.getPassword());
            Sleep.wait(1);
            findById("bd_auth_login_type_submit").click();
        }
        else if (!StringUtils.isBlank(login.getText())){
            findById("bd_auth_login_type_submit").click();
        }
        else reporting.info("Please Enter your login/password then click on Connect");

        boolean connected = false;
        do {
            try {
                waitUrlIsNot("https://www.boursedirect.fr/fr/login");
                connected =  true;
            } catch (TimeoutException t) {
                Sleep.wait(1);
            }
        }while(!connected);

        get("https://www.boursedirect.fr/priv/avis-operes.php");

        for (BourseDirectAccountDeclaration account : bourseDirectSettings.getAccounts()) {
            try(Reporting ignored = reporting.pushSection("PDF Extraction for account: " + account.getName())) {

                Optional<BRDate> fromDateOpt = ezPortfolio.getMesOperations().getLastOperationDate(EnumBRCourtier.BourseDirect, account);
                BRDate fromDate;
                if (!fromDateOpt.isPresent()) {
                    // BourseDirect fournit le relevé jusqu'a un an en arriere
                    LocalDate currentdate = LocalDate.now();
                    fromDate = new BRDate(currentdate.getYear() - 1, currentdate.getMonthValue(), 1);
                } else fromDate = fromDateOpt.get();

                String cptIndex = selectAccount(account);

                goToMonth(cptIndex, new Month(fromDate));

                Month dateFromPage = extractDateFromPage();
                // just a check to be sure we are on the correct page
                if (dateFromPage.getYear() != fromDate.getYear() || dateFromPage.getMonth() != fromDate.getMonth()) {
                    throw new BRException("The date of the download page: " + dateFromPage + " is not the expected one: " + fromDate);
                }
                /*
                // go to the oldest page for this account
                Month dateFromPage = extractDateFromPage();
                while(dateFromPage.getYear() != fromDate.getYear() || dateFromPage.getMonth() != fromDate.getMonth()) {
                    dateFromPage = clickMoisPrecedent();
                    if (dateFromPage == null) throw new BRException("Impossible to find the first page to start dowload from date: "+fromDate);
                }
                */

                Month monthToDownload = dateFromPage;
                do {
                    extractMonthActivities(account, cptIndex, monthToDownload);
                    monthToDownload = clickMoisSuivant();
                }
                while (monthToDownload != null);

                reporting.info("Extraction done for account: " + account.getName());
            }
        }
    }

    // return true if we must stop the download of the previous monthes
    // return false if we should continue
    private void extractMonthActivities(BourseDirectAccountDeclaration account, String cptIndex, Month month) {
        reporting.info("Checking all days to find files to download...");
        List<WebElement> allDayActivities = getAllElements("a", "linkE");

        List<BRDate> allDays = allDayActivities.stream()
                .map(webElt -> new BRDate(month.getYear(), month.getMonth(), Integer.parseInt(webElt.getText())))
                .sorted(Comparator.comparing(d -> d.toDate('/')))
                .collect(Collectors.toList());

        for (BRDate d : allDays) {
            if (!new File(getNewFilename(account, d)).exists()) { // si le fichier pdf n'existe pas deja
                downloadPdf(account, cptIndex, d);
            }
        }
    }

    private void goToMonth(String cptIndex, Month month){
        get("https://www.boursedirect.fr/priv/avis-operes.php?tr=RO&nc="+cptIndex+"&month="+ month.getMonth()+"&year="+ month.getYear());
    }

    private String selectAccount(BourseDirectAccountDeclaration account) {
        WebElement option = findByContainsText("option", account.getNumber());
        reporting.info("Account "+account.getNumber()+" found");
        String cptIndex = option.getAttribute("value");
        Select select = new Select (getParent(option));
        select.selectByValue(cptIndex);
        return cptIndex;
    }

    private Month clickMoisPrecedent() {
        // Mois Précedent
        // si proleme, changer l'url plutot que de cliquer sur precedent avec goToMonth
        String currentUrl = getDriver().getCurrentUrl();
        click(findByHref("javascript:MoisPrecedent"));
        waitUrlIsNot(currentUrl);
        Month dateFromPage = extractDateFromPage();
        Month dateFromUrl = extractMonthYearFromUrl();
        if (dateFromPage.getMonth() != dateFromUrl.getMonth() || dateFromPage.getYear() != dateFromUrl.getYear())
            return null; // impossible d'aller plus loin dans le passé

        return dateFromPage;
    }

    private Month clickMoisSuivant() {
        // Mois Suivant
        // si proleme, changer l'url plutot que de cliquer sur suivant avec goToMonth
        String currentUrl = getDriver().getCurrentUrl();
        click(findByHref("javascript:MoisSuivant"));
        waitUrlIsNot(currentUrl);
        Month dateFromPage = extractDateFromPage();
        Month dateFromUrl = extractMonthYearFromUrl();
        if (dateFromPage.getMonth() != dateFromUrl.getMonth() || dateFromPage.getYear() != dateFromUrl.getYear())
            return null; // impossible d'aller dans le futur

        return dateFromPage;
    }


    private Month extractDateFromPage(){
        WebElement elem = findByHref("javascript:AnneePrecedente");
        String ref = elem.getAttribute("href"); // href="javascript:AnneePrecedente('RO',3,08,2021)"
        String s[] = ref.split(",");
        String monthStr = s[2];
        String yearStr = s[3].substring(0, 4);
        return new Month(Integer.parseInt(yearStr), Integer.parseInt(monthStr));
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

        return new Month(Integer.parseInt(year), Integer.parseInt(monthStr));
    }


    private void downloadPdf(BourseDirectAccountDeclaration account, String cptIndex, BRDate d) {
        String month = BRDate.leadingZero(d.getMonth());
        String day = BRDate.leadingZero(d.getDay());
        String downloadUrl = "https://www.boursedirect.fr/priv/releveOpe.php?nc="+cptIndex+"&type=RO&year="+d.getYear()+"&month="+month+"&day="+day+"&trash=/avis.pdf&pdf=1";

        String newFile = getNewFilename(account, d);
        reporting.info("Download pdf file from URL: "+downloadUrl);
        reporting.info("Creating file "+newFile);
        download(downloadUrl, newFile);
    }


    private String getNewFilename(BourseDirectAccountDeclaration account, BRDate d){
        String month = BRDate.leadingZero(d.getMonth());
        String day = BRDate.leadingZero(d.getDay());
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
