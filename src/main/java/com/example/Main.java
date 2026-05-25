package com.example;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import javax.crypto.SecretKey;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

public class Main extends Application {

    private static CryptoService crypto = new CryptoService();
    private static KeyFileManager keyManager = new KeyFileManager();
    private static PasswordRepository repository = new JsonPasswordRepository();

    private Stage primaryStage;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.primaryStage.setTitle("堅牢パスワード管理ツール (JavaFX)");

        // ★保存先フォルダの絶対パスを指定
        String baseDir = System.getProperty("user.home") + "/.rsa_password_tool/";

        // 鍵が存在しない場合はセットアップ画面、存在する場合はログイン画面
        if (!Files.exists(Paths.get(baseDir + "private_key.bin"))) {
            showSetupScene();
        } else {
            showLoginScene();
        }

        primaryStage.show();
    }

    /**
     * 初回セットアップ画面
     */
    private void showSetupScene() {
        VBox root = new VBox(15);
        root.setPadding(new Insets(20));
        root.setAlignment(Pos.CENTER);

        Label titleLabel = new Label("【初回セットアップ】");
        titleLabel.setStyle("-font-size: 18px; -fx-font-weight: bold;");

        Label descLabel = new Label("あなた専用のRSA鍵（2048bit）を生成し、マスターパスワードを設定します。");
        descLabel.setWrapText(true);

        PasswordField passField = new PasswordField();
        passField.setPromptText("マスターパスワードを入力");
        PasswordField confirmField = new PasswordField();
        confirmField.setPromptText("マスターパスワード（確認用）");

        Button submitBtn = new Button("鍵を生成してセットアップを完了する");
        submitBtn.setMaxWidth(Double.MAX_VALUE);

        root.getChildren().addAll(titleLabel, descLabel, passField, confirmField, submitBtn);

        submitBtn.setOnAction(e -> {
            String pass = passField.getText();
            String confirm = confirmField.getText();

            if (pass.isEmpty() || !pass.equals(confirm)) {
                showAlert(Alert.AlertType.ERROR, "エラー", "パスワードが一致しないか、空欄です。");
                return;
            }

            try {
                // 鍵生成と暗号化保存処理
                BigInteger[] keys = crypto.generateKeys();
                byte[] salt = keyManager.generateSalt();
                keyManager.saveSalt(salt);

                SecretKey aesKey = keyManager.deriveAESKey(pass, salt);
                keyManager.saveEncryptedPrivateKey(keys[1], keys[2], aesKey);
                keyManager.savePublicKey(keys[0], keys[2]);

                String recoveryKey = UUID.randomUUID().toString();
                SecretKey recoveryAesKey = keyManager.deriveAESKey(recoveryKey, salt);
                keyManager.saveRecoveryPrivateKey(keys[1], keys[2], recoveryAesKey);

                // 復旧キーの明示
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("【超重要】復旧キーの発行");
                alert.setHeaderText("マスターパスワードを忘れた場合のリカバリーキーです。");
                
                TextArea textArea = new TextArea(recoveryKey);
                textArea.setEditable(false);
                textArea.setWrapText(true);
                textArea.setMaxHeight(80);
                alert.getDialogPane().setContent(textArea);
                
                alert.showAndWait();

                showLoginScene(); // セットアップ完了後にログインへ

            } catch (Exception ex) {
                showAlert(Alert.AlertType.ERROR, "システムエラー", ex.getMessage());
            }
        });

        primaryStage.setScene(new Scene(root, 450, 300));
    }

    /**
     * ログイン（マスターパスワード入力）画面
     */
    private void showLoginScene() {
        VBox root = new VBox(15);
        root.setPadding(new Insets(30));
        root.setAlignment(Pos.CENTER);

        Label titleLabel = new Label("マスターパスワード入力");
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        PasswordField passField = new PasswordField();
        passField.setPromptText("マスターパスワード");

        Button loginBtn = new Button("ログイン");
        loginBtn.setMaxWidth(Double.MAX_VALUE);

        Hyperlink recoverLink = new Hyperlink("マスターパスワードを忘れた場合 (復旧)");

        root.getChildren().addAll(titleLabel, passField, loginBtn, recoverLink);

        // ログイン処理
        loginBtn.setOnAction(e -> {
            String master = passField.getText();
            try {
                SecretKey aesKey = keyManager.deriveAESKey(master, keyManager.loadSalt());
                BigInteger[] priv = keyManager.loadDecryptedPrivateKey(aesKey); // 復号テスト

                // 認証成功：メイン画面へ（秘密鍵を渡して管理させる）
                showMainDashboard(priv);

            } catch (Exception ex) {
                showAlert(Alert.AlertType.ERROR, "認証失敗", "マスターパスワードが正しくありません。");
            }
        });

        // 復旧画面へのリンク
        recoverLink.setOnAction(e -> showRecoveryDialog());

        primaryStage.setScene(new Scene(root, 400, 250));
    }

    /**
     * パスワード管理メインダッシュボード（ログイン後画面）
     */
    private void showMainDashboard(BigInteger[] privKey) {
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(15));

        // 左側：パスワードのタイトル一覧
        ListView<String> listView = new ListView<>();
        updateListView(listView);
        VBox leftBox = new VBox(10, new Label("登録済みデータ一覧"), listView);
        VBox.setVgrow(listView, Priority.ALWAYS);
        root.setLeft(leftBox);

        // 右側：詳細表示 ＆ 操作ボタン
        VBox rightBox = new VBox(15);
        rightBox.setPadding(new Insets(0, 0, 0, 15));
        rightBox.setAlignment(Pos.TOP_LEFT);

        Label detailTitle = new Label("詳細情報");
        detailTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        
        TextField txtTitle = new TextField(); txtTitle.setEditable(false); txtTitle.setPromptText("タイトル");
        TextField txtUser = new TextField(); txtUser.setEditable(false); txtUser.setPromptText("ユーザー名");
        TextField txtPass = new TextField(); txtPass.setEditable(false); txtPass.setPromptText("パスワード");

        Button addBtn = new Button("新規追加");
        Button deleteBtn = new Button("選択中のデータを削除");
        Button initBtn = new Button("システム初期化");
        initBtn.setStyle("-fx-text-fill: red;");

        addBtn.setMaxWidth(Double.MAX_VALUE);
        deleteBtn.setMaxWidth(Double.MAX_VALUE);
        initBtn.setMaxWidth(Double.MAX_VALUE);

        rightBox.getChildren().addAll(detailTitle, 
                new Label("タイトル:"), txtTitle, 
                new Label("ユーザー名:"), txtUser, 
                new Label("パスワード:"), txtPass, 
                new Separator(), addBtn, deleteBtn, initBtn);
        root.setCenter(rightBox);

        // リストを選択した時のイベント（自動復号表示）
        listView.getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) -> {
            int idx = newValue.intValue();
            if (idx < 0) return;
            try {
                List<PasswordEntry> entries = repository.findAll();
                PasswordEntry entry = entries.get(idx);

                // 暗号化されたユーザ名とパスワードをRSAで復号
                String decUser = crypto.decrypt(entry.getEncryptedUsername(), privKey[0], privKey[1]);
                String decPass = crypto.decrypt(entry.getEncryptedPassword(), privKey[0], privKey[1]);

                txtTitle.setText(entry.getTitle());
                txtUser.setText(decUser);
                txtPass.setText(decPass);
            } catch (Exception ex) {
                showAlert(Alert.AlertType.ERROR, "復号エラー", "データの復号に失敗しました。");
            }
        });

        // 新規追加ボタンの挙動
        addBtn.setOnAction(e -> showAddDialog(listView));

        // 削除ボタンの挙動
        deleteBtn.setOnAction(e -> {
            int idx = listView.getSelectionModel().getSelectedIndex();
            if (idx < 0) {
                showAlert(Alert.AlertType.WARNING, "警告", "削除するデータを選択してください。");
                return;
            }
            try {
                repository.delete(idx);
                txtTitle.clear(); txtUser.clear(); txtPass.clear();
                updateListView(listView);
                showAlert(Alert.AlertType.INFORMATION, "成功", "削除しました。");
            } catch (Exception ex) {
                showAlert(Alert.AlertType.ERROR, "エラー", ex.getMessage());
            }
        });

        // 初期化ボタンの挙動
        initBtn.setOnAction(e -> {
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("システム初期化");
            dialog.setHeaderText("【警告】すべてのデータと鍵が消去されます。\n続行するにはマスターパスワードを入力してください。");
            dialog.showAndWait().ifPresent(pass -> {
                try {
                    // パスワード確認
                    keyManager.loadDecryptedPrivateKey(keyManager.deriveAESKey(pass, keyManager.loadSalt()));
                    
                    repository.deleteAll();
                    
                    // ★削除するファイルも絶対パスで指定する
                    String baseDir = System.getProperty("user.home") + "/.rsa_password_tool/";
                    Files.deleteIfExists(Paths.get(baseDir + "private_key.bin"));
                    Files.deleteIfExists(Paths.get(baseDir + "public_key.txt"));
                    Files.deleteIfExists(Paths.get(baseDir + "salt.bin"));
                    Files.deleteIfExists(Paths.get(baseDir + "recovery_key.bin"));

                    showAlert(Alert.AlertType.INFORMATION, "初期化完了", "システムを初期化しました。アプリケーションを再起動してください。");
                    System.exit(0);
                } catch (Exception ex) {
                    showAlert(Alert.AlertType.ERROR, "エラー", "マスターパスワードが正しくありません。");
                }
            });
        });

        primaryStage.setScene(new Scene(root, 650, 450));
    }

    private void updateListView(ListView<String> listView) {
        try {
            listView.getItems().clear();
            for (PasswordEntry entry : repository.findAll()) {
                listView.getItems().add(entry.getTitle());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 新規追加ダイアログ
     */
    private void showAddDialog(ListView<String> listView) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("新しいパスワードを登録");
        dialog.setHeaderText("情報を入力してください。");

        ButtonType saveButtonType = new ButtonType("保存", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10); grid.setPadding(new Insets(20, 150, 10, 10));

        TextField title = new TextField(); title.setPromptText("GitHub, Amazon など");
        TextField username = new TextField(); username.setPromptText("User ID / Email");
        PasswordField password = new PasswordField(); password.setPromptText("Password");

        grid.add(new Label("タイトル:"), 0, 0);  grid.add(title, 1, 0);
        grid.add(new Label("ユーザー名:"), 0, 1); grid.add(username, 1, 1);
        grid.add(new Label("パスワード:"), 0, 2); grid.add(password, 1, 2);

        dialog.getDialogPane().setContent(grid);

        dialog.showAndWait().ifPresent(buttonType -> {
            if (buttonType == saveButtonType) {
                try {
                    BigInteger[] pub = keyManager.loadPublicKey();
                    String encUser = crypto.encrypt(username.getText(), pub[0], pub[1]);
                    String encPass = crypto.encrypt(password.getText(), pub[0], pub[1]);

                    repository.save(new PasswordEntry(title.getText(), encUser, encPass));
                    updateListView(listView);
                } catch (Exception ex) {
                    showAlert(Alert.AlertType.ERROR, "エラー", "保存に失敗しました: " + ex.getMessage());
                }
            }
        });
    }

    /**
     * 復旧ダイアログ
     */
    private void showRecoveryDialog() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("マスターパスワードの復旧");
        dialog.setHeaderText("保管していた「復旧キー」と「新しいマスターパスワード」を入力してください。");

        ButtonType okBtnType = new ButtonType("再設定を実行", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(okBtnType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10);

        TextField recKeyField = new TextField(); recKeyField.setPromptText("UUID形式の復旧キー");
        PasswordField newPassField = new PasswordField(); newPassField.setPromptText("新しいパスワード");
        PasswordField confirmField = new PasswordField(); confirmField.setPromptText("新しいパスワード(確認)");

        grid.add(new Label("復旧キー:"), 0, 0); grid.add(recKeyField, 1, 0);
        grid.add(new Label("新パスワード:"), 0, 1); grid.add(newPassField, 1, 1);
        grid.add(new Label("新パスワード(確認):"), 0, 2); grid.add(confirmField, 1, 2);

        dialog.getDialogPane().setContent(grid);

        dialog.showAndWait().ifPresent(btn -> {
            if (btn == okBtnType) {
                String recKey = recKeyField.getText();
                String newPass = newPassField.getText();
                String confirm = confirmField.getText();

                if (!newPass.equals(confirm) || newPass.isEmpty()) {
                    showAlert(Alert.AlertType.ERROR, "エラー", "パスワードが一致しないか空欄です。");
                    return;
                }

                try {
                    SecretKey recoveryAesKey = keyManager.deriveAESKey(recKey, keyManager.loadSalt());
                    BigInteger[] priv = keyManager.loadRecoveryPrivateKey(recoveryAesKey);

                    // 新しいパスワードで秘密鍵を暗号化し直して上書き
                    SecretKey newAesKey = keyManager.deriveAESKey(newPass, keyManager.loadSalt());
                    keyManager.saveEncryptedPrivateKey(priv[0], priv[1], newAesKey);

                    showAlert(Alert.AlertType.INFORMATION, "成功", "マスターパスワードの再設定が完了しました。ログインしてください。");
                    showLoginScene();
                } catch (Exception ex) {
                    showAlert(Alert.AlertType.ERROR, "エラー", "復旧キーが正しくありません。");
                }
            }
        });
    }

    private void showAlert(Alert.AlertType type, String title, String text) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(text);
        alert.showAndWait();
    }
}