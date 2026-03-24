package com.utc.ec.service.impl;

import com.utc.ec.dto.*;
import com.utc.ec.entity.*;
import com.utc.ec.exception.BusinessException;
import com.utc.ec.exception.ResourceNotFoundException;
import com.utc.ec.repository.*;
import com.utc.ec.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final ShopOrderRepository       orderRepository;
    private final OrderLineRepository       orderLineRepository;
    private final OrderStatusRepository     orderStatusRepository;
    private final ShippingMethodRepository  shippingMethodRepository;
    private final AddressRepository         addressRepository;
    private final UserAddressRepository     userAddressRepository;
    private final PaymentTypeRepository     paymentTypeRepository;
    private final ShopBankAccountRepository shopBankAccountRepository;
    private final ShoppingCartRepository    cartRepository;
    private final ShoppingCartItemRepository cartItemRepository;
    private final VariantStockRepository    variantStockRepository;
    private final ProductVariantRepository  productVariantRepository;
    private final ProductRepository         productRepository;
    private final ColorRepository           colorRepository;
    private final SizeRepository            sizeRepository;
    private final SiteUserRepository        userRepository;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final String VIETQR_TEMPLATE = "https://img.vietqr.io/image/%s-%s-compact2.png?amount=%d&addInfo=%s&accountName=%s";

    // =====================================================================
    //  USER operations
    // =====================================================================

    @Override
    @Transactional
    public OrderDetailDTO placeOrder(String username, OrderRequest request) {
        SiteUser user = getUser(username);

        // 1. Kiểm tra loại thanh toán
        PaymentType paymentType = paymentTypeRepository.findById(request.getPaymentTypeId())
                .orElseThrow(() -> new ResourceNotFoundException("payment.typeNotFound",
                        request.getPaymentTypeId()));

        // 2. Kiểm tra địa chỉ giao hàng thuộc về user
        if (!userAddressRepository.existsByUserIdAndAddressId(user.getId(), request.getShippingAddressId())) {
            throw new BusinessException("order.addressNotBelongToUser", request.getShippingAddressId());
        }

        // 3. Kiểm tra phương thức vận chuyển
        ShippingMethod shippingMethod = shippingMethodRepository.findById(request.getShippingMethodId())
                .orElseThrow(() -> new ResourceNotFoundException("shipping.notFound",
                        request.getShippingMethodId()));

        // 4. Lấy giỏ hàng
        ShoppingCart cart = cartRepository.findByUserId(user.getId())
                .orElseThrow(() -> new BusinessException("order.cartEmpty"));

        List<ShoppingCartItem> cartItems = cartItemRepository.findByCartId(cart.getId());
        if (cartItems.isEmpty()) {
            throw new BusinessException("order.cartEmpty");
        }

        // 5. Kiểm tra tồn kho và tính toán subtotal
        Map<Integer, VariantStock> stockMap = variantStockRepository
                .findAllById(cartItems.stream().map(ShoppingCartItem::getVariantStockId).collect(Collectors.toList()))
                .stream().collect(Collectors.toMap(VariantStock::getId, Function.identity()));

        Map<Integer, Product> productByVariantId = buildProductByVariantIdMap(stockMap);

        int subtotal = 0;
        for (ShoppingCartItem cartItem : cartItems) {
            VariantStock stock = stockMap.get(cartItem.getVariantStockId());
            if (stock == null) {
                throw new BusinessException("cart.stockNotFound", cartItem.getVariantStockId());
            }
            if (stock.getStockQty() < cartItem.getQty()) {
                throw new BusinessException("order.insufficientStock", stock.getSku(), stock.getStockQty());
            }
            int unitPrice = resolveUnitPrice(stock, productByVariantId);
            subtotal += unitPrice * cartItem.getQty();
        }

        // 6. Lấy trạng thái PENDING
        OrderStatus pendingStatus = orderStatusRepository
                .findByStatus(OrderStatusServiceImpl.STATUS_PENDING)
                .orElseThrow(() -> new BusinessException("order.pendingStatusNotConfigured"));

        // 7. Sinh mã đơn hàng (DH + yyyyMMdd + số thứ tự 3 chữ số)
        String orderCode = generateOrderCode();

        // 8. Tạo đơn hàng
        int orderTotal = subtotal + (shippingMethod.getPrice() != null ? shippingMethod.getPrice() : 0);

        ShopOrder order = new ShopOrder();
        order.setUserId(user.getId());
        order.setOrderDate(LocalDateTime.now());
        order.setOrderCode(orderCode);
        order.setPaymentTypeId(paymentType.getId());
        order.setPaymentNote(request.getNote());
        order.setShippingAddress(request.getShippingAddressId());
        order.setShippingMethod(request.getShippingMethodId());
        order.setOrderTotal(orderTotal);
        order.setOrderStatus(pendingStatus.getId());
        order = orderRepository.save(order);

        // 9. Tạo order lines và trừ tồn kho
        for (ShoppingCartItem cartItem : cartItems) {
            VariantStock stock = stockMap.get(cartItem.getVariantStockId());
            int unitPrice = resolveUnitPrice(stock, productByVariantId);

            OrderLine line = new OrderLine();
            line.setOrderId(order.getId());
            line.setVariantStockId(cartItem.getVariantStockId());
            line.setQty(cartItem.getQty());
            line.setPrice(unitPrice);
            orderLineRepository.save(line);

            // Trừ tồn kho
            stock.setStockQty(stock.getStockQty() - cartItem.getQty());
            variantStockRepository.save(stock);
        }

        // 10. Làm trống giỏ hàng
        cartItemRepository.deleteByCartId(cart.getId());

        return buildOrderDetail(order);
    }

    @Override
    public List<OrderDetailDTO> getMyOrders(String username) {
        SiteUser user = getUser(username);
        return orderRepository.findByUserIdOrderByOrderDateDesc(user.getId())
                .stream().map(this::buildOrderDetail).collect(Collectors.toList());
    }

    @Override
    public OrderDetailDTO getMyOrderById(String username, Integer orderId) {
        SiteUser user = getUser(username);
        ShopOrder order = orderRepository.findByIdAndUserId(orderId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("order.notFound", orderId));
        return buildOrderDetail(order);
    }

    @Override
    @Transactional
    public OrderDetailDTO cancelOrder(String username, Integer orderId) {
        SiteUser user = getUser(username);
        ShopOrder order = orderRepository.findByIdAndUserId(orderId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("order.notFound", orderId));

        OrderStatus currentStatus = orderStatusRepository.findById(order.getOrderStatus()).orElse(null);
        if (currentStatus == null
                || !OrderStatusServiceImpl.STATUS_PENDING.equalsIgnoreCase(currentStatus.getStatus())) {
            throw new BusinessException("order.cannotCancel");
        }

        restoreStock(order.getId());

        OrderStatus cancelledStatus = orderStatusRepository
                .findByStatus(OrderStatusServiceImpl.STATUS_CANCELLED)
                .orElseThrow(() -> new BusinessException("order.cancelledStatusNotConfigured"));

        order.setOrderStatus(cancelledStatus.getId());
        return buildOrderDetail(orderRepository.save(order));
    }

    // =====================================================================
    //  ADMIN operations
    // =====================================================================

    @Override
    public Page<OrderDetailDTO> getAllOrders(Pageable pageable) {
        return orderRepository.findAllByOrderByOrderDateDesc(pageable)
                .map(this::buildOrderDetail);
    }

    @Override
    public Page<OrderDetailDTO> getOrdersByStatus(Integer statusId, Pageable pageable) {
        return orderRepository.findByOrderStatusOrderByOrderDateDesc(statusId, pageable)
                .map(this::buildOrderDetail);
    }

    @Override
    public OrderDetailDTO getOrderById(Integer orderId) {
        ShopOrder order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("order.notFound", orderId));
        return buildOrderDetail(order);
    }

    @Override
    @Transactional
    public OrderDetailDTO updateOrderStatus(Integer orderId, Integer statusId) {
        ShopOrder order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("order.notFound", orderId));

        if (!orderStatusRepository.existsById(statusId)) {
            throw new ResourceNotFoundException("order.statusNotFound", statusId);
        }

        order.setOrderStatus(statusId);
        return buildOrderDetail(orderRepository.save(order));
    }

    // =====================================================================
    //  Private helpers
    // =====================================================================

    private SiteUser getUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException("auth.user.notFound"));
    }

    /**
     * Sinh mã đơn hàng: DH + yyyyMMdd + 3 chữ số (VD: DH20260324001)
     */
    private String generateOrderCode() {
        String datePrefix = "DH" + LocalDateTime.now().format(DATE_FMT);
        long count = orderRepository.countByOrderCodeStartingWith(datePrefix);
        return String.format("%s%03d", datePrefix, count + 1);
    }

    /**
     * Tạo VietQR URL cho chuyển khoản ngân hàng.
     */
    private String buildVietQrUrl(ShopBankAccount bank, int amount, String orderCode) {
        String encodedName = URLEncoder.encode(bank.getAccountHolderName(), StandardCharsets.UTF_8);
        String encodedInfo = URLEncoder.encode(orderCode, StandardCharsets.UTF_8);
        return String.format(VIETQR_TEMPLATE,
                bank.getBankId(), bank.getAccountNumber(),
                amount, encodedInfo, encodedName);
    }

    private int resolveUnitPrice(VariantStock stock, Map<Integer, Product> productByVariantId) {
        if (stock.getPriceOverride() != null) {
            return stock.getPriceOverride().intValue();
        }
        Product product = productByVariantId.get(stock.getVariantId());
        if (product != null && product.getBasePrice() != null) {
            return product.getBasePrice().intValue();
        }
        return 0;
    }

    private Map<Integer, Product> buildProductByVariantIdMap(Map<Integer, VariantStock> stockMap) {
        List<Integer> variantIds = stockMap.values().stream()
                .map(VariantStock::getVariantId).distinct().collect(Collectors.toList());

        Map<Integer, ProductVariant> variantMap = productVariantRepository.findAllById(variantIds)
                .stream().collect(Collectors.toMap(ProductVariant::getId, Function.identity()));

        List<Integer> productIds = variantMap.values().stream()
                .map(ProductVariant::getProductId).distinct().collect(Collectors.toList());

        Map<Integer, Product> productMap = productRepository.findAllById(productIds)
                .stream().collect(Collectors.toMap(Product::getId, Function.identity()));

        return variantMap.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> productMap.getOrDefault(e.getValue().getProductId(), new Product())));
    }

    private void restoreStock(Integer orderId) {
        List<OrderLine> lines = orderLineRepository.findByOrderId(orderId);
        for (OrderLine line : lines) {
            variantStockRepository.findById(line.getVariantStockId()).ifPresent(stock -> {
                stock.setStockQty(stock.getStockQty() + line.getQty());
                variantStockRepository.save(stock);
            });
        }
    }

    /**
     * Tạo OrderDetailDTO đầy đủ, bao gồm QR URL nếu là chuyển khoản.
     */
    private OrderDetailDTO buildOrderDetail(ShopOrder order) {
        OrderDetailDTO dto = new OrderDetailDTO();
        dto.setId(order.getId());
        dto.setOrderCode(order.getOrderCode());
        dto.setUserId(order.getUserId());
        dto.setOrderDate(order.getOrderDate());
        dto.setPaymentNote(order.getPaymentNote());
        dto.setShippingAddressId(order.getShippingAddress());

        // Trạng thái
        if (order.getOrderStatus() != null) {
            orderStatusRepository.findById(order.getOrderStatus()).ifPresent(s -> {
                dto.setStatusId(s.getId());
                dto.setStatusName(s.getStatus());
            });
        }

        // Loại thanh toán + QR
        if (order.getPaymentTypeId() != null) {
            paymentTypeRepository.findById(order.getPaymentTypeId()).ifPresent(pt -> {
                dto.setPaymentTypeId(pt.getId());
                dto.setPaymentTypeName(pt.getValue());

                // Nếu là chuyển khoản → tạo QR URL
                if (isTransferPayment(pt.getValue())) {
                    shopBankAccountRepository.findByIsActiveTrue().ifPresent(bank -> {
                        dto.setQrUrl(buildVietQrUrl(bank, order.getOrderTotal(), order.getOrderCode()));
                        // Gửi kèm thông tin bank để FE hiển thị
                        ShopBankAccountDTO bankDto = new ShopBankAccountDTO();
                        bankDto.setId(bank.getId());
                        bankDto.setBankId(bank.getBankId());
                        bankDto.setBankName(bank.getBankName());
                        bankDto.setAccountNumber(bank.getAccountNumber());
                        bankDto.setAccountHolderName(bank.getAccountHolderName());
                        bankDto.setLogoUrl(bank.getLogoUrl());
                        dto.setBankInfo(bankDto);
                    });
                }
            });
        }

        // Phương thức vận chuyển
        if (order.getShippingMethod() != null) {
            shippingMethodRepository.findById(order.getShippingMethod()).ifPresent(sm -> {
                dto.setShippingMethodId(sm.getId());
                dto.setShippingMethodName(sm.getName());
                dto.setShippingFee(sm.getPrice());
            });
        }

        // Địa chỉ giao hàng
        if (order.getShippingAddress() != null) {
            addressRepository.findById(order.getShippingAddress()).ifPresent(addr -> {
                AddressDTO addrDto = new AddressDTO();
                addrDto.setId(addr.getId());
                addrDto.setUnitNumber(addr.getUnitNumber());
                addrDto.setStreetNumber(addr.getStreetNumber());
                addrDto.setAddressLine1(addr.getAddressLine1());
                addrDto.setAddressLine2(addr.getAddressLine2());
                addrDto.setCity(addr.getCity());
                addrDto.setRegion(addr.getRegion());
                addrDto.setPostalCode(addr.getPostalCode());
                addrDto.setCountryId(addr.getCountryId());
                dto.setShippingAddressDetail(addrDto);
            });
        }

        // Order lines
        List<OrderLine> lines = orderLineRepository.findByOrderId(order.getId());
        List<OrderLineDetailDTO> lineDetails = buildLineDetails(lines);
        dto.setItems(lineDetails);

        int subtotal = lineDetails.stream()
                .mapToInt(l -> l.getSubtotal() != null ? l.getSubtotal() : 0).sum();
        dto.setSubtotal(subtotal);
        dto.setOrderTotal(order.getOrderTotal());

        return dto;
    }

    /**
     * Kiểm tra loại thanh toán có phải chuyển khoản không.
     */
    private boolean isTransferPayment(String paymentTypeValue) {
        if (paymentTypeValue == null) return false;
        String lower = paymentTypeValue.toLowerCase();
        return lower.contains("chuyển khoản") || lower.contains("chuyen khoan")
                || lower.contains("bank transfer");
    }

    private List<OrderLineDetailDTO> buildLineDetails(List<OrderLine> lines) {
        if (lines.isEmpty()) return Collections.emptyList();

        List<Integer> stockIds = lines.stream()
                .map(OrderLine::getVariantStockId).distinct().collect(Collectors.toList());

        Map<Integer, VariantStock> stockMap = variantStockRepository.findAllById(stockIds)
                .stream().collect(Collectors.toMap(VariantStock::getId, Function.identity()));

        List<Integer> variantIds = stockMap.values().stream()
                .map(VariantStock::getVariantId).distinct().collect(Collectors.toList());

        Map<Integer, ProductVariant> variantMap = productVariantRepository.findAllById(variantIds)
                .stream().collect(Collectors.toMap(ProductVariant::getId, Function.identity()));

        List<Integer> productIds = variantMap.values().stream()
                .map(ProductVariant::getProductId).distinct().collect(Collectors.toList());

        Map<Integer, Product> productMap = productRepository.findAllById(productIds)
                .stream().collect(Collectors.toMap(Product::getId, Function.identity()));

        List<Integer> colorIds = variantMap.values().stream()
                .map(ProductVariant::getColorId).distinct().collect(Collectors.toList());

        Map<Integer, Color> colorMap = colorRepository.findAllById(colorIds)
                .stream().collect(Collectors.toMap(Color::getId, Function.identity()));

        List<Integer> sizeIds = stockMap.values().stream()
                .map(VariantStock::getSizeId).distinct().collect(Collectors.toList());

        Map<Integer, Size> sizeMap = sizeRepository.findAllById(sizeIds)
                .stream().collect(Collectors.toMap(Size::getId, Function.identity()));

        return lines.stream()
                .map(line -> toLineDetailDTO(line, stockMap, variantMap, productMap, colorMap, sizeMap))
                .collect(Collectors.toList());
    }

    private OrderLineDetailDTO toLineDetailDTO(OrderLine line,
                                               Map<Integer, VariantStock> stockMap,
                                               Map<Integer, ProductVariant> variantMap,
                                               Map<Integer, Product> productMap,
                                               Map<Integer, Color> colorMap,
                                               Map<Integer, Size> sizeMap) {
        OrderLineDetailDTO dto = new OrderLineDetailDTO();
        dto.setId(line.getId());
        dto.setVariantStockId(line.getVariantStockId());
        dto.setQty(line.getQty());
        dto.setPrice(line.getPrice());
        dto.setSubtotal(line.getPrice() != null && line.getQty() != null
                ? line.getPrice() * line.getQty() : null);

        VariantStock stock = stockMap.get(line.getVariantStockId());
        if (stock != null) {
            dto.setSku(stock.getSku());

            ProductVariant variant = variantMap.get(stock.getVariantId());
            if (variant != null) {
                dto.setVariantId(variant.getId());
                dto.setColorImageUrl(variant.getColorImageUrl());

                Product product = productMap.get(variant.getProductId());
                if (product != null) {
                    dto.setProductId(product.getId());
                    dto.setProductName(product.getName());
                    dto.setProductSlug(product.getSlug());
                }

                Color color = colorMap.get(variant.getColorId());
                if (color != null) {
                    dto.setColorName(color.getName());
                    dto.setColorHex(color.getHexCode());
                }
            }

            Size size = sizeMap.get(stock.getSizeId());
            if (size != null) {
                dto.setSizeLabel(size.getLabel());
                dto.setSizeType(size.getType());
            }
        }
        return dto;
    }
}
