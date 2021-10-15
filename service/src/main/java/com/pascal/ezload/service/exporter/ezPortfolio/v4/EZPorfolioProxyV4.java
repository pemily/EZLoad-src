package com.pascal.ezload.service.exporter.ezPortfolio.v4;

import com.pascal.ezload.service.exporter.EZPortfolioProxy;
import com.pascal.ezload.service.exporter.ezEdition.EzReport;
import com.pascal.ezload.service.gdrive.GDriveSheets;
import com.pascal.ezload.service.gdrive.Row;
import com.pascal.ezload.service.model.EZAccountDeclaration;
import com.pascal.ezload.service.model.EZDate;
import com.pascal.ezload.service.model.EnumEZBroker;
import com.pascal.ezload.service.sources.Reporting;
import org.apache.commons.lang3.NotImplementedException;

import java.util.List;
import java.util.Optional;

public class EZPorfolioProxyV4 implements EZPortfolioProxy {


    public EZPorfolioProxyV4(Reporting reporting, GDriveSheets sheets){
    }

    @Override
    public int getEzPortfolioVersion() {
        return 4;
    }

    @Override
    public void load() throws Exception {
        throw new NotImplementedException();
    }

    @Override
    public List<EzReport> save(List<EzReport> operationsToAdd){
        throw new NotImplementedException();
    }

    @Override
    public Optional<EZDate> getLastOperationDate(EnumEZBroker courtier, EZAccountDeclaration account) {
        throw new NotImplementedException();
    }

    @Override
    public boolean isFileAlreadyLoaded(EnumEZBroker courtier, EZAccountDeclaration account, EZDate pdfDate) {
        throw new NotImplementedException();
    }

    @Override
    public boolean isOperationsExists(Row operation) {
        throw new NotImplementedException();
    }

    @Override
    public Optional<Row> searchPortefeuilleRow(String valeur) {
        throw new NotImplementedException();
    }

    @Override
    public Row getNewPortefeuilleRow(String valeur) {
        throw new NotImplementedException();
    }
}