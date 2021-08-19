package com.pascal.bientotrentier.model;

public abstract class BROperation {
    public enum COMPTE_TYPE {
        PEA("PEA"), PEA_PME("PEA-PME"), COMPTE_TITRES_ORDINAIRE("Compte-Titres Ordinaire");

        private String name;
        COMPTE_TYPE(String name){
            this.name = name;
        }

        public String getEZPortfolioName(){
            return name;
        }
    }

    private String date;
    private String amount;
    private String description;
    private COMPTE_TYPE compteType;
    private String courtier;

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public abstract BROperationType getOperationType();

    public COMPTE_TYPE getCompteType() {
        return compteType;
    }

    public void setCompteType(COMPTE_TYPE compteType) {
        this.compteType = compteType;
    }

    public String getCourtier() {
        return courtier;
    }

    public void setCourtier(String courtier) {
        this.courtier = courtier;
    }

    @Override
    public String toString() {
        return "BROperation{" +
                "date='" + date + '\'' +
                ", amount='" + amount + '\'' +
                ", description='" + description + '\'' +
                ", compteType=" + compteType +
                ", courtier='" + courtier + '\'' +
                '}';
    }
}
