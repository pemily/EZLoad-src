package com.pascal.ezload.service.exporter;

import com.pascal.ezload.service.config.SettingsManager;
import com.pascal.ezload.service.util.Checkable;
import com.pascal.ezload.service.util.FileValue;
import com.pascal.ezload.service.util.StringValue;

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
        new FileValue(this, Field.gdriveCredsFile.name(), gdriveCredsFile).checkRequired().checkFile();
        new StringValue(this, Field.ezPortfolioUrl.name(), ezPortfolioUrl).checkRequired().checkPrefixMatch(SettingsManager.EZPORTFOLIO_GDRIVE_URL_PREFIX);
        return this;
    }

}
