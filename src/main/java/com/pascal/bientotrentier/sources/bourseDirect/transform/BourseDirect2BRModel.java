package com.pascal.bientotrentier.sources.bourseDirect.transform;

import com.pascal.bientotrentier.model.*;
import com.pascal.bientotrentier.parsers.bourseDirect.*;
import com.pascal.bientotrentier.sources.Reporting;
import com.pascal.bientotrentier.sources.bourseDirect.transform.model.BourseDirectModel;
import com.pascal.bientotrentier.util.BRException;
import com.pascal.bientotrentier.util.DeviseUtil;
import com.pascal.bientotrentier.util.FinanceTools;
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
        brModel.setReportDate(model.getDateAvisOperation());
        brModel.setSource(EnumBRCourtier.BourseDirect);
        brModel.setSourceFile(sourceFile);

        BRAccount brAccount = new BRAccount();
        brAccount.setAccountNumber(model.getAccountNumber());
        brAccount.setAccountType(model.getAccountType());
        brAccount.setOwnerAdress(model.getAddress());
        if (!model.getDeviseDebit().equals("€")) throw new BRException("The account is not an Euro account");
        brAccount.setDevise(DeviseUtil.foundByCode("EUR"));
        brAccount.setOwnerName(model.getAccountOwnerName());
        brModel.setAccount(brAccount);

        List<BROperation> brOperations = new ArrayList<>();
        brModel.setOperations(brOperations);

        for (int i = 0; i < model.getOperations().size(); i++) {
            if (model.getOperations().get(i) instanceof DroitsDeGarde) {
                // the last operation is an DroitsDeGarde and the amount is not in the amounts array, it is in the details text
                brOperations.add(toBROperation(brModel, model.getOperations().get(i), model.getDates().get(i), null));
            } else {
                brOperations.add(toBROperation(brModel, model.getOperations().get(i), model.getDates().get(i), model.getAmounts().get(i)));
            }
        }

        reporting.info("Standard Model => ok");
        return brModel;
    }

    private BROperation toBROperation(BRModel brModel, Operation operation, BRDate date, String amount) {
        BROperation brOperation = null;
        if (operation instanceof VirementEspece){
            VirementEspece op = (VirementEspece) operation;
            if (amount.contains("-")) {
                BRRetraitFonds brOp = new BRRetraitFonds();
                brOp.setDevise(brModel.getAccount().getDevise());
                brOperation = brOp;
            }
            else {
                BRVersementFonds brOp = new BRVersementFonds();
                brOp.setDevise(brModel.getAccount().getDevise());
                brOperation = brOp;
            }
            brOperation.setDescription(op.getDetails());
        }
        else if (operation instanceof AchatComptant){
            AchatComptant op = (AchatComptant) operation;
            BRAchat brOp = new BRAchat();
            brOperation = brOp;
            brOp.setAction(FinanceTools.getInstance().get(reporting, op.getId()));
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
            brOp.setAction(FinanceTools.getInstance().get(reporting, op.getId()));
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
            brOp.setAction(FinanceTools.getInstance().get(reporting, op.getId()));
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
            brOp.setAction(FinanceTools.getInstance().get(reporting, op.getId()));
            brOp.setDescription(op.getDetails());
        }
        else if (operation instanceof Coupons){
            Coupons op = (Coupons) operation;
            BRCoupons brOp = new BRCoupons();
            brOperation = brOp;
            brOp.setAction(FinanceTools.getInstance().get(reporting, op.getId()));
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
            brOp.setAction(FinanceTools.getInstance().get(reporting, op.getId()));
            brOp.setNumber(ModelUtils.normalizeNumber(op.getQuantite()));
            brOp.setCours(ModelUtils.normalizeAmount(op.getCours()));
        }
        else if (operation instanceof EspecesSurOST){
            EspecesSurOST op = (EspecesSurOST) operation;
            BREspeceSurOST brOp = new BREspeceSurOST();
            brOperation = brOp;
            brOp.setAction(FinanceTools.getInstance().get(reporting, op.getId()));
            brOp.setDescription(op.getDetails());
        }
        else {
            reporting.error("Unknown Operation type: "+operation.getClass().getSimpleName());
        }

        if (brOperation != null) {
            brOperation.setAmount(ModelUtils.normalizeAmount(amount));
            brOperation.setDate(date);
            brOperation.setCourtier(EnumBRCourtier.BourseDirect);
            if (brModel.getAccount().getAccountType().equals("Ordinaire")){
                brOperation.setCompteType(EnumBRCompteType.COMPTE_TITRES_ORDINAIRE);
            }
            brOperation.setAccount(brModel.getAccount());
            brOperation.setAccountDeclaration(brModel.getAccountDeclaration());

        }
        return brOperation;
    }

}
