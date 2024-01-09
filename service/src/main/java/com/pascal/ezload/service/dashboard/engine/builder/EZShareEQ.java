package com.pascal.ezload.service.dashboard.engine.builder;

import com.pascal.ezload.service.model.EZShare;

import java.util.Objects;

public class EZShareEQ extends EZShare {

    public EZShareEQ(EZShare ez){
        if (ez.getEzName() == null) throw new IllegalStateException("Le nom d'une action ne peut pas etre null");
        if (ez.getEzName().isBlank()) throw new IllegalStateException("Le nom d'une action ne peut pas etre null");
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
        return Objects.equals(getEzName(), ezShareEQ.getEzName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getEzName());
    }
}
