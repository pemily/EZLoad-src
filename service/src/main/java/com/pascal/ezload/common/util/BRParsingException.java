/**
 * ezService - EZLoad an automatic loader for EZPortfolio
 * Copyright © 2021 EMILY Pascal (pascal.emily@gmail.com)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.pascal.ezload.common.util;

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
