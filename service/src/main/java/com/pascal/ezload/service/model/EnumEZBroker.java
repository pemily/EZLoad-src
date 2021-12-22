package com.pascal.ezload.service.model;

import com.pascal.ezload.service.exporter.ezEdition.EzData;
import com.pascal.ezload.service.exporter.ezEdition.data.common.BrokerData;
import com.pascal.ezload.service.sources.bourseDirect.selenium.BourseDirectDownloader;

import java.util.Arrays;
import java.util.Optional;

public enum EnumEZBroker implements BrokerData {
    BourseDirect("Bourse Direct", "BourseDirect");

    private String ezPortfolioName, dirName;

    EnumEZBroker(String ezPortfolioName, String dirName){
        this.ezPortfolioName = ezPortfolioName;
        this.dirName = dirName;
    }

    public String getEzPortfolioName(){
        return ezPortfolioName;
    }

    public String getDirName() {
        return dirName;
    }

    public void fill(EzData data) {
        data.put(broker_name, getEzPortfolioName());
        // data.put(broker_dir, getDirName());
    }

}
