package com.pascal.ezload.service.exporter;

import com.google.api.services.sheets.v4.Sheets;
import com.pascal.ezload.service.config.MainSettings;
import com.pascal.ezload.service.exporter.ezPortfolio.v5.EZPorfolioProxyV5;
import com.pascal.ezload.service.gdrive.GDriveConnection;
import com.pascal.ezload.service.gdrive.GDriveSheets;
import com.pascal.ezload.service.sources.Reporting;

import java.io.IOException;
import java.security.GeneralSecurityException;

public class EZPortfolioManager {

    private Reporting reporting;
    private GDriveSheets sheets;
    private MainSettings mainSettings;

    public EZPortfolioManager(Reporting reporting, MainSettings mainSettings) throws GeneralSecurityException, IOException {
        this.mainSettings = mainSettings;
        this.reporting = reporting;
    }


    public EZPortfolioProxy load() throws Exception {
        try(Reporting rep = reporting.pushSection("Connection EZPortfolio...")){
            connect(reporting, mainSettings);
            reporting.info("Récupération des données de Google Drive...");
            return load(1);
        }
    }

    private EZPortfolioProxy load(int retry) throws Exception {
        try {
            // ici detection de la version de EZPortfolio
            if (EZPorfolioProxyV5.isCompatible(reporting, sheets)) {
                return new EZPorfolioProxyV5(reporting, sheets);
            }

            // the default version
            throw new IllegalStateException("Vous devez utiliser EZPortfolio V5");
        }
        catch(Exception e){
            // TODO ici mettre l'exception du token expired
            reporting.info("Le token de connection est expiré, renouvellement du token.");
            GDriveConnection.deleteOldToken(mainSettings.getEzPortfolio().getGdriveCredsFile());
            connect(reporting, mainSettings);
            if (retry > 0) {
               return load(retry--);
            }
            reporting.error("Impossible de récupérer les données de EZPortfolio");
            throw new Exception("Impossible de récupérer les données de EZPortfolio", e);
        }
    }

    private void connect(Reporting reporting, MainSettings mainSettings) throws IOException, GeneralSecurityException {
        Sheets service;
        try {
            reporting.info("Connection à votre EZPortfolio: "+mainSettings.getEzPortfolio().getEzPortfolioUrl());
            service = GDriveConnection.getService(reporting, mainSettings.getEzPortfolio().getGdriveCredsFile());
        }
        catch(Exception e){
            reporting.error("Impossible de se connecter à Google Drive. Vérifiez votre fichier de sécurité Google Drive");
            throw e;
        }
        sheets = new GDriveSheets(reporting, service, mainSettings.getEzPortfolio().getEzPortfolioUrl());
    }
}
