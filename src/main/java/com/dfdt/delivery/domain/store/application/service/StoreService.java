package com.dfdt.delivery.domain.store.application.service;

import com.dfdt.delivery.common.exception.BusinessException;
import com.dfdt.delivery.domain.category.domain.entity.Category;
import com.dfdt.delivery.domain.category.domain.enums.CategoryErrorCode;
import com.dfdt.delivery.domain.category.domain.repository.CategoryRepository;
import com.dfdt.delivery.domain.region.domain.entity.Region;
import com.dfdt.delivery.domain.region.domain.enums.RegionErrorCode;
import com.dfdt.delivery.domain.region.domain.repository.RegionRepository;
import com.dfdt.delivery.domain.store.domain.entity.Store;
import com.dfdt.delivery.domain.store.domain.entity.StoreCategory;
import com.dfdt.delivery.domain.store.domain.entity.StoreRating;
import com.dfdt.delivery.domain.store.domain.enums.StoreErrorCode;
import com.dfdt.delivery.domain.store.domain.enums.StoreStatus;
import com.dfdt.delivery.domain.store.domain.repository.StoreCategoryRepository;
import com.dfdt.delivery.domain.store.domain.repository.StoreCustomRepository;
import com.dfdt.delivery.domain.store.domain.repository.StoreRatingRepository;
import com.dfdt.delivery.domain.store.domain.repository.StoreRepository;
import com.dfdt.delivery.domain.store.presentation.dto.request.StoreCreateReqDto;
import com.dfdt.delivery.domain.store.presentation.dto.request.StoreStatusReqDto;
import com.dfdt.delivery.domain.store.presentation.dto.request.StoreUpdateReqDto;
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
    private final RegionRepository regionRepository;

    @Transactional(readOnly = true)
    public StoreResDto getStore(UUID storeId) {
        Store store = findStoreById(storeId);
        StoreRating rating = storeRatingRepository.findById(storeId).orElse(null);

        return StoreResDto.from(store, rating);
    }

    @Transactional(readOnly = true)
    public Page<StoreResDto> getStores(int page, int size, String sortBy, boolean isAsc, UUID category, String name) {
        Pageable pageable = createPageable(page, size, sortBy, isAsc);
        Page<StoreResDto> storeResDto = storeCustomRepository.searchStores(pageable, category, name);

        checkStores(storeResDto.getTotalElements());

        return storeResDto;
    }

    @Transactional(readOnly = true)
    public Page<StoreAdminResDto> getStoresAdmin(int page, int size, String sortBy, boolean isAsc, UUID category, String name, Boolean isDeleted) {
        Pageable pageable = createPageable(page, size, sortBy, isAsc);
        Page<StoreAdminResDto> storeAdminResDto = storeCustomRepository.searchStoresAdmin(pageable, category, name, isDeleted);

        checkStores(storeAdminResDto.getTotalElements());

        return storeAdminResDto;
    }

    public StoreCreateResDto createStore(StoreCreateReqDto request, User user) {
        List<Category> categories = categoryRepository.findAllById(request.getCategoryIds());

        checkExistCategory(request.getCategoryIds().size(), categories);
        Region region = regionRepository.findById(request.getRegionId()).orElseThrow(() -> new BusinessException(RegionErrorCode.NOT_FOUND_REGION));

        Store store = Store.create(request, user, region);
        store.addCategories(categories, user.getUsername());
        storeRepository.save(store);

        return StoreCreateResDto.from(store);
    }

    public StoreUpdateResDto updateStore(UUID storeId, StoreUpdateReqDto request, User user) {
        List<Category> newCategories = categoryRepository.findAllById(request.getCategoryIds());
        Store store = findStoreById(storeId);
        List<StoreCategory> before = storeCategoryRepository.findByStore(store);

        checkExistCategory(request.getCategoryIds().size(), newCategories);
        checkMyStore(user, store);
        checkDeletedStore(store);

        // 1. 기존 active 카테고리
        List<StoreCategory> existing = before.stream()
                .filter(sc -> sc.getSoftDeleteAudit() == null)
                .toList();

        // 2. 삭제 처리 (기존에는 있는데 새 리스트에는 없는 경우)
        existing.stream()
                .filter(sc -> !newCategories.contains(sc.getCategory()))
                .forEach(sc -> sc.delete(user.getUsername())
                );

        // 3. 새로 추가 (새 리스트에는 있는데 기존에는 없는 경우)
        newCategories.stream()
                .filter(c -> existing.stream().noneMatch(sc -> sc.getCategory().equals(c)))
                .forEach(c -> store.addCategory(c, user.getUsername()));

        store.update(request, user.getUsername());

        return StoreUpdateResDto.from(store);
    }

    public void deleteStore(UUID storeId, User user) {
        Store store = findStoreById(storeId);
        checkMyStore(user, store);

        if (store.getSoftDeleteAudit() == null) {
            throw new BusinessException(StoreErrorCode.ALREADY_DELETED);    // 삭제된 가게인지 확인
        }

        store.delete(user.getUsername());
    }

    public void changeIsOpen(UUID storeId, User user) {
        Store store = findStoreById(storeId);
        checkMyStore(user, store);
        checkDeletedStore(store);

        store.changeIsOpen(user.getUsername());
    }

    public List<MyStoreResDto> getMyStores(String username) {
        return storeRepository
                .findByUser_UsernameOrderByCreateAuditAsc(username)
                .stream()
                .map(MyStoreResDto::from)
                .toList();
    }

    public void restoreStore(UUID storeId, User user) {
        // 영업 중지된 가게인지 확인
        Store store = findStoreById(storeId);
        if (!store.getStatus().equals(StoreStatus.SUSPENDED)) {
            throw new BusinessException(StoreErrorCode.NOT_SUSPENDED);
        }

        store.restore(user.getUsername());
    }

    public StoreStatusResDto changeStatus(UUID storeId, StoreStatusReqDto request, User user) {
        Store store = findStoreById(storeId);
        store.changeStatus(request.getStatus(), user.getUsername());

        return StoreStatusResDto.from(store);
    }

    public List<StoreStatusRequestResDto> getRequestedStores() {
        List<Store> stores = storeRepository.findStoresByStatusNotDeleted(StoreStatus.REQUESTED);
        if (stores.isEmpty()) {
            return null;
        }

        return stores.stream()
                .map(StoreStatusRequestResDto::from)
                .toList();
    }

    private Pageable createPageable(int page, int size, String sortBy, boolean isAsc) {
        Sort.Direction direction = isAsc ? Sort.Direction.ASC : Sort.Direction.DESC;
        Sort sort = Sort.by(direction, sortBy);
        return PageRequest.of(page, size, sort);
    }

    // 해당 가게가 존재하는지 확인
    private Store findStoreById(UUID storeId) {
        return storeRepository.findById(storeId).orElseThrow(() -> new BusinessException(StoreErrorCode.NOT_FOUND_STORE));
    }

    // 해당 카테고리가 존재하는지 확인
    private void checkExistCategory(Integer size, List<Category> categories) {
        if (categories.isEmpty() || categories.size() != size) {
            throw new BusinessException(CategoryErrorCode.NOT_FOUND_CATEGORY);
        }
    }

    // 등록된 가게가 있는지 확인
    private static void checkStores(long totalElements) {
        if (totalElements == 0) {
            throw new BusinessException(StoreErrorCode.NOT_FOUND_STORES);
        }
    }

    // 본인 소유의 가게만 정보 변경 가능
    private static void checkMyStore(User user, Store store) {
        if (!store.getUser().getUsername().equals(user.getUsername()) && !user.getRole().equals(UserRole.MASTER)) {
            throw new BusinessException(StoreErrorCode.NOT_MY_STORE);
        }
    }

    // 삭제된 가게는 정보 변경 X
    private static void checkDeletedStore(Store store) {
        if (store.getSoftDeleteAudit() != null) {
            throw new BusinessException(StoreErrorCode.NOT_MODIFIED);
        }
    }

}
