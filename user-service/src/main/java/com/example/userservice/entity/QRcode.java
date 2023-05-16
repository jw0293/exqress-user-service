package com.example.deliveryservice.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table
public class QRcode {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "qrcode_id")
    private Long id;

    @Column(nullable = false)
    private String qrId;

    @Column(nullable = false)
    private String receiverName;

    @Column(nullable = false)
    private String productName;

    @Embedded
    private Address address;

    @OneToOne(mappedBy = "qRcode")
    private DeliveryItem deliveryItem;
}
