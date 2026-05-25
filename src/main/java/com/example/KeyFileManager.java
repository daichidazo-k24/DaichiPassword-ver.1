package com.example;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.security.spec.KeySpec;

public class KeyFileManager {
    // 保存先フォルダをユーザーホーム直下の「.rsa_password_tool」に固定
    private static final String BASE_DIR = System.getProperty("user.home") + "/.rsa_password_tool/";
    
    private static final String PRIVATE_KEY_FILE = BASE_DIR + "private_key.bin";
    private static final String PUBLIC_KEY_FILE = BASE_DIR + "public_key.txt";
    private static final String SALT_FILE = BASE_DIR + "salt.bin";
    private static final String RECOVERY_KEY_FILE = BASE_DIR + "recovery_key.bin";

    // 親フォルダがなければ自動作成する共通メソッド
    private void ensureDirectoryExists() throws Exception {
        Path dirPath = Paths.get(BASE_DIR);
        if (!Files.exists(dirPath)) {
            Files.createDirectories(dirPath);
        }
    }

    // パスワードからAES鍵を生成(PBKDF2)
    public SecretKey deriveAESKey(String password, byte[] salt) throws Exception {
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, 65536, 256);
        SecretKey tmp = factory.generateSecret(spec);
        return new SecretKeySpec(tmp.getEncoded(), "AES");
    }

    // ソルトの生成
    public byte[] generateSalt() {
        byte[] salt = new byte[16];
        new SecureRandom().nextBytes(salt);
        return salt;
    }

    // ソルトの保存
    public void saveSalt(byte[] salt) throws Exception {
        ensureDirectoryExists(); // フォルダチェック
        Files.write(Paths.get(SALT_FILE), salt);
    }

    // ソルトの読み込み
    public byte[] loadSalt() throws Exception {
        return Files.readAllBytes(Paths.get(SALT_FILE));
    }

    // 公開鍵の保存
    public void savePublicKey(BigInteger e, BigInteger N) throws Exception {
        ensureDirectoryExists(); // フォルダチェック
        String content = e.toString(16) + "," + N.toString(16);
        Files.writeString(Paths.get(PUBLIC_KEY_FILE), content, StandardCharsets.UTF_8);
    }

    // 公開鍵の読み込み
    public BigInteger[] loadPublicKey() throws Exception {
        String content = Files.readString(Paths.get(PUBLIC_KEY_FILE), StandardCharsets.UTF_8);
        String[] parts = content.split(",");
        return new BigInteger[]{new BigInteger(parts[0], 16), new BigInteger(parts[1], 16)};
    }

    // 暗号化された秘密鍵の保存
    public void saveEncryptedPrivateKey(BigInteger d, BigInteger N, SecretKey aesKey) throws Exception {
        ensureDirectoryExists(); // フォルダチェック
        byte[] dBytes = d.toByteArray();
        byte[] nBytes = N.toByteArray();

        ByteBuffer buffer = ByteBuffer.allocate(4 + dBytes.length + 4 + nBytes.length);
        buffer.putInt(dBytes.length);
        buffer.put(dBytes);
        buffer.putInt(nBytes.length);
        buffer.put(nBytes);

        byte[] plainData = buffer.array();
        byte[] iv = new byte[12];
        new SecureRandom().nextBytes(iv);

        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, aesKey, new GCMParameterSpec(128, iv));
        byte[] encryptedData = cipher.doFinal(plainData);

        ByteBuffer fileBuffer = ByteBuffer.allocate(4 + iv.length + encryptedData.length);
        fileBuffer.putInt(iv.length);
        fileBuffer.put(iv);
        fileBuffer.put(encryptedData);

        Files.write(Paths.get(PRIVATE_KEY_FILE), fileBuffer.array());
    }

    // 暗号化された秘密鍵の復号読み込み
    public BigInteger[] loadDecryptedPrivateKey(SecretKey aesKey) throws Exception {
        byte[] fileData = Files.readAllBytes(Paths.get(PRIVATE_KEY_FILE));
        ByteBuffer fileBuffer = ByteBuffer.wrap(fileData);

        int ivLength = fileBuffer.getInt();
        byte[] iv = new byte[ivLength];
        fileBuffer.get(iv);

        byte[] encryptedData = new byte[fileBuffer.remaining()];
        fileBuffer.get(encryptedData);

        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE, aesKey, new GCMParameterSpec(128, iv));
        byte[] decryptedData = cipher.doFinal(encryptedData);

        ByteBuffer buffer = ByteBuffer.wrap(decryptedData);
        int dLength = buffer.getInt();
        byte[] dBytes = new byte[dLength];
        buffer.get(dBytes);
        BigInteger d = new BigInteger(dBytes);

        int nLength = buffer.getInt();
        byte[] nBytes = new byte[nLength];
        buffer.get(nBytes);
        BigInteger N = new BigInteger(nBytes);

        return new BigInteger[]{d, N};
    }

    // 復旧用秘密鍵の保存
    public void saveRecoveryPrivateKey(BigInteger d, BigInteger N, SecretKey aesKey) throws Exception {
        ensureDirectoryExists(); // フォルダチェック
        byte[] dBytes = d.toByteArray();
        byte[] nBytes = N.toByteArray();

        ByteBuffer buffer = ByteBuffer.allocate(4 + dBytes.length + 4 + nBytes.length);
        buffer.putInt(dBytes.length);
        buffer.put(dBytes);
        buffer.putInt(nBytes.length);
        buffer.put(nBytes);

        byte[] plainData = buffer.array();
        byte[] iv = new byte[12];
        new SecureRandom().nextBytes(iv);

        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, aesKey, new GCMParameterSpec(128, iv));
        byte[] encryptedData = cipher.doFinal(plainData);

        ByteBuffer fileBuffer = ByteBuffer.allocate(4 + iv.length + encryptedData.length);
        fileBuffer.putInt(iv.length);
        fileBuffer.put(iv);
        fileBuffer.put(encryptedData);

        Files.write(Paths.get(RECOVERY_KEY_FILE), fileBuffer.array());
    }

    // 復旧用秘密鍵の読み込み
    public BigInteger[] loadRecoveryPrivateKey(SecretKey aesKey) throws Exception {
        byte[] fileData = Files.readAllBytes(Paths.get(RECOVERY_KEY_FILE));
        ByteBuffer fileBuffer = ByteBuffer.wrap(fileData);

        int ivLength = fileBuffer.getInt();
        byte[] iv = new byte[ivLength];
        fileBuffer.get(iv);

        byte[] encryptedData = new byte[fileBuffer.remaining()];
        fileBuffer.get(encryptedData);

        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE, aesKey, new GCMParameterSpec(128, iv));
        byte[] decryptedData = cipher.doFinal(encryptedData);

        ByteBuffer buffer = ByteBuffer.wrap(decryptedData);
        int dLength = buffer.getInt();
        byte[] dBytes = new byte[dLength];
        buffer.get(dBytes);
        BigInteger d = new BigInteger(dBytes);

        int nLength = buffer.getInt();
        byte[] nBytes = new byte[nLength];
        buffer.get(nBytes);
        BigInteger N = new BigInteger(nBytes);

        return new BigInteger[]{d, N};
    }
}