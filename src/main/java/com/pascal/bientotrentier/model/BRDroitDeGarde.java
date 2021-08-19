package com.pascal.bientotrentier.model;

public class BRDroitDeGarde extends BROperation {
    @Override
    public BROperationType getOperationType() {
        return BROperationType.DROITS_DE_GARDE_OU_FRAIS_DIVERS;
    }

}
