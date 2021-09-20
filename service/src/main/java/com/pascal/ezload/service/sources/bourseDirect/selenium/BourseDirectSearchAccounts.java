package com.pascal.ezload.service.sources.bourseDirect.selenium;

import com.pascal.ezload.service.config.MainSettings;
import com.pascal.ezload.service.model.BRAccount;
import com.pascal.ezload.service.sources.Reporting;
import com.pascal.ezload.service.sources.bourseDirect.BourseDirectBRAccountDeclaration;
import com.pascal.ezload.service.util.StringUtils;
import org.openqa.selenium.WebElement;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class BourseDirectSearchAccounts extends BourseDirectSeleniumHelper {

    public BourseDirectSearchAccounts(MainSettings mainSettings, Reporting reporting) {
        super(reporting, mainSettings);
    }

    // the devise and owner address will not be filled
    public List<BourseDirectBRAccountDeclaration> extract() throws Exception {
        login();
        goToAvisOperes();
        WebElement label = findByContainsText("label", "Sélectionnez votrer compte :");
        WebElement selectAccount = nextSibling(label, 1);
        List<WebElement> allChildren = getChildren(selectAccount, "option");
        return allChildren.stream()
                .map(option -> {
                    String text = option.getText();
                    String tmp[] = StringUtils.divide(text, '(', ')');
                    if (tmp.length == 2) {
                        BourseDirectBRAccountDeclaration account = new BourseDirectBRAccountDeclaration();
                        String accountType = tmp[1].trim();
                        String tmp2[] = StringUtils.divide(tmp[0], " ");
                        if (tmp2.length == 2) {
                            account.setNumber(tmp2[0].trim());
                            account.setName(tmp2[1].trim()+" "+accountType);
                            return account;
                        }
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

}
