package com.dfdt.delivery.domain.store.application.service.query;

import com.dfdt.delivery.domain.store.presentation.dto.response.MyStoreResDto;
import com.dfdt.delivery.domain.store.presentation.dto.response.StoreAdminResDto;
import com.dfdt.delivery.domain.store.presentation.dto.response.StoreResDto;
import com.dfdt.delivery.domain.store.presentation.dto.response.StoreStatusRequestResDto;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.UUID;

public interface StoreQueryService {
    StoreResDto getStore(UUID storeId);

    Page<StoreResDto> getStores(int page, int size, String sortBy, boolean isAsc, UUID category, String name, UUID region);

    Page<StoreAdminResDto> getStoresAdmin(int page, int size, String sortBy, boolean isAsc, UUID category, String name, UUID region, Boolean isDeleted);

    List<MyStoreResDto> getMyStores(String username);

    Page<StoreStatusRequestResDto> getRequestedStores(int page, int size, String sortBy, boolean isAsc);
}
