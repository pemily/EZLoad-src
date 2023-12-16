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

import com.pascal.ezload.service.exporter.ezEdition.EzData;
import com.pascal.ezload.service.exporter.ezEdition.EzDataKey;
import com.pascal.ezload.service.model.EZDate;
import com.pascal.ezload.service.model.EZShare;
import com.pascal.ezload.service.model.EnumEZBroker;
import com.pascal.ezload.service.util.HttpUtilCached;
import com.pascal.ezload.service.util.LoggerReporting;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class SeekingAlphaToolsTest {

    private HttpUtilCached cache() throws IOException {
        String dir = System.getProperty("java.io.tmpdir")+ File.separator+this.getClass().getSimpleName()+"_"+Math.random();
        new File(dir).mkdirs();
        return new HttpUtilCached(dir);
    }



    @Test
    public void testSearchDividendeHistory() throws IOException {
        EZShare action = new EZShare();
        action.setSeekingAlphaCode("WSR");
        List<Dividend> dividends = SeekingAlphaTools.searchDividends(new LoggerReporting(), cache(), action, EZDate.today().minusYears(2), EZDate.today());
        Assertions.assertTrue(dividends.size() > 12);
    }


    @Test
    public void testSearchDividendeHistory2() throws IOException {
        EZShare action = new EZShare();
        List<String> l = new LinkedList<>();
        List.of("AOS", "WSR", "WPC", "IBM", "HP", "ABBV", "ABBV", "ABBV","ABBV","WBA", "WBA", "VZ", "UHT", "HP", "ABBV", "IBM", "UHT")
                .forEach(sh -> {
                action.setSeekingAlphaCode(sh);
                List<Dividend> dividends = null;
                try {
                    dividends = SeekingAlphaTools.searchDividends(new LoggerReporting(), cache(), action, EZDate.today().minusYears(2), EZDate.today());
                    if (dividends == null){
                        l.add(sh+"   ERROR 1 \n");
                    }
                    else l.add(sh+"   OK\n");
                } catch (IOException e) {
                    l.add(sh+"   ERROR 2\n");
                }
        });
        System.out.println(l);
    }

}
