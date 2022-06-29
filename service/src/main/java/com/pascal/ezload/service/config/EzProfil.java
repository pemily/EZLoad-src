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
import com.pascal.ezload.service.sources.bourseDirect.BourseDirectSettings;
import com.pascal.ezload.service.util.Checkable;
import com.pascal.ezload.service.util.FileValue;


public class EzProfil extends Checkable<EzProfil> {
    enum Field {downloadDir, courtierCredFile }

    private BourseDirectSettings bourseDirect;
    private EZPortfolioSettings ezPortfolio;
    private String courtierCredsFile;
    private String downloadDir;
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

    public String getCourtierCredsFile() {
        return courtierCredsFile;
    }

    public void setCourtierCredsFile(String courtierCredsFile) {
        this.courtierCredsFile = courtierCredsFile == null ? null : courtierCredsFile.trim();
    }

    public String getDownloadDir() {
        return downloadDir;
    }

    public void setDownloadDir(String downloadDir) {
        this.downloadDir = downloadDir == null ? null : downloadDir.trim();
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

    public String getSourceRef(String filePath) {
        String file = filePath.substring(this.getDownloadDir().length()).replace('\\', '/');
        if (file.startsWith("/")) file = file.substring(1);
        return file;
    }


    public EzProfil validate(){
        new FileValue(this, Field.courtierCredFile.name(), courtierCredsFile).checkRequired().checkFile();
        new FileValue(this, Field.downloadDir.name(), downloadDir).checkRequired().checkDirectory();
        bourseDirect.validate();
        ezPortfolio.validate();
        return this;
    }

    public void clearErrors(){
        bourseDirect.clearErrors();
        ezPortfolio.clearErrors();
    }

}
