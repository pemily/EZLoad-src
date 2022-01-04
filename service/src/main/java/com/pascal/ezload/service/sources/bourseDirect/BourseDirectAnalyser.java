package com.pascal.ezload.service.sources.bourseDirect;

import com.pascal.ezload.service.config.EzProfil;
import com.pascal.ezload.service.config.MainSettings;
import com.pascal.ezload.service.config.SettingsManager;
import com.pascal.ezload.service.exporter.EZPortfolioProxy;
import com.pascal.ezload.service.model.EZAccountDeclaration;
import com.pascal.ezload.service.model.EZDate;
import com.pascal.ezload.service.model.EZModel;
import com.pascal.ezload.service.model.EnumEZBroker;
import com.pascal.ezload.service.sources.Reporting;
import com.pascal.ezload.service.sources.bourseDirect.selenium.BourseDirectDownloader;
import com.pascal.ezload.service.sources.bourseDirect.transform.BourseDirect2EZModel;
import com.pascal.ezload.service.sources.bourseDirect.transform.BourseDirectModelChecker;
import com.pascal.ezload.service.sources.bourseDirect.transform.BourseDirectText2Model;
import com.pascal.ezload.service.sources.bourseDirect.transform.model.BourseDirectModel;
import com.pascal.ezload.service.util.FileProcessor;
import com.pascal.ezload.service.util.PdfTextExtractor;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class BourseDirectAnalyser {

    private final MainSettings mainSettings;
    private final EzProfil ezProfil;
    private final static int UNKNOWN_VERSION = -1;

    public BourseDirectAnalyser(MainSettings mainSettings, EzProfil ezProfil) {
        this.mainSettings = mainSettings;
        this.ezProfil = ezProfil;
    }

    private interface IProcess<R> {
        R execute(EZAccountDeclaration account, String pdfFilePath) throws Exception;
    }

    public List<String> getFilesNotYetLoaded(Reporting reporting, EZPortfolioProxy ezPortfolioProxy) throws Exception {
        return startProcess(reporting, ezPortfolioProxy, ((account, pdfFilePath) -> getSourceRef(ezProfil, pdfFilePath)));
    }

    public List<EZModel> start(final Reporting reporting, EZPortfolioProxy ezPortfolioProxy) throws Exception {
        return startProcess(reporting, ezPortfolioProxy, (account, pdfFilePath) -> start(reporting, account, pdfFilePath));
    }

    private <R> List<R> startProcess(final Reporting reporting, EZPortfolioProxy ezPortfolioProxy, IProcess<R> process) throws Exception {
        BourseDirectDownloader bourseDirectDownloader = new BourseDirectDownloader(reporting, mainSettings, ezProfil);

        try(Reporting ignored = reporting.pushSection("Analyse des fichiers téléchargés...")) {
            String downloadDir = SettingsManager.getDownloadDir(ezProfil, EnumEZBroker.BourseDirect);
            reporting.info("Répertoire des fichiers à analyser: "+downloadDir);
              return new FileProcessor(downloadDir,
                                        BourseDirectDownloader.dirFilter(ezProfil), BourseDirectDownloader.fileFilter())
                    .mapFile(pdfFilePath -> {
                        EZAccountDeclaration account = bourseDirectDownloader.getAccountFromPdfFilePath(pdfFilePath);
                        EZDate pdfDate = BourseDirectDownloader.getDateFromPdfFilePath(pdfFilePath);

                        if (account != null
                                && pdfDate != null
                                && !ezPortfolioProxy.isFileAlreadyLoaded(EnumEZBroker.BourseDirect, account, pdfDate)
                                && account.isActive()) {
                            // if the pdf file is valid, and is not yet processed
                            // start its analysis
                            try {
                                return process.execute(account, pdfFilePath);
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                        }
                        return null;
                    });
        }
    }

    public EZModel start(Reporting reporting, EZAccountDeclaration EZAccountDeclaration, String pdfFilePath) throws IOException {
        try(Reporting ignored = reporting.pushSection((rep1, fileLinkCreator) -> rep1.escape("Fichier en cours d'analyse: ") + fileLinkCreator.createSourceLink(rep1, pdfFilePath))){

            List<String> errors = new LinkedList<>();
            try {
                PdfTextExtractor extractor = new PdfTextExtractor(reporting, pdfFilePath);
                PdfTextExtractor.Result pdfText = extractor.process();

                BourseDirectModel model = new BourseDirectText2Model(reporting).toModel(pdfText);

                errors = new BourseDirectModelChecker(reporting, model).searchErrors();

                if (errors.size() == 0) {
                    return new BourseDirect2EZModel(ezProfil, reporting).create(pdfFilePath, EZAccountDeclaration, model);
                }
            }
            catch(Exception e){
                reporting.error("Erreur pendant l'analyze", e);
                errors.add("Erreur pendant l'analyze: "+e.getMessage());
            }
            EZModel ezModel = new EZModel(EnumEZBroker.BourseDirect, UNKNOWN_VERSION, getSourceRef(ezProfil, pdfFilePath));
            ezModel.setErrors(errors);
            return ezModel;
        }
    }

    public static String getSourceRef(EzProfil ezProfil, String pdfFilePath) {
        String file = pdfFilePath.substring(ezProfil.getDownloadDir().length()).replace('\\', '/');
        if (file.startsWith("/")) file = file.substring(1);
        return file;
    }
}
