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
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AIChatService {

    private final String apiKey;
    private final String openaiUrl;
    private final HttpClient httpClient;
    private final ObjectMapper mapper;

    private final ProductRepository productRepo;
    private final OrderDetailRepository orderRepo;
    private final ReviewRepository reviewRepo;

    // Cache for product data
    private final Map<String, List<Product>> productCache = new ConcurrentHashMap<>();
    private long lastCacheUpdate = 0;
    private static final long CACHE_DURATION = 300000; // 5 minutes

    // Extended keyword mappings - FIXED: Use proper initialization
    private final Map<String, String[]> keywordPatterns;

    // Product keyword mappings
    private final Map<String, String> productKeywords;

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

        // Initialize keyword patterns properly
        this.keywordPatterns = initializeKeywordPatterns();
        this.productKeywords = initializeProductKeywords();
    }

    private Map<String, String[]> initializeKeywordPatterns() {
        Map<String, String[]> patterns = new HashMap<>();
        patterns.put("shop_info", new String[]{
                "bán gì", "bán cái gì", "shop bán gì", "shop mình bán gì", "mình bán gì",
                "cửa hàng bán gì", "bán những gì", "bán những", "có gì", "sản phẩm gì",
                "dịch vụ gì", "offer gì", "cung cấp gì", "kinh doanh gì"
        });
        patterns.put("price", new String[]{
                "giá", "bao nhiêu", "mắc", "rẻ", "price", "cost", "chi phí", "phí",
                "đắt", "giá cả", "bao nhiều tiền", "tốn bao nhiêu", "hết bao nhiêu", "bao nhiêu tiền"
        });
        patterns.put("description", new String[]{
                "là gì", "mô tả", "giới thiệu", "dùng làm gì", "công dụng", "tính năng",
                "chức năng", "dùng để làm gì", "có gì", "thông tin", "miêu tả", "công năng"
        });
        patterns.put("popular", new String[]{
                "bán chạy", "mua nhiều", "best seller", "phổ biến", "nổi tiếng",
                "hot", "trending", "được mua nhiều", "ưa chuộng", "top bán chạy"
        });
        patterns.put("rating", new String[]{
                "đánh giá cao", "tốt nhất", "rating cao", "sao", "review tốt",
                "được đánh giá", "chất lượng", "uy tín", "đánh giá sao"
        });
        patterns.put("discount", new String[]{
                "giảm giá", "khuyến mãi", "sale", "discount", "promotion",
                "ưu đãi", "deal", "hot deal", "khuyến mại"
        });
        patterns.put("support", new String[]{
                "hỗ trợ", "giúp đỡ", "tư vấn", "support", "help",
                "cần giúp", "trợ giúp", "hướng dẫn", "tư vấn giúp"
        });
        patterns.put("contact", new String[]{
                "liên hệ", "contact", "address", "địa chỉ", "số điện thoại",
                "email", "zalo", "facebook", "fanpage", "phone", "sdt"
        });
        patterns.put("delivery", new String[]{
                "giao hàng", "vận chuyển", "ship", "delivery", "thời gian giao",
                "phí ship", "phí vận chuyển", "cod", "ship cod", "giao mã"
        });
        patterns.put("payment", new String[]{
                "thanh toán", "payment", "momo", "chuyển khoản", "ví điện tử",
                "thẻ", "cash", "tiền mặt", "banking", "ngân hàng"
        });
        patterns.put("warranty", new String[]{
                "bảo hành", "warranty", "đổi trả", "hoàn tiền", "refund",
                "bảo đảm", "cam kết", "chính hãng", "authentic"
        });
        patterns.put("usage", new String[]{
                "cài đặt", "sử dụng", "activate", "kích hoạt", "hướng dẫn sử dụng",
                "cách dùng", "tutorial", "manual", "hướng dẫn cài đặt"
        });
        return patterns;
    }

    private Map<String, String> initializeProductKeywords() {
        Map<String, String> keywords = new HashMap<>();
        keywords.put("office", "microsoft office word excel powerpoint");
        keywords.put("windows", "windows 10 11 pro home");
        keywords.put("photoshop", "adobe photoshop cc");
        keywords.put("adobe", "adobe creative cloud premiere illustrator");
        keywords.put("matlab", "matlab mathworks");
        keywords.put("steam", "steam wallet game");
        keywords.put("kaspersky", "kaspersky antivirus");
        keywords.put("norton", "norton security");
        keywords.put("autocad", "autocad design");
        keywords.put("visual studio", "visual studio code");
        return keywords;
    }

    public String chat(String message) {
        if (message == null || message.trim().isEmpty()) {
            return "Xin chào! Tôi có thể giúp gì cho bạn?";
        }

        String lower = normalize(message);

        // ==============================
        // 🏪 Thông tin cửa hàng
        // ==============================
        if (containsAny(lower, keywordPatterns.get("shop_info"))) {
            return "🛍️ **LicenseShop - Cửa hàng License Bản Quyền Chính Hãng**\n\n" +
                    "Chuyên cung cấp các loại license bản quyền chất lượng cao:\n\n" +
                    "🎓 **HỌC TẬP & VĂN PHÒNG**\n" +
                    "• Microsoft Office 365, 2021, 2019\n" +
                    "• Windows 11/10/8.1 Pro\n" +
                    "• Adobe Creative Cloud (Photoshop, Premiere, Illustrator)\n" +
                    "• MATLAB, SPSS, EndNote\n\n" +
                    "🎮 **GIẢI TRÍ & CÔNG NGHỆ**\n" +
                    "• Game keys (Steam, Origin, Epic Games)\n" +
                    "• Antivirus (Kaspersky, Norton, Bitdefender)\n" +
                    "• Cloud Storage (Google Drive, Dropbox)\n\n" +
                    "💼 **CHUYÊN NGHIỆP**\n" +
                    "• Windows Server, SQL Server\n" +
                    "• Visual Studio, JetBrains IDE\n" +
                    "• AutoCAD, SketchUp, Revit\n\n" +
                    "💡 **Hỗ trợ kích hoạt 24/7 - Cam kết chính hãng 100%**";
        }

        // ==============================
        // 📞 Thông tin liên hệ
        // ==============================
        if (containsAny(lower, keywordPatterns.get("contact"))) {
            return "📞 **Thông tin liên hệ LicenseShop:**\n\n" +
                    "• 📞 Hotline: 1900 636 969\n" +
                    "• 📧 Email: support@licenseshop.vn\n" +
                    "• 🔵 Facebook: fb.com/LicenseShopVietnam\n" +
                    "• 📱 Zalo: 0944 334 455\n" +
                    "• 🌐 Website: licenseshop.vn\n" +
                    "• 🏢 Địa chỉ: 456 Trần Não, Quận 2, TP.HCM\n\n" +
                    "⏰ Thời gian làm việc: 7:00 - 23:00 hàng ngày";
        }

        // ==============================
        // 🚚 Giao hàng & Thanh toán
        // ==============================
        if (containsAny(lower, keywordPatterns.get("delivery"))) {
            return "🚚 **Chính sách giao hàng:**\n\n" +
                    "• 📧 Giao license qua email ngay sau khi thanh toán\n" +
                    "• ⚡ Nhận key ngay sau khi thanh toán\n" +

                    "• 🔒 Bảo mật thông tin khách hàng tuyệt đối\n" +
                    "• 📞 Thông báo ngay khi có sự cố";
        }

        if (containsAny(lower, keywordPatterns.get("payment"))) {
            return "💳 **Phương thức thanh toán:**\n\n" +
                    "• 🏦 Chuyển khoản ngân hàng (VCB, VIB, MB)\n" +
                    "• 📱 Ví điện tử (Momo, ZaloPay, VNPay)\n" +
                    "• 💳 Thẻ quốc tế (Visa, Mastercard)\n" +
                    "• 🔐 Internet Banking\n" +
                    "• 💰 COD (Nhận key xong thanh toán)";
        }

        // ==============================
        // 🔧 Bảo hành & Hỗ trợ
        // ==============================
        if (containsAny(lower, keywordPatterns.get("warranty"))) {
            return "🔒 **Chính sách bảo hành:**\n\n" +
                    "• ✅ Cam kết license chính hãng 100%\n" +
                    "• 🔄 Bảo hành uy tín\n" +
                    "• 🆘 Hỗ trợ kích hoạt 24/7\n" +
                    "• 💰 Hoàn tiền 100% nếu không kích hoạt được\n" +
                    "• 📞 Tư vấn kỹ thuật miễn phí";
        }

        if (containsAny(lower, keywordPatterns.get("usage"))) {
            return "🛠️ **Hướng dẫn sử dụng:**\n\n" +
                    "1. 🛒 Mua license phù hợp với nhu cầu\n" +
                    "2. 📧 Nhận key bản quyền qua email\n" +
                    "3. 💻 Mở phần mềm cần kích hoạt\n" +
                    "4. 🔑 Nhập key license khi được yêu cầu\n" +
                    "5. ✅ Hoàn tất kích hoạt và sử dụng\n\n" +
                    "📞 Cần hỗ trợ kích hoạt? Gọi ngay 0987890JQK";
        }


        // ==============================
        // 1️⃣ Tìm sản phẩm trong DB
        // ==============================
        List<Product> allProducts = getCachedProducts();

        // Tìm sản phẩm trực tiếp theo tên
        Product exactMatch = findExactProductMatch(lower, allProducts);
        if (exactMatch != null) {
            return buildProductResponse(exactMatch, lower);
        }

        // Tìm sản phẩm theo từ khóa mở rộng
        Product keywordMatch = findProductByExtendedKeywords(lower, allProducts);
        if (keywordMatch != null) {
            return buildProductResponse(keywordMatch, lower);
        }

        // ==============================
        // 2️⃣ Sản phẩm bán chạy / đánh giá cao
        // ==============================
        if (containsAny(lower, keywordPatterns.get("popular"))) {
            List<Object[]> top = orderRepo.findTopSellingProducts();
            if (top == null || top.isEmpty()) return "Hiện chưa có dữ liệu sản phẩm bán chạy.";

            StringBuilder sb = new StringBuilder("🔥 **Top sản phẩm bán chạy tại LicenseShop:**\n\n");
            int rank = 1;
            for (Object[] o : top) {
                if (rank > 5) break;
                String name = String.valueOf(o[0]);
                String count = String.valueOf(o[1]);
                sb.append(rank).append(". ").append(name)
                        .append(" - ").append(count).append(" lượt mua\n");
                rank++;
            }
            sb.append("\n💡 Gõ tên sản phẩm để xem chi tiết!");
            return sb.toString();
        }

        if (containsAny(lower, keywordPatterns.get("rating"))) {
            List<Object[]> rated = reviewRepo.findTopRatedProducts();
            if (rated == null || rated.isEmpty()) return "Hiện chưa có đánh giá nào.";

            StringBuilder sb = new StringBuilder("⭐ **Sản phẩm được đánh giá cao nhất:**\n\n");
            int rank = 1;
            for (Object[] o : rated) {
                if (rank > 5) break;
                String name = String.valueOf(o[0]);
                Double rating = (o[1] instanceof Number) ? ((Number) o[1]).doubleValue() : 0.0;
                sb.append(rank).append(". ").append(name)
                        .append(" - ⭐ ").append(String.format(Locale.ROOT, "%.1f", rating))
                        .append("/5\n");
                rank++;
            }
            return sb.toString();
        }

        // ==============================
        // 3️⃣ Tìm theo keyword trong DB
        // ==============================
        List<Product> found = productRepo.searchByKeyword(lower);
        if (found != null && !found.isEmpty()) {
            StringBuilder sb = new StringBuilder("🔎 **Tìm thấy " + found.size() + " sản phẩm liên quan:**\n\n");
            for (int i = 0; i < Math.min(found.size(), 5); i++) {
                Product p = found.get(i);
                List<Variant> vs = p.getVariants() == null ? List.of() : p.getVariants();
                sb.append("• ").append(p.getName())
                        .append(" - ")
                        .append(vs.isEmpty() ? "Liên hệ" : fmtVnd(minPrice(vs)))
                        .append("\n");
            }
            if (found.size() > 5) {
                sb.append("\n💡 Và ").append(found.size() - 5).append(" sản phẩm khác...");
            }
            sb.append("\n📝 Gõ tên chính xác sản phẩm để xem chi tiết!");
            return sb.toString();
        }

        // ==============================
        // 4️⃣ Hỗ trợ
        // ==============================
        if (containsAny(lower, keywordPatterns.get("support"))) {
            return "🆘 **Trung tâm hỗ trợ LicenseShop:**\n\n" +
                    "Chúng tôi có thể giúp gì cho bạn?\n\n" +
                    "• 🛒 Tư vấn sản phẩm phù hợp\n" +
                    "• 🔑 Hỗ trợ kích hoạt license\n" +
                    "• 💳 Hướng dẫn thanh toán\n" +
                    "• 🚚 Thông tin giao hàng\n" +
                    "• 🔧 Sửa lỗi kỹ thuật\n\n" +
                    "📞 Gọi ngay 1900 636 969 để được hỗ trợ nhanh nhất!";
        }

        // ==============================
        // 5️⃣ Chào hỏi
        // ==============================
        if (containsAny(lower, "xin chào", "hello", "hi", "chào", "có ai không", "alo")) {
            return "👋 **Xin chào! Chào mừng bạn đến với LicenseShop!**\n\n" +
                    "Tôi có thể giúp bạn:\n" +
                    "• 🔎 Tìm kiếm license phù hợp\n" +
                    "• 💰 So sánh giá cả\n" +
                    "• ⭐ Xem sản phẩm bán chạy\n" +
                    "• 🆘 Hỗ trợ kỹ thuật\n\n" +
                    "💡 Hãy cho tôi biết bạn cần gì!";
        }

        // ==============================
        // 6️⃣ Không khớp → Gọi OpenAI
        // ==============================
        return callOpenAI(message);
    }

    // ============================
    // 🔧 Helpers
    // ============================
    private List<Product> getCachedProducts() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastCacheUpdate > CACHE_DURATION) {
            productCache.clear();
            List<Product> products = productRepo.findAll();
            productCache.put("all", products);
            lastCacheUpdate = currentTime;
            return products;
        }
        return productCache.getOrDefault("all", productRepo.findAll());
    }

    private Product findExactProductMatch(String query, List<Product> products) {
        for (Product p : products) {
            String productName = normalize(p.getName());
            if (query.contains(productName) || productName.contains(query)) {
                return p;
            }

            // Check for partial matches (first word)
            String[] productWords = productName.split(" ");
            if (productWords.length > 0 && query.contains(productWords[0])) {
                return p;
            }
        }
        return null;
    }

    private Product findProductByExtendedKeywords(String query, List<Product> products) {
        for (Map.Entry<String, String> entry : productKeywords.entrySet()) {
            if (query.contains(entry.getKey())) {
                for (String keyword : entry.getValue().split(" ")) {
                    for (Product p : products) {
                        if (normalize(p.getName()).contains(keyword)) {
                            return p;
                        }
                    }
                }
            }
        }
        return null;
    }

    private String buildProductResponse(Product product, String query) {
        boolean askPrice = containsAny(query, keywordPatterns.get("price"));
        boolean askDesc = containsAny(query, keywordPatterns.get("description"));
        List<Variant> variants = product.getVariants() == null ? List.of() : product.getVariants();

        StringBuilder reply = new StringBuilder("📦 **" + product.getName() + "**\n\n");

        if (askPrice) {
            if (variants.isEmpty()) {
                return reply.append("💰 **Giá:** Liên hệ 1900 636 969 để biết chi tiết").toString();
            }
            reply.append("💰 **Bảng giá:**\n");
            for (Variant v : variants) {
                reply.append("• ")
                        .append(buildVariantLabel(v))
                        .append(" — ")
                        .append(fmtVnd(v.getPrice()))
                        .append("\n");
            }
            reply.append("\n💡 **Khuyến mãi:** Giảm 10% cho đơn hàng đầu tiên!");
            return reply.toString();
        }

        if (askDesc) {
            reply.append("📜 **Mô tả:** ")
                    .append(product.getDescription() != null ? product.getDescription() : "Sản phẩm license bản quyền chính hãng.\n");
            if (!variants.isEmpty()) {
                reply.append("\n💰 **Giá từ:** ").append(fmtVnd(minPrice(variants)));
            }
            reply.append("\n\n🔒 **Cam kết:** Chính hãng 100% - Bảo hành trọn đời");
            return reply.toString();
        }

        // Default response
        if (!variants.isEmpty()) {
            reply.append("💰 **Giá từ:** ").append(fmtVnd(minPrice(variants))).append("\n");
        } else {
            reply.append("💰 **Giá:** Liên hệ 1900 636 969\n");
        }

        if (product.getDescription() != null) {
            reply.append("📜 ").append(product.getDescription()).append("\n");
        }

        reply.append("\n🔒 **Bảo hành:** Trọn đời sản phẩm");
        reply.append("\n⚡ **Giao key:** Ngay sau khi thanh toán");

        return reply.toString();
    }

    private String callOpenAI(String message) {
        Map<String, Object> payload = Map.of(
                "model", "gpt-4o-mini",
                "messages", List.of(
                        Map.of("role", "system", "content", "Bạn là trợ lý ảo của LicenseShop - cửa hàng license bản quyền. " +
                                "Giúp khách hàng tìm hiểu về các sản phẩm license Microsoft, Adobe, Windows, game keys, antivirus. " +
                                "Trả lời thân thiện, chuyên nghiệp. Giới thiệu về ưu đãi giảm 10% cho đơn đầu tiên. " +
                                "Hotline: 1900 636 969"),
                        Map.of("role", "user", "content", message)
                ),
                "max_tokens", 500,
                "temperature", 0.7
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
                return "⚠️ Hiện tại hệ thống đang bận. Vui lòng liên hệ hotline 1900 636 969 để được hỗ trợ nhanh nhất!";
            }

            Map<String, Object> response = mapper.readValue(resp.body(), new TypeReference<>() {});
            List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
            if (choices != null && !choices.isEmpty()) {
                Map<String, Object> msgObj = (Map<String, Object>) choices.get(0).get("message");
                Object content = msgObj != null ? msgObj.get("content") : null;
                return content != null ? content.toString() : "Xin lỗi, tôi chưa hiểu câu hỏi. Vui lòng liên hệ 1900 636 969!";
            }
        } catch (Exception e) {
            return "🔧 Hiện tại tôi không thể kết nối. Vui lòng:\n\n" +
                    "• 📞 Gọi hotline: 1900 636 969\n" +
                    "• 📧 Email: support@licenseshop.vn\n" +
                    "• 🔵 Nhắn tin qua Facebook\n\n" +
                    "Chúng tôi sẽ hỗ trợ bạn ngay!";
        }

        return "🤔 Tôi chưa hiểu rõ câu hỏi của bạn. Bạn có thể:\n\n" +
                "• 🔎 Gõ tên sản phẩm cụ thể (Office, Windows, Photoshop...)\n" +
                "• 💰 Hỏi về giá cả, khuyến mãi\n" +
                "• 🆘 Yêu cầu hỗ trợ kỹ thuật\n" +
                "• 📞 Gọi 1900 636 969 để được tư vấn ngay!";
    }

    private static boolean containsAny(String text, String... keys) {
        if (text == null || keys == null) return false;
        for (String k : keys) {
            if (k != null && text.contains(k)) return true;
        }
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
        return "Gói cơ bản";
    }
}