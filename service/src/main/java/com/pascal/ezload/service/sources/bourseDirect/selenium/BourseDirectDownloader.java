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
package com.pascal.ezload.service.sources.bourseDirect.selenium;

import com.pascal.ezload.common.util.BRException;
import com.pascal.ezload.common.util.Month;
import com.pascal.ezload.service.config.EzProfil;
import com.pascal.ezload.service.config.MainSettings;
import com.pascal.ezload.service.config.SettingsManager;
import com.pascal.ezload.service.exporter.EZPortfolioProxy;
import com.pascal.ezload.common.model.EZDate;
import com.pascal.ezload.service.model.EnumEZBroker;
import com.pascal.ezload.common.sources.Reporting;
import com.pascal.ezload.service.sources.bourseDirect.BourseDirectEZAccountDeclaration;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.Select;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;


public class BourseDirectDownloader extends BourseDirectSeleniumHelper {

    public static final String BOURSE_DIRECT_FILE_PREFIX = "boursedirect-";
    public static final String BOURSE_DIRECT_PDF_SUFFIX = ".pdf";
    public static final String BOURSE_DIRECT_JSON_SUFFIX = ".json";

    public BourseDirectDownloader(Reporting reporting, SettingsManager settingsManager, MainSettings mainSettings, EzProfil ezProfil) {
        super(reporting, settingsManager, mainSettings, ezProfil);
    }

    public static Predicate<File> fileFilter(){
        return file -> file.getName().startsWith(BourseDirectDownloader.BOURSE_DIRECT_FILE_PREFIX)
                && (
                        file.getName().endsWith(BourseDirectDownloader.BOURSE_DIRECT_PDF_SUFFIX)
                    ||  file.getName().endsWith(BourseDirectDownloader.BOURSE_DIRECT_JSON_SUFFIX)
                   )
                && getDateFromFilePath(file.getName()) != null;
    }

    public static Predicate<File> dirFilter(EzProfil ezProfil){
        return dir ->
                ezProfil.getBourseDirect().getAccounts()
                        .stream()
                        .anyMatch(acc -> dir.getAbsolutePath().contains(acc.getName()));
    }

    public void start(String currentChromeVersion, EZPortfolioProxy ezPortfolioProxy) throws IOException {
        try(Reporting ignored = reporting.pushSection("Downloading BourseDirect Reports...")) {

            try {
                downloadUpdates(currentChromeVersion, ezPortfolioProxy);
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

    private void downloadUpdates(String currentChromeVersion, EZPortfolioProxy ezPortfolioProxy) throws Exception {
        login(currentChromeVersion);

        goToAvisOperes();

        EnumEZBroker courtier = EnumEZBroker.BourseDirect;
        for (BourseDirectEZAccountDeclaration account : bourseDirectSettings.getAccounts()) {
            if (account.isActive() && !account.getNumber().equalsIgnoreCase("N/A")) {
                try (Reporting ignored = reporting.pushSection("PDF Extraction for account: " + account.getName())) {

                    Optional<EZDate> fromDateOpt = ezPortfolioProxy.getLastOperationDate(EnumEZBroker.BourseDirect, account);
                    if (fromDateOpt.isPresent()) {
                        reporting.info("Dernière date chargé dans EZPortfolio par EZLoad pour le compte " + courtier.getEzPortfolioName() + ":" + account.getName() + "=> " + fromDateOpt.get().toEzPortoflioDate());
                    } else {
                        reporting.info("Aucune opération chargé par EZLoad dans le compte " + courtier.getEzPortfolioName() + ":" + account.getName());
                    }

                    LocalDate currentdate = LocalDate.now();
                    EZDate fromDate;
                    // BourseDirect fournit le relevé jusqu'a un an en arriere
                    fromDate = fromDateOpt.orElse(new EZDate(currentdate.getYear() - 1, currentdate.getMonthValue(), 1));

                    String cptIndex = selectAccount(account);

                    goToAvisOperes(cptIndex, new Month(fromDate));

                    Month dateFromPage = extractDateFromPage();
                    // just a check to be sure we are on the correct page
                    if (dateFromPage.getYear() != fromDate.getYear() || dateFromPage.getMonth() != fromDate.getMonth()) {
                        reporting.info("The date of the download page: " + dateFromPage + " is not the expected one: " + fromDate+" => restart from the beginning");
                        // BourseDirect fournit le relevé jusqu'a un an en arriere
                        fromDate = new EZDate(currentdate.getYear() - 1, currentdate.getMonthValue(), 1);
                        goToAvisOperes(cptIndex, new Month(fromDate));
                        dateFromPage = extractDateFromPage();
                    }

                    Month monthToDownload = dateFromPage;
                    do {
                        extractMonthActivities(settingsManager.getDownloadDir(mainSettings.getActiveEzProfilName(), EnumEZBroker.BourseDirect), account, cptIndex, monthToDownload);
                        monthToDownload = clickMoisSuivant();
                    }
                    while (monthToDownload != null);

                    reporting.info("Extraction done for account: " + account.getName());
                }
            }
        }
    }

    // return true if we must stop the download of the previous monthes
    // return false if we should continue
    private void extractMonthActivities(String downloadDir, BourseDirectEZAccountDeclaration account, String cptIndex, Month month) throws IOException {
        reporting.info("Checking all days to find files to download...");
        List<WebElement> allDayActivities = getAllElements("div[@id='avis']//a", "linkE");

        List<EZDate> allDays = allDayActivities.stream()
                .map(webElt -> new EZDate(month.getYear(), month.getMonth(), Integer.parseInt(webElt.getText())))
                .sorted(Comparator.comparing(d -> d.toDate('/')))
                .collect(Collectors.toList());

        for (EZDate d : allDays) {
            if (!new File(getNewFilename(downloadDir, account, d)).exists()) { // si le fichier pdf n'existe pas deja
                downloadPdf(downloadDir, account, cptIndex, d);
            }
        }
    }


    private String selectAccount(BourseDirectEZAccountDeclaration account) {
        WebElement legacy_iframe = getDriver().findElement(By.className("iframe-legacy-resized"));
        getDriver().switchTo().frame(legacy_iframe);
        WebElement option = findByContainsText("option", account.getNumber());
        reporting.info("Account "+account.getNumber()+" found");
        String cptIndex = option.getAttribute("value");
        Select select = new Select (getParent(option));
        select.selectByValue(cptIndex);
        getDriver().switchTo().defaultContent();
        return cptIndex;
    }

    private Month clickMoisPrecedent() {
        // Mois Précedent
        // si proleme, changer l'url plutot que de cliquer sur precedent avec goToMonth
        String currentUrl = getDriver().getCurrentUrl();
        click(findByHref("javascript:MoisPrecedent"));
        try{
            waitUrlIsNot(currentUrl);
        }
        catch(TimeoutException te){
            return null;// impossible d'aller plus loin dans le passé
        }
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
        try {
            waitUrlIsNot(currentUrl);
        }
        catch(TimeoutException te){
            return null; // impossible d'aller dans le futur
        }
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


    private void downloadPdf(String downloadDir, BourseDirectEZAccountDeclaration account, String cptIndex, EZDate d) throws IOException {
        String month = EZDate.leadingZero(d.getMonth());
        String day = EZDate.leadingZero(d.getDay());
        String downloadUrl = "https://www.boursedirect.fr/priv/releveOpe.php?nc="+cptIndex+"&type=RO&year="+d.getYear()+"&month="+month+"&day="+day+"&trash=/avis.pdf&pdf=1";

        String newFile = getNewFilename(downloadDir, account, d);
        reporting.info("Download pdf file from URL: "+downloadUrl);
        reporting.info("Creating file "+newFile);
        download(downloadUrl, newFile);
    }


    private String getNewFilename(String downloadDir, BourseDirectEZAccountDeclaration account, EZDate d) throws IOException {
        String month = EZDate.leadingZero(d.getMonth());
        String day = EZDate.leadingZero(d.getDay());
        return downloadDir
                + File.separator + account.getName() // if this change, review the method  getAccountNameFromPdfFilePath
                + File.separator + d.getYear()
                + File.separator +
                BOURSE_DIRECT_FILE_PREFIX +d.getYear()+"-"+month+"-"+day+BOURSE_DIRECT_PDF_SUFFIX; // if this change, review the method getDateFromPdfFilePath
    }

    // @TODO cette fonction devrait etre revu et deplacer dans une fonction plus générale, non lié au Profider.
    public static EZDate getDateFromFilePath(String filePath) {
        try {
            String s = new File(filePath).getName().substring(BOURSE_DIRECT_FILE_PREFIX.length());

            int suffixLength = filePath.endsWith(BOURSE_DIRECT_PDF_SUFFIX) ? BOURSE_DIRECT_PDF_SUFFIX.length() : BOURSE_DIRECT_JSON_SUFFIX.length();
            String s2 = s.substring(0, s.length() - suffixLength);
            String[] elem = s2.split("-");
            EZDate date = new EZDate(Integer.parseInt(elem[0]), Integer.parseInt(elem[1]), Integer.parseInt(elem[2]));
            if (date.isValid()) {
                return date;
            }
        }
        catch(Throwable t) {
            return null;
        }
        return null;
    }

    public BourseDirectEZAccountDeclaration getAccountFromFilePath(String pdfFile){
        String[] section = pdfFile.replace('\\', '/') // for windows
                                    .split("/");
        String account = section.length >=3 ? section[section.length-3] : null; // the pdfFile is: /path/AccountDeclarationName/Year/file.pdf => extract the AccountDeclarationName
        return  bourseDirectSettings.getAccounts()
                .stream()
                .filter(acc -> acc.getName().equals(account))
                .findFirst()
                .orElse(null);
    }


}
