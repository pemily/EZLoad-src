package com.pascal.ezload.service.dashboard.config;

public class CurrencyIndexConfig {
    // all the devises found in the charts will be automatically added

    // No additional properties
    private boolean active; // just un boolean qui ne sert a rien, mais c'est pour avoir un attribut dans l'objet json

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
