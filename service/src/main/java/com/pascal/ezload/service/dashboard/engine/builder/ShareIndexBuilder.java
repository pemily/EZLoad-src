package com.pascal.ezload.service.dashboard.engine.builder;

import com.pascal.ezload.service.dashboard.config.ShareIndex;
import com.pascal.ezload.service.model.EZDate;
import com.pascal.ezload.service.model.Price;
import com.pascal.ezload.service.model.PriceAtDate;
import com.pascal.ezload.service.model.Prices;
import com.pascal.ezload.service.sources.Reporting;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShareIndexBuilder {

    private final SharePriceBuilder sharePriceBuilder;
    private final PortfolioIndexBuilder portfolioIndexBuilder;
    private final CurrenciesIndexBuilder currenciesBuilder;
    private final PerfIndexBuilder perfIndexBuilder;
    private final Reporting reporting;
    private final List<EZDate> dates;


    public ShareIndexBuilder(Reporting reporting, List<EZDate> dates, PortfolioIndexBuilder portfolioIndexBuilder, SharePriceBuilder sharePriceBuilder, CurrenciesIndexBuilder currenciesBuilder, PerfIndexBuilder perfIndexBuilder){
        this.reporting = reporting;
        this.dates = dates;
        this.portfolioIndexBuilder = portfolioIndexBuilder;
        this.sharePriceBuilder = sharePriceBuilder;
        this.currenciesBuilder = currenciesBuilder;
        this.perfIndexBuilder = perfIndexBuilder;
    }


    private Price get(ShareIndex shareIndex, EZShareEQ ezShare, EZDate date) {
            return switch (shareIndex) {
                case SHARE_PRICE -> sharePriceBuilder.getPricesToTargetDevise(reporting, ezShare).getPriceAt(date);
                case CUMULABLE_SHARE_DIVIDEND -> sharePriceBuilder.getDividendsWithCurrentYearEstimates(reporting, ezShare).getPriceAt(date);
                case SHARE_ANNUAL_DIVIDEND_YIELD -> sharePriceBuilder.getRendementDividendeAnnuel(reporting, ezShare).getPriceAt(date);
                case SHARE_COUNT -> portfolioIndexBuilder.getDate2share2ShareNb(ezShare).getPriceAt(date);
                case CUMULABLE_SHARE_DIVIDEND_YIELD_BASED_ON_PRU_BRUT -> {
                        Price pru = portfolioIndexBuilder.getDate2share2PRUBrut(ezShare).getPriceAt(date);
                        PriceAtDate p = sharePriceBuilder.getDividendsWithCurrentYearEstimates(reporting, ezShare).getPriceAt(date);
                        if (p == null || pru == null || pru.getValue() == null || pru.getValue() == 0) yield new Price();
                        yield p.multiply(Price.CENT).divide(pru);
                }
                case CUMULABLE_SHARE_DIVIDEND_YIELD_BASED_ON_PRU_NET -> {
                        Price pru = portfolioIndexBuilder.getDate2share2PRUNet(ezShare).getPriceAt(date);
                        PriceAtDate p = sharePriceBuilder.getDividendsWithCurrentYearEstimates(reporting, ezShare).getPriceAt(date);
                        if (p == null || pru == null || pru.getValue() == null || pru.getValue() == 0) yield new Price();
                        yield p.multiply(Price.CENT).divide(pru);
                }
                case SHARE_PRU_BRUT -> portfolioIndexBuilder.getDate2share2PRUBrut(ezShare).getPriceAt(date);
                case SHARE_PRU_NET -> portfolioIndexBuilder.getDate2share2PRUNet(ezShare).getPriceAt(date);
                case CUMULABLE_SHARE_BUY_SOLD -> portfolioIndexBuilder.getDate2share2BuyOrSoldAmount(ezShare).getPriceAt(date);
                case CUMULABLE_SHARE_BUY -> portfolioIndexBuilder.getDate2share2BuyAmount(ezShare).getPriceAt(date);
                case CUMULABLE_SHARE_SOLD -> portfolioIndexBuilder.getDate2share2SoldAmount(ezShare).getPriceAt(date);
                case ACTION_CROISSANCE -> sharePriceBuilder.getCroissanceAnnuelDuDividendeWithEstimates(reporting, ezShare).getPriceAt(date);
                };
    }


    private final Map<ShareIndex, Map<EZShareEQ, Prices>> shareIndex2share2TargetPrices = new HashMap<>();

    public Prices getShareIndex2TargetPrices(ShareIndex shareIndex, EZShareEQ ezShare) {
        Map<EZShareEQ, Prices> share2Prices = shareIndex2share2TargetPrices.computeIfAbsent(shareIndex, si -> new HashMap<>());
        return share2Prices.computeIfAbsent(ezShare, sh -> {
            Prices prices = new Prices();
            prices.setDevise(currenciesBuilder.getTargetDevise());
            prices.setLabel(shareIndex.name()+" of "+ezShare.getEzName());
            for (EZDate date : dates) {
                Price value = get(shareIndex, ezShare, date);
                prices.addPrice(value == null ? new PriceAtDate(date) : new PriceAtDate(date, value));
            }
            return prices;
        });
    }
}
