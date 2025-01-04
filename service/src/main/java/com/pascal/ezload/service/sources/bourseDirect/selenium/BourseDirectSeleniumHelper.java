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

import com.pascal.ezload.common.util.Month;
import com.pascal.ezload.service.config.AuthInfo;
import com.pascal.ezload.service.config.EzProfil;
import com.pascal.ezload.service.config.MainSettings;
import com.pascal.ezload.service.config.SettingsManager;
import com.pascal.ezload.service.model.EnumEZBroker;
import com.pascal.ezload.common.sources.Reporting;
import com.pascal.ezload.service.sources.bourseDirect.BourseDirectSettings;
import com.pascal.ezload.common.util.BaseSelenium;
import com.pascal.ezload.common.util.Sleep;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;

public class BourseDirectSeleniumHelper extends BaseSelenium {

    protected final SettingsManager settingsManager;
    protected final MainSettings mainSettings;
    protected final BourseDirectSettings bourseDirectSettings;

    public BourseDirectSeleniumHelper(Reporting reporting, SettingsManager settingsManager, MainSettings mainSettings, EzProfil ezProfil) {
        super(reporting);
        this.mainSettings = mainSettings;
        this.settingsManager = settingsManager;
        this.bourseDirectSettings = ezProfil.getBourseDirect();
    }

    public void login(String currentChromeVersion) throws Exception {
        super.init(currentChromeVersion, mainSettings.getChrome(), mainSettings.getChrome().getDefaultTimeout());
        try(Reporting rep = reporting.pushSection("Login")) {
            goTo("https://www.boursedirect.fr/fr/login");

            try {
                // reject cookies
                findById("didomi-notice-disagree-button").click();
                Sleep.waitSeconds(1);
                reporting.info("Cookies Rejected");
            } catch (NoSuchElementException ignored) {
            }

            WebElement login = findById("bd_auth_login_type_login");
            WebElement password = findById("bd_auth_login_type_password");

            AuthInfo authInfo = settingsManager.getAuthManager(mainSettings).getAuthInfo(EnumEZBroker.BourseDirect);
            if (authInfo != null || StringUtils.isBlank(login.getText())) {
                if (authInfo != null && authInfo.getUsername() != null) login.sendKeys(authInfo.getUsername());
                if (authInfo != null && authInfo.getPassword() !=null){
                    password.sendKeys(authInfo.getPassword());
                    Sleep.waitSeconds(1);
                    findByCss("button[data-testid=\"button-submit\"]").click();
                }
                else{
                    // Pas de password
                    reporting.info("Entrez votre Identifiant & Mot de passe puis cliquez sur \"SE CONNECTER\"");
                }
            }
            else reporting.info("Entrez votre Identifiant & Mot de passe puis cliquez sur \"SE CONNECTER\"");

            boolean connected = false;
            do {
                try {
                    waitUrlIsNot("https://www.boursedirect.fr/fr/login");
                    connected = true;
                } catch (TimeoutException t) {
                    Sleep.waitSeconds(1);
                }
            } while (!connected);

            waitPageLoaded();
        }
    }


    protected void goToAvisOperes() throws Exception {
        goTo("https://www.boursedirect.fr/fr/page/avis-operes");
    }

    protected void goToAvisOperes(String cptIndex, Month month) throws Exception {
        goTo("https://www.boursedirect.fr/priv/new/avis-operes.php?tr=RO&nc="+cptIndex+"&month="+ month.getMonth()+"&year="+ month.getYear());
    }
}
