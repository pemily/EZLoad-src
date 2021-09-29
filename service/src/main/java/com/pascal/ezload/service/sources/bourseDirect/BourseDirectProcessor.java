package com.pascal.ezload.service.sources.bourseDirect;

import com.pascal.ezload.service.config.SettingsManager;
import com.pascal.ezload.service.model.BRAccountDeclaration;
import com.pascal.ezload.service.config.MainSettings;
import com.pascal.ezload.service.exporter.ezPortfolio.EZPortfolio;
import com.pascal.ezload.service.model.BRDate;
import com.pascal.ezload.service.model.BRModel;
import com.pascal.ezload.service.model.EnumBRCourtier;
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

    public List<BRModel> start(final Reporting reporting,
                               String currentChromeVersion, Consumer<String> newDriverPathSaver,
                               EZPortfolio ezPortfolio) throws IOException {
        BourseDirectDownloader bourseDirectDownloader = new BourseDirectDownloader(reporting, mainSettings);
        bourseDirectDownloader.start(currentChromeVersion, newDriverPathSaver, ezPortfolio);

        try(Reporting ignored = reporting.pushSection("Analyzing downloaded files...")) {
            return new FileProcessor(SettingsManager.getDownloadDir(mainSettings, EnumBRCourtier.BourseDirect),
                    BourseDirectDownloader.dirFilter(mainSettings),
                    BourseDirectDownloader.fileFilter())
                    .forEachFiles(pdfFilePath -> {
                        BRAccountDeclaration BRAccountDeclaration = bourseDirectDownloader.getAccountFromPdfFilePath(pdfFilePath);
                        BRDate pdfDate = BourseDirectDownloader.getDateFromPdfFilePath(pdfFilePath);
                        if (BRAccountDeclaration != null
                                && pdfDate != null
                                && !ezPortfolio.getMesOperations().isAlreadyProcessed(EnumBRCourtier.BourseDirect, BRAccountDeclaration, pdfDate)) {
                            // if the pdf file is valid, and is not yet processed
                            // start its analysis
                            BRModel model = start(reporting, BRAccountDeclaration, pdfFilePath);
                            return model;
                        }
                        return null;
                    });
        }
    }

    public BRModel start(Reporting reporting, BRAccountDeclaration BRAccountDeclaration, String pdfFilePath) {
        try(Reporting ignored = reporting.pushSection((rep1, fileLinkCreator) -> rep1.escape("Working on ") + fileLinkCreator.createSourceLink(rep1, pdfFilePath))){
            String pdfText = new BourseDirectPdfExtractor(reporting).getText(pdfFilePath);

            BourseDirectModel model = new BourseDirectText2Model(reporting).toModel(pdfText);

            boolean isValid = new BourseDirectModelChecker(reporting).isValid(model);

            if (isValid){
                return new BourseDirect2BRModel(reporting).create(pdfFilePath, BRAccountDeclaration, model);
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

        throw new BRException("The pdf file cannot be fully analyzed, please check the error in the report. pdfFile: "+pdfFilePath);
    }

}
