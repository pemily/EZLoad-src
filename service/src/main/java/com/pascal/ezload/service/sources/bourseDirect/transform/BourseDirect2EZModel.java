package com.pascal.ezload.service.sources.bourseDirect.transform;

import com.pascal.ezload.service.config.EzProfil;
import com.pascal.ezload.service.model.EZAccountDeclaration;
import com.pascal.ezload.service.model.*;
import com.pascal.ezload.service.sources.Reporting;
import com.pascal.ezload.service.sources.bourseDirect.BourseDirectAnalyser;
import com.pascal.ezload.service.sources.bourseDirect.transform.model.BourseDirectModel;
import com.pascal.ezload.service.sources.bourseDirect.transform.model.BourseDirectOperation;
import com.pascal.ezload.service.util.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class BourseDirect2EZModel {

    private Reporting reporting;
    private EzProfil ezProfil;

    public BourseDirect2EZModel(EzProfil ezProfil, Reporting reporting){
        this.reporting = reporting;
        this.ezProfil = ezProfil;
    }
    
    public EZModel create(String sourceFile, EZAccountDeclaration EZAccountDeclaration, BourseDirectModel model) throws IOException {
        reporting.info("Creating Standard Model...");
        EZModel ezModel = new EZModel(EnumEZBroker.BourseDirect, model.getBrokerFileVersion(), BourseDirectAnalyser.getSourceRef(ezProfil, sourceFile));
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
            EZOperations.add(toEZOperation(ezModel, model.getOperations().get(i)));
        }

        reporting.info("Standard Model => ok");
        return ezModel;
    }

    private EZOperation toEZOperation(EZModel EZModel, BourseDirectOperation operation) {
        EZOperation ezOperation = new EZOperation();
        ezOperation.setFields(operation.getFields());
        ezOperation.setDesignation(operation.getOperationDescription());
        ezOperation.setDate(operation.getDate());
        ezOperation.setBroker(EnumEZBroker.BourseDirect);
        ezOperation.setAccount(EZModel.getAccount());
        ezOperation.setEzAccountDeclaration(EZModel.getAccountDeclaration());

        return ezOperation;
    }

}
