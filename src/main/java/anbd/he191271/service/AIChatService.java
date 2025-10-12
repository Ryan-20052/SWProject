package anbd.he191271.service;

import anbd.he191271.entity.Product;
import anbd.he191271.entity.Variant;
import anbd.he191271.repository.ProductRepository;
import anbd.he191271.repository.OrderDetailRepository;
import anbd.he191271.repository.ReviewRepository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.text.NumberFormat;
import java.time.Duration;
import java.util.*;

@Service
public class AIChatService {

    private final String apiKey;
    private final String openaiUrl;
    private final HttpClient httpClient;
    private final ObjectMapper mapper;

    private final ProductRepository productRepo;
    private final OrderDetailRepository orderRepo;
    private final ReviewRepository reviewRepo;

    @Autowired
    public AIChatService(
            @Value("${openai.api.key}") String apiKey,
            @Value("${openai.url:https://api.openai.com/v1/chat/completions}") String openaiUrl,
            ProductRepository productRepo,
            OrderDetailRepository orderRepo,
            ReviewRepository reviewRepo
    ) {
        this.apiKey = apiKey;
        this.openaiUrl = openaiUrl;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(15))
                .build();
        this.mapper = new ObjectMapper();

        this.productRepo = productRepo;
        this.orderRepo = orderRepo;
        this.reviewRepo = reviewRepo;
    }

    public String chat(String message) {
        String lower = normalize(message);
        if (containsAny(lower,
                "bán gì", "bán cái gì", "shop bán gì", "shop mình bán gì",
                "mình bán gì", "cửa hàng bán gì", "bán những gì", "bán những")
        ) {
            return "Shop mình bán license bạn nhé, tất tần tật mọi thể loại từ học tập, giải trí,...";
        }
        // =============================
        // 1️⃣ Xử lý sản phẩm trong DB
        // =============================
        List<Product> allProducts = productRepo.findAll();

        for (Product p : allProducts) {
            String productName = normalize(p.getName());

            // kiểm tra chứa từ khóa linh hoạt
            if (lower.contains(productName)
                    || productName.contains(lower)
                    || lower.contains(p.getName().toLowerCase())
                    || p.getName().toLowerCase().contains(lower)
                    || lower.matches(".*\\b" + productName.split(" ")[0] + "\\b.*")) {
                boolean askPrice = containsAny(lower, "giá", "bao nhiêu", "mắc", "rẻ", "price", "cost");
                boolean askDesc  = containsAny(lower, "là gì", "mô tả", "giới thiệu", "dùng làm gì");


                List<Variant> variants = p.getVariants() == null ? List.of() : p.getVariants();
                StringBuilder reply = new StringBuilder("📦 **" + p.getName() + "**\n");

                if (askPrice) {
                    if (variants.isEmpty()) {
                        return reply.append("💰 Giá: Liên hệ để biết chi tiết.").toString();
                    }
                    reply.append("💰 **Bảng giá:**\n");
                    for (Variant v : variants) {
                        reply.append("• ")
                                .append(buildVariantLabel(v))
                                .append(" — ")
                                .append(fmtVnd(v.getPrice()))
                                .append("\n");
                    }
                    return reply.toString();
                }


                if (askDesc) {
                    reply.append("📜 Mô tả: ")
                            .append(p.getDescription() != null ? p.getDescription() : "Hiện chưa có mô tả.\n");
                    if (!variants.isEmpty()) {
                        reply.append("💰 Giá từ ").append(fmtVnd(minPrice(variants))).append(".");
                    }
                    return reply.toString();
                }

                // nếu chỉ nhắc tên sản phẩm
                if (!variants.isEmpty()) {
                    reply.append("💰 Giá từ ").append(fmtVnd(minPrice(variants))).append(".\n");
                } else {
                    reply.append("💰 Giá: Liên hệ.\n");
                }
                if (p.getDescription() != null) {
                    reply.append("📜 ").append(p.getDescription());
                }
                return reply.toString();
            }
        }

        // =============================
        // 2️⃣ Bán chạy & đánh giá cao
        // =============================
        if (containsAny(lower, "bán chạy", "mua nhiều", "best seller")) {
            var top = orderRepo.findTopSellingProducts();
            if (top == null || top.isEmpty()) return "Hiện chưa có dữ liệu sản phẩm bán chạy.";
            StringBuilder sb = new StringBuilder("🔥 **Top sản phẩm bán chạy:**\n");
            top.stream().limit(5).forEach(o ->
                    sb.append("- ").append(o[0]).append(" (").append(o[1]).append(" lượt mua)\n"));
            return sb.toString();
        }

        if (containsAny(lower, "đánh giá cao", "tốt nhất", "rating cao")) {
            var rated = reviewRepo.findTopRatedProducts();
            if (rated == null || rated.isEmpty()) return "Hiện chưa có đánh giá nào.";
            StringBuilder sb = new StringBuilder("⭐ **Sản phẩm được đánh giá cao:**\n");
            rated.stream().limit(5).forEach(o ->
                    sb.append("- ").append(o[0])
                            .append(": ⭐ ").append(String.format(Locale.ROOT, "%.2f", o[1])).append("\n"));
            return sb.toString();
        }

        // =============================
        // 3️⃣ Tìm sản phẩm theo từ khóa
        // =============================
        var found = productRepo.searchByKeyword(lower);
        if (found != null && !found.isEmpty()) {
            StringBuilder sb = new StringBuilder("🔎 **Một số sản phẩm liên quan:**\n");
            found.stream().limit(5).forEach(p ->
                    sb.append("- ").append(p.getName())
                            .append(" — ")
                            .append(p.getVariants().isEmpty() ? "Liên hệ"
                                    : fmtVnd(p.getVariants().get(0).getPrice()))
                            .append("\n"));
            return sb.toString();
        }

        // =============================
        // 4️⃣ Không khớp → gọi OpenAI
        // =============================
        Map<String, Object> payload = Map.of(
                "model", "gpt-4o-mini",
                "messages", List.of(
                        Map.of("role", "system", "content", "Bạn là trợ lý ảo của ShineShop, giúp khách hàng tra cứu sản phẩm."),
                        Map.of("role", "user", "content", message)
                ),
                "max_tokens", 400
        );

        try {
            String json = mapper.writeValueAsString(payload);
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(openaiUrl))
                    .timeout(Duration.ofSeconds(30))
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() != 200) {
                return "⚠️ Lỗi OpenAI: " + resp.statusCode() + " — " + resp.body();
            }

            Map<String, Object> response = mapper.readValue(resp.body(), new TypeReference<>() {});
            List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
            if (choices != null && !choices.isEmpty()) {
                Map<String, Object> msgObj = (Map<String, Object>) choices.get(0).get("message");
                Object content = msgObj != null ? msgObj.get("content") : null;
                return content != null ? content.toString() : "OpenAI không trả về nội dung.";
            }
        } catch (Exception e) {
            return "🚫 Không thể kết nối tới OpenAI: " + e.getMessage();
        }

        return "🤔 Xin lỗi, tôi chưa hiểu câu hỏi của bạn.";
    }

    // ============================
    // 🔧 Helpers
    // ============================
    private static boolean containsAny(String text, String... keys) {
        for (String k : keys) if (text.contains(k)) return true;
        return false;
    }

    private static String normalize(String text) {
        return text == null ? "" : text
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9áàảãạăắằẳẵặâấầẩẫậéèẻẽẹêếềểễệíìỉĩịóòỏõọôốồổỗộơớờởỡợúùủũụưứừửữựýỳỷỹỵđ ]", "")
                .trim();
    }

    private static int minPrice(List<Variant> vs) {
        return vs.stream().mapToInt(Variant::getPrice).min().orElse(0);
    }

    private static String fmtVnd(int price) {
        NumberFormat nf = NumberFormat.getInstance(new Locale("vi", "VN"));
        nf.setMaximumFractionDigits(0);
        return nf.format(price) + " đ";
    }

    private static String buildVariantLabel(Variant v) {
        String name = v.getName() != null ? v.getName().trim() : "";
        String dur  = v.getDuration() != null ? v.getDuration().trim() : "";
        if (!name.isEmpty() && !dur.isEmpty()) return name + " (" + dur + ")";
        if (!name.isEmpty()) return name;
        if (!dur.isEmpty()) return dur;
        return "Gói";
    }
}
