package com.pascal.ezload.service.util;

import com.pascal.ezload.service.sources.Reporting;

public interface FileLinkCreator {
    String createSourceLink(Reporting reporting, String sourceFile);
}
