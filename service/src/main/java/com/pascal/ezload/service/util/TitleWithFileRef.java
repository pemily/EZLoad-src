package com.pascal.ezload.service.util;

import com.pascal.ezload.service.sources.Reporting;

public interface TitleWithFileRef {

    String format(Reporting rep, FileLinkCreator fileLinkCreator);
}
