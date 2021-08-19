package com.pascal.bientotrentier.loader;

import com.pascal.bientotrentier.loader.ezPortfolio.EZPortfolio;
import com.pascal.bientotrentier.loader.ezPortfolio.MesOperations;
import com.pascal.bientotrentier.model.BRRetraitFonds;
import com.pascal.bientotrentier.model.BRVersementFonds;
import com.pascal.bientotrentier.sources.Reporting;

public class LoadVirement {
    private Reporting reporting;

    public LoadVirement(Reporting reporting) {
        this.reporting = reporting;
    }

    public void load(EZPortfolio ezPortfolio, BRVersementFonds op){
        MesOperations mesOperations = ezPortfolio.getMesOperations();
        if (!mesOperations.isOperationsExists(op)){
            reporting.info("New operation "+op.getDate()+" "+op.getOperationType()+" "+op.getAmount());
            mesOperations.newOperation(op.getDate(), op.getCompteType(), op.getCourtier(), null, op.getOperationType(), null, null, op.getAmount(), op.getDescription());
        }
    }

    public void load(EZPortfolio ezPortfolio, BRRetraitFonds op){
        MesOperations mesOperations = ezPortfolio.getMesOperations();
        if (!mesOperations.isOperationsExists(op)){
            reporting.info("New operation "+op.getDate()+" "+op.getOperationType()+" "+op.getAmount());
            mesOperations.newOperation(op.getDate(), op.getCompteType(), op.getCourtier(), null, op.getOperationType(), null, null, op.getAmount(), op.getDescription());
        }
    }

}
