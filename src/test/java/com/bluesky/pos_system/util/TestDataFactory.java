package com.bluesky.pos_system.util;

import com.bluesky.pos_system.domains.*;
import com.bluesky.pos_system.models.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TestDataFactory {

    // === Transient entities for JPA save (id == null) ===

    public static Store newStore() {
        return Store.builder()
                .branch("Test Main Store")
                .description("Test Store Description")
                .status(StoreStatus.ACTIVE)
                .storeType("RETAIL")
                .build();
    }

    public static Branch newBranch(Store store) {
        return Branch.builder()
                .name("Test Branch 1")
                .address("123 Test Street")
                .phone("0901234567")
                .store(store)
                .build();
    }

    public static User newUser(Store store, Branch branch, UserRole role) {
        return User.builder()
                .fullName("Test User " + System.currentTimeMillis() % 1000)
                .email("test" + System.currentTimeMillis() % 10000 + "@pos.com")
                .password("$2a$10$abcdefghijklmnopqrstuv") // dummy bcrypt
                .roles(role)
                .store(store)
                .branch(branch)
                .createdAt(LocalDate.now())
                .build();
    }

    public static Category newCategory(Store store) {
        return Category.builder()
                .name("Cà Phê")
                .slug("ca-phe")
                .description("Danh mục các loại cà phê")
                .isActive(true)
                .store(store)
                .build();
    }

    public static Supplier newSupplier(Store store) {
        return Supplier.builder()
                .code("SUP-TEST-01")
                .name("Nhà Cung Cấp Hạt Cà Phê")
                .contactName("Tran Van B")
                .phone("0912345678")
                .email("supplier@test.com")
                .store(store)
                .isActive(true)
                .build();
    }

    public static Product newProduct(Store store, Category category, Supplier supplier) {
        return Product.builder()
                .name("Cà Phê Sữa Đậm Đà")
                .sku("CFS-TEST-" + System.currentTimeMillis() % 10000)
                .barcode("89300000" + (System.currentTimeMillis() % 1000))
                .costPrice(15000.0)
                .sellingPrice(35000.0)
                .vatRate(0.08)
                .minStockLevel(5)
                .status(ProductStatus.IN_STOCK)
                .isDeleted(false)
                .category(category)
                .supplier(supplier)
                .store(store)
                .build();
    }

    public static Customer newCustomer() {
        return Customer.builder()
                .customerCode("CUST-TEST-001")
                .fullName("Nguyen Van Khach")
                .email("khachhang@test.com")
                .phone("0987654321")
                .loyaltyPoints(50)
                .totalSpent(500000.0)
                .isDeleted(false)
                .build();
    }

    public static Promotion newPromotion(Store store) {
        return Promotion.builder()
                .code("SALE10")
                .title("Giảm giá 10% mùa hè")
                .discountType(DiscountType.PERCENTAGE)
                .discountValue(10.0)
                .minOrderValue(100000.0)
                .maxDiscountAmount(50000.0)
                .startDate(LocalDateTime.now().minusDays(1))
                .endDate(LocalDateTime.now().plusDays(30))
                .usageLimit(100)
                .usedCount(10)
                .isActive(true)
                .store(store)
                .build();
    }

    // === Mocked entities with pre-populated UUIDs (for Mockito unit tests) ===

    public static Store createStore() {
        Store store = newStore();
        store.setId(UUID.randomUUID());
        return store;
    }

    public static Branch createBranch(Store store) {
        Branch branch = newBranch(store);
        branch.setId(UUID.randomUUID());
        return branch;
    }

    public static User createUser(Store store, Branch branch, UserRole role) {
        User user = newUser(store, branch, role);
        user.setId(UUID.randomUUID());
        return user;
    }

    public static Category createCategory(Store store) {
        Category category = newCategory(store);
        category.setId(UUID.randomUUID());
        return category;
    }

    public static Supplier createSupplier(Store store) {
        Supplier supplier = newSupplier(store);
        supplier.setId(UUID.randomUUID());
        return supplier;
    }

    public static Product createProduct(Store store, Category category, Supplier supplier) {
        Product product = newProduct(store, category, supplier);
        product.setId(UUID.randomUUID());
        return product;
    }

    public static Customer createCustomer() {
        Customer customer = newCustomer();
        customer.setId(UUID.randomUUID());
        return customer;
    }

    public static Promotion createPromotion(Store store) {
        Promotion promotion = newPromotion(store);
        promotion.setId(UUID.randomUUID());
        return promotion;
    }

    public static Order createOrder(Branch branch, Customer customer, User cashier, Product product) {
        Order order = Order.builder()
                .id(UUID.randomUUID())
                .orderNumber("ORD-TEST-" + System.currentTimeMillis() % 10000)
                .branch(branch)
                .customer(customer)
                .cashier(cashier)
                .paymentType(PaymentType.CASH)
                .status(OrderStatus.COMPLETED)
                .subtotal(70000.0)
                .discount(10000.0)
                .tax(5600.0)
                .totalAmount(65600.0)
                .build();

        OrderItem item = OrderItem.builder()
                .id(UUID.randomUUID())
                .product(product)
                .quantity(2)
                .price(70000.0)
                .order(order)
                .build();

        order.setItems(new ArrayList<>(List.of(item)));
        return order;
    }
}
