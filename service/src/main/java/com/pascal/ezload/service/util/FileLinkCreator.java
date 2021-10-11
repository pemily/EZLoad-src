package com.pascal.ezload.service.util;

import com.pascal.ezload.service.sources.Reporting;

public interface FileLinkCreator {
    // return the <a href element as string
    String createSourceLink(Reporting reporting, String sourceFile);
}
