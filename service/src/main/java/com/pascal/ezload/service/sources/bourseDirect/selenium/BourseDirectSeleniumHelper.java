package com.pascal.ezload.service.sources.bourseDirect.selenium;

import com.pascal.ezload.service.config.AuthInfo;
import com.pascal.ezload.service.config.MainSettings;
import com.pascal.ezload.service.config.SettingsManager;
import com.pascal.ezload.service.model.EnumBRCourtier;
import com.pascal.ezload.service.sources.Reporting;
import com.pascal.ezload.service.sources.bourseDirect.BourseDirectSettings;
import com.pascal.ezload.service.util.BaseSelenium;
import com.pascal.ezload.service.util.Sleep;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;

public class BourseDirectSeleniumHelper extends BaseSelenium {

    protected final MainSettings mainSettings;
    protected final BourseDirectSettings bourseDirectSettings;

    public BourseDirectSeleniumHelper(Reporting reporting, MainSettings mainSettings) {
        super(reporting);
        this.mainSettings = mainSettings;
        this.bourseDirectSettings = mainSettings.getBourseDirect();
    }

    public void login() throws Exception {
        super.init(mainSettings.getChrome(), mainSettings.getChrome().getDefaultTimeout());
        get("https://www.boursedirect.fr/fr/login");

        try {
            // reject cookies
            findById("didomi-notice-disagree-button").click();
            Sleep.wait(1);
            reporting.info("Cookies Rejected");
        }
        catch (NoSuchElementException ignored){}

        WebElement login = findById("bd_auth_login_type_login");
        WebElement password = findById("bd_auth_login_type_password");

        AuthInfo authInfo = SettingsManager.getAuthManager().getAuthInfo(EnumBRCourtier.BourseDirect);
        if (StringUtils.isBlank(login.getText())){
            login.sendKeys(authInfo.getUsername());
            password.sendKeys(authInfo.getPassword());
            Sleep.wait(1);
            findById("bd_auth_login_type_submit").click();
        }
        else if (!StringUtils.isBlank(login.getText())){
            findById("bd_auth_login_type_submit").click();
        }
        else reporting.info("Please Enter your login/password then click on Connect");

        boolean connected = false;
        do {
            try {
                waitUrlIsNot("https://www.boursedirect.fr/fr/login");
                connected =  true;
            } catch (TimeoutException t) {
                Sleep.wait(1);
            }
        }while(!connected);
    }


    protected void goToAvisOperes() {
        get("https://www.boursedirect.fr/priv/avis-operes.php");
    }

}
