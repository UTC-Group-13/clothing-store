package com.utc.ec.service.impl;

import com.utc.ec.dto.AddressDTO;
import com.utc.ec.dto.OrderDetailDTO;
import com.utc.ec.dto.OrderLineDetailDTO;
import com.utc.ec.service.EmailService;
import jakarta.annotation.PostConstruct;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.from:noreply@clothingstore.com}")
    private String fromEmail;

    @Value("${app.mail.from-name:Clothing Store}")
    private String fromName;

    private static final DateTimeFormatter EMAIL_DATE_FMT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private static final String TEMPLATE_PATH = "templates/email/order-confirmation.html";

    /** Cache template HTML lúc khởi động — tránh đọc file mỗi lần gửi mail */
    private String orderConfirmationTemplate;

    @PostConstruct
    public void initTemplates() {
        orderConfirmationTemplate = loadTemplate(TEMPLATE_PATH);
        log.info("[Email] Da tai template: {}", TEMPLATE_PATH);
    }

    // =========================================================
    //  Public API
    // =========================================================

    @Override
    @Async("emailTaskExecutor")
    public void sendOrderConfirmationEmail(String toEmail,
                                           String customerName,
                                           OrderDetailDTO order) {
        if (toEmail == null || toEmail.isBlank()) {
            log.warn("[Email] Bo qua gui email xac nhan: email nguoi dung trong (orderId={})",
                    order.getId());
            return;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(new InternetAddress(fromEmail, fromName, "UTF-8"));
            helper.setTo(toEmail);
            helper.setSubject("✅ Đặt hàng thành công - Mã đơn: " + order.getOrderCode());
            helper.setText(buildEmailContent(customerName, order), true);

            mailSender.send(message);
            log.info("[Email] Da gui email xac nhan don hang {} den {}",
                    order.getOrderCode(), toEmail);

        } catch (Exception e) {
            log.error("[Email] Loi gui email don hang {}: {}",
                    order.getOrderCode(), e.getMessage());
        }
    }

    // =========================================================
    //  Template rendering — thay thế {{PLACEHOLDER}} bằng data
    // =========================================================

    private String buildEmailContent(String customerName, OrderDetailDTO order) {
        String orderDate = order.getOrderDate() != null
                ? order.getOrderDate().format(EMAIL_DATE_FMT)
                : "N/A";

        return orderConfirmationTemplate
                .replace("{{CUSTOMER_NAME}}",    safe(customerName))
                .replace("{{ORDER_CODE}}",        safe(order.getOrderCode()))
                .replace("{{ORDER_DATE}}",        orderDate)
                .replace("{{STATUS_NAME}}",       safe(order.getStatusName(), "PENDING"))
                .replace("{{PAYMENT_TYPE_NAME}}", safe(order.getPaymentTypeName(), "N/A"))
                .replace("{{ITEMS_ROWS}}",        buildItemsRows(order))
                .replace("{{SUBTOTAL}}",          formatCurrency(order.getSubtotal()))
                .replace("{{SHIPPING_FEE}}",      formatCurrency(order.getShippingFee()))
                .replace("{{ORDER_TOTAL}}",       formatCurrency(order.getOrderTotal()))
                .replace("{{ADDRESS_SECTION}}",   buildAddressSection(order.getShippingAddressDetail()))
                .replace("{{QR_SECTION}}",        buildQrSection(order));
    }

    // =========================================================
    //  Dynamic HTML builders
    // =========================================================

    private String buildItemsRows(OrderDetailDTO order) {
        if (order.getItems() == null || order.getItems().isEmpty()) {
            return "<tr><td colspan=\"4\" style=\"padding:16px;text-align:center;"
                    + "color:#a0aec0;font-size:13px;\">Không có sản phẩm</td></tr>";
        }

        StringBuilder sb = new StringBuilder();
        boolean even = false;

        for (OrderLineDetailDTO item : order.getItems()) {
            String bg = even ? "#f7fafc" : "#ffffff";
            even = !even;

            String colorBadge = buildColorBadge(item);
            String sizeLabel  = safe(item.getSizeLabel());
            String name       = safe(item.getProductName(), "Sản phẩm");

            sb.append("<tr style=\"background:").append(bg).append(";\">")
              .append("<td style=\"padding:12px 14px;border-bottom:1px solid #e2e8f0;\">")
                .append("<div style=\"font-weight:600;color:#2d3748;font-size:14px;\">")
                  .append(name)
                .append("</div>")
                .append("<div style=\"color:#a0aec0;font-size:12px;margin-top:3px;\">")
                  .append(colorBadge)
                  .append(colorBadge.isEmpty() ? "" : " &nbsp;|&nbsp; ")
                  .append("Size: ").append(sizeLabel)
                .append("</div>")
              .append("</td>")
              .append("<td style=\"padding:12px 14px;text-align:center;"
                      + "border-bottom:1px solid #e2e8f0;color:#2d3748;font-weight:600;\">")
                .append(item.getQty() != null ? item.getQty() : 0)
              .append("</td>")
              .append("<td style=\"padding:12px 14px;text-align:right;"
                      + "border-bottom:1px solid #e2e8f0;color:#2d3748;font-size:13px;\">")
                .append(formatCurrency(item.getPrice()))
              .append("</td>")
              .append("<td style=\"padding:12px 14px;text-align:right;"
                      + "border-bottom:1px solid #e2e8f0;color:#2d3748;"
                      + "font-weight:600;font-size:13px;\">")
                .append(formatCurrency(item.getSubtotal()))
              .append("</td>")
            .append("</tr>");
        }
        return sb.toString();
    }

    private String buildColorBadge(OrderLineDetailDTO item) {
        if (item.getColorName() == null) return "";
        String hex = item.getColorHex() != null ? item.getColorHex() : "#cccccc";
        return "<span style=\"display:inline-block;width:10px;height:10px;border-radius:50%;"
                + "background:" + hex + ";margin-right:4px;vertical-align:middle;\"></span>"
                + item.getColorName();
    }

    private String buildAddressSection(AddressDTO addr) {
        if (addr == null) return "";

        return "<div style=\"background:#f8f9ff;border:1px solid #e2e8f0;border-radius:8px;"
                + "padding:16px;margin-bottom:20px;\">"
                + "<h4 style=\"color:#2d3748;margin:0 0 10px;font-size:14px;font-weight:700;\">"
                + "📍 Địa chỉ giao hàng</h4>"
                + "<p style=\"margin:0;color:#4a5568;font-size:14px;line-height:1.65;\">"
                + formatAddress(addr)
                + "</p></div>";
    }

    private String formatAddress(AddressDTO addr) {
        StringBuilder sb = new StringBuilder();
        appendPart(sb, addr.getUnitNumber());
        appendPart(sb, addr.getStreetNumber());
        appendPart(sb, addr.getAddressLine1());
        appendPart(sb, addr.getAddressLine2());
        appendPart(sb, addr.getCity());
        appendPart(sb, addr.getRegion());
        appendPart(sb, addr.getPostalCode());
        return sb.toString().trim().replaceAll("^,\\s*", "").replaceAll(",\\s*,", ",");
    }

    private void appendPart(StringBuilder sb, String value) {
        if (value != null && !value.isBlank()) {
            if (!sb.isEmpty()) sb.append(", ");
            sb.append(value.trim());
        }
    }

    private String buildQrSection(OrderDetailDTO order) {
        if (order.getQrUrl() == null || order.getQrUrl().isBlank()) return "";

        String bankName   = "";
        String accountNo  = "";
        String holderName = "";

        if (order.getBankInfo() != null) {
            bankName   = safe(order.getBankInfo().getBankName());
            accountNo  = safe(order.getBankInfo().getAccountNumber());
            holderName = safe(order.getBankInfo().getAccountHolderName());
        }

        return "<div style=\"background:#f0fff4;border:2px solid #48bb78;border-radius:10px;"
                + "padding:20px;margin-bottom:20px;text-align:center;\">"
                + "<h4 style=\"color:#276749;margin:0 0 12px;font-size:15px;font-weight:700;\">"
                + "🏦 Thông tin chuyển khoản</h4>"
                + "<p style=\"color:#4a5568;font-size:13px;margin:0 0 16px;\">"
                + "Vui lòng chuyển khoản đúng nội dung mã đơn hàng để được xác nhận nhanh nhất.</p>"
                + "<table width=\"auto\" cellpadding=\"0\" cellspacing=\"0\" "
                + "style=\"margin:0 auto 16px;background:#fff;border:1px solid #c6f6d5;"
                + "border-radius:8px;\">"
                + buildBankInfoRow("Ngân hàng",     bankName,               true)
                + buildBankInfoRow("Số tài khoản",  accountNo,              true)
                + buildBankInfoRow("Chủ tài khoản", holderName,             true)
                + buildBankInfoRow("Nội dung CK",   order.getOrderCode(),   false)
                + "</table>"
                + "<img src=\"" + order.getQrUrl() + "\" alt=\"QR Code chuyen khoan\" "
                + "style=\"max-width:200px;border-radius:8px;border:3px solid #fff;"
                + "box-shadow:0 2px 8px rgba(0,0,0,0.15);\" />"
                + "<p style=\"color:#718096;font-size:12px;margin:12px 0 0;\">"
                + "Quét mã QR bằng ứng dụng ngân hàng để chuyển khoản tự động</p>"
                + "</div>";
    }

    private String buildBankInfoRow(String label, String value, boolean hasBorder) {
        String border = hasBorder ? "border-bottom:1px solid #c6f6d5;" : "";
        return "<tr><td style=\"padding:10px 20px;" + border + "\">"
                + "<span style=\"color:#718096;font-size:12px;\">" + label + ":</span>"
                + "<strong style=\"color:#2d3748;margin-left:8px;font-size:14px;\">"
                + value + "</strong></td></tr>";
    }

    // =========================================================
    //  Utilities
    // =========================================================

    private String loadTemplate(String classpathPath) {
        try {
            ClassPathResource resource = new ClassPathResource(classpathPath);
            byte[] bytes = resource.getInputStream().readAllBytes();
            return new String(bytes, StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.error("[Email] Khong the doc template email: {}", classpathPath, e);
            throw new IllegalStateException("Email template not found: " + classpathPath, e);
        }
    }

    private String formatCurrency(Integer amount) {
        if (amount == null) return "0đ";
        return String.format("%,d", amount).replace(",", ".") + "đ";
    }

    private String safe(String value) {
        return value != null ? value : "";
    }

    private String safe(String value, String defaultValue) {
        return (value != null && !value.isBlank()) ? value : defaultValue;
    }
}

