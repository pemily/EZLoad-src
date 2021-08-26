package com.pascal.bientotrentier.service.download;

import com.pascal.bientotrentier.service.config.MainSettings;
import com.pascal.bientotrentier.service.sources.bourseDirect.BourseDirectBRAccountDeclaration;
import com.pascal.bientotrentier.service.sources.bourseDirect.BourseDirectSettings;
import com.pascal.bientotrentier.service.sources.bourseDirect.download.BourseDirectDownloader;
import com.pascal.bientotrentier.service.util.LoggerReporting;
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
        BourseDirectBRAccountDeclaration aaaa =new BourseDirectBRAccountDeclaration();
        aaaa.setName("AAAA");
        BourseDirectBRAccountDeclaration aaa =new BourseDirectBRAccountDeclaration();
        aaa.setName("AAA");
        BourseDirectBRAccountDeclaration aaaaa =new BourseDirectBRAccountDeclaration();
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
