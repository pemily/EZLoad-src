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
package com.pascal.ezload.service.dashboard.config;

public class ChartPerfSettings {

    private ChartGroupedBy perfGroupedBy; // calcul: la valeur du Price à la date n - la valeur du Price à la date n-1
    private ChartPerfFilter perfFilter; // Transformé en % ou reste en valeur ?

    public ChartPerfFilter getPerfFilter() {
        return perfFilter;
    }

    public void setPerfFilter(ChartPerfFilter perfFilter) {
        this.perfFilter = perfFilter;
    }

    public ChartGroupedBy getPerfGroupedBy() {
        return perfGroupedBy;
    }

    public void setPerfGroupedBy(ChartGroupedBy perfGroupedBy) {
        this.perfGroupedBy = perfGroupedBy;
    }

    public boolean correctlyDefined() {
        return perfGroupedBy != null && perfFilter != null;
    }
}
