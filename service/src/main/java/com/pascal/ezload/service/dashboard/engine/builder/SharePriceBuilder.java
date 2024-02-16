package com.pascal.ezload.service.dashboard.engine.builder;

import com.pascal.ezload.service.financial.EZActionManager;
import com.pascal.ezload.service.model.*;
import com.pascal.ezload.service.sources.Reporting;
import com.pascal.ezload.service.util.DeviseUtil;
import com.pascal.ezload.service.util.finance.Dividend;

import java.util.*;

public class SharePriceBuilder {


    private final EZActionManager actionManager;
    private final CurrenciesIndexBuilder currencies;
    private final List<EZDate> dates;

    public SharePriceBuilder(EZActionManager actionManager, CurrenciesIndexBuilder currencies, List<EZDate> dates){
        this.actionManager = actionManager;
        this.currencies = currencies;
        this.dates = dates;
    }

    private final Map<EZShareEQ, Prices> shares2TargetPrices = new HashMap<>();
    private final Map<EZShareEQ, Prices> share2RealDividendsTargetPrices = new HashMap<>(); // les dividendes reellement recu a la date de detachement
    private final Map<EZShareEQ, Prices> share2DividendsWithEstimatesTargetPrices = new HashMap<>(); // avec l'estimation sur l'année en cours
    private final Map<EZShareEQ, Prices> share2annualDividendYieldsWithEstimatesTargetPrices = new HashMap<>();  // avec l'estimation sur l'année en cours
    private final Map<String, Price> shareYear2annualDividend = new HashMap<>();
    private final Map<String, Price> shareYear2annualDividendWithEstimates = new HashMap<>();


    public EZShareEQ getShareFromName(String name) {
        return actionManager.getAllEZShares().stream()
                .filter(s -> s.getEzName().equalsIgnoreCase(name)
                        || (s.getAlternativeName() != null && s.getAlternativeName().equalsIgnoreCase(name))) // TODO utiliser levenshtein ou Jaro Winkler?
                .findFirst()
                .map(EZShareEQ::new)
                .orElseThrow(() -> new IllegalStateException("L'action " + name + " n'a pas été trouvé dans la liste d'actions. Vous devez la rajouter"));
    }


    public Prices getPricesToTargetDevise(Reporting reporting, EZShareEQ share) {
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

    public Prices getDividendsWithCurrentYearEstimates(Reporting reporting, EZShareEQ share){
        return share2DividendsWithEstimatesTargetPrices.computeIfAbsent(share, ezShare -> {
            Prices prices = getDividends(reporting, share);
            Prices pricesWithEstimates = new Prices(prices);
            int currentYear = EZDate.today().getYear();
            Price previousAnnualDiv = computeAnnualDividend(reporting, share, currentYear - 1, false);
            Price currentAnnualDiv = computeAnnualDividend(reporting, share, currentYear, false);
            if (previousAnnualDiv.getValue() != null && (currentAnnualDiv.getValue() == null || currentAnnualDiv.getValue() < previousAnnualDiv.getValue())) {
                EZDate today = EZDate.today();

                PriceAtDate lastYearDividend = pricesWithEstimates.getPrices().get(pricesWithEstimates.getPrices().size()-1);
                if (lastYearDividend == null || lastYearDividend.getDate().getYear() != today.getYear()) throw new RuntimeException("Il manque le dividende de cette année"+pricesWithEstimates.getPrices());
                // le dividend qui manque pour atteindre le niveau de l'année dernière (+ le dividend du jour car je supprime cette donnée de la liste)
                pricesWithEstimates.replacePriceAt(prices.getPrices().size()-1, new PriceAtDate(today,
                        previousAnnualDiv.getValue() - currentAnnualDiv.getValue() + lastYearDividend.getValue(), true));
            }
            return pricesWithEstimates;
        });
    }

    public Prices getDividends(Reporting reporting, EZShareEQ share) {
        return share2RealDividendsTargetPrices.computeIfAbsent(share, ezShare -> {
            try {
                List<Dividend> dividends = actionManager.searchDividends(reporting, ezShare, dates.get(0));
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
                            if (dividendFound != null && dividendFound.getValue() > 0) {
                                value = dividendFound.getValue();
                            }
                            prices.addPrice(new PriceAtDate(date, value, dividendFound != null && dividendFound.isEstimated()));
                            previousDate = currentDate;
                        }
                        return currencies.convertPricesToTargetDevise(reporting, prices, true);
                    }
                }
                // Pas de dividendes ou pas encore recu
                prices.setDevise(DeviseUtil.EUR);
                for (EZDate currentDate : dates) {
                    prices.addPrice(new PriceAtDate(currentDate));
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
                    estimated = currentDate.endPeriodDate().isAfter(EZDate.today()); // si je suis dans la periode actuelle, je n'ai peut etre pas encore toutes les données, donc c'est estimée
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


    public Prices getAnnualDividendYieldWithEstimates(Reporting reporting, EZShareEQ share) {
        // Le rendement du dividend
        return share2annualDividendYieldsWithEstimatesTargetPrices.computeIfAbsent(share, ezShare -> {
            Prices dividends = getDividends(reporting, ezShare);
            if (dividends != null) {
                Prices annualDividendYields = new Prices();
                annualDividendYields.setLabel(dividends.getLabel());
                annualDividendYields.setDevise(DeviseUtil.SPECIAL_PERCENT);
                for (EZDate currentDate : dates) {
                    int year = currentDate.getYear();
                    Price annualDividend = computeAnnualDividend(reporting, share, year, true);

                    Price rendement = new Price(0);
                    if (annualDividend.getValue() != null && annualDividend.getValue() > 0) {
                        Prices p = getPricesToTargetDevise(reporting, ezShare);
                        if (p != null) {
                            Price price = p.getPriceAt(currentDate);
                            rendement = price.getValue()  == null || price.getValue() == 0 ? new Price() : annualDividend.multiply(new Price(100)).divide(price);

                        }
                    }
                    annualDividendYields.addPrice(new PriceAtDate(currentDate, rendement));
                }
                return annualDividendYields;
            }
            return null;
        });
    }

    private Price computeAnnualDividend(Reporting reporting, EZShareEQ ezShare, int year, boolean withEstimatesForCurrentYear) {
        Map<String, Price> selectedMap = withEstimatesForCurrentYear ? shareYear2annualDividendWithEstimates : shareYear2annualDividend;
        return selectedMap.computeIfAbsent(ezShare.getEzName()+"_"+year, ezShareEQ -> {
            Prices dividends = withEstimatesForCurrentYear ? getDividendsWithCurrentYearEstimates(reporting, ezShare) : getDividends(reporting, ezShare);
            EZDate today = EZDate.today();

            Price annualDividend = new Price(0, year == today.getYear());
            if (dividends == null) return annualDividend;

            for (PriceAtDate p : dividends.getPrices()) {
                if (p.getDate().getYear() == year) {
                    annualDividend = annualDividend.plus(p);
                }
                if (p.getDate().getYear() > year)
                    break;
            }

            return annualDividend;
        });
    }

}
