package com.bluesky.pos_system.configuration;

import com.bluesky.pos_system.domains.OrderStatus;
import com.bluesky.pos_system.domains.PaymentStatus;
import com.bluesky.pos_system.domains.PaymentType;
import com.bluesky.pos_system.domains.StoreStatus;
import com.bluesky.pos_system.domains.UserRole;
import com.bluesky.pos_system.models.*;
import com.bluesky.pos_system.repositories.*;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@Profile("!test")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class DataInitializer implements CommandLineRunner {

    UserRepository userRepository;
    StoreRepository storeRepository;
    BranchRepository branchRepository;
    CategoryRepository categoryRepository;
    ProductRepository productRepository;
    InventoryRepository inventoryRepository;
    CustomerRepository customerRepository;
    OrderRepository orderRepository;
    PaymentTransactionRepository transactionRepository;
    RefundRepository refundRepository;
    PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        log.info("Checking and seeding database sample data...");

        // 1. Ensure Admin & Store & Branch Exist
        User admin = userRepository.findByEmail("admin@pos.com");
        if (admin == null) {
            admin = User.builder()
                    .fullName("Quản trị viên")
                    .email("admin@pos.com")
                    .password(passwordEncoder.encode("admin123"))
                    .roles(UserRole.ROLE_STORE_ADMIN)
                    .phone("0987654321")
                    .lastLogin(LocalDateTime.now())
                    .build();
            admin = userRepository.save(admin);
        }

        Store store = storeRepository.findAll().stream().findFirst().orElse(null);
        if (store == null) {
            store = Store.builder()
                    .branch("SkyPOS Flagship")
                    .description("Hệ thống bán lẻ & tiện ích hiện đại SkyPOS")
                    .storeType("Siêu thị mini & Bán lẻ")
                    .status(StoreStatus.ACTIVE)
                    .storeAdmin(admin)
                    .contact(StoreContact.builder()
                            .address("123 Lê Lợi, Phường Bến Nghé, Quận 1, TP. Hồ Chí Minh")
                            .phone("02838123456")
                            .email("contact@skypos.vn")
                            .build())
                    .build();
            store = storeRepository.save(store);
            admin.setStore(store);
            userRepository.save(admin);
        }

        final Store finalStore = store;

        Branch branch = branchRepository.findAll().stream().findFirst().orElse(null);
        if (branch == null) {
            branch = Branch.builder()
                    .name("Chi nhánh Quận 1")
                    .address("123 Lê Lợi, Quận 1, TP.HCM")
                    .phone("0901234567")
                    .email("branch.q1@skypos.vn")
                    .openTime(LocalTime.of(7, 30))
                    .closeTime(LocalTime.of(22, 30))
                    .workingDays(List.of("Thứ 2", "Thứ 3", "Thứ 4", "Thứ 5", "Thứ 6", "Thứ 7", "Chủ nhật"))
                    .store(store)
                    .manager(admin)
                    .build();
            branch = branchRepository.save(branch);
            admin.setBranch(branch);
            userRepository.save(admin);
        }

        final Branch finalBranch = branch;

        User cashier = userRepository.findByEmail("cashier@pos.com");
        if (cashier == null) {
            cashier = User.builder()
                    .fullName("Nguyễn Thu Ngân")
                    .email("cashier@pos.com")
                    .password(passwordEncoder.encode("cashier123"))
                    .roles(UserRole.ROLE_BRANCH_CASHIER)
                    .phone("0912345678")
                    .store(store)
                    .branch(branch)
                    .lastLogin(LocalDateTime.now())
                    .build();
            cashier = userRepository.save(cashier);
        }

        final User finalCashier = cashier;

        // 2. Ensure Categories Exist
        Category catDrinks = getOrCreateCategory("Đồ uống & Giải khát", finalStore);
        Category catSnacks = getOrCreateCategory("Bánh kẹo & Snack", finalStore);
        Category catFresh = getOrCreateCategory("Thực phẩm đóng gói", finalStore);
        Category catDairy = getOrCreateCategory("Sữa & Chế phẩm từ sữa", finalStore);
        Category catHome = getOrCreateCategory("Hàng tiêu dùng & Gia dụng", finalStore);
        Category catPersonal = getOrCreateCategory("Chăm sóc cá nhân", finalStore);
        Category catBakery = getOrCreateCategory("Bánh mì & Đồ ăn nhanh", finalStore);
        Category catFruits = getOrCreateCategory("Trái cây tươi & Nông sản", finalStore);

        // 3. Ensure Rich Products Exist (25+ items)
        if (productRepository.count() < 15) {
            log.info("Seeding comprehensive product catalog...");
            List<Product> newProducts = List.of(
                    // Đồ uống
                    Product.builder()
                            .name("Cà Phê Sữa Highland Canned 235ml")
                            .sku("SKU-CF-001").barcode("8934567890123")
                            .description("Cà phê sữa thơm béo lon tiện lợi Highlands")
                            .mrp(18000.0).costPrice(11000.0).sellingPrice(15000.0)
                            .brand("Highlands Coffee")
                            .image("https://images.unsplash.com/photo-1559056199-641a0ac8b55e?w=500&auto=format&fit=crop&q=60")
                            .category(catDrinks).store(finalStore).minStockLevel(10).isActive(true).build(),
                    Product.builder()
                            .name("Trà Xanh Không Độ 455ml")
                            .sku("SKU-TEA-002").barcode("8934567890124")
                            .description("Trà xanh giải nhiệt tự nhiên chứa EGCG")
                            .mrp(12000.0).costPrice(7000.0).sellingPrice(10000.0)
                            .brand("Number 1")
                            .image("https://images.unsplash.com/photo-1556881286-fc6915169721?w=500&auto=format&fit=crop&q=60")
                            .category(catDrinks).store(finalStore).minStockLevel(15).isActive(true).build(),
                    Product.builder()
                            .name("Nước Ngọt Coca Cola Sleek Can 320ml")
                            .sku("SKU-COCA-007").barcode("8934567890131")
                            .description("Nước giải khát có gas sảng khoái mát lạnh")
                            .mrp(14000.0).costPrice(8000.0).sellingPrice(12000.0)
                            .brand("Coca-Cola")
                            .image("https://images.unsplash.com/photo-1622483767028-3f66f32aef97?w=500&auto=format&fit=crop&q=60")
                            .category(catDrinks).store(finalStore).minStockLevel(20).isActive(true).build(),
                    Product.builder()
                            .name("Nước Tăng Lực Red Bull Thái Lon 250ml")
                            .sku("SKU-RB-008").barcode("8934567890132")
                            .description("Nước tăng lực bò húc nạp năng lượng tỉnh táo")
                            .mrp(18000.0).costPrice(11500.0).sellingPrice(16000.0)
                            .brand("Red Bull")
                            .image("https://images.unsplash.com/photo-1527960471264-932f39eb5846?w=500&auto=format&fit=crop&q=60")
                            .category(catDrinks).store(finalStore).minStockLevel(15).isActive(true).build(),
                    Product.builder()
                            .name("Nước Khoáng Thiên Nhiên La Vie 500ml")
                            .sku("SKU-LAVIE-009").barcode("8934567890133")
                            .description("Nước khoáng thiên nhiên thanh khiết dịu nhẹ")
                            .mrp(9000.0).costPrice(4500.0).sellingPrice(7000.0)
                            .brand("La Vie")
                            .image("https://images.unsplash.com/photo-1548839140-29a749e1bc4e?w=500&auto=format&fit=crop&q=60")
                            .category(catDrinks).store(finalStore).minStockLevel(25).isActive(true).build(),
                    Product.builder()
                            .name("Trà Sữa Macchiato Đóng Chai Đài Loan 350ml")
                            .sku("SKU-TS-010").barcode("8934567890134")
                            .description("Trà sữa ngọt thơm kèm lớp kem sữa béo ngậy")
                            .mrp(30000.0).costPrice(18000.0).sellingPrice(25000.0)
                            .brand("Royal Tea")
                            .image("https://images.unsplash.com/photo-1572490122747-3968b75cc699?w=500&auto=format&fit=crop&q=60")
                            .category(catDrinks).store(finalStore).minStockLevel(10).isActive(true).build(),

                    // Bánh kẹo & Snack
                    Product.builder()
                            .name("Snack Khoai Tây Lay's Vị Tự Nhiên 95g")
                            .sku("SKU-SNK-003").barcode("8934567890125")
                            .description("Khoai tây chiên giòn rụm vị muối cổ điển")
                            .mrp(25000.0).costPrice(15000.0).sellingPrice(22000.0)
                            .brand("Lay's")
                            .image("https://images.unsplash.com/photo-1566478989037-eec170784d0b?w=500&auto=format&fit=crop&q=60")
                            .category(catSnacks).store(finalStore).minStockLevel(15).isActive(true).build(),
                    Product.builder()
                            .name("Bánh Quy Oreo Socola Kem Vani 133g")
                            .sku("SKU-SNK-004").barcode("8934567890126")
                            .description("Bánh quy socola kẹp kem thơm ngon kinh điển")
                            .mrp(20000.0).costPrice(12000.0).sellingPrice(18000.0)
                            .brand("Oreo")
                            .image("https://images.unsplash.com/photo-1558961363-fa8fdf82db35?w=500&auto=format&fit=crop&q=60")
                            .category(catSnacks).store(finalStore).minStockLevel(10).isActive(true).build(),
                    Product.builder()
                            .name("Snack Mực Cay Bento Thái Lan Đỏ 20g")
                            .sku("SKU-BENTO-011").barcode("8934567890135")
                            .description("Mực tẩm gia vị cay nồng giòn ngon chuẩn vị Thái")
                            .mrp(16000.0).costPrice(9500.0).sellingPrice(14000.0)
                            .brand("Bento")
                            .image("https://images.unsplash.com/photo-1621996346565-e3d5d6281141?w=500&auto=format&fit=crop&q=60")
                            .category(catSnacks).store(finalStore).minStockLevel(20).isActive(true).build(),
                    Product.builder()
                            .name("Bánh Chocopie Orion Hộp 6 Cái 198g")
                            .sku("SKU-CHOCO-012").barcode("8934567890136")
                            .description("Bánh mềm phủ socola kẹp kẹo dẻo marshmallow")
                            .mrp(42000.0).costPrice(26000.0).sellingPrice(36000.0)
                            .brand("Orion")
                            .image("https://images.unsplash.com/photo-1606313564200-e75d5e30476c?w=500&auto=format&fit=crop&q=60")
                            .category(catSnacks).store(finalStore).minStockLevel(12).isActive(true).build(),
                    Product.builder()
                            .name("Kẹo Dẻo Gấu Haribo Goldbears Gói 80g")
                            .sku("SKU-HARIBO-013").barcode("8934567890137")
                            .description("Kẹo dẻo hình gấu vị trái cây thơm ngon tự nhiên")
                            .mrp(32000.0).costPrice(20000.0).sellingPrice(28000.0)
                            .brand("Haribo")
                            .image("https://images.unsplash.com/photo-1582058091505-f87a2e55a40f?w=500&auto=format&fit=crop&q=60")
                            .category(catSnacks).store(finalStore).minStockLevel(10).isActive(true).build(),

                    // Sữa & Chế phẩm
                    Product.builder()
                            .name("Sữa Tươi Tiệt Trùng Vinamilk Có Đường Hộp 1L")
                            .sku("SKU-MILK-014").barcode("8934567890138")
                            .description("100% sữa bò tươi nguyên chất giàu dinh dưỡng")
                            .mrp(44000.0).costPrice(29000.0).sellingPrice(38000.0)
                            .brand("Vinamilk")
                            .image("https://images.unsplash.com/photo-1550583724-b2692b85b150?w=500&auto=format&fit=crop&q=60")
                            .category(catDairy).store(finalStore).minStockLevel(15).isActive(true).build(),
                    Product.builder()
                            .name("Sữa Chua Ăn Nha Đam Vinamilk Hộp 100g")
                            .sku("SKU-YOG-015").barcode("8934567890139")
                            .description("Sữa chua lên men tự nhiên giòn sần sật nha đam")
                            .mrp(10000.0).costPrice(6500.0).sellingPrice(8500.0)
                            .brand("Vinamilk")
                            .image("https://images.unsplash.com/photo-1488477181946-6428a0291777?w=500&auto=format&fit=crop&q=60")
                            .category(catDairy).store(finalStore).minStockLevel(25).isActive(true).build(),
                    Product.builder()
                            .name("Phô Mai Con Bò Cười Hộp 8 Miếng 112g")
                            .sku("SKU-CHZ-016").barcode("8934567890140")
                            .description("Phô mai mềm béo ngậy bổ sung canxi & vitamin D")
                            .mrp(48000.0).costPrice(32000.0).sellingPrice(42000.0)
                            .brand("La Vache Quirit")
                            .image("https://images.unsplash.com/photo-1452195100486-9cc805987862?w=500&auto=format&fit=crop&q=60")
                            .category(catDairy).store(finalStore).minStockLevel(10).isActive(true).build(),

                    // Thực phẩm đóng gói
                    Product.builder()
                            .name("Mì Hảo Hảo Tôm Chua Cay Gói 75g")
                            .sku("SKU-NOD-005").barcode("8934567890127")
                            .description("Hương vị tôm chua cay đậm đà truyền thống quốc dân")
                            .mrp(6000.0).costPrice(3800.0).sellingPrice(5000.0)
                            .brand("Acecook")
                            .image("https://images.unsplash.com/photo-1612927601601-6638404737ce?w=500&auto=format&fit=crop&q=60")
                            .category(catFresh).store(finalStore).minStockLevel(50).isActive(true).build(),
                    Product.builder()
                            .name("Mì Trộn Khô Indomie Mi Goreng Vị Đặc Biệt 85g")
                            .sku("SKU-INDO-017").barcode("8934567890141")
                            .description("Mì xào khô hương vị Indonesia thơm cay hấp dẫn")
                            .mrp(8500.0).costPrice(5200.0).sellingPrice(7000.0)
                            .brand("Indomie")
                            .image("https://images.unsplash.com/photo-1569718212165-3a8278d5f624?w=500&auto=format&fit=crop&q=60")
                            .category(catFresh).store(finalStore).minStockLevel(40).isActive(true).build(),
                    Product.builder()
                            .name("Xúc Xích Tiệt Trùng Vissan Gói 4 Cây 160g")
                            .sku("SKU-SAU-018").barcode("8934567890142")
                            .description("Xúc xích heo tiệt trùng dinh dưỡng ăn liền tiện lợi")
                            .mrp(22000.0).costPrice(14000.0).sellingPrice(18000.0)
                            .brand("Vissan")
                            .image("https://images.unsplash.com/photo-1541529086526-db283c563270?w=500&auto=format&fit=crop&q=60")
                            .category(catFresh).store(finalStore).minStockLevel(20).isActive(true).build(),

                    // Gia dụng & Chăm sóc cá nhân
                    Product.builder()
                            .name("Khăn Giấy Ướt Cao Cấp Mamamy 80 Tờ")
                            .sku("SKU-HOM-006").barcode("8934567890128")
                            .description("Khăn ướt không cồn, an toàn dịu nhẹ cho mọi làn da")
                            .mrp(38000.0).costPrice(22000.0).sellingPrice(32000.0)
                            .brand("Mamamy")
                            .image("https://images.unsplash.com/photo-1584308666744-24d5c474f2ae?w=500&auto=format&fit=crop&q=60")
                            .category(catHome).store(finalStore).minStockLevel(15).isActive(true).build(),
                    Product.builder()
                            .name("Bàn Chải Đánh Răng Colgate Slim Soft Charcoal")
                            .sku("SKU-COLG-019").barcode("8934567890143")
                            .description("Lông chải siêu mảnh phủ than hoạt tính làm sạch sâu")
                            .mrp(35000.0).costPrice(19000.0).sellingPrice(28000.0)
                            .brand("Colgate")
                            .image("https://images.unsplash.com/photo-1559591937-e1032c525f68?w=500&auto=format&fit=crop&q=60")
                            .category(catPersonal).store(finalStore).minStockLevel(15).isActive(true).build(),
                    Product.builder()
                            .name("Nước Rửa Tay Diệt Khuẩn Lifebuoy Bảo Vệ Vượt Trội 500g")
                            .sku("SKU-LIFE-020").barcode("8934567890144")
                            .description("Nước rửa tay kháng khuẩn 99.9% bảo vệ gia đình")
                            .mrp(78000.0).costPrice(48000.0).sellingPrice(68000.0)
                            .brand("Lifebuoy")
                            .image("https://images.unsplash.com/photo-1584515979956-d9f6e5d09982?w=500&auto=format&fit=crop&q=60")
                            .category(catPersonal).store(finalStore).minStockLevel(10).isActive(true).build(),

                    // Bánh mì & Trái cây tươi
                    Product.builder()
                            .name("Bánh Mì Hoa Cúc Harrys Brioche Pháp 500g")
                            .sku("SKU-BREAD-021").barcode("8934567890145")
                            .description("Bánh mì ngọt truyền thống Pháp thơm mùi hoa cúc")
                            .mrp(135000.0).costPrice(85000.0).sellingPrice(115000.0)
                            .brand("Harrys")
                            .image("https://images.unsplash.com/photo-1509440159596-0249088772ff?w=500&auto=format&fit=crop&q=60")
                            .category(catBakery).store(finalStore).minStockLevel(8).isActive(true).build(),
                    Product.builder()
                            .name("Táo Envy New Zealand Nhập Khẩu (Kg)")
                            .sku("SKU-APPLE-022").barcode("8934567890146")
                            .description("Táo Envy vỏ đỏ sọc vàng, giòn ngọt mọng nước cao cấp")
                            .mrp(220000.0).costPrice(140000.0).sellingPrice(185000.0)
                            .brand("Envy Apple")
                            .image("https://images.unsplash.com/photo-1560806887-1e4cd0b6cbd6?w=500&auto=format&fit=crop&q=60")
                            .category(catFruits).store(finalStore).minStockLevel(10).isActive(true).build(),
                    Product.builder()
                            .name("Nho Đen Không Hạt Mỹ Nhập Khẩu Hộp 500g")
                            .sku("SKU-GRAPE-023").barcode("8934567890147")
                            .description("Nho đen quả to, giòn đượm vị ngọt tự nhiên không hạt")
                            .mrp(150000.0).costPrice(95000.0).sellingPrice(130000.0)
                            .brand("Sun World")
                            .image("https://images.unsplash.com/photo-1537640538966-79f369143f8f?w=500&auto=format&fit=crop&q=60")
                            .category(catFruits).store(finalStore).minStockLevel(8).isActive(true).build()
            );

            for (Product p : newProducts) {
                if (!productRepository.existsBySku(p.getSku())) {
                    Product saved = productRepository.save(p);
                    inventoryRepository.save(Inventory.builder()
                            .branch(finalBranch)
                            .product(saved)
                            .quantity(40 + (int)(Math.random() * 80))
                            .build());
                }
            }
        }

        // 4. Ensure Customers Exist
        Customer c1 = getOrCreateCustomer("Trần Thị Mai Anh", "0909123456", "maianh.tran@gmail.com", "45 Hai Bà Trưng, Quận 1, TP.HCM", 250, 2450000.0);
        Customer c2 = getOrCreateCustomer("Lê Hoàng Nam", "0988776655", "hoangnam.le@yahoo.com", "88 Nguyễn Thị Minh Khai, Quận 3, TP.HCM", 115, 1150000.0);
        Customer c3 = getOrCreateCustomer("Nguyễn Văn Hùng", "0918223344", "hung.nguyen@outlook.com", "12 Hoàng Hoa Thám, Tân Bình, TP.HCM", 520, 5200000.0);
        Customer c4 = getOrCreateCustomer("Phạm Thu Thảo", "0934556677", "thuthao.pham@gmail.com", "204 Pasteur, Quận 3, TP.HCM", 85, 850000.0);
        Customer c5 = getOrCreateCustomer("Đặng Quốc Bảo", "0977112233", "baodang@fpt.com.vn", "72 Nguyễn Huệ, Quận 1, TP.HCM", 340, 3400000.0);

        // 5. Ensure Historical Orders and Payment Transactions Exist
        if (orderRepository.count() == 0) {
            log.info("Seeding historical orders and Stripe payment transactions...");

            List<Product> allProds = productRepository.findAll();
            if (allProds.size() >= 5) {
                Product pCoffee = allProds.stream().filter(p -> p.getSku().contains("CF")).findFirst().orElse(allProds.get(0));
                Product pTea = allProds.stream().filter(p -> p.getSku().contains("TEA")).findFirst().orElse(allProds.get(1));
                Product pLays = allProds.stream().filter(p -> p.getSku().contains("SNK-003")).findFirst().orElse(allProds.get(2));
                Product pOreo = allProds.stream().filter(p -> p.getSku().contains("SNK-004")).findFirst().orElse(allProds.get(3));
                Product pNoodles = allProds.stream().filter(p -> p.getSku().contains("NOD")).findFirst().orElse(allProds.get(4));

                LocalDateTime now = LocalDateTime.now();

                // Order 1: Stripe SUCCESS (Visa 4242)
                createSeedOrderAndTransaction(
                        "ORD-20261006-0101",
                        OrderStatus.COMPLETED,
                        PaymentType.STRIPE,
                        PaymentStatus.SUCCESS,
                        finalBranch,
                        finalCashier,
                        c1,
                        List.of(new SeedItem(pCoffee, 2), new SeedItem(pLays, 1)),
                        now.minusDays(3).minusHours(2),
                        "pi_3Ptest_001_succeeded",
                        "ch_3Ptest_001",
                        "visa",
                        "4242",
                        0.0,
                        null,
                        null,
                        "Thanh toán thẻ Visa qua Stripe Elements"
                );

                // Order 2: Stripe SUCCESS (Mastercard 5555)
                createSeedOrderAndTransaction(
                        "ORD-20261006-0102",
                        OrderStatus.COMPLETED,
                        PaymentType.STRIPE,
                        PaymentStatus.SUCCESS,
                        finalBranch,
                        finalCashier,
                        c2,
                        List.of(new SeedItem(pTea, 3), new SeedItem(pOreo, 2), new SeedItem(pNoodles, 4)),
                        now.minusDays(3).minusHours(5),
                        "pi_3Ptest_002_succeeded",
                        "ch_3Ptest_002",
                        "mastercard",
                        "5555",
                        0.0,
                        null,
                        null,
                        "Thanh toán thẻ Mastercard trực tuyến"
                );

                // Order 3: CASH SUCCESS
                createSeedOrderAndTransaction(
                        "ORD-20261007-0103",
                        OrderStatus.COMPLETED,
                        PaymentType.CASH,
                        PaymentStatus.SUCCESS,
                        finalBranch,
                        finalCashier,
                        c3,
                        List.of(new SeedItem(pCoffee, 4), new SeedItem(pNoodles, 10)),
                        now.minusDays(2).minusHours(1),
                        null,
                        null,
                        null,
                        null,
                        0.0,
                        null,
                        null,
                        "Khách thanh toán tiền mặt tại quầy"
                );

                // Order 4: Stripe REFUNDED (Full Refund)
                Order refundOrder = createSeedOrderAndTransaction(
                        "ORD-20261007-0104",
                        OrderStatus.REFUNDED,
                        PaymentType.STRIPE,
                        PaymentStatus.REFUNDED,
                        finalBranch,
                        finalCashier,
                        c4,
                        List.of(new SeedItem(pLays, 2), new SeedItem(pTea, 2)),
                        now.minusDays(2).minusHours(4),
                        "pi_3Ptest_004_refunded",
                        "ch_3Ptest_004",
                        "visa",
                        "4242",
                        64000.0,
                        "re_3Ptest_004_full",
                        null,
                        "Khách đổi ý trả hàng - Hoàn tiền 100% qua Stripe"
                );
                // Record in Refund repository
                refundRepository.save(Refund.builder()
                        .order(refundOrder)
                        .amount(64000.0)
                        .reason("Khách yêu cầu đổi ý trả hàng")
                        .branch(finalBranch)
                        .cashier(finalCashier)
                        .paymentType(PaymentType.STRIPE)
                        .createdAt(now.minusDays(2).minusHours(2))
                        .build());

                // Order 5: Stripe PARTIALLY_REFUNDED (JCB 0099)
                createSeedOrderAndTransaction(
                        "ORD-20261008-0105",
                        OrderStatus.COMPLETED,
                        PaymentType.STRIPE,
                        PaymentStatus.PARTIALLY_REFUNDED,
                        finalBranch,
                        finalCashier,
                        c5,
                        List.of(new SeedItem(pCoffee, 3), new SeedItem(pOreo, 3), new SeedItem(pLays, 2)),
                        now.minusDays(1).minusHours(6),
                        "pi_3Ptest_005_partial",
                        "ch_3Ptest_005",
                        "jcb",
                        "0099",
                        50000.0,
                        "re_3Ptest_005_partial",
                        null,
                        "Hoàn trả một phần do sản phẩm bị móp vỏ"
                );

                // Order 6: Stripe FAILED (Card Declined / Insufficient funds)
                createSeedOrderAndTransaction(
                        "ORD-20261008-0106",
                        OrderStatus.PENDING,
                        PaymentType.STRIPE,
                        PaymentStatus.FAILED,
                        finalBranch,
                        finalCashier,
                        c1,
                        List.of(new SeedItem(pOreo, 4), new SeedItem(pTea, 2)),
                        now.minusDays(1).minusHours(2),
                        "pi_3Ptest_006_failed",
                        null,
                        "visa",
                        "0002",
                        0.0,
                        null,
                        "Your card has insufficient funds",
                        "Giao dịch Stripe không thành công do thẻ không đủ số dư"
                );

                // Order 7: Stripe SUCCESS (Apple Pay / Visa 1111)
                createSeedOrderAndTransaction(
                        "ORD-20261009-0107",
                        OrderStatus.COMPLETED,
                        PaymentType.STRIPE,
                        PaymentStatus.SUCCESS,
                        finalBranch,
                        finalCashier,
                        c2,
                        List.of(new SeedItem(pCoffee, 5), new SeedItem(pNoodles, 6), new SeedItem(pLays, 3)),
                        now.minusHours(4),
                        "pi_3Ptest_007_succeeded",
                        "ch_3Ptest_007",
                        "visa",
                        "1111",
                        0.0,
                        null,
                        null,
                        "Thanh toán Apple Pay qua cổng Stripe"
                );

                // Order 8: CASH SUCCESS (Đơn sáng nay)
                createSeedOrderAndTransaction(
                        "ORD-20261009-0108",
                        OrderStatus.COMPLETED,
                        PaymentType.CASH,
                        PaymentStatus.SUCCESS,
                        finalBranch,
                        finalCashier,
                        c3,
                        List.of(new SeedItem(pTea, 2), new SeedItem(pNoodles, 2)),
                        now.minusHours(1),
                        null,
                        null,
                        null,
                        null,
                        0.0,
                        null,
                        null,
                        "Bán lẻ ca sáng"
                );
            }
        }

        log.info("Sample data initialization completed successfully! Total products: {}, Orders: {}, Transactions: {}",
                productRepository.count(), orderRepository.count(), transactionRepository.count());
    }

    private Category getOrCreateCategory(String name, Store store) {
        return categoryRepository.findAll().stream()
                .filter(c -> c.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElseGet(() -> categoryRepository.save(Category.builder().name(name).store(store).build()));
    }

    private Customer getOrCreateCustomer(String name, String phone, String email, String address, int points, double spent) {
        return customerRepository.findAll().stream()
                .filter(c -> c.getPhone().equals(phone))
                .findFirst()
                .orElseGet(() -> customerRepository.save(Customer.builder()
                        .fullName(name)
                        .phone(phone)
                        .email(email)
                        .address(address)
                        .loyaltyPoints(points)
                        .totalSpent(spent)
                        .build()));
    }

    private static class SeedItem {
        Product product;
        int qty;
        SeedItem(Product product, int qty) {
            this.product = product;
            this.qty = qty;
        }
    }

    private Order createSeedOrderAndTransaction(
            String orderNumber,
            OrderStatus orderStatus,
            PaymentType paymentType,
            PaymentStatus paymentStatus,
            Branch branch,
            User cashier,
            Customer customer,
            List<SeedItem> items,
            LocalDateTime createdAt,
            String gatewayReference,
            String chargeId,
            String cardBrand,
            String cardLast4,
            double refundedAmount,
            String stripeRefundId,
            String errorMessage,
            String notes
    ) {
        Order order = Order.builder()
                .orderNumber(orderNumber)
                .status(orderStatus)
                .paymentType(paymentType)
                .branch(branch)
                .cashier(cashier)
                .customer(customer)
                .createdAt(createdAt)
                .updatedAt(createdAt)
                .notes(notes)
                .build();

        List<OrderItem> orderItems = new ArrayList<>();
        double subtotal = 0.0;
        for (SeedItem si : items) {
            double itemTotal = (si.product.getSellingPrice() != null ? si.product.getSellingPrice() : 0.0) * si.qty;
            subtotal += itemTotal;
            orderItems.add(OrderItem.builder()
                    .order(order)
                    .product(si.product)
                    .quantity(si.qty)
                    .price(itemTotal)
                    .build());
        }

        double tax = subtotal * 0.08; // 8% VAT
        double total = subtotal + tax;

        order.setSubtotal(subtotal);
        order.setDiscount(0.0);
        order.setTax(tax);
        order.setTotalAmount(total);
        order.setItems(orderItems);

        Order savedOrder = orderRepository.save(order);

        // Generate corresponding transaction
        String txnCode = "TXN-" + orderNumber.replace("ORD-", "");
        PaymentTransaction txn = PaymentTransaction.builder()
                .transactionCode(txnCode)
                .order(savedOrder)
                .amount(total)
                .currency("vnd")
                .paymentType(paymentType)
                .status(paymentStatus)
                .gatewayReference(gatewayReference)
                .chargeId(chargeId)
                .receiptUrl(chargeId != null ? "https://pay.stripe.com/receipts/" + chargeId : null)
                .cardBrand(cardBrand)
                .cardLast4(cardLast4)
                .refundedAmount(refundedAmount)
                .stripeRefundId(stripeRefundId)
                .errorMessage(errorMessage)
                .customerEmail(customer != null ? customer.getEmail() : null)
                .notes(notes)
                .createdAt(createdAt)
                .updatedAt(createdAt)
                .build();

        transactionRepository.save(txn);
        return savedOrder;
    }
}
