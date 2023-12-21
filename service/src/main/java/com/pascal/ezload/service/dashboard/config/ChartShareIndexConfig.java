package com.pascal.ezload.service.dashboard.config;

import java.util.Set;

public class ChartShareIndexConfig {

    // Quel index ?
    private ShareIndex shareIndex;

    // Sur quelles actions ?
    private ShareSelection shareSelection;
    private Set<String> additionalShareList;

    // Quel settings ? peux etre null
    private ChartPerfSettings perfSettings;

    public ShareIndex getShareIndex() {
        return shareIndex;
    }

    public void setShareIndex(ShareIndex shareIndex) {
        this.shareIndex = shareIndex;
    }

    public ShareSelection getShareSelection() {
        return shareSelection;
    }

    public void setShareSelection(ShareSelection shareSelection) {
        this.shareSelection = shareSelection;
    }

    public Set<String> getAdditionalShareList() {
        return additionalShareList;
    }

    public void setAdditionalShareList(Set<String> additionalShareList) {
        this.additionalShareList = additionalShareList;
    }

    public ChartPerfSettings getPerfSettings() {
        return perfSettings;
    }

    public void setPerfSettings(ChartPerfSettings perfSettings) {
        this.perfSettings = perfSettings;
    }
}
