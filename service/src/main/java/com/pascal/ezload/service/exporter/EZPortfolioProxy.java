package com.pascal.ezload.service.exporter;

import com.pascal.ezload.service.exporter.ezEdition.EzData;
import com.pascal.ezload.service.exporter.ezEdition.EzPortefeuilleEdition;
import com.pascal.ezload.service.exporter.ezEdition.EzReport;
import com.pascal.ezload.service.exporter.ezEdition.ShareValue;
import com.pascal.ezload.service.exporter.ezPortfolio.v5.PRU;
import com.pascal.ezload.service.gdrive.Row;
import com.pascal.ezload.service.model.EZAccountDeclaration;
import com.pascal.ezload.service.model.EZDate;
import com.pascal.ezload.service.model.EnumEZBroker;
import com.pascal.ezload.service.sources.Reporting;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface EZPortfolioProxy {

    int getEzPortfolioVersion();

    void load(Reporting reporting) throws Exception;

    // return the list of EzEdition operation not saved
    List<EzReport> save(Reporting reporting, List<EzReport> operations, List<String> ignoreOperations) throws Exception;

    Optional<EZDate> getLastOperationDate(EnumEZBroker courtier, EZAccountDeclaration account);

    boolean isFileAlreadyLoaded(EnumEZBroker courtier, EZAccountDeclaration account, EZDate pdfDate);

    boolean isOperationsExists(Row operation);

    void applyOnPortefeuille(EzPortefeuilleEdition ezPortefeuilleEdition);

    void fillFromMonPortefeuille(EzData data, String valeur);

    Set<ShareValue> getShareValues();

    PRU getPRU();

    List<String> getNewPRUValues();

    EZPortfolioProxy createDeepCopy();
}
