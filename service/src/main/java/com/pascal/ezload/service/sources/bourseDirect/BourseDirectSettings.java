package com.pascal.ezload.service.sources.bourseDirect;

import java.util.LinkedList;
import java.util.List;

public class BourseDirectSettings {

    private List<BourseDirectBRAccountDeclaration> accounts = new LinkedList<>();

    public List<BourseDirectBRAccountDeclaration> getAccounts() {
        return accounts;
    }

    public void setAccounts(List<BourseDirectBRAccountDeclaration> accounts) {
        this.accounts = accounts;
    }


}
