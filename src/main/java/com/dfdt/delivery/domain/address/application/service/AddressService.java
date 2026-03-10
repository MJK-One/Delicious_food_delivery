package com.dfdt.delivery.domain.address.application.service;

import com.dfdt.delivery.common.exception.BusinessException;
import com.dfdt.delivery.domain.address.domain.entity.Address;
import com.dfdt.delivery.domain.address.domain.exception.error.enums.AddressErrorCode;
import com.dfdt.delivery.domain.address.domain.repository.AddressRepository;
import com.dfdt.delivery.domain.address.presentation.dto.AddressRequest;
import com.dfdt.delivery.domain.address.presentation.dto.AddressResponse;
import com.dfdt.delivery.domain.region.domain.entity.Region;
import com.dfdt.delivery.domain.region.domain.enums.RegionErrorCode;
import com.dfdt.delivery.domain.region.domain.repository.RegionRepository;
import com.dfdt.delivery.domain.user.domain.entity.User;
import com.dfdt.delivery.domain.user.domain.exception.error.enums.UserErrorCode;
import com.dfdt.delivery.domain.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 배송지 관련 비즈니스 로직을 처리하는 서비스.
 * 기본 배송지 자동 변경, 소유자 검증, Soft Delete 등을 포함.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AddressService {

    private final AddressRepository addressRepository;
    private final UserRepository userRepository;
    private final RegionRepository regionRepository;

    /**
     * 새로운 배송지를 등록.
     * 등록 시 'isDefault'가 true인 경우, 기존 기본 배송지를 일반 배송지로 변경.
     * @param username 사용자 계정
     * @param request 배송지 등록 요청 정보
     * @return 등록된 배송지 정보 응답 DTO
     */
    @Transactional
    public AddressResponse createAddress(String username, AddressRequest.Create request) {
        User user = findUserOrThrow(username);
        Region region = findRegionOrThrow(request.getRegionId());

        // 기본 배송지로 설정하는 경우 기존 기본 배송지 해제
        if (Boolean.TRUE.equals(request.getIsDefault())) {
            resetDefaultAddress(user);
        }

        Address address = Address.builder()
                .user(user)
                .region(region)
                .addressName(request.getAddressName())
                .addressLine1(request.getAddressLine1())
                .addressLine2(request.getAddressLine2())
                .receiverName(request.getReceiverName())
                .receiverPhone(request.getReceiverPhone())
                .isDefault(request.getIsDefault())
                .deliveryMemo(request.getDeliveryMemo())
                .build();

        Address savedAddress = addressRepository.save(address);
        return AddressResponse.from(savedAddress);
    }

    /**
     * 사용자의 유효한(삭제되지 않은) 배송지 목록을 조회.
     * @param username 사용자 계정
     * @return 배송지 목록 응답 DTO 리스트
     */
    public List<AddressResponse> getMyAddresses(String username) {
        User user = findUserOrThrow(username);
        return addressRepository.findByUserAndSoftDeleteAuditDeletedAtIsNull(user)
                .stream()
                .map(AddressResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 상세 정보를 조회합니다. 소유자 여부를 검증.
     * @param username 사용자 계정 (소유권 확인용)
     * @param addressId 배송지 ID
     * @return 배송지 상세 정보 응답 DTO
     */
    public AddressResponse getAddress(String username, UUID addressId) {
        Address address = findAddressAndValidateOwner(username, addressId);
        return AddressResponse.from(address);
    }

    /**
     * 배송지 정보를 업데이트합니다.
     * 기본 배송지 여부가 변경될 경우 기존 기본 배송지를 해제.
     * @param username 사용자 계정
     * @param addressId 수정할 배송지 ID
     * @param request 수정할 배송지 정보
     * @return 수정된 배송지 정보 응답 DTO
     */
    @Transactional
    public AddressResponse updateAddress(String username, UUID addressId, AddressRequest.Update request) {
        Address address = findAddressAndValidateOwner(username, addressId);

        // 지역 변경이 요청된 경우에만 조회 및 업데이트
        Region region = (request.getRegionId() != null) ? findRegionOrThrow(request.getRegionId()) : null;

        // 기본 배송지 상태 변경 시 중복 방지 로직 실행
        if (Boolean.TRUE.equals(request.getIsDefault())) {
            resetDefaultAddress(address.getUser());
        }

        address.updateAddressInfo(
                region,
                request.getAddressName(),
                request.getAddressLine1(),
                request.getAddressLine2(),
                request.getReceiverName(),
                request.getReceiverPhone(),
                request.getDeliveryMemo()
        );

        if (request.getIsDefault() != null) {
            address.changeDefaultStatus(request.getIsDefault());
        }

        return AddressResponse.from(address);
    }

    /**
     * 배송지를 논리적으로 삭제(Soft Delete).
     * @param username 사용자 계정 (소유권 확인용)
     * @param addressId 삭제할 배송지 ID
     */
    @Transactional
    public void deleteAddress(String username, UUID addressId) {
        Address address = findAddressAndValidateOwner(username, addressId);
        address.deleteAddress(username);
    }

    /**
     * 사용자의 기존 기본 배송지를 찾아 비활성화(false) 처리.
     */
    private void resetDefaultAddress(User user) {
        addressRepository.findByUserAndIsDefaultTrueAndSoftDeleteAuditDeletedAtIsNull(user)
                .ifPresent(address -> address.changeDefaultStatus(false));
    }

    /**
     * 사용자 식별자로 사용자 엔티티를 조회.
     */
    private User findUserOrThrow(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException(UserErrorCode.USER_NOT_FOUND));
    }

    /**
     * 지역 식별자로 지역 엔티티를 조회.
     */
    private Region findRegionOrThrow(UUID regionId) {
        return regionRepository.findById(regionId)
                .orElseThrow(() -> new BusinessException(RegionErrorCode.NOT_FOUND_REGION));
    }

    /**
     * 배송지 식별자로 조회하고, 요청한 사용자가 소유자인지 검증.
     */
    private Address findAddressAndValidateOwner(String username, UUID addressId) {
        Address address = addressRepository.findByAddressIdAndSoftDeleteAuditDeletedAtIsNull(addressId)
                .orElseThrow(() -> new BusinessException(AddressErrorCode.ADDRESS_NOT_FOUND));

        if (!address.getUser().getUsername().equals(username)) {
            throw new BusinessException(AddressErrorCode.ADDRESS_ACCESS_DENIED);
        }
        return address;
    }
}