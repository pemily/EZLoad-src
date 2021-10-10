package com.pascal.ezload.service.exporter.ezPortfolio.v5;

import com.pascal.ezload.service.exporter.EZPortfolioProxy;
import com.pascal.ezload.service.exporter.ezEdition.EzEdition;
import com.pascal.ezload.service.exporter.ezPortfolio.v4.EZPortfolio;
import com.pascal.ezload.service.exporter.ezPortfolio.v4.MesOperations;
import com.pascal.ezload.service.exporter.ezPortfolio.v4.MonPortefeuille;
import com.pascal.ezload.service.gdrive.GDriveSheets;
import com.pascal.ezload.service.gdrive.SheetValues;
import com.pascal.ezload.service.model.EZAccountDeclaration;
import com.pascal.ezload.service.model.EZDate;
import com.pascal.ezload.service.model.EZOperation;
import com.pascal.ezload.service.model.EnumEZCourtier;
import com.pascal.ezload.service.sources.Reporting;
import org.apache.commons.lang3.NotImplementedException;

import java.util.List;
import java.util.Optional;

public class EZPorfolioProxyV5 implements EZPortfolioProxy {

    private Reporting reporting;
    private GDriveSheets sheets;
    private EZPortfolio ezPortfolio;

    public EZPorfolioProxyV5(Reporting reporting, GDriveSheets sheets){
        this.reporting = reporting;
        this.sheets = sheets;
    }

    public static boolean isCompatible(Reporting reporting, GDriveSheets sheets) {
        try{
            // en V4 la colonne MesOperations.Periode existe, elle a été renommé en "Quantité" en V5
            SheetValues s = sheets.getCells("MesOperations!D1:D1"); // récupère le nom de la colonne D ligne 1 de MesOperations
            return s.getValues().get(0).valueStr(0).equals("Quantité");
        }
        catch(Exception e){
            return false;
        }
    }

    @Override
    public void load() throws Exception {
        throw new NotImplementedException();
    }

    @Override
    public List<EzEdition> save(List<EzEdition> operationsToAdd){
        throw new NotImplementedException();
    }

    @Override
    public Optional<EZDate> getLastOperationDate(EnumEZCourtier courtier, EZAccountDeclaration account) {
        throw new NotImplementedException();
    }

    @Override
    public boolean isFileAlreadyLoaded(EnumEZCourtier courtier, EZAccountDeclaration account, EZDate pdfDate) {
        throw new NotImplementedException();
    }

    @Override
    public boolean isOperationsExists(EZOperation operation) {
        throw new NotImplementedException();
    }
}
