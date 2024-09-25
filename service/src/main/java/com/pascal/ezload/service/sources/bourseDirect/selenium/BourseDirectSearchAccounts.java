/**
 * ezService - EZLoad an automatic loader for EZPortfolio
 * Copyright © 2021 EMILY Pascal (pascal.emily@gmail.com)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.pascal.ezload.service.sources.bourseDirect.selenium;

import com.pascal.ezload.service.config.EzProfil;
import com.pascal.ezload.service.config.MainSettings;
import com.pascal.ezload.service.config.SettingsManager;
import com.pascal.ezload.common.sources.Reporting;
import com.pascal.ezload.service.sources.bourseDirect.BourseDirectEZAccountDeclaration;
import com.pascal.ezload.common.util.StringUtils;
import org.openqa.selenium.WebElement;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class BourseDirectSearchAccounts extends BourseDirectSeleniumHelper {

    public BourseDirectSearchAccounts(SettingsManager settingsManager, MainSettings mainSettings, EzProfil ezProfil, Reporting reporting) {
        super(reporting, settingsManager, mainSettings, ezProfil);
    }

    // the devise and owner address will not be filled
    public List<BourseDirectEZAccountDeclaration> extract(String currentChromeVersion) throws Exception {
        try {
            login(currentChromeVersion);
            goToAvisOperes();
            WebElement label = findByContainsText("label", "Sélectionnez votre compte :");
            WebElement parent = getParent(label);
            List<WebElement> allChildren = getChildren(parent, "option");
            return allChildren.stream()
                    .map(option -> {
                        String text = option.getText();
                        String tmp[] = StringUtils.divide(text, '(');
                        if (tmp.length == 2) {
                            BourseDirectEZAccountDeclaration account = new BourseDirectEZAccountDeclaration();
                            String accountType = tmp[1].trim();
                            String tmp2[] = StringUtils.divide(tmp[0], " ");
                            if (tmp2.length == 2) {
                                String type[] = StringUtils.divide(accountType, ')');
                                if (type.length > 0) {
                                    account.setNumber(tmp2[0].trim());
                                    account.setName(tmp2[1].trim() + " " + type[0]);
                                    account.setActive(true);
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
