package com.pascal.ezload.service.dashboard.engine.builder;

import com.pascal.ezload.service.dashboard.config.ChartIndex;
import com.pascal.ezload.service.dashboard.config.ShareIndex;
import com.pascal.ezload.service.model.EZDate;
import com.pascal.ezload.service.model.EZShare;
import com.pascal.ezload.service.model.PriceAtDate;
import com.pascal.ezload.service.model.Prices;
import com.pascal.ezload.service.sources.Reporting;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

import static com.pascal.ezload.service.dashboard.config.ShareIndex.*;

public class ShareIndexBuilder {

    private final SharePriceBuilder.Result sharePriceResult;
    private final PortfolioIndexBuilder.Result portfolioIndexResult;
    private final CurrenciesIndexBuilder.Result currenciesResult;
    private final ShareSelectionBuilder shareSelectionBuilder;


    public ShareIndexBuilder(PortfolioIndexBuilder.Result portfolioIndexResult, SharePriceBuilder.Result sharePriceResult, CurrenciesIndexBuilder.Result currenciesResult, ShareSelectionBuilder shareSelectionBuilder){
        this.portfolioIndexResult = portfolioIndexResult;
        this.sharePriceResult = sharePriceResult;
        this.currenciesResult = currenciesResult;
        this.shareSelectionBuilder = shareSelectionBuilder;
    }


    public Result build(Reporting reporting, List<EZDate> dates, List<ChartIndex> indexSelection) {
        Result result = new Result();
        build(reporting, dates, indexSelection, result);
        return result;
    }


    private void build(Reporting reporting, List<EZDate> dates, List<ChartIndex> shareIndexesConfig, Result r) {
        shareIndexesConfig
                .forEach(si -> {
                    if (si.getShareIndexConfig() != null) {
                        ShareSelectionBuilder.Result shareSelectionResult = shareSelectionBuilder.build(si.getShareIndexConfig());
                        r.chartIndexId2ShareSelection.put(si.getId(), shareSelectionResult);

                        shareSelectionResult.getSelectedShares()
                                .forEach(ezShare -> {
                                    switch (si.getShareIndexConfig().getShareIndex()) {
                                        case SHARE_PRICE:
                                            addIndexInResult(r, SHARE_PRICE, ezShare, sharePriceResult.getTargetPrices(reporting, ezShare));
                                            break;
                                        case SHARE_COUNT:
                                            buildPricesAndSaveInResult(r, SHARE_COUNT, ezShare, portfolioIndexResult.getDate2share2ShareNb(), dates);
                                            break;
                                        case CUMULABLE_SHARE_DIVIDEND:
                                            addIndexInResult(r, CUMULABLE_SHARE_DIVIDEND, ezShare, sharePriceResult.getDividends(reporting, ezShare));
                                            break;
                                        case SHARE_ANNUAL_DIVIDEND_YIELD:
                                            addIndexInResult(r, SHARE_ANNUAL_DIVIDEND_YIELD, ezShare, sharePriceResult.getAnnualDividendYield(reporting, ezShare));
                                            break;
                                        case CUMULABLE_SHARE_DIVIDEND_YIELD_BASED_ON_PRU_BRUT:
                                            buildPricesAndSaveInResult(r, CUMULABLE_SHARE_DIVIDEND_YIELD_BASED_ON_PRU_BRUT, ezShare, portfolioIndexResult.getDate2share2PRUBrut(), dates,
                                                                                (d, pru) -> {
                                                                                    PriceAtDate p = sharePriceResult.getDividends(reporting, ezShare).getPriceAt(d);
                                                                                    if (p == null || p.getPrice() == null || pru == null) return new PriceAtDate(d, p.isEstimated());
                                                                                    return new PriceAtDate(d, (p.getPrice() * 100f) / pru, p.isEstimated());
                                                                                });
                                            break;
                                        case CUMULABLE_SHARE_DIVIDEND_YIELD_BASED_ON_PRU_NET:
                                            buildPricesAndSaveInResult(r, CUMULABLE_SHARE_DIVIDEND_YIELD_BASED_ON_PRU_NET, ezShare, portfolioIndexResult.getDate2share2PRUNet(), dates,
                                                                                    (d, pru) -> {
                                                                                        PriceAtDate p = sharePriceResult.getDividends(reporting, ezShare).getPriceAt(d);
                                                                                        if (p == null || p.getPrice() == null || pru == null) return new PriceAtDate(d, p.isEstimated());
                                                                                        return new PriceAtDate(d, (p.getPrice() * 100f)/ pru, p.isEstimated());
                                                                                    });
                                            break;
                                        case SHARE_PRU_BRUT:
                                            buildPricesAndSaveInResult(r, SHARE_PRU_BRUT, ezShare, portfolioIndexResult.getDate2share2PRUBrut(), dates);
                                            break;
                                        case SHARE_PRU_NET:
                                            buildPricesAndSaveInResult(r, SHARE_PRU_NET, ezShare, portfolioIndexResult.getDate2share2PRUNet(), dates);
                                            break;
                                        case CUMULABLE_SHARE_BUY_SOLD:
                                            buildPricesAndSaveInResult(r, CUMULABLE_SHARE_BUY_SOLD, ezShare, portfolioIndexResult.getDate2share2BuyOrSoldAmount(), dates);
                                            break;
                                        case CUMULABLE_SHARE_BUY:
                                            buildPricesAndSaveInResult(r, CUMULABLE_SHARE_BUY, ezShare, portfolioIndexResult.getDate2share2BuyAmount(), dates);
                                            break;
                                        case CUMULABLE_SHARE_SOLD:
                                            buildPricesAndSaveInResult(r, CUMULABLE_SHARE_SOLD, ezShare, portfolioIndexResult.getDate2share2SoldAmount(), dates);
                                            break;
                                        default:
                                            throw new IllegalStateException("Missing case");
                                    }
                                });
                    }
        });
    }

    private void buildPricesAndSaveInResult(Result r, ShareIndex shareIndex, EZShareEQ ezShare,Map<EZDate, Map<EZShareEQ, Float>> date2share2value, List<EZDate> dates) {
        buildPricesAndSaveInResult(r, shareIndex, ezShare, date2share2value, dates, (d, v) -> v == null ? new PriceAtDate(d, false) : new PriceAtDate(d, v, false));
    }

    private void buildPricesAndSaveInResult(Result r, ShareIndex shareIndex, EZShareEQ ezShare, Map<EZDate, Map<EZShareEQ, Float>> date2share2value, List<EZDate> dates, BiFunction<EZDate, Float, PriceAtDate> valueTransformation) {
        Prices prices = new Prices();
        prices.setDevise(currenciesResult.getTargetDevise());
        prices.setLabel(shareIndex.name()+" of "+ezShare.getEzName());
        for (EZDate date : dates) {
            Map<EZShareEQ, Float> share2value = date2share2value.get(date);
            Float value = share2value.get(ezShare);
            prices.addPrice(date, valueTransformation.apply(date, value));
        }
        addIndexInResult(r, shareIndex, ezShare, prices);
    }


    private void addIndexInResult(Result r, ShareIndex index, EZShare ezShare, Prices prices) {
        if (prices != null) {
            r.getShareIndex2TargetPrices().compute(index, (k, v) -> {
                if (v == null)  v = new HashMap<>();
                v.put(ezShare, prices);
                return v; }
            );
        }
    }


    public static class Result {
        private final Map<ShareIndex, Map<EZShare, Prices>> shareIndex2share2TargetPrices = new HashMap<>();
        private final Map<String,  ShareSelectionBuilder.Result> chartIndexId2ShareSelection = new HashMap<>();

        public ShareSelectionBuilder.Result getShareSelection(String chartIndexId){
            return chartIndexId2ShareSelection.get(chartIndexId);
        }

        public Map<ShareIndex, Map<EZShare, Prices>> getShareIndex2TargetPrices() {
            return shareIndex2share2TargetPrices;
        }
    }
}
