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
