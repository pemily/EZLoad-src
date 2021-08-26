package com.pascal.bientotrentier.service.util;

import com.pascal.bientotrentier.service.sources.Reporting;

public interface FileLinkCreator {
    String createSourceLink(Reporting reporting, String sourceFile);
}
