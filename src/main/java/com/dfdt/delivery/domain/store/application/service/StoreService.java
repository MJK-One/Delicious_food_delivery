package com.dfdt.delivery.domain.store.application.service;

import com.dfdt.delivery.domain.category.repository.CategoryRepository;
import com.dfdt.delivery.domain.store.domain.repository.StoreCategoryRepository;
import com.dfdt.delivery.domain.store.domain.repository.StoreCustomRepository;
import com.dfdt.delivery.domain.store.domain.repository.StoreRatingRepository;
import com.dfdt.delivery.domain.store.domain.repository.StoreRepository;
import com.dfdt.delivery.domain.store.presentation.dto.request.StoreCreateReqDto;
import com.dfdt.delivery.domain.store.presentation.dto.request.StoreStatusReqDto;
import com.dfdt.delivery.domain.store.presentation.dto.request.StoreUpdateReqDto;
import com.dfdt.delivery.domain.category.entity.Category;
import com.dfdt.delivery.domain.store.domain.entity.Store;
import com.dfdt.delivery.domain.store.domain.entity.StoreCategory;
import com.dfdt.delivery.domain.store.domain.entity.StoreRating;
import com.dfdt.delivery.domain.store.domain.enums.StoreStatus;
import com.dfdt.delivery.domain.store.presentation.dto.response.*;
import com.dfdt.delivery.domain.user.entity.User;
import com.dfdt.delivery.domain.user.enums.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class StoreService {

    private final StoreRepository storeRepository;
    private final StoreCustomRepository storeCustomRepository;
    private final CategoryRepository categoryRepository;
    private final StoreCategoryRepository storeCategoryRepository;
    private final StoreRatingRepository storeRatingRepository;

    @Transactional(readOnly = true)
    public StoreResDto getStore(UUID storeId) {
        Store store = storeRepository.findByStoreIdAndDeletedAtIsNull(storeId)
                .filter(s -> s.getRegion().getIsOrderEnabled())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않거나 삭제된 가게입니다."));

        StoreRating rating = storeRatingRepository.findById(storeId).orElse(null);

        return StoreResDto.from(store, rating);
    }

    @Transactional(readOnly = true)
    public Page<StoreResDto> getStores(int page, int size, String sortBy, boolean isAsc, UUID category, String name) {
        Sort.Direction direction = isAsc ? Sort.Direction.ASC : Sort.Direction.DESC;
        Sort sort = Sort.by(direction, sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        return storeCustomRepository.searchStores(pageable, category, name);
    }

    public StoreCreateResDto createStore(StoreCreateReqDto request, User user) {
        List<Category> categories = categoryRepository.findAllById(request.getCategoryIds());
        if (categories.isEmpty() || categories.size() != request.getCategoryIds().size()) {
            throw new IllegalArgumentException("해당 카테고리가 존재하지 않습니다.");
        }

        Store store = Store.create(request, user);
        store.addCategories(categories);
        storeRepository.save(store);

        return StoreCreateResDto.from(store);
    }

    public StoreUpdateResDto updateStore(UUID storeId, StoreUpdateReqDto request, User user) {
        List<Category> newCategories = categoryRepository.findAllById(request.getCategoryIds());
        if (newCategories.isEmpty() || newCategories.size() != request.getCategoryIds().size()) {
            throw new IllegalArgumentException("해당 카테고리가 존재하지 않습니다.");
        }
        Store store = storeRepository.findById(storeId).orElseThrow(() -> new IllegalArgumentException("해당 가게를 찾을 수 없습니다."));
        if (!store.getUser().getUsername().equals(user.getUsername()) || user.getRole().equals(UserRole.OWNER)) {
            throw new SecurityException("본인 소유 가게만 수정할 수 있습니다.");
        }

        List<StoreCategory> before = storeCategoryRepository.findByStore(store);

        // 1. 기존 active 카테고리
//        List<StoreCategory> existing = before.stream()
//                .filter(sc -> sc.getDeletedAt() == null)
//                .toList();
//
//        // 2. 삭제 처리 (기존에는 있는데 새 리스트에는 없는 경우)
//        existing.stream()
//                .filter(sc -> !newCategories.contains(sc.getCategory()))
//                .forEach(sc -> sc.deleteStoreCategory(user)
//                );
//
//        // 3. 새로 추가 (새 리스트에는 있는데 기존에는 없는 경우)
//        newCategories.stream()
//                .filter(c -> existing.stream().noneMatch(sc -> sc.getCategory().equals(c)))
//                .forEach(store::addCategory);

        store.update(request);

        return StoreUpdateResDto.from(store);
    }

    public void deleteStore(UUID storeId, User user) {
        Store store = storeRepository.findById(storeId).orElseThrow(() -> new IllegalArgumentException("해당 가게를 찾을 수 없습니다."));
        if (!store.getUser().getUsername().equals(user.getUsername()) || user.getRole().equals(UserRole.OWNER)) {
            throw new SecurityException("본인 소유 가게만 삭제할 수 있습니다.");
        }

//        store.delete(user.getUsername());
    }

    public void changeIsOpen(UUID storeId, User user) {
        Store store = storeRepository.findById(storeId).orElseThrow(() -> new IllegalArgumentException("해당 가게를 찾을 수 없습니다."));
        if (!store.getUser().getUsername().equals(user.getUsername()) || user.getRole().equals(UserRole.OWNER)) {
            throw new SecurityException("본인 소유 가게만 영업 상태를 변경할 수 있습니다.");
        }

        store.changeIsOpen();
    }

    public List<MyStoreResDto> getMyStores(String username) {
        return storeRepository
                .findByUsername(username)
                .stream()
                .map(MyStoreResDto::from)
                .toList();
    }

    public void restoreStore(UUID storeId) {
        Store store = storeRepository.findById(storeId).orElseThrow(() -> new IllegalArgumentException("해당 가게를 찾을 수 없습니다."));
        if (!store.getStatus().equals(StoreStatus.SUSPENDED)) {
            throw new IllegalArgumentException("영업 중지된 가게가 아닙니다.");
        }

        store.restore();
    }

    public StoreStatusResDto changeStatus(UUID storeId, StoreStatusReqDto request) {
        Store store = storeRepository.findById(storeId).orElseThrow(() -> new IllegalArgumentException("해당 가게를 찾을 수 없습니다."));
        store.changeStatus(request.getStatus());

        return StoreStatusResDto.from(store);
    }
}
