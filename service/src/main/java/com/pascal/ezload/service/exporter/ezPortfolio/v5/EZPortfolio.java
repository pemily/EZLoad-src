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
package com.pascal.ezload.service.exporter.ezPortfolio.v5;

public class EZPortfolio {
    private final String ezPortfolioVersion;
    private MesOperations mesOperations;
    private MonPortefeuille monPortefeuille;
    private PRU pru;

    public EZPortfolio(String ezPortfolioVersion){
        this.ezPortfolioVersion = ezPortfolioVersion;
    }

    public MesOperations getMesOperations() {
        return mesOperations;
    }

    public void setMesOperations(MesOperations mesOperations) {
        this.mesOperations = mesOperations;
    }

    public MonPortefeuille getMonPortefeuille() {
        return monPortefeuille;
    }

    public void setMonPortefeuille(MonPortefeuille monPortefeuille) {
        this.monPortefeuille = monPortefeuille;
    }

    public String getEzPortfolioVersion() {
        return ezPortfolioVersion;
    }

    public PRU getPru() {
        return pru;
    }

    public void setPru(PRU pru) {
        this.pru = pru;
    }

    public EZPortfolio createDeepCopy(){
        EZPortfolio copy = new EZPortfolio(ezPortfolioVersion);
        copy.setPru(pru.createDeepCopy());
        copy.setMonPortefeuille(monPortefeuille.createDeepCopy());
        copy.setMesOperations(mesOperations.createDeepCopy());
        return copy;
    }
}
