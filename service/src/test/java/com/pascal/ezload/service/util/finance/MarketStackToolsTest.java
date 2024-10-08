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

import com.pascal.ezload.common.util.finance.MarketStackTools;
import com.pascal.ezload.service.model.EZShare;
import com.pascal.ezload.common.util.HttpUtilCached;
import com.pascal.ezload.common.util.LoggerReporting;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

public class MarketStackToolsTest {

    private HttpUtilCached cache() throws IOException {
        String dir = System.getProperty("java.io.tmpdir")+ File.separator+this.getClass().getSimpleName()+"_"+Math.random();
        new File(dir).mkdirs();
        return new HttpUtilCached(dir);
    }

    @Test
    public void testSearchMarketStack() throws Exception {
        EZShare action = MarketStackTools.searchActionFromMarketstack(new LoggerReporting(), cache(), "WPC");
        assertNotNull(action);
        Assertions.assertEquals("WPC", action.getGoogleCode());
        Assertions.assertEquals("W. P. Carey Inc", action.getEzName());
    }

}
