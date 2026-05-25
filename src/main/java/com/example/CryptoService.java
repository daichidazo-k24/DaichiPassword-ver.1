package com.example;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.Random;

public class CryptoService {
    private Random rnd = new Random();

    //鍵の生成
    public BigInteger[] generateKeys() {
        BigInteger p = BigInteger.probablePrime(1024,rnd);
        BigInteger q = BigInteger.probablePrime(1024,rnd);
        BigInteger e = BigInteger.valueOf(65537); //公開暗号鍵
        BigInteger N = p.multiply(q);
        BigInteger A = p.subtract(BigInteger.ONE).multiply(q.subtract(BigInteger.ONE));

        BigInteger gcd = e.gcd(A);
        while(!gcd.equals(BigInteger.ONE)) {
            e = BigInteger.probablePrime(5,rnd);
            gcd = e.gcd(A);
        }
        BigInteger d = e.modInverse(A); //秘密鍵

        //e d N を配列にして返す
        return new BigInteger[]{e,d,N};
    }

    //暗号化
    public String encrypt(String plainText, BigInteger e, BigInteger N) {
        byte[] textBytes = plainText.getBytes(StandardCharsets.UTF_8);
        BigInteger m = new BigInteger(1, textBytes);
        BigInteger cipher = m.modPow(e,N);
        return cipher.toString(16);
    }

    //復号
    public String decrypt(String cipherHex, BigInteger d, BigInteger N) {
        BigInteger cipher = new BigInteger(cipherHex,16);
        BigInteger m = cipher.modPow(d,N);
        byte[] textBytes = m.toByteArray();

        //ここ安全対策らしい
        if (textBytes.length > 0 && textBytes[0] == 0) {
            byte[] tmp = new byte[textBytes.length - 1];
            System.arraycopy(textBytes, 1, tmp, 0, tmp.length);
            textBytes = tmp;
        }

        return new String(textBytes, StandardCharsets.UTF_8);
    }
}