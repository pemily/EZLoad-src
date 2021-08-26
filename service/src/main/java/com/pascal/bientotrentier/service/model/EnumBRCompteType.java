package com.pascal.bientotrentier.service.model;

public enum EnumBRCompteType {
        PEA("PEA"), PEA_PME("PEA-PME"), COMPTE_TITRES_ORDINAIRE("Compte-Titres Ordinaire");

        private String ezPortfolioName;

        EnumBRCompteType(String ezPortfolioName){
            this.ezPortfolioName = ezPortfolioName;
        }

        public String getEZPortfolioName(){
            return ezPortfolioName;
        }
    }