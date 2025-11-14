package anbd.he191271.service;

import anbd.he191271.dto.IntentAnalysis;
import anbd.he191271.entity.Product;
import anbd.he191271.entity.Variant;
import anbd.he191271.entity.Review;
import anbd.he191271.repository.ProductRepository;
import anbd.he191271.repository.ReviewRepository;
import anbd.he191271.service.AIChatService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AIChatServiceImpl implements AIChatService {

    private final ProductRepository productRepository;
    private final ReviewRepository reviewRepository;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    // Gemini API Configuration
    @Value("${gemini.api.key}")
    private String geminiApiKey;

    @Value("${gemini.api.url}")
    private String geminiApiUrl;

    @Value("${gemini.api.model}")
    private String geminiModel;

    @Value("${gemini.generation.temperature}")
    private double temperature;

    @Value("${gemini.generation.max-output-tokens}")
    private int maxOutputTokens;

    @Value("${gemini.api.timeout:30000}")
    private int timeout;

    // Cache
    private final Map<String, List<Product>> productCache = new ConcurrentHashMap<>();
    private long lastCacheUpdate = 0;
    private static final long CACHE_DURATION = 300000;

    // System prompt cho AI
    private static final String SYSTEM_PROMPT = """
        Bạn là Mia - trợ lý bán hàng thân thiện của LicenseShop, chuyên cung cấp license bản quyền phần mềm.
        
        VAI TRÒ:
        - Tư vấn sản phẩm license phù hợp với nhu cầu khách hàng
        - Cung cấp thông tin giá cả, tính năng sản phẩm
        - Hỗ trợ thông tin về cửa hàng, chính sách
        - Cung cấp thông tin đánh giá, xếp hạng sản phẩm từ người dùng
        - Luôn thân thiện, nhiệt tình, chuyên nghiệp
        - Được chào thì phải chào lại
        
        QUY TẮC:
        - CHỈ trả lời các câu hỏi liên quan đến LicenseShop và sản phẩm license
        - KHÔNG trả lời các câu hỏi không liên quan, thay vào đó chuyển hướng về sản phẩm
        - Luôn sử dụng tiếng Việt tự nhiên, thân thiện
        - Khi có thông tin sản phẩm từ database, hãy sử dụng chính xác thông tin đó
        - Khi có thông tin đánh giá, hãy sử dụng để tư vấn khách hàng
        - Khi không có sản phẩm phù hợp, gợi ý sản phẩm tương tự
        
        THÔNG TIN ĐÁNH GIÁ:
        - Khi khách hỏi về đánh giá, cung cấp thông tin:
          + Đánh giá tốt nhất (rating cao nhất)
          + Đánh giá tệ nhất (rating thấp nhất) 
          + Sản phẩm được đánh giá nhiều nhất
        
        ĐỊNH DẠNG TRẢ LỜI:
        - Sử dụng emoji để sinh động
        - Cấu trúc rõ ràng, dễ đọc
        - Luôn có call-to-action rõ ràng
        - Khi có đánh giá: hiển thị ⭐ rating và số lượng review
        """;

    @Override
    public String chat(String message) {
        log.info("Processing chat message: {}", message);

        if (message == null || message.trim().isEmpty()) {
            return generateGreetingResponse();
        }

        try {
            // Kiểm tra câu hỏi về đánh giá
            if (isReviewRelated(message)) {
                return handleReviewRequest(message);
            }

            // 1. Phân tích intent và query database
            IntentAnalysis intent = analyzeIntent(message);
            List<Product> relevantProducts = findRelevantProducts(intent, message);

            // 2. Tạo context từ database results
            String context = buildEnhancedContext(relevantProducts, intent, message);

            // 3. Gọi Gemini với context HOẶC fallback thông minh
            String aiResponse = handleAIResponse(message, context, relevantProducts);

            return aiResponse;

        } catch (Exception e) {
            log.error("Error in AI chat service", e);
            return fallbackResponse(message);
        }
    }

    // ==================== REVIEW HANDLING ====================

    private boolean isReviewRelated(String message) {
        String normalized = normalize(message);

        Pattern reviewPattern = Pattern.compile(
                "(?i)\\b(đánh giá|review|rating|sao|bình luận|nhận xét|" +
                        "tốt nhất|tệ nhất|tồi nhất|hay nhất|chất lượng|" +
                        "được đánh giá|nhiều đánh giá|ít đánh giá)\\b"
        );

        return reviewPattern.matcher(normalized).find();
    }

    private String handleReviewRequest(String message) {
        String normalized = normalize(message);

        try {
            if (normalized.contains("tốt nhất") || normalized.contains("rating cao") || normalized.contains("hay nhất")) {
                return buildBestRatedProductsResponse();
            } else if (normalized.contains("tệ nhất") || normalized.contains("tồi nhất") || normalized.contains("rating thấp")) {
                return buildWorstRatedProductsResponse();
            } else if (normalized.contains("nhiều đánh giá") || normalized.contains("được đánh giá")) {
                return buildMostReviewedProductsResponse();
            } else {
                return buildCompleteReviewResponse();
            }

        } catch (Exception e) {
            log.error("Error handling review request", e);
            return "❌ Hiện không thể lấy thông tin đánh giá. Vui lòng thử lại sau.";
        }
    }

    private String buildBestRatedProductsResponse() {
        List<Object[]> topRatedData = reviewRepository.findTopRatedProducts();

        if (topRatedData.isEmpty()) {
            return "⭐ **Chưa có đánh giá nào cho sản phẩm**\n\nHãy là người đầu tiên đánh giá sản phẩm của chúng tôi!";
        }

        StringBuilder response = new StringBuilder();
        response.append("🏆 **TOP SẢN PHẨM ĐƯỢC ĐÁNH GIÁ TỐT NHẤT** ⭐\n\n");

        for (int i = 0; i < Math.min(5, topRatedData.size()); i++) {
            Object[] data = topRatedData.get(i);
            String productName = (String) data[0];
            Double avgRating = (Double) data[1];

            // Tìm product để lấy giá
            List<Product> products = productRepository.searchByKeyword(productName);
            String priceInfo = "Liên hệ";
            if (!products.isEmpty()) {
                Product product = products.get(0);
                priceInfo = "Từ " + formatPrice(getMinPrice(product.getVariants()));
            }

            response.append(i + 1).append(". **").append(productName).append("**\n");
            response.append("   ⭐ ").append(String.format("%.1f", avgRating));
            response.append(" • 💰 ").append(priceInfo).append("\n\n");
        }

        response.append("💡 **Dựa trên đánh giá thực tế từ khách hàng!**");
        return response.toString();
    }

    private String buildWorstRatedProductsResponse() {
        List<Product> allProducts = getCachedProducts();
        List<ProductStats> worstRated = new ArrayList<>();

        for (Product product : allProducts) {
            List<Review> reviews = reviewRepository.findByProduct_IdAndStatus(product.getId(), "ACTIVE");
            if (!reviews.isEmpty()) {
                double avgRating = reviews.stream()
                        .mapToInt(Review::getRating)
                        .average()
                        .orElse(0.0);

                if (avgRating > 0 && avgRating < 3.0) {
                    worstRated.add(new ProductStats(product, avgRating, reviews.size()));
                }
            }
        }

        if (worstRated.isEmpty()) {
            return "😊 **Tất cả sản phẩm đều có đánh giá tích cực!**";
        }

        worstRated.sort(Comparator.comparingDouble(ProductStats::getAverageRating));

        StringBuilder response = new StringBuilder();
        response.append("⚠️ **SẢN PHẨM CẦN CẢI THIỆN**\n\n");

        for (int i = 0; i < Math.min(5, worstRated.size()); i++) {
            ProductStats stats = worstRated.get(i);
            Product product = stats.getProduct();
            response.append(i + 1).append(". **").append(product.getName()).append("**\n");
            response.append("   ⭐ ").append(String.format("%.1f", stats.getAverageRating()));
            response.append(" • ").append(stats.getReviewCount()).append(" đánh giá\n");
            response.append("   💰 ").append(formatPrice(getMinPrice(product.getVariants()))).append("\n\n");
        }

        response.append("📞 **Chúng tôi đang nỗ lực cải thiện chất lượng!**");
        return response.toString();
    }

    private String buildMostReviewedProductsResponse() {
        List<Product> allProducts = getCachedProducts();
        List<ProductStats> mostReviewed = new ArrayList<>();

        for (Product product : allProducts) {
            List<Review> reviews = reviewRepository.findByProduct_IdAndStatus(product.getId(), "ACTIVE");
            if (!reviews.isEmpty()) {
                double avgRating = reviews.stream()
                        .mapToInt(Review::getRating)
                        .average()
                        .orElse(0.0);
                mostReviewed.add(new ProductStats(product, avgRating, reviews.size()));
            }
        }

        if (mostReviewed.isEmpty()) {
            return "📝 **Chưa có sản phẩm nào được đánh giá**";
        }

        mostReviewed.sort((p1, p2) -> Integer.compare(p2.getReviewCount(), p1.getReviewCount()));

        StringBuilder response = new StringBuilder();
        response.append("🗣️ **SẢN PHẨM ĐƯỢC ĐÁNH GIÁ NHIỀU NHẤT**\n\n");

        for (int i = 0; i < Math.min(5, mostReviewed.size()); i++) {
            ProductStats stats = mostReviewed.get(i);
            Product product = stats.getProduct();
            response.append(i + 1).append(". **").append(product.getName()).append("**\n");
            response.append("   📊 ").append(stats.getReviewCount()).append(" đánh giá");
            response.append(" • ⭐ ").append(String.format("%.1f", stats.getAverageRating())).append("\n");
            response.append("   💰 ").append(formatPrice(getMinPrice(product.getVariants()))).append("\n\n");
        }

        response.append("🎯 **Sản phẩm được nhiều khách hàng quan tâm!**");
        return response.toString();
    }

    private String buildCompleteReviewResponse() {
        StringBuilder response = new StringBuilder();
        response.append("📊 **THỐNG KÊ ĐÁNH GIÁ SẢN PHẨM**\n\n");

        // Best Rated
        List<Object[]> topRated = reviewRepository.findTopRatedProducts();
        if (!topRated.isEmpty()) {
            response.append("🏆 **TOP ĐÁNH GIÁ CAO:**\n");
            for (int i = 0; i < Math.min(3, topRated.size()); i++) {
                Object[] data = topRated.get(i);
                response.append("• ").append(data[0])
                        .append(" - ⭐").append(String.format("%.1f", data[1])).append("\n");
            }
            response.append("\n");
        }

        // Most Reviewed
        List<Product> allProducts = getCachedProducts();
        Optional<Product> mostReviewed = allProducts.stream()
                .max(Comparator.comparingInt(p -> reviewRepository.findByProduct_IdAndStatus(p.getId(), "ACTIVE").size()));
        if (mostReviewed.isPresent()) {
            Product product = mostReviewed.get();
            int reviewCount = reviewRepository.findByProduct_IdAndStatus(product.getId(), "ACTIVE").size();
            response.append("🗣️ **ĐƯỢC REVIEW NHIỀU NHẤT:**\n");
            response.append("• ").append(product.getName())
                    .append(" - ").append(reviewCount).append(" reviews\n\n");
        }

        response.append("💡 **Cần thông tin chi tiết? Hãy hỏi cụ thể hơn!**");
        return response.toString();
    }

    // ==================== GEMINI CORE ====================

    private String handleAIResponse(String userMessage, String context, List<Product> products) {
        try {
            return callGeminiAI(userMessage, context);
        } catch (Exception e) {
            log.warn("Gemini API failed, using smart fallback response");
            return buildSmartFallbackResponse(userMessage, products);
        }
    }

    private String callGeminiAI(String userMessage, String context) {
        if (geminiApiKey == null || geminiApiKey.isEmpty()) {
            log.warn("Gemini API key not configured, using fallback");
            throw new RuntimeException("Gemini API key not configured");
        }

        log.info("🔑 Using API Key: {}... (length: {})",
                geminiApiKey.substring(0, Math.min(10, geminiApiKey.length())),
                geminiApiKey.length());

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        try {
            Map<String, Object> request = buildGeminiRequest(userMessage, context);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);
            String urlWithKey = geminiApiUrl + "?key=" + geminiApiKey;

            log.info("🔧 Calling Gemini API: {}", geminiApiUrl);

            ResponseEntity<Map> response = restTemplate.postForEntity(urlWithKey, entity, Map.class);
            log.info("✅ Gemini API call successful");
            return extractGeminiResponse(response.getBody());

        } catch (Exception e) {
            log.error("❌ Gemini API call failed: {}", e.getMessage());
            throw new RuntimeException("Gemini API call failed: " + e.getMessage());
        }
    }

    private Map<String, Object> buildGeminiRequest(String userMessage, String context) {
        Map<String, Object> request = new HashMap<>();

        String fullPrompt = SYSTEM_PROMPT + "\n\nCONTEXT FROM DATABASE:\n" + context +
                "\n\nUser: " + userMessage + "\n\nAssistant:";

        Map<String, Object> content = new HashMap<>();
        Map<String, Object> part = new HashMap<>();
        part.put("text", fullPrompt);

        content.put("parts", List.of(part));
        request.put("contents", List.of(content));

        Map<String, Object> generationConfig = new HashMap<>();
        generationConfig.put("temperature", temperature);
        generationConfig.put("maxOutputTokens", maxOutputTokens);
        generationConfig.put("topP", 0.8);
        generationConfig.put("topK", 40);

        request.put("generationConfig", generationConfig);

        Map<String, Object> safetySettings = new HashMap<>();
        safetySettings.put("category", "HARM_CATEGORY_DANGEROUS_CONTENT");
        safetySettings.put("threshold", "BLOCK_MEDIUM_AND_ABOVE");

        request.put("safetySettings", List.of(safetySettings));

        return request;
    }

    private String extractGeminiResponse(Map<String, Object> response) {
        try {
            if (response == null) {
                return "Không nhận được phản hồi từ AI.";
            }

            if (response.containsKey("error")) {
                Map<String, Object> error = (Map<String, Object>) response.get("error");
                String errorMsg = (String) error.get("message");
                log.error("Gemini API error: {}", errorMsg);
                return "Xin lỗi, hệ thống AI đang gặp sự cố. Vui lòng thử lại sau.";
            }

            List<Map<String, Object>> candidates = (List<Map<String, Object>>) response.get("candidates");
            if (candidates == null || candidates.isEmpty()) {
                return "Không có phản hồi từ AI.";
            }

            Map<String, Object> candidate = candidates.get(0);
            Map<String, Object> content = (Map<String, Object>) candidate.get("content");
            List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");

            if (parts != null && !parts.isEmpty()) {
                String text = (String) parts.get(0).get("text");
                if (text != null && text.startsWith("Assistant:")) {
                    text = text.substring("Assistant:".length()).trim();
                }
                return text;
            }

        } catch (Exception e) {
            log.error("Error extracting Gemini response", e);
        }
        return "Xin lỗi, tôi không thể tạo phản hồi lúc này.";
    }

    // ==================== SMART PRODUCT SEARCH ====================

    private IntentAnalysis analyzeIntent(String message) {
        IntentAnalysis intent = new IntentAnalysis();
        String normalized = normalize(message);

        intent.setProductRelated(true);
        intent.setSearchQueries(extractSearchQueries(normalized));
        intent.setMaxPrice(extractMaxPrice(normalized));
        intent.setCategory(extractCategory(normalized));

        log.info("🎯 Intent Analysis - Queries: {}, MaxPrice: {}, Category: {}",
                intent.getSearchQueries(), intent.getMaxPrice(), intent.getCategory());

        return intent;
    }

    private List<String> extractSearchQueries(String message) {
        String normalized = normalize(message);

        if (!isProductRelated(normalized)) {
            return Collections.emptyList();
        }

        Set<String> queries = new LinkedHashSet<>();

        String[][] productKeywordGroups = {
                {"microsoft office 365", "office 365", "microsoft office", "office"},
                {"grammarly premium", "grammarly"},
                {"khan academy plus", "khan academy", "coursera pro", "coursera"},
                {"spotify premium", "spotify", "netflix gift card", "netflix", "disney+ 1 năm", "disney+", "disney plus"},
                {"steam wallet 100k", "steam wallet", "steam"},
                {"slack pro", "slack", "zoom business", "zoom", "notion plus", "notion", "trello premium", "trello"},
                {"windows 11 pro key", "windows 11 home key", "windows 11", "windows 10 pro key", "windows 10 home key", "windows 10", "windows"},
                {"nordvpn 1 năm", "nordvpn", "expressvpn 6 tháng", "expressvpn", "surfshark vpn", "surfshark", "cyberghost vpn", "cyberghost", "vpn"}
        };

        boolean foundSpecificKeyword = false;

        for (String[] keywordGroup : productKeywordGroups) {
            for (String keyword : keywordGroup) {
                if (normalized.contains(keyword)) {
                    if (queries.add(keyword)) {
                        log.info("✅ Found specific keyword: '{}'", keyword);
                    }
                    foundSpecificKeyword = true;
                    break;
                }
            }
        }

        if (!foundSpecificKeyword) {
            String[] generalKeywords = {
                    "premium", "pro", "plus", "business", "key", "gift card",
                    "wallet", "vpn", "phần mềm", "license", "bản quyền"
            };

            for (String keyword : generalKeywords) {
                if (normalized.contains(keyword) && queries.size() < 2) {
                    queries.add(keyword);
                    log.info("✅ Added general keyword: '{}'", keyword);
                }
            }
        }

        if (queries.isEmpty()) {
            String[] words = normalized.split("\\s+");
            for (String word : words) {
                if (word.length() > 3 && isEnhancedProductWord(word) && queries.size() < 3) {
                    queries.add(word);
                    log.info("✅ Found product word: '{}'", word);
                }
            }
        }

        log.info("🔍 Final extracted queries: {}", queries);
        return new ArrayList<>(queries);
    }

    private boolean isEnhancedProductWord(String word) {
        if (word.length() < 3) return false;

        String[] enhancedProductWords = {
                "microsoft", "office", "grammarly", "khan", "coursera", "spotify",
                "netflix", "steam", "disney", "slack", "zoom", "notion", "trello",
                "windows", "nordvpn", "expressvpn", "surfshark", "cyberghost",
                "premium", "pro", "plus", "business", "key", "wallet", "vpn",
                "gift", "card", "license", "home", "pro", "year", "tháng",
                "academy", "streaming", "music", "video", "game", "tool", "security"
        };

        for (String productWord : enhancedProductWords) {
            if (productWord.equalsIgnoreCase(word) ||
                    word.contains(productWord) ||
                    productWord.contains(word)) {
                return true;
            }
        }
        return false;
    }

    private List<Product> findRelevantProducts(IntentAnalysis intent, String originalMessage) {
        List<Product> products = new ArrayList<>();
        Set<String> processedProductNames = new HashSet<>();

        if (intent.getSearchQueries() != null && !intent.getSearchQueries().isEmpty()) {
            for (String query : intent.getSearchQueries()) {
                log.info("Searching products by keyword: '{}'", query);
                List<Product> keywordResults = productRepository.searchByKeyword(query);
                log.info("Found {} products for query: '{}'", keywordResults.size(), query);

                for (Product product : keywordResults) {
                    if (product != null && product.getName() != null) {
                        if (processedProductNames.add(product.getName().toLowerCase())) {
                            products.add(product);
                        }
                    }
                }
            }
        }

        if (products.isEmpty() && intent.getCategory() != null) {
            log.info("Searching products by category: '{}'", intent.getCategory());
            List<Product> categoryResults = productRepository.searchByKeyword(intent.getCategory());
            for (Product product : categoryResults) {
                if (product != null && product.getName() != null) {
                    if (processedProductNames.add(product.getName().toLowerCase())) {
                        products.add(product);
                    }
                }
            }
        }

        if (products.isEmpty()) {
            log.info("Falling back to semantic search for: '{}'", originalMessage);
            List<Product> semanticResults = findProductsBySemanticSearch(originalMessage);
            for (Product product : semanticResults) {
                if (product != null && product.getName() != null) {
                    if (processedProductNames.add(product.getName().toLowerCase())) {
                        products.add(product);
                    }
                }
            }
        }

        if (products.isEmpty() && isMeaningfulProductQuery(originalMessage)) {
            log.info("Returning popular products as fallback");
            List<Product> popularResults = getPopularProducts();
            for (Product product : popularResults) {
                if (product != null && product.getName() != null) {
                    if (processedProductNames.add(product.getName().toLowerCase())) {
                        products.add(product);
                    }
                }
            }
        }

        if (intent.getMaxPrice() != null) {
            products = products.stream()
                    .filter(p -> p != null && hasAffordableVariant(p, intent.getMaxPrice()))
                    .collect(Collectors.toList());
        }

        List<Product> finalProducts = products.stream()
                .filter(p -> p != null && "available".equals(p.getStatus()))
                .distinct()
                .limit(10)
                .collect(Collectors.toList());

        log.info("🎯 Final products found: {}", finalProducts.size());
        return finalProducts;
    }

    private boolean isMeaningfulProductQuery(String message) {
        String normalized = normalize(message);

        Pattern meaninglessPattern = Pattern.compile(
                "(?i).*(có gì|gì dưới|sản phẩm nào|mặt hàng|món nào|đồ nào|tìm gì|kiếm gì).*"
        );

        Pattern meaningfulPattern = Pattern.compile(
                "(?i).*(office|windows|photoshop|steam|game|microsoft|adobe|phần mềm|license|bản quyền|word|excel|powerpoint).*"
        );

        return !meaninglessPattern.matcher(normalized).matches() &&
                meaningfulPattern.matcher(normalized).matches();
    }

    private List<Product> findProductsBySemanticSearch(String message) {
        List<Product> allProducts = getCachedProducts();
        List<Product> matches = new ArrayList<>();

        for (Product product : allProducts) {
            if (calculateRelevanceScore(product, message) > 0.1) {
                matches.add(product);
            }
        }

        matches.sort((p1, p2) ->
                Double.compare(calculateRelevanceScore(p2, message),
                        calculateRelevanceScore(p1, message)));

        return matches.stream().limit(10).collect(Collectors.toList());
    }

    private double calculateRelevanceScore(Product product, String query) {
        if (product == null) return 0.0;

        String productText = (product.getName() + " " +
                (product.getDescription() != null ? product.getDescription() : "") +
                (product.getCategory() != null ? " " + product.getCategory().getName() : ""))
                .toLowerCase();

        String queryLower = query.toLowerCase();

        double score = 0.0;
        String[] importantWords = extractImportantWords(queryLower);

        for (String word : importantWords) {
            if (productText.contains(word)) {
                score += 1.0;
            }
        }

        if (productText.contains("office") && queryLower.contains("office")) score += 2.0;
        if (productText.contains("windows") && queryLower.contains("windows")) score += 2.0;
        if (productText.contains("photoshop") && queryLower.contains("photoshop")) score += 2.0;
        if (productText.contains("steam") && queryLower.contains("steam")) score += 2.0;
        if (productText.contains("game") && queryLower.contains("game")) score += 2.0;

        return importantWords.length > 0 ? score / importantWords.length : 0.0;
    }

    private String[] extractImportantWords(String query) {
        String cleaned = query.replaceAll(
                        "(?i)\\b(tôi|mình|muốn|mua|cần|giá|bao nhiêu|bạn|có|không|gì|nào|ạ|ơi|à|để|làm|cho)\\b", "")
                .replaceAll("\\s+", " ")
                .trim();

        return cleaned.isEmpty() ? new String[0] : cleaned.split("\\s+");
    }

    private boolean hasAffordableVariant(Product product, BigDecimal maxPrice) {
        if (product == null || product.getVariants() == null || product.getVariants().isEmpty()) {
            return true;
        }

        return product.getVariants().stream()
                .anyMatch(v -> v != null && BigDecimal.valueOf(v.getPrice()).compareTo(maxPrice) <= 0);
    }

    private List<Product> getPopularProducts() {
        return getCachedProducts().stream()
                .filter(p -> "available".equals(p.getStatus()))
                .limit(5)
                .collect(Collectors.toList());
    }

    // ==================== SMART FALLBACK RESPONSES ====================

    private String buildSmartFallbackResponse(String userMessage, List<Product> products) {
        if (products != null && !products.isEmpty()) {
            return buildFallbackProductResponse(products);
        }

        String normalized = normalize(userMessage);

        if (normalized.contains("có gì") || normalized.contains("gì dưới") || normalized.contains("sản phẩm nào")) {
            return buildNoProductsFoundResponse(userMessage);
        } else if (isProductRelated(normalized)) {
            return buildProductSuggestionResponse(userMessage);
        } else {
            return getDefaultShopResponse();
        }
    }

    private String buildNoProductsFoundResponse(String userMessage) {
        String normalized = normalize(userMessage);

        if (normalized.contains("video") || normalized.contains("làm video")) {
            return "🎬 **Hiện chưa có sản phẩm phần mềm làm video**\n\n" +
                    "Tuy nhiên, chúng tôi có các sản phẩm Adobe Creative Cloud phù hợp:\n\n" +
                    "• **Adobe Photoshop** - Chỉnh sửa ảnh chuyên nghiệp\n" +
                    "• **Adobe Premiere** - Dựng video chuyên nghiệp\n\n" +
                    "💡 **Liên hệ hotline 1900 636 969 để được tư vấn thêm!**";
        }

        if (normalized.contains("diệt virus") || normalized.contains("antivirus") || normalized.contains("bảo mật")) {
            return "🛡️ **Hiện chưa có sản phẩm diệt virus**\n\n" +
                    "Tuy nhiên, chúng tôi có các giải pháp bảo mật khác:\n\n" +
                    "• **VPN services** - Bảo vệ truy cập internet\n" +
                    "• **Password managers** - Quản lý mật khẩu an toàn\n\n" +
                    "💡 **Liên hệ hotline 1900 636 969 để được tư vấn giải pháp bảo mật!**";
        }

        if (normalized.contains("dưới") && (normalized.contains("500") || normalized.contains("500k"))) {
            return "💰 **Các sản phẩm dưới 500k:**\n\n" +
                    "• **Microsoft Office 365** - 1 tháng: 50.000đ\n" +
                    "• **Grammarly Premium** - 1 tháng: 50.000đ\n" +
                    "• **Spotify Premium** - 1 tháng: 50.000đ\n" +
                    "• **Steam Wallet 100k** - 1 tháng: 50.000đ\n\n" +
                    "💡 **Tất cả đều có gói 1 tháng với giá 50.000đ!**";
        }

        return "🔍 **Không tìm thấy sản phẩm phù hợp với yêu cầu**\n\n" +
                "Nhưng chúng tôi có các sản phẩm nổi bật:\n\n" +
                "• **Microsoft Office 365** - Soạn thảo văn bản\n" +
                "• **Steam Wallet** - Nạp game Steam\n" +
                "• **Spotify Premium** - Nghe nhạc không quảng cáo\n\n" +
                "💡 **Gõ tên sản phẩm cụ thể hoặc liên hệ 1900 636 969**";
    }

    private String buildProductSuggestionResponse(String userMessage) {
        String normalized = normalize(userMessage);

        if (normalized.contains("office") || normalized.contains("word") || normalized.contains("excel")) {
            return "📊 **Microsoft Office 365**\n\n" +
                    "💰 **Giá:** Từ 50.000đ (1 tháng) đến 500.000đ (1 năm)\n\n" +
                    "✅ Bao gồm: Word, Excel, PowerPoint, Outlook\n" +
                    "⚡ Giao key ngay sau thanh toán\n" +
                    "📞 **Đặt mua: 1900 636 969**";
        }

        if (normalized.contains("game") || normalized.contains("steam")) {
            return "🎮 **Steam Wallet Code**\n\n" +
                    "💰 **Mệnh giá:** 50.000đ, 100.000đ, 200.000đ, 500.000đ\n\n" +
                    "✅ Nạp tiền vào tài khoản Steam\n" +
                    "⚡ Giao code ngay tức thì\n" +
                    "📞 **Đặt mua: 1900 636 969**";
        }

        return "🎯 **Dựa trên nhu cầu của bạn, tôi gợi ý:**\n\n" +
                "📊 **Microsoft Office** - Soạn thảo văn bản, bảng tính\n" +
                "🎨 **Adobe Creative Cloud** - Thiết kế đồ họa, chỉnh sửa ảnh\n" +
                "🎮 **Steam Wallet** - Nạp tiền chơi game\n" +
                "🎵 **Spotify Premium** - Nghe nhạc trực tuyến\n\n" +
                "💡 **Hãy cho tôi biết bạn cần sản phẩm cụ thể nào!**\n" +
                "📞 **Hỗ trợ nhanh: 1900 636 969**";
    }

    private String buildFallbackProductResponse(List<Product> products) {
        if (products == null || products.isEmpty()) {
            return getDefaultShopResponse();
        }

        if (products.size() == 1) {
            Product product = products.get(0);
            String productName = product.getName() != null ? product.getName() : "Sản phẩm";
            String description = product.getDescription() != null ? "📝 " + product.getDescription() + "\n\n" : "";

            String priceInfo = "Liên hệ";
            if (product.getVariants() != null && !product.getVariants().isEmpty()) {
                int minPrice = getMinPrice(product.getVariants());
                priceInfo = "Từ " + formatPrice(minPrice);
            }

            return "📦 **" + productName + "**\n\n" +
                    description +
                    "💰 **Giá:** " + priceInfo + "\n\n" +
                    "✅ Chính hãng 100% • ⚡ Giao key ngay • 📞 1900 636 969";
        } else {
            StringBuilder response = new StringBuilder();
            response.append("🔍 **Tìm thấy ").append(products.size()).append(" sản phẩm phù hợp:**\n\n");

            for (Product p : products) {
                if (p != null) {
                    response.append("• **").append(p.getName() != null ? p.getName() : "Sản phẩm").append("**");
                    if (p.getVariants() != null && !p.getVariants().isEmpty()) {
                        int minPrice = getMinPrice(p.getVariants());
                        response.append(" — Từ ").append(formatPrice(minPrice));
                    }
                    response.append("\n");
                }
            }

            response.append("\n💡 **Gõ tên sản phẩm để xem chi tiết!**");
            response.append("\n📞 **Hỗ trợ nhanh: 1900 636 969**");
            return response.toString();
        }
    }

    // ==================== ENHANCED CONTEXT BUILDER ====================

    private String buildEnhancedContext(List<Product> products, IntentAnalysis intent, String originalMessage) {
        StringBuilder context = new StringBuilder();

        if (products != null && !products.isEmpty()) {
            context.append("TẤT CẢ SẢN PHẨM TÌM THẤY TRONG DATABASE (").append(products.size()).append(" sản phẩm):\n\n");
            for (int i = 0; i < products.size(); i++) {
                Product product = products.get(i);
                if (product != null) {
                    context.append("【Sản phẩm ").append(i + 1).append("】\n");
                    context.append(buildEnhancedProductContext(product)).append("\n---\n");
                }
            }
        } else {
            context.append("KHÔNG CÓ SẢN PHẨM PHÙ HỢP TRONG DATABASE.\n");
        }

        context.append("\nINTENT PHÂN TÍCH:\n");
        context.append("- Product Related: ").append(intent.isProductRelated()).append("\n");
        context.append("- Search Queries: ").append(intent.getSearchQueries()).append("\n");
        context.append("- Max Price: ").append(intent.getMaxPrice()).append("\n");
        context.append("- Category: ").append(intent.getCategory()).append("\n");

        log.info("📋 Enhanced context built with {} products", products != null ? products.size() : 0);
        return context.toString();
    }

    private String buildEnhancedProductContext(Product product) {
        if (product == null) {
            return "Sản phẩm không tồn tại";
        }

        StringBuilder context = new StringBuilder();

        context.append("Tên: ").append(product.getName() != null ? product.getName() : "N/A").append("\n");

        if (product.getDescription() != null) {
            context.append("Mô tả: ").append(product.getDescription()).append("\n");
        }

        if (product.getCategory() != null && product.getCategory().getName() != null) {
            context.append("Danh mục: ").append(product.getCategory().getName()).append("\n");
        }

        List<Review> reviews = reviewRepository.findByProduct_IdAndStatus(product.getId(), "ACTIVE");
        if (!reviews.isEmpty()) {
            double avgRating = reviews.stream()
                    .mapToInt(Review::getRating)
                    .average()
                    .orElse(0.0);
            context.append("Đánh giá: ⭐ ").append(String.format("%.1f", avgRating))
                    .append(" (").append(reviews.size()).append(" reviews)\n");
        } else {
            context.append("Đánh giá: Chưa có đánh giá\n");
        }

        if (product.getVariants() != null && !product.getVariants().isEmpty()) {
            context.append("Các phiên bản:\n");
            for (Variant variant : product.getVariants()) {
                if (variant != null) {
                    context.append("  - ").append(buildVariantLabel(variant))
                            .append(": ").append(formatPrice(variant.getPrice())).append("\n");
                }
            }
        }

        context.append("Trạng thái: ").append(product.getStatus() != null ? product.getStatus() : "N/A");

        return context.toString();
    }

    // ==================== FALLBACK & UTILITIES ====================

    private String fallbackResponse(String originalMessage) {
        if (isReviewRelated(originalMessage)) {
            return handleReviewRequest(originalMessage);
        }

        IntentAnalysis intent = analyzeIntent(originalMessage);
        List<Product> products = findRelevantProducts(intent, originalMessage);

        if (products != null && !products.isEmpty()) {
            return buildFallbackProductResponse(products);
        }

        return getDefaultShopResponse();
    }

    // ==================== HELPER METHODS ====================

    private boolean isProductRelated(String message) {
        if (message == null || message.trim().isEmpty()) {
            return false;
        }

        String normalized = normalize(message);

        Pattern productPattern = Pattern.compile(
                "(?i)\\b(office|windows|photoshop|steam|game|microsoft|adobe|" +
                        "license|bản quyền|grammarly|spotify|netflix|matlab|autocad|" +
                        "visual studio|antivirus|phần mềm|software|mua|giá|cần|tìm|" +
                        "định mua|muốn mua|tư vấn|hỏi về)\\b"
        );

        boolean hasProduct = productPattern.matcher(normalized).find();

        log.info("🎯 Product detection - HasProduct: {}, Message: '{}'", hasProduct, normalized);

        return hasProduct;
    }

    private BigDecimal extractMaxPrice(String message) {
        Pattern pattern = Pattern.compile("(dưới|dưới khoảng|dưới tầm|khoảng|tầm|giá)\\s*(\\d+)(\\s*(triệu|tr|k|nghìn))?");
        Matcher matcher = pattern.matcher(message);

        if (matcher.find()) {
            try {
                BigDecimal amount = new BigDecimal(matcher.group(2));
                String unit = matcher.group(4);

                if (unit != null && (unit.contains("triệu") || unit.contains("tr"))) {
                    return amount.multiply(BigDecimal.valueOf(1000000));
                } else if (unit != null && unit.equals("k")) {
                    return amount.multiply(BigDecimal.valueOf(1000));
                }
                return amount;
            } catch (Exception e) {
                log.warn("Price extraction failed for: {}", message);
            }
        }
        return null;
    }

    private String extractCategory(String message) {
        if (message.contains("office") || message.contains("word") || message.contains("excel") || message.contains("powerpoint") || message.contains("soạn thảo") || message.contains("văn bản"))
            return "Office";
        if (message.contains("windows") || message.contains("win")) return "Windows";
        if (message.contains("adobe") || message.contains("photoshop") || message.contains("premiere") || message.contains("illustrator") || message.contains("chỉnh sửa") || message.contains("ảnh") || message.contains("video") || message.contains("thiết kế"))
            return "Design";
        if (message.contains("game") || message.contains("steam")) return "Game";
        if (message.contains("antivirus") || message.contains("bảo mật") || message.contains("diệt virus")) return "Security";
        if (message.contains("matlab") || message.contains("autocad") || message.contains("visual studio")) return "Professional";
        return null;
    }

    private String generateGreetingResponse() {
        return "👋 **Xin chào! Chào mừng đến với LicenseShop!**\n\n" +
                "Tôi là Mia - trợ lý AI của LicenseShop. Tôi có thể giúp bạn:\n\n" +
                "🔎 Tìm license phù hợp với nhu cầu\n" +
                "💰 So sánh giá cả các phiên bản\n" +
                "⭐ Tư vấn sản phẩm đánh giá tốt\n" +
                "📊 Cung cấp thông tin đánh giá sản phẩm\n" +
                "🆘 Hỗ trợ thông tin cửa hàng\n\n" +
                "💡 **Hãy cho tôi biết bạn cần gì!**\n" +
                "📞 **Hỗ trợ nhanh: 1900 636 969**";
    }

    private String getDefaultShopResponse() {
        return "🤖 **Xin lỗi, tôi chỉ có thể hỗ trợ các câu hỏi về LicenseShop**\n\n" +
                "Tôi có thể giúp bạn tìm kiếm và tư vấn về:\n\n" +
                "🛍️ **Sản phẩm license:** Microsoft, Adobe, Windows, Game keys...\n" +
                "📊 **Đánh giá sản phẩm:** Sản phẩm tốt nhất, được review nhiều...\n" +
                "🏪 **Thông tin cửa hàng:** Liên hệ, giao hàng, thanh toán\n\n" +
                "💡 **Gợi ý:** Hãy hỏi về 'Office 2021', 'Windows 11', 'đánh giá sản phẩm'...\n" +
                "📞 **Hỗ trợ nhanh: 1900 636 969**";
    }

    // ==================== UTILITY METHODS ====================

    private List<Product> getCachedProducts() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastCacheUpdate > CACHE_DURATION) {
            productCache.clear();
            List<Product> products = productRepository.findAll();
            productCache.put("all", products);
            lastCacheUpdate = currentTime;
            return products;
        }
        return productCache.getOrDefault("all", productRepository.findAll());
    }

    private static String normalize(String text) {
        return text == null ? "" : text.toLowerCase()
                .replaceAll("[^a-z0-9áàảãạăắằẳẵặâấầẩẫậéèẻẽẹêếềểễệíìỉĩịóòỏõọôốồổỗộơớờởỡợúùủũụưứừửữựýỳỷỹỵđ ]", "")
                .trim();
    }

    private static String formatPrice(int price) {
        NumberFormat nf = NumberFormat.getInstance(new Locale("vi", "VN"));
        nf.setMaximumFractionDigits(0);
        return nf.format(price) + " đ";
    }

    private static int getMinPrice(List<Variant> variants) {
        if (variants == null || variants.isEmpty()) {
            return 0;
        }
        return variants.stream()
                .mapToInt(Variant::getPrice)
                .min()
                .orElse(0);
    }

    private static String buildVariantLabel(Variant v) {
        if (v == null) return "Gói cơ bản";

        String name = v.getName() != null ? v.getName().trim() : "";
        String dur = v.getDuration() != null ? v.getDuration().trim() : "";

        if (!name.isEmpty() && !dur.isEmpty()) return name + " (" + dur + ")";
        if (!name.isEmpty()) return name;
        if (!dur.isEmpty()) return dur;
        return "Gói cơ bản";
    }

    // ==================== INNER CLASS CHO PRODUCT STATS ====================

    private static class ProductStats {
        private final Product product;
        private final double averageRating;
        private final int reviewCount;

        public ProductStats(Product product, double averageRating, int reviewCount) {
            this.product = product;
            this.averageRating = averageRating;
            this.reviewCount = reviewCount;
        }

        public Product getProduct() { return product; }
        public double getAverageRating() { return averageRating; }
        public int getReviewCount() { return reviewCount; }
    }
}