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

/* Il n'y a plus les regles dans src test resources, elles sont dans un autre repo maintenant
    @Test
    public void test1() throws IOException {
      test("boursedirect-2021-02-24");
    }
*/

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
