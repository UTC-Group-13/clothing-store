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

import java.time.LocalDateTime;
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
    private final UserPaymentMethodRepository paymentMethodRepository;
    private final ShoppingCartRepository    cartRepository;
    private final ShoppingCartItemRepository cartItemRepository;
    private final VariantStockRepository    variantStockRepository;
    private final ProductVariantRepository  productVariantRepository;
    private final ProductRepository         productRepository;
    private final ColorRepository           colorRepository;
    private final SizeRepository            sizeRepository;
    private final SiteUserRepository        userRepository;

    // =====================================================================
    //  USER operations
    // =====================================================================

    @Override
    @Transactional
    public OrderDetailDTO placeOrder(String username, OrderRequest request) {
        SiteUser user = getUser(username);

        // 1. Kiem tra payment method thuoc ve user
        paymentMethodRepository.findByIdAndUserId(request.getPaymentMethodId(), user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("order.paymentNotFound",
                        request.getPaymentMethodId()));

        // 2. Kiem tra dia chi giao hang thuoc ve user
        if (!userAddressRepository.existsByUserIdAndAddressId(user.getId(), request.getShippingAddressId())) {
            throw new BusinessException("order.addressNotBelongToUser", request.getShippingAddressId());
        }

        // 3. Kiem tra phuong thuc van chuyen
        ShippingMethod shippingMethod = shippingMethodRepository.findById(request.getShippingMethodId())
                .orElseThrow(() -> new ResourceNotFoundException("shipping.notFound",
                        request.getShippingMethodId()));

        // 4. Lay gio hang
        ShoppingCart cart = cartRepository.findByUserId(user.getId())
                .orElseThrow(() -> new BusinessException("order.cartEmpty"));

        List<ShoppingCartItem> cartItems = cartItemRepository.findByCartId(cart.getId());
        if (cartItems.isEmpty()) {
            throw new BusinessException("order.cartEmpty");
        }

        // 5. Kiem tra ton kho va tinh toan subtotal
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

        // 6. Lay trang thai PENDING
        OrderStatus pendingStatus = orderStatusRepository
                .findByStatus(OrderStatusServiceImpl.STATUS_PENDING)
                .orElseThrow(() -> new BusinessException("order.pendingStatusNotConfigured"));

        // 7. Tao don hang
        int orderTotal = subtotal + (shippingMethod.getPrice() != null ? shippingMethod.getPrice() : 0);

        ShopOrder order = new ShopOrder();
        order.setUserId(user.getId());
        order.setOrderDate(LocalDateTime.now());
        order.setPaymentMethodId(request.getPaymentMethodId());
        order.setShippingAddress(request.getShippingAddressId());
        order.setShippingMethod(request.getShippingMethodId());
        order.setOrderTotal(orderTotal);
        order.setOrderStatus(pendingStatus.getId());
        order = orderRepository.save(order);

        // 8. Tao order lines va tru ton kho
        for (ShoppingCartItem cartItem : cartItems) {
            VariantStock stock = stockMap.get(cartItem.getVariantStockId());
            int unitPrice = resolveUnitPrice(stock, productByVariantId);

            OrderLine line = new OrderLine();
            line.setOrderId(order.getId());
            line.setVariantStockId(cartItem.getVariantStockId());
            line.setQty(cartItem.getQty());
            line.setPrice(unitPrice);
            orderLineRepository.save(line);

            // Tru ton kho
            stock.setStockQty(stock.getStockQty() - cartItem.getQty());
            variantStockRepository.save(stock);
        }

        // 9. Lam trong gio hang
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

        // Chi duoc huy khi trang thai la PENDING
        OrderStatus currentStatus = orderStatusRepository.findById(order.getOrderStatus()).orElse(null);
        if (currentStatus == null
                || !OrderStatusServiceImpl.STATUS_PENDING.equalsIgnoreCase(currentStatus.getStatus())) {
            throw new BusinessException("order.cannotCancel");
        }

        // Khoi phuc ton kho
        restoreStock(order.getId());

        // Cap nhat trang thai → CANCELLED
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
     * Giai quyet don gia: price_override neu co, nguoc lai dung base_price
     */
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

    /**
     * Build map: variantId → Product (for price resolution)
     */
    private Map<Integer, Product> buildProductByVariantIdMap(Map<Integer, VariantStock> stockMap) {
        List<Integer> variantIds = stockMap.values().stream()
                .map(VariantStock::getVariantId).distinct().collect(Collectors.toList());

        Map<Integer, ProductVariant> variantMap = productVariantRepository.findAllById(variantIds)
                .stream().collect(Collectors.toMap(ProductVariant::getId, Function.identity()));

        List<Integer> productIds = variantMap.values().stream()
                .map(ProductVariant::getProductId).distinct().collect(Collectors.toList());

        Map<Integer, Product> productMap = productRepository.findAllById(productIds)
                .stream().collect(Collectors.toMap(Product::getId, Function.identity()));

        // Map: variantId → Product
        return variantMap.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> productMap.getOrDefault(e.getValue().getProductId(), new Product())));
    }

    /**
     * Khoi phuc ton kho khi huy don hang.
     */
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
     * Tao OrderDetailDTO day du tu ShopOrder (batch-load moi thu).
     */
    private OrderDetailDTO buildOrderDetail(ShopOrder order) {
        OrderDetailDTO dto = new OrderDetailDTO();
        dto.setId(order.getId());
        dto.setUserId(order.getUserId());
        dto.setOrderDate(order.getOrderDate());
        dto.setPaymentMethodId(order.getPaymentMethodId());
        dto.setShippingAddressId(order.getShippingAddress());

        // Trang thai
        if (order.getOrderStatus() != null) {
            orderStatusRepository.findById(order.getOrderStatus()).ifPresent(s -> {
                dto.setStatusId(s.getId());
                dto.setStatusName(s.getStatus());
            });
        }

        // Phuong thuc van chuyen
        if (order.getShippingMethod() != null) {
            shippingMethodRepository.findById(order.getShippingMethod()).ifPresent(sm -> {
                dto.setShippingMethodId(sm.getId());
                dto.setShippingMethodName(sm.getName());
                dto.setShippingFee(sm.getPrice());
            });
        }

        // Dia chi giao hang
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

        // Tinh toan tong tien
        int subtotal = lineDetails.stream()
                .mapToInt(l -> l.getSubtotal() != null ? l.getSubtotal() : 0).sum();
        dto.setSubtotal(subtotal);
        dto.setOrderTotal(order.getOrderTotal());

        return dto;
    }

    /**
     * Enrich danh sach order lines voi thong tin san pham (batch-load).
     */
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

