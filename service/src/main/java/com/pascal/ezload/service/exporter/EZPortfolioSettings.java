package com.pascal.ezload.service.exporter;

import com.pascal.ezload.service.util.Checkable;
import com.pascal.ezload.service.util.FileValue;
import com.pascal.ezload.service.util.StringValue;

public class EZPortfolioSettings extends Checkable {

    public enum Field {gdriveCredsFile, ezPortfolioId}

    private String gdriveCredsFile;
    private String ezPortfolioId;

    public String getEzPortfolioId() {
        return ezPortfolioId;
    }

    public void setEzPortfolioId(String ezPortfolioId) {
        this.ezPortfolioId = ezPortfolioId == null ? null : ezPortfolioId.trim();
    }

    public String getGdriveCredsFile() {
        return gdriveCredsFile;
    }

    public void setGdriveCredsFile(String gdriveCredsFile) {
        this.gdriveCredsFile = gdriveCredsFile == null ? null : gdriveCredsFile.trim();
    }

    @Override
    public void validate() {
        new FileValue(true).validate(this, Field.gdriveCredsFile.name(), gdriveCredsFile);
        new StringValue(true).validate(this, Field.ezPortfolioId.name(), ezPortfolioId);
    }

}
