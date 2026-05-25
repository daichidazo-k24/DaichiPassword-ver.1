import os
from weasyprint import HTML

html_content = """
<!DOCTYPE html>
<html lang="ja">
<head>
    <meta charset="UTF-8">
    <title>堅牢パスワード管理ツール 仕様書</title>
    <style>
        @page {
            size: A4;
            margin: 20mm 15mm;
            @top-right {
                content: "堅牢パスワード管理ツール 仕様書";
                font-family: 'Helvetica Neue', Arial, 'Hiragino Kaku Gothic ProN', 'Hiragino Sans', Meiryo, sans-serif;
                font-size: 8pt;
                color: #718096;
            }
            @bottom-right {
                content: counter(page) " / " counter(pages);
                font-family: 'Helvetica Neue', Arial, 'Hiragino Kaku Gothic ProN', 'Hiragino Sans', Meiryo, sans-serif;
                font-size: 9pt;
                color: #718096;
            }
        }
        
        *, *::before, *::after {
            box-sizing: border-box;
        }

        body {
            font-family: 'Helvetica Neue', Arial, 'Hiragino Kaku Gothic ProN', 'Hiragino Sans', Meiryo, sans-serif;
            font-size: 10.5pt;
            line-height: 1.6;
            color: #2d3748;
            margin: 0;
            padding: 0;
        }

        /* タイトルセクション */
        .header-banner {
            background-color: #1a365d;
            color: #ffffff;
            margin: -20mm -15mm 25px -15mm;
            padding: 30px 15mm;
            border-bottom: 5px solid #2b6cb0;
        }
        
        .header-banner h1 {
            font-size: 24pt;
            margin: 0 0 10px 0;
            font-weight: bold;
            letter-spacing: 1px;
        }
        
        .header-banner .subtitle {
            font-size: 12pt;
            color: #ebf8ff;
            margin: 0;
            opacity: 0.9;
        }

        .meta-info {
            text-align: right;
            font-size: 9pt;
            color: #4a5568;
            margin-bottom: 30px;
        }

        /* 見出し */
        h2 {
            font-size: 14pt;
            color: #1a365d;
            border-left: 5px solid #2b6cb0;
            padding-left: 10px;
            margin-top: 30px;
            margin-bottom: 15px;
            page-break-after: avoid;
        }

        h3 {
            font-size: 11.5pt;
            color: #2c5282;
            margin-top: 20px;
            margin-bottom: 10px;
            page-break-after: avoid;
        }

        p {
            margin-top: 0;
            margin-bottom: 15px;
            text-align: justify;
        }

        /* 箇条書き */
        ul, ol {
            margin-top: 0;
            margin-bottom: 15px;
            padding-left: 20px;
        }

        li {
            margin-bottom: 5px;
        }

        /* テーブル */
        table {
            width: 100%;
            border-collapse: collapse;
            margin-bottom: 20px;
            font-size: 10pt;
        }

        th, td {
            border: 1px solid #cbd5e0;
            padding: 8px 12px;
            text-align: left;
        }

        th {
            background-color: #edf2f7;
            color: #2d3748;
            font-weight: bold;
        }

        tr:nth-child(even) td {
            background-color: #f7fafc;
        }

        /* コードブロック */
        pre {
            background-color: #f7fafc;
            border: 1px solid #e2e8f0;
            border-radius: 4px;
            padding: 12px;
            font-family: Consolas, Monaco, monospace;
            font-size: 9.5pt;
            overflow: -webkit-paged-x;
            margin-top: 0;
            margin-bottom: 15px;
            white-space: pre-wrap;
        }

        /* コールアウト（強調枠） */
        .callout {
            background-color: #ebf8ff;
            border-left: 4px solid #3182ce;
            padding: 12px 15px;
            margin-bottom: 20px;
            border-radius: 0 4px 4px 0;
        }
        
        .callout-title {
            font-weight: bold;
            color: #2b6cb0;
            margin-bottom: 5px;
        }

        .callout-warning {
            background-color: #fffaf0;
            border-left: 4px solid #dd6b20;
            padding: 12px 15px;
            margin-bottom: 20px;
            border-radius: 0 4px 4px 0;
        }

        .callout-warning .callout-title {
            color: #dd6b20;
        }

        .page-break {
            page-break-before: always;
        }
    </style>
</head>
<body>

    <div class="header-banner">
        <h1>堅牢パスワード管理ツール</h1>
        <div class="subtitle">製品仕様書・配布マニュアル (GitHub公開用)</div>
    </div>

    <div class="meta-info">
        作成日: 2026年5月25日<br>
        バージョン: 1.0.0
    </div>

    <h2>1. 製品概要</h2>
    <p>
        本製品は、ハイブリッド暗号方式（RSAおよびAES）を採用した、スタンドアロン型の高セキュリティなデスクトップ用パスワード管理アプリケーションです。
        JavaFXによる視覚的で直感的なグラフィカルユーザーインターフェース（GUI）を提供し、外部のクラウドサーバーに依存することなく、すべての認証データおよび秘密鍵をローカル環境で安全に一元管理します。
    </p>

    <h3>主な特徴</h3>
    <ul>
        <li><strong>強力な暗号化アルゴリズム:</strong> パスワードやユーザー名の暗号化にはRSA（2048bit）を用い、秘密鍵自体の保護にはAES-GCM（128bit）およびPBKDF2によるストレッチングを採用。</li>
        <li><strong>完全なローカル管理:</strong> ネットワーク通信を一切行わず、認証情報はすべてユーザーのローカルディレクトリ内に暗号化保存（JSON形式）されます。</li>
        <li><strong>ポータビリティの確保:</strong> 実行ファイルの保存場所に関わらず、常に一定の絶対パス（ユーザーホームディレクトリ直下）を参照するため、USBメモリやGitHub配布など、どこから起動しても同一のデータに安全にアクセスできます。</li>
    </ul>

    <h2>2. システム要件</h2>
    <table>
        <thead>
            <tr>
                <th style="width: 30%;">項目</th>
                <th style="width: 70%;">要件・仕様</th>
            </tr>
        </thead>
        <tbody>
            <tr>
                <td><strong>対象OS</strong></td>
                <td>Windows 10 / 11, macOS, Linux (Java環境が動作する主要OS)</td>
            </tr>
            <tr>
                <td><strong>推奨Java環境</strong></td>
                <td>Java 17 以上 (OpenJDK 推奨)</td>
            </tr>
            <tr>
                <td><strong>ビルドツール</strong></td>
                <td>Apache Maven 3.6 以上 (ソースコードからビルドする場合)</td>
            </tr>
            <tr>
                <td><strong>主な依存ライブラリ</strong></td>
                <td>OpenJFX (JavaFX) 17.0.2, Google Gson 2.10.1</td>
            </tr>
        </tbody>
    </table>

    <h2>3. 暗号化・データ構造仕様</h2>
    <h3>3.1 使用アルゴリズム一覧</h3>
    <ul>
        <li><strong>データの暗号化（非対称鍵）:</strong> RSA 2048bit (鍵生成時の素数: 1024bit &times; 2)</li>
        <li><strong>秘密鍵の保護（共通鍵）:</strong> AES-GCM (128bit), IV (12px / 96bit 乱数)</li>
        <li><strong>マスターパスワードのストレッチング:</strong> PBKDF2WithHmacSHA256 (反復回数: 65,536回, ソルト: 16px 乱数)</li>
    </ul>

    <h3>3.2 データ保存先とファイル構成</h3>
    <p>本ツールは、どこから実行されてもデータを統一できるよう、以下の「絶対パス」に自動で専用フォルダを生成し、関連ファイルを保管します。</p>
    <p><strong>保存先フォルダ:</strong> <code>${user.home}/.rsa_password_tool/</code> （Windowsの場合: <code>C:\Users\(ユーザー名)\.rsa_password_tool\</code>）</p>

    <table>
        <thead>
            <tr>
                <th style="width: 30%;">ファイル名</th>
                <th style="width: 25%;">フォーマット</th>
                <th style="width: 45%;">役割</th>
            </tr>
        </thead>
        <tbody>
            <tr>
                <td><code>passwords.json</code></td>
                <td>JSON</td>
                <td>暗号化されたログイン情報（タイトル、ユーザー名、パスワード）のリスト。</td>
            </tr>
            <tr>
                <td><code>private_key.bin</code></td>
                <td>バイナリ</td>
                <td>マスターパスワード由来のAES鍵で暗号化されたRSA秘密鍵。</td>
            </tr>
            <tr>
                <td><code>public_key.txt</code></td>
                <td>テキスト</td>
                <td>暗号化に使用するRSA公開鍵（平文保存）。</td>
            </tr>
            <tr>
                <td><code>salt.bin</code></td>
                <td>バイナリ</td>
                <td>PBKDF2の鍵生成で使用する16バイトの固有ソルト値。</td>
            </tr>
            <tr>
                <td><code>recovery_key.bin</code></td>
                <td>バイナリ</td>
                <td>初回に自動発行される「復旧キー」で暗号化されたRSA秘密鍵。</td>
            </tr>
        </tbody>
    </table>

    <div class="page-break"></div>

    <h2>4. 機能一覧</h2>
    <ol>
        <li><strong>初回セットアップ機能:</strong> アプリ初回起動時に自動検知。マスターパスワードを設定し、RSA鍵ペア、暗号化秘密鍵、公開鍵、およびソルトを自動生成。同時に「復旧キー（UUID）」を発行。</li>
        <li><strong>ログイン認証機能:</strong> 起動時にマスターパスワードを入力。PBKDF2によりAES鍵を導出し、<code>private_key.bin</code> の復号を試みることで認証を行う（成否は復号の成否に依存）。</li>
        <li><strong>パスワード一覧・自動復号表示:</strong> 登録されているサイトタイトルのリストを表示。リスト選択時、内部で自動的にRSA秘密鍵を用いてユーザー名とパスワードを復号し、画面に安全に表示。</li>
        <li><strong>新規パスワード登録機能:</strong> タイトル、ユーザー名、パスワードを入力し、公開鍵（RSA）でユーザー名・パスワードを個別に暗号化した上で <code>passwords.json</code> に追加。</li>
        <li><strong>データ削除機能:</strong> 選択された特定のパスワードデータを一覧およびJSONファイルから完全に削除。</li>
        <li><strong>マスターパスワード忘却時のリカバリー機能:</strong> 初期画面のリンクから、初回発行された「復旧キー」と「新しいマスターパスワード」を入力することで、秘密鍵を安全に再暗号化し、アカウントを復旧。</li>
        <li><strong>システム初期化機能:</strong> ダッシュボードからマスターパスワードを入力して実行。すべての保存データおよび鍵ファイルを絶対パスフォルダ内から物理削除し、工場出荷状態に戻す。</li>
    </ol>

    <h2>5. 配布パッケージ・リポジトリ構成 (.zip / GitHub)</h2>
    <p>GitHubへ公開、または <code>.zip</code> 形式で配布する際は、以下のファイル構造で格納します。一般ユーザー向けに、あらかじめビルド済みの大容量JAR（依存ライブラリ内包型）を <code>target/</code> もしくはリポジトリの Releases 機能に配置することを推奨します。</p>

    <pre>
rsa-password/
├── pom.xml                        # Mavenプロジェクト設定ファイル
├── src/                           # ソースコードディレクトリ
│   └── main/
│       └── java/
│           └── com/
│               └── example/
│                   ├── Launcher.java               # メインクラス呼び出し用ランチャー
│                   ├── Main.java                   # JavaFX 画面制御・メインロジック
│                   ├── CryptoService.java          # RSA暗号・復号ロジック
│                   ├── KeyFileManager.java         # 鍵ファイル・AES・PBKDF2制御
│                   ├── PasswordEntry.java          # パスワードデータモデル
│                   ├── PasswordRepository.java     # リポジトリインターフェース
│                   └── JsonPasswordRepository.java # JSONファイル入出力実装
└── target/                        # ビルド成果物（配布時はここに格納、またはReleasesに添付）
    └── rsa-password-1.0-SNAPSHOT-jar-with-dependencies.jar  # ビルド済み実行可能ファイル
    </pre>

    <h2>6. 利用・実行手順</h2>
    
    <h3>6.1 開発者・ビルドを行う場合</h3>
    <p>リポジトリをクローン、または <code>.zip</code> を解凍後、ルートディレクトリで以下のコマンドを実行します。</p>
    
    <p><strong>開発環境での直接実行:</strong></p>
    <pre>mvn javafx:run</pre>
    
    <p><strong>配布用JARファイルの生成（ビルド）:</strong></p>
    <pre>mvn clean package</pre>
    <p>ビルドが成功すると、<code>target/</code> ディレクトリ配下に <code>-jar-with-dependencies.jar</code> で終わるファイルが生成されます。これがライブラリを内包したスタンドアロン実行可能ファイルです。</p>

    <h3>6.2 一般ユーザーが実行する場合（配布方法）</h3>
    <p>利用者は、Java 17以上がインストールされた環境であれば、以下のいずれかの方法で簡単にツールを実行できます。</p>
    <ul>
        <li><strong>ダブルクリックによる実行:</strong> <code>rsa-password-1.0-SNAPSHOT-jar-with-dependencies.jar</code> を直接ダブルクリック。</li>
        <li><strong>コマンドラインからの実行:</strong> ターミナルまたはコマンドプロンプトを開き、以下のコマンドを実行。</li>
    </ul>
    <pre>java -jar rsa-password-1.0-SNAPSHOT-jar-with-dependencies.jar</pre>

    <div class="callout">
        <div class="callout-title">💡 データ統一のメリット</div>
        このJARファイルは、デスクトップやドキュメントフォルダ、あるいはUSBメモリなど、PC内のどこの場所に移動させて実行しても、常にユーザーフォルダ直下の <code>.rsa_password_tool</code> を見に行くため、データが散らばったり紛失したりする心配がありません。
    </div>

    <h2>7. セキュリティに関する注意事項</h2>
    <div class="callout-warning">
        <div class="callout-title">⚠️ 利用者への重要なお願い</div>
        <ol>
            <li><strong>マスターパスワードの厳重保管:</strong> マスターパスワードはローカル内のどこにも平文で保存されていません。忘却し、かつ復旧キーもない場合、暗号の性質上、開発者であってもデータを復元することは不可能です。</li>
            <li><strong>復旧キーのオフライン退避:</strong> 初回セットアップ時に発行される「復旧キー」は、メモ帳などにコピーして印刷するか、物理的なノートに書き留めるなど、PC外の安全な場所に保管してください。</li>
            <li><strong>データのバックアップ:</strong> PCの故障や紛失に備え、定期的に <code>~/.rsa_password_tool/</code> フォルダごと安全な外部ストレージに暗号化バックアップを取ることを推奨します。</li>
        </ol>
    </div>

</body>
</html>
"""

# HTMLファイルとして一時保存
html_filename = "password_manager_spec.html"
pdf_filename = "password_manager_spec.pdf"

with open(html_filename, "w", encoding="utf-8") as f:
    f.write(html_content)

# WeasyPrintでPDFに変換
HTML(html_filename).write_pdf(pdf_filename)

print(f"Successfully generated {pdf_filename}")