package com.pascal.ezload.service.sources.bourseDirect.transform;

import com.pascal.ezload.service.config.MainSettings;
import com.pascal.ezload.service.model.EZAccountDeclaration;
import com.pascal.ezload.service.model.*;
import com.pascal.ezload.service.model.operations.*;
import com.pascal.ezload.service.parsers.bourseDirect.*;
import com.pascal.ezload.service.sources.Reporting;
import com.pascal.ezload.service.sources.bourseDirect.BourseDirectAnalyser;
import com.pascal.ezload.service.sources.bourseDirect.transform.model.BourseDirectModel;
import com.pascal.ezload.service.util.*;

import java.util.ArrayList;
import java.util.List;


public class BourseDirect2BRModel {

    private Reporting reporting;
    private MainSettings mainSettings;

    public BourseDirect2BRModel(MainSettings mainSettings, Reporting reporting){
        this.reporting = reporting;
        this.mainSettings = mainSettings;
    }
    
    public EZModel create(String sourceFile, EZAccountDeclaration EZAccountDeclaration, BourseDirectModel model, ShareUtil shareUtil) {
        reporting.info("Creating Standard Model...");
        EZModel ezModel = new EZModel(EnumEZBroker.BourseDirect, model.getBrokerFileVersion(), BourseDirectAnalyser.getSourceRef(mainSettings, sourceFile));
        ezModel.setReportDate(model.getDateAvisOperation());

        EZAccount EZAccount = new EZAccount();
        EZAccount.setAccountNumber(model.getAccountNumber());
        EZAccount.setAccountType(model.getAccountType());
        EZAccount.setOwnerAdress(model.getAddress());
        if (!model.getDeviseDebit().equals("€")) throw new BRException("Le compte n'est pas un compte en Euro");
        EZAccount.setDevise(DeviseUtil.foundByCode("EUR"));
        EZAccount.setOwnerName(model.getAccountOwnerName());
        ezModel.setAccount(EZAccount);
        ezModel.setAccountDeclaration(EZAccountDeclaration);

        List<EZOperation> EZOperations = new ArrayList<>();
        ezModel.setOperations(EZOperations);

        for (int i = 0; i < model.getOperations().size(); i++) {
            if (model.getOperations().get(i) instanceof DroitsDeGarde) {
                // the last operation is an DroitsDeGarde and the amount is not in the amounts array, it is in the details text
                EZOperations.add(toBROperation(ezModel, model.getOperations().get(i), model.getDates().get(i), null, shareUtil));
            } else {
                EZOperations.add(toBROperation(ezModel, model.getOperations().get(i), model.getDates().get(i), model.getAmounts().get(i), shareUtil));
            }
        }

        reporting.info("Standard Model => ok");
        return ezModel;
    }

    private EZOperation toBROperation(EZModel EZModel, Operation operation, EZDate date, String amount, ShareUtil shareUtil) {
        EZOperation ezOperation;
        if (operation instanceof VirementEspece){
            VirementEspece op = (VirementEspece) operation;
            if (amount.contains("-")) {
                EZRetraitFonds brOp = new EZRetraitFonds();
                brOp.setDevise(EZModel.getAccount().getDevise());
                ezOperation = brOp;
            }
            else {
                EZVersementFonds brOp = new EZVersementFonds();
                brOp.setDevise(EZModel.getAccount().getDevise());
                ezOperation = brOp;
            }
            ezOperation.setDescription(op.getDetails());
        }
        else if (operation instanceof AchatComptant){
            AchatComptant op = (AchatComptant) operation;
            EZAchat brOp = new EZAchat();
            ezOperation = brOp;
            brOp.setAction(FinanceTools.getInstance().get(reporting, op.getId(), shareUtil));
            brOp.setQuantity(ModelUtils.normalizeNumber(op.getQuantite()));
            brOp.setDescription(op.getHeureExecution()+" "+op.getLieu());
            brOp.setCours(ModelUtils.normalizeAmount(op.getCours()));
            brOp.setTva(ModelUtils.normalizeAmount(op.getTva()));
            brOp.setAmountBrut(ModelUtils.normalizeAmount(op.getBrut()));
            brOp.setFraisCourtage(ModelUtils.normalizeAmount(op.getCourtage()));
        }
        else if (operation instanceof AchatEtranger){
            AchatEtranger op = (AchatEtranger) operation;
            EZAchatEtranger brOp = new EZAchatEtranger();
            ezOperation = brOp;
            brOp.setAction(FinanceTools.getInstance().get(reporting, op.getId(), shareUtil));
            brOp.setQuantity(ModelUtils.normalizeNumber(op.getQuantite()));
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
            EZVenteEtranger brOp = new EZVenteEtranger();
            ezOperation = brOp;
            brOp.setAction(FinanceTools.getInstance().get(reporting, op.getId(), shareUtil));
            brOp.setQuantity(ModelUtils.normalizeNumber(op.getQuantite()));
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
            EZTaxe brOp = new EZTaxe();
            ezOperation = brOp;
            brOp.setAction(FinanceTools.getInstance().get(reporting, op.getId(), shareUtil));
            brOp.setDescription(op.getDetails());
        }
        else if (operation instanceof Coupons){
            Coupons op = (Coupons) operation;
            EZCoupons brOp = new EZCoupons();
            ezOperation = brOp;
            brOp.setAction(FinanceTools.getInstance().get(reporting, op.getId(), shareUtil));
            brOp.setQuantity(ModelUtils.normalizeNumber(op.getQuantite()));
            brOp.setPrixUnitaireBrut(ModelUtils.normalizeAmount(op.getPrixUnitBrut()));
            brOp.setCommission(ModelUtils.normalizeAmount(op.getCommission()));
            brOp.setPrelevement(ModelUtils.normalizeAmount(op.getPrelevement()));
            brOp.setAmountBrut(ModelUtils.normalizeAmount(op.getBrut()));
            brOp.setCreditImpot(ModelUtils.normalizeAmount(op.getCreditImpot()));
            brOp.setContributionSocial(ModelUtils.normalizeAmount(op.getContributionSocial()));
        }
        else if (operation instanceof DroitsDeGarde){
            DroitsDeGarde op = (DroitsDeGarde) operation;
            EZDroitDeGarde brOp = new EZDroitDeGarde();
            ezOperation = brOp;
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
            EZDividendeOptionel brOp = new EZDividendeOptionel();
            ezOperation = brOp;
            brOp.setAction(FinanceTools.getInstance().get(reporting, op.getId(), shareUtil));
            brOp.setQuantity(ModelUtils.normalizeNumber(op.getQuantite()));
            brOp.setCours(ModelUtils.normalizeAmount(op.getCours()));
        }
        else if (operation instanceof EspecesSurOST){
            EspecesSurOST op = (EspecesSurOST) operation;
            EZEspecesSurOST brOp = new EZEspecesSurOST();
            ezOperation = brOp;
            brOp.setAction(FinanceTools.getInstance().get(reporting, op.getId(), shareUtil));
            brOp.setDescription(op.getDetails());
        }
        else {
            reporting.error("Unknown Operation type: "+operation.getClass().getSimpleName());
            throw new BRException("Unknown Operation type: "+operation.getClass().getSimpleName());
        }

        ezOperation.setAmount(ModelUtils.normalizeAmount(amount));
        ezOperation.setDate(date);
        ezOperation.setBroker(EnumEZBroker.BourseDirect);
        ezOperation.setAccount(EZModel.getAccount());
        ezOperation.setEzAccountDeclaration(EZModel.getAccountDeclaration());
        ezOperation.setEzLiquidityName(shareUtil.getEzLiquidityName());

        return ezOperation;
    }

}
