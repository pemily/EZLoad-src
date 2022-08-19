package com.pascal.ezload.service.config;

import com.pascal.ezload.service.exporter.ezEdition.EzData;
import com.pascal.ezload.service.exporter.ezEdition.ShareValue;
import com.pascal.ezload.service.model.EZShare;
import com.pascal.ezload.service.model.EnumEZBroker;
import com.pascal.ezload.service.sources.Reporting;
import com.pascal.ezload.service.util.CountryUtil;
import com.pascal.ezload.service.util.finance.BourseDirectFinanceTools;
import com.pascal.ezload.service.util.finance.FinanceTools;
import com.pascal.ezload.service.util.JsonUtil;
import com.pascal.ezload.service.util.StringUtils;
import com.pascal.ezload.service.util.finance.YahooFinanceTools;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class EZActionManager {

    private final String cacheDir;
    private final String shareDataFile;
    private ShareDataFileContent ezShareData;

    public EZActionManager(String cacheDir, String shareDataFile) throws IOException {
        this.cacheDir = cacheDir;
        this.shareDataFile = shareDataFile;
        loadEZActions();
    }


    public void createIfNeeded(ShareValue sv) throws IOException {
        if (sv.getTickerCode() != null && sv.getTickerCode().equals(ShareValue.LIQUIDITY_CODE))
            return;

        if (StringUtils.isBlank(sv.getTickerCode())){
            throw new RuntimeException("Il n'y a pas de Ticker Google dans votre EzPortfolio pour l'action "+sv.getUserShareName());
        }
        Optional<EZShare> action = getFromGoogleTicker(sv.getTickerCode());
        if (!action.isPresent()){
            EZShare newAction = new EZShare();
            newAction.setIsin(null);
            newAction.setDescription(EZShare.NEW_SHARE);
            newAction.setCountryCode(CountryUtil.foundByName(sv.getCountryName()).getCode());
            newAction.setType(sv.getShareType());
            newAction.setGoogleCode(sv.getTickerCode());
            newAction.setEzName(sv.getUserShareName());
            ezShareData.getShares().add(newAction);
            saveEZActions();
        }
    }

    public EZShare getOrCreate(Reporting reporting, String isin, EnumEZBroker broker, EzData ezData){
        if (StringUtils.isBlank(isin)) throw new RuntimeException("Le code ISIN est vide pour les data: "+ezData);
        return getFromISIN(isin)
                .orElseGet(() -> {
                    // does not yet exist, create it
                    try {
                        Optional<EZShare> ezAction = BourseDirectFinanceTools.searchAction(reporting, isin, broker, ezData);
                        if (ezAction.isPresent()){
                            YahooFinanceTools.addYahooInfoTo(reporting, ezAction.get());
                            EZShare newAction = ezAction.get();
                            newAction.setDescription(EZShare.NEW_SHARE);
                            ezShareData.getShares().add(newAction);
                            saveEZActions();
                            return newAction;
                        }
                        throw new RuntimeException("Pas d'information trouvée sur la valeur: "+isin);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    public List<EZShare> getIncompleteActionsOrNew() {
        ActionWithMsg actionWithMsg = getActionWithError();
        ezShareData.getShares().stream()
                .filter(s -> EZShare.NEW_SHARE.equals(s.getDescription()))
                .forEach(ezShare -> actionWithMsg.addMsg(ezShare, "Nouvelle action détectée"));
        return actionWithMsg.getActions();
    }

    public Optional<EZShare> getFromISIN(String isin) {
        if (StringUtils.isBlank(isin)) return Optional.empty();
        return ezShareData.getShares().stream().filter(a -> a.getIsin() != null && a.getIsin().equals(isin)).findFirst();
    }

    public Optional<EZShare> getFromGoogleTicker(String gooleTicker) {
        if (StringUtils.isBlank(gooleTicker)) return Optional.empty();
        return ezShareData.getShares().stream().filter(a -> a.getGoogleCode() != null && a.getGoogleCode().equals(gooleTicker)).findFirst();
    }

    private void loadEZActions() throws IOException {
        if (!new File(shareDataFile).exists()){
            ezShareData = new ShareDataFileContent();
        }
        else {
            try (Reader reader = new FileReader(shareDataFile, StandardCharsets.UTF_8)) {
                ezShareData = JsonUtil.createDefaultMapper().readValue(reader, ShareDataFileContent.class);
            }
        }
    }

    private void saveEZActions() throws IOException {
        JsonUtil.createDefaultWriter().writeValue(new FileWriter(shareDataFile, StandardCharsets.UTF_8), ezShareData);
    }

    public ActionWithMsg getActionWithError() {
        ActionWithMsg actionWithMsg = new ActionWithMsg();
        Map<String, EZShare> isinFound = new HashMap<>();
        Map<String, EZShare> gTickerFound = new HashMap<>();
        Map<String, EZShare> yahooFound = new HashMap<>();
        Map<String, EZShare> seekingAlphaFound = new HashMap<>();
        Map<String, EZShare> nameFound = new HashMap<>();
        ezShareData.getShares().forEach(ezAction -> {
            actionWithMsg.addMsgs(ezAction, getError(ezAction));

            if (!StringUtils.isBlank(ezAction.getIsin())) {
                EZShare old = isinFound.put(ezAction.getIsin(), ezAction);
                if (old != null){
                    actionWithMsg.addMsg(ezAction, "Le code ISIN: "+ezAction.getIsin()+" est présent sur plusieurs actions!");
                    actionWithMsg.addMsg(old, "Le code ISIN: "+ezAction.getIsin()+" est présent sur plusieurs actions!");
                }
            }

            if (!StringUtils.isBlank(ezAction.getGoogleCode())) {
                EZShare old = gTickerFound.put(ezAction.getGoogleCode(), ezAction);
                if (old != null){
                    actionWithMsg.addMsg(ezAction, "Le ticker google: "+ezAction.getGoogleCode()+" est présent sur plusieurs actions!");
                    actionWithMsg.addMsg(old, "Le ticker google: "+ezAction.getGoogleCode()+" est présent sur plusieurs actions!");
                }
            }

            if (!StringUtils.isBlank(ezAction.getYahooCode())) {
                EZShare old = yahooFound.put(ezAction.getYahooCode(), ezAction);
                if (old != null){
                    actionWithMsg.addMsg(ezAction, "Le code yahoo: "+ezAction.getYahooCode()+" est présent sur plusieurs actions!");
                    actionWithMsg.addMsg(old, "Le code yahoo: "+ezAction.getYahooCode()+" est présent sur plusieurs actions!");
                }
            }

            if (!StringUtils.isBlank(ezAction.getSeekingAlphaCode())) {
                EZShare old = seekingAlphaFound.put(ezAction.getSeekingAlphaCode(), ezAction);
                if (old != null){
                    actionWithMsg.addMsg(ezAction, "Le code seekingAlpha: "+ezAction.getSeekingAlphaCode()+" est présent sur plusieurs actions!");
                    actionWithMsg.addMsg(old, "Le code seekingAlpha: "+ezAction.getSeekingAlphaCode()+" est présent sur plusieurs actions!");
                }
            }

            if (!StringUtils.isBlank(ezAction.getEzName())) {
                EZShare old = nameFound.put(ezAction.getEzName(), ezAction);
                if (old != null){
                    actionWithMsg.addMsg(ezAction, "Le nom: "+ezAction.getEzName()+" est présent sur plusieurs actions!");
                    actionWithMsg.addMsg(old, "Le nom: "+ezAction.getEzName()+" est présent sur plusieurs actions!");
                }
            }
        });

        return actionWithMsg;
    }

    public void clearNewShareDescription() throws IOException {
        ezShareData.getShares()
                .stream()
                .filter(ezShare -> EZShare.NEW_SHARE.equals(ezShare.getDescription()))
                .forEach(ezShare -> ezShare.setDescription(""));
        saveEZActions();
    }


    public static class ActionWithMsg {

        private final Set<String> errors = new HashSet<>();
        private final List<EZShare> actions = new LinkedList<>();

        void addMsg(EZShare action, String error){
            if (StringUtils.isBlank(action.getIsin())) {
                Optional<EZShare> search = actions.stream()
                        .filter(a -> action.getGoogleCode().equals(a.getGoogleCode()))
                        .findFirst();
                if (search.isEmpty()){
                    actions.add(action);
                }
            }
            else{
                Optional<EZShare> search = actions.stream()
                        .filter(a -> action.getIsin().equals(a.getIsin()))
                        .findFirst();
                if (search.isEmpty()){
                    actions.add(action);
                }
            }
            errors.add(error);
        }

        void addMsgs(EZShare action, List<String> errors){
            actions.add(action);
            errors.addAll(errors);
        }

        public Set<String> getErrors() {
            return errors;
        }

        public List<EZShare> getActions() {
            return actions;
        }
    }

    private List<String> getError(EZShare ezShare) {
        List<String> errors = new LinkedList<>();
        if (StringUtils.isBlank(ezShare.getIsin())){
            errors.add("Le code ISIN de l'action "+toString(ezShare)+" est vide");
        }
        if (StringUtils.isBlank(ezShare.getEzName())){
            errors.add("Le node de l'action "+toString(ezShare)+" est vide");
        }
        if (StringUtils.isBlank(ezShare.getGoogleCode())){
            errors.add("Le ticker Google de l'action "+toString(ezShare)+" est vide");
        }
        if (StringUtils.isBlank(ezShare.getCountryCode())){
            errors.add("Le code pays de l'action "+toString(ezShare)+" est vide");
        }
        return errors;
    }

    private String toString(EZShare action){
        return action.getEzName() + " ISIN: "+action.getIsin()+" GoogleTicker: "+action.getGoogleCode();
    }


    public void update(EZShare shareValue) throws IOException {
        if (StringUtils.isBlank(shareValue.getIsin()) && StringUtils.isBlank(shareValue.getGoogleCode()))
            throw new IllegalStateException("Il n'y a pas de ISIN ni de ticker google pour cette action. "+shareValue);

        Optional<EZShare> oldOpt = getFromISIN(shareValue.getIsin());
        if (oldOpt.isEmpty()){
            // le code ISIN a ete edité?
            oldOpt = getFromGoogleTicker(shareValue.getGoogleCode());
        }
        if (oldOpt.isEmpty()){
            throw new IllegalStateException("Vous ne devez pas changer dans la meme opération les valeurs ISIN et le google ticker ");
        }

        EZShare old = oldOpt.get();
        old.setSeekingAlphaCode(shareValue.getSeekingAlphaCode());
        old.setCountryCode(shareValue.getCountryCode());
        old.setType(shareValue.getType());
        old.setIndustry(shareValue.getIndustry());
        old.setSector(shareValue.getSector());
        old.setGoogleCode(shareValue.getGoogleCode());
        old.setIsin(shareValue.getIsin());
        old.setYahooCode(shareValue.getYahooCode());
        old.setEzName(shareValue.getEzName());
        old.setDescription(EZShare.NEW_SHARE.equals(shareValue.getDescription()) ? "" : shareValue.getDescription()); // it is no more a NEW SHARE as the user update it and saw it
        saveEZActions();
    }


    public static class ShareDataFileContent {
        private List<EZShare> ezShares = new LinkedList<>();

        public List<EZShare> getShares() {
            return ezShares;
        }

        public void setShares(List<EZShare> ezShares) {
            this.ezShares = ezShares;
        }
    }
}
