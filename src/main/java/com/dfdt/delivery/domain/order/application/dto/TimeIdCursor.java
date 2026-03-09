package com.dfdt.delivery.domain.order.application.dto;

import com.dfdt.delivery.domain.order.domain.entity.Order;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.UUID;

@Getter
@Setter
@RequiredArgsConstructor
@AllArgsConstructor
public class TimeIdCursor {
    private OffsetDateTime time;
    private UUID id;

    public TimeIdCursor(Order order)
    {
        this.time = order.getCreatedAudit().getCreatedAt();
        this.id = order.getOrderId();
    }

    public TimeIdCursor(String cursorString) {
        byte[] decodedBytes = Base64.getDecoder().decode(cursorString.getBytes());
        String decodedBytesString = new String(decodedBytes);
        String[] split = decodedBytesString.split(",");
        this.id = UUID.fromString(split[0]);
        this.time = OffsetDateTime.parse(split[1]);
    }
    public String getCursorString()
    {
        try {
            String s = this.id + "," + this.time;
            byte[] base64EncodedBytes = Base64.getEncoder().encode(s.getBytes());
            return new String(base64EncodedBytes);
        } catch (Exception e) {
            return null;
        }

    }
}
