package com.pascal.ezload.service.sources.bourseDirect.selenium;

import com.pascal.ezload.service.config.MainSettings;
import com.pascal.ezload.service.sources.Reporting;
import com.pascal.ezload.service.sources.bourseDirect.BourseDirectBRAccountDeclaration;
import com.pascal.ezload.service.util.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class BourseDirectSearchAccounts extends BourseDirectSeleniumHelper {

    public BourseDirectSearchAccounts(MainSettings mainSettings, Reporting reporting) {
        super(reporting, mainSettings);
    }

    // the devise and owner address will not be filled
    public List<BourseDirectBRAccountDeclaration> extract(String currentChromeVersion, Consumer<String> newDriverPathSaver) throws Exception {
        try {
            login(currentChromeVersion, newDriverPathSaver);
            goToAvisOperes();
            WebElement label = findByContainsText("label", "Sélectionnez votre compte :");
            WebElement parent = getParent(label);
            List<WebElement> allChildren = getChildren(parent, "option");
            return allChildren.stream()
                    .map(option -> {
                        String text = option.getText();
                        String tmp[] = StringUtils.divide(text, '(');
                        if (tmp.length == 2) {
                            BourseDirectBRAccountDeclaration account = new BourseDirectBRAccountDeclaration();
                            String accountType = tmp[1].trim();
                            String tmp2[] = StringUtils.divide(tmp[0], " ");
                            if (tmp2.length == 2) {
                                String type[] = StringUtils.divide(accountType, ')');
                                if (type.length > 0) {
                                    account.setNumber(tmp2[0].trim());
                                    account.setName(tmp2[1].trim() + " " + type[0]);
                                    return account;
                                }
                            }
                        }
                        return null;
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        }
        finally{
            closeChrome();
        }
    }

}
