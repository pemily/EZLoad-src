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
package com.pascal.ezload.server.httpserver;

import com.pascal.ezload.service.dashboard.DashboardData;
import com.pascal.ezload.service.exporter.EZPortfolioProxy;
import com.pascal.ezload.service.exporter.ezEdition.EzReport;

import java.util.LinkedList;
import java.util.List;

public class EzServerState {
    private boolean processRunning = false;

    private List<EzReport> ezReports = new LinkedList<>();
    private List<String> filesNotYetLoaded = new LinkedList<>();
    private List<String> detailedActionErrors = new LinkedList<>();

    private EZPortfolioProxy ezOriginalPortfolioProxy; // cached, if null will be loaded from google drive, Do not modified it, it must kept in read only mode
    private EZPortfolioProxy ezNewPortfolioProxy; // cached, if null will be loaded from google drive, it is used to contains the updates
    private boolean ezActionDirty; // if the user changed the name of an action
    private DashboardData dashboardData;

    public boolean isProcessRunning() {
        return processRunning;
    }

    public void setProcessRunning(boolean processRunning){
        this.processRunning = processRunning;
    }

    public List<EzReport> getEzReports() {
        return ezReports;
    }

    public void setEzReports(List<EzReport> ezReports) {
        this.ezReports = ezReports;
    }

    public List<String> getFilesNotYetLoaded() {
        return filesNotYetLoaded;
    }

    public void setFilesNotYetLoaded(List<String> filesNotYetLoaded) {
        this.filesNotYetLoaded = filesNotYetLoaded;
    }

    public EZPortfolioProxy getOriginalEzPortfolioProxy() {
        return ezOriginalPortfolioProxy;
    }

    public void setOriginalEzPortfolioProxy(EZPortfolioProxy ezPortfolioProxy) {
        this.ezOriginalPortfolioProxy = ezPortfolioProxy;
    }

    public boolean isEzActionDirty() {
        return ezActionDirty;
    }

    public void setEzActionDirty(boolean ezActionDirty) {
        this.ezActionDirty = ezActionDirty;
    }

    public void clear(){
        ezOriginalPortfolioProxy = null;
        processRunning = false;
        ezActionDirty = false;
        ezReports = new LinkedList<>();
        filesNotYetLoaded = new LinkedList<>();
        detailedActionErrors = new LinkedList<>();
        dashboardData = null;
    }

    public EZPortfolioProxy getEzNewPortfolioProxy() {
        return ezNewPortfolioProxy;
    }

    public void setEzNewPortfolioProxy(EZPortfolioProxy ezNewPortfolioProxy) {
        this.ezNewPortfolioProxy = ezNewPortfolioProxy;
    }

    public DashboardData getDashboardData() {
        return dashboardData;
    }

    public void setDashboardData(DashboardData dashboardData) {
        this.dashboardData = dashboardData;
    }

    public List<String> getDetailedActionErrors() {
        return detailedActionErrors;
    }

    public void setDetailedActionErros(List<String> detailedActionErrors) {
        this.detailedActionErrors = detailedActionErrors;
    }
}
