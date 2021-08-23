package com.pascal.bientotrentier.util;

import com.pascal.bientotrentier.sources.Reporting;

public interface FileLinkCreator {
    String createSourceLink(Reporting reporting, String sourceFile);
}
