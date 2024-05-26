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

import com.pascal.ezload.service.dashboard.config.ImpotChartSettings;

import java.util.LinkedList;
import java.util.List;

public class ImpotChart extends ImpotChartSettings {

    private List<ImpotAnnuel> impotAnnuels = new LinkedList<>();

    public ImpotChart(){}
    public ImpotChart(ImpotChartSettings chartSettings) {
        this.setTitle(chartSettings.getTitle());
        this.setEzPortfolioDeviseCode(chartSettings.getEzPortfolioDeviseCode());
    }

    public List<ImpotAnnuel> getImpotAnnuels() {
        return impotAnnuels;
    }

    public void setImpotAnnuels(List<ImpotAnnuel> impotAnnuels) {
        this.impotAnnuels = impotAnnuels;
    }

    public static class ImpotAnnuel {
        private int year;
        private String declaration;

        public int getYear() {
            return year;
        }

        public void setYear(int year) {
            this.year = year;
        }

        public String getDeclaration() {
            return declaration;
        }

        public void setDeclaration(String declaration) {
            this.declaration = declaration;
        }
    }
}
