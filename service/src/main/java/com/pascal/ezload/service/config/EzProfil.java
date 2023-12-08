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
package com.pascal.ezload.service.config;

import com.pascal.ezload.service.exporter.EZPortfolioSettings;
import com.pascal.ezload.service.model.EnumEZBroker;
import com.pascal.ezload.service.sources.bourseDirect.BourseDirectSettings;
import com.pascal.ezload.service.util.Checkable;
import com.pascal.ezload.service.util.FileValue;


public class EzProfil extends Checkable<EzProfil> {

    private BourseDirectSettings bourseDirect;
    private EZPortfolioSettings ezPortfolio;
    private MainSettings.AnnualDividendConfig annualDividend;
    private MainSettings.DividendCalendarConfig dividendCalendar;

    public BourseDirectSettings getBourseDirect() {
        return bourseDirect;
    }

    public void setBourseDirect(BourseDirectSettings bourseDirect) {
        this.bourseDirect = bourseDirect;
    }

    public EZPortfolioSettings getEzPortfolio() {
        return ezPortfolio;
    }

    public void setEzPortfolio(EZPortfolioSettings ezPortfolio) {
        this.ezPortfolio = ezPortfolio;
    }

    public MainSettings.AnnualDividendConfig getAnnualDividend() {
        return annualDividend;
    }

    public void setAnnualDividend(MainSettings.AnnualDividendConfig annualDividend) {
        this.annualDividend = annualDividend;
    }

    public MainSettings.DividendCalendarConfig getDividendCalendar() {
        return dividendCalendar;
    }

    public void setDividendCalendar(MainSettings.DividendCalendarConfig dividendCalendar) {
        this.dividendCalendar = dividendCalendar;
    }

    public String getSourceRef(SettingsManager settingsManager, String ezProfilName, String filePath) {
        String file = filePath.substring(settingsManager.getDownloadDir(ezProfilName, EnumEZBroker.BourseDirect).length()).replace('\\', '/');
        if (file.startsWith("/")) file = file.substring(1);
        return file;
    }


    public EzProfil validate(){
        bourseDirect.validate();
        ezPortfolio.validate();
        return this;
    }

    public void clearErrors(){
        bourseDirect.clearErrors();
        ezPortfolio.clearErrors();
    }

}
