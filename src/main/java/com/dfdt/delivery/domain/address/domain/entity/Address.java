package com.dfdt.delivery.domain.address.domain.entity;

import com.dfdt.delivery.common.infrastructure.persistence.embedded.CreateAudit;
import com.dfdt.delivery.common.infrastructure.persistence.embedded.SoftDeleteAudit;
import com.dfdt.delivery.common.infrastructure.persistence.embedded.UpdateAudit;
import com.dfdt.delivery.domain.region.domain.entity.Region;
import com.dfdt.delivery.domain.user.domain.entity.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.UUID;

@Entity
@Table(
        name = "p_address",
        indexes = {
                @Index(name = "IDX_address_username", columnList = "username"),
                @Index(name = "IDX_address_username_default", columnList = "username, is_default"),
                @Index(name = "IDX_address_region_id", columnList = "region_id")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "address_id", updatable = false, nullable = false)
    private UUID addressId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "username", referencedColumnName = "username", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "region_id", nullable = false)
    private Region region;

    @Column(name = "address_name", length = 50)
    private String addressName;

    @NotBlank
    @Column(name = "address_line1", length = 255, nullable = false)
    private String addressLine1;

    @Column(name = "address_line2", length = 255)
    private String addressLine2;

    @Column(name = "receiver_name", length = 50)
    private String receiverName;

    @Column(name = "receiver_phone", length = 20)
    @Pattern(regexp = "^(0\\d{1,2})-?\\d{3,4}-?\\d{4}$")
    private String receiverPhone;

    @Column(name = "is_default", nullable = false)
    private Boolean isDefault = false;

    @Column(name = "delivery_memo", length = 255)
    private String deliveryMemo;

    @Embedded
    private CreateAudit createAudit;

    @Embedded
    private UpdateAudit updateAudit;

    @Embedded
    private SoftDeleteAudit softDeleteAudit;

    @Builder
    public Address(User user, Region region, String addressName, String addressLine1,
                   String addressLine2, String receiverName, String receiverPhone,
                   Boolean isDefault, String deliveryMemo) {
        this.user = user;
        this.region = region;
        this.addressName = addressName;
        this.addressLine1 = addressLine1;
        this.addressLine2 = addressLine2;
        this.receiverName = receiverName;
        this.receiverPhone = receiverPhone;
        this.isDefault = (isDefault != null) ? isDefault : false;
        this.deliveryMemo = deliveryMemo;

        this.createAudit = CreateAudit.now(user != null ? user.getUsername() : "system");
        this.updateAudit = UpdateAudit.empty();
        this.softDeleteAudit = SoftDeleteAudit.active();
    }

    /**
     * 기본 배송지 상태 변경 로직
     * 서비스 레이어에서 다른 주소들을 false로 만들 때 이 메서드를 사용.
     */
    public void changeDefaultStatus(boolean isDefault) {
        this.isDefault = isDefault;
    }

    /**
     * 주소 정보 업데이트 로직
     */
    public void updateAddressInfo(Region region, String addressName, String addressLine1,
                                  String addressLine2, String receiverName,
                                  String receiverPhone, String deliveryMemo) {
        if (region != null) this.region = region;
        if (addressName != null) this.addressName = addressName;
        if (addressLine1 != null && !addressLine1.isBlank()) this.addressLine1 = addressLine1;
        if (addressLine2 != null) this.addressLine2 = addressLine2;
        if (receiverName != null) this.receiverName = receiverName;
        if (receiverPhone != null) this.receiverPhone = receiverPhone;
        if (deliveryMemo != null) this.deliveryMemo = deliveryMemo;
    }

    /**
     * Soft Delete 로직
     */
    public void deleteAddress(String deleteBy) {
        if (this.softDeleteAudit == null) {
            this.softDeleteAudit = SoftDeleteAudit.active();
        }
        this.softDeleteAudit.softDelete(deleteBy);
    }
}