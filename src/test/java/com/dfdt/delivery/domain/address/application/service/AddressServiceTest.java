package com.dfdt.delivery.domain.address.application.service;

import com.dfdt.delivery.common.exception.BusinessException;
import com.dfdt.delivery.domain.address.domain.entity.Address;
import com.dfdt.delivery.domain.address.domain.exception.error.enums.AddressErrorCode;
import com.dfdt.delivery.domain.address.domain.repository.AddressRepository;
import com.dfdt.delivery.domain.address.presentation.dto.AddressRequest;
import com.dfdt.delivery.domain.address.presentation.dto.AddressResponse;
import com.dfdt.delivery.domain.region.domain.entity.Region;
import com.dfdt.delivery.domain.region.domain.repository.RegionRepository;
import com.dfdt.delivery.domain.user.domain.entity.User;
import com.dfdt.delivery.domain.user.domain.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AddressService 단위 테스트")
class AddressServiceTest {

    @InjectMocks
    private AddressService addressService;

    @Mock
    private AddressRepository addressRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RegionRepository regionRepository;

    private User user;
    private Region region;
    private final String username = "testuser";
    private final UUID regionId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        user = User.builder().username(username).name("테스터").build();
        region = Region.builder().regionId(regionId).name("광화문").build();
    }

    @Nested
    @DisplayName("배송지 등록 테스트")
    class CreateAddress {

        @Test
        @DisplayName("성공: 새로운 배송지를 등록한다.")
        void createAddress_Success() {
            // given
            AddressRequest.Create request = AddressRequest.Create.builder()
                    .regionId(regionId)
                    .addressLine1("서울시 종로구")
                    .isDefault(false)
                    .build();

            given(userRepository.findByUsername(username)).willReturn(Optional.of(user));
            given(regionRepository.findById(regionId)).willReturn(Optional.of(region));

            Address savedAddress = Address.builder()
                    .user(user).region(region).addressLine1(request.getAddressLine1()).isDefault(false)
                    .build();
            ReflectionTestUtils.setField(savedAddress, "addressId", UUID.randomUUID());
            given(addressRepository.save(any(Address.class))).willReturn(savedAddress);

            // when
            AddressResponse response = addressService.createAddress(username, request);

            // then
            assertThat(response.getAddressLine1()).isEqualTo(request.getAddressLine1());
            verify(addressRepository, times(1)).save(any(Address.class));
            verify(addressRepository, never()).findByUserAndIsDefaultTrueAndSoftDeleteAuditDeletedAtIsNull(any());
        }

        @Test
        @DisplayName("성공: 기본 배송지로 등록 시 기존 기본 배송지는 일반 배송지로 변경된다.")
        void createAddress_WithDefault_Success() {
            // given
            AddressRequest.Create request = AddressRequest.Create.builder()
                    .regionId(regionId)
                    .isDefault(true)
                    .build();

            Address existingDefault = Address.builder().user(user).isDefault(true).build();

            given(userRepository.findByUsername(username)).willReturn(Optional.of(user));
            given(regionRepository.findById(regionId)).willReturn(Optional.of(region));
            given(addressRepository.findByUserAndIsDefaultTrueAndSoftDeleteAuditDeletedAtIsNull(user))
                    .willReturn(Optional.of(existingDefault));

            Address savedAddress = Address.builder().user(user).region(region).isDefault(true).build();
            ReflectionTestUtils.setField(savedAddress, "addressId", UUID.randomUUID());
            given(addressRepository.save(any(Address.class))).willReturn(savedAddress);

            // when
            addressService.createAddress(username, request);

            // then
            assertThat(existingDefault.getIsDefault()).isFalse();
            verify(addressRepository, times(1)).save(any(Address.class));
        }
    }

    @Nested
    @DisplayName("배송지 조회 및 수정 테스트")
    class UpdateAndDeleteAddress {

        private final UUID addressId = UUID.randomUUID();
        private Address address;

        @BeforeEach
        void setUp() {
            address = Address.builder()
                    .user(user)
                    .region(region)
                    .addressLine1("원래 주소")
                    .isDefault(false)
                    .build();
            ReflectionTestUtils.setField(address, "addressId", addressId);
        }

        @Test
        @DisplayName("실패: 소유자가 아닌 사용자가 수정/삭제를 시도하면 예외가 발생한다.")
        void validateOwner_Fail() {
            // given
            String otherUser = "other";
            given(addressRepository.findByAddressIdAndSoftDeleteAuditDeletedAtIsNull(addressId))
                    .willReturn(Optional.of(address));

            // when & then
            assertThatThrownBy(() -> addressService.deleteAddress(otherUser, addressId))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining(AddressErrorCode.ADDRESS_ACCESS_DENIED.getMessage());
        }

        @Test
        @DisplayName("성공: 배송지를 수정한다.")
        void updateAddress_Success() {
            // given
            AddressRequest.Update request = AddressRequest.Update.builder()
                    .addressLine1("수정된 주소")
                    .isDefault(true)
                    .build();

            given(addressRepository.findByAddressIdAndSoftDeleteAuditDeletedAtIsNull(addressId))
                    .willReturn(Optional.of(address));
            given(addressRepository.findByUserAndIsDefaultTrueAndSoftDeleteAuditDeletedAtIsNull(user))
                    .willReturn(Optional.empty());

            // when
            AddressResponse response = addressService.updateAddress(username, addressId, request);

            // then
            assertThat(response.getAddressLine1()).isEqualTo("수정된 주소");
            assertThat(address.getIsDefault()).isTrue();
        }

        @Test
        @DisplayName("성공: 배송지를 삭제(Soft Delete)한다.")
        void deleteAddress_Success() {
            // given
            given(addressRepository.findByAddressIdAndSoftDeleteAuditDeletedAtIsNull(addressId))
                    .willReturn(Optional.of(address));

            // when
            addressService.deleteAddress(username, addressId);

            // then
            assertThat(address.getSoftDeleteAudit()).isNotNull();
            assertThat(address.getSoftDeleteAudit().isDeleted()).isTrue();
        }
    }
}