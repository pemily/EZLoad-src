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
package com.pascal.ezload.service.exporter.ezPortfolio.v5_v6;

import com.pascal.ezload.service.exporter.ezEdition.EzPerformanceEdition;
import com.pascal.ezload.service.gdrive.Row;
import com.pascal.ezload.service.gdrive.SheetValues;
import com.pascal.ezload.service.util.NumberUtils;
import com.pascal.ezload.service.util.StringUtils;

public class MaPerformance {
    public static final int INPUT_OUTPUT_COL = 0; // 0 car la liste chargé ne contient que la cellule qui doit etre mise a jour
    private final SheetValues performance;

    public MaPerformance(SheetValues performance) {
        this.performance = performance;
    }

    public void updateWith(EzPerformanceEdition ezPerformanceEdition) {
        if (!StringUtils.isBlank(ezPerformanceEdition.getValue())){
            if (performance.getValues().size() == 1) {
                float currentValue = performance.getValues().get(0).getValueFloat(INPUT_OUTPUT_COL);
                float newValue = currentValue + NumberUtils.str2Float(ezPerformanceEdition.getValue());
                String newValueStr = NumberUtils.float2Str(newValue);
                performance.getValues().get(0).setValue(INPUT_OUTPUT_COL, newValueStr);
            }
            else if (performance.getValues().size() == 0){
                float newValue = NumberUtils.str2Float(ezPerformanceEdition.getValue());
                String newValueStr = NumberUtils.float2Str(newValue);
                Row r = new Row(0);
                r.setValue(INPUT_OUTPUT_COL, newValueStr);
                performance.getValues().add(r);
            }
        }
    }

    public MaPerformance createDeepCopy() {
        return new MaPerformance(performance.createDeepCopy());
    }

    public SheetValues getSheetValues() {
        return performance;
    }
}
