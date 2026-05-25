package com.example;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class JsonPasswordRepository implements PasswordRepository {

    // 保存先フォルダをユーザーホーム直下の「.rsa_password_tool」に固定
    private static final String BASE_DIR = System.getProperty("user.home") + "/.rsa_password_tool/";
    private static final String FILE_PATH = BASE_DIR + "passwords.json";
    private Gson gson;

    public JsonPasswordRepository() {
        this.gson = new GsonBuilder().setPrettyPrinting().create();
    }

    // 親フォルダがなければ自動作成する共通メソッド
    private void ensureDirectoryExists() throws Exception {
        Path dirPath = Paths.get(BASE_DIR);
        if (!Files.exists(dirPath)) {
            Files.createDirectories(dirPath);
        }
    }

    @Override
    public List<PasswordEntry> findAll() throws Exception {
        Path path = Paths.get(FILE_PATH);
        
        if (!Files.exists(path)) {
            return new ArrayList<>();
        }

        String json = Files.readString(path, StandardCharsets.UTF_8);
        if (json.trim().isEmpty()) {
            return new ArrayList<>();
        }

        Type listType = new TypeToken<ArrayList<PasswordEntry>>(){}.getType();
        return gson.fromJson(json, listType);
    }

    @Override
    public void save(PasswordEntry entry) throws Exception {
        ensureDirectoryExists(); // フォルダチェック
        List<PasswordEntry> entries = findAll();
        entries.add(entry);

        String json = gson.toJson(entries);
        Files.writeString(Paths.get(FILE_PATH), json, StandardCharsets.UTF_8);
    }

    @Override
    public void delete(int index) throws Exception {
        ensureDirectoryExists(); // フォルダチェック
        List<PasswordEntry> entries = findAll();
        
        if (index >= 0 && index < entries.size()) {
            entries.remove(index);
            String json = gson.toJson(entries);
            Files.writeString(Paths.get(FILE_PATH), json, StandardCharsets.UTF_8);
        }
    }

    @Override
    public void deleteAll() throws Exception {
        ensureDirectoryExists(); // フォルダチェック
        Files.deleteIfExists(Paths.get(FILE_PATH));
    }
}