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
package com.pascal.ezload.service.sources.bourseDirect;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import com.pascal.ezload.service.sources.jsonSource.JsonSource;
import com.pascal.ezload.service.util.FileProcessor;
import com.pascal.ezload.service.util.JsonUtil;
import com.pascal.ezload.service.util.PdfTextExtractor;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;

public class BourseDirectAnalyser {

    private final SettingsManager settingsManager;
    private final MainSettings mainSettings;
    private final EzProfil ezProfil;
    private final static int UNKNOWN_VERSION = -1;

    public BourseDirectAnalyser(SettingsManager settingsManager, MainSettings mainSettings, EzProfil ezProfil) {
        this.mainSettings = mainSettings;
        this.settingsManager = settingsManager;
        this.ezProfil = ezProfil;
    }

    private interface IProcess<R> {
        R execute(EZAccountDeclaration account, String pdfFilePath) throws Exception;
    }

    public List<String> getFilesNotYetLoaded(Reporting reporting, EZPortfolioProxy ezPortfolioProxy) throws Exception {
        return startProcess(reporting, ezPortfolioProxy, ((account, pdfFilePath) -> ezProfil.getSourceRef(settingsManager, mainSettings.getActiveEzProfilName(), pdfFilePath)));
    }

    public List<EZModel> start(final Reporting reporting, EZPortfolioProxy ezPortfolioProxy) throws Exception {
        return startProcess(reporting, ezPortfolioProxy, (account, pdfFilePath) -> start(reporting, account, pdfFilePath));
    }

    private <R> List<R> startProcess(final Reporting reporting, EZPortfolioProxy ezPortfolioProxy, IProcess<R> process) throws Exception {
        BourseDirectDownloader bourseDirectDownloader = new BourseDirectDownloader(reporting, settingsManager, mainSettings, ezProfil);

        try(Reporting ignored = reporting.pushSection("Analyse des fichiers téléchargés...")) {
            String downloadDir = settingsManager.getDownloadDir(mainSettings.getActiveEzProfilName(), EnumEZBroker.BourseDirect);
            reporting.info("Répertoire des fichiers à analyser: "+downloadDir);
              return new FileProcessor(downloadDir,
                                        BourseDirectDownloader.dirFilter(ezProfil), BourseDirectDownloader.fileFilter())
                    .mapFile(filePath -> {
                        EZAccountDeclaration account = bourseDirectDownloader.getAccountFromFilePath(filePath);
                        EZDate fileDate = BourseDirectDownloader.getDateFromFilePath(filePath);

                        if (account != null
                                && fileDate != null
                                && !ezPortfolioProxy.isFileAlreadyLoaded(EnumEZBroker.BourseDirect, account, fileDate)
                                && account.isActive()) {
                            // if the file is valid, and is not yet processed
                            // start its analysis
                            try {
                                return process.execute(account, filePath);
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                        }
                        return null;
                    });
        }
    }

    public EZModel start(Reporting reporting, EZAccountDeclaration ezAccountDeclaration, String filePath) throws IOException {
        try(Reporting ignored = reporting.pushSection((rep1, fileLinkCreator) -> rep1.escape("Fichier en cours d'analyse: ") + fileLinkCreator.createSourceLink(rep1, filePath))){

            if (filePath.endsWith(".pdf")){
                List<String> errors = new LinkedList<>();
                try {
                    BourseDirectModel model = genModelFromPdf(reporting, filePath);
                    errors = new BourseDirectModelChecker(reporting, model).searchErrors();

                    if (errors.size() == 0) {
                        return new BourseDirect2EZModel(settingsManager, mainSettings.getActiveEzProfilName(), ezProfil, reporting).create(filePath, ezAccountDeclaration, model);
                    }
                } catch (Exception e) {
                    reporting.error("Erreur pendant l'analyze", e);
                    errors.add("Erreur pendant l'analyze: " + e.getMessage());
                }
                EZModel ezModel = new EZModel(EnumEZBroker.BourseDirect, UNKNOWN_VERSION, ezProfil.getSourceRef(settingsManager, mainSettings.getActiveEzProfilName(), filePath));
                ezModel.setErrors(errors);
                return ezModel;

            }
            else {
                return JsonSource.genModelFromJson(reporting, settingsManager, mainSettings.getActiveEzProfilName(), ezProfil, ezAccountDeclaration, filePath);
            }
        }
    }

    private BourseDirectModel genModelFromPdf(Reporting reporting, String filePath) throws IOException {
        PdfTextExtractor extractor = new PdfTextExtractor(reporting, filePath);
        PdfTextExtractor.Result pdfText = extractor.process();
        return new BourseDirectText2Model(reporting).toModel(pdfText);
    }

}
