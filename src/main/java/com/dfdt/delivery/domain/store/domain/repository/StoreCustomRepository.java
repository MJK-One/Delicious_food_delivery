package com.dfdt.delivery.domain.store.domain.repository;

import com.dfdt.delivery.domain.store.domain.enums.StoreStatus;
import com.dfdt.delivery.domain.store.presentation.dto.response.StoreAdminResDto;
import com.dfdt.delivery.domain.store.presentation.dto.response.StoreResDto;
import com.dfdt.delivery.domain.store.presentation.dto.response.StoreStatusRequestResDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface StoreCustomRepository {
    Page<StoreResDto> searchStores(Pageable pageable, UUID category, String name);

    Page<StoreAdminResDto> searchStoresAdmin(Pageable pageable, UUID category, String name, Boolean isDeleted);

    Page<StoreStatusRequestResDto> searchRequestStores(Pageable pageable, StoreStatus requested);
}
