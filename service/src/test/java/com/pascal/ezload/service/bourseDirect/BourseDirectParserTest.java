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
package com.pascal.ezload.service.bourseDirect;

import com.pascal.ezload.service.config.EzProfil;
import com.pascal.ezload.service.config.MainSettings;
import com.pascal.ezload.service.config.SettingsManager;
import com.pascal.ezload.service.model.EZModel;
import com.pascal.ezload.service.sources.bourseDirect.BourseDirectAnalyser;
import com.pascal.ezload.service.sources.bourseDirect.BourseDirectEZAccountDeclaration;
import com.pascal.ezload.service.util.FileUtil;
import com.pascal.ezload.service.util.ModelUtils;
import com.pascal.ezload.service.util.TextReporting;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
        MainSettings mainSettings = new MainSettings();
        MainSettings.EZLoad ezLoad = new MainSettings.EZLoad();
        mainSettings.setEzLoad(ezLoad);

        SettingsManager settingsManager = new SettingsManager("ezload.yaml");

        EzProfil ezProfil = new EzProfil();
        EZModel brModel = new BourseDirectAnalyser(settingsManager, mainSettings, ezProfil).start(reporting, bracc, getFilePath(file +".pdf").getFile());
        String jsonBrModel = ModelUtils.toJson(brModel);
        String report = reporting.getReport();

        check(file +".model.expected.txt", jsonBrModel);
        check( file +".reporting.expected.txt", report);
    }
}
