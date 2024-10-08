package com.pascal.ezload.common.util;

import com.pascal.ezload.common.model.EZDate;
import com.pascal.ezload.common.model.Price;
import com.pascal.ezload.common.model.PriceAtDate;
import com.pascal.ezload.common.util.DeviseUtil;
import com.pascal.ezload.common.util.JsonUtil;
import com.pascal.ezload.service.model.Prices;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

public class PricesCached {
    private final String cacheDir;

    private final Map<String, Prices> firstCacheLevel = new HashMap<>();

    public PricesCached(String cacheDir){
        this.cacheDir = cacheDir;
    }


    public boolean exists(String cacheName){
        return getCacheFile(cacheName).exists();
    }

    // Warning do not save the Prices tags for the moment
    public void save(String cacheName, Prices prices) throws IOException {
        SerialisablePrices pricesToSave = new SerialisablePrices();
        pricesToSave.setPriceLabel(prices.getLabel());
        pricesToSave.setDeviseCode(prices.getDevise() == null ? null : prices.getDevise().getCode());
        prices.getPrices().forEach(priceAtDate -> {
            PriceAtDateJson priceAtDateJson = new PriceAtDateJson();
            priceAtDateJson.setEstimated(priceAtDate.isEstimated());
            priceAtDateJson.setValue(priceAtDate.getValue());
            priceAtDateJson.setEpochSecond(priceAtDate.getDate().toEpochSecond());
            pricesToSave.getPrices().add(priceAtDateJson);
        });
        try (FileWriter f = new FileWriter(getCacheFile(cacheName), StandardCharsets.UTF_8)) {
            JsonUtil.createDefaultWriter().writeValue(f, pricesToSave);
        }
        firstCacheLevel.put(cacheName, prices);
    }

    public Prices load(String cacheName) throws IOException {
        Prices p = firstCacheLevel.get(cacheName);
        if (p != null){
            return p;
        }
        try (InputStream fileInputStream = new BufferedInputStream(new FileInputStream(getCacheFile(cacheName)))) {
            SerialisablePrices savedPrices = JsonUtil.readWithDefaultMapper(fileInputStream, SerialisablePrices.class);
            Prices prices = new Prices();
            prices.setLabel(savedPrices.getPriceLabel());
            prices.setDevise(savedPrices.getDeviseCode() == null ? null : DeviseUtil.foundByCode(savedPrices.getDeviseCode()));

            savedPrices.getPrices()
                    .forEach(priceAtDateJson -> {
                        Price price = new Price(priceAtDateJson.getValue(), priceAtDateJson.isEstimated());
                        PriceAtDate priceAtDate = new PriceAtDate(new EZDate(priceAtDateJson.getEpochSecond()), price);
                        prices.addPrice(priceAtDate);
            });

            firstCacheLevel.put(cacheName, prices);
            return prices;
        }
    }


    private File getCacheFile(String cacheName){
        cacheName = cacheName.replaceAll("[*?:/\\\\]", "_");
        return new File(cacheDir+File.separator+cacheName+".json");
    }


    public static class SerialisablePrices implements java.io.Serializable{
        private String priceLabel, deviseCode;
        private List<PriceAtDateJson> prices = new LinkedList<>();

        public String getPriceLabel() {
            return priceLabel;
        }

        public void setPriceLabel(String priceLabel) {
            this.priceLabel = priceLabel;
        }

        public String getDeviseCode() {
            return deviseCode;
        }

        public void setDeviseCode(String deviseCode) {
            this.deviseCode = deviseCode;
        }

        public List<PriceAtDateJson> getPrices() {
            return prices;
        }

        public void setPrices(List<PriceAtDateJson> prices) {
            this.prices = prices;
        }
    }

    public static class PriceAtDateJson {
        private long epochSecond;
        private Float value;
        private boolean estimated;


        public Float getValue() {
            return value;
        }

        public void setValue(Float value) {
            this.value = value;
        }

        public boolean isEstimated() {
            return estimated;
        }

        public void setEstimated(boolean estimated) {
            this.estimated = estimated;
        }

        public long getEpochSecond() {
            return epochSecond;
        }

        public void setEpochSecond(long epochSecond) {
            this.epochSecond = epochSecond;
        }
    }
}
