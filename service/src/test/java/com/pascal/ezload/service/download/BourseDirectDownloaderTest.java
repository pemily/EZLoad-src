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
package com.pascal.ezload.service.download;

import com.pascal.ezload.service.config.EzProfil;
import com.pascal.ezload.service.config.MainSettings;
import com.pascal.ezload.service.config.SettingsManager;
import com.pascal.ezload.service.sources.bourseDirect.BourseDirectEZAccountDeclaration;
import com.pascal.ezload.service.sources.bourseDirect.BourseDirectSettings;
import com.pascal.ezload.service.sources.bourseDirect.selenium.BourseDirectDownloader;
import com.pascal.ezload.common.util.LoggerReporting;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

public class BourseDirectDownloaderTest {

    @Test
    public void testDateFromPdf(){
        Assertions.assertEquals("2021/01/23", BourseDirectDownloader.getDateFromFilePath("/un/path/boursedirect-2021-01-23.pdf").toDate('/'));
        assertNull(BourseDirectDownloader.getDateFromFilePath("/un/path/file.pdf"));
    }

    @Test
    public void getAccountFromPdfFilePathTest(){
        SettingsManager settingsManager = new SettingsManager("ezload.yaml");
        MainSettings mainSettings = new MainSettings();
        BourseDirectSettings bourseDircectSettings = new BourseDirectSettings();
        BourseDirectEZAccountDeclaration aaaa = new BourseDirectEZAccountDeclaration();
        aaaa.setName("AAAA");
        BourseDirectEZAccountDeclaration aaa = new BourseDirectEZAccountDeclaration();
        aaa.setName("AAA");
        BourseDirectEZAccountDeclaration aaaaa = new BourseDirectEZAccountDeclaration();
        aaaaa.setName("AAAAA");
        bourseDircectSettings.setAccounts(Arrays.asList(aaaa,aaa,aaaaa));
        EzProfil ezProfil = new EzProfil();
        ezProfil.setBourseDirect(bourseDircectSettings);
        BourseDirectDownloader bdd = new BourseDirectDownloader(new LoggerReporting(), settingsManager, mainSettings, ezProfil);
        Assertions.assertEquals("AAAA", bdd.getAccountFromFilePath("/AAAA/2021/boursedirect-2021-01-23.pdf").getName());
        Assertions.assertEquals("AAA", bdd.getAccountFromFilePath("/AAA/2021/boursedirect-2021-01-23.pdf").getName());
        Assertions.assertEquals("AAAAA", bdd.getAccountFromFilePath("/AAAAA/2021/boursedirect-2021-01-23.pdf").getName());
        assertNull(bdd.getAccountFromFilePath("/AAAAAAA/2021/boursedirect-2021-01-23.pdf"));
    }
}
