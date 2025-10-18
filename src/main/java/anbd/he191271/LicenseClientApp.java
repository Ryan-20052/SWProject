package anbd.he191271;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
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
    private String customerEmail; // lưu email sau khi login

    @Override
    public void start(Stage primaryStage) {
        this.stage = primaryStage;
        primaryStage.setTitle("SHINE SHOP Desktop");
        showLoginScene();
    }

    /* ===========================
       1️⃣ MÀN HÌNH ĐĂNG NHẬP
       =========================== */
    private void showLoginScene() {
        TextField usernameField = new TextField();
        usernameField.setPromptText("Tên đăng nhập");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Mật khẩu");

        Label msgLabel = new Label();

        Button loginBtn = new Button("Đăng nhập");
        loginBtn.setOnAction(e -> {
            try {
                Map<String, String> body = new HashMap<>();
                body.put("username", usernameField.getText());
                body.put("password", passwordField.getText());

                String response = postJson(BASE_URL + "/auth/customer/app-login", mapToJson(body));

                if (response.contains("\"ok\":true")) {
                    msgLabel.setText("OTP đã gửi tới email của bạn!");
                    String email = extractJsonField(response, "email");
                    this.customerEmail = email;
                    showOtpScene(email);
                } else {
                    String msg = extractJsonField(response, "message");
                    msgLabel.setText("❌ " + (msg.isEmpty() ? "Đăng nhập thất bại" : msg));
                }
            } catch (Exception ex) {
                msgLabel.setText("Lỗi kết nối backend!");
                ex.printStackTrace();
            }
        });

        VBox root = new VBox(10,
                new Label("Đăng nhập tài khoản Customer:"),
                usernameField, passwordField, loginBtn, msgLabel);
        root.setPadding(new Insets(15));
        stage.setScene(new Scene(root, 400, 220));
        stage.show();
    }

    /* ===========================
       2️⃣ MÀN HÌNH NHẬP OTP
       =========================== */
    private void showOtpScene(String email) {
        Label emailLabel = new Label("Mã OTP đã gửi tới: " + email);
        TextField otpField = new TextField();
        otpField.setPromptText("Nhập mã OTP (6 chữ số)");
        Label msgLabel = new Label();

        Button verifyBtn = new Button("Xác thực OTP");
        verifyBtn.setOnAction(e -> {
            try {
                Map<String, String> body = new HashMap<>();
                body.put("email", email);
                body.put("otp", otpField.getText());

                String response = postJson(BASE_URL + "/auth/customer/verify-otp", mapToJson(body));

                if (response.contains("\"ok\":true")) {
                    msgLabel.setText("✅ OTP hợp lệ! Đang chuyển sang phần License...");
                    showLicenseScene(email);
                } else {
                    msgLabel.setText("❌ OTP không hợp lệ hoặc đã hết hạn");
                }
            } catch (Exception ex) {
                msgLabel.setText("Lỗi kết nối backend!");
                ex.printStackTrace();
            }
        });

        VBox root = new VBox(10, emailLabel, otpField, verifyBtn, msgLabel);
        root.setPadding(new Insets(15));
        stage.setScene(new Scene(root, 400, 200));
        stage.show();
    }

    /* ===========================
       3️⃣ MÀN HÌNH KIỂM TRA LICENSE
       =========================== */
    private void showLicenseScene(String email) {
        Label emailLabel = new Label("Email: " + email);
        TextField keyField = new TextField();
        keyField.setPromptText("Nhập license key (plain)");
        Label resultLabel = new Label();

        Button checkBtn = new Button("Kiểm tra License");
        checkBtn.setOnAction(e -> {
            String key = keyField.getText();
            if (key == null || key.isBlank()) {
                resultLabel.setText("⚠️ Vui lòng nhập key!");
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

                    resultLabel.setText("✅ License hợp lệ — hết hạn: " + expired);
                    showLicenseInfoScene(customerName, productName, expired);
                } else {
                    String msg = extractJsonField(response, "message");
                    resultLabel.setText("❌ Không hợp lệ: " + (msg.isEmpty() ? "Key sai hoặc hết hạn" : msg));
                }
            } catch (Exception ex) {
                resultLabel.setText("Lỗi: " + ex.getMessage());
                ex.printStackTrace();
            }
        });

        VBox root = new VBox(10, emailLabel,
                new Label("License key:"), keyField, checkBtn, resultLabel);
        root.setPadding(new Insets(15));
        stage.setScene(new Scene(root, 420, 220));
        stage.show();
    }

    /* ===========================
       💡 HÀM TIỆN ÍCH
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

    private void showLicenseInfoScene(String customerName, String productName, String expiredAt) {
        Label title = new Label("🎉 Kích hoạt thành công!");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: green;");

        Label customerLabel = new Label("Khách hàng: " + customerName);
        Label productLabel = new Label("Sản phẩm: " + productName);
        Label expiredLabel = new Label("Ngày hết hạn: " + expiredAt);

        Button closeBtn = new Button("Đóng");
        closeBtn.setOnAction(e -> stage.close());

        VBox root = new VBox(15, title, customerLabel, productLabel, expiredLabel, closeBtn);
        root.setPadding(new Insets(20));
        root.setAlignment(Pos.CENTER);

        stage.setScene(new Scene(root, 400, 250));
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}