package com.bluesky.pos_system.configuration;

import com.bluesky.pos_system.domains.StoreStatus;
import com.bluesky.pos_system.domains.UserRole;
import com.bluesky.pos_system.models.*;
import com.bluesky.pos_system.repositories.*;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

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
    PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.count() > 0) {
            return; // Data already exists
        }

        // 1. Create Default Admin / Cashier User
        User admin = User.builder()
                .fullName("Quản trị viên")
                .email("admin@pos.com")
                .password(passwordEncoder.encode("admin123"))
                .roles(UserRole.ROLE_STORE_ADMIN)
                .phone("0987654321")
                .lastLogin(LocalDateTime.now())
                .build();
        admin = userRepository.save(admin);

        // 2. Create Default Store
        Store store = Store.builder()
                .branch("SkyPOS Flagship")
                .description("Hệ thống bán lẻ & tiện ích hiện đại")
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

        // Update admin store
        admin.setStore(store);

        // 3. Create Default Branch
        Branch branch = Branch.builder()
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

        // Create Cashier User
        User cashier = User.builder()
                .fullName("Nguyễn Thu Ngân")
                .email("cashier@pos.com")
                .password(passwordEncoder.encode("cashier123"))
                .roles(UserRole.ROLE_BRANCH_CASHIER)
                .phone("0912345678")
                .store(store)
                .branch(branch)
                .lastLogin(LocalDateTime.now())
                .build();
        userRepository.save(cashier);

        // 4. Create Categories
        Category catDrinks = categoryRepository.save(Category.builder().name("Đồ uống & Giải khát").store(store).build());
        Category catSnacks = categoryRepository.save(Category.builder().name("Bánh kẹo & Snack").store(store).build());
        Category catFresh = categoryRepository.save(Category.builder().name("Thực phẩm đóng gói").store(store).build());
        Category catHome = categoryRepository.save(Category.builder().name("Hàng tiêu dùng").store(store).build());

        // 5. Create Sample Products
        Product p1 = Product.builder()
                .name("Cà Phê Sữa Highland Canned 235ml")
                .sku("SKU-CF-001")
                .barcode("8934567890123")
                .description("Cà phê sữa thơm béo lon tiện lợi")
                .mrp(18000.0)
                .costPrice(11000.0)
                .sellingPrice(15000.0)
                .brand("Highlands Coffee")
                .image("https://images.unsplash.com/photo-1559056199-641a0ac8b55e?w=500&auto=format&fit=crop&q=60")
                .category(catDrinks)
                .store(store)
                .minStockLevel(10)
                .isActive(true)
                .build();

        Product p2 = Product.builder()
                .name("Trà Xanh Không Độ 455ml")
                .sku("SKU-TEA-002")
                .barcode("8934567890124")
                .description("Trà xanh giải nhiệt tự nhiên chứa EGCG")
                .mrp(12000.0)
                .costPrice(7000.0)
                .sellingPrice(10000.0)
                .brand("Number 1")
                .image("https://images.unsplash.com/photo-1556881286-fc6915169721?w=500&auto=format&fit=crop&q=60")
                .category(catDrinks)
                .minStockLevel(15)
                .store(store)
                .isActive(true)
                .build();

        Product p3 = Product.builder()
                .name("Snack Khoai Tây Lay's Vị Tự Nhiên 95g")
                .sku("SKU-SNK-003")
                .barcode("8934567890125")
                .description("Khoai tây chiên giòn rụm vị muối cổ điển")
                .mrp(25000.0)
                .costPrice(15000.0)
                .sellingPrice(22000.0)
                .brand("Lay's")
                .image("https://images.unsplash.com/photo-1566478989037-eec170784d0b?w=500&auto=format&fit=crop&q=60")
                .category(catSnacks)
                .minStockLevel(8)
                .store(store)
                .isActive(true)
                .build();

        Product p4 = Product.builder()
                .name("Bánh Quy Oreo Socola Kem Vani 133g")
                .sku("SKU-SNK-004")
                .barcode("8934567890126")
                .description("Bánh quy socola kẹp kem thơm ngon")
                .mrp(20000.0)
                .costPrice(12000.0)
                .sellingPrice(18000.0)
                .brand("Oreo")
                .image("https://images.unsplash.com/photo-1558961363-fa8fdf82db35?w=500&auto=format&fit=crop&q=60")
                .category(catSnacks)
                .minStockLevel(10)
                .store(store)
                .isActive(true)
                .build();

        Product p5 = Product.builder()
                .name("Mì Hảo Hảo Tôm Chua Cay Gói 75g")
                .sku("SKU-NOD-005")
                .barcode("8934567890127")
                .description("Hương vị tôm chua cay đậm đà truyền thống")
                .mrp(6000.0)
                .costPrice(3800.0)
                .sellingPrice(5000.0)
                .brand("Acecook")
                .image("https://images.unsplash.com/photo-1612927601601-6638404737ce?w=500&auto=format&fit=crop&q=60")
                .category(catFresh)
                .minStockLevel(25)
                .store(store)
                .isActive(true)
                .build();

        Product p6 = Product.builder()
                .name("Khăn Giấy Ướt Cao Cấp Mamamy 80 Tờ")
                .sku("SKU-HOM-006")
                .barcode("8934567890128")
                .description("Khăn ướt không cồn, an toàn dịu nhẹ")
                .mrp(38000.0)
                .costPrice(22000.0)
                .sellingPrice(32000.0)
                .brand("Mamamy")
                .image("https://images.unsplash.com/photo-1584308666744-24d5c474f2ae?w=500&auto=format&fit=crop&q=60")
                .category(catHome)
                .minStockLevel(5)
                .store(store)
                .isActive(true)
                .build();

        List<Product> products = productRepository.saveAll(List.of(p1, p2, p3, p4, p5, p6));

        // 6. Create Inventories for products in this branch
        int[] quantities = {45, 80, 24, 15, 120, 18};
        for (int i = 0; i < products.size(); i++) {
            inventoryRepository.save(Inventory.builder()
                    .branch(branch)
                    .product(products.get(i))
                    .quantity(quantities[i])
                    .build());
        }

        // 7. Create Sample Customers
        customerRepository.save(Customer.builder()
                .fullName("Trần Thị Mai Anh")
                .phone("0909123456")
                .email("maianh.tran@gmail.com")
                .address("45 Hai Bà Trưng, Quận 1, TP.HCM")
                .loyaltyPoints(120)
                .totalSpent(1250000.0)
                .build());

        customerRepository.save(Customer.builder()
                .fullName("Lê Hoàng Nam")
                .phone("0988776655")
                .email("hoangnam.le@yahoo.com")
                .address("88 Nguyễn Thị Minh Khai, Quận 3, TP.HCM")
                .loyaltyPoints(45)
                .totalSpent(480000.0)
                .build());
    }
}
