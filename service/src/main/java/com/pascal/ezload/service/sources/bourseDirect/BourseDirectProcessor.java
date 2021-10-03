package com.pascal.ezload.service.sources.bourseDirect;

import com.pascal.ezload.service.config.SettingsManager;
import com.pascal.ezload.service.model.EZAccountDeclaration;
import com.pascal.ezload.service.config.MainSettings;
import com.pascal.ezload.service.exporter.ezPortfolio.EZPortfolio;
import com.pascal.ezload.service.model.EZDate;
import com.pascal.ezload.service.model.EZModel;
import com.pascal.ezload.service.model.EnumEZCourtier;
import com.pascal.ezload.service.sources.FileProcessor;
import com.pascal.ezload.service.sources.Reporting;
import com.pascal.ezload.service.sources.bourseDirect.selenium.BourseDirectDownloader;
import com.pascal.ezload.service.sources.bourseDirect.transform.BourseDirect2BRModel;
import com.pascal.ezload.service.sources.bourseDirect.transform.BourseDirectModelChecker;
import com.pascal.ezload.service.sources.bourseDirect.transform.BourseDirectPdfExtractor;
import com.pascal.ezload.service.sources.bourseDirect.transform.BourseDirectText2Model;
import com.pascal.ezload.service.sources.bourseDirect.transform.model.BourseDirectModel;
import com.pascal.ezload.service.util.BRException;
import com.pascal.ezload.service.util.BRParsingException;

import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;

public class BourseDirectProcessor {

    private final MainSettings mainSettings;

    public BourseDirectProcessor(MainSettings mainSettings) {
        this.mainSettings = mainSettings;
    }

    public List<EZModel> start(final Reporting reporting,
                               String currentChromeVersion, Consumer<String> newDriverPathSaver,
                               EZPortfolio ezPortfolio) throws IOException {


        BourseDirectDownloader bourseDirectDownloader = new BourseDirectDownloader(reporting, mainSettings);
        // Donwload the files, according to the last date retrieved from ezPortfolio
        bourseDirectDownloader.start(currentChromeVersion, newDriverPathSaver, ezPortfolio);

        try(Reporting ignored = reporting.pushSection("Analyse des fichiers téléchargés...")) {
            return new FileProcessor(SettingsManager.getDownloadDir(mainSettings, EnumEZCourtier.BourseDirect),
                    BourseDirectDownloader.dirFilter(mainSettings),
                    BourseDirectDownloader.fileFilter())
                    .forEachFiles(pdfFilePath -> {
                        EZAccountDeclaration account = bourseDirectDownloader.getAccountFromPdfFilePath(pdfFilePath);
                        EZDate pdfDate = BourseDirectDownloader.getDateFromPdfFilePath(pdfFilePath);

                        if (account != null
                                && pdfDate != null
                                && !ezPortfolio.getMesOperations().isAlreadyProcessed(EnumEZCourtier.BourseDirect, account, pdfDate)
                                && account.isActive()) {
                            // if the pdf file is valid, and is not yet processed
                            // start its analysis
                            EZModel model = start(reporting, account, pdfFilePath);
                            return model;
                        }
                        return null;
                    });
        }
    }

    public EZModel start(Reporting reporting, EZAccountDeclaration EZAccountDeclaration, String pdfFilePath) {
        try(Reporting ignored = reporting.pushSection((rep1, fileLinkCreator) -> rep1.escape("Fichier en cours d'analyse: ") + fileLinkCreator.createSourceLink(rep1, pdfFilePath))){
            String pdfText = new BourseDirectPdfExtractor(reporting).getText(pdfFilePath);

            BourseDirectModel model = new BourseDirectText2Model(reporting).toModel(pdfText);

            boolean isValid = new BourseDirectModelChecker(reporting).isValid(model);

            if (isValid){
                EZModel EZModel = new BourseDirect2BRModel(reporting).create(pdfFilePath, EZAccountDeclaration, model);
                return EZModel;
            }
        }
        catch (BRParsingException e){
            e.setFilePath(pdfFilePath);
            throw e;
        }
        catch (Throwable t) {
            BRParsingException parsingException = new BRParsingException(t);
            parsingException.setFilePath(pdfFilePath);
            throw parsingException;
        }

        throw new BRException("Le fichier PDF The pdf file cannot be fully analyzed, please check the error in the report. pdfFile: "+pdfFilePath);
    }

}
