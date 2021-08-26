package com.pascal.bientotrentier.service.security;

import com.google.gson.Gson;
import com.pascal.bientotrentier.service.config.MainSettings;
import com.pascal.bientotrentier.service.model.EnumBRCourtier;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class AuthManager {

    private final String authFilePath;
    private final String passPhrase;

    public AuthManager(String passPhrase , String authFilePath){
        this.passPhrase = passPhrase;
        this.authFilePath = authFilePath;
    }

    public MainSettings.AuthInfo getAuthInfo(EnumBRCourtier courtier) throws Exception {
        Data data = loadFile(authFilePath);
        MainSettings.AuthInfo info = data.getInfo().get(courtier.name());
        if (info == null) return null;
        MainSettings.AuthInfo result = new MainSettings.AuthInfo();
        result.setPassword(decryptPassword(info.getPassword(), passPhrase));
        result.setUsername(decryptPassword(info.getUsername(), passPhrase));
        return result;
    }

    public void addAuthInfo(EnumBRCourtier courtier, MainSettings.AuthInfo authInfo) throws Exception {
        Data data = loadFile(authFilePath);
        MainSettings.AuthInfo encrypted = new MainSettings.AuthInfo();
        encrypted.setPassword(encryptPassword(authInfo.getPassword(), passPhrase));
        encrypted.setUsername(encryptPassword(authInfo.getUsername(), passPhrase));
        data.getInfo().put(courtier.name(), encrypted);
        saveFile(authFilePath, data);
    }

    private Data loadFile(String authFilePath) throws IOException {
        if (!new File(authFilePath).exists()) return new Data();
        Reader reader = new BufferedReader(new FileReader(authFilePath));
        Data map = new Gson().fromJson(reader, Data.class);
        reader.close();
        return map;
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
        private Map<String, MainSettings.AuthInfo> info = new HashMap<>();

        public Map<String, MainSettings.AuthInfo> getInfo() {
            return info;
        }

        public void setInfo(Map<String, MainSettings.AuthInfo> info) {
            this.info = info;
        }
    }
}
