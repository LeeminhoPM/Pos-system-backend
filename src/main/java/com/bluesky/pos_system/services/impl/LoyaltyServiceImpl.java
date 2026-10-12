package com.bluesky.pos_system.services.impl;

import com.bluesky.pos_system.domains.CustomerTier;
import com.bluesky.pos_system.domains.LoyaltyTransactionType;
import com.bluesky.pos_system.mappers.OrderMapper;
import com.bluesky.pos_system.models.Customer;
import com.bluesky.pos_system.models.CustomerLoyaltyTransaction;
import com.bluesky.pos_system.models.Order;
import com.bluesky.pos_system.payload.dto.*;
import com.bluesky.pos_system.repositories.CustomerLoyaltyRepository;
import com.bluesky.pos_system.repositories.CustomerRepository;
import com.bluesky.pos_system.repositories.OrderRepository;
import com.bluesky.pos_system.services.LoyaltyService;
import jakarta.persistence.EntityNotFoundException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class LoyaltyServiceImpl implements LoyaltyService {
    CustomerRepository customerRepository;
    CustomerLoyaltyRepository loyaltyRepository;
    OrderRepository orderRepository;

    private static final double POINT_EARN_BASE_RATIO = 10000.0; // 10,000 VND = 1 base point
    private static final double POINT_REDEEM_VALUE = 1000.0;     // 1 point = 1,000 VND discount

    @Override
    @Transactional
    public LoyaltyTransactionDTO earnPoints(Customer customer, Order order, double orderAmount) {
        if (customer == null || orderAmount <= 0) {
            return null;
        }

        // Tier multipliers
        CustomerTier currentTier = customer.getTier() != null ? customer.getTier() : CustomerTier.BRONZE;
        double multiplier = switch (currentTier) {
            case DIAMOND -> 2.0;
            case GOLD -> 1.5;
            case SILVER -> 1.25;
            default -> 1.0;
        };

        int pointsEarned = (int) Math.round((orderAmount / POINT_EARN_BASE_RATIO) * multiplier);
        if (pointsEarned <= 0) {
            pointsEarned = 1;
        }

        int currentBalance = customer.getLoyaltyPoints() != null ? customer.getLoyaltyPoints() : 0;
        int newBalance = currentBalance + pointsEarned;
        customer.setLoyaltyPoints(newBalance);

        // Evaluate Tier progression based on cumulative totalSpent
        double totalSpent = (customer.getTotalSpent() != null ? customer.getTotalSpent() : 0.0);
        CustomerTier newTier = evaluateTier(totalSpent);
        if (newTier != currentTier) {
            log.info("Customer {} promoted from Tier {} to {}", customer.getId(), currentTier, newTier);
            customer.setTier(newTier);
        }
        customerRepository.save(customer);

        // Record ledger transaction
        CustomerLoyaltyTransaction txn = CustomerLoyaltyTransaction.builder()
                .customer(customer)
                .order(order)
                .type(LoyaltyTransactionType.EARN)
                .pointsChange(pointsEarned)
                .balanceAfter(newBalance)
                .description(String.format("Tích điểm đơn hàng %s (Hạng %s, x%.2f)",
                        order != null ? order.getOrderNumber() : "POS", currentTier.name(), multiplier))
                .build();
        CustomerLoyaltyTransaction savedTxn = loyaltyRepository.save(txn);

        return mapToDTO(savedTxn);
    }

    @Override
    @Transactional
    public RedeemPointsResponseDTO redeemPoints(RedeemPointsRequestDTO request) {
        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy khách hàng"));

        int currentPoints = customer.getLoyaltyPoints() != null ? customer.getLoyaltyPoints() : 0;
        int pointsToRedeem = request.getPointsToRedeem();

        if (pointsToRedeem <= 0) {
            throw new IllegalArgumentException("Số điểm quy đổi phải lớn hơn 0");
        }
        if (currentPoints < pointsToRedeem) {
            throw new IllegalArgumentException(String.format(
                    "Số điểm tích lũy không đủ. Hiện có: %d điểm, yêu cầu đổi: %d điểm", currentPoints, pointsToRedeem));
        }

        int remainingPoints = currentPoints - pointsToRedeem;
        customer.setLoyaltyPoints(remainingPoints);
        customerRepository.save(customer);

        Order order = null;
        if (request.getOrderId() != null) {
            order = orderRepository.findById(request.getOrderId()).orElse(null);
        }

        double discountAmount = pointsToRedeem * POINT_REDEEM_VALUE;

        // Record redeem transaction
        CustomerLoyaltyTransaction txn = CustomerLoyaltyTransaction.builder()
                .customer(customer)
                .order(order)
                .type(LoyaltyTransactionType.REDEEM)
                .pointsChange(-pointsToRedeem)
                .balanceAfter(remainingPoints)
                .description(String.format("Đổi %d điểm thưởng giảm trừ %,.0f VND", pointsToRedeem, discountAmount))
                .build();
        loyaltyRepository.save(txn);

        return RedeemPointsResponseDTO.builder()
                .customerId(customer.getId())
                .customerName(customer.getFullName())
                .redeemedPoints(pointsToRedeem)
                .discountAmount(discountAmount)
                .remainingPoints(remainingPoints)
                .message(String.format("Quy đổi thành công %d điểm tương đương giảm %,.0f VND", pointsToRedeem, discountAmount))
                .build();
    }

    @Override
    public PageResponse<LoyaltyTransactionDTO> getCustomerLoyaltyHistory(UUID customerId, int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size);
        Page<CustomerLoyaltyTransaction> pageResult = loyaltyRepository.findByCustomerIdOrderByCreatedAtDesc(customerId, pageRequest);

        List<LoyaltyTransactionDTO> dtoList = pageResult.getContent().stream()
                .map(this::mapToDTO)
                .toList();

        return PageResponse.<LoyaltyTransactionDTO>builder()
                .content(dtoList)
                .pageNumber(pageResult.getNumber())
                .pageSize(pageResult.getSize())
                .totalElements(pageResult.getTotalElements())
                .totalPages(pageResult.getTotalPages())
                .isLast(pageResult.isLast())
                .build();
    }

    @Override
    public CustomerCRMDetailDTO getCustomerCRMDetail(UUID customerId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy khách hàng"));

        List<CustomerLoyaltyTransaction> recentTxns = loyaltyRepository.findByCustomerIdOrderByCreatedAtDesc(customerId)
                .stream().limit(10).toList();

        List<Order> customerOrders = orderRepository.findByCustomerId(customerId);
        long totalOrders = customerOrders.size();
        double totalSpent = customer.getTotalSpent() != null ? customer.getTotalSpent() : 0.0;
        double aov = totalOrders > 0 ? totalSpent / totalOrders : 0.0;

        List<OrderDTO> recentOrders = customerOrders.stream()
                .sorted((a, b) -> {
                    if (a.getCreatedAt() == null || b.getCreatedAt() == null) return 0;
                    return b.getCreatedAt().compareTo(a.getCreatedAt());
                })
                .limit(5)
                .map(OrderMapper::toDTO)
                .toList();

        var lastOrderDate = customerOrders.stream()
                .map(Order::getCreatedAt)
                .filter(Objects::nonNull)
                .max(LocalDateTime::compareTo)
                .orElse(null);

        // Segment calculation
        String segmentName;
        if (totalSpent >= 50000000.0) {
            segmentName = "VIP_PLATINUM";
        } else if (totalSpent >= 15000000.0) {
            segmentName = "HIGH_VALUE";
        } else if (totalOrders >= 5) {
            segmentName = "LOYAL_REGULAR";
        } else if (totalOrders >= 1) {
            segmentName = "RETURNING";
        } else {
            segmentName = "NEW_PROSPECT";
        }

        return CustomerCRMDetailDTO.builder()
                .id(customer.getId())
                .customerCode(customer.getCustomerCode())
                .fullName(customer.getFullName())
                .email(customer.getEmail())
                .phone(customer.getPhone())
                .address(customer.getAddress())
                .tier(customer.getTier() != null ? customer.getTier() : CustomerTier.BRONZE)
                .dateOfBirth(customer.getDateOfBirth())
                .gender(customer.getGender())
                .loyaltyPoints(customer.getLoyaltyPoints() != null ? customer.getLoyaltyPoints() : 0)
                .totalSpent(totalSpent)
                .totalOrders(totalOrders)
                .averageOrderValue(aov)
                .segmentName(segmentName)
                .createdAt(customer.getCreatedAt())
                .lastOrderDate(lastOrderDate)
                .recentLoyaltyTransactions(recentTxns.stream().map(this::mapToDTO).toList())
                .recentOrders(recentOrders)
                .build();
    }

    @Override
    public CustomerSegmentSummaryDTO getCustomerSegmentSummary() {
        List<Customer> allCustomers = customerRepository.findAll();
        long total = allCustomers.size();
        long bronze = allCustomers.stream().filter(c -> c.getTier() == CustomerTier.BRONZE || c.getTier() == null).count();
        long silver = allCustomers.stream().filter(c -> c.getTier() == CustomerTier.SILVER).count();
        long gold = allCustomers.stream().filter(c -> c.getTier() == CustomerTier.GOLD).count();
        long diamond = allCustomers.stream().filter(c -> c.getTier() == CustomerTier.DIAMOND).count();

        double totalPoints = allCustomers.stream()
                .mapToDouble(c -> c.getLoyaltyPoints() != null ? c.getLoyaltyPoints() : 0)
                .sum();

        Map<String, Long> segmentCounts = new HashMap<>();
        segmentCounts.put("BRONZE", bronze);
        segmentCounts.put("SILVER", silver);
        segmentCounts.put("GOLD", gold);
        segmentCounts.put("DIAMOND", diamond);

        return CustomerSegmentSummaryDTO.builder()
                .totalCustomers(total)
                .bronzeCount(bronze)
                .silverCount(silver)
                .goldCount(gold)
                .diamondCount(diamond)
                .totalLoyaltyPointsCirculating(totalPoints)
                .segmentCounts(segmentCounts)
                .build();
    }

    private CustomerTier evaluateTier(double totalSpent) {
        if (totalSpent >= 50000000.0) {
            return CustomerTier.DIAMOND;
        } else if (totalSpent >= 15000000.0) {
            return CustomerTier.GOLD;
        } else if (totalSpent >= 5000000.0) {
            return CustomerTier.SILVER;
        }
        return CustomerTier.BRONZE;
    }

    private LoyaltyTransactionDTO mapToDTO(CustomerLoyaltyTransaction txn) {
        return LoyaltyTransactionDTO.builder()
                .id(txn.getId())
                .customerId(txn.getCustomer() != null ? txn.getCustomer().getId() : null)
                .customerName(txn.getCustomer() != null ? txn.getCustomer().getFullName() : null)
                .orderId(txn.getOrder() != null ? txn.getOrder().getId() : null)
                .orderNumber(txn.getOrder() != null ? txn.getOrder().getOrderNumber() : null)
                .type(txn.getType())
                .pointsChange(txn.getPointsChange())
                .balanceAfter(txn.getBalanceAfter())
                .description(txn.getDescription())
                .createdAt(txn.getCreatedAt())
                .build();
    }
}
