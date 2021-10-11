package com.pascal.ezload.service.exporter.ezEdition;

public class EzPortefeuilleEdition {

    public static final String LIQUIDITE_ACTION = "LIQUIDITE";

    private String valeur;
    private float quantite; // can be negative

    public EzPortefeuilleEdition(){}

    public EzPortefeuilleEdition(String valeur, float quantite) {
        this.valeur = valeur;
        this.quantite = quantite;
    }

    public String getValeur() {
        return valeur;
    }

    public void setValeur(String valeur) {
        this.valeur = valeur;
    }

    public float getQuantite() {
        return quantite;
    }

    public void setQuantite(float quantite) {
        this.quantite = quantite;
    }
}
