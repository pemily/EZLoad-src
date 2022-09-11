/**
 * ezService - EZLoad an automatic loader for EZPortfolio
 * Copyright © 2021 EMILY Pascal (pascal.emily@gmail.com)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.pascal.ezload.service.financial;

import com.pascal.ezload.service.exporter.EZPortfolioProxy;
import com.pascal.ezload.service.exporter.ezEdition.EzData;
import com.pascal.ezload.service.exporter.ezEdition.ShareValue;
import com.pascal.ezload.service.exporter.ezPortfolio.v5.MesOperations;
import com.pascal.ezload.service.model.*;
import com.pascal.ezload.service.sources.Reporting;
import com.pascal.ezload.service.util.*;
import com.pascal.ezload.service.util.finance.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

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

        Optional<EZShare> action = getFromGoogleTicker(sv.getTickerCode());
        if (action.isEmpty()) {
            action = getFromName(sv.getUserShareName());
        }

        if (action.isEmpty()){
            EZShare newAction = new EZShare();
            newAction.setIsin(null);
            newAction.setDescription(EZShare.NEW_SHARE);
            try {
                newAction.setCountryCode(CountryUtil.foundByName(sv.getCountryName()).getCode());
            }
            catch(Exception ignore){
                // the countryCode will be null
            }
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
                        Optional<EZShare> ezAction = BourseDirectTools.searchAction(reporting, cache, isin, broker, ezData);
                        if (ezAction.isPresent()){
                            YahooTools.addYahooInfoTo(reporting, cache, ezAction.get());
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

    public ActionWithMsg getIncompleteSharesOrNew(Reporting reporting) throws IOException {
        ActionWithMsg actionWithMsg = getActionWithError(reporting);
        ActionWithMsg onlyNewAndErrorMsg = new ActionWithMsg();
        onlyNewAndErrorMsg.getErrors().addAll(actionWithMsg.getErrors());
        ezShareData.getShares().stream()
                .filter(s -> EZShare.NEW_SHARE.equals(s.getDescription()))
                .forEach(ezShare -> onlyNewAndErrorMsg.addMsg(ezShare, null)); // ce n'est pas une erreur, c'est juste pour qu'elle s'affiche dans la page, pour qu'on puisse la voir, et l'editer
        return onlyNewAndErrorMsg;
    }

    public List<EZShare> getAllEZShares(){
        return ezShareData.getShares();
    }

    public ActionWithMsg getAllEZSharesWithMessages(Reporting reporting) throws IOException {
        ActionWithMsg actionWithMsg = getActionWithError(reporting);
        actionWithMsg.setShares(ezShareData.getShares());
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

    public Optional<EZShare> getFromName(String shareName) {
        if (StringUtils.isBlank(shareName)) return Optional.empty();
        return ezShareData.getShares().stream().filter(a -> a.getEzName() != null && a.getEzName().equals(shareName)).findFirst();
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
        ezShareData.getShares()
                .sort(Comparator.comparing(EZShare::getEzName, Comparator.nullsLast(Comparator.naturalOrder())));
        JsonUtil.createDefaultWriter().writeValue(new FileWriter(shareDataFile, StandardCharsets.UTF_8), ezShareData);
    }

    public ActionWithMsg getActionWithError(Reporting reporting) throws IOException {
        ActionWithMsg actionWithMsg = new ActionWithMsg();
        Map<String, EZShare> isinFound = new HashMap<>();
        Map<String, EZShare> gTickerFound = new HashMap<>();
        Map<String, EZShare> yahooFound = new HashMap<>();
        Map<String, EZShare> seekingAlphaFound = new HashMap<>();
        Map<String, EZShare> nameFound = new HashMap<>();

        for (EZShare ezAction : ezShareData.getShares()) {
            actionWithMsg.addMsgs(ezAction, getError(reporting, ezAction));

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
        }

        return actionWithMsg;
    }

    public void clearNewShareDescription() throws IOException {
        ezShareData.getShares()
                .stream()
                .filter(ezShare -> EZShare.NEW_SHARE.equals(ezShare.getDescription()))
                .forEach(ezShare -> ezShare.setDescription(""));
        saveEZActions();
    }

    public List<Dividend> searchDividends(Reporting rep, EZShare ezShare) throws IOException {
        try(Reporting reporting = rep.pushSection("Extraction des dividendes pour "+ezShare.getEzName())) {
            return SeekingAlphaTools.searchDividends(reporting, cache, ezShare);
        }
    }


    private List<String> getError(Reporting rep, EZShare ezShare) throws IOException {
        try(Reporting reporting = rep.pushSection("Verification de l'action: "+ezShare.getEzName())){
            List<String> errors = new LinkedList<>();
            if (StringUtils.isBlank(ezShare.getIsin())) {
                errors.add(toString(ezShare) + ": Le code ISIN est vide");
            }
            if (StringUtils.isBlank(ezShare.getEzName())) {
                errors.add(toString(ezShare) + ": Le nom de l'action est vide");
            }
            if (StringUtils.isBlank(ezShare.getGoogleCode())) {
                errors.add(toString(ezShare) + ": Le ticker Google est vide");
            }
            if (StringUtils.isBlank(ezShare.getCountryCode())) {
                errors.add(toString(ezShare) + ": Le code pays de l'action est vide");
            }

            // Validate that google code & seeking alpha & yahoo code are aligned
            Prices yahooPrices = null;
            EZDate to = EZDate.today();
            EZDate from = to.minusDays(10);
            try {
                yahooPrices = StringUtils.isBlank(ezShare.getYahooCode()) ? null : YahooTools.getPrices(reporting, cache, ezShare, from, to);
            } catch (Exception e) {
            }
            Prices seekingPrices = null;
            try {
                seekingPrices = StringUtils.isBlank(ezShare.getSeekingAlphaCode()) ? null : SeekingAlphaTools.getPrices(reporting, cache, ezShare, from, to);
            } catch (Exception e) {
            }
            Prices googlePrices = null;
            try {
                googlePrices = StringUtils.isBlank(ezShare.getGoogleCode()) ? null : GoogleTools.getCurrentPrice(reporting, cache, ezShare);
            }
            catch (Exception e){
            }

            if (yahooPrices == null && !StringUtils.isBlank(ezShare.getYahooCode())) {
                errors.add(toString(ezShare) + ": Le code Yahoo ne fonctionne pas");
            }
            if (seekingPrices == null && !StringUtils.isBlank(ezShare.getSeekingAlphaCode())) {
                errors.add(toString(ezShare) + ": Le code SeekingAlpha ne fonctionne pas");
            }
            if (googlePrices == null && !StringUtils.isBlank(ezShare.getGoogleCode())) {
                errors.add(toString(ezShare) + ": Le code Google ne fonctionne pas");
            }

            try {
                checkPrices(ezShare, reporting, errors, yahooPrices, seekingPrices, "Le code Yahoo & SeekingAlpha ne semblent pas être pas la meme action");
                checkPrices(ezShare, reporting, errors, yahooPrices, googlePrices, "Le code Yahoo & Google ne semblent pas être pas la meme action");
                checkPrices(ezShare, reporting, errors, googlePrices, seekingPrices, "Le code Google & SeekingAlpha ne semblent pas être pas la meme action");
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Erreur lors du chargement des prix de l'action " + toString(ezShare), e);
            }

            return errors;
        }
    }

    private void checkPrices(EZShare ezShare, Reporting reporting, List<String> errors, Prices prices1, Prices prices2, String errorMessage) throws Exception {
        if (prices1 == null || prices2 == null) return;
        EZDate today = EZDate.today();
        PriceAtDate todayPrice1 = prices1.getPriceAt(today);
        PriceAtDate todayPrice2 = prices2.getPriceAt(today);
        if (todayPrice1.getPrice() != 0 && todayPrice2.getPrice() != 0) {
            List<EZDate> lastWeek = Arrays.asList(EZDate.today().minusDays(7), EZDate.today());
            CurrencyMap local2Euro1 = getCurrencyMap(reporting, prices1.getDevise(), DeviseUtil.EUR, lastWeek);
            CurrencyMap local2Euro2 = getCurrencyMap(reporting, prices2.getDevise(), DeviseUtil.EUR, lastWeek);
            float price1InEuro = local2Euro1.getTargetPrice(todayPrice1);
            float price2InEuro = local2Euro2.getTargetPrice(todayPrice2);

            float diff = Math.abs(price1InEuro - price2InEuro);
            float percentOfDiff = diff * 100.f / price1InEuro;
            if (percentOfDiff > 6) { // si la difference est plus grande que 6% c'est surement pas la meme action (attention il y a des differences a l'ouverture des marché, des sites ne sont pas a jour en meme temps)
                errors.add(toString(ezShare) + ": "+errorMessage);
            }
        }
    }

    public CurrencyMap getCurrencyMap(Reporting rep, EZDevise fromDevise, EZDevise toDevise, List<EZDate> listOfDates) throws Exception {
        try(Reporting reporting = rep.pushSection("Extraction de la conversion "+fromDevise+" vers "+toDevise)) {
            return YahooTools.getCurrencyMap(reporting, cache, fromDevise, toDevise, listOfDates);
        }
    }


    private String toString(EZShare action){
        if (StringUtils.isBlank(action.getEzName())){
            if (StringUtils.isBlank(action.getIsin())) return action.getGoogleCode() == null ? "" : action.getGoogleCode();
            return action.getIsin() == null ? "" : action.getIsin();
        }
        return action.getEzName() == null ? "" : action.getEzName();
    }


    public void deleteShare(int index) throws IOException {
        ezShareData.getShares().remove(index);
        saveEZActions();
    }

    public void newShare() throws IOException {
        boolean emptyRow = ezShareData.getShares().stream().anyMatch(sh ->
            StringUtils.isBlank(sh.getEzName())
                    && StringUtils.isBlank(sh.getIsin())
                    && StringUtils.isBlank(sh.getGoogleCode())
                    && StringUtils.isBlank(sh.getYahooCode())
                    && StringUtils.isBlank(sh.getSeekingAlphaCode())
                    && StringUtils.isBlank(sh.getCountryCode())
                    && StringUtils.isBlank(sh.getDescription())
        );
        if (!emptyRow) {
            ezShareData.getShares().add(new EZShare());
            saveEZActions();
        }
    }

    public void update(int index, EZShare shareValue) throws IOException {
        if (index >= 0 && index < ezShareData.getShares().size()) {
            EZShare old = ezShareData.getShares().get(index);

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


    public List<EZShare> listAllShares() {
        return ezShareData.getShares();
    }

    public Prices getPrices(Reporting rep, EZShare ez, EZDate from, EZDate to) throws IOException {
        try(Reporting reporting = rep.pushSection("Extraction du prix pour "+ez.getEzName())) {
            Prices prices = YahooTools.getPrices(reporting, cache, ez, from, to);
            if (prices == null) {
                prices = SeekingAlphaTools.getPrices(reporting, cache, ez, from, to);
            }
            if (prices == null) {
                reporting.error("Pas de prix trouvé pour l'action " + ez.getEzName());
            }
            return prices;
        }
    }

    public Prices getPrices(Reporting rep, EZShare ez, List<EZDate> listOfDates) throws IOException {
        try(Reporting reporting = rep.pushSection("Extraction du prix pour "+ez.getEzName())) {
            Prices prices = YahooTools.getPrices(reporting, cache, ez, listOfDates);
            if (prices == null) {
                prices = SeekingAlphaTools.getPrices(reporting, cache, ez, listOfDates);
            }
            if (prices == null) {
                reporting.error("Pas de prix trouvé pour l'action " + ez.getEzName());
            }
            return prices;
        }
    }
}
