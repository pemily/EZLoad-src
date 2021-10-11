package com.pascal.ezload.service.exporter;

import com.google.api.services.sheets.v4.Sheets;
import com.pascal.ezload.service.config.MainSettings;
import com.pascal.ezload.service.config.SettingsManager;
import com.pascal.ezload.service.exporter.ezPortfolio.v5.EZPorfolioProxyV5;
import com.pascal.ezload.service.gdrive.GDriveConnection;
import com.pascal.ezload.service.gdrive.GDriveSheets;
import com.pascal.ezload.service.sources.Reporting;
import com.pascal.ezload.service.util.StringUtils;

import java.io.IOException;
import java.security.GeneralSecurityException;

public class EZPortfolioManager {

    private Reporting reporting;
    private GDriveSheets sheets;
    private MainSettings mainSettings;

    public EZPortfolioManager(Reporting reporting, MainSettings settings) throws GeneralSecurityException, IOException {
        this.mainSettings = settings;
        Sheets service;
        try {
            service = GDriveConnection.getService(settings.getEzPortfolio().getGdriveCredsFile());
        }
        catch(Exception e){
            reporting.error("Impossible de se connecter à Google Drive. Vérifiez votre fichier de sécurité Google Drive");
            throw e;
        }
        String url = settings.getEzPortfolio().getEzPortfolioUrl();
        String next = url.substring(SettingsManager.EZPORTFOLIO_GDRIVE_URL_PREFIX.length());
        String ezPortfolioId = StringUtils.divide(next, '/')[0];
        sheets = new GDriveSheets(reporting, service, ezPortfolioId);
        this.reporting = reporting;
    }

    public EZPortfolioProxy load() throws Exception {
        try(Reporting rep = reporting.pushSection("Loading EZPortfolio...")){
            reporting.info("Getting data from Google Drive API...");

            // ici detection de la version de EZPortfolio
            if (EZPorfolioProxyV5.isCompatible(reporting, sheets)){
                return new EZPorfolioProxyV5(reporting, sheets);
            }

            // the default version
            throw new IllegalStateException("Vous devez utiliser EZPortfolio V5");
        }
    }

}
