package com.pascal.ezload.service.dashboard.config;

public class ChartPerfSettings {

    private ChartPerfGroupedBy perfGroupedBy; // calcul: la valeur du Price à la date n - la valeur du Price à la date n-1
    private ChartPerfFilter perfFilter; // Transformé en % ou reste en valeur ?

    public ChartPerfFilter getPerfFilter() {
        return perfFilter;
    }

    public void setPerfFilter(ChartPerfFilter perfFilter) {
        this.perfFilter = perfFilter;
    }

    public ChartPerfGroupedBy getPerfGroupedBy() {
        return perfGroupedBy;
    }

    public void setPerfGroupedBy(ChartPerfGroupedBy perfGroupedBy) {
        this.perfGroupedBy = perfGroupedBy;
    }

    public boolean correctlyDefined() {
        return perfGroupedBy != null && perfFilter != null;
    }
}
