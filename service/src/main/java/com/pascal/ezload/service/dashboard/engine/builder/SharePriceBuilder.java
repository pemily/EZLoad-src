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
        private final Map<EZShareEQ, Prices> share2annualDividendYieldsTargetPrices = new HashMap<>();
        private final Map<String, Float> shareYear2annualDividend = new HashMap<>();

        Result(List<EZDate> dates) {
            this.dates = dates;
        }

        public EZShareEQ getShareFromName(String name) {
            return actionManager.getAllEZShares().stream()
                    .filter(s -> s.getEzName().equalsIgnoreCase(name)) // TODO utiliser levenshtein ou Jaro Winkler?
                    .findFirst()
                    .map(EZShareEQ::new)
                    .orElseThrow(() -> new IllegalStateException("L'action " + name + " n'a pas été trouvé dans la liste d'actions. Vous devez la rajouter"));
        }


        public Prices getTargetPrices(Reporting reporting, EZShareEQ share) {
            return shares2TargetPrices.computeIfAbsent(share, ezShare -> {
                try {
                    Prices prices = actionManager.getPrices(reporting, ezShare, dates);
                    if (prices != null)
                        return currencies.convertPricesToTargetDevise(reporting, prices, true);
                    reporting.error("Les cours de l'action " + share.getEzName() + " n'ont pas été trouvé");
                    throw new RuntimeException("Les cours de l'action " + share.getEzName() + " " + share.getGoogleCode() + " n'ont pas été trouvé");
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
        }


        public Prices getDividends(Reporting reporting, EZShareEQ share) {
            return share2DividendsTargetPrices.computeIfAbsent(share, ezShare -> {
                try {
                    List<Dividend> dividends = actionManager.searchDividends(reporting, ezShare, dates.get(0), dates.get(dates.size() - 1));
                    Prices prices = new Prices();
                    prices.setLabel("Dividendes of " + ezShare.getEzName());
                    if (dividends != null) {
                        Optional<EZDevise> devise = dividends.stream().map(Dividend::getDevise).findFirst();
                        if (devise.isPresent()) {
                            prices.setDevise(devise.get());
                            EZDate previousDate = dates.get(0);
                            for (EZDate currentDate : dates) {
                                PriceAtDate dividendFound = extractDividendForCurrentDate(dividends, previousDate, currentDate);

                                EZDate date = dividendFound != null ? dividendFound.getDate() : currentDate;

                                float value = 0f;
                                if (dividendFound != null && dividendFound.getPrice() > 0) {
                                    value = dividendFound.getPrice();
                                }
                                prices.addPrice(currentDate, new PriceAtDate(date, value, dividendFound != null && dividendFound.isEstimated()));
                                previousDate = currentDate;
                            }
                            return currencies.convertPricesToTargetDevise(reporting, prices, true);
                        }
                    }
                    // Pas de dividendes
                    prices.setDevise(DeviseUtil.EUR);
                    for (EZDate currentDate : dates) {
                        prices.addPrice(currentDate, new PriceAtDate(currentDate, false));
                    }
                    return prices;
                } catch (Exception e) {
                    reporting.error("Erreur lors de la récupération du dividende de l'action: " + ezShare.getEzName(), e);
                    throw new RuntimeException("Erreur lors de la récupération du dividende de l'action: " + ezShare.getEzName(), e);
                }
            });
        }

        private PriceAtDate extractDividendForCurrentDate(List<Dividend> dividends, EZDate previousDate, EZDate currentDate) {
            float totalDividendInOriginalDevise = 0f;
            PriceAtDate dividendFound = null;
            boolean estimated = false;
            for (Dividend div : dividends){
                estimated |= div.isEstimated();
                if (currentDate.isPeriod()) {
                    if (currentDate.contains(div.getDetachmentDate())){
                        totalDividendInOriginalDevise += div.getAmount();
                        dividendFound = new PriceAtDate(div.getDetachmentDate(), totalDividendInOriginalDevise, estimated);
                        estimated = false;
                    }
                }
                else if(div.getDetachmentDate().isAfter(previousDate)
                            && div.getDetachmentDate().isBeforeOrEquals(currentDate)){
                    totalDividendInOriginalDevise += div.getAmount();
                    dividendFound = new PriceAtDate(div.getDetachmentDate(), totalDividendInOriginalDevise, estimated);
                    estimated = false;
                }
            }
            return dividendFound;
        }


        public Prices getAnnualDividendYield(Reporting reporting, EZShareEQ share) {
            // Le rendement du dividend
            return share2annualDividendYieldsTargetPrices.computeIfAbsent(share, ezShare -> {
                Prices dividends = getDividends(reporting, ezShare);
                if (dividends != null) {
                    Prices annualDividendYields = new Prices();
                    annualDividendYields.setLabel(dividends.getLabel());
                    annualDividendYields.setDevise(DeviseUtil.SPECIAL_PERCENT);
                    for (EZDate currentDate : dates) {
                        int year = currentDate.getYear();
                        boolean isEstimation = false;
                        Float annualDividend = computeAnnualDividend(reporting, share, year);
                        if (year == EZDate.today().getYear()) {
                            Float previousAnnualDiv = computeAnnualDividend(reporting, share, year-1);
                            if (previousAnnualDiv != null && annualDividend < previousAnnualDiv) {
                                annualDividend = previousAnnualDiv;
                                isEstimation = true;
                            }

                        }

                        float rendement = 0f;
                        if (annualDividend != null && annualDividend > 0) {
                            Prices p = getTargetPrices(reporting, ezShare);
                            if (p != null) {
                                float price = p.getPriceAt(currentDate).getPrice();
                                rendement = (annualDividend * 100f) / price;

                            }
                        }
                        annualDividendYields.addPrice(currentDate, new PriceAtDate(currentDate, rendement, isEstimation));
                    }
                    return annualDividendYields;
                }
                return null;
            });
        }

        private Float computeAnnualDividend(Reporting reporting, EZShareEQ ezShare, int year) {
            return shareYear2annualDividend.computeIfAbsent(ezShare.getEzName()+"_"+year, ezShareEQ -> {
                Prices dividends = getDividends(reporting, ezShare);
                float annualDividend = 0;
                if (dividends == null) return null;

                for (PriceAtDate p : dividends.getPrices()) {
                    if (p.getDate().getYear() == year) {
                        annualDividend += p.getPrice() == null ? 0 : p.getPrice();
                    }
                    if (p.getDate().getYear() > year)
                        break;
                }

                return annualDividend;
            });
        }
    }

}
