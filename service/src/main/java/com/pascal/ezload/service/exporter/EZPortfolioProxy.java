package com.pascal.ezload.service.exporter;

import com.pascal.ezload.service.exporter.ezEdition.EzReport;
import com.pascal.ezload.service.gdrive.Row;
import com.pascal.ezload.service.model.EZAccountDeclaration;
import com.pascal.ezload.service.model.EZDate;
import com.pascal.ezload.service.model.EZOperation;
import com.pascal.ezload.service.model.EnumEZCourtier;

import java.util.List;
import java.util.Optional;

public interface EZPortfolioProxy {

    void load() throws Exception;
    // return the list of EzEdition operation not saved
    List<EzReport> save(List<EzReport> operations) throws Exception;

    Optional<EZDate> getLastOperationDate(EnumEZCourtier courtier, EZAccountDeclaration account);

    boolean isFileAlreadyLoaded(EnumEZCourtier courtier, EZAccountDeclaration account, EZDate pdfDate);

    boolean isOperationsExists(Row operation);
}
