package com.pascal.ezload.service.exporter;

import com.pascal.ezload.service.exporter.ezPortfolio.EZPortfolio;
import com.pascal.ezload.service.exporter.ezPortfolio.MesOperations;
import com.pascal.ezload.service.exporter.ezPortfolio.MonPortefeuille;
import com.pascal.ezload.service.model.EZRetraitFonds;
import com.pascal.ezload.service.model.EZVersementFonds;
import com.pascal.ezload.service.sources.Reporting;

import static com.pascal.ezload.service.util.ModelUtils.str2Float;

public class LoadVirement {
    private Reporting reporting;

    public LoadVirement(Reporting reporting) {
        this.reporting = reporting;
    }

    public void load(EZPortfolio ezPortfolio, EZVersementFonds op){
        MesOperations mesOperations = ezPortfolio.getMesOperations();
        if (!mesOperations.isOperationsExists(op)){
            reporting.info("New operation "+op.getDate()+" "+op.getOperationType()+" "+op.getAmount()+op.getDevise().getSymbol());
            mesOperations.newOperation(op.getDate(), op.getCompteType(), op.getCourtier(), op.getAccountDeclaration(), null,
                    op.getOperationType(), null, null, op.getAmount()+op.getDevise().getSymbol(), op.getDescription());

            MonPortefeuille portefeuille = ezPortfolio.getMonPortefeuille();
            portefeuille.updateLiquidite(str2Float(op.getAmount()));
        }
    }

    public void load(EZPortfolio ezPortfolio, EZRetraitFonds op){
        MesOperations mesOperations = ezPortfolio.getMesOperations();
        if (!mesOperations.isOperationsExists(op)){
            reporting.info("New operation "+op.getDate()+" "+op.getOperationType()+" "+op.getAmount()+op.getDevise().getSymbol());
            mesOperations.newOperation(op.getDate(), op.getCompteType(), op.getCourtier(), op.getAccountDeclaration(), null,
                    op.getOperationType(), null, null, op.getAmount()+op.getDevise().getSymbol(), op.getDescription());

            MonPortefeuille portefeuille = ezPortfolio.getMonPortefeuille();
            portefeuille.updateLiquidite(str2Float(op.getAmount()));
        }
    }

}
