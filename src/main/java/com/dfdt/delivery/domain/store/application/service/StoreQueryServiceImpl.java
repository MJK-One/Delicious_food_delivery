package com.dfdt.delivery.domain.store.application.service;

import com.dfdt.delivery.common.exception.BusinessException;
import com.dfdt.delivery.domain.store.application.service.query.StoreQueryService;
import com.dfdt.delivery.domain.store.domain.entity.Store;
import com.dfdt.delivery.domain.store.domain.entity.StoreRating;
import com.dfdt.delivery.domain.store.domain.enums.StoreErrorCode;
import com.dfdt.delivery.domain.store.domain.enums.StoreStatus;
import com.dfdt.delivery.domain.store.domain.repository.JpaStoreRepository;
import com.dfdt.delivery.domain.store.domain.repository.StoreCustomRepository;
import com.dfdt.delivery.domain.store.domain.repository.StoreRatingRepository;
import com.dfdt.delivery.domain.store.presentation.dto.response.MyStoreResDto;
import com.dfdt.delivery.domain.store.presentation.dto.response.StoreAdminResDto;
import com.dfdt.delivery.domain.store.presentation.dto.response.StoreResDto;
import com.dfdt.delivery.domain.store.presentation.dto.response.StoreStatusRequestResDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StoreQueryServiceImpl implements StoreQueryService {

    private final JpaStoreRepository storeRepository;
    private final StoreCustomRepository storeCustomRepository;
    private final StoreRatingRepository storeRatingRepository;

    public StoreResDto getStore(UUID storeId) {
        Store store = findStoreById(storeId);
        StoreRating rating = storeRatingRepository.findById(storeId).orElse(null);

        return StoreResDto.from(store, rating);
    }

    public Page<StoreResDto> getStores(int page, int size, String sortBy, boolean isAsc, UUID category, String name) {
        Pageable pageable = createPageable(page, size, sortBy, isAsc);
        Page<StoreResDto> storeResDto = storeCustomRepository.searchStores(pageable, category, name);

        checkStores(storeResDto.getTotalElements());

        return storeResDto;
    }

    public Page<StoreAdminResDto> getStoresAdmin(int page, int size, String sortBy, boolean isAsc, UUID category, String name, Boolean isDeleted) {
        Pageable pageable = createPageable(page, size, sortBy, isAsc);
        Page<StoreAdminResDto> storeAdminResDto = storeCustomRepository.searchStoresAdmin(pageable, category, name, isDeleted);

        checkStores(storeAdminResDto.getTotalElements());

        return storeAdminResDto;
    }

    public List<MyStoreResDto> getMyStores(String username) {
        return storeRepository
                .findByUser_UsernameOrderByCreateAuditAsc(username)
                .stream()
                .map(MyStoreResDto::from)
                .toList();
    }

    public Page<StoreStatusRequestResDto> getRequestedStores(int page, int size, String sortBy, boolean isAsc) {
        Pageable pageable = createPageable(page, size, sortBy, isAsc);
        Page<StoreStatusRequestResDto> requestResDto = storeCustomRepository.searchRequestStores(pageable, StoreStatus.REQUESTED);

        checkStores(requestResDto.getTotalElements());

        return requestResDto;
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

    // 등록된 가게가 있는지 확인
    private static void checkStores(long totalElements) {
        if (totalElements == 0) {
            throw new BusinessException(StoreErrorCode.NOT_FOUND_STORES);
        }
    }

}
