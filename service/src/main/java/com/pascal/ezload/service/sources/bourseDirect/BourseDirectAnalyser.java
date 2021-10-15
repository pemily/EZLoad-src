package com.pascal.ezload.service.sources.bourseDirect;

import com.pascal.ezload.service.config.SettingsManager;
import com.pascal.ezload.service.exporter.EZPortfolioProxy;
import com.pascal.ezload.service.model.EZAccountDeclaration;
import com.pascal.ezload.service.config.MainSettings;
import com.pascal.ezload.service.model.EZDate;
import com.pascal.ezload.service.model.EZModel;
import com.pascal.ezload.service.model.EnumEZBroker;
import com.pascal.ezload.service.sources.FileProcessor;
import com.pascal.ezload.service.sources.Reporting;
import com.pascal.ezload.service.sources.bourseDirect.selenium.BourseDirectDownloader;
import com.pascal.ezload.service.sources.bourseDirect.transform.BourseDirect2BRModel;
import com.pascal.ezload.service.sources.bourseDirect.transform.BourseDirectModelChecker;
import com.pascal.ezload.service.sources.bourseDirect.transform.BourseDirectPdfExtractor;
import com.pascal.ezload.service.sources.bourseDirect.transform.BourseDirectText2Model;
import com.pascal.ezload.service.sources.bourseDirect.transform.model.BourseDirectModel;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class BourseDirectAnalyser {

    private final MainSettings mainSettings;
    private final static int UNKNOWN_VERSION = -1;

    public BourseDirectAnalyser(MainSettings mainSettings) {
        this.mainSettings = mainSettings;
    }

    public List<EZModel> start(final Reporting reporting, EZPortfolioProxy ezPortfolioProxy) throws Exception {
        BourseDirectDownloader bourseDirectDownloader = new BourseDirectDownloader(reporting, mainSettings);
        try(Reporting rep = reporting.pushSection("Chargement EZPortfolio")) {
            ezPortfolioProxy.load();
        }
        try(Reporting ignored = reporting.pushSection("Analyse des fichiers téléchargés...")) {
            reporting.info("Répertoire des fichiers à analyser: "+SettingsManager.getDownloadDir(mainSettings, EnumEZBroker.BourseDirect));
              return new FileProcessor(SettingsManager.getDownloadDir(mainSettings, EnumEZBroker.BourseDirect),
                                        BourseDirectDownloader.dirFilter(mainSettings), BourseDirectDownloader.fileFilter())
                    .mapFile(pdfFilePath -> {
                        EZAccountDeclaration account = bourseDirectDownloader.getAccountFromPdfFilePath(pdfFilePath);
                        EZDate pdfDate = BourseDirectDownloader.getDateFromPdfFilePath(pdfFilePath);

                        if (account != null
                                && pdfDate != null
                                && !ezPortfolioProxy.isFileAlreadyLoaded(EnumEZBroker.BourseDirect, account, pdfDate)
                                && account.isActive()) {
                            // if the pdf file is valid, and is not yet processed
                            // start its analysis
                            return start(reporting, account, pdfFilePath);
                        }
                        return null;
                    });
        }
    }

    public EZModel start(Reporting reporting, EZAccountDeclaration EZAccountDeclaration, String pdfFilePath) {
        try(Reporting ignored = reporting.pushSection((rep1, fileLinkCreator) -> rep1.escape("Fichier en cours d'analyse: ") + fileLinkCreator.createSourceLink(rep1, pdfFilePath))){

            List<String> errors = new LinkedList<>();
            try {
                String pdfText = new BourseDirectPdfExtractor(reporting).getText(pdfFilePath);

                BourseDirectModel model = new BourseDirectText2Model(reporting).toModel(pdfText);

                errors = new BourseDirectModelChecker(reporting, model).getErrors();

                if (errors.size() == 0) {
                    return new BourseDirect2BRModel(mainSettings, reporting).create(pdfFilePath, EZAccountDeclaration, model);
                }
            }
            catch(Exception e){
                reporting.error("Erreur pendant l'analyze", e);
                errors.add("Erreur pendant l'analyze: "+e.getMessage());
            }
            EZModel ezModel = new EZModel(EnumEZBroker.BourseDirect, UNKNOWN_VERSION, getSourceRef(mainSettings, pdfFilePath));
            ezModel.setErrors(errors);
            return ezModel;
        }
    }

    public static String getSourceRef(MainSettings mainSettings, String pdfFilePath) {
        String file = pdfFilePath.substring(mainSettings.getEzLoad().getDownloadDir().length()).replace('\\', '/');
        if (file.startsWith("/")) file = file.substring(1);
        return file;
    }
}
