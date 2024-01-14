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
                                        case SHARE_PRU_NET:
                                            buildPricesAndSaveInResult(r, SHARE_PRU_NET, ezShare, portfolioIndexResult.getDate2share2PRUNet(), dates);
                                            break;
                                        case SHARE_COUNT:
                                            buildPricesAndSaveInResult(r, SHARE_COUNT, ezShare, portfolioIndexResult.getDate2share2ShareNb(), dates);
                                            break;
                                        case CUMULABLE_SHARE_DIVIDEND:
                                            addIndexInResult(r, CUMULABLE_SHARE_DIVIDEND, ezShare, sharePriceResult.getDividends(reporting, ezShare));
                                            break;
                                        case CUMULABLE_SHARE_DIVIDEND_YIELD:
                                            addIndexInResult(r, CUMULABLE_SHARE_DIVIDEND_YIELD, ezShare, sharePriceResult.getDividendYield(reporting, ezShare));
                                            break;
                                        case SHARE_PRU_NET_WITH_DIVIDEND:
                                            buildPricesAndSaveInResult(r, SHARE_PRU_NET_WITH_DIVIDEND, ezShare, portfolioIndexResult.getDate2share2PRUNetDividend(), dates);
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
        Prices prices = new Prices();
        prices.setDevise(currenciesResult.getTargetDevise());
        prices.setLabel(shareIndex.name()+" of "+ezShare.getEzName());
        for (EZDate date : dates) {
            Map<EZShareEQ, Float> share2value = date2share2value.get(date);
            Float value = share2value.get(ezShare);
            prices.addPrice(date, value != null ? new PriceAtDate(date, value) : new PriceAtDate(date));
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
