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
package com.pascal.ezload.service.gdrive;


import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.pascal.ezload.common.sources.Reporting;

import java.io.*;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;


public class GDriveConnection {

    private static final String APPLICATION_NAME = "EZLoad";

    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();

    private static final List<String> SCOPES = Collections.singletonList(SheetsScopes.SPREADSHEETS);

    public static Sheets getService(Reporting reporting, String gDriveCredentialsFile) throws IOException, GeneralSecurityException {

        // 2: Build a new authorized API client service.
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();

        // 3: Read client_secret.json file & create Credential object.
        Credential credential = getCredentials(reporting, gDriveCredentialsFile, HTTP_TRANSPORT);

        // 5: Create Google Sheet Service.
        Sheets service = new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, setTimeout(credential, 10*1000))
                .setApplicationName(APPLICATION_NAME)
                .build();

        return service;
    }


    private static Credential getCredentials(Reporting reporting, String gDriveCredentialsFile, final NetHttpTransport HTTP_TRANSPORT) throws IOException {
        java.io.File clientSecretFilePath = new java.io.File(gDriveCredentialsFile);

        // Load client secrets.
        InputStream in = new FileInputStream(clientSecretFilePath);

        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        // Build flow and trigger user authorization request.
        // ici le fichier StoredCredential va etre créé dans le repertoire clientSecretFilePath.getParentFile()
        // il va correspondre a un token temporaire de connection (valable 7 jours pour les comptes google dev de tests)
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(clientSecretFilePath.getParentFile()))
                .setAccessType("offline").build();

        reporting.info("Si cela fait longtemps que vous ne vous êtes pas connecté avec EZLoad");
        reporting.info("Google va vous re-demander de choisir le compte de connection et de valider l'application EZLoad");
        reporting.info("Vous devrez selectionner votre compte Google et cliquer sur 'Continuer'");
        LocalServerReceiver localServerReceiver = new LocalServerReceiver();
        AuthorizationCodeInstalledApp authCode = new AuthorizationCodeInstalledApp(flow, localServerReceiver);
        return authCode.authorize("user"); // this call is blocked until the user gives the google permissions
    }


    private static HttpRequestInitializer setTimeout(final HttpRequestInitializer initializer, final int timeout) {
        return request -> {
            initializer.initialize(request);
            request.setConnectTimeout(timeout);
            request.setReadTimeout(timeout);
        };
    }

    public static void deleteOldToken(String gDriveCredentialsFile) {
        new File(new File(gDriveCredentialsFile).getParent()+"/StoredCredential").delete();
    }
}
