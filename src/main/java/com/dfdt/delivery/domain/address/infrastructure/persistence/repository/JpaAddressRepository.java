package com.dfdt.delivery.domain.address.infrastructure.persistence.repository;

import com.dfdt.delivery.domain.address.domain.entity.Address;
import com.dfdt.delivery.domain.address.domain.repository.AddressRepository;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface JpaAddressRepository extends JpaRepository<Address, UUID>, AddressRepository {
}