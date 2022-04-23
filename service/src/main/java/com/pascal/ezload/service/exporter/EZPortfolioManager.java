package com.pascal.ezload.service.exporter;

import com.google.api.client.auth.oauth2.TokenResponseException;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.Spreadsheet;
import com.pascal.ezload.service.config.EzProfil;
import com.pascal.ezload.service.exporter.ezPortfolio.v5.EZPorfolioProxyV5;
import com.pascal.ezload.service.gdrive.GDriveConnection;
import com.pascal.ezload.service.gdrive.GDriveSheets;
import com.pascal.ezload.service.sources.Reporting;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.Map;

public class EZPortfolioManager {

    private Reporting reporting;
    private GDriveSheets sheets;
    private EzProfil ezProfil;

    public EZPortfolioManager(Reporting reporting, EzProfil ezProfil) {
        this.ezProfil = ezProfil;
        this.reporting = reporting;
    }


    public EZPortfolioProxy load() throws Exception {
        try(Reporting rep = reporting.pushSection("Connection EZPortfolio...")){
            return load(1);
        }
        catch(Exception e){
            if (e instanceof IllegalStateException){
                throw e;
            }
            reporting.error("Il ne s'agit pas de EZPortfolio V5 ou il y a eu un problème de connection", e);
            throw new IllegalStateException("Il ne s'agit pas de EZPortfolio V5 ou il y a eu un problème de connection", e);
        }
    }

    private EZPortfolioProxy load(int retry) throws Exception {
        try {
            connect(reporting, ezProfil);

            reporting.info("Récupération des données de Google Drive...");
            // ici detection de la version de EZPortfolio
            if (EZPorfolioProxyV5.isCompatible(reporting, sheets)) {
                EZPorfolioProxyV5 proxy = new EZPorfolioProxyV5(sheets);
                proxy.load(reporting);
                return proxy;
            }

            // the default version
            throw new IllegalStateException("Vous devez utiliser EZPortfolio V5");
        }
        catch(TokenResponseException e){
            // Token expired
            reporting.info("Le token de connection est expiré, renouvellement du token.");
            GDriveConnection.deleteOldToken(ezProfil.getEzPortfolio().getGdriveCredsFile());
            if (retry-- > 0) {
               return load(retry);
            }
            reporting.error("Impossible de récupérer les données de EZPortfolio");
            throw new Exception("Impossible de récupérer les données de EZPortfolio", e);
        }
    }

    private void connect(Reporting reporting, EzProfil ezProfil) throws Exception {
        Sheets service;
        try {
            EZPortfolioSettings ezPortfolioSettings = ezProfil.getEzPortfolio();
            reporting.info("Connection à votre EZPortfolio: "+ ezPortfolioSettings.getEzPortfolioUrl());
            service = GDriveConnection.getService(reporting, ezPortfolioSettings.getGdriveCredsFile());
            sheets = new GDriveSheets(service, ezPortfolioSettings.getEzPortfolioUrl());
            sheets.init(reporting);
        }
        catch(Exception e){
            reporting.error("Impossible de se connecter à Google Drive. Vérifiez votre fichier de sécurité Google Drive");
            throw e;
        }
    }
}
