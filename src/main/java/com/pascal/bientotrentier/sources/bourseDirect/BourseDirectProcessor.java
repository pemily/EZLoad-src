package com.pascal.bientotrentier.sources.bourseDirect;

import com.pascal.bientotrentier.MainSettings;
import com.pascal.bientotrentier.exporter.ezPortfolio.EZPortfolio;
import com.pascal.bientotrentier.model.BRDate;
import com.pascal.bientotrentier.model.BRModel;
import com.pascal.bientotrentier.model.EnumBRCourtier;
import com.pascal.bientotrentier.sources.FileProcessor;
import com.pascal.bientotrentier.sources.Reporting;
import com.pascal.bientotrentier.sources.bourseDirect.download.BourseDirectDownloader;
import com.pascal.bientotrentier.sources.bourseDirect.transform.BourseDirect2BRModel;
import com.pascal.bientotrentier.sources.bourseDirect.transform.BourseDirectModelChecker;
import com.pascal.bientotrentier.sources.bourseDirect.transform.BourseDirectPdfExtractor;
import com.pascal.bientotrentier.sources.bourseDirect.transform.BourseDirectText2Model;
import com.pascal.bientotrentier.sources.bourseDirect.transform.model.BourseDirectModel;
import com.pascal.bientotrentier.util.BRException;
import com.pascal.bientotrentier.util.BRParsingException;
import com.pascal.bientotrentier.util.FileLinkCreator;
import com.pascal.bientotrentier.util.TitleWithFileRef;

import java.io.IOException;
import java.util.List;

public class BourseDirectProcessor {

    private final MainSettings mainSettings;

    public BourseDirectProcessor(MainSettings mainSettings) {
        this.mainSettings = mainSettings;
    }

    public List<BRModel> start(final Reporting reporting, EZPortfolio ezPortfolio) throws IOException {
        BourseDirectDownloader bourseDirectDownloader = new BourseDirectDownloader(reporting, mainSettings);
        bourseDirectDownloader.start();

        try(Reporting rep = reporting.pushSection("Analyzing downloaded files...")) {
            return new FileProcessor(mainSettings.getBourseDirect().getPdfOutputDir(),
                    BourseDirectDownloader.dirFilter(mainSettings),
                    BourseDirectDownloader.fileFilter())
                    .forEachFiles(pdfFilePath -> {
                        MainSettings.AccountDeclaration accountDeclaration = bourseDirectDownloader.getAccountFromPdfFilePath(pdfFilePath);
                        BRDate pdfDate = BourseDirectDownloader.getDateFromPdfFilePath(pdfFilePath);
                        if (accountDeclaration != null
                                && pdfDate != null
                                && !ezPortfolio.getMesOperations().isAlreadyProcessed(EnumBRCourtier.BourseDirect, accountDeclaration, pdfDate)) {
                            // if the pdf file is valid, and is not yet processed
                            // start its analysis
                            BRModel model = start(reporting, accountDeclaration, pdfFilePath);
                            return model;
                        }
                        return null;
                    });
        }
    }

    public BRModel start(Reporting reporting, MainSettings.AccountDeclaration accountDeclaration, String pdfFilePath) {
        try(Reporting rep = reporting.pushSection((rep1, fileLinkCreator) -> rep1.escape("Working on ") + fileLinkCreator.createSourceLink(rep1, pdfFilePath))){
            String pdfText = new BourseDirectPdfExtractor(reporting).getText(pdfFilePath);

            BourseDirectModel model = new BourseDirectText2Model(reporting).toModel(pdfText);

            boolean isValid = new BourseDirectModelChecker(reporting).isValid(model);

            if (isValid){
                return new BourseDirect2BRModel(reporting).create(pdfFilePath, accountDeclaration, model);
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
