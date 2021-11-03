package com.pascal.ezload.service.model;

public enum EZOperationType {

    ACHAT_TITRES("Achat titres"),
    DROITS_DE_GARDE("Droits de garde"),
    RETRAIT_FONDS("Retrait fonds"),
    TAXE("Taxe"),
    VENTE_TITRES("Vente titres"),
    VERSEMENT_FONDS("Versement fonds"),
    ACHAT_TITRES_ETRANGER("Achat titres etranger"),
    COUPONS("Coupons"),
    DIVIDENDE_OPTIONEL("Dividende optionel"),
    ESPECES_SUR_OST("Espece sur OST"),
    VENTE_TITRES_ETRANGER("Vente titres etranger");


    private String ezOperationType;

    EZOperationType(String ezOperationType) {
        this.ezOperationType = ezOperationType;
    }

    public String getValue(){
        return ezOperationType;
    }
}
