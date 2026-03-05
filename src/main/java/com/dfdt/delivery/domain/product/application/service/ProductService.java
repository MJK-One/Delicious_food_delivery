package com.dfdt.delivery.domain.product.application.service;

import com.dfdt.delivery.domain.product.domain.entity.Product;
import com.dfdt.delivery.domain.product.domain.repository.ProductCustomRepository;
import com.dfdt.delivery.domain.product.domain.repository.ProductRepository;
import com.dfdt.delivery.domain.product.presentation.dto.request.ProductCreateReqDto;
import com.dfdt.delivery.domain.product.presentation.dto.request.ProductUpdateReqDto;
import com.dfdt.delivery.domain.product.presentation.dto.response.ProductResDto;
import com.dfdt.delivery.domain.store.domain.entity.Store;
import com.dfdt.delivery.domain.user.domain.entity.User;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductCustomRepository productCustomRepository;
    private final EntityManager entityManager;

    @Transactional(readOnly = true)
    public ProductResDto getProduct(UUID storeId, UUID productId) {
        Product product = productRepository.findByProductIdAndStoreStoreIdAndDeletedAtIsNull(productId, storeId)
                .filter(p -> !Boolean.TRUE.equals(p.getIsHidden()))
                .orElseThrow(() -> new IllegalArgumentException("해당 메뉴가 존재하지 않습니다."));

        return ProductResDto.from(product);
    }

    @Transactional(readOnly = true)
    public Page<ProductResDto> getProducts(UUID storeId, int page, int size, String sortBy, boolean isAsc, String keyword) {
        Sort.Direction direction = isAsc ? Sort.Direction.ASC : Sort.Direction.DESC;
        Sort sort = Sort.by(direction, sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        return productCustomRepository.searchProducts(pageable, storeId, keyword);
    }

    public Product createProduct(UUID storeId, ProductCreateReqDto request, User user) {
        Integer displayOrder = request.getDisplayOrder();
        if (displayOrder == null) {
            Integer max = productRepository.findMaxDisplayOrderByStoreId(storeId);
            displayOrder = (max == null ? 0 : max) + 1;
        }

        Store storeRef = entityManager.getReference(Store.class, storeId);
        Product product = Product.create(
                storeRef,
                request.getName(),
                request.getDescription(),
                request.getPrice(),
                displayOrder
        );

        return productRepository.save(product);
    }

    public Product updateProduct(UUID storeId, UUID productId, ProductUpdateReqDto request, User user) {
        Product product = productRepository.findByProductIdAndStoreStoreIdAndDeletedAtIsNull(productId, storeId)
                .orElseThrow(() -> new IllegalArgumentException("해당 메뉴를 찾을 수 없습니다."));

        product.update(request.getName(), request.getDescription(), request.getPrice(), request.getDisplayOrder());
        return productRepository.save(product);
    }

    public void deleteProduct(UUID storeId, UUID productId, User user) {
        Product product = productRepository.findByProductIdAndStoreStoreIdAndDeletedAtIsNull(productId, storeId)
                .orElseThrow(() -> new IllegalArgumentException("해당 메뉴를 찾을 수 없습니다."));

        product.delete(user.getUsername());
    }

    public void markSoldOut(UUID storeId, UUID productId, User user) {
        Product product = productRepository.findByProductIdAndStoreStoreIdAndDeletedAtIsNull(productId, storeId)
                .orElseThrow(() -> new IllegalArgumentException("해당 메뉴를 찾을 수 없습니다."));

        product.soldOut();
    }

    public void restoreProduct(UUID storeId, UUID productId, User user) {
        Product product = productRepository.findByProductIdAndStoreStoreIdAndDeletedAtIsNull(productId, storeId)
                .orElseThrow(() -> new IllegalArgumentException("해당 메뉴를 찾을 수 없습니다."));

        product.restore();
    }
}

