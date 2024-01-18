package com.pascal.ezload.service.dashboard.engine.builder;

import com.pascal.ezload.service.financial.EZActionManager;
import com.pascal.ezload.service.model.*;
import com.pascal.ezload.service.sources.Reporting;
import com.pascal.ezload.service.util.DeviseUtil;
import com.pascal.ezload.service.util.finance.Dividend;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class SharePriceBuilder {


    private final EZActionManager actionManager;
    private final CurrenciesIndexBuilder.Result currencies;

    public SharePriceBuilder(EZActionManager actionManager, CurrenciesIndexBuilder.Result currencies){
        this.actionManager = actionManager;
        this.currencies = currencies;
    }

    public Result build(Reporting reporting, List<EZDate> dates){
        return new Result(dates);
    }


    public class Result {
        private final List<EZDate> dates;
        private final Map<EZShareEQ, Prices> shares2TargetPrices = new HashMap<>();
        private final Map<EZShareEQ, Prices> share2DividendsTargetPrices = new HashMap<>();
        private final Map<EZShareEQ, Prices> share2dividendYieldsTargetPrices = new HashMap<>();

        Result(List<EZDate> dates){
            this.dates = dates;
        }

        public EZShareEQ getShareFromName(String name){
            return actionManager.getAllEZShares().stream()
                    .filter(s -> s.getEzName().equalsIgnoreCase(name)) // TODO utiliser levenshtein ou Jaro Winkler?
                    .findFirst()
                    .map(EZShareEQ::new)
                    .orElseThrow(() -> new IllegalStateException("L'action "+name+" n'a pas été trouvé dans la liste d'actions. Vous devez la rajouter"));
        }


        public Prices getTargetPrices(Reporting reporting, EZShareEQ share) {
            return shares2TargetPrices.computeIfAbsent(share, ezShare -> {
                try {
                    Prices prices = actionManager.getPrices(reporting, ezShare, dates);
                    if (prices != null)
                        return currencies.convertPricesToTargetDevise(reporting, prices, true);
                    reporting.error("Les cours de l'action "+share.getEzName()+" n'ont pas été trouvé");
                    throw new RuntimeException("Les cours de l'action "+share.getEzName()+" "+share.getGoogleCode()+" n'ont pas été trouvé");
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
        }


        public Prices getDividends(Reporting reporting, EZShareEQ share){
            return share2DividendsTargetPrices.computeIfAbsent(share, ezShare -> {
                try {
                    List<Dividend> dividends = actionManager.searchDividends(reporting, ezShare, dates.get(0), dates.get(dates.size() - 1));
                    Prices prices = new Prices();
                    prices.setLabel("Dividendes of "+ezShare.getEzName());
                    if (dividends != null) {
                        Optional<EZDevise> devise = dividends.stream().map(Dividend::getDevise).findFirst();
                        if (devise.isPresent()) {
                            prices.setDevise(devise.get());
                            EZDate previousDate = dates.get(0);
                            for (EZDate currentDate : dates) {
                                EZDate finalPreviousDate = previousDate;
                                float value = 0f;
                                float totalDividendInOriginalDevise = (float) dividends.stream()
                                        .filter(div -> currentDate.isPeriod() ? currentDate.contains(div.getDetachementDate()) : div.getDetachementDate().isAfter(finalPreviousDate) && div.getDetachementDate().isBeforeOrEquals(currentDate))
                                        .mapToDouble(Dividend::getAmount)
                                        .sum();

                                if (totalDividendInOriginalDevise > 0) {
                                   value = totalDividendInOriginalDevise;
                                }
                                prices.addPrice(currentDate, new PriceAtDate(currentDate, value));
                                previousDate = currentDate;
                            }
                            return currencies.convertPricesToTargetDevise(reporting, prices, true);
                        }
                    }
                    // Pas de dividendes
                    prices.setDevise(DeviseUtil.EUR);
                    for (EZDate currentDate : dates) {
                        prices.addPrice(currentDate, new PriceAtDate(currentDate));
                    }
                    return prices;
                } catch (Exception e) {
                    reporting.error("Erreur lors de la récupération du dividende de l'action: " + ezShare.getEzName(), e);
                    throw new RuntimeException("Erreur lors de la récupération du dividende de l'action: " + ezShare.getEzName(), e);
                }
            });
        }



        public Prices getDividendYield(Reporting reporting, EZShareEQ share){
            // Le rendement du dividend
            return share2dividendYieldsTargetPrices.computeIfAbsent(share, ezShare -> {
                Prices dividends = getDividends(reporting, ezShare);
                if (dividends != null) {
                    Prices dividendYields = new Prices();
                    dividendYields.setLabel(dividends.getLabel());
                    dividendYields.setDevise(dividends.getDevise());
                    for (EZDate currentDate : dates) {
                        Float dividend = dividends.getPriceAt(currentDate).getPrice();

                        float rendement = 0f;
                        if (dividend != null && dividend > 0) {
                            Prices p = getTargetPrices(reporting, ezShare);
                            if (p != null) {
                                float price = p.getPriceAt(currentDate).getPrice();
                                rendement = (dividend * 100f) / price;

                            }
                        }
                        dividendYields.addPrice(currentDate, new PriceAtDate(currentDate, rendement));
                    }
                    return dividendYields;
                }
                return null;
            });
        }

    }
}
