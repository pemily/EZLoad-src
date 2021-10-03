package com.pascal.ezload.service.sources.bourseDirect;

import java.util.LinkedList;
import java.util.List;

public class BourseDirectSettings  {

    private List<BourseDirectEZAccountDeclaration> accounts = new LinkedList<>();

    public List<BourseDirectEZAccountDeclaration> getAccounts() {
        return accounts;
    }

    public void setAccounts(List<BourseDirectEZAccountDeclaration> accounts) {
        this.accounts = accounts;
    }

    public void validate() {
        accounts.stream().forEach(acc -> acc.validate());
    }

    public void clearErrors(){
        accounts.stream().forEach(acc -> acc.clearErrors());
    }

}
