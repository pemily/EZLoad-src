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
package com.pascal.ezload.service.dashboard;

import com.pascal.ezload.service.model.EZDate;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class ChartsToolsTest {

    @Test
    public void getDatesSample3Points(){
        List<EZDate> r = ChartsTools.getDatesSample(new EZDate(2000,1,1), new EZDate(2000,1,10), 3);
        Assert.assertEquals(3, r.size());
        Assert.assertEquals(new EZDate(2000,1,1), r.get(0));
        Assert.assertEquals(new EZDate(2000,1,5), r.get(1));
        Assert.assertEquals(new EZDate(2000,1,10), r.get(2));
    }

    @Test
    public void getDatesSample4Points(){
        List<EZDate> r = ChartsTools.getDatesSample(new EZDate(2000,1,1), new EZDate(2000,1,10), 4);
        Assert.assertEquals(4, r.size());
        Assert.assertEquals(new EZDate(2000,1,1), r.get(0));
        Assert.assertEquals(new EZDate(2000,1,4), r.get(1));
        Assert.assertEquals(new EZDate(2000,1,7), r.get(2));
        Assert.assertEquals(new EZDate(2000,1,10), r.get(3));
    }

    @Test
    public void getDatesSample5Points(){
        List<EZDate> r = ChartsTools.getDatesSample(new EZDate(2000,1,1), new EZDate(2000,1,10), 5);
        Assert.assertEquals(5, r.size());
        Assert.assertEquals(new EZDate(2000,1,1), r.get(0));
        Assert.assertEquals(new EZDate(2000,1,3), r.get(1));
        Assert.assertEquals(new EZDate(2000,1,5), r.get(2));
        Assert.assertEquals(new EZDate(2000,1,7), r.get(3));
        Assert.assertEquals(new EZDate(2000,1,10), r.get(4));
    }

    @Test
    public void getDatesSample6Points(){
        List<EZDate> r = ChartsTools.getDatesSample(new EZDate(2000,1,1), new EZDate(2000,1,10), 6);
        Assert.assertEquals(6, r.size());
        Assert.assertEquals(new EZDate(2000,1,1), r.get(0));
        Assert.assertEquals(new EZDate(2000,1,2), r.get(1));
        Assert.assertEquals(new EZDate(2000,1,4), r.get(2));
        Assert.assertEquals(new EZDate(2000,1,6), r.get(3));
        Assert.assertEquals(new EZDate(2000,1,8), r.get(4));
        Assert.assertEquals(new EZDate(2000,1,10), r.get(5));
    }

    @Test
    public void getDatesSample7Points(){
        List<EZDate> r = ChartsTools.getDatesSample(new EZDate(2000,1,1), new EZDate(2000,1,10), 7);
        Assert.assertEquals(7, r.size());
        Assert.assertEquals(new EZDate(2000,1,1), r.get(0));
        Assert.assertEquals(new EZDate(2000,1,2), r.get(1));
        Assert.assertEquals(new EZDate(2000,1,4), r.get(2));
        Assert.assertEquals(new EZDate(2000,1,5), r.get(3));
        Assert.assertEquals(new EZDate(2000,1,7), r.get(4));
        Assert.assertEquals(new EZDate(2000,1,8), r.get(5));
        Assert.assertEquals(new EZDate(2000,1,10), r.get(6));
    }


    @Test
    public void getDatesSample10Points(){
        List<EZDate> r = ChartsTools.getDatesSample(new EZDate(2000,1,1), new EZDate(2000,1,10), 10);
        Assert.assertEquals(10, r.size());
        Assert.assertEquals(new EZDate(2000,1,1), r.get(0));
        Assert.assertEquals(new EZDate(2000,1,2), r.get(1));
        Assert.assertEquals(new EZDate(2000,1,3), r.get(2));
        Assert.assertEquals(new EZDate(2000,1,4), r.get(3));
        Assert.assertEquals(new EZDate(2000,1,5), r.get(4));
        Assert.assertEquals(new EZDate(2000,1,6), r.get(5));
        Assert.assertEquals(new EZDate(2000,1,7), r.get(6));
        Assert.assertEquals(new EZDate(2000,1,8), r.get(7));
        Assert.assertEquals(new EZDate(2000,1,9), r.get(8));
        Assert.assertEquals(new EZDate(2000,1,10), r.get(9));
    }


    @Test
    public void getDatesSampleTooManyPoints(){
        List<EZDate> r = ChartsTools.getDatesSample(new EZDate(2000,1,1), new EZDate(2000,1,10), 11);
        Assert.assertEquals(9, r.size());
        Assert.assertEquals(new EZDate(2000,1,1), r.get(0));
        Assert.assertEquals(new EZDate(2000,1,2), r.get(1));
        Assert.assertEquals(new EZDate(2000,1,3), r.get(2));
        Assert.assertEquals(new EZDate(2000,1,4), r.get(3));
        Assert.assertEquals(new EZDate(2000,1,5), r.get(4));
        Assert.assertEquals(new EZDate(2000,1,6), r.get(5));
        Assert.assertEquals(new EZDate(2000,1,7), r.get(6));
        Assert.assertEquals(new EZDate(2000,1,8), r.get(7));
        Assert.assertEquals(new EZDate(2000,1,10), r.get(8));
    }
}
