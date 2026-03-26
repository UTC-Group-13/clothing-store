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
import org.springframework.web.client.RestClient;

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

    @Value("${claude.api.key:}")
    private String apiKey;

    @Value("${claude.api.model:claude-3-haiku-20240307}")
    private String model;

    @Value("${claude.api.max-tokens:1024}")
    private int maxTokens;

    private static final String CLAUDE_API_URL = "https://api.anthropic.com/v1/messages";
    private static final String ANTHROPIC_VERSION  = "2023-06-01";
    private static final int    MAX_HISTORY         = 20;
    private static final int    MAX_CONTEXT_PRODUCTS = 8;
    private static final int    MAX_SUGGESTIONS      = 5;
    private static final long   SESSION_TTL_MS       = 2 * 60 * 60 * 1000L; // 2 giờ

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

        // 6. Gọi Claude API
        String aiMessage = callClaudeApi(systemPrompt, session.messages);

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

        // Tìm kiếm theo từ khóa trong tin nhắn
        String keyword = message.length() > 150 ? message.substring(0, 150) : message;
        Specification<Product> spec = ProductSpecification.withFilters(keyword, null, null, null, null, true);
        List<Product> found = productRepository.findAll(spec);
        for (Product p : found) {
            if (!addedIds.contains(p.getId())) {
                results.add(p);
                addedIds.add(p.getId());
            }
            if (results.size() >= MAX_CONTEXT_PRODUCTS) break;
        }

        // Fallback: lấy sản phẩm active nếu chưa đủ
        if (results.size() < 3) {
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
        sb.append("Ban la tro ly mua sam thong minh cua cua hang thoi trang.\n");
        sb.append("Nhiem vu: giup khach hang tim kiem va lua chon san pham phu hop.\n\n");

        if (username != null && !username.isBlank()) {
            sb.append("Khach hang: ").append(username).append("\n\n");
        }

        if (!products.isEmpty()) {
            sb.append("SAN PHAM HIEN CO TRONG CUA HANG:\n");
            for (Product p : products) {
                sb.append(String.format(
                        "• [ID:%d] %s | Gia: %,.0f VND | Danh muc: %s%s%s%s\n",
                        p.getId(),
                        p.getName(),
                        p.getBasePrice(),
                        categoryMap.getOrDefault(p.getCategoryId(), "Khac"),
                        p.getBrand()    != null ? " | Thuong hieu: " + p.getBrand()    : "",
                        p.getMaterial() != null ? " | Chat lieu: "   + p.getMaterial() : "",
                        p.getDescription() != null
                                ? " | Mo ta: " + p.getDescription().substring(0, Math.min(80, p.getDescription().length())) + "..."
                                : ""
                ));
            }
        } else {
            sb.append("Hien tai chua co san pham de goi y.\n");
        }

        sb.append("\nHUONG DAN TRA LOI:\n");
        sb.append("- Tra loi bang tieng Viet, than thien va tu nhien\n");
        sb.append("- Tu van ve size, phoi do, chat lieu khi duoc hoi\n");
        sb.append("- Neu goi y san pham, ghi ten va gia cu the\n");
        sb.append("- Khong bịa san pham ngoai danh sach tren\n");
        sb.append("- Giu cau tra loi ngan gon, toi da 200 tu\n");

        return sb.toString();
    }

    // ─── Claude API Call ────────────────────────────────────────────────────────

    @SuppressWarnings("unchecked")
    private String callClaudeApi(String systemPrompt, List<Map<String, String>> history) {
        if (apiKey == null || apiKey.isBlank()) {
            log.warn("Claude API key chua duoc cau hinh (claude.api.key). Su dung fallback response.");
            return buildFallbackResponse();
        }

        try {
            // Chỉ gửi role user/assistant (không gửi system trong messages[])
            List<Map<String, String>> messages = history.stream()
                    .filter(m -> "user".equals(m.get("role")) || "assistant".equals(m.get("role")))
                    .collect(Collectors.toList());

            Map<String, Object> requestBody = new LinkedHashMap<>();
            requestBody.put("model", model);
            requestBody.put("max_tokens", maxTokens);
            requestBody.put("system", systemPrompt);
            requestBody.put("messages", messages);

            RestClient restClient = RestClient.create();

            String raw = restClient.post()
                    .uri(CLAUDE_API_URL)
                    .header("x-api-key", apiKey)
                    .header("anthropic-version", ANTHROPIC_VERSION)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody)
                    .retrieve()
                    .body(String.class);

            if (raw == null || raw.isBlank()) {
                return buildFallbackResponse();
            }

            Map<String, Object> response = objectMapper.readValue(raw, Map.class);
            List<Map<String, Object>> content = (List<Map<String, Object>>) response.get("content");
            if (content != null && !content.isEmpty()) {
                Object text = content.getFirst().get("text");
                if (text instanceof String s) return s;
            }

        } catch (Exception e) {
            log.error("Loi goi Claude API: {}", e.getMessage(), e);
        }

        return buildFallbackResponse();
    }

    private String buildFallbackResponse() {
        return "Xin chao! Toi la tro ly mua sam. "
                + "Ban co the xem cac san pham duoc goi y ben duoi. "
                + "Hay lien he cua hang neu can tu van them nhe!";
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




