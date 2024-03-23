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
package com.pascal.ezload.service.dashboard.engine.builder;

import com.pascal.ezload.service.model.EZShare;

import java.util.Objects;

public class EZShareEQ extends EZShare {

    public EZShareEQ(EZShare ez){
        if (ez.getEzName() == null) throw new IllegalStateException("Le nom d'une action ne peut pas être null");
        if (ez.getEzName().isBlank()) throw new IllegalStateException("Le nom d'une action ne peut pas être vide");
        this.setEzName(ez.getEzName());
        this.setCountryCode(ez.getCountryCode());
        this.setDescription(ez.getDescription());
        this.setGoogleCode(ez.getGoogleCode());
        this.setIsin(ez.getIsin());
        this.setSeekingAlphaCode(ez.getSeekingAlphaCode());
        this.setType(ez.getType());
        this.setIndustry(ez.getIndustry());
        this.setSector(ez.getSector());
        this.setYahooCode(ez.getYahooCode());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EZShareEQ ezShareEQ = (EZShareEQ) o;
        return Objects.equals(getEzName(), ezShareEQ.getEzName())
                || Objects.equals(getAlternativeName(), ezShareEQ.getEzName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getEzName());
    }
}
