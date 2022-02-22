package com.pascal.ezload.service.rules.update;

public class FileStatus {
    private String filepath;
    private FileState fileState;

    public FileStatus(String filepath, FileState fileState) {
        this.filepath = filepath;
        this.fileState = fileState;
    }

    public String getFilepath() {
        return filepath;
    }

    public FileState getFileState() {
        return fileState;
    }

}