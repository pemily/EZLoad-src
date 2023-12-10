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
package com.pascal.ezload.service.exporter;

import com.google.api.client.auth.oauth2.TokenResponseException;
import com.google.api.services.sheets.v4.Sheets;
import com.pascal.ezload.service.config.EzProfil;
import com.pascal.ezload.service.config.MainSettings;
import com.pascal.ezload.service.config.SettingsManager;
import com.pascal.ezload.service.exporter.ezEdition.ShareValue;
import com.pascal.ezload.service.exporter.ezPortfolio.v5_v6.EZPorfolioProxyV5_V6;
import com.pascal.ezload.service.exporter.ezPortfolio.v5_v6.MesOperations;
import com.pascal.ezload.service.gdrive.GDriveConnection;
import com.pascal.ezload.service.gdrive.GDriveSheets;
import com.pascal.ezload.service.sources.Reporting;
import com.pascal.ezload.service.util.StringUtils;

import java.util.*;

public class EZPortfolioManager {

    private Reporting reporting;
    private GDriveSheets sheets;
    private EzProfil ezProfil;
    private SettingsManager settingsManager;
    private MainSettings mainSettings;

    public EZPortfolioManager(Reporting reporting, SettingsManager settingsManager, MainSettings mainSettings, EzProfil ezProfil) {
        this.ezProfil = ezProfil;
        this.settingsManager = settingsManager;
        this.mainSettings = mainSettings;
        this.reporting = reporting;
    }


    public EZPortfolioProxy load(SettingsManager settingsManager, MainSettings mainSettings) throws Exception {
        try(Reporting rep = reporting.pushSection("Connection EZPortfolio...")){
            EZPortfolioProxy ezPortfolioProxy = load(1);
            Set<String> allShareNames = new HashSet<>();
            ezPortfolioProxy.getShareValuesFromMonPortefeuille()
                    .forEach(sv -> {
                        try {
                            allShareNames.add(sv.getUserShareName());
                            mainSettings.getEzLoad().getEZActionManager(settingsManager).createIfNeeded(sv);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    });
            ezPortfolioProxy.getAllOperations()
                    .getExistingOperations()
                    .forEach(op -> {
                        String shareName = op.getValueStr(MesOperations.ACTION_NAME_COL);
                        if (!StringUtils.isBlank(shareName)
                                && !shareName.equals("Valeur")
                                && (!shareName.toLowerCase(Locale.ROOT).contains("liquidit")) // pas le e pour si le user a ecrit avec ou sans accent dans ses operations
                                && !allShareNames.contains(shareName))
                        {
                            ShareValue sv = new ShareValue();
                            sv.setUserShareName(op.getValueStr(MesOperations.ACTION_NAME_COL));
                            try {
                                mainSettings.getEzLoad().getEZActionManager(settingsManager).createIfNeeded(sv);
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                            ;
                        }
                    });
            return ezPortfolioProxy;
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
            if (EZPorfolioProxyV5_V6.isCompatible(reporting, sheets)) {
                EZPorfolioProxyV5_V6 proxy = new EZPorfolioProxyV5_V6(sheets);
                proxy.load(reporting);
                return proxy;
            }

            // the default version
            throw new IllegalStateException("Vous devez utiliser EZPortfolio V5");
        }
        catch(TokenResponseException e){
            // Token expired
            reporting.info("Le token de connection est expiré, renouvellement du token.");
            GDriveConnection.deleteOldToken(this.settingsManager.getGDriveCredsFile(mainSettings.getActiveEzProfilName()));
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
            service = GDriveConnection.getService(reporting, this.settingsManager.getGDriveCredsFile(mainSettings.getActiveEzProfilName()));
            sheets = new GDriveSheets(service, ezPortfolioSettings.getEzPortfolioUrl());
            sheets.init(reporting);
        }
        catch(Exception e){
            reporting.error("Impossible de se connecter à Google Drive. Vérifiez votre fichier de sécurité Google Drive");
            throw e;
        }
    }
}
