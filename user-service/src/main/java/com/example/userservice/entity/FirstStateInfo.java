package com.example.userservice.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.ColumnDefault;

@Data
@Entity
@Table(name = "FIRST_STATE_INFO")
public class FirstStateInfo {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "first_state_id")
    private Long id;

    @Column(nullable = false)
    private String deliveryName;
    @Column(nullable = false)
    private String deliveryPhoneNumber;
    @Column(nullable = false)
    private String curState;

    @OneToOne(mappedBy = "firstStateInfo")
    private QRinfo qRinfo;
}
