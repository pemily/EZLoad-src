package com.pascal.bientotrentier.sources.bourseDirect.download;

import com.pascal.bientotrentier.config.MainSettings;
import com.pascal.bientotrentier.sources.bourseDirect.BourseDirectAccountDeclaration;
import com.pascal.bientotrentier.sources.bourseDirect.BourseDirectSettings;
import com.pascal.bientotrentier.util.LoggerReporting;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

public class BourseDirectDownloaderTest {

    @Test
    public void testDateFromPdf(){
        assertEquals("2021/01/23", BourseDirectDownloader.getDateFromPdfFilePath("/un/path/boursedirect-2021-01-23.pdf").toDate('/'));
        assertNull(BourseDirectDownloader.getDateFromPdfFilePath("/un/path/file.pdf"));
    }

    @Test
    public void getAccountFromPdfFilePathTest(){
        MainSettings mainSettings = new MainSettings();
        BourseDirectSettings bourseDircectSettings = new BourseDirectSettings();
        BourseDirectAccountDeclaration aaaa =new BourseDirectAccountDeclaration();
        aaaa.setName("AAAA");
        BourseDirectAccountDeclaration aaa =new BourseDirectAccountDeclaration();
        aaa.setName("AAA");
        BourseDirectAccountDeclaration aaaaa =new BourseDirectAccountDeclaration();
        aaaaa.setName("AAAAA");
        bourseDircectSettings.setAccounts(Arrays.asList(aaaa,aaa,aaaaa));
        mainSettings.setBourseDirect(bourseDircectSettings);
        BourseDirectDownloader bdd = new BourseDirectDownloader(new LoggerReporting(), mainSettings);
        assertEquals("AAAA", bdd.getAccountFromPdfFilePath("/AAAA/2021/boursedirect-2021-01-23.pdf").getName());
        assertEquals("AAA", bdd.getAccountFromPdfFilePath("/AAA/2021/boursedirect-2021-01-23.pdf").getName());
        assertEquals("AAAAA", bdd.getAccountFromPdfFilePath("/AAAAA/2021/boursedirect-2021-01-23.pdf").getName());
        assertNull(bdd.getAccountFromPdfFilePath("/AAAAAAA/2021/boursedirect-2021-01-23.pdf"));
    }
}
