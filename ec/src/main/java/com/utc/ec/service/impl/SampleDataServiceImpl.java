package com.utc.ec.service.impl;

import com.utc.ec.entity.*;
import com.utc.ec.repository.*;
import com.utc.ec.service.SampleDataService;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class SampleDataServiceImpl implements SampleDataService {

    private final EntityManager entityManager;
    private final CategoryRepository categoryRepo;
    private final ColorRepository colorRepo;
    private final SizeRepository sizeRepo;
    private final ProductRepository productRepo;
    private final ProductVariantRepository variantRepo;
    private final VariantStockRepository stockRepo;
    private final PaymentTypeRepository paymentTypeRepo;
    private final ShippingMethodRepository shippingMethodRepo;
    private final OrderStatusRepository orderStatusRepo;
    private final ShopBankAccountRepository shopBankAccountRepo;
    private final UserReviewRepository userReviewRepo;
    private final OrderLineRepository orderLineRepo;
    private final ShopOrderRepository shopOrderRepo;
    private final ShoppingCartItemRepository cartItemRepo;

    @Override
    @Transactional
    public String generateSampleData() {
        log.info("=== BẮT ĐẦU TẠO DỮ LIỆU MẪU ===");

        // Xóa dữ liệu cũ (theo đúng thứ tự FK - từ con đến cha)
        log.info("Đang xóa dữ liệu cũ...");
        userReviewRepo.deleteAll();
        orderLineRepo.deleteAll();
        shopOrderRepo.deleteAll();
        cartItemRepo.deleteAll();
        stockRepo.deleteAll();
        variantRepo.deleteAll();
        productRepo.deleteAll();
        sizeRepo.deleteAll();
        colorRepo.deleteAll();
        categoryRepo.deleteAll();
        shopBankAccountRepo.deleteAll();
        shippingMethodRepo.deleteAll();
        paymentTypeRepo.deleteAll();
        orderStatusRepo.deleteAll();
        
        // CRITICAL: Flush và clear persistence context để đảm bảo data thực sự bị xóa
        entityManager.flush();
        entityManager.clear();
        log.info("✓ Đã xóa sạch dữ liệu cũ.");

        // 1. Tạo danh mục sản phẩm
        Map<String, Category> cats = createCategories();
        log.info("✓ Đã tạo {} danh mục.", cats.size());

        // 2. Tạo màu sắc
        Map<String, Color> colors = createColors();
        log.info("✓ Đã tạo {} màu sắc.", colors.size());

        // 3. Tạo sizes
        Map<String, Size> sizes = createSizes();
        log.info("✓ Đã tạo {} sizes.", sizes.size());

        // 4. Tạo trạng thái đơn hàng (dữ liệu hệ thống - cần có trước)
        int totalOrderStatuses = createOrderStatuses();
        log.info("✓ Đã tạo {} trạng thái đơn hàng.", totalOrderStatuses);

        // 5. Tạo loại thanh toán
        int totalPaymentTypes = createPaymentTypes();
        log.info("✓ Đã tạo {} loại thanh toán.", totalPaymentTypes);

        // 6. Tạo phương thức vận chuyển
        int totalShippingMethods = createShippingMethods();
        log.info("✓ Đã tạo {} phương thức vận chuyển.", totalShippingMethods);

        // 7. Tạo tài khoản ngân hàng shop
        int totalBankAccounts = createShopBankAccounts();
        log.info("✓ Đã tạo {} tài khoản ngân hàng shop.", totalBankAccounts);

        // 8. Tạo 50 sản phẩm + biến thể + tồn kho
        int totalProducts = createProducts(cats, colors, sizes);
        log.info("✓ Đã tạo {} sản phẩm.", totalProducts);

        long totalVariants = variantRepo.count();
        long totalStocks = stockRepo.count();

        String result = String.format(
                "✅ TẠO DỮ LIỆU MẪU THÀNH CÔNG!\n\n" +
                "📊 THỐNG KÊ:\n" +
                "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n" +
                "  • Danh mục sản phẩm:        %,d\n" +
                "  • Màu sắc:                  %,d\n" +
                "  • Sizes:                    %,d\n" +
                "  • Trạng thái đơn hàng:      %,d\n" +
                "  • Loại thanh toán:          %,d\n" +
                "  • Phương thức vận chuyển:   %,d\n" +
                "  • Tài khoản ngân hàng shop: %,d\n" +
                "  • Sản phẩm:                 %,d\n" +
                "  • Biến thể (product×color): %,d\n" +
                "  • Tồn kho (variant×size):   %,d\n" +
                "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━",
                cats.size(), colors.size(), sizes.size(),
                totalOrderStatuses, totalPaymentTypes, totalShippingMethods, totalBankAccounts,
                totalProducts, totalVariants, totalStocks);

        log.info("=== HOÀN TẤT TẠO DỮ LIỆU MẪU ===");
        return result;
    }

    // ========================================================================
    //  1. CATEGORIES
    // ========================================================================
    private Map<String, Category> createCategories() {
        Map<String, Category> map = new LinkedHashMap<>();

        // Danh mục gốc
        Category ao       = saveCat("Áo", "ao", null, "Tất cả các loại áo");
        Category quan      = saveCat("Quần", "quan", null, "Tất cả các loại quần");
        Category vay       = saveCat("Váy & Đầm", "vay-dam", null, "Các loại váy và đầm");
        Category doLot     = saveCat("Đồ Lót", "do-lot", null, "Đồ lót nam nữ");
        Category phukien   = saveCat("Phụ Kiện", "phu-kien", null, "Phụ kiện thời trang");

        // Danh mục con — Áo
        Category aoThun    = saveCat("Áo Thun", "ao-thun", ao.getId(), "Áo thun nam nữ");
        Category aoSomi    = saveCat("Áo Sơ Mi", "ao-so-mi", ao.getId(), "Áo sơ mi công sở & casual");
        Category aoKhoac   = saveCat("Áo Khoác", "ao-khoac", ao.getId(), "Áo khoác gió, bomber, hoodie");
        Category aoPolo    = saveCat("Áo Polo", "ao-polo", ao.getId(), "Áo polo thể thao & smart casual");
        Category aoHoodie  = saveCat("Áo Hoodie & Sweater", "ao-hoodie-sweater", ao.getId(), "Áo hoodie và sweater");
        Category aoLen     = saveCat("Áo Len", "ao-len", ao.getId(), "Áo len mùa đông");

        // Danh mục con — Quần
        Category quanJean  = saveCat("Quần Jean", "quan-jean", quan.getId(), "Quần jean nam nữ");
        Category quanKaki  = saveCat("Quần Kaki", "quan-kaki", quan.getId(), "Quần kaki công sở");
        Category quanShort = saveCat("Quần Short", "quan-short", quan.getId(), "Quần short nam nữ");
        Category quanJogger= saveCat("Quần Jogger", "quan-jogger", quan.getId(), "Quần jogger thể thao");
        Category quanTay   = saveCat("Quần Tây", "quan-tay", quan.getId(), "Quần tây công sở");

        // Danh mục con — Váy
        Category vayNgan   = saveCat("Váy Ngắn", "vay-ngan", vay.getId(), "Váy ngắn thời trang");
        Category damDai    = saveCat("Đầm Dài", "dam-dai", vay.getId(), "Đầm dài dự tiệc & dạo phố");

        map.put("ao-thun", aoThun);         map.put("ao-so-mi", aoSomi);
        map.put("ao-khoac", aoKhoac);       map.put("ao-polo", aoPolo);
        map.put("ao-hoodie-sweater", aoHoodie); map.put("ao-len", aoLen);
        map.put("quan-jean", quanJean);     map.put("quan-kaki", quanKaki);
        map.put("quan-short", quanShort);   map.put("quan-jogger", quanJogger);
        map.put("quan-tay", quanTay);
        map.put("vay-ngan", vayNgan);       map.put("dam-dai", damDai);
        map.put("do-lot", doLot);           map.put("phu-kien", phukien);

        return map;
    }

    private Category saveCat(String name, String slug, Integer parentId, String desc) {
        Category c = new Category();
        c.setName(name);
        c.setSlug(slug);
        c.setParentId(parentId);
        c.setDescription(desc);
        return categoryRepo.save(c);
    }

    // ========================================================================
    //  2. COLORS
    // ========================================================================
    private Map<String, Color> createColors() {
        Map<String, Color> map = new LinkedHashMap<>();
        map.put("den",    saveColor("Đen",       "#000000", "den"));
        map.put("trang",  saveColor("Trắng",     "#FFFFFF", "trang"));
        map.put("xam",    saveColor("Xám",       "#808080", "xam"));
        map.put("xanh-navy", saveColor("Xanh Navy", "#000080", "xanh-navy"));
        map.put("xanh-duong", saveColor("Xanh Dương", "#1E90FF", "xanh-duong"));
        map.put("xanh-la",  saveColor("Xanh Lá",  "#228B22", "xanh-la"));
        map.put("do",     saveColor("Đỏ",        "#DC143C", "do"));
        map.put("hong",   saveColor("Hồng",      "#FF69B4", "hong"));
        map.put("vang",   saveColor("Vàng",      "#FFD700", "vang"));
        map.put("be",     saveColor("Be",        "#F5F5DC", "be"));
        map.put("nau",    saveColor("Nâu",       "#8B4513", "nau"));
        map.put("cam",    saveColor("Cam",       "#FF8C00", "cam"));
        map.put("tim",    saveColor("Tím",       "#800080", "tim"));
        map.put("reu",    saveColor("Rêu",       "#556B2F", "reu"));
        return map;
    }

    private Color saveColor(String name, String hex, String slug) {
        Color c = new Color();
        c.setName(name);
        c.setHexCode(hex);
        c.setSlug(slug);
        return colorRepo.save(c);
    }

    // ========================================================================
    //  3. SIZES
    // ========================================================================
    private Map<String, Size> createSizes() {
        Map<String, Size> map = new LinkedHashMap<>();
        // Clothing sizes
        int i = 0;
        for (String label : new String[]{"XS", "S", "M", "L", "XL", "XXL", "3XL"}) {
            map.put("c-" + label, saveSize(label, "clothing", i++));
        }
        // Numeric sizes (quần)
        i = 0;
        for (String label : new String[]{"26", "27", "28", "29", "30", "31", "32", "33", "34", "36"}) {
            map.put("n-" + label, saveSize(label, "numeric", i++));
        }
        // Freesize (phụ kiện, đồ lót một cỡ)
        map.put("f-Freesize", saveSize("Freesize", "freesize", 0));
        return map;
    }

    private Size saveSize(String label, String type, int sortOrder) {
        Size s = new Size();
        s.setLabel(label);
        s.setType(type);
        s.setSortOrder(sortOrder);
        return sizeRepo.save(s);
    }

    // ========================================================================
    //  4. ORDER STATUSES (dữ liệu hệ thống - bắt buộc cho order workflow)
    // ========================================================================
    private int createOrderStatuses() {
        // Don't delete here - already deleted at start
        
        String[] statuses = {
            "PENDING",      // Chờ xử lý
            "PROCESSING",   // Đang xử lý
            "SHIPPED",      // Đang giao hàng
            "DELIVERED",    // Đã giao hàng
            "CANCELLED"     // Đã hủy
        };

        for (String statusName : statuses) {
            OrderStatus status = new OrderStatus();
            status.setStatus(statusName);
            orderStatusRepo.save(status);
        }

        return statuses.length;
    }

    // ========================================================================
    //  5. PAYMENT TYPES  (chỉ 2 loại hệ thống hỗ trợ)
    //     - COD: thanh toán khi nhận hàng
    //     - Chuyển khoản ngân hàng: trả QR VietQR, ADMIN kiểm tra thủ công
    // ========================================================================
    private int createPaymentTypes() {
        // Don't delete here - already deleted at start
        
        PaymentType cod = new PaymentType();
        cod.setValue("COD");
        paymentTypeRepo.save(cod);

        PaymentType transfer = new PaymentType();
        transfer.setValue("Chuyển khoản ngân hàng");
        paymentTypeRepo.save(transfer);

        return 2;
    }

    // ========================================================================
    //  6. SHIPPING METHODS
    // ========================================================================
    private int createShippingMethods() {
        // Don't delete here - already deleted at start
        
        Object[][] methods = {
            {"Giao hàng tiêu chuẩn",  20000},
            {"Giao hàng nhanh",        35000},
            {"Giao hàng hỏa tốc",     60000},
            {"Nhận tại cửa hàng",          0}
        };

        for (Object[] m : methods) {
            ShippingMethod sm = new ShippingMethod();
            sm.setName((String) m[0]);
            sm.setPrice((Integer) m[1]);
            shippingMethodRepo.save(sm);
        }
        return methods.length;
    }

    // ========================================================================
    //  7. SHOP BANK ACCOUNTS (tài khoản ngân hàng shop — dùng cho QR)
    // ========================================================================
    private int createShopBankAccounts() {
        // Don't delete here - already deleted at start
        
        ShopBankAccount mb = new ShopBankAccount();
        mb.setBankId("MB");
        mb.setBankName("Ngân hàng TMCP Quân Đội (MB Bank)");
        mb.setAccountNumber("0365123456");
        mb.setAccountHolderName("NGUYEN VAN A");
        mb.setIsActive(true);
        shopBankAccountRepo.save(mb);

        ShopBankAccount vcb = new ShopBankAccount();
        vcb.setBankId("VCB");
        vcb.setBankName("Ngân hàng TMCP Ngoại thương (Vietcombank)");
        vcb.setAccountNumber("1021234567");
        vcb.setAccountHolderName("NGUYEN VAN A");
        vcb.setIsActive(false);
        shopBankAccountRepo.save(vcb);

        return 2;
    }

    // ========================================================================
    //  8. PRODUCTS (50 sản phẩm) + VARIANTS + STOCKS
    // ========================================================================
    private int createProducts(Map<String, Category> cats,
                               Map<String, Color> colors,
                               Map<String, Size> sizes) {

        // Ảnh sản phẩm public (Unsplash — free to use)
        // Áo thun
        String imgAoThun1  = "https://images.unsplash.com/photo-1521572163474-6864f9cf17ab?w=600";
        String imgAoThun2  = "https://images.unsplash.com/photo-1583743814966-8936f5b7be1a?w=600";
        String imgAoThun3  = "https://images.unsplash.com/photo-1562157873-818bc0726f68?w=600";
        // Áo sơ mi
        String imgSoMi1    = "https://images.unsplash.com/photo-1596755094514-f87e34085b2c?w=600";
        String imgSoMi2    = "https://images.unsplash.com/photo-1602810318383-e386cc2a3ccf?w=600";
        // Áo khoác
        String imgKhoac1   = "https://images.unsplash.com/photo-1551028719-00167b16eac5?w=600";
        String imgKhoac2   = "https://images.unsplash.com/photo-1544923246-77307dd270b5?w=600";
        // Áo polo
        String imgPolo1    = "https://images.unsplash.com/photo-1625910513413-5fc421e0fd4f?w=600";
        String imgPolo2    = "https://images.unsplash.com/photo-1586790170083-2f9ceadc732d?w=600";
        // Hoodie
        String imgHoodie1  = "https://images.unsplash.com/photo-1556821840-3a63f95609a7?w=600";
        String imgHoodie2  = "https://images.unsplash.com/photo-1578768079470-a84f7dbd25f5?w=600";
        // Áo len
        String imgLen1     = "https://images.unsplash.com/photo-1576566588028-4147f3842f27?w=600";
        // Quần jean
        String imgJean1    = "https://images.unsplash.com/photo-1542272604-787c3835535d?w=600";
        String imgJean2    = "https://images.unsplash.com/photo-1604176354204-9268737828e4?w=600";
        // Quần kaki
        String imgKaki1    = "https://images.unsplash.com/photo-1624378439575-d8705ad7ae80?w=600";
        // Quần short
        String imgShort1   = "https://images.unsplash.com/photo-1591195853828-11db59a44f6b?w=600";
        String imgShort2   = "https://images.unsplash.com/photo-1617952739858-28043cbb4168?w=600";
        // Quần jogger
        String imgJogger1  = "https://images.unsplash.com/photo-1580906853149-f29d0e57b4b9?w=600";
        // Quần tây
        String imgTay1     = "https://images.unsplash.com/photo-1594938298603-c8148c4dae35?w=600";
        // Váy
        String imgVay1     = "https://images.unsplash.com/photo-1595777457583-95e059d581b8?w=600";
        String imgVay2     = "https://images.unsplash.com/photo-1572804013309-59a88b7e92f1?w=600";
        // Đầm dài
        String imgDam1     = "https://images.unsplash.com/photo-1496747611176-843222e1e57c?w=600";
        String imgDam2     = "https://images.unsplash.com/photo-1612336307429-8a898d10e223?w=600";
        // Đồ lót
        String imgDoLot1   = "https://images.unsplash.com/photo-1617331721458-bd3bd3f9c7f8?w=600";
        // Phụ kiện
        String imgPhuKien1 = "https://images.unsplash.com/photo-1556306535-0f09a537f0a3?w=600";
        String imgPhuKien2 = "https://images.unsplash.com/photo-1589782182703-2aaa69037b5b?w=600";

        // Clothing sizes & numeric sizes
        List<Size> clothingSizes = List.of(
            sizes.get("c-S"), sizes.get("c-M"), sizes.get("c-L"), sizes.get("c-XL"), sizes.get("c-XXL")
        );
        List<Size> numericSizes = List.of(
            sizes.get("n-28"), sizes.get("n-29"), sizes.get("n-30"), sizes.get("n-31"), sizes.get("n-32"), sizes.get("n-34")
        );
        // Dùng clothing sizes cho váy / đầm
        List<Size> dressSizes = List.of(
            sizes.get("c-XS"), sizes.get("c-S"), sizes.get("c-M"), sizes.get("c-L"), sizes.get("c-XL")
        );
        // Freesize cho phụ kiện
        List<Size> freeSizes = List.of(
            sizes.get("f-Freesize")
        );

        List<ProductDef> defs = new ArrayList<>();

        // ─── ÁO THUN (8 sản phẩm) ────────────────────────────────
        defs.add(new ProductDef("Áo Thun Trơn Cotton Basic", "ao-thun-tron-cotton-basic",
                "Áo thun cổ tròn cotton 100%, mềm mại thoáng mát, phù hợp mặc hàng ngày.",
                "ao-thun", 149000, "YODY", "Cotton 100%",
                new String[]{"den","trang","xam","xanh-navy"}, clothingSizes, imgAoThun1));

        defs.add(new ProductDef("Áo Thun Nam Cổ Tròn Premium", "ao-thun-nam-co-tron-premium",
                "Áo thun nam chất liệu cotton compact co giãn, form regular fit.",
                "ao-thun", 199000, "Coolmate", "Cotton Compact",
                new String[]{"den","trang","xanh-duong"}, clothingSizes, imgAoThun2));

        defs.add(new ProductDef("Áo Thun Nữ Baby Tee", "ao-thun-nu-baby-tee",
                "Áo thun nữ kiểu baby tee ôm nhẹ, chất cotton mịn.",
                "ao-thun", 129000, "YODY", "Cotton",
                new String[]{"trang","hong","den"}, clothingSizes, imgAoThun3));

        defs.add(new ProductDef("Áo Thun Unisex Oversize", "ao-thun-unisex-oversize",
                "Áo thun oversize unisex, chất vải dày dặn, form rộng thoải mái.",
                "ao-thun", 179000, "Routine", "Cotton French Terry",
                new String[]{"den","trang","xam","be"}, clothingSizes, imgAoThun1));

        defs.add(new ProductDef("Áo Thun In Hình Graphic", "ao-thun-in-hinh-graphic",
                "Áo thun in hình phong cách streetwear, chất liệu cotton co giãn.",
                "ao-thun", 219000, "Routine", "Cotton Spandex",
                new String[]{"den","trang","xanh-navy"}, clothingSizes, imgAoThun2));

        defs.add(new ProductDef("Áo Thun Thể Thao Dry-Fit", "ao-thun-the-thao-dry-fit",
                "Áo thun thể thao chất liệu dry-fit, thấm hút mồ hôi nhanh.",
                "ao-thun", 249000, "Coolmate", "Polyester Dry-Fit",
                new String[]{"den","xanh-duong","do"}, clothingSizes, imgAoThun3));

        defs.add(new ProductDef("Áo Thun Cổ V Nam", "ao-thun-co-v-nam",
                "Áo thun cổ V chất liệu cotton pha, mát mẻ thoải mái.",
                "ao-thun", 169000, "YODY", "Cotton Polyester",
                new String[]{"den","trang","xam"}, clothingSizes, imgAoThun1));

        defs.add(new ProductDef("Áo Thun Raglan Phối Màu", "ao-thun-raglan-phoi-mau",
                "Áo thun raglan tay phối màu, phong cách trẻ trung năng động.",
                "ao-thun", 189000, "Routine", "Cotton",
                new String[]{"xanh-navy","do","xanh-la"}, clothingSizes, imgAoThun2));

        // ─── ÁO SƠ MI (5 sản phẩm) ──────────────────────────────
        defs.add(new ProductDef("Áo Sơ Mi Trắng Công Sở", "ao-so-mi-trang-cong-so",
                "Áo sơ mi trắng form slim fit, chất cotton không nhăn, phù hợp đi làm.",
                "ao-so-mi", 349000, "Owen", "Cotton Non-Iron",
                new String[]{"trang"}, clothingSizes, imgSoMi1));

        defs.add(new ProductDef("Áo Sơ Mi Oxford Nam", "ao-so-mi-oxford-nam",
                "Áo sơ mi Oxford dài tay, chất vải dày dặn, button-down collar.",
                "ao-so-mi", 399000, "Owen", "Oxford Cotton",
                new String[]{"trang","xanh-duong","xanh-navy"}, clothingSizes, imgSoMi2));

        defs.add(new ProductDef("Áo Sơ Mi Linen Cộc Tay", "ao-so-mi-linen-coc-tay",
                "Áo sơ mi linen ngắn tay, thoáng mát mùa hè, form relaxed.",
                "ao-so-mi", 329000, "YODY", "Linen",
                new String[]{"trang","be","xanh-la"}, clothingSizes, imgSoMi1));

        defs.add(new ProductDef("Áo Sơ Mi Flannel Kẻ Caro", "ao-so-mi-flannel-ke-caro",
                "Áo sơ mi flannel kẻ caro dài tay, giữ ấm mùa đông, phong cách vintage.",
                "ao-so-mi", 379000, "Routine", "Flannel Cotton",
                new String[]{"do","xanh-navy","reu"}, clothingSizes, imgSoMi2));

        defs.add(new ProductDef("Áo Sơ Mi Nữ Cổ Bow", "ao-so-mi-nu-co-bow",
                "Áo sơ mi nữ cổ nơ thanh lịch, chất liệu lụa mềm rũ.",
                "ao-so-mi", 359000, "IVY moda", "Lụa tổng hợp",
                new String[]{"trang","hong","den"}, clothingSizes, imgSoMi1));

        // ─── ÁO KHOÁC (5 sản phẩm) ──────────────────────────────
        defs.add(new ProductDef("Áo Khoác Gió Chống Nước", "ao-khoac-gio-chong-nuoc",
                "Áo khoác gió 2 lớp, chống nước nhẹ, có mũ trùm đầu.",
                "ao-khoac", 499000, "Routine", "Nylon chống nước",
                new String[]{"den","xanh-navy","reu"}, clothingSizes, imgKhoac1));

        defs.add(new ProductDef("Áo Khoác Bomber Nam", "ao-khoac-bomber-nam",
                "Áo bomber jacket phong cách Hàn Quốc, khóa kéo YKK, lót lưới thoáng.",
                "ao-khoac", 599000, "Coolmate", "Polyester",
                new String[]{"den","xanh-navy","reu"}, clothingSizes, imgKhoac2));

        defs.add(new ProductDef("Áo Khoác Denim Classic", "ao-khoac-denim-classic",
                "Áo khoác jean denim washed phong cách Mỹ, đường may chắc chắn.",
                "ao-khoac", 549000, "Routine", "Denim Cotton",
                new String[]{"xanh-duong","den"}, clothingSizes, imgKhoac1));

        defs.add(new ProductDef("Áo Khoác Cardigan Len Mỏng", "ao-khoac-cardigan-len-mong",
                "Cardigan len mỏng phối nút, thích hợp mùa thu, dễ phối đồ.",
                "ao-khoac", 429000, "YODY", "Len Acrylic",
                new String[]{"den","xam","be","nau"}, clothingSizes, imgKhoac2));

        defs.add(new ProductDef("Áo Khoác Phao Siêu Nhẹ", "ao-khoac-phao-sieu-nhe",
                "Áo phao lông vũ siêu nhẹ, gấp gọn được, giữ ấm tối đa.",
                "ao-khoac", 799000, "Uniqlo", "Nylon/Down",
                new String[]{"den","xanh-navy","do"}, clothingSizes, imgKhoac1));

        // ─── ÁO POLO (3 sản phẩm) ──────────────────────────────
        defs.add(new ProductDef("Áo Polo Pique Classic", "ao-polo-pique-classic",
                "Áo polo pique cổ bẻ, thêu logo ngực, form regular fit.",
                "ao-polo", 279000, "Coolmate", "Cotton Pique",
                new String[]{"den","trang","xanh-navy","do"}, clothingSizes, imgPolo1));

        defs.add(new ProductDef("Áo Polo Thể Thao Dry-Fit", "ao-polo-the-thao-dry-fit",
                "Áo polo thể thao chất dry-fit, thấm hút tốt, thích hợp chơi golf.",
                "ao-polo", 329000, "Coolmate", "Polyester Dry-Fit",
                new String[]{"den","trang","xanh-duong"}, clothingSizes, imgPolo2));

        defs.add(new ProductDef("Áo Polo Nữ Croptop", "ao-polo-nu-croptop",
                "Áo polo nữ dáng croptop trẻ trung, chất cotton co giãn.",
                "ao-polo", 259000, "IVY moda", "Cotton Spandex",
                new String[]{"trang","hong","den"}, clothingSizes, imgPolo1));

        // ─── HOODIE & SWEATER (4 sản phẩm) ──────────────────────
        defs.add(new ProductDef("Hoodie Nỉ Bông Oversize", "hoodie-ni-bong-oversize",
                "Hoodie nỉ bông dày dặn, form oversize, có mũ trùm và túi kangaroo.",
                "ao-hoodie-sweater", 399000, "Routine", "French Terry Fleece",
                new String[]{"den","xam","xanh-navy","be"}, clothingSizes, imgHoodie1));

        defs.add(new ProductDef("Sweater Cổ Tròn Basic", "sweater-co-tron-basic",
                "Sweater cổ tròn basic, chất nỉ mịn nhẹ, phù hợp layering.",
                "ao-hoodie-sweater", 349000, "Coolmate", "Cotton Fleece",
                new String[]{"den","trang","xam","reu"}, clothingSizes, imgHoodie2));

        defs.add(new ProductDef("Hoodie Zip Thể Thao", "hoodie-zip-the-thao",
                "Hoodie kéo khóa zip, chất vải dry-fit bên trong, nỉ bên ngoài.",
                "ao-hoodie-sweater", 449000, "Coolmate", "Polyester Fleece",
                new String[]{"den","xanh-navy"}, clothingSizes, imgHoodie1));

        defs.add(new ProductDef("Sweater Len Cổ Lọ", "sweater-len-co-lo",
                "Sweater len cổ lọ (turtleneck), giữ ấm tuyệt vời mùa đông.",
                "ao-hoodie-sweater", 479000, "YODY", "Len Merino pha",
                new String[]{"den","xam","nau","reu"}, clothingSizes, imgLen1));

        // ─── ÁO LEN (2 sản phẩm) ────────────────────────────────
        defs.add(new ProductDef("Áo Len Mỏng Cổ V", "ao-len-mong-co-v",
                "Áo len mỏng cổ V, dễ phối đồ công sở hoặc casual.",
                "ao-len", 329000, "Uniqlo", "Len Merino Extra Fine",
                new String[]{"den","xam","xanh-navy","nau"}, clothingSizes, imgLen1));

        defs.add(new ProductDef("Áo Len Dệt Kim Vintage", "ao-len-det-kim-vintage",
                "Áo len dệt kim họa tiết vintage, phong cách retro Hàn Quốc.",
                "ao-len", 369000, "Routine", "Acrylic Knit",
                new String[]{"be","reu","nau"}, clothingSizes, imgLen1));

        // ─── QUẦN JEAN (5 sản phẩm) ─────────────────────────────
        defs.add(new ProductDef("Quần Jean Nam Slim Fit", "quan-jean-nam-slim-fit",
                "Quần jean nam form slim fit co giãn nhẹ, wash nhẹ phong cách.",
                "quan-jean", 449000, "Routine", "Denim Cotton Spandex",
                new String[]{"xanh-duong","den","xanh-navy"}, numericSizes, imgJean1));

        defs.add(new ProductDef("Quần Jean Nữ Skinny", "quan-jean-nu-skinny",
                "Quần jean nữ skinny ôm chân, co giãn 4 chiều, tôn dáng.",
                "quan-jean", 399000, "IVY moda", "Denim Stretch",
                new String[]{"xanh-duong","den"}, numericSizes, imgJean2));

        defs.add(new ProductDef("Quần Jean Relaxed Fit", "quan-jean-relaxed-fit",
                "Quần jean relaxed fit rộng thoải mái, kiểu dáng vintage 90s.",
                "quan-jean", 479000, "Routine", "Denim Heavy Cotton",
                new String[]{"xanh-duong","xam"}, numericSizes, imgJean1));

        defs.add(new ProductDef("Quần Jean Baggy Nữ", "quan-jean-baggy-nu",
                "Quần jean baggy nữ ống rộng, dáng high waist trẻ trung.",
                "quan-jean", 429000, "IVY moda", "Denim Cotton",
                new String[]{"xanh-duong","trang","den"}, numericSizes, imgJean2));

        defs.add(new ProductDef("Quần Jean Rách Gối Nam", "quan-jean-rach-goi-nam",
                "Quần jean rách gối phong cách streetwear, chất denim dày.",
                "quan-jean", 459000, "Routine", "Denim Cotton",
                new String[]{"xanh-duong","den"}, numericSizes, imgJean1));

        // ─── QUẦN KAKI (3 sản phẩm) ─────────────────────────────
        defs.add(new ProductDef("Quần Kaki Công Sở Slim", "quan-kaki-cong-so-slim",
                "Quần kaki slim fit công sở, vải mịn không nhăn, form chuẩn.",
                "quan-kaki", 399000, "Owen", "Cotton Twill",
                new String[]{"be","den","xanh-navy"}, numericSizes, imgKaki1));

        defs.add(new ProductDef("Quần Kaki Ống Suông", "quan-kaki-ong-suong",
                "Quần kaki ống suông thoải mái, chất liệu cotton pha, dễ mặc hàng ngày.",
                "quan-kaki", 369000, "YODY", "Cotton Blend",
                new String[]{"be","nau","reu"}, numericSizes, imgKaki1));

        defs.add(new ProductDef("Quần Kaki Cargo Túi Hộp", "quan-kaki-cargo-tui-hop",
                "Quần kaki cargo nhiều túi hộp, phong cách outdoor mạnh mẽ.",
                "quan-kaki", 449000, "Routine", "Cotton Ripstop",
                new String[]{"den","reu","nau"}, numericSizes, imgKaki1));

        // ─── QUẦN SHORT (3 sản phẩm) ────────────────────────────
        defs.add(new ProductDef("Quần Short Kaki Nam", "quan-short-kaki-nam",
                "Quần short kaki nam trên gối, thoáng mát mùa hè.",
                "quan-short", 249000, "Coolmate", "Cotton Twill",
                new String[]{"be","den","xanh-navy"}, numericSizes, imgShort1));

        defs.add(new ProductDef("Quần Short Thể Thao Dry", "quan-short-the-thao-dry",
                "Quần short thể thao chất dry-fit, có lót bên trong, chạy bộ lý tưởng.",
                "quan-short", 199000, "Coolmate", "Polyester Dry-Fit",
                new String[]{"den","xam","xanh-navy"}, clothingSizes, imgShort2));

        defs.add(new ProductDef("Quần Short Jean Nữ", "quan-short-jean-nu",
                "Quần short jean nữ high waist, chất denim co giãn, trẻ trung.",
                "quan-short", 279000, "IVY moda", "Denim Stretch",
                new String[]{"xanh-duong","trang","den"}, numericSizes, imgShort1));

        // ─── QUẦN JOGGER (2 sản phẩm) ───────────────────────────
        defs.add(new ProductDef("Quần Jogger Nỉ Unisex", "quan-jogger-ni-unisex",
                "Quần jogger nỉ bông unisex, bo gấu, túi kéo khóa tiện lợi.",
                "quan-jogger", 299000, "Routine", "French Terry",
                new String[]{"den","xam","xanh-navy"}, clothingSizes, imgJogger1));

        defs.add(new ProductDef("Quần Jogger Kaki Co Giãn", "quan-jogger-kaki-co-gian",
                "Quần jogger kaki co giãn, form tapered, bo gấu trẻ trung.",
                "quan-jogger", 349000, "YODY", "Cotton Spandex",
                new String[]{"den","be","reu"}, numericSizes, imgJogger1));

        // ─── QUẦN TÂY (2 sản phẩm) ─────────────────────────────
        defs.add(new ProductDef("Quần Tây Âu Slim Fit", "quan-tay-au-slim-fit",
                "Quần tây âu slim fit công sở, vải cao cấp, ly sắc nét.",
                "quan-tay", 499000, "Owen", "Wool Blend",
                new String[]{"den","xanh-navy","xam"}, numericSizes, imgTay1));

        defs.add(new ProductDef("Quần Tây Nữ Ống Đứng", "quan-tay-nu-ong-dung",
                "Quần tây nữ ống đứng thanh lịch, cạp cao tôn dáng.",
                "quan-tay", 429000, "IVY moda", "Polyester Blend",
                new String[]{"den","be","xam"}, numericSizes, imgTay1));

        // ─── VÁY NGẮN (3 sản phẩm) ─────────────────────────────
        defs.add(new ProductDef("Chân Váy Chữ A Mini", "chan-vay-chu-a-mini",
                "Chân váy chữ A mini trẻ trung, chất vải dày dặn giữ form.",
                "vay-ngan", 279000, "IVY moda", "Polyester Blend",
                new String[]{"den","be","hong"}, dressSizes, imgVay1));

        defs.add(new ProductDef("Chân Váy Xếp Ly Tennis", "chan-vay-xep-ly-tennis",
                "Chân váy xếp ly phong cách tennis, cạp cao tôn dáng.",
                "vay-ngan", 249000, "YODY", "Polyester",
                new String[]{"trang","den","xanh-navy"}, dressSizes, imgVay2));

        defs.add(new ProductDef("Chân Váy Jean Ngắn", "chan-vay-jean-ngan",
                "Chân váy jean ngắn cá tính, chất denim co giãn thoải mái.",
                "vay-ngan", 299000, "Routine", "Denim Stretch",
                new String[]{"xanh-duong","den"}, dressSizes, imgVay1));

        // ─── ĐẦM DÀI (2 sản phẩm) ──────────────────────────────
        defs.add(new ProductDef("Đầm Maxi Hoa Nhí", "dam-maxi-hoa-nhi",
                "Đầm maxi hoa nhí bay bổng, chất vải voan mềm mại, thích hợp đi biển.",
                "dam-dai", 499000, "IVY moda", "Chiffon",
                new String[]{"do","xanh-duong","hong"}, dressSizes, imgDam1));

        defs.add(new ProductDef("Đầm Suông Công Sở", "dam-suong-cong-so",
                "Đầm suông thanh lịch cho nàng công sở, chất liệu lụa cao cấp.",
                "dam-dai", 549000, "IVY moda", "Lụa tổng hợp",
                new String[]{"den","be","xanh-navy"}, dressSizes, imgDam2));

        // ─── ĐỒ LÓT (1 sản phẩm) ───────────────────────────────
        defs.add(new ProductDef("Quần Lót Nam Cotton Combo 3", "quan-lot-nam-cotton-combo-3",
                "Combo 3 quần lót nam cotton co giãn, thấm hút mồ hôi.",
                "do-lot", 199000, "Coolmate", "Cotton Spandex",
                new String[]{"den","xam","xanh-navy"}, clothingSizes, imgDoLot1));

        // ─── PHỤ KIỆN (2 sản phẩm) ──────────────────────────────
        defs.add(new ProductDef("Nón Lưỡi Trai Unisex", "non-luoi-trai-unisex",
                "Nón lưỡi trai (baseball cap) unisex, vải cotton wash mềm, khóa điều chỉnh.",
                "phu-kien", 149000, "Routine", "Cotton Washed",
                new String[]{"den","trang","xanh-navy","be"}, freeSizes, imgPhuKien1));

        defs.add(new ProductDef("Thắt Lưng Da Bò Khóa Kim", "that-lung-da-bo-khoa-kim",
                "Thắt lưng da bò thật 100%, khóa kim cổ điển, bề rộng 3.5cm.",
                "phu-kien", 299000, "Owen", "Da bò thật",
                new String[]{"den","nau"}, freeSizes, imgPhuKien2));

        // ─── Tạo tất cả sản phẩm ────────────────────────────────
        Random random = new Random(42);
        for (ProductDef def : defs) {
            createOneProduct(def, cats, colors, random);
        }

        return defs.size();
    }

    private void createOneProduct(ProductDef def,
                                  Map<String, Category> cats,
                                  Map<String, Color> colors,
                                  Random random) {

        Category cat = cats.get(def.categorySlug);

        Product p = new Product();
        p.setName(def.name);
        p.setSlug(def.slug);
        p.setDescription(def.description);
        p.setCategoryId(cat.getId());
        p.setBasePrice(BigDecimal.valueOf(def.basePrice));
        p.setBrand(def.brand);
        p.setMaterial(def.material);
        p.setIsActive(true);
        p = productRepo.save(p);

        boolean firstColor = true;
        for (String colorSlug : def.colorSlugs) {
            Color color = colors.get(colorSlug);

            ProductVariant v = new ProductVariant();
            v.setProductId(p.getId());
            v.setColorId(color.getId());
            v.setColorImageUrl(def.imageUrl);
            // JSON array of images
            v.setImages("[\"" + def.imageUrl + "\",\"" + def.imageUrl.replace("w=600","w=800") + "\"]");
            v.setIsDefault(firstColor);
            v = variantRepo.save(v);
            firstColor = false;

            // Tạo variant_stocks cho mỗi size
            for (Size size : def.sizes) {
                VariantStock vs = new VariantStock();
                vs.setVariantId(v.getId());
                vs.setSizeId(size.getId());
                vs.setStockQty(20 + random.nextInt(80)); // 20-99
                // 30% cơ hội có giá riêng (±10% so với base_price)
                if (random.nextInt(10) < 3) {
                    double factor = 0.9 + random.nextDouble() * 0.2; // 0.9 – 1.1
                    long override = Math.round(def.basePrice * factor / 1000.0) * 1000;
                    vs.setPriceOverride(BigDecimal.valueOf(override));
                }
                String sku = String.format("P%d-V%d-%s",
                        p.getId(), v.getId(), size.getLabel().toUpperCase());
                vs.setSku(sku);
                stockRepo.save(vs);
            }
        }
    }


    // ────────────────────────────────────────────────────────────
    //  Inner helper class
    // ────────────────────────────────────────────────────────────
    private static class ProductDef {
        String name, slug, description, categorySlug, brand, material, imageUrl;
        int basePrice;
        String[] colorSlugs;
        List<Size> sizes;

        ProductDef(String name, String slug, String desc, String catSlug,
                   int basePrice, String brand, String material,
                   String[] colorSlugs, List<Size> sizes, String imageUrl) {
            this.name = name;
            this.slug = slug;
            this.description = desc;
            this.categorySlug = catSlug;
            this.basePrice = basePrice;
            this.brand = brand;
            this.material = material;
            this.colorSlugs = colorSlugs;
            this.sizes = sizes;
            this.imageUrl = imageUrl;
        }
    }
}

