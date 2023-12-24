package com.pascal.ezload.service.dashboard.config;

import java.util.Set;

public class ChartShareIndexConfig {

    // Quel index ?
    private ShareIndex shareIndex;

    // Sur quelles actions ?
    private ShareSelection shareSelection;
    private Set<String> additionalShareGoogleCodeList;

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

    public Set<String> getAdditionalShareGoogleCodeList() {
        return additionalShareGoogleCodeList;
    }

    public void setAdditionalShareGoogleCodeList(Set<String> additionalShareGoogleCodeList) {
        this.additionalShareGoogleCodeList = additionalShareGoogleCodeList;
    }

}
