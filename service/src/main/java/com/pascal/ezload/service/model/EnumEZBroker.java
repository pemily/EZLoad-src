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
package com.pascal.ezload.service.model;

import com.pascal.ezload.service.exporter.ezEdition.EzData;
import com.pascal.ezload.service.exporter.ezEdition.data.common.BrokerData;

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
