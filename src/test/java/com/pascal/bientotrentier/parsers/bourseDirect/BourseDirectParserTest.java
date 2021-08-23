package com.pascal.bientotrentier.parsers.bourseDirect;

import com.pascal.bientotrentier.MainSettings;
import com.pascal.bientotrentier.model.BRModel;
import com.pascal.bientotrentier.util.TextReporting;
import com.pascal.bientotrentier.sources.bourseDirect.BourseDirectProcessor;
import com.pascal.bientotrentier.util.ModelUtils;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.net.URL;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

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

    private void check(String result, String filename) throws IOException {
        URL file = getBenchmark(filename);

        if (file == null) {
            System.out.println("GENERATING BENCHMARK for "+filename);
            // the file does not exists, create the benchmark
            String filePath = "src/test/resources/"
                    +(BourseDirectParserTest.class.getPackage().getName().replace('.','/'))
                    +"/benchmark/"+filename;
            new File(filePath).getParentFile().mkdirs();
            FileWriter fileWriter = new FileWriter(filePath);
            fileWriter.write(result);
            fileWriter.close();
            file = new File(filePath).toURI().toURL();
        }

        String text = new BufferedReader(new FileReader(file.getFile())).lines().collect(Collectors.joining("\n"));
        assertEquals(result.trim(), text.trim());
    }

    private void test(String file) throws IOException {
        TextReporting reporting = new TextReporting();
        BRModel brModel = new BourseDirectProcessor(new MainSettings()).start(reporting, getFilePath(file +".pdf").getFile());
        String jsonBrModel = ModelUtils.toJson(brModel);
        String report = reporting.getReport();
        check(jsonBrModel, file +".model.expected.txt");
        check(report, file +".reporting.expected.txt");
    }
}
