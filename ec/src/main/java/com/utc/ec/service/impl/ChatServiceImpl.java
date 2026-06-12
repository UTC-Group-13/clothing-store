package com.utc.ec.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.utc.ec.dto.chat.ChatRequest;
import com.utc.ec.dto.chat.ChatResponse;
import com.utc.ec.dto.chat.ProductSuggestionDTO;
import com.utc.ec.entity.Category;
import com.utc.ec.entity.Product;
import com.utc.ec.entity.ProductVariant;
import com.utc.ec.repository.CategoryRepository;
import com.utc.ec.repository.ProductRepository;
import com.utc.ec.repository.ProductVariantRepository;
import com.utc.ec.repository.spec.ProductSpecification;
import com.utc.ec.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductVariantRepository variantRepository;
    private final ObjectMapper objectMapper;

    // ─── GitHub Models config (primary) ────────────────────────────────────────
    @Value("${ai.provider:github}")
    private String aiProvider;

    @Value("${ai.api.key:}")
    private String aiApiKey;

    @Value("${ai.api.model:gpt-4o-mini}")
    private String aiModel;

    @Value("${ai.api.max-tokens:1024}")
    private int aiMaxTokens;

    // ─── DeepSeek config (fallback after GitHub) ──────────────────────────────────
    @Value("${deepseek.api.url:https://api.ai-box.vn}")
    private String deepseekApiUrl;

    @Value("${deepseek.api.key:}")
    private String deepseekApiKey;

    @Value("${deepseek.api.model:deepseek-chat}")
    private String deepseekModel;

    @Value("${deepseek.api.max-tokens:1024}")
    private int deepseekMaxTokens;

    // ─── Claude config (fallback) ───────────────────────────────────────────────
    @Value("${claude.api.key:}")
    private String claudeApiKey;

    @Value("${claude.api.model:claude-3-haiku-20240307}")
    private String claudeModel;

    @Value("${claude.api.max-tokens:1024}")
    private int claudeMaxTokens;

    // ─── Constants ──────────────────────────────────────────────────────────────
    private static final String GITHUB_MODELS_URL  = "https://models.inference.ai.azure.com/chat/completions";
    private static final String DEEPSEEK_COMPLETIONS_PATH = "/v1/chat/completions";
    private static final String CLAUDE_API_URL      = "https://api.anthropic.com/v1/messages";
    private static final String ANTHROPIC_VERSION   = "2023-06-01";
    private static final int    MAX_HISTORY          = 20;
    private static final int    MAX_CONTEXT_PRODUCTS = 8;
    private static final int    MAX_SUGGESTIONS      = 5;
    private static final long   SESSION_TTL_MS       = 2 * 60 * 60 * 1000L; // 2 giờ
    private static final long   QUOTA_RETRY_MS       = 60 * 60 * 1000L;      // 1 giờ

    // ─── Circuit breaker ────────────────────────────────────────────────────────
    private volatile boolean quotaExhausted   = false;
    private volatile long    quotaExhaustedAt = 0L;

    // ─── Session storage (in-memory) ───────────────────────────────────────────
    private static class SessionData {
        final List<Map<String, String>> messages = new ArrayList<>();
        long lastAccessMs = System.currentTimeMillis();
    }

    private final ConcurrentHashMap<String, SessionData> sessions = new ConcurrentHashMap<>();

    // ─── Public API ─────────────────────────────────────────────────────────────

    @Override
    public ChatResponse chat(ChatRequest request, String username) {
        cleanupOldSessions();

        // 1. Lấy hoặc tạo session
        String sessionId = resolveSessionId(request.getSessionId());
        SessionData session = sessions.computeIfAbsent(sessionId, k -> new SessionData());
        session.lastAccessMs = System.currentTimeMillis();

        // 2. Tìm sản phẩm liên quan từ DB
        List<Product> relevantProducts = searchRelevantProducts(request.getMessage(), request.getProductId());

        // 3. Build các map hỗ trợ
        Map<Integer, String> categoryMap  = buildCategoryMap(relevantProducts);
        Map<Integer, String> thumbnailMap = buildThumbnailMap(relevantProducts);

        // 4. Thêm tin nhắn user vào lịch sử
        session.messages.add(Map.of("role", "user", "content", request.getMessage()));

        // 5. Build system prompt với context sản phẩm
        String systemPrompt = buildSystemPrompt(relevantProducts, categoryMap, username);

        // 6. Gọi AI API (GitHub Models → Claude fallback)
        String aiMessage = callAI(systemPrompt, session.messages);

        // 7. Lưu phản hồi AI vào lịch sử
        session.messages.add(Map.of("role", "assistant", "content", aiMessage));
        trimHistory(session.messages);

        // 8. Build danh sách gợi ý sản phẩm
        List<ProductSuggestionDTO> suggestions = buildSuggestions(relevantProducts, categoryMap, thumbnailMap);

        return ChatResponse.builder()
                .message(aiMessage)
                .sessionId(sessionId)
                .suggestions(suggestions)
                .build();
    }

    @Override
    public void clearSession(String sessionId) {
        sessions.remove(sessionId);
        log.debug("Session cleared: {}", sessionId);
    }

    // ─── Session Helpers ────────────────────────────────────────────────────────

    private String resolveSessionId(String requested) {
        if (requested != null && !requested.isBlank() && sessions.containsKey(requested)) {
            return requested;
        }
        return UUID.randomUUID().toString();
    }

    private void trimHistory(List<Map<String, String>> messages) {
        while (messages.size() > MAX_HISTORY) {
            messages.removeFirst();
        }
    }

    private void cleanupOldSessions() {
        long now = System.currentTimeMillis();
        sessions.entrySet().removeIf(e -> now - e.getValue().lastAccessMs > SESSION_TTL_MS);
    }

    // ─── Product Search ─────────────────────────────────────────────────────────

    /** Stopwords tiếng Việt — bỏ qua khi trích keyword sản phẩm */
    private static final Set<String> STOPWORDS = new HashSet<>(Arrays.asList(
            "tôi", "toi", "mình", "minh", "bạn", "ban", "cần", "can", "muốn", "muon",
            "tìm", "tim", "kiếm", "kiem", "mua", "xem", "cho", "có", "co", "không", "khong",
            "một", "mot", "cái", "cai", "chiếc", "chiec", "đôi", "doi", "bộ", "bo",
            "giá", "gia", "trên", "tren", "dưới", "duoi", "từ", "tu", "đến", "den",
            "khoảng", "khoang", "tầm", "tam", "hơn", "hon", "rẻ", "re",
            "nào", "nao", "gì", "gi", "được", "duoc", "với", "voi", "và", "va",
            "là", "la", "của", "cua", "này", "nay", "đó", "do", "thì", "thi",
            "hay", "hoặc", "hoac", "nhưng", "nhung", "vì", "vi", "nên", "nen",
            "rất", "rat", "quá", "qua", "lắm", "lam", "nhất", "nhat",
            "đẹp", "dep", "tốt", "tot", "ok", "ổn", "on", "hỏi", "hoi",
            "gợi", "goi", "ý", "giúp", "giup", "tư", "vấn", "van",
            "về", "ve", "thông", "thong", "tin", "sản", "san", "phẩm", "pham",
            "shop", "cửa", "hàng", "hang", "nhé", "nhe", "ạ",
            "the", "is", "are", "have", "has", "does", "will", "would",
            "me", "my", "you", "your", "we", "our", "it", "its",
            "what", "which", "how", "where", "when", "who",
            "want", "need", "find", "search", "look", "looking", "for", "show"
    ));

    /**
     * Trích xuất giá trị tiền từ tin nhắn.
     * Hỗ trợ: "500k", "500K", "500000", "1tr", "1.5tr", "1m"
     * Trả về giá trị BigDecimal hoặc null.
     */
    private BigDecimal extractPrice(String token) {
        // Loại bỏ dấu chấm phân cách hàng nghìn (500.000)
        String cleaned = token.replaceAll("\\.", "").toLowerCase();
        java.util.regex.Matcher m = java.util.regex.Pattern
                .compile("^(\\d+(?:[,.]\\d+)?)(k|tr|trieu|triệu|m)?$")
                .matcher(cleaned);
        if (!m.matches()) return null;
        try {
            double value = Double.parseDouble(m.group(1).replace(",", "."));
            String unit = m.group(2);
            if (unit != null) {
                switch (unit) {
                    case "k" -> value *= 1_000;
                    case "tr", "trieu", "triệu", "m" -> value *= 1_000_000;
                }
            }
            if (value < 1_000) return null; // Quá nhỏ, không phải giá tiền
            return BigDecimal.valueOf(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Phân tích tin nhắn: trích keyword sản phẩm + giá min/max.
     */
    private record ParsedQuery(List<String> keywords, BigDecimal minPrice, BigDecimal maxPrice) {}

    private ParsedQuery parseMessage(String message) {
        List<String> keywords = new ArrayList<>();
        BigDecimal minPrice = null;
        BigDecimal maxPrice = null;

        String[] tokens = message.toLowerCase()
                .replaceAll("[?,!.;:\"'()\\[\\]{}]", " ")
                .split("\\s+");

        for (int i = 0; i < tokens.length; i++) {
            String token = tokens[i].trim();
            if (token.isEmpty()) continue;

            // Kiểm tra pattern giá: "trên/dưới/từ/đến + số"
            BigDecimal price = extractPrice(token);
            if (price != null) {
                // Xem từ trước đó để xác định min/max
                String prev = (i > 0) ? tokens[i - 1].trim() : "";
                if (prev.matches("dưới|duoi|under|tối đa|max|<=")) {
                    maxPrice = price;
                } else {
                    // Mặc định: "trên", "từ", "hơn", hoặc không có từ chỉ hướng → minPrice
                    minPrice = price;
                }
                continue;
            }

            // Bỏ stopwords và token quá ngắn
            if (token.length() <= 1 || STOPWORDS.contains(token)) continue;

            // Bỏ token chỉ chứa số (không phải giá hợp lệ)
            if (token.matches("\\d+")) continue;

            keywords.add(token);
        }

        return new ParsedQuery(keywords, minPrice, maxPrice);
    }

    private List<Product> searchRelevantProducts(String message, Integer productId) {
        Set<Integer> addedIds = new LinkedHashSet<>();
        List<Product> results = new ArrayList<>();

        // Ưu tiên sản phẩm đang xem (nếu có)
        if (productId != null) {
            productRepository.findById(productId).ifPresent(p -> {
                results.add(p);
                addedIds.add(p.getId());
            });
        }

        // Phân tích tin nhắn: tách keyword + giá
        ParsedQuery parsed = parseMessage(message);
        log.debug("Chat search parsed: keywords={}, minPrice={}, maxPrice={}",
                parsed.keywords(), parsed.minPrice(), parsed.maxPrice());

        // Tìm theo từng keyword riêng lẻ (OR logic)
        for (String keyword : parsed.keywords()) {
            if (results.size() >= MAX_CONTEXT_PRODUCTS) break;
            Specification<Product> spec = ProductSpecification.withFilters(
                    keyword, null, parsed.minPrice(), parsed.maxPrice(), null, true);
            List<Product> found = productRepository.findAll(spec);
            for (Product p : found) {
                if (!addedIds.contains(p.getId())) {
                    results.add(p);
                    addedIds.add(p.getId());
                }
                if (results.size() >= MAX_CONTEXT_PRODUCTS) break;
            }
        }

        // Nếu không tìm được theo keyword, thử chỉ với giá
        if (results.isEmpty() && (parsed.minPrice() != null || parsed.maxPrice() != null)) {
            Specification<Product> spec = ProductSpecification.withFilters(
                    null, null, parsed.minPrice(), parsed.maxPrice(), null, true);
            List<Product> found = productRepository.findAll(spec);
            for (Product p : found) {
                if (!addedIds.contains(p.getId())) {
                    results.add(p);
                    addedIds.add(p.getId());
                }
                if (results.size() >= MAX_CONTEXT_PRODUCTS) break;
            }
        }

        // Fallback: chỉ khi không tìm được sản phẩm nào liên quan
        if (results.isEmpty()) {
            List<Product> all = productRepository.findByIsActiveTrue();
            for (Product p : all) {
                if (!addedIds.contains(p.getId())) {
                    results.add(p);
                    addedIds.add(p.getId());
                }
                if (results.size() >= MAX_CONTEXT_PRODUCTS) break;
            }
        }

        return results.stream().limit(MAX_CONTEXT_PRODUCTS).collect(Collectors.toList());
    }

    // ─── Context Builders ───────────────────────────────────────────────────────

    private Map<Integer, String> buildCategoryMap(List<Product> products) {
        Set<Integer> catIds = products.stream()
                .map(Product::getCategoryId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (catIds.isEmpty()) return Map.of();

        return categoryRepository.findAllById(catIds).stream()
                .collect(Collectors.toMap(Category::getId, Category::getName));
    }

    private Map<Integer, String> buildThumbnailMap(List<Product> products) {
        Map<Integer, String> map = new HashMap<>();
        for (Product p : products) {
            List<ProductVariant> variants = variantRepository.findByProductId(p.getId());
            if (variants.isEmpty()) continue;

            // Ưu tiên variant được đặt làm mặc định
            String url = variants.stream()
                    .filter(v -> Boolean.TRUE.equals(v.getIsDefault()))
                    .map(ProductVariant::getColorImageUrl)
                    .filter(Objects::nonNull)
                    .findFirst()
                    .orElseGet(() -> variants.stream()
                            .map(ProductVariant::getColorImageUrl)
                            .filter(Objects::nonNull)
                            .findFirst()
                            .orElse(null));
            if (url != null) {
                map.put(p.getId(), url);
            }
        }
        return map;
    }

    // ─── System Prompt ──────────────────────────────────────────────────────────

    private String buildSystemPrompt(List<Product> products, Map<Integer, String> categoryMap, String username) {
        StringBuilder sb = new StringBuilder();
        sb.append("Bạn là trợ lý mua sắm thông minh của cửa hàng thời trang.\n");
        sb.append("Nhiệm vụ: giúp khách hàng tìm kiếm và lựa chọn sản phẩm phù hợp.\n\n");

        if (username != null && !username.isBlank()) {
            sb.append("Khách hàng: ").append(username).append("\n\n");
        }

        if (!products.isEmpty()) {
            sb.append("SẢN PHẨM HIỆN CÓ TRONG CỬA HÀNG:\n");
            for (Product p : products) {
                sb.append(String.format(
                        "• [ID:%d] %s | Giá: %,.0f VND | Danh mục: %s%s%s%s\n",
                        p.getId(),
                        p.getName(),
                        p.getBasePrice(),
                        categoryMap.getOrDefault(p.getCategoryId(), "Khác"),
                        p.getBrand()    != null ? " | Thương hiệu: " + p.getBrand()    : "",
                        p.getMaterial() != null ? " | Chất liệu: "   + p.getMaterial() : "",
                        p.getDescription() != null
                                ? " | Mô tả: " + p.getDescription().substring(0, Math.min(80, p.getDescription().length())) + "..."
                                : ""
                ));
            }
        } else {
            sb.append("Hiện tại chưa có sản phẩm để gợi ý.\n");
        }

        sb.append("\nHƯỚNG DẪN TRẢ LỜI:\n");
        sb.append("- Trả lời bằng tiếng Việt, thân thiện và tự nhiên\n");
        sb.append("- Tư vấn về size, phối đồ, chất liệu khi được hỏi\n");
        sb.append("- Nếu gợi ý sản phẩm, ghi tên và giá cụ thể\n");
        sb.append("- Không đề xuất sản phẩm ngoài danh sách trên\n");
        sb.append("- Giữ câu trả lời ngắn gọn, tối đa 200 từ\n");

        return sb.toString();
    }

    // ─── AI Dispatcher ──────────────────────────────────────────────────────────

    /**
     * Gọi AI theo provider được cấu hình.
     * Thứ tự ưu tiên: GitHub Models → DeepSeek → Claude → fallback
     */
    private String callAI(String systemPrompt, List<Map<String, String>> history) {
        // Circuit breaker: skip nếu quota vừa hết
        if (quotaExhausted) {
            long elapsed = System.currentTimeMillis() - quotaExhaustedAt;
            if (elapsed < QUOTA_RETRY_MS) {
                log.debug("AI quota exhausted, dung fallback (con {}m).", (QUOTA_RETRY_MS - elapsed) / 60_000);
                return buildFallbackResponse(true);
            }
            quotaExhausted = false;
            log.info("AI circuit breaker reset. Thu goi AI lai.");
        }

        // 1. Primary: provider được cấu hình
        if ("github".equalsIgnoreCase(aiProvider) && isKeyPresent(aiApiKey)) {
            String result = callGitHubModels(systemPrompt, history);
            if (!isFallback(result)) return result;
            log.info("GitHub Models that bai, thu DeepSeek...");
        }

        // 2. Fallback: DeepSeek
        if ("deepseek".equalsIgnoreCase(aiProvider) || isKeyPresent(deepseekApiKey)) {
            String result = callDeepSeekApi(systemPrompt, history);
            if (!isFallback(result)) return result;
            log.info("DeepSeek that bai, thu Claude...");
        }

        // 3. Fallback: Claude
        if (isKeyPresent(claudeApiKey)) {
            String result = callClaudeApi(systemPrompt, history);
            if (!isFallback(result)) return result;
        }

        log.debug("Tat ca AI provider deu that bai. Su dung fallback response.");
        return buildFallbackResponse(false);
    }

    private boolean isKeyPresent(String key) {
        return key != null && !key.isBlank();
    }

    private boolean isFallback(String response) {
        return response.startsWith("Xin chào! Hiện tại trợ lý AI")
            || response.startsWith("Xin chào! Tôi là trợ lý mua sắm");
    }

    // ─── GitHub Models (OpenAI-compatible) ──────────────────────────────────────

    @SuppressWarnings("unchecked")
    private String callGitHubModels(String systemPrompt, List<Map<String, String>> history) {
        try {
            // Build messages theo OpenAI format: system + history
            List<Map<String, Object>> messages = new ArrayList<>();
            messages.add(Map.of("role", "system", "content", systemPrompt));
            history.stream()
                   .filter(m -> "user".equals(m.get("role")) || "assistant".equals(m.get("role")))
                   .forEach(m -> messages.add(Map.of(
                           "role",    m.get("role"),
                           "content", m.get("content")
                   )));

            Map<String, Object> requestBody = new LinkedHashMap<>();
            requestBody.put("model",       aiModel);       // "gpt-4o-mini"
            requestBody.put("messages",    messages);
            requestBody.put("max_tokens",  aiMaxTokens);
            requestBody.put("temperature", 0.7);

            String raw = RestClient.create()
                    .post()
                    .uri(GITHUB_MODELS_URL)
                    .header("Authorization", "Bearer " + aiApiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody)
                    .retrieve()
                    .body(String.class);

            if (raw == null || raw.isBlank()) return buildFallbackResponse(false);

            Map<String, Object> response = objectMapper.readValue(raw, Map.class);
            List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
            if (choices != null && !choices.isEmpty()) {
                Map<String, Object> msg = (Map<String, Object>) choices.getFirst().get("message");
                if (msg != null && msg.get("content") instanceof String s) {
                    log.debug("GitHub Models ({}) tra loi thanh cong.", aiModel);
                    return s;
                }
            }

        } catch (HttpClientErrorException e) {
            handleHttpError("GitHub Models", e.getStatusCode().value(), e.getResponseBodyAsString());
        } catch (ResourceAccessException e) {
            log.warn("Khong ket noi duoc GitHub Models: {}", e.getMessage());
        } catch (Exception e) {
            log.error("Loi goi GitHub Models: {}", e.getMessage());
        }

        return buildFallbackResponse(false);
    }

    // ─── DeepSeek API (OpenAI-compatible) ──────────────────────────────────────────

    @SuppressWarnings("unchecked")
    private String callDeepSeekApi(String systemPrompt, List<Map<String, String>> history) {
        try {
            List<Map<String, Object>> messages = new ArrayList<>();
            messages.add(Map.of("role", "system", "content", systemPrompt));
            history.stream()
                   .filter(m -> "user".equals(m.get("role")) || "assistant".equals(m.get("role")))
                   .forEach(m -> messages.add(Map.of(
                           "role",    m.get("role"),
                           "content", m.get("content")
                   )));

            Map<String, Object> requestBody = new LinkedHashMap<>();
            requestBody.put("model",       deepseekModel);
            requestBody.put("messages",    messages);
            requestBody.put("max_tokens",  deepseekMaxTokens);
            requestBody.put("temperature", 0.7);

            String raw = RestClient.create()
                    .post()
                    .uri(deepseekApiUrl + DEEPSEEK_COMPLETIONS_PATH)
                    .header("Authorization", "Bearer " + deepseekApiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody)
                    .retrieve()
                    .body(String.class);

            if (raw == null || raw.isBlank()) return buildFallbackResponse(false);

            Map<String, Object> response = objectMapper.readValue(raw, Map.class);
            List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
            if (choices != null && !choices.isEmpty()) {
                Map<String, Object> msg = (Map<String, Object>) choices.getFirst().get("message");
                if (msg != null && msg.get("content") instanceof String s) {
                    log.debug("DeepSeek ({}) tra loi thanh cong.", deepseekModel);
                    return s;
                }
            }

        } catch (HttpClientErrorException e) {
            handleHttpError("DeepSeek", e.getStatusCode().value(), e.getResponseBodyAsString());
        } catch (ResourceAccessException e) {
            log.warn("Khong ket noi duoc DeepSeek API: {}", e.getMessage());
        } catch (Exception e) {
            log.error("Loi goi DeepSeek API: {}", e.getMessage());
        }

        return buildFallbackResponse(false);
    }

    // ─── Claude API ──────────────────────────────────────────────────────────────

    @SuppressWarnings("unchecked")
    private String callClaudeApi(String systemPrompt, List<Map<String, String>> history) {
        try {
            List<Map<String, String>> messages = history.stream()
                    .filter(m -> "user".equals(m.get("role")) || "assistant".equals(m.get("role")))
                    .collect(Collectors.toList());

            Map<String, Object> requestBody = new LinkedHashMap<>();
            requestBody.put("model",      claudeModel);
            requestBody.put("max_tokens", claudeMaxTokens);
            requestBody.put("system",     systemPrompt);
            requestBody.put("messages",   messages);

            String raw = RestClient.create()
                    .post()
                    .uri(CLAUDE_API_URL)
                    .header("x-api-key", claudeApiKey)
                    .header("anthropic-version", ANTHROPIC_VERSION)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody)
                    .retrieve()
                    .body(String.class);

            if (raw == null || raw.isBlank()) return buildFallbackResponse(false);

            Map<String, Object> response = objectMapper.readValue(raw, Map.class);
            List<Map<String, Object>> content = (List<Map<String, Object>>) response.get("content");
            if (content != null && !content.isEmpty()) {
                Object text = content.getFirst().get("text");
                if (text instanceof String s) return s;
            }

        } catch (HttpClientErrorException e) {
            handleHttpError("Claude", e.getStatusCode().value(), e.getResponseBodyAsString());
        } catch (ResourceAccessException e) {
            log.warn("Khong ket noi duoc Claude API: {}", e.getMessage());
        } catch (Exception e) {
            log.error("Loi goi Claude API: {}", e.getMessage());
        }

        return buildFallbackResponse(false);
    }

    // ─── Error Handler ───────────────────────────────────────────────────────────

    private void handleHttpError(String provider, int statusCode, String body) {
        String shortBody = body != null && body.length() > 200 ? body.substring(0, 200) : body;

        if (body != null && (body.contains("credit balance is too low")
                || body.contains("insufficient_quota")
                || body.contains("rate limit")
                || statusCode == 429)) {
            quotaExhausted   = true;
            quotaExhaustedAt = System.currentTimeMillis();
            log.warn("[{}] API het quota/credits (HTTP {}). Tam dung 1 gio. Chi tiet: {}",
                     provider, statusCode, shortBody);
        } else if (statusCode == 401) {
            log.error("[{}] API key khong hop le (401). Kiem tra lai AI_API_KEY.", provider);
        } else {
            log.error("[{}] API loi HTTP {}: {}", provider, statusCode, shortBody);
        }
    }

    private String buildFallbackResponse(boolean quotaEmpty) {
        if (quotaEmpty) {
            return "Xin chào! Hiện tại trợ lý AI tạm thời không khả dụng. "
                 + "Bạn có thể xem các sản phẩm gợi ý bên dưới hoặc liên hệ cửa hàng để được tư vấn trực tiếp nhé!";
        }
        return "Xin chào! Tôi là trợ lý mua sắm của cửa hàng. "
             + "Bạn có thể xem các sản phẩm gợi ý bên dưới. "
             + "Liên hệ cửa hàng nếu cần tư vấn thêm nhé!";
    }

    // ─── Suggestion Builder ─────────────────────────────────────────────────────

    private List<ProductSuggestionDTO> buildSuggestions(
            List<Product> products,
            Map<Integer, String> categoryMap,
            Map<Integer, String> thumbnailMap) {

        return products.stream()
                .limit(MAX_SUGGESTIONS)
                .map(p -> ProductSuggestionDTO.builder()
                        .id(p.getId())
                        .name(p.getName())
                        .price(p.getBasePrice())
                        .thumbnailUrl(thumbnailMap.get(p.getId()))
                        .slug(p.getSlug())
                        .brand(p.getBrand())
                        .material(p.getMaterial())
                        .categoryName(categoryMap.get(p.getCategoryId()))
                        .build())
                .collect(Collectors.toList());
    }
}

