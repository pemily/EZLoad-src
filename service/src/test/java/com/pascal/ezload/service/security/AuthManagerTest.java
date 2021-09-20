package com.pascal.ezload.service.security;

import com.pascal.ezload.service.config.AuthInfo;
import com.pascal.ezload.service.model.EnumBRCourtier;
import com.pascal.ezload.service.util.FileValue;
import com.pascal.ezload.service.util.StringValue;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AuthManagerTest {
    @Test
    public void test() throws Exception {
        AuthManager authManager = new AuthManager(AuthManager.getNewRandonmEncryptionPhrase(), "target/.passwords");
        AuthInfo login = new AuthInfo();
        login.setUsername("pascal");
        login.setPassword("motdepasse");
        authManager.saveAuthInfo(EnumBRCourtier.BourseDirect, login);

        login = authManager.getAuthInfo(EnumBRCourtier.BourseDirect);
        assertEquals("pascal", login.getUsername());
        assertEquals("motdepasse", login.getPassword());

        login = authManager.getAuthWithDummyPassword(EnumBRCourtier.BourseDirect);
        assertEquals("pascal", login.getUsername());
        assertEquals("@@@@@@@@@@", login.getPassword());
    }
}
