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
package com.pascal.ezload.service.dashboard;

import com.pascal.ezload.service.dashboard.config.DashboardSettings;

import java.util.List;

public class DashboardData {

    private List<Chart> charts;
    private DashboardSettings dashboardSettings;
    private List<EzShareData> shareGoogleCodeAndNames;

    public List<Chart> getCharts() {
        return charts;
    }

    public void setCharts(List<Chart> charts) {
        this.charts = charts;
    }

    public DashboardSettings getDashboardSettings() {
        return dashboardSettings;
    }

    public void setDashboardSettings(DashboardSettings dashboardSettings) {
        this.dashboardSettings = dashboardSettings;
    }

    public List<EzShareData> getShareGoogleCodeAndNames() {
        return shareGoogleCodeAndNames;
    }

    public void setShareGoogleCodeAndNames(List<EzShareData> shareGoogleCodeAndNames) {
        this.shareGoogleCodeAndNames = shareGoogleCodeAndNames;
    }

    public static class EzShareData {
        private String googleCode, shareName;

        public EzShareData(String googleCode, String shareName){
            this.googleCode = googleCode;
            this.shareName = shareName;
        }

        public EzShareData(){}

        public String getShareName() {
            return shareName;
        }

        public void setShareName(String shareName) {
            this.shareName = shareName;
        }

        public String getGoogleCode() {
            return googleCode;
        }

        public void setGoogleCode(String googleCode) {
            this.googleCode = googleCode;
        }
    }
}
