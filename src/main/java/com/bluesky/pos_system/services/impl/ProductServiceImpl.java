package com.bluesky.pos_system.services.impl;

import com.bluesky.pos_system.domains.ProductStatus;
import com.bluesky.pos_system.mappers.ProductMapper;
import com.bluesky.pos_system.models.Category;
import com.bluesky.pos_system.models.Product;
import com.bluesky.pos_system.models.Store;
import com.bluesky.pos_system.models.Supplier;
import com.bluesky.pos_system.models.User;
import com.bluesky.pos_system.payload.dto.PageResponse;
import com.bluesky.pos_system.payload.dto.ProductDTO;
import com.bluesky.pos_system.repositories.CategoryRepository;
import com.bluesky.pos_system.repositories.ProductRepository;
import com.bluesky.pos_system.repositories.StoreRepository;
import com.bluesky.pos_system.repositories.SupplierRepository;
import com.bluesky.pos_system.services.ProductService;
import jakarta.persistence.EntityNotFoundException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProductServiceImpl implements ProductService {
    ProductRepository productRepository;
    StoreRepository storeRepository;
    CategoryRepository categoryRepository;
    SupplierRepository supplierRepository;

    @Override
    @Transactional
    public ProductDTO createProduct(ProductDTO productDTO, User user) {
        UUID storeId = productDTO.getStoreId();
        if (storeId == null && user != null && user.getStore() != null) {
            storeId = user.getStore().getId();
        }
        if (storeId == null) {
            throw new RuntimeException("Cửa hàng không được để trống");
        }

        final UUID finalStoreId = storeId;
        Store store = storeRepository.findById(finalStoreId).orElseThrow(
                () -> new RuntimeException("Cửa hàng với id: " + finalStoreId + " không được tìm thấy")
        );

        Category category = null;
        if (productDTO.getCategoryId() != null) {
            category = categoryRepository.findById(productDTO.getCategoryId()).orElse(null);
        }

        Supplier supplier = null;
        if (productDTO.getSupplierId() != null) {
            supplier = supplierRepository.findById(productDTO.getSupplierId()).orElse(null);
        }

        if (productDTO.getSku() == null || productDTO.getSku().isBlank()) {
            productDTO.setSku("SKU-" + System.currentTimeMillis() % 1000000);
        }
        if (productDTO.getBarcode() == null || productDTO.getBarcode().isBlank()) {
            productDTO.setBarcode(productDTO.getSku());
        }

        Product product = ProductMapper.toEntity(productDTO, store, category, supplier);
        return ProductMapper.toDTO(productRepository.save(product));
    }

    @Override
    @Transactional
    @CacheEvict(value = "products", key = "#id")
    public ProductDTO updateProduct(UUID id, ProductDTO productDTO, User user) {
        Product product = productRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("Không tìm thấy sản phẩm")
        );

        if (productDTO.getCategoryId() != null) {
            Category category = categoryRepository.findById(productDTO.getCategoryId()).orElse(null);
            product.setCategory(category);
        }
        if (productDTO.getSupplierId() != null) {
            Supplier supplier = supplierRepository.findById(productDTO.getSupplierId()).orElse(null);
            product.setSupplier(supplier);
        }

        product.setName(productDTO.getName());
        product.setDescription(productDTO.getDescription());
        if (productDTO.getSku() != null && !productDTO.getSku().isBlank()) {
            product.setSku(productDTO.getSku());
        }
        if (productDTO.getBarcode() != null && !productDTO.getBarcode().isBlank()) {
            product.setBarcode(productDTO.getBarcode());
        }
        product.setImage(productDTO.getImage());
        product.setMrp(productDTO.getMrp());
        product.setCostPrice(productDTO.getCostPrice());
        product.setSellingPrice(productDTO.getSellingPrice());
        product.setBrand(productDTO.getBrand());
        if (productDTO.getVatRate() != null) {
            product.setVatRate(productDTO.getVatRate());
        }
        if (productDTO.getStatus() != null) {
            product.setStatus(productDTO.getStatus());
        }
        if (productDTO.getMinStockLevel() != null) {
            product.setMinStockLevel(productDTO.getMinStockLevel());
        }
        if (productDTO.getIsActive() != null) {
            product.setIsActive(productDTO.getIsActive());
        }

        return ProductMapper.toDTO(productRepository.save(product));
    }

    @Override
    @Cacheable(value = "products", key = "#id")
    public ProductDTO getProductById(UUID id) {
        Product product = productRepository.findByIdAndIsDeletedFalse(id).orElseThrow(
                () -> new EntityNotFoundException("Không tìm thấy sản phẩm với id: " + id)
        );
        return ProductMapper.toDTO(product);
    }

    @Override
    @Transactional
    @CacheEvict(value = "products", key = "#id")
    public void deleteProduct(UUID id, User user) {
        softDeleteProduct(id, user);
    }

    @Override
    @Transactional
    @CacheEvict(value = "products", key = "#id")
    public void softDeleteProduct(UUID id, User user) {
        Product product = productRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("Không tìm thấy sản phẩm")
        );
        product.setIsDeleted(true);
        product.setDeletedAt(LocalDateTime.now());
        product.setIsActive(false);
        productRepository.save(product);
    }

    @Override
    public List<ProductDTO> getAllProductsByStoreId(UUID storeId) {
        List<Product> products = productRepository.findByStoreIdAndIsDeletedFalse(storeId);
        return products.stream().map(ProductMapper::toDTO).toList();
    }

    @Override
    public PageResponse<ProductDTO> getProductsPaged(
            UUID storeId, int page, int size, String keyword, UUID categoryId, ProductStatus status) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("createdAt").descending());
        String kw = (keyword != null && !keyword.isBlank()) ? keyword.trim() : null;

        Page<Product> pageResult = productRepository.filterProducts(storeId, kw, categoryId, status, pageRequest);
        List<ProductDTO> dtoList = pageResult.getContent().stream()
                .map(ProductMapper::toDTO)
                .toList();

        return PageResponse.<ProductDTO>builder()
                .content(dtoList)
                .pageNumber(pageResult.getNumber())
                .pageSize(pageResult.getSize())
                .totalElements(pageResult.getTotalElements())
                .totalPages(pageResult.getTotalPages())
                .isLast(pageResult.isLast())
                .build();
    }

    @Override
    public List<ProductDTO> searchByKeyword(UUID storeId, String keyword) {
        List<Product> products = productRepository.searchByKeyword(storeId, keyword);
        return products.stream().map(ProductMapper::toDTO).toList();
    }
}
