package com.example;
import java.util.List;

public interface PasswordRepository {
    // パスワードを1件保存する
    void save(PasswordEntry entry) throws Exception;
    // 保存されているすべてのパスワードを読み込む
    List<PasswordEntry> findAll() throws Exception;
    // 指定した番号のパスワードを削除する
    void delete(int index) throws Exception;
    // 【追加】全データを削除する
    void deleteAll() throws Exception;
}