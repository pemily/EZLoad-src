package com.pascal.ezload.service.rules.update;

import com.pascal.ezload.service.model.EnumEZBroker;

public class RemoteFile {
    private final String filename;
    private final EnumEZBroker broker;
    private final int brokerFileVersion;
    private final String downloadUrl;

    public RemoteFile(String name, EnumEZBroker broker, int brokerFileVersion, String downloadUrl) {
        this.filename = name;
        this.broker = broker;
        this.downloadUrl = downloadUrl;
        this.brokerFileVersion = brokerFileVersion;
    }

    public String getFilename() {
        return filename;
    }

    public EnumEZBroker getBroker() {
        return broker;
    }

    public int getBrokerFileVersion() {
        return brokerFileVersion;
    }

    public String getDownloadUrl(){
        return downloadUrl;
    }

}
