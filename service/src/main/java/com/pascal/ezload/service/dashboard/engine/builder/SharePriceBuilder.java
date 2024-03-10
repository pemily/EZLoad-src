package com.pascal.ezload.service.dashboard.engine.builder;

import com.pascal.ezload.service.dashboard.config.ChartGroupedBy;
import com.pascal.ezload.service.dashboard.config.ChartPerfFilter;
import com.pascal.ezload.service.dashboard.engine.tag.DividendInfo;
import com.pascal.ezload.service.dashboard.engine.tag.DividendTagDetails;
import com.pascal.ezload.service.financial.EZActionManager;
import com.pascal.ezload.service.model.*;
import com.pascal.ezload.service.sources.Reporting;
import com.pascal.ezload.service.util.DeviseUtil;
import com.pascal.ezload.service.util.finance.Dividend;

import java.util.*;

public class SharePriceBuilder {

    public static final int HISTORICAL_NB_OF_YEAR = 10;

    public enum DIVIDEND_SELECTION { ONLY_REGULAR, ONLY_EXCEPTIONAL, ALL }

    private final EZActionManager actionManager;
    private final CurrenciesIndexBuilder currencies;
    private final PerfIndexBuilder perfIndexBuilder;
    private final List<EZDate> dates;

    public SharePriceBuilder(EZActionManager actionManager, CurrenciesIndexBuilder currencies, PerfIndexBuilder perfIndexBuilder, List<EZDate> dates){
        this.actionManager = actionManager;
        this.currencies = currencies;
        this.perfIndexBuilder = perfIndexBuilder;
        this.dates = dates;
    }

    private final Map<EZShareEQ, Prices> shares2TargetPrices = new HashMap<>();
    private final Map<String, Prices> share2RealDividendsTargetPrices = new HashMap<>(); // les dividendes reellement recu a la date de detachement
    private final Map<String, Prices> share2DividendsWithEstimatesTargetPrices = new HashMap<>(); // avec l'estimation sur l'année en cours
    private final Map<String, Prices> share2HistoricalDividendsTargetPrices = new HashMap<>(); // historique avec l'estimation sur l'année en cours
    private final Map<EZShareEQ, Prices> share2annualDividendYieldsTargetPrices = new HashMap<>();  // avec l'estimation sur l'année en cours
    private final Map<String, Price> shareYear2annualDividend = new HashMap<>();
    private final Map<String, Price> shareYear2annualDividendWithEstimates = new HashMap<>();
    private final Map<EZShareEQ, Prices> share2croissanceDividendAnnualWithEstimates = new HashMap<>();


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

    public Prices getHistoricalDividends(Reporting reporting, EZShareEQ share, int nbOfPastYears, DIVIDEND_SELECTION dividendSelection){
        return share2HistoricalDividendsTargetPrices.computeIfAbsent(share.getEzName()+"_"+nbOfPastYears+"_"+dividendSelection.name(), ezShare -> getHistoricalDividendes(reporting, share, nbOfPastYears, dividendSelection));
    }

    public Prices getDividendsWithCurrentYearEstimates(Reporting reporting, EZShareEQ share, DIVIDEND_SELECTION dividendSelection){
        return share2DividendsWithEstimatesTargetPrices.computeIfAbsent(share.getEzName()+"_"+dividendSelection.name(), ezShare -> {
            Prices prices = getDividends(reporting, share, dividendSelection);
            return updateCurrentYearWithEstimates(reporting, share, prices);

        });
    }

    private Prices updateCurrentYearWithEstimates(Reporting reporting, EZShareEQ share, Prices dividendesPrices){

        Prices pricesWithEstimates = new Prices(dividendesPrices);
        int currentYear = EZDate.today().getYear();
        Price previousAnnualDiv = getAnnualDividend(reporting, share, currentYear - 1, false, DIVIDEND_SELECTION.ONLY_REGULAR);
        Prices croissance = getCroissanceAnnuelDuDividendeWithEstimates(reporting, share);
        PriceAtDate croissanceAnneeCourrante = croissance.getPriceAt(EZDate.today());
        Price estimatedDivForCurrentYear = previousAnnualDiv.plus(previousAnnualDiv.multiply(croissanceAnneeCourrante.divide(Price.CENT)));
        // TODO PASCAL ici pour estimer le dividende de l'année en cours, je rajoute a la derniere année, le min de la croissance
        // Bertrand lui ne prend que la valeur de l'année précedente
        Price alreadyReceivedCurrentAnnualDiv = getAnnualDividend(reporting, share, currentYear, false, DIVIDEND_SELECTION.ONLY_REGULAR);
        if (previousAnnualDiv.getValue() != null && (alreadyReceivedCurrentAnnualDiv.getValue() == null || alreadyReceivedCurrentAnnualDiv.getValue() < estimatedDivForCurrentYear.getValue())) {

            EZDate today = EZDate.today();
            EZDate lastDayOfYear = new EZDate(today.getYear(), 12, 31);
            PriceAtDate lastYearDividend = pricesWithEstimates.getPrices().get(pricesWithEstimates.getPrices().size()-1);
            if (lastYearDividend == null || lastYearDividend.getDate().getYear() != today.getYear()) throw new RuntimeException("Il manque le dividende de cette année: "+pricesWithEstimates.getPrices());
            //
            pricesWithEstimates.replacePriceAt(dividendesPrices.getPrices().size()-1, new PriceAtDate(lastDayOfYear,
                    estimatedDivForCurrentYear.getValue(), true));
        }
        return pricesWithEstimates;
    }

    public static Prices dividendPerYear(List<Dividend> divs, DIVIDEND_SELECTION dividendSelection){
        Prices dividendes = new Prices();
        divs.stream()
                .peek(div -> dividendes.setDevise(div.getDevise()))
                .peek(div -> dividendes.setLabel("Dividendes from : "+div.getSource()))
                .filter(div -> {
                    boolean takeIt = dividendSelection == DIVIDEND_SELECTION.ALL
                            || (div.getFrequency() == Dividend.EnumFrequency.EXCEPTIONEL && dividendSelection == DIVIDEND_SELECTION.ONLY_EXCEPTIONAL)
                            || (div.getFrequency() != Dividend.EnumFrequency.EXCEPTIONEL && dividendSelection == DIVIDEND_SELECTION.ONLY_REGULAR);
                    return takeIt;
                })
                .map(d -> new PriceAtDate(d.getDetachmentDate(), new Price(d.getAmount())))
                .sorted(Comparator.comparing(PriceAtDate::getDate))
                .forEach(dividendes::addPrice);
        return new PerfIndexBuilder(ChartGroupedBy.YEARLY).buildGroupBy(dividendes, true);
    }


    private Prices getHistoricalDividendes(Reporting reporting, EZShareEQ share, int nbOfPastYears, DIVIDEND_SELECTION dividendSelection) {
        List<EZDate> dates = new ArrayList<>(nbOfPastYears);
        int currentYear = EZDate.today().getYear();
        for (int year = currentYear-nbOfPastYears; year <= currentYear; year++){
            dates.add(EZDate.yearPeriod(year));
        }
        return getDividends(reporting, share, dates, dividendSelection);
    }

    public Prices getDividends(Reporting reporting, EZShareEQ share, DIVIDEND_SELECTION dividendSelection) {
        return share2RealDividendsTargetPrices.computeIfAbsent(share.getEzName()+"_"+dividendSelection.name(), ezShare -> getDividends(reporting, share, dates, dividendSelection));
    }

    private Prices getDividends(Reporting reporting, EZShareEQ ezShare, List<EZDate> datesParam, DIVIDEND_SELECTION dividendSelection) {
            try {
                List<Dividend> dividends = actionManager.searchDividends(reporting, ezShare, datesParam.get(0));
                Prices prices = new Prices();
                prices.setLabel("Dividendes of " + ezShare.getEzName());
                if (dividends != null) {
                    Optional<Dividend> firstDividend = dividends.stream().findFirst();
                    if (firstDividend.isPresent()) {
                        prices.setLabel(prices.getLabel()+" source: "+firstDividend.get().getSource());
                        prices.setDevise(firstDividend.get().getDevise());
                        EZDate previousDate = datesParam.get(0);
                        for (EZDate currentDate : datesParam) {
                            PriceAtDate dividendFound = extractDividendForCurrentDate(dividends, previousDate, currentDate, dividendSelection);

                            EZDate date = dividendFound != null ? dividendFound.getDate() : currentDate;

                            float value = 0f;
                            if (dividendFound != null && dividendFound.getValue() > 0) {
                                value = dividendFound.getValue();
                            }
                            PriceAtDate p = new PriceAtDate(date, value, dividendFound != null && dividendFound.isEstimated());
                            if (dividendFound != null){
                                p.addTag(DividendTagDetails.DIVIDEND_TAG_NAME, dividendFound.getTag(DividendTagDetails.DIVIDEND_TAG_NAME));
                            }
                            prices.addPrice(p);
                            previousDate = currentDate;
                        }
                        return currencies.convertPricesToTargetDevise(reporting, prices, true);
                    }
                }
                // Pas de dividendes ou pas encore recu
                prices.setDevise(DeviseUtil.EUR);
                for (EZDate currentDate : datesParam) {
                    prices.addPrice(new PriceAtDate(currentDate));
                }
                return prices;
            } catch (Exception e) {
                reporting.error("Erreur lors de la récupération du dividende de l'action: " + ezShare.getEzName(), e);
                throw new RuntimeException("Erreur lors de la récupération du dividende de l'action: " + ezShare.getEzName(), e);
            }
    }

    private PriceAtDate extractDividendForCurrentDate(List<Dividend> dividends, EZDate previousDate, EZDate currentDate, DIVIDEND_SELECTION dividendSelection) {
        float totalDividendInOriginalDevise = 0f;
        PriceAtDate finalDividend = null;
        boolean estimated = false;
        DividendTagDetails details = new DividendTagDetails();
        for (Dividend div : dividends){
            boolean takeIt = dividendSelection == DIVIDEND_SELECTION.ALL
                    || (div.getFrequency() == Dividend.EnumFrequency.EXCEPTIONEL && dividendSelection == DIVIDEND_SELECTION.ONLY_EXCEPTIONAL)
                    || (div.getFrequency() != Dividend.EnumFrequency.EXCEPTIONEL && dividendSelection == DIVIDEND_SELECTION.ONLY_REGULAR);

            if (takeIt) {
                estimated |= div.isEstimated();
                if (currentDate.isPeriod()) {
                    if (currentDate.contains(div.getDetachmentDate())) {
                        totalDividendInOriginalDevise += div.getAmount();
                        details.add(new DividendInfo(div.getFrequency() == Dividend.EnumFrequency.EXCEPTIONEL ? DividendInfo.TYPE.EXCEPTIONAL : DividendInfo.TYPE.REGULAR, new PriceAtDate(div.getDetachmentDate(), div.getAmount(), div.isEstimated())));
                        finalDividend = new PriceAtDate(div.getDetachmentDate(), totalDividendInOriginalDevise, estimated);
                        estimated = currentDate.endPeriodDate().isAfter(EZDate.today()); // si je suis dans la periode actuelle, je n'ai peut etre pas encore toutes les données, donc c'est estimée
                    }
                } else if (div.getDetachmentDate().isAfter(previousDate)
                        && div.getDetachmentDate().isBeforeOrEquals(currentDate)) {
                    totalDividendInOriginalDevise += div.getAmount();
                    details.add(new DividendInfo(div.getFrequency() == Dividend.EnumFrequency.EXCEPTIONEL ? DividendInfo.TYPE.EXCEPTIONAL : DividendInfo.TYPE.REGULAR, new PriceAtDate(div.getDetachmentDate(), div.getAmount(), div.isEstimated())));
                    finalDividend = new PriceAtDate(div.getDetachmentDate(), totalDividendInOriginalDevise, estimated);
                    estimated = false;
                }
            }
        }
        if (finalDividend != null) finalDividend.addTag(DividendTagDetails.DIVIDEND_TAG_NAME, details);
        return finalDividend;
    }


    public Prices getRendementDividendeAnnuel(Reporting reporting, EZShareEQ share) {
        // Le rendement du dividend
        return share2annualDividendYieldsTargetPrices.computeIfAbsent(share, ezShare -> {
            Prices annualDividendYields = new Prices();
            annualDividendYields.setLabel("Rendement du dividende de "+share.getEzName());
            annualDividendYields.setDevise(DeviseUtil.SPECIAL_PERCENT);
            for (EZDate currentDate : dates) {
                int year = currentDate.getYear();

                // TODO PASCAL Le rendement de l'année en cours intégre l'estimation, il est donc un peu supérieur a celui de bertrand
                // Pour son calcul Bertrand ne prend que le dividende de l'année passé, voir la methode: updateCurrentYearWithEstimates
                Price annualDividend = getAnnualDividend(reporting, share, year, true, DIVIDEND_SELECTION.ONLY_REGULAR);

                Price rendement = Price.ZERO;
                if (annualDividend.getValue() != null && annualDividend.getValue() > 0) {
                    Prices p = getPricesToTargetDevise(reporting, ezShare);
                    if (p != null) {
                        Price price = p.getPriceAt(currentDate);
                        rendement = price.getValue()  == null || price.getValue() == 0 ? new Price() : annualDividend.multiply(Price.CENT).divide(price);

                    }
                }
                annualDividendYields.addPrice(new PriceAtDate(currentDate, rendement));
            }
            return annualDividendYields;
        });
    }

    public Price getAnnualDividend(Reporting reporting, EZShareEQ ezShare, int year, boolean withEstimatesForCurrentYear, DIVIDEND_SELECTION dividendSelection) {
        // Je ne peux pas merger les 2 Maps, sinon concurrencyException, car pour calcule le divWithEstimates je dois d'abord calcule l'autre
        Map<String, Price> selectedMap = withEstimatesForCurrentYear ? shareYear2annualDividendWithEstimates : shareYear2annualDividend;
        return selectedMap.computeIfAbsent(ezShare.getEzName()+"_"+year+"_"+dividendSelection.name(), ezShareEQ -> {
            Prices dividends = withEstimatesForCurrentYear ? getDividendsWithCurrentYearEstimates(reporting, ezShare, dividendSelection) : getDividends(reporting, ezShare, dividendSelection);
            EZDate today = EZDate.today();

            Price annualDividend = new Price(0, year >= today.getYear());

            for (PriceAtDate dividend : dividends.getPrices()) {
                if (dividend.getDate().getYear() == year) {
                    annualDividend = annualDividend.plus(dividend);
                }
                if (dividend.getDate().getYear() > year)
                    break;
            }

            return annualDividend;
        });
    }


    // Pour l'année en cours, je prends la plus petite croissance sur les 10 dernieres années
    // Revenue & Dividendes, prends un autre calcul
    public Prices getCroissanceAnnuelDuDividendeWithEstimates(Reporting reporting, EZShareEQ ezShare) {
        // donne la croissance annuel du dividendes avec l'estimation pour l'annee courrante
        // il y aura une date par année
        return share2croissanceDividendAnnualWithEstimates.computeIfAbsent(ezShare, s -> {
                        Prices allDividends = getHistoricalDividends(reporting, ezShare, HISTORICAL_NB_OF_YEAR, DIVIDEND_SELECTION.ONLY_REGULAR); // remonte a 10 ans en arriere
                        Prices dividendPerYear = perfIndexBuilder.buildGroupBy(allDividends, ChartGroupedBy.YEARLY,true);
                        Prices croissance = perfIndexBuilder.buildPerfPrices(dividendPerYear, ChartPerfFilter.VARIATION_EN_PERCENT);

                        PriceAtDate minimalCroissance = croissance.getPrices().stream()
                                                                            .filter(p -> p.getValue() != null)
                                                                            .filter(p -> p.getDate().getYear() < EZDate.today().getYear()) // l'année courrante n'est pas terminé, ne regarde pas l'année en cours
                                                                            .min(Comparator.comparing(Price::getValue))
                                                                            .orElse(new PriceAtDate(EZDate.yearPeriod(EZDate.today().getYear()), 0, true));

// TODO PASCAL pour se rapprocher des chiffres de bertrand
// prendre le min entre la derniere année, la moyenne sur 5 ans, la moyenne sur 10 ans
                        int size = croissance.getPrices().size();
                        float croissanceAnneeEnCours = croissance.getPrices().get(size-1).getValue() == null ? Float.NEGATIVE_INFINITY : croissance.getPrices().get(size-1).getValue();
                        if (croissanceAnneeEnCours < minimalCroissance.getValue()){
                            // si la croissance de l'année en cours est inférieure à la plus petite croissance depuis le début de la période, alors utilise la croissance minimale
                            croissance.replacePriceAt(size-1, new PriceAtDate(EZDate.yearPeriod(EZDate.today().getYear()), minimalCroissance.getValue(), true));
                        }
                        return croissance;
                    });
    }
}
