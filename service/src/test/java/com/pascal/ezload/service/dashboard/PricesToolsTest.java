package com.pascal.ezload.service.dashboard;

import com.pascal.ezload.service.model.EZDate;
import com.pascal.ezload.service.model.PriceAtDate;
import com.pascal.ezload.service.model.Prices;
import com.pascal.ezload.service.util.finance.PricesTools;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

public class PricesToolsTest {


    @Test
    public void fillPricesForAListOfDatesVerySimpleTest1() {
        List<PriceAtDate> pricesAtDates = Arrays.asList(
                new PriceAtDate(new EZDate(2000,1,10), 1)
        );
        List<EZDate> dates = Arrays.asList(
                new EZDate(2002,1,10)
        );

        Prices prices = new Prices();
        new PricesTools<>(pricesAtDates.stream(), dates, PriceAtDate::getDate, p -> p, prices).fillPricesForAListOfDates();
        
        Assert.assertEquals(1, prices.getPrices().size());
        Assert.assertEquals(new EZDate(2000,1,10), prices.getPrices().get(0).getDate());
    }

    @Test
    public void fillPricesForAListOfDatesVerySimpleTest2() {
        List<PriceAtDate> pricesAtDates = Arrays.asList(
                new PriceAtDate(new EZDate(2000,1,10), 1)
        );
        List<EZDate> dates = Arrays.asList(
                new EZDate(1999,1,10)
        );

        Prices prices = new Prices();
        new PricesTools<>(pricesAtDates.stream(), dates, PriceAtDate::getDate, p -> p, prices).fillPricesForAListOfDates();
        Assert.assertEquals(1, prices.getPrices().size());
        Assert.assertEquals(new EZDate(1999,1,10), prices.getPrices().get(0).getDate());
        Assert.assertEquals(0, prices.getPrices().get(0).getPrice(), 0);
    }

    @Test
    public void fillPricesForAListOfDatesVerySimpleTest3() {
        List<PriceAtDate> pricesAtDates = Arrays.asList(
                new PriceAtDate(new EZDate(2000,1,10), 1)
        );
        List<EZDate> dates = Arrays.asList(
                new EZDate(2000,1,10)
        );

        Prices prices = new Prices();
        new PricesTools<>(pricesAtDates.stream(), dates, PriceAtDate::getDate, p -> p, prices).fillPricesForAListOfDates();
        Assert.assertEquals(1, prices.getPrices().size());
        Assert.assertEquals(new EZDate(2000,1,10), prices.getPrices().get(0).getDate());
        Assert.assertEquals(1, prices.getPrices().get(0).getPrice(), 0);
    }


    @Test
    public void fillPricesForAListOfDatesSimpleTest1() {
        List<PriceAtDate> pricesAtDates = Arrays.asList(
                new PriceAtDate(new EZDate(2000,1,10), 1),
                new PriceAtDate(new EZDate(2000,1,11), 1),
                new PriceAtDate(new EZDate(2000,1,12), 1)
        );
        List<EZDate> dates = Arrays.asList(
                new EZDate(2002,1,10),
                new EZDate(2002,1,11)
        );

        Prices prices = new Prices();
        new PricesTools<>(pricesAtDates.stream(), dates, PriceAtDate::getDate, p -> p, prices).fillPricesForAListOfDates();
        Assert.assertEquals(2, prices.getPrices().size());
        Assert.assertEquals(new EZDate(2000,1,12), prices.getPrices().get(0).getDate());
        Assert.assertEquals(new EZDate(2000,1,12), prices.getPrices().get(1).getDate());
    }

    @Test
    public void fillPricesForAListOfDatesSimpleTest2() {
        List<PriceAtDate> pricesAtDates = Arrays.asList(
                new PriceAtDate(new EZDate(2001,1,10), 1),
                new PriceAtDate(new EZDate(2001,1,11), 1),
                new PriceAtDate(new EZDate(2001,1,12), 1)
        );
        List<EZDate> dates = Arrays.asList(
                new EZDate(2000,1,10),
                new EZDate(2000,1,11)
        );

        Prices prices = new Prices();
        new PricesTools<>(pricesAtDates.stream(), dates, PriceAtDate::getDate, p -> p, prices).fillPricesForAListOfDates();
        Assert.assertEquals(2, prices.getPrices().size());
        Assert.assertEquals(new EZDate(2000,1,10), prices.getPrices().get(0).getDate());
        Assert.assertEquals(0, prices.getPrices().get(0).getPrice(), 0);
        Assert.assertEquals(new EZDate(2000,1,11), prices.getPrices().get(1).getDate());
        Assert.assertEquals(0, prices.getPrices().get(1).getPrice(), 0);
    }

    @Test
    public void fillPricesForAListOfDatesSimpleTest3() {
        List<PriceAtDate> pricesAtDates = Arrays.asList(
                new PriceAtDate(new EZDate(2000,1,10), 1),
                new PriceAtDate(new EZDate(2001,1,11), 1),
                new PriceAtDate(new EZDate(2001,1,12), 1)
        );
        List<EZDate> dates = Arrays.asList(
                new EZDate(2000,1,11),
                new EZDate(2000,1,12)
        );

        Prices prices = new Prices();
        new PricesTools<>(pricesAtDates.stream(), dates, PriceAtDate::getDate, p -> p, prices).fillPricesForAListOfDates();
        Assert.assertEquals(2, prices.getPrices().size());
        Assert.assertEquals(new EZDate(2000,1,10), prices.getPrices().get(0).getDate());
        Assert.assertEquals(1, prices.getPrices().get(0).getPrice(), 0);
        Assert.assertEquals(new EZDate(2000,1,10), prices.getPrices().get(1).getDate());
        Assert.assertEquals(1, prices.getPrices().get(1).getPrice(), 0);
    }


    @Test
    public void fillPricesForAListOfDatesSimpleTest4() {
        List<PriceAtDate> pricesAtDates = Arrays.asList(
                new PriceAtDate(new EZDate(2000,1,10), 1),
                new PriceAtDate(new EZDate(2001,1,10), 1)
        );
        List<EZDate> dates = Arrays.asList(
                new EZDate(2000,1,12)
        );

        Prices prices = new Prices();
        new PricesTools<>(pricesAtDates.stream(), dates, PriceAtDate::getDate, p -> p, prices).fillPricesForAListOfDates();
        Assert.assertEquals(1, prices.getPrices().size());
        Assert.assertEquals(new EZDate(2000,1,10), prices.getPrices().get(0).getDate());
    }


    @Test
    public void fillPricesForAListOfDatesSimpleTest5() {
        List<PriceAtDate> pricesAtDates = Arrays.asList(
                new PriceAtDate(new EZDate(2000,1,10), 1)
        );
        List<EZDate> dates = Arrays.asList(
                new EZDate(2000,1,12),
                new EZDate(2002,1,12)
        );

        Prices prices = new Prices();
        new PricesTools<>(pricesAtDates.stream(), dates, PriceAtDate::getDate, p -> p, prices).fillPricesForAListOfDates();
        Assert.assertEquals(2, prices.getPrices().size());
        Assert.assertEquals(new EZDate(2000,1,10), prices.getPrices().get(0).getDate());
        Assert.assertEquals(new EZDate(2000,1,10), prices.getPrices().get(1).getDate());
    }

    @Test
    public void fillPricesForAListOfDatesSimpleTest6() {
        List<PriceAtDate> pricesAtDates = Arrays.asList(
                new PriceAtDate(new EZDate(2000,1,10), 1),
                new PriceAtDate(new EZDate(2001,1,10), 1)
        );
        List<EZDate> dates = Arrays.asList(
                new EZDate(2000,1,12),
                new EZDate(2002,1,12)
        );

        Prices prices = new Prices();
        new PricesTools<>(pricesAtDates.stream(), dates, PriceAtDate::getDate, p -> p, prices).fillPricesForAListOfDates();
        Assert.assertEquals(2, prices.getPrices().size());
        Assert.assertEquals(new EZDate(2000,1,10), prices.getPrices().get(0).getDate());
        Assert.assertEquals(new EZDate(2001,1,10), prices.getPrices().get(1).getDate());
    }


    @Test
    public void fillPricesForAListOfDatesTest1() {
        List<PriceAtDate> pricesAtDates = Arrays.asList(
                new PriceAtDate(new EZDate(2000,1,10), 1),
                new PriceAtDate(new EZDate(2001,1,10), 1),
                new PriceAtDate(new EZDate(2002,1,10), 1),
                new PriceAtDate(new EZDate(2003,1,10), 1),
                new PriceAtDate(new EZDate(2004,1,10), 1),
                new PriceAtDate(new EZDate(2005,1,10), 1),
                new PriceAtDate(new EZDate(2006,1,10), 1),
                new PriceAtDate(new EZDate(2007,1,10), 1),
                new PriceAtDate(new EZDate(2008,1,10), 1),
                new PriceAtDate(new EZDate(2009,1,10), 1)
        );
        List<EZDate> dates = Arrays.asList(
                new EZDate(2000,1,10),
                new EZDate(2002,1,10),
                new EZDate(2004,1,10),
                new EZDate(2006,1,10),
                new EZDate(2008,1,10),
                new EZDate(2009,1,10)
        );

        Prices prices = new Prices();
        new PricesTools<>(pricesAtDates.stream(), dates, PriceAtDate::getDate, p -> p, prices).fillPricesForAListOfDates();
        Assert.assertEquals(6, prices.getPrices().size());
        Assert.assertEquals(new EZDate(2000,1,10), prices.getPrices().get(0).getDate());
        Assert.assertEquals(new EZDate(2002,1,10), prices.getPrices().get(1).getDate());
        Assert.assertEquals(new EZDate(2004,1,10), prices.getPrices().get(2).getDate());
        Assert.assertEquals(new EZDate(2006,1,10), prices.getPrices().get(3).getDate());
        Assert.assertEquals(new EZDate(2008,1,10), prices.getPrices().get(4).getDate());
        Assert.assertEquals(new EZDate(2009,1,10), prices.getPrices().get(5).getDate());
    }


    @Test
    public void fillPricesForAListOfDatesTest2() {
        List<PriceAtDate> pricesAtDates = Arrays.asList(
                new PriceAtDate(new EZDate(2000,1,10), 1),
                new PriceAtDate(new EZDate(2001,1,10), 1),
                new PriceAtDate(new EZDate(2002,1,10), 1),
                new PriceAtDate(new EZDate(2003,1,10), 1),
                new PriceAtDate(new EZDate(2004,1,10), 1),
                new PriceAtDate(new EZDate(2005,1,10), 1),
                new PriceAtDate(new EZDate(2006,1,10), 1),
                new PriceAtDate(new EZDate(2007,1,10), 1),
                new PriceAtDate(new EZDate(2008,1,10), 1),
                new PriceAtDate(new EZDate(2009,1,10), 1)
        );
        List<EZDate> dates = Arrays.asList(
                new EZDate(2000,1,12),
                new EZDate(2002,1,12),
                new EZDate(2004,1,12),
                new EZDate(2006,1,12),
                new EZDate(2008,1,12),
                new EZDate(2009,1,12)
        );

        Prices prices = new Prices();
        new PricesTools<>(pricesAtDates.stream(), dates, PriceAtDate::getDate, p -> p, prices).fillPricesForAListOfDates();
        Assert.assertEquals(6, prices.getPrices().size());
        Assert.assertEquals(new EZDate(2000,1,10), prices.getPrices().get(0).getDate());
        Assert.assertEquals(new EZDate(2002,1,10), prices.getPrices().get(1).getDate());
        Assert.assertEquals(new EZDate(2004,1,10), prices.getPrices().get(2).getDate());
        Assert.assertEquals(new EZDate(2006,1,10), prices.getPrices().get(3).getDate());
        Assert.assertEquals(new EZDate(2008,1,10), prices.getPrices().get(4).getDate());
        Assert.assertEquals(new EZDate(2009,1,10), prices.getPrices().get(5).getDate());
    }


    @Test
    public void fillPricesForAListOfDatesTest3() {
        List<PriceAtDate> pricesAtDates = Arrays.asList(
                new PriceAtDate(new EZDate(2000,1,1), 1),
                new PriceAtDate(new EZDate(2000,1,5), 1),
                new PriceAtDate(new EZDate(2000,1,10), 1),
                new PriceAtDate(new EZDate(2000,1,15), 1),
                new PriceAtDate(new EZDate(2000,1,20), 1),
                new PriceAtDate(new EZDate(2000,1,25), 1),
                new PriceAtDate(new EZDate(2000,1,30), 1),
                new PriceAtDate(new EZDate(2000,2,5), 1),
                new PriceAtDate(new EZDate(2000,2,10), 1),
                new PriceAtDate(new EZDate(2000,2,15), 1)
        );
        List<EZDate> dates = Arrays.asList(
                new EZDate(2000,1,2),
                new EZDate(2000,1,7),
                new EZDate(2000,1,12),
                new EZDate(2000,1,17),
                new EZDate(2000,1,18),
                new EZDate(2000,1,23)
        );

        Prices prices = new Prices();
        new PricesTools<>(pricesAtDates.stream(), dates, PriceAtDate::getDate, p -> p, prices).fillPricesForAListOfDates();
        Assert.assertEquals(6, prices.getPrices().size());
        Assert.assertEquals(new EZDate(2000,1,1), prices.getPrices().get(0).getDate());
        Assert.assertEquals(new EZDate(2000,1,5), prices.getPrices().get(1).getDate());
        Assert.assertEquals(new EZDate(2000,1,10), prices.getPrices().get(2).getDate());
        Assert.assertEquals(new EZDate(2000,1,15), prices.getPrices().get(3).getDate());
        Assert.assertEquals(new EZDate(2000,1,15), prices.getPrices().get(4).getDate());
        Assert.assertEquals(new EZDate(2000,1,20), prices.getPrices().get(5).getDate());
    }
}
