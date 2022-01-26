package com.pascal.ezload.service.bourseDirect;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashSet;
import java.util.LinkedList;

import com.pascal.ezload.service.config.EzProfil;
import com.pascal.ezload.service.config.MainSettings;
import com.pascal.ezload.service.exporter.ezPortfolio.v5.PRU;
import com.pascal.ezload.service.gdrive.Row;
import com.pascal.ezload.service.gdrive.SheetValues;
import com.pascal.ezload.service.model.EZModel;
import com.pascal.ezload.service.sources.bourseDirect.BourseDirectAnalyser;
import com.pascal.ezload.service.sources.bourseDirect.BourseDirectEZAccountDeclaration;
import com.pascal.ezload.service.util.FileUtil;
import com.pascal.ezload.service.util.ModelUtils;
import com.pascal.ezload.service.util.ShareUtil;
import com.pascal.ezload.service.util.TextReporting;

import org.junit.jupiter.api.Test;

public class BourseDirectParserTest {

    // to regenerate the benchmark, just remove them and relaunch the test

    @Test
    public void test1() throws IOException {
        test("boursedirect-2021-02-24");
    }

    @Test
    public void test2() throws IOException {
        test("boursedirect-2021-02-26");
    }

    @Test
    public void test3() throws IOException {
        test("boursedirect-2021-03-01");
    }

    @Test
    public void test4() throws IOException {
        test("boursedirect-2021-03-02");
    }

    @Test
    public void test5() throws IOException {
        test("boursedirect-2021-03-03");
    }

    @Test
    public void test6() throws IOException {
        test("boursedirect-2021-03-08");
    }

    @Test
    public void test7() throws IOException {
        test("boursedirect-2021-03-09");
    }

    @Test
    public void test8() throws IOException {
        test("boursedirect-2021-03-16");
    }

    @Test
    public void test9() throws IOException {
        test("boursedirect-2021-03-18");
    }

    @Test
    public void test10() throws IOException {
        test("boursedirect-2021-03-19");
    }

    @Test
    public void test11() throws IOException {
        test("boursedirect-2021-03-30");
    }

    @Test
    public void test12() throws IOException {
        test("boursedirect-2021-04-07");
    }

    @Test
    public void test13() throws IOException {
        test("boursedirect-2021-04-19");
    }

    @Test
    public void test14() throws IOException {
        test("boursedirect-2021-04-30");
    }

    @Test
    public void test15() throws IOException {
        test("boursedirect-2021-05-11");
    }

    @Test
    public void test16() throws IOException {
        test("boursedirect-2021-05-18");
    }

    @Test
    public void test17() throws IOException {
        test("boursedirect-2021-07-06");
    }

    @Test
    public void test18() throws IOException {
        test("boursedirect-2021-07-08");
    }

    @Test
    public void test19() throws IOException {
        test("boursedirect-2021-07-12");
    }

    @Test
    public void test20() throws IOException {
        test("boursedirect-2021-07-15");
    }

    @Test
    public void test21() throws IOException {
        test("boursedirect-2021-07-19");
    }

    private URL getFilePath(String fileName){
        return BourseDirectParserTest.class.getResource(fileName);
    }

    private URL getBenchmark(String fileName){
        return BourseDirectParserTest.class.getResource("benchmark/"+fileName);
    }

    private void check(String filename, String result) throws IOException {
        URL file = getBenchmark(filename);
        if (file == null) {
            // the file does not exists, create the benchmark
            String filePath = "src/test/resources/"
                    +(BourseDirectParserTest.class.getPackage().getName().replace('.','/'))
                    +"/benchmark/"+filename;
            System.out.println("GENERATING BENCHMARK for "+filePath);
            FileUtil.string2file(filePath, result);
            file = new File(filePath).toURI().toURL();
        }

        String text = FileUtil.file2String(file.getFile());
        assertEquals(text.trim(), result.trim());
    }

    private void test(String file) throws IOException {
        TextReporting reporting = new TextReporting();
        BourseDirectEZAccountDeclaration bracc = new BourseDirectEZAccountDeclaration();
        bracc.setName("Pascal CTO");
        ShareUtil shareUtil = new ShareUtil(new PRU(SheetValues.createFromRowLists("a1:a", new LinkedList<>())), new HashSet<>());
        MainSettings mainSettings = new MainSettings();
        MainSettings.EZLoad ezLoad = new MainSettings.EZLoad();
        mainSettings.setEzLoad(ezLoad);

        EzProfil ezProfil = new EzProfil();
        ezProfil.setDownloadDir(new File(getFilePath(file +".pdf").getFile()).getParent());
        EZModel brModel = new BourseDirectAnalyser(mainSettings, ezProfil).start(reporting, bracc, getFilePath(file +".pdf").getFile());
        String jsonBrModel = ModelUtils.toJson(brModel);
        String report = reporting.getReport();

        check(file +".model.expected.txt", jsonBrModel);
        check( file +".reporting.expected.txt", report);
    }
}
