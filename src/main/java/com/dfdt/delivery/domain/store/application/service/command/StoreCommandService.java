package com.dfdt.delivery.domain.store.application.service.command;

import com.dfdt.delivery.domain.auth.infrastructure.security.CustomUserDetails;
import com.dfdt.delivery.domain.store.presentation.dto.request.StoreCreateReqDto;
import com.dfdt.delivery.domain.store.presentation.dto.request.StoreStatusReqDto;
import com.dfdt.delivery.domain.store.presentation.dto.request.StoreUpdateReqDto;
import com.dfdt.delivery.domain.store.presentation.dto.response.StoreCreateResDto;
import com.dfdt.delivery.domain.store.presentation.dto.response.StoreStatusResDto;
import com.dfdt.delivery.domain.store.presentation.dto.response.StoreUpdateResDto;

import java.util.UUID;

public interface StoreCommandService {
    StoreCreateResDto createStore(StoreCreateReqDto request, CustomUserDetails userDetails);

    StoreUpdateResDto updateStore(UUID storeId, StoreUpdateReqDto request, CustomUserDetails userDetails);

    void deleteStore(UUID storeId, CustomUserDetails user);

    void changeIsOpen(UUID storeId, CustomUserDetails userDetails);

    void restoreStore(UUID storeId, CustomUserDetails user);

    StoreStatusResDto changeStatus(UUID storeId, StoreStatusReqDto request, CustomUserDetails userDetails);
}
