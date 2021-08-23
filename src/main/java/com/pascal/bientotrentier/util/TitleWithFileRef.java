package com.pascal.bientotrentier.util;

import com.pascal.bientotrentier.sources.Reporting;

public interface TitleWithFileRef {

    String format(Reporting rep, FileLinkCreator fileLinkCreator);
}
