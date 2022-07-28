package com.pascal.ezload.service.exporter.ezPortfolio.v5;

import com.pascal.ezload.service.exporter.ezEdition.EzPerformanceEdition;
import com.pascal.ezload.service.gdrive.Row;
import com.pascal.ezload.service.gdrive.SheetValues;
import com.pascal.ezload.service.util.ModelUtils;
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
                float newValue = currentValue + ModelUtils.str2Float(ezPerformanceEdition.getValue());
                String newValueStr = ModelUtils.float2Str(newValue);
                performance.getValues().get(0).setValue(INPUT_OUTPUT_COL, newValueStr);
            }
            else if (performance.getValues().size() == 0){
                float newValue = ModelUtils.str2Float(ezPerformanceEdition.getValue());
                String newValueStr = ModelUtils.float2Str(newValue);
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
