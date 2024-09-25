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
package com.pascal.ezload.service.dashboard.engine.builder;

import com.pascal.ezload.common.model.PriceAtDate;
import com.pascal.ezload.service.model.Prices;

public class CumulBuilder {

    public static Prices applyCumul(Prices p){
        Prices r = new Prices();
        r.setLabel(p.getLabel()+" cumulé");
        r.setDevise(p.getDevise());

        float cumul = 0;
        for (PriceAtDate pd : p.getPrices()) {
            float value = pd.getValue() == null ? 0 : pd.getValue();
            cumul = value + cumul;
            r.addPrice(new PriceAtDate(pd.getDate(), cumul, pd.isEstimated()));
        }
        return r;
    }

}
