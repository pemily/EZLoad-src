package com.pascal.ezload.service.model;

public enum EnumEZAccountType {
        PEA("PEA"), PEA_PME("PEA-PME"), COMPTE_TITRES_ORDINAIRE("Compte-Titres Ordinaire");

        private String ezPortfolioName;

        EnumEZAccountType(String ezPortfolioName){
            this.ezPortfolioName = ezPortfolioName;
        }

        public String getEZPortfolioName(){
            return ezPortfolioName;
        }
    }