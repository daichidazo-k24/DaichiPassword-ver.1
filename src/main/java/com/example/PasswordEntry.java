package com.example;
import java.io.Serializable;

public class PasswordEntry implements Serializable {
    private String title;
    private String encryptedUsername; 
    private String encryptedPassword; 

    // コンストラクタ
    public PasswordEntry(String title, String encryptedUsername, String encryptedPassword) {
        this.title = title;
        this.encryptedUsername = encryptedUsername;
        this.encryptedPassword = encryptedPassword;
    }
    
    // ゲッター
    public String getTitle() { return title; }
    public String getEncryptedUsername() { return encryptedUsername; } 
    public String getEncryptedPassword() { return encryptedPassword; }
}