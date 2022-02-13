package com.pascal.ezload.service.security;

import com.pascal.ezload.service.config.AuthInfo;
import com.pascal.ezload.service.model.EnumEZBroker;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AuthManagerTest {
    @Test
    public void test() throws Exception {
        AuthManager authManager = new AuthManager(AuthManager.getNewRandonmEncryptionPhrase(), "target/.passwords");
        AuthInfo login = new AuthInfo();
        login.setUsername("pascal");
        login.setPassword("motdepasse");
        authManager.saveAuthInfo(EnumEZBroker.BourseDirect, login);

        login = authManager.getAuthInfo(EnumEZBroker.BourseDirect);
        assertEquals("pascal", login.getUsername());
        assertEquals("motdepasse", login.getPassword());

        login = authManager.getAuthWithoutPassword(EnumEZBroker.BourseDirect);
        assertEquals("pascal", login.getUsername());
        assertEquals("", login.getPassword());
    }
}
