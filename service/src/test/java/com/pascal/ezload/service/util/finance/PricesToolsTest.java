/**
 * ezService - EZLoad an automatic loader for EZPortfolio
 * Copyright © 2021 EMILY Pascal (pascal.emily@gmail.com)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.pascal.ezload.service.util.finance;

import com.pascal.ezload.service.model.EZDate;
import com.pascal.ezload.service.model.PriceAtDate;
import com.pascal.ezload.service.model.Prices;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

public class PricesToolsTest {


    @Test
    public void fillPricesForAListOfDatesVerySimpleTest1() {
        List<PriceAtDate> pricesAtDates = List.of(
                new PriceAtDate(new EZDate(2000, 1, 10), 1, false)
        );
        List<EZDate> dates = List.of(
                new EZDate(2002, 1, 10)
        );

        Prices prices = new Prices();
        new PricesTools<>(pricesAtDates.stream(), dates, PriceAtDate::getDate, p -> p, prices).fillPricesForAListOfDates();
        
        Assert.assertEquals(1, prices.getPrices().size());
        Assert.assertEquals(new EZDate(2000,1,10), prices.getPrices().get(0).getDate());
    }

    @Test
    public void fillPricesForAListOfDatesVerySimpleTest2() {
        List<PriceAtDate> pricesAtDates = List.of(
                new PriceAtDate(new EZDate(2000, 1, 10), 1, false)
        );
        List<EZDate> dates = List.of(
                new EZDate(1999, 1, 10)
        );

        Prices prices = new Prices();
        new PricesTools<>(pricesAtDates.stream(), dates, PriceAtDate::getDate, p -> p, prices).fillPricesForAListOfDates();
        Assert.assertEquals(1, prices.getPrices().size());
        Assert.assertEquals(new EZDate(1999,1,10), prices.getPrices().get(0).getDate());
        Assert.assertEquals(0, prices.getPrices().get(0).getValue(), 0);
    }

    @Test
    public void fillPricesForAListOfDatesVerySimpleTest3() {
        List<PriceAtDate> pricesAtDates = List.of(
                new PriceAtDate(new EZDate(2000, 1, 10), 1, false)
        );
        List<EZDate> dates = List.of(
                new EZDate(2000, 1, 10)
        );

        Prices prices = new Prices();
        new PricesTools<>(pricesAtDates.stream(), dates, PriceAtDate::getDate, p -> p, prices).fillPricesForAListOfDates();
        Assert.assertEquals(1, prices.getPrices().size());
        Assert.assertEquals(new EZDate(2000,1,10), prices.getPrices().get(0).getDate());
        Assert.assertEquals(1, prices.getPrices().get(0).getValue(), 0);
    }


    @Test
    public void fillPricesForAListOfDatesSimpleTest1() {
        List<PriceAtDate> pricesAtDates = Arrays.asList(
                new PriceAtDate(new EZDate(2000,1,10), 1, false),
                new PriceAtDate(new EZDate(2000,1,11), 1, false),
                new PriceAtDate(new EZDate(2000,1,12), 1, false)
        );
        List<EZDate> dates = Arrays.asList(
                new EZDate(2002,1,10),
                new EZDate(2002,1,11)
        );

        Prices prices = new Prices();
        new PricesTools<>(pricesAtDates.stream(), dates, PriceAtDate::getDate, p -> p, prices).fillPricesForAListOfDates();
        Assert.assertEquals(2, prices.getPrices().size());
        Assert.assertEquals(new EZDate(2000,1,12), prices.getPrices().get(0).getDate());
        Assert.assertEquals(new EZDate(2000,1,12), prices.getPrices().get(1).getDate());////// TODO PASCAL TO CHECK
    }

    @Test
    public void fillPricesForAListOfDatesSimpleTest2() {
        List<PriceAtDate> pricesAtDates = Arrays.asList(
                new PriceAtDate(new EZDate(2001,1,10), 1, false),
                new PriceAtDate(new EZDate(2001,1,11), 1, false),
                new PriceAtDate(new EZDate(2001,1,12), 1, false)
        );
        List<EZDate> dates = Arrays.asList(
                new EZDate(2000,1,10),
                new EZDate(2000,1,11)
        );

        Prices prices = new Prices();
        new PricesTools<>(pricesAtDates.stream(), dates, PriceAtDate::getDate, p -> p, prices).fillPricesForAListOfDates();
        Assert.assertEquals(2, prices.getPrices().size());
        Assert.assertEquals(new EZDate(2000,1,10), prices.getPrices().get(0).getDate());
        Assert.assertEquals(0, prices.getPrices().get(0).getValue(), 0);
        Assert.assertEquals(new EZDate(2000,1,11), prices.getPrices().get(1).getDate());
        Assert.assertEquals(0, prices.getPrices().get(1).getValue(), 0);
    }

    @Test
    public void fillPricesForAListOfDatesSimpleTest3() {
        List<PriceAtDate> pricesAtDates = Arrays.asList(
                new PriceAtDate(new EZDate(2000,1,10), 1, false),
                new PriceAtDate(new EZDate(2001,1,11), 1, false),
                new PriceAtDate(new EZDate(2001,1,12), 1, false)
        );
        List<EZDate> dates = Arrays.asList(
                new EZDate(2000,1,11),
                new EZDate(2000,1,12)
        );

        Prices prices = new Prices();
        new PricesTools<>(pricesAtDates.stream(), dates, PriceAtDate::getDate, p -> p, prices).fillPricesForAListOfDates();
        Assert.assertEquals(2, prices.getPrices().size());
        Assert.assertEquals(new EZDate(2000,1,10), prices.getPrices().get(0).getDate());
        Assert.assertEquals(1, prices.getPrices().get(0).getValue(), 0);
        Assert.assertEquals(new EZDate(2000,1,10), prices.getPrices().get(1).getDate()); //// TODO PASCAL TO CHECK
        Assert.assertEquals(1, prices.getPrices().get(1).getValue(), 0);
    }


    @Test
    public void fillPricesForAListOfDatesSimpleTest4() {
        List<PriceAtDate> pricesAtDates = Arrays.asList(
                new PriceAtDate(new EZDate(2000,1,10), 1, false),
                new PriceAtDate(new EZDate(2001,1,10), 1, false)
        );
        List<EZDate> dates = List.of(
                new EZDate(2000, 1, 12)
        );

        Prices prices = new Prices();
        new PricesTools<>(pricesAtDates.stream(), dates, PriceAtDate::getDate, p -> p, prices).fillPricesForAListOfDates();
        Assert.assertEquals(1, prices.getPrices().size());
        Assert.assertEquals(new EZDate(2000,1,10), prices.getPrices().get(0).getDate());
    }


    @Test
    public void fillPricesForAListOfDatesSimpleTest5() {
        List<PriceAtDate> pricesAtDates = List.of(
                new PriceAtDate(new EZDate(2000, 1, 10), 1, false)
        );
        List<EZDate> dates = Arrays.asList(
                new EZDate(2000,1,12),
                new EZDate(2002,1,12)
        );

        Prices prices = new Prices();
        new PricesTools<>(pricesAtDates.stream(), dates, PriceAtDate::getDate, p -> p, prices).fillPricesForAListOfDates();
        Assert.assertEquals(2, prices.getPrices().size());
        Assert.assertEquals(new EZDate(2000,1,10), prices.getPrices().get(0).getDate());
        Assert.assertEquals(new EZDate(2000,1,10), prices.getPrices().get(1).getDate()); /// TODO PASCAL TO CHECK
    }

    @Test
    public void fillPricesForAListOfDatesSimpleTest6() {
        List<PriceAtDate> pricesAtDates = Arrays.asList(
                new PriceAtDate(new EZDate(2000,1,10), 1, false),
                new PriceAtDate(new EZDate(2001,1,10), 1, false)
        );
        List<EZDate> dates = Arrays.asList(
                new EZDate(2000,1,12),
                new EZDate(2002,1,12)
        );

        Prices prices = new Prices();
        new PricesTools<>(pricesAtDates.stream(), dates, PriceAtDate::getDate, p -> p, prices).fillPricesForAListOfDates();
        Assert.assertEquals(2, prices.getPrices().size());
        Assert.assertEquals(new EZDate(2000,1,10), prices.getPrices().get(0).getDate());
        Assert.assertEquals(new EZDate(2001,1,10), prices.getPrices().get(1).getDate()); // TODO PASCAL TO CHECK
    }


    @Test
    public void fillPricesForAListOfDatesTest1() {
        List<PriceAtDate> pricesAtDates = Arrays.asList(
                new PriceAtDate(new EZDate(2000,1,10), 1, false),
                new PriceAtDate(new EZDate(2001,1,10), 1, false),
                new PriceAtDate(new EZDate(2002,1,10), 1, false),
                new PriceAtDate(new EZDate(2003,1,10), 1, false),
                new PriceAtDate(new EZDate(2004,1,10), 1, false),
                new PriceAtDate(new EZDate(2005,1,10), 1, false),
                new PriceAtDate(new EZDate(2006,1,10), 1, false),
                new PriceAtDate(new EZDate(2007,1,10), 1, false),
                new PriceAtDate(new EZDate(2008,1,10), 1, false),
                new PriceAtDate(new EZDate(2009,1,10), 1, false)
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
                new PriceAtDate(new EZDate(2000,1,10), 1, false),
                new PriceAtDate(new EZDate(2001,1,10), 1, false),
                new PriceAtDate(new EZDate(2002,1,10), 1, false),
                new PriceAtDate(new EZDate(2003,1,10), 1, false),
                new PriceAtDate(new EZDate(2004,1,10), 1, false),
                new PriceAtDate(new EZDate(2005,1,10), 1, false),
                new PriceAtDate(new EZDate(2006,1,10), 1, false),
                new PriceAtDate(new EZDate(2007,1,10), 1, false),
                new PriceAtDate(new EZDate(2008,1,10), 1, false),
                new PriceAtDate(new EZDate(2009,1,10), 1, false)
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
                new PriceAtDate(new EZDate(2000,1,1), 1, false),
                new PriceAtDate(new EZDate(2000,1,5), 1, false),
                new PriceAtDate(new EZDate(2000,1,10), 1, false),
                new PriceAtDate(new EZDate(2000,1,15), 1, false),
                new PriceAtDate(new EZDate(2000,1,20), 1, false),
                new PriceAtDate(new EZDate(2000,1,25), 1, false),
                new PriceAtDate(new EZDate(2000,1,30), 1, false),
                new PriceAtDate(new EZDate(2000,2,5), 1, false),
                new PriceAtDate(new EZDate(2000,2,10), 1, false),
                new PriceAtDate(new EZDate(2000,2,15), 1, false)
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
        Assert.assertEquals(new EZDate(2000,1,15), prices.getPrices().get(4).getDate()); /////////////// TODO PASCAL TO CHECK
        Assert.assertEquals(new EZDate(2000,1,20), prices.getPrices().get(5).getDate());
    }
}
