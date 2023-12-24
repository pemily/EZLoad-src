package com.pascal.ezload.service.dashboard.engine.builder;

import com.pascal.ezload.service.dashboard.config.ChartIndexV2;
import com.pascal.ezload.service.dashboard.config.ChartShareIndexConfig;
import com.pascal.ezload.service.dashboard.config.ShareIndex;
import com.pascal.ezload.service.model.EZDate;
import com.pascal.ezload.service.model.EZShare;
import com.pascal.ezload.service.model.PriceAtDate;
import com.pascal.ezload.service.model.Prices;
import com.pascal.ezload.service.sources.Reporting;

import java.util.*;
import java.util.stream.Collectors;

import static com.pascal.ezload.service.dashboard.config.ShareIndex.*;

public class ShareIndexBuilder {

    private final SharePriceBuilder.Result sharePriceResult;
    private final PortfolioIndexBuilderV2.Result portfolioIndexResult;
    private final CurrenciesIndexBuilder.Result currenciesResult;
    private final ShareSelectionBuilder.Result shareSelectionResult;

    private static final String NO_LABEL = "";
    private static final Float NO_VALUE = null;


    public ShareIndexBuilder(PortfolioIndexBuilderV2.Result portfolioIndexResult, SharePriceBuilder.Result sharePriceResult, CurrenciesIndexBuilder.Result currenciesResult, ShareSelectionBuilder.Result shareSelectionResult){
        this.portfolioIndexResult = portfolioIndexResult;
        this.sharePriceResult = sharePriceResult;
        this.currenciesResult = currenciesResult;
        this.shareSelectionResult = shareSelectionResult;
    }


    public Result build(Reporting reporting, List<EZDate> dates, List<ChartIndexV2> indexV2Selection) {
        Result result = new Result();

        Set<ShareIndex> shareIndexSet = indexV2Selection
                .stream()
                .map(ChartIndexV2::getShareIndexConfig)
                .filter(Objects::nonNull)
                .map(ChartShareIndexConfig::getShareIndex)
                .collect(Collectors.toSet());

        build(reporting, dates, shareIndexSet, result);
        return result;
    }


    private void build(Reporting reporting, List<EZDate> dates, Set<ShareIndex> shareIndexes, Result r) {
        shareSelectionResult.getSelectedShares()
                .forEach(ezShare -> {
                    shareIndexes
                            .forEach(si -> {
                                switch (si) {
                                    case SHARE_PRICES: buildSharePrices(reporting, ezShare, r); break;
                                    case SHARE_PRU: buildPricesAndSaveInResult(dates, ezShare, r, portfolioIndexResult.getDate2share2PRU(), SHARE_PRU); break;
                                    case SHARE_COUNT: buildPricesAndSaveInResult(dates, ezShare, r, portfolioIndexResult.getDate2share2ShareNb(), SHARE_COUNT); break;
                                    case SHARE_DIVIDEND: sharePriceResult.getDividends(reporting, ezShare); break;
                                    case SHARE_DIVIDEND_YIELD: sharePriceResult.getDividendYield(reporting, ezShare); break;
                                    case SHARE_PRU_WITH_DIVIDEND: buildPricesAndSaveInResult(dates, ezShare, r, portfolioIndexResult.getDate2share2PRUDividend(), SHARE_PRU_WITH_DIVIDEND); break;
                                    case SHARE_BUY_SOLD_WITH_DETAILS: buildPricesAndSaveInResult(dates, ezShare, r, portfolioIndexResult.getDate2share2BuyOrSoldAmount(), SHARE_BUY_SOLD_WITH_DETAILS); break;
                                    default:
                                        throw new IllegalStateException("Missing case");
                                }
                            });
                });
    }


    private void buildSharePrices(Reporting reporting, EZShare ezShare, Result r) {
        Prices prices = sharePriceResult.getTargetPrices(reporting, ezShare);
        addIndexInResult(r, SHARE_PRICES, ezShare, prices);
    }


    private void buildPricesAndSaveInResult(List<EZDate> dates, EZShare ezShare, Result r, Map<EZDate, Map<EZShare, Float>> date2share2value, ShareIndex shareIndex) {
        Prices prices = new Prices();
        prices.setDevise(currenciesResult.getTargetDevise());
        for (EZDate date : dates) {
            Map<EZShare, Float> share2value = date2share2value.get(date);
            share2value
                .entrySet()
                .stream()
                .filter(e -> e.getKey().equals(ezShare))
                .forEach(e -> prices.addPrice(date, new PriceAtDate(date, e.getValue())));
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


        public Map<ShareIndex, Map<EZShare, Prices>> getShareIndex2TargetPrices() {
            return shareIndex2share2TargetPrices;
        }
    }
}
