package com.pascal.ezload.service.exporter;

import com.google.api.client.auth.oauth2.TokenResponseException;
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
            return load(1);
        }
    }

    private EZPortfolioProxy load(int retry) throws Exception {
        try {
            connect(reporting, mainSettings);

            reporting.info("Récupération des données de Google Drive...");
            // ici detection de la version de EZPortfolio
            if (EZPorfolioProxyV5.isCompatible(reporting, sheets)) {
                EZPorfolioProxyV5 proxy = new EZPorfolioProxyV5(reporting, sheets);
                proxy.load();
                return proxy;
            }

            // the default version
            throw new IllegalStateException("Vous devez utiliser EZPortfolio V5");
        }
        catch(TokenResponseException e){
            // Token expired
            reporting.info("Le token de connection est expiré, renouvellement du token.");
            if (retry-- > 0) {
               return load(retry);
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
            sheets = new GDriveSheets(reporting, service, mainSettings.getEzPortfolio().getEzPortfolioUrl());
        }
        catch(Exception e){
            reporting.error("Impossible de se connecter à Google Drive. Vérifiez votre fichier de sécurité Google Drive");
            throw e;
        }
    }
}
