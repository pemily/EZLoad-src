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
package com.pascal.ezload.service.util.finance;

import com.gargoylesoftware.htmlunit.Page;
import com.pascal.ezload.service.config.MainSettings;
import com.pascal.ezload.service.config.SettingsManager;
import com.pascal.ezload.service.exporter.ezEdition.EzData;
import com.pascal.ezload.service.exporter.ezEdition.EzDataKey;
import com.pascal.ezload.service.model.EZDate;
import com.pascal.ezload.service.model.EZShare;
import com.pascal.ezload.service.model.EnumEZBroker;
import com.pascal.ezload.service.sources.bourseDirect.selenium.BourseDirectSeleniumHelper;
import com.pascal.ezload.service.util.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.CookieManager;
import java.net.http.HttpResponse;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;

import static org.junit.jupiter.api.Assertions.*;

public class SeekingAlphaToolsTest {

    private HttpUtilCached cache() throws IOException {
        String dir = System.getProperty("java.io.tmpdir")+ File.separator+this.getClass().getSimpleName()+"_"+Math.random();
        new File(dir).mkdirs();
        return new HttpUtilCached(dir);
    }



    @Test
    public void testSearchDividendeHistory() throws Exception {
        EZShare action = new EZShare();
        action.setSeekingAlphaCode("WSR");
        action.setCountryCode("US");
        List<Dividend> dividends = SeekingAlphaTools.searchDividends(new LoggerReporting(), cache(), action, EZDate.today().minusYears(2));
        Assertions.assertTrue(dividends.size() > 12);
    }

    @Test
    public void testSearchDividendeHistoryBankOfNova() throws Exception {
        EZShare action = new EZShare();
        action.setSeekingAlphaCode("BNS:CA");
        action.setCountryCode("CA");


        String conf = SettingsManager.searchConfigFilePath();
        SettingsManager settingsManager = new SettingsManager(conf);
        MainSettings mainSettings = settingsManager.loadProps();

        List<Dividend> dividends = SeekingAlphaTools.searchDividends(new LoggerReporting(), cache(), action, EZDate.today().minusYears(10));
        Assertions.assertTrue(dividends.size() > 12);
    }

    @Test
    public void test() throws Exception {
        String url = "https://seekingalpha.com/api/v3/symbols/avgo/dividend_history?group_by=year&years=6"; // ok
        url = "https://seekingalpha.com/api/v3/symbols/avgo/dividend_history?group_by=quarterly&sort=-date";
        url = "https://seekingalpha.com/api/v3/symbols/bah/dividend_history?group_by=quarterly&sort=-date";
        url = "https://seekingalpha.com/api/v3/symbols/tte/dividend_history?group_by=quarterly&sort=-date";
        url = "https://seekingalpha-com.translate.goog/api/v3/symbols/IBM/dividend_history?group_by=quarterly&sort=-date&_x_tr_sl=en&_x_tr_tl=fr&_x_tr_hl=fr&_x_tr_pto=wapp";
        List<String[]> header= new LinkedList<>();

        header.add(new String[]{ "sec-ch-ua","\"Chromium\";v=\"122\", \"Not(A:Brand\";v=\"24\", \"Google Chrome\";v=\"122\""});
        header.add(new String[]{ "sec-ch-ua-mobile","?0"});
        header.add(new String[]{ "sec-ch-ua-platform", "\"Windows\""});
        header.add(new String[]{ "upgrade-insecure-requests","1"});
        header.add(new String[]{ "user-agent","Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36"});
        header.add(new String[]{ "accept","text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7"});
        header.add(new String[]{ "sec-fetch-site","none"});
        header.add(new String[]{ "sec-fetch-mode","navigate"});
        header.add(new String[]{ "sec-fetch-user","?1"});
        header.add(new String[]{ "sec-fetch-dest","document"});
        header.add(new String[]{ "accept-encoding","gzip, deflate, br"});
        header.add(new String[]{ "accept-language","fr-FR,fr;q=0.9,en-US;q=0.8,en;q=0.7"});
/*
        HttpResponse<InputStream> httpResponse = HttpUtil.httpGET(url, header);
        System.out.println(httpResponse.statusCode());
        System.out.println(FileUtil.inputStream2String(new GZIPInputStream(httpResponse.body())));*/
        System.out.println(HttpUtil.urlContent(url));
    }

    @Test
    public void testSearchDividendeHistory2() throws Exception {
        EZShare action = new EZShare();
        List<String> l = new LinkedList<>();
        List.of("AOS", "WSR", "WPC", "IBM", "HP", "ABBV", "ABBV", "ABBV","ABBV","WBA", "WBA", "VZ", "UHT", "HP", "ABBV", "IBM", "UHT")
                .forEach(sh -> {
                action.setSeekingAlphaCode(sh);
                List<Dividend> dividends = null;
                try {
                    dividends = SeekingAlphaTools.searchDividends(new LoggerReporting(), cache(), action, EZDate.today().minusYears(2));
                    if (dividends == null){
                        l.add(sh+"   ERROR 1 \n");
                    }
                } catch (Exception e) {
                    l.add(sh+"   ERROR 2\n");
                }
        });
        assertEquals(List.of(), l);
    }

}
