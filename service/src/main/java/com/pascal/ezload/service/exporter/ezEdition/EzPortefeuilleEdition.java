package com.pascal.ezload.service.exporter.ezEdition;

public class EzPortefeuilleEdition {

    public static final String LIQUIDITE_ACTION = "LIQUIDITE";

    private String valeur;
    private float quantitée; // can be negative

    public EzPortefeuilleEdition(){}

    public EzPortefeuilleEdition(String valeur, float quantitée) {
        this.valeur = valeur;
        this.quantitée = quantitée;
    }

    public String getValeur() {
        return valeur;
    }

    public void setValeur(String valeur) {
        this.valeur = valeur;
    }

    public float getQuantitée() {
        return quantitée;
    }

    public void setQuantitée(float quantitée) {
        this.quantitée = quantitée;
    }
}
