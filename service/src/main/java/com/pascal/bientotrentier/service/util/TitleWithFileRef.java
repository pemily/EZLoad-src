package com.pascal.bientotrentier.service.util;

import com.pascal.bientotrentier.service.sources.Reporting;

public interface TitleWithFileRef {

    String format(Reporting rep, FileLinkCreator fileLinkCreator);
}
