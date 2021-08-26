package com.pascal.bientotrentier.service.exporter;

import com.pascal.bientotrentier.service.exporter.ezPortfolio.EZPortfolio;
import com.pascal.bientotrentier.service.exporter.ezPortfolio.MesOperations;
import com.pascal.bientotrentier.service.exporter.ezPortfolio.MonPortefeuille;
import com.pascal.bientotrentier.service.model.BRRetraitFonds;
import com.pascal.bientotrentier.service.model.BRVersementFonds;
import com.pascal.bientotrentier.service.sources.Reporting;

import static com.pascal.bientotrentier.service.util.ModelUtils.str2Float;

public class LoadVirement {
    private Reporting reporting;

    public LoadVirement(Reporting reporting) {
        this.reporting = reporting;
    }

    public void load(EZPortfolio ezPortfolio, BRVersementFonds op){
        MesOperations mesOperations = ezPortfolio.getMesOperations();
        if (!mesOperations.isOperationsExists(op)){
            reporting.info("New operation "+op.getDate()+" "+op.getOperationType()+" "+op.getAmount()+op.getDevise().getSymbol());
            mesOperations.newOperation(op.getDate(), op.getCompteType(), op.getCourtier(), op.getAccountDeclaration(), null,
                    op.getOperationType(), null, null, op.getAmount()+op.getDevise().getSymbol(), op.getDescription());

            MonPortefeuille portefeuille = ezPortfolio.getMonPortefeuille();
            portefeuille.updateLiquidite(str2Float(op.getAmount()));
        }
    }

    public void load(EZPortfolio ezPortfolio, BRRetraitFonds op){
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
