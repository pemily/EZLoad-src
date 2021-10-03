package com.pascal.ezload.service.download;

import com.pascal.ezload.service.config.MainSettings;
import com.pascal.ezload.service.sources.bourseDirect.BourseDirectEZAccountDeclaration;
import com.pascal.ezload.service.sources.bourseDirect.BourseDirectSettings;
import com.pascal.ezload.service.sources.bourseDirect.selenium.BourseDirectDownloader;
import com.pascal.ezload.service.util.LoggerReporting;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

public class BourseDirectDownloaderTest {

    @Test
    public void testDateFromPdf(){
        Assertions.assertEquals("2021/01/23", BourseDirectDownloader.getDateFromPdfFilePath("/un/path/boursedirect-2021-01-23.pdf").toDate('/'));
        assertNull(BourseDirectDownloader.getDateFromPdfFilePath("/un/path/file.pdf"));
    }

    @Test
    public void getAccountFromPdfFilePathTest(){
        MainSettings mainSettings = new MainSettings();
        BourseDirectSettings bourseDircectSettings = new BourseDirectSettings();
        BourseDirectEZAccountDeclaration aaaa = new BourseDirectEZAccountDeclaration();
        aaaa.setName("AAAA");
        BourseDirectEZAccountDeclaration aaa = new BourseDirectEZAccountDeclaration();
        aaa.setName("AAA");
        BourseDirectEZAccountDeclaration aaaaa = new BourseDirectEZAccountDeclaration();
        aaaaa.setName("AAAAA");
        bourseDircectSettings.setAccounts(Arrays.asList(aaaa,aaa,aaaaa));
        mainSettings.setBourseDirect(bourseDircectSettings);
        BourseDirectDownloader bdd = new BourseDirectDownloader(new LoggerReporting(), mainSettings);
        Assertions.assertEquals("AAAA", bdd.getAccountFromPdfFilePath("/AAAA/2021/boursedirect-2021-01-23.pdf").getName());
        Assertions.assertEquals("AAA", bdd.getAccountFromPdfFilePath("/AAA/2021/boursedirect-2021-01-23.pdf").getName());
        Assertions.assertEquals("AAAAA", bdd.getAccountFromPdfFilePath("/AAAAA/2021/boursedirect-2021-01-23.pdf").getName());
        assertNull(bdd.getAccountFromPdfFilePath("/AAAAAAA/2021/boursedirect-2021-01-23.pdf"));
    }
}
