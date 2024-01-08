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
    private final ShareSelectionBuilder shareSelectionBuilder;

    private static final String NO_LABEL = "";
    private static final Float NO_VALUE = null;


    public ShareIndexBuilder(PortfolioIndexBuilderV2.Result portfolioIndexResult, SharePriceBuilder.Result sharePriceResult, CurrenciesIndexBuilder.Result currenciesResult, ShareSelectionBuilder shareSelectionBuilder){
        this.portfolioIndexResult = portfolioIndexResult;
        this.sharePriceResult = sharePriceResult;
        this.currenciesResult = currenciesResult;
        this.shareSelectionBuilder = shareSelectionBuilder;
    }


    public Result build(Reporting reporting, List<EZDate> dates, List<ChartIndexV2> indexV2Selection) {
        Result result = new Result();

        Set<ChartShareIndexConfig> shareIndexSet = indexV2Selection
                .stream()
                .map(ChartIndexV2::getShareIndexConfig)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        build(reporting, dates, shareIndexSet, result);
        return result;
    }


    private void build(Reporting reporting, List<EZDate> dates, Set<ChartShareIndexConfig> shareIndexesConfig, Result r) {
        shareIndexesConfig
                .forEach(si -> {
                    ShareSelectionBuilder.Result shareSelectionResult = shareSelectionBuilder.build(si);

                    shareSelectionResult.getSelectedShares()
                            .forEach(ezShare -> {
                                switch (si.getShareIndex()) {
                                    case SHARE_PRICES: addIndexInResult(r, SHARE_PRICES, ezShare, sharePriceResult.getTargetPrices(reporting, ezShare)); break;
                                    case SHARE_PRU_NET: buildPricesAndSaveInResult(dates, ezShare, r, portfolioIndexResult.getDate2share2PRUNet(), SHARE_PRU_NET); break;
                                    case SHARE_COUNT: buildPricesAndSaveInResult(dates, ezShare, r, portfolioIndexResult.getDate2share2ShareNb(), SHARE_COUNT); break;
                                    case SHARE_DIVIDEND: addIndexInResult(r, SHARE_DIVIDEND, ezShare, sharePriceResult.getDividends(reporting, ezShare)); break;
                                    case SHARE_DIVIDEND_YIELD: addIndexInResult(r, SHARE_DIVIDEND_YIELD, ezShare, sharePriceResult.getDividendYield(reporting, ezShare)); break;
                                    case SHARE_PRU_NET_WITH_DIVIDEND: buildPricesAndSaveInResult(dates, ezShare, r, portfolioIndexResult.getDate2share2PRUNetDividend(), SHARE_PRU_NET_WITH_DIVIDEND); break;
                                    case SHARE_BUY_SOLD_WITH_DETAILS: buildPricesAndSaveInResult(dates, ezShare, r, portfolioIndexResult.getDate2share2BuyOrSoldAmount(), SHARE_BUY_SOLD_WITH_DETAILS); break;
                                    default:
                                        throw new IllegalStateException("Missing case");
                                }
                });
        });
    }


    private void buildPricesAndSaveInResult(List<EZDate> dates, EZShare ezShare, Result r, Map<EZDate, Map<EZShare, Float>> date2share2value, ShareIndex shareIndex) {
        Prices prices = new Prices();
        prices.setDevise(currenciesResult.getTargetDevise());
        prices.setLabel(shareIndex.name()+" of "+ezShare.getEzName());
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


        public Map<ShareIndex, Map<EZShare, Prices>> getShareIndex2TargetPrices() { ShareIndex doit etre computed avec le perfSettings.computeKey dans PerfIndexBuilder
            return shareIndex2share2TargetPrices;
        }
    }
}
