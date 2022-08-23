package com.pascal.ezload.service.financial;

import com.pascal.ezload.service.exporter.ezEdition.EzData;
import com.pascal.ezload.service.exporter.ezEdition.ShareValue;
import com.pascal.ezload.service.model.*;
import com.pascal.ezload.service.sources.Reporting;
import com.pascal.ezload.service.util.*;
import com.pascal.ezload.service.util.finance.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class EZActionManager {
    private static final Logger logger = Logger.getLogger("EZActionManager");

    private final HttpUtilCached cache;
    private final String shareDataFile;
    private ShareDataFileContent ezShareData;

    public EZActionManager(String cacheDir, String shareDataFile) throws IOException {
        this.cache = new HttpUtilCached(cacheDir);
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
        if (action.isEmpty()){
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
                        Optional<EZShare> ezAction = BourseDirectTools.searchAction(cache, reporting, isin, broker, ezData);
                        if (ezAction.isPresent()){
                            YahooTools.addYahooInfoTo(cache, reporting, ezAction.get());
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

    public ActionWithMsg getIncompleteActionsOrNew() {
        ActionWithMsg actionWithMsg = getActionWithError();
        ezShareData.getShares().stream()
                .filter(s -> EZShare.NEW_SHARE.equals(s.getDescription()))
                .forEach(ezShare -> actionWithMsg.addMsg(ezShare, null)); // ce n'est pas une erreur, c'est juste pour qu'elle s'affiche dans la page, pour qu'on puisse la voir, et l'editer
        return actionWithMsg;
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

    public List<Dividend> searchDividends(EZShare ezShare) {
        return SeekingAlphaTools.searchDividends(cache, ezShare);
    }


    private List<String> getError(EZShare ezShare) {
        List<String> errors = new LinkedList<>();
        if (StringUtils.isBlank(ezShare.getIsin())){
            errors.add(toString(ezShare)+": Le code ISIN est vide");
        }
        if (StringUtils.isBlank(ezShare.getEzName())){
            errors.add(toString(ezShare)+": Le nom de l'action est vide");
        }
        if (StringUtils.isBlank(ezShare.getGoogleCode())){
            errors.add(toString(ezShare)+": Le ticker Google est vide");
        }
        if (StringUtils.isBlank(ezShare.getCountryCode())){
            errors.add(toString(ezShare)+"Le code pays de l'action est vide");
        }

        if (!StringUtils.isBlank(ezShare.getYahooCode()) && !StringUtils.isBlank(ezShare.getSeekingAlphaCode())) {
            // Validate that seeking alpha & yahoo code are aligned
            EZSharePrices yahooPrices = null;
            try {
                yahooPrices = YahooTools.getPrices(cache, ezShare);
            }
            catch (Exception e){
                errors.add(toString(ezShare)+": Le code Yahoo ne fonctionne pas");
            }
            EZSharePrices seekingPrices = null;
            try {
                seekingPrices = SeekingAlphaTools.getPrices(cache, ezShare);
            }
            catch (Exception e){
                errors.add(toString(ezShare)+": Le code SeekingAlpha ne fonctionne pas");
            }

            if (yahooPrices != null && seekingPrices != null) {
                EZDate date = findCommonDate(yahooPrices, seekingPrices);
                if (date != null) {
                    try {
                        EZSharePrice yahooPriceInEuro = getPrice(date, yahooPrices, DeviseUtil.EUR);
                        EZSharePrice seekingPriceInEuro = getPrice(date, seekingPrices, DeviseUtil.EUR);

                        float diff = Math.abs(yahooPriceInEuro.getPrice() - seekingPriceInEuro.getPrice());
                        float percentOfDiff = diff * 100.f / yahooPriceInEuro.getPrice();
                        if (percentOfDiff > 3) { // si la difference est plus grande que 3% c'est surement pas la meme action
                            errors.add(toString(ezShare)+": Les codes Yahoo & SeekingAlpha ne semble pas être pas la meme action");
                        }
                    }
                    catch (Exception e){
                        logger.log(Level.SEVERE, "Erreur lors du chargement des prix de l'action "+toString(ezShare), e);
                    }
                }
            }
       }

        return errors;
    }

    private EZDate findCommonDate(EZSharePrices l1, EZSharePrices l2) {
        int indexList1 = l1.getPrices().size() - 1;
        int indexList2 = l2.getPrices().size() - 1;

        while (indexList1 != 0 && indexList2 != 0) {
            EZSharePrice l1Price = l1.getPrices().get(indexList1);
            EZSharePrice l2Price = l2.getPrices().get(indexList2);

            if (l1Price.getDate().equals(l2Price.getDate()))
                return l1Price.getDate();

            if (l1Price.getDate().isAfter(l2Price.getDate())) indexList1--;
            else indexList2--;
        }

        return null;
    }

    // si la date n'est pas presente dans prices.getPrices, il prendra la date precedente
    public EZSharePrice getPrice(EZDate date, EZSharePrices prices, EZDevise finalDevise) throws Exception {
        CurrencyMap currencyMap = YahooTools.getCurrencyMap(cache, prices.getDevise(), finalDevise);
        return currencyMap.getPrice(prices.getPriceAt(date));
    }

    private String toString(EZShare action){
        if (StringUtils.isBlank(action.getEzName())){
            if (StringUtils.isBlank(action.getIsin())) return action.getGoogleCode();
            return action.getIsin();
        }
        return action.getEzName();
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

        if (!old.getIsin().equals(shareValue.getIsin()) && !StringUtils.isBlank(old.getIsin())) {
            throw new IllegalStateException("Nous ne devez pas changer le code ISIN, car celui ci est celui fournit par BourseDirect dans vos relevé d'opérations");
        }

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


}
