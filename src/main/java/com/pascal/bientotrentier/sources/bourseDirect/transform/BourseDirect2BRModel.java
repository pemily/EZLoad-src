package com.pascal.bientotrentier.sources.bourseDirect.transform;

import com.pascal.bientotrentier.model.*;
import com.pascal.bientotrentier.parsers.bourseDirect.*;
import com.pascal.bientotrentier.sources.Reporting;
import com.pascal.bientotrentier.sources.bourseDirect.transform.model.BourseDirectModel;
import com.pascal.bientotrentier.util.ModelUtils;

import java.util.ArrayList;
import java.util.List;

public class BourseDirect2BRModel {

    public Reporting reporting;

    public BourseDirect2BRModel(Reporting reporting){
        this.reporting = reporting;
    }
    
    public BRModel create(String sourceFile, BourseDirectModel model) {
        reporting.info("Creating Standard Model...");
        BRModel brModel = new BRModel();
        brModel.setReportDate(ModelUtils.normalizeDate(model.getDateAvisOperation()));
        brModel.setSource(BRModel.SourceModel.BOURSE_DIRECT);
        brModel.setSourceFile(sourceFile);

        BRAccount brAccount = new BRAccount();
        brAccount.setAccountNumber(model.getAccountNumber());
        brAccount.setAccountType(model.getAccountType());
        brAccount.setOwnerAdress(model.getAddress());
        brAccount.setDevise(model.getDeviseDebit());
        brAccount.setOwnerName(model.getAccountOwnerName());
        brModel.setAccount(brAccount);

        List<BROperation> brOperations = new ArrayList<>();
        brModel.setOperations(brOperations);

        for (int i = 0; i < model.getOperations().size(); i++) {
            if (model.getOperations().get(i) instanceof DroitsDeGarde) {
                // the last operation is an DroitsDeGarde and the amount is not in the amounts array, it is in the details text
                brOperations.add(toBROperation(model.getDates().get(i), model.getOperations().get(i), null));
            } else {
                brOperations.add(toBROperation(model.getDates().get(i), model.getOperations().get(i), model.getAmounts().get(i)));
            }
        }
        reporting.info("Standard Model => ok");
        return brModel;
    }

    private BROperation toBROperation(String date, Operation operation, String amount) {
        BROperation brOperation = null;
        if (operation instanceof VirementEspece){
            VirementEspece op = (VirementEspece) operation;
            BRVirementCptEspece brOp = new BRVirementCptEspece();
            brOperation = brOp;
            brOp.setDescription(op.getDetails());
        }
        else if (operation instanceof AchatComptant){
            AchatComptant op = (AchatComptant) operation;
            BRAchat brOp = new BRAchat();
            brOperation = brOp;
            brOp.setActionName(op.getActionName());
            brOp.setId(op.getId());
            brOp.setNumber(ModelUtils.normalizeNumber(op.getQuantite()));
            brOp.setDescription(op.getHeureExecution()+" "+op.getLieu());
            brOp.setCours(ModelUtils.normalizeAmount(op.getCours()));
            brOp.setTva(ModelUtils.normalizeAmount(op.getTva()));
            brOp.setAmountBrut(ModelUtils.normalizeAmount(op.getBrut()));
            brOp.setFraisCourtage(ModelUtils.normalizeAmount(op.getCourtage()));
        }
        else if (operation instanceof AchatEtranger){
            AchatEtranger op = (AchatEtranger) operation;
            BRAchatEtranger brOp = new BRAchatEtranger();
            brOperation = brOp;
            brOp.setActionName(op.getActionName());
            brOp.setId(op.getId());
            brOp.setNumber(ModelUtils.normalizeNumber(op.getQuantite()));
            brOp.setChangeRate(ModelUtils.normalizeAmount(op.getTxUSDvsEUR()));
            brOp.setDescription(op.getHeureExecution()+" "+op.getLieu());
            brOp.setCours(ModelUtils.normalizeAmount(op.getCours()));
            brOp.setTva(ModelUtils.normalizeAmount(op.getTva()));
            brOp.setAmountBrut(ModelUtils.normalizeAmount(op.getBrut()));
            brOp.setFraisCourtage(ModelUtils.normalizeAmount(op.getCourtage()));
            brOp.setCoursUSD(ModelUtils.normalizeAmount(op.getCoursUSD()));
        }
        else if (operation instanceof VenteEtranger){
            VenteEtranger op = (VenteEtranger) operation;
            BRVenteEtranger brOp = new BRVenteEtranger();
            brOperation = brOp;
            brOp.setActionName(op.getActionName());
            brOp.setId(op.getId());
            brOp.setNumber(ModelUtils.normalizeNumber(op.getQuantite()));
            brOp.setChangeRate(ModelUtils.normalizeAmount(op.getTxUSDvsEUR()));
            brOp.setDescription(op.getHeureExecution()+" "+op.getLieu());
            brOp.setCours(ModelUtils.normalizeAmount(op.getCours()));
            brOp.setTva(ModelUtils.normalizeAmount(op.getTva()));
            brOp.setAmountBrut(ModelUtils.normalizeAmount(op.getBrut()));
            brOp.setFraisCourtage(ModelUtils.normalizeAmount(op.getCourtage()));
            brOp.setCoursUSD(ModelUtils.normalizeAmount(op.getCoursUSD()));
        }
        else if (operation instanceof TaxeTransatFinancieres){
            TaxeTransatFinancieres op = (TaxeTransatFinancieres) operation;
            BRTaxe brOp = new BRTaxe();
            brOperation = brOp;
            brOp.setId(op.getId());
            brOp.setDescription(op.getDetails());
        }
        else if (operation instanceof Coupons){
            Coupons op = (Coupons) operation;
            BRCoupons brOp = new BRCoupons();
            brOperation = brOp;
            brOp.setActionName(op.getActionName());
            brOp.setId(op.getId());
            brOp.setNumber(ModelUtils.normalizeNumber(op.getQuantite()));
            brOp.setPrixUnitaireBrut(ModelUtils.normalizeAmount(op.getPrixUnitBrut()));
            brOp.setCommission(ModelUtils.normalizeAmount(op.getCommission()));
            brOp.setPrelevement(ModelUtils.normalizeAmount(op.getPrelevement()));
            brOp.setAmountBrut(ModelUtils.normalizeAmount(op.getBrut()));
            brOp.setCreditImpot(ModelUtils.normalizeAmount(op.getCreditImpot()));
            brOp.setContributionSocial(ModelUtils.normalizeAmount(op.getContributionSocial()));
        }
        else if (operation instanceof DroitsDeGarde){
            DroitsDeGarde op = (DroitsDeGarde) operation;
            BRDroitDeGarde brOp = new BRDroitDeGarde();
            brOperation = brOp;
            String[] details = op.getDetails().split(" ");
            // the amount is the last figure in the details
            amount = details[details.length-1];
            String description = "";
            for (int i = 0; i < details.length-1; i++){
                description += details[i];
            }
            brOp.setDescription(description);
        }
        else if (operation instanceof DividendeOptionnel){
            DividendeOptionnel op = (DividendeOptionnel) operation;
            BRDividendeOptionel brOp = new BRDividendeOptionel();
            brOperation = brOp;
            brOp.setActionName(op.getActionName());
            brOp.setId(op.getId());
            brOp.setNumber(ModelUtils.normalizeNumber(op.getQuantite()));
            brOp.setCours(ModelUtils.normalizeAmount(op.getCours()));
        }
        else if (operation instanceof EspecesSurOST){
            EspecesSurOST op = (EspecesSurOST) operation;
            BREspeceSurOST brOp = new BREspeceSurOST();
            brOperation = brOp;
            brOp.setId(op.getId());
            brOp.setDescription(op.getDetails());
        }
        else {
            reporting.error("Unkown Operation type: "+operation.getClass().getSimpleName());
        }

        if (brOperation != null) {
            brOperation.setAmount(ModelUtils.normalizeAmount(amount));
            brOperation.setDate(ModelUtils.normalizeDate(date));
            brOperation.setOperationType(brOperation.getClass().getSimpleName());
        }
        return brOperation;
    }
}
