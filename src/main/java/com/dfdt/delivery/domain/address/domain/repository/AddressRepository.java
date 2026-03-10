package com.dfdt.delivery.domain.address.domain.repository;

import com.dfdt.delivery.domain.address.domain.entity.Address;
import com.dfdt.delivery.domain.user.domain.entity.User;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AddressRepository {
    Address save(Address address);
    Optional<Address> findById(UUID id);
    List<Address> findByUserAndSoftDeleteAuditDeletedAtIsNull(User user);
    Optional<Address> findByUserAndIsDefaultTrueAndSoftDeleteAuditDeletedAtIsNull(User user);
    Optional<Address> findByAddressIdAndSoftDeleteAuditDeletedAtIsNull(UUID addressId);
}