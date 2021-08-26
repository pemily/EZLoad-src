package com.pascal.bientotrentier.service.util;

public class BRParsingException extends BRException {

    private String filePath;
    private String fileContent;
    private String analyzedText;

    public BRParsingException(Throwable t) {
        super(t);
    }


    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getFileContent() {
        return fileContent;
    }

    public void setFileContent(String fileContent) {
        this.fileContent = fileContent;
    }

    public String getAnalyzedText() {
        return analyzedText;
    }

    public void setAnalyzedText(String analyzedText) {
        this.analyzedText = analyzedText;
    }

    @Override
    public String getMessage() {
        return  ifNotNull("Error: ", getCause().getMessage(), false)
                +ifNotNull("File: ", filePath, false);
                // +ifNotNull("Parsed text: ", analyzedText, true)
                // +ifNotNull("Full File content: ", fileContent, true);
    }

    private String ifNotNull(String prefix, String data, boolean useLargeSeparator){
        if (data != null){
            String result = prefix;
            if (useLargeSeparator) result += "\n###############################################################################\n"+data+"\n###############################################################################";
            else result += data;
            return result+"\n";
        }
        return "";
    }
}
