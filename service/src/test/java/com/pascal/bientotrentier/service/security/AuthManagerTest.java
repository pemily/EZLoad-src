package com.pascal.bientotrentier.service.security;

import com.pascal.bientotrentier.service.config.MainSettings;
import com.pascal.bientotrentier.service.model.EnumBRCourtier;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AuthManagerTest {
    @Test
    public void test() throws Exception {
        String passphrase = AuthManager.getNewRandonmEncryptionPhrase();
        AuthManager authManager = new AuthManager(passphrase, "target/.passwords");
        MainSettings.AuthInfo login = new MainSettings.AuthInfo();
        login.setUsername("pascal");
        login.setPassword("motdepasse");
        authManager.addAuthInfo(EnumBRCourtier.BourseDirect, login);

        login = authManager.getAuthInfo(EnumBRCourtier.BourseDirect);
        assertEquals("pascal", login.getUsername());
        assertEquals("motdepasse", login.getPassword());
    }
}
