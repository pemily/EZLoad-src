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
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import com.google.gson.Gson;
import com.pascal.ezload.service.config.AuthInfo;
import com.pascal.ezload.service.model.EnumEZBroker;

import org.apache.commons.lang3.StringUtils;

public class AuthManager {

    private final String authFilePath;
    private final String passPhrase;

    public AuthManager(String passPhrase, String authFilePath){
        this.passPhrase = passPhrase;
        this.authFilePath = authFilePath;
    }

    public AuthInfo getAuthWithDummyPassword(EnumEZBroker courtier) throws Exception {
        Data data = loadFile(authFilePath);
        AuthInfo info = data.getInfo().get(courtier);
        if (info == null) return null;
        AuthInfo result = new AuthInfo();
        result.setUsername(info.getUsername());
        String dummyPassword = "";
        if (passPhrase != null)
            for (int i = 0; i < decryptPassword(info.getPassword(),passPhrase).length(); i++) dummyPassword += "@";
        result.setPassword(StringUtils.isBlank(info.getPassword()) ? null : dummyPassword); // to send it to the browser
        return result;
    }

    public AuthInfo getAuthInfo(EnumEZBroker courtier) throws Exception {
        Data data = loadFile(authFilePath);
        AuthInfo info = data.getInfo().get(courtier);
        if (info == null) return null;
        AuthInfo result = new AuthInfo();
        result.setPassword(decryptPassword(info.getPassword(), passPhrase));
        result.setUsername(info.getUsername());
        return result;
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
        try {
            Reader reader = new BufferedReader(new FileReader(authFilePath));
            Data map = new Gson().fromJson(reader, Data.class);
            reader.close();
            return map;
        }
        catch(IOException e){
            return new Data();
        }
    }

    private void saveFile(String authFilePath, Data data) throws IOException {
        Writer writer = new BufferedWriter(new FileWriter(authFilePath));
        new Gson().toJson(data, writer);
        writer.close();
    }

    // Get a encrypted password using PBKDF2 hash algorithm
    private static String encryptPassword(String clearPassword, String passPhrase) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, getSecretKey(passPhrase));
        return byteArrayToHexStr(cipher.doFinal(clearPassword.getBytes(StandardCharsets.UTF_8)));
    }

    // Get a decrypted password
    private static String decryptPassword(String encryptedPassword, String passPhrase) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5PADDING");
        cipher.init(Cipher.DECRYPT_MODE, getSecretKey(passPhrase));
        return new String(cipher.doFinal(hexStrToByteArray(encryptedPassword)));
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

        return byteArrayToHexStr(salt);
    }

    private static byte[] hexStrToByteArray(String s){
        return Base64.getDecoder().decode(s);
    }

    private static String byteArrayToHexStr(byte[] b){
        return Base64.getEncoder().encodeToString(b);
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
