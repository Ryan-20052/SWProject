package anbd.he191271;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LicenseClientApp extends Application {

    private final String BASE_URL = "http://localhost:8080";
    private Stage stage;
    private String customerEmail;

    @Override
    public void start(Stage primaryStage) {
        this.stage = primaryStage;
        primaryStage.setTitle("License Shop - Desktop Client");
        showLoginScene();
    }

    /* ===========================
       1️⃣ MÀN HÌNH ĐĂNG NHẬP - ĐƯỢC THIẾT KẾ LẠI
       =========================== */
    private void showLoginScene() {
        // Header
        Label titleLabel = new Label("LICENSE SHOP");
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 24));
        titleLabel.setTextFill(Color.web("#2C3E50"));

        Label subtitleLabel = new Label("Đăng nhập vào hệ thống");
        subtitleLabel.setFont(Font.font("System", 14));
        subtitleLabel.setTextFill(Color.web("#7F8C8D"));

        // Form fields
        Label usernameLabel = new Label("Tên đăng nhập:");
        usernameLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #2C3E50;");
        TextField usernameField = new TextField();
        usernameField.setPromptText("Nhập tên đăng nhập");
        usernameField.setStyle("-fx-pref-height: 35px; -fx-background-radius: 5;");

        Label passwordLabel = new Label("Mật khẩu:");
        passwordLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #2C3E50;");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Nhập mật khẩu");
        passwordField.setStyle("-fx-pref-height: 35px; -fx-background-radius: 5;");

        Label msgLabel = new Label();
        msgLabel.setWrapText(true);

        Button loginBtn = new Button("ĐĂNG NHẬP");
        loginBtn.setStyle("-fx-background-color: #3498DB; -fx-text-fill: white; -fx-font-weight: bold; -fx-pref-height: 40px; -fx-pref-width: 200px; -fx-background-radius: 5;");
        loginBtn.setOnAction(e -> {
            try {
                Map<String, String> body = new HashMap<>();
                body.put("username", usernameField.getText());
                body.put("password", passwordField.getText());

                String response = postJson(BASE_URL + "/auth/customer/app-login", mapToJson(body));

                if (response.contains("\"ok\":true")) {
                    msgLabel.setText("✅ OTP đã được gửi tới email của bạn!");
                    msgLabel.setTextFill(Color.GREEN);
                    String email = extractJsonField(response, "email");
                    this.customerEmail = email;
                    showOtpScene(email);
                } else {
                    String msg = extractJsonField(response, "message");
                    msgLabel.setText("❌ " + (msg.isEmpty() ? "Đăng nhập thất bại" : msg));
                    msgLabel.setTextFill(Color.RED);
                }
            } catch (Exception ex) {
                msgLabel.setText("⚠️ Lỗi kết nối đến server!");
                msgLabel.setTextFill(Color.ORANGE);
                ex.printStackTrace();
            }
        });

        // Layout
        VBox headerBox = new VBox(5, titleLabel, subtitleLabel);
        headerBox.setAlignment(Pos.CENTER);

        VBox formBox = new VBox(10, usernameLabel, usernameField, passwordLabel, passwordField);
        VBox mainContent = new VBox(20, headerBox, formBox, loginBtn, msgLabel);
        mainContent.setAlignment(Pos.CENTER);
        mainContent.setPadding(new Insets(30));
        mainContent.setStyle("-fx-background-color: #ECF0F1;");

        // Container với border
        StackPane container = new StackPane(mainContent);
        container.setPadding(new Insets(20));
        container.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-border-color: #BDC3C7; -fx-border-radius: 10;");

        BorderPane root = new BorderPane(container);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: linear-gradient(to bottom right, #3498DB, #2C3E50);");

        stage.setScene(new Scene(root, 500, 500));
        stage.setMinWidth(500);
        stage.setMinHeight(500);
        stage.show();
    }

    /* ===========================
       2️⃣ MÀN HÌNH NHẬP OTP - ĐƯỢC THIẾT KẾ LẠI
       =========================== */
    private void showOtpScene(String email) {
        Label titleLabel = new Label("XÁC THỰC OTP");
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 20));
        titleLabel.setTextFill(Color.web("#2C3E50"));

        Label emailLabel = new Label("Mã OTP đã được gửi tới:");
        emailLabel.setStyle("-fx-font-weight: bold;");
        Label emailValueLabel = new Label(email);
        emailValueLabel.setStyle("-fx-text-fill: #E74C3C; -fx-font-weight: bold;");

        TextField otpField = new TextField();
        otpField.setPromptText("Nhập mã OTP 6 chữ số");
        otpField.setStyle("-fx-pref-height: 40px; -fx-font-size: 16px; -fx-alignment: center; -fx-background-radius: 5;");
        otpField.setMaxWidth(200);

        Label msgLabel = new Label();
        msgLabel.setWrapText(true);

        Button verifyBtn = new Button("XÁC THỰC OTP");
        verifyBtn.setStyle("-fx-background-color: #27AE60; -fx-text-fill: white; -fx-font-weight: bold; -fx-pref-height: 40px; -fx-pref-width: 200px; -fx-background-radius: 5;");
        verifyBtn.setOnAction(e -> {
            try {
                Map<String, String> body = new HashMap<>();
                body.put("email", email);
                body.put("otp", otpField.getText());

                String response = postJson(BASE_URL + "/auth/customer/verify-otp", mapToJson(body));

                if (response.contains("\"ok\":true")) {
                    msgLabel.setText("✅ Xác thực thành công! Đang chuyển hướng...");
                    msgLabel.setTextFill(Color.GREEN);
                    showLicenseScene(email);
                } else {
                    msgLabel.setText("❌ OTP không hợp lệ hoặc đã hết hạn");
                    msgLabel.setTextFill(Color.RED);
                }
            } catch (Exception ex) {
                msgLabel.setText("⚠️ Lỗi kết nối đến server!");
                msgLabel.setTextFill(Color.ORANGE);
                ex.printStackTrace();
            }
        });

        Button backBtn = new Button("Quay lại");
        backBtn.setStyle("-fx-background-color: #95A5A6; -fx-text-fill: white; -fx-pref-height: 35px; -fx-background-radius: 5;");
        backBtn.setOnAction(e -> showLoginScene());

        VBox emailBox = new VBox(5, emailLabel, emailValueLabel);
        emailBox.setAlignment(Pos.CENTER);

        VBox contentBox = new VBox(20, titleLabel, emailBox, otpField, verifyBtn, backBtn, msgLabel);
        contentBox.setAlignment(Pos.CENTER);
        contentBox.setPadding(new Insets(30));
        contentBox.setStyle("-fx-background-color: #ECF0F1;");

        StackPane container = new StackPane(contentBox);
        container.setPadding(new Insets(20));
        container.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-border-color: #BDC3C7; -fx-border-radius: 10;");

        BorderPane root = new BorderPane(container);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: linear-gradient(to bottom right, #3498DB, #2C3E50);");

        stage.setScene(new Scene(root, 500, 500));
    }

    /* ===========================
       3️⃣ MÀN HÌNH KIỂM TRA LICENSE - ĐƯỢC THIẾT KẾ LẠI
       =========================== */
    private void showLicenseScene(String email) {
        Label titleLabel = new Label("KIỂM TRA LICENSE");
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 20));
        titleLabel.setTextFill(Color.web("#2C3E50"));

        Label emailLabel = new Label("Email đăng nhập:");
        emailLabel.setStyle("-fx-font-weight: bold;");
        Label emailValueLabel = new Label(email);
        emailValueLabel.setStyle("-fx-text-fill: #2980B9; -fx-font-weight: bold;");

        Label keyLabel = new Label("License Key:");
        keyLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #2C3E50;");
        TextField keyField = new TextField();
        keyField.setPromptText("Nhập license key của bạn");
        keyField.setStyle("-fx-pref-height: 40px; -fx-background-radius: 5;");

        Label resultLabel = new Label();
        resultLabel.setWrapText(true);

        Button checkBtn = new Button("KIỂM TRA LICENSE");
        checkBtn.setStyle("-fx-background-color: #E67E22; -fx-text-fill: white; -fx-font-weight: bold; -fx-pref-height: 40px; -fx-pref-width: 220px; -fx-background-radius: 5;");
        checkBtn.setOnAction(e -> {
            String key = keyField.getText();
            if (key == null || key.isBlank()) {
                resultLabel.setText("⚠️ Vui lòng nhập license key!");
                resultLabel.setTextFill(Color.ORANGE);
                return;
            }
            try {
                Map<String, String> body = new HashMap<>();
                body.put("key", key);
                body.put("email", email);

                String response = postJson(BASE_URL + "/api/license/verify", mapToJson(body));

                if (response.contains("\"ok\":true")) {
                    String expired = extractJsonField(response, "expiredAt");
                    String customerName = extractJsonField(response, "customerName");
                    String productName = extractJsonField(response, "productName");

                    resultLabel.setText("✅ License hợp lệ!");
                    resultLabel.setTextFill(Color.GREEN);
                    showLicenseInfoScene(customerName, productName, expired);
                } else {
                    String msg = extractJsonField(response, "message");
                    resultLabel.setText("❌ " + (msg.isEmpty() ? "Key không hợp lệ hoặc đã hết hạn" : msg));
                    resultLabel.setTextFill(Color.RED);
                }
            } catch (Exception ex) {
                resultLabel.setText("⚠️ Lỗi kết nối: " + ex.getMessage());
                resultLabel.setTextFill(Color.ORANGE);
                ex.printStackTrace();
            }
        });

        Button logoutBtn = new Button("Đăng xuất");
        logoutBtn.setStyle("-fx-background-color: #95A5A6; -fx-text-fill: white; -fx-pref-height: 35px; -fx-background-radius: 5;");
        logoutBtn.setOnAction(e -> showLoginScene());

        VBox emailBox = new VBox(5, emailLabel, emailValueLabel);
        emailBox.setAlignment(Pos.CENTER);

        VBox formBox = new VBox(10, keyLabel, keyField);
        VBox contentBox = new VBox(20, titleLabel, emailBox, formBox, checkBtn, logoutBtn, resultLabel);
        contentBox.setAlignment(Pos.CENTER);
        contentBox.setPadding(new Insets(30));
        contentBox.setStyle("-fx-background-color: #ECF0F1;");

        StackPane container = new StackPane(contentBox);
        container.setPadding(new Insets(20));
        container.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-border-color: #BDC3C7; -fx-border-radius: 10;");

        BorderPane root = new BorderPane(container);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: linear-gradient(to bottom right, #3498DB, #2C3E50);");

        stage.setScene(new Scene(root, 550, 550));
    }

    /* ===========================
       4️⃣ MÀN HÌNH THÔNG TIN LICENSE - ĐƯỢC THIẾT KẾ LẠI
       =========================== */
    private void showLicenseInfoScene(String customerName, String productName, String expiredAt) {
        Label title = new Label("🎉 KÍCH HOẠT THÀNH CÔNG!");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #27AE60;");

        VBox infoBox = new VBox(15);
        infoBox.setAlignment(Pos.CENTER_LEFT);
        infoBox.setStyle("-fx-background-color: #F8F9F9; -fx-padding: 20; -fx-background-radius: 10; -fx-border-color: #27AE60; -fx-border-radius: 10;");

        Label customerLabel = createInfoLabel("👤 Khách hàng: ", customerName);
        Label productLabel = createInfoLabel("📦 Sản phẩm: ", productName);
        Label expiredLabel = createInfoLabel("⏰ Ngày hết hạn: ", expiredAt);

        infoBox.getChildren().addAll(customerLabel, productLabel, expiredLabel);

        Button closeBtn = new Button("ĐÓNG ỨNG DỤNG");
        closeBtn.setStyle("-fx-background-color: #E74C3C; -fx-text-fill: white; -fx-font-weight: bold; -fx-pref-height: 40px; -fx-background-radius: 5;");
        closeBtn.setOnAction(e -> stage.close());

        Button backBtn = new Button("KIỂM TRA LICENSE KHÁC");
        backBtn.setStyle("-fx-background-color: #3498DB; -fx-text-fill: white; -fx-pref-height: 35px; -fx-background-radius: 5;");
        backBtn.setOnAction(e -> showLicenseScene(this.customerEmail));

        HBox buttonBox = new HBox(15, backBtn, closeBtn);
        buttonBox.setAlignment(Pos.CENTER);

        VBox contentBox = new VBox(25, title, infoBox, buttonBox);
        contentBox.setAlignment(Pos.CENTER);
        contentBox.setPadding(new Insets(30));
        contentBox.setStyle("-fx-background-color: #ECF0F1;");

        StackPane container = new StackPane(contentBox);
        container.setPadding(new Insets(20));
        container.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-border-color: #BDC3C7; -fx-border-radius: 10;");

        BorderPane root = new BorderPane(container);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: linear-gradient(to bottom right, #3498DB, #2C3E50);");

        stage.setScene(new Scene(root, 600, 500));
    }

    private Label createInfoLabel(String prefix, String value) {
        Label label = new Label(prefix + value);
        label.setStyle("-fx-font-size: 14px; -fx-text-fill: #2C3E50;");
        return label;
    }

    /* ===========================
       🛠️ HÀM TIỆN ÍCH (GIỮ NGUYÊN)
       =========================== */
    private static String mapToJson(Map<String, String> data) {
        StringBuilder sb = new StringBuilder("{");
        int i = 0;
        for (var e : data.entrySet()) {
            if (i++ > 0) sb.append(",");
            sb.append("\"").append(e.getKey()).append("\":\"")
                    .append(e.getValue().replace("\"", "\\\"")).append("\"");
        }
        sb.append("}");
        return sb.toString();
    }

    private static String postJson(String urlStr, String json) throws IOException {
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "application/json; utf-8");
        try (OutputStream os = conn.getOutputStream()) {
            os.write(json.getBytes(StandardCharsets.UTF_8));
        }
        int code = conn.getResponseCode();
        InputStream is = (code >= 200 && code < 400) ? conn.getInputStream() : conn.getErrorStream();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) sb.append(line);
            return sb.toString();
        }
    }

    private static String extractJsonField(String json, String field) {
        Pattern p = Pattern.compile("\"" + field + "\":\"(.*?)\"");
        Matcher m = p.matcher(json);
        return m.find() ? m.group(1) : "";
    }

    public static void main(String[] args) {
        launch(args);
    }
}