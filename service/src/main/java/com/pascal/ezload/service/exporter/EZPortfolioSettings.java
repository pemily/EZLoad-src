package com.pascal.ezload.service.exporter;

import com.pascal.ezload.service.config.SettingsManager;
import com.pascal.ezload.service.util.Checkable;
import com.pascal.ezload.service.util.FileValue;
import com.pascal.ezload.service.util.HttpValue;

public class EZPortfolioSettings extends Checkable<EZPortfolioSettings> {

    public enum Field {gdriveCredsFile, ezPortfolioUrl}

    private String gdriveCredsFile;
    private String ezPortfolioUrl;

    public String getEzPortfolioUrl() {
        return ezPortfolioUrl;
    }

    public void setEzPortfolioUrl(String ezPortfolioUrl) {
        this.ezPortfolioUrl = ezPortfolioUrl == null ? null : ezPortfolioUrl.trim();
    }

    public String getGdriveCredsFile() {
        return gdriveCredsFile;
    }

    public void setGdriveCredsFile(String gdriveCredsFile) {
        this.gdriveCredsFile = gdriveCredsFile == null ? null : gdriveCredsFile.trim();
    }

    @Override
    public EZPortfolioSettings validate() {
        new FileValue(true).validate(this, Field.gdriveCredsFile.name(), gdriveCredsFile);
        new HttpValue(true, SettingsManager.EZPORTFOLIO_GDRIVE_URL_PREFIX).validate(this, Field.ezPortfolioUrl.name(), ezPortfolioUrl);
        return this;
    }

}
