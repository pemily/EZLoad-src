package com.pascal.bientotrentier.sources.bourseDirect.transform;

import com.pascal.bientotrentier.MainSettings;
import com.pascal.bientotrentier.model.BRModel;
import com.pascal.bientotrentier.sources.FileProcessor;
import com.pascal.bientotrentier.sources.Reporting;
import com.pascal.bientotrentier.sources.bourseDirect.BourseDirectSettings;
import com.pascal.bientotrentier.sources.bourseDirect.transform.model.BourseDirectModel;
import com.pascal.bientotrentier.util.BRException;
import com.pascal.bientotrentier.util.BRParsingException;

import java.io.IOException;
import java.util.List;

public class BourseDirectProcessor {

    private final BourseDirectSettings bourseDirectSettings;

    public BourseDirectProcessor(MainSettings mainSettings) {
        this.bourseDirectSettings = mainSettings.getBourseDirect();
    }

    public List<BRModel> start(final Reporting reporting) throws IOException {
        return new FileProcessor(this.bourseDirectSettings.getPdfOutputDir())
            .forEachFiles(pdfFilePath -> start(reporting, pdfFilePath));
    }

    public BRModel start(Reporting reporting, String pdfFilePath) {
        reporting.pushSection(pdfFilePath);
        try {

            String pdfText = new BourseDirectPdfExtractor(reporting).getText(pdfFilePath);

            BourseDirectModel model = new BourseDirectText2Model(reporting).toModel(pdfText);

            boolean isValid = new BourseDirectModelChecker(reporting).isValid(model);

            if (isValid){
                BRModel brModel = new BourseDirect2BRModel(reporting).create(pdfFilePath, model);
                return brModel;
            }
        }
        catch (BRParsingException e){
            e.setFilePath(pdfFilePath);
            reporting.error(e);
        }
        catch (IOException | BRException e) {
            BRParsingException parsingException = new BRParsingException(e);
            parsingException.setFilePath(pdfFilePath);
            reporting.error(e);
        }
        finally {
            reporting.popSection();
        }

        return null;
    }


}
