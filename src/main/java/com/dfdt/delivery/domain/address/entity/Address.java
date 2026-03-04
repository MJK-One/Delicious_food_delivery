package com.dfdt.delivery.domain.address.entity;

import com.dfdt.delivery.common.Entity.BaseAuditSoftDeleteEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;

import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "p_address")
public class Address extends BaseAuditSoftDeleteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "address_id")
    private UUID addressId;

    @Column(length = 50)
    private String addressName;

    @NotBlank
    @Column(length = 255, nullable = false)
    private String addressLine1;

    @Column(length = 255)
    private String addressLine2;

    @Column(length = 50)
    private String receiverName;

    @Column(length = 20)
    @Pattern(regexp = "^(0\\d{1,2})-?\\d{3,4}-?\\d{4}$")
    private String receiverPhone;

    @Column(nullable = false)
    private Boolean isDefault = true;

    @Column(length = 255)
    private String deliveryMemo;
}