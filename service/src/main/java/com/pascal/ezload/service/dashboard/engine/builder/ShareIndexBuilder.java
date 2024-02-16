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

    private final SharePriceBuilder sharePriceResult;
    private final PortfolioIndexBuilder portfolioIndexResult;
    private final CurrenciesIndexBuilder currenciesResult;
    private final Reporting reporting;
    private final List<EZDate> dates;


    public ShareIndexBuilder(Reporting reporting, List<EZDate> dates, PortfolioIndexBuilder portfolioIndexResult, SharePriceBuilder sharePriceResult, CurrenciesIndexBuilder currenciesResult){
        this.reporting = reporting;
        this.dates = dates;
        this.portfolioIndexResult = portfolioIndexResult;
        this.sharePriceResult = sharePriceResult;
        this.currenciesResult = currenciesResult;
    }


    private Price get(ShareIndex shareIndex, EZShareEQ ezShare, EZDate date) {
            return switch (shareIndex) {
                case SHARE_PRICE -> sharePriceResult.getPricesToTargetDevise(reporting, ezShare).getPriceAt(date);
                case CUMULABLE_SHARE_DIVIDEND -> sharePriceResult.getDividendsWithCurrentYearEstimates(reporting, ezShare).getPriceAt(date);
                case SHARE_ANNUAL_DIVIDEND_YIELD -> sharePriceResult.getAnnualDividendYieldWithEstimates(reporting, ezShare).getPriceAt(date);
                case SHARE_COUNT -> portfolioIndexResult.getDate2share2ShareNb(ezShare).getPriceAt(date);
                case CUMULABLE_SHARE_DIVIDEND_YIELD_BASED_ON_PRU_BRUT -> {
                        Price pru = portfolioIndexResult.getDate2share2PRUBrut(ezShare).getPriceAt(date);
                        PriceAtDate p = sharePriceResult.getDividendsWithCurrentYearEstimates(reporting, ezShare).getPriceAt(date);
                        if (p == null || pru == null || pru.getValue() == 0) yield new Price();
                        yield p.multiply(new Price(100)).divide(pru);
                }
                case CUMULABLE_SHARE_DIVIDEND_YIELD_BASED_ON_PRU_NET -> {
                        Price pru = portfolioIndexResult.getDate2share2PRUNet(ezShare).getPriceAt(date);
                        PriceAtDate p = sharePriceResult.getDividendsWithCurrentYearEstimates(reporting, ezShare).getPriceAt(date);
                        if (p == null || pru == null || pru.getValue() == 0) yield new Price();
                        yield p.multiply(new Price(100)).divide(pru);
                }
                case SHARE_PRU_BRUT -> portfolioIndexResult.getDate2share2PRUBrut(ezShare).getPriceAt(date);
                case SHARE_PRU_NET -> portfolioIndexResult.getDate2share2PRUNet(ezShare).getPriceAt(date);
                case CUMULABLE_SHARE_BUY_SOLD -> portfolioIndexResult.getDate2share2BuyOrSoldAmount(ezShare).getPriceAt(date);
                case CUMULABLE_SHARE_BUY -> portfolioIndexResult.getDate2share2BuyAmount(ezShare).getPriceAt(date);
                case CUMULABLE_SHARE_SOLD -> portfolioIndexResult.getDate2share2SoldAmount(ezShare).getPriceAt(date);
            };
    }


    private final Map<ShareIndex, Map<EZShareEQ, Prices>> shareIndex2share2TargetPrices = new HashMap<>();

    public Prices getShareIndex2TargetPrices(ShareIndex shareIndex, EZShareEQ ezShare) {
        Map<EZShareEQ, Prices> share2Prices = shareIndex2share2TargetPrices.computeIfAbsent(shareIndex, si -> new HashMap<>());
        return share2Prices.computeIfAbsent(ezShare, sh -> {
            Prices prices = new Prices();
            prices.setDevise(currenciesResult.getTargetDevise());
            prices.setLabel(shareIndex.name()+" of "+ezShare.getEzName());
            for (EZDate date : dates) {
                Price value = get(shareIndex, ezShare, date);
                prices.addPrice(value == null ? new PriceAtDate(date) : new PriceAtDate(date, value));
            }
            return prices;
        });
    }
}
