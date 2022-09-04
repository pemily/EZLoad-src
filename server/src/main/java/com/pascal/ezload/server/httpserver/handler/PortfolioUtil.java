package com.pascal.ezload.server.httpserver.handler;

import com.pascal.ezload.server.httpserver.EzServerState;
import com.pascal.ezload.service.config.EzProfil;
import com.pascal.ezload.service.config.MainSettings;
import com.pascal.ezload.service.exporter.EZPortfolioManager;
import com.pascal.ezload.service.exporter.EZPortfolioProxy;
import com.pascal.ezload.service.sources.Reporting;

public class PortfolioUtil {


    public static EZPortfolioProxy loadOriginalEzPortfolioProxyOrGetFromCache(EzServerState serverState, MainSettings mainSettings, EzProfil ezProfil, Reporting reporting) throws Exception {
        EZPortfolioProxy original = serverState.getOriginalEzPortfolioProxy();
        if (original == null) {
            EZPortfolioManager ezPortfolioManager = new EZPortfolioManager(reporting, ezProfil);
            original = ezPortfolioManager.load(mainSettings);
            serverState.setOriginalEzPortfolioProxy(original);
        }
        EZPortfolioProxy newEZPortfolio = original.createDeepCopy();
        serverState.setEzNewPortfolioProxy(newEZPortfolio);
        return newEZPortfolio;
    }

}
