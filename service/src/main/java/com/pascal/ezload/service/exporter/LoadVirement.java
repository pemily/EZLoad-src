package com.pascal.ezload.service.exporter;

import com.pascal.ezload.service.exporter.ezEdition.EzEdition;
import com.pascal.ezload.service.exporter.ezEdition.EzOperationEdition;
import com.pascal.ezload.service.exporter.ezEdition.EzPortefeuilleEdition;
import com.pascal.ezload.service.model.*;
import com.pascal.ezload.service.sources.Reporting;

import static com.pascal.ezload.service.util.ModelUtils.str2Float;

public class LoadVirement {
    private Reporting reporting;

    public LoadVirement(Reporting reporting) {
        this.reporting = reporting;
    }

    public EzEdition load(EZModel fromEzModel, EZVersementFonds op){
        EzOperationEdition ezOperationEdition = new EzOperationEdition(op.getDate(), op.getCompteType(), op.getCourtier(), op.getAccountDeclaration(), null,
                op.getOperationType(), null, null, op.getAmount()+op.getDevise().getSymbol(), op.getDescription());
        EzPortefeuilleEdition ezPortefeuilleEdition = new EzPortefeuilleEdition(EzPortefeuilleEdition.LIQUIDITE_ACTION, str2Float(op.getAmount()));
        return new EzEdition(fromEzModel, op, ezOperationEdition, ezPortefeuilleEdition);
    }

    public EzEdition load(EZModel fromEzModel, EZRetraitFonds op){
        EzOperationEdition ezOperationEdition = new EzOperationEdition(op.getDate(), op.getCompteType(), op.getCourtier(), op.getAccountDeclaration(), null,
                op.getOperationType(), null, null, op.getAmount()+op.getDevise().getSymbol(), op.getDescription());
        EzPortefeuilleEdition ezPortefeuilleEdition = new EzPortefeuilleEdition(EzPortefeuilleEdition.LIQUIDITE_ACTION, str2Float(op.getAmount()));
        return new EzEdition(fromEzModel, op, ezOperationEdition, ezPortefeuilleEdition);
    }

}
