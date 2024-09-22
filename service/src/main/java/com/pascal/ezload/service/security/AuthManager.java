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
package com.pascal.ezload.service.security;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import com.google.gson.Gson;
import com.pascal.ezload.service.config.AuthInfo;
import com.pascal.ezload.service.model.EnumEZBroker;
import com.pascal.ezload.common.util.StringUtils;

public class AuthManager {

    private final String authFilePath;
    private final String passPhrase;

    public AuthManager(String passPhrase, String authFilePath){
        this.passPhrase = passPhrase;
        this.authFilePath = authFilePath;
    }

    public AuthInfo getAuthInfo(EnumEZBroker courtier) throws Exception {
        Data data = loadFile(authFilePath);
        AuthInfo info = data.getInfo().get(courtier);
        if (info == null) return null;
        AuthInfo result = new AuthInfo();
        try {
            result.setPassword(decryptPassword(info.getPassword(), passPhrase));
            result.setUsername(info.getUsername());
            return result;
        }
        catch(Exception e){
            throw new Exception("Essayez de supprimer le fichier "+authFilePath+" redonnez votre mot de passe et recommencez", e);
        }
    }

    public void saveAuthInfo(EnumEZBroker courtier, AuthInfo authInfo) throws Exception {
        Data data = loadFile(authFilePath);
        AuthInfo encrypted = new AuthInfo();
        encrypted.setPassword(authInfo.getPassword() != null ? encryptPassword(authInfo.getPassword(), passPhrase) : null);
        encrypted.setUsername(authInfo.getUsername());
        data.getInfo().put(courtier, encrypted);
        saveFile(authFilePath, data);
    }

    private Data loadFile(String authFilePath) {
        Data map = null;
        try {
            Reader reader = new BufferedReader(new FileReader(authFilePath));
            map = new Gson().fromJson(reader, Data.class);
            reader.close();
        }
        catch(IOException e){
        }
        if (map == null){
            map = new Data();
            map.setInfo(new HashMap<>());
        }
        return map;
    }

    private void saveFile(String authFilePath, Data data) throws IOException {
        Writer writer = new BufferedWriter(new FileWriter(authFilePath));
        new Gson().toJson(data, writer);
        writer.close();
    }

    // Get a encrypted password using PBKDF2 hash algorithm
    public static String encryptPassword(String clearPassword, String passPhrase) throws Exception {
        if (clearPassword == null) return null;
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, getSecretKey(passPhrase));
        return StringUtils.byteArrayToHexStr(cipher.doFinal(clearPassword.getBytes(StandardCharsets.UTF_8)));
    }

    // Get a decrypted password
    public static String decryptPassword(String encryptedPassword, String passPhrase) throws Exception {
        if (encryptedPassword == null) return null;
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5PADDING");
        cipher.init(Cipher.DECRYPT_MODE, getSecretKey(passPhrase));
        return new String(cipher.doFinal(StringUtils.hexStrToByteArray(encryptedPassword)));
    }

    private static SecretKeySpec getSecretKey(String passPhrase) throws NoSuchAlgorithmException {
        byte[] key = passPhrase.getBytes(StandardCharsets.UTF_8);
        MessageDigest sha = MessageDigest.getInstance("SHA-1");
        key = sha.digest(key);
        key = Arrays.copyOf(key, 16);
        return new SecretKeySpec(key, "AES"); // "AES/CBC/PKCS5Padding"
    }

    // Returns base64 encoded salt
    public static String getNewRandonmEncryptionPhrase() throws Exception {
        // Don't use Random!
        SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
        // NIST recommends minimum 4 bytes. We use 8.
        byte[] salt = new byte[32];
        random.nextBytes(salt);

        return StringUtils.byteArrayToHexStr(salt);
    }

    private static class Data {
        private Map<EnumEZBroker, AuthInfo> info = new HashMap<>();

        public Map<EnumEZBroker, AuthInfo> getInfo() {
            return info;
        }

        public void setInfo(Map<EnumEZBroker, AuthInfo> info) {
            this.info = info;
        }
    }
}
