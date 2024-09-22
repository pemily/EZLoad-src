/**
 * ezServer - EZLoad an automatic loader for EZPortfolio
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
package com.pascal.ezload.server.httpserver.handler;

import com.pascal.ezload.server.httpserver.EzServerState;
import com.pascal.ezload.service.config.EzProfil;
import com.pascal.ezload.service.config.MainSettings;
import com.pascal.ezload.service.config.SettingsManager;
import com.pascal.ezload.service.exporter.EZPortfolioManager;
import com.pascal.ezload.service.exporter.EZPortfolioProxy;
import com.pascal.ezload.common.sources.Reporting;

public class PortfolioUtil {


    public static EZPortfolioProxy loadOriginalEzPortfolioProxyOrGetFromCache(EzServerState serverState, SettingsManager settingsManager, MainSettings mainSettings, EzProfil ezProfil, Reporting reporting) throws Exception {
        EZPortfolioProxy original = serverState.getOriginalEzPortfolioProxy();
        if (original == null) {
            EZPortfolioManager ezPortfolioManager = new EZPortfolioManager(reporting, settingsManager, mainSettings, ezProfil);
            original = ezPortfolioManager.load(settingsManager, mainSettings);
            serverState.setOriginalEzPortfolioProxy(original);
        }
        EZPortfolioProxy newEZPortfolio = original.createDeepCopy();
        serverState.setEzNewPortfolioProxy(newEZPortfolio);
        return newEZPortfolio;
    }

}
